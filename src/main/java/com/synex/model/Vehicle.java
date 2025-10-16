package com.synex.model;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;

@Entity
@Table(name = "VEHICLE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VEHICLE_TYPE")
public abstract class Vehicle {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	private String name;
	private String type;
	private String model;
	private double price;
	
	protected Vehicle() {
		
	}
	
	public Vehicle(String name, String type, String model, double price) {
		this.name = name;
		this.type = type;
		this.model = model;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getModel() {
		return model;
	}

	public double getPrice() {
		return price;
	}
	
	public void displayInfo() {
		System.out.println("Vehicle: \nName: " + name + 
        		"\nModel:" + model + 
        		"\nType: " + type + 
        		"\nPrice: $" + price);
	}
	
	public abstract String start();
}
