package cz.rekola.android.core.map;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class DirectionManager {

	private DownloadDirectionsTask directionTask;
	private GoogleMap map;
	private Polyline directionPath;

	public DirectionManager(GoogleMap map) {
		this.map = map;
	}

	public void loadDirections(DirectionParams params) {
		cancelTasks();

		directionTask = new DownloadDirectionsTask();
		directionTask.execute(params);
	}

	/*public void cancelLoadingDirections() {
		cancelTasks();
	}*/

	public void clearDirections() {
		cancelTasks();
		if (directionPath != null) {
			directionPath.remove();
			directionPath = null;
		}
	}

	private void cancelTasks() {
		if (directionTask != null) {
			directionTask.cancel(true);
			directionTask = null;
		}
	}

	private class DownloadDirectionsTask extends AsyncTask<DirectionParams, Void, PolylineOptions> {

		private DirectionParams dirPar;

		@Override
		protected PolylineOptions doInBackground(DirectionParams... directionParams) {
			dirPar = directionParams[0];

			GMapV2Direction md = new GMapV2Direction();
			Document doc = md.getDocument(dirPar.start, dirPar.end, dirPar.mode);
			ArrayList<LatLng> directionPoint = md.getDirection(doc);
			PolylineOptions rectLine = new PolylineOptions().width(dirPar.size).color(dirPar.color);

			for(int i = 0 ; i < directionPoint.size() ; i++) {
				rectLine.add(directionPoint.get(i));
			}

			return(rectLine);
		}

		@Override
		protected void onPostExecute(PolylineOptions result) {
			directionPath = map.addPolyline(result);
		}
	}

}
