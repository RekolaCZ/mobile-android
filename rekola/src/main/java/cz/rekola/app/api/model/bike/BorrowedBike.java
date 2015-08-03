package cz.rekola.app.api.model.bike;

import java.util.Date;

public class BorrowedBike extends Bike {
    /**
     * Bike model used when user borrow bike
     */
    public String bikeCode;
    public String lockCode;
    public Date borrowedAt;
}
