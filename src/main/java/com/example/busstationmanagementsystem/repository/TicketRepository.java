package com.example.busstationmanagementsystem.repository;

import com.example.busstationmanagementsystem.model.Ticket;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
@Profile("memory")
public class TicketRepository extends InMemoryRepository<Ticket, String> {
    public TicketRepository() {
        super(Ticket::getId);
    }
    @Repository
    @Profile("file")
    public static class TicketFileRepository extends InFileRepository<Ticket> {
        public TicketFileRepository(@Value("${busstation.data-directory:src/main/resources/data}") String dataDirectory) {
            super(Path.of(dataDirectory, "tickets.json").toString(), Ticket.class, Ticket::getId);
        }
    }
}
