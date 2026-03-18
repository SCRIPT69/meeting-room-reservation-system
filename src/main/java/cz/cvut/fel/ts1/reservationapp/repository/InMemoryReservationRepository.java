package cz.cvut.fel.ts1.reservationapp.repository;

import cz.cvut.fel.ts1.reservationapp.model.Reservation;
import cz.cvut.fel.ts1.reservationapp.model.ReservationStatus;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new HashMap<>();
    private long sequence = 1L;

    @Override
    public boolean hasConflict(Long roomId, LocalDateTime start, LocalDateTime end) {
        return reservations.values().stream()
                .filter(r -> r.getRoomId().equals(roomId))
                .filter(r -> r.getStatus() != ReservationStatus.CANCELLED)
                .anyMatch(r -> start.isBefore(r.getEnd()) && end.isAfter(r.getStart()));
    }

    @Override
    public Reservation save(Reservation reservation) {
        if (reservation.getId() == null) {
            reservation.setId(sequence++);
        }
        reservations.put(reservation.getId(), reservation);
        return reservation;
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(reservations.get(id));
    }
}