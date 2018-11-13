package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import java.util.ArrayList;
import java.util.HashMap;



public class VehicleTripCollection {
	public HashMap<VehicleTrip, Integer> mTrip2Route;
	public ArrayList<VehicleTrip> trips;
	public HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips;
	
	
	public VehicleTripCollection(HashMap<VehicleTrip, Integer> mTrip2Route,
			ArrayList<VehicleTrip> trips,
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips) {
		super();
		this.mTrip2Route = mTrip2Route;
		this.trips = trips;
		this.mVehicle2Trips = mVehicle2Trips;
	}


	public VehicleTripCollection(HashMap<VehicleTrip, Integer> mTrip2Route,
			ArrayList<VehicleTrip> trips) {
		super();
		this.mTrip2Route = mTrip2Route;
		this.trips = trips;
	}
	
}
