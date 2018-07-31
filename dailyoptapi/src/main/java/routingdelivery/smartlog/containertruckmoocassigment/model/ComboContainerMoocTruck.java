package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ComboContainerMoocTruck {
	public Truck truck;
	public Mooc mooc;
	public Container container;
	public String lastLocationCode;// location where these 3 components are together
	public int startTime;// startTime where these combo can work
	public RouteElement routeElement;// (if not-null) the element of service route
	public TruckRoute truckRoute;// (if not-null) the route of truck in service
	public double extraDistance;// extra distance raised to obtain the combo
	public ComboContainerMoocTruck(Truck truck, Mooc mooc, Container container,
			String lastLocationCode, int startTime, RouteElement routeElement,
			TruckRoute truckRoute, double extraDistance) {
		super();
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.lastLocationCode = lastLocationCode;
		this.startTime = startTime;
		this.routeElement = routeElement;
		this.truckRoute = truckRoute;
		this.extraDistance = extraDistance;
	}
	
	
	
	
}
