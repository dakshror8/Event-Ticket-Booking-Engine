package com.ticketBookingEngine.ticketBookingEngine.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "venues")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "venue_id")
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(name = "total_capacity", nullable = false)
    private Integer totalCapacity;
}
