package cz.cvut.fel.ts1.reservationapp.repository;

import cz.cvut.fel.ts1.reservationapp.model.Reservation;

import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationRepository {
    boolean hasConflict(Long roomId, LocalDateTime start, LocalDateTime end);
    Reservation save(Reservation reservation);
    Optional<Reservation> findById(Long id);
}