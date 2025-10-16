package com.synex.service;

import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synex.exception.VehicleNotFoundException;
import com.synex.model.Vehicle;
import com.synex.repository.VehicleRepository;

@Service
public class VehicleService {
	
	private static final Logger log = LoggerFactory.getLogger(VehicleService.class);
    private final VehicleRepository repo;

    public VehicleService(VehicleRepository repo) {
        this.repo = repo;
    }

    public Vehicle save(Vehicle v) {
        log.info("Saving vehicle: {}", v.getName());
        return repo.save(v);
    }

    public List<Vehicle> findAll() {
        return repo.findAll();
    }

    public Vehicle findById(Long id) {
        return repo.findById(id).orElseThrow(
            () -> new VehicleNotFoundException("Vehicle with ID " + id + " not found"));
    }

    public String start(Long id) {
        Vehicle v = findById(id);
        String msg = v.start();
        log.info("Started vehicle: {}", msg);
        return msg;
    }
}
