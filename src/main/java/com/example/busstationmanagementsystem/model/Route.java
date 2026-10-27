package com.example.busstationmanagementsystem.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Route {
    private String id;
    private BusStation origin;
    private BusStation destination;
    private BigDecimal distance;
    private List<BusTrip> trips = new ArrayList<>();

    public Route(String id, BusStation origin, BusStation destination, BigDecimal distance) {
        this.id = id;
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public BusStation getOrigin() {
        return origin;
    }

    public void setOrigin(BusStation origin) {
        this.origin = origin;
    }

    public BusStation getDestination() {
        return destination;
    }

    public void setDestination(BusStation destination) {
        this.destination = destination;
    }

    public BigDecimal getDistance() {
        return distance;
    }

    public void setDistance(BigDecimal distance) {
        this.distance = distance;
    }

    public List<BusTrip> getTrips() {
        return trips;
    }

    public void setTrips(List<BusTrip> trips) {
        this.trips = trips;
    }
}
