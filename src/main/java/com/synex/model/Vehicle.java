package com.synex.model;

import jakarta.persistence.Column;
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
@DiscriminatorColumn(name = "VEHICLE_KIND")
public abstract class Vehicle {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	@Column(name = "NAME")
	private String name;
	@Column(name = "KIND")
	private String kind;
	@Column(name = "MODEL")
	private String model;
	@Column(name = "PRICE")
	private double price;
	
	protected Vehicle() {
		
	}
	
	public Vehicle(String name, String kind, String model, double price) {
		this.name = name;
		this.kind = kind;
		this.model = model;
		this.price = price;
	}

	public String getName() {
		return name;
	}

	public String getKind() {
		return kind;
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
        		"\nKind: " + kind + 
        		"\nPrice: $" + price);
	}
	
	public abstract String start();
}
