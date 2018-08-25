package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.*;

public class CandidateSolution {
	private HashMap<Truck, TruckItinerary> mTruck2Itinerary;
	private HashMap<TruckRoute, ArrayList<ExportContainerRequest>> mRoute2ServedExportRequests;
	private HashMap<TruckRoute, ArrayList<ImportContainerRequest>> mRoute2ServedImportRequests;
	private HashMap<TruckRoute, ArrayList<WarehouseContainerTransportRequest>> mRoute2ServedWarehouseRequests;

	public HashMap<Truck, DepotTruck> mTruck2LastDepot;// map each truck to the
	// last depot (after
	// service plan)
	public HashMap<Truck, DepotTruck> bku_mTruck2LastDepot;

	public HashMap<Truck, Integer> mTruck2LastTime;// the time where the truck
	// is available at last
	// depot
	public HashMap<Truck, Integer> bku_mTruck2LastTime;

	public HashMap<Mooc, DepotMooc> mMooc2LastDepot;// map each mooc to the last
	// depot (after service
	// plan)
	public HashMap<Mooc, DepotMooc> bku_mMooc2LastDepot;

	public HashMap<Mooc, Integer> mMooc2LastTime;// the time where mooc is
	// available at the last
	// depot
	public HashMap<Mooc, Integer> bku_mMooc2LastTime;

	public HashMap<Container, DepotContainer> mContainer2LastDepot; // map each
	// container
	// to the
	// last
	// depot
	public HashMap<Container, DepotContainer> bku_mContainer2LastDepot;

	public HashMap<Container, Integer> mContainer2LastTime;// the time where
	// container is
	// available at the
	// last depot
	public HashMap<Container, Integer> bku_mContainer2LastTime;

	private double distance;

	public String name(){
		return "CandidateSolution";
	}
	public TruckItinerary getItineraryOfTruck(Truck tr){
		if(mTruck2Itinerary == null) return null;
		return mTruck2Itinerary.get(tr);
	}
	public void commitItinerary(TruckRouteInfo4Request tri){
		if(tri == null) return;
		System.out.println(name() + "::commitItinerary, route = " + tri.route.toString());
		Truck truck = tri.route.getTruck();
		TruckItinerary I = mTruck2Itinerary.get(truck);
		if(I == null){
			I = new TruckItinerary();
			mTruck2Itinerary.put(truck, I);
		}
		I.addRoute(tri.route, tri.lastUsedIndex);
	}

}
