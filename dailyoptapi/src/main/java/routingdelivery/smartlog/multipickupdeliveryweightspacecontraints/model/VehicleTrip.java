package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import java.util.ArrayList;



import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service.PickupDeliverySolver;
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
	public boolean contains(Point p){
		for(Point x: seqPoints)
			if(x == p) return true;
		return false;
	}
	public ArrayList<ArrayList<Point>> computeDeliveryPointsSameLocation(){
		ArrayList<ArrayList<Point>> L = new ArrayList<ArrayList<Point>>();
		int i = 0;
		while(i < seqPoints.size()){
			Point p = seqPoints.get(i);
			//System.out.println("i = " + i);
			if(solver.mPoint2Type.get(p).equals("P")){i++; continue;}
			String lc = solver.mPoint2LocationCode.get(p);
			
			ArrayList<Point> Lp = new ArrayList<Point>();
			Lp.add(p);
			int j = i+1;
			while(j < seqPoints.size()){
				//System.out.println("j = " + j);
				Point q = seqPoints.get(j);
				if(solver.mPoint2LocationCode.get(q).equals(lc)){
					Lp.add(q);
					j++;
				}else{
					break;
				}				
			}
			L.add(Lp);
			i = j;
		}
		return L;
	}
	public Point getLastPickupPoint(){
		if(lst_pickup == null || lst_pickup.size() == 0) return null;
		return lst_pickup.get(lst_pickup.size()-1);
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
	
	public static void main(String[] args){
		int[] a= new int[]{1};
		int i = 0; 
		while(i < a.length){
			System.out.print(a[i] + " ");
			int j = i+1;
			while(j < a.length){
				if(a[i] == a[j]){
					System.out.print(a[j] + " ");
					j++;
				}else{
					break;
				}
			}
			System.out.println();
			i = j;
		}
	}
}
