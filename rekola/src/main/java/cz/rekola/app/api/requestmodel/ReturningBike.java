package cz.rekola.app.api.requestmodel;

public class ReturningBike {

    /**
     * Bike which user want return
     */

    public ReturningLocation location;

    public ReturningBike(ReturningLocation location) {
        this.location = location;
    }

}
