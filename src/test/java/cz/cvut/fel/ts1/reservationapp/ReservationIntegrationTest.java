package cz.cvut.fel.ts1.reservationapp;
import cz.cvut.fel.ts1.reservationapp.model.Reservation;
import cz.cvut.fel.ts1.reservationapp.model.ReservationStatus;
import cz.cvut.fel.ts1.reservationapp.repository.ReservationRepository;
import cz.cvut.fel.ts1.reservationapp.repository.RoomRepository;
import cz.cvut.fel.ts1.reservationapp.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ReservationIntegrationTest {
    @Autowired
    ReservationService reservationService;

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    ReservationRepository reservationRepository;

    public Reservation reservationForRoom(Long roomId, int people, LocalDateTime start, LocalDateTime end){
        return new Reservation(null, roomId, people, start, end, null);
    }

    @Test
    public void thisShouldLoadSpringContextAndRepositories(){
        assertNotNull(reservationService);
        assertNotNull(roomRepository);
        assertNotNull(reservationRepository);
    }

    @Test
    public void thisShouldCreateConfirmedReservation_forNormalMeeting(){
        Reservation guestReservation = reservationForRoom(1L,
                4,                 LocalDateTime.of(2026, 5, 11, 9, 0),
                LocalDateTime.of(2026, 5, 11, 10, 0));

        Reservation savedReservation = reservationService.createReservation(guestReservation);

        assertNotNull(savedReservation.getId());
        assertEquals(ReservationStatus.CONFIRMED, savedReservation.getStatus());
    }

    @Test
    public void thisShouldCreatePendingReservation_ifMoreThan4People(){
        Reservation guestReservation = reservationForRoom(
                2L, 6,
                LocalDateTime.of(2026, 5, 11, 10, 0),
                LocalDateTime.of(2026, 5, 11, 11, 0)
        );

        Reservation savedReservation = reservationService.createReservation(guestReservation);

        assertNotNull(savedReservation.getId());
        assertEquals(ReservationStatus.PENDING, savedReservation.getStatus());
    }

    @Test
    public void thisShouldKeepTheReservationInMemoryRepository(){
        Reservation guestReservation = reservationForRoom(
                1L, 3,
                LocalDateTime.of(2026, 5, 12, 9, 0),
                LocalDateTime.of(2026, 5, 12, 10, 0)
        );

        Reservation savedReservation = reservationService.createReservation(guestReservation);
        Reservation loadedReservation = reservationRepository.findById(savedReservation.getId()).orElseThrow();

        assertEquals(savedReservation.getId(), loadedReservation.getId());
        assertEquals(savedReservation.getRoomId(), loadedReservation.getRoomId());
        assertEquals(savedReservation.getPeople(), loadedReservation.getPeople());
        assertEquals(savedReservation.getStatus(), loadedReservation.getStatus());
    }

    @Test
    public void thisShouldThrowExceptionMsgIfConflictingReservationInSameRoom(){
        reservationService.createReservation(reservationForRoom(
                2L, 4,
                LocalDateTime.of(2026, 5, 12, 13, 0),
                LocalDateTime.of(2026, 5, 12, 14, 0)
        ));

        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class, ()-> reservationService.createReservation(reservationForRoom(
                        2L, 2, LocalDateTime.of(2026,5,12,13,30),
                        LocalDateTime.of(2026,5,12,14,30)
                ))
        );

        assertEquals("Reservation conflict", illegalArgumentException.getMessage());
    }

    @Test
    public void thisShouldAllowSameTimeInDifferentRooms(){
        Reservation firstGuestReservation  = reservationService.createReservation(reservationForRoom(
                1L, 2,
                LocalDateTime.of(2026, 5, 13, 10, 0),
                LocalDateTime.of(2026, 5, 13, 11, 0)
        ));

        Reservation secondGuestReservation = reservationService.createReservation(reservationForRoom(
                2L, 2,
                LocalDateTime.of(2026, 5, 13, 10, 0),
                LocalDateTime.of(2026, 5, 13, 11, 0)
        ));

        assertNotNull(firstGuestReservation.getId());
        assertNotNull(secondGuestReservation.getId());
        assertNotEquals(firstGuestReservation.getId(), secondGuestReservation.getId());
    }


    @Test
    public void thisShouldCancelReservation_butThenAllowAnotherExactSameReservation(){
        Reservation guestReservation = reservationService.createReservation(reservationForRoom(
                1L, 3,
                LocalDateTime.of(2026, 5, 14, 15, 0),
                LocalDateTime.of(2026, 5, 14, 16, 0)
        ));

        Reservation cancelled_guest_reservation = reservationService.cancelReservation(guestReservation.getId());
        assertEquals(ReservationStatus.CANCELLED, cancelled_guest_reservation.getStatus());

        Reservation thatSameReservation = reservationService.createReservation(reservationForRoom(
                1L, 3,
                LocalDateTime.of(2026, 5, 14, 15, 0),
                LocalDateTime.of(2026, 5, 14, 16, 0)
        ));

        assertNotNull(thatSameReservation.getId());


    }

    @Test
    public void thisShouldRejectReservationOutsideWorkingTime(){
        IllegalArgumentException illegalArgumentException = assertThrows(
                IllegalArgumentException.class, ()-> reservationService.createReservation(
                     reservationForRoom(   1L, 2,
                             LocalDateTime.of(2026, 5, 15, 7, 45),
                             LocalDateTime.of(2026, 5, 15, 8, 30))
                ));

        assertEquals("Outside working hours", illegalArgumentException.getMessage());

    }




}
