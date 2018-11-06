package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.HashMap;

public class TruckRouteInfo4Request {
	public TruckRoute route;
	public int lastUsedIndex;
	public double additionalDistance;// additional distance when accept this
										// truck route

	public HashMap<Truck, DepotTruck> mTruck2LastDepot;// map each truck to the
	// last depot (after
	// service plan)
	public HashMap<Truck, Integer> mTruck2LastTime;// the time where the truck
	// is available at last
	// depot

	public HashMap<Mooc, DepotMooc> mMooc2LastDepot;// map each mooc to the last
	// depot (after service
	// plan)
	public HashMap<Mooc, Integer> mMooc2LastTime;// the time where mooc is
	// available at the last
	// depot

	public HashMap<Container, DepotContainer> mContainer2LastDepot; // map each
	// container
	// to the
	// last
	// depot
	public HashMap<Container, Integer> mContainer2LastTime;// the time where
	// container is
	// available at the
	// last depot
	
	public TruckRouteInfo4Request(){
		mTruck2LastDepot = new HashMap<Truck, DepotTruck>();
		mTruck2LastTime = new HashMap<Truck, Integer>();
		mMooc2LastDepot = new HashMap<Mooc, DepotMooc>();
		mMooc2LastTime = new HashMap<Mooc, Integer>();
		mContainer2LastDepot = new HashMap<Container, DepotContainer>();
		mContainer2LastTime = new HashMap<Container, Integer>();
	}
	public void setLastDepotTruck(Truck truck, DepotTruck depot){
		mTruck2LastDepot.put(truck, depot);
	}
	public DepotTruck getLastDepotTruck(Truck truck){
		return mTruck2LastDepot.get(truck);
	}
	
	public void setLastTimeTruck(Truck truck, Integer lastTime){
		mTruck2LastTime.put(truck, lastTime);
	}
	public int getLastTimeTruck(Truck truck){
		return mTruck2LastTime.get(truck);
	}
	
	public void setLastDepotMooc(Mooc mooc, DepotMooc depot){
		mMooc2LastDepot.put(mooc, depot);
	}
	public DepotMooc getLastDepotMooc(Mooc mooc){
		return mMooc2LastDepot.get(mooc);
	}
	
	public void setLastTimeMooc(Mooc mooc, Integer lastTime){
		mMooc2LastTime.put(mooc, lastTime);
	}
	public int getLastTimeMooc(Mooc mooc){
		return mMooc2LastTime.get(mooc);
	}
	
	public void setLastDepotContainer(Container container, DepotContainer depot){
		mContainer2LastDepot.put(container, depot);
	}
	
	public DepotContainer getLastDepotContainer(Container container){
		return mContainer2LastDepot.get(container);
	}
	
	public void setLastTimeContainer(Container container, Integer lastTime){
		mContainer2LastTime.put(container,  lastTime);
	}
	public String name(){
		return "TruckRouteInfo4Request";
	}
	public int getLastTimeContainer(Container container){
		//if(mContainer2LastTime == null) System.out.println(name() + "::getLastTimeContainer, mContainer2LastTime NULL???");
		//if(container == null)System.out.println(name() + "::getLastTimeContainer, container NULL???");
		if( mContainer2LastTime.get(container) != null)
			return mContainer2LastTime.get(container);
		return -1;
	}
	
}
