package com.example.busstationmanagementsystem.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BusTrip {
    private String id;
    private String routeId;
    private String busId;
    private LocalDateTime startTime;
    private LocalDateTime arrivalTime; // erweiterung
    private List<Ticket> tickets = new ArrayList<>();
    private List<DutyAssignment> assignments = new ArrayList<>();
    private TripStatus status;

    public BusTrip(String id, String routeId, String busId, LocalDateTime startTime, LocalDateTime arrivalTime, TripStatus status) {
        this.id = id;
        this.routeId = routeId;
        this.busId = busId;
        this.startTime = startTime;
        this.arrivalTime = arrivalTime;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public String getBusId() {
        return busId;
    }

    public void setBusId(String busId) {
        this.busId = busId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public List<DutyAssignment> getAssignments() {
        return assignments;
    }

    public void setAssignments(List<DutyAssignment> assignments) {
        this.assignments = assignments;
    }

    public TripStatus getStatus() {
        return status;
    }

    public void setStatus(TripStatus status) {
        this.status = status;
    }
}
