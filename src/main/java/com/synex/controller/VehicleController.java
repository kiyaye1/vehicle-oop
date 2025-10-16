package com.synex.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synex.model.Bike;
import com.synex.model.Car;
import com.synex.model.ElectricCar;
import com.synex.model.Truck;
import com.synex.model.Vehicle;
import com.synex.service.VehicleService;

@RestController
@RequestMapping("/vehicles")
public class VehicleController {
	
	private final VehicleService service;

    public VehicleController(VehicleService service) {
        this.service = service;
    }

    @PostMapping("/add/car")
    public Vehicle addCar(@RequestBody Car car) {
        return service.save(car);
    }

    @PostMapping("/add/bike")
    public Vehicle addBike(@RequestBody Bike bike) {
        return service.save(bike);
    }

    @PostMapping("/add/truck")
    public Vehicle addTruck(@RequestBody Truck truck) {
        return service.save(truck);
    }

    @PostMapping("/add/electric")
    public Vehicle addElectric(@RequestBody ElectricCar car) {
        return service.save(car);
    }

    @GetMapping
    public List<Vehicle> all() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Vehicle get(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/{id}/start")
    public String start(@PathVariable Long id) {
        return service.start(id);
    }
}
