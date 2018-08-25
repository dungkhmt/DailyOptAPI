package routingdelivery.smartlog.containertruckmoocassigment.model;

public class KepLechRouteComposer implements RouteComposer {
	private Truck truck;
	private Mooc mooc;
	private Container container;
	private ImportContainerRequest imReq;
	private ExportContainerRequest exReq;
	private double distance;
	public KepLechRouteComposer(Truck truck, Mooc mooc, Container container,
			ImportContainerRequest imReq, ExportContainerRequest exReq, double distance) {
		super();
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.imReq = imReq;
		this.exReq = exReq;
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
