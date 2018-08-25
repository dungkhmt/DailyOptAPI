package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Individual2ImportRoutesComposer implements RouteComposer {
	private TruckRoute route1;
	private TruckRoute route2;
	private ImportContainerRequest imReq1;
	private ImportContainerRequest imReq2;
	private double distance;
	
	public Individual2ImportRoutesComposer(TruckRoute route1,
			TruckRoute route2, ImportContainerRequest imReq1,
			ImportContainerRequest imReq2, double distance) {
		super();
		this.route1 = route1;
		this.route2 = route2;
		this.imReq1 = imReq1;
		this.imReq2 = imReq2;
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
