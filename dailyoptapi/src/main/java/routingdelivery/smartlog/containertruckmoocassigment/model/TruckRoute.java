package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TruckRoute {
	private Truck truck;
	private RouteElement[] nodes;
	public Truck getTruck() {
		return truck;
	}
	public void setTruck(Truck truck) {
		this.truck = truck;
	}
	public RouteElement[] getNodes() {
		return nodes;
	}
	public void setNodes(RouteElement[] nodes) {
		this.nodes = nodes;
	}
	public TruckRoute(Truck truck, RouteElement[] nodes) {
		super();
		this.truck = truck;
		this.nodes = nodes;
	}
	public TruckRoute() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
