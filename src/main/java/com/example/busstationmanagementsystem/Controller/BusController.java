package com.example.busstationmanagementsystem.Controller;

import com.example.busstationmanagementsystem.model.Bus;
import com.example.busstationmanagementsystem.model.BusStatus;
import com.example.busstationmanagementsystem.service.BusService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buses")
public class BusController {

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    @PostMapping
    public Bus createBus(@RequestBody Bus bus) {
        return busService.createBus(bus);
    }

    @GetMapping("/{id}")
    public Bus getBusById(@PathVariable String id) {
        return busService.getBusById(id);
    }

    @GetMapping
    public List<Bus> getAllBuses() {
        return busService.getAllBuses();
    }

    @GetMapping("/active")
    public List<Bus> getActiveBuses() {
        return busService.getActiveBuses();
    }

    @PutMapping("/{id}/status")
    public Bus updateStatus(@PathVariable String id, @RequestParam BusStatus status) {
        return busService.updateBusStatus(id, status);
    }

    @DeleteMapping("/{id}")
    public boolean deleteBus(@PathVariable String id) {
        return busService.deleteBus(id);
    }
}