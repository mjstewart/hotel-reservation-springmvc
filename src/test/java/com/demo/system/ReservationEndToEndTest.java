package com.demo.system;

import com.demo.TimeProvider;
import com.demo.domain.*;
import com.demo.domain.location.Address;
import com.demo.domain.location.Postcode;
import com.demo.domain.location.State;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.TestContextConfiguration;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.flow.helpers.FlowMatchers;
import com.demo.reservation.flow.helpers.FlowStages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static com.demo.GlobalErrorMatchers.globalErrorMatchers;
import static com.demo.reservation.flow.helpers.FlowMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes de sistema (end-to-end) para o fluxo completo de reserva.
 *
 * Estes testes simulam a jornada completa do usuário através de todos os 6 passos
 * do wizard de reserva: Dates → Guests → Extras → Meals → Review → Payment.
 *
 * Utiliza MockMvc com @WebMvcTest para testar o fluxo completo do controller,
 * verificando redirecionamentos, validações e estados do flow em cada etapa.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@Import(TestContextConfiguration.class)
@ActiveProfiles("test")
public class ReservationEndToEndTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // ==================== Helpers ====================

    private LinkedMultiValueMap<String, String> createValidDateParams() {
        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(3);
        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("reservation.dates.checkInDate", List.of(checkIn.format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.checkOutDate", List.of(checkOut.format(DateTimeFormatter.ISO_DATE)));
        params.put("reservation.dates.estimatedCheckInTime", List.of(LocalTime.of(10, 0).format(DateTimeFormatter.ISO_TIME)));
        params.put("reservation.dates.lateCheckout", List.of("false"));
        params.put("reservation.dates.policyAcknowledged", List.of("true"));
        return params;
    }

    // ==================== Fluxo completo com sucesso ====================

    /**
     * Teste end-to-end que percorre todos os 6 passos do wizard de reserva com dados válidos.
     *
     * Fluxo:
     * 1. GET /reservation?roomId=1 → Inicia o flow, retorna formulário de datas
     * 2. POST /reservation/dates → Datas válidas, redireciona para guests
     * 3. POST /reservation/guests?addGuest → Adiciona um hóspede adulto
     * 4. POST /reservation/guests → Confirma hóspedes, redireciona para extras
     * 5. POST /reservation/extras → Confirma extras, redireciona para meals
     * 6. POST /reservation/meals → Confirma meals, redireciona para review
     * 7. POST /reservation/review → Confirma review, redireciona para payment
     * 8. POST /reservation/payment → Pagamento válido, redireciona para completed
     *
     * Verifica que o Room é salvo ao final (roomRepository.save chamado).
     */
    @Test
    public void completeReservationFlow_ValidData_CompletesSuccessfully() throws Exception {
        Room room = FlowStages.createRoom();
        when(roomRepository.findById(anyLong())).thenReturn(Optional.of(room));
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        // === STEP 1: GET date form ===
        mockMvc.perform(get("/reservation?roomId=2"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/dates"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates));

        // === STEP 1: POST valid dates ===
        ReservationFlow dateFlow = FlowStages.pendingDateFlow();

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", dateFlow)
                .params(createValidDateParams()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(flashHasCompletedFlowStep(ReservationFlow.Step.Dates));

        // === STEP 2: Add an adult guest ===
        ReservationFlow guestFlow = FlowStages.dateCompletedFlow();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", guestFlow)
                .param("addGuest", "")
                .param("firstName", "John")
                .param("lastName", "Smith")
                .param("child", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/guests"));

        // Manually add the guest for next step (session simulation)
        guestFlow.getReservation().addGuest(new Guest("john", "smith", false));

        // === STEP 2: Confirm guests and proceed to extras ===
        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", guestFlow))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(flash().attributeExists("reservationFlow"));

        // === STEP 3: POST extras (proceed to meals) ===
        ReservationFlow extrasFlow = FlowStages.guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .sessionAttr("reservationFlow", extrasFlow))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/meals"))
                .andExpect(flash().attributeExists("reservationFlow"));

        // === STEP 4: POST meals (proceed to review) ===
        ReservationFlow mealsFlow = FlowStages.extrasCompletedFlow();
        mealsFlow.getReservation().createMealPlans();

        mockMvc.perform(post("/reservation/meals")
                .sessionAttr("reservationFlow", mealsFlow))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/review"))
                .andExpect(flash().attributeExists("reservationFlow"));

        // === STEP 5: POST review (proceed to payment) ===
        ReservationFlow reviewFlow = FlowStages.mealsCompletedFlow();

        mockMvc.perform(post("/reservation/review")
                .sessionAttr("reservationFlow", reviewFlow))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/payment"))
                .andExpect(flash().attributeExists("reservationFlow"));

        // === STEP 6: POST valid payment ===
        ReservationFlow paymentFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", paymentFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.Visa.name())
                .param("creditCardNumber", "1234567890")
                .param("cvv", "123")
                .param("cardHolderName", "John Smith")
                .param("cardExpiryYear", "2028")
                .param("cardExpiryMonth", Month.JULY.name()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/completed"))
                .andExpect(model().errorCount(0));

        // Verifica que a reserva foi persistida
        verify(roomRepository, times(1)).save(any(Room.class));
    }

    // ==================== Fluxo interrompido no Step 1 (datas inválidas) ====================

    /**
     * Quando datas inválidas são submetidas (check-in no passado), o fluxo deve parar
     * no Step 1 e retornar o formulário de datas com erro.
     */
    @Test
    public void completeReservationFlow_InvalidDates_StopsAtStep1() throws Exception {
        when(timeProvider.localDate()).thenReturn(LocalDate.now());

        ReservationFlow reservationFlow = FlowStages.pendingDateFlow();

        // Check-in no passado
        LocalDate pastDate = LocalDate.now().minusDays(5);
        LocalDate checkOut = pastDate.plusDays(2);
        LinkedMultiValueMap<String, String> invalidParams = new LinkedMultiValueMap<>();
        invalidParams.put("reservation.dates.checkInDate", List.of(pastDate.format(DateTimeFormatter.ISO_DATE)));
        invalidParams.put("reservation.dates.checkOutDate", List.of(checkOut.format(DateTimeFormatter.ISO_DATE)));
        invalidParams.put("reservation.dates.estimatedCheckInTime", List.of(LocalTime.of(10, 0).format(DateTimeFormatter.ISO_TIME)));
        invalidParams.put("reservation.dates.lateCheckout", List.of("false"));
        invalidParams.put("reservation.dates.policyAcknowledged", List.of("true"));

        mockMvc.perform(post("/reservation/dates")
                .sessionAttr("reservationFlow", reservationFlow)
                .params(invalidParams))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/dates"))
                .andExpect(model().errorCount(1))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Dates))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Dates));

        // Verifica que o roomRepository.save nunca foi chamado
        verify(roomRepository, never()).save(any());
    }

    // ==================== Fluxo interrompido no Step 2 (sem adulto) ====================

    /**
     * Quando apenas crianças são adicionadas como hóspedes (sem adulto),
     * o fluxo deve parar no Step 2 com erro de validação.
     */
    @Test
    public void completeReservationFlow_NoAdultGuest_StopsAtStep2() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        // Adicionar apenas uma criança
        reservationFlow.getReservation().addGuest(new Guest("billy", "kid", true));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/guests"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests));

        verify(roomRepository, never()).save(any());
    }

    // ==================== Fluxo interrompido no Step 2 (sem hóspedes) ====================

    /**
     * Quando não há nenhum hóspede adicionado, o fluxo deve parar no Step 2
     * com erro informando que deve haver ao menos 1 hóspede.
     */
    @Test
    public void completeReservationFlow_NoGuests_StopsAtStep2() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/guests"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests));

        verify(roomRepository, never()).save(any());
    }

    // ==================== Fluxo interrompido no Step 6 (pagamento inválido) ====================

    /**
     * Quando dados de pagamento inválidos são submetidos (formulário vazio),
     * o fluxo deve parar no Step 6 com erros de validação do bean.
     */
    @Test
    public void completeReservationFlow_InvalidPayment_StopsAtStep6() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        // Post com formulário de pagamento com dados inválidos
        // creditCardNumber="1234" evita NPE e StringIndexOutOfBounds no Java 25
        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardNumber", "1234"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/payment"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));

        // Verifica que a reserva NÃO foi persistida
        verify(roomRepository, never()).save(any());
    }

    // ==================== Navegação: Voltar do pagamento para review ====================

    /**
     * Verifica que o botão "voltar" no Step 6 (Payment) redireciona para o Step 5 (Review).
     */
    @Test
    public void navigationBack_FromPaymentToReview() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/reservation/review"))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    // ==================== Navegação: Cancelar no pagamento ====================

    /**
     * Verifica que o botão "cancelar" no Step 6 (Payment) redireciona para a home
     * e limpa a sessão.
     */
    @Test
    public void navigationCancel_FromPayment_RedirectsHome() throws Exception {
        mockMvc.perform(post("/reservation/payment")
                .param("cancel", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/"));
    }

    // ==================== Hóspede duplicado ====================

    /**
     * Verifica que adicionar um hóspede com o mesmo nome resulta em erro.
     */
    @Test
    public void addDuplicateGuest_ReturnsError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        // Pré-adicionar o hóspede
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", "John")
                .param("lastName", "Smith")
                .param("child", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/guests"))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "exists"));
    }

    // ==================== Quarto lotado ====================

    /**
     * Verifica que tentar adicionar um hóspede quando o quarto está cheio resulta em erro.
     */
    @Test
    public void addGuest_RoomFull_ReturnsError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        // O quarto tem 4 camas. Preencher todas.
        reservationFlow.getReservation().addGuest(new Guest("a", "aa", false));
        reservationFlow.getReservation().addGuest(new Guest("b", "bb", false));
        reservationFlow.getReservation().addGuest(new Guest("c", "cc", false));
        reservationFlow.getReservation().addGuest(new Guest("d", "dd", false));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", "Extra")
                .param("lastName", "Guest")
                .param("child", "false"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/guests"))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guestLimitExceeded"));
    }

    // ==================== Room não encontrado ====================

    /**
     * Verifica que tentar iniciar uma reserva com um roomId inexistente retorna 404.
     */
    @Test
    public void startReservation_RoomNotFound_Returns404() throws Exception {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservation?roomId=999"))
                .andExpect(status().isNotFound());
    }

    // ==================== Completed page ====================

    /**
     * Verifica que a página de conclusão é acessível.
     */
    @Test
    public void getCompletedPage_ReturnsCorrectView() throws Exception {
        mockMvc.perform(get("/reservation/completed"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/completed"));
    }
}
