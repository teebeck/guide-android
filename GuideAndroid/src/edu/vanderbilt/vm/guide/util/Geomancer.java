package edu.vanderbilt.vm.guide.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.vanderbilt.vm.guide.annotations.NeedsTesting;
import edu.vanderbilt.vm.guide.container.Place;
import edu.vanderbilt.vm.guide.db.GuideDBConstants;
import edu.vanderbilt.vm.guide.ui.listener.GeomancerListener;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Provide methods related to Geolocation and positioning. - at the start of the
 * application, call activateGeolocation() which returns void and accepts no
 * argument. - when the device's location is needed, call getDeviceLocation()
 * which accepts no argument. It returns a Location object which can then be fed
 * into findClosestPlace() which returns a Place object.
 * 
 * These are array-transversal procedures, so there may be conflicts with the
 * SQL approach
 * 
 * @author abdulra1
 */
@NeedsTesting(lastModifiedDate = "12/22/12")
public class Geomancer {

	private static final Logger logger = LoggerFactory
			.getLogger("util.Geomancer");
	private static final double DEFAULT_LONGITUDE = -86.803889;
	private static final double DEFAULT_LATITUDE = 36.147381;

	private static Location sCurrLocation;
	private static LocationManager sLocationManager;
	private static LocationListener mLocListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			// Called when a new location is found
			// by the network location provider.
			sCurrLocation = location;
			logger.info("Receiving location at lat/lon {},{}",
					location.getLatitude(), location.getLongitude());

			alertTheGuards(location);

		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};
	private final static int DEFAULT_RADIUS = 5; // 5 meters, for you americans
													// out there.
	private final static int DEFAULT_TIMEOUT = 5000;
	private static Location sDefaultLocation;
	private static ArrayList<GeomancerListener> mPadawan = new ArrayList<GeomancerListener>();

	/**
	 * Returns a Place which has the closest coordinate to the given Location. I
	 * made it take a List<Place> because someone might need to find a Place in
	 * a custom List, like the nearest academic building or the nearest buiding
	 * that has a tornado shelter (GASP!!!)
	 * 
	 * @param location
	 * @param placeList
	 * @return
	 * @deprecated Use the version of findClosestPlace() that takes a
	 *             placeCursor instead. Note that this version returns the index of
	 *             the place, not the place itself.
	 */
	@Deprecated
	public static Place findClosestPlace(Location location,
			List<Place> placeList) {
		if (placeList.size() == 0) {
			return null;
		}

		Place currPlace = placeList.get(0);
		double currDist = findDistance(currPlace.getLatitude(),
				currPlace.getLongitude(), location.getLatitude(),
				location.getLongitude());
		int closestIx = 0;

		for (int i = 1; i < placeList.size(); i++) {
			currPlace = placeList.get(i);
			double dist = findDistance(currPlace.getLatitude(),
					currPlace.getLongitude(), location.getLatitude(),
					location.getLongitude());
			if (dist < currDist) {
				currDist = dist;
				closestIx = i;
			}
		}

		Place result = placeList.get(closestIx);
		logger.debug("Closest is {} at position {}", result.getName(),
				closestIx);
		return placeList.get(closestIx);
	}

	/**
	 * Find the closest place in the placeCursor to the given location
	 * 
	 * @param location
	 *            The location to find the closest place to
	 * @param placeCursor
	 *            The cursor containing the places to search for
	 * @return The position in the cursor of the closest place
	 */
	public static int findClosestPlace(Location location, Cursor placeCursor) {
		if (!placeCursor.moveToFirst()) {
			// Cursor was empty
			return -1;
		}

		int latIx = placeCursor
				.getColumnIndex(GuideDBConstants.PlaceTable.LATITUDE_COL);
		int lonIx = placeCursor
				.getColumnIndex(GuideDBConstants.PlaceTable.LONGITUDE_COL);

		if (latIx == -1 || lonIx == -1) {
			throw new SQLException(
					"Place cursor must have a lat and lon column");
		}

		double lat = placeCursor.getDouble(latIx);
		double lon = placeCursor.getDouble(lonIx);
		double shortestDist = findDistance(location.getLatitude(),
				location.getLongitude(), lat, lon);
		int closestIx = 0;

		while (placeCursor.moveToNext()) {
			lat = placeCursor.getDouble(latIx);
			lon = placeCursor.getDouble(lonIx);
			double dist = findDistance(location.getLatitude(),
					location.getLongitude(), lat, lon);
			if (dist < shortestDist) {
				shortestDist = dist;
				closestIx = placeCursor.getPosition();
			}
		}

		return closestIx;
	}

	/**
	 * Setup the mechanism for determining device location. this method is
	 * called by GuideMain on application loading.
	 * 
	 * Any activity that needs the device's location simply need to call
	 * getDeviceLocation()
	 */
	public static void activateGeolocation(Context ctx) {

		if (sLocationManager == null) {
			// Acquire a reference to the system Location Manager
			sLocationManager = (LocationManager) ctx
					.getSystemService(Context.LOCATION_SERVICE);
		}
		
		String provider = sLocationManager.getBestProvider(getCriteriaA(), true);
		if (provider != null) {
			sLocationManager.requestLocationUpdates(provider, 
					DEFAULT_TIMEOUT, DEFAULT_RADIUS, mLocListener);
		}
		
		/*
		List<String> matchingProviders = sLocationManager.getProviders(
				getCriteriaA(), false);
		logger.trace("Found {} providers.", matchingProviders.size());
		if (!matchingProviders.isEmpty()) {
			String provider = matchingProviders.get(0);
			sLocationManager.requestLocationUpdates(provider, DEFAULT_TIMEOUT,
					DEFAULT_RADIUS, mLocListener);
		} else {
			sLocationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, DEFAULT_TIMEOUT,
					DEFAULT_RADIUS, mLocListener);
		}
		*/
		
		
		logger.trace("Geolocation init done.");
	}

	public static double findDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	/**
	 * Gives the device location. The method is guaranteed to never return null.
	 * It always delivers d=(^o^d=)
	 * 
	 * @return device's location
	 */
	public static Location getDeviceLocation() {
		
		if (sCurrLocation == null) {
			sCurrLocation = sLocationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		}
		if (sCurrLocation == null) {
			sCurrLocation = sLocationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		if (sCurrLocation == null) {
			sCurrLocation = sLocationManager
					.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
		}
		if (sCurrLocation == null) {
			sDefaultLocation.setTime((new Date()).getTime());
			sCurrLocation = sDefaultLocation;
		}

		return sCurrLocation;
	}

	/**
	 * Provide Geomancer with fresh information on device location. This is
	 * intended for later on when we may have alternative methods to get that
	 * information other than GPS and radio.
	 * 
	 * @param loc
	 *            Current device Location
	 */
	public static void setDeviceLocation(Location loc) {
		sCurrLocation = loc;
		alertTheGuards(loc);
	}

	static {
		// Set up the default location
		sDefaultLocation = new Location("Dummy");
		sDefaultLocation.setLatitude(DEFAULT_LATITUDE);
		sDefaultLocation.setLongitude(DEFAULT_LONGITUDE);
	}

	/**
	 * In order to get update on device location, make the Activity implement
	 * GeomancerListener and call: Geomancer.registerGeomancerListener(this)
	 * 
	 * Make sure to call removeGeomancerListener() inside the onPause() 
	 * callback in order to avoid memory leak.
	 * 
	 * @param listener
	 *            The Activity that implements GeomancerListener
	 */
	public static void registerGeomancerListener(GeomancerListener listener) {
		mPadawan.add(listener);
		
		if (mPadawan.size() == 1) {
			String provider = sLocationManager.getBestProvider(getCriteriaA(), true);
			if (provider != null) {
				sLocationManager.requestLocationUpdates(provider, 
						DEFAULT_TIMEOUT, DEFAULT_RADIUS, mLocListener);
			}
		}
	}

	public static void removeGeomancerListener(GeomancerListener listener) {
		mPadawan.remove(listener);
		
		if (mPadawan.isEmpty()) {
			sLocationManager.removeUpdates(mLocListener);
		}
	}

	private static void alertTheGuards(Location loc) {
		for (GeomancerListener anakin : mPadawan) {
			anakin.updateLocation(loc);
		}
	}
	
	private static Criteria getCriteriaA() {
		Criteria crit = new Criteria();
		crit.setAccuracy(Criteria.ACCURACY_FINE);
		crit.setAltitudeRequired(false);
		crit.setBearingRequired(false);
		crit.setSpeedRequired(false);
		crit.setCostAllowed(true);
		return crit;
	}
	
	
	
	/*
	 * SUMMARY OF PATH FINDING IMPLEMENTATION
	 * 
	 * 	class Node {
	 * 	private:
	 * 		double score
	 * 		final int id
	 * 		final double latitude
	 * 		final double longitude
	 * 		final int[] neighbours
	 * 		int previous
	 * 	public:
	 * 		double getScore()
	 * 		int getId()
	 * 		Graph getNeighbours()
	 * 		double distanceTo(Node)
	 * 		
	 * 		void setScore(double)
	 * 		void setPrevious(Node)
	 * 	}
	 * 	
	 * 	class Graph extends ArrayList<Node> {
	 * 	public:
	 * 		Node getNodeWithLowestScore()
	 * 	}
	 * 
	 */
	
	/*
	static Graph findPath(Graph g, Node start, Node end) {
		// Assert that "start" and "end" are elements of "g"
		// Assert that Graph is a typedef of Arraylist<Node>
		
		// Initialization routine
		for (Node node : g) {
			if (node.getId() == start.getId()) {
				node.setScore(0);
			} else {
				node.setScore(Double.MAX_VALUE);
			}
			node.setPrevious(null);
		}
		
		// Create a set of nodes not yet examined. This initially contain all
		// the nodes in "g"
		Graph unvisited = g.clone();
		
		// This is the bulk of the algorithm
		while (!unvisited.isEmpty()) {
			
			Node u = unvisited.getNodeWithLowestScore();
			unvisited.remove(u);
			if (u.getId() == end.getId()) {
				break;
			}
			
			if (u.getScore() == Double.MAX_VALUE) {
				break;
			}
			
			for (Node neigh : u.getNeighbours()) { // getNeighbours() returns a Graph object
				
				if (!unvisited.contains(neigh)) {
					continue;
				}
				
				double dist = u.getScore() + u.distanceTo(neigh);
				if (dist < neigh.getScore()) {
					neigh.setScore(dist);
					neigh.setPrevious(u);
					
				}
			}
		}
		
		// backtracing the path
		
		// Since "start" and "end" are elements of "g", They should have received
		// the result of the algorithm run
		Graph path = Graph.getEmptySet(); 	// just use the default constructor
											// here if there's no issue
		Node prev = end.getPrevious();
		while (prev != null) {
			path.add(0, prev);
			prev = prev.getPrevious();
		}
		
		return path
	}
	*/
	
	
	
	
	
}
