package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualWarehouseRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Measure ms;
	private TruckRoute route;
	private TruckRouteInfo4Request tri;
	private WarehouseContainerTransportRequest whReq;
	private double distance;
	

	public IndividualWarehouseRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, 
			Measure ms, TruckRoute route,
			TruckRouteInfo4Request tri,
			WarehouseContainerTransportRequest whReq, double distance) {
		super();
		this.solver = solver;
		this.ms = ms;
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
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
		TruckItinerary I = solver.mTruck2Itinerary.get(tri.route.getTruck());
		solver.logln(name() + "::acceptRoute, Itinerary = " + I.toString());
		for(Truck trk: tri.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri.getLastTimeTruck(trk));
			solver.updateTruckAtDepot(trk);			
		}
		for(Mooc mooc: tri.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri.getLastTimeMooc(mooc));
			solver.updateMoocAtDepot(mooc);
		}
		for(Container container: tri.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri.getLastTimeContainer(container));
			solver.updateContainerAtDepot(container);
		}	
		solver.updateDriverAccessWarehouse(ms.driverId, ms.wh);
		for(String key : ms.srcdest.keySet())
			solver.updateDriverIsBalance(ms.driverId, key, ms.srcdest.get(key));
	}
	public String name(){
		return "IndividualWarehouseRouteComposer";
	}
}
