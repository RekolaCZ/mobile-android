package cz.rekola.app.api.model.bike;

import java.util.List;

/**
 * Bike model
 */

public class Bike {
    public static final int UNDEFINED_BIKE = -1;

    public int id;
    public String name;
    public String bikeType;
    public String iconUrl;
    public String imageUrl;
    public String description;
    public String issue;
    public List<Integer> issues;
    public boolean borrowed;
    public boolean operational;
    public Location location;
    public List<Equipment> equipment;
}