package routingdelivery.model;

public class PickupDeliveryInput {
	private PickupDeliveryRequest[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	private ConfigParams params;
	
	public PickupDeliveryRequest[] cloneRequests(){
		PickupDeliveryRequest[] req = new PickupDeliveryRequest[requests.length];
		for(int i = 0; i < req.length; i++)
			req[i] = requests[i].clone();
		return req;
	}
	public PickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, DistanceElement[] distances, ConfigParams params) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.distances = distances;
		this.params = params;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	public PickupDeliveryRequest[] getRequests() {
		return requests;
	}
	public void setRequests(PickupDeliveryRequest[] requests) {
		this.requests = requests;
	}
	public Vehicle[] getVehicles() {
		return vehicles;
	}
	public void setVehicles(Vehicle[] vehicles) {
		this.vehicles = vehicles;
	}
	public ConfigParams getParams() {
		return params;
	}
	public void setParams(ConfigParams params) {
		this.params = params;
	}
	public PickupDeliveryInput(PickupDeliveryRequest[] requests,
			Vehicle[] vehicles, ConfigParams params) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.params = params;
	}
	public PickupDeliveryInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
