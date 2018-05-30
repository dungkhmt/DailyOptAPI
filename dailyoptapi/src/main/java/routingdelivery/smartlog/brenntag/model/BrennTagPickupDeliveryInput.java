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
	
}
