package edu.ucla.cs.HighMobilitySearch;

import java.text.DecimalFormat;
import java.util.List;

import javax.vecmath.Vector2d;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class FuturePositionOverlay extends Overlay implements LocationListener {
	private static final String TAG = "High Mobility Search";
	private static final int ARRAY_SIZE = 10;

	private final MapView mapView;
	private boolean myLocationEnabled = false;
	private LocationManager locationManager;
	private static final int futureTime = 60; // 60 Seconds, or 1 minutes into the future
	private Location[] locationHistory = new Location[ARRAY_SIZE];
	private Location prevLocation = null;
	private Location futureLocation = null;
	private Paint paint = new Paint();
	private static final double earthRadiusMeters = 6378100.0;
	
	private boolean rollover = false;
	private int index = 0;

	public FuturePositionOverlay(Context context, MapView mapView) {
		this.mapView = mapView;
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	public boolean isMyLocationEnabled() {
		return myLocationEnabled;
	}

	public synchronized boolean enableMyLocation() {
		List<String> providers = locationManager.getAllProviders();

		if (providers.contains(LocationManager.GPS_PROVIDER)) {
			myLocationEnabled = true;
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 2L, this);
		} else if (providers.contains(LocationManager.NETWORK_PROVIDER)) {
			myLocationEnabled = true;
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 2L, this);
		} else {
			myLocationEnabled = false;
		}

		return myLocationEnabled;
	}

	public synchronized void disableMyLocation() {
		if (myLocationEnabled) 
			locationManager.removeUpdates(this);
		myLocationEnabled = false;
	}

	public void onLocationChanged (Location location) {
		Log.d(TAG, "TRUE LOCATION: " + location.getLatitude() + ", " + location.getLongitude());
		
		/** If no previous location, future Location is just the current user location */
		futureLocation = new Location(location);

		/** Do we have a previous location to calculate a vector from?
		 *  Each vector is calculated between the previous point and the current point */
		if (prevLocation != null) {

			/** Create a new vector based on the latitude and longitude we calculated.
			 *  Normalize the vector using the difference in time between the two locations. */
			locationHistory[index] = new Location(location);

			/** Increment the locationVector array */
			if (index + 1 < ARRAY_SIZE) { 
				index++;
			}
			else {
				/** We have rolled over */
				rollover = true;
				index = 0;
			}

			/** Has a rollover occurred? */
			int upperBound = 0;
			if (rollover)
				upperBound = ARRAY_SIZE;
			else
				upperBound = index;

			/** Add up all the vectors, put the result into trueVector
			 *  Also, add up all the lengths (magnitudes) of the vectors to
			 *  get the average speed of the user */
			double avgBearing = 0.0, avgSpeed = 0.0, tempDist = 0.0, numerator = 0.0, denominator = 0.0, tempBearing=0.0;
			int numBearing = 0;
			for (int i = 0; i < upperBound; i++) {
				if (locationHistory[i].hasBearing() && i > 0) {
					tempBearing=locationHistory[i-1].bearingTo(locationHistory[i]);
					if(tempBearing < 0)
						tempBearing += 360;
					avgBearing += tempBearing;
					numBearing++;
				}
				
				if (locationHistory[i].hasSpeed() && i > 0) {
					tempDist = locationHistory[i].distanceTo(locationHistory[i-1]);
					numerator += tempDist;
					denominator += tempDist / locationHistory[i].getSpeed();
				}
			}
			
			avgBearing = avgBearing / numBearing;
			
			avgSpeed = numerator / denominator;
						
			Log.d(TAG, "Average Bearing: " + avgBearing + ", Average Speed: " + avgSpeed);

			/** trueVector is the sum of all vectors in our history.  It is the vector
			 *  we will use, along with the average speed, to predict the futureLocation of the user. 
			 */

			/** Calculate the Distance (miles) */
			double predictedDistance = avgSpeed * futureTime;

			Log.d(TAG, "Predicted Distance (meters): " + predictedDistance);
			
			Projection projection = mapView.getProjection();
			GeoPoint geoPoint = new GeoPoint ((int) (location.getLatitude() * 1e6), (int) (location.getLongitude() * 1e6));
			Point point = projection.toPixels(geoPoint, null);
			
			double theta = (avgBearing - (90.0 * ((int) avgBearing) / 90));
			
			if (avgBearing > 0 && avgBearing < 90) {
				point.x += predictedDistance * Math.sin(theta);
				point.y += predictedDistance * Math.cos(theta);
			}
			else if (avgBearing > 90 && avgBearing < 180) {
				point.x += predictedDistance * Math.cos(theta);
				point.y -= predictedDistance * Math.sin(theta);
			}
			else if (avgBearing > 180 && avgBearing < 270) {
				point.x -= predictedDistance * Math.sin(theta);
				point.y -= predictedDistance * Math.cos(theta);
			}
			else if (avgBearing > 270 && avgBearing < 360) {
				point.x -= predictedDistance * Math.cos(theta);
				point.y += predictedDistance * Math.sin(theta);
			}
			else if (avgBearing == 90) {
				point.x += predictedDistance;
			}
			else if (avgBearing == 180) {
				point.y -= predictedDistance;
			}
			else if (avgBearing == 270) {
				point.x -= predictedDistance;
			}
			else if (avgBearing == 360) {
				point.y += predictedDistance;
			}
			else {
				Log.e(TAG, "Bearing Error");
			}
			
			Log.d(TAG, "x: " + point.x + " y: " + point.y);
			
			GeoPoint geoPoint2 = projection.fromPixels(point.x, point.y);
			
			double futureLatitude = ((double) geoPoint2.getLatitudeE6()) / 1e6;
			double futureLongitude = ((double) geoPoint2.getLongitudeE6()) / 1e6;
			
			if (!Double.isNaN(futureLatitude) && !Double.isNaN(futureLongitude)) {
				futureLocation.setLatitude(futureLatitude);
				futureLocation.setLongitude(futureLongitude);
			}

			Log.d(TAG, "Future Location: " + futureLocation.getLatitude() + ", " + futureLocation.getLongitude());
		}

		/** Current Location becomes Previous Location */
		prevLocation = location;
	}

	public GeoPoint getMyPredictedLocation() {
		return new GeoPoint((int) (futureLocation.getLatitude() * 1e6), (int) (futureLocation.getLongitude() * 1e6));
	}

	public String getMyPredictedLatitude() {
		return Double.toString(futureLocation.getLatitude());
	}

	public String getMyPredictedLongitude() {
		return Double.toString(futureLocation.getLongitude());
	}

	@Override
	public synchronized boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
		if (!shadow) {
			if (isMyLocationEnabled() && futureLocation != null) {
				drawMyLocation(canvas, mapView, futureLocation, getMyPredictedLocation(), when);
			}
		}

		return false;
	}

	protected void drawMyLocation(Canvas canvas, MapView mapView, Location lastFix, GeoPoint myLocation, long when) {
		Projection p = mapView.getProjection();
		float accuracy = p.metersToEquatorPixels(lastFix.getAccuracy());
		Point loc = p.toPixels(myLocation, null);
		paint.setAntiAlias(true);
		paint.setColor(Color.BLUE);
		
		if (accuracy > 10.0f) {
			paint.setAlpha(50);
			canvas.drawCircle(loc.x, loc.y, accuracy, paint);
		}
		
		paint.setAlpha(255);
		canvas.drawCircle(loc.x, loc.y, 10, paint);
	}

	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub

	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub

	}
}