package com.ticketBookingEngine.ticketBookingEngine.repository;

import com.ticketBookingEngine.ticketBookingEngine.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
}
