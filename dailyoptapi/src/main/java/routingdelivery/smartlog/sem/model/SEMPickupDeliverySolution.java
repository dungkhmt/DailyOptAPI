package routingdelivery.smartlog.sem.model;

public class SEMPickupDeliverySolution {
	private SEMRoutingSolution[] routes;

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
