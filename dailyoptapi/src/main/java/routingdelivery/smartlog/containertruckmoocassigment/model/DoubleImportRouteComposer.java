package routingdelivery.smartlog.containertruckmoocassigment.model;

public class DoubleImportRouteComposer implements RouteComposer {
	private Truck truck;
	private Mooc mooc;
	private TruckRoute route;
	private ImportContainerRequest importRequest1;
	private ImportContainerRequest importRequest2;
	private double distance;
	
	public DoubleImportRouteComposer(Truck truck, Mooc mooc, TruckRoute route,
			ImportContainerRequest importRequest1,
			ImportContainerRequest importRequest2, double distance) {
		super();
		this.truck = truck;
		this.mooc = mooc;
		this.route = route;
		this.importRequest1 = importRequest1;
		this.importRequest2 = importRequest2;
		this.distance = distance;
	}

	@Override
	public void commitRoute() {
		// TODO Auto-generated method stub

	}

	@Override
	public double evaluation() {
		// TODO Auto-generated method stub
		return distance;
	}

	@Override
	public void acceptRoute() {
		// TODO Auto-generated method stub

	}

}
