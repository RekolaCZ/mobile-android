package cz.rekola.app.api.requestmodel;

/**
 * Request add issue comment
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {14. 7. 2015}
 **/
public class IssueReport {
    public final int type;
    public final String title;
    public final String description;
    public final boolean disabling;
    public final IssueReportLocation location;

    public IssueReport(int type, String title, String description, boolean disabling,
                       IssueReportLocation location) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.disabling = disabling;
        this.location = location;
    }

    @Override
    public String toString() {
        return "IssueReport{" +
                "type=" + type +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", disabling=" + disabling +
                ", location=" + location +
                '}';
    }
}
