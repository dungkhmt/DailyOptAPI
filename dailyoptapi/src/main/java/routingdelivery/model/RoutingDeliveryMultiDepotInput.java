package routingdelivery.model;

public class RoutingDeliveryMultiDepotInput {
	private Request[] requests;
	private DistanceElement[] distances;
	private Depot[] depots;
	
	private ConfigParams configParams;

	public Request[] getRequests() {
		return requests;
	}

	public void setRequests(Request[] requests) {
		this.requests = requests;
	}

	public DistanceElement[] getDistances() {
		return distances;
	}

	public void setDistances(DistanceElement[] distances) {
		this.distances = distances;
	}

	public Depot[] getDepots() {
		return depots;
	}

	public void setDepots(Depot[] depots) {
		this.depots = depots;
	}

	public ConfigParams getConfigParams() {
		return configParams;
	}

	public void setConfigParams(ConfigParams configParams) {
		this.configParams = configParams;
	}

	public RoutingDeliveryMultiDepotInput(Request[] requests,
			DistanceElement[] distances, Depot[] depots,
			ConfigParams configParams) {
		super();
		this.requests = requests;
		this.distances = distances;
		this.depots = depots;
		this.configParams = configParams;
	}

	public RoutingDeliveryMultiDepotInput() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
