package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Ticket;
import org.springframework.stereotype.Repository;

@Repository
public class TicketRepository extends InMemoryRepository<Ticket, String> {
    private final CrudRepository<Ticket, String> fileRepository;

    public TicketRepository() {
        super(Ticket::getId);
        fileRepository = new InFileRepository<Ticket>("src/main/resources/data/tickets.json", Ticket.class, Ticket::getId) {};
    }

    public CrudRepository<Ticket, String> forMode(StorageMode mode) {
        return mode == StorageMode.IN_FILE ? fileRepository : this;
    }
}
