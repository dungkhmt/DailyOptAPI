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
	public ArrayList<Point> lst_pickup;
	public ArrayList<Point> lst_delivery;
	public PickupDeliverySolver solver;
	
	public VehicleTrip(Vehicle vehicle, ArrayList<Point> points, int nbOrders, double load, double distance, PickupDeliverySolver solver){
		this.solver = solver;
		this.vehicle = vehicle;
		seqPoints = points;
		this.nbOrders = nbOrders;
		this.load= load;
		this.distance = distance;
		lst_pickup = new ArrayList<Point>();
		lst_delivery = new ArrayList<Point>();
		for(int i = 0; i < seqPoints.size(); i++){
			if(solver.mPoint2Type.get(seqPoints.get(i)).equals("D"))
				lst_delivery.add(seqPoints.get(i));
			else if(solver.mPoint2Type.get(seqPoints.get(i)).equals("P"))
				lst_pickup.add(seqPoints.get(i));
		}
	}
	public ArrayList<Point> getPickupSeqPoints(){
		return lst_pickup;
		/*
		ArrayList<Point> L = new ArrayList<Point>();
		for(int i=0; i < seqPoints.size(); i++){
			if(solver.mPoint2Type.get(seqPoints.get(i)).equals("P"))
				L.add(seqPoints.get(i));
		}
		return L;
		*/
	}
	public ArrayList<Point> getDeliverySeqPoints(){
		return lst_delivery;
		/*
		ArrayList<Point> L = new ArrayList<Point>();
		for(int i=0; i < seqPoints.size(); i++){
			if(solver.mPoint2Type.get(seqPoints.get(i)).equals("D"))
				L.add(seqPoints.get(i));
		}
		return L;
		*/
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
