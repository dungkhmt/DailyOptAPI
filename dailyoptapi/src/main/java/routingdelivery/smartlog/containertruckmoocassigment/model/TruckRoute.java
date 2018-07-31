package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.ArrayList;

public class TruckRoute {
	private Truck truck;
	private RouteElement[] nodes;
	
	public void concat(TruckRoute tr){
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		if(nodes != null)for(int i = 0; i < nodes.length; i++)
			L.add(nodes[i]);
		if(tr.getNodes() != null)
			for(int i = 0; i < tr.getNodes().length; i++)
				L.add(tr.getNodes()[i]);
		nodes = new RouteElement[L.size()];
		for(int i = 0; i < L.size(); i++)nodes[i] = L.get(i);
	}
	public void removeNodesAfter(RouteElement e){
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		for(int i = 0; i < nodes.length; i++){
			L.add(nodes[i]);
			if(nodes[i] == e){
				break;
			}
		}
		nodes = new RouteElement[L.size()];
		for(int i = 0; i < L.size(); i++)
			nodes[i] = L.get(i);
	}
	public void addNodes(ArrayList<RouteElement> L){
		ArrayList<RouteElement> tmp = new ArrayList<RouteElement>();
		for(int i = 0; i < nodes.length; i++) tmp.add(nodes[i]);
		for(int i = 0; i < L.size(); i++) tmp.add(L.get(i));
		nodes = new RouteElement[tmp.size()];
		for(int i=0; i < tmp.size(); i++)
			nodes[i]=tmp.get(i);
	}
	public RouteElement getLastNode(){
		if(nodes == null || nodes.length == 0) return null;
		return nodes[nodes.length-1];
	}
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
