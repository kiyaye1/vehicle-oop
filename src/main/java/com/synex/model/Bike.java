package com.synex.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Bike")
public class Bike extends Vehicle {
	
	public Bike() {
		
	}

	public Bike(String name, String model, double price) {
		super(name, "Bike", model, price);
	}

	@Override
	public String start() {
		return getName() + " starts with a kick.";
	}
}
