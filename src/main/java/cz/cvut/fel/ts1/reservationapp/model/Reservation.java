package cz.cvut.fel.ts1.reservationapp.model;

import java.time.LocalDateTime;

public class Reservation {

    private Long id;
    private Long roomId;
    private int people;
    private LocalDateTime start;
    private LocalDateTime end;
    private ReservationStatus status;

    public Reservation() {
    }

    public Reservation(Long id, Long roomId, int people, LocalDateTime start, LocalDateTime end, ReservationStatus status) {
        this.id = id;
        this.roomId = roomId;
        this.people = people;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public Long getRoomId() {
        return roomId;
    }

    public int getPeople() {
        return people;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setPeople(int people) {
        this.people = people;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }
}