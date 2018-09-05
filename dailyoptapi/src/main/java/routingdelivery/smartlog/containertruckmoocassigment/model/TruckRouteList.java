package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TruckRouteList {
	private Truck truck;
	private double distance;
	private int nbRoutes;
	private TruckRoute[] routes;
	
	public Truck getTruck() {
		return truck;
	}

	public void setTruck(Truck truck) {
		this.truck = truck;
	}

	public int getNbRoutes() {
		return nbRoutes;
	}

	public void setNbRoutes(int nbRoutes) {
		this.nbRoutes = nbRoutes;
	}

	public double getDistance() {
		return distance;
	}
	
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public TruckRoute[] getRoutes() {
		return routes;
	}
	public void setRoutes(TruckRoute[] routes) {
		this.routes = routes;
	}
	
	

	public TruckRouteList(Truck truck, double distance, int nbRoutes,
			TruckRoute[] routes) {
		super();
		this.truck = truck;
		this.distance = distance;
		this.nbRoutes = nbRoutes;
		this.routes = routes;
	}

	public TruckRouteList() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
