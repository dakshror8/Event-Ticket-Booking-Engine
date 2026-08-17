package com.ticketBookingEngine.ticketBookingEngine.repository;

import com.ticketBookingEngine.ticketBookingEngine.entity.Booking;
import com.ticketBookingEngine.ticketBookingEngine.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // fetch bookings that are pending and expired
    List<Booking> findByStatusAndExpiresAtBefore(BookingStatus status, LocalDateTime now);
}
