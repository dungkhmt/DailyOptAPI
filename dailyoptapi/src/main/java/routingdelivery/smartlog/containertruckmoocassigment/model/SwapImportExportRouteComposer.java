package routingdelivery.smartlog.containertruckmoocassigment.model;

import routingdelivery.smartlog.containertruckmoocassigment.service.InitGreedyImproveSpecialOperatorSolver;

public class SwapImportExportRouteComposer implements RouteComposer {
	InitGreedyImproveSpecialOperatorSolver solver;
	private Truck truck;
	private Mooc mooc;
	private ExportContainerRequest exReq;
	private ImportContainerRequest imReq;
	private TruckRouteInfo4Request tri;
	private double distance;
	
	
	

	public SwapImportExportRouteComposer(
			InitGreedyImproveSpecialOperatorSolver solver, Truck truck,
			Mooc mooc, ExportContainerRequest exReq,
			ImportContainerRequest imReq, TruckRouteInfo4Request tri,
			double distance) {
		super();
		this.solver = solver;
		this.truck = truck;
		this.mooc = mooc;
		this.exReq = exReq;
		this.imReq = imReq;
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
	}

	public String name(){
		return "SwapImportExportRouteComposer";
	}
}
