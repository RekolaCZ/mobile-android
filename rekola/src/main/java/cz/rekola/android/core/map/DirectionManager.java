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

	public void loadDirections(DirectionParams params) {
		if (loadedPathId == params.id && rectLine != null) {
			if (directionPath != null)
				directionPath.remove();
			directionPath = map.addPolyline(rectLine);
		}

		if (loadedPathId != params.id) {
			directionTask = new DownloadDirectionsTask();
			directionTask.execute(params);
		}
	}

	public void clearDirections() {
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
			clearDirections();
			directionPath = map.addPolyline(result);
			rectLine = result;
			loadedPathId = dirPar.id;
		}
	}

}
