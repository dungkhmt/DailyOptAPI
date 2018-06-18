package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;

import utils.DateTimeUtils;



public class RouteNode {
	public int locationIndex;
	public int arrivalTime;
	public int departureTime;
	public ArrayList<ItemAmount> items;
	public int vehicleIndex;
	public String type;
	
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
	public int computeTotalLoad(){
		int load = 0;
		for(int i = 0; i < items.size(); i++)
			load += items.get(i).amount;
		return load;
	}
	public String toString(){
		String is = "";
		for(ItemAmount ia: items) is += "(" + ia.itemIndex + "," + ia.amount + ") ";
		return "location " + locationIndex + ", arrT " + DateTimeUtils.unixTimeStamp2DateTime(arrivalTime) + ", depT " + 
				DateTimeUtils.unixTimeStamp2DateTime(departureTime) + ", vh " + vehicleIndex + ", items " + is;
	}
}
