package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Ticket;
import org.springframework.stereotype.Repository;

@Repository
public class TicketFileRepository extends InFileRepository<Ticket> {
    public TicketFileRepository() {
        super("src/main/resources/data/tickets.json", Ticket.class, Ticket::getId);
    }
}
