package routingdelivery.smartlog.sem.model;

public class SEMPickupDeliverySolution {
	private SEMRoutingSolution[] routes;
	private SEMPickupDeliveryRequest[] unServedRequests;
	
	public SEMPickupDeliverySolution(SEMRoutingSolution[] routes,
			SEMPickupDeliveryRequest[] unServedRequests) {
		super();
		this.routes = routes;
		this.unServedRequests = unServedRequests;
	}

	public SEMPickupDeliveryRequest[] getUnServedRequests() {
		return unServedRequests;
	}

	public void setUnServedRequests(SEMPickupDeliveryRequest[] unServedRequests) {
		this.unServedRequests = unServedRequests;
	}

	public SEMRoutingSolution[] getRoutes() {
		return routes;
	}

	public void setRoutes(SEMRoutingSolution[] routes) {
		this.routes = routes;
	}

	public SEMPickupDeliverySolution(SEMRoutingSolution[] routes) {
		super();
		this.routes = routes;
	}

	public SEMPickupDeliverySolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
