package routingdelivery.model;

public class RoutingLoad3DInput {
	private Request[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	private Depot depot;
	private int maxNbTrips;
	
	private ConfigParams configParams;
	
	
	public int getMaxNbTrips() {
		return maxNbTrips;
	}
	public void setMaxNbTrips(int maxNbTrips) {
		this.maxNbTrips = maxNbTrips;
	}
	public ConfigParams getConfigParams() {
		return configParams;
	}
	public void setConfigParams(ConfigParams configParams) {
		this.configParams = configParams;
	}
	public Depot getDepot() {
		return depot;
	}
	public void setDepot(Depot depot) {
		this.depot = depot;
	}
	public Request[] getRequests() {
		return requests;
	}
	public void setRequests(Request[] requests) {
		this.requests = requests;
	}
	public Vehicle[] getVehicles() {
		return vehicles;
	}
	public void setVehicles(Vehicle[] vehicles) {
		this.vehicles = vehicles;
	}
	public DistanceElement[] getDistances() {
		return distances;
	}
	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}
	
	
	public RoutingLoad3DInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public RoutingLoad3DInput(Request[] requests, Vehicle[] vehicles,
			DistanceElement[] distances, Depot depot, int maxNbTrips,
			ConfigParams configParams) {
		super();
		this.requests = requests;
		this.vehicles = vehicles;
		this.distances = distances;
		this.depot = depot;
		this.maxNbTrips = maxNbTrips;
		this.configParams = configParams;
	}
	
	
}
