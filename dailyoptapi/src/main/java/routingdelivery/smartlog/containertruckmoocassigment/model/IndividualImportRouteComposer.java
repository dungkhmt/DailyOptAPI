package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualImportRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route;
	private TruckRouteInfo4Request tri;
	private ImportContainerRequest imReq;
	private double distance;
	

	public IndividualImportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route,
			TruckRouteInfo4Request tri, ImportContainerRequest imReq,
			double distance) {
		super();
		this.solver = solver;
		this.route = route;
		this.tri = tri;
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
		solver.markServed(imReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
	}

}
