package com.example.busstationmanagementsystem;

import com.example.busstationmanagementsystem.model.*;
import com.example.busstationmanagementsystem.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("memory")
class MemoryDataInitializerTests {
    @LocalServerPort
    private int port;

    @Autowired
    private BusService busService;

    @Autowired
    private BusStationService busStationService;

    @Autowired
    private RouteService routeService;

    @Autowired
    private BusTripService busTripService;

    @Autowired
    private PassengerService passengerService;

    @Autowired
    private StaffService staffService;

    @Autowired
    private DutyAssignmentService dutyAssignmentService;

    @Autowired
    private TicketService ticketService;

    @Test
    void initializesFiveLinkedExamplesForEachModel() {
        assertEquals(5, busService.getAllBuses().size());
        assertEquals(5, busStationService.getAllStations().size());
        assertEquals(5, routeService.getAllRoutes().size());
        assertEquals(5, busTripService.getAllTrips().size());
        assertEquals(5, passengerService.getAllPassengers().size());
        assertEquals(5, staffService.getAllStaff().size());
        assertEquals(5, dutyAssignmentService.getAllAssignments().size());
        assertEquals(5, ticketService.getAllTickets().size());

        assertTrue(staffService.getAllDrivers().size() > 0);
        assertTrue(staffService.getAllTripManagers().size() > 0);
        assertEquals(1, busTripService.getTripById("MEM-TRIP-01").getTickets().size());
        assertEquals(1, busTripService.getTripById("MEM-TRIP-01").getAssignments().size());
        assertEquals(1, passengerService.getPassengerById("MEM-PASSENGER-01").getTickets().size());
        assertTrue(busStationService.getStationById("MEM-STATION-01").getTrips().size() > 0);
        assertTrue(routeService.getRouteById("MEM-ROUTE-01").getTrips().size() > 0);
    }

    @Test
    void rendersTheSeededRecordsInAllModelTables() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        List<String> endpoints = List.of("buses", "stations", "routes", "trips", "passengers",
                "staff", "duty-assignments", "tickets");

        for (String endpoint : endpoints) {
            HttpResponse<String> response = client.send(HttpRequest.newBuilder(
                            URI.create("http://localhost:" + port + "/" + endpoint)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(200, response.statusCode(), endpoint);
            assertEquals(6, response.body().split("<tr>", -1).length - 1, endpoint);
            assertTrue(response.body().contains("MEM-"), endpoint);
        }
    }

    @Test
    void seededTicketsSupportSeatValidationAndCrud() {
        assertThrows(IllegalArgumentException.class, () -> ticketService.bookTicket(
                new Ticket("MEM-TICKET-DUPLICATE-SEAT", "MEM-TRIP-01", "MEM-PASSENGER-02", "01A", 30.0)));

        Ticket addedTicket = ticketService.bookTicket(
                new Ticket("MEM-TICKET-CHECK", "MEM-TRIP-01", "MEM-PASSENGER-02", "02A", 30.0));
        assertEquals(6, ticketService.getAllTickets().size());
        assertTrue(busTripService.getTripById("MEM-TRIP-01").getTickets().stream()
                .anyMatch(ticket -> ticket.getId().equals(addedTicket.getId())));
        assertTrue(ticketService.deleteTicket(addedTicket.getId()));
        assertEquals(5, ticketService.getAllTickets().size());
        assertEquals(1, busTripService.getTripById("MEM-TRIP-01").getTickets().size());
    }
}
