package edu.ucla.cs.HighMobilitySearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ToggleButton;

public class HighMobilitySearchActivity extends MapActivity {
	private MapView mapView;    
	private MyLocationOverlay currentPositionOverlay;
	private FuturePositionOverlay futurePositionOverlay;
	private SearchResultOverlay searchResultOverlay;
	private List<Overlay> mapOverlays;

	private static final String TAG = "High Mobility Search";
	private static final String searchRadius = "500";
	private static final String searchQuery = "food";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// main.xml contains a MapView
		setContentView(R.layout.main);

		// extract MapView from layout
		mapView = (MapView) findViewById(R.id.mapView);
		mapView.setBuiltInZoomControls(true);
		mapView.setSatellite(true);

		// create an overlay that shows the current location
		currentPositionOverlay = new MyLocationOverlay(this, mapView);
		futurePositionOverlay = new FuturePositionOverlay(this, mapView);

		// add this overlay to MapView and refresh it
		mapView.getOverlays().add(currentPositionOverlay);
		mapView.postInvalidate();

		currentPositionOverlay.enableMyLocation();
		futurePositionOverlay.enableMyLocation();
		currentPositionOverlay.enableCompass();

		// call convenience method that zooms map on the current location
		currentPositionOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				zoomToCurrentLocation();
			}
		});

		// initialize search result overlays
		mapOverlays = mapView.getOverlays();
		mapOverlays.add(futurePositionOverlay);
		Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
		searchResultOverlay = new SearchResultOverlay(drawable, this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		currentPositionOverlay.enableMyLocation();
		futurePositionOverlay.enableMyLocation();
		currentPositionOverlay.enableCompass();

		// call convenience method that zooms map on the current location
		currentPositionOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				// TODO Auto-generated method stub
				zoomToCurrentLocation();
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();

		currentPositionOverlay.disableMyLocation();
		futurePositionOverlay.disableMyLocation();
		currentPositionOverlay.disableCompass();
	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	private void zoomToCurrentLocation() {
		GeoPoint myLocationGeoPoint = currentPositionOverlay.getMyLocation();
		if (myLocationGeoPoint != null) {
			mapView.getController().animateTo(myLocationGeoPoint);
			mapView.getController().setZoom(18);
		}
	}

	public void performSearch() {
		String request = "https://maps.googleapis.com/maps/api/place/search/json?location=" + futurePositionOverlay.getMyPredictedLatitude() + "," + futurePositionOverlay.getMyPredictedLongitude() + "&radius=" + searchRadius + "&keyword="+ searchQuery + "&sensor=false&key=AIzaSyCi74Yt7FMPTjTUMDHWFRLOYs6Q3hfDLII";
		
		Log.d(TAG, request);
		
		StringBuilder builder = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(request);
		
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) {
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
			} else {
				Log.e(TAG, "Failed to download file");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			JSONObject jsonObject = new JSONObject(builder.toString());
			JSONArray results = jsonObject.getJSONArray("results");
			Log.i(TAG, "Number of entries " + results.length());
			
			for (int i = 0; i < results.length(); i++) {
				JSONObject result = results.getJSONObject(i);
				Log.i(TAG, result.getString("name"));
				int latitude = (int) (Double.parseDouble(result.getJSONObject("geometry").getJSONObject("location").getString("lat")) * 1e6);
				int longitude = (int) (Double.parseDouble(result.getJSONObject("geometry").getJSONObject("location").getString("lng")) * 1e6);
				
				GeoPoint point = new GeoPoint(latitude, longitude);
			
				OverlayItem overlayItem = new OverlayItem(point, result.getString("name"), Integer.toString(i));
				
				searchResultOverlay.addOverlay(overlayItem);
			}
			
			mapOverlays.add(searchResultOverlay);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void onToggleClicked(View v) {
		// Perform action on clicks
		if (((ToggleButton) v).isChecked()) {
			performSearch();
		}
	}
}