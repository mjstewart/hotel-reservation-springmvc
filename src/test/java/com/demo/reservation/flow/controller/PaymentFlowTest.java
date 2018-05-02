package com.demo.reservation.flow.controller;

import com.demo.reservation.flow.TestContextConfiguration;
import com.demo.TimeProvider;
import com.demo.domain.PendingPayment;
import com.demo.domain.Room;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.flow.helpers.FlowMatchers;
import com.demo.reservation.flow.helpers.FlowStages;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@Import(TestContextConfiguration.class)
@ActiveProfiles("test")
public class PaymentFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // Flow step 6 - payment

    /**
     * Assert that the Model contains a {@code PendingPayment} object ready to bind to the payment form.
     * The reservationFlow must still exist to ensure the Reservation is not lost.
     */
    @Test
    public void getPayment_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(get("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attribute("pendingPayment", Matchers.isA(PendingPayment.class)))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Redirects to review form when in the payment form. Note the flash attributes
     * used are so the Model is not lost between redirects.
     */
    @Test
    public void fromPaymentBackToReview() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/review"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.flashHasIncompleteFlowStep(ReservationFlow.Step.Payment))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * SessionStatus should be set to complete but not sure how to test this.
     */
    @Test
    public void cancelPayment_RedirectToHome() throws Exception {
        mockMvc.perform(post("/reservation/payment")
                .param("cancel", ""))
                .andExpect(view().name("redirect:/"));

    }

    /**
     * When posting an empty form, assert all expected bean validation errors occur.
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_EmptyForm_ExpectAllBeanValidationErrors() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(6))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardHolderName", "NotEmpty"))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "creditCardNumber", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "creditCardType", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cvv", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardExpiryYear", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardExpiryMonth", "NotNull"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid credit card type. All fields valid except for card type
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_creditCardType() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", "ERROR")
                .param("creditCardNumber", "1234567892")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "creditCardType", "typeMismatch"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid cardHolderName is not empty. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_cardHolderName() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "123")
                .param("cardHolderName", "")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardHolderName", "NotEmpty"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid creditCardNumber has letters. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_creditCardNumber_HasLetter() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234A67892")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "creditCardNumber", "Pattern"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid creditCardNumber - not 10 digits. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_creditCardNumber_NotTenDigits() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "123")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "creditCardNumber", "Pattern"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid cvv - has letter. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_cvv_HasLetter() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "12C")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cvv", "Pattern"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid cvv - has letter. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_cvv_NotThreeDigits() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "12452")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cvv", "Pattern"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid cardExpiryYear. All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_cardExpiryYear_NotFourDigits() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "18")
                .param("cardExpiryMonth", "JULY"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardExpiryYear", "typeMismatch"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * Check for invalid cardExpiryMonth. Must be the same as {@code Month} constants such as {@code Month.JULY}.
     * All fields valid except for cardHolderName
     * <p>
     * Fill the form with values that trigger the error conditions and assert the expected bean validations occurs
     * <p>
     * Important, the createdTime must always be included since this field is always provided (its
     * a hidden field in the form). Without this the credit card expiration fields cant be calculated
     * since there is no base date.
     */
    @Test
    public void postPayment_ValidationErrors_cardExpiryMonth_InvalidTypeFormat() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", "NOV"))
                .andExpect(view().name("reservation/payment"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(model().attributeExists("pendingPayment"))
                .andExpect(model().errorCount(1))
                .andExpect(model().attributeHasFieldErrorCode("pendingPayment", "cardExpiryMonth", "typeMismatch"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Payment))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Payment));
    }

    /**
     * All fields are valid. The {@code Reservation} should be saved to database and session state
     * cleared.
     */
    @Test
    public void postPayment_Valid() throws Exception {
        ReservationFlow reservationFlow = FlowStages.reviewCompletedFlow();

        mockMvc.perform(post("/reservation/payment")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("createdTime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .param("creditCardType", PendingPayment.CreditCardType.MasterCard.name())
                .param("creditCardNumber", "1234567892")
                .param("cvv", "123")
                .param("cardHolderName", "john smith")
                .param("cardExpiryYear", "2018")
                .param("cardExpiryMonth", Month.DECEMBER.name()))
                .andExpect(view().name("redirect:/reservation/completed"))
                .andExpect(flash().attributeCount(0))
                .andExpect(model().errorCount(0));

        verify(roomRepository, times(1)).save(any(Room.class));
        verifyNoMoreInteractions(roomRepository);
    }
}
