package cz.cvut.fel.ts1.reservationapp.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import cz.cvut.fel.ts1.reservationapp.model.Reservation;
import cz.cvut.fel.ts1.reservationapp.model.ReservationStatus;
import cz.cvut.fel.ts1.reservationapp.model.Room;
import cz.cvut.fel.ts1.reservationapp.repository.ReservationRepository;
import cz.cvut.fel.ts1.reservationapp.repository.RoomRepository;
import org.mockito.ArgumentCaptor;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ReservationServiceMockitoTest {

    RoomRepository roomRepositoryMock;
    ReservationRepository reservationRepositoryMock;
    ReservationService reservationService;

    @BeforeEach
    public void setUp(){
        roomRepositoryMock = mock(RoomRepository.class);
        reservationRepositoryMock = mock(ReservationRepository.class);
        reservationService = new ReservationService(roomRepositoryMock, reservationRepositoryMock);
    }



    public Reservation validReservation(int people){
        return new Reservation(
                null,
                1L,
                people,
                LocalDateTime.of(2026,5,11,9,0),
                LocalDateTime.of(2026,5,11,10,0),
                null
        );
    }




    @Test
    public void createReservation_thisShouldBeConfirmed_becausePeopleLessOrEqual4_alsoNoConflict(){
        Reservation guestReservation = validReservation(4);
        Room room = new Room(1L, "Alpha", 4);


        when(roomRepositoryMock.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepositoryMock.hasConflict(1L, guestReservation.getStart(), guestReservation.getEnd())).thenReturn(false);

        when(reservationRepositoryMock.save(any(Reservation.class))).thenAnswer(invocation -> {
            Reservation saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Reservation result = reservationService.createReservation(guestReservation);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());

        ArgumentCaptor<Reservation>  captor = ArgumentCaptor.forClass(Reservation.class);
        verify(reservationRepositoryMock).save(captor.capture());
        Reservation savedReservationObject = captor.getValue();

        assertEquals(1L, savedReservationObject.getRoomId());
        assertEquals(4, savedReservationObject.getPeople());
        assertEquals(guestReservation.getStart(), savedReservationObject.getStart());
        assertEquals(guestReservation.getEnd(), savedReservationObject.getEnd());
        assertEquals(ReservationStatus.CONFIRMED, savedReservationObject.getStatus());
    }


    @Test
    public void createReservation_thisShouldSetPending_whenPeopleMoreThan4(){
        Reservation guestReservation = validReservation(6);
        Room room = new Room(1L, "Alpha", 8);

        when(roomRepositoryMock.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepositoryMock.hasConflict(1L, guestReservation.getStart(),
                guestReservation.getEnd())).thenReturn(false);

        when(reservationRepositoryMock.save(any(Reservation.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        Reservation result = reservationService.createReservation(guestReservation);

        assertEquals(ReservationStatus.PENDING, result.getStatus());
        verify(reservationRepositoryMock, times(1)).save(any(Reservation.class));

    }


    @Test
    public void createReservation_thisShouldThrowException_whenConflictExists(){
        Reservation guestReservation = validReservation(3);
        Room room = new Room(1L, "Alpha", 4);

        when(roomRepositoryMock.findById(1L)).thenReturn(Optional.of(room));
        when(reservationRepositoryMock.hasConflict(1L, guestReservation.getStart(),
                guestReservation.getEnd())).thenReturn(true);

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                ()-> reservationService.createReservation(guestReservation));



        assertEquals("Reservation conflict", illegalArgumentException.getMessage());
        verify(reservationRepositoryMock, never()).save(any());

    }

    @Test
    public void createReservation_shouldThrow_whenRoomNotFound(){
        Reservation guestReservation = validReservation(2);

        when(roomRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
                ()->reservationService.createReservation(guestReservation));

        assertEquals("Room not found", illegalArgumentException.getMessage());
        verify(reservationRepositoryMock, never()).save(any());

    }

    @Test
    public void confirmReservation_thisShouldChangeStatusToConfirmed_whenPending(){
        Reservation pendingReservation = new Reservation(
          55L, 1L, 6, LocalDateTime.of(2026,5,11,10,0),
         LocalDateTime.of(2026,5,11,11,0), ReservationStatus.PENDING
        );


        when(reservationRepositoryMock.findById(55L)).thenReturn(Optional.of(pendingReservation));

        when(reservationRepositoryMock.save(any(Reservation.class))).thenAnswer(invocation ->
                invocation.getArgument(0));

        Reservation result = reservationService.confirmReservation(55L);

        assertEquals(ReservationStatus.CONFIRMED, result.getStatus());
        verify(reservationRepositoryMock, times(1)).save(pendingReservation);


    }

}
