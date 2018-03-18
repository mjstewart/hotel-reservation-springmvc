package com.demo.reservation;

import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.ReservationController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoomRepository roomRepository;

    @Test
    public void getDateForm_ReturnsCorrectTemplate() throws Exception {
        mockMvc.perform(get("/reservation?roomId=5"))
                .andExpect(status().isOk())
                .andExpect(view().name("reservation/dates"));
    }

    @Test
    public void getDateForm_IllegalRoomIdType_400BadRequest() throws Exception {
        mockMvc.perform(get("/reservation?roomId=abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void getDateForm_RoomIdDoesNotExist_404NotFound() throws Exception {
        when(roomRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/reservation?roomId=34"))
                .andExpect(status().isNotFound());

        verify(roomRepository, times(1)).findById(anyLong());
    }


//    @Test
//    public void getDateForm_EntryPoint_PopulatesModelCorrectly() throws Exception {
//        mockMvc.perform(get("/reservation?roomId=5"))
//                .andExpect(model().attributeExists("reservationFlowForms"))
//                .andExpect(model().attributeExists("dateForm"));
//    }
//
//    @Test
//    public void getDateForm_EntryPoint_PopulatesModelCorrectly() throws Exception {
//        mockMvc.perform(get("/reservation"))
//                .andExpect(model().attributeExists("reservationFlowForms"))
//                .andExpect(model().attributeExists("dateForm"));
//    }

}