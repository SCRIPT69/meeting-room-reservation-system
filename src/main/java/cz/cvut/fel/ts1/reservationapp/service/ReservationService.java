package cz.cvut.fel.ts1.reservationapp.service;

import cz.cvut.fel.ts1.reservationapp.model.Reservation;
import cz.cvut.fel.ts1.reservationapp.model.ReservationStatus;
import cz.cvut.fel.ts1.reservationapp.model.Room;
import cz.cvut.fel.ts1.reservationapp.repository.ReservationRepository;
import cz.cvut.fel.ts1.reservationapp.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;

@Service
public class ReservationService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;

    public ReservationService(RoomRepository roomRepository, ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
    }

    public Reservation createReservation(Reservation r) {
        if (r == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        Room room = roomRepository.findById(r.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));

        validateReservation(r, room.getCapacity());

        if (reservationRepository.hasConflict(r.getRoomId(), r.getStart(), r.getEnd())) {
            throw new IllegalArgumentException("Reservation conflict");
        }

        Reservation reservationToSave = new Reservation(
                null,
                r.getRoomId(),
                r.getPeople(),
                r.getStart(),
                r.getEnd(),
                ReservationStatus.CREATED
        );

        return reservationRepository.save(reservationToSave);
    }

    public Reservation cancelReservation(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Reservation id cannot be null");
        }

        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalArgumentException("Reservation already cancelled");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        return reservationRepository.save(reservation);
    }

    public boolean validateReservation(Reservation r, int roomCapacity) {
        if (r == null) {
            throw new IllegalArgumentException("Reservation cannot be null");
        }

        if (r.getRoomId() == null) {
            throw new IllegalArgumentException("Room id cannot be null");
        }

        if (r.getStart() == null || r.getEnd() == null) {
            throw new IllegalArgumentException("Start and end must not be null");
        }

        if (!r.getStart().isBefore(r.getEnd())) {
            throw new IllegalArgumentException("Start must be before end");
        }

        if (r.getPeople() <= 0) {
            throw new IllegalArgumentException("People must be greater than zero");
        }

        if (r.getPeople() > roomCapacity) {
            throw new IllegalArgumentException("Capacity exceeded");
        }

        if (r.getStart().getDayOfWeek() == DayOfWeek.SATURDAY ||
                r.getStart().getDayOfWeek() == DayOfWeek.SUNDAY) {
            throw new IllegalArgumentException("Weekend reservations not allowed");
        }

        Duration duration = Duration.between(r.getStart(), r.getEnd());
        if (duration.toMinutes() < 30) {
            throw new IllegalArgumentException("Too short reservation");
        }

        LocalTime openingTime = LocalTime.of(8, 0);
        LocalTime closingTime = LocalTime.of(18, 0);

        if (r.getStart().toLocalTime().isBefore(openingTime) ||
                r.getEnd().toLocalTime().isAfter(closingTime)) {
            throw new IllegalArgumentException("Outside working hours");
        }

        return true;
    }
}