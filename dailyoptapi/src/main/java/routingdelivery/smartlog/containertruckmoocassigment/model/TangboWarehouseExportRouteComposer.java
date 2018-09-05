package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class TangboWarehouseExportRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Truck truck;
	private Mooc mooc;
	private Container container;
	private TruckRoute route;
	private WarehouseContainerTransportRequest whReq;
	private ExportContainerRequest exReq;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	

	

	public TangboWarehouseExportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, Truck truck,
			Mooc mooc, Container container, TruckRoute route,
			WarehouseContainerTransportRequest whReq,
			ExportContainerRequest exReq, TruckRouteInfo4Request tri,
			double distance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.route = route;
		this.whReq = whReq;
		this.exReq = exReq;
		this.tri = tri;
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
		solver.markServed(whReq);
		solver.markServed(exReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
	}
	public String name(){
		return "TangboWarehouseExportRouteComposer";
	}
}
