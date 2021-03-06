package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class IndividualImportWarehouseRoutesComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Measure ms1;
	private Measure ms2;
	private TruckRoute route1;
	private TruckRoute route2;
	private WarehouseContainerTransportRequest whReq;
	private ImportContainerRequest imReq;
	private TruckRouteInfo4Request tri1;
	private TruckRouteInfo4Request tri2;
	private double distance;
	
	

	public IndividualImportWarehouseRoutesComposer(
			InitGreedyImproveSpecialOperatorSolver solver,
			Measure ms1, Measure ms2,TruckRoute route1,
			TruckRoute route2, WarehouseContainerTransportRequest whReq,
			ImportContainerRequest imReq, TruckRouteInfo4Request tri1,
			TruckRouteInfo4Request tri2, double distance) {
		super();
		this.solver = solver;
		this.ms1 = ms1;
		this.ms2 = ms2;
		this.route1 = route1;
		this.route2 = route2;
		this.whReq = whReq;
		this.imReq = imReq;
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
		solver.markServed(imReq);
		solver.markServed(whReq);
		solver.addRoute(tri1.route, tri1.lastUsedIndex);
		solver.addRoute(tri2.route, tri2.lastUsedIndex);
		
		for(Truck trk: tri1.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri1.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri1.getLastTimeTruck(trk));
			solver.updateTruckAtDepot(trk);
		}
		for(Mooc mooc: tri1.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri1.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri1.getLastTimeMooc(mooc));
			solver.updateMoocAtDepot(mooc);
		}
		for(Container container: tri1.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri1.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri1.getLastTimeContainer(container));
			solver.updateContainerAtDepot(container);
		}		
		
		for(Truck trk: tri2.mTruck2LastDepot.keySet()){
			solver.mTruck2LastDepot.put(trk, tri2.getLastDepotTruck(trk));
			solver.mTruck2LastTime.put(trk, tri2.getLastTimeTruck(trk));
			solver.updateTruckAtDepot(trk);
		}
		for(Mooc mooc: tri2.mMooc2LastDepot.keySet()){
			solver.mMooc2LastDepot.put(mooc, tri2.getLastDepotMooc(mooc));
			solver.mMooc2LastTime.put(mooc, tri2.getLastTimeMooc(mooc));
			solver.updateMoocAtDepot(mooc);
		}
		for(Container container: tri2.mContainer2LastDepot.keySet()){
			solver.mContainer2LastDepot.put(container, tri2.getLastDepotContainer(container));
			solver.mContainer2LastTime.put(container, tri2.getLastTimeContainer(container));
			solver.updateContainerAtDepot(container);
		}
		solver.updateDriverAccessWarehouse(ms1.driverId, ms1.wh);
		for(String key : ms1.srcdest.keySet())
			solver.updateDriverIsBalance(ms1.driverId, key, ms1.srcdest.get(key));
		solver.updateDriverAccessWarehouse(ms2.driverId, ms2.wh);
		for(String key : ms2.srcdest.keySet())
			solver.updateDriverIsBalance(ms2.driverId, key, ms2.srcdest.get(key));
	}

}
