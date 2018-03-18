package com.demo.reservation.flow;

import com.demo.domain.Room;
import com.demo.exceptions.NotFoundException;
import com.demo.persistance.RoomRepository;
import com.demo.reservation.flow.forms.ReservationFlowForms;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import javax.persistence.EntityNotFoundException;
import java.util.Optional;

@Controller
@SessionAttributes("reservationFlowForms")
public class ReservationController {

    private RoomRepository roomRepository;

    public ReservationController(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Since {@code reservationFlowForms} is used in the {@code SessionAttributes} on the controller level, it informs
     * spring to treat our {@code ReservationFlowForms} as session scoped. This method will be invoked the very first
     * HTTP request to populate the session with the new object.
     *
     * <p>Subsequent HTTP requests will go directly to the handler and the statement {@code @ModelAttribute("reservationFlowForms")}
     * will grab the object directly from the session.
     */
    @ModelAttribute("reservationFlowForms")
    public ReservationFlowForms getReservationFlowForms() {
        System.out.println("getReservationFlowForms");
        return new ReservationFlowForms();
    }

    @GetMapping("/reservation")
    public String getDateForm(@RequestParam(value = "roomId") Long roomId,
                              @ModelAttribute("reservationFlowForms") ReservationFlowForms reservationFlowForms) throws NotFoundException {
        System.out.println("getDateForm: roomId=" + roomId + ", reservationFlowForms=" + reservationFlowForms);
        System.out.println(roomId);
        System.out.println();

        Optional<Room> maybeRoom = roomRepository.findById(roomId);
        if (!maybeRoom.isPresent()) {
            throw new NotFoundException();
        }

        return "reservation/dates";
    }
}
