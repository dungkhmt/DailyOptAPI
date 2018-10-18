package routingdelivery.service;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.test.TestAPI;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IConstraintVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.ValueRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.Implicate;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.eq.Eq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.LexMultiValues;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.RouteIndex;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyCrossExchangeMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOnePointMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyOrOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyThreeOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove1Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove2Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove3Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove4Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove5Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove6Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove7Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoOptMove8Explorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.GreedyTwoPointsMoveExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;
import localsearch.domainspecific.vehiclerouting.vrp.search.Neighborhood;
import localsearch.domainspecific.vehiclerouting.vrp.utils.googlemaps.LatLng;
import routingdelivery.model.DateTimePeriod;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.StatisticInformation;
import routingdelivery.model.StatisticRoute;
import routingdelivery.model.StatisticTrip;
import routingdelivery.model.Vehicle;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ExclusiveItem;
import routingdelivery.smartlog.brenntag.model.ExclusiveVehicleLocation;
import routingdelivery.smartlog.brenntag.model.InputIndicator;
import routingdelivery.smartlog.brenntag.model.LocationConfig;
import routingdelivery.smartlog.brenntag.model.ModelRoute;
import routingdelivery.smartlog.brenntag.model.SolutionIndicator;
import routingdelivery.smartlog.brenntag.model.VehicleTrip;
import routingdelivery.smartlog.brenntag.model.VehicleTripCollection;
import routingdelivery.smartlog.brenntag.service.BrennTagRouteSolverForOneVehicle;
import routingdelivery.smartlog.brenntag.service.ItemAmount;
import routingdelivery.smartlog.brenntag.service.Trip;
import utils.DateTimeUtils;
import utils.Utility;

public class PickupDeliverySolver {
	public static final String module = PickupDeliverySolver.class.getName();
	public static final String ROUTE_ELEMENT_STARTING_POINT = "START_POINT";
	public static final String ROUTE_ELEMENT_END_POINT = "END_POINT";
	public static final String ROUTE_ELEMENT_END_TRIP = "END_TRIP";
	public static final String ROUTE_ELEMENT_PICKUP_POINT = "PICKUP_POINT";
	public static final String ROUTE_ELEMENT_DELIVERY_POINT = "DELIVERY_POINT";
	public static final String NOI_THANH = "NOI_THANH";
	public static final String NGOAI_THANH = "NGOAI_THANH";

	public static final double EPS = 0.0000001;
	public BrennTagPickupDeliveryInput input;
	public PickupDeliveryRequest[] requests;
	public Vehicle[] vehicles;
	public Vehicle[] externalVehicles;
	public DistanceElement[] distances;
	public DistanceElement[] travelTimes;
	public HashMap<String, Integer> mLocationCode2Index;
	public HashMap<Item, Integer> mItem2LocationIndex;
	public double[][] a_distance;// distance between two location indices
	public double[][] a_travelTime;// a_travelTime between two location
									// indices
	public HashMap<String, Double> mDistance;
	public HashMap<String, Double> mTravelTime;
	public int N;// pickup points: 0, 1, 2, ..., N-1 delivery points:
					// N,...,N+N-1
	public int M;// start points of vehicles 2N,...,2N+M-1, end points of
					// vehicles are 2N+M,..., 2N+2M-1
	public double[][] dis;// dis[i][j] is the distance from point i to point
							// j
	public double[] cap;// capacity of vehicles
	public HashMap<Integer, Integer> mItemIndex2RequestIndex;
	public HashMap<PickupDeliveryRequest, Integer> mRequest2Index;
	public HashMap<Point, Vehicle> mPoint2Vehicle;
	// public HashMap<Point, PickupDeliveryRequest> mPoint2Request;
	public HashMap<Point, ArrayList<PickupDeliveryRequest>> mPoint2Request;
	public HashMap<Point, String> mPoint2Type;// "S": start, "P": pickup,
												// "D": delivery, "T":
												// terminate

	public ArrayList<Point> startPoints;
	public ArrayList<Point> endPoints;
	public ArrayList<Point> pickupPoints;
	public ArrayList<Point> deliveryPoints;
	public HashMap<Point, Point> pickup2DeliveryOfGood = new HashMap<Point, Point>();
	public ArrayList<Point> allPoints;
	public HashMap<Point, Integer> mPoint2Index;
	public HashMap<Point, String> mPoint2LocationCode;
	public HashMap<Point, Double> mPoint2Demand;
	public HashMap<Point, HashSet<Integer>> mPoint2PossibleVehicles;
	public HashMap<String, HashSet<String>> mItem2ExclusiveItems;
	public HashMap<String, HashSet<String>> mVehicle2NotReachedLocations;
	public HashMap<String, HashSet<String>> mVehicleCategory2NotReachedLocations;
	public HashMap<Item, Item[]> mLogicalItem2PhysicalItems;
	public HashMap<Point, Integer> mPickupPoint2RequestIndex;

	public HashMap<String, String> mLocation2Type;
	public HashMap<String, ArrayList<Item>> mLocation2Items;
	public HashMap<String, LocationConfig> mLocationCode2Config;
		
	public HashMap<Point, Integer> mPickupPoint2PickupIndex;
	public HashMap<Point, Integer> mDeliveryPoint2DeliveryIndex;

	public HashMap<Integer, HashSet<Integer>> mRequest2PointIndices;

	public InputIndicator inputIndicator;

	public HashMap<Point, Integer> earliestAllowedArrivalTime;
	public HashMap<Point, Integer> serviceDuration;
	public HashMap<Point, Integer> lastestAllowedArrivalTime;
	public ArcWeightsManager awm;
	public ArcWeightsManager travelTime;
	public NodeWeightsManager nwm;
	public VRManager mgr;
	public VarRoutesVR XR;
	public AccumulatedWeightNodesVR awn;
	public AccumulatedWeightEdgesVR awe;

	public ConstraintSystemVR CS;
	// public CEarliestArrivalTimeVR ceat;
	// public EarliestArrivalTimeVR eat;
	public IFunctionVR cost;
	public IFunctionVR[] load;
	public LexMultiFunctions F;

	// public HashMap<Point, String> mPoint2ItemCode;// map a point to the
	// code
	// of an item
	public HashMap<Point, Integer[]> mPoint2IndexItems;// map a point to an
														// item

	public HashMap<Point, HashSet<Integer>> mPoint2IndexLoadedItems;// mPoint2LoadedItems[p]
																	// is
																	// the
																	// set
																	// of
																	// codes
																	// of
																	// items
																	// loaded
																	// on
																	// the
																	// vehicle
																	// after
																	// point
																	// p

	public HashMap<Item, Integer> mItem2Index;
	public HashMap<String, Integer> mItemCode2Index;
	public ArrayList<Item> items;// itemCodes.get(i) is the code of ith item,
									// use with mItemCode2Index;
	public boolean[][] itemConflict;// itemConflict[i][j] = T if item i and j
									// conflict

	public HashMap<Point, Integer> mPoint2ArrivalTime;
	public HashMap<Point, Integer> mPoint2DepartureTime;
	public HashMap<VehicleTrip, VarRoutesVR> mTrip2VarRoute;
	public HashSet<Vehicle> usedInternalVehicles;
	public HashSet<Vehicle> usedSugesstedVehicles;
	public HashMap<Vehicle, Integer> mVehicle2Index;
	public boolean[][] fixVehiclePoint;// fixVehiclePoint[v][p] = true, xe v
										// duoc assign co dinh cho delivery
										// point p
	public ArrayList<String> intCityPickupLocations;
	public ArrayList<String> intCityDeliveryLocations;
	public ArrayList<String> extCityPickupLocations;
	public ArrayList<String> extCityDeliveryLocations;

	public PrintWriter log;
	public boolean CHECK_AND_LOG = true;
	public long startExecutionTime;
	public boolean timeLimitExpired = false;

	
	// data structure for backing solution
	ArrayList<ArrayList<Point>> backup_point_routes;
	HashMap<Point, Vehicle> backup_mPoint2Vehicle;
	
	public void backupXR(){
		if(backup_point_routes == null) backup_point_routes = new ArrayList<ArrayList<Point>>();
		backup_point_routes.clear();
		if(backup_mPoint2Vehicle == null) backup_mPoint2Vehicle = new HashMap<Point, Vehicle>();
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			backup_mPoint2Vehicle.put(s, vh);
			ArrayList<Point> L = new ArrayList<Point>();
			for(Point p = XR.next(s); p != XR.endPoint(k); p = XR.next(p)) L.add(p);
			backup_point_routes.add(L);
		}
	}
	public ArrayList<Point> collectPoints(VarRoutesVR XR){
		ArrayList<Point> L = new ArrayList<Point>();
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			for(Point p = XR.next(XR.startPoint(k)); p != XR.endPoint(k); p = XR.next(p))
				L.add(p);
		}
		return L;
	}
	public void restoreXR(){
		//ArrayList<Point> L = collectPoints(XR);
		mgr.performRemoveAllClientPoints();
		for(int i = 0; i < backup_point_routes.size(); i++){
			ArrayList<Point> seq = backup_point_routes.get(i);
			Point s = XR.startPoint(i+1);
			Vehicle vh = backup_mPoint2Vehicle.get(s);
			mPoint2Vehicle.put(s, vh);
			for(int j = 0; j < seq.size(); j++){
				Point p = seq.get(j);
				mgr.performAddOnePoint(p, s);
				s = p;
			}
		}
		
		for(int k = 1; k <= XR.getNbRoutes(); k++)
			propagate(XR, k);
	}
	public int computeInternalVehicles() {
		if (vehicles != null)
			return vehicles.length;
		return 0;
	}

	public int computeExternalVehicles() {
		if (externalVehicles == null)
			return 0;
		return externalVehicles.length;
	}

	public int computeNbVehicles() {
		return computeInternalVehicles() + computeExternalVehicles();
	}

	public void log2Console(String s) {
		if (!CHECK_AND_LOG)
			return;
		System.out.println(s);
	}

	public void log(String s) {
		if (!CHECK_AND_LOG)
			return;

		if (log != null)
			log.println(s);
	}

	public void logNotln(String s) {
		if (!CHECK_AND_LOG)
			return;

		if (log != null)
			log.print(s);
	}

	public void initializeLog() {
		try {
			if (log == null)
				log = new PrintWriter(TestAPI.ROOT_DIR + "/log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
			log = null;
		}
		if (log == null) {
			try {
				log = new PrintWriter(TestAPI.SECONDARY_ROOT_DIR + "/log.txt");
			} catch (Exception ex) {
				ex.printStackTrace();
				log = null;
			}
		}
	}

	public void finalizeLog() {
		if (log != null) {
			log.close();
			log = null;
		}
	}

	public SolutionIndicator evaluation(VarRoutesVR XR) {
		double distance = 0;
		int nbInternalTrucks = 0;
		int nbExternalTrucks = 0;
		int nbTrips = 0;
		double internalTruckLoad = 0;
		double externalTruckLoad = 0;
		double internalCapacity = 0;
		double externalCapacity = 0;
		double longestRoute = 0;
		double shortestRoute = Integer.MAX_VALUE;
		double rateDelivery = 0;
		double rateReturn = 0;
		
		VehicleTripCollection VTC = analyzeTrips(XR);

		nbTrips = VTC.trips.size();

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.emptyRoute(k))
				continue;
			double dis = 0;
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				distance += getDistance(p, XR.next(p));// awm.getDistance(p,
														// XR.next(p));
				dis += getDistance(p, XR.next(p));// awm.getDistance(p,
													// XR.next(p));
			}
			if (longestRoute < dis)
				longestRoute = dis;
			if (shortestRoute > dis)
				shortestRoute = dis;

			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if (isInternalVehicle(vh)) {
				nbInternalTrucks++;
				//internalCapacity += vh.getWeight();
			} else {
				nbExternalTrucks++;
				//externalCapacity += vh.getWeight();
			}
		}

		if (vehicles != null) {
			for (int i = 0; i < vehicles.length; i++) {
				Vehicle vh = vehicles[i];
				for (int j = 0; j < VTC.mVehicle2Trips.get(vh).size(); j++) {
					VehicleTrip t = VTC.mVehicle2Trips.get(vh).get(j);
					internalTruckLoad += t.load;
				}
				internalCapacity += vh.getWeight()*VTC.mVehicle2Trips.get(vh).size();
			}
		}
		if (externalVehicles != null) {
			for (int i = 0; i < externalVehicles.length; i++) {
				Vehicle vh = externalVehicles[i];
				for (int j = 0; j < VTC.mVehicle2Trips.get(vh).size(); j++) {
					VehicleTrip t = VTC.mVehicle2Trips.get(vh).get(j);
					externalTruckLoad += t.load;
				}
				externalCapacity += vh.getWeight()*VTC.mVehicle2Trips.get(vh).size();
			}
		}
		// SolutionIndicator I = new SolutionIndicator(distance,
		// nbInternalTrucks, nbExternalTrucks, nbTrips, internalTruckLoad,
		// externalTruckLoad);
		rateDelivery = (internalTruckLoad + externalTruckLoad)*100.0/(internalCapacity + externalCapacity);
		rateReturn = nbTrips*1.0/(nbInternalTrucks + nbExternalTrucks);
		
		SolutionIndicator I = new SolutionIndicator(distance, nbInternalTrucks,
				nbExternalTrucks, nbTrips, internalTruckLoad,
				externalTruckLoad, internalCapacity, externalCapacity,
				longestRoute, shortestRoute,rateDelivery, rateReturn);

		return I;
	}

	public boolean conflictItemPoints(Point p, Point q){
		//boolean ok = false;
		if(mPoint2IndexItems.get(p) == null || mPoint2IndexItems.get(q) == null) return false;
		for(int i: mPoint2IndexItems.get(p)){
			for(int j: mPoint2IndexItems.get(q)){
				if(itemConflict[i][j]) return true;
			}
		}
		return false;
	}
	public ArrayList<Point> createPickupListOfDelivery(ArrayList<Point> lst_delivery){
		ArrayList<Point> lst_pickup = new ArrayList<Point>();
		for(int j = lst_delivery.size()-1; j >= 0; j--){
			Point pickup = getPickupOfDelivery(lst_delivery.get(j));
			lst_pickup.add(pickup);
		}
		return lst_pickup;
	}
	public Point[] getSortedDecreasingWeight(ArrayList<Point> lst_delivery){
		Point[] L = new Point[lst_delivery.size()];
		for(int i = 0; i < lst_delivery.size(); i++){
			L[i] = lst_delivery.get(i);
		}
		for(int i = 0; i < L.length; i++)
			for(int j = i+1; j < L.length; j++)
				if(mPoint2Demand.get(L[i]) < mPoint2Demand.get(L[j])){
					Point tmp = L[i]; L[i] = L[j]; L[j] = tmp;
				}
		return L;
	}
	public boolean conflictTrips(VehicleTrip t1, VehicleTrip t2) {
		for (Point p : t1.seqPoints) {
			if (mPoint2Type.get(p).equals("P")) {
				for (Point q : t2.seqPoints) {
					if (mPoint2Type.get(q).equals("P")) {
						if (mPoint2IndexItems.get(p) != null
								&& mPoint2IndexItems.get(q) != null) {
							for (int i = 0; i < mPoint2IndexItems.get(p).length; i++) {
								for (int j = 0; j < mPoint2IndexItems.get(q).length; j++) {
									// Item I1 =
									// items.get(mPoint2IndexItems.get(p)[i]);
									// Item I2 =
									// items.get(mPoint2IndexItems.get(q)[j]);
									if (itemConflict[mPoint2IndexItems.get(p)[i]][mPoint2IndexItems
											.get(q)[j]]) {
										return true;
									}
								}
							}
						}
					}
				}
			}
		}
		return false;
	}

	public int getNextPointID() {
		int maxID = -1;
		for (Point p : allPoints) {
			if (maxID < p.ID)
				maxID = p.ID;
		}
		return maxID + 1;
	}

	public String code(String from, String to) {
		return from + "-" + to;
	}

	/*
	 * public HashSet<Integer>
	 * greedyConstructMaintainConstraintOptionNotSplitItems() { // consider
	 * split/not-split delivery HashSet<Integer> cand = new HashSet<Integer>();
	 * for (int i = 0; i < pickupPoints.size(); i++) cand.add(i);
	 * 
	 * while (cand.size() > 0) { Point sel_pickup = null; Point sel_delivery =
	 * null; double eval_violations = Integer.MAX_VALUE; double eval_cost =
	 * Integer.MAX_VALUE; Point sel_p = null; Point sel_d = null; int sel_i =
	 * -1;
	 * 
	 * for (int i : cand) { Point pickup = pickupPoints.get(i); Point delivery =
	 * deliveryPoints.get(i); PickupDeliveryRequest r =
	 * mPoint2Request.get(pickup);
	 * 
	 * // for(int k = 1; k <= XR.getNbRoutes(); k++){ // try internal vehicles
	 * FIRST for (int k = 1; k <= vehicles.length; k++) { String vehicleCode =
	 * vehicles[k - 1].getCode(); String pickupLocation =
	 * mPoint2LocationCode.get(pickup); String deliveryLocation =
	 * mPoint2LocationCode.get(delivery);
	 * 
	 * // check points cannot be visited //
	 * if(!mPoint2PossibleVehicles.get(pickup).contains(k) || //
	 * !mPoint2PossibleVehicles.get(delivery).contains(k)) // continue; if
	 * (mVehicle2NotReachedLocations.get(vehicleCode).contains( pickupLocation)
	 * || mVehicle2NotReachedLocations.get(vehicleCode)
	 * .contains(deliveryLocation)) continue;
	 * 
	 * for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR .next(p)) {
	 * // check exclusive items boolean okExclusiveItems = true; HashSet<String>
	 * E = mItem2ExclusiveItems .get(mPoint2ItemCode.get(pickup)); for (String I
	 * : E) { if (mPoint2LoadedItems.get(p).contains(I)) { okExclusiveItems =
	 * false; break; } } if (!okExclusiveItems) continue;
	 * 
	 * for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) { boolean ok =
	 * true; for (Point tmp = p; tmp != XR.next(d); tmp = XR .next(tmp)) { if
	 * (nwm.getWeight(pickup) + awn.getSumWeights(tmp) > cap[k - 1]) { ok =
	 * false; break; } } if (!ok) continue;
	 * 
	 * double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d); double ef =
	 * cost.evaluateAddTwoPoints(pickup, p, delivery, d);
	 * 
	 * if (ec > 0) continue;// ensure constraint always satisfied
	 * 
	 * if (ec < eval_violations) { eval_violations = ec; eval_cost = ef; sel_p =
	 * p; sel_d = d; sel_pickup = pickup; sel_delivery = delivery; sel_i = i; }
	 * else if (ec == eval_violations && ef < eval_cost) { eval_cost = ef; sel_p
	 * = p; sel_d = d; sel_pickup = pickup; sel_delivery = delivery; sel_i = i;
	 * } } } }
	 * 
	 * // NO internal vehicles are possible --> TRY external vehicles if (sel_p
	 * == null) { for (int k = vehicles.length + 1; k <= M; k++) { String
	 * vehicleCode = externalVehicles[k - vehicles.length - 1].getCode(); String
	 * pickupLocation = mPoint2LocationCode.get(pickup); String deliveryLocation
	 * = mPoint2LocationCode .get(delivery);
	 * 
	 * // check points cannot be visited //
	 * if(!mPoint2PossibleVehicles.get(pickup).contains(k) // || //
	 * !mPoint2PossibleVehicles.get(delivery).contains(k)) // continue; if
	 * (mVehicle2NotReachedLocations.get(vehicleCode) .contains(pickupLocation)
	 * || mVehicle2NotReachedLocations .get(vehicleCode).contains(
	 * deliveryLocation)) continue;
	 * 
	 * for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR .next(p)) {
	 * // check exclusive items boolean okExclusiveItems = true; HashSet<String>
	 * E = mItem2ExclusiveItems .get(mPoint2ItemCode.get(pickup)); for (String I
	 * : E) { if (mPoint2LoadedItems.get(p).contains(I)) { okExclusiveItems =
	 * false; break; } } if (!okExclusiveItems) continue;
	 * 
	 * for (Point d = p; d != XR.endPoint(k); d = XR .next(d)) { boolean ok =
	 * true; for (Point tmp = p; tmp != XR.next(d); tmp = XR .next(tmp)) { if
	 * (nwm.getWeight(pickup) + awn.getSumWeights(tmp) > cap[k - 1]) { ok =
	 * false; break; } } if (!ok) continue;
	 * 
	 * double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d); double ef =
	 * cost.evaluateAddTwoPoints(pickup, p, delivery, d); if (ec > 0) continue;
	 * if (ec < eval_violations) { eval_violations = ec; eval_cost = ef; sel_p =
	 * p; sel_d = d; sel_pickup = pickup; sel_delivery = delivery; sel_i = i; }
	 * else if (ec == eval_violations && ef < eval_cost) { eval_cost = ef; sel_p
	 * = p; sel_d = d; sel_pickup = pickup; sel_delivery = delivery; sel_i = i;
	 * } } } }
	 * 
	 * }
	 * 
	 * } if (sel_i != -1) { mgr.performAddOnePoint(sel_delivery, sel_d);
	 * mgr.performAddOnePoint(sel_pickup, sel_p);
	 * System.out.println("init addOnePoint(" + sel_pickup.ID + "," + sel_p.ID +
	 * "), and (" + sel_delivery.ID + "," + sel_d.ID + ", XR = " + XR.toString()
	 * + ", CS = " + CS.violations() + ", cost = " + cost.getValue());
	 * cand.remove(sel_i);
	 * 
	 * // update loaded items for (String I : mPoint2LoadedItems.get(sel_p)) {
	 * mPoint2LoadedItems.get(sel_pickup).add(I); } for (String I :
	 * mPoint2LoadedItems.get(sel_d)) {
	 * mPoint2LoadedItems.get(sel_delivery).add(I); } for (Point p = sel_pickup;
	 * p != sel_delivery; p = XR.next(p)) { mPoint2LoadedItems.get(p).add(
	 * mPoint2ItemCode.get(sel_pickup)); }
	 * 
	 * } else { System.out.println("Cannot schedule any more, BREAK"); break;
	 * 
	 * } }
	 * 
	 * if (cand.size() > 0) { // return list of un-scheduled items
	 * System.out.println("number of unscheduled items is " + cand.size());
	 * 
	 * }
	 * 
	 * return cand; }
	 */
	public String loadedItems(Point p) {
		String s = "";
		for (int ic : mPoint2IndexLoadedItems.get(p)) {
			s = s + items.get(ic).getCode() + ",";
		}
		return s;
	}

	public String itemsAt(Point pickup) {
		String s = "";
		if (mPoint2IndexItems.get(pickup) != null)
			for (int I : mPoint2IndexItems.get(pickup)) {
				s += items.get(I).getCode() + ",";
			}
		return s;
	}

	public HashSet<String> itemCodeAt(Point pickup) {
		HashSet<String> S = new HashSet<String>();
		if (mPoint2IndexItems.get(pickup) != null)
			for (int I : mPoint2IndexItems.get(pickup)) {
				S.add(items.get(I).getCode());
			}

		return S;
	}

	private HashSet<String> getExclusiveItemsOfPickupPoint(Point pickup) {
		HashSet<String> E = new HashSet<String>();
		for (int ite : mPoint2IndexItems.get(pickup)) {
			for (String ic : mItem2ExclusiveItems.get(items.get(ite).getCode())) {
				E.add(ic);
			}
		}
		return E;
	}

	public String printRouteAndItems(VarRoutesVR XR, int k) {
		String s = "Vehicle " + getVehicle(k - 1).getCode();
		HashSet<String> loadedItems = new HashSet<String>();
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			String s1 = "";
			String s2 = "";
			if (mPoint2Type.get(p) == "P") {
				for (int j = 0; j < mPoint2IndexItems.get(p).length; j++) {
					Item I = items.get(mPoint2IndexItems.get(p)[j]);
					loadedItems.add(I.getCode());
					s1 = s1 + I.getCode() + ",";
				}
				// for(String ic: loadedItems) s1 = s1 + ic + ",";
			}
			for (int i : mPoint2IndexLoadedItems.get(p)) {
				Item I = items.get(i);
				s2 = s2 + I.getCode() + ",";
			}
			s = s + p.ID + ", location " + mPoint2LocationCode.get(p)
					+ ": items = " + s1 + ", loaded items = " + s2 + "\n";
		}
		return s;
	}

	public boolean checkConflictItemsAtPoint(Point p) {
		if (mPoint2IndexLoadedItems.get(p) == null)
			return true;

		for (int i : mPoint2IndexLoadedItems.get(p)) {
			for (int j : mPoint2IndexLoadedItems.get(p))
				if (i < j) {

					if (itemConflict[i][j]) {
						Item I = items.get(i);
						Item J = items.get(j);
						/*
						 * if(I.getOrderId().equals(J.getOrderId())){ log(name()
						 * + "::checkConflictItemsAtPoint, CONFLICT-ITEMS " +
						 * I.getCode() + "," + J.getCode() +
						 * " have the SAME Order " + I.getOrderId()); continue;
						 * }
						 */
						// log(name()
						// +
						// "::checkConflictItemsAtPoint, FAILED??? CONFLICT-ITEMS at point "
						// + p.ID + ": "
						// + items.get(i).getCode() + "-"
						// + items.get(j).getCode());
						return false;
					}
				}
		}
		return true;
	}

	public String printRouteAndTime(VarRoutesVR XR) {
		String s = "";
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			s += "route[" + k + "]\n";
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				long at = mPoint2ArrivalTime.get(p);
				String sat = DateTimeUtils.unixTimeStamp2DateTime(at);
				s += "ID = "
						+ p.ID
						+ ": "
						+ mPoint2LocationCode.get(p)
						+ " ["
						+ sat
						+ "] late = "
						+ DateTimeUtils
								.unixTimeStamp2DateTime(lastestAllowedArrivalTime
										.get(p)) + "\n";
			}
		}
		return s;
	}

	public String printRouteAndTime(VarRoutesVR XR, int k) {
		if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
			return "";

		String s = "";
		s += "route[" + k + ", vehicle " + getVehicle(k - 1).getCode() + "]\n";
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			if (mPoint2ArrivalTime.get(p) == null) {
				log(name() + "::printRouteAndTime, point " + p.ID
						+ " KHONG CO ARRIVAL TIME, BUG???, XR = "
						+ XR.toStringRoute(k));
				continue;
			}
			if (mPoint2DepartureTime.get(p) == null) {
				log(name() + "::printRouteAndTime, point " + p.ID
						+ " KHONG CO DEPARTURE TIME, BUG???, XR = "
						+ XR.toStringRoute(k));
				continue;
			}

			long at = mPoint2ArrivalTime.get(p);
			long dt = mPoint2DepartureTime.get(p);
			String sat = DateTimeUtils.unixTimeStamp2DateTime(at);
			String sdt = DateTimeUtils.unixTimeStamp2DateTime(dt);
			s += "ID = "
					+ p.ID
					+ ": "
					+ mPoint2LocationCode.get(p)
					+ " [arr = "
					+ sat
					+ ", dep = "
					+ sdt
					+ "] late = "
					+ DateTimeUtils
							.unixTimeStamp2DateTime(lastestAllowedArrivalTime
									.get(p)) + "\n";
		}
		Point p = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(p) == null) {
			log(name() + "::printRouteAndTime, point " + p.ID
					+ " KHONG CO ARRIVAL TIME, BUG???, XR = "
					+ XR.toStringRoute(k));

		} else {
			long at = mPoint2ArrivalTime.get(p);
			// long dt = mPoint2DepartureTime.get(p);
			String sat = DateTimeUtils.unixTimeStamp2DateTime(at);
			String sdt = "-";// DateTimeUtils.unixTimeStamp2DateTime(dt);
			s += "ID = "
					+ p.ID
					+ ": "
					+ mPoint2LocationCode.get(p)
					+ " [arr = "
					+ sat
					+ ", dep = "
					+ sdt
					+ "] late = "
					+ DateTimeUtils
							.unixTimeStamp2DateTime(lastestAllowedArrivalTime
									.get(p)) + "\n";
		}
		return s;
	}

	public VehicleTripCollection analyzeTrips(VarRoutesVR XR) {
		ArrayList<VehicleTrip> trips = new ArrayList<VehicleTrip>();
		HashMap<VehicleTrip, Integer> mTrip2Route = new HashMap<VehicleTrip, Integer>();
		mTrip2VarRoute = new HashMap<VehicleTrip, VarRoutesVR>();
		HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = new HashMap<Vehicle, ArrayList<VehicleTrip>>();

		int sz = 0;
		if (vehicles != null)
			sz += vehicles.length;
		if (externalVehicles != null)
			sz += externalVehicles.length;

		for (int i = 0; i < sz; i++) {
			Vehicle vh = getVehicle(i);
			mVehicle2Trips.put(vh, new ArrayList<VehicleTrip>());
		}

		// System.out.println(name() + "::analyzeTrips, XR = "
		// + XR.toStringShort());

		if (log != null) {
			// log.println(name()
			// +
			// "::analyzeTrips-----------------------------------------------------------------------------");
			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
					continue;
				Point s = XR.startPoint(k);
				Vehicle vh = mPoint2Vehicle.get(s);
				// log.println("Vehicle[" + k + "] code = " + vh.getCode());
				// for (Point p = s; p != XR.endPoint(k); p = XR.next(p)) {
				// log.println("POINT " + mPoint2LocationCode.get(p)
				// + ", type = " + mPoint2Type.get(p) + ", load = "
				// + mPoint2Demand.get(p));
				// }
			}
		}
		/*
		 * int r_index = getRouteIndex("60C-242.61"); int pi =
		 * getPickupPointIndex("60007742"); Point pickup = pickupPoints.get(pi);
		 * Point delivery = deliveryPoints.get(pi); int delta_time =
		 * getTimeViolationsWhenInsert(pickup, delivery, r_index); boolean
		 * okconflictitem = feasibleMoveConflictItems(pickup, delivery,
		 * r_index); boolean okconflictlocation =
		 * feaisbleMoveConflictLocation(pickup, delivery, r_index); boolean
		 * okMove = feasibleMove(pickup, delivery, r_index);
		 * 
		 * System.out.println(name() + "::analyzeTrip, --> TEST delta_time = " +
		 * delta_time + ", move conflict item = " + okconflictitem +
		 * ", conflict location = " + okconflictlocation + ", okmove = " +
		 * okMove);
		 */

		int nbTrips = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			Point p = XR.startPoint(k);

			// if (log != null) {
			Vehicle vh = mPoint2Vehicle.get(p);
			// if (log != null)
			// log.println(name() + "::analyzeTrips, VEHICLE " + vh.getCode());
			p = XR.next(p);
			Point np = XR.next(p);
			while (p != XR.endPoint(k)) {
				double loadPerTrip = 0;
				double length = 0;
				int nbLocations = 0;
				ArrayList<Point> points = new ArrayList<Point>();
				while (p != XR.endPoint(k)) {
					points.add(p);
					// System.out.println(name() + "::analyzeTrips, XR = "
					// + XR.toStringShort() + ", p = " + p.ID
					// + ", type = " + mPoint2Type.get(p) + ", np = "
					// + np.ID);
					if (mPoint2Type.get(p).equals("D")
							&& (mPoint2Type.get(np).equals("P")
									|| mPoint2Type.get(np).equals("T") || np == XR
									.endPoint(k))) {
						break;
					} else {
						if (mPoint2Type.get(p).equals("P")) {
							loadPerTrip += mPoint2Demand.get(p);
							nbLocations += 1;
						}
						// System.out.println(name() + "::analyzeTrips, p = "
						// + mPoint2LocationCode.get(p) + ", np = "
						// + mPoint2LocationCode.get(np));
						length += getDistance(p, np);// awm.getDistance(p,
														// np);
						p = np;
						np = XR.next(np);
					}
				}
				nbTrips++;
				/*
				 * if(mPoint2DepartureTime.get(p) == null ||
				 * mPoint2ArrivalTime.get(p) == null){ log(name() +
				 * "::analyzeTrips, arrival or departure time = NULL at point "
				 * + p.ID + ", BUG???, vehicle[" + k + "] = " +
				 * getVehicle(k).getCode() + ", XR = " + XR.toStringRoute(k));
				 * System.out.println(name() +
				 * "::analyzeTrips, arrival or departure time = NULL at point "
				 * + p.ID + ", BUG???, vehicle[" + k + "] = " +
				 * getVehicle(k).getCode() + ", XR = " + XR.toStringRoute(k));
				 * continue; }
				 */
				String s_dt = "-";
				if (mPoint2DepartureTime.get(p) != null) {
					long dt = mPoint2DepartureTime.get(p);
					s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);
				}

				VehicleTrip tr = new VehicleTrip(vh, points, nbLocations,
						loadPerTrip, length, this);
				tr.setSolver(this);

				mVehicle2Trips.get(vh).add(tr);

				mTrip2Route.put(tr, k);
				mTrip2VarRoute.put(tr, XR);
				trips.add(tr);

				// if (log != null)
				// log.println("TRIP " + (nbTrips - 1) + ", vehicle.cap = "
				// + vh.getWeight() + ", loadTrip = " + loadPerTrip
				// + ", nbLocations = " + nbLocations + ", length = "
				// + length + ", departure-time = " + s_dt);

				p = np;
				np = XR.next(np);
			}
			long at = mPoint2ArrivalTime.get(XR.endPoint(k));
			// if (log != null)
			// log.println("Arrival-time to END = "
			// + DateTimeUtils.unixTimeStamp2DateTime(at));
			// if (log != null)
			// log.println("-------------------------");
			// }
		}
		return new VehicleTripCollection(mTrip2Route, trips, mVehicle2Trips);
	}

	/*
	 * public void performMoveTrip(VarRoutesVR XR, VehicleTrip vt1, VehicleTrip
	 * vt2) { ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
	 * ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
	 * ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
	 * ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();
	 * 
	 * if (lst_pickup_1.size() == 0) return;
	 * 
	 * Point p = XR.prev(lst_pickup_1.get(0));// store the point for recover int
	 * n = lst_pickup_1.size(); // remove trip vt1 for (int i = 0; i < n; i++) {
	 * performRemoveOnePoint(XR, lst_pickup_1.get(i)); performRemoveOnePoint(XR,
	 * lst_delivery_1.get(n - 1 - i)); } // System.out.println(name() +
	 * "::performMoveTrip, init cost = " + // cost.getValue()); Point np =
	 * lst_pickup_2.get(lst_pickup_2.size() - 1); // re-insert into vt2 for (int
	 * i = 0; i < lst_pickup_1.size(); i++) { Point pickup =
	 * lst_pickup_1.get(i); Point delivery = lst_delivery_1.get(n - 1 - i);
	 * performAddOnePoint(XR, pickup, np); // System.out.println(name() + //
	 * "::performMoveTrip, first addOnePoint(" + pickup.ID + "," + np.ID // +
	 * ") -> cost = " + cost.getValue()); performAddOnePoint(XR, delivery,
	 * pickup); // System.out.println(name() + //
	 * "::performMoveTrip, second addOnePoint(" + delivery.ID + "," + //
	 * delivery.ID + ") -> cost = " + cost.getValue()); np = pickup; } }
	 */
	public Point[] getBestSequenceGreedy(Point start, ArrayList<Point> L,
			boolean DIXAVEGAN) {
		// return the shortest sequence of points from start
		// if DIXAVEGAN, then the first point of the sequence will be the
		// farthest point
		Point[] ret = new Point[L.size()];
		boolean[] mark = new boolean[L.size()];
		for (int i = 0; i < mark.length; i++)
			mark[i] = false;

		double maxD = 0;
		int sel_p = -1;
		for (int i = 0; i < L.size(); i++) {
			double d = getDistance(start, L.get(i));
			if (d > maxD) {
				maxD = d;
				sel_p = i;
			}
		}
		int idx = -1;
		if (DIXAVEGAN) {
			idx++;
			ret[idx] = L.get(sel_p);
			start = L.get(sel_p);
			mark[sel_p] = true;
		} else {

		}
		while (true) {
			double minD = Integer.MAX_VALUE;
			int sel_i = -1;
			for (int i = 0; i < L.size(); i++)
				if (!mark[i]) {
					Point p = L.get(i);
					double d = getDistance(start, p);
					if (d < minD) {
						minD = d;
						sel_i = i;
					}
				}
			if (sel_i == -1)
				break;
			mark[sel_i] = true;
			idx++;
			ret[idx] = L.get(sel_i);
			start = L.get(sel_i);
		}

		/*
		 * double maxD = 0; Point sel_p = null; for (Point p : L) { double d =
		 * getDistance(start, p); if (d > maxD) { maxD = d; sel_p = p; } }
		 * HashSet<Point> S = new HashSet<Point>(); int idx = -1; if (DIXAVEGAN)
		 * { for (Point p : L) if (p != sel_p) S.add(p); idx++; ret[idx] =
		 * sel_p; start = sel_p; } else { for (Point p : L) S.add(p); } while
		 * (S.size() > 0) { double minD = Integer.MAX_VALUE; Point next = null;
		 * for (Point p : S) { double d = getDistance(start, p); if (d < minD) {
		 * minD = d; next = p; } } idx++; ret[idx] = next; start = next;
		 * S.remove(next); }
		 */
		return ret;
	}

	public Point getDeliveryOfPickup(Point pickup) {
		int idx = mPickupPoint2PickupIndex.get(pickup);
		return deliveryPoints.get(idx);
	}

	public Point getPickupOfDelivery(Point delivery) {
		int idx = mDeliveryPoint2DeliveryIndex.get(delivery);
		return pickupPoints.get(idx);
	}

	public boolean checkPossibleVehicleLocation(Vehicle vh, Point p) {
		String lc = mPoint2LocationCode.get(p);
		return !mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc);
	}

	public boolean checkPossibleVehicleLocation(Vehicle vh,
			ArrayList<Point> listPoints) {
		for (Point p : listPoints) {
			String lc = mPoint2LocationCode.get(p);
			// System.out.println(name() +
			// "::checkPossibleVehicleLocation, location lc = " + lc +
			// " vehicle " + vh.getCode() + " NOT REACH " +
			// mVehicle2NotReachedLocations.get(vh.getCode()));
			// if (mVehicle2NotReachedLocations.get(vh.getCode()) != null)
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return false;
		}
		return true;
	}

	public boolean checkPossibleVehicleCategoryLocation(Vehicle vh, Point p) {
		String lc = mPoint2LocationCode.get(p);
		return !mVehicleCategory2NotReachedLocations.get(
				vh.getVehicleCategory()).contains(lc);
	}

	public boolean isVehicleCategory(Vehicle v) {
		return v.getCode() == null || v.getCode().equals("");
	}

	public boolean checkLoadOnRoute(VarRoutesVR XR) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
			double load = 0;
			double maxload = 0;
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				if (mPoint2Type.get(p).equals("P")) {
					load += mPoint2Demand.get(p);
				} else if (mPoint2Type.get(p).equals("D")) {
					// int idx = mDeliveryPoint2DeliveryIndex.get(p);
					// Point pickup = pickupPoints.get(idx);
					// load -= load += mPoint2Demand.get(p);
					load += mPoint2Demand.get(p);
				}
				if (maxload < load)
					maxload = load;
			}
			if (maxload > vh.getWeight()) {
				log(name() + "::checkLoadOnRoute, vehicle weight = "
						+ vh.getWeight() + " < maxload = " + maxload
						+ ", checkLoadOnRoute FAILED, BUG????");
				return false;
			}
		}
		return true;
	}

	public boolean checkExclusiveVehicleLocation(VarRoutesVR XR) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
			if (isVehicleCategory(vh)) {
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String lc = mPoint2LocationCode.get(p);
					if (mVehicleCategory2NotReachedLocations.get(
							vh.getVehicleCategory()).contains(lc)) {
						log(name()
								+ "::checkExclusiveVehicleLocation(XR), EXCLUSIVE VEHICLE-LOCATION FAILED, vehicle category "
								+ vh.getVehicleCategory() + " <-> location "
								+ lc);
						return false;
					}
				}
			} else {
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String lc = mPoint2LocationCode.get(p);
					if (mVehicle2NotReachedLocations.get(vh.getCode())
							.contains(lc)) {
						log(name()
								+ "::checkExclusiveVehicleLocation(XR), EXCLUSIVE VEHICLE-LOCATION FAILED, vehicle code "
								+ vh.getCode() + " <-> location " + lc);
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean checkPossibleVehicleCategoryLocation(Vehicle vh,
			ArrayList<Point> listPoints) {
		for (Point p : listPoints) {
			String lc = mPoint2LocationCode.get(p);
			// System.out.println(name() +
			// "::checkPossibleVehicleCategoryLocation, location lc = " + lc +
			// " vehicle " + vh.getCode() + ", category " +
			// vh.getVehicleCategory() + " NOT REACH " +
			// mVehicle2NotReachedLocations.get(vh.getCode()));
			// if (mVehicle2NotReachedLocations.get(vh.getCode()) != null)
			if (mVehicleCategory2NotReachedLocations.get(
					vh.getVehicleCategory()).contains(lc))
				return false;
		}
		return true;
	}

	public double evaluateMoveTrip(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, boolean DIXAVEGAN, boolean loadConstraint) {
		if (vt1.vehicle == vt2.vehicle)
			return Integer.MAX_VALUE;

		// check if vt1 and be moved to vt2 w.r.t exclusiveVehicleLocations
		for (Point p : vt1.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vt2.vehicle.getCode())
					.contains(lc))
				return Integer.MAX_VALUE;
		}
		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load + vt2.load > vt2.vehicle.getWeight())
				return Integer.MAX_VALUE;
		}

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return delta;
		if (lst_pickup_2.size() == 0)
			return delta;
		int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_pickup_2.get(0));

		Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		int k = XR.route(np);// index of route containing points of vt2

		// if (vt1.load + vt2.load > cap[k - 1])return delta;

		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		// for(int i = 0; i < n; i++){
		// performRemoveOnePoint(XR, lst_pickup_1.get(i));
		// performRemoveOnePoint(XR, lst_delivery_1.get(n-1-i));
		// }
		// System.out.println(name() +
		// "::evaluateMoveTrip, after remove trip, value = " + cost.getValue());

		// re-insert into vt2 in an optimal way

		Point start = np;
		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = start2;
		if (vt1.contains(start_p))
			start_p = start1;// vt1 is before vt2 on the same route[k]

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			// if (awn.getSumWeights(q) > cap[k - 1]) {
			if (awn.getSumWeights(q) > vt2.vehicle.getWeight()) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		// recover
		for (int j = 0; j < lst_pickup_2.size(); j++) {
			Point p2 = lst_pickup_2.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p2);
			performAddOnePoint(XR, p2, start2);
			// performAddOnePoint(XR, d1,p1);
			start2 = p2;
		}
		for (int j = 0; j < lst_pickup_2.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d2 = lst_delivery_2.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d2);
			performAddOnePoint(XR, d2, start2);
			start2 = d2;
		}

		for (int j = 0; j < lst_pickup_1.size(); j++) {
			Point p1 = lst_pickup_1.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p1);
			performAddOnePoint(XR, p1, start1);
			// performAddOnePoint(XR, d1,p1);
			start1 = p1;
		}
		for (int j = 0; j < lst_pickup_1.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d1 = lst_delivery_1.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d1);
			performAddOnePoint(XR, d1, start1);
			start1 = d1;
		}

		return delta;

		// return Integer.MAX_VALUE;
	}

	public double[] evaluateMoveTripOneVehicle(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, boolean DIXAVEGAN, boolean loadConstraint) {
		// return change of totalCost AND cost of the merged trip

		if (vt1.vehicle != vt2.vehicle)
			return new double[] { Integer.MAX_VALUE, Integer.MAX_VALUE };

		String debugCode = "51C-586.32";

		Vehicle vh = vt1.vehicle;

		// check if vt1 and be moved to vt2 w.r.t exclusiveVehicleLocations
		for (Point p : vt1.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vt2.vehicle.getCode())
					.contains(lc))
				return new double[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
		}
		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load + vt2.load > vt2.vehicle.getWeight()) {
				if (vh.getCode().equals(debugCode)) {
					log(name() + "::evaluateMoveTripOneVehicle, vehicle "
							+ vh.getCode() + ", vt1.load = " + vt1.load
							+ " vt2.load =  " + vt2.load + " > cap = "
							+ vt2.vehicle.getWeight() + " RETURN INFTY");
				}
				return new double[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
			}
		}

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return new double[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
		if (lst_pickup_2.size() == 0)
			return new double[] { Integer.MAX_VALUE, Integer.MAX_VALUE };
		int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_pickup_2.get(0));

		Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		int k = XR.route(np);// index of route containing points of vt2

		// if (vt1.load + vt2.load > cap[k - 1])return delta;

		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		// for(int i = 0; i < n; i++){
		// performRemoveOnePoint(XR, lst_pickup_1.get(i));
		// performRemoveOnePoint(XR, lst_delivery_1.get(n-1-i));
		// }
		// System.out.println(name() +
		// "::evaluateMoveTrip, after remove trip, value = " + cost.getValue());

		// re-insert into vt2 in an optimal way

		Point start = np;
		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		boolean recover2First = true;
		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = start2;
		if (vt1.contains(start_p)) {
			start_p = start1;// vt1 is before vt2 on the same route[k]
			recover2First = false;
		}

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		double newDistanceK = computeDistance(XR, k);

		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));

				if (vh.getCode().equals(debugCode)) {
					log(name()
							+ "::evaluateMoveTripOneVehicle VIOLATE TIME -> violations = "
							+ violations);
				}
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
				if (vh.getCode().equals(debugCode)) {
					log(name()
							+ "::evaluateMoveTripOneVehicle, conflict-items -> violations = "
							+ violations);
				}
			}
			// if (awn.getSumWeights(q) > cap[k - 1]) {
			if (awn.getSumWeights(q) > vh.getWeight()) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
				if (vh.getCode().equals(debugCode)) {
					log(name()
							+ "::evaluateMoveTripOneVehicle, Over-Load -> violations = "
							+ violations);
				}

			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));
			if (vh.getCode().equals(debugCode)) {
				log(name()
						+ "::evaluateMoveTripOneVehicle, violate-time-end-point -> violations = "
						+ violations);
			}
		}

		if (violations == 0) {
			delta = cost.getValue() - value;
			if (vh.getCode().equals(debugCode)) {
				log(name()
						+ "::evaluateMoveTripOneVehicle, violations = 0 -> delta = "
						+ delta);
			}
		}

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		if (recover2First) {
			// recover
			for (int j = 0; j < lst_pickup_2.size(); j++) {
				Point p2 = lst_pickup_2.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p2);
				performAddOnePoint(XR, p2, start2);
				// performAddOnePoint(XR, d1,p1);
				start2 = p2;
			}
			for (int j = 0; j < lst_pickup_2.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d2 = lst_delivery_2.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d2);
				performAddOnePoint(XR, d2, start2);
				start2 = d2;
			}

			for (int j = 0; j < lst_pickup_1.size(); j++) {
				Point p1 = lst_pickup_1.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p1);
				performAddOnePoint(XR, p1, start1);
				// performAddOnePoint(XR, d1,p1);
				start1 = p1;
			}
			for (int j = 0; j < lst_pickup_1.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d1 = lst_delivery_1.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d1);
				performAddOnePoint(XR, d1, start1);
				start1 = d1;
			}
		} else {
			// recover

			for (int j = 0; j < lst_pickup_1.size(); j++) {
				Point p1 = lst_pickup_1.get(j);
				
				performRemoveOnePoint(XR, p1);
				performAddOnePoint(XR, p1, start1);
				
				start1 = p1;
			}
			for (int j = 0; j < lst_pickup_1.size(); j++) {
				
				Point d1 = lst_delivery_1.get(j);
				
				performRemoveOnePoint(XR, d1);
				performAddOnePoint(XR, d1, start1);
				start1 = d1;
			}

			for (int j = 0; j < lst_pickup_2.size(); j++) {
				Point p2 = lst_pickup_2.get(j);
				
				performRemoveOnePoint(XR, p2);
				performAddOnePoint(XR, p2, start2);
				
				start2 = p2;
			}
			for (int j = 0; j < lst_pickup_2.size(); j++) {
				
				Point d2 = lst_delivery_2.get(j);
				
				performRemoveOnePoint(XR, d2);
				performAddOnePoint(XR, d2, start2);
				start2 = d2;
			}

		}
		if(Math.abs(cost.getValue() - value) > EPS){
			log(name() + "::evaluateMoveTripOneVehicle FAILED???, INCONSISTENT????");
		}
		return new double[] { delta, newDistanceK };

		// return Integer.MAX_VALUE;
	}
	public void performMoveTripOneVehicle(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, boolean DIXAVEGAN, boolean loadConstraint) {
		// return change of totalCost AND cost of the merged trip

		if (vt1.vehicle != vt2.vehicle)
			return;

		String debugCode = "";
		Vehicle vh = vt1.vehicle;

		// check if vt1 and be moved to vt2 w.r.t exclusiveVehicleLocations
		for (Point p : vt1.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vt2.vehicle.getCode())
					.contains(lc))
				return;
				
		}
		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load + vt2.load > vt2.vehicle.getWeight()) {
				if (vh.getCode().equals(debugCode)) {
					log(name() + "::evaluateMoveTripOneVehicle, vehicle "
							+ vh.getCode() + ", vt1.load = " + vt1.load
							+ " vt2.load =  " + vt2.load + " > cap = "
							+ vt2.vehicle.getWeight() + " RETURN INFTY");
				}
				return;
			}
		}

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		//double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		//double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return;
		if (lst_pickup_2.size() == 0)
			return;
		int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_pickup_2.get(0));

		Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		int k = XR.route(np);// index of route containing points of vt2

		// if (vt1.load + vt2.load > cap[k - 1])return delta;

		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		// for(int i = 0; i < n; i++){
		// performRemoveOnePoint(XR, lst_pickup_1.get(i));
		// performRemoveOnePoint(XR, lst_delivery_1.get(n-1-i));
		// }
		// System.out.println(name() +
		// "::evaluateMoveTrip, after remove trip, value = " + cost.getValue());

		// re-insert into vt2 in an optimal way

		Point start = np;
		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

	
		Point start_p = start2;
		if (vt1.contains(start_p)) {
			start_p = start1;// vt1 is before vt2 on the same route[k]
		}

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		

	}


	public double evaluateMoveTrip(VarRoutesVR XR, VehicleTrip vt1, int k,
			boolean DIXAVEGAN, boolean loadConstraint) {

		// check if t[i] and be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		Point startPoint = XR.startPoint(k);
		Vehicle vh = mPoint2Vehicle.get(startPoint);

		for (Point p : vt1.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}
		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load > vh.getWeight())
				return Integer.MAX_VALUE;
		}

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return delta;
		int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover

		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > vh.getWeight()) {// cap[k - 1]) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		for (int j = 0; j < lst_pickup_1.size(); j++) {
			Point p1 = lst_pickup_1.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p1);
			performAddOnePoint(XR, p1, start1);
			// performAddOnePoint(XR, d1,p1);
			start1 = p1;
		}
		for (int j = 0; j < lst_pickup_1.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d1 = lst_delivery_1.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d1);
			performAddOnePoint(XR, d1, start1);
			start1 = d1;
		}

		return delta;

		// return Integer.MAX_VALUE;
	}
	public boolean contains(ArrayList<Point> L, Point p){
		for(Point q: L) if(q == p) return true;
		return false;
	}
	public double computeLoad(ArrayList<Point> lst_pickup){
		double load = 0;
		for(Point p: lst_pickup)
			load += mPoint2Demand.get(p);
		return load;
	}
	public String shortInfo(){
		String s = "cost = " + cost.getValue();
		int nbIntV = 0;
		int nbExtV = 0;
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.emptyRoute(k)) continue;
			Point start = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(start);
			if(isInternalVehicle(vh)) nbIntV++;
			else nbExtV++;
		}
		s = s + ", nbIntVehicles = " + nbIntV + ", nbExtVehicles = " + nbExtV;
		return s;
	}
	
	public void reinsert(Point p, VarRoutesVR XR, HashMap<Point, Point> mPoint2Prev){
		if(XR.contains(p)) return;
		Point pp = mPoint2Prev.get(p);
		if(!XR.contains(pp)){
			reinsert(pp,XR,mPoint2Prev);
		}
		XR.getVRManager().performAddOnePoint(p, pp);
	}
	public double evaluateMoveSequencePoints(VarRoutesVR XR,
			ArrayList<Point> lst_pickup, ArrayList<Point> lst_delivery,
			VehicleTrip vt, Point s, boolean DIXAVEGAN, boolean loadConstraint) {
		// move sequence of delivery points (lst_pickup, lst_delivery) to vt
		// containing s, after s
		
				
		if(vt != null)for(Point p: lst_delivery) if(vt.contains(p)) return Integer.MAX_VALUE;
		
		if(lst_pickup == null || lst_pickup.size() == 0) return Integer.MAX_VALUE;
		Point s1 = lst_pickup.get(0);
		int k1 = XR.route(s1);
		
		
		//System.out.println(name() + "::evaluateMoveSequencePoints, XR = " + XR.toStringShort());
		//System.out.println(name() + "::evaluateMoveSequencePoints, lst_pickup = " + toStringListPoints(lst_pickup)
		//		+ ", lst_delivery = " + toStringListPoints(lst_delivery) + ", vt = " + (vt != null ? vt.seqPointString() : "NIL")
		//		+ ", s = " + s.ID);
		
		// check if t[i] and be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		Point startPoint = s;
		int k = XR.route(s);
		Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
		double load = 0;
		for (Point p : lst_pickup)
			load += mPoint2Demand.get(p);
		
		for (Point p : lst_pickup) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}
		for (Point p : lst_delivery) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			double loadVH = 0;
			if(vt != null) loadVH = vt.load;
			if (load + loadVH > vh.getWeight())
				return Integer.MAX_VALUE;
		}
		ArrayList<Point> restore_L1 = XR.getClientPointList(k1);
		ArrayList<Point> restore_L = XR.getClientPointList(k);

		ArrayList<Point> lst_pickup_0 = null;
		if (vt != null)
			lst_pickup_0 = vt.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_0 = null;
		if (vt != null)
			lst_delivery_0 = vt.getDeliverySeqPoints();

		/*
		HashMap<Point, Point> mPoint2Prev = new HashMap<Point, Point>();
		HashSet<Point> S = new HashSet<Point>();
		
		for(Point p: lst_pickup){
			mPoint2Prev.put(p, XR.prev(p));
			S.add(p);
		}
		for(Point p: lst_delivery){
			mPoint2Prev.put(p, XR.prev(p));
			S.add(p);
		}
		if(lst_pickup_0 != null)
			for(Point p: lst_pickup_0){
				mPoint2Prev.put(p, XR.prev(p));
				S.add(p);
			}
		if(lst_delivery_0 != null)
			for(Point p: lst_delivery_0){
				mPoint2Prev.put(p, XR.prev(p));
				S.add(p);
			}
		*/
		
		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup.size() == 0)
			return delta;
		
		Point restore_start_pickup = XR.prev(lst_pickup.get(0));// store the
																// point for
		// recover

		Point restore_start_delivery = XR.prev(lst_delivery.get(0));

		Point restore_start_vt = null;
		if (lst_pickup_0 != null)
			restore_start_vt = XR.prev(lst_pickup_0.get(0));
		boolean LOG = false;
		if(lst_pickup_0 != null && lst_pickup_0.size() > 0){
			if(lst_pickup_0.get(0).ID == 52) LOG = true;
		}
		
		
		if(restore_start_vt != null){
			//if(LOG)
			//log(name() + "::evaluateMoveSequencePoints, restore_start_vt = " + restore_start_vt.ID);
			while(true){
				//System.out.println("restore_start_vt = " + restore_start_vt.ID);
				if(restore_start_vt == XR.startPoint(k)) break;
				if(contains(lst_pickup, restore_start_vt) || contains(lst_delivery,restore_start_vt))
					restore_start_vt = XR.prev(restore_start_vt);
				else break;
			}
			//if(LOG)
			//	log(name() + "::evaluateMoveSequencePoints, after WHILE restore_start_vt = " + restore_start_vt.ID);
				
		}
		boolean restoreVTFirst = false;
		if (vt != null)
			if (vt.contains(restore_start_pickup))
				restoreVTFirst = true;

		for (Point x : lst_pickup) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : lst_delivery) {
			mgr.performRemoveOnePoint(x);
		}

		if (vt != null)
			for (Point p : vt.seqPoints) {
				mgr.performRemoveOnePoint(p);
			}

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		if (lst_delivery_0 != null)
			for (Point x : lst_delivery_0)
				L.add(x);

		for (Point x : lst_delivery)
			L.add(x);

		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = restore_start_vt;
		if(start_p == null) start_p = XR.startPoint(k);// vt is null, new VehicleTrip

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			//System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			//delivery.ID + ", pickup = " + pickup.ID + ", start_p = " + start_p.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > vh.getWeight()) {// cap[k - 1]) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		//System.out.println(name() + "::evaluateMoveSequencePoints, new cost = " +
		//cost.getValue() + ", delta = " + delta + ", restoreVTFirst = " + restoreVTFirst);

		//for(Point p: S){
		//	reinsert(p, XR, mPoint2Prev);
		//}
		XR.getVRManager().performRemoveAllClientPoints(k);
		Point start_point = XR.startPoint(k);
		for(Point p: restore_L){
			//mgr.performAddOnePoint(p, start_point);
			performAddOnePoint(XR, p, start_point);
			start_point = p;
		}
		if(k1 != k){
			XR.getVRManager().performRemoveAllClientPoints(k1);
			start_point = XR.startPoint(k1);
			for(Point p: restore_L1){
				//mgr.performAddOnePoint(p, start_point);
				performAddOnePoint(XR, p, start_point);
				start_point = p;
			}				
		}
		
		/*
		 * 
		if (restoreVTFirst) {
			Point start = restore_start_vt;
			for (int j = 0; j < lst_pickup_0.size(); j++) {
				Point p = lst_pickup_0.get(j);
				performRemoveOnePoint(XR, p);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(p = " + p.ID + "," + start.ID);
				performAddOnePoint(XR, p, start);
				start = p;
			}
			for (int j = 0; j < lst_delivery_0.size(); j++) {
				Point d = lst_delivery_0.get(j);
				performRemoveOnePoint(XR, d);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(d = " + d.ID + "," + start.ID);
				performAddOnePoint(XR, d, start);
				start = d;
			}
			start = restore_start_pickup;
			for (int j = 0; j < lst_pickup.size(); j++) {
				Point p = lst_pickup.get(j);
				performRemoveOnePoint(XR, p);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(p = " + p.ID + "," + start.ID);
				performAddOnePoint(XR, p, start);
				start = p;
			}
			start = restore_start_delivery;
			for (int j = 0; j < lst_delivery.size(); j++) {
				Point d = lst_delivery.get(j);
				performRemoveOnePoint(XR, d);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(d = " + d.ID + "," + start.ID);
				performAddOnePoint(XR, d, start);
				start = d;
			}
		} else {
			Point start = restore_start_pickup;
			for (int j = 0; j < lst_pickup.size(); j++) {
				Point p = lst_pickup.get(j);
				performRemoveOnePoint(XR, p);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(p = " + p.ID + "," + start.ID);
				performAddOnePoint(XR, p, start);
				start = p;
			}
			start = restore_start_delivery;
			for (int j = 0; j < lst_delivery.size(); j++) {
				Point d = lst_delivery.get(j);
				performRemoveOnePoint(XR, d);
				//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(d = " + d.ID + "," + start.ID);
				performAddOnePoint(XR, d, start);
				start = d;
			}
			if (restore_start_vt != null) {
				start = restore_start_vt;
				for (int j = 0; j < lst_pickup_0.size(); j++) {
					Point p = lst_pickup_0.get(j);
					performRemoveOnePoint(XR, p);
					//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(p = " + p.ID + "," + start.ID);
					performAddOnePoint(XR, p, start);
					start = p;
				}
				for (int j = 0; j < lst_delivery_0.size(); j++) {
					Point d = lst_delivery_0.get(j);
					performRemoveOnePoint(XR, d);
					//System.out.println(name() + "::evaluateMoveSequencePoints, restore, addOnePoint(d = " + d.ID + "," + start.ID);
					performAddOnePoint(XR, d, start);
					start = d;
				}
			}
		}
		*/
		
		return delta;
	}

	public double[] evaluateMoveSetPointsNewRoute(VarRoutesVR XR,
			ArrayList<Point> lst_pickup, ArrayList<Point> lst_delivery,
			int k, boolean DIXAVEGAN, boolean loadConstraint) {
		// move set of delivery points (lst_pickup, lst_delivery) route[k] of XR, at the begining of route[k]
		// lst_pickup, lst_delivery is not a neccesarily a consecutive sequence of points, just a set
		
		Point s = XR.startPoint(k);
				
		//if(vt != null)for(Point p: lst_delivery) if(vt.contains(p)) return Integer.MAX_VALUE;
		if(lst_pickup == null || lst_pickup.size() == 0) return new double[]{1,Integer.MAX_VALUE};
		
		/*
		HashMap<Point, Point> mPoint2Prev = new HashMap<Point, Point>();
		HashMap<Point, Integer> mPoint2Sequence = new HashMap<Point, Integer>();
		for(Point p: lst_pickup){
			mPoint2Prev.put(p, XR.prev(p));
			mPoint2Sequence.put(p, XR.index(p));
		}
		for(Point p: lst_delivery){
			mPoint2Prev.put(p, XR.prev(p));
			mPoint2Sequence.put(p, XR.index(p));
		}
		*/
		
		Point p1 = lst_pickup.get(0);
		int k1 = XR.route(p1);
		ArrayList<Point> restore_L1 = XR.getClientPointList(k1);
		ArrayList<Point> restore_L = XR.getClientPointList(k);
		
		//System.out.println(name() + "::evaluateMoveSequencePoints, XR = " + XR.toStringShort());
		//System.out.println(name() + "::evaluateMoveSequencePoints, lst_pickup = " + toStringListPoints(lst_pickup)
		//		+ ", lst_delivery = " + toStringListPoints(lst_delivery)
		//		+ ", k = " + k + ", s = " + s.ID);
		
		// check if t[i] and be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		Point startPoint = s;
		
		
		if(k == k1) return new double[]{1,Integer.MAX_VALUE};// consider only cases where points of lst_pickup and lst_delivery is not on the same route with s
		
		Vehicle vh = mPoint2Vehicle.get(s);
		double load = 0;
		for (Point p : lst_pickup)
			load += mPoint2Demand.get(p);
		
		for (Point p : lst_pickup) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return new double[]{1,Integer.MAX_VALUE};
		}
		for (Point p : lst_delivery) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return new double[]{1,Integer.MAX_VALUE};
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			double loadVH = 0;
			//if(vt != null) loadVH = vt.load;
			if (load + loadVH > vh.getWeight())
				return new double[]{1,Integer.MAX_VALUE};
		}

		ArrayList<Point> lst_pickup_0 = null;
		//if (vt != null)
		//	lst_pickup_0 = vt.getPickupSeqPoints();
		//ArrayList<Point> lst_delivery_0 = null;
		//if (vt != null)
		//	lst_delivery_0 = vt.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		
		boolean retOK = true;

		for (Point x : lst_pickup) {
			boolean ok = mgr.performRemoveOnePoint(x);
			if(!ok) retOK = false;
		}
		for (Point x : lst_delivery) {
			boolean ok = mgr.performRemoveOnePoint(x);
			if(!ok) retOK = false;
		}

		

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		

		for (Point x : lst_delivery)
			L.add(x);

		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			//System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			//delivery.ID + ", pickup = " + pickup.ID + ", start_p = " + start_p.ID);
			boolean ok = mgr.performAddOnePoint(pickup, start_p);
			if(!ok) retOK = false;
			ok = mgr.performAddOnePoint(delivery, pickup);
			if(!ok) retOK = false;
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > vh.getWeight()) {// cap[k - 1]) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		//System.out.println(name() + "::evaluateMoveSetPointsNewRoute, new cost = " +
		//cost.getValue() + ", delta = " + delta + " START RECOVER"); 
		

		// recover
		XR.getVRManager().performRemoveAllClientPoints(k1);
		XR.getVRManager().performRemoveAllClientPoints(k);
		
		Point start_point = XR.startPoint(k1);
		for(Point p: restore_L1){
			boolean ok = performAddOnePoint(XR, p, start_point);
			if(!ok) retOK = false;
			//System.out.println(name() + "::evaluateMoveSetPointsNewRoute, recover, addOnePoint(" + p.ID + "," + start_point.ID + "), ok = " + ok);
			start_point = p;
		}
		
		start_point = XR.startPoint(k);
		for(Point p: restore_L){
			boolean ok = performAddOnePoint(XR, p, start_point);
			if(!ok) retOK = false;
			//System.out.println(name() + "::evaluateMoveSetPointsNewRoute, recover, addOnePoint(" + p.ID + "," + start_point.ID + "), ok = " + ok);
			start_point = p;
		}
		
		if(retOK)
			return new double[]{1,delta};
		else 
			return new double[]{-1,delta};
	}

	public boolean performMoveSetPointsNewRoute(VarRoutesVR XR,
			ArrayList<Point> lst_pickup, ArrayList<Point> lst_delivery,
			int k, boolean DIXAVEGAN, boolean loadConstraint) {
		// move set of delivery points (lst_pickup, lst_delivery) route[k] of XR, at the begining of route[k]
		// lst_pickup, lst_delivery is not a neccesarily a consecutive sequence of points, just a set
		
		Point s = XR.startPoint(k);
				
		//if(vt != null)for(Point p: lst_delivery) if(vt.contains(p)) return Integer.MAX_VALUE;
		if(lst_pickup == null || lst_pickup.size() == 0) return true;
		
		/*
		HashMap<Point, Point> mPoint2Prev = new HashMap<Point, Point>();
		HashMap<Point, Integer> mPoint2Sequence = new HashMap<Point, Integer>();
		for(Point p: lst_pickup){
			mPoint2Prev.put(p, XR.prev(p));
			mPoint2Sequence.put(p, XR.index(p));
		}
		for(Point p: lst_delivery){
			mPoint2Prev.put(p, XR.prev(p));
			mPoint2Sequence.put(p, XR.index(p));
		}
		*/
		
		Point p1 = lst_pickup.get(0);
		int k1 = XR.route(p1);
		ArrayList<Point> restore_L1 = XR.getClientPointList(k1);
		
		//System.out.println(name() + "::performMoveSetPointsNewRoute, XR = " + XR.toStringShort());
		//System.out.println(name() + "::performMoveSetPointsNewRoute, lst_pickup = " + toStringListPoints(lst_pickup)
		//		+ ", lst_delivery = " + toStringListPoints(lst_delivery) 
		//		+ ", k = " + k + ", s = " + s.ID);
		
		// check if t[i] and be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		Point startPoint = s;
		
		
		if(k == k1) return true;// consider only cases where points of lst_pickup and lst_delivery is not on the same route with s
		
		Vehicle vh = mPoint2Vehicle.get(s);
		double load = 0;
		for (Point p : lst_pickup)
			load += mPoint2Demand.get(p);
		
		for (Point p : lst_pickup) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return true;
		}
		for (Point p : lst_delivery) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return true;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			double loadVH = 0;
			//if(vt != null) loadVH = vt.load;
			if (load + loadVH > vh.getWeight())
				return true;
		}

		ArrayList<Point> lst_pickup_0 = null;
		//if (vt != null)
		//	lst_pickup_0 = vt.getPickupSeqPoints();
		//ArrayList<Point> lst_delivery_0 = null;
		//if (vt != null)
		//	lst_delivery_0 = vt.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		
		boolean retOK = true;

		for (Point x : lst_pickup) {
			boolean ok = mgr.performRemoveOnePoint(x);
			if(!ok) retOK = false;
		}
		for (Point x : lst_delivery) {
			boolean ok = mgr.performRemoveOnePoint(x);
			if(!ok) retOK = false;
		}

		

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		

		for (Point x : lst_delivery)
			L.add(x);

		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			//System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			//delivery.ID + ", pickup = " + pickup.ID + ", start_p = " + start_p.ID);
			boolean ok = mgr.performAddOnePoint(pickup, start_p);
			if(!ok) retOK = false;
			ok = mgr.performAddOnePoint(delivery, pickup);
			if(!ok) retOK = false;
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		propagate(XR,k1);
		
		return retOK;
	}

	
	public void performMoveSequencePoints(VarRoutesVR XR,
			ArrayList<Point> lst_pickup, ArrayList<Point> lst_delivery,
			VehicleTrip vt, Point s, boolean DIXAVEGAN, boolean loadConstraint) {
		// move sequence of delivery points (lst_pickup, lst_delivery) to vt
		// containing s, after s

		if(vt != null)for(Point p: lst_delivery) if(vt.contains(p)) return;
		
		//System.out.println(name() + "::evaluateMoveSequencePoints, XR = " + XR.toStringShort());
		//System.out.println(name() + "::performMoveSequencePoints, lst_pickup = " + toStringListPoints(lst_pickup)
		//		+ ", lst_delivery = " + toStringListPoints(lst_delivery) + ", vt = " + (vt != null ? vt.seqPointString() : "NIL")
		//		+ ", s = " + s.ID);
		
		if(lst_pickup.size() == 0 || lst_delivery.size() == 0) return;
		Point s0 = lst_pickup.get(0);
		int k0 = XR.route(s0);
		
		// check if t[i] and be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		Point startPoint = s;
		int k = XR.route(s);
		Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
		double load = 0;
		for (Point p : lst_pickup)
			load += mPoint2Demand.get(p);
		
		for (Point p : lst_pickup) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return;
		}
		for (Point p : lst_delivery) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			double loadVH = 0;
			if(vt != null) loadVH = vt.load;
			if (load + loadVH > vh.getWeight())
				return;
		}

		ArrayList<Point> lst_pickup_0 = null;
		if (vt != null)
			lst_pickup_0 = vt.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_0 = null;
		if (vt != null)
			lst_delivery_0 = vt.getDeliverySeqPoints();

		
		if (lst_pickup.size() == 0)
			return;
		
		Point restore_start_pickup = XR.prev(lst_pickup.get(0));// store the
																// point for
		// recover

		Point restore_start_delivery = XR.prev(lst_delivery.get(0));

		Point restore_start_vt = null;
		if (lst_pickup_0 != null)
			restore_start_vt = XR.prev(lst_pickup_0.get(0));
		
		if(restore_start_vt != null)
			while(true){
				//System.out.println("restore_start_vt = " + restore_start_vt.ID);
				if(restore_start_vt == XR.startPoint(k)) break;
				if(contains(lst_pickup, restore_start_vt) || contains(lst_delivery,restore_start_vt))
					restore_start_vt = XR.prev(restore_start_vt);
				else break;
			}
		
		//boolean restoreVTFirst = false;
		//if (vt != null)
		//	if (vt.contains(restore_start_pickup))
		//		restoreVTFirst = true;

		for (Point x : lst_pickup) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : lst_delivery) {
			mgr.performRemoveOnePoint(x);
		}

		if (vt != null)
			for (Point p : vt.seqPoints) {
				mgr.performRemoveOnePoint(p);
			}

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		if (lst_delivery_0 != null)
			for (Point x : lst_delivery_0)
				L.add(x);

		for (Point x : lst_delivery)
			L.add(x);

		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = restore_start_vt;
		if(start_p == null) start_p = XR.startPoint(k);// vt is null, new VehicleTrip

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			//System.out.println(name() + "::performMoveSequencePoints, delivery = " +
			//delivery.ID + ", pickup = " + pickup.ID + ", start_p = " + start_p.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		propagate(XR, k0);
	}

	public String toStringListPoints(ArrayList<Point> lst) {
		String s = "";
		for (Point p : lst) {
			s = s + p.ID + "[" + mPoint2Type.get(p) + "] ";
		}
		return s;
	}

	public double evaluateMoveTrip(VarRoutesVR XR, Point pickup,
			Point delivery, Point s, boolean DIXAVEGAN, boolean loadConstraint) {

		// check if (pickup,delivery) can be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		int k = XR.route(s);

		Point startPoint = XR.startPoint(k);
		Vehicle vh = mPoint2Vehicle.get(startPoint);
		ArrayList<Point> lst_pickup = new ArrayList<Point>();// vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery = new ArrayList<Point>();// vt1.getDeliverySeqPoints();
		ArrayList<Point> ss = new ArrayList<Point>();
		ss.add(pickup);
		ss.add(delivery);

		lst_pickup.add(pickup);
		lst_delivery.add(delivery);

		for (Point p : ss) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			if (mPoint2Demand.get(pickup) > vh.getWeight())
				return Integer.MAX_VALUE;
		}

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		Point start1 = XR.prev(lst_pickup.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_delivery.get(0));

		// remove (pickup,delivery)
		for (Point x : ss) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point d = seq[i];
			Point p = getPickupOfDelivery(d);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(p, start_p);
			mgr.performAddOnePoint(d, p);
			start_p = p;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > vh.getWeight()) {// cap[k - 1]) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		for (int j = 0; j < lst_pickup.size(); j++) {
			Point p = lst_pickup.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p);
			performAddOnePoint(XR, p, start1);
			// mgr.performRemoveOnePoint(p);
			// mgr.performAddOnePoint(p, start1);
			start1 = p;
		}
		for (int j = 0; j < lst_pickup.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d = lst_delivery.get(j);
			performRemoveOnePoint(XR, d);
			performAddOnePoint(XR, d, start2);
			// mgr.performRemoveOnePoint(d);
			// mgr.performAddOnePoint(d, start2);

			start2 = d;
		}

		return delta;

		// return Integer.MAX_VALUE;
	}

	public double evaluateExchangeRequestPoints(VarRoutesVR XR, Point pickup1,
			Point delivery1, VehicleTrip vt2, Point pickup2, Point delivery2,
			VehicleTrip vt1, boolean DIXAVEGAN, boolean loadConstraint) {

		// System.out.println(name() + "::evaluateExchangeRequestPoints, XR = "
		// + XR.toStringShort());

		// System.out.println(name() +
		// "::evaluateExchangeRequestPoints, pickup1 = " + pickup1.ID +
		// ", delivery1 = " + delivery1.ID +
		// ", vt2 = " + vt2.seqPointString() + ", pcikup2 = " + pickup2.ID +
		// ", delivery2 = " + delivery2.ID + ", vt1 = " +
		// vt1.seqPointString());

		// (pickup1,delivery1) belongs to vt1, (pickup2, delivery2) belongs to
		// vt2
		// move (pickup1, delivery1) to vt2 and (pickup2,delivery2) to vt1
		Vehicle vh1 = vt1.vehicle;
		Vehicle vh2 = vt2.vehicle;
		// if(vh1 == vh2) return Integer.MAX_VALUE;

		int k1 = XR.route(pickup1);
		int k2 = XR.route(pickup2);

		ArrayList<Point> lst_pickup1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery2 = vt2.getDeliverySeqPoints();

		String lc = mPoint2LocationCode.get(pickup1);
		if (mVehicle2NotReachedLocations.get(vh2.getCode()).contains(lc))
			return Integer.MAX_VALUE;
		lc = mPoint2LocationCode.get(delivery1);
		if (mVehicle2NotReachedLocations.get(vh2.getCode()).contains(lc))
			return Integer.MAX_VALUE;
		lc = mPoint2LocationCode.get(pickup2);
		if (mVehicle2NotReachedLocations.get(vh1.getCode()).contains(lc))
			return Integer.MAX_VALUE;
		lc = mPoint2LocationCode.get(delivery2);
		if (mVehicle2NotReachedLocations.get(vh1.getCode()).contains(lc))
			return Integer.MAX_VALUE;

		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load - mPoint2Demand.get(pickup1)
					+ mPoint2Demand.get(pickup2) > vh1.getWeight())
				return Integer.MAX_VALUE;
			if (vt2.load - mPoint2Demand.get(pickup2)
					+ mPoint2Demand.get(pickup1) > vh2.getWeight())
				return Integer.MAX_VALUE;
		}

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		Point restore_start1 = XR.prev(lst_pickup1.get(0));// store the point
															// for
		// recover
		Point restore_start2 = XR.prev(lst_pickup2.get(0));

		boolean recoverVT2First = false;
		if (vt2.contains(restore_start1)) {
			recoverVT2First = true;
		}

		Point startPoint1 = XR.prev(lst_pickup1.get(0));
		if (vt1.seqPoints.size() > 2) {
			startPoint1 = lst_pickup1.get(lst_pickup1.size() - 1);
			if (startPoint1 == pickup1)
				startPoint1 = lst_pickup1.get(lst_pickup1.size() - 2);
		}
		Point startPoint2 = XR.prev(lst_pickup2.get(0));
		if (vt2.seqPoints.size() > 2) {
			startPoint2 = lst_pickup2.get(lst_pickup2.size() - 1);
			if (startPoint2 == pickup2)
				startPoint2 = lst_pickup2.get(lst_pickup2.size() - 2);
		}
		// System.out.println(name() +
		// "::evaluateExchangeRequestPoints, startPoint1 = " + startPoint1.ID +
		// ", startPoint2 = " + startPoint2.ID);

		for (Point p : vt1.seqPoints) {
			mgr.performRemoveOnePoint(p);
		}
		for (Point p : vt2.seqPoints) {
			mgr.performRemoveOnePoint(p);
		}

		// re-insert into vt1 in an optimal way
		ArrayList<Point> L1 = new ArrayList<Point>();
		for (Point x : lst_delivery1)
			if (x != delivery1)
				L1.add(x);
		L1.add(delivery2);
		Point[] seq1 = getBestSequenceGreedy(startPoint1, L1, DIXAVEGAN);

		ArrayList<Point> L2 = new ArrayList<Point>();
		for (Point x : lst_delivery2)
			if (x != delivery2)
				L2.add(x);
		L2.add(delivery1);
		Point[] seq2 = getBestSequenceGreedy(startPoint2, L2, DIXAVEGAN);

		String seq1Str = "";
		for (int i = 0; i < seq1.length; i++)
			seq1Str += seq1[i] + ",";
		String seq2Str = "";
		for (int i = 0; i < seq2.length; i++)
			seq2Str += seq2[i] + ",";

		// System.out.println(name() +
		// "::evaluateExchangeRequestPoints, seq1 = " + seq1Str + ", seq2 = " +
		// seq2Str);

		// re-insert seq into route

		if (recoverVT2First) {// restore_start2 is not a removed point,
								// restore_start1 is
			Point start_p = restore_start2;
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq2.length - 1; i >= 0; i--) {
				Point d = seq2[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq2, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}
			// start = XR.prev(vt2.seqPoints.get(0));
			start_p = seq2[seq2.length - 1];// restore_start1; concat seq1
											// follwoing the last point of seq2
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq1.length - 1; i >= 0; i--) {
				Point d = seq1[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq1, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}

		} else {
			// restore_start1 is not a removed point
			Point start_p = restore_start1;
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq1.length - 1; i >= 0; i--) {
				Point d = seq1[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq1, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}
			start_p = restore_start2;
			if (vt1.contains(restore_start2))
				start_p = seq1[seq1.length - 1];
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq2.length - 1; i >= 0; i--) {
				Point d = seq2[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq2, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}

		}

		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k1);
		propagate(XR, k2);

		int violations = 0;
		for (Point q = XR.startPoint(k1); q != XR.endPoint(k1); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			}
			if (!checkConflictItemsAtPoint(q)) {
				violations++;
			}
			if (awn.getSumWeights(q) > vh1.getWeight()) {// cap[k - 1]) {
				violations++;
			}
		}
		for (Point q = XR.startPoint(k2); q != XR.endPoint(k2); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			}
			if (!checkConflictItemsAtPoint(q)) {
				violations++;
			}
			if (awn.getSumWeights(q) > vh2.getWeight()) {// cap[k - 1]) {
				violations++;
			}
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k1);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));
		q = XR.endPoint(k2);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		if (recoverVT2First) {
			Point start2 = restore_start2;
			for (int j = 0; j < lst_pickup2.size(); j++) {
				Point p = lst_pickup2.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, start2);
				start2 = p;
			}
			for (int j = 0; j < lst_delivery2.size(); j++) {
				Point d = lst_delivery2.get(j);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, start2);
				start2 = d;
			}

			Point start1 = restore_start1;
			for (int j = 0; j < lst_pickup1.size(); j++) {
				Point p = lst_pickup1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, start1);
				start1 = p;
			}
			for (int j = 0; j < lst_delivery1.size(); j++) {
				Point d = lst_delivery1.get(j);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, start1);
				start1 = d;
			}

		} else {
			Point start1 = restore_start1;
			for (int j = 0; j < lst_pickup1.size(); j++) {
				Point p = lst_pickup1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, start1);
				start1 = p;
			}
			for (int j = 0; j < lst_delivery1.size(); j++) {
				Point d = lst_delivery1.get(j);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, start1);
				start1 = d;
			}

			Point start2 = restore_start2;
			for (int j = 0; j < lst_pickup2.size(); j++) {
				Point p = lst_pickup2.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, start2);
				start2 = p;
			}
			for (int j = 0; j < lst_delivery2.size(); j++) {
				Point d = lst_delivery2.get(j);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, start2);
				start2 = d;
			}

		}

		return delta;

		// return Integer.MAX_VALUE;
	}

	public void performExchangeRequestPoints(VarRoutesVR XR, Point pickup1,
			Point delivery1, VehicleTrip vt2, Point pickup2, Point delivery2,
			VehicleTrip vt1, boolean DIXAVEGAN, boolean loadConstraint) {
		// (pickup1,delivery1) belongs to vt1, (pickup2, delivery2) belongs to
		// vt2
		// move (pickup1, delivery1) to vt2 and (pickup2,delivery2) to vt1
		Vehicle vh1 = vt1.vehicle;
		Vehicle vh2 = vt2.vehicle;
		// if(vh1 == vh2) return Integer.MAX_VALUE;

		int k1 = XR.route(pickup1);
		int k2 = XR.route(pickup2);

		ArrayList<Point> lst_pickup1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery2 = vt2.getDeliverySeqPoints();

		String lc = mPoint2LocationCode.get(pickup1);
		if (mVehicle2NotReachedLocations.get(vh2.getCode()).contains(lc))
			return;
		lc = mPoint2LocationCode.get(delivery1);
		if (mVehicle2NotReachedLocations.get(vh2.getCode()).contains(lc))
			return;
		lc = mPoint2LocationCode.get(pickup2);
		if (mVehicle2NotReachedLocations.get(vh1.getCode()).contains(lc))
			return;
		lc = mPoint2LocationCode.get(delivery2);
		if (mVehicle2NotReachedLocations.get(vh1.getCode()).contains(lc))
			return;

		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load - mPoint2Demand.get(pickup1)
					+ mPoint2Demand.get(pickup2) > vh1.getWeight())
				return;
			if (vt2.load - mPoint2Demand.get(pickup2)
					+ mPoint2Demand.get(pickup1) > vh2.getWeight())
				return;
		}

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		Point restore_start1 = XR.prev(lst_pickup1.get(0));// store the point
															// for
		// recover
		Point restore_start2 = XR.prev(lst_pickup2.get(0));

		boolean recoverVT2First = false;
		if (vt2.contains(restore_start1)) {
			recoverVT2First = true;
		}

		Point startPoint1 = XR.prev(lst_pickup1.get(0));
		if (vt1.seqPoints.size() > 2) {
			startPoint1 = lst_pickup1.get(lst_pickup1.size() - 1);
			if (startPoint1 == pickup1)
				startPoint1 = lst_pickup1.get(lst_pickup1.size() - 2);
		}
		Point startPoint2 = XR.prev(lst_pickup2.get(0));
		if (vt2.seqPoints.size() > 2) {
			startPoint2 = lst_pickup2.get(lst_pickup2.size() - 1);
			if (startPoint2 == pickup2)
				startPoint2 = lst_pickup2.get(lst_pickup2.size() - 2);
		}
		// System.out.println(name() +
		// "::evaluateExchangeRequestPoints, startPoint1 = " + startPoint1.ID +
		// ", startPoint2 = " + startPoint2.ID);

		for (Point p : vt1.seqPoints) {
			mgr.performRemoveOnePoint(p);
		}
		for (Point p : vt2.seqPoints) {
			mgr.performRemoveOnePoint(p);
		}

		// re-insert into vt1 in an optimal way
		ArrayList<Point> L1 = new ArrayList<Point>();
		for (Point x : lst_delivery1)
			if (x != delivery1)
				L1.add(x);
		L1.add(delivery2);
		Point[] seq1 = getBestSequenceGreedy(startPoint1, L1, DIXAVEGAN);

		ArrayList<Point> L2 = new ArrayList<Point>();
		for (Point x : lst_delivery2)
			if (x != delivery2)
				L2.add(x);
		L2.add(delivery1);
		Point[] seq2 = getBestSequenceGreedy(startPoint2, L2, DIXAVEGAN);

		String seq1Str = "";
		for (int i = 0; i < seq1.length; i++)
			seq1Str += seq1[i] + ",";
		String seq2Str = "";
		for (int i = 0; i < seq2.length; i++)
			seq2Str += seq2[i] + ",";

		// System.out.println(name() +
		// "::evaluateExchangeRequestPoints, seq1 = " + seq1Str + ", seq2 = " +
		// seq2Str);

		// re-insert seq into route

		if (recoverVT2First) {// restore_start2 is not a removed point,
								// restore_start1 is
			Point start_p = restore_start2;
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq2.length - 1; i >= 0; i--) {
				Point d = seq2[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq2, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}
			// start = XR.prev(vt2.seqPoints.get(0));
			start_p = seq2[seq2.length - 1];// restore_start1; concat seq1
											// follwoing the last point of seq2
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq1.length - 1; i >= 0; i--) {
				Point d = seq1[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq1, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}

		} else {
			// restore_start1 is not a removed point
			Point start_p = restore_start1;
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq1.length - 1; i >= 0; i--) {
				Point d = seq1[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq1, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}
			start_p = restore_start2;
			if (vt1.contains(restore_start2))
				start_p = seq1[seq1.length - 1];
			// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
			// ", start_p = " + start_p.ID);
			for (int i = seq2.length - 1; i >= 0; i--) {
				Point d = seq2[i];
				Point p = getPickupOfDelivery(d);
				// System.out.println(name() +
				// "::evaluateExchangeRequestPoints, for seq2, start_p = " +
				// start_p.ID + ", d = " +
				// d.ID + ", p = " + p.ID);
				mgr.performAddOnePoint(p, start_p);
				mgr.performAddOnePoint(d, p);
				start_p = p;
			}

		}

		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k1);
		propagate(XR, k2);

	}

	public double evaluateMoveTripNewVehicle(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, Point startPointNewRoute, boolean DIXAVEGAN,
			boolean loadConstraint) {
		// if (vt1.vehicle == vt2.vehicle)
		// return Integer.MAX_VALUE;

		Vehicle newVehicle = mPoint2Vehicle.get(startPointNewRoute);

		// check if t[i] and be moved to t[j] w.r.t exclusiveVehicleLocations
		for (Point p : vt1.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(newVehicle.getCode())
					.contains(lc))
				return Integer.MAX_VALUE;
		}
		for (Point p : vt2.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(newVehicle.getCode())
					.contains(lc))
				return Integer.MAX_VALUE;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt1.load + vt2.load > newVehicle.getWeight())
				return Integer.MAX_VALUE;
		}

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return delta;
		if (lst_pickup_2.size() == 0)
			return delta;
		// int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_pickup_2.get(0));
		// Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);

		int k = XR.route(startPointNewRoute);// index of route containing points
												// of vt2

		// if (vt1.load + vt2.load > cap[k - 1])
		// return delta;

		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		// for(int i = 0; i < n; i++){
		// performRemoveOnePoint(XR, lst_pickup_1.get(i));
		// performRemoveOnePoint(XR, lst_delivery_1.get(n-1-i));
		// }
		// System.out.println(name() +
		// "::evaluateMoveTrip, after remove trip, value = " + cost.getValue());

		// re-insert into vt2 in an optimal way

		Point start = startPointNewRoute;
		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPointNewRoute;
		// if (vt1.contains(start_p))
		// start_p = start1;// vt1 is before vt2 on the same route[k]

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after re-insert, XR(k) = " +
		// XR.toStringRoute(k));
		propagate(XR, k);
		int violations = 0;
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > newVehicle.getWeight()) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		// recover
		for (int j = 0; j < lst_pickup_2.size(); j++) {
			Point p2 = lst_pickup_2.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p2);
			performAddOnePoint(XR, p2, start2);
			// performAddOnePoint(XR, d1,p1);
			start2 = p2;
		}
		for (int j = 0; j < lst_pickup_2.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d2 = lst_delivery_2.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d2);
			performAddOnePoint(XR, d2, start2);
			start2 = d2;
		}

		for (int j = 0; j < lst_pickup_1.size(); j++) {
			Point p1 = lst_pickup_1.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p1);
			performAddOnePoint(XR, p1, start1);
			// performAddOnePoint(XR, d1,p1);
			start1 = p1;
		}
		for (int j = 0; j < lst_pickup_1.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d1 = lst_delivery_1.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d1);
			performAddOnePoint(XR, d1, start1);
			start1 = d1;
		}

		return delta;

		// return Integer.MAX_VALUE;
	}

	public boolean fixVehicleTrip(Vehicle vh, VehicleTrip t) {
		// if(true)return false;

		int idx = mVehicle2Index.get(vh);
		for (Point p : t.seqPoints) {
			if (mPoint2Type.get(p).equals("D")) {
				int idxP = mDeliveryPoint2DeliveryIndex.get(p);
				if (CHECK_AND_LOG) {
					//log(name() + "::fixVehicleTrip, idx vehicle = " + idx
					//		+ ", idxP = " + idxP);
					// System.out.println(name() +
					// "::fixVehicleTrip, idx vehicle = " + idx + ", idxP = " +
					// idxP);
					if (fixVehiclePoint == null)
						System.out.println(name()
								+ "::fixVehicleTrip, idx vehicle = " + idx
								+ ", idxP = " + idxP
								+ ", fixVehiclePoint = NULLNULL, BUG???");
				}
				if (fixVehiclePoint[idx][idxP])
					return true;
			}
		}
		return false;
	}

	public boolean fixVehicleDeliveryPoint(Vehicle vh, Point delivery) {
		if (true)
			return false;
		if (mVehicle2Index.get(vh) == null)
			return false;
		if (mDeliveryPoint2DeliveryIndex.get(delivery) == null)
			return false;

		int idx = mVehicle2Index.get(vh);
		int idxP = mDeliveryPoint2DeliveryIndex.get(delivery);
		return fixVehiclePoint[idx][idxP];
	}

	public double evaluateExchangeTrip22Vehicles(VarRoutesVR XR,
			VehicleTrip vt11, VehicleTrip vt21, VehicleTrip vt22,
			VehicleTrip vt12, boolean DIXAVEGAN, boolean loadConstraint) {
		// vt11 of vehicle1 is merged to vt21 of vehicle2 and vt22 of vehicle2
		// is merged to vt12 of vehicle1

		// System.out.println(name() +
		// "::evaluateExchangeTrip22Vehicles, vt11 = "
		// + vt11.seqPointString() + ", vt21 = " + vt21.seqPointString()
		// + ", vt22 = " + vt22.seqPointString() + ", vt12 = "
		// + vt12.seqPointString());

		boolean preCondition = vt11.vehicle == vt12.vehicle
				&& vt21.vehicle == vt22.vehicle && vt11.vehicle != vt22.vehicle;
		if (!preCondition)
			return Integer.MAX_VALUE;
		Vehicle vh1 = vt11.vehicle;
		Vehicle vh2 = vt22.vehicle;

		// int idxVH1 = mVehicle2Index.get(vh1);
		// int idxVH2 = mVehicle2Index.get(vh2);

		// check exclusiveVehicleLocations
		for (Point p : vt11.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh2.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}
		for (Point p : vt22.seqPoints) {
			String lc = mPoint2LocationCode.get(p);
			if (mVehicle2NotReachedLocations.get(vh1.getCode()).contains(lc))
				return Integer.MAX_VALUE;
		}

		// check load w.r.t capacity
		if (loadConstraint) {
			if (vt11.load + vt21.load > vh2.getWeight())
				return Integer.MAX_VALUE;
			if (vt22.load + vt12.load > vh1.getWeight())
				return Integer.MAX_VALUE;
		}

		ArrayList<Point> lst_pickup_11 = vt11.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_11 = vt11.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_12 = vt12.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_12 = vt12.getDeliverySeqPoints();

		ArrayList<Point> lst_pickup_21 = vt21.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_21 = vt21.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_22 = vt22.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_22 = vt22.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_21.size() == 0)
			return delta;
		if (lst_pickup_12.size() == 0)
			return delta;
		// int n = lst_pickup_1.size();
		Point restore_start11 = XR.prev(lst_pickup_11.get(0));// store the point
																// for
		// recover
		Point restore_start22 = XR.prev(lst_pickup_22.get(0));

		Point restore_start12 = XR.prev(lst_pickup_12.get(0));// store the point
																// for
		// recover
		Point restore_start21 = XR.prev(lst_pickup_21.get(0));

		boolean vt11Beforevt12 = false;
		if (vt11.contains(restore_start12))
			vt11Beforevt12 = true;// vt11 must be restored before vt12
		boolean vt21Beforevt22 = false;
		if (vt21.contains(restore_start22))
			vt21Beforevt22 = true;// vt21 must be restored before vt22

		// Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);

		int k1 = XR.route(restore_start11);
		int k2 = XR.route(restore_start22);// index of route containing points
											// of vt2

		// if (vt1.load + vt2.load > cap[k - 1])
		// return delta;

		// remove trip vt11 and vt22
		for (Point x : vt11.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt22.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt12.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt21.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		Point start12 = lst_pickup_12.get(lst_pickup_12.size() - 1);
		ArrayList<Point> L1 = new ArrayList<Point>();
		for (Point x : lst_delivery_12)
			L1.add(x);
		for (Point x : lst_delivery_22)
			L1.add(x);
		Point[] seq1 = getBestSequenceGreedy(start12, L1, DIXAVEGAN);

		Point start21 = lst_pickup_21.get(lst_pickup_21.size() - 1);
		ArrayList<Point> L2 = new ArrayList<Point>();
		for (Point x : lst_delivery_11)
			L2.add(x);
		for (Point x : lst_delivery_21)
			L2.add(x);
		Point[] seq2 = getBestSequenceGreedy(start21, L2, DIXAVEGAN);

		// re-insert seq into route

		Point start_p = restore_start12;
		if ((vt11.contains(restore_start12)))
			start_p = restore_start11;

		for (int i = seq1.length - 1; i >= 0; i--) {
			Point delivery = seq1[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() +
			// "::evaluateExchangeTrip22Vehicles, add-optimal seq1, start12 = "
			// + start12.ID + ": delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID + ", start_p = " +
			// start_p.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		propagate(XR, k1);

		start_p = restore_start21;
		if (vt22.contains(restore_start21))
			start_p = restore_start22;

		for (int i = seq2.length - 1; i >= 0; i--) {
			Point delivery = seq2[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() +
			// "::evaluateExchangeTrip22Vehicles, add-optimal seq2, start21 = "
			// + start21.ID + ": delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID + ", start_p = " +
			// start_p.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}

		propagate(XR, k2);

		int violations = 0;
		for (Point q = XR.startPoint(k1); q != XR.endPoint(k1); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			}
			if (!checkConflictItemsAtPoint(q)) {
				violations++;
			}
			if (awn.getSumWeights(q) > vh1.getWeight()) {
				violations++;
			}
		}
		for (Point q = XR.startPoint(k2); q != XR.endPoint(k2); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			}
			if (!checkConflictItemsAtPoint(q)) {
				violations++;
			}
			if (awn.getSumWeights(q) > vh2.getWeight()) {
				violations++;
			}
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k1);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));
		q = XR.endPoint(k2);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		if (violations == 0)
			delta = cost.getValue() - value;

		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		// recover
		if (vt11Beforevt12) {
			for (int j = 0; j < lst_pickup_11.size(); j++) {
				Point p = lst_pickup_11.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				// System.out.println(name() +
				// "::evaluateExchangeTrip22Vehicles, restore vh1, vt11, p = " +
				// p.ID + ", restore_start11 = " + restore_start11);
				performAddOnePoint(XR, p, restore_start11);
				// performAddOnePoint(XR, d1,p1);
				restore_start11 = p;
			}
			for (int j = 0; j < lst_delivery_11.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_11.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				// System.out.println(name() +
				// "::evaluateExchangeTrip22Vehicles, restore vh1, vt11, d = " +
				// d.ID + ", restore_start11 = " + restore_start11);
				performAddOnePoint(XR, d, restore_start11);
				restore_start11 = d;
			}
			for (int j = 0; j < lst_pickup_12.size(); j++) {
				Point p = lst_pickup_12.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start12);
				// performAddOnePoint(XR, d1,p1);
				restore_start12 = p;
			}
			for (int j = 0; j < lst_delivery_12.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_12.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start12);
				restore_start12 = d;
			}
		} else {
			for (int j = 0; j < lst_pickup_12.size(); j++) {
				Point p = lst_pickup_12.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start12);
				// performAddOnePoint(XR, d1,p1);
				restore_start12 = p;
			}
			for (int j = 0; j < lst_delivery_12.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_12.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start12);
				restore_start12 = d;
			}
			for (int j = 0; j < lst_pickup_11.size(); j++) {
				Point p = lst_pickup_11.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start11);
				// performAddOnePoint(XR, d1,p1);
				restore_start11 = p;
			}
			for (int j = 0; j < lst_delivery_11.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_11.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start11);
				restore_start11 = d;
			}

		}

		if (vt21Beforevt22) {
			for (int j = 0; j < lst_pickup_21.size(); j++) {
				Point p = lst_pickup_21.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start21);
				// performAddOnePoint(XR, d1,p1);
				restore_start21 = p;
			}
			for (int j = 0; j < lst_delivery_21.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_21.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start21);
				restore_start21 = d;
			}
			for (int j = 0; j < lst_pickup_22.size(); j++) {
				Point p = lst_pickup_22.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start22);
				// performAddOnePoint(XR, d1,p1);
				restore_start22 = p;
			}
			for (int j = 0; j < lst_delivery_22.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_22.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start22);
				restore_start22 = d;
			}
		} else {
			for (int j = 0; j < lst_pickup_22.size(); j++) {
				Point p = lst_pickup_22.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start22);
				// performAddOnePoint(XR, d1,p1);
				restore_start22 = p;
			}
			for (int j = 0; j < lst_delivery_22.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_22.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start22);
				restore_start22 = d;
			}
			for (int j = 0; j < lst_pickup_21.size(); j++) {
				Point p = lst_pickup_21.get(j);
				// Point d1 = lst_delivery_1.get(j);
				performRemoveOnePoint(XR, p);
				performAddOnePoint(XR, p, restore_start21);
				// performAddOnePoint(XR, d1,p1);
				restore_start21 = p;
			}
			for (int j = 0; j < lst_delivery_21.size(); j++) {
				// Point p1 = lst_pickup_1.get(j);
				Point d = lst_delivery_21.get(j);
				// performAddOnePoint(XR, p1,p);
				performRemoveOnePoint(XR, d);
				performAddOnePoint(XR, d, restore_start21);
				restore_start21 = d;
			}

		}
		return delta;
	}

	public void performExchangeTrip22Vehicles(VarRoutesVR XR, VehicleTrip vt11,
			VehicleTrip vt21, VehicleTrip vt22, VehicleTrip vt12,
			boolean DIXAVEGAN, boolean loadConstraint) {
		// vt11 of vehicle1 is merged to vt21 of vehicle2 and vt22 of vehicle2
		// is merged to vt12 of vehicle1

		// System.out.println(name() +
		// "::performExchangeTrip22Vehicles, vt11 = "
		// + vt11.seqPointString() + ", vt21 = " + vt21.seqPointString()
		// + ", vt22 = " + vt22.seqPointString() + ", vt12 = "
		// + vt12.seqPointString());

		boolean preCondition = vt11.vehicle == vt12.vehicle
				&& vt21.vehicle == vt22.vehicle && vt11.vehicle != vt22.vehicle;
		if (!preCondition)
			return;
		Vehicle vh1 = vt11.vehicle;
		Vehicle vh2 = vt22.vehicle;

		ArrayList<Point> lst_pickup_11 = vt11.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_11 = vt11.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_12 = vt12.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_12 = vt12.getDeliverySeqPoints();

		ArrayList<Point> lst_pickup_21 = vt21.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_21 = vt21.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_22 = vt22.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_22 = vt22.getDeliverySeqPoints();

		Point restore_start11 = XR.prev(lst_pickup_11.get(0));// store the point
		// for
		// recover
		Point restore_start22 = XR.prev(lst_pickup_22.get(0));

		Point restore_start12 = XR.prev(lst_pickup_12.get(0));// store the point
		// for
		// recover
		Point restore_start21 = XR.prev(lst_pickup_21.get(0));

		if (lst_pickup_21.size() == 0)
			return;
		if (lst_pickup_12.size() == 0)
			return;
		Point start1 = XR.prev(lst_pickup_11.get(0));// store the point for
														// recover
		Point start2 = XR.prev(lst_pickup_22.get(0));

		int k1 = XR.route(start1);
		int k2 = XR.route(start2);// index of route containing points
									// of vt2

		// remove trip vt11 and vt22
		for (Point x : vt11.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt22.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt12.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt21.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		Point start12 = lst_pickup_12.get(lst_pickup_12.size() - 1);
		ArrayList<Point> L1 = new ArrayList<Point>();
		for (Point x : lst_delivery_12)
			L1.add(x);
		for (Point x : lst_delivery_22)
			L1.add(x);
		Point[] seq1 = getBestSequenceGreedy(start12, L1, DIXAVEGAN);

		Point start21 = lst_pickup_21.get(lst_pickup_21.size() - 1);
		ArrayList<Point> L2 = new ArrayList<Point>();
		for (Point x : lst_delivery_11)
			L2.add(x);
		for (Point x : lst_delivery_21)
			L2.add(x);
		Point[] seq2 = getBestSequenceGreedy(start21, L2, DIXAVEGAN);

		// re-insert seq into route
		Point start_p = restore_start12;
		if (vt11.contains(restore_start12))
			start_p = restore_start11;
		for (int i = seq1.length - 1; i >= 0; i--) {
			Point delivery = seq1[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() +
			// "::performExchangeTrip22Vehicles, add seq1: restore_start12 = " +
			// restore_start12.ID + ", start_p = " + start_p.ID
			// + ", pickup = " + pickup.ID + ", delivery = " + delivery.ID);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		propagate(XR, k1);

		start_p = restore_start21;
		if (vt22.contains(restore_start21))
			start_p = restore_start22;
		for (int i = seq2.length - 1; i >= 0; i--) {
			Point delivery = seq2[i];
			Point pickup = getPickupOfDelivery(delivery);
			// System.out.println(name() +
			// "::performExchangeTrip22Vehicles, add seq2: restore_start21 = " +
			// restore_start12.ID + ", start_p = " + start_p.ID
			// + ", pickup = " + pickup.ID + ", delivery = " + delivery.ID);

			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}

		propagate(XR, k2);

	}

	public void performMoveTripNewVehicle(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, Point startPointNewRoute, boolean DIXAVEGAN) {
		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		if (lst_pickup_1.size() == 0)
			return;
		if (lst_pickup_2.size() == 0)
			return;
		// int n = lst_pickup_1.size();
		// Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
		// recover
		// Point start2 = XR.prev(lst_pickup_2.get(0));

		// Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		Point s1 = lst_pickup_1.get(0);
		Point s2 = lst_pickup_2.get(0);
		int k1 = XR.route(s1);
		int k2 = XR.route(s2);
		
		int k = XR.route(startPointNewRoute);// index of route containing points
												// of vt2
		System.out.println(name() + "::performMoveTripNewRoute, vt1 = "
				+ vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		Point start = startPointNewRoute;

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPointNewRoute;
		// if (vt1.contains(start_p))
		// start_p = start1;// vt1 is before vt2 on the same route[k]

		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		propagate(XR, k);
		propagate(XR, k1);
		propagate(XR, k2);
	}

	public void performMoveTrip(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2, boolean DIXAVEGAN) {
		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		if (lst_pickup_1.size() == 0)
			return;
		if (lst_pickup_2.size() == 0)
			return;
		// int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		Point start2 = XR.prev(lst_pickup_2.get(0));
		
		Point s1 = lst_pickup_1.get(0);
		int k1 = XR.route(s1);
		
		Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		int k = XR.route(np);// index of route containing points of vt2
		System.out.println(name() + "::performMoveTrip, vt1 = "
				+ vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}
		for (Point x : vt2.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		Point start = np;

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		for (Point x : lst_delivery_2)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = start2;
		if (vt1.contains(start_p))
			start_p = start1;// vt1 is before vt2 on the same route[k]

		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		propagate(XR, k);
		propagate(XR, k1);

	}

	public void log(VehicleTripCollection VTC, VarRoutesVR XR) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			String msg = "";
			for (VehicleTrip t : VTC.mVehicle2Trips.get(vh)) {
				msg += t.seqPointString() + "; ";
			}
			log(name() + "::log vehicle " + vh.getCode() + ", nbTrips = "
					+ VTC.mVehicle2Trips.get(vh).size() + ", msg = " + msg
					+ ", route[" + k + "] = " + XR.toStringRoute(k));

		}
	}

	public HashSet<Vehicle> getUnusedInternalVehicles(VarRoutesVR XR) {
		HashSet<Vehicle> S = new HashSet<Vehicle>();
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			// log(name() + "::getUnusedInternalVehicles, vehicle " +
			// vh.getCode() + ", route[" + k + "] = " + XR.toStringRoute(k));
			if (isInternalVehicle(vh)) {
				if (XR.emptyRoute(k)) {
					S.add(vh);
					// log(name() +
					// "::getUnusedInternalVehicles, S.add vehicle " +
					// vh.getCode());
				}
			}
		}
		return S;
	}

	public void performMoveTrip(VarRoutesVR XR, VehicleTrip vt1, int k,
			boolean DIXAVEGAN) {
		// move trip vt1 to XR.route(k)

		Point startPoint = XR.startPoint(k);

		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();

		if (lst_pickup_1.size() == 0)
			return;
		// int n = lst_pickup_1.size();
		Point start1 = XR.prev(lst_pickup_1.get(0));// store the point for
													// recover
		System.out.println(name() + "::performMoveTrip, vt1 = "
				+ vt1.seqPointString() + ", k = " + k);
		// remove trip vt1 and vt2
		for (Point x : vt1.seqPoints) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		Point start = startPoint;

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery_1)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(start, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		for (int i = seq.length - 1; i >= 0; i--) {
			Point delivery = seq[i];
			Point pickup = getPickupOfDelivery(delivery);
			mgr.performAddOnePoint(pickup, start_p);
			mgr.performAddOnePoint(delivery, pickup);
			start_p = pickup;
		}
		propagate(XR, k);

	}

	public void performMoveTrip(VarRoutesVR XR, Point pickup, Point delivery,
			Point s, boolean DIXAVEGAN) {
		// move trip (pickup,delivery) to XR.route(k)

		// check if (pickup,delivery) can be moved to route XR.route(k) w.r.t
		// exclusiveVehicleLocations
		int k = XR.route(s);

		Point startPoint = XR.startPoint(k);
		Vehicle vh = mPoint2Vehicle.get(startPoint);
		ArrayList<Point> lst_pickup = new ArrayList<Point>();// vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery = new ArrayList<Point>();// vt1.getDeliverySeqPoints();
		ArrayList<Point> ss = new ArrayList<Point>();
		ss.add(pickup);
		ss.add(delivery);

		lst_pickup.add(pickup);
		lst_delivery.add(delivery);

		Point start1 = XR.prev(lst_pickup.get(0));// store the point for
													// recover
		// remove (pickup,delivery)
		for (Point x : ss) {
			mgr.performRemoveOnePoint(x);
		}

		// re-insert into vt2 in an optimal way

		ArrayList<Point> L = new ArrayList<Point>();
		for (Point x : lst_delivery)
			L.add(x);
		Point[] seq = getBestSequenceGreedy(startPoint, L, DIXAVEGAN);
		// re-insert seq into route

		// start = XR.prev(vt2.seqPoints.get(0));
		Point start_p = startPoint;

		// System.out.println(name() + "::evaluateMoveTrip, k = " + k +
		// ", start_p = " + start_p.ID);
		for (int i = seq.length - 1; i >= 0; i--) {
			Point d = seq[i];
			Point p = getPickupOfDelivery(d);
			// System.out.println(name() + "::evaluateMoveTrip, delivery = " +
			// delivery.ID + ", pickup = " + pickup.ID);
			mgr.performAddOnePoint(p, start_p);
			mgr.performAddOnePoint(d, p);
			start_p = p;
		}
		propagate(XR, k);
		System.out.println(name() + "::performMoveTrip, new cost = "
				+ cost.getValue());
	}

	public double evaluateMoveTrip(VarRoutesVR XR, VehicleTrip vt1,
			VehicleTrip vt2) {
		ArrayList<Point> lst_pickup_1 = vt1.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_1 = vt1.getDeliverySeqPoints();
		ArrayList<Point> lst_pickup_2 = vt2.getPickupSeqPoints();
		ArrayList<Point> lst_delivery_2 = vt2.getDeliverySeqPoints();

		double value = cost.getValue();
		// System.out.println(name() + "::evaluateMoveTrip, vt1 = " +
		// vt1.seqPointString() + ", vt2 = " + vt2.seqPointString());
		// System.out.println(name() + "::evaluateMoveTrip, value = " + value);
		// return the differentiation of total distance if trip vt1 is remove
		// and re-insert into trip vt2
		// satisfying constraint
		double delta = Integer.MAX_VALUE;
		if (lst_pickup_1.size() == 0)
			return delta;
		int n = lst_pickup_1.size();
		Point p = XR.prev(lst_pickup_1.get(0));// store the point for recover

		// remove trip vt1
		for (int i = 0; i < n; i++) {
			performRemoveOnePoint(XR, lst_pickup_1.get(i));
			performRemoveOnePoint(XR, lst_delivery_1.get(n - 1 - i));
		}
		// System.out.println(name() +
		// "::evaluateMoveTrip, after remove trip, value = " + cost.getValue());

		Point np = lst_pickup_2.get(lst_pickup_2.size() - 1);
		// re-insert into vt2

		for (int i = 0; i < n; i++) {
			Point pickup = lst_pickup_1.get(i);
			Point delivery = lst_delivery_1.get(n - 1 - i);
			int violations = evaluateViolationsAddTwoPoints(XR, pickup, np,
					delivery, np);
			// System.out.println(name() + "::evaluateMoveTrip, violations(" +
			// pickup.ID + "," + np.ID + "," +
			// delivery.ID + "," + np.ID + ") = " + violations);

			if (violations > 0) {// recover and return

				// recover
				for (int j = 0; j < lst_pickup_1.size(); j++) {
					Point p1 = lst_pickup_1.get(j);
					// Point d1 = lst_delivery_1.get(j);
					if (XR.contains(p1))
						performRemoveOnePoint(XR, p1);
					performAddOnePoint(XR, p1, p);
					// performAddOnePoint(XR, d1,p1);
					p = p1;
				}
				for (int j = 0; j < lst_pickup_1.size(); j++) {
					// Point p1 = lst_pickup_1.get(j);
					Point d1 = lst_delivery_1.get(j);
					// performAddOnePoint(XR, p1,p);
					if (XR.contains(d1))
						performRemoveOnePoint(XR, d1);
					performAddOnePoint(XR, d1, p);
					p = d1;
				}
				return delta;
			}

			performAddOnePoint(XR, pickup, np);
			// System.out.println(name() +
			// "::evaluateMoveTrip, violations = 0, performAddOnePoint(" +
			// pickup.ID + "," + np.ID + ")");
			performAddOnePoint(XR, delivery, pickup);
			// System.out.println(name() +
			// "::evaluateMoveTrip, violations = 0, performAddOnePoint(" +
			// delivery.ID + "," + pickup.ID + ")");
			np = pickup;
		}
		delta = cost.getValue() - value;
		// System.out.println(name() + "::evaluateMoveTrip, new cost = " +
		// cost.getValue() + ", delta = " + delta);

		// recover
		for (int j = 0; j < lst_pickup_1.size(); j++) {
			Point p1 = lst_pickup_1.get(j);
			// Point d1 = lst_delivery_1.get(j);
			performRemoveOnePoint(XR, p1);
			performAddOnePoint(XR, p1, p);
			// performAddOnePoint(XR, d1,p1);
			p = p1;
		}
		for (int j = 0; j < lst_pickup_1.size(); j++) {
			// Point p1 = lst_pickup_1.get(j);
			Point d1 = lst_delivery_1.get(j);
			// performAddOnePoint(XR, p1,p);
			performRemoveOnePoint(XR, d1);
			performAddOnePoint(XR, d1, p);
			p = d1;
		}

		return delta;
	}

	public String toStringShort(VarRoutesVR XR) {
		String s = "";
		for (int k = 1; k <= XR.getNbRoutes(); k++)
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				s += "route[" + k + "] = ";
				Point x = XR.getStartingPointOfRoute(k);
				while (x != XR.getTerminatingPointOfRoute(k)) {
					s = s + x.getID() + "[" + mPoint2Type.get(x) + "]" + " -> ";
					x = XR.next(x);
				}
				s = s + x.getID() + "\n";
			}
		return s;
	}

	public VehicleTripCollection analyzeTripsWithoutMappingTrip2VarRoute(
			VarRoutesVR XR) {
		ArrayList<VehicleTrip> trips = new ArrayList<VehicleTrip>();
		HashMap<VehicleTrip, Integer> mTrip2Route = new HashMap<VehicleTrip, Integer>();
		// mTrip2VarRoute = new HashMap<VehicleTrip, VarRoutesVR>();

		// System.out.println(name()
		// + "::analyzeTripsWithoutMappingTrip2VarRoute, XR = "
		// + XR.toStringShort());

		// if (log != null) {
		// log.println(name()
		// +
		// "::analyzeTripsWithoutMappingTrip2VarRoute-----------------------------------------------------------------------------");
		// for (int k = 1; k <= XR.getNbRoutes(); k++) {
		// if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
		// continue;
		// Point s = XR.startPoint(k);
		// Vehicle vh = mPoint2Vehicle.get(s);
		// //log.println("Vehicle[" + k + "] code = " + vh.getCode());
		// for (Point p = s; p != XR.endPoint(k); p = XR.next(p)) {
		// log.println("POINT " + mPoint2LocationCode.get(p)
		// + ", type = " + mPoint2Type.get(p) + ", load = "
		// + mPoint2Demand.get(p));
		// }
		// }
		// }
		/*
		 * int r_index = getRouteIndex("60C-242.61"); int pi =
		 * getPickupPointIndex("60007742"); Point pickup = pickupPoints.get(pi);
		 * Point delivery = deliveryPoints.get(pi); int delta_time =
		 * getTimeViolationsWhenInsert(pickup, delivery, r_index); boolean
		 * okconflictitem = feasibleMoveConflictItems(pickup, delivery,
		 * r_index); boolean okconflictlocation =
		 * feaisbleMoveConflictLocation(pickup, delivery, r_index); boolean
		 * okMove = feasibleMove(pickup, delivery, r_index);
		 * 
		 * System.out.println(name() + "::analyzeTrip, --> TEST delta_time = " +
		 * delta_time + ", move conflict item = " + okconflictitem +
		 * ", conflict location = " + okconflictlocation + ", okmove = " +
		 * okMove);
		 */

		int nbTrips = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			Point p = XR.startPoint(k);

			// if (log != null) {
			Vehicle vh = mPoint2Vehicle.get(p);
			// if (log != null)
			// log.println(name()
			// + "::analyzeTripsWithoutMappingTrip2VarRoute, VEHICLE "
			// + vh.getCode());
			p = XR.next(p);
			Point np = XR.next(p);
			while (p != XR.endPoint(k)) {
				double loadPerTrip = 0;
				double length = 0;
				int nbLocations = 0;
				ArrayList<Point> points = new ArrayList<Point>();
				while (p != XR.endPoint(k)) {
					points.add(p);
					// System.out.println(name() + "::analyzeTrips, XR = "
					// + XR.toStringShort() + ", p = " + p.ID
					// + ", type = " + mPoint2Type.get(p) + ", np = "
					// + np.ID);
					if (mPoint2Type.get(p).equals("D")
							&& (mPoint2Type.get(np).equals("P")
									|| mPoint2Type.get(np).equals("T") || np == XR
									.endPoint(k))) {
						break;
					} else {
						if (mPoint2Type.get(p).equals("P")) {
							loadPerTrip += mPoint2Demand.get(p);
							nbLocations += 1;
						}
						// System.out.println(name() + "::analyzeTrips, p = "
						// + mPoint2LocationCode.get(p) + ", np = "
						// + mPoint2LocationCode.get(np));
						length += getDistance(p, np);// awm.getDistance(p,
														// np);
						p = np;
						np = XR.next(np);
					}
				}
				nbTrips++;
				/*
				 * if(mPoint2DepartureTime.get(p) == null ||
				 * mPoint2ArrivalTime.get(p) == null){ log(name() +
				 * "::analyzeTrips, arrival or departure time = NULL at point "
				 * + p.ID + ", BUG???, vehicle[" + k + "] = " +
				 * getVehicle(k).getCode() + ", XR = " + XR.toStringRoute(k));
				 * System.out.println(name() +
				 * "::analyzeTrips, arrival or departure time = NULL at point "
				 * + p.ID + ", BUG???, vehicle[" + k + "] = " +
				 * getVehicle(k).getCode() + ", XR = " + XR.toStringRoute(k));
				 * continue; }
				 */
				String s_dt = "-";
				if (mPoint2DepartureTime.get(p) != null) {
					long dt = mPoint2DepartureTime.get(p);
					s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);
				}

				VehicleTrip tr = new VehicleTrip(vh, points, nbLocations,
						loadPerTrip, length, this);
				// tr.setSolver(this);

				mTrip2Route.put(tr, k);
				// mTrip2VarRoute.put(tr, XR);
				trips.add(tr);

				// if (log != null)
				// log.println("TRIP " + (nbTrips - 1) + ", vehicle.cap = "
				// + vh.getWeight() + ", loadTrip = " + loadPerTrip
				// + ", nbLocations = " + nbLocations + ", length = "
				// + length + ", departure-time = " + s_dt);

				p = np;
				np = XR.next(np);
			}
			long at = mPoint2ArrivalTime.get(XR.endPoint(k));
			// if (log != null)
			// log.println("Arrival-time to END = "
			// + DateTimeUtils.unixTimeStamp2DateTime(at));
			// if (log != null)
			// log.println("-------------------------");
			// }
		}
		return new VehicleTripCollection(mTrip2Route, trips);
	}

	public int computeNbExternalVehicleRoutes(VarRoutesVR XR){
		int sz = 0;
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.emptyRoute(k)) continue;
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if(isInternalVehicle(vh)) continue;
			sz++;
		}
		return sz;
	}
	public Vehicle[] getSortedFreeInternalVehicles(VarRoutesVR XR){
		ArrayList<Vehicle> L = new ArrayList<Vehicle>();
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.emptyRoute(k)){
				Point s = XR.startPoint(k);
				Vehicle vh = mPoint2Vehicle.get(s);
				if(isInternalVehicle(vh)){
					L.add(vh);
				}
			}
		}
		Vehicle[] sL = new Vehicle[L.size()];
		for(int i = 0; i < sL.length; i++) sL[i] = L.get(i);
		
		for(int i = 0; i < sL.length-1; i++){
			for(int j = i+1; j < sL.length; j++){
				if(sL[i].getWeight() < sL[j].getWeight()){
					Vehicle tmp = sL[i]; sL[i] = sL[j]; sL[j] = tmp;
				}
			}
		}
		return sL;
	}
	public boolean checkTimeConstraint(VarRoutesVR XR) {
		boolean ok = true;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;

			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				/*
				log(name()
						+ "::checkTimeConstraint, vehicle["
						+ k
						+ ", "
						+ getVehicle(k - 1).getCode()
						+ "], time at "
						+ mPoint2LocationCode.get(p)
						+ " arrivaltime = "
						+ mPoint2ArrivalTime.get(p)
						+ " ["
						+ DateTimeUtils
								.unixTimeStamp2DateTime(mPoint2ArrivalTime
										.get(p))
						+ "], latest = "
						+ lastestAllowedArrivalTime.get(p)
						+ "["
						+ DateTimeUtils
								.unixTimeStamp2DateTime(lastestAllowedArrivalTime
										.get(p)) + "]");
				*/
				if (mPoint2ArrivalTime.get(p) > lastestAllowedArrivalTime
						.get(p)) {
					System.out.println(name()
							+ "::checkTimeConstraint, FAILED, arrivalTime("
							+ mPoint2LocationCode.get(p) + ") = "
							+ mPoint2ArrivalTime.get(p) + " >= "
							+ lastestAllowedArrivalTime.get(p));
					log(name() + "::checkTimeConstraint, vehicle[" + k + ", "
							+ getVehicle(k - 1).getCode()
							+ "], FAILED, arrivalTime("
							+ mPoint2LocationCode.get(p) + ") = "
							+ mPoint2ArrivalTime.get(p) + " >= "
							+ lastestAllowedArrivalTime.get(p) + ", XR[" + k
							+ "] = " + printRouteAndTime(XR, k));
					// return false;
					ok = false;
				}
			}
		}
		return ok;
	}
	public void logArrivalDepartureTime(){
		if(!CHECK_AND_LOG) return;
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.emptyRoute(k)) continue;
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			logNotln(name() + "::logArrivalDepartureTime, " + vh.getCode() + ", route[" + k + "]: ");
			for(Point p = s; p != XR.endPoint(k); p = XR.next(p)){
				String lc = mPoint2LocationCode.get(p);
				String t = DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(p));
				logNotln(lc + "[" + t + "], ");
			}
			log("----------------------");
		}
	}
	
	public void logVehicleNotReachedLocations() {
		if (!CHECK_AND_LOG)
			return;
		int nbIntTrucks = computeInternalVehicles();
		int nbExtTrucks = computeExternalVehicles();
		for (int i = 0; i < computeInternalVehicles(); i++) {
			Vehicle vh = vehicles[i];
			HashSet<String> NRL = mVehicle2NotReachedLocations
					.get(vh.getCode());
			if (NRL == null) {
				log(name() + "::logVehicleNotReachedLocations, vehicle "
						+ vh.getCode() + "," + vh.getWeight() + " REACH-ALL");
			} else {
				String lcs = "";
				for (String lc : NRL)
					lcs += lc + ",";
				log(name() + "::logVehicleNotReachedLocations, vehicle "
						+ vh.getCode() + "," + vh.getWeight() + " not-reach "
						+ lcs);
			}
		}
		for (int i = 0; i < nbExtTrucks; i++) {
			Vehicle vh = externalVehicles[i];
			HashSet<String> NRL = mVehicle2NotReachedLocations
					.get(vh.getCode());
			if (NRL == null) {
				log(name() + "::logVehicleNotReachedLocations, vehicle "
						+ vh.getCode() + "," + vh.getWeight() + " REACH-ALL");
			} else {
				String lcs = "";
				for (String lc : NRL)
					lcs += lc + ",";
				log(name() + "::logVehicleNotReachedLocations, vehicle "
						+ vh.getCode() + "," + vh.getWeight() + " not-reach "
						+ lcs);
			}
		}
	}

	public boolean checkAllSolution(VarRoutesVR XR) {
		boolean ok = true;
		// if(true) return true;

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;

			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {

				// check exclusive vehicle-location
				Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));// getVehicle(k
																	// - 1);
				String locationCode = mPoint2LocationCode.get(p);
				if (isVehicleCategory(vh)) {
					if (mVehicleCategory2NotReachedLocations.get(
							vh.getVehicleCategory()).contains(locationCode)) {
						log(name() + "::checkAllSolution, vehicle category "
								+ vh.getVehicleCategory()
								+ ", goes to forbidden point " + p.ID
								+ " at location " + locationCode
								+ " FAILED -> BUG????");
						System.out.println(name()
								+ "::checkAllSolution, vehicle category "
								+ vh.getVehicleCategory()
								+ ", goes to forbidden point " + p.ID
								+ " at location " + locationCode
								+ " FAILED -> BUG????");

						return false;
					}

				} else {
					if (mVehicle2NotReachedLocations.get(vh.getCode())
							.contains(locationCode)) {
						log(name() + "::checkAllSolution, vehicle "
								+ vh.getCode() + ", goes to forbidden point "
								+ p.ID + " at location " + locationCode
								+ " FAILED -> BUG????");
						System.out.println(name()
								+ "::checkAllSolution, vehicle " + vh.getCode()
								+ ", goes to forbidden point " + p.ID
								+ " at location " + locationCode
								+ " FAILED -> BUG????");

						return false;
					}
				}
				if (mPoint2ArrivalTime.get(p) == null) {
					log(name() + "::checkAllSolution, time at "
							+ mPoint2LocationCode.get(p) + " arrivaltime = "
							+ mPoint2ArrivalTime.get(p)
							+ " BUG???? ArrivalTime=NULL at point " + p.ID
							+ ", vehicle = " + getVehicle(k - 1).getCode()
							+ ", XR = " + XR.toStringRoute(k));
					continue;
				}
				/*
				 * log(name() + "::checkAllSolution, time at " +
				 * mPoint2LocationCode.get(p) + " arrivaltime = " +
				 * mPoint2ArrivalTime.get(p) + " [" + DateTimeUtils
				 * .unixTimeStamp2DateTime(mPoint2ArrivalTime .get(p)) +
				 * "], latest = " + lastestAllowedArrivalTime.get(p) + "[" +
				 * DateTimeUtils
				 * .unixTimeStamp2DateTime(lastestAllowedArrivalTime .get(p)) +
				 * "]");
				 */
				if (mPoint2ArrivalTime.get(p) > lastestAllowedArrivalTime
						.get(p)) {
					System.out
							.println(name()
									+ "::checkAllSolution, vehicle["
									+ k
									+ ", code = "
									+ getVehicle(k - 1).getCode()
									+ "], FAILED, arrivalTime("
									+ mPoint2LocationCode.get(p)
									+ ") = "
									+ mPoint2ArrivalTime.get(p)
									+ " ("
									+ DateTimeUtils
											.unixTimeStamp2DateTime(mPoint2ArrivalTime
													.get(p))
									+ ") "
									+ " >= "
									+ lastestAllowedArrivalTime.get(p)
									+ "( "
									+ DateTimeUtils
											.unixTimeStamp2DateTime(lastestAllowedArrivalTime
													.get(p)) + ") ");
					log(name()
							+ "::checkAllSolution, vehicle["
							+ k
							+ ", code = "
							+ getVehicle(k - 1).getCode()
							+ "], FAILED, arrivalTime("
							+ mPoint2LocationCode.get(p)
							+ ") = "
							+ mPoint2ArrivalTime.get(p)
							+ " ("
							+ DateTimeUtils
									.unixTimeStamp2DateTime(mPoint2ArrivalTime
											.get(p))
							+ ") "
							+ " >= "
							+ lastestAllowedArrivalTime.get(p)
							+ "( "
							+ DateTimeUtils
									.unixTimeStamp2DateTime(lastestAllowedArrivalTime
											.get(p)) + ") ");
					// return false;
					ok = false;

				}
				if (!checkConflictItemsAtPoint(p)) {
					ok = false;// return false;
					log(name()
							+ "::checkALlSolution, checkConflictItemsAtPoint("
							+ p.ID + ") FAILED, BUG???, XR = "
							+ printRouteAndItems(XR, k));

				}
				/*
				 * for(Point q = XR.next(p); q != XR.endPoint(k); q =
				 * XR.next(q)){ if(mPoint2Type.get(p).equals("P") &&
				 * mPoint2Type.get(q).equals("P"))
				 * if(!checkExclusiveItemsAtPoints(p,q)){
				 * System.out.println(name() +
				 * "::::checkAllSolution, FAILED checkExclusiveItemsAtPoints(" +
				 * p.ID + "," + q.ID + "), XR = " + XR.toStringRoute(k)); ok =
				 * false;//return false; } }
				 */
			}
		}

		VehicleTripCollection VTC = analyzeTripsWithoutMappingTrip2VarRoute(XR);
		for (VehicleTrip t : VTC.trips) {
			ArrayList<Point> P = t.seqPoints;
			for (int i = 0; i < P.size(); i++)
				if (mPoint2Type.get(P.get(i)).equals("P")) {
					for (int j = i + 1; j < P.size(); j++)
						if (mPoint2Type.get(P.get(j)).equals("P")) {
							if (!checkExclusiveItemsAtPoints(P.get(i), P.get(j))) {
								ok = false;
								log(name()
										+ "::checkALlSolution, CONFLICT point "
										+ P.get(i).ID + " <-> " + P.get(j).ID
										+ ", trip " + t.seqPointString());
							}
						}
				}
		}

		return ok;
		// return true;
	}

	public boolean checkConflictItemsOnTrip(Trip t) {
		ArrayList<ItemAmount> it = t.start.items;
		for (int i = 0; i < it.size() - 1; i++) {
			for (int j = i + 1; j < it.size(); j++) {
				if (itemConflict[it.get(i).itemIndex][it.get(j).itemIndex])
					return false;
			}
		}
		return true;
	}

	public boolean checkExclusiveItemsAtPoints(Point p, Point q) {
		if (mPoint2IndexItems.get(p) != null) {
			for (int i : mPoint2IndexItems.get(p)) {
				if (mPoint2IndexItems.get(q) != null) {
					for (int j : mPoint2IndexItems.get(q)) {
						if (itemConflict[i][j])
							return false;
					}
				} else {
					log(name() + "::checkExclusiveItemsAtPoints, point q = "
							+ q.ID + " has not items");
				}
			}
		} else {
			log(name() + "::checkExclusiveItemsAtPoints, point p = " + p.ID
					+ " has not items");
		}
		return true;
	}

	public boolean checkExclusiveItems(Point fromPoint, Point pickup) {
		boolean ok = true;
		/*
		 * HashSet<String> E = getExclusiveItemsOfPickupPoint(pickup); for
		 * (String I : E) { if (mPoint2LoadedItems.get(fromPoint).contains(I)) {
		 * ok = false; break; } }
		 */
		for (int i : mPoint2IndexLoadedItems.get(fromPoint)) {
			for (int j : mPoint2IndexItems.get(pickup))
				if (itemConflict[i][j])
					return false;
		}
		return ok;
	}

	public Point findFirstPointOfNextTrip(VarRoutesVR XR, int k, Point p) {
		// return the first point (from p) of next trip
		for (Point x = p; x != XR.endPoint(k); x = XR.next(x)) {
			String typeX = mPoint2Type.get(x);
			String typeNX = mPoint2Type.get(XR.next(x));
			if ((typeX.equals("D") || typeX.equals("S"))
					&& (!typeNX.equals("D")))
				return XR.next(x);
		}
		return null;
	}

	public Point findFirstPointOfCurrentTrip(VarRoutesVR XR, int k, Point p) {
		for (Point x = p; x != XR.startPoint(k); x = XR.prev(x)) {
			String typeX = mPoint2Type.get(x);
			String typePX = mPoint2Type.get(XR.prev(x));
			if (typeX.equals("P") && !typePX.equals("P"))
				return x;
		}
		return null;
	}

	public boolean checkExclusiveItemAddPoint2Route(VarRoutesVR XR, int k,
			Point pickup, Point xp, Point delivery, Point xd) {
		// return true (in term of exclusive items) if pickup is added after xp
		// and delivery is added after xd
		if (pickup.ID == 18 && delivery.ID == 19 && xp.ID == 40 && xd.ID == 9) {
			System.out.println(name()
					+ "::checkExclusiveItemAddPoint2Route, pickup = "
					+ pickup.ID + ", delivery = " + delivery.ID + ", p = "
					+ xp.ID + ", d = " + xd.ID);
		}
		if (mPoint2Type.get(xp).equals("D")) {
			if (mPoint2Type.get(XR.next(xp)).equals("D"))
				return false;
			Point y = XR.next(xp);// this point is of type P or T (end-point)
			for (Point z = y; z != XR.endPoint(k); z = XR.next(z)) {
				if (mPoint2Type.get(z).equals("P")) {
					if (!checkExclusiveItemsAtPoints(pickup, z))
						return false;
				} else {
					break;
				}
			}
			Point np = findFirstPointOfNextTrip(XR, k, xp);
			if (XR.isBefore(np, xd) || xd == np)
				return false;
			if (mPoint2Type.get(XR.next(xd)).equals("P"))
				return false;
		} else if (mPoint2Type.get(xp).equals("P")) {
			Point fp = findFirstPointOfCurrentTrip(XR, k, xp);
			for (Point z = fp; z != XR.endPoint(k); z = XR.next(z)) {
				if (mPoint2Type.get(z).equals("P")) {
					if (!checkExclusiveItemsAtPoints(pickup, z))
						return false;
				} else {
					break;
				}
			}
			Point np = findFirstPointOfNextTrip(XR, k, xp);
			if (XR.isBefore(np, xd) || xd == np)
				return false;
			if (mPoint2Type.get(XR.next(xd)).equals("P"))
				return false;
		} else {// xp is the starting point of route XR.startPoint(k)
			if (xp == xd)
				return true;
			for (Point z = XR.next(xp); z != XR.endPoint(k); z = XR.next(z)) {
				if (mPoint2Type.get(z).equals("P")) {
					if (!checkExclusiveItemsAtPoints(pickup, z))
						return false;
				} else {
					break;
				}
			}
			Point np = findFirstPointOfNextTrip(XR, k, xp);
			if (XR.isBefore(np, xd) || xd == np)
				return false;
			// if (mPoint2Type.get(XR.next(xd)).equals("P"))
			// return false;
		}
		return true;
	}

	private boolean checkExclusiveItemForPickupAndDeliveryPoint(Point p,
			Point d, Point pickup) {
		// return true if pickup can be inserted after p and delivery is
		// inserted after d
		for (Point tp = p; tp != XR.next(d); tp = XR.next(tp))
			if (!checkExclusiveItems(tp, pickup))
				return false;
		return true;
	}

	public void propagateArrivalDepartureTime(boolean DEBUG) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			propagateArrivalDepartureTime(k, DEBUG);
		}
	}

	public void propagateArrivalDepartureTime(VarRoutesVR XR, boolean DEBUG) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			propagateArrivalDepartureTime(XR, k, DEBUG);
		}
	}

	public String name() {
		return "PickupDeliverySolver";
	}

	public int adaptTimeSubjectToInteruption(int startTime, LocationConfig config){
		if(config == null) return startTime;
		if(config.getInterupPeriods() == null | config.getInterupPeriods().length == 0) return startTime;
		for(int i = 0;i < config.getInterupPeriods().length; i++){
			DateTimePeriod dtp = config.getInterupPeriods()[i];
			int s = (int)DateTimeUtils.dateTime2Int(dtp.getStart());
			int e = (int)DateTimeUtils.dateTime2Int(dtp.getEnd());
			if(startTime >= s && startTime <= e) startTime = e;
		}
		return startTime;
	}
	public void propagateArrivalDepartureTime(int k, boolean DEBUG) {

		// for(int k = 1; k <= XR.getNbRoutes(); k++){
		Point p = XR.startPoint(k);
		int startTime = earliestAllowedArrivalTime.get(p);// +
															// serviceDuration.get(p);
		mPoint2DepartureTime.put(p, startTime);
		mPoint2ArrivalTime.put(p, startTime);

		// if(log != null){
		// log.println(name() + "::propagateArrivalDepartureTime(" + k +
		// "), FIRST POINT startTime = " +
		// DateTimeUtils.unixTimeStamp2DateTime(startTime) +
		// ", travelTime(p,np) = " + (int) travelTime.getWeight(p, XR.next(p)));
		// }

		startTime = startTime + (int) travelTime.getWeight(p, XR.next(p));
		p = XR.next(p);

		while (p != XR.endPoint(k)) {
			
			
			// Point p = s;
			String locationCode = mPoint2LocationCode.get(p);
			if (log != null && DEBUG) {
				// log.println("start  point " + p.ID + ", locationCode = "
				// + locationCode);
			}
			int fixTime = 0;
			if (mPoint2Type.get(p).equals("D") && mPoint2Request.get(p) != null
					&& mPoint2Request.get(p).size() > 0)
				fixTime = mPoint2Request.get(p).get(0).getFixUnloadTime();
			else if (mPoint2Type.get(p).equals("P")
					&& mPoint2Request.get(p) != null
					&& mPoint2Request.get(p).size() > 0)
				fixTime = mPoint2Request.get(p).get(0).getFixLoadTime();

			Point np = XR.next(p);
			int duration = serviceDuration.get(p);
			while (np != null) {
				if (log != null && DEBUG) {
					// log.println("point np =  " + np.ID + ", locationCode = "
					// + mPoint2LocationCode.get(np));
				}
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				duration += serviceDuration.get(np);
				if (log != null && DEBUG) {
					// log.println("point np =  " + np.ID + ", locationCode = "
					// + mPoint2LocationCode.get(np)
					// + ", KEEP and augment duration = " + duration);
				}

				if (np == XR.endPoint(k))
					break;
				else
					np = XR.next(np);
			}
			duration += fixTime;
			if (log != null && DEBUG) {
				// log.println("ADD FIX TIME " + fixTime + " --> duration = "
				// + duration);
			}
			// if(log != null){
			// log.println(name() + "::propagateArrivalDepartureTime(" + k +
			// "), NEXT POINT startTime = " +
			// DateTimeUtils.unixTimeStamp2DateTime(startTime) + ", fixTime = "
			// + fixTime);
			// }
			

			mPoint2ArrivalTime.put(p, startTime);
			
			String lc = mPoint2LocationCode.get(p);
			LocationConfig config = mLocationCode2Config.get(lc);
			
			if(startTime < earliestAllowedArrivalTime.get(p))
				startTime = earliestAllowedArrivalTime.get(p);
			
			startTime = adaptTimeSubjectToInteruption(startTime, config);
			if(config != null){
				log(name() + "::propagateArrivalDepartureTime, locationCode " + lc + " has Config "
						+ ", startTime = " + DateTimeUtils.unixTimeStamp2DateTime(startTime));
			}else{
				log(name() + "::propagateArrivalDepartureTime, locationCode " + lc + " no Config ");
			}
			
			int departureTime = startTime + duration;
			
			mPoint2DepartureTime.put(p, departureTime);
			if (log != null && DEBUG) {
				// log.println("SET START point p = " + p.ID + ", SET arr = "
				// + mPoint2ArrivalTime.get(p) + ", dep = "
				// + mPoint2DepartureTime.get(p));
			}
			
			// WHILE LOOP to fill infos for points at the same location
			np = XR.next(p);
			while (np != null) {
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				mPoint2ArrivalTime.put(np, startTime);
				
				//if(startTime < earliestAllowedArrivalTime.get(np))
				//	startTime = earliestAllowedArrivalTime.get(np);
				
				mPoint2DepartureTime.put(np, departureTime);
				if (log != null && DEBUG) {
					// log.println("KEEP point " + np.ID + ", SET arr = "
					// + mPoint2ArrivalTime.get(np) + ", dep = "
					// + mPoint2DepartureTime.get(np));
				}

				if (np == XR.endPoint(k))
					break;
				else {
					p = np;
					np = XR.next(np);
				}
			}
			startTime = departureTime + (int) travelTime.getWeight(p, np);
			if (np != XR.endPoint(k)) {
				// startTime = departureTime + (int) travelTime.getWeight(p,
				// np);
				p = np;
			} else {

				break;
			}
		}
		mPoint2ArrivalTime.put(XR.endPoint(k), startTime);
		// }
	}

	public int getTravelTime(Point p, Point q) {
		int ip = mLocationCode2Index.get(mPoint2LocationCode.get(p));
		int iq = mLocationCode2Index.get(mPoint2LocationCode.get(q));
		int tt = (int) a_travelTime[ip][iq];
		return tt;
	}

	public int getTravelTime(String src, String dest) {
		int ip = mLocationCode2Index.get(src);
		int iq = mLocationCode2Index.get(dest);
		int tt = (int) a_travelTime[ip][iq];
		return tt;
	}

	public boolean insertGreedyPickupDelivery(Point pickup, Point delivery,
			ModelRoute MR, int k, Point fromPoint, Point toPoint) {

		double eval_violations = Integer.MAX_VALUE;
		double eval_cost = Integer.MAX_VALUE;
		int eval_newOrderLoaded = Integer.MAX_VALUE;
		Point sel_p = null;
		Point sel_d = null;
		for (Point p = fromPoint; p != toPoint; p = MR.XR.next(p)) {

			if (mPoint2Type.get(p).equals("D") && MR.awn.getSumWeights(p) > 0) {
				// cannot pickup any more if there are still items
				// on the vehicle
				continue;
			}

			for (Point d = p; d != toPoint; d = MR.XR.next(d)) {
				// new trial items will be unloaded after d --> need
				// check exclusive items

				if (mPoint2Type.get(MR.XR.next(d)).equals("P")
						&& MR.awn.getSumWeights(d) > 0) {
					// after delivery (accumulated load > 0), there
					// is a pickup --> IGNORE
					continue;
				}

				double ec = evaluateTimeViolationsAddTwoPoints(MR.XR, k,
						pickup, p, delivery, d);

				double ef = MR.cost
						.evaluateAddTwoPoints(pickup, p, delivery, d);

				int e_o = evaluateNewOrderLoad(k, pickup, p, delivery, d);

				if (ec > 0)
					continue;// ensure constraint always satisfied

				if (better(ec, e_o, ef, eval_violations, eval_newOrderLoaded,
						eval_cost)) {
					eval_violations = ec;
					eval_cost = ef;
					eval_newOrderLoaded = e_o;
					sel_p = p;
					sel_d = d;
				}
			}
		}
		if (sel_p != null) {
			/*
			 * MR.mgr.performAddOnePoint(delivery, sel_d);
			 * MR.mgr.performAddOnePoint(pickup, sel_p);
			 * propagateArrivalDepartureTime(MR.XR, k, false);
			 */
			performAddOnePoint(MR.XR, delivery, sel_d);
			performAddOnePoint(MR.XR, pickup, sel_p);

			return true;
		} else
			return false;
	}

	public double computeDistance(VarRoutesVR XR, int k) {
		double d = 0;
		for (Point s = XR.startPoint(k); s != XR.endPoint(k); s = XR.next(s)) {
			d = d + awm.getDistance(s, XR.next(s));
		}
		return d;
	}

	public void propagate(VarRoutesVR XR, int k) {
		propagateArrivalDepartureTime(XR, k, false);
		propagateLoadedItems(XR, k);
	}

	public boolean performAddOnePoint(VarRoutesVR XR, Point x, Point y) {
		int k = XR.route(y);
		boolean ok = XR.getVRManager().performAddOnePoint(x, y);
		if(!ok) return ok;
		propagateArrivalDepartureTime(XR, k, false);
		propagateLoadedItems(XR, k);
		return ok;
	}

	public boolean performRemoveOnePoint(VarRoutesVR XR, Point x) {
		int k = XR.route(x);
		boolean ok = XR.getVRManager().performRemoveOnePoint(x);
		if(!ok) return ok;
		propagateArrivalDepartureTime(XR, k, false);
		propagateLoadedItems(XR, k);
		return true;
	}

	public boolean checkVehicleTripConsistent(VarRoutesVR XR){
		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;
		HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

		// printTrips(XR);
		VehicleTrip[] t = new VehicleTrip[trips.size()];
		for (int i = 0; i < t.length; i++) {
			t[i] = trips.get(i);
			if(t[i].seqPoints.size() % 2 == 1) return false;
		}
		return true;
	}
	public void propagateArrivalDepartureTime(VarRoutesVR XR, int k,
			boolean DEBUG) {

		// for(int k = 1; k <= XR.getNbRoutes(); k++){
		Point p = XR.startPoint(k);
		Vehicle vh = mPoint2Vehicle.get(p);
		String debugVehicleCode = "";
		int startTime = earliestAllowedArrivalTime.get(p)
				+ serviceDuration.get(p);
		mPoint2DepartureTime.put(p, startTime);
		
		
		
		mPoint2ArrivalTime.put(p, startTime);
		startTime = startTime + getTravelTime(p, XR.next(p));// (int)
																// travelTime.getWeight(p,
																// XR.next(p));
		p = XR.next(p);
		while (p != XR.endPoint(k)) {
			// Point p = s;
			String locationCode = mPoint2LocationCode.get(p);
			
			int fixTime = 0;
			if (mPoint2Type.get(p).equals("D") && mPoint2Request.get(p) != null
					&& mPoint2Request.get(p).size() > 0)
				fixTime = mPoint2Request.get(p).get(0).getFixUnloadTime();
			else if (mPoint2Type.get(p).equals("P")
					&& mPoint2Request.get(p) != null
					&& mPoint2Request.get(p).size() > 0)
				fixTime = mPoint2Request.get(p).get(0).getFixLoadTime();

			Point np = XR.next(p);
			int duration = serviceDuration.get(p);
			while (np != null) {
				
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				duration += serviceDuration.get(np);
				if (log != null && DEBUG) {
					// log.println("point np =  " + np.ID + ", locationCode = "
					// + mPoint2LocationCode.get(np)
					// + ", KEEP and augment duration = " + duration);
				}

				if (np == XR.endPoint(k))
					break;
				else
					np = XR.next(np);
			}
			duration += fixTime;
			if (log != null && DEBUG) {
				// log.println("ADD FIX TIME " + fixTime + " --> duration = "
				// + duration);
			}

			//mPoint2ArrivalTime.put(p, startTime);
			
			//if(vh.getCode().equals(debugVehicleCode)){
			//	String lc = mPoint2LocationCode.get(p);
			//	log(name() + "::propagateArrivalDepartureTime, arrivalTime at " + lc + " = " +
			//	DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(p)));
			//}

			String lc = mPoint2LocationCode.get(p);
			LocationConfig config = mLocationCode2Config.get(lc);
			
			if(startTime < earliestAllowedArrivalTime.get(p))
				startTime = earliestAllowedArrivalTime.get(p);
			
			startTime = adaptTimeSubjectToInteruption(startTime, config);
			//if(config != null){
			//	log(name() + "::propagateArrivalDepartureTime, locationCode " + lc + " has Config "
			//			+ ", startTime = " + DateTimeUtils.unixTimeStamp2DateTime(startTime));
			//}else{
			//	log(name() + "::propagateArrivalDepartureTime, locationCode " + lc + " no Config ");
			//}
			
			mPoint2ArrivalTime.put(p, startTime);
			
			int departureTime = startTime + duration;

			
			mPoint2DepartureTime.put(p, departureTime);
			if (log != null && DEBUG) {
				// log.println("SET START point p = " + p.ID + ", SET arr = "
				// + mPoint2ArrivalTime.get(p) + ", dep = "
				// + mPoint2DepartureTime.get(p));
			}

			np = XR.next(p);
			while (np != null) {
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				mPoint2ArrivalTime.put(np, startTime);
				mPoint2DepartureTime.put(np, departureTime);
				if (log != null && DEBUG) {
					// log.println("KEEP point " + np.ID + ", SET arr = "
					// + mPoint2ArrivalTime.get(np) + ", dep = "
					// + mPoint2DepartureTime.get(np));
				}

				if (np == XR.endPoint(k))
					break;
				else {
					p = np;
					np = XR.next(np);
				}
			}
			startTime = departureTime + getTravelTime(p, np);// (int)
																// travelTime.getWeight(p,
																// np);
			if (np != XR.endPoint(k)) {
				// startTime = departureTime + (int) travelTime.getWeight(p,
				// np);
				p = np;
			} else {

				break;
			}
		}
		mPoint2ArrivalTime.put(XR.endPoint(k), startTime);
		// }
	}

	public int evaluateNewOrderLoad(int k, Point pickup, Point p,
			Point delivery, Point d) {
		// return 1 is there is new order loaded when inserting
		// (pickup,delivery) after (p,d)
		// int reqID = mPickupPoint2RequestIndex.get(pickup);
		boolean ok = false;
		if (mPoint2IndexLoadedItems.get(p) != null) {
			for (int i : mPoint2IndexLoadedItems.get(p)) {
				int r = mItemIndex2RequestIndex.get(i);
				ArrayList<PickupDeliveryRequest> R = mPoint2Request.get(pickup);
				if (R != null)
					for (PickupDeliveryRequest req : R) {
						int reqID = mRequest2Index.get(req);
						if (r == reqID) {
							ok = true;
							break;
						}
					}
			}
		}
		if (!ok)
			return 1;// has new order to be loaded into vehicle
		return 0;
	}

	public HashSet<Integer> collectLoadedItemIndexAtPoint(VarRoutesVR XR,
			int k, Point p) {
		HashSet<Integer> S = new HashSet<Integer>();
		for (Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)) {
			if (mPoint2Type.get(x).equals("P")
					&& mPoint2IndexItems.get(x) != null) {
				for (int i : mPoint2IndexItems.get(x)) {
					S.add(i);
				}
				if (x == p)
					return S;
			}
		}
		return new HashSet<Integer>();
	}

	public int evaluateNewOrderLoad(VarRoutesVR XR, int k, Point pickup,
			Point p, Point delivery, Point d) {
		// return 1 is there is new order loaded when inserting
		// (pickup,delivery) after (p,d)
		// int reqID = mPickupPoint2RequestIndex.get(pickup);
		boolean ok = false;
		if (mPoint2IndexLoadedItems.get(p) != null) {
			HashSet<Integer> S = collectLoadedItemIndexAtPoint(XR, k, p);
			// for (int i : mPoint2IndexLoadedItems.get(p)) {
			for (int i : S) {
				if (mItemIndex2RequestIndex.get(i) == null) {
					log(name() + "::evaluateNewOrderLoad, item index " + i
							+ " does not match to any requets --> BUG???");
					return 0;
				}
				int r = mItemIndex2RequestIndex.get(i);
				ArrayList<PickupDeliveryRequest> R = mPoint2Request.get(pickup);
				if (R != null)
					for (PickupDeliveryRequest req : R) {
						int reqID = mRequest2Index.get(req);
						if (r == reqID) {
							ok = true;
							break;
						}
					}
			}
		}
		if (!ok)
			return 1;// has new order to be loaded into vehicle
		return 0;
	}

	public int evaluateTimeViolationsMoveTwoPoints(VarRoutesVR XR, int k,
			Point pickup, Point p, Point delivery, Point d) {
		VRManager mgr = XR.getVRManager();

		int k1 = XR.route(pickup);

		Point oldp = XR.prev(pickup);
		Point oldd = XR.prev(delivery);
		if (XR.next(pickup) == delivery)
			oldd = oldp;

		mgr.performRemoveOnePoint(pickup);
		mgr.performRemoveOnePoint(delivery);
		propagateArrivalDepartureTime(k1, true);

		int delta = evaluateTimeViolationsAddTwoPoints(XR, k, pickup, p,
				delivery, d);

		mgr.performAddOnePoint(delivery, oldd);
		mgr.performAddOnePoint(pickup, oldp);
		propagateArrivalDepartureTime(k1, true);

		return delta;
	}

	public int evaluateTimeViolationsAddTwoPoints(int k, Point pickup, Point p,
			Point delivery, Point d) {
		XR.performAddOnePoint(delivery, d);
		XR.performAddOnePoint(pickup, p);
		propagateArrivalDepartureTime(k, true);
		// if (pickup.ID == 8 && delivery.ID == 9)
		// log(printRouteAndTime(XR, k));

		int violations = 0;
		// for (Point q = XR.next(XR.startPoint(k)); q != XR.endPoint(k); q = XR
		for (Point q = XR.startPoint(k); q != XR.endPoint(k); q = XR.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// CONSIDER END POINT
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		// recovery
		XR.performRemoveOnePoint(delivery);
		XR.performRemoveOnePoint(pickup);
		propagateArrivalDepartureTime(k, false);

		return violations;

	}

	public int evaluateTimeViolationsAddTwoPoints(Point pickup, Point p,
			Point delivery, Point d) {
		int k = XR.route(p);
		return evaluateTimeViolationsAddTwoPoints(k, pickup, p, delivery, d);

		/*
		 * XR.performAddOnePoint(delivery, d); XR.performAddOnePoint(pickup, p);
		 * propagateArrivalDepartureTime(k, true); // if (pickup.ID == 8 &&
		 * delivery.ID == 9) // log(printRouteAndTime(XR, k));
		 * 
		 * int violations = 0; // for (Point q = XR.next(XR.startPoint(k)); q !=
		 * XR.endPoint(k); q = XR for (Point q = XR.startPoint(k); q !=
		 * XR.endPoint(k); q = XR.next(q)) { if (mPoint2ArrivalTime.get(q) >
		 * lastestAllowedArrivalTime.get(q)) violations +=
		 * (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime .get(q)); //
		 * System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" + //
		 * q.ID + ") = " + mPoint2ArrivalTime.get(q) + // "(" + //
		 * DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) + //
		 * ")" + // ", latestAllowArrivalTime(" + q.ID + ") = " + //
		 * lastestAllowedArrivalTime.get(q) + // "(" + //
		 * DateTimeUtils.unixTimeStamp2DateTime
		 * (lastestAllowedArrivalTime.get(q))); }
		 * 
		 * // DO NOT CONSIDER END POINT // Point q = XR.endPoint(k); // if
		 * (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) //
		 * violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
		 * // .get(q));
		 * 
		 * // recovery XR.performRemoveOnePoint(delivery);
		 * XR.performRemoveOnePoint(pickup); propagateArrivalDepartureTime(k,
		 * false);
		 * 
		 * return violations;
		 */
	}

	/*
	 * public boolean checkConflictItemAtPoint(Point p){ Integer[] IDX =
	 * mPoint2IndexItems.get(p); if(IDX == null || IDX.length == 0) return true;
	 * for(int i = 0; i < IDX.length; i++ ){ for(int j = i+1; j < IDX.length;
	 * j++){ if(itemConflict[IDX[i]][IDX[j]]) return false; } } return true; }
	 */
	public int evaluateViolationsAddTwoPoints(VarRoutesVR XR, Point pickup,
			Point p, Point delivery, Point d) {
		int k = XR.route(p);
		// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID + "," +
		// p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k);
		// System.out.println(name() + "::evaluateViolationsAddTwoPoints(" +
		// pickup.ID + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " +
		// k);
		// XR.performAddOnePoint(delivery, d);
		// XR.performAddOnePoint(pickup, p);
		// propagateArrivalDepartureTime(XR, k, true);
		performAddOnePoint(XR, delivery, d);
		// System.out.println(name() +
		// "::evaluateViolationsAddTwoPoints, after AddPoint(" + delivery.ID +
		// "," + d.ID +
		// "), XR[" + k + "] = " + XR.toStringRoute(k));
		performAddOnePoint(XR, pickup, p);
		// System.out.println(name() +
		// "::evaluateViolationsAddTwoPoints, after AddPoint(" + pickup.ID + ","
		// + p.ID +
		// "), XR[" + k + "] = " + XR.toStringRoute(k));

		int violations = 0;
		for (Point q = XR.next(XR.startPoint(k)); q != XR.endPoint(k); q = XR
				.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q)) {
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", AT point " + q.ID + ", arrT = " +
				// mPoint2ArrivalTime.get(q) + " > latest = " +
				// lastestAllowedArrivalTime.get(q) + ", violations = " +
				// violations );
			}
			// if(pickup.ID == 46 && delivery.ID == 47 && p.ID == 60){
			// log.print(name() +
			// "::evaluateViolationsAddTwoPoints, loaded-items at " + q.ID +
			// "");
			// }
			if (!checkConflictItemsAtPoint(q)) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", conflict item at " + p.ID + ", violations = " +
				// violations);
				violations++;
			}
			if (awn.getSumWeights(q) > cap[k - 1]) {
				// log(name() + "::evaluateViolationsAddTwoPoints(" + pickup.ID
				// + "," + p.ID + "," + delivery.ID + "," + d.ID + ", k = " + k
				// + ", sumWeight(" + p.ID + ") = " + awn.getSumWeights(p) +
				// " > cap = " + cap[k-1] + ", violations = " + violations);
				violations++;
			}

			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}

		// check end_working_time of vehicle
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		// recovery
		// XR.performRemoveOnePoint(delivery);
		// XR.performRemoveOnePoint(pickup);
		// propagateArrivalDepartureTime(XR, k, false);
		performRemoveOnePoint(XR, delivery);
		performRemoveOnePoint(XR, pickup);

		return violations;

	}

	public int evaluateTimeViolationsAddTwoPoints(VarRoutesVR XR, int k,
			Point pickup, Point p, Point delivery, Point d) {
		XR.performAddOnePoint(delivery, d);
		XR.performAddOnePoint(pickup, p);
		propagateArrivalDepartureTime(XR, k, true);

		int violations = 0;
		for (Point q = XR.next(XR.startPoint(k)); q != XR.endPoint(k); q = XR
				.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
				violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
						.get(q));
			// System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" +
			// q.ID + ") = " + mPoint2ArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) +
			// ")" +
			// ", latestAllowArrivalTime(" + q.ID + ") = " +
			// lastestAllowedArrivalTime.get(q) +
			// "(" +
			// DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q) - lastestAllowedArrivalTime
					.get(q));

		// recovery
		XR.performRemoveOnePoint(delivery);
		XR.performRemoveOnePoint(pickup);
		propagateArrivalDepartureTime(XR, k, false);

		return violations;

	}

	public HashSet<Integer> greedyConstructMaintainConstraint() {
		initializeLog();

		HashSet<Integer> cand = new HashSet<Integer>();
		for (int i = 0; i < pickupPoints.size(); i++)
			cand.add(i);

		while (cand.size() > 0) {
			Point sel_pickup = null;
			Point sel_delivery = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_d = null;
			int sel_i = -1;
			int sel_k = -1;

			if (log != null) {
				for (int k = 1; k <= XR.getNbRoutes(); k++) {
					log.println("Route " + k);
					for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
							.next(p)) {
						log.println("point " + p.ID + ", location = "
								+ mPoint2LocationCode.get(p) + ", arr = "
								+ mPoint2ArrivalTime.get(p) + ", dep = "
								+ mPoint2DepartureTime.get(p) + ", duration = "
								+ serviceDuration.get(p));
					}
					log.println("---------------------------------------");
				}
			}
			int nbIntVehicles = computeInternalVehicles();
			for (int i : cand) {
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				// for(int k = 1; k <= XR.getNbRoutes(); k++){
				// try internal vehicles FIRST
				for (int k = 1; k <= nbIntVehicles; k++) {
					String vehicleCode = vehicles[k - 1].getCode();
					String pickupLocation = mPoint2LocationCode.get(pickup);
					String deliveryLocation = mPoint2LocationCode.get(delivery);

					// check points cannot be visited
					// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
					// !mPoint2PossibleVehicles.get(delivery).contains(k))
					// continue;
					if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
							pickupLocation)
							|| mVehicle2NotReachedLocations.get(vehicleCode)
									.contains(deliveryLocation))
						continue;

					for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
							.next(p)) {
						// check exclusive items
						boolean okExclusiveItems = true;

						okExclusiveItems = checkExclusiveItems(p, pickup);

						if (!okExclusiveItems)
							continue;

						for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
							// new trial items will be unloaded after d --> need
							// check exclusive items

							okExclusiveItems = true;
							for (Point tp = XR.startPoint(k); tp != XR.next(d); tp = XR
									.next(tp)) {
								okExclusiveItems = checkExclusiveItems(tp,
										pickup);
								if (!okExclusiveItems)
									break;
							}
							if (!okExclusiveItems)
								continue;

							boolean ok = true;
							for (Point tmp = p; tmp != XR.next(d); tmp = XR
									.next(tmp)) {
								if (nwm.getWeight(pickup)
										+ awn.getSumWeights(tmp) > cap[k - 1]) {
									ok = false;
									break;
								}
							}
							if (!ok)
								continue;

							if (awn.getSumWeights(p) > 0) {
								// cannot pickup any more if there are still
								// items on the vehicle
								continue;
							}

							// double ec = CS.evaluateAddTwoPoints(pickup, p,
							// delivery, d);
							double ec = evaluateTimeViolationsAddTwoPoints(k,
									pickup, p, delivery, d);

							double ef = cost.evaluateAddTwoPoints(pickup, p,
									delivery, d);

							System.out.println("consider i = " + i
									+ ", vehicle k = " + k + ", pickup = "
									+ pickup.ID + ", delivery = " + delivery.ID
									+ ", p = " + p.ID + ", d = " + d.ID
									+ ", ec = " + ec + ", ef = " + ef);

							if (ec > 0)
								continue;// ensure constraint always satisfied

							if (ec < eval_violations) {
								eval_violations = ec;
								eval_cost = ef;
								sel_p = p;
								sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
								sel_i = i;
								sel_k = k;
							} else if (ec == eval_violations && ef < eval_cost) {
								eval_cost = ef;
								sel_p = p;
								sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
								sel_i = i;
								sel_k = k;
							}
						}
					}
				}

				// NO internal vehicles are possible --> TRY external vehicles
				if (sel_p == null) {
					for (int k = nbIntVehicles + 1; k <= M; k++) {
						String vehicleCode = externalVehicles[k - nbIntVehicles
								- 1].getCode();
						String pickupLocation = mPoint2LocationCode.get(pickup);
						String deliveryLocation = mPoint2LocationCode
								.get(delivery);

						// check points cannot be visited
						// if(!mPoint2PossibleVehicles.get(pickup).contains(k)
						// ||
						// !mPoint2PossibleVehicles.get(delivery).contains(k))
						// continue;
						if (mVehicle2NotReachedLocations.get(vehicleCode)
								.contains(pickupLocation)
								|| mVehicle2NotReachedLocations
										.get(vehicleCode).contains(
												deliveryLocation))
							continue;

						for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
								.next(p)) {
							// check exclusive items
							boolean okExclusiveItems = true;

							okExclusiveItems = checkExclusiveItems(p, pickup);

							if (!okExclusiveItems)
								continue;

							for (Point d = p; d != XR.endPoint(k); d = XR
									.next(d)) {

								okExclusiveItems = true;
								for (Point tp = XR.startPoint(k); tp != XR
										.next(d); tp = XR.next(tp)) {
									okExclusiveItems = checkExclusiveItems(tp,
											pickup);
									if (!okExclusiveItems)
										break;
								}
								if (!okExclusiveItems)
									continue;

								boolean ok = true;

								for (Point tmp = p; tmp != XR.next(d); tmp = XR
										.next(tmp)) {
									if (nwm.getWeight(pickup)
											+ awn.getSumWeights(tmp) > cap[k - 1]) {
										ok = false;
										break;
									}
								}
								if (!ok)
									continue;

								// double ec = CS.evaluateAddTwoPoints(pickup,
								// p,
								// delivery, d);
								double ec = evaluateTimeViolationsAddTwoPoints(
										k, pickup, p, delivery, d);
								double ef = cost.evaluateAddTwoPoints(pickup,
										p, delivery, d);
								if (ec > 0)
									continue;
								if (ec < eval_violations) {
									eval_violations = ec;
									eval_cost = ef;
									sel_p = p;
									sel_d = d;
									sel_pickup = pickup;
									sel_delivery = delivery;
									sel_i = i;
									sel_k = k;
								} else if (ec == eval_violations
										&& ef < eval_cost) {
									eval_cost = ef;
									sel_p = p;
									sel_d = d;
									sel_pickup = pickup;
									sel_delivery = delivery;
									sel_i = i;
									sel_k = k;
								}
							}
						}
					}

				}

			}
			if (sel_i != -1) {
				/*
				 * mgr.performAddOnePoint(sel_delivery, sel_d);
				 * mgr.performAddOnePoint(sel_pickup, sel_p);
				 * propagateArrivalDepartureTime(sel_k, true);
				 */
				performAddOnePoint(XR, sel_delivery, sel_d);
				performAddOnePoint(XR, sel_pickup, sel_p);

				// log.println("add delivery " + sel_delivery.ID + " after "+
				// sel_d.ID +
				// " AND pickup " + sel_pickup.ID + " after " + sel_p.ID);

				System.out.println("init addOnePoint(" + sel_pickup.ID + ","
						+ sel_p.ID + "), and (" + sel_delivery.ID + ","
						+ sel_d.ID + ", XR = " + XR.toString() + ", CS = "
						+ CS.violations() + ", cost = " + cost.getValue());
				cand.remove(sel_i);

				/*
				 * // update loaded items for (int I :
				 * mPoint2IndexLoadedItems.get(sel_p)) {
				 * mPoint2IndexLoadedItems.get(sel_pickup).add(I); } for (int I
				 * : mPoint2IndexLoadedItems.get(sel_d)) {
				 * mPoint2IndexLoadedItems.get(sel_delivery).add(I); } for
				 * (Point p = sel_pickup; p != sel_delivery; p = XR.next(p)) {
				 * // mPoint2LoadedItems.get(p).add( //
				 * mPoint2ItemCode.get(sel_pickup)); for (int ite :
				 * mPoint2IndexItems.get(sel_pickup)) {
				 * mPoint2IndexLoadedItems.get(p).add(ite); } }
				 */
			} else {
				System.out.println("Cannot schedule any more, BREAK");
				break;

			}
		}

		if (cand.size() > 0) {
			// return list of un-scheduled items
			System.out.println("number of unscheduled items is " + cand.size());

		}
		finalizeLog();
		return cand;
	}

	public boolean better(double e1, double e2, double e3, double f1,
			double f2, double f3) {
		if (e1 < f1)
			return true;
		if (e1 > f1)
			return false;
		if (e2 < f2)
			return true;
		if (e2 > f2)
			return false;
		if (e3 < f3)
			return true;
		return false;
	}

	public void propagateLoadedItems(VarRoutesVR XR, int k) {
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			if (mPoint2IndexLoadedItems.get(p) == null)
				mPoint2IndexLoadedItems.put(p, new HashSet<Integer>());
			else
				mPoint2IndexLoadedItems.get(p).clear();
		}
		HashSet<Integer> S = new HashSet<Integer>();
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			if (mPoint2Type.get(p).equals("P")) {
				for (int i : mPoint2IndexItems.get(p)) {
					if (S.contains(i)) {
						log(name()
								+ "::propagateLoadedItems, BUG???? duplicate loaded items");
					}
					S.add(i);
				}
				for (int i : S) {
					mPoint2IndexLoadedItems.get(p).add(i);
				}
			} else if (mPoint2Type.get(p).equals("D")) {
				int idx = mDeliveryPoint2DeliveryIndex.get(p);
				Point pickup = pickupPoints.get(idx);
				for (int i : mPoint2IndexItems.get(pickup)) {
					S.remove(i);
				}
				for (int i : S) {
					mPoint2IndexLoadedItems.get(p).add(i);
				}
			}
		}

	}

	public void propagateLoadedItems(VarRoutesVR XR) {
		for (int k = 1; k <= XR.getNbRoutes(); k++)
			propagateLoadedItems(XR, k);
	}

	public String analyzeRequest(int idx) {
		String des = "";
		Point pickup = pickupPoints.get(idx);
		Point delivery = deliveryPoints.get(idx);
		des = "request[" + idx + "] demand = " + mPoint2Demand.get(pickup);
		// analyze if the weight of order exceed capacity of vehicles
		boolean ok = false;
		for (int k = 1; k <= M; k++) {
			if (mPoint2Demand.get(pickup) < cap[k - 1]) {
				ok = true;
				break;
			}
		}
		if (!ok) {
			des = des
					+ "\n Trá»�ng lÆ°á»£ng Ä‘Æ¡n hÃ ng vÆ°á»£t quÃ¡ táº£i trá»�ng cá»§a xe náº·ng nháº¥t";
			return des;
		}

		// analyze if there exists a vehicle that can go to the location of
		// request
		ok = false;
		int nbIntVehicles = computeInternalVehicles();
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > nbIntVehicles)
				vh = externalVehicles[k - nbIntVehicles - 1];
			else
				vh = vehicles[k - 1];
			vehicleCode = vh.getCode();

			String pickupLocation = mPoint2LocationCode.get(pickup);
			String deliveryLocation = mPoint2LocationCode.get(delivery);

			// check points cannot be visited
			// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
			// !mPoint2PossibleVehicles.get(delivery).contains(k)) continue;
			if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
					pickupLocation)
					|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
							deliveryLocation)) {
				// des = des + "\n" + "vehicle " + vehicleCode
				// + " cannot go to pickuplocation " + pickupLocation
				// + " or deliverylocation " + deliveryLocation;
				continue;
			} else {
				ok = true;
				break;
			}
		}
		if (!ok) {
			des = des
					+ "\n KhÃ´ng cÃ³ xe nÃ o cÃ³ thá»ƒ váº­n chuyá»ƒn Ä‘Æ¡n hÃ ng do cáº¥m Ä‘Æ°á»�ng";
			return des;
		}

		// analyze if exclusive items
		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > nbIntVehicles)
				vh = externalVehicles[k - nbIntVehicles - 1];
			else
				vh = vehicles[k - 1];
			vehicleCode = vh.getCode();

			String pickupLocation = mPoint2LocationCode.get(pickup);
			String deliveryLocation = mPoint2LocationCode.get(delivery);

			// check points cannot be visited
			// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
			// !mPoint2PossibleVehicles.get(delivery).contains(k)) continue;
			if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
					pickupLocation)
					|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
							deliveryLocation)) {
				// des = des + "\n" + "vehicle " + vehicleCode
				// + " cannot go to pickuplocation " + pickupLocation
				// + " or deliverylocation " + deliveryLocation;
				continue;
			}

			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
					if (checkExclusiveItemForPickupAndDeliveryPoint(p, d,
							pickup)) {
						ok = true;
						break;
					}
				}
				if (ok)
					break;
			}
		}
		if (!ok) {
			des = des
					+ "\n Ä�Æ¡n hÃ ng khÃ´ng thá»ƒ Ä‘Æ°á»£c váº­n chuyá»ƒn do rÃ ng buá»™c cÃ¡c máº·t hÃ ng khÃ´ng Ä‘i kÃ¨m vá»›i nhau";
			return des;
		}

		// analyze the if there exists a feaisble location w.r.t capacity

		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > nbIntVehicles)
				vh = externalVehicles[k - nbIntVehicles - 1];
			else
				vh = vehicles[k - 1];
			vehicleCode = vh.getCode();

			String pickupLocation = mPoint2LocationCode.get(pickup);
			String deliveryLocation = mPoint2LocationCode.get(delivery);

			// check points cannot be visited
			// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
			// !mPoint2PossibleVehicles.get(delivery).contains(k)) continue;
			if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
					pickupLocation)
					|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
							deliveryLocation)) {
				// des = des + "\n" + "vehicle " + vehicleCode
				// + " cannot go to pickuplocation " + pickupLocation
				// + " or deliverylocation " + deliveryLocation;
				continue;
			}

			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
					if (!checkExclusiveItemForPickupAndDeliveryPoint(p, d,
							pickup))
						continue;

					boolean okok = true;
					Point sel_tmp = null;
					for (Point tmp = p; tmp != XR.next(d); tmp = XR.next(tmp)) {
						if (nwm.getWeight(pickup) + awn.getSumWeights(tmp) > cap[k - 1]) {
							okok = false;
							sel_tmp = tmp;
							break;
						}
					}
					if (!okok) {
						// des = des + "\n vehicle " + vehicleCode
						// + " cannot pickup after point "
						// + mPoint2LocationCode.get(p)
						// + " and delivery after point "
						// + mPoint2LocationCode.get(d)
						// + " because load after point "
						// + mPoint2LocationCode.get(sel_tmp) + " = "
						// + awn.getSumWeights(sel_tmp)
						// + ", weight pickup = " + nwm.getWeight(pickup)
						// + ", capacity[" + vehicleCode + "] = "
						// + cap[k - 1];
						continue;
					} else {
						ok = true;
						break;
					}

				}
				if (ok)
					break;
			}
			if (ok)
				break;
		}
		if (!ok) {
			des = des
					+ "\n KhÃ´ng thá»ƒ xáº¿p thÃªm Ä‘Æ¡n hÃ ng lÃªn xe do vÆ°á»£t quÃ¡ rÃ ng buá»™c vá»� táº£i trá»�ng";
			return des;
		}

		// analyze time constraint
		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > nbIntVehicles)
				vh = externalVehicles[k - nbIntVehicles - 1];
			else
				vh = vehicles[k - 1];
			vehicleCode = vh.getCode();

			String pickupLocation = mPoint2LocationCode.get(pickup);
			String deliveryLocation = mPoint2LocationCode.get(delivery);

			// check points cannot be visited
			// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
			// !mPoint2PossibleVehicles.get(delivery).contains(k)) continue;
			if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
					pickupLocation)
					|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
							deliveryLocation)) {
				// des = des + "\n" + "vehicle " + vehicleCode
				// + " cannot go to pickuplocation " + pickupLocation
				// + " or deliverylocation " + deliveryLocation;
				continue;
			}

			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {

				for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
					if (!checkExclusiveItemForPickupAndDeliveryPoint(p, d,
							pickup))
						continue;

					boolean okok = true;
					Point sel_tmp = null;
					for (Point tmp = p; tmp != XR.next(d); tmp = XR.next(tmp)) {
						if (nwm.getWeight(pickup) + awn.getSumWeights(tmp) > cap[k - 1]) {
							okok = false;
							sel_tmp = tmp;
							break;
						}
					}
					if (!okok) {
						// des = des + "\n vehicle " + vehicleCode
						// + " cannot pickup after point "
						// + mPoint2LocationCode.get(p)
						// + " and delivery after point "
						// + mPoint2LocationCode.get(d)
						// + " because load after point "
						// + mPoint2LocationCode.get(sel_tmp) + " = "
						// + awn.getSumWeights(sel_tmp)
						// + ", weight pickup = " + nwm.getWeight(pickup)
						// + ", capacity[" + vehicleCode + "] = "
						// + cap[k - 1];
						continue;
					}
					double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d);
					double ef = cost.evaluateAddTwoPoints(pickup, p, delivery,
							d);
					if (ec > 0) {
						// des = des
						// + "\n"
						// + " vehicle "
						// + vehicleCode
						// + " cannot insert pickup after "
						// + mPoint2LocationCode.get(p)
						// + " and delivery after "
						// + mPoint2LocationCode.get(d)
						// + " because time violations: "
						// + " arrival time at lastPoint  "
						// + mPoint2LocationCode.get(XR.endPoint(k))
						// + " = "
						// + DateTimeUtils
						// .unixTimeStamp2DateTime((long) eat
						// .getEarliestArrivalTime(XR
						// .endPoint(k)))
						// + " travelTime(current -> pickup) = "
						// + DateTimeUtils.second2HMS((int) travelTime
						// .getDistance(p, pickup))
						// + " travelTime(pickup -> delivery) = "
						// + DateTimeUtils.second2HMS((int) travelTime
						// .getDistance(pickup, delivery));
						continue;
					} else {
						ok = true;
						break;
					}
				}
				if (ok)
					break;
			}
			if (ok)
				break;
		}

		if (!ok) {
			des = des
					+ "\n KhÃ´ng thá»ƒ xáº¿p Ä‘Æ¡n hÃ ng do thá»�i gian lÃ m viá»‡c cá»§a cÃ¡c xe Ä‘Ã£ háº¿t";
			return des;
		}
		return des;
	}

	/*
	 * public void mapData() { mDistance = new HashMap<String, Double>();
	 * mTravelTime = new HashMap<String, Double>(); for (int i = 0; i <
	 * distances.length; i++) { String src = distances[i].getSrcCode(); String
	 * dest = distances[i].getDestCode(); mDistance.put(code(src, dest),
	 * distances[i].getDistance()); // System.out.println(module +
	 * "::mapData, mDistance.put(" + // code(src,dest) + "," +
	 * distances[i].getDistance() + ")"); } for (int i = 0; i <
	 * travelTimes.length; i++) { String src = travelTimes[i].getSrcCode();
	 * String dest = travelTimes[i].getDestCode(); mTravelTime.put(code(src,
	 * dest), travelTimes[i].getDistance()); // System.out.println(module +
	 * "::mapData, mDistance.put(" + // code(src,dest) + "," +
	 * distances[i].getDistance() + ")"); } items = new ArrayList<Item>();
	 * mItemCode2Index = new HashMap<String, Integer>();
	 * 
	 * N = requests.length; M = vehicles.length + externalVehicles.length;
	 * System.out.println(module + "::mapData, requests = " + N +
	 * ", vehicles = " + M);
	 * 
	 * mRequest2PointIndices = new HashMap<Integer, HashSet<Integer>>();
	 * mItemIndex2RequestIndex = new HashMap<Integer, Integer>();
	 * 
	 * for (int i = 0; i < requests.length; i++) { for (int j = 0; j <
	 * requests[i].getItems().length; j++) { Item I = requests[i].getItems()[j];
	 * I.setOrderId(requests[i].getOrderID()); int idx = items.size();// index
	 * of item mItemCode2Index.put(I.getCode(), idx); items.add(I);
	 * mItemIndex2RequestIndex.put(idx, i); } }
	 * 
	 * startPoints = new ArrayList<Point>(); endPoints = new ArrayList<Point>();
	 * pickupPoints = new ArrayList<Point>(); deliveryPoints = new
	 * ArrayList<Point>(); allPoints = new ArrayList<Point>(); N = 0; for (int i
	 * = 0; i < requests.length; i++) N += requests[i].getItems().length;
	 * mPoint2Index = new HashMap<Point, Integer>(); mPoint2LocationCode = new
	 * HashMap<Point, String>(); mPoint2Demand = new HashMap<Point, Double>();
	 * mPoint2Vehicle = new HashMap<Point, Vehicle>(); // mPoint2Request = new
	 * HashMap<Point, PickupDeliveryRequest>(); mPoint2Request = new
	 * HashMap<Point, ArrayList<PickupDeliveryRequest>>(); mPoint2Type = new
	 * HashMap<Point, String>(); mPoint2PossibleVehicles = new HashMap<Point,
	 * HashSet<Integer>>(); // mPoint2ItemCode = new HashMap<Point, String>();
	 * mPoint2IndexItems = new HashMap<Point, Integer[]>(); mItem2ExclusiveItems
	 * = new HashMap<String, HashSet<String>>(); mVehicle2NotReachedLocations =
	 * new HashMap<String, HashSet<String>>(); mPoint2IndexLoadedItems = new
	 * HashMap<Point, HashSet<Integer>>();
	 * 
	 * pickup2DeliveryOfGood = new HashMap<Point, Point>();
	 * earliestAllowedArrivalTime = new HashMap<Point, Integer>();
	 * lastestAllowedArrivalTime = new HashMap<Point, Integer>();
	 * serviceDuration = new HashMap<Point, Integer>();
	 * 
	 * mPickupPoint2RequestIndex = new HashMap<Point, Integer>(); int idxPoint =
	 * -1; for (int i = 0; i < requests.length; i++) {
	 * mRequest2PointIndices.put(i, new HashSet<Integer>());
	 * 
	 * if (requests[i].getSplitDelivery() != null &&
	 * requests[i].getSplitDelivery().equals("Y")) { for (int j = 0; j <
	 * requests[i].getItems().length; j++) {
	 * requests[i].getItems()[j].setOrderId(requests[i] .getOrderID());
	 * 
	 * idxPoint++; Point pickup = new Point(idxPoint); pickupPoints.add(pickup);
	 * mRequest2PointIndices.get(i).add(pickupPoints.size() - 1);
	 * mPickupPoint2RequestIndex.put(pickup, i);
	 * 
	 * mPoint2Index.put(pickup, idxPoint); mPoint2LocationCode.put(pickup,
	 * requests[i].getPickupLocationCode()); mPoint2Demand.put(pickup,
	 * requests[i].getItems()[j].getWeight());
	 * 
	 * // mPoint2Request.put(pickup, requests[i]); if
	 * (mPoint2Request.get(pickup) == null) mPoint2Request.put(pickup, new
	 * ArrayList<PickupDeliveryRequest>());
	 * mPoint2Request.get(pickup).add(requests[i]);
	 * 
	 * mPoint2Type.put(pickup, "P"); mPoint2PossibleVehicles.put(pickup, new
	 * HashSet<Integer>()); // mPoint2LoadedItems.put(pickup, new
	 * HashSet<String>()); // mPoint2ItemCode.put(pickup, //
	 * requests[i].getItems()[j].getCode()); Integer[] ite = new Integer[1];
	 * ite[0] = mItemCode2Index.get(requests[i].getItems()[j] .getCode());
	 * 
	 * mPoint2IndexItems.put(pickup, ite);
	 * 
	 * mItem2ExclusiveItems.put( requests[i].getItems()[j].getCode(), new
	 * HashSet<String>());
	 * 
	 * Point delivery = new Point(idxPoint + N); deliveryPoints.add(delivery);
	 * mPoint2Index.put(delivery, idxPoint + N);
	 * mPoint2LocationCode.put(delivery, requests[i].getDeliveryLocationCode());
	 * mPoint2Demand.put(delivery, -requests[i].getItems()[j].getWeight());
	 * 
	 * // mPoint2Request.put(delivery, requests[i]); if
	 * (mPoint2Request.get(delivery) == null) mPoint2Request.put(delivery, new
	 * ArrayList<PickupDeliveryRequest>());
	 * mPoint2Request.get(delivery).add(requests[i]);
	 * 
	 * mPoint2Type.put(delivery, "D"); mPoint2PossibleVehicles.put(delivery, new
	 * HashSet<Integer>()); // mPoint2LoadedItems.put(delivery, new
	 * HashSet<String>());
	 * 
	 * pickup2DeliveryOfGood.put(pickup, delivery); allPoints.add(pickup);
	 * allPoints.add(delivery);
	 * 
	 * earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getEarlyPickupTime())); //
	 * serviceDuration.put(pickup, 1800);// load-unload is 30 // minutes int
	 * pickupDuration = requests[i].getItems()[j] .getPickupDuration(); // if
	 * (pickupDuration < requests[i].getPickupDuration()) // pickupDuration =
	 * requests[i].getPickupDuration(); // pickupDuration +=
	 * requests[i].getFixLoadTime();
	 * 
	 * serviceDuration.put(pickup, pickupDuration);
	 * 
	 * lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getLatePickupTime()));
	 * earliestAllowedArrivalTime.put(delivery, (int)
	 * DateTimeUtils.dateTime2Int(requests[i] .getEarlyDeliveryTime())); //
	 * serviceDuration.put(delivery, 1800);// load-unload is 30 // minutes int
	 * deliveryDuration = requests[i].getItems()[j] .getDeliveryDuration(); //
	 * if (deliveryDuration < requests[i].getDeliveryDuration()) //
	 * deliveryDuration = requests[i].getDeliveryDuration(); deliveryDuration +=
	 * requests[i].getFixUnloadTime();
	 * 
	 * serviceDuration.put(delivery, deliveryDuration);
	 * 
	 * lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getLateDeliveryTime())); } } else { idxPoint++;
	 * Point pickup = new Point(idxPoint); pickupPoints.add(pickup);
	 * 
	 * mRequest2PointIndices.get(i).add(pickupPoints.size() - 1);
	 * mPickupPoint2RequestIndex.put(pickup, i);
	 * 
	 * mPoint2Index.put(pickup, idxPoint); mPoint2LocationCode.put(pickup,
	 * requests[i].getPickupLocationCode()); double demand = 0; int
	 * pickupDuration = 0; int deliveryDuration = 0; for (int j = 0; j <
	 * requests[i].getItems().length; j++) { demand = demand +
	 * requests[i].getItems()[j].getWeight(); deliveryDuration =
	 * deliveryDuration + requests[i].getItems()[j].getDeliveryDuration();
	 * pickupDuration = pickupDuration +
	 * requests[i].getItems()[j].getPickupDuration(); } // if (pickupDuration <
	 * requests[i].getPickupDuration()) // pickupDuration =
	 * requests[i].getPickupDuration(); // pickupDuration +=
	 * requests[i].getFixLoadTime();
	 * 
	 * // if (deliveryDuration < requests[i].getDeliveryDuration()) //
	 * deliveryDuration = requests[i].getDeliveryDuration(); // deliveryDuration
	 * += requests[i].getFixUnloadTime();
	 * 
	 * mPoint2Demand.put(pickup, demand); // mPoint2Request.put(pickup,
	 * requests[i]); if (mPoint2Request.get(pickup) == null)
	 * mPoint2Request.put(pickup, new ArrayList<PickupDeliveryRequest>());
	 * mPoint2Request.get(pickup).add(requests[i]);
	 * 
	 * mPoint2Type.put(pickup, "P"); mPoint2PossibleVehicles.put(pickup, new
	 * HashSet<Integer>()); // mPoint2LoadedItems.put(pickup, new
	 * HashSet<String>());
	 * 
	 * // mPoint2ItemCode.put(pickup, // requests[i].getItems()[j].getCode());
	 * 
	 * Integer[] L = new Integer[requests[i].getItems().length]; for (int ii =
	 * 0; ii < requests[i].getItems().length; ii++) L[ii] =
	 * mItemCode2Index.get(requests[i].getItems()[ii] .getCode());
	 * mPoint2IndexItems.put(pickup, L);
	 * 
	 * for (int j = 0; j < requests[i].getItems().length; j++)
	 * mItem2ExclusiveItems.put( requests[i].getItems()[j].getCode(), new
	 * HashSet<String>());
	 * 
	 * Point delivery = new Point(idxPoint + N); deliveryPoints.add(delivery);
	 * mPoint2Index.put(delivery, idxPoint + N);
	 * mPoint2LocationCode.put(delivery, requests[i].getDeliveryLocationCode());
	 * mPoint2Demand.put(delivery, -demand); // mPoint2Request.put(delivery,
	 * requests[i]); if (mPoint2Request.get(delivery) == null)
	 * mPoint2Request.put(delivery, new ArrayList<PickupDeliveryRequest>());
	 * mPoint2Request.get(delivery).add(requests[i]);
	 * 
	 * mPoint2Type.put(delivery, "D"); mPoint2PossibleVehicles.put(delivery, new
	 * HashSet<Integer>()); // mPoint2LoadedItems.put(delivery, new
	 * HashSet<String>());
	 * 
	 * pickup2DeliveryOfGood.put(pickup, delivery); allPoints.add(pickup);
	 * allPoints.add(delivery);
	 * 
	 * earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getEarlyPickupTime())); //
	 * serviceDuration.put(pickup, 1800);// load-unload is 30 // minutes
	 * 
	 * serviceDuration.put(pickup, pickupDuration);
	 * lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getLatePickupTime()));
	 * earliestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getEarlyDeliveryTime())); //
	 * serviceDuration.put(delivery, 1800);// load-unload is 30 // minutes
	 * serviceDuration.put(delivery, deliveryDuration);
	 * lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
	 * .dateTime2Int(requests[i].getLateDeliveryTime()));
	 * 
	 * } } cap = new double[M]; for (int k = 0; k < M; k++) { Vehicle vh = null;
	 * if (k < vehicles.length) vh = vehicles[k]; else vh = externalVehicles[k -
	 * vehicles.length];
	 * 
	 * cap[k] = vh.getWeight();// vehicles[k].getWeight();
	 * 
	 * Point s = new Point(2 * N + k); Point t = new Point(2 * N + M + k);
	 * mPoint2Index.put(s, s.ID); mPoint2Index.put(t, t.ID); startPoints.add(s);
	 * endPoints.add(t); mPoint2Type.put(s, "S"); mPoint2Type.put(t, "T");
	 * 
	 * mPoint2LocationCode.put(s, vh.getStartLocationCode());//
	 * vehicles[k].getStartLocationCode()); mPoint2LocationCode.put(t,
	 * vh.getEndLocationCode());// vehicles[k].getEndLocationCode());
	 * mPoint2Demand.put(s, 0.0); mPoint2Demand.put(t, 0.0);
	 * mPoint2Vehicle.put(s, vh);// vehicles[k]); mPoint2Vehicle.put(t, vh);//
	 * vehicles[k]); // mVehicle2NotReachedLocations.put(vehicles[k].getCode(),
	 * new // HashSet<String>()); mVehicle2NotReachedLocations.put(vh.getCode(),
	 * new HashSet<String>());
	 * 
	 * allPoints.add(s); allPoints.add(t);
	 * 
	 * // earliestAllowedArrivalTime.put(s, //
	 * (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
	 * earliestAllowedArrivalTime.put(s, (int)
	 * DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
	 * serviceDuration.put(s, 0);// load-unload is 30 minutes //
	 * lastestAllowedArrivalTime.put(s, //
	 * (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime())); if
	 * (vh.getEndWorkingTime() != null) lastestAllowedArrivalTime.put(s, (int)
	 * DateTimeUtils .dateTime2Int(vh.getEndWorkingTime())); //
	 * earliestAllowedArrivalTime.put(t, //
	 * (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
	 * earliestAllowedArrivalTime.put(t, (int)
	 * DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
	 * serviceDuration.put(t, 0);// load-unload is 30 minutes //
	 * lastestAllowedArrivalTime.put(t, //
	 * (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime())); if
	 * (vh.getEndWorkingTime() != null) lastestAllowedArrivalTime.put(t, (int)
	 * DateTimeUtils .dateTime2Int(vh.getEndWorkingTime()));
	 * 
	 * // System.out.println("mapData, startWorkingTime = " + //
	 * vh.getStartWorkingTime() + ", end working time = " + //
	 * vh.getEndWorkingTime()); } for (Point p : allPoints) {
	 * mPoint2IndexLoadedItems.put(p, new HashSet<Integer>()); } itemConflict =
	 * new boolean[items.size()][items.size()]; for (int ii = 0; ii <
	 * items.size(); ii++) for (int jj = 0; jj < items.size(); jj++)
	 * itemConflict[ii][jj] = false;
	 * 
	 * ExclusiveItem[] exclusiveItems = input.getExclusiveItemPairs(); for (int
	 * i = 0; i < exclusiveItems.length; i++) { String I1 =
	 * exclusiveItems[i].getCode1(); String I2 = exclusiveItems[i].getCode2();
	 * if (mItem2ExclusiveItems.get(I1) == null) mItem2ExclusiveItems.put(I1,
	 * new HashSet<String>()); if (mItem2ExclusiveItems.get(I2) == null)
	 * mItem2ExclusiveItems.put(I2, new HashSet<String>());
	 * 
	 * mItem2ExclusiveItems.get(I1).add(I2);
	 * mItem2ExclusiveItems.get(I2).add(I1);
	 * 
	 * if (mItemCode2Index.get(I1) != null && mItemCode2Index.get(I2) != null) {
	 * itemConflict[mItemCode2Index.get(I1)][mItemCode2Index.get(I2)] = true;
	 * itemConflict[mItemCode2Index.get(I2)][mItemCode2Index.get(I1)] = true; }
	 * }
	 * 
	 * ExclusiveVehicleLocation[] exclusiveVehicleLocations = input
	 * .getExclusiveVehicleLocations(); for (int i = 0; i <
	 * exclusiveVehicleLocations.length; i++) { String vehicleCode =
	 * exclusiveVehicleLocations[i].getVehicleCode(); String locationCode =
	 * exclusiveVehicleLocations[i] .getLocationCode();
	 * mVehicle2NotReachedLocations.get(vehicleCode).add(locationCode); }
	 * 
	 * awm = new ArcWeightsManager(allPoints); nwm = new
	 * NodeWeightsManager(allPoints); travelTime = new
	 * ArcWeightsManager(allPoints);
	 * 
	 * for (Point p : allPoints) { String lp = mPoint2LocationCode.get(p); for
	 * (Point q : allPoints) { String lq = mPoint2LocationCode.get(q); double d
	 * = mDistance.get(code(lp, lq)); awm.setWeight(p, q, d); //
	 * travelTime.setWeight(p, q, //
	 * (d*1000)/input.getParams().getAverageSpeed());// meter per // second
	 * double t = mTravelTime.get(code(lp, lq)); travelTime.setWeight(p, q, t);
	 * } } for (Point p : allPoints) { nwm.setWeight(p, mPoint2Demand.get(p));
	 * // System.out.println(module + "::compute, nwm.setWeight(" + p.ID // +
	 * "," + mPoint2Demand.get(p)); }
	 * 
	 * mPoint2ArrivalTime = new HashMap<Point, Integer>(); mPoint2DepartureTime
	 * = new HashMap<Point, Integer>();
	 * 
	 * mgr = new VRManager(); XR = new VarRoutesVR(mgr); for (int k = 0; k <
	 * startPoints.size(); k++) { XR.addRoute(startPoints.get(k),
	 * endPoints.get(k)); } for (Point p : pickupPoints) XR.addClientPoint(p);
	 * for (Point p : deliveryPoints) XR.addClientPoint(p);
	 * 
	 * CS = new ConstraintSystemVR(mgr); awn = new AccumulatedWeightNodesVR(XR,
	 * nwm); awe = new AccumulatedWeightEdgesVR(XR, awm); // eat = new
	 * EarliestArrivalTimeVR(XR, travelTime, // earliestAllowedArrivalTime,
	 * serviceDuration); // ceat = new CEarliestArrivalTimeVR(eat,
	 * lastestAllowedArrivalTime); // CS.post(ceat);
	 * 
	 * cost = new TotalCostVR(XR, awm);
	 * 
	 * F = new LexMultiFunctions(); F.add(new ConstraintViolationsVR(CS));
	 * F.add(cost);
	 * 
	 * mgr.close();
	 * 
	 * if (true) return; }
	 */

	public HashSet<Integer> search() {
		HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraint();
		// HashSet<Integer> remainUnScheduled =
		// greedyConstructMaintainConstraintFTL();

		System.out.println("solution XR = " + XR.toString() + ", cost = "
				+ cost.getValue());
		/*
		 * for (int k = 1; k <= XR.getNbRoutes(); k++) {
		 * System.out.println("Route[" + k + "]"); for (Point p =
		 * XR.startPoint(k); p != XR.endPoint(k); p = XR .next(p)) { Point np =
		 * XR.next(p); double tt = travelTime.getWeight(p, np); long at = (long)
		 * eat.getEarliestArrivalTime(p); System.out.println("point " +
		 * p.getID() + ", demand = " + nwm.getWeight(p) + ", t2n = " + tt +
		 * ", eat = " + DateTimeUtils.unixTimeStamp2DateTime(at)); } //
		 * System.out.println(", load = " + load[k-1].getValue() + // ", cap = "
		 * + cap[k-1]);
		 * System.out.println("------------------------------------"); }
		 */
		return remainUnScheduled;
	}

	public void preprocess(BrennTagPickupDeliveryInput input) {

		mLogicalItem2PhysicalItems = new HashMap<Item, Item[]>();
		PickupDeliveryRequest[] req = new PickupDeliveryRequest[input
				.getRequests().length];
		for (int i = 0; i < req.length; i++)
			req[i] = input.getRequests()[i].clone();
		for (int i = 0; i < req.length; i++) {
			if (req[i].getSplitDelivery().equals("N")) {
				int w = 0;
				int l = 0;
				int h = 0;
				String name = "";
				String code = "";
				int quantity = 0;
				double weight = 0;
				int pickupDuration = 0;
				int deliveryDuration = 0;
				String orderId = req[i].getOrderID();
				String description = "";
				for (int j = 0; j < req[i].getItems().length; j++) {
					w += req[i].getItems()[j].getW();
					l += req[i].getItems()[j].getL();
					h += req[i].getItems()[j].getH();
					name = name + req[i].getItems()[j].getName() + "-";
					code = code + req[i].getItems()[j].getCode() + "-";
					quantity += req[i].getItems()[j].getQuantity();
					weight += req[i].getItems()[j].getWeight();
					pickupDuration += req[i].getItems()[j].getPickupDuration();
					deliveryDuration += req[i].getItems()[j]
							.getDeliveryDuration();
					description += req[i].getItems()[j].getDescription() + "-";
				}
				Item I = new Item(w, l, h, name, code, quantity, weight,
						pickupDuration, deliveryDuration, orderId, description);
				Item[] items = new Item[1];
				items[0] = I;
				mLogicalItem2PhysicalItems.put(I, req[i].getItems());
				req[i].setItems(items);
			} else {
				Item[] items = req[i].getItems();
				for (int j = 0; j < items.length; j++) {
					Item I = items[j];
					Item[] II = new Item[1];
					II[0] = I;
					mLogicalItem2PhysicalItems.put(I, II);
				}
			}
		}
	}

	public Vehicle getVehicle(int idx) {
		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		if (nbIntVehicles + nbExtVehicles == 0)
			return null;
		if (idx < nbIntVehicles)
			return vehicles[idx];
		return externalVehicles[idx - nbIntVehicles];
	}

	public void mapRoute2Vehicles(int[] scheduled_vehicle) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				mPoint2Vehicle.put(XR.startPoint(k),
						getVehicle(scheduled_vehicle[k - 1]));
				mPoint2Vehicle.put(XR.endPoint(k),
						getVehicle(scheduled_vehicle[k - 1]));
			}

			// check exclusive vehicle-location
			Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				String lc = mPoint2LocationCode.get(p);
				if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc)) {
					log(name()
							+ "::mapRoute2Vehicles, EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????, vehicle "
							+ vh.getCode() + " <-> location " + lc);
				}
			}

		}

	}

	public int getRouteIndex(String vehicleCode) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if (vh.getCode().equals(vehicleCode))
				return k;
		}
		return -1;
	}

	public int getPickupPointIndex(Point pickup) {
		for (int i = 0; i < pickupPoints.size(); i++)
			if (pickupPoints.get(i) == pickup)
				return i;
		return -1;
	}

	public int getPickupPointIndex(String deliveryLocationCode) {
		for (int i = 0; i < pickupPoints.size(); i++) {
			Point delivery = deliveryPoints.get(i);
			String lc = mPoint2LocationCode.get(delivery);
			if (lc.equals(deliveryLocationCode))
				return i;
		}
		return -1;
	}

	public int getTimeViolationsWhenInsert(Point pickup, Point delivery, int k) {
		int min_delta = Integer.MAX_VALUE;
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
				int delta = evaluateTimeViolationsMoveTwoPoints(XR, k, pickup,
						p, delivery, d);
				if (delta < min_delta)
					min_delta = delta;
			}
		}
		return min_delta;
	}

	public boolean feasibleMoveConflictItems(Point pickup, Point delivery, int k) {
		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			// check exclusive items
			boolean okExclusiveItems = true;
			okExclusiveItems = checkExclusiveItems(p, pickup);
			if (!okExclusiveItems)
				continue;
			for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
				// new trial items will be unloaded after d --> need
				// check exclusive items

				okExclusiveItems = true;
				for (Point tp = XR.startPoint(k); tp != XR.next(d); tp = XR
						.next(tp)) {
					okExclusiveItems = checkExclusiveItems(tp, pickup);
					if (!okExclusiveItems)
						break;
				}
				if (!okExclusiveItems)
					continue;
				else
					return true;
			}
		}
		return false;
	}

	public boolean feaisbleMoveConflictLocation(Point pickup, Point delivery,
			int k) {
		String vehicleCode = getVehicle(k - 1).getCode();
		String pickupLocation = mPoint2LocationCode.get(pickup);
		String deliveryLocation = mPoint2LocationCode.get(delivery);

		if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
				pickupLocation)
				|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
						deliveryLocation))
			return false;
		else
			return true;
	}

	public String getItemsAtPoint(Point p) {
		String s = "";
		if (mPoint2IndexItems.get(p) != null)
			for (int j = 0; j < mPoint2IndexItems.get(p).length; j++) {
				int idx = mPoint2IndexItems.get(p)[j];
				Item I = items.get(idx);
				s = s + I.getCode() + ",";
			}
		return s;
	}

	public String conflictItemStr(Point p1, Point p2) {
		String s = "";
		if (mPoint2IndexItems.get(p1) != null
				&& mPoint2IndexItems.get(p2) != null) {
			for (int i = 0; i < mPoint2IndexItems.get(p1).length; i++) {
				for (int j = 0; j < mPoint2IndexItems.get(p2).length; j++) {
					Item I1 = items.get(mPoint2IndexItems.get(p1)[i]);
					Item I2 = items.get(mPoint2IndexItems.get(p2)[j]);
					if (itemConflict[mPoint2IndexItems.get(p1)[i]][mPoint2IndexItems
							.get(p2)[j]]) {
						s += "(" + I1.getCode() + "-" + I2.getCode() + ")";
					}
				}
			}
		}
		return s;
	}

	public boolean isInternalVehicle(Vehicle vh) {
		int nbIntVehicles = computeInternalVehicles();
		for (int i = 0; i < nbIntVehicles; i++)
			if (vehicles[i] == vh)
				return true;
		return false;
	}

	// public Vehicle findBestFitVehicle(double load) {
	public Vehicle findBestFitVehicle(VehicleTrip t) {
		double minCap = Integer.MAX_VALUE;
		Vehicle sel_vh = null;
		for (int i = 0; i < input.getVehicleCategories().length; i++) {
			Vehicle vh = input.getVehicleCategories()[i];
			if (!checkPossibleVehicleCategoryLocation(vh, t.seqPoints))
				continue;

			if (vh.getWeight() >= t.load) {
				if (input.getVehicleCategories()[i].getWeight() < minCap) {
					minCap = input.getVehicleCategories()[i].getWeight();
					sel_vh = input.getVehicleCategories()[i];
				}
			}
		}
		int nbIntVehicles = computeInternalVehicles();
		for (int i = 0; i < nbIntVehicles; i++) {
			Vehicle vh = vehicles[i];
			if (!checkPossibleVehicleLocation(vh, t.seqPoints))
				continue;

			if (!usedInternalVehicles.contains(vh)) {
				if (vh.getWeight() >= t.load) {
					if (vh.getWeight() < minCap) {
						minCap = vh.getWeight();
						sel_vh = vh;
					}
				}
			}
		}
		return sel_vh;
	}

	public ArrayList<Vehicle> findPossibleVehicle(Point pickup, Point delivery) {
		double demand = mPoint2Demand.get(pickup);
		String deliveryLocationCode = mPoint2LocationCode.get(delivery);
		ArrayList<Vehicle> L = new ArrayList<Vehicle>();

		int nbExtVehicles = computeExternalVehicles();
		for (int k = 0; k < nbExtVehicles; k++) {
			Vehicle vh = externalVehicles[k];
			if (vh.getWeight() >= demand && vehicleCanGoToPoint(vh, delivery)
					&& vehicleCanGoToPoint(vh, pickup)) {
				L.add(vh);
			}
		}
		return L;
	}

	public boolean checkExclusiveItemsAtAPoint(Point p) {
		for (int i : mPoint2IndexItems.get(p)) {
			for (int j : mPoint2IndexItems.get(p)) {
				if (i != j && itemConflict[i][j])
					return false;
			}
		}
		return true;
	}

	public boolean vehicleCategoryCanDoToLocationCode(Vehicle vh,
			String locationCode) {

		for (int k = 0; k < input.getExclusiveVehicleCategoryLocations().length; k++) {
			String vehicleCode = input.getExclusiveVehicleCategoryLocations()[k]
					.getVehicleCode();
			String lc = input.getExclusiveVehicleCategoryLocations()[k]
					.getLocationCode();
			if (vh.getVehicleCategory().equals(vehicleCode)
					&& locationCode.equals(lc)) {
				return false;
			}
		}
		return true;
	}

	public boolean vehicleCanGoToPoint(Vehicle vh, Point p) {
		if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
				mPoint2LocationCode.get(p)))
			return false;
		return true;
	}

	public boolean vehicleCanGoToPointsTrip(Vehicle vh, VehicleTrip t) {
		for (Point p : t.seqPoints)
			if (!vehicleCanGoToPoint(vh, p))
				return false;
		return true;
	}

	public boolean vehicleCategoryCanGoToPoint(Vehicle vh, Point p) {
		if (mVehicleCategory2NotReachedLocations.get(vh.getVehicleCategory()) == null)
			return true;
		if (mPoint2LocationCode.get(p) == null)
			return true;
		if (mVehicleCategory2NotReachedLocations.get(vh.getVehicleCategory())
				.contains(mPoint2LocationCode.get(p)))
			return false;
		return true;
	}

	public boolean vehicleCategoryCanGoToPointsTrip(Vehicle vh, VehicleTrip t) {
		for (Point p : t.seqPoints)
			if (!vehicleCategoryCanGoToPoint(vh, p))
				return false;
		return true;
	}

	public boolean feasibleMove(Point pickup, Point delivery, int k) {
		// return true if we can remove pickup, delivery from its current
		// locations and re-insert them into route k
		String vehicleCode = getVehicle(k - 1).getCode();
		String pickupLocation = mPoint2LocationCode.get(pickup);
		String deliveryLocation = mPoint2LocationCode.get(delivery);
		String debug_location_code = "";

		// if (pickup.ID == 18 && delivery.ID == 19) {
		// System.out.println(name() + "::feasibleMove, delivery location = "
		// + deliveryLocation + ", pickup = " + pickup.ID
		// + ", delivery = " + delivery.ID + ", XR = "
		// + XR.toStringRoute(k));
		// }
		if (debug_location_code.equals(deliveryLocation)) {
			if (log != null) {
				log.println(name() + "::feasibleMove, delivery location = "
						+ deliveryLocation + ", consider route[" + k + "] = "
						+ XR.toStringRoute(k));
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					if (mPoint2Type.get(p).equals("P"))
						log.println("ON ROUTE, Items at point(" + p.ID + ") = "
								+ getItemsAtPoint(p));
				}
				log.println("Item at new pickup point = "
						+ getItemsAtPoint(pickup));
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String s = conflictItemStr(p, pickup);
					if (s != null && !s.equals("")) {
						log.println("CONFLICT ITEMS at point p = " + p.ID
								+ ": " + s);
					}
				}
			}
		}

		if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
				pickupLocation)
				|| mVehicle2NotReachedLocations.get(vehicleCode).contains(
						deliveryLocation)) {
			if (debug_location_code.equals(deliveryLocation)) {
				if (log != null) {
					log.println(name()
							+ "::feasibleMove, INFEASIBLE because vehicle cannot reach location");
				}
			}
			return false;
		}

		for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)) {
			// check exclusive items
			boolean okExclusiveItems = true;

			okExclusiveItems = checkExclusiveItems(p, pickup);

			if (!okExclusiveItems) {
				if (debug_location_code.equals(deliveryLocation)) {
					if (log != null) {
						log.println(name()
								+ "::feasibleMove, INFEASIBLE insertion p = "
								+ p.ID
								+ " because exclusive items --> CONTINUE");
					}
				}
				continue;
			}
			// check if after deliverying some items, the vehicle is
			// still loaded,
			// then it must be unloaded (until empty) before
			// pickingup new items
			if (mPoint2Type.get(p).equals("D") && awn.getSumWeights(p) > 0) {
				// cannot pickup any more if there are still items
				// on the vehicle
				continue;
			}

			for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
				// new trial items will be unloaded after d --> need
				// check exclusive items
				if (!checkExclusiveItemAddPoint2Route(XR, k, pickup, p,
						delivery, d))
					continue;
				else {
					/*
					 * if (pickup.ID == 18 && delivery.ID == 19) {
					 * System.out.println(name() +
					 * "::feasibleMove, delivery location = " + deliveryLocation
					 * + ", pickup = " + pickup.ID + ", delivery = " +
					 * delivery.ID + ", XR = " + XR.toStringRoute(k) +
					 * ", FOUND p = " + p.ID + ", d= " + d.ID); }
					 */
				}
				okExclusiveItems = true;
				for (Point tp = XR.startPoint(k); tp != XR.next(d); tp = XR
						.next(tp)) {
					okExclusiveItems = checkExclusiveItems(tp, pickup);
					if (!okExclusiveItems)
						break;
				}
				if (!okExclusiveItems) {
					if (debug_location_code.equals(deliveryLocation)) {
						if (log != null) {
							log.println(name()
									+ "::feasibleMove, INFEASIBLE insertion d = "
									+ d.ID
									+ " because exclusive items --> CONTINUE");
						}
					}

					continue;
				}
				if (mPoint2Type.get(XR.next(d)).equals("P")
						&& awn.getSumWeights(d) > 0) {
					// after delivery (accumulated load > 0), there
					// is a pickup --> IGNORE
					continue;
				}

				// double ec = CS.evaluateAddTwoPoints(pickup, p,
				// delivery, d);
				// double ec = evaluateTimeViolationsAddTwoPoints(k, pickup,
				// p,delivery, d);
				double ec = evaluateTimeViolationsMoveTwoPoints(XR, k, pickup,
						p, delivery, d);

				// double ef = cost.evaluateAddTwoPoints(pickup, p, delivery,
				// d);

				// int e_o = evaluateNewOrderLoad(k, pickup, p, delivery, d);

				if (ec > 0) {
					if (debug_location_code.equals(deliveryLocation)) {
						if (log != null) {
							log.println(name()
									+ "::feasibleMove, INFEASIBLE insertion because time violation ec = "
									+ ec + " p = " + p.ID + ", d = " + d.ID
									+ " --> CONTINUE");
						}
					}
					continue;// ensure constraint always satisfied
				} else {
					if (debug_location_code.equals(deliveryLocation)) {
						if (log != null) {
							log.println(name()
									+ "::feasibleMove, RETURN TRUE!!!");
							System.out.println(name()
									+ "::feasibleMove, RETURN TRUE!!!");
						}
					}
					/*
					 * if (pickup.ID == 18 && delivery.ID == 19) { if (pickup.ID
					 * == 18 && delivery.ID == 19) { System.out.println(name() +
					 * "::feasibleMove, delivery location = " + deliveryLocation
					 * + ", pickup = " + pickup.ID + ", delivery = " +
					 * delivery.ID + ", XR = " + XR.toStringRoute(k) +
					 * " FOUND satisfy time p = " + p.ID + ", d = " + d.ID); }
					 * // log(name() + "::feasibleMove, pickup = " + pickup.ID
					 * // + ", delivery = " + delivery.ID + //
					 * " FOUND satisfy time p = " + p.ID + ", d = " + d.ID); }
					 */
					return true;
				}
			}
		}
		if (debug_location_code.equals(deliveryLocation)) {
			if (log != null) {
				log.println(name() + "::feasibleMove, FINAL return FALSE");
			}
		}

		return false;
	}

	public double getDistance(Point p, Point q) {
		String lp = mPoint2LocationCode.get(p);
		String lq = mPoint2LocationCode.get(q);
		int ip = mLocationCode2Index.get(lp);
		int iq = mLocationCode2Index.get(lq);
		return a_distance[ip][iq];
	}

	public double getAccumulatedDistance(VarRoutesVR XR, Point p) {
		double aw = 0;
		int k = XR.route(p);
		for (Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)) {
			aw += getDistance(x, XR.next(x));
			if (XR.next(x) == p)
				break;
		}
		return aw;
	}

	double getAccumulatedLoad(VarRoutesVR XR, Point p) {
		double aw = 0;
		int k = XR.route(p);
		for (Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)) {
			aw += mPoint2Demand.get(x);
			if (x == p)
				break;
		}
		return aw;
	}

	public RoutingSolution[] buildRoutingSolution(ModelRoute MR) {
		VarRoutesVR XR = MR.XR;

		int nbr = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
			}
		}
		// check exclusive vehicle-location
		if (!checkExclusiveVehicleLocation(XR)) {
			log(name()
					+ "::buildSolution(ModelRoute), EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????");
		}
		/*
		 * for (int k = 1; k <= XR.getNbRoutes(); k++) { if
		 * (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
		 * 
		 * // check exclusive vehicle-location Vehicle vh =
		 * mPoint2Vehicle.get(XR.startPoint(k)); for (Point p =
		 * XR.startPoint(k); p != XR.endPoint(k); p = XR .next(p)) { String lc =
		 * mPoint2LocationCode.get(p); if
		 * (mVehicle2NotReachedLocations.get(vh.getCode()) .contains(lc)) {
		 * log(name() +
		 * "::buildSolution(ModelRoute), EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????, vehicle "
		 * + vh.getCode() + " <-> location " + lc); } } } }
		 */

		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				double distance = 0;
				double maxLoad = 0;
				// double totalLoadOfTrips = 0;
				// double loadPerTrip = 0;
				// double tripDistance = 0;
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					// int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if (p == XR.startPoint(k)) {
						// depot
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						long at = mPoint2ArrivalTime.get(p);

						long dt = mPoint2DepartureTime.get(p);// at +
																// serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getLat();
						double d_lng = v.getLng();
						e = new RoutingElement(v.getStartLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-",
								s_at, s_dt);
						e.setDescription("weight: " + v.getWeight());
						double e_load = getAccumulatedLoad(XR, p);// awn.getSumWeights(p);
						double e_distance = getAccumulatedDistance(XR, p);// awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_STARTING_POINT);

					} else if (p == XR.endPoint(k)) {
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getEndLat();
						double d_lng = v.getEndLng();
						e = new RoutingElement(v.getEndLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-");
						double e_load = getAccumulatedLoad(XR, p); // awn.getSumWeights(p);
						double e_distance = getAccumulatedDistance(XR, p); // awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_END_POINT);

					} else {
						// int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p).get(0);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						boolean isEndTrip = false;
						double tripLoad = 0;
						if (mPoint2Type.get(p).equals("P")) {
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
							// totalLoadOfTrips += awn.getWeights(p);
							// Point pp = XR.prev(p);
							// if(Math.abs(awn.getSumWeights(pp)) <= EPS){
							// tripLoad = loadPerTrip;
							// loadPerTrip = 0;
							// isEndTrip = true;
							// tripDistance += awm.getWeight(pp, p);
							// }else{

							// }
							// loadPerTrip += awn.getWeights(p);

						} else {

						}
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						// long dt = at + serviceDuration.get(p);
						long at = mPoint2ArrivalTime.get(p);
						long dt = mPoint2DepartureTime.get(p);

						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						e = new RoutingElement(locationCode, "-", lat + ","
								+ lng, lat, lng, s_at, s_dt);
						// e = new RoutingElement(requests[ir].getOrderID(),
						// "-", lat + "," + lng,lat,lng, s_at, s_dt);
						double al = getAccumulatedLoad(XR, p);
						e.setDescription("orderID: " + r.getOrderID()
								+ ", type = " + mPoint2Type.get(p)
								+ ", amount: " + mPoint2Demand.get(p)
								+ ", accumulate load = " + al);// awn.getSumWeights(p));
						if (maxLoad < al)
							maxLoad = al;
						e.setOrderId(r.getOrderID());
						double e_load = al;// awn.getSumWeights(p);
						double e_distance = getAccumulatedDistance(XR, p);// awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);

						Item[] pitems = null;
						if (mPoint2Type.get(p).equals("P")) {

							Integer[] L = mPoint2IndexItems.get(p);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}
						} else if (mPoint2Type.get(p).equals("D")) {
							int idx = mDeliveryPoint2DeliveryIndex.get(p);
							Point pickup = pickupPoints.get(idx);
							Integer[] L = mPoint2IndexItems.get(pickup);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}

						}
						e.setItems(pitems);
					}
					distance += getDistance(p, XR.next(p));// awm.getWeight(p,
															// XR.next(p));
					lst.add(e);
				}
				// add terminating point of route
				Point p = XR.endPoint(k);
				// long at = (long) eat.getEarliestArrivalTime(p);
				// if (at < earliestAllowedArrivalTime.get(p))
				// at = earliestAllowedArrivalTime.get(p);
				long at = mPoint2ArrivalTime.get(p);

				String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);

				Vehicle v = mPoint2Vehicle.get(p);
				double d_lat = v.getEndLat();
				double d_lng = v.getEndLng();
				RoutingElement e = new RoutingElement(v.getEndLocationCode(),
						"-", d_lat + "," + d_lng, d_lat, d_lng, s_at, "-");
				double e_load = getAccumulatedLoad(XR, p);// awn.getSumWeights(p);
				double e_distance = getAccumulatedDistance(XR, p);// awe.getCostRight(p);
				e.setLoad(e_load);
				e.setDistance(e_distance);
				// e.setTotalTripLoad(loadPerTrip);
				// e.setType(ROUTE_ELEMENT_END_TRIP);
				// e.setTripDistance(awm.getWeight(XR.prev(p), p));

				lst.add(e);
				RoutingElement firstEle = lst.get(0);
				NumberFormat formatter = new DecimalFormat("#0.00");
				String std_d = formatter.format(distance);
				firstEle.setDescription(firstEle.getDescription()
						+ ", length = " + std_d + " (km)" + ", maxLoad = "
						+ maxLoad + " (kg)");

				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for (int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				// routes[nbr] = new RoutingSolution(a_route);

				/*
				 * Vehicle vh = null;
				 * 
				 * if (k <= vehicles.length) vh = vehicles[k - 1]; else vh =
				 * externalVehicles[k - vehicles.length - 1];
				 */
				Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
				routes[nbr] = new RoutingSolution(vh, a_route, 0.0, distance);
			}
		}
		return routes;
	}

	public PickupDeliverySolution buildSolution(VarRoutesVR XR,
			int[] scheduled_vehicle, HashSet<Integer> remainUnScheduled) {
		// scheduled_vehicle[k] is the index of vehicle assigned to route k (k =
		// 0,1,...)

		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;
		HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = new HashMap<Vehicle, ArrayList<VehicleTrip>>();
		for (VehicleTrip trip : trips) {
			if (mVehicle2Trips.get(trip.vehicle) == null)
				mVehicle2Trips.put(trip.vehicle, new ArrayList<VehicleTrip>());
			mVehicle2Trips.get(trip.vehicle).add(trip);

		}

		// mapRoute2Vehicles(scheduled_vehicle);

		int nbr = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				// mPoint2Vehicle.put(XR.startPoint(k),
				// getVehicle(scheduled_vehicle[k-1]));
				// mPoint2Vehicle.put(XR.endPoint(k),
				// getVehicle(scheduled_vehicle[k-1]));
			}
		}

		int nbTrips = 0;
		double totalDistance = 0;
		/*
		 * for (int k = 1; k <= XR.getNbRoutes(); k++) { if
		 * (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
		 * mPoint2Vehicle.put(XR.startPoint(k), getVehicle(scheduled_vehicle[k -
		 * 1])); mPoint2Vehicle.put(XR.endPoint(k),
		 * getVehicle(scheduled_vehicle[k - 1]));
		 * 
		 * // check exclusive vehicle-location Vehicle vh =
		 * mPoint2Vehicle.get(XR.startPoint(k)); for (Point p =
		 * XR.startPoint(k); p != XR.endPoint(k); p = XR .next(p)) { String lc =
		 * mPoint2LocationCode.get(p); if
		 * (mVehicle2NotReachedLocations.get(vh.getCode()) .contains(lc)) {
		 * log(name() +
		 * "::buildSolution, EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????, vehicle "
		 * + vh.getCode() + " <-> location " + lc); } } } }
		 */

		System.out.println(name() + "::buildSolution, number of routes = "
				+ nbr);

		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				Point s = XR.startPoint(k);
				Vehicle vh = mPoint2Vehicle.get(s);
				System.out.println(name() + "::buildSollution, route[" + k
						+ "], vehicle " + vh.getCode() + ", nbTrips = "
						+ mVehicle2Trips.get(vh).size());

				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				double distance = 0;
				double maxLoad = 0;
				// double totalLoadOfTrips = 0;
				// double loadPerTrip = 0;
				// double tripDistance = 0;
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if (p == XR.startPoint(k)) {
						// depot
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						long at = mPoint2ArrivalTime.get(p);

						long dt = mPoint2DepartureTime.get(p);// at +
																// serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getLat();
						double d_lng = v.getLng();
						e = new RoutingElement(v.getStartLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-",
								s_at, s_dt);
						e.setDescription("weight: " + v.getWeight());
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_STARTING_POINT);

					} else if (p == XR.endPoint(k)) {
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getEndLat();
						double d_lng = v.getEndLng();
						e = new RoutingElement(v.getEndLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-");
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_END_POINT);

					} else {
						// int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p).get(0);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						boolean isEndTrip = false;
						double tripLoad = 0;
						if (mPoint2Type.get(p).equals("P")) {
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
							// totalLoadOfTrips += awn.getWeights(p);
							// Point pp = XR.prev(p);
							// if(Math.abs(awn.getSumWeights(pp)) <= EPS){
							// tripLoad = loadPerTrip;
							// loadPerTrip = 0;
							// isEndTrip = true;
							// tripDistance += awm.getWeight(pp, p);
							// }else{

							// }
							// loadPerTrip += awn.getWeights(p);

						} else {

						}
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						// long dt = at + serviceDuration.get(p);
						long at = mPoint2ArrivalTime.get(p);
						long dt = mPoint2DepartureTime.get(p);

						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						e = new RoutingElement(locationCode, "-", lat + ","
								+ lng, lat, lng, s_at, s_dt);
						// e = new RoutingElement(requests[ir].getOrderID(),
						// "-", lat + "," + lng,lat,lng, s_at, s_dt);
						String orderCodes = "";
						for (int jj = 0; jj < mPoint2Request.get(p).size(); jj++) {
							PickupDeliveryRequest rr = mPoint2Request.get(p)
									.get(jj);
							orderCodes += rr.getOrderCode() + ", ";
						}
						String s_accumulate_load = "0";
						if (awn.getSumWeights(p) > Utility.EPS) {
							s_accumulate_load = awn.getSumWeights(p) + "";
						}
						e.setDescription("orderID: " + r.getOrderID()
								+ ", orderCodes = " + orderCodes + ", type = "
								+ mPoint2Type.get(p) + ", amount: "
								+ mPoint2Demand.get(p) + ", accumulate load = "
								+ s_accumulate_load);
						if (maxLoad < awn.getSumWeights(p))
							maxLoad = awn.getSumWeights(p);
						e.setOrderId(r.getOrderID());
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);

						Item[] pitems = null;
						if (mPoint2Type.get(p).equals("P")) {

							Integer[] L = mPoint2IndexItems.get(p);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}
						} else if (mPoint2Type.get(p).equals("D")) {
							int idx = mDeliveryPoint2DeliveryIndex.get(p);
							Point pickup = pickupPoints.get(idx);
							Integer[] L = mPoint2IndexItems.get(pickup);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}

						}
						e.setItems(pitems);
					}
					distance += awm.getWeight(p, XR.next(p));
					lst.add(e);
				}
				// add terminating point of route
				Point p = XR.endPoint(k);
				// long at = (long) eat.getEarliestArrivalTime(p);
				// if (at < earliestAllowedArrivalTime.get(p))
				// at = earliestAllowedArrivalTime.get(p);
				long at = mPoint2ArrivalTime.get(p);

				String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);

				Vehicle v = mPoint2Vehicle.get(p);
				double d_lat = v.getEndLat();
				double d_lng = v.getEndLng();
				RoutingElement e = new RoutingElement(v.getEndLocationCode(),
						"-", d_lat + "," + d_lng, d_lat, d_lng, s_at, "-");

				double e_load = (int) 0;
				if (awn.getSumWeights(p) > Utility.EPS)
					e_load = awn.getSumWeights(p);

				double e_distance = awe.getCostRight(p);
				e.setLoad(e_load);
				e.setDistance(e_distance);
				// e.setTotalTripLoad(loadPerTrip);
				// e.setType(ROUTE_ELEMENT_END_TRIP);
				// e.setTripDistance(awm.getWeight(XR.prev(p), p));

				lst.add(e);
				RoutingElement firstEle = lst.get(0);
				NumberFormat formatter = new DecimalFormat("#0.00");
				String std_d = formatter.format(distance);
				firstEle.setDescription(firstEle.getDescription()
						+ ", length = " + std_d + " (km)" + ", maxLoad = "
						+ maxLoad + " (kg)");

				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for (int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				// routes[nbr] = new RoutingSolution(a_route);

				/*
				 * Vehicle vh = null;
				 * 
				 * if (k <= vehicles.length) vh = vehicles[k - 1]; else vh =
				 * externalVehicles[k - vehicles.length - 1];
				 */
				// Vehicle vh = getVehicle(scheduled_vehicle[k - 1]);
				routes[nbr] = new RoutingSolution(vh, a_route, maxLoad,
						distance);
				totalDistance += distance;
			}
		}
		/*
		 * for (int i = 0; i < requests.length; i++) { PickupDeliveryRequest r =
		 * requests[i]; System.out.println("req[" + i + "], pickup = " +
		 * r.getPickupLat() + "," + r.getPickupLng() + ", delivery = " +
		 * r.getDeliveryLat() + "," + r.getDeliveryLng());
		 * 
		 * }
		 */
		/*
		 * ArrayList<Item> L = new ArrayList<Item>(); for (int i :
		 * remainUnScheduled) { Point pickup = pickupPoints.get(i); String des =
		 * analyzeRequest(i); for (int j = 0; j <
		 * mPoint2IndexItems.get(pickup).length; j++) { Item I =
		 * items.get(mPoint2IndexItems.get(pickup)[j]); I.setDescription(des);
		 * L.add(I); } }
		 * 
		 * Item[] unScheduledItems = new Item[L.size()]; for (int i = 0; i <
		 * L.size(); i++) { unScheduledItems[i] = L.get(i); }
		 */

		PickupDeliverySolution solution = new PickupDeliverySolution(routes,
				null);
		totalDistance = 0;
		StatisticRoute[] routeInfo = new StatisticRoute[routes.length];

		for (int i = 0; i < routes.length; i++) {
			totalDistance += routes[i].getDistance();
			Vehicle vh = routes[i].getVehicle();
			StatisticTrip[] st = new StatisticTrip[mVehicle2Trips.get(vh)
					.size()];
			for (int j = 0; j < st.length; j++) {
				VehicleTrip trip = mVehicle2Trips.get(vh).get(j);
				st[j] = new StatisticTrip(trip.load, trip.seqPoints.size() / 2);
			}
			routeInfo[i] = new StatisticRoute(vh.getCode(),
					routes[i].getDistance(),
					routes[i].getVehicle().getWeight(), routes[i].getLoad(),
					(routes[i].getElements().length - 2) / 2, st);
		}
		StatisticInformation info = new StatisticInformation(totalDistance,
				routes.length, routeInfo);
		solution.setStatistic(info);

		return solution;

	}

	public PickupDeliverySolution buildSolution(VarRoutesVR XR) {
		// scheduled_vehicle[k] is the index of vehicle assigned to route k (k =
		// 0,1,...)

		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;
		HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = new HashMap<Vehicle, ArrayList<VehicleTrip>>();
		for (VehicleTrip trip : trips) {
			if (mVehicle2Trips.get(trip.vehicle) == null)
				mVehicle2Trips.put(trip.vehicle, new ArrayList<VehicleTrip>());
			mVehicle2Trips.get(trip.vehicle).add(trip);

		}
		int nbInternalTrucks = 0;
		int nbExternalTrucks = 0;
		int nbr = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				// mPoint2Vehicle.put(XR.startPoint(k),
				// getVehicle(scheduled_vehicle[k-1]));
				// mPoint2Vehicle.put(XR.endPoint(k),
				// getVehicle(scheduled_vehicle[k-1]));
			}
		}

		int nbTrips = 0;
		double totalDistance = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {

				// check exclusive vehicle-location
				Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String lc = mPoint2LocationCode.get(p);
					if (mVehicle2NotReachedLocations.get(vh.getCode())
							.contains(lc)) {
						log(name()
								+ "::buildSolution, EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????, vehicle "
								+ vh.getCode() + " <-> location " + lc);
					}
				}
			}
		}

		System.out.println(name() + "::buildSolution, number of routes = "
				+ nbr);

		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				double distance = 0;
				double maxLoad = 0;
				// double totalLoadOfTrips = 0;
				// double loadPerTrip = 0;
				// double tripDistance = 0;
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					//int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if (p == XR.startPoint(k)) {
						// depot
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						long at = mPoint2ArrivalTime.get(p);

						long dt = mPoint2DepartureTime.get(p);// at +
																// serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getLat();
						double d_lng = v.getLng();
						e = new RoutingElement(v.getStartLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-",
								s_at, s_dt);
						e.setDescription("weight: " + v.getWeight());
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_STARTING_POINT);

					} else if (p == XR.endPoint(k)) {
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getEndLat();
						double d_lng = v.getEndLng();
						e = new RoutingElement(v.getEndLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-");
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_END_POINT);

					} else {
						// int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p).get(0);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						boolean isEndTrip = false;
						double tripLoad = 0;
						if (mPoint2Type.get(p).equals("P")) {
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
							// totalLoadOfTrips += awn.getWeights(p);
							// Point pp = XR.prev(p);
							// if(Math.abs(awn.getSumWeights(pp)) <= EPS){
							// tripLoad = loadPerTrip;
							// loadPerTrip = 0;
							// isEndTrip = true;
							// tripDistance += awm.getWeight(pp, p);
							// }else{

							// }
							// loadPerTrip += awn.getWeights(p);

						} else {

						}
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						// long dt = at + serviceDuration.get(p);
						long at = mPoint2ArrivalTime.get(p);
						long dt = mPoint2DepartureTime.get(p);
						if(at < earliestAllowedArrivalTime.get(p))
							at = earliestAllowedArrivalTime.get(p);
						
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						//if(locationCode.equals("10008314")){
						//if(mPoint2Type.get(p).equals("D")){
						//	if(DateTimeUtils.dateTime2Int(r.getLateDeliveryTime()) < at){
						//		log(name() + "::buildSolution, BUG?? Location " + locationCode + 
						//			", arrivalTime " + s_at + ", lateDeliveryTime = " + r.getLateDeliveryTime()
						//			+ ", departureTime = " + s_dt);
							//}
						//}
						//}
						
						e = new RoutingElement(locationCode, "-", lat + ","
								+ lng, lat, lng, s_at, s_dt);
						// e = new RoutingElement(requests[ir].getOrderID(),
						// "-", lat + "," + lng,lat,lng, s_at, s_dt);
						String orderCodes = "";
						for (int jj = 0; jj < mPoint2Request.get(p).size(); jj++) {
							PickupDeliveryRequest rr = mPoint2Request.get(p)
									.get(jj);
							orderCodes += rr.getOrderCode() + ", ";
						}
						String s_accumulate_load = "0";
						if (awn.getSumWeights(p) > Utility.EPS) {
							s_accumulate_load = awn.getSumWeights(p) + "";
						}
						e.setDescription("orderID: " + r.getOrderID()
								+ ", orderCodes = " + orderCodes + ", type = "
								+ mPoint2Type.get(p) + ", amount: "
								+ mPoint2Demand.get(p) + ", accumulate load = "
								+ s_accumulate_load);
						if (maxLoad < awn.getSumWeights(p))
							maxLoad = awn.getSumWeights(p);
						e.setOrderId(r.getOrderID());
						double e_load = (int) 0;
						if (awn.getSumWeights(p) > Utility.EPS)
							e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);

						Item[] pitems = null;
						if (mPoint2Type.get(p).equals("P")) {

							Integer[] L = mPoint2IndexItems.get(p);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}
						} else if (mPoint2Type.get(p).equals("D")) {
							int idx = mDeliveryPoint2DeliveryIndex.get(p);
							Point pickup = pickupPoints.get(idx);
							Integer[] L = mPoint2IndexItems.get(pickup);

							if (L != null) {
								pitems = new Item[L.length];
								for (int ii = 0; ii < L.length; ii++)
									pitems[ii] = items.get(L[ii]);
							} else {

							}

						}
						e.setItems(pitems);
					}
					distance += awm.getWeight(p, XR.next(p));
					lst.add(e);
				}
				// add terminating point of route
				Point p = XR.endPoint(k);
				// long at = (long) eat.getEarliestArrivalTime(p);
				// if (at < earliestAllowedArrivalTime.get(p))
				// at = earliestAllowedArrivalTime.get(p);
				long at = mPoint2ArrivalTime.get(p);

				String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);

				Vehicle v = mPoint2Vehicle.get(p);
				double d_lat = v.getEndLat();
				double d_lng = v.getEndLng();
				RoutingElement e = new RoutingElement(v.getEndLocationCode(),
						"-", d_lat + "," + d_lng, d_lat, d_lng, s_at, "-");

				double e_load = (int) 0;
				if (awn.getSumWeights(p) > Utility.EPS)
					e_load = awn.getSumWeights(p);

				double e_distance = awe.getCostRight(p);
				e.setLoad(e_load);
				e.setDistance(e_distance);
				// e.setTotalTripLoad(loadPerTrip);
				// e.setType(ROUTE_ELEMENT_END_TRIP);
				// e.setTripDistance(awm.getWeight(XR.prev(p), p));

				lst.add(e);
				RoutingElement firstEle = lst.get(0);
				NumberFormat formatter = new DecimalFormat("#0.00");
				String std_d = formatter.format(distance);
				firstEle.setDescription(firstEle.getDescription()
						+ ", length = " + std_d + " (km)" + ", maxLoad = "
						+ maxLoad + " (kg)");

				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for (int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				// routes[nbr] = new RoutingSolution(a_route);

				/*
				 * Vehicle vh = null;
				 * 
				 * if (k <= vehicles.length) vh = vehicles[k - 1]; else vh =
				 * externalVehicles[k - vehicles.length - 1];
				 */
				Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
				if (isInternalVehicle(vh)) {
					nbInternalTrucks++;
				} else {
					nbExternalTrucks++;
				}
				routes[nbr] = new RoutingSolution(vh, a_route, 0.0, distance);
				totalDistance += distance;
			}
		}
		/*
		 * for (int i = 0; i < requests.length; i++) { PickupDeliveryRequest r =
		 * requests[i]; System.out.println("req[" + i + "], pickup = " +
		 * r.getPickupLat() + "," + r.getPickupLng() + ", delivery = " +
		 * r.getDeliveryLat() + "," + r.getDeliveryLng());
		 * 
		 * }
		 */
		/*
		 * ArrayList<Item> L = new ArrayList<Item>(); for (int i :
		 * remainUnScheduled) { Point pickup = pickupPoints.get(i); String des =
		 * analyzeRequest(i); for (int j = 0; j <
		 * mPoint2IndexItems.get(pickup).length; j++) { Item I =
		 * items.get(mPoint2IndexItems.get(pickup)[j]); I.setDescription(des);
		 * L.add(I); } }
		 * 
		 * Item[] unScheduledItems = new Item[L.size()]; for (int i = 0; i <
		 * L.size(); i++) { unScheduledItems[i] = L.get(i); }
		 */

		PickupDeliverySolution solution = new PickupDeliverySolution(routes,
				null);
		totalDistance = 0;
		StatisticRoute[] routeInfo = new StatisticRoute[routes.length];

		for (int i = 0; i < routes.length; i++) {
			totalDistance += routes[i].getDistance();
			Vehicle vh = routes[i].getVehicle();
			StatisticTrip[] st = new StatisticTrip[mVehicle2Trips.get(vh)
					.size()];
			for (int j = 0; j < st.length; j++) {
				VehicleTrip trip = mVehicle2Trips.get(vh).get(j);
				st[j] = new StatisticTrip(trip.load, trip.seqPoints.size() / 2);
			}
			routeInfo[i] = new StatisticRoute(vh.getCode(),
					routes[i].getDistance(),
					routes[i].getVehicle().getWeight(), routes[i].getLoad(),
					(routes[i].getElements().length - 2) / 2, st);
		}

		SolutionIndicator indicator = evaluation(XR);

		StatisticInformation info = new StatisticInformation(totalDistance,
				routes.length, routeInfo);
		info.setNumberExternalTrucks(nbExternalTrucks);
		info.setNumberInternalTrucks(nbInternalTrucks);
		info.setNumberTrips(trips.size());
		info.setIndicator(indicator);
		info.setInputIndicator(inputIndicator);

		long time = System.currentTimeMillis() - startExecutionTime;
		time = time / 1000;
		String hms = DateTimeUtils.second2HMS((int) time);
		info.setExecutionTime(hms);

		if (timeLimitExpired) {
			info.setTimeLimitExpired("Y");
		} else {
			info.setTimeLimitExpired("N");
		}
		solution.setStatistic(info);
		solution.setErrorMSG("OK");
		solution.setDescription("OK");
		return solution;
	}

	public void logVehicleRoutes(VarRoutesVR XR) {
		if (CHECK_AND_LOG) {
			VehicleTripCollection VTC = analyzeTrips(XR);

			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				Point s = XR.startPoint(k);

				Vehicle vh = mPoint2Vehicle.get(s);
				if (XR.emptyRoute(k) && !isInternalVehicle(vh))
					continue;

				ArrayList<VehicleTrip> t = VTC.mVehicle2Trips.get(vh);
				String tripInfo = "";
				for (int i = 0; i < t.size(); i++) {
					tripInfo = tripInfo + "[" + t.get(i).load + ", nbPoints "
							+ t.get(i).seqPoints.size() + "], ";
				}
				for (int i = 0; i < t.size(); i++)
					for (int j = i + 1; j < t.size(); j++)
						if (conflictTrips(t.get(i), t.get(j))) {
							tripInfo = tripInfo + " conflict[" + i + "," + j
									+ "], ";
						}
				log(name() + "::logVehicleRoutes, vehicle " + vh.getCode()
						+ ", cap = " + vh.getWeight() + ", route = "
						+ (XR.emptyRoute(k) ? "EMPTY" : "NOT_EMPTY")
						+ ", trips " + tripInfo);
			}
		}
	}

	public PickupDeliverySolution buildSolution(VarRoutesVR XR, Vehicle vh,
			AccumulatedWeightNodesVR awn, AccumulatedWeightEdgesVR awe,
			ArcWeightsManager awm) {
		// scheduled_vehicle[k] is the index of vehicle assigned to route k (k =
		// 0,1,...)

		int nbr = 0;
		for (int k = 1; k <= 1; k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				mPoint2Vehicle.put(XR.startPoint(k), vh);
				mPoint2Vehicle.put(XR.endPoint(k), vh);
			}
		}

		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)) {
				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				double distance = 0;
				double maxLoad = 0;
				// double totalLoadOfTrips = 0;
				// double loadPerTrip = 0;
				// double tripDistance = 0;
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					// int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if (p == XR.startPoint(k)) {
						// depot
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						long at = mPoint2ArrivalTime.get(p);

						long dt = mPoint2DepartureTime.get(p);// at +
																// serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getLat();
						double d_lng = v.getLng();
						e = new RoutingElement(v.getStartLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-",
								s_at, s_dt);
						e.setDescription("weight: " + v.getWeight());
						double e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_STARTING_POINT);

					} else if (p == XR.endPoint(k)) {
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getEndLat();
						double d_lng = v.getEndLng();
						e = new RoutingElement(v.getEndLocationCode(), "-",
								d_lat + "," + d_lng, d_lat, d_lng, "-", "-");
						double e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
						// e.setType(ROUTE_ELEMENT_END_POINT);

					} else {
						// int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p).get(0);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						boolean isEndTrip = false;
						double tripLoad = 0;
						if (mPoint2Type.get(p).equals("P")) {
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
							// totalLoadOfTrips += awn.getWeights(p);
							// Point pp = XR.prev(p);
							// if(Math.abs(awn.getSumWeights(pp)) <= EPS){
							// tripLoad = loadPerTrip;
							// loadPerTrip = 0;
							// isEndTrip = true;
							// tripDistance += awm.getWeight(pp, p);
							// }else{

							// }
							// loadPerTrip += awn.getWeights(p);

						} else {

						}
						// long at = (long) eat.getEarliestArrivalTime(p);
						// if (at < earliestAllowedArrivalTime.get(p))
						// at = earliestAllowedArrivalTime.get(p);
						// long dt = at + serviceDuration.get(p);
						long at = mPoint2ArrivalTime.get(p);
						long dt = mPoint2DepartureTime.get(p);

						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);

						e = new RoutingElement(locationCode, "-", lat + ","
								+ lng, lat, lng, s_at, s_dt);
						// e = new RoutingElement(requests[ir].getOrderID(),
						// "-", lat + "," + lng,lat,lng, s_at, s_dt);
						e.setDescription("orderID: " + r.getOrderID()
								+ ", type = " + mPoint2Type.get(p)
								+ ", amount: " + mPoint2Demand.get(p)
								+ ", accumulate load = " + awn.getSumWeights(p));
						if (maxLoad < awn.getSumWeights(p))
							maxLoad = awn.getSumWeights(p);
						e.setOrderId(r.getOrderID());
						double e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);

						Integer[] L = mPoint2IndexItems.get(p);
						Item[] pitems = null;
						if (L != null) {
							pitems = new Item[L.length];
							for (int ii = 0; ii < L.length; ii++)
								pitems[ii] = items.get(L[ii]);
						}
						e.setItems(pitems);
					}
					distance += awm.getWeight(p, XR.next(p));
					lst.add(e);
				}
				// add terminating point of route
				Point p = XR.endPoint(k);
				// long at = (long) eat.getEarliestArrivalTime(p);
				// if (at < earliestAllowedArrivalTime.get(p))
				// at = earliestAllowedArrivalTime.get(p);
				long at = mPoint2ArrivalTime.get(p);

				String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);

				Vehicle v = mPoint2Vehicle.get(p);
				double d_lat = v.getEndLat();
				double d_lng = v.getEndLng();
				RoutingElement e = new RoutingElement(v.getEndLocationCode(),
						"-", d_lat + "," + d_lng, d_lat, d_lng, s_at, "-");
				double e_load = awn.getSumWeights(p);
				double e_distance = awe.getCostRight(p);
				e.setLoad(e_load);
				e.setDistance(e_distance);
				// e.setTotalTripLoad(loadPerTrip);
				// e.setType(ROUTE_ELEMENT_END_TRIP);
				// e.setTripDistance(awm.getWeight(XR.prev(p), p));

				lst.add(e);
				RoutingElement firstEle = lst.get(0);
				NumberFormat formatter = new DecimalFormat("#0.00");
				String std_d = formatter.format(distance);
				firstEle.setDescription(firstEle.getDescription()
						+ ", length = " + std_d + " (km)" + ", maxLoad = "
						+ maxLoad + " (kg)");

				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for (int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				// routes[nbr] = new RoutingSolution(a_route);

				/*
				 * Vehicle vh = null;
				 * 
				 * if (k <= vehicles.length) vh = vehicles[k - 1]; else vh =
				 * externalVehicles[k - vehicles.length - 1];
				 */

				routes[nbr] = new RoutingSolution(vh, a_route, 0.0, distance);
			}
		}
		for (int i = 0; i < requests.length; i++) {
			PickupDeliveryRequest r = requests[i];
			System.out.println("req[" + i + "], pickup = " + r.getPickupLat()
					+ "," + r.getPickupLng() + ", delivery = "
					+ r.getDeliveryLat() + "," + r.getDeliveryLng());

		}

		return new PickupDeliverySolution(routes, null);

	}
	/*
	 * public PickupDeliverySolution compute(BrennTagPickupDeliveryInput input)
	 * { this.input = input; this.requests = input.getRequests(); this.vehicles
	 * = input.getVehicles(); this.distances = input.getDistances();
	 * this.travelTimes = input.getTravelTime(); this.externalVehicles =
	 * input.getExternalVehicles(); mapData();
	 * 
	 * HashSet<Integer> remainUnScheduled = search(); int[] scheduled_vehicle =
	 * new int[XR.getNbRoutes()]; for (int k = 0; k < XR.getNbRoutes(); k++) {
	 * scheduled_vehicle[k] = k; } PickupDeliverySolution sol =
	 * buildSolution(XR, scheduled_vehicle, remainUnScheduled);
	 * 
	 * return sol; }
	 */

	/*
	 * // model public VRManager mgr; public VarRoutesVR XR; public
	 * ArrayList<Point> starts; public ArrayList<Point> ends; public
	 * ArrayList<Point> pickup; public ArrayList<Point> delivery; public
	 * ArrayList<Point> allPoints; public ArrayList<Point> clientPoints; public
	 * ArcWeightsManager awm; public HashMap<Point, String> mPoint2GeoPoint;
	 * public HashMap<String, Double> mCode2Distance; public
	 * HashMap<PickupDeliveryRequest, Point> mReq2PickupPoint; public
	 * HashMap<PickupDeliveryRequest, Point> mReq2DeliveryPoint; public
	 * HashMap<Point, PickupDeliveryRequest> mPoint2Request; public
	 * HashMap<Point, String> mPoint2LatLng; public HashMap<Point, String>
	 * mPoint2Code;
	 * 
	 * public IFunctionVR obj;
	 * 
	 * public void greedyConstruct(){ for(int i = 0; i < requests.length; i++){
	 * Point p = mReq2PickupPoint.get(requests[i]); Point d =
	 * mReq2DeliveryPoint.get(requests[i]); double eval = Integer.MAX_VALUE;
	 * Point sel_x = null; for(int k = 1; k <= XR.getNbRoutes(); k++){ for(Point
	 * x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)){ double e =
	 * obj.evaluateAddOnePoint(p, x); if(e < eval){ eval = e; sel_x = x; } } }
	 * mgr.performAddOnePoint(p, sel_x); mgr.performAddOnePoint(d, p);
	 * 
	 * System.out.println(XR.toString() + ", obj = " + obj.getValue()); } }
	 * public PickupDeliverySolution compute(PickupDeliveryInput input){
	 * requests = input.getRequests(); vehicles = input.getVehicles(); distances
	 * = input.getDistances(); for(int i = 0; i < requests.length; i++){
	 * PickupDeliveryRequest r = requests[i]; System.out.println(r.getOrderID()
	 * + ", pickupTime = " + r.getEarlyPickupTime() + " - " +
	 * r.getLatePickupTime() + ", deliveryTime = " + r.getEarlyDeliveryTime() +
	 * " - " + r.getLateDeliveryTime() + " travel time = " +
	 * DateTimeUtils.distanceDateTime(r.getEarlyDeliveryTime(),
	 * r.getEarlyPickupTime())); } for(int i = 0; i < distances.length; i++){
	 * System.out.println("distance " + distances[i].getSrcCode() + " --> " +
	 * distances[i].getDestCode() + " = " + distances[i].getDistance()); }
	 * 
	 * starts = new ArrayList<Point>(); ends = new ArrayList<Point>(); allPoints
	 * = new ArrayList<Point>(); clientPoints = new ArrayList<Point>();
	 * mPoint2GeoPoint = new HashMap<Point, String>(); mReq2PickupPoint = new
	 * HashMap<PickupDeliveryRequest, Point>(); mReq2DeliveryPoint = new
	 * HashMap<PickupDeliveryRequest, Point>(); mPoint2Request = new
	 * HashMap<Point, PickupDeliveryRequest>(); mPoint2Code = new HashMap<Point,
	 * String>(); mPoint2LatLng = new HashMap<Point, String>();
	 * 
	 * for(int k = 0; k < vehicles.length; k++){ Point s = new Point(); Point t
	 * = new Point(); starts.add(s); ends.add(t); allPoints.add(s);
	 * allPoints.add(t); mPoint2GeoPoint.put(s,
	 * vehicles[k].getStartLocationCode()); mPoint2GeoPoint.put(t,
	 * vehicles[k].getEndLocationCode()); mPoint2LatLng.put(s,
	 * vehicles[k].getLat() + "," + vehicles[k].getLng()); mPoint2LatLng.put(t,
	 * vehicles[k].getEndLat() + "," + vehicles[k].getEndLng());
	 * mPoint2Code.put(s, vehicles[k].getStartLocationCode());
	 * mPoint2Code.put(t, vehicles[k].getEndLocationCode()); } for(int i = 0; i
	 * < requests.length; i++){ Point p = new Point(); Point d = new Point();
	 * allPoints.add(p); allPoints.add(d); clientPoints.add(p);
	 * clientPoints.add(d);
	 * 
	 * mPoint2GeoPoint.put(p, requests[i].getPickupLocationCode());
	 * mPoint2GeoPoint.put(d, requests[i].getDeliveryLocationCode());
	 * mReq2PickupPoint.put(requests[i], p); mReq2DeliveryPoint.put(requests[i],
	 * d); mPoint2Request.put(p, requests[i]); mPoint2Request.put(d,
	 * requests[i]);
	 * 
	 * mPoint2LatLng.put(p, requests[i].getPickupLat() + "," +
	 * requests[i].getPickupLng()); mPoint2LatLng.put(d,
	 * requests[i].getDeliveryLat() + "," + requests[i].getDeliveryLng());
	 * mPoint2Code.put(p, vehicles[i].getStartLocationCode());
	 * mPoint2Code.put(d, vehicles[i].getEndLocationCode()); } mCode2Distance =
	 * new HashMap<String, Double>(); for(int i = 0; i < distances.length; i++){
	 * String code = distances[i].getSrcCode() + "-" +
	 * distances[i].getDestCode(); mCode2Distance.put(code,
	 * distances[i].getDistance()); } awm = new ArcWeightsManager(allPoints);
	 * for(Point p1: allPoints){ String src = mPoint2GeoPoint.get(p1); for(Point
	 * p2: allPoints){ String dest = mPoint2GeoPoint.get(p2); String code = src
	 * + "-" + dest; double d = mCode2Distance.get(code); awm.setWeight(p1, p2,
	 * d); } } mgr = new VRManager(); XR = new VarRoutesVR(mgr); for(int k = 0;
	 * k < starts.size(); k++){ XR.addRoute(starts.get(k), ends.get(k)); }
	 * for(Point p: clientPoints) XR.addClientPoint(p);
	 * 
	 * obj = new TotalCostVR(XR, awm); mgr.close();
	 * 
	 * greedyConstruct();
	 * 
	 * RoutingSolution[] rs = new RoutingSolution[XR.getNbRoutes()]; for(int k =
	 * 1; k <= XR.getNbRoutes(); k++){ ArrayList<Point> P = new
	 * ArrayList<Point>(); for(Point p = XR.startPoint(k); p != XR.endPoint(k);
	 * p = XR.next(p)){ P.add(p); } P.add(XR.endPoint(k)); RoutingElement[] re =
	 * new RoutingElement[P.size()];
	 * 
	 * 
	 * for(int i = 0; i < P.size(); i++){ Point p = P.get(i); String code =
	 * mPoint2Code.get(p); String latlng = mPoint2LatLng.get(p); String[] s =
	 * latlng.split(","); double lat = Double.valueOf(s[0]); double lng =
	 * Double.valueOf(s[1]); re[i] = new RoutingElement(code, "-",
	 * latlng,lat,lng); System.out.print(code + " -> "); } System.out.println();
	 * rs[k-1] = new RoutingSolution(re); }
	 * 
	 * 
	 * PickupDeliverySolution sol = new PickupDeliverySolution(rs); return sol;
	 * }
	 */
}
