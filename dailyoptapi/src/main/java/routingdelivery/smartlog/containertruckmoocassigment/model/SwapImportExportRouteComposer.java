package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class SwapImportExportRouteComposer implements RouteComposer {
	InitGreedyImproveSpecialOperatorSolver solver;
	private Truck truck;
	private Mooc mooc;
	private ExportContainerRequest exReq;
	private ImportContainerRequest imReq;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	
	

	public SwapImportExportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, Truck truck,
			Mooc mooc, ExportContainerRequest exReq,
			ImportContainerRequest imReq, TruckRouteInfo4Request tri,
			double distance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.exReq = exReq;
		this.imReq = imReq;
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
		solver.markServed(exReq);
		solver.markServed(imReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
	}

	public String name(){
		return "SwapImportExportRouteComposer";
	}
}
