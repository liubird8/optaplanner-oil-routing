package org.example.domain;

import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

public class Vehicle implements Standstill {

    private String id;
    private Location startLocation;
    private int capacity;

    
    private Customer nextCustomer;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Customer getNextCustomer() {
        return nextCustomer;
    }

    public void setNextCustomer(Customer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    @Override
    public Location getLocation() {
        return startLocation;
    }

}
