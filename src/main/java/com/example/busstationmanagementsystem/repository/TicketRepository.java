package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Ticket;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository extends InMemoryRepository<Ticket, String> {
    public TicketRepository() {
        super(Ticket::getId);
    }
}