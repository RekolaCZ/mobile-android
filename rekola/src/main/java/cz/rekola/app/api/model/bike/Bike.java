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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bike bike = (Bike) o;

        if (id != bike.id) return false;
        if (borrowed != bike.borrowed) return false;
        if (operational != bike.operational) return false;
        if (name != null ? !name.equals(bike.name) : bike.name != null) return false;
        if (bikeType != null ? !bikeType.equals(bike.bikeType) : bike.bikeType != null)
            return false;
        if (iconUrl != null ? !iconUrl.equals(bike.iconUrl) : bike.iconUrl != null) return false;
        if (imageUrl != null ? !imageUrl.equals(bike.imageUrl) : bike.imageUrl != null)
            return false;
        if (description != null ? !description.equals(bike.description) : bike.description != null)
            return false;
        if (issue != null ? !issue.equals(bike.issue) : bike.issue != null) return false;
        if (issues != null ? !issues.equals(bike.issues) : bike.issues != null) return false;
        if (location != null ? !location.equals(bike.location) : bike.location != null)
            return false;
        return !(equipment != null ? !equipment.equals(bike.equipment) : bike.equipment != null);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (bikeType != null ? bikeType.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (issue != null ? issue.hashCode() : 0);
        result = 31 * result + (issues != null ? issues.hashCode() : 0);
        result = 31 * result + (borrowed ? 1 : 0);
        result = 31 * result + (operational ? 1 : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        result = 31 * result + (equipment != null ? equipment.hashCode() : 0);
        return result;
    }
}