package com.demo.reservation.flow.controller;

import com.demo.TimeProvider;
import com.demo.domain.Extra;
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

import java.math.BigDecimal;
import java.util.List;

import static com.demo.reservation.flow.helpers.FlowMatchers.modelHasActiveFlowStep;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class GeneralExtraFlowTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @MockBean
    private ExtraRepository extraRepository;

    @MockBean
    private TimeProvider timeProvider;

    // Flow step 3 - extras

    /**
     * Assert that the Model contains the expected general extras fetched from repository and it
     * contains the reservationFlow from the session. Also ensure the reservationFlow step is on
     * Extras.
     */
    @Test
    public void getGeneralExtrasForm_HasValidStartingState() throws Exception {
        ReservationFlow reservationFlow = FlowStages.guestCompletedFlow();

        List<Extra> generalExtras = List.of(
                new Extra("foxtel", BigDecimal.valueOf(3.94), Extra.Type.Premium, Extra.Category.General)
        );
        when(extraRepository.findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.General)))
                .thenReturn(generalExtras);

        mockMvc.perform(get("/reservation/extras")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("reservation/extras"))
                .andExpect(model().attribute("extras", Matchers.equalTo(generalExtras)))
                .andExpect(model().attributeExists("reservationFlow"))
                .andExpect(modelHasActiveFlowStep(ReservationFlow.Step.Extras))
                .andExpect(FlowMatchers.modelHasIncompleteFlowStep(ReservationFlow.Step.Extras));

        verify(extraRepository, times(1))
                .findAllByTypeAndCategory(any(Extra.Type.class), eq(Extra.Category.General));
        verifyNoMoreInteractions(extraRepository);
    }

    /**
     * Going back requires the model be transferred into flash attributes otherwise it will be lost
     * during the redirect.
     */
    @Test
    public void fromGeneralExtrasBackToGuests_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = FlowStages.guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

    /**
     * When the selected general extras are submitted, check the flow step is completed for Extras and
     * we redirect to the next meal view. flash attributes are needed so the Model state is not lost
     * across the redirect.
     * <p>
     * We cant test for checking extras are added to the reservation since all of that happens
     * using spring binding infrastructure so would need to make an integration test for that using
     * {@literal @SpringBootTest}.
     */
    @Test
    public void submitGeneralExtras_RedirectsToCorrectViewAndStep() throws Exception {
        ReservationFlow reservationFlow = FlowStages.guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/meals"))
                .andExpect(FlowMatchers.flashHasActiveFlowStep(ReservationFlow.Step.Extras))
                .andExpect(FlowMatchers.flashHasCompletedFlowStep(ReservationFlow.Step.Extras))
                .andExpect(flash().attributeExists("reservationFlow"));
    }

}
