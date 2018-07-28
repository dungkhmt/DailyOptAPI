package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;

import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.Vehicle;
import routingdelivery.service.PickupDeliverySolver;
import utils.DateTimeUtils;

public class Trip {
	public RouteNode start;
	public RouteNode end;
	public String type;
	public Vehicle vh;
	BrenntagPickupDeliverySolver solver;
	
	public Trip(RouteNode start, RouteNode end, String type) {
		super();
		this.start = start;
		this.end = end;
		this.type = type;
		this.solver = start.solver;
	}
	
	public boolean checkTime(){
		return start.checkPickupTime() && end.checkDeliveryTime();
	}
	public ArrayList<Item> getItems(){
		ArrayList<Item> IT = new ArrayList<Item>();
		for(ItemAmount ia: start.items){
			Item I = solver.items.get(ia.itemIndex);
			IT.add(I);
		}
		return IT;
	}
	public double computeTotalItemWeight(){
		double totalW = 0;
		for(ItemAmount ia: start.items){
			totalW += ia.amount;
		}
		return totalW;
	}
	public String toStringShort(){
		String s = "";
		Vehicle vh = solver.getVehicle(start.vehicleIndex);
		s = s + vh.getCode() + ", weight = " + vh.getWeight();
		s = s + "\n" + start.toStringShort() + " -> " + end.toStringShort();
		String is = "";
		double totalW = 0;
		for(ItemAmount ia: start.items){
			Item I = solver.items.get(ia.itemIndex);
			is = is + "[" + I.getCode() + ", weight " + ia.amount + "],";
			totalW += ia.amount;
		}
		s = s + "\n" + is + ", totalW = " + totalW;
		return s;
	}
	public String toString(){
		String s = "type = " + type + ", startNode " + start.toString() + "\nendNode" + end.toString() + "\n";
		double travelTime = solver.a_travelTime[start.locationIndex][end.locationIndex];
		String sTravelTime = DateTimeUtils.second2HMS((int)travelTime);
		
		for(ItemAmount ia: end.items){
			String itemCode = end.solver.items.get(ia.itemIndex).getCode();
			int r_idx = end.solver.mItemIndex2RequestIndex.get(ia.itemIndex);
			PickupDeliveryRequest r = end.solver.requests[r_idx];
			s = s + "late-delivery[orderID " + r.getOrderID() + ", itemID " + end.solver.items.get(ia.itemIndex).getCode() + "] = " + r.getLateDeliveryTime() + "\n";
		}		
		s = s + "Travel-Time from " + solver.locationCodes.get(start.locationIndex) + " --> " + solver.locationCodes.get(end.locationIndex) + " = " + sTravelTime;
		s = s + "\npickupDuration = " + start.description;
		s = s + "\ndeliveryDuration = " + end.description; 
		return s;
	}
	
}
