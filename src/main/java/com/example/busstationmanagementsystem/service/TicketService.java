package com.example.busstationmanagementsystem.service;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusTrip;
import com.example.busstationmanagementsystem.model.Passenger;
import com.example.busstationmanagementsystem.model.BusStation;
import com.example.busstationmanagementsystem.model.Route;
import com.example.busstationmanagementsystem.model.Ticket;
import com.example.busstationmanagementsystem.repository.CrudRepository;
import com.example.busstationmanagementsystem.repository.BusTripFileRepository;
import com.example.busstationmanagementsystem.repository.BusTripRepository;
import com.example.busstationmanagementsystem.repository.BusFileRepository;
import com.example.busstationmanagementsystem.repository.BusRepository;
import com.example.busstationmanagementsystem.repository.RouteRepository;
import com.example.busstationmanagementsystem.repository.RouteFileRepository;
import com.example.busstationmanagementsystem.repository.BusStationRepository;
import com.example.busstationmanagementsystem.repository.BusStationFileRepository;
import com.example.busstationmanagementsystem.repository.TicketFileRepository;
import com.example.busstationmanagementsystem.repository.TicketRepository;
import com.example.busstationmanagementsystem.repository.PassengerFileRepository;
import com.example.busstationmanagementsystem.repository.PassengerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketService {
    public enum RepositoryType {
        IN_MEMORY,
        IN_FILE
    }

    private final CrudRepository<Ticket, String> inMemoryTicketRepository;
    private final CrudRepository<Ticket, String> inFileTicketRepository;
    private final CrudRepository<Passenger, String> inMemoryPassengerRepository;
    private final CrudRepository<Passenger, String> inFilePassengerRepository;
    private final CrudRepository<BusTrip, String> inMemoryBusTripRepository;
    private final CrudRepository<BusTrip, String> inFileBusTripRepository;
    private final CrudRepository<Bus, String> inMemoryBusRepository;
    private final CrudRepository<Bus, String> inFileBusRepository;
    private final CrudRepository<Route, String> inMemoryRouteRepository;
    private final CrudRepository<Route, String> inFileRouteRepository;
    private final CrudRepository<BusStation, String> inMemoryStationRepository;
    private final CrudRepository<BusStation, String> inFileStationRepository;

    public TicketService(TicketRepository ticketRepository,
                         TicketFileRepository ticketFileRepository,
                         PassengerRepository passengerRepository,
                         PassengerFileRepository passengerFileRepository,
                         BusTripRepository busTripRepository,
                         BusTripFileRepository busTripFileRepository,
                         BusRepository busRepository,
                         BusFileRepository busFileRepository,
                         RouteRepository routeRepository,
                         RouteFileRepository routeFileRepository,
                         BusStationRepository stationRepository,
                         BusStationFileRepository stationFileRepository) {
        this.inMemoryTicketRepository = ticketRepository;
        this.inFileTicketRepository = ticketFileRepository;
        this.inMemoryPassengerRepository = passengerRepository;
        this.inFilePassengerRepository = passengerFileRepository;
        this.inMemoryBusTripRepository = busTripRepository;
        this.inFileBusTripRepository = busTripFileRepository;
        this.inMemoryBusRepository = busRepository;
        this.inFileBusRepository = busFileRepository;
        this.inMemoryRouteRepository = routeRepository;
        this.inFileRouteRepository = routeFileRepository;
        this.inMemoryStationRepository = stationRepository;
        this.inFileStationRepository = stationFileRepository;
    }

    public Passenger registerPassenger(Passenger passenger) {
        return inMemoryPassengerRepository.save(passenger);
    }

    public Passenger getPassengerById(String id) {
        return inMemoryPassengerRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + id));
    }

    public Ticket bookTicket(Ticket ticket, RepositoryType repositoryType) {
        CrudRepository<Ticket, String> ticketRepository = getRepository(repositoryType);
        if (ticketRepository.findById(ticket.getId()).isPresent()) {
            throw new IllegalArgumentException("Ticket already exists with ID: " + ticket.getId());
        }
        CrudRepository<BusTrip, String> busTripRepository = getBusTripRepository(repositoryType);
        CrudRepository<Bus, String> busRepository = getBusRepository(repositoryType);
        CrudRepository<Passenger, String> passengerRepository = getPassengerRepository(repositoryType);
        BusTrip trip = busTripRepository.findById(ticket.getTripId())
                .orElseThrow(() -> new NoSuchElementException("Trip not found: " + ticket.getTripId()));

        Bus bus = busRepository.findById(trip.getBusId())
                .orElseThrow(() -> new NoSuchElementException("Bus not found for trip: " + trip.getBusId()));

        if (trip.getTickets().size() >= bus.getCapacity()) {
            throw new IllegalStateException("Cannot book ticket: bus capacity (" + bus.getCapacity() + ") reached.");
        }

        boolean seatTaken = trip.getTickets().stream()
                .anyMatch(t -> t.getSeatNumber().equalsIgnoreCase(ticket.getSeatNumber()));
        if (seatTaken) {
            throw new IllegalArgumentException("Seat " + ticket.getSeatNumber() + " is already booked.");
        }

        Passenger passenger = passengerRepository.findById(ticket.getPassengerId())
                .orElseThrow(() -> new NoSuchElementException("Passenger not found: " + ticket.getPassengerId()));
        Ticket savedTicket = ticketRepository.save(ticket);
        trip.getTickets().add(savedTicket);
        busTripRepository.save(trip);
        syncTripReferences(trip, repositoryType);
        passenger.getTickets().add(savedTicket);
        passengerRepository.save(passenger);

        return savedTicket;
    }

    public List<Ticket> getTicketsByTrip(String tripId) {
        return inMemoryTicketRepository.findAll().stream()
                .filter(t -> tripId.equals(t.getTripId()))
                .toList();
    }

    public List<Ticket> getAllTickets(RepositoryType repositoryType) {
        return getRepository(repositoryType).findAll();
    }

    public List<Passenger> getAllPassengers(RepositoryType repositoryType) {
        return getPassengerRepository(repositoryType).findAll();
    }

    public boolean deleteTicket(String id, RepositoryType repositoryType) {
        CrudRepository<Ticket, String> ticketRepository = getRepository(repositoryType);
        CrudRepository<BusTrip, String> busTripRepository = getBusTripRepository(repositoryType);
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found: " + id));

        busTripRepository.findById(ticket.getTripId())
                .ifPresent(trip -> {
                    trip.getTickets().removeIf(t -> id.equals(t.getId()));
                    busTripRepository.save(trip);
                    syncTripReferences(trip, repositoryType);
                });
        getPassengerRepository(repositoryType).findById(ticket.getPassengerId())
                .ifPresent(passenger -> {
                    passenger.getTickets().removeIf(t -> id.equals(t.getId()));
                    getPassengerRepository(repositoryType).save(passenger);
                });
        return ticketRepository.deleteById(id);
    }

    private CrudRepository<Ticket, String> getRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileTicketRepository
                : inMemoryTicketRepository;
    }

    private CrudRepository<Passenger, String> getPassengerRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFilePassengerRepository
                : inMemoryPassengerRepository;
    }

    private CrudRepository<BusTrip, String> getBusTripRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusTripRepository
                : inMemoryBusTripRepository;
    }

    private CrudRepository<Bus, String> getBusRepository(RepositoryType repositoryType) {
        return repositoryType == RepositoryType.IN_FILE
                ? inFileBusRepository
                : inMemoryBusRepository;
    }

    private void syncTripReferences(BusTrip trip, RepositoryType repositoryType) {
        CrudRepository<Route, String> routeRepository = repositoryType == RepositoryType.IN_FILE
                ? inFileRouteRepository
                : inMemoryRouteRepository;
        CrudRepository<BusStation, String> stationRepository = repositoryType == RepositoryType.IN_FILE
                ? inFileStationRepository
                : inMemoryStationRepository;

        routeRepository.findById(trip.getRouteId()).ifPresent(route -> {
            replaceTrip(route.getTrips(), trip);
            routeRepository.save(route);
            syncStation(route.getOrigin(), trip, stationRepository);
            syncStation(route.getDestination(), trip, stationRepository);
        });
    }

    private void syncStation(BusStation station, BusTrip trip,
                             CrudRepository<BusStation, String> stationRepository) {
        if (station == null) {
            return;
        }
        stationRepository.findById(station.getId()).ifPresent(storedStation -> {
            replaceTrip(storedStation.getTrips(), trip);
            stationRepository.save(storedStation);
        });
    }

    private void replaceTrip(List<BusTrip> trips, BusTrip trip) {
        trips.removeIf(existing -> trip.getId().equals(existing.getId()));
        trips.add(trip);
    }
}