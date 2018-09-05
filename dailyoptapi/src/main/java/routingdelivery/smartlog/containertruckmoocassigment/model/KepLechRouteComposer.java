package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class KepLechRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Truck truck;
	private Mooc mooc;
	private Container container;
	private ImportContainerRequest imReq;
	private ExportContainerRequest exReq;
	private TruckRouteInfo4Request tri;
	private double distance;
	

	public KepLechRouteComposer(InitGreedyImproveSpecialOperatorSolver solver,
			Truck truck, Mooc mooc, Container container,
			ImportContainerRequest imReq, ExportContainerRequest exReq,
			TruckRouteInfo4Request tri, double distance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.container = container;
		this.imReq = imReq;
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
		solver.markServed(imReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
	}
	public String name(){
		return "KepLechRouteComposer";
	}

}
