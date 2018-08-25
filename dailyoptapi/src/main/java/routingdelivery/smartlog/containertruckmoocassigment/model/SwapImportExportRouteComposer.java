package routingdelivery.smartlog.containertruckmoocassigment.model;

public class SwapImportExportRouteComposer implements RouteComposer {
	private Truck truck;
	private Mooc mooc;
	private ExportContainerRequest exReq;
	private ImportContainerRequest imReq;
	private double distance;
	
	
	public SwapImportExportRouteComposer(Truck truck, Mooc mooc,
			ExportContainerRequest exReq, ImportContainerRequest imReq,
			double distance) {
		super();
		this.truck = truck;
		this.mooc = mooc;
		this.exReq = exReq;
		this.imReq = imReq;
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
