package com.ticketBookingEngine.ticketBookingEngine.service;

import com.ticketBookingEngine.ticketBookingEngine.dto.SeatResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.dto.ShowSeatsResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.entity.Show;
import com.ticketBookingEngine.ticketBookingEngine.entity.ShowSeat;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowRepository;
import com.ticketBookingEngine.ticketBookingEngine.repository.ShowSeatRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional(readOnly = true)
    public ShowSeatsResponseDTO getSeatsForShow(Long showId) {
        // 1. Validate show existence
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new RuntimeException("Show not found with ID: " + showId));

        // 2. Fetch seats in 1 query
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeatDetails(showId);

        // 3. Map Entities to DTOs
        List<SeatResponseDTO> seatDTOs = showSeats.stream()
                .map(ss -> SeatResponseDTO.builder()
                        .showSeatId(ss.getId())
                        .seatNumber(ss.getSeat().getSeatNumber())
                        .seatType(ss.getSeat().getSeatType())
                        .price(ss.getPrice())
                        .status(ss.getStatus())
                        .build())
                .toList();

        return ShowSeatsResponseDTO.builder()
                .showId(show.getId())
                .eventTitle(show.getEvent().getTitle())
                .venueName(show.getVenue().getName())
                .startTime(show.getStartTime())
                .seats(seatDTOs)
                .build();
    }
}