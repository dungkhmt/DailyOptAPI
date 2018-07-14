package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;

import routingdelivery.model.Vehicle;
import routingdelivery.service.PickupDeliverySolver;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

public class VehicleTrip {
	// single trip from pickups to deliveries
	public Vehicle vehicle;
	public ArrayList<Point> seqPoints;
	public int nbOrders;
	public double load;
	public double distance;
	public PickupDeliverySolver solver;
	
	public VehicleTrip(Vehicle vehicle, ArrayList<Point> points, int nbOrders, double load, double distance){
		this.vehicle = vehicle;
		seqPoints = points;
		this.nbOrders = nbOrders;
		this.load= load;
		this.distance = distance;
	}
	public void setSolver(PickupDeliverySolver solver){
		this.solver = solver;
	}
	public String seqPointString(){
		String s = "";
		for(int i = 0; i < seqPoints.size(); i++){
			s = s + seqPoints.get(i).ID + "[" + solver.mPoint2Type.get(seqPoints.get(i)) + "], ";
		}
		return s;
	}
}
