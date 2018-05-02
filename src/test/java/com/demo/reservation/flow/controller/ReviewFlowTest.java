package com.demo.reservation.flow.controller;

import com.demo.TimeProvider;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.ExtraRepository;
import com.demo.reservation.flow.ReservationController;
import com.demo.reservation.flow.forms.ReservationFlow;
import com.demo.reservation.flow.helpers.FlowMatchers;
import com.demo.reservation.flow.helpers.FlowStages;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class ReviewFlowTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // Flow step 5 - review

    /**
     * Getting the review form only requires the reservationFlow in the model as the contained
     * Reservation is now in its final state before payment.
     */
    @Test
    public void getReview_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = FlowStages.mealsCompletedFlow();

        mockMvc.perform(get("/reservation/review")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/review"))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(FlowMatchers.modelHasActiveFlowStep(ReservationFlow.Step.Review))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Review));
    }

    /**
     * Going back requires the model be transferred into flash attributes otherwise it will be lost
     * during the redirect.
     */
    @Test
    public void fromReviewBackToMealPlans_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = FlowStages.mealsCompletedFlow();

        mockMvc.perform(post("/reservation/review")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/meals"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Review))
                .andExpect(FlowMatchers.flashHasIncompleteFlowStep(ReservationFlow.Step.Review))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * Posting the review simply means user is happy and doesn't need to go back and edit so lets
     * go to the payment page.
     */
    @Test
    public void postReview() throws Exception {
        ReservationFlow reservationFlow = FlowStages.mealsCompletedFlow();

        mockMvc.perform(post("/reservation/review")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/payment"))
                .andExpect(flash().attributeExists("reservationFlow"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Review))
                .andExpect(FlowMatchers.flashHasCompletedFlowStep(ReservationFlow.Step.Review));
    }
}
