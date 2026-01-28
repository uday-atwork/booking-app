package model;

import constant.SeatStatus;
import jakarta.persistence.*;

/**
 * State of a seat for a given show(scheduled screening of a movie at a given theater on a given date and time).
 */
@Entity
@Table(name = "SHOW_SEAT_AVAILABILITY")
public class ShowSeatAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    @JoinColumn(name = "show_id")
    private Show show;

    @Column
    @ManyToOne
    private Seat seat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatStatus seatStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Show getShow() {
        return show;
    }

    public void setShow(Show show) {
        this.show = show;
    }

    public Seat getSeat() {
        return seat;
    }

    public void setSeat(Seat seat) {
        this.seat = seat;
    }

    public SeatStatus getSeatStatus() {
        return seatStatus;
    }

    public void setSeatStatus(SeatStatus seatStatus) {
        this.seatStatus = seatStatus;
    }
}
