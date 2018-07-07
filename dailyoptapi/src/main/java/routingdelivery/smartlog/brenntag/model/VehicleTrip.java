package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;

import routingdelivery.model.Vehicle;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

public class VehicleTrip {
	// single trip from pickups to deliveries
	public Vehicle vehicle;
	public ArrayList<Point> seqPoints;
	public int nbOrders;
	public double load;
	public double distance;
	
	public VehicleTrip(Vehicle vehicle, ArrayList<Point> points, int nbOrders, double load, double distance){
		this.vehicle = vehicle;
		seqPoints = points;
		this.nbOrders = nbOrders;
		this.load= load;
		this.distance = distance;
	}
}
