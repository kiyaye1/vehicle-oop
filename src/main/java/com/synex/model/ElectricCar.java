package com.synex.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("ELECTRICCAR")
public class ElectricCar extends Vehicle {
	
	private int batteryLevel;
	
	public ElectricCar() {
		
	}

	public ElectricCar(String name, String type ,String model, double price, int batteryLevel) {
		super(name, type, model, price);
		this.batteryLevel = batteryLevel;
	}
	
	public int getBatteryLevel() {
        return batteryLevel;
    }
	
	@Override
	public String start() {
		return getName() + " starts using electric motor. Battery: " + batteryLevel + "%";
	}
}
