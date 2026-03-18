package cz.cvut.fel.ts1.reservationapp.repository;

import cz.cvut.fel.ts1.reservationapp.model.Room;

import java.util.Optional;

public interface RoomRepository {
    Optional<Room> findById(Long id);
}