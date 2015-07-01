package cz.rekola.app.api.model.bike;

import java.util.List;

public class Bike {

    public int id;
    public String name;
    public String bikeType;
    public String iconUrl;
    public String imageUrl;
    public String description;
    public String issue;
    //   public List<Integer> issues; //TODO uncomment
    public boolean borrowed;
    public boolean operational;
    public String lastSeen; //TODO change to Date
    public Location location;
    public List<Equipment> equipment;
}