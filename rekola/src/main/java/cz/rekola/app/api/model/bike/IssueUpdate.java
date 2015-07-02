package cz.rekola.app.api.model.bike;

import java.util.Date;

/**
 * Bike issue update works like "user comment" about issue with bike
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */
public class IssueUpdate {
    public String author;
    public String description;
    public Date issuedAt;
}
