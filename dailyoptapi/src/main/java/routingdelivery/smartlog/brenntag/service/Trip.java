package routingdelivery.smartlog.brenntag.service;

import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.service.PickupDeliverySolver;
import utils.DateTimeUtils;

public class Trip {
	public RouteNode start;
	public RouteNode end;
	public String type;
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
