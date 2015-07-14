package cz.rekola.app.api.requestmodel;

/**
 * Location used in IssueReport
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class IssueReportLocation {

    public final double lat;
    public final double lng;

    public IssueReportLocation(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    @Override
    public String toString() {
        return "IssueReportLocation{" +
                "lat=" + lat +
                ", lng=" + lng +
                '}';
    }
}
