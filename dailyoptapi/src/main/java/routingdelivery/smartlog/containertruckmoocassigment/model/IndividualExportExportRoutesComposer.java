package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualExportExportRoutesComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private TruckRoute route1;
	private TruckRoute route2;
	private ExportContainerRequest exReq1;
	private ExportContainerRequest exReq2;
	private TruckRouteInfo4Request tri1;
	private TruckRouteInfo4Request tri2;
	private double distance;
	
	
	

	public IndividualExportExportRoutesComposer(
			InitGreedyImproveSpecialOperatorSolver solver, TruckRoute route1,
			TruckRoute route2, ExportContainerRequest exReq1,
			ExportContainerRequest exReq2, TruckRouteInfo4Request tri1,
			TruckRouteInfo4Request tri2, double distance) {
		super();
		this.solver = solver;
		this.route1 = route1;
		this.route2 = route2;
		this.exReq1 = exReq1;
		this.exReq2 = exReq2;
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
		solver.markServed(exReq1);
		solver.markServed(exReq2);
		solver.addRoute(tri1.route, tri1.lastUsedIndex);
		solver.addRoute(tri2.route, tri2.lastUsedIndex);
		
		for(Truck trk: tri1.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri1.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri1.getLastTimeTruck(trk));
		}
		for(Mooc mooc: tri1.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri1.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri1.getLastTimeMooc(mooc));
		}
		for(Container container: tri1.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri1.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri1.getLastTimeContainer(container));
		}		
		
		for(Truck trk: tri2.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri2.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri2.getLastTimeTruck(trk));
		}
		for(Mooc mooc: tri2.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri2.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri2.getLastTimeMooc(mooc));
		}
		for(Container container: tri2.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri2.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri2.getLastTimeContainer(container));
		}		
		
	}
}
