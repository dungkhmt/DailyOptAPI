package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class DoubleImportRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Truck truck;
	private Mooc mooc;
	private TruckRoute route;
	private ImportContainerRequest importRequest1;
	private ImportContainerRequest importRequest2;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	
	public DoubleImportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, Truck truck,
			Mooc mooc, TruckRoute route, ImportContainerRequest importRequest1,
			ImportContainerRequest importRequest2, TruckRouteInfo4Request tri,
			double distance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.route = route;
		this.importRequest1 = importRequest1;
		this.importRequest2 = importRequest2;
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
		solver.markServed(importRequest1);
		solver.markServed(importRequest2);
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
		for(Truck trk: tri.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri.getLastTimeTruck(trk));
		}
		for(Mooc mooc: tri.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri.getLastTimeMooc(mooc));
		}
		for(Container container: tri.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri.getLastTimeContainer(container));
		}		
	}
	public String name(){
		return "DoubleImportRouteComposer";
	}
}
