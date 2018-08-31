package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualWarehouseRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route;
	private TruckRouteInfo4Request tri;
	private WarehouseContainerTransportRequest whReq;
	private double distance;
	

	public IndividualWarehouseRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route,
			TruckRouteInfo4Request tri,
			WarehouseContainerTransportRequest whReq, double distance) {
		super();
		this.solver = solver;
		this.route = route;
		this.tri = tri;
		this.whReq = whReq;
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
		solver.addRoute(tri.route, tri.lastUsedIndex);
	}

}
