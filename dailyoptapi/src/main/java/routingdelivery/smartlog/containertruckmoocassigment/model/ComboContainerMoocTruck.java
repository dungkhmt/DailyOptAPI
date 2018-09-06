package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.ContainerTruckMoocSolver;
import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class ComboContainerMoocTruck {
	ContainerTruckMoocSolver solver;
	
	public Truck truck;
	public Mooc mooc;
	public Container container;
	public String lastLocationCode;// location where these 3 components are together
	public int startTime;// startTime where these combo can work
	public RouteElement routeElement;// (if not-null) the element of service route
	public TruckRoute truckRoute;// (if not-null) the route of truck in service
	public double extraDistance;// extra distance raised to obtain the combo
	public ComboContainerMoocTruck(ContainerTruckMoocSolver solver, Truck truck, Mooc mooc, Container container,
			String lastLocationCode, int startTime, RouteElement routeElement,
			TruckRoute truckRoute, double extraDistance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.lastLocationCode = lastLocationCode;
		this.startTime = startTime;
		this.routeElement = routeElement;
		this.truckRoute = truckRoute;
		this.extraDistance = extraDistance;
	}
	
	public String toString(){
		String s = "combo: ";
		String truckCode = "NULL";
		String truckLocation = "NULL";
		if(truck != null){
			truckCode = truck.getCode();
			truckLocation = solver.mTruck2LastDepot.get(truck).getLocationCode();
		}
		String moocCode = "NULL";
		String moocLocation = "NULL";
		if(mooc != null){
			moocCode = mooc.getCode();
			moocLocation = solver.mMooc2LastDepot.get(mooc).getLocationCode();
		}
		String containerCode = "NULL";
		String containerLocation = "NULL";
		if(container != null){
			containerCode = container.getCode();
			containerLocation = solver.mContainer2LastDepot.get(container).getLocationCode();
		}
		s = "truck " + truckCode + " at " + truckLocation + 
				", mooc " + moocCode + " at " + moocLocation + 
				", container " + containerCode + " at " + containerLocation;
		
		return s;
	}
	
	
}
