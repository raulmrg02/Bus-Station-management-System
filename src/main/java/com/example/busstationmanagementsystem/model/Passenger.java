package com.example.busstationmanagementsystem.model;

import java.util.ArrayList;
import java.util.List;

public class Passenger {
    private String id;
    private String name;
    private String currency;
    private String email; // erweiterung
    private List<Ticket> tickets = new ArrayList<>();

    public Passenger(String id, String name, String currency, String email) {
        this.id = id;
        this.name = name;
        this.currency = currency;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}
