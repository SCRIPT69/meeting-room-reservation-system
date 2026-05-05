package cz.cvut.fel.ts1.reservationapp.service;

import cz.cvut.fel.ts1.reservationapp.model.Reservation;
import cz.cvut.fel.ts1.reservationapp.model.ReservationStatus;
import cz.cvut.fel.ts1.reservationapp.repository.ReservationRepository;
import cz.cvut.fel.ts1.reservationapp.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import static org.mockito.Mockito.mock;

import static org.junit.jupiter.api.Assertions.*;

class ReservationServiceTest {
    private ReservationService service;

    // Pomocná metoda: vytvoří validní rezervaci (pondělí 10:00–11:00, 2 osoby)
    private Reservation validReservation() {
        // Pondělí 16.3.2026 10:00 - 11:00
        return new Reservation(
                null,
                1L,
                2,
                LocalDateTime.of(2026, 3, 16, 10, 0),
                LocalDateTime.of(2026, 3, 16, 11, 0),
                ReservationStatus.CONFIRMED
        );
    }

    @BeforeEach
    void setUp() {
        // validateReservation nepoužívá repository – mocky stačí jen pro konstruktor
        RoomRepository roomRepo = mock(RoomRepository.class);
        ReservationRepository reservationRepo = mock(ReservationRepository.class);
        service = new ReservationService(roomRepo, reservationRepo);
    }

    // Test 1: Null rezervace → IllegalArgumentException
    // EC: reservation == null (nevalidní)
    @Test
    void validateReservation_nullReservation_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(null, 10)
        );
        assertEquals("Reservation cannot be null", ex.getMessage());
    }

    // Test 2: Null roomId → IllegalArgumentException
    // EC: roomId == null (nevalidní)
    @Test
    void validateReservation_nullRoomId_throwsException() {
        Reservation r = validReservation();
        r.setRoomId(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Room id cannot be null", ex.getMessage());
    }

    // Test 3: start >= end (start == end) → IllegalArgumentException
    // EC: start = end (nevalidní interval)
    // BVA: mezní situace start == end
    @Test
    void validateReservation_startEqualsEnd_throwsException() {
        Reservation r = validReservation();
        LocalDateTime same = LocalDateTime.of(2026, 3, 16, 10, 0);
        r.setStart(same);
        r.setEnd(same);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Start must be before end", ex.getMessage());
    }

    // Test 4: startMinute % 15 != 0 → IllegalArgumentException
    // EC: startMinute % 15 != 0 (neplatný začátek)
    // BVA: minuta 16 (hraniční hodnota vedle platného 15)
    @Test
    void validateReservation_startNotAlignedTo15Minutes_throwsException() {
        Reservation r = validReservation();
        // 10:16 – neplatné zarovnání
        r.setStart(LocalDateTime.of(2026, 3, 16, 10, 16));
        r.setEnd(LocalDateTime.of(2026, 3, 16, 11, 0));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Reservation time must be aligned to 15-minute intervals", ex.getMessage());
    }

    // Test 5: endMinute % 15 != 0 → IllegalArgumentException
    // EC: endMinute % 15 != 0 (neplatný konec)
    // BVA: minuta 31 (hraniční hodnota vedle platného 30)
    @Test
    void validateReservation_endNotAlignedTo15Minutes_throwsException() {
        Reservation r = validReservation();
        // 11:31 – neplatné zarovnání konce
        r.setEnd(LocalDateTime.of(2026, 3, 16, 11, 31));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Reservation time must be aligned to 15-minute intervals", ex.getMessage());
    }
}