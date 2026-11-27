package com.example.busstationmanagementsystem;

import com.example.busstationmanagementsystem.model.*;
import com.example.busstationmanagementsystem.service.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("memory")
public class MemoryDataInitializer implements ApplicationRunner {
    private static final int SAMPLE_COUNT = 5;

    private final BusService busService;
    private final BusStationService busStationService;
    private final RouteService routeService;
    private final BusTripService busTripService;
    private final PassengerService passengerService;
    private final StaffService staffService;
    private final DutyAssignmentService dutyAssignmentService;
    private final TicketService ticketService;

    public MemoryDataInitializer(BusService busService,
                                 BusStationService busStationService,
                                 RouteService routeService,
                                 BusTripService busTripService,
                                 PassengerService passengerService,
                                 StaffService staffService,
                                 DutyAssignmentService dutyAssignmentService,
                                 TicketService ticketService) {
        this.busService = busService;
        this.busStationService = busStationService;
        this.routeService = routeService;
        this.busTripService = busTripService;
        this.passengerService = passengerService;
        this.staffService = staffService;
        this.dutyAssignmentService = dutyAssignmentService;
        this.ticketService = ticketService;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<BusStation> stations = new ArrayList<>();
        List<Route> routes = new ArrayList<>();
        List<BusTrip> trips = new ArrayList<>();
        List<Staff> staffMembers = new ArrayList<>();
        List<Passenger> passengers = new ArrayList<>();

        for (int i = 1; i <= SAMPLE_COUNT; i++) {
            String suffix = String.format("%02d", i);
            busService.registerBus(new Bus("MEM-BUS-" + suffix, "MEM-REG-" + suffix, 45,
                    BusStatus.ACTIVE, "Memory Coach " + suffix, 2020 + i));

            BusStation station = new BusStation("MEM-STATION-" + suffix,
                    "Memory Station " + suffix, "City " + suffix);
            stations.add(busStationService.registerStation(station));

            Passenger passenger = new Passenger("MEM-PASSENGER-" + suffix,
                    "Memory Passenger " + suffix, "EUR", "passenger" + suffix + "@example.com");
            passengers.add(passengerService.createPassenger(passenger));

            Staff staff = i % 2 == 1
                    ? new Driver("MEM-STAFF-" + suffix, "Memory Driver " + suffix, i + 2)
                    : new TripManager("MEM-STAFF-" + suffix, "Memory Manager " + suffix, "EMP-" + suffix);
            staffMembers.add(staffService.registerStaff(staff));
        }

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            BusStation origin = stations.get(i);
            BusStation destination = stations.get((i + 1) % SAMPLE_COUNT);
            Route route = new Route("MEM-ROUTE-" + String.format("%02d", i + 1), origin, destination,
                    BigDecimal.valueOf(40 + i * 10L));
            routeService.createRoute(route);
            routes.add(routeService.getRouteById(route.getId()));
        }

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            String suffix = String.format("%02d", i + 1);
            LocalDateTime startTime = LocalDateTime.of(2027, 1, i + 1, 8, 0);
            BusTrip trip = new BusTrip("MEM-TRIP-" + suffix, routes.get(i).getId(), "MEM-BUS-" + suffix,
                    startTime, startTime.plusHours(2), TripStatus.PLANNED);
            trips.add(busTripService.scheduleTrip(trip));
        }

        for (int i = 0; i < SAMPLE_COUNT; i++) {
            String suffix = String.format("%02d", i + 1);
            dutyAssignmentService.assignDuty(new DutyAssignment("MEM-DUTY-" + suffix,
                    trips.get(i).getId(), staffMembers.get(i).getId(),
                    i % 2 == 0 ? DutyRole.PRIMARY_DRIVER : DutyRole.RESERVE_DRIVER));

            ticketService.bookTicket(new Ticket("MEM-TICKET-" + suffix, trips.get(i).getId(),
                    passengers.get(i).getId(), suffix + "A", 25.0 + i * 5));
        }
    }
}
