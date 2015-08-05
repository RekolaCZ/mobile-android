package cz.rekola.app.api.model.bike;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Bike issue
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {24. 6. 2015}
 */
public class Issue {
    public String title;
    public List<IssueUpdate> updates;

    public static List<Issue> getGroupedIssues(List<Issue> issueList) {
        HashMap<String, List<IssueUpdate>> hashMap = new HashMap<>();

        for (Issue issue : issueList) {
            if (!hashMap.containsKey(issue.title)) {
                List<IssueUpdate> list = new ArrayList<>();
                list.addAll(issue.updates);
                hashMap.put(issue.title, list);
            } else
                hashMap.get(issue.title).addAll(issue.updates);
        }

        List<Issue> rtnIssueList = new ArrayList<>();

        Iterator it = hashMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            Issue issue = new Issue();
            issue.title = (String) pair.getKey();
            issue.updates = (List<IssueUpdate>) pair.getValue();
            rtnIssueList.add(issue);
            it.remove(); // avoids a ConcurrentModificationException
        }

        return rtnIssueList;
    }


}
