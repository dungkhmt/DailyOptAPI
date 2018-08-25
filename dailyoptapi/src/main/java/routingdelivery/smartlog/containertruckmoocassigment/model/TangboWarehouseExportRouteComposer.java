package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TangboWarehouseExportRouteComposer implements RouteComposer {
	private Truck truck;
	private Mooc mooc;
	private Container container;
	private TruckRoute route;
	private WarehouseContainerTransportRequest whReq;
	private ExportContainerRequest exReq;
	private double distance;
	
	
	
	public TangboWarehouseExportRouteComposer(Truck truck, Mooc mooc,
			Container container, TruckRoute route,
			WarehouseContainerTransportRequest whReq,
			ExportContainerRequest exReq, double distance) {
		super();
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.route = route;
		this.whReq = whReq;
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
