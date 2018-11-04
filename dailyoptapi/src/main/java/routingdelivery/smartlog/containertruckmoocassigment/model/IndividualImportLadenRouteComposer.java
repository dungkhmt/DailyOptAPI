package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualImportLadenRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route;
	private ImportLadenRequests req;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	
	public IndividualImportLadenRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route,
			ImportLadenRequests req, TruckRouteInfo4Request tri, double distance) {
		super();
		this.solver = solver;
		this.route = route;
		this.req = req;
		this.tri = tri;
		this.distance = distance;
	}


	public InitGreedyImproveSpecialOperatorSolver getSolver() {
		return solver;
	}


	public void setSolver(InitGreedyImproveSpecialOperatorSolver solver) {
		this.solver = solver;
	}


	public TruckRoute getRoute() {
		return route;
	}


	public void setRoute(TruckRoute route) {
		this.route = route;
	}


	public ImportLadenRequests getReq() {
		return req;
	}


	public void setReq(ImportLadenRequests req) {
		this.req = req;
	}


	public TruckRouteInfo4Request getTri() {
		return tri;
	}


	public void setTri(TruckRouteInfo4Request tri) {
		this.tri = tri;
	}


	public double getDistance() {
		return distance;
	}


	public void setDistance(double distance) {
		this.distance = distance;
	}


	public IndividualImportLadenRouteComposer(){
		
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
	public String name(){
		return "IndividualImportLadenRouteComposer";
	}
	@Override
	public void acceptRoute() {
		// TODO Auto-generated method stub
		Truck truck = tri.route.getTruck();
		solver.markServed(req);
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
			solver.mContainer2LastDepot.put(container, tri.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri.getLastTimeContainer(container));
		}		

	}

}
