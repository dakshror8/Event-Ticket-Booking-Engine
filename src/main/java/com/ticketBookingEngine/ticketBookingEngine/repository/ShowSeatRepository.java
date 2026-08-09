package com.ticketBookingEngine.ticketBookingEngine.repository;

import com.ticketBookingEngine.ticketBookingEngine.entity.ShowSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.show.id = :showId")
    List<ShowSeat> findByShowIdWithSeatDetails(@Param("showId")Long showId);
}
