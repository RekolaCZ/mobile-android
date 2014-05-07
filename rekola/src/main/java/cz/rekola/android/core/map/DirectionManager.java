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
	private PolylineOptions rectLine;
	private int loadedPathId;

	public DirectionManager(GoogleMap map) {
		this.map = map;
		loadedPathId = Integer.MIN_VALUE;
	}

	public void loadDirections(int id, DirectionParams params) {
		if (id == loadedPathId)
			return;

		loadedPathId = id;
		cancelTasks();

		directionTask = new DownloadDirectionsTask();
		directionTask.execute(params);
	}

	public void addDirectionsIfAvailable(int id) {
		if (loadedPathId != id)
			return;

		if (rectLine == null)
			return;

		directionPath = map.addPolyline(rectLine);
	}

	public void hideDirections() {
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
			if (result == null) {
				loadedPathId = Integer.MIN_VALUE;
				directionPath = null;
				rectLine = null;
				return;
			}
			rectLine = result;
			addDirectionsIfAvailable(loadedPathId);
		}
	}

}