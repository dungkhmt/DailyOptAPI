package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualImportExportRoutesComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route1;
	private TruckRoute route2;
	private ImportContainerRequest imReq;
	private ExportContainerRequest exReq;
	private TruckRouteInfo4Request tri1;
	private TruckRouteInfo4Request tri2;
	private double distance;
	
	
	

	public IndividualImportExportRoutesComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route1,
			TruckRoute route2, ImportContainerRequest imReq,
			ExportContainerRequest exReq, TruckRouteInfo4Request tri1,
			TruckRouteInfo4Request tri2, double distance) {
		super();
		this.solver = solver;
		this.route1 = route1;
		this.route2 = route2;
		this.imReq = imReq;
		this.exReq = exReq;
		this.tri1 = tri1;
		this.tri2 = tri2;
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
		solver.addRoute(tri1.route, tri1.lastUsedIndex);
		solver.addRoute(tri2.route, tri2.lastUsedIndex);
	}

}
