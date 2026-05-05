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

    // Test 1: Null rezervace -> IllegalArgumentException
    // EC: reservation == null (nevalidní)
    @Test
    void validateReservation_nullReservation_throwsException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(null, 10)
        );
        assertEquals("Reservation cannot be null", ex.getMessage());
    }

    // Test 2: Null roomId -> IllegalArgumentException
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

    // Test 3: start >= end (start == end) -> IllegalArgumentException
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

    // Test 4: startMinute % 15 != 0 -> IllegalArgumentException
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

    // Test 5: endMinute % 15 != 0 -> IllegalArgumentException
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

    // Test 6: people <= 0 -> IllegalArgumentException
    // EC1: people ≤ 0 (nevalidní počet osob)
    // BVA: mezní hodnota 0
    @Test
    void validateReservation_zeroPeople_throwsException() {
        Reservation r = validReservation();
        r.setPeople(0);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("People must be greater than zero", ex.getMessage());
    }

    // Test 7: people > roomCapacity -> IllegalArgumentException
    // EC4: people > roomCapacity (překročení kapacity)
    // BVA: roomCapacity + 1 (první nevalidní hodnota nad kapacitou)
    @Test
    void validateReservation_peopleExceedsCapacity_throwsException() {
        Reservation r = validReservation();
        r.setPeople(9); // kapacita je 8

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 8)
        );
        assertEquals("Capacity exceeded", ex.getMessage());
    }

    // Test 8: rezervace o víkendu (sobota) -> IllegalArgumentException
    // EC2 dayOfWeek: SATURDAY (nevalidní)
    @Test
    void validateReservation_weekendReservation_throwsException() {
        Reservation r = validReservation();
        // 14.3.2026 je sobota
        r.setStart(LocalDateTime.of(2026, 3, 14, 10, 0));
        r.setEnd(LocalDateTime.of(2026, 3, 14, 11, 0));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Weekend reservations not allowed", ex.getMessage());
    }

    // Test 9: délka rezervace 15 minut -> IllegalArgumentException
    // EC1 duration: < 30 minut (příliš krátká)
    // BVA: nejbližší zarovnaná hodnota pod 30 min = 15 minut
    @Test
    void validateReservation_durationTooShort_throwsException() {
        Reservation r = validReservation();
        // 10:00–10:15 = 15 minut, zarovnané na 15minutový interval, ale kratší než 30 minut
        r.setStart(LocalDateTime.of(2026, 3, 16, 10, 0));
        r.setEnd(LocalDateTime.of(2026, 3, 16, 10, 15));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Too short reservation", ex.getMessage());
    }

    // Test 10: délka rezervace 255 minut -> IllegalArgumentException
    // EC3 duration: > 240 minut (příliš dlouhá)
    // BVA: nejbližší zarovnaná hodnota nad 240 min = 255 minut (08:00–12:15)
    @Test
    void validateReservation_durationTooLong_throwsException() {
        Reservation r = validReservation();
        // 08:00–12:15 = 255 minut, zarovnané na 15minutový interval, ale delší než 240 minut
        r.setStart(LocalDateTime.of(2026, 3, 16, 8, 0));
        r.setEnd(LocalDateTime.of(2026, 3, 16, 12, 15));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Too long reservation", ex.getMessage());
    }

    // Test 11: začátek rezervace před 08:00 -> IllegalArgumentException
    // EC2 workingHours: start < 08:00 (nevalidní)
    // BVA: 07:45 (nejbližší zarovnaná hodnota před hranicí 08:00)
    @Test
    void validateReservation_startBeforeWorkingHours_throwsException() {
        Reservation r = validReservation();
        // 07:45 – 08:45 = 60 minut, zarovnáno, ale začátek je před pracovní dobou
        r.setStart(LocalDateTime.of(2026, 3, 16, 7, 45));
        r.setEnd(LocalDateTime.of(2026, 3, 16, 8, 45));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Outside working hours", ex.getMessage());
    }

    // Test 12: konec rezervace po 18:00 -> IllegalArgumentException
    // EC3 workingHours: end > 18:00 (nevalidní)
    // BVA: 18:15 (nejbližší zarovnaná hodnota za hranicí 18:00)
    @Test
    void validateReservation_endAfterWorkingHours_throwsException() {
        Reservation r = validReservation();
        // 17:00 – 18:15 = 75 minut, zarovnáno, ale konec je po pracovní době
        r.setStart(LocalDateTime.of(2026, 3, 16, 17, 0));
        r.setEnd(LocalDateTime.of(2026, 3, 16, 18, 15));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.validateReservation(r, 10)
        );
        assertEquals("Outside working hours", ex.getMessage());
    }

    // Test 13: zcela validní rezervace -> vrátí true
    // EC: people = 2 (1–4 -> CONFIRMED), vše ostatní v pořádku
    // BVA: start = 08:00 (minimální platná hodnota), end = 09:00
    @Test
    void validateReservation_validReservation_returnsTrue() {
        Reservation r = new Reservation(
                null,
                1L,
                2,                                              // EC2: 1–4 osob
                LocalDateTime.of(2026, 3, 16, 8, 0),   // BVA: přesně 08:00
                LocalDateTime.of(2026, 3, 16, 9, 0),   // 60 minut, zarovnáno
                ReservationStatus.CONFIRMED
        );

        boolean result = service.validateReservation(r, 10);

        assertTrue(result);
    }
}