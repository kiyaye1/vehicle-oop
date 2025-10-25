package com.synex.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("TRUCK")
public class Truck extends Vehicle {
	
	public Truck() {
		
	}

	public Truck(String name, String kind, String model, double price) {
		super(name, kind, model, price);
	}

	@Override
	public String start() {
		return getName() + " starts with diesel engine.";
	}
}
