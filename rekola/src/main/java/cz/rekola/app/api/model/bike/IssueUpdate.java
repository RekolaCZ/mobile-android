package cz.rekola.app.api.model.bike;

/**
 * Bike issue update works like "user comment" about issue with bike
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */
public class IssueUpdate {
    public String author;
    public String description;
    public String issuedAt; //TODO waiting for api Date
}
