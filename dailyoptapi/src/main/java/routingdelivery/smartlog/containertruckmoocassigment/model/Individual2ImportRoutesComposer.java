package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class Individual2ImportRoutesComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route1;
	private TruckRoute route2;
	private ImportContainerRequest imReq1;
	private ImportContainerRequest imReq2;
	private TruckRouteInfo4Request tri1;
	private TruckRouteInfo4Request tri2;
	private double distance;
	
	
	public Individual2ImportRoutesComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route1,
			TruckRoute route2, ImportContainerRequest imReq1,
			ImportContainerRequest imReq2, TruckRouteInfo4Request tri1,
			TruckRouteInfo4Request tri2, double distance) {
		super();
		this.solver = solver;
		this.route1 = route1;
		this.route2 = route2;
		this.imReq1 = imReq1;
		this.imReq2 = imReq2;
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
		solver.markServed(imReq1);
		solver.markServed(imReq2);
		solver.addRoute(tri1.route, tri1.lastUsedIndex);
		solver.addRoute(tri2.route, tri2.lastUsedIndex);
	}

}
