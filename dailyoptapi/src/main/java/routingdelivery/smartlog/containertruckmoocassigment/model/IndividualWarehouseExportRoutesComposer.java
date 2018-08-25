package routingdelivery.smartlog.containertruckmoocassigment.model;

public class IndividualWarehouseExportRoutesComposer implements RouteComposer {
	private TruckRoute route1;
	private TruckRoute route2;
	private WarehouseContainerTransportRequest whReq;
	private ExportContainerRequest exReq;
	private double distance;
	
	
	public IndividualWarehouseExportRoutesComposer(TruckRoute route1,
			TruckRoute route2, WarehouseContainerTransportRequest whReq,
			ExportContainerRequest exReq, double distance) {
		super();
		this.route1 = route1;
		this.route2 = route2;
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
