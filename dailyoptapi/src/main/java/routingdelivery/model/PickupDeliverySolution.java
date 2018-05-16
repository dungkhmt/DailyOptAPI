package routingdelivery.model;

public class PickupDeliverySolution {
	private RoutingSolution[] routes;

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
