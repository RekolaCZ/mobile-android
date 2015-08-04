package cz.rekola.app.core.model;

import android.widget.Button;

import java.util.Date;
import java.util.List;

import cz.rekola.app.api.model.bike.Equipment;
import cz.rekola.app.api.model.bike.IssueUpdate;

/**
 * Class is used for recycler view to show different views
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */

public class BikeDetailItem {

    private final int mType;

    public static final int TYPE_SEPARATOR = 0;
    public static final int TYPE_BASIC_INFO = 1;
    public static final int TYPE_RECENTLY_RETURNED = 2;
    public static final int TYPE_EQUIPMENT = 3;
    public static final int TYPE_ISSUE_HEADER = 4;
    public static final int TYPE_ISSUE_TITLE = 5;
    public static final int TYPE_ISSUE_ITEM = 6;

    //@layout/bike_detail_basic_info
    private String bikeIconUrl;
    private String bikeType;
    private String bikeName;
    private String bikeDescription;
    private boolean operationalWithIssues;
    private boolean operational;

    //@layout/bike_detail_recently_returned
    private Date recentlyReturned;
    private String recentPlaceDescription;

    //@layout/bike_detail_equipment
    private List<Equipment> equipments;
    private Button.OnClickListener equipmentsDetailListener;

    //@layout/bike_detail_issues_header
    private Button.OnClickListener addIssueListener;
    private boolean mHasIssues;

    //@layout/bike_detail_issues_title
    private String issueTitle;

    //@layout/bike_detail_issues_item
    private IssueUpdate issueUpdate;


    public static BikeDetailItem getSeparatorInstance() {
        return new BikeDetailItem(TYPE_SEPARATOR);
    }

    public static BikeDetailItem getBasicInfoInstance(String bikeIconUrl, String bikeType, String bikeName,
                                                      boolean operationalWithIssues, boolean operational,
                                                      String description) {
        return new BikeDetailItem(TYPE_BASIC_INFO, bikeIconUrl, bikeType, bikeName,
                operationalWithIssues, operational, description);
    }

    public static BikeDetailItem getRecentlyReturnedInstance(Date recentlyReturned, String recentPlaceDescription) {
        return new BikeDetailItem(TYPE_RECENTLY_RETURNED, recentlyReturned, recentPlaceDescription);
    }

    public static BikeDetailItem getEquipmentInstance(List<Equipment> equipments, Button
            .OnClickListener equipmentsDetailListener) {
        return new BikeDetailItem(TYPE_EQUIPMENT, equipments, equipmentsDetailListener);
    }

    public static BikeDetailItem getIssueHeaderInstance(Button.OnClickListener addIssueListener,
                                                        boolean hasIssues) {
        return new BikeDetailItem(TYPE_ISSUE_HEADER, addIssueListener, hasIssues);
    }

    public static BikeDetailItem getIssueTitleInstance(String issueTitle) {
        return new BikeDetailItem(TYPE_ISSUE_TITLE, issueTitle);
    }

    public static BikeDetailItem getIssueItemInstance(IssueUpdate issueUpdate) {
        return new BikeDetailItem(TYPE_ISSUE_ITEM, issueUpdate);
    }


    public int getType() {
        return mType;
    }

    private BikeDetailItem(int type) {
        mType = type;
    }

    /**
     * private constructors
     */

    //TYPE_BASIC_INFO
    private BikeDetailItem(int type, String bikeIconUrl, String bikeType, String bikeName, boolean
            operationalWithIssues, boolean operational, String bikeDescription) {
        mType = type;
        this.bikeIconUrl = bikeIconUrl;
        this.bikeType = bikeType;
        this.bikeName = bikeName;
        this.operationalWithIssues = operationalWithIssues;
        this.operational = operational;
        this.bikeDescription = bikeDescription;
    }

    //TYPE_RECENTLY_RETURNED
    private BikeDetailItem(int type, Date recentlyReturned, String recentPlaceDescription) {
        mType = type;
        this.recentlyReturned = recentlyReturned;
        this.recentPlaceDescription = recentPlaceDescription;
    }

    //TYPE_EQUIPMENT
    private BikeDetailItem(int type, List<Equipment> equipments, Button
            .OnClickListener equipmentsDetailListener) {
        mType = type;
        this.equipments = equipments;
        this.equipmentsDetailListener = equipmentsDetailListener;
    }

    //TYPE_ISSUE_HEADER
    public BikeDetailItem(int type, Button.OnClickListener addIssueListener, boolean hasIssues) {
        mType = type;
        this.addIssueListener = addIssueListener;
        this.mHasIssues = hasIssues;
    }

    //TYPE_ISSUE_TITLE
    private BikeDetailItem(int type, String issueTitle) {
        mType = type;
        this.issueTitle = issueTitle;
    }

    //TYPE_ISSUE_ITEM
    private BikeDetailItem(int type, IssueUpdate issueUpdate) {
        mType = type;
        this.issueUpdate = issueUpdate;
    }

    /**
     * public getters
     */

    public String getBikeIconUrl() {
        return bikeIconUrl;
    }

    public String getBikeType() {
        return bikeType;
    }

    public String getBikeName() {
        return bikeName;
    }

    public boolean isOperationalWithIssues() {
        return operationalWithIssues;
    }

    public boolean isInOperational() {
        return !operational;
    }

    public boolean isOperational() {
        return operational;
    }

    public String getBikeDescription() {
        return bikeDescription;
    }

    public Date getRecentlyReturned() {
        return recentlyReturned;
    }

    public String getRecentPlaceDescription() {
        return recentPlaceDescription;
    }

    public List<Equipment> getEquipments() {
        return equipments;
    }

    public Button.OnClickListener getEquipmentsDetailListener() {
        return equipmentsDetailListener;
    }

    public Button.OnClickListener getAddIssueListener() {
        return addIssueListener;
    }

    public boolean hasIssues() {
        return mHasIssues;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public IssueUpdate getIssueUpdate() {
        return issueUpdate;
    }
}
