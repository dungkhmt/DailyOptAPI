package routingdelivery.smartlog.containertruckmoocassigment.model;

public class IndividualImportRouteComposer implements RouteComposer {
	private TruckRoute route;
	private double distance;
	
	public IndividualImportRouteComposer(TruckRoute route, double distance) {
		super();
		this.route = route;
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
