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


    @Test
    public void fromGeneralExtrasBackToGuests_RedirectsCorrectView() throws Exception {
        ReservationFlow reservationFlow = FlowStages.guestCompletedFlow();

        mockMvc.perform(post("/reservation/extras")
                .param("back", "")
                .sessionAttr("reservationFlow", reservationFlow))
                .andExpect(view().name("redirect:/reservation/guests"))
                .andExpect(flash().attributeExists("reservationFlow"));
    }


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
