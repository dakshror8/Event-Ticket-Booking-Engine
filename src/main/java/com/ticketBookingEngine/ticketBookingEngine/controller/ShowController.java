package com.ticketBookingEngine.ticketBookingEngine.controller;

import com.ticketBookingEngine.ticketBookingEngine.dto.ShowSeatsResponseDTO;
import com.ticketBookingEngine.ticketBookingEngine.service.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @GetMapping("/{showId}/seats")
    public ResponseEntity<ShowSeatsResponseDTO> getShowSeats(@PathVariable Long showId) {
        ShowSeatsResponseDTO response = showService.getSeatsForShow(showId);
        return ResponseEntity.ok(response);
    }
}
