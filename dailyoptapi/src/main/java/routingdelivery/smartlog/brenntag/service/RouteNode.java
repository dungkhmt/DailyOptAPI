package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;

import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.service.PickupDeliverySolver;
import utils.DateTimeUtils;



public class RouteNode {
	public int locationIndex;
	public int arrivalTime;
	public int departureTime;
	public ArrayList<ItemAmount> items;
	public int vehicleIndex;
	public String type;
	public BrenntagPickupDeliverySolver solver;
	public String description;
	public RouteNode(int locationIndex, int arrivalTime, int departureTime,
			ArrayList<ItemAmount> items, int vehicleIndex, String type) {
		super();
		this.locationIndex = locationIndex;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.items = items;
		this.vehicleIndex = vehicleIndex;
		this.type = type;
	}
	public boolean checkDeliveryTime(){
		long latesAllowedArrivalTime = Integer.MAX_VALUE;
		for(int i = 0; i < items.size(); i++){
			int rId = solver.mItemIndex2RequestIndex.get(items.get(i).itemIndex);
			PickupDeliveryRequest r = solver.requests[rId];
			long t = DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
			if(latesAllowedArrivalTime > t) latesAllowedArrivalTime = t;
		}
		return arrivalTime <= latesAllowedArrivalTime;
		
	}
	public boolean checkPickupTime(){
		long latesAllowedArrivalTime = Integer.MAX_VALUE;
		for(int i = 0; i < items.size(); i++){
			int rId = solver.mItemIndex2RequestIndex.get(items.get(i).itemIndex);
			PickupDeliveryRequest r = solver.requests[rId];
			long t = DateTimeUtils.dateTime2Int(r.getLatePickupTime());
			if(latesAllowedArrivalTime > t) latesAllowedArrivalTime = t;
		}
		return arrivalTime <= latesAllowedArrivalTime;
		
	}

	public int computeTotalLoad(){
		int load = 0;
		for(int i = 0; i < items.size(); i++)
			load += items.get(i).amount;
		return load;
	}
	
	public String toString(){
		String is = "";
		String locationCode = solver.locationCodes.get(locationIndex);
		
		for(ItemAmount ia: items){
			String itemCode = solver.items.get(ia.itemIndex).getCode();
			int r_idx = solver.mItemIndex2RequestIndex.get(ia.itemIndex);
			PickupDeliveryRequest r = solver.requests[r_idx];
			
			is += "(" + itemCode + "," + ia.amount + ") ";
		}
		return "location " + locationCode + ", arrT " + DateTimeUtils.unixTimeStamp2DateTime(arrivalTime) + ", depT " + 
				DateTimeUtils.unixTimeStamp2DateTime(departureTime) + ", vh " + vehicleIndex + " = " + 
		solver.getVehicle(vehicleIndex).getCode() + ", items " + is;
	}
}
