package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TruckRoute {
	private Truck truck;
	private RouteElement[] nodes;
	
	public double getDistance(){
		if(nodes == null || nodes.length == 0) return 0;
		return nodes[nodes.length-1].getDistance();
	}
	public double getDistanceSubRoute(int i, int j){
		if(nodes == null || nodes.length == 0) return 0;
		if(i >= nodes.length || j >= nodes.length || i >= j) return 0;
		return nodes[j].getDistance() - nodes[i].getDistance();
	}
	public double getDistanceFromBeginToPosition(int i){
		return getDistanceSubRoute(0, i);
	}
	public double getDistanceFromPositionToEnd(int i){
		if(nodes == null || nodes.length == 0) return 0;
		return getDistanceSubRoute(i,nodes.length-1);
	}
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
