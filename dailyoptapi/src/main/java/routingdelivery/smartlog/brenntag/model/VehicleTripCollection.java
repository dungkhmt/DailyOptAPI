package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;
import java.util.HashMap;

public class VehicleTripCollection {
	public HashMap<VehicleTrip, Integer> mTrip2Route;
	public ArrayList<VehicleTrip> trips;
	
	public VehicleTripCollection(HashMap<VehicleTrip, Integer> mTrip2Route,
			ArrayList<VehicleTrip> trips) {
		super();
		this.mTrip2Route = mTrip2Route;
		this.trips = trips;
	}
	
}
