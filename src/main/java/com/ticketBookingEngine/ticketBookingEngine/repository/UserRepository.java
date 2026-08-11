package com.ticketBookingEngine.ticketBookingEngine.repository;

import com.ticketBookingEngine.ticketBookingEngine.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
