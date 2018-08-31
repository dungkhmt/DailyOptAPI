package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualExportRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route;
	private ExportContainerRequest exReq;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	

	

	public IndividualExportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route,
			ExportContainerRequest exReq, TruckRouteInfo4Request tri,
			double distance) {
		super();
		this.solver = solver;
		this.route = route;
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
		solver.markServed(exReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
	}

}
