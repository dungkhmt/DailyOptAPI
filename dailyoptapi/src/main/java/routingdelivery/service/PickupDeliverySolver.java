package routingdelivery.service;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.test.TestAPI;

import localsearch.domainspecific.vehiclerouting.apps.sharedaride.Constraint.CPickupDeliveryOfGoodVR;
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
import routingdelivery.model.DistanceElement;
import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ExclusiveItem;
import routingdelivery.smartlog.brenntag.model.ExclusiveVehicleLocation;
import utils.DateTimeUtils;

public class PickupDeliverySolver {
	public static final String module = PickupDeliverySolver.class.getName();
	protected BrennTagPickupDeliveryInput input;
	protected PickupDeliveryRequest[] requests;
	protected Vehicle[] vehicles;
	protected Vehicle[] externalVehicles;
	protected DistanceElement[] distances;
	protected DistanceElement[] travelTimes;

	protected HashMap<String, Double> mDistance;
	protected HashMap<String, Double> mTravelTime;
	protected int N;// pickup points: 0, 1, 2, ..., N-1 delivery points:
					// N,...,N+N-1
	protected int M;// start points of vehicles 2N,...,2N+M-1, end points of
					// vehicles are 2N+M,..., 2N+2M-1
	protected double[][] dis;// dis[i][j] is the distance from point i to point
								// j
	protected double[] cap;// capacity of vehicles
	protected HashMap<Integer, Integer> mItemIndex2RequestIndex;
	protected HashMap<Point, Vehicle> mPoint2Vehicle;
	protected HashMap<Point, PickupDeliveryRequest> mPoint2Request;
	protected HashMap<Point, String> mPoint2Type;// "S": start, "P": pickup,
													// "D": delivery, "T":
													// terminate

	protected ArrayList<Point> startPoints;
	protected ArrayList<Point> endPoints;
	protected ArrayList<Point> pickupPoints;
	protected ArrayList<Point> deliveryPoints;
	protected HashMap<Point, Point> pickup2DeliveryOfGood = new HashMap<Point, Point>();
	protected ArrayList<Point> allPoints;
	protected HashMap<Point, Integer> mPoint2Index;
	protected HashMap<Point, String> mPoint2LocationCode;
	protected HashMap<Point, Double> mPoint2Demand;
	protected HashMap<Point, HashSet<Integer>> mPoint2PossibleVehicles;
	protected HashMap<String, HashSet<String>> mItem2ExclusiveItems;
	protected HashMap<String, HashSet<String>> mVehicle2NotReachedLocations;
	protected HashMap<Item, Item[]> mLogicalItem2PhysicalItems;
	protected HashMap<Point, Integer> mPickupPoint2RequestIndex;

	protected HashMap<Integer, HashSet<Integer>> mRequest2PointIndices;
	
	protected HashMap<Point, Integer> earliestAllowedArrivalTime;
	protected HashMap<Point, Integer> serviceDuration;
	protected HashMap<Point, Integer> lastestAllowedArrivalTime;
	protected ArcWeightsManager awm;
	protected ArcWeightsManager travelTime;
	protected NodeWeightsManager nwm;
	protected VRManager mgr;
	protected VarRoutesVR XR;
	protected AccumulatedWeightNodesVR awn;
	protected AccumulatedWeightEdgesVR awe;

	protected ConstraintSystemVR CS;
	//protected CEarliestArrivalTimeVR ceat;
	//protected EarliestArrivalTimeVR eat;
	protected IFunctionVR cost;
	protected IFunctionVR[] load;
	protected LexMultiFunctions F;

	// protected HashMap<Point, String> mPoint2ItemCode;// map a point to the
	// code
	// of an item
	protected HashMap<Point, Integer[]> mPoint2IndexItems;// map a point to an
															// item

	protected HashMap<Point, HashSet<Integer>> mPoint2IndexLoadedItems;// mPoint2LoadedItems[p]
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


	protected HashMap<Item, Integer> mItem2Index;
	protected HashMap<String, Integer> mItemCode2Index;
	protected ArrayList<Item> items;// itemCodes.get(i) is the code of ith item,
									// use with mItemCode2Index;
	protected boolean[][] itemConflict;// itemConflict[i][j] = T if item i and j
										// conflict

	protected HashMap<Point, Integer> mPoint2ArrivalTime;
	protected HashMap<Point, Integer> mPoint2DepartureTime;

	protected PrintWriter log;

	public void initializeLog() {
		try {
			log = new PrintWriter(TestAPI.ROOT_DIR + "/log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
			log = null;
		}
	}

	public void finalizeLog() {
		if (log != null)
			log.close();
	}

	protected String code(String from, String to) {
		return from + "-" + to;
	}

	/*
	 * protected HashSet<Integer>
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

	protected boolean checkExclusiveItems(Point fromPoint, Point pickup) {
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

	private boolean checkExclusiveItemForPickupAndDeliveryPoint(Point p,
			Point d, Point pickup) {
		// return true if pickup can be inserted after p and delivery is
		// inserted after d
		for (Point tp = p; tp != XR.next(d); tp = XR.next(tp))
			if (!checkExclusiveItems(tp, pickup))
				return false;
		return true;
	}

	protected void propagateArrivalDepartureTime(boolean DEBUG) {
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			propagateArrivalDepartureTime(k, DEBUG);
		}
	}

	protected void propagateArrivalDepartureTime(int k, boolean DEBUG) {

		// for(int k = 1; k <= XR.getNbRoutes(); k++){
		Point p = XR.startPoint(k);
		int startTime = earliestAllowedArrivalTime.get(p)
				+ serviceDuration.get(p);
		mPoint2DepartureTime.put(p, startTime);
		mPoint2ArrivalTime.put(p, startTime);
		startTime = startTime + (int) travelTime.getWeight(p, XR.next(p));
		p = XR.next(p);
		while (p != XR.endPoint(k)) {
			// Point p = s;
			String locationCode = mPoint2LocationCode.get(p);
			if (log != null && DEBUG) {
				//log.println("start  point " + p.ID + ", locationCode = "
				//		+ locationCode);
			}
			int fixTime = 0;
			if (mPoint2Type.get(p).equals("D"))
				fixTime = mPoint2Request.get(p).getFixUnloadTime();
			else if (mPoint2Type.get(p).equals("P"))
				fixTime = mPoint2Request.get(p).getFixLoadTime();

			Point np = XR.next(p);
			int duration = serviceDuration.get(p);
			while (np != null) {
				if (log != null && DEBUG) {
					//log.println("point np =  " + np.ID + ", locationCode = "
					//		+ mPoint2LocationCode.get(np));
				}
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				duration += serviceDuration.get(np);
				if (log != null && DEBUG) {
					//log.println("point np =  " + np.ID + ", locationCode = "
					//		+ mPoint2LocationCode.get(np)
					//		+ ", KEEP and augment duration = " + duration);
				}

				if (np == XR.endPoint(k))
					break;
				else
					np = XR.next(np);
			}
			duration += fixTime;
			if (log != null && DEBUG) {
				//log.println("ADD FIX TIME " + fixTime + " --> duration = "
				//		+ duration);
			}

			int departureTime = startTime + duration;

			mPoint2ArrivalTime.put(p, startTime);
			mPoint2DepartureTime.put(p, departureTime);
			if (log != null && DEBUG) {
				//log.println("SET START point p = " + p.ID + ", SET arr = "
				//		+ mPoint2ArrivalTime.get(p) + ", dep = "
				//		+ mPoint2DepartureTime.get(p));
			}

			np = XR.next(p);
			while (np != null) {
				if (!mPoint2LocationCode.get(np).equals(locationCode))
					break;

				mPoint2ArrivalTime.put(np, startTime);
				mPoint2DepartureTime.put(np, departureTime);
				if (log != null && DEBUG) {
					//log.println("KEEP point " + np.ID + ", SET arr = "
					//		+ mPoint2ArrivalTime.get(np) + ", dep = "
					//		+ mPoint2DepartureTime.get(np));
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
				//startTime = departureTime + (int) travelTime.getWeight(p, np);
				p = np;
			} else{
				
				break;
			}
		}
		mPoint2ArrivalTime.put(XR.endPoint(k), startTime);
		// }
	}

	protected int evaluateNewOrderLoad(int k, Point pickup,
			Point p, Point delivery, Point d) {
		// return 1 is there is new order loaded when inserting (pickup,delivery) after (p,d)
		int reqID = mPickupPoint2RequestIndex.get(pickup);
		boolean ok = false;
		if(mPoint2IndexLoadedItems.get(p) != null){
			for(int i: mPoint2IndexLoadedItems.get(p)){
				int r = mItemIndex2RequestIndex.get(i);
				if(r == reqID){
					ok = true;
					break;
				}
			}
		}
		if(!ok) return 1;// has new order to be loaded into vehicle
		return 0;
	}
	protected int evaluateTimeViolationsAddTwoPoints(int k, Point pickup,
			Point p, Point delivery, Point d) {
		XR.performAddOnePoint(delivery, d);
		XR.performAddOnePoint(pickup, p);
		propagateArrivalDepartureTime(k, true);

		int violations = 0;
		for (Point q = XR.next(XR.startPoint(k)); q != XR.endPoint(k); q = XR
				.next(q)) {
			if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
				violations += (mPoint2ArrivalTime.get(q)
						- lastestAllowedArrivalTime.get(q));
			//System.out.println("evaluateTimeViolationsAddTwoPoints, arrT(" + q.ID + ") = " + mPoint2ArrivalTime.get(q) + 
			//		"(" + DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(q)) + ")" +  
			//		", latestAllowArrivalTime(" + q.ID + ") = " + lastestAllowedArrivalTime.get(q) + 
			//		"(" + DateTimeUtils.unixTimeStamp2DateTime(lastestAllowedArrivalTime.get(q)));
		}
		Point q = XR.endPoint(k);
		if (mPoint2ArrivalTime.get(q) > lastestAllowedArrivalTime.get(q))
			violations += (mPoint2ArrivalTime.get(q)
					- lastestAllowedArrivalTime.get(q));
		
		// recovery
		XR.performRemoveOnePoint(delivery);
		XR.performRemoveOnePoint(pickup);
		propagateArrivalDepartureTime(k, false);

		return violations;

	}

	protected HashSet<Integer> greedyConstructMaintainConstraint() {
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
			for (int i : cand) {
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				// for(int k = 1; k <= XR.getNbRoutes(); k++){
				// try internal vehicles FIRST
				for (int k = 1; k <= vehicles.length; k++) {
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

							
							if(awn.getSumWeights(p) > 0){
								// cannot pickup any more if there are still items on the vehicle
								continue;
							}
							
							// double ec = CS.evaluateAddTwoPoints(pickup, p,
							// delivery, d);
							double ec = evaluateTimeViolationsAddTwoPoints(k,
									pickup, p, delivery, d);

							double ef = cost.evaluateAddTwoPoints(pickup, p,
									delivery, d);

							System.out.println("consider i = " + i + ", vehicle k = " + k + ", pickup = " +  pickup.ID + ", delivery = "
									+ delivery.ID + ", p = " + p.ID + ", d = " + d.ID + ", ec = " + ec + ", ef = " + ef);
							
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
					for (int k = vehicles.length + 1; k <= M; k++) {
						String vehicleCode = externalVehicles[k
								- vehicles.length - 1].getCode();
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
				mgr.performAddOnePoint(sel_delivery, sel_d);
				mgr.performAddOnePoint(sel_pickup, sel_p);
				propagateArrivalDepartureTime(sel_k, true);

				// log.println("add delivery " + sel_delivery.ID + " after "+
				// sel_d.ID +
				// " AND pickup " + sel_pickup.ID + " after " + sel_p.ID);

				System.out.println("init addOnePoint(" + sel_pickup.ID + ","
						+ sel_p.ID + "), and (" + sel_delivery.ID + ","
						+ sel_d.ID + ", XR = " + XR.toString() + ", CS = "
						+ CS.violations() + ", cost = " + cost.getValue());
				cand.remove(sel_i);

				// update loaded items
				for (int I : mPoint2IndexLoadedItems.get(sel_p)) {
					mPoint2IndexLoadedItems.get(sel_pickup).add(I);
				}
				for (int I : mPoint2IndexLoadedItems.get(sel_d)) {
					mPoint2IndexLoadedItems.get(sel_delivery).add(I);
				}
				for (Point p = sel_pickup; p != sel_delivery; p = XR.next(p)) {
					// mPoint2LoadedItems.get(p).add(
					// mPoint2ItemCode.get(sel_pickup));
					for (int ite : mPoint2IndexItems.get(sel_pickup)) {
						mPoint2IndexLoadedItems.get(p).add(ite);
					}
				}

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
	public boolean better(double e1, double e2, double e3, double f1, double f2, double f3){
		if(e1 < f1) return true;
		if(e1 > f1) return false;
		if(e2 < f2) return true;
		if(e2 > f2) return false;
		if(e3 < f3) return true;
		return false;
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
					+ "\n Trọng lượng đơn hàng vượt quá tải trọng của xe nặng nhất";
			return des;
		}

		// analyze if there exists a vehicle that can go to the location of
		// request
		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > vehicles.length)
				vh = externalVehicles[k - vehicles.length - 1];
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
					+ "\n Không có xe nào có thể vận chuyển đơn hàng do cấm đường";
			return des;
		}

		// analyze if exclusive items
		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > vehicles.length)
				vh = externalVehicles[k - vehicles.length - 1];
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
					+ "\n Đơn hàng không thể được vận chuyển do ràng buộc các mặt hàng không đi kèm với nhau";
			return des;
		}

		// analyze the if there exists a feaisble location w.r.t capacity

		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > vehicles.length)
				vh = externalVehicles[k - vehicles.length - 1];
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
					+ "\n Không thể xếp thêm đơn hàng lên xe do vượt quá ràng buộc về tải trọng";
			return des;
		}

		// analyze time constraint
		ok = false;
		for (int k = 1; k <= M; k++) {
			Vehicle vh = null;
			String vehicleCode = "";
			if (k > vehicles.length)
				vh = externalVehicles[k - vehicles.length - 1];
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
					+ "\n Không thể xếp đơn hàng do thời gian làm việc của các xe đã hết";
			return des;
		}
		return des;
	}

	protected void mapData() {
		mDistance = new HashMap<String, Double>();
		mTravelTime = new HashMap<String, Double>();
		for (int i = 0; i < distances.length; i++) {
			String src = distances[i].getSrcCode();
			String dest = distances[i].getDestCode();
			mDistance.put(code(src, dest), distances[i].getDistance());
			// System.out.println(module + "::mapData, mDistance.put(" +
			// code(src,dest) + "," + distances[i].getDistance() + ")");
		}
		for (int i = 0; i < travelTimes.length; i++) {
			String src = travelTimes[i].getSrcCode();
			String dest = travelTimes[i].getDestCode();
			mTravelTime.put(code(src, dest), travelTimes[i].getDistance());
			// System.out.println(module + "::mapData, mDistance.put(" +
			// code(src,dest) + "," + distances[i].getDistance() + ")");
		}
		items = new ArrayList<Item>();
		mItemCode2Index = new HashMap<String, Integer>();

		N = requests.length;
		M = vehicles.length + externalVehicles.length;
		System.out.println(module + "::mapData, requests = " + N
				+ ", vehicles = " + M);

		mRequest2PointIndices = new HashMap<Integer, HashSet<Integer>>();
		mItemIndex2RequestIndex = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < requests.length; i++) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				Item I = requests[i].getItems()[j];
				I.setOrderId(requests[i].getOrderID());
				int idx = items.size();// index of item
				mItemCode2Index.put(I.getCode(), idx);
				items.add(I);
				mItemIndex2RequestIndex.put(idx, i);
			}
		}
		/*
		 * dis = new double[2*N+2*M][2*N+2*M]; for(int i = 0; i <
		 * requests.length; i++){
		 * 
		 * String pi = requests[i].getPickupLocationCode(); String di =
		 * requests[i].getDeliveryLocationCode(); dis[i][i+N] =
		 * mDistance.get(code(pi,di)); dis[i+N][i] = mDistance.get(code(di,pi));
		 * for(int j = 0; j < requests.length; j++)if(i != j){ String pj =
		 * requests[j].getPickupLocationCode(); String dj =
		 * requests[j].getDeliveryLocationCode(); System.out.println(module +
		 * "::mapData, code pi = " + pi + ", pj = " + pj); dis[i][j] =
		 * mDistance.get(code(pi,pj)); dis[i][j+N] = mDistance.get(code(pi,dj));
		 * dis[i+N][j] = mDistance.get(code(di,pj)); dis[i+N][j+N] =
		 * mDistance.get(code(di,dj)); }
		 * 
		 * for(int j = 0; j < vehicles.length; j++){ String sj =
		 * vehicles[j].getStartLocationCode(); String tj =
		 * vehicles[j].getEndLocationCode(); int svj = 2*N+j;// start point of
		 * vehicle j int tvj = 2*N+j+M;// end point of vehicle j dis[svj][tvj] =
		 * mDistance.get(code(sj,tj)); dis[tvj][svj] =
		 * mDistance.get(code(tj,sj));
		 * 
		 * dis[i][svj] = mDistance.get(code(pi,sj)); dis[svj][i] =
		 * mDistance.get(code(sj,pi));
		 * 
		 * dis[i][tvj] = mDistance.get(code(pi,tj)); dis[tvj][i] =
		 * mDistance.get(code(tj,pi));
		 * 
		 * dis[i+N][svj] = mDistance.get(code(di,sj)); dis[svj][i+N] =
		 * mDistance.get(code(sj,di));
		 * 
		 * dis[i+N][tvj] = mDistance.get(code(di,tj)); dis[tvj][i+N] =
		 * mDistance.get(code(tj,di)); } }
		 */

		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		pickupPoints = new ArrayList<Point>();
		deliveryPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		N = 0;
		for (int i = 0; i < requests.length; i++)
			N += requests[i].getItems().length;
		mPoint2Index = new HashMap<Point, Integer>();
		mPoint2LocationCode = new HashMap<Point, String>();
		mPoint2Demand = new HashMap<Point, Double>();
		mPoint2Vehicle = new HashMap<Point, Vehicle>();
		mPoint2Request = new HashMap<Point, PickupDeliveryRequest>();
		mPoint2Type = new HashMap<Point, String>();
		mPoint2PossibleVehicles = new HashMap<Point, HashSet<Integer>>();
		// mPoint2ItemCode = new HashMap<Point, String>();
		mPoint2IndexItems = new HashMap<Point, Integer[]>();
		mItem2ExclusiveItems = new HashMap<String, HashSet<String>>();
		mVehicle2NotReachedLocations = new HashMap<String, HashSet<String>>();
		mPoint2IndexLoadedItems = new HashMap<Point, HashSet<Integer>>();

		pickup2DeliveryOfGood = new HashMap<Point, Point>();
		earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		lastestAllowedArrivalTime = new HashMap<Point, Integer>();
		serviceDuration = new HashMap<Point, Integer>();

		mPickupPoint2RequestIndex = new HashMap<Point, Integer>();
		int idxPoint = -1;
		for (int i = 0; i < requests.length; i++) {
			mRequest2PointIndices.put(i, new HashSet<Integer>());
			
			if (requests[i].getSplitDelivery() != null
					&& requests[i].getSplitDelivery().equals("Y")) {
				for (int j = 0; j < requests[i].getItems().length; j++) {
					requests[i].getItems()[j].setOrderId(requests[i]
							.getOrderID());

					idxPoint++;
					Point pickup = new Point(idxPoint);
					pickupPoints.add(pickup);
					mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
					mPickupPoint2RequestIndex.put(pickup, i);
					
					mPoint2Index.put(pickup, idxPoint);
					mPoint2LocationCode.put(pickup,
							requests[i].getPickupLocationCode());
					mPoint2Demand.put(pickup,
							requests[i].getItems()[j].getWeight());
					mPoint2Request.put(pickup, requests[i]);
					mPoint2Type.put(pickup, "P");
					mPoint2PossibleVehicles.put(pickup, new HashSet<Integer>());
					// mPoint2LoadedItems.put(pickup, new HashSet<String>());
					// mPoint2ItemCode.put(pickup,
					// requests[i].getItems()[j].getCode());
					Integer[] ite = new Integer[1];
					ite[0] = mItemCode2Index.get(requests[i].getItems()[j]
							.getCode());

					
					mPoint2IndexItems.put(pickup, ite);

					mItem2ExclusiveItems.put(
							requests[i].getItems()[j].getCode(),
							new HashSet<String>());

					Point delivery = new Point(idxPoint + N);
					deliveryPoints.add(delivery);
					mPoint2Index.put(delivery, idxPoint + N);
					mPoint2LocationCode.put(delivery,
							requests[i].getDeliveryLocationCode());
					mPoint2Demand.put(delivery,
							-requests[i].getItems()[j].getWeight());
					mPoint2Request.put(delivery, requests[i]);
					mPoint2Type.put(delivery, "D");
					mPoint2PossibleVehicles.put(delivery,
							new HashSet<Integer>());
					// mPoint2LoadedItems.put(delivery, new HashSet<String>());

					pickup2DeliveryOfGood.put(pickup, delivery);
					allPoints.add(pickup);
					allPoints.add(delivery);

					earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
							.dateTime2Int(requests[i].getEarlyPickupTime()));
					// serviceDuration.put(pickup, 1800);// load-unload is 30
					// minutes
					int pickupDuration = requests[i].getItems()[j]
							.getPickupDuration();
					// if (pickupDuration < requests[i].getPickupDuration())
					// pickupDuration = requests[i].getPickupDuration();
					// pickupDuration += requests[i].getFixLoadTime();

					serviceDuration.put(pickup, pickupDuration);

					lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
							.dateTime2Int(requests[i].getLatePickupTime()));
					earliestAllowedArrivalTime.put(delivery,
							(int) DateTimeUtils.dateTime2Int(requests[i]
									.getEarlyDeliveryTime()));
					// serviceDuration.put(delivery, 1800);// load-unload is 30
					// minutes
					int deliveryDuration = requests[i].getItems()[j]
							.getDeliveryDuration();
					// if (deliveryDuration < requests[i].getDeliveryDuration())
					// deliveryDuration = requests[i].getDeliveryDuration();
					deliveryDuration += requests[i].getFixUnloadTime();

					serviceDuration.put(delivery, deliveryDuration);

					lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
							.dateTime2Int(requests[i].getLateDeliveryTime()));
				}
			} else {
				idxPoint++;
				Point pickup = new Point(idxPoint);
				pickupPoints.add(pickup);
				
				mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
				mPickupPoint2RequestIndex.put(pickup, i);
				
				mPoint2Index.put(pickup, idxPoint);
				mPoint2LocationCode.put(pickup,
						requests[i].getPickupLocationCode());
				double demand = 0;
				int pickupDuration = 0;
				int deliveryDuration = 0;
				for (int j = 0; j < requests[i].getItems().length; j++) {
					demand = demand + requests[i].getItems()[j].getWeight();
					deliveryDuration = deliveryDuration
							+ requests[i].getItems()[j].getDeliveryDuration();
					pickupDuration = pickupDuration
							+ requests[i].getItems()[j].getPickupDuration();
				}
				// if (pickupDuration < requests[i].getPickupDuration())
				// pickupDuration = requests[i].getPickupDuration();
				// pickupDuration += requests[i].getFixLoadTime();

				// if (deliveryDuration < requests[i].getDeliveryDuration())
				// deliveryDuration = requests[i].getDeliveryDuration();
				// deliveryDuration += requests[i].getFixUnloadTime();

				mPoint2Demand.put(pickup, demand);
				mPoint2Request.put(pickup, requests[i]);
				mPoint2Type.put(pickup, "P");
				mPoint2PossibleVehicles.put(pickup, new HashSet<Integer>());
				// mPoint2LoadedItems.put(pickup, new HashSet<String>());

				// mPoint2ItemCode.put(pickup,
				// requests[i].getItems()[j].getCode());

				Integer[] L = new Integer[requests[i].getItems().length];
				for (int ii = 0; ii < requests[i].getItems().length; ii++)
					L[ii] = mItemCode2Index.get(requests[i].getItems()[ii]
							.getCode());
				mPoint2IndexItems.put(pickup, L);

				for (int j = 0; j < requests[i].getItems().length; j++)
					mItem2ExclusiveItems.put(
							requests[i].getItems()[j].getCode(),
							new HashSet<String>());

				Point delivery = new Point(idxPoint + N);
				deliveryPoints.add(delivery);
				mPoint2Index.put(delivery, idxPoint + N);
				mPoint2LocationCode.put(delivery,
						requests[i].getDeliveryLocationCode());
				mPoint2Demand.put(delivery, -demand);
				mPoint2Request.put(delivery, requests[i]);
				mPoint2Type.put(delivery, "D");
				mPoint2PossibleVehicles.put(delivery, new HashSet<Integer>());
				// mPoint2LoadedItems.put(delivery, new HashSet<String>());

				pickup2DeliveryOfGood.put(pickup, delivery);
				allPoints.add(pickup);
				allPoints.add(delivery);

				earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
						.dateTime2Int(requests[i].getEarlyPickupTime()));
				// serviceDuration.put(pickup, 1800);// load-unload is 30
				// minutes

				serviceDuration.put(pickup, pickupDuration);
				lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
						.dateTime2Int(requests[i].getLatePickupTime()));
				earliestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
						.dateTime2Int(requests[i].getEarlyDeliveryTime()));
				// serviceDuration.put(delivery, 1800);// load-unload is 30
				// minutes
				serviceDuration.put(delivery, deliveryDuration);
				lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
						.dateTime2Int(requests[i].getLateDeliveryTime()));

			}
		}
		cap = new double[M];
		for (int k = 0; k < M; k++) {
			Vehicle vh = null;
			if (k < vehicles.length)
				vh = vehicles[k];
			else
				vh = externalVehicles[k - vehicles.length];

			cap[k] = vh.getWeight();// vehicles[k].getWeight();

			Point s = new Point(2 * N + k);
			Point t = new Point(2 * N + M + k);
			mPoint2Index.put(s, s.ID);
			mPoint2Index.put(t, t.ID);
			startPoints.add(s);
			endPoints.add(t);
			mPoint2Type.put(s, "S");
			mPoint2Type.put(t, "T");
			
			mPoint2LocationCode.put(s, vh.getStartLocationCode());// vehicles[k].getStartLocationCode());
			mPoint2LocationCode.put(t, vh.getEndLocationCode());// vehicles[k].getEndLocationCode());
			mPoint2Demand.put(s, 0.0);
			mPoint2Demand.put(t, 0.0);
			mPoint2Vehicle.put(s, vh);// vehicles[k]);
			mPoint2Vehicle.put(t, vh);// vehicles[k]);
			// mVehicle2NotReachedLocations.put(vehicles[k].getCode(), new
			// HashSet<String>());
			mVehicle2NotReachedLocations.put(vh.getCode(),
					new HashSet<String>());

			allPoints.add(s);
			allPoints.add(t);

			// earliestAllowedArrivalTime.put(s,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			earliestAllowedArrivalTime.put(s,
					(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
			serviceDuration.put(s, 0);// load-unload is 30 minutes
			// lastestAllowedArrivalTime.put(s,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			lastestAllowedArrivalTime.put(s,
					(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));
			// earliestAllowedArrivalTime.put(t,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			earliestAllowedArrivalTime.put(t,
					(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
			serviceDuration.put(t, 0);// load-unload is 30 minutes
			// lastestAllowedArrivalTime.put(t,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			lastestAllowedArrivalTime.put(t,
					(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));
			
			//System.out.println("mapData, startWorkingTime = " + vh.getStartWorkingTime() + ", end working time = " + vh.getEndWorkingTime());
		}
		for (Point p : allPoints) {
			mPoint2IndexLoadedItems.put(p, new HashSet<Integer>());
		}
		itemConflict = new boolean[items.size()][items.size()];
		for (int ii = 0; ii < items.size(); ii++)
			for (int jj = 0; jj < items.size(); jj++)
				itemConflict[ii][jj] = false;

		ExclusiveItem[] exclusiveItems = input.getExclusiveItemPairs();
		for (int i = 0; i < exclusiveItems.length; i++) {
			String I1 = exclusiveItems[i].getCode1();
			String I2 = exclusiveItems[i].getCode2();
			if (mItem2ExclusiveItems.get(I1) == null)
				mItem2ExclusiveItems.put(I1, new HashSet<String>());
			if (mItem2ExclusiveItems.get(I2) == null)
				mItem2ExclusiveItems.put(I2, new HashSet<String>());

			mItem2ExclusiveItems.get(I1).add(I2);
			mItem2ExclusiveItems.get(I2).add(I1);

			if (mItemCode2Index.get(I1) != null
					&& mItemCode2Index.get(I2) != null) {
				itemConflict[mItemCode2Index.get(I1)][mItemCode2Index.get(I2)] = true;
				itemConflict[mItemCode2Index.get(I2)][mItemCode2Index.get(I1)] = true;
			}
		}

		ExclusiveVehicleLocation[] exclusiveVehicleLocations = input
				.getExclusiveVehicleLocations();
		for (int i = 0; i < exclusiveVehicleLocations.length; i++) {
			String vehicleCode = exclusiveVehicleLocations[i].getVehicleCode();
			String locationCode = exclusiveVehicleLocations[i]
					.getLocationCode();
			mVehicle2NotReachedLocations.get(vehicleCode).add(locationCode);
		}

		awm = new ArcWeightsManager(allPoints);
		nwm = new NodeWeightsManager(allPoints);
		travelTime = new ArcWeightsManager(allPoints);

		for (Point p : allPoints) {
			String lp = mPoint2LocationCode.get(p);
			for (Point q : allPoints) {
				String lq = mPoint2LocationCode.get(q);
				double d = mDistance.get(code(lp, lq));
				awm.setWeight(p, q, d);
				// travelTime.setWeight(p, q,
				// (d*1000)/input.getParams().getAverageSpeed());// meter per
				// second
				double t = mTravelTime.get(code(lp, lq));
				travelTime.setWeight(p, q, t);
			}
		}
		for (Point p : allPoints) {
			nwm.setWeight(p, mPoint2Demand.get(p));
			//System.out.println(module + "::compute, nwm.setWeight(" + p.ID
			//		+ "," + mPoint2Demand.get(p));
		}

		mPoint2ArrivalTime = new HashMap<Point, Integer>();
		mPoint2DepartureTime = new HashMap<Point, Integer>();

		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for (int k = 0; k < startPoints.size(); k++) {
			XR.addRoute(startPoints.get(k), endPoints.get(k));
		}
		for (Point p : pickupPoints)
			XR.addClientPoint(p);
		for (Point p : deliveryPoints)
			XR.addClientPoint(p);

		CS = new ConstraintSystemVR(mgr);
		awn = new AccumulatedWeightNodesVR(XR, nwm);
		awe = new AccumulatedWeightEdgesVR(XR, awm);
		//eat = new EarliestArrivalTimeVR(XR, travelTime,
		//		earliestAllowedArrivalTime, serviceDuration);
		//ceat = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
		//CS.post(ceat);

		cost = new TotalCostVR(XR, awm);

		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(cost);

		mgr.close();

		if (true)
			return;
		/*
		 * ArrayList<INeighborhoodExplorer> NE = new
		 * ArrayList<INeighborhoodExplorer>(); NE.add(new
		 * GreedyTwoPointsMoveExplorer(XR, F)); NE.add(new
		 * GreedyCrossExchangeMoveExplorer(XR, F)); // NE.add(new
		 * GreedyAddOnePointMoveExplorer(XR, F));
		 * 
		 * PickupDeliveryTWSearch se = new
		 * PickupDeliveryTWSearch(mgr,F,CS,cost,XR,pickupPoints,deliveryPoints);
		 * se.setNeighborhoodExplorer(NE); se.setObjectiveFunction(F);
		 * se.setMaxStable(50);
		 * 
		 * se.search(100000, input.getParams().getTimeLimit());
		 * 
		 * System.out.println("solution XR = " + XR.toString() + ", cost = " +
		 * cost.getValue()); for(int k = 1; k <= XR.getNbRoutes(); k++){
		 * System.out.println("Route[" + k + "]"); for(Point p =
		 * XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){ Point np =
		 * XR.next(p); double tt = travelTime.getWeight(p,np); long at =
		 * (long)eat.getEarliestArrivalTime(p); System.out.println("point " +
		 * p.getID() + ", demand = " + nwm.getWeight(p) + ", t2n = " + tt +
		 * ", eat = " + DateTimeUtils.unixTimeStamp2DateTime(at)); }
		 * //System.out.println(", load = " + load[k-1].getValue() + ", cap = "
		 * + cap[k-1]);
		 * System.out.println("------------------------------------"); }
		 */

	}

	protected HashSet<Integer> search() {
		HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraint();
		//HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraintFTL();
		
		System.out.println("solution XR = " + XR.toString() + ", cost = "
				+ cost.getValue());
		/*
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			System.out.println("Route[" + k + "]");
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				Point np = XR.next(p);
				double tt = travelTime.getWeight(p, np);
				long at = (long) eat.getEarliestArrivalTime(p);
				System.out.println("point " + p.getID() + ", demand = "
						+ nwm.getWeight(p) + ", t2n = " + tt + ", eat = "
						+ DateTimeUtils.unixTimeStamp2DateTime(at));
			}
			// System.out.println(", load = " + load[k-1].getValue() +
			// ", cap = " + cap[k-1]);
			System.out.println("------------------------------------");
		}
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
		if (idx < vehicles.length)
			return vehicles[idx];
		return externalVehicles[idx - vehicles.length];
	}

	public PickupDeliverySolution buildSolution(int[] scheduled_vehicle,
			HashSet<Integer> remainUnScheduled) {
		//scheduled_vehicle[k] is the index of vehicle assigned to route k (k = 0,1,...)
		
		int nbr = 0;
		for (int k = 1; k <= XR.getNbRoutes(); k++){
			if (XR.next(XR.startPoint(k)) != XR.endPoint(k)){
				nbr++;
				mPoint2Vehicle.put(XR.startPoint(k), getVehicle(scheduled_vehicle[k-1]));
				mPoint2Vehicle.put(XR.endPoint(k), getVehicle(scheduled_vehicle[k-1]));
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
						double e_load = awn.getSumWeights(p);
						double e_distance = awe.getCostRight(p);
						e.setLoad(e_load);
						e.setDistance(e_distance);
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
					} else {
						// int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						if (mPoint2Type.get(p).equals("P")) {
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
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
				Vehicle vh = null;
				
				if (k <= vehicles.length)
					vh = vehicles[k - 1];
				else
					vh = externalVehicles[k - vehicles.length - 1];
				*/
				Vehicle vh = getVehicle(scheduled_vehicle[k-1]);
				routes[nbr] = new RoutingSolution(vh, a_route, 0.0, distance);
			}
		}
		for (int i = 0; i < requests.length; i++) {
			PickupDeliveryRequest r = requests[i];
			System.out.println("req[" + i + "], pickup = " + r.getPickupLat()
					+ "," + r.getPickupLng() + ", delivery = "
					+ r.getDeliveryLat() + "," + r.getDeliveryLng());

		}

		ArrayList<Item> L = new ArrayList<Item>();
		for (int i : remainUnScheduled) {
			Point pickup = pickupPoints.get(i);
			String des = analyzeRequest(i);
			for (int j = 0; j < mPoint2IndexItems.get(pickup).length; j++) {
				Item I = items.get(mPoint2IndexItems.get(pickup)[j]);
				I.setDescription(des);
				L.add(I);
			}
		}

		Item[] unScheduledItems = new Item[L.size()];
		for (int i = 0; i < L.size(); i++) {
			unScheduledItems[i] = L.get(i);
		}

		return new PickupDeliverySolution(routes, unScheduledItems);

	}

	public PickupDeliverySolution compute(BrennTagPickupDeliveryInput input) {
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		this.travelTimes = input.getTravelTime();
		this.externalVehicles = input.getExternalVehicles();
		mapData();

		HashSet<Integer> remainUnScheduled = search();
		int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		for(int k = 0; k < XR.getNbRoutes(); k++){
			scheduled_vehicle[k] = k;
		}
		PickupDeliverySolution sol = buildSolution(scheduled_vehicle,remainUnScheduled);

		return sol;
	}
	/*
	 * // model protected VRManager mgr; protected VarRoutesVR XR; protected
	 * ArrayList<Point> starts; protected ArrayList<Point> ends; protected
	 * ArrayList<Point> pickup; protected ArrayList<Point> delivery; protected
	 * ArrayList<Point> allPoints; protected ArrayList<Point> clientPoints;
	 * protected ArcWeightsManager awm; protected HashMap<Point, String>
	 * mPoint2GeoPoint; protected HashMap<String, Double> mCode2Distance;
	 * protected HashMap<PickupDeliveryRequest, Point> mReq2PickupPoint;
	 * protected HashMap<PickupDeliveryRequest, Point> mReq2DeliveryPoint;
	 * protected HashMap<Point, PickupDeliveryRequest> mPoint2Request; protected
	 * HashMap<Point, String> mPoint2LatLng; protected HashMap<Point, String>
	 * mPoint2Code;
	 * 
	 * protected IFunctionVR obj;
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
