package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;
import routingdelivery.smartlog.containertruckmoocassigment.service.KepGenerator;

public class KepRouteComposer implements RouteComposer {
	private InitGreedyImproveSpecialOperatorSolver solver;
	private Measure ms;
	private KepGenerator kg;
	private TruckRouteInfo4Request tri;
	private double distance;
	

	public KepRouteComposer(InitGreedyImproveSpecialOperatorSolver solver, Measure ms,
			KepGenerator kg, TruckRouteInfo4Request tri, double distance) {
		super();
		this.solver = solver;
		this.ms = ms;
		this.kg = kg;
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
		markServed();
		solver.addRoute(tri.route, tri.lastUsedIndex);
		solver.logln(name() + "::acceptRoute " + tri.route.toString());
		
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
		return "KepLechRouteComposer";
	}
	public void markServed(){
		if(kg.exportRequest != null)
			solver.markServed(kg.exportRequest);
		if(kg.importRequest != null)
			solver.markServed(kg.importRequest);
		if(kg.exportRequestKep != null)
			solver.markServed(kg.exportRequestKep);
		if(kg.importRequestKep != null)
			solver.markServed(kg.importRequestKep);
		if(kg.warehouseRequest != null)
			solver.markServed(kg.warehouseRequest);
		if(kg.warehouseRequestKep != null)
			solver.markServed(kg.warehouseRequestKep);
		if(kg.exportLadenRequest != null)
			solver.markServed(kg.exportLadenRequest);
		if(kg.exportEmptyRequest != null)
			solver.markServed(kg.exportEmptyRequest);
		if(kg.importLadenRequest != null)
			solver.markServed(kg.importLadenRequest);
		if(kg.importEmptyRequest != null)
			solver.markServed(kg.importEmptyRequest);
		if(kg.exportLadenRequestKep != null)
			solver.markServed(kg.exportLadenRequestKep);
		if(kg.exportEmptyRequestKep != null)
			solver.markServed(kg.exportEmptyRequestKep);
		if(kg.importLadenRequestKep != null)
			solver.markServed(kg.importLadenRequestKep);
		if(kg.importEmptyRequestKep != null)
			solver.markServed(kg.importEmptyRequestKep);
	}

}
