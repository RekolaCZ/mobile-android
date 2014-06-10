package cz.rekola.app.core.map;

import android.os.AsyncTask;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;

import java.util.ArrayList;

public class DirectionManager {

	private DownloadDirectionsTask directionTask;
	private Polyline directionPath;
	private PolylineOptions rectLine;
	private DirectionsLoadedListener callback;

	public DirectionManager(DirectionsLoadedListener callback) {
		this.callback = callback;
	}

	public void loadDirections(int id, DirectionParams params) {
		cancelTasks();

		directionTask = new DownloadDirectionsTask();
		directionTask.execute(params);
	}

	public void addDirections(GoogleMap map) {
		if (rectLine == null)
			return;

		if (directionPath != null) {
			directionPath.remove();
		}

		directionPath = map.addPolyline(rectLine);
	}

	public void hideDirections() {
		cancelTasks();
		rectLine = null;
		if (directionPath != null) {
			directionPath.remove();
			directionPath = null;
		}
	}

	private void cancelTasks() {
		if (directionTask != null) {
			directionTask.cancel(true);
			callback.onDirectionsError();
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

			if (doc == null)
				return null;

			ArrayList<LatLng> directionPoint = md.getDirection(doc);
			PolylineOptions rectLine = new PolylineOptions().width(dirPar.size).color(dirPar.color);

			for(int i = 0 ; i < directionPoint.size() ; i++) {
				rectLine.add(directionPoint.get(i));
			}

			return rectLine;
		}

		@Override
		protected void onPostExecute(PolylineOptions result) {
			directionTask = null;
			if (result == null) {
				if (directionPath != null) {
					directionPath.remove();
				}
				directionPath = null;
				rectLine = null;
				callback.onDirectionsError();
				return;
			}
			rectLine = result;
			callback.onDirectionsLoaded();
		}
	}

	public interface DirectionsLoadedListener {
		public void onDirectionsLoaded();
		public void onDirectionsError();
	}
}
