package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.HashMap;

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
		Truck truck = tri.route.getTruck();
		solver.markServed(exReq);
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
		TruckItinerary I = solver.mTruck2Itinerary.get(truck);
		solver.logln(name() + "::acceptRoute, Itinerary = " + I.toString());
		for(Truck trk: tri.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri.getLastTimeTruck(trk));
		}
		for(Mooc mooc: tri.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri.getLastTimeMooc(mooc));
		}
		for(Container container: tri.mContainer2LastDepot.keySet()){
			if(tri.getLastDepotContainer(container) != null)
				solver.mContainer2LastDepot.put(container, tri.getLastDepotContainer(container));
			//if(solver.mContainer2LastTime == null)
			//	solver.mContainer2LastTime = new HashMap<Container, Integer>();
			
				solver.mContainer2LastTime.put(container, tri.getLastTimeContainer(container));
		}		
	}
	public String name(){
		return "IndividualExportRouteComposer";
	}
}
