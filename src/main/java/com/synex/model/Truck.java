package com.synex.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Truck")
public class Truck extends Vehicle {
	
	public Truck() {
		
	}

	public Truck(String name, String model, double price) {
		super(name, "Truck", model, price);
	}

	@Override
	public String start() {
		return getName() + " starts with diesel engine.";
	}
}
