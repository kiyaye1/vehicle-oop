package com.synex.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {
	
	public Car() {
		
	}

	public Car(String name, String kind, String model, double price) {
		super(name, kind, model, price);
	}

	@Override
	public String start() {
		return getName() + " starts using key ignition.";
	}
}
