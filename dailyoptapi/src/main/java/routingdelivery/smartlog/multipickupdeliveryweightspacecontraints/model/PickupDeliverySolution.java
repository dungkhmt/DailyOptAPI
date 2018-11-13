package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import java.util.ArrayList;



public class PickupDeliverySolution {
	private RoutingSolution[] routes;
	private Item[] unScheduledItems;
	private PickupDeliveryRequest[] unScheduledRequests;
	private String description;
	private String errorMSG;
	private StatisticInformation statistic;
	
	public PickupDeliverySolution(RoutingSolution[] routes,
			Item[] unScheduledItems,
			PickupDeliveryRequest[] unScheduledRequests, String description,
			String errorMSG, StatisticInformation statistic) {
		super();
		this.routes = routes;
		this.unScheduledItems = unScheduledItems;
		this.unScheduledRequests = unScheduledRequests;
		this.description = description;
		this.errorMSG = errorMSG;
		this.statistic = statistic;
	}
	public boolean checkLoadConstraint(){
		for(int i = 0; i < routes.length; i++){
			RoutingSolution r = routes[i];
			for(int j = 0; j < r.getElements().length; j++){
				RoutingElement e = r.getElements()[j];
				if(r.getVehicle().getWeight() < e.getLoad())
					return false;
			}
		}
		return true;
	}
	public StatisticInformation getStatistic() {
		return statistic;
	}
	public void setStatistic(StatisticInformation statistic) {
		this.statistic = statistic;
	}
	public String getErrorMSG() {
		return errorMSG;
	}
	public void setErrorMSG(String errorMSG) {
		this.errorMSG = errorMSG;
	}
	public PickupDeliverySolution(RoutingSolution[] routes,
			Item[] unScheduledItems,
			PickupDeliveryRequest[] unScheduledRequests, String description) {
		super();
		this.routes = routes;
		this.unScheduledItems = unScheduledItems;
		this.unScheduledRequests = unScheduledRequests;
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public PickupDeliveryRequest[] getUnScheduledRequests() {
		return unScheduledRequests;
	}
	public void setUnScheduledRequests(PickupDeliveryRequest[] unScheduledRequests) {
		this.unScheduledRequests = unScheduledRequests;
	}
	public PickupDeliverySolution(RoutingSolution[] routes,
			Item[] unScheduledItems, PickupDeliveryRequest[] unScheduledRequests) {
		super();
		this.routes = routes;
		this.unScheduledItems = unScheduledItems;
		this.unScheduledRequests = unScheduledRequests;
	}
	public void append(ArrayList<RoutingSolution> new_r){
		ArrayList<RoutingSolution> L = new ArrayList<RoutingSolution>();
		for(int i = 0; i < routes.length; i++)
			L.add(routes[i]);
		for(int i = 0; i < new_r.size(); i++)
			L.add(new_r.get(i));
		routes = new RoutingSolution[L.size()];
		for(int i = 0; i < L.size(); i++){
			routes[i] = L.get(i);
		}
	}
	public void append(RoutingSolution[] new_r){
		ArrayList<RoutingSolution> L = new ArrayList<RoutingSolution>();
		for(int i = 0; i < routes.length; i++)
			L.add(routes[i]);
		for(int i = 0; i < new_r.length; i++)
			L.add(new_r[i]);
		routes = new RoutingSolution[L.size()];
		for(int i = 0; i < L.size(); i++){
			routes[i] = L.get(i);
		}
	}

	public PickupDeliverySolution(RoutingSolution[] routes,
			Item[] unScheduledItems) {
		super();
		this.routes = routes;
		this.unScheduledItems = unScheduledItems;
	}
	
	public void insertHead(ArrayList<RoutingSolution> L){
		ArrayList<RoutingSolution> tmp = new ArrayList<RoutingSolution>();
		//double distance = 0;
		for(int i = 0; i < L.size(); i++){
			tmp.add(L.get(i));
			//distance += L.get(i).getDistance();
		}
		//if(L.size() > 0)
		//	distance = L.get(L.size()-1).getDistance();
		
		for(int i = 0; i < routes.length; i++){
			tmp.add(routes[i]);
			//routes[i].setDistance(distance + routes[i].getDistance());
			//System.out.println("route[" + i + "].setDistance(" + routes[i].getDistance() + "), base distance = " + distance);
			
		}
		routes = new RoutingSolution[tmp.size()];
		for(int i=0; i < tmp.size(); i++)
			routes[i] = tmp.get(i);
	}
	public RoutingSolution getRoute(String vehicleCode){
		for(int i = 0; i < routes.length; i++)
			if(routes[i].getVehicle().getCode().equals(vehicleCode))
				return routes[i];
		return null;
	}
	public Item[] getUnScheduledItems() {
		return unScheduledItems;
	}

	public void setUnScheduledItems(Item[] unScheduledItems) {
		this.unScheduledItems = unScheduledItems;
	}

	public RoutingSolution[] getRoutes() {
		return routes;
	}

	public void setRoutes(RoutingSolution[] routes) {
		this.routes = routes;
	}

	public PickupDeliverySolution(RoutingSolution[] routes) {
		super();
		this.routes = routes;
	}

	public PickupDeliverySolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
