package routingdelivery.model;

public class RoutingLoad3DSolution {
	private RoutingSolution[] routes;
	private LoadingSolution[] loads;
	public RoutingSolution[] getRoutes() {
		return routes;
	}
	public void setRoutes(RoutingSolution[] routes) {
		this.routes = routes;
	}
	public LoadingSolution[] getLoads() {
		return loads;
	}
	public void setLoads(LoadingSolution[] loads) {
		this.loads = loads;
	}
	public RoutingLoad3DSolution(RoutingSolution[] routes,
			LoadingSolution[] loads) {
		super();
		this.routes = routes;
		this.loads = loads;
	}
	public RoutingLoad3DSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
