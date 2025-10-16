package com.synex.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synex.model.Car;
import com.synex.repository.VehicleRepository;

import static org.junit.jupiter.api.Assertions.*;

public class VehicleServiceTest {

	@Test
    void testVehicleServiceStart() {
        
        VehicleRepository mockRepo = Mockito.mock(VehicleRepository.class);
        VehicleService service = new VehicleService(mockRepo);

        Car car = new Car("Toyota", "Regular", "Corolla", 20000);
        Mockito.when(mockRepo.findById(1L)).thenReturn(java.util.Optional.of(car));

        String result = service.start(1L);

        assertTrue(result.contains("key ignition"));
        Mockito.verify(mockRepo).findById(1L);
    }
}
