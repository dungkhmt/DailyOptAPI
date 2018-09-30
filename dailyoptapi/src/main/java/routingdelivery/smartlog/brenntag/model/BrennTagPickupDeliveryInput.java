package routingdelivery.smartlog.brenntag.model;

import routingdelivery.model.ConfigParams;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.Vehicle;

public class BrennTagPickupDeliveryInput extends PickupDeliveryInput{
	private DistanceElement[] travelTime;
	private ExclusiveItem[] exclusiveItemPairs;// cap 2 items khong the van chuyen cung nhau
	private ExclusiveVehicleLocation[] exclusiveVehicleLocations;// xe ko the di den location
	private Vehicle[] externalVehicles;// xe thau ngoai
	private Vehicle[] vehicleCategories;
	private ExclusiveVehicleLocation[] exclusiveVehicleCategoryLocations;
	
	
	public boolean findVehicleLocationConflict(String vehicleCode, String locationCode){
		for(int i = 0; i < exclusiveVehicleLocations.length; i++){
			if(exclusiveVehicleLocations[i].getVehicleCode().equals(vehicleCode)
					&& exclusiveVehicleLocations[i].getLocationCode().equals(locationCode))
				return true;
		}
		return false;
	}

	
	public ExclusiveVehicleLocation[] getExclusiveVehicleCategoryLocations() {
		return exclusiveVehicleCategoryLocations;
	}

	public void setExclusiveVehicleCategoryLocations(
			ExclusiveVehicleLocation[] exclusiveVehicleCategoryLocations) {
		this.exclusiveVehicleCategoryLocations = exclusiveVehicleCategoryLocations;
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances,
			ConfigParams params, DistanceElement[] travelTime,
			ExclusiveItem[] exclusiveItemPairs,
			ExclusiveVehicleLocation[] exclusiveVehicleLocations,
			Vehicle[] externalVehicles, Vehicle[] vehicleCategories,
			ExclusiveVehicleLocation[] exclusiveVehicleCategoryLocations) {
		super(requests, vehicles, distances, params);
		this.travelTime = travelTime;
		this.exclusiveItemPairs = exclusiveItemPairs;
		this.exclusiveVehicleLocations = exclusiveVehicleLocations;
		this.externalVehicles = externalVehicles;
		this.vehicleCategories = vehicleCategories;
		this.exclusiveVehicleCategoryLocations = exclusiveVehicleCategoryLocations;
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances,
			ConfigParams params, DistanceElement[] travelTime,
			ExclusiveItem[] exclusiveItemPairs,
			ExclusiveVehicleLocation[] exclusiveVehicleLocations,
			Vehicle[] externalVehicles, Vehicle[] vehicleCategories) {
		super(requests, vehicles, distances, params);
		this.travelTime = travelTime;
		this.exclusiveItemPairs = exclusiveItemPairs;
		this.exclusiveVehicleLocations = exclusiveVehicleLocations;
		this.externalVehicles = externalVehicles;
		this.vehicleCategories = vehicleCategories;
	}

	public Vehicle[] getVehicleCategories() {
		return vehicleCategories;
	}

	public void setVehicleCategories(Vehicle[] vehicleCategories) {
		this.vehicleCategories = vehicleCategories;
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances,
			ConfigParams params, DistanceElement[] travelTime,
			ExclusiveItem[] exclusiveItemPairs,
			ExclusiveVehicleLocation[] exclusiveVehicleLocations,
			Vehicle[] externalVehicles) {
		super(requests, vehicles, distances, params);
		this.travelTime = travelTime;
		this.exclusiveItemPairs = exclusiveItemPairs;
		this.exclusiveVehicleLocations = exclusiveVehicleLocations;
		this.externalVehicles = externalVehicles;
	}

	public DistanceElement[] getTravelTime() {
		return travelTime;
	}

	public void setTravelTime(DistanceElement[] travelTime) {
		this.travelTime = travelTime;
	}

	public ExclusiveItem[] getExclusiveItemPairs() {
		return exclusiveItemPairs;
	}

	public void setExclusiveItemPairs(ExclusiveItem[] exclusiveItemPairs) {
		this.exclusiveItemPairs = exclusiveItemPairs;
	}

	public ExclusiveVehicleLocation[] getExclusiveVehicleLocations() {
		return exclusiveVehicleLocations;
	}

	public void setExclusiveVehicleLocations(
			ExclusiveVehicleLocation[] exclusiveVehicleLocations) {
		this.exclusiveVehicleLocations = exclusiveVehicleLocations;
	}

	public Vehicle[] getExternalVehicles() {
		return externalVehicles;
	}

	public void setExternalVehicles(Vehicle[] externalVehicles) {
		this.externalVehicles = externalVehicles;
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances,
			ConfigParams params, DistanceElement[] travelTime) {
		super(requests, vehicles, distances, params);
		this.travelTime = travelTime;
	}

	public BrennTagPickupDeliveryInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, ConfigParams params) {
		super(requests, vehicles, params);
		// TODO Auto-generated constructor stub
	}

	public BrennTagPickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances, ConfigParams params) {
		super(requests, vehicles, distances, params);
		// TODO Auto-generated constructor stub
	}
	
	public String analyzeDistanceAndTravelTime(){
		double maxD = -1;
		double maxT = -1;
		double minD = Integer.MAX_VALUE;
		double minT = Integer.MAX_VALUE;
		
		for(int k = 0; k < this.travelTime.length; k++){
			DistanceElement de = this.travelTime[k];
			String src = de.getSrcCode();
			String dest = de.getDestCode();
			if(!src.equals(dest)){
				if(minT > de.getDistance()) minT = de.getDistance();
				if(maxT < de.getDistance()) maxT = de.getDistance();
			}
		}
	
		for(int k = 0; k < this.getDistances().length; k++){
			DistanceElement de = this.getDistances()[k];
			String src = de.getSrcCode();
			String dest = de.getDestCode();
			if(!src.equals(dest)){
				if(minD > de.getDistance()) minD = de.getDistance();
				if(maxD < de.getDistance()) maxD = de.getDistance();
			}
		}
	
		String rs = "minD = " + minD + ", maxD = " + maxD + ", minT = " + minT + ", maxT = " + maxT;
		return rs;
	}
}
