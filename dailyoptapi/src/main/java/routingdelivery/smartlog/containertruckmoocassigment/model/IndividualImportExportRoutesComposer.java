package routingdelivery.smartlog.containertruckmoocassigment.model;

public class IndividualImportExportRoutesComposer implements RouteComposer {
	private TruckRoute route1;
	private TruckRoute route2;
	private ImportContainerRequest imReq;
	private ExportContainerRequest exReq;
	private double distance;
	
	
	public IndividualImportExportRoutesComposer(TruckRoute route1,
			TruckRoute route2, ImportContainerRequest imReq,
			ExportContainerRequest exReq, double distance) {
		super();
		this.route1 = route1;
		this.route2 = route2;
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
