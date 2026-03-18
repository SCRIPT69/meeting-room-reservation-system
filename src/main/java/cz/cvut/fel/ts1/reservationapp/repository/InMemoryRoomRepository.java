package cz.cvut.fel.ts1.reservationapp.repository;

import cz.cvut.fel.ts1.reservationapp.model.Room;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
public class InMemoryRoomRepository implements RoomRepository {

    private final Map<Long, Room> rooms = new HashMap<>();

    public InMemoryRoomRepository() {
        rooms.put(1L, new Room(1L, "Alpha", 4));
        rooms.put(2L, new Room(2L, "Beta", 8));
        rooms.put(3L, new Room(3L, "Gamma", 12));
    }

    @Override
    public Optional<Room> findById(Long id) {
        return Optional.ofNullable(rooms.get(id));
    }
}