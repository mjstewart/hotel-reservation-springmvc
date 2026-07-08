package com.demo.reservation.flow.controller;

import com.demo.TimeProvider;
import com.demo.domain.Guest;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.UUID;

import static com.demo.GlobalErrorMatchers.globalErrorMatchers;
import static com.demo.reservation.flow.helpers.FlowMatchers.modelHasActiveFlowStep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class GuestFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // Flow step 2 - guests


    @Test
    public void getGuestForm_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        mockMvc.perform(get("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests));
    }


    @Test
    public void fromGuestBackToDates_GoBackToDateView_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        Long roomId = reservationFlow.getReservation().getRoom().getId();

        mockMvc.perform(post("/reservation/guests")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation?roomId=" + roomId))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.flashHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_BeanValidationError_NullFields() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(new Guest()));

        mockMvc.perform(post("/reservation/guests")
                .param("addGuest", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode("guest", "firstName", "NotNull"))
                .andExpect(model().attributeHasFieldErrorCode("guest", "lastName", "NotNull"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_BeanValidationError_EmptyFields() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        Guest formGuest = new Guest("", "", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(2))
                .andExpect(model().attributeHasFieldErrorCode("guest", "firstName", "Size"))
                .andExpect(model().attributeHasFieldErrorCode("guest", "lastName", "Size"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_GuestExists_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        // case should be irrelevant in detecting duplicates
        reservationFlow.getReservation().addGuest(new Guest("JoHn", "sMITh", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

        Guest formGuest = new Guest("john", "smith", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "exists"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_GuestLimitExceeded_HasGlobalError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        // next guest added should be denied.
        reservationFlow.getReservation().getRoom().setBeds(1);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        ResultMatcher didNotAddGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(1))));

        Guest formGuest = new Guest("marie", "clarke", false);
        ResultMatcher didNotCreateNewGuestObject = model().attribute("guest", Matchers.is(formGuest));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", formGuest.getFirstName())
                .param("lastName", formGuest.getLastName())
                .param("child", formGuest.isChild() + ""))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guestLimitExceeded"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(didNotAddGuest)
                .andExpect(didNotCreateNewGuestObject)
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postAddGuest_Valid() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        reservationFlow.getReservation().getRoom().setBeds(1);

        ResultMatcher reservationContainsGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests",
                                Matchers.containsInAnyOrder(new Guest("marie", "clarke", false)))));

        ResultMatcher didCreateNewGuestObject = model().attribute("guest", Matchers.is(new Guest()));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("addGuest", "")
                .param("firstName", "marie")
                .param("lastName", "clarke")
                .param("child", "false"))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(didCreateNewGuestObject)
                .andExpect(reservationContainsGuest)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postRemoveGuest_GuestIdNotFound_ShouldHaveNoEffect() throws Exception {
        // No guest is in here so the supplied guest id wont be found.
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();

        ResultMatcher reservationContainsGuest = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.hasSize(0))));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("removeGuest", UUID.randomUUID().toString()))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(reservationContainsGuest)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    /**
     * Note that a new {@code Guest} must be in the model to bind to the guest form.
     */
    @Test
    public void postRemoveGuest_GuestIdFound_ShouldBeRemoved() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        reservationFlow.getReservation().getRoom().setBeds(2);

        Guest guestA = new Guest("john", "smith", false);
        Guest guestB = new Guest("nicole", "smith", false);

        reservationFlow.getReservation().addGuest(guestA);
        reservationFlow.getReservation().addGuest(guestB);

        assertThat(reservationFlow.getReservation().getGuests()).containsExactlyInAnyOrder(guestA, guestB);

        ResultMatcher hasExpectedGuests = model().attribute("reservationFlow",
                Matchers.hasProperty("reservation",
                        Matchers.hasProperty("guests", Matchers.containsInAnyOrder(guestB))));

        // remove guestA
        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow)
                .param("removeGuest", guestA.getTempId().toString()))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().hasNoErrors())
                .andExpect(model().attribute("guest", Matchers.is(new Guest())))
                .andExpect(hasExpectedGuests)
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postGuestToExtras_NoGuestExists_ExpectGlobalError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        reservationFlow.getReservation().clearGuests();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guests.noneExist"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postGuestToExtras_NoAdultExists_ExpectGlobalError() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        reservationFlow.getReservation().clearGuests();

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/guests"))
                .andExpect(model().errorCount(1))
                .andExpect(globalErrorMatchers().hasGlobalErrorCode("guest", "guests.noneExist"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Guests))
                .andExpect(model().attributeExists("reservationFlow"));
    }

    @Test
    public void postGuestToExtras_Valid() throws Exception {
        ReservationFlow reservationFlow = FlowStages.dateCompletedFlow();
        reservationFlow.getReservation().getRoom().setBeds(3);
        reservationFlow.getReservation().addGuest(new Guest("john", "smith", false));

        mockMvc.perform(post("/reservation/guests")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/extras"))
                .andExpect(model().hasNoErrors())
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Guests))
                .andExpect(FlowMatchers.flashHasCompletedFlowStep(ReservationFlow.Step.Guests))
                .andExpect(flash().attributeExists("reservationFlow"));
    }
}
