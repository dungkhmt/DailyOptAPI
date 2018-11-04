package routingdelivery.smartlog.brenntag.service;

import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.google.gson.Gson;

import algorithms.matching.MaxMatching;
import algorithms.matching.WeightedMaxMatching;
import algorithms.tsp.branchandbound.BBTSP;
import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import routingdelivery.service.PickupDeliverySolver;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ClusterItems;
import routingdelivery.smartlog.brenntag.model.ExclusiveItem;
import routingdelivery.smartlog.brenntag.model.ExclusiveVehicleLocation;
import routingdelivery.smartlog.brenntag.model.GreedyMatchingVehicleTrip;
import routingdelivery.smartlog.brenntag.model.InputIndicator;
import routingdelivery.smartlog.brenntag.model.LocationConfig;
import routingdelivery.smartlog.brenntag.model.ModelRoute;
import routingdelivery.smartlog.brenntag.model.MoveMergeTrip;
import routingdelivery.smartlog.brenntag.model.PairInt;
import routingdelivery.smartlog.brenntag.model.VehicleTrip;
import routingdelivery.smartlog.brenntag.model.VehicleTripCollection;
import routingdelivery.utils.assignment.OptimizeLoadTruckAssignment;
import utils.DateTimeUtils;

public class BrenntagPickupDeliverySolver extends PickupDeliverySolver {

	public HashMap<String, HashSet<Integer>> mLocationCode2RequestIndex;

	public HashMap<String, String> mItemCode2OrderID;

	public ArrayList<Trip>[] trips; // trips[k] is the list of trip of kth
									// vehicle (k = 0,1,...)
	public ArrayList<String> locationCodes;
	public HashMap<Trip, Item[]> mTrip2Items;
	public HashMap<Trip, ArrayList<Integer>> mTrip2PickupPointIndices;
	// public HashMap<Trip, Point> mTrip2DeliveryPoint;

	public HashMap<Integer, Double> mVehicle2Distance;
	public HashMap<Integer, String> mVehicle2OriginStartWoringTime;
	public HashMap<Integer, Integer> mPickupIndex2ScheduledVehicleIndex;

	public ArrayList<String> distinct_pickupLocationCodes = new ArrayList<String>();
	public ArrayList<String> distinct_deliveryLocationCodes = new ArrayList<String>();
	public ArrayList<HashSet<Integer>> distinct_request_indices = new ArrayList<HashSet<Integer>>();

	public HashSet<Integer> unScheduledPointIndices;

	public Trip[][] matchTrips;// matchTrips[i][j] = trip that vehicle i carries
								// cluster-item j
	public ArrayList<ClusterItems> clusterItems;
	public HashMap<ClusterItems, Integer> mCluster2Index;

	public int getLastDepartureTimeOfVehicle(int vh) {
		String debugVehicleCode = "";
		if (trips[vh].size() > 0) {
			Trip last_trip = trips[vh].get(trips[vh].size() - 1);
			if (getVehicle(vh).getCode().equals(debugVehicleCode)) {
				log(name()
						+ "::getLastDepartureTimeOfVehicle, vehicle["
						+ vh
						+ "], "
						+ getVehicle(vh).getCode()
						+ ", sz = "
						+ trips[vh].size()
						+ ", lastDepartureTime = "
						+ last_trip.end.departureTime
						+ ", HMS = "
						+ DateTimeUtils
								.unixTimeStamp2DateTime(last_trip.end.departureTime));
			}
			return last_trip.end.departureTime;
		} else {
			Vehicle v = getVehicle(vh);//null;
			//if (vh >= vehicles.length)
			//	v = externalVehicles[vh - vehicles.length];
			//else
			//	v = vehicles[vh];
			return (int) DateTimeUtils.dateTime2Int(v.getStartWorkingTime());
		}
	}

	public void mapLocation2Type() {
		mLocation2Type = new HashMap<String, String>();
		for (String lc : locationCodes) {
			mLocation2Type.put(lc, NGOAI_THANH);
		}
		if (input.getExclusiveVehicleLocations() != null)
			for (int i = 0; i < input.getExclusiveVehicleLocations().length; i++) {
				ExclusiveVehicleLocation evl = input
						.getExclusiveVehicleLocations()[i];
				String lc = evl.getLocationCode();
				mLocation2Type.put(lc, NOI_THANH);
			}
		if (input.getExclusiveVehicleCategoryLocations() != null)
			for (int i = 0; i < input.getExclusiveVehicleCategoryLocations().length; i++) {
				ExclusiveVehicleLocation evl = input
						.getExclusiveVehicleCategoryLocations()[i];
				String lc = evl.getLocationCode();
				mLocation2Type.put(lc, NOI_THANH);
			}
	}

	public String getOrderIDAtPoint(Point pickupPoint) {
		String s = "";
		if (mPoint2Request.get(pickupPoint) != null)
			for (PickupDeliveryRequest r : mPoint2Request.get(pickupPoint)) {
				s = s + r.getOrderID() + "-";
			}
		return s;
	}

	public int getLastLocationIndex(int vh) {
		if (trips[vh].size() > 0) {
			Trip last_trip = trips[vh].get(trips[vh].size() - 1);
			return last_trip.end.locationIndex;
		} else {
			Vehicle v = getVehicle(vh);//null;
			//if (vh >= vehicles.length)
			//	v = externalVehicles[vh - vehicles.length];
			//else
			//	v = vehicles[vh];
			return mLocationCode2Index.get(v.getStartLocationCode());
		}
	}

	public boolean checkFTLSatisfyingDeliveryTime(Vehicle vh,
			int arrivalTimePickup, double amount, int pickupLocationIdx,
			int deliveryLocationIdx, int pickupDurationPerUnit,
			int deliveryDurationPerUnit, int fix_load_time,
			int fix_unload_time, int ealiestAllowArrivalTimePickup,
			int latestAllowArrivalTimePickup,
			int earliestAllowArrivalTimeDelivery,
			int latestAllowArrivalTimeDelivery, boolean useDurationPerUnit) {

		double cap = vh.getWeight();
		int serviceTimePickup = arrivalTimePickup < ealiestAllowArrivalTimePickup ? ealiestAllowArrivalTimePickup
				: arrivalTimePickup;
		double factor = 1;

		int pickupDuration = (int) (pickupDurationPerUnit * cap * factor / amount)
				+ fix_load_time;
		int departureTimePickup = (int) serviceTimePickup + pickupDuration;

		int arrivalTimeDelivery = departureTimePickup
				+ (int) a_travelTime[pickupLocationIdx][deliveryLocationIdx];
		int serviceTimeDelivery = arrivalTimeDelivery < earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery
				: arrivalTimeDelivery;
		int deliveryDuration = (int) (deliveryDurationPerUnit * cap * factor / amount)
				+ fix_unload_time;
		int departureTimeDelivery = serviceTimeDelivery + deliveryDuration;

		if (arrivalTimeDelivery >= latestAllowArrivalTimeDelivery)
			return false;

		// NEED TO CHECK DEPARTURE-TIME-DELIVERY???
		int end_working_time = (int) DateTimeUtils.dateTime2Int(vh
				.getEndWorkingTime());
		int depotLocationCodeIdx = mLocationCode2Index.get(vh
				.getEndLocationCode());
		int arrivalTimeDepot = departureTimeDelivery
				+ (int) a_travelTime[deliveryLocationIdx][depotLocationCodeIdx];
		if (arrivalTimeDepot > end_working_time)
			return false;

		return true;

	}

	public Trip splitLargeItemCreateATripWithInternalVehicle(int itemIndex,
			double amount, int pickupLocationIdx, int deliveryLocationIdx,
			int pickupDurationPerUnit, int deliveryDurationPerUnit,
			int fix_load_time, int fix_unload_time,
			int ealiestAllowArrivalTimePickup,
			int latestAllowArrivalTimePickup,
			int earliestAllowArrivalTimeDelivery,
			int latestAllowArrivalTimeDelivery, boolean useDurationPerUnit) {
		double max_cap = 0;
		int sel_vehicle = -1;
		int sel_locationIdx = -1;
		int arrivalTimePickup = -1;
		int departureTimePickup = -1;
		String debugVehicleCode = "";
		int nbIntVehicles = computeInternalVehicles();
		for (int i = 0; i < nbIntVehicles; i++) {
			Vehicle vh = getVehicle(i);
			String pickupLocationCode = locationCodes.get(pickupLocationIdx);
			String deliveryLocationCode = locationCodes
					.get(deliveryLocationIdx);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
					pickupLocationCode)
					|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
							deliveryLocationCode))
				continue;

			int locationIdx = getLastLocationIndex(i);
			int deptime = getLastDepartureTimeOfVehicle(i);
			if (getVehicle(i).getCode().equals(debugVehicleCode)) {
				log(name()
						+ "::splitLargeItemCreateATripWithInternalVehicle, trips["
						+ i + "," + getVehicle(i).getCode() + "].sz = "
						+ trips[i].size() + ", deptime = "
						+ DateTimeUtils.unixTimeStamp2DateTime(deptime));
			}

			if (deptime + a_travelTime[locationIdx][pickupLocationIdx] <= latestAllowArrivalTimePickup) {
				if (amount <= vehicles[i].getWeight()) {
					// has available vehicle with enough capacity
					arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);

					if (checkFTLSatisfyingDeliveryTime(vehicles[i],
							arrivalTimePickup, amount, pickupLocationIdx,
							deliveryLocationIdx, pickupDurationPerUnit,
							deliveryDurationPerUnit, fix_load_time,
							fix_unload_time, ealiestAllowArrivalTimePickup,
							latestAllowArrivalTimePickup,
							earliestAllowArrivalTimeDelivery,
							latestAllowArrivalTimeDelivery, useDurationPerUnit))

						return null;
				} else {
					// check if vehicle i can load FTL satisfying deliverying
					// time
					arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);

					if (!checkFTLSatisfyingDeliveryTime(vehicles[i],
							arrivalTimePickup, amount, pickupLocationIdx,
							deliveryLocationIdx, pickupDurationPerUnit,
							deliveryDurationPerUnit, fix_load_time,
							fix_unload_time, ealiestAllowArrivalTimePickup,
							latestAllowArrivalTimePickup,
							earliestAllowArrivalTimeDelivery,
							latestAllowArrivalTimeDelivery, useDurationPerUnit))
						continue;

					if (max_cap < vehicles[i].getWeight()) {
						sel_vehicle = i;
						sel_locationIdx = locationIdx;
						max_cap = vehicles[i].getWeight();
						arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);
					}
				}
			}
		}
		if (sel_vehicle == -1)
			return null;

		Vehicle vh = getVehicle(sel_vehicle);
		int deptime = getLastDepartureTimeOfVehicle(sel_vehicle);
		arrivalTimePickup = (int) (deptime + a_travelTime[sel_locationIdx][pickupLocationIdx]);

		ArrayList<ItemAmount> L = new ArrayList<ItemAmount>();
		L.add(new ItemAmount(itemIndex, max_cap));
		int serviceTimePickup = arrivalTimePickup < ealiestAllowArrivalTimePickup ? ealiestAllowArrivalTimePickup
				: arrivalTimePickup;
		double factor = 1;
		if (useDurationPerUnit) {
			factor = vehicles[sel_vehicle].getWeight();
		}

		int pickupDuration = (int) (pickupDurationPerUnit * max_cap * factor / amount)
				+ fix_load_time;
		departureTimePickup = (int) serviceTimePickup + pickupDuration;
		String des_pickup = "pickupDurationTotal = " + pickupDurationPerUnit
				+ ", fix_load_time = " + fix_load_time + ", factor = " + factor
				+ ", max_cap = " + max_cap + ", totalAmount = " + amount
				+ ", splitPickupDuration = " + pickupDuration + "("
				+ DateTimeUtils.second2HMS(pickupDuration) + ")";

		int arrivalTimeDelivery = departureTimePickup
				+ (int) a_travelTime[pickupLocationIdx][deliveryLocationIdx];
		int serviceTimeDelivery = arrivalTimeDelivery < earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery
				: arrivalTimeDelivery;
		int deliveryDuration = (int) (deliveryDurationPerUnit * max_cap
				* factor / amount)
				+ fix_unload_time;
		int departureTimeDelivery = serviceTimeDelivery + deliveryDuration;
		String des_delivery = "deliveryDurationTotal = "
				+ deliveryDurationPerUnit + ", fix_unload_time = "
				+ fix_unload_time + ", factor = " + factor + ", max_cap = "
				+ max_cap + ", totalAmount = " + amount
				+ ", splitDeliveryDuration = " + deliveryDuration + "("
				+ DateTimeUtils.second2HMS(deliveryDuration) + ")";

		RouteNode start = new RouteNode(pickupLocationIdx, arrivalTimePickup,
				departureTimePickup, L, sel_vehicle, "P");// pickup
		RouteNode end = new RouteNode(deliveryLocationIdx, arrivalTimeDelivery,
				departureTimeDelivery, L, sel_vehicle, "D");// delivery

		start.solver = this;
		start.description = des_pickup;
		end.solver = this;
		end.description = des_delivery;
		Trip tr = new Trip(start, end, "FTL");

		if (vh.getCode().equals(debugVehicleCode)) {
			log(name()
					+ "::::splitLargeItemCreateATripWithInternalVehicle, vehicle["
					+ sel_vehicle
					+ "] = "
					+ vh.getCode()
					+ ", departureTime from last location = "
					+ deptime
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(deptime)
					+ ")"
					+ ", arrivalTimePickup = "
					+ arrivalTimePickup
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTimePickup)
					+ ")"
					+ ", pickupDuration = "
					+ pickupDuration
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(pickupDuration)
					+ ")"
					+ ", departureTimePickup = "
					+ departureTimePickup
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(departureTimePickup)
					+ ")"
					+ ", arrivalTimeDelivery = "
					+ arrivalTimeDelivery
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeDelivery)
					+ ")"
					+ ", deliveryDuration = "
					+ deliveryDuration
					+ " ("
					+ DateTimeUtils.unixTimeStamp2DateTime(deliveryDuration)
					+ ")"
					+ ", departureTimePickup = "
					+ departureTimeDelivery
					+ " ("
					+ DateTimeUtils
							.unixTimeStamp2DateTime(departureTimeDelivery)
					+ ")"

			);
		}
		// log(name()
		// +
		// "::splitLargeItemCreateATripWithInternalVehicle, ACCEPT for vehicle["
		// + start.vehicleIndex + "], "
		// + getVehicle(start.vehicleIndex).getCode() + ", splitted trip "
		// + tr.toString());

		return tr;

	}

	public Trip splitLargeItemCreateATripWithExternalVehicle(int itemIndex,
			double amount, int pickupLocationIdx, int deliveryLocationIdx,
			int pickupDurationPerUnit, int deliveryDurationPerUnit,
			int fix_load_time, int fix_unload_time,
			int ealiestAllowArrivalTimePickup,
			int latestAllowArrivalTimePickup,
			int earliestAllowArrivalTimeDelivery,
			int latestAllowArrivalTimeDelivery, boolean useDurationPerUnit) {
		// log(name() + "::splitLargeItemCreateATripWithExternalVehicle"
		// + ", externalVehicles = " + externalVehicles.length);
		if (externalVehicles == null || externalVehicles.length == 0)
			return null;

		double max_cap = 0;
		int sel_vehicle = -1;
		int sel_locationIdx = -1;
		int arrivalTimePickup = -1;
		int departureTimePickup = -1;
		int rID = mItemIndex2RequestIndex.get(itemIndex);
		PickupDeliveryRequest r = requests[rID];
		Item I = items.get(itemIndex);

		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		for (int i = 0; i < nbExtVehicles; i++) {
			int vehicleIdx = i + nbIntVehicles;
			Vehicle vh = getVehicle(vehicleIdx);
			String pickupLocationCode = locationCodes.get(pickupLocationIdx);
			String deliveryLocationCode = locationCodes
					.get(deliveryLocationIdx);
			if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
					pickupLocationCode)
					|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
							deliveryLocationCode))
				continue;

			int locationIdx = getLastLocationIndex(vehicleIdx);
			int deptime = getLastDepartureTimeOfVehicle(vehicleIdx);
			if (deptime + a_travelTime[locationIdx][pickupLocationIdx] <= latestAllowArrivalTimePickup) {
				if (amount <= externalVehicles[i].getWeight()) {
					// has available vehicle with enough capacity
					arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);

					if (checkFTLSatisfyingDeliveryTime(externalVehicles[i],
							arrivalTimePickup, amount, pickupLocationIdx,
							deliveryLocationIdx, pickupDurationPerUnit,
							deliveryDurationPerUnit, fix_load_time,
							fix_unload_time, ealiestAllowArrivalTimePickup,
							latestAllowArrivalTimePickup,
							earliestAllowArrivalTimeDelivery,
							latestAllowArrivalTimeDelivery, useDurationPerUnit)) {

						// log(name()
						// +
						// "::splitLargeItemCreateATripWithExternalVehicle, DONOT SPLIT large item "
						// + I.getCode() + ", amount = " + amount
						// + ", order = " + r.getOrderID()
						// + " BECAUSE AVAILABLE vehicle "
						// + externalVehicles[i].getCode() + ", "
						// + externalVehicles[i].getWeight()
						// + ", external-vehicle = "
						// + externalVehicles.length);
						return null;
					}
				} else {
					// check if vehicle i can load FTL satisfying deliverying
					// time
					arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);

					if (!checkFTLSatisfyingDeliveryTime(externalVehicles[i],
							arrivalTimePickup, amount, pickupLocationIdx,
							deliveryLocationIdx, pickupDurationPerUnit,
							deliveryDurationPerUnit, fix_load_time,
							fix_unload_time, ealiestAllowArrivalTimePickup,
							latestAllowArrivalTimePickup,
							earliestAllowArrivalTimeDelivery,
							latestAllowArrivalTimeDelivery, useDurationPerUnit))
						continue;

					if (max_cap < externalVehicles[i].getWeight()) {
						sel_vehicle = vehicleIdx;// i;
						sel_locationIdx = locationIdx;
						max_cap = externalVehicles[i].getWeight();
						arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);
					}
				}
			}
		}
		if (sel_vehicle == -1) {
			// log(name()
			// +
			// "::splitLargeItemCreateATripWithExternalVehicle, CANNOT SPLIT large item "
			// + I.getCode() + ", amount = " + amount + ", order = "
			// // + r.getOrderID()
			// + " BECAUSE NO AVAILABLE vehicle, external-vehicle = "
			// + externalVehicles.length);
			return null;
		} else {
			// log(name()
			// +
			// "::splitLargeItemCreateATripWithExternalVehicle, CAN SPLIT large item "
			// + I.getCode() + ", amount = " + amount + ", order = "
			// + r.getOrderID() + " WITH vehicle "
			// + externalVehicles[sel_vehicle].getCode() + ", "
			// + externalVehicles[sel_vehicle].getWeight()
			// + ", external-vehicle = " + externalVehicles.length);

		}
		Vehicle vh = getVehicle(sel_vehicle);
		int deptime = getLastDepartureTimeOfVehicle(sel_vehicle);
		arrivalTimePickup = (int) (deptime + a_travelTime[sel_locationIdx][pickupLocationIdx]);

		ArrayList<ItemAmount> L = new ArrayList<ItemAmount>();
		L.add(new ItemAmount(itemIndex, max_cap));
		int serviceTimePickup = arrivalTimePickup < ealiestAllowArrivalTimePickup ? ealiestAllowArrivalTimePickup
				: arrivalTimePickup;
		double factor = 1;
		if (useDurationPerUnit) {
			factor = externalVehicles[sel_vehicle].getWeight();
		}

		int pickupDuration = (int) (pickupDurationPerUnit * max_cap * factor / amount)
				+ fix_load_time;
		departureTimePickup = (int) serviceTimePickup + pickupDuration;
		String des_pickup = "pickupDurationTotal = " + pickupDurationPerUnit
				+ ", fix_load_time = " + fix_load_time + ", factor = " + factor
				+ ", max_cap = " + max_cap + ", totalAmount = " + amount
				+ ", splitPickupDuration = " + pickupDuration + "("
				+ DateTimeUtils.second2HMS(pickupDuration) + ")";

		int arrivalTimeDelivery = departureTimePickup
				+ (int) a_travelTime[pickupLocationIdx][deliveryLocationIdx];
		int serviceTimeDelivery = arrivalTimeDelivery < earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery
				: arrivalTimeDelivery;
		int deliveryDuration = (int) (deliveryDurationPerUnit * max_cap
				* factor / amount)
				+ fix_unload_time;
		int departureTimeDelivery = serviceTimeDelivery + deliveryDuration;
		String des_delivery = "deliveryDurationTotal = "
				+ deliveryDurationPerUnit + ", fix_unload_time = "
				+ fix_unload_time + ", factor = " + factor + ", max_cap = "
				+ max_cap + ", totalAmount = " + amount
				+ ", splitDeliveryDuration = " + deliveryDuration + "("
				+ DateTimeUtils.second2HMS(deliveryDuration) + ")";

		RouteNode start = new RouteNode(pickupLocationIdx, arrivalTimePickup,
		// departureTimePickup, L, sel_vehicle + vehicles.length, "P");// pickup
				departureTimePickup, L, sel_vehicle, "P");// pickup
		RouteNode end = new RouteNode(deliveryLocationIdx, arrivalTimeDelivery,
		// departureTimeDelivery, L, sel_vehicle + vehicles.length, "D");//
		// delivery
				departureTimeDelivery, L, sel_vehicle, "D");// delivery

		start.solver = this;
		start.description = des_pickup;
		end.solver = this;
		end.description = des_delivery;

		return new Trip(start, end, "FTL");
	}

	/*
	 * public Trip splitLargeItemCreateATripWithExternalVehicle(int itemIndex,
	 * double amount, int pickupLocationIdx, int deliveryLocationIdx, int
	 * pickupDurationPerUnit, int deliveryDurationPerUnit, int
	 * ealiestAllowArrivalTimePickup, int latestAllowArrivalTimePickup, int
	 * earliestAllowArrivalTimeDelivery, int latestAllowArrivalTimeDelivery,
	 * boolean useDurationPerUnit) { double max_cap = 0; int sel_vehicle = -1;
	 * int arrivalTimePickup = -1; int departureTimePickup = -1; for (int i = 0;
	 * i < externalVehicles.length; i++) { int locationIdx =
	 * getLastLocationIndex(i); int deptime = getLastDepartureTimeOfVehicle(i);
	 * if (deptime + a_travelTime[locationIdx][pickupLocationIdx] <=
	 * latestAllowArrivalTimePickup) { if (amount <=
	 * externalVehicles[i].getWeight()) // has available vehicle with enough
	 * capacity return null; else { if (max_cap <
	 * externalVehicles[i].getWeight()) { sel_vehicle = i + vehicles.length;
	 * max_cap = externalVehicles[i].getWeight(); arrivalTimePickup = (int)
	 * (deptime + a_travelTime[locationIdx][pickupLocationIdx]); } } } } if
	 * (sel_vehicle == -1) return null;
	 * 
	 * ArrayList<ItemAmount> L = new ArrayList<ItemAmount>(); L.add(new
	 * ItemAmount(itemIndex, (int) max_cap)); int serviceTimePickup =
	 * arrivalTimePickup < ealiestAllowArrivalTimePickup ?
	 * ealiestAllowArrivalTimePickup : arrivalTimePickup; double factor = 1; if
	 * (useDurationPerUnit) factor = vehicles[sel_vehicle].getWeight();
	 * 
	 * departureTimePickup = (int) serviceTimePickup + (int)
	 * (pickupDurationPerUnit * factor);
	 * 
	 * int arrivalTimeDelivery = departureTimePickup + (int)
	 * a_travelTime[pickupLocationIdx][deliveryLocationIdx]; int
	 * serviceTimeDelivery = arrivalTimeDelivery <
	 * earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery :
	 * arrivalTimeDelivery; int departureTimeDelivery = serviceTimeDelivery +
	 * (int) (deliveryDurationPerUnit * factor);
	 * 
	 * RouteNode start = new RouteNode(pickupLocationIdx, arrivalTimePickup,
	 * departureTimePickup, L, sel_vehicle, "P");// pickup RouteNode end = new
	 * RouteNode(deliveryLocationIdx, arrivalTimeDelivery,
	 * departureTimeDelivery, L, sel_vehicle, "D");// delivery
	 * 
	 * return new Trip(start, end, "FTL"); }
	 */

	public void processSplitOrderItemWithInternalVehicle(Item item) {
		// log(name() +
		// "::processSplitOrderItemWithInternalVehicle, START Item "
		// + item.getCode() + ", weight = " + item.getWeight());
		int itemIndex = mItem2Index.get(item);
		int reqIndex = mItemIndex2RequestIndex.get(itemIndex);
		PickupDeliveryRequest r = requests[reqIndex];
		int pickupLocationIdx = mLocationCode2Index.get(r
				.getPickupLocationCode());
		int deliveryLocationIdx = mLocationCode2Index.get(r
				.getDeliveryLocationCode());
		int earliesAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyPickupTime());
		int earliestAllowArrivalTimeDelivery = (int) DateTimeUtils
				.dateTime2Int(r.getEarlyDeliveryTime());
		int latestAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getLatePickupTime());
		int latestAllowArrivalTimeDelivery = (int) DateTimeUtils.dateTime2Int(r
				.getLateDeliveryTime());

		double ori_weight = item.getWeight();
		int ori_pickup_duration = item.getPickupDuration();
		int ori_delivery_duration = item.getDeliveryDuration();
		double amount = ori_weight;

		while (true) {
			// ItemAmount ia = IA.get(i);
			// int pickupDuration = //items[i].getPickupDuration();
			// int deliveryDuration = //items[i].getDeliveryDuration();
			Trip a = splitLargeItemCreateATripWithInternalVehicle(itemIndex,
					amount, pickupLocationIdx, deliveryLocationIdx,
					ori_pickup_duration, ori_delivery_duration,
					r.getFixLoadTime(), r.getFixUnloadTime(),
					earliesAllowArrivalTimePickup,
					latestAllowArrivalTimePickup,
					earliestAllowArrivalTimeDelivery,
					latestAllowArrivalTimeDelivery, false);
			if (a == null) {
				// System.out.println("TRY SPLIT item " + ia.code +
				// ", amount = " + ia.amount);
				break;
			}
			double cap = vehicles[a.start.vehicleIndex].getWeight();
			int ftl_pickup_duration = (int) (ori_pickup_duration * cap / amount);
			int ftl_delivery_duration = (int) (ori_delivery_duration * cap / amount);
			ori_pickup_duration = ori_pickup_duration - ftl_pickup_duration;// (ori_pickup_duration*(ia.amount-cap))/(ia.amount);
			ori_delivery_duration = ori_delivery_duration
					- ftl_delivery_duration;// (ori_delivery_duration*(ia.amount-cap))/(ia.amount);
			amount = amount - cap;

			item.setWeight(amount);
			item.setPickupDuration(ori_pickup_duration);
			item.setDeliveryDuration(ori_delivery_duration);

			// System.out
			// .println(name()
			// + "::processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
			// + a.toString() + ", remain amount = "
			// + item.getWeight());

			// if (log != null) {
			// log.println(name()
			// + "::processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
			// + a.toString() + ", remain amount = "
			// + item.getWeight() + ", REMAIN totalItemWeight"
			// + getTotalItemWeight());
			// }
			Item[] ci = new Item[1];
			ci[0] = item.clone();
			ci[0].setWeight(cap);
			ci[0].setPickupDuration(ftl_pickup_duration);
			ci[0].setDeliveryDuration(ftl_delivery_duration);

			a.start.solver = this;
			a.end.solver = this;
			mTrip2Items.put(a, ci);
			Vehicle vh = getVehicle(a.start.vehicleIndex);
			// if(vh.getCode().equals("51C-632.74")){
			// log(name()
			// + "::processSplitOrderItemWithInternalVehicle, ADD 2 trip["
			// + a.start.vehicleIndex + "], the trip " + a.toString());

			// }
			trips[a.start.vehicleIndex].add(a);
			// if
			// (getVehicle(a.start.vehicleIndex).getCode().equals("51C-632.74"))
			// log(name()
			// + "::processSplitOrderItemWithInternalVehicle, trips["
			// + a.start.vehicleIndex + "].add, sz = "
			// + trips[a.start.vehicleIndex].size());
		}

	}

	public double computeItemWeightOnTrips() {
		double W = 0;
		int len = 0;
		if (input.getExternalVehicles() != null)
			len = input.getExternalVehicles().length;
		for (int i = 0; i < input.getVehicles().length
		// + input.getExternalVehicles().length; i++) {
				+ len; i++) {
			if (trips[i].size() > 0) {
				for (int j = 0; j < trips[i].size(); j++) {
					Trip t = trips[i].get(j);
					W += t.computeTotalItemWeight();
				}
			}
		}
		return W;
	}

	public void processSplitOrderItemWithExternalVehicle(Item item) {
		int itemIndex = mItem2Index.get(item);
		int reqIndex = mItemIndex2RequestIndex.get(itemIndex);
		PickupDeliveryRequest r = requests[reqIndex];
		int pickupLocationIdx = mLocationCode2Index.get(r
				.getPickupLocationCode());
		int deliveryLocationIdx = mLocationCode2Index.get(r
				.getDeliveryLocationCode());
		int earliesAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyPickupTime());
		int earliestAllowArrivalTimeDelivery = (int) DateTimeUtils
				.dateTime2Int(r.getEarlyDeliveryTime());
		int latestAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getLatePickupTime());
		int latestAllowArrivalTimeDelivery = (int) DateTimeUtils.dateTime2Int(r
				.getLateDeliveryTime());

		double ori_weight = item.getWeight();
		int ori_pickup_duration = item.getPickupDuration();
		int ori_delivery_duration = item.getDeliveryDuration();
		double amount = ori_weight;

		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		while (true) {
			// ItemAmount ia = IA.get(i);
			// int pickupDuration = //items[i].getPickupDuration();
			// int deliveryDuration = //items[i].getDeliveryDuration();
			Trip a = splitLargeItemCreateATripWithExternalVehicle(itemIndex,
					amount, pickupLocationIdx, deliveryLocationIdx,
					ori_pickup_duration, ori_delivery_duration,
					r.getFixLoadTime(), r.getFixUnloadTime(),
					earliesAllowArrivalTimePickup,
					latestAllowArrivalTimePickup,
					earliestAllowArrivalTimeDelivery,
					latestAllowArrivalTimeDelivery, false);
			if (a == null) {
				// System.out.println("TRY SPLIT item " + ia.code +
				// ", amount = " + ia.amount);
				break;
			}
			double cap = externalVehicles[a.start.vehicleIndex
					- nbIntVehicles].getWeight();
			int ftl_pickup_duration = (int) (ori_pickup_duration * cap / amount);
			int ftl_delivery_duration = (int) (ori_delivery_duration * cap / amount);
			ori_pickup_duration = ori_pickup_duration - ftl_pickup_duration;// (ori_pickup_duration*(ia.amount-cap))/(ia.amount);
			ori_delivery_duration = ori_delivery_duration
					- ftl_delivery_duration;// (ori_delivery_duration*(ia.amount-cap))/(ia.amount);
			amount = amount - cap;

			item.setWeight(amount);
			item.setPickupDuration(ori_pickup_duration);
			item.setDeliveryDuration(ori_delivery_duration);

			// System.out
			// .println("processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
			// + a.toString()
			// + ", remain amount = "
			// + item.getWeight());

			if (log != null) {
				log.println(name()
						+ "::processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
						+ a.toString() + ", remain amount = "
						+ item.getWeight() + ", REMAIN totalItemWeight"
						+ getTotalItemWeight());
			}

			Item[] ci = new Item[1];
			ci[0] = item.clone();
			ci[0].setWeight(cap);
			ci[0].setPickupDuration(ftl_pickup_duration);
			ci[0].setDeliveryDuration(ftl_delivery_duration);

			a.start.solver = this;
			a.end.solver = this;
			mTrip2Items.put(a, ci);
			Vehicle vh = getVehicle(a.start.vehicleIndex);
			if (vh.getCode().equals("51C-632.74")) {
				log(name()
						+ "::processSplitOrderItemWithInternalVehicle, ADD 2 trip["
						+ a.start.vehicleIndex + "], the trip " + a.toString());

			}
			trips[a.start.vehicleIndex].add(a);
		}

	}

	public void processSplitAnOrderWithInternalVehicle(PickupDeliveryRequest r) {
		Item[] items = r.getItems();
		int pickupLocationIdx = mLocationCode2Index.get(r
				.getPickupLocationCode());
		int deliveryLocationIdx = mLocationCode2Index.get(r
				.getDeliveryLocationCode());
		int earliesAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyPickupTime());
		int earliestAllowArrivalTimeDelivery = (int) DateTimeUtils
				.dateTime2Int(r.getEarlyDeliveryTime());
		int latestAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getLatePickupTime());
		int latestAllowArrivalTimeDelivery = (int) DateTimeUtils.dateTime2Int(r
				.getLateDeliveryTime());

		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		for (int i = 0; i < items.length; i++) {
			int itemIndex = mItem2Index.get(items[i]);
			IA.add(new ItemAmount(itemIndex, (int) items[i].getWeight()));
		}

		for (int i = 0; i < IA.size(); i++) {
			double ori_weight = items[i].getWeight();
			int ori_pickup_duration = items[i].getPickupDuration();
			int ori_delivery_duration = items[i].getDeliveryDuration();

			while (true) {
				ItemAmount ia = IA.get(i);
				// int pickupDuration = //items[i].getPickupDuration();
				// int deliveryDuration = //items[i].getDeliveryDuration();
				Trip a = splitLargeItemCreateATripWithInternalVehicle(
						ia.itemIndex, ia.amount, pickupLocationIdx,
						deliveryLocationIdx, ori_pickup_duration,
						ori_delivery_duration, r.getFixLoadTime(),
						r.getFixUnloadTime(), earliesAllowArrivalTimePickup,
						latestAllowArrivalTimePickup,
						earliestAllowArrivalTimeDelivery,
						latestAllowArrivalTimeDelivery, false);
				if (a == null) {
					// System.out.println("TRY SPLIT item " + ia.code +
					// ", amount = " + ia.amount);
					break;
				}
				double cap = vehicles[a.start.vehicleIndex].getWeight();
				int ftl_pickup_duration = (int) (ori_pickup_duration * cap / ia.amount);
				int ftl_delivery_duration = (int) (ori_delivery_duration * cap / ia.amount);
				ori_pickup_duration = ori_pickup_duration - ftl_pickup_duration;// (ori_pickup_duration*(ia.amount-cap))/(ia.amount);
				ori_delivery_duration = ori_delivery_duration
						- ftl_delivery_duration;// (ori_delivery_duration*(ia.amount-cap))/(ia.amount);
				ia.amount = ia.amount - cap;

				items[i].setWeight(ia.amount);
				items[i].setPickupDuration(ori_pickup_duration);
				items[i].setDeliveryDuration(ori_delivery_duration);

				// System.out.println("SPLIT new trip " + a.toString()
				// + ", remain amount = " + items[i].getWeight());
				Item[] ci = new Item[1];
				ci[0] = items[i].clone();
				ci[0].setWeight(cap);
				ci[0].setPickupDuration(ftl_pickup_duration);
				ci[0].setDeliveryDuration(ftl_delivery_duration);

				mTrip2Items.put(a, ci);
				trips[a.start.vehicleIndex].add(a);
			}
		}
	}

	public void processSplitAnOrderWithExternalVehicle(PickupDeliveryRequest r) {
		Item[] items = r.getItems();
		int pickupLocationIdx = mLocationCode2Index.get(r
				.getPickupLocationCode());
		int deliveryLocationIdx = mLocationCode2Index.get(r
				.getDeliveryLocationCode());
		int earliesAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyPickupTime());
		int earliestAllowArrivalTimeDelivery = (int) DateTimeUtils
				.dateTime2Int(r.getEarlyDeliveryTime());
		int latestAllowArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getLatePickupTime());
		int latestAllowArrivalTimeDelivery = (int) DateTimeUtils.dateTime2Int(r
				.getLateDeliveryTime());

		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		for (int i = 0; i < items.length; i++) {
			IA.add(new ItemAmount(mItem2Index.get(items[i]), (int) items[i]
					.getWeight()));
		}
		int nbIntVehicles = computeInternalVehicles();
		
		for (int i = 0; i < IA.size(); i++) {
			double ori_weight = items[i].getWeight();
			int ori_pickup_duration = items[i].getPickupDuration();
			int ori_delivery_duration = items[i].getDeliveryDuration();

			while (true) {
				ItemAmount ia = IA.get(i);
				int pickupDuration = items[i].getPickupDuration();
				int deliveryDuration = items[i].getDeliveryDuration();
				Trip a = splitLargeItemCreateATripWithExternalVehicle(
						ia.itemIndex, ia.amount, pickupLocationIdx,
						deliveryLocationIdx, ori_pickup_duration,
						ori_delivery_duration, r.getFixLoadTime(),
						r.getFixUnloadTime(), earliesAllowArrivalTimePickup,
						latestAllowArrivalTimePickup,
						earliestAllowArrivalTimeDelivery,
						latestAllowArrivalTimeDelivery, false);
				if (a == null) {
					// System.out.println("TRY SPLIT item " + ia.code +
					// ", amount = " + ia.amount);
					break;
				}
				int cap = (int) externalVehicles[a.start.vehicleIndex
						- nbIntVehicles].getWeight();

				int ftl_pickup_duration = (int) (ori_pickup_duration * cap / ia.amount);
				int ftl_delivery_duration = (int) (ori_delivery_duration * cap / ia.amount);
				ori_pickup_duration = ori_pickup_duration - ftl_pickup_duration;// (ori_pickup_duration*(ia.amount-cap))/(ia.amount);
				ori_delivery_duration = ori_delivery_duration
						- ftl_delivery_duration;// (ori_delivery_duration*(ia.amount-cap))/(ia.amount);
				ia.amount = ia.amount - cap;

				items[i].setWeight(ia.amount);
				items[i].setPickupDuration(ori_pickup_duration);
				items[i].setDeliveryDuration(ori_delivery_duration);

				items[i].setWeight(ia.amount);
				System.out.println("SPLIT new trip " + a.toString()
						+ ", remain amount = " + items[i].getWeight());
				Item[] ci = new Item[1];
				ci[0] = items[i].clone();
				ci[0].setWeight(cap);
				ci[0].setPickupDuration(ftl_pickup_duration);
				ci[0].setDeliveryDuration(ftl_delivery_duration);
				mTrip2Items.put(a, ci);
				trips[a.start.vehicleIndex].add(a);
			}
		}
	}

	public ArrayList<Item> sortDecreasing(ArrayList<Item> items) {
		ArrayList<Item> s_items = new ArrayList<Item>();
		Item[] I = new Item[items.size()];
		for (int i = 0; i < items.size(); i++)
			I[i] = items.get(i);
		for (int i = 0; i < I.length - 1; i++) {
			for (int j = i + 1; j < I.length; j++) {
				if (I[i].getWeight() < I[j].getWeight()) {
					Item tmp = I[i];
					I[i] = I[j];
					I[j] = tmp;
				}
			}
		}
		for (int i = 0; i < I.length; i++)
			s_items.add(I[i]);
		return s_items;
	}

	public double max(double a, double b) {
		if (a < b)
			return b;
		return a;
	}

	public double min(double a, double b) {
		if (a < b)
			return a;
		return b;
	}

	public int MIN(int a, int b) {
		return a < b ? a : b;
	}

	public int MAX(int a, int b) {
		return a > b ? a : b;
	}

	public int computeRemainItemOnRequest() {
		int c = 0;
		for (int i = 0; i < input.getRequests().length; i++) {
			c += input.getRequests()[i].getItems().length;
		}
		return c;
	}

	public ArrayList<Item> loadFTLToVehicle(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {
		Vehicle vh = getVehicle(vehicle_index);
		// Vehicle vh = getVehicle(vehicle_index);
		String pickupLocationCode = locationCodes.get(pickuplocationIndex);
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		// log(name() + "::loadFTLToVehicle, deliveryLocation " +
		// deliveryLocationCode + ", type " +
		// mLocation2Type.get(deliveryLocationCode) + ", vehicle " +
		// vh.getCode() + ", capacity " + vh.getWeight());

		if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
				pickupLocationCode)
				|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
						deliveryLocationCode)){
			log(name() + "::loadFTLToVehicle, vehicle " + vh.getCode() + ", category = " + vh.getVehicleCategory()
					+ ", cap = " + vh.getWeight() +
					", CANNOT GO TO LOCATION -> RETURN FALSE");
			return new ArrayList<Item>();
		}
		
		String debug_vehicle_code = "";
		String debug_location_code = "";

		// String deliveryLocationCode =
		// locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

	
		boolean DEBUG_LOG = deliveryLocationCode.equals(debug_location_code) &&
				vh.getCode().contains("SUGGESTED-4-2");
		
		ArrayList<Item> collectItems = new ArrayList<Item>();

		// Vehicle vh = getVehicle(vehicle_index);
		int locationIndex = getLastLocationIndex(vehicle_index);
		int endLocationIndexVehicle = mLocationCode2Index.get(vh
				.getEndLocationCode());

		int startTime = getLastDepartureTimeOfVehicle(vehicle_index);
		double travel_time = a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		double arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double departure_pickup = -1;
		double arrival_delivery = -1;
		double departure_delivery = -1;

		double sel_arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double sel_departure_pickup = -1;
		double sel_arrival_delivery = -1;
		double sel_departure_delivery = -1;

		int end_work_time_vehicle = -1;
		if (vh.getEndWorkingTime() != null)
			end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
					.getEndWorkingTime());

		int load_time = 0;
		int unload_time = 0;
		int load = 0;

		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();

		int pickup_early_time = 1 - Integer.MAX_VALUE;
		int pickup_late_time = Integer.MAX_VALUE;
		int delivery_early_time = 1 - Integer.MAX_VALUE;
		int delivery_late_time = Integer.MAX_VALUE;

		while (decreasing_weight_items.size() > 0) {
			int sel_i = -1;
			for (int i = 0; i < decreasing_weight_items.size(); i++) {
				Item I = decreasing_weight_items.get(i);
				int itemIndex = mItem2Index.get(I);
				PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
						.get(itemIndex)];

				if(DEBUG_LOG)
					log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
							+ ", vehicle cap = " + vh.getWeight());
				
				if (I.getWeight() + load > vh.getWeight()) {
					if(DEBUG_LOG)
						log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
								+ ", vehicle cap = " + vh.getWeight() + " --> continue");
					continue;
				}

				// check time
				pickup_early_time = MAX(
						pickup_early_time,
						(int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime()));
				pickup_late_time = MIN(pickup_late_time,
						(int) DateTimeUtils.dateTime2Int(r.getLatePickupTime()));
				delivery_early_time = MAX(delivery_early_time,
						(int) DateTimeUtils.dateTime2Int(r
								.getEarlyDeliveryTime()));
				delivery_late_time = MIN(delivery_late_time,
						(int) DateTimeUtils.dateTime2Int(r
								.getLateDeliveryTime()));

				if (arrival_pickup > pickup_late_time){
					if(DEBUG_LOG)
						log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
								+ ", vehicle cap = " + vh.getWeight() 
								+ ", arrival_pickup = " + arrival_pickup + " > pickup_late_time = " + pickup_late_time
								+ " --> continue");
					continue;
				}
				departure_pickup = max(arrival_pickup, pickup_early_time)
						+ load_time + I.getPickupDuration() + fix_load_time;
				arrival_delivery = departure_pickup + travel_time;
				if (arrival_delivery > delivery_late_time){
					if(DEBUG_LOG)
						log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
								+ ", vehicle cap = " + vh.getWeight() 
								+ ", arrival_delivery = " + arrival_delivery + " > delivery_late_time = " + delivery_late_time
								+ " --> continue");
					
					continue;
				}
				departure_delivery = max(arrival_delivery, delivery_early_time)
						+ unload_time + I.getDeliveryDuration()
						+ fix_unload_time;

				double arrival_depot = departure_delivery
						+ a_travelTime[deliveryLocationIndex][endLocationIndexVehicle];

				if (arrival_depot > end_work_time_vehicle
						&& end_work_time_vehicle >= 0){
					if(DEBUG_LOG)
						log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
								+ ", vehicle cap = " + vh.getWeight() 
								+ ", arrival_depot = " + arrival_depot + " > end_work_time_vehicle = " + end_work_time_vehicle
								+ " --> continue");
					continue;
				}

				// check exclusive items

				boolean conflict = false;
				for (ItemAmount ia : IA) {
					int ii = ia.itemIndex;
					if (itemConflict[ii][itemIndex]) {
						conflict = true;
						break;
					}
				}
				if (conflict){
					if(DEBUG_LOG)
						log(name() + "::loadFTLToVehicle, consider item " + I.getWeight() + ", load = " + load
								+ ", vehicle cap = " + vh.getWeight() 
								+ " conflict items"
								+ " --> continue");
					continue;
				}
				load_time += I.getPickupDuration();
				unload_time += I.getDeliveryDuration();
				load += I.getWeight();

				sel_arrival_pickup = arrival_pickup;
				sel_departure_pickup = departure_pickup;
				sel_arrival_delivery = arrival_delivery;
				sel_departure_delivery = departure_delivery;

				sel_i = i;
				if(DEBUG_LOG)
					log(name() + "::loadFTLToVehicle, sel_i = " + sel_i);
				break;
			}
			if (sel_i > -1) {
				Item I = decreasing_weight_items.get(sel_i);
				int itemIndex = mItem2Index.get(I);
				IA.add(new ItemAmount(itemIndex, (int) I.getWeight()));
				decreasing_weight_items.remove(sel_i);
				collectItems.add(I);
				if(DEBUG_LOG)
					log(name() + "::loadFTLToVehicle, IA.sz = " + IA.size());
				// System.out.println("CONSIDER item " + I.getCode() + ","
				// + I.getWeight());
				// if (log != null)
				// log.println("CONSIDER item " + I.getCode() + ","
				// + I.getWeight());
			} else {
				break;
			}
		}

		if (IA.size() > 0) {
			if (decreasing_weight_items.size() > 0) {
				RouteNode start = new RouteNode(pickuplocationIndex,
						(int) sel_arrival_pickup, (int) sel_departure_pickup,
						IA, vehicle_index, "P");
				RouteNode end = new RouteNode(deliveryLocationIndex,
						(int) sel_arrival_delivery,
						(int) sel_departure_delivery, IA, vehicle_index, "D");

				start.solver = this;
				end.solver = this;

				Trip t = new Trip(start, end, "COLLECT_TO_FTL");
				// t.vh = vh;

				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(t, items_of_trip);
				if (!t.checkTime()) {
					log(name() + "::loadFTLToVehicle, BUG checkTime????");
					log(name() + "::loadFTLToVehicle, BUG Vehicle = "
							+ getVehicle(vehicle_index).getCode()
							+ ", INFO TRIP = " + t.toString());
				}
				// Vehicle sel_vh = getVehicle(vehicle_index);
				trips[vehicle_index].add(t);
				

				int remainItems = computeRemainItemOnRequest();

				

				return collectItems;
			
			} else {
				if (DEBUG_LOG) {
					log(name() + " LOAD ALL items to vehicle "
							+ vh.getCode()
							+ ", BUT IGNORE, TRY to optimize later");
				}
			}
		}
		// System.out.println("CONSIDER KOKOKOKOKO-----------------------");
		// restore list
		decreasing_weight_items.clear();
		for (int i = 0; i < storeL.size(); i++)
			decreasing_weight_items.add(storeL.get(i));
		collectItems.clear();
		return collectItems;
	}

	public boolean canVehicleDelivery(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex, Item I,
			int fix_load_time, int fix_unload_time) {
		Vehicle vh = getVehicle(vehicle_index);
		// Vehicle vh = getVehicle(vehicle_index);
		String pickupLocationCode = locationCodes.get(pickuplocationIndex);
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);
		if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
				pickupLocationCode)
				|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
						deliveryLocationCode))
			return false;

		int locationIndex = mLocationCode2Index.get(vh.getStartLocationCode());// getLastLocationIndex(vehicle_index);
		int endLocationIndexVehicle = mLocationCode2Index.get(vh
				.getEndLocationCode());

		int startTime = (int) DateTimeUtils.dateTime2Int(vh
				.getStartWorkingTime());// getLastDepartureTimeOfVehicle(vehicle_index);
		double travel_time = a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		double arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double departure_pickup = -1;
		double arrival_delivery = -1;
		double departure_delivery = -1;

		int end_work_time_vehicle = -1;
		if (vh.getEndWorkingTime() != null)
			end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
					.getEndWorkingTime());

		int load_time = 0;
		int unload_time = 0;
		int load = 0;

		int pickup_early_time = 1 - Integer.MAX_VALUE;
		int pickup_late_time = Integer.MAX_VALUE;
		int delivery_early_time = 1 - Integer.MAX_VALUE;
		int delivery_late_time = Integer.MAX_VALUE;

		int itemIndex = mItem2Index.get(I);
		PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
				.get(itemIndex)];

		if (I.getWeight() + load > vh.getWeight()) {
			return false;
		}

		// check time
		pickup_early_time = MAX(pickup_early_time,
				(int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime()));
		pickup_late_time = MIN(pickup_late_time,
				(int) DateTimeUtils.dateTime2Int(r.getLatePickupTime()));
		delivery_early_time = MAX(delivery_early_time,
				(int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime()));
		delivery_late_time = MIN(delivery_late_time,
				(int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime()));

		if (arrival_pickup > pickup_late_time)
			return false;
		departure_pickup = max(arrival_pickup, pickup_early_time) + load_time
				+ I.getPickupDuration() + fix_load_time;
		arrival_delivery = departure_pickup + travel_time;
		if (arrival_delivery > delivery_late_time)
			return false;
		departure_delivery = max(arrival_delivery, delivery_early_time)
				+ unload_time + I.getDeliveryDuration() + fix_unload_time;

		double arrival_depot = departure_delivery
				+ a_travelTime[deliveryLocationIndex][endLocationIndexVehicle];

		if (arrival_depot > end_work_time_vehicle && end_work_time_vehicle >= 0)
			return false;

		return true;
	}

	public ArrayList<Item> loadFTLToVehicleNoConstraints(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {
		Vehicle vh = getVehicle(vehicle_index);
		System.out.println(name()
				+ "::loadFTLToVehicleNoConstraints, vehicle = " + vh.getCode());

		String debug_vehicle_code = "";
		String debug_location_code = "";

		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		if (debug_vehicle_code.equals(vh.getCode()))
			System.out.println("loadFTLToVehicleNoConstraints(vehicle_index = "
					+ vehicle_index + ", code = " + vh.getCode() + ", cap = "
					+ vh.getWeight() + "), decreasing_items = ");
		// if (log != null)
		// log.println("loadFTLToVehicle(vehicle_index = " + vehicle_index
		// + "), decreasing_items = ");
		// if (debug_vehicle_code.equals(vh.getCode()))
		if (deliveryLocationCode.equals(debug_location_code))
			System.out.println(name()
					+ "::loadFTLToVehicleNoConstraints(vehicle = "
					+ vh.getCode() + ", location = " + deliveryLocationCode
					+ ", items = ");
		for (int i = 0; i < decreasing_weight_items.size(); i++) {
			System.out.print("[" + decreasing_weight_items.get(i).getCode()
					+ "," + decreasing_weight_items.get(i).getWeight() + "] ");

			// if (log != null)
			// log.print("[" + decreasing_weight_items.get(i).getCode() + ","
			// + decreasing_weight_items.get(i).getWeight() + "] ");
		}

		// if(deliveryLocationCode.equals("60005447"))
		// System.out.println();

		// if (log != null)
		// log.println();

		ArrayList<Item> collectItems = new ArrayList<Item>();

		// Vehicle vh = getVehicle(vehicle_index);
		int locationIndex = getLastLocationIndex(vehicle_index);
		int endLocationIndexVehicle = mLocationCode2Index.get(vh
				.getEndLocationCode());

		int startTime = getLastDepartureTimeOfVehicle(vehicle_index);
		double travel_time = a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		double arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double departure_pickup = -1;
		double arrival_delivery = -1;
		double departure_delivery = -1;

		double sel_arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double sel_departure_pickup = -1;
		double sel_arrival_delivery = -1;
		double sel_departure_delivery = -1;

		int end_work_time_vehicle = -1;
		if (vh.getEndWorkingTime() != null)
			end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
					.getEndWorkingTime());

		int load_time = 0;
		int unload_time = 0;
		int load = 0;

		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();

		int pickup_early_time = 1 - Integer.MAX_VALUE;
		int pickup_late_time = Integer.MAX_VALUE;
		int delivery_early_time = 1 - Integer.MAX_VALUE;
		int delivery_late_time = Integer.MAX_VALUE;

		while (decreasing_weight_items.size() > 0) {
			int sel_i = -1;
			for (int i = 0; i < decreasing_weight_items.size(); i++) {
				Item I = decreasing_weight_items.get(i);
				int itemIndex = mItem2Index.get(I);
				PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
						.get(itemIndex)];

				// check time
				pickup_early_time = MAX(
						pickup_early_time,
						(int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime()));
				pickup_late_time = MIN(pickup_late_time,
						(int) DateTimeUtils.dateTime2Int(r.getLatePickupTime()));
				delivery_early_time = MAX(delivery_early_time,
						(int) DateTimeUtils.dateTime2Int(r
								.getEarlyDeliveryTime()));
				delivery_late_time = MIN(delivery_late_time,
						(int) DateTimeUtils.dateTime2Int(r
								.getLateDeliveryTime()));

				departure_pickup = max(arrival_pickup, pickup_early_time)
						+ load_time + I.getPickupDuration() + fix_load_time;
				arrival_delivery = departure_pickup + travel_time;
				departure_delivery = max(arrival_delivery, delivery_early_time)
						+ unload_time + I.getDeliveryDuration()
						+ fix_unload_time;

				double arrival_depot = departure_delivery
						+ a_travelTime[deliveryLocationIndex][endLocationIndexVehicle];

				load_time += I.getPickupDuration();
				unload_time += I.getDeliveryDuration();
				load += I.getWeight();

				sel_arrival_pickup = arrival_pickup;
				sel_departure_pickup = departure_pickup;
				sel_arrival_delivery = arrival_delivery;
				sel_departure_delivery = departure_delivery;

				sel_i = i;
				break;
			}
			if (sel_i > -1) {
				Item I = decreasing_weight_items.get(sel_i);
				int itemIndex = mItem2Index.get(I);
				IA.add(new ItemAmount(itemIndex, (int) I.getWeight()));
				decreasing_weight_items.remove(sel_i);
				collectItems.add(I);
				// System.out.println("CONSIDER item " + I.getCode() + ","
				// + I.getWeight());
				if (log != null)
					log.println("CONSIDER item " + I.getCode() + ","
							+ I.getWeight());
			} else {
				break;
			}
		}

		if (IA.size() > 0) {
			// if (decreasing_weight_items.size() > 0) {
			RouteNode start = new RouteNode(pickuplocationIndex,
					(int) sel_arrival_pickup, (int) sel_departure_pickup, IA,
					vehicle_index, "P");
			RouteNode end = new RouteNode(deliveryLocationIndex,
					(int) sel_arrival_delivery, (int) sel_departure_delivery,
					IA, vehicle_index, "D");

			start.solver = this;
			end.solver = this;

			Trip t = new Trip(start, end, "COLLECT_TO_FTL");
			Item[] items_of_trip = new Item[IA.size()];
			for (int ii = 0; ii < IA.size(); ii++) {
				items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
			}
			mTrip2Items.put(t, items_of_trip);
			trips[vehicle_index].add(t);
			// System.out.println("CONSIDER OK********************");
			log(name() + "::loadFTLToVehicleNoConstraints, create-trip "
					+ t.toString());
			log(name() + "::loadFTLToVehicle, CONSIDER OK********************");
			return collectItems;

		}

		return collectItems;
	}

	public Trip createTripLoadAllFTLToVehicleCategory(Vehicle vh,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {

		String debugLocationCode = "60005547";
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		// System.out.println("loadFTLToVehicle(vehicle_index = " +
		// vehicle_index
		// + "), decreasing_items = ");

		for (Item I1 : decreasing_weight_items) {
			for (Item I2 : decreasing_weight_items) {
				int i1 = mItem2Index.get(I1);
				int i2 = mItem2Index.get(I2);
				if (itemConflict[i1][i2])
					return null;
			}
		}
		int locationIndex = mLocationCode2Index.get(vh.getStartLocationCode());

		int early_pickup = -1;
		int early_delivery = -1;
		int late_pickup = Integer.MAX_VALUE;
		int late_delivery = Integer.MAX_VALUE;
		int load_time = 0;
		int unload_time = 0;
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			PickupDeliveryRequest r = getRequestOfItem(I);
			int ep = (int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime());
			int ed = (int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime());
			if (early_pickup < ep)
				early_pickup = ep;
			if (early_delivery < ed)
				early_delivery = ed;
			int lp = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
			int ld = (int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
			if (late_pickup < lp)
				late_pickup = lp;
			if (late_delivery < ld)
				late_delivery = ld;

			load_time += I.getPickupDuration();
			unload_time += I.getDeliveryDuration();

			totalW += I.getWeight();
		}

		if (vh.getWeight() < totalW) {
			// if (deliveryLocationCode.equals(debugLocationCode)) {
			// log(name() + "::createTripLoadAllFTLToVehicleCategory " +
			// vh.getCode() + "-" + vh.getVehicleCategory()
			// + ") "
			// + "RETURN null totalW = " + totalW + ", cap = " +
			// vh.getWeight());
			// }
			return null;
		}

		int startTime = (int) DateTimeUtils.dateTime2Int(vh
				.getStartWorkingTime());
		int travel_time = (int) a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		int arrival_pickup = startTime
				+ (int) a_travelTime[locationIndex][pickuplocationIndex];

		if (arrival_pickup > late_pickup) {
			// if (deliveryLocationCode.equals(debugLocationCode)) {
			// log(name() + "::createTripLoadAllFTLToVehicleCategory " +
			// vh.getCode() + "-" + vh.getVehicleCategory()
			// + ") "
			// + "RETURN null arrival_pickup = " +
			// DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup) +
			// ", late_pickup = " +
			// DateTimeUtils.unixTimeStamp2DateTime(late_pickup));
			// }

			return null;
		}

		int departure_pickup = MAX(arrival_pickup, early_pickup) + load_time
				+ fix_load_time;
		int arrival_delivery = departure_pickup + travel_time;
		if (arrival_delivery > late_delivery) {
			// if (deliveryLocationCode.equals(debugLocationCode)) {
			// log(name() + "::createTripLoadAllFTLToVehicleCategory " +
			// vh.getCode() + "-" + vh.getVehicleCategory()
			// + ") "
			// + "RETURN null arrival_delivery = " +
			// DateTimeUtils.unixTimeStamp2DateTime(arrival_delivery) +
			// ", late_delivery = " +
			// DateTimeUtils.unixTimeStamp2DateTime(late_delivery));
			// }

			return null;
		}

		int departure_delivery = MAX(arrival_delivery, early_delivery)
				+ unload_time + fix_unload_time;

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::createTripLoadAllFTLToVehicleCategory "
					+ vh.getCode() + "-" + vh.getVehicleCategory() + ") "
					+ "RETURN NEW trip");
		}
		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		for (Item I : decreasing_weight_items) {
			int itemIndex = mItem2Index.get(I);
			ItemAmount ia = new ItemAmount(itemIndex, I.getWeight());
			IA.add(ia);
		}
		RouteNode start = new RouteNode(pickuplocationIndex, arrival_pickup,
				departure_pickup, IA, -1, "P");
		RouteNode end = new RouteNode(deliveryLocationIndex, arrival_delivery,
				departure_delivery, IA, -1, "D");
		Trip t = new Trip(start, end, "SEPARATE");
		Item[] items_of_trip = new Item[IA.size()];
		for (int ii = 0; ii < IA.size(); ii++) {
			items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
		}
		mTrip2Items.put(t, items_of_trip);
		return t;
	}

	public Trip createTripLoadAllFTLToVehicle(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {

		Vehicle vh = getVehicle(vehicle_index);
		String pickupLocationCode = locationCodes.get(pickuplocationIndex);
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);
		if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
				pickupLocationCode)
				|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
						deliveryLocationCode))
			return null;

		String debugLocationCode = "";
		// String deliveryLocationCode =
		// locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		// System.out.println("loadFTLToVehicle(vehicle_index = " +
		// vehicle_index
		// + "), decreasing_items = ");

		for (Item I1 : decreasing_weight_items) {
			for (Item I2 : decreasing_weight_items) {
				int i1 = mItem2Index.get(I1);
				int i2 = mItem2Index.get(I2);
				if (itemConflict[i1][i2])
					return null;
			}
		}
		// Vehicle vh = getVehicle(vehicle_index);

		int locationIndex = getLastLocationIndex(vehicle_index);// mLocationCode2Index.get(vh.getStartLocationCode());

		int early_pickup = -1;// Integer.MAX_VALUE;
		int early_delivery = -1;// Integer.MAX_VALUE;
		int late_pickup = Integer.MAX_VALUE;
		int late_delivery = Integer.MAX_VALUE;
		int load_time = 0;
		int unload_time = 0;
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			PickupDeliveryRequest r = getRequestOfItem(I);
			int ep = (int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime());
			int ed = (int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime());
			if (early_pickup < ep)
				early_pickup = ep;
			if (early_delivery < ed)
				early_delivery = ed;
			int lp = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
			int ld = (int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
			if (late_pickup > lp)
				late_pickup = lp;
			if (late_delivery > ld)
				late_delivery = ld;

			load_time += I.getPickupDuration();
			unload_time += I.getDeliveryDuration();

			totalW += I.getWeight();
		}

		if (vh.getWeight() < totalW) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::createTripLoadAllFTLToVehicle " + vh.getCode()
						+ "-" + vh.getVehicleCategory() + ") "
						+ "RETURN null totalW = " + totalW + ", cap = "
						+ vh.getWeight());
			}
			return null;
		}

		int startTime = getLastDepartureTimeOfVehicle(vehicle_index);// (int)DateTimeUtils.dateTime2Int(vh.getStartWorkingTime());
		int travel_time = (int) a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		int arrival_pickup = startTime
				+ (int) a_travelTime[locationIndex][pickuplocationIndex];

		if (arrival_pickup > late_pickup) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::createTripLoadAllFTLToVehicle " + vh.getCode()
						+ "-" + vh.getVehicleCategory() + ") "
						+ "RETURN null arrival_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
						+ ", late_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup));
			}

			return null;
		}

		int departure_pickup = MAX(arrival_pickup, early_pickup) + load_time
				+ fix_load_time;
		int arrival_delivery = departure_pickup + travel_time;
		if (arrival_delivery > late_delivery) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name()
						+ "::createTripLoadAllFTLToVehicle "
						+ vh.getCode()
						+ "-"
						+ vh.getVehicleCategory()
						+ ") "
						+ "RETURN null arrival_delivery = "
						+ DateTimeUtils
								.unixTimeStamp2DateTime(arrival_delivery)
						+ ", late_delivery = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery));
			}

			return null;
		}

		int departure_delivery = MAX(arrival_delivery, early_delivery)
				+ unload_time + fix_unload_time;

		// check time vehicle returning to the depot
		int end_working_time = (int) DateTimeUtils.dateTime2Int(vh
				.getEndWorkingTime());
		int depotLocationIdx = mLocationCode2Index.get(vh.getEndLocationCode());
		int arrivalTimeDepot = departure_delivery
				+ (int) a_travelTime[deliveryLocationIndex][depotLocationIdx];
		if (arrivalTimeDepot > end_working_time)
			return null;

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::createTripLoadAllFTLToVehicle " + vh.getCode()
					+ "-" + vh.getVehicleCategory() + ") "
					+ "RETURN NEW trip, startTime = "
					+ DateTimeUtils.unixTimeStamp2DateTime(startTime)
					+ ", arrival_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
					+ ", departure_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_pickup)
					+ ", arrival_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_delivery)
					+ ", departure_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_delivery)
					+ ", late_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup)
					+ ", late_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery)
					+ ", load_time = " + DateTimeUtils.second2HMS(load_time)
					+ ", unload_time = "
					+ DateTimeUtils.second2HMS(unload_time));
		}
		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		for (Item I : decreasing_weight_items) {
			int itemIndex = mItem2Index.get(I);
			ItemAmount ia = new ItemAmount(itemIndex, I.getWeight());
			IA.add(ia);
		}
		RouteNode start = new RouteNode(pickuplocationIndex, arrival_pickup,
				departure_pickup, IA, vehicle_index, "P");
		RouteNode end = new RouteNode(deliveryLocationIndex, arrival_delivery,
				departure_delivery, IA, vehicle_index, "D");
		start.solver = this;
		end.solver = this;

		Trip t = new Trip(start, end, "SEPARATE");
		// t.vh = vh;

		// System.out.println(name() +
		// "::createTripLoadAllFTLToVehicle, create trip for vehicle " +
		// vh.getCode());

		return t;
	}

	public Trip createDirectTrip4OnItem(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex, Item I,
			int fix_load_time, int fix_unload_time) {

		Vehicle vh = getVehicle(vehicle_index);
		String pickupLocationCode = locationCodes.get(pickuplocationIndex);
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);
		if (mVehicle2NotReachedLocations.get(vh.getCode()).contains(
				pickupLocationCode)
				|| mVehicle2NotReachedLocations.get(vh.getCode()).contains(
						deliveryLocationCode))
			return null;

		String debugLocationCode = "";
		// String deliveryLocationCode =
		// locationCodes.get(deliveryLocationIndex);

		int locationIndex = mLocationCode2Index.get(vh.getStartLocationCode());// getLastLocationIndex(vehicle_index);//
																				// mLocationCode2Index.get(vh.getStartLocationCode());

		int early_pickup = -1;// Integer.MAX_VALUE;
		int early_delivery = -1;// Integer.MAX_VALUE;
		int late_pickup = Integer.MAX_VALUE;
		int late_delivery = Integer.MAX_VALUE;
		int load_time = 0;
		int unload_time = 0;
		double totalW = 0;
		PickupDeliveryRequest r = getRequestOfItem(I);
		int ep = (int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime());
		int ed = (int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime());
		if (early_pickup < ep)
			early_pickup = ep;
		if (early_delivery < ed)
			early_delivery = ed;
		int lp = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
		int ld = (int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
		if (late_pickup > lp)
			late_pickup = lp;
		if (late_delivery > ld)
			late_delivery = ld;

		load_time += I.getPickupDuration();
		unload_time += I.getDeliveryDuration();

		totalW += I.getWeight();

		if (vh.getWeight() < totalW) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::createDirectTrip4OnItem " + vh.getCode() + "-"
						+ vh.getVehicleCategory() + ") "
						+ "RETURN null totalW = " + totalW + ", cap = "
						+ vh.getWeight());
			}
			return null;
		}

		int startTime = (int) DateTimeUtils.dateTime2Int(vh
				.getStartWorkingTime());// getLastDepartureTimeOfVehicle(vehicle_index);//
										// (int)DateTimeUtils.dateTime2Int(vh.getStartWorkingTime());
		int travel_time = (int) a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		int arrival_pickup = startTime
				+ (int) a_travelTime[locationIndex][pickuplocationIndex];

		if (arrival_pickup > late_pickup) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::createDirectTrip4OnItem " + vh.getCode() + "-"
						+ vh.getVehicleCategory() + ") "
						+ "RETURN null arrival_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
						+ ", late_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup));
			}

			return null;
		}

		int departure_pickup = MAX(arrival_pickup, early_pickup) + load_time
				+ fix_load_time;
		int arrival_delivery = departure_pickup + travel_time;
		if (arrival_delivery > late_delivery) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name()
						+ "::createDirectTrip4OnItem "
						+ vh.getCode()
						+ "-"
						+ vh.getVehicleCategory()
						+ ") "
						+ "RETURN null arrival_delivery = "
						+ DateTimeUtils
								.unixTimeStamp2DateTime(arrival_delivery)
						+ ", late_delivery = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery));
			}

			return null;
		}

		int departure_delivery = MAX(arrival_delivery, early_delivery)
				+ unload_time + fix_unload_time;

		// check time vehicle returning to the depot
		int end_working_time = (int) DateTimeUtils.dateTime2Int(vh
				.getEndWorkingTime());
		int depotLocationIdx = mLocationCode2Index.get(vh.getEndLocationCode());
		int arrivalTimeDepot = departure_delivery
				+ (int) a_travelTime[deliveryLocationIndex][depotLocationIdx];
		if (arrivalTimeDepot > end_working_time)
			return null;

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::createDirectTrip4OnItem " + vh.getCode() + "-"
					+ vh.getVehicleCategory() + ") "
					+ "RETURN NEW trip, startTime = "
					+ DateTimeUtils.unixTimeStamp2DateTime(startTime)
					+ ", arrival_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
					+ ", departure_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_pickup)
					+ ", arrival_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_delivery)
					+ ", departure_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_delivery)
					+ ", late_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup)
					+ ", late_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery)
					+ ", load_time = " + DateTimeUtils.second2HMS(load_time)
					+ ", unload_time = "
					+ DateTimeUtils.second2HMS(unload_time));
		}
		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();

		int itemIndex = mItem2Index.get(I);
		ItemAmount ia = new ItemAmount(itemIndex, I.getWeight());
		IA.add(ia);

		RouteNode start = new RouteNode(pickuplocationIndex, arrival_pickup,
				departure_pickup, IA, vehicle_index, "P");
		RouteNode end = new RouteNode(deliveryLocationIndex, arrival_delivery,
				departure_delivery, IA, vehicle_index, "D");
		start.solver = this;
		end.solver = this;

		Trip t = new Trip(start, end, "SEPARATE");
		return t;
	}

	public Trip createTripLoadAllFTLToVehicleNoConstraints(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {

		// Vehicle vh = getVehicle(vehicle_index);

		String debugLocationCode = "";
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		// System.out.println("loadFTLToVehicle(vehicle_index = " +
		// vehicle_index
		// + "), decreasing_items = ");

		for (Item I1 : decreasing_weight_items) {
			for (Item I2 : decreasing_weight_items) {
				int i1 = mItem2Index.get(I1);
				int i2 = mItem2Index.get(I2);
				if (itemConflict[i1][i2])
					return null;
			}
		}
		Vehicle vh = getVehicle(vehicle_index);

		int locationIndex = getLastLocationIndex(vehicle_index);// mLocationCode2Index.get(vh.getStartLocationCode());

		int early_pickup = -1;// Integer.MAX_VALUE;
		int early_delivery = -1;// Integer.MAX_VALUE;
		int late_pickup = Integer.MAX_VALUE;
		int late_delivery = Integer.MAX_VALUE;
		int load_time = 0;
		int unload_time = 0;
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			PickupDeliveryRequest r = getRequestOfItem(I);
			int ep = (int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime());
			int ed = (int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime());
			if (early_pickup < ep)
				early_pickup = ep;
			if (early_delivery < ed)
				early_delivery = ed;
			int lp = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
			int ld = (int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
			if (late_pickup > lp)
				late_pickup = lp;
			if (late_delivery > ld)
				late_delivery = ld;

			load_time += I.getPickupDuration();
			unload_time += I.getDeliveryDuration();

			totalW += I.getWeight();
		}

		int startTime = getLastDepartureTimeOfVehicle(vehicle_index);// (int)DateTimeUtils.dateTime2Int(vh.getStartWorkingTime());
		int travel_time = (int) a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		int arrival_pickup = startTime
				+ (int) a_travelTime[locationIndex][pickuplocationIndex];

		int departure_pickup = MAX(arrival_pickup, early_pickup) + load_time
				+ fix_load_time;
		int arrival_delivery = departure_pickup + travel_time;

		int departure_delivery = MAX(arrival_delivery, early_delivery)
				+ unload_time + fix_unload_time;

		// check time vehicle returning to the depot
		int end_working_time = (int) DateTimeUtils.dateTime2Int(vh
				.getEndWorkingTime());
		int depotLocationIdx = mLocationCode2Index.get(vh.getEndLocationCode());
		int arrivalTimeDepot = departure_delivery
				+ (int) a_travelTime[deliveryLocationIndex][depotLocationIdx];

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::createTripLoadAllFTLToVehicle " + vh.getCode()
					+ "-" + vh.getVehicleCategory() + ") "
					+ "RETURN NEW trip, startTime = "
					+ DateTimeUtils.unixTimeStamp2DateTime(startTime)
					+ ", arrival_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
					+ ", departure_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_pickup)
					+ ", arrival_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrival_delivery)
					+ ", departure_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(departure_delivery)
					+ ", late_pickup = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup)
					+ ", late_delivery = "
					+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery)
					+ ", load_time = " + DateTimeUtils.second2HMS(load_time)
					+ ", unload_time = "
					+ DateTimeUtils.second2HMS(unload_time));
		}
		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		for (Item I : decreasing_weight_items) {
			int itemIndex = mItem2Index.get(I);
			ItemAmount ia = new ItemAmount(itemIndex, I.getWeight());
			IA.add(ia);
		}
		RouteNode start = new RouteNode(pickuplocationIndex, arrival_pickup,
				departure_pickup, IA, vehicle_index, "P");
		RouteNode end = new RouteNode(deliveryLocationIndex, arrival_delivery,
				departure_delivery, IA, vehicle_index, "D");
		start.solver = this;
		end.solver = this;

		Trip t = new Trip(start, end, "SEPARATE");
		return t;
	}

	public boolean canLoadAllFTLToVehicle(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {

		String debugLocationCode = "60005547";
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		// System.out.println("loadFTLToVehicle(vehicle_index = " +
		// vehicle_index
		// + "), decreasing_items = ");
		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::canLoadAllFTLToVehicle(vehicle_index = "
					+ vehicle_index + "), WITH decreasing_items.sz = "
					+ decreasing_weight_items.size());
			for (Item I : decreasing_weight_items) {
				log(name() + "::canLoadAllFTLToVehicle --> Item " + I.getCode()
						+ ", weight " + I.getWeight());
			}
		}

		for (Item I1 : decreasing_weight_items) {
			for (Item I2 : decreasing_weight_items) {
				int i1 = mItem2Index.get(I1);
				int i2 = mItem2Index.get(I2);
				if (itemConflict[i1][i2])
					return false;
			}
		}
		int locationIndex = getLastLocationIndex(vehicle_index);

		int early_pickup = -1;
		int early_delivery = -1;
		int late_pickup = Integer.MAX_VALUE;
		int late_delivery = Integer.MAX_VALUE;
		int load_time = 0;
		int unload_time = 0;
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			PickupDeliveryRequest r = getRequestOfItem(I);
			int ep = (int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime());
			int ed = (int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime());
			if (early_pickup < ep)
				early_pickup = ep;
			if (early_delivery < ed)
				early_delivery = ed;
			int lp = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
			int ld = (int) DateTimeUtils.dateTime2Int(r.getLateDeliveryTime());
			if (late_pickup < lp)
				late_pickup = lp;
			if (late_delivery < ld)
				late_delivery = ld;

			load_time += I.getPickupDuration();
			unload_time += I.getDeliveryDuration();

			totalW += I.getWeight();
		}

		Vehicle vh = getVehicle(vehicle_index);
		if (vh.getWeight() < totalW) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::::canLoadAllFTLToVehicle(vehicle_index "
						+ vehicle_index + ") " + "RETURN FALSE totalW = "
						+ totalW + ", cap = " + vh.getWeight());
			}
			return false;
		}

		int startTime = getLastDepartureTimeOfVehicle(vehicle_index);
		int travel_time = (int) a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		int arrival_pickup = startTime
				+ (int) a_travelTime[locationIndex][pickuplocationIndex];

		if (arrival_pickup > late_pickup) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name() + "::::canLoadAllFTLToVehicle(vehicle_index "
						+ vehicle_index + ") "
						+ "RETURN FALSE arrival_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrival_pickup)
						+ ", late_pickup = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_pickup));
			}

			return false;
		}

		int departure_pickup = MAX(arrival_pickup, early_pickup) + load_time
				+ fix_load_time;
		int arrival_delivery = departure_pickup + travel_time;
		if (arrival_delivery > late_delivery) {
			if (deliveryLocationCode.equals(debugLocationCode)) {
				log(name()
						+ "::::canLoadAllFTLToVehicle(vehicle_index "
						+ vehicle_index
						+ ") "
						+ "RETURN FALSE arrival_delivery = "
						+ DateTimeUtils
								.unixTimeStamp2DateTime(arrival_delivery)
						+ ", late_delivery = "
						+ DateTimeUtils.unixTimeStamp2DateTime(late_delivery));
			}

			return false;
		}

		int departure_delivery = MAX(arrival_delivery, early_delivery)
				+ unload_time + fix_unload_time;

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::::canLoadAllFTLToVehicle(vehicle_index "
					+ vehicle_index + ") " + "RETURN TRUE");
		}
		return true;

		/*
		 * ArrayList<Item> collectItems = new ArrayList<Item>();
		 * 
		 * Vehicle vh = getVehicle(vehicle_index); int locationIndex =
		 * getLastLocationIndex(vehicle_index); int endLocationIndexVehicle =
		 * mLocationCode2Index.get(vh .getEndLocationCode());
		 * 
		 * int startTime = getLastDepartureTimeOfVehicle(vehicle_index); double
		 * travel_time =
		 * a_travelTime[pickuplocationIndex][deliveryLocationIndex]; double
		 * arrival_pickup = startTime +
		 * a_travelTime[locationIndex][pickuplocationIndex]; double
		 * departure_pickup = -1; double arrival_delivery = -1; double
		 * departure_delivery = -1; int end_work_time_vehicle = (int)
		 * DateTimeUtils.dateTime2Int(vh .getEndWorkingTime()); int load_time =
		 * 0; int unload_time = 0; int load = 0;
		 * 
		 * ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
		 * 
		 * while (decreasing_weight_items.size() > 0) { int sel_i = -1; for (int
		 * i = 0; i < decreasing_weight_items.size(); i++) { Item I =
		 * decreasing_weight_items.get(i); int itemIndex = mItem2Index.get(I);
		 * PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
		 * .get(itemIndex)];
		 * 
		 * if (I.getWeight() + load > vh.getWeight()) { continue; }
		 * 
		 * // check time int pickup_early_time = (int)
		 * DateTimeUtils.dateTime2Int(r .getEarlyPickupTime()); int
		 * pickup_late_time = (int) DateTimeUtils.dateTime2Int(r
		 * .getLatePickupTime()); int delivery_early_time = (int)
		 * DateTimeUtils.dateTime2Int(r .getEarlyDeliveryTime()); int
		 * delivery_late_time = (int) DateTimeUtils.dateTime2Int(r
		 * .getLateDeliveryTime());
		 * 
		 * if (arrival_pickup > pickup_late_time) continue; departure_pickup =
		 * max(arrival_pickup, pickup_early_time) + load_time + fix_load_time;
		 * arrival_delivery = departure_pickup + travel_time; if
		 * (arrival_delivery > delivery_late_time) continue; departure_delivery
		 * = max(arrival_delivery, delivery_early_time) + unload_time +
		 * fix_unload_time;
		 * 
		 * double arrival_depot = departure_delivery +
		 * a_travelTime[deliveryLocationIndex][endLocationIndexVehicle]; // if
		 * (arrival_depot > end_work_time_vehicle) // continue;
		 * 
		 * // check exclusive items
		 * 
		 * boolean conflict = false; for (ItemAmount ia : IA) { int ii =
		 * ia.itemIndex; if (itemConflict[ii][itemIndex]) { conflict = true;
		 * break; } } if (conflict) continue;
		 * 
		 * load_time += I.getPickupDuration(); unload_time +=
		 * I.getDeliveryDuration(); load += I.getWeight();
		 * 
		 * sel_i = i; break; } if (sel_i > -1) { Item I =
		 * decreasing_weight_items.get(sel_i); int itemIndex =
		 * mItem2Index.get(I); IA.add(new ItemAmount(itemIndex, (int)
		 * I.getWeight())); decreasing_weight_items.remove(sel_i);
		 * collectItems.add(I); // System.out.println("CONSIDER item " +
		 * I.getCode() + "," // + I.getWeight()); if (log != null)
		 * log.println("CONSIDER item " + I.getCode() + "," + I.getWeight()); }
		 * else { break; } }
		 * 
		 * if (IA.size() > 0 && decreasing_weight_items.size() == 0) {// try
		 * load // all return true; } //
		 * System.out.println("CONSIDER KOKOKOKOKO-----------------------"); //
		 * restore list decreasing_weight_items.clear(); for (int i = 0; i <
		 * storeL.size(); i++) decreasing_weight_items.add(storeL.get(i));
		 * collectItems.clear(); return false;
		 */
	}

	public boolean canLoadAllFTLToVehicleCategory(Vehicle vh,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {

		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		// System.out.println("loadFTLToVehicle(vehicle_index = " +
		// vehicle_index
		// + "), decreasing_items = ");
		// if(deliveryLocationCode.equals("60005447"))
		// for (int i = 0; i < decreasing_weight_items.size(); i++) {
		// System.out.print("[" + decreasing_weight_items.get(i).getCode()
		// + "," + decreasing_weight_items.get(i).getWeight() + "] ");

		// if (log != null)
		// log.print("[" + decreasing_weight_items.get(i).getCode() + ","
		// + decreasing_weight_items.get(i).getWeight() + "] ");
		// }

		// if(deliveryLocationCode.equals("60005447"))
		// System.out.println();

		if (log != null)
			log.println();

		ArrayList<Item> collectItems = new ArrayList<Item>();

		int locationIndex = mLocationCode2Index.get(vh.getStartLocationCode());// getLastLocationIndex(vehicle_index);
		int endLocationIndexVehicle = mLocationCode2Index.get(vh
				.getEndLocationCode());

		int startTime = (int) DateTimeUtils.dateTime2Int(vh
				.getStartWorkingTime());// getLastDepartureTimeOfVehicle(vehicle_index);
		double travel_time = a_travelTime[pickuplocationIndex][deliveryLocationIndex];
		double arrival_pickup = startTime
				+ a_travelTime[locationIndex][pickuplocationIndex];
		double departure_pickup = -1;
		double arrival_delivery = -1;
		double departure_delivery = -1;
		int end_work_time_vehicle = -1;
		if (vh.getEndWorkingTime() != null)
			end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
					.getEndWorkingTime());
		int load_time = 0;
		int unload_time = 0;
		int load = 0;

		ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();

		while (decreasing_weight_items.size() > 0) {
			int sel_i = -1;
			for (int i = 0; i < decreasing_weight_items.size(); i++) {
				Item I = decreasing_weight_items.get(i);
				int itemIndex = mItem2Index.get(I);
				PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
						.get(itemIndex)];

				if (I.getWeight() + load > vh.getWeight()) {
					continue;
				}

				// check time
				int pickup_early_time = (int) DateTimeUtils.dateTime2Int(r
						.getEarlyPickupTime());
				int pickup_late_time = (int) DateTimeUtils.dateTime2Int(r
						.getLatePickupTime());
				int delivery_early_time = (int) DateTimeUtils.dateTime2Int(r
						.getEarlyDeliveryTime());
				int delivery_late_time = (int) DateTimeUtils.dateTime2Int(r
						.getLateDeliveryTime());

				if (arrival_pickup > pickup_late_time)
					continue;
				departure_pickup = max(arrival_pickup, pickup_early_time)
						+ load_time + fix_load_time;
				arrival_delivery = departure_pickup + travel_time;
				if (arrival_delivery > delivery_late_time)
					continue;
				departure_delivery = max(arrival_delivery, delivery_early_time)
						+ unload_time + fix_unload_time;

				double arrival_depot = departure_delivery
						+ a_travelTime[deliveryLocationIndex][endLocationIndexVehicle];
				if (arrival_depot > end_work_time_vehicle
						&& end_work_time_vehicle >= 0)
					continue;

				// check exclusive items

				boolean conflict = false;
				for (ItemAmount ia : IA) {
					int ii = ia.itemIndex;
					if (itemConflict[ii][itemIndex]) {
						conflict = true;
						break;
					}
				}
				if (conflict)
					continue;

				load_time += I.getPickupDuration();
				unload_time += I.getDeliveryDuration();
				load += I.getWeight();

				sel_i = i;
				break;
			}
			if (sel_i > -1) {
				Item I = decreasing_weight_items.get(sel_i);
				int itemIndex = mItem2Index.get(I);
				IA.add(new ItemAmount(itemIndex, (int) I.getWeight()));
				decreasing_weight_items.remove(sel_i);
				collectItems.add(I);
				// System.out.println("CONSIDER item " + I.getCode() + ","
				// + I.getWeight());
				if (log != null)
					log.println("CONSIDER item " + I.getCode() + ","
							+ I.getWeight());
			} else {
				break;
			}
		}

		if (IA.size() > 0 && decreasing_weight_items.size() == 0) {// try load
																	// all
			return true;
		}
		// System.out.println("CONSIDER KOKOKOKOKO-----------------------");
		// restore list
		decreasing_weight_items.clear();
		for (int i = 0; i < storeL.size(); i++)
			decreasing_weight_items.add(storeL.get(i));
		collectItems.clear();
		return false;
	}

	public Vehicle findVehicleCategoryCanLoad(int pickuplocationIndex,
			int deliveryLocationIndex, Item I) {
		ArrayList<Item> a_items = new ArrayList<Item>();
		a_items.add(I);
		int idx = mItem2Index.get(I);
		int idxReq = mItemIndex2RequestIndex.get(idx);
		PickupDeliveryRequest r = requests[idxReq];
		int fix_load_time = r.getFixLoadTime();
		int fix_unload_time = r.getFixUnloadTime();
		return findVehicleCategoryCanLoadAllFTLToVehicle(pickuplocationIndex,
				deliveryLocationIndex, a_items, fix_load_time, fix_unload_time);
	}

	public Vehicle findVehicleCategoryCanLoadAllFTLToVehicle(
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			totalW += I.getWeight();
		}
		Vehicle sel_vh = null;
		double minW = Integer.MAX_VALUE;
		for (int k = 0; k < input.getVehicleCategories().length; k++) {
			Vehicle vh = input.getVehicleCategories()[k];
			boolean ok = canLoadAllFTLToVehicleCategory(vh,
					pickuplocationIndex, deliveryLocationIndex,
					decreasing_weight_items, fix_load_time, fix_unload_time);
			if (ok) {
				if (vh.getWeight() > minW) {
					sel_vh = vh;
					minW = vh.getWeight();
				}
			}
		}
		return sel_vh;
	}

	public Trip createTripVehicleCategoryCanLoadAllFTLToVehicle(
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			totalW += I.getWeight();
		}
		Vehicle sel_vh = null;
		Trip sel_trip = null;
		double minW = Integer.MAX_VALUE;
		for (int k = 0; k < input.getVehicleCategories().length; k++) {
			Vehicle vh = input.getVehicleCategories()[k];
			Trip t = createTripLoadAllFTLToVehicleCategory(vh,
					pickuplocationIndex, deliveryLocationIndex,
					decreasing_weight_items, fix_load_time, fix_unload_time);
			if (t != null) {
				if (vh.getWeight() > minW) {
					sel_vh = vh;
					sel_trip = t;
					minW = vh.getWeight();
				}
			}
		}
		return sel_trip;
	}

	public int findVehicleCanLoadAllFTLToVehicle(int pickuplocationIndex,
			int deliveryLocationIndex, ArrayList<Item> decreasing_weight_items,
			int fix_load_time, int fix_unload_time) {
		double totalW = 0;
		for (Item I : decreasing_weight_items) {
			totalW += I.getWeight();
		}
		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);
		String debugLocationCode = "60005547";
		int sel_vh_index = -1;
		double minW = Integer.MAX_VALUE;
		// for(int k = 0; k < input.getVehicleCategories().length; k++){
		// Vehicle vh = input.getVehicleCategories()[k];
		int nbVehicles = input.getVehicles().length
				+ input.getExternalVehicles().length;
		for (int vehicle_idx = 0; vehicle_idx < nbVehicles; vehicle_idx++) {
			// boolean ok = canLoadAllFTLToVehicleCategory(vh,
			// pickuplocationIndex, deliveryLocationIndex,
			// decreasing_weight_items, fix_load_time, fix_unload_time);
			boolean ok = canLoadAllFTLToVehicle(vehicle_idx,
					pickuplocationIndex, deliveryLocationIndex,
					decreasing_weight_items, fix_load_time, fix_unload_time);
			if (ok) {
				Vehicle vh = getVehicle(vehicle_idx);
				if (vh.getWeight() < minW) {
					sel_vh_index = vehicle_idx;
					minW = vh.getWeight();

					if (deliveryLocationCode.equals(debugLocationCode)) {
						log(name()
								+ "::findVehicleCanLoadAllFTLToVehicle, items.sz = "
								+ decreasing_weight_items.size()
								+ ", FOUND VEHICLE " + sel_vh_index
								+ ", weigth = " + vh.getWeight()
								+ ", totalW = " + totalW);
					}
				}
			}
		}
		return sel_vh_index;
	}

	public int[] sortDecreaseVehicleIndex(Vehicle[] v) {
		if (v == null)
			return null;
		int[] idx = new int[v.length];
		double[] w = new double[v.length];
		for (int i = 0; i < v.length; i++) {
			idx[i] = i;
			w[i] = v[i].getWeight();
		}
		for (int i = 0; i < w.length; i++) {
			for (int j = i + 1; j < w.length; j++) {
				if (w[i] < w[j]) {
					double tw = w[i];
					w[i] = w[j];
					w[j] = tw;
					int ti = idx[i];
					idx[i] = idx[j];
					idx[j] = ti;
				}
			}
		}
		return idx;
	}

	public void removeScheduledItem(Item I) {
		int idx = mItem2Index.get(I);
		int idxReq = mItemIndex2RequestIndex.get(idx);
		PickupDeliveryRequest r = requests[idxReq];
		removeItemFromRequest(r, I);
	}

	public void removeItemFromRequest(PickupDeliveryRequest r, Item I) {
		ArrayList<Item> L = new ArrayList<Item>();
		for (int i = 0; i < r.getItems().length; i++) {
			if (r.getItems()[i] != I) {
				L.add(r.getItems()[i]);
			}
		}
		Item[] new_items = new Item[L.size()];
		for (int i = 0; i < L.size(); i++)
			new_items[i] = L.get(i);
		r.setItems(new_items);
	}

	public void printRequestsOfDistinctLocations() {
		System.out
				.println(name()
						+ "::printRequestsOfDistinctLocations, distinct_pickupLocationCodes = "
						+ distinct_pickupLocationCodes.size());

		for (int i = 0; i < distinct_pickupLocationCodes.size(); i++) {
			String plc = distinct_pickupLocationCodes.get(i);
			String dlc = distinct_deliveryLocationCodes.get(i);
			System.out.println("FROM " + plc + " TO " + dlc);
			double W = 0;
			for (int j : distinct_request_indices.get(i)) {
				PickupDeliveryRequest r = requests[j];
				System.out
						.print("r[" + j + "], order " + r.getOrderID() + ": ");
				double w = 0;
				for (int k = 0; k < r.getItems().length; k++) {
					Item I = r.getItems()[k];
					System.out.print("[code" + I.getCode() + ","
							+ I.getWeight() + "] ");
					w += I.getWeight();
				}
				// System.out.println();
				System.out.println("w request = " + w);
				W += w;
			}
			System.out.println("Weight Of Location " + dlc + " = " + W);
			System.out.println("-----------------");
		}
	}

	public String toStringVehicle(int vh_index) {
		String s = "";
		Vehicle vh = getVehicle(vh_index);
		s = "Vehicle["
				+ vh_index
				+ "], "
				+ vh.getCode()
				+ ", weight "
				+ vh.getWeight()
				+ ", lastDepTime = "
				+ DateTimeUtils
						.unixTimeStamp2DateTime(getLastDepartureTimeOfVehicle(vh_index))
				+ " lastLocation = "
				+ locationCodes.get(getLastLocationIndex(vh_index));
		return s;
	}

	public void logVehicles() {
		for (int i = 0; i < input.getVehicles().length; i++) {
			// Vehicle vh = input.getVehicles()[i];
			log(name() + "::logVehicles, internal vehicle "
					+ toStringVehicle(i));
		}
		for (int i = 0; i < input.getExternalVehicles().length; i++) {
			log(name() + "::logVehicles, external vehicle "
					+ toStringVehicle(i + input.getVehicles().length));
		}
	}

	public void processSeparateItemsAtLocation(String pickuplocationCode,
			String deliveryLocationCode, HashSet<Integer> RI) {
		// separate items delivery at location deliveryLocationCode when total
		// sum of items
		// exceeds the capacity of individual vehicle
		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);

		String debugLocationCode = "60005547";

		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++)
				a_items.add(requests[i].getItems()[j]);
		}
		a_items = sortDecreasing(a_items);

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name() + "::processSeparateItemsAtLocation, items = ");
			for (Item I : a_items)
				log(name() + "::processSeparateItemsAtLocation AVEC I = "
						+ I.getCode() + ", weight = " + I.getWeight());
		}
		logVehicles();

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (int i : RI) {
			fix_load_time = requests[i].getFixLoadTime();
			fix_unload_time = requests[i].getFixUnloadTime();
			break;
		}
		if (fix_load_time < 0)
			return;
		while (a_items.size() > 0) {

			int vh_index_trial = findVehicleCanLoadAllFTLToVehicle(pi, di,
					a_items, fix_load_time, fix_unload_time);
			if (vh_index_trial >= 0) {
				if (deliveryLocationCode.equals(debugLocationCode)) {
					log(name()
							+ "::processSeparateItemsAtLocation, can load all -> BREAK");
				}
				break;
			}

			int sel_i = -1;
			int sel_vh_index = -1;
			ArrayList<Item> sel_items = null;
			for (int i = 0; i < a_items.size(); i++) {
				ArrayList<Item> s_items = new ArrayList<Item>();
				for (int j = 0; j <= i; j++) {
					s_items.add(a_items.get(j));
				}
				// Vehicle vh = findVehicleCategoryCanLoadAllFTLToVehicle(pi,
				// di, s_items, fix_load_time, fix_unload_time);
				int vh_index = findVehicleCanLoadAllFTLToVehicle(pi, di,
						s_items, fix_load_time, fix_unload_time);
				if (vh_index < 0) {
					break;
				} else {
					sel_i = i;
					sel_vh_index = vh_index;
					sel_items = s_items;
				}
			}
			if (sel_i >= 0) {
				// create trip for vh serving items 0,1,..,sel_i
				ArrayList<ItemAmount> IA = new ArrayList<ItemAmount>();
				int early_pickup_time = -1;
				int early_delivery_time = -1;
				for (Item I : sel_items) {
					int itemIndex = mItem2Index.get(I);
					ItemAmount ia = new ItemAmount(itemIndex, I.getWeight());
					IA.add(ia);

					int idxReq = mItemIndex2RequestIndex.get(itemIndex);
					PickupDeliveryRequest r = requests[idxReq];
					int early = (int) DateTimeUtils.dateTime2Int(r
							.getEarlyPickupTime());
					if (early_pickup_time < early)
						early_pickup_time = early;
					early = (int) DateTimeUtils.dateTime2Int(r
							.getEarlyDeliveryTime());
					if (early_delivery_time < early)
						early_delivery_time = early;
				}
				Vehicle vh = getVehicle(sel_vh_index);
				int startTime = getLastDepartureTimeOfVehicle(sel_vh_index);
				int start_location_index = getLastLocationIndex(sel_vh_index);

				int sel_arrival_pickup = startTime
						+ (int) a_travelTime[start_location_index][pi];
				int start_service_time = MAX(sel_arrival_pickup,
						early_pickup_time);
				int pickup_duration = fix_load_time;
				for (Item I : sel_items)
					pickup_duration += I.getPickupDuration();
				int sel_departure_pickup = start_service_time + pickup_duration;

				int sel_arrival_delivery = sel_departure_pickup
						+ (int) a_travelTime[pi][di];
				start_service_time = MAX(sel_arrival_delivery,
						early_delivery_time);
				int delivery_duration = fix_unload_time;
				for (Item I : sel_items)
					pickup_duration += I.getDeliveryDuration();
				int sel_departure_delivery = start_service_time
						+ delivery_duration;

				RouteNode start = new RouteNode(pi, (int) sel_arrival_pickup,
						(int) sel_departure_pickup, IA, sel_vh_index, "P");
				RouteNode end = new RouteNode(di, (int) sel_arrival_delivery,
						(int) sel_departure_delivery, IA, sel_vh_index, "D");

				start.solver = this;
				end.solver = this;

				Trip t = new Trip(start, end, "COLLECT_TO_FTL");
				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(t, items_of_trip);
				log(name() + "::processSeparateItemsAtLocation, SEPARATE trip "
						+ t.toString());

				if (!t.checkTime()) {
					log(name()
							+ "::loadFTLprocessSeparateItemsAtLocationToVehicle, BUG checkTime????");
					log(name()
							+ "::processSeparateItemsAtLocation, BUG Vehicle = "
							+ getVehicle(sel_vh_index).getCode()
							+ ", INFO TRIP = " + t.toString());
				}
				trips[sel_vh_index].add(t);

				for (int j = 0; j < sel_i; j++) {
					a_items.remove(0);
				}

				for (Item ite : sel_items) {
					PickupDeliveryRequest r = getRequestOfItem(ite);
					removeItemFromRequest(r, ite);
				}
			} else {
				log(name()
						+ "::processSeparateItemsAtLocation, sel_i = -1, CANNOT SEPARATE ANYTHING");
				break;
			}
		}
	}

	public ArrayList<Trip> processSeparateItemsAtLocationWithVehicleCategory(
			String pickuplocationCode, String deliveryLocationCode,
			HashSet<Item> inp_items) {
		// separate items delivery at location deliveryLocationCode when total
		// sum of items
		// exceeds the capacity of individual vehicle
		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);

		String debugLocationCode = "60005547";
		ArrayList<Trip> T = new ArrayList<Trip>();

		ArrayList<Item> a_items = new ArrayList<Item>();
		for (Item I : inp_items)
			a_items.add(I);

		a_items = sortDecreasing(a_items);

		if (deliveryLocationCode.equals(debugLocationCode)) {
			log(name()
					+ "::processSeparateItemsAtLocationWithVehicleCategory, items = ");
			for (Item I : a_items)
				log(name()
						+ "::processSeparateItemsAtLocationWithVehicleCategory AVEC I = "
						+ I.getCode() + ", weight = " + I.getWeight());
		}
		logVehicles();

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (Item I : inp_items) {
			PickupDeliveryRequest r = getRequestOfItem(I);
			fix_load_time = r.getFixLoadTime();
			fix_unload_time = r.getFixUnloadTime();
			break;
		}

		if (fix_load_time < 0)
			return T;
		while (a_items.size() > 0) {

			Trip t = createTripVehicleCategoryCanLoadAllFTLToVehicle(pi, di,
					a_items, fix_load_time, fix_unload_time);
			if (t != null) {
				if (deliveryLocationCode.equals(debugLocationCode)) {
					log(name()
							+ "::processSeparateItemsAtLocationWithVehicleCategory, can load all -> create new vehicle-trip BREAK");
				}
				T.add(t);
				break;
			}

			int sel_i = -1;
			int sel_vh_index = -1;
			Trip sel_trip = null;
			ArrayList<Item> sel_items = null;
			for (int i = 0; i < a_items.size(); i++) {
				ArrayList<Item> s_items = new ArrayList<Item>();
				for (int j = 0; j <= i; j++) {
					s_items.add(a_items.get(j));
				}
				// Vehicle vh = findVehicleCategoryCanLoadAllFTLToVehicle(pi,
				// di, s_items, fix_load_time, fix_unload_time);
				t = createTripVehicleCategoryCanLoadAllFTLToVehicle(pi, di,
						s_items, fix_load_time, fix_unload_time);
				if (t == null) {
					break;
				} else {
					sel_i = i;
					sel_trip = t;
					sel_items = s_items;
				}
			}
			if (sel_trip != null) {
				T.add(sel_trip);

				for (int j = 0; j < sel_i; j++) {
					a_items.remove(0);
				}
			} else {
				log(name()
						+ "::processSeparateItemsAtLocationWithVehicleCategory, sel_i = -1, CANNOT SEPARATE ANYTHING");
				break;
			}
		}
		inp_items.clear();
		for (Item I : a_items)
			inp_items.add(I);

		return T;
	}

	public PickupDeliveryRequest getRequestOfItem(Item I) {
		int idx = mItem2Index.get(I);
		int idxReq = mItemIndex2RequestIndex.get(idx);
		return requests[idxReq];
	}

	public void processMergeOrderItemsFTL(String pickuplocationCode,
			String deliveryLocationCode, HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode

		System.out.println(name() + "::processMergeOrderItemsFTL("
				+ pickuplocationCode + "," + deliveryLocationCode
				+ ", RI.sz = " + RI.size() + ")");
		log(name() + "::processMergeOrderItemsFTL(" + pickuplocationCode + ","
				+ deliveryLocationCode + ", RI.sz = " + RI.size() + ")");

		// System.out.println(name() + "::processMergeOrderItemsFTL, RI.sz = " +
		// RI.size());

		String debug_delivery_location_code = "";

		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);
		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				Item I = requests[i].getItems()[j];
				a_items.add(I);
				if (deliveryLocationCode.equals(debug_delivery_location_code)) {
					log(name() + "::processMergeOrderItemsFTL, item "
							+ I.getCode() + ", " + I.getWeight());
				}
			}
		}
		a_items = sortDecreasing(a_items);

		ClusterItems CI = new ClusterItems(a_items);
		clusterItems.add(CI);
		mCluster2Index.put(CI, clusterItems.size() - 1);

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (int i : RI) {
			fix_load_time = requests[i].getFixLoadTime();
			fix_unload_time = requests[i].getFixUnloadTime();
			break;
		}
		if (fix_load_time < 0)
			return;
		int[] idx1 = sortDecreaseVehicleIndex(vehicles);
		int[] idx2 = sortDecreaseVehicleIndex(externalVehicles);
		int[] vehicle_index = new int[idx1.length + idx2.length];
		for (int i = 0; i < idx1.length; i++)
			vehicle_index[i] = idx1[i];
		for (int i = 0; i < idx2.length; i++)
			vehicle_index[i + idx1.length] = idx2[i];

		// System.out.println(name()
		// + "::processMergeOrderItemsFTL, nbVehicles = "
		// + vehicle_index.length);

		int k = 0;
		while (k < vehicle_index.length) {
			if (a_items.size() == 0)
				break;

			if (deliveryLocationCode.equals(debug_delivery_location_code)
					|| debug_delivery_location_code == null) {
				double W = 0;
				for (Item I : a_items)
					W += I.getWeight();

				if (deliveryLocationCode.equals(debug_delivery_location_code)) {
					System.out
							.print(name()
									+ "::processMergeOrderItemsFTL -> prepare LoadFTL for vehicle "
									+ getVehicle(vehicle_index[k]).getCode()
									+ ", cap = "
									+ getVehicle(vehicle_index[k]).getWeight()
									+ " a_items = " + a_items.size() + ", W = "
									+ W + ", items = ");
					for (int jj = 0; jj < a_items.size(); jj++) {
						System.out.print(a_items.get(jj).getWeight() + ", ");
					}
					System.out.println();
				}

			}

			ArrayList<Trip> T = new ArrayList<Trip>();
			for (int k1 = 0; k1 < vehicle_index.length; k1++) {
				Trip t = createTripLoadAllFTLToVehicle(vehicle_index[k1], pi,
						di, a_items, fix_load_time, fix_unload_time);
				if (t != null)
					T.add(t);
			}
			if (T.size() > 0) {
				// ClusterItems cl = new ClusterItems(a_items);
				// clusterItems.add(cl);
				for (Trip t : T) {
					int vh_idx = t.start.vehicleIndex;
					matchTrips[vh_idx][clusterItems.size() - 1] = t;
				}
				System.out
						.println(name()
								+ "::processMergeOrderItemsFTL, FOUND createTripLoadAllFTLToVehicle!!!!!!!");
				return;
			}

			if (deliveryLocationCode.equals(debug_delivery_location_code))
				log(name()
						+ "::::processMergeOrderItemsFTL, prepare LoadFTL for vehicle "
						+ getVehicle(vehicle_index[k]).getCode()
						+ ", a_items = " + a_items.size());

			ArrayList<Item> collectItems = loadFTLToVehicle(vehicle_index[k],
					pi, di, a_items, fix_load_time, fix_unload_time);

			if (collectItems.size() == 0) {
				System.out.println(name()
						+ "::processMergeOrderItemsFTL, vehicle " + k
						+ " FAILED LOAD");
				k++;
				// System.out
				// .println("FAILED -> TRY LoadFTL for new vehicle, a_items = "
				// + a_items.size());
			} else {
				if (log != null) {
					Vehicle vh = getVehicle(vehicle_index[k]);
					if (deliveryLocationCode
							.equals(debug_delivery_location_code))
						log.println(name()
								+ "::processMergeOrderItemsFTL, LOAD-FTL for vehicle "
								+ vh.getCode() + "-" + vh.getVehicleCategory()
								+ ", weight " + vh.getWeight()
								+ ", remain weight = " + getTotalItemWeight());
				}
				System.out
						.println(name()
								+ "::processMergeOrderItemsFTL, LOAD-FTL, remain weight = "
								+ getTotalItemWeight());

				if (deliveryLocationCode.equals(debug_delivery_location_code)
						|| debug_delivery_location_code == null) {
					System.out.print(name() + "::processMergeOrderItemsFTL at "
							+ deliveryLocationCode
							+ " -> LoadFTL success for vehicle "
							+ getVehicle(vehicle_index[k]).getCode()
							+ ", cap = "
							+ getVehicle(vehicle_index[k]).getWeight()
							+ ", collectItems = " + collectItems.size() + ": ");
					double w = 0;
					for (int jj = 0; jj < collectItems.size(); jj++) {
						System.out.print(collectItems.get(jj).getWeight()
								+ ", ");
						w += collectItems.get(jj).getWeight();
					}
					System.out.println(", total served weight = " + w);
				}

				for (Item ite : collectItems) {
					int idx = mItem2Index.get(ite);
					PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
							.get(idx)];
					// remove Item
					removeItemFromRequest(r, ite);
				}
			}
		}
	}

	public void processMergeOrderItemsFTLNoConstraints(
			String pickuplocationCode, String deliveryLocationCode,
			HashSet<Integer> RI) {

		String debug_delivery_location_code = "";

		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);
		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				Item I = requests[i].getItems()[j];
				a_items.add(I);
				if (deliveryLocationCode.equals(debug_delivery_location_code)) {
					log(name()
							+ "::processMergeOrderItemsFTLNoConstraints, item "
							+ I.getCode() + ", " + I.getWeight());
				}
			}
		}
		a_items = sortDecreasing(a_items);

		ClusterItems CI = new ClusterItems(a_items);
		clusterItems.add(CI);
		mCluster2Index.put(CI, clusterItems.size() - 1);

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (int i : RI) {
			fix_load_time = requests[i].getFixLoadTime();
			fix_unload_time = requests[i].getFixUnloadTime();
			break;
		}
		System.out.println(name()
				+ "::processMergeOrderItemsFTLNoConstraints, fix_load_time = "
				+ fix_load_time);

		if (fix_load_time < 0)
			return;

		int[] vehicle_index = { 0 };

		/*
		 * 
		 * ArrayList<Trip> T = new ArrayList<Trip>(); for (int k1 = 0; k1 <
		 * vehicle_index.length; k1++) { Trip t =
		 * createTripLoadAllFTLToVehicle(vehicle_index[k1], pi, di, a_items,
		 * fix_load_time, fix_unload_time); if (t != null) T.add(t); } if
		 * (T.size() > 0) { // ClusterItems cl = new ClusterItems(a_items); //
		 * clusterItems.add(cl); for (Trip t : T) { int vh_idx =
		 * t.start.vehicleIndex; matchTrips[vh_idx][clusterItems.size() - 1] =
		 * t; } return; }
		 */

		ArrayList<Item> collectItems = loadFTLToVehicleNoConstraints(
				vehicle_index[0], pi, di, a_items, fix_load_time,
				fix_unload_time);

		if (log != null) {
			Vehicle vh = getVehicle(vehicle_index[0]);
			if (deliveryLocationCode.equals(debug_delivery_location_code))
				log.println(name()
						+ "::processMergeOrderItemsFTLNoConstraints, LOAD-FTL for vehicle "
						+ vh.getCode() + "-" + vh.getVehicleCategory()
						+ ", weight " + vh.getWeight() + ", remain weight = "
						+ getTotalItemWeight());
		}
		System.out
				.println(name()
						+ "::processMergeOrderItemsFTLNoConstraints, LOAD-FTL, remain weight = "
						+ getTotalItemWeight());

		for (Item ite : collectItems) {
			int idx = mItem2Index.get(ite);
			PickupDeliveryRequest r = requests[mItemIndex2RequestIndex.get(idx)];
			// remove Item
			removeItemFromRequest(r, ite);
		}

	}

	public ArrayList<Trip> processSeparateItemsAtEachLocationWithVehicleCategory(
			HashSet<Item> unscheduledItems) {
		// HashSet<Item> unscheduledItems = new HashSet<Item>();
		ArrayList<Trip> L = new ArrayList<Trip>();
		for (int i : unScheduledPointIndices) {
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			String pickuplocationCode = mPoint2LocationCode.get(pickup);
			String deliveryLocationCode = mPoint2LocationCode.get(delivery);
			Integer[] I = mPoint2IndexItems.get(pickup);
			HashSet<Item> inp_items = new HashSet<Item>();
			for (int j = 0; j < I.length; j++) {
				inp_items.add(items.get(I[j]));
			}

			ArrayList<Trip> T = processSeparateItemsAtLocationWithVehicleCategory(
					pickuplocationCode, deliveryLocationCode, inp_items);
			for (Trip t : T)
				L.add(t);
		}
		return L;
	}

	public void processSeparateItemsAtEachLocation() {
		processDistinctPickupDeliveryLocationCodes();

		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			System.out.println(name() + "::processMergeOrderItemsFTL(), from "
					+ distinct_pickupLocationCodes.get(i) + " --> "
					+ distinct_deliveryLocationCodes.get(i) + ", RI = "
					+ distinct_request_indices.get(i).size());
			for (int j : distinct_request_indices.get(i)) {
				System.out.print(j + ",");
			}
			System.out.println();
			for (int j : distinct_request_indices.get(i)) {
				System.out.print("req[" + j + "]: ");
				for (int jj = 0; jj < requests[j].getItems().length; jj++)
					System.out.print("[Order" + requests[j].getOrderID()
							+ ", Item" + requests[j].getItems()[jj].getCode()
							+ "," + requests[j].getItems()[jj].getWeight()
							+ "] ");
				System.out.println();
			}

			processSeparateItemsAtLocation(distinct_pickupLocationCodes.get(i),
					distinct_deliveryLocationCodes.get(i),
					distinct_request_indices.get(i));

		}

	}

	public void processDistinctPickupDeliveryLocationCodes() {
		mLocationCode2RequestIndex = new HashMap<String, HashSet<Integer>>();
		distinct_pickupLocationCodes = new ArrayList<String>();
		distinct_deliveryLocationCodes = new ArrayList<String>();
		distinct_request_indices = new ArrayList<HashSet<Integer>>();
		intCityPickupLocations = new ArrayList<String>();
		intCityDeliveryLocations = new ArrayList<String>();
		extCityPickupLocations = new ArrayList<String>();
		extCityDeliveryLocations = new ArrayList<String>();
		//mLocation2Items = new HashMap<String, ArrayList<Item>>();
		
		for (int i = 0; i < requests.length; i++) {
			PickupDeliveryRequest r = requests[i];
			int idx = -1;
			for (int j = 0; j < distinct_pickupLocationCodes.size(); j++) {
				if (r.getPickupLocationCode().equals(
						distinct_pickupLocationCodes.get(j))
						&& r.getDeliveryLocationCode().equals(
								distinct_deliveryLocationCodes.get(j))) {
					idx = j;
					break;
				}
			}
			if (idx != -1) {
				//for(Item I: r.getItems())
				//	mLocation2Items.get(r.getDeliveryLocationCode()).add(I);
				
				distinct_request_indices.get(idx).add(i);
				// System.out.println("processMergeOrderItemsFTL, add request "
				// + i + " to " + distinct_pickupLocationCodes.get(idx)
				// + "--" + distinct_deliveryLocationCodes.get(idx)
				// + ", sz = " + distinct_request_indices.get(idx).size());
			} else {
				//mLocation2Items.put(r.getDeliveryLocationCode(), new ArrayList<Item>());
				
				distinct_pickupLocationCodes.add(r.getPickupLocationCode());
				distinct_deliveryLocationCodes.add(r.getDeliveryLocationCode());
				if (mLocation2Type.get(r.getDeliveryLocationCode()).equals(
						NOI_THANH)) {
					intCityPickupLocations.add(r.getPickupLocationCode());
					intCityDeliveryLocations.add(r.getDeliveryLocationCode());
				} else {
					extCityPickupLocations.add(r.getPickupLocationCode());
					extCityDeliveryLocations.add(r.getDeliveryLocationCode());
				}
				HashSet<Integer> S = new HashSet<Integer>();
				S.add(i);
				distinct_request_indices.add(S);

				/*
				 * System.out.println("processMergeOrderItemsFTL, add request "
				 * + i + " to NEW " + distinct_pickupLocationCodes
				 * .get(distinct_pickupLocationCodes.size() - 1) + "--" +
				 * distinct_deliveryLocationCodes
				 * .get(distinct_deliveryLocationCodes.size() - 1) + ", sz = " +
				 * distinct_request_indices.get( distinct_request_indices.size()
				 * - 1).size());
				 */
			}
		}
		
		
		// System.out.println("processMergeOrderItemsFTL, requests.sz = "
		// + requests.length + ", distinct.sz = "
		// + distinct_pickupLocationCodes.size());

	}

	/*
	 * PQD -> OLD public void
	 * processMergeOrderItemsFTLInternalVehicleFIRST(String pickuplocationCode,
	 * String deliveryLocationCode, HashSet<Integer> RI) { // pre-schedule FTL
	 * for items of requests in RI from pickupLocationCode // ->
	 * deliveryLocationCode
	 * 
	 * //System.out.println(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST(" + //pickuplocationCode
	 * + "," + deliveryLocationCode + ", RI.sz = " + //RI.size() + ")");
	 * 
	 * 
	 * 
	 * //System.out.println(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST(" + //
	 * pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " + //
	 * RI.size() + ") RBrenntagMultiPickupDelivery");
	 * 
	 * String debug_delivery_location_code = "";
	 * 
	 * int pi = mLocationCode2Index.get(pickuplocationCode); int di =
	 * mLocationCode2Index.get(deliveryLocationCode); ArrayList<Item> a_items =
	 * new ArrayList<Item>(); for (int i : RI) { for (int j = 0; j <
	 * requests[i].getItems().length; j++) { Item I = requests[i].getItems()[j];
	 * a_items.add(I); if
	 * (deliveryLocationCode.equals(debug_delivery_location_code)) { log(name()
	 * + "::processMergeOrderItemsFTLInternalVehicleFIRST, item " + I.getCode()
	 * + ", " + I.getWeight()); } } } a_items = sortDecreasing(a_items);
	 * 
	 * ClusterItems CI = new ClusterItems(a_items); clusterItems.add(CI);
	 * mCluster2Index.put(CI, clusterItems.size() - 1);
	 * 
	 * int fix_load_time = -1; int fix_unload_time = -1; for (int i : RI) {
	 * fix_load_time = requests[i].getFixLoadTime(); fix_unload_time =
	 * requests[i].getFixUnloadTime(); break; } if (fix_load_time < 0) return;
	 * int[] idx1 = sortDecreaseVehicleIndex(vehicles); int[] idx2 =
	 * sortDecreaseVehicleIndex(externalVehicles); int len1 = 0; if(idx1 !=
	 * null) len1 = idx1.length; int len2 = 0; if (idx2 != null) len2 =
	 * idx2.length; int[] vehicle_index = new int[len1 + len2]; for (int i = 0;
	 * i < len1; i++) vehicle_index[i] = idx1[i];
	 * 
	 * for (int i = 0; i < len2; i++) vehicle_index[i + len1] = idx2[i] + len1;
	 * 
	 * int nbVehicles = vehicle_index.length;
	 * 
	 * log(name() + "::processMergeOrderItemsFTLInternalVehicleFIRST(" +
	 * pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
	 * RI.size() + ") location-type " + mLocation2Type.get(deliveryLocationCode)
	 * + ", nbInternalvehicle = " + len1 + ", nbExternalVehicle = " + len2);
	 * 
	 * int k = 0;
	 * 
	 * while (k < vehicle_index.length) { if (a_items.size() == 0) break;
	 * 
	 * 
	 * 
	 * 
	 * ArrayList<Trip> T = new ArrayList<Trip>(); if(idx1 != null)for (int k1 =
	 * 0; k1 < idx1.length; k1++) { Trip t =
	 * createTripLoadAllFTLToVehicle(idx1[k1], pi, di, a_items, fix_load_time,
	 * fix_unload_time); Vehicle vh = getVehicle(idx1[k1]); if (t != null){
	 * T.add(t);
	 * 
	 * //log(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
	 * + vh.getCode()); } } if(T.size() == 0){ // consider external vehicles
	 * if(idx2 != null)for (int k1 = 0; k1 < idx2.length; k1++) { Trip t =
	 * createTripLoadAllFTLToVehicle(idx2[k1], pi, di, a_items, fix_load_time,
	 * fix_unload_time); Vehicle vh = getVehicle(idx2[k1]); if (t != null){
	 * T.add(t);
	 * 
	 * //log(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
	 * + vh.getCode()); } } }
	 * 
	 * 
	 * 
	 * if (T.size() > 0) { // ClusterItems cl = new ClusterItems(a_items); //
	 * clusterItems.add(cl); Trip sel_trip = null; double minW =
	 * Integer.MAX_VALUE; for (Trip t : T) { int vh_idx = t.start.vehicleIndex;
	 * matchTrips[vh_idx][clusterItems.size() - 1] = t;
	 * 
	 * Vehicle vh = getVehicle(vh_idx); // System.out.println(name() // +
	 * "::processMergeOrderItemsFTL, T.sz = " + T.size() + //
	 * ", vehicle weight = " + vh.getWeight() // + ", MAX_INT = " + minW);
	 * 
	 * if (vh.getWeight() / 1000 < minW) { minW = vh.getWeight() / 1000;
	 * sel_trip = t; } } // System.out.println(name() // +
	 * "::processMergeOrderItemsFTL, T.sz = " + T.size());
	 * 
	 * // System.out.println(name() // +
	 * "::processMergeOrderItemsFTL, vehicle = " + //
	 * sel_trip.start.vehicleIndex);
	 * 
	 * int remainItems = computeRemainItemOnRequest();
	 * 
	 * Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
	 * trips[sel_trip.start.vehicleIndex].add(sel_trip); log(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips[" +
	 * vh.getCode() + "].sz = " + trips[sel_trip.start.vehicleIndex].size() +
	 * ", vehicle capacity " + vh.getWeight() + ", load = " +
	 * sel_trip.computeTotalItemWeight() + ", nbItems = " +
	 * sel_trip.getItems().size() + ", deliveryLocation " + deliveryLocationCode
	 * + "-" + mLocation2Type.get(deliveryLocationCode) + ", remainItems = " +
	 * remainItems); //System.out.println(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips[" +
	 * vh.getCode() + "].sz = " + //trips[sel_trip.start.vehicleIndex].size());
	 * //System.out.println(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST, VEHICLE " +
	 * sel_trip.getVh().getCode());
	 * 
	 * ArrayList<ItemAmount> IA = sel_trip.start.items; Item[] items_of_trip =
	 * new Item[IA.size()]; for (int ii = 0; ii < IA.size(); ii++) {
	 * items_of_trip[ii] = items.get(IA.get(ii).itemIndex); }
	 * mTrip2Items.put(sel_trip, items_of_trip);
	 * 
	 * for (Item I : a_items) { removeScheduledItem(I); } return; }
	 * 
	 * 
	 * //if (deliveryLocationCode.equals(debug_delivery_location_code)) double
	 * itemWeights = 0; for(Item ia: a_items) itemWeights += ia.getWeight();
	 * 
	 * log(name() +
	 * "::::processMergeOrderItemsFTLInternalVehicleFIRST, prepare LoadFTL for vehicle["
	 * + k + "/" + vehicle_index.length + "] " +
	 * getVehicle(vehicle_index[k]).getCode() + ", capacity = " +
	 * getVehicle(vehicle_index[k]).getWeight() + ", itemWeights = " +
	 * itemWeights + ", a_items = " + a_items.size());
	 * 
	 * ArrayList<Item> collectItems = loadFTLToVehicle(vehicle_index[k], pi, di,
	 * a_items, fix_load_time, fix_unload_time);
	 * 
	 * if (collectItems.size() == 0) { // System.out.println(name() // +
	 * "::processMergeOrderItemsFTL, vehicle " + k + "/" + // nbVehicles +
	 * " -> loadFTLToVehicle FAILED"); k++; // System.out //
	 * .println("FAILED -> TRY LoadFTL for new vehicle, a_items = " // +
	 * a_items.size()); } else { Trip t =
	 * trips[vehicle_index[k]].get(trips[vehicle_index[k]].size()-1);
	 * 
	 * int remainItems = computeRemainItemOnRequest();
	 * 
	 * log(name() + "::loadFTLToVehicle, ADD-TRIP COLLECT_TO_FTL for vehicle " +
	 * t.getVh().getCode() + ", nbTrips = " + trips[vehicle_index[k]].size() +
	 * ", capacity " + t.getVh().getWeight() + ", load = " +
	 * t.computeTotalItemWeight() + ", nbItems = " + t.getItems().size() +
	 * ", deliveryLocation " + deliveryLocationCode + "-" +
	 * mLocation2Type.get(deliveryLocationCode) + ", remainItems = " +
	 * remainItems);
	 * 
	 * if (deliveryLocationCode.equals(debug_delivery_location_code) ||
	 * debug_delivery_location_code == null) { System.out.print(name() +
	 * "::processMergeOrderItemsFTLInternalVehicleFIRST at " +
	 * deliveryLocationCode + " -> LoadFTL success for vehicle " +
	 * getVehicle(vehicle_index[k]).getCode() + ", cap = " +
	 * getVehicle(vehicle_index[k]).getWeight() + ", collectItems = " +
	 * collectItems.size() + ": "); double w = 0; for (int jj = 0; jj <
	 * collectItems.size(); jj++) {
	 * System.out.print(collectItems.get(jj).getWeight() + ", "); w +=
	 * collectItems.get(jj).getWeight(); }
	 * //System.out.println(", total served weight = " + w); }
	 * 
	 * for (Item ite : collectItems) { int idx = mItem2Index.get(ite);
	 * PickupDeliveryRequest r = requests[mItemIndex2RequestIndex .get(idx)]; //
	 * remove Item removeItemFromRequest(r, ite); } } }
	 * 
	 * }
	 */

	public boolean processMergeOrderItemsFTLInternalVehicleFIRSTNEW(
			String pickuplocationCode, String deliveryLocationCode,
			HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode

		// System.out.println(name() +
		// "::processMergeOrderItemsFTLInternalVehicleFIRST(" +
		// pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		// RI.size() + ")");

		// System.out.println(name() +
		// "::processMergeOrderItemsFTLInternalVehicleFIRST(" +
		// pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		// RI.size() + ") RBrenntagMultiPickupDelivery");

		String debug_delivery_location_code = "";

		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);
		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				Item I = requests[i].getItems()[j];
				a_items.add(I);
				if (deliveryLocationCode.equals(debug_delivery_location_code)) {
					log(name()
							+ "::processMergeOrderItemsFTLInternalVehicleFIRST, item "
							+ I.getCode() + ", " + I.getWeight());
				}
			}
		}
		a_items = sortDecreasing(a_items);

		ClusterItems CI = new ClusterItems(a_items);
		clusterItems.add(CI);
		mCluster2Index.put(CI, clusterItems.size() - 1);

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (int i : RI) {
			fix_load_time = requests[i].getFixLoadTime();
			fix_unload_time = requests[i].getFixUnloadTime();
			break;
		}
		if (fix_load_time < 0)
			return true;
		
		int[] idx1 = sortDecreaseVehicleIndex(vehicles);
		int[] idx2 = sortDecreaseVehicleIndex(externalVehicles);
		int len1 = 0;
		if (idx1 != null)
			len1 = idx1.length;
		int len2 = 0;
		if (idx2 != null)
			len2 = idx2.length;
		int[] vehicle_index = new int[len1 + len2];
		for (int i = 0; i < len1; i++)
			vehicle_index[i] = idx1[i];

		for (int i = 0; i < len2; i++)
			vehicle_index[i + len1] = idx2[i] + len1;

		int nbVehicles = vehicle_index.length;

		log(name() + "::processMergeOrderItemsFTLInternalVehicleFIRST("
				+ pickuplocationCode + "," + deliveryLocationCode
				+ ", RI.sz = " + RI.size() + ") location-type "
				+ mLocation2Type.get(deliveryLocationCode)
				+ ", nbInternalvehicle = " + len1 + ", nbExternalVehicle = "
				+ len2 + ", nbItems = " + a_items.size());

		int lastRemainItems = a_items.size();
		while (true) {
			if(a_items.size() == 0) break;
			
			// FIRST: try to find a vehicle (internal vehicles first) that can
			// served all items at the location
			ArrayList<Trip> T = new ArrayList<Trip>();
			if (idx1 != null)
				for (int k1 = 0; k1 < idx1.length; k1++) {
					Trip t = createTripLoadAllFTLToVehicle(idx1[k1], pi, di,
							a_items, fix_load_time, fix_unload_time);
					Vehicle vh = getVehicle(idx1[k1]);
					if (t != null) {
						T.add(t);

						// log(name() +
						// "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
						// + vh.getCode());
					}
				}
			if (T.size() == 0) {
				// consider external vehicles
				if (idx2 != null)
					for (int k1 = 0; k1 < idx2.length; k1++) {
						Trip t = createTripLoadAllFTLToVehicle(idx2[k1] + len1, pi,
								di, a_items, fix_load_time, fix_unload_time);
						//Vehicle vh = getVehicle(idx2[k1]);
						if (t != null) {
							T.add(t);

							// log(name() +
							// "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
							// + vh.getCode());
						}
					}
			}
			if (T.size() > 0) {
				Trip sel_trip = null;
				double minW = Integer.MAX_VALUE;
				for (Trip t : T) {
					int vh_idx = t.start.vehicleIndex;
					matchTrips[vh_idx][clusterItems.size() - 1] = t;

					Vehicle vh = getVehicle(vh_idx);

					if (vh.getWeight() / 1000 < minW) {
						minW = vh.getWeight() / 1000;
						sel_trip = t;
					}
				}

				int remainItems = computeRemainItemOnRequest();

				Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
				trips[sel_trip.start.vehicleIndex].add(sel_trip);
				log(name()
						+ "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips["
						+ vh.getCode() + "].sz = "
						+ trips[sel_trip.start.vehicleIndex].size()
						+ ", vehicle capacity " + vh.getWeight() + ", load = "
						+ sel_trip.computeTotalItemWeight() + ", nbItems = "
						+ sel_trip.getItems().size() + ", deliveryLocation "
						+ deliveryLocationCode + "-"
						+ mLocation2Type.get(deliveryLocationCode)
						+ ", remainItems = " + remainItems);

				ArrayList<ItemAmount> IA = sel_trip.start.items;
				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(sel_trip, items_of_trip);

				for (Item I : a_items) {
					removeScheduledItem(I);
				}
				return true;
			}

			// SECOND: try to load as full as possible items to vehicles
			// (internal vehicles first, descreasing order of capacity)
			int k = 0;

			boolean hasChanged = false;
			
			while (k < vehicle_index.length) {
				if (a_items.size() == 0)
					break;

				/*
				 * 
				 * ArrayList<Trip> T = new ArrayList<Trip>(); if(idx1 !=
				 * null)for (int k1 = 0; k1 < idx1.length; k1++) { Trip t =
				 * createTripLoadAllFTLToVehicle(idx1[k1], pi, di, a_items,
				 * fix_load_time, fix_unload_time); Vehicle vh =
				 * getVehicle(idx1[k1]); if (t != null){ T.add(t);
				 * 
				 * //log(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
				 * + vh.getCode()); } } if(T.size() == 0){ // consider external
				 * vehicles if(idx2 != null)for (int k1 = 0; k1 < idx2.length;
				 * k1++) { Trip t = createTripLoadAllFTLToVehicle(idx2[k1], pi,
				 * di, a_items, fix_load_time, fix_unload_time); Vehicle vh =
				 * getVehicle(idx2[k1]); if (t != null){ T.add(t);
				 * 
				 * //log(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
				 * + vh.getCode()); } } }
				 * 
				 * 
				 * 
				 * if (T.size() > 0) { // ClusterItems cl = new
				 * ClusterItems(a_items); // clusterItems.add(cl); Trip sel_trip
				 * = null; double minW = Integer.MAX_VALUE; for (Trip t : T) {
				 * int vh_idx = t.start.vehicleIndex;
				 * matchTrips[vh_idx][clusterItems.size() - 1] = t;
				 * 
				 * Vehicle vh = getVehicle(vh_idx); // System.out.println(name()
				 * // + "::processMergeOrderItemsFTL, T.sz = " + T.size() + //
				 * ", vehicle weight = " + vh.getWeight() // + ", MAX_INT = " +
				 * minW);
				 * 
				 * if (vh.getWeight() / 1000 < minW) { minW = vh.getWeight() /
				 * 1000; sel_trip = t; } } // System.out.println(name() // +
				 * "::processMergeOrderItemsFTL, T.sz = " + T.size());
				 * 
				 * // System.out.println(name() // +
				 * "::processMergeOrderItemsFTL, vehicle = " + //
				 * sel_trip.start.vehicleIndex);
				 * 
				 * int remainItems = computeRemainItemOnRequest();
				 * 
				 * Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
				 * trips[sel_trip.start.vehicleIndex].add(sel_trip); log(name()
				 * +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips["
				 * + vh.getCode() + "].sz = " +
				 * trips[sel_trip.start.vehicleIndex].size() +
				 * ", vehicle capacity " + vh.getWeight() + ", load = " +
				 * sel_trip.computeTotalItemWeight() + ", nbItems = " +
				 * sel_trip.getItems().size() + ", deliveryLocation " +
				 * deliveryLocationCode + "-" +
				 * mLocation2Type.get(deliveryLocationCode) + ", remainItems = "
				 * + remainItems); //System.out.println(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips["
				 * + vh.getCode() + "].sz = " +
				 * //trips[sel_trip.start.vehicleIndex].size());
				 * //System.out.println(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, VEHICLE " +
				 * sel_trip.getVh().getCode());
				 * 
				 * ArrayList<ItemAmount> IA = sel_trip.start.items; Item[]
				 * items_of_trip = new Item[IA.size()]; for (int ii = 0; ii <
				 * IA.size(); ii++) { items_of_trip[ii] =
				 * items.get(IA.get(ii).itemIndex); } mTrip2Items.put(sel_trip,
				 * items_of_trip);
				 * 
				 * for (Item I : a_items) { removeScheduledItem(I); } return; }
				 */

				// if
				// (deliveryLocationCode.equals(debug_delivery_location_code))
				double itemWeights = 0;
				for (Item ia : a_items)
					itemWeights += ia.getWeight();

				log(name()
						+ "::::processMergeOrderItemsFTLInternalVehicleFIRST, prepare LoadFTL for vehicle["
						+ k + "/" + vehicle_index.length + "] "
						+ getVehicle(vehicle_index[k]).getCode()
						+ ", capacity = "
						+ getVehicle(vehicle_index[k]).getWeight()
						+ ", itemWeights = " + itemWeights + ", a_items = "
						+ a_items.size());

				ArrayList<Item> collectItems = loadFTLToVehicle(
						vehicle_index[k], pi, di, a_items, fix_load_time,
						fix_unload_time);

				if (collectItems.size() == 0) {
					// System.out.println(name()
					// + "::processMergeOrderItemsFTL, vehicle " + k + "/" +
					// nbVehicles + " -> loadFTLToVehicle FAILED");
					k++;
					// System.out
					// .println("FAILED -> TRY LoadFTL for new vehicle, a_items = "
					// + a_items.size());
				} else {
					Trip t = trips[vehicle_index[k]]
							.get(trips[vehicle_index[k]].size() - 1);

					int remainItems = computeRemainItemOnRequest();

					log(name()
							+ "::loadFTLToVehicle, ADD-TRIP COLLECT_TO_FTL for vehicle "
							+ t.getVh().getCode() + ", nbTrips = "
							+ trips[vehicle_index[k]].size() + ", capacity "
							+ t.getVh().getWeight() + ", load = "
							+ t.computeTotalItemWeight() + ", nbItems = "
							+ t.getItems().size() + ", deliveryLocation "
							+ deliveryLocationCode + "-"
							+ mLocation2Type.get(deliveryLocationCode)
							+ ", remainItems at location = " + a_items.size()
							+ ", total remainItems = " + remainItems);

					hasChanged = true;
					
					if (deliveryLocationCode
							.equals(debug_delivery_location_code)
							|| debug_delivery_location_code == null) {
						System.out
								.print(name()
										+ "::processMergeOrderItemsFTLInternalVehicleFIRST at "
										+ deliveryLocationCode
										+ " -> LoadFTL success for vehicle "
										+ getVehicle(vehicle_index[k])
												.getCode()
										+ ", cap = "
										+ getVehicle(vehicle_index[k])
												.getWeight()
										+ ", collectItems = "
										+ collectItems.size() + ": ");
						double w = 0;
						for (int jj = 0; jj < collectItems.size(); jj++) {
							System.out.print(collectItems.get(jj).getWeight()
									+ ", ");
							w += collectItems.get(jj).getWeight();
						}
						// System.out.println(", total served weight = " + w);
					}

					for (Item ite : collectItems) {
						int idx = mItem2Index.get(ite);
						PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
								.get(idx)];
						// remove Item
						removeItemFromRequest(r, ite);
					}
					
					break;
				}
				
				
			}
			if(!hasChanged){
				log(name() 	+ "::processMergeOrderItemsFTLInternalVehicleFIRST at "
										+ deliveryLocationCode + ", FAILED, remainItems = " + a_items.size()
										+ " --> RETURN FALSE");
				return false;
			}
		}
		return true;
	}

	public boolean processMergeOrderItemsFTLNEW(
			String pickuplocationCode, String deliveryLocationCode,
			HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode

		// System.out.println(name() +
		// "::processMergeOrderItemsFTLInternalVehicleFIRST(" +
		// pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		// RI.size() + ")");

		// System.out.println(name() +
		// "::processMergeOrderItemsFTLInternalVehicleFIRST(" +
		// pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		// RI.size() + ") RBrenntagMultiPickupDelivery");

		String debug_delivery_location_code = "";

		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);
		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				Item I = requests[i].getItems()[j];
				a_items.add(I);
				if (deliveryLocationCode.equals(debug_delivery_location_code)) {
					log(name()
							+ "::processMergeOrderItemsNEW, item "
							+ I.getCode() + ", " + I.getWeight());
				}
			}
		}
		a_items = sortDecreasing(a_items);

		ClusterItems CI = new ClusterItems(a_items);
		clusterItems.add(CI);
		mCluster2Index.put(CI, clusterItems.size() - 1);

		int fix_load_time = -1;
		int fix_unload_time = -1;
		for (int i : RI) {
			fix_load_time = requests[i].getFixLoadTime();
			fix_unload_time = requests[i].getFixUnloadTime();
			break;
		}
		if (fix_load_time < 0)
			return true;
		
		int[] idx1 = sortDecreaseVehicleIndex(vehicles);
		int[] idx2 = sortDecreaseVehicleIndex(externalVehicles);
		int len1 = 0;
		if (idx1 != null)
			len1 = idx1.length;
		int len2 = 0;
		if (idx2 != null)
			len2 = idx2.length;
		int[] vehicle_index = new int[len1 + len2];
		for (int i = 0; i < len1; i++)
			vehicle_index[i] = idx1[i];

		for (int i = 0; i < len2; i++)
			vehicle_index[i + len1] = idx2[i] + len1;

		int nbVehicles = vehicle_index.length;

		log(name() + "::processMergeOrderItemsNEW("
				+ pickuplocationCode + "," + deliveryLocationCode
				+ ", RI.sz = " + RI.size() + ") location-type "
				+ mLocation2Type.get(deliveryLocationCode)
				+ ", nbInternalvehicle = " + len1 + ", nbExternalVehicle = "
				+ len2 + ", nbItems = " + a_items.size());

		int lastRemainItems = a_items.size();
		while (true) {
			if(a_items.size() == 0) break;
			
			// FIRST: try to find a vehicle (internal vehicles first) that can
			// served all items at the location
			ArrayList<Trip> T = new ArrayList<Trip>();
			
			
				for (int k1 = 0; k1 < len1 + len2; k1++) {
					Trip t = createTripLoadAllFTLToVehicle(vehicle_index[k1], pi, di,
							a_items, fix_load_time, fix_unload_time);
					//Vehicle vh = getVehicle(idx1[k1]);
					if (t != null) {
						T.add(t);

						// log(name() +
						// "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
						// + vh.getCode());
					}
				}
			if (T.size() > 0) {
				Trip sel_trip = null;
				double minW = Integer.MAX_VALUE;
				for (Trip t : T) {
					int vh_idx = t.start.vehicleIndex;
					matchTrips[vh_idx][clusterItems.size() - 1] = t;

					Vehicle vh = getVehicle(vh_idx);

					if (vh.getWeight() / 1000 < minW) {
						minW = vh.getWeight() / 1000;
						sel_trip = t;
					}
				}

				int remainItems = computeRemainItemOnRequest();

				Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
				trips[sel_trip.start.vehicleIndex].add(sel_trip);
				log(name()
						+ "::processMergeOrderItemsNEW, ADD-TRIP trips["
						+ vh.getCode() + "].sz = "
						+ trips[sel_trip.start.vehicleIndex].size()
						+ ", vehicle capacity " + vh.getWeight() + ", load = "
						+ sel_trip.computeTotalItemWeight() + ", nbItems = "
						+ sel_trip.getItems().size() + ", deliveryLocation "
						+ deliveryLocationCode + "-"
						+ mLocation2Type.get(deliveryLocationCode)
						+ ", remainItems = " + remainItems);

				ArrayList<ItemAmount> IA = sel_trip.start.items;
				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(sel_trip, items_of_trip);

				for (Item I : a_items) {
					removeScheduledItem(I);
				}
				return true;
			}

			// SECOND: try to load as full as possible items to vehicles
			// (internal vehicles first, descreasing order of capacity)
			int k = 0;

			boolean hasChanged = false;
			
			while (k < vehicle_index.length) {
				if (a_items.size() == 0)
					break;

				/*
				 * 
				 * ArrayList<Trip> T = new ArrayList<Trip>(); if(idx1 !=
				 * null)for (int k1 = 0; k1 < idx1.length; k1++) { Trip t =
				 * createTripLoadAllFTLToVehicle(idx1[k1], pi, di, a_items,
				 * fix_load_time, fix_unload_time); Vehicle vh =
				 * getVehicle(idx1[k1]); if (t != null){ T.add(t);
				 * 
				 * //log(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
				 * + vh.getCode()); } } if(T.size() == 0){ // consider external
				 * vehicles if(idx2 != null)for (int k1 = 0; k1 < idx2.length;
				 * k1++) { Trip t = createTripLoadAllFTLToVehicle(idx2[k1], pi,
				 * di, a_items, fix_load_time, fix_unload_time); Vehicle vh =
				 * getVehicle(idx2[k1]); if (t != null){ T.add(t);
				 * 
				 * //log(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, FOUND FTL trip for vehicle "
				 * + vh.getCode()); } } }
				 * 
				 * 
				 * 
				 * if (T.size() > 0) { // ClusterItems cl = new
				 * ClusterItems(a_items); // clusterItems.add(cl); Trip sel_trip
				 * = null; double minW = Integer.MAX_VALUE; for (Trip t : T) {
				 * int vh_idx = t.start.vehicleIndex;
				 * matchTrips[vh_idx][clusterItems.size() - 1] = t;
				 * 
				 * Vehicle vh = getVehicle(vh_idx); // System.out.println(name()
				 * // + "::processMergeOrderItemsFTL, T.sz = " + T.size() + //
				 * ", vehicle weight = " + vh.getWeight() // + ", MAX_INT = " +
				 * minW);
				 * 
				 * if (vh.getWeight() / 1000 < minW) { minW = vh.getWeight() /
				 * 1000; sel_trip = t; } } // System.out.println(name() // +
				 * "::processMergeOrderItemsFTL, T.sz = " + T.size());
				 * 
				 * // System.out.println(name() // +
				 * "::processMergeOrderItemsFTL, vehicle = " + //
				 * sel_trip.start.vehicleIndex);
				 * 
				 * int remainItems = computeRemainItemOnRequest();
				 * 
				 * Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
				 * trips[sel_trip.start.vehicleIndex].add(sel_trip); log(name()
				 * +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips["
				 * + vh.getCode() + "].sz = " +
				 * trips[sel_trip.start.vehicleIndex].size() +
				 * ", vehicle capacity " + vh.getWeight() + ", load = " +
				 * sel_trip.computeTotalItemWeight() + ", nbItems = " +
				 * sel_trip.getItems().size() + ", deliveryLocation " +
				 * deliveryLocationCode + "-" +
				 * mLocation2Type.get(deliveryLocationCode) + ", remainItems = "
				 * + remainItems); //System.out.println(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, ADD-TRIP trips["
				 * + vh.getCode() + "].sz = " +
				 * //trips[sel_trip.start.vehicleIndex].size());
				 * //System.out.println(name() +
				 * "::processMergeOrderItemsFTLInternalVehicleFIRST, VEHICLE " +
				 * sel_trip.getVh().getCode());
				 * 
				 * ArrayList<ItemAmount> IA = sel_trip.start.items; Item[]
				 * items_of_trip = new Item[IA.size()]; for (int ii = 0; ii <
				 * IA.size(); ii++) { items_of_trip[ii] =
				 * items.get(IA.get(ii).itemIndex); } mTrip2Items.put(sel_trip,
				 * items_of_trip);
				 * 
				 * for (Item I : a_items) { removeScheduledItem(I); } return; }
				 */

				// if
				// (deliveryLocationCode.equals(debug_delivery_location_code))
				double itemWeights = 0;
				for (Item ia : a_items)
					itemWeights += ia.getWeight();

				log(name()
						+ "::::processMergeOrderItemsNEW, prepare LoadFTL for vehicle["
						+ k + "/" + vehicle_index.length + "] "
						+ getVehicle(vehicle_index[k]).getCode()
						+ ", capacity = "
						+ getVehicle(vehicle_index[k]).getWeight()
						+ ", itemWeights = " + itemWeights + ", a_items = "
						+ a_items.size());

				ArrayList<Item> collectItems = loadFTLToVehicle(
						vehicle_index[k], pi, di, a_items, fix_load_time,
						fix_unload_time);

				if (collectItems.size() == 0) {
					// System.out.println(name()
					// + "::processMergeOrderItemsFTL, vehicle " + k + "/" +
					// nbVehicles + " -> loadFTLToVehicle FAILED");
					k++;
					// System.out
					// .println("FAILED -> TRY LoadFTL for new vehicle, a_items = "
					// + a_items.size());
				} else {
					Trip t = trips[vehicle_index[k]]
							.get(trips[vehicle_index[k]].size() - 1);

					int remainItems = computeRemainItemOnRequest();

					log(name()
							+ "::processMergeOrderItemsNEW, ADD-TRIP COLLECT_TO_FTL for vehicle "
							+ t.getVh().getCode() + ", nbTrips = "
							+ trips[vehicle_index[k]].size() + ", capacity "
							+ t.getVh().getWeight() + ", load = "
							+ t.computeTotalItemWeight() + ", nbItems = "
							+ t.getItems().size() + ", deliveryLocation "
							+ deliveryLocationCode + "-"
							+ mLocation2Type.get(deliveryLocationCode)
							+ ", remainItems at location = " + a_items.size()
							+ ", total remainItems = " + remainItems);

					hasChanged = true;
					
					if (deliveryLocationCode
							.equals(debug_delivery_location_code)
							|| debug_delivery_location_code == null) {
						System.out
								.print(name()
										+ "::processMergeOrderItemsNEW at "
										+ deliveryLocationCode
										+ " -> LoadFTL success for vehicle "
										+ getVehicle(vehicle_index[k])
												.getCode()
										+ ", cap = "
										+ getVehicle(vehicle_index[k])
												.getWeight()
										+ ", collectItems = "
										+ collectItems.size() + ": ");
						double w = 0;
						for (int jj = 0; jj < collectItems.size(); jj++) {
							System.out.print(collectItems.get(jj).getWeight()
									+ ", ");
							w += collectItems.get(jj).getWeight();
						}
						// System.out.println(", total served weight = " + w);
					}

					for (Item ite : collectItems) {
						int idx = mItem2Index.get(ite);
						PickupDeliveryRequest r = requests[mItemIndex2RequestIndex
								.get(idx)];
						// remove Item
						removeItemFromRequest(r, ite);
					}
					
					break;
				}
				
				
			}
			if(!hasChanged){
				log(name() 	+ "::processMergeOrderItemsNEW at "
										+ deliveryLocationCode + ", FAILED, remainItems = " + a_items.size()
										+ " --> RETURN FALSE");
				return false;
			}
		}
		return true;
	}

	public void processMergeOrderItemsFTL() {
		initializeLog();
		// processDistinctPickupDeliveryLocationCodes();

		int len = 0;
		if (input.getExternalVehicles() != null)
			len = input.getExternalVehicles().length;

		int nbVehicles = input.getVehicles().length
		// + input.getExternalVehicles().length;
				+ len;
		int nbClusters = distinct_deliveryLocationCodes.size();

		matchTrips = new Trip[nbVehicles][nbClusters];
		mCluster2Index = new HashMap<ClusterItems, Integer>();
		clusterItems = new ArrayList<ClusterItems>();
		// System.out.println(name()
		// + "::processMergeOrderItemsFTL, nbVehicles = " + nbVehicles
		// + ", distinct_locations.sz = "
		// + distinct_deliveryLocationCodes.size());

		ArrayList<Integer> intCityLocationIndex = new ArrayList<Integer>();
		ArrayList<Integer> extCityLocationIndex = new ArrayList<Integer>();

		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			String deliveryLocationCode = distinct_deliveryLocationCodes.get(i);
			if (mLocation2Type.get(deliveryLocationCode).equals(NOI_THANH)) {
				intCityLocationIndex.add(i);
			} else {
				extCityLocationIndex.add(i);
			}
		}

		
		// process intCityLocations FIRST
		for (int i : intCityLocationIndex) {
			if (input.getParams().getInternalVehicleFirst().equals("Y")) {
				//processMergeOrderItemsFTLInternalVehicleFIRST(
				boolean ok = processMergeOrderItemsFTLInternalVehicleFIRSTNEW(
						distinct_pickupLocationCodes.get(i),
						distinct_deliveryLocationCodes.get(i),
						distinct_request_indices.get(i));

				if(!ok) return;// cannot served all items at the location
			} else {
				//processMergeOrderItemsFTL(distinct_pickupLocationCodes.get(i),
				boolean ok = processMergeOrderItemsFTLNEW(distinct_pickupLocationCodes.get(i),
						distinct_deliveryLocationCodes.get(i),
						distinct_request_indices.get(i));
				if(!ok) return;
			}
		}
		for (int i : extCityLocationIndex) {
			if (input.getParams().getInternalVehicleFirst().equals("Y")) {
				//processMergeOrderItemsFTLInternalVehicleFIRST(
				boolean ok = processMergeOrderItemsFTLInternalVehicleFIRSTNEW(
						distinct_pickupLocationCodes.get(i),
						distinct_deliveryLocationCodes.get(i),
						distinct_request_indices.get(i));
				
				if(!ok) return;// cannot served all items at the location
				
			} else {
				//processMergeOrderItemsFTL(distinct_pickupLocationCodes.get(i),
				boolean ok = processMergeOrderItemsFTLNEW(distinct_pickupLocationCodes.get(i),
						distinct_deliveryLocationCodes.get(i),
						distinct_request_indices.get(i));
				if(!ok) return; 
			}
		}

		/*
		 * for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) { //
		 * System.out.println(name()+ //
		 * "::processMergeOrderItemsFTL, nbVehicles = " + nbVehicles + //
		 * ", i = " + i + "/" + "distinct_locations.sz = " + //
		 * distinct_deliveryLocationCodes.size());
		 * 
		 * if(input.getParams().getInternalVehicleFirst().equals("Y")){
		 * processMergeOrderItemsFTLInternalVehicleFIRST
		 * (distinct_pickupLocationCodes.get(i),
		 * distinct_deliveryLocationCodes.get(i),
		 * distinct_request_indices.get(i));
		 * 
		 * }else{ processMergeOrderItemsFTL(distinct_pickupLocationCodes.get(i),
		 * distinct_deliveryLocationCodes.get(i),
		 * distinct_request_indices.get(i)); } }
		 */

		// finalizeLog();
	}
	
	public void logTripsAfterMergingOrderAtLocations(){
		
		int nbVehicles = 0;
		int nbIntVehicles = 0;
		int nbExtVehicles = 0;
		if(vehicles != null){
			nbVehicles += vehicles.length;
			nbIntVehicles = vehicles.length;
		}
		if(externalVehicles != null){
			nbVehicles += externalVehicles.length;
			nbExtVehicles += externalVehicles.length;
		}
		HashMap<String, ArrayList<Trip>> mLocation2Trip = new HashMap<String, ArrayList<Trip>>();
		for(String lc: locationCodes)
			mLocation2Trip.put(lc,  new ArrayList<Trip>());
		
		for(int i= 0; i < nbVehicles; i++){
			for(int j = 0; j < trips[i].size(); j++){
				Trip t = trips[i].get(j);
				String lc = locationCodes.get(t.end.locationIndex);
				mLocation2Trip.get(lc).add(t);
			}
		}
		
		HashMap<String, Double> mLocation2ItemWeight = new HashMap<String, Double>();
		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			String deliveryLocationCode = distinct_deliveryLocationCodes.get(i);
			double w = 0;
			for(Item I: mLocation2Items.get(deliveryLocationCode)){
				w += I.getWeight();
			}
			mLocation2ItemWeight.put(deliveryLocationCode, w);
		}
		
		for(String lc: locationCodes){
			log(name() + "::logTripsAfterMergingOrderAtLocations, location " + lc + "-" + mLocation2Type.get(lc) + 
					", weight " + mLocation2ItemWeight.get(lc));
			for(int j = 0; j < mLocation2Trip.get(lc).size(); j++){
				Trip t = mLocation2Trip.get(lc).get(j);
				log(t.getVh().getCode() + ", cap = " + t.getVh().getWeight() + ", load " + t.computeTotalItemWeight());
			}
		}
	}
	
	public void processMergeOrderItemsFTLNoConstraints() {
		initializeLog();
		System.out.println(name()
				+ "::processMergeOrderItemsFTLNoConstraints START");
		processDistinctPickupDeliveryLocationCodes();

		int nbVehicles = input.getVehicles().length
				+ input.getExternalVehicles().length;
		int nbClusters = distinct_deliveryLocationCodes.size();

		matchTrips = new Trip[nbVehicles][nbClusters];
		mCluster2Index = new HashMap<ClusterItems, Integer>();
		clusterItems = new ArrayList<ClusterItems>();

		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			/*
			 * System.out.println(name() +
			 * "::processMergeOrderItemsFTL(), from " +
			 * distinct_pickupLocationCodes.get(i) + " --> " +
			 * distinct_deliveryLocationCodes.get(i) + ", RI = " +
			 * distinct_request_indices.get(i).size()); for (int j :
			 * distinct_request_indices.get(i)) { System.out.print(j + ","); }
			 * System.out.println(); for (int j :
			 * distinct_request_indices.get(i)) { System.out.print("req[" + j +
			 * "]: "); for (int jj = 0; jj < requests[j].getItems().length;
			 * jj++) System.out.print("[Order" + requests[j].getOrderID() +
			 * ", Item" + requests[j].getItems()[jj].getCode() + "," +
			 * requests[j].getItems()[jj].getWeight() + "] ");
			 * System.out.println(); }
			 */
			processMergeOrderItemsFTLNoConstraints(
					distinct_pickupLocationCodes.get(i),
					distinct_deliveryLocationCodes.get(i),
					distinct_request_indices.get(i));

		}

		// finalizeLog();
	}

	public void processSplitOrders() {
		if (log != null)
			log.println(name()
					+ "::processSplitOrders, externalVehicles = "
					+ (externalVehicles != null ? externalVehicles.length
							: "NULL"));

		// System.out
		// .println(name()
		// + "::processSplitOrders, externalVehicles = "
		// + (externalVehicles != null ? externalVehicles.length
		// : "NULL"));
		int sz = 0;
		int len = 0;
		if (externalVehicles != null)
			len = externalVehicles.length;
		sz = len;
		if (vehicles != null)
			sz += vehicles.length;

		trips = new ArrayList[sz];
		for (int i = 0; i < sz; i++)
			trips[i] = new ArrayList<Trip>();

		// sorting request in an decreasing order of weight
		Item[] sorted_items = new Item[items.size()];
		for (int i = 0; i < items.size(); i++)
			sorted_items[i] = items.get(i);
		for (int i = 0; i < sorted_items.length; i++) {
			for (int j = i + 1; j < sorted_items.length; j++) {
				if (sorted_items[i].getWeight() < sorted_items[j].getWeight()) {
					Item ti = sorted_items[i];
					sorted_items[i] = sorted_items[j];
					sorted_items[j] = ti;
				}
			}
		}
		ArrayList<Integer> intCityItemIndex = new ArrayList<Integer>();
		ArrayList<Integer> extCityItemIndex = new ArrayList<Integer>();
		for (int i = 0; i < sorted_items.length; i++) {
			Item I = sorted_items[i];
			int itemIndex = mItem2Index.get(I);
			int reqIndex = mItemIndex2RequestIndex.get(itemIndex);
			PickupDeliveryRequest r = requests[reqIndex];
			if (mLocation2Type.get(r.getDeliveryLocationCode()).equals(
					NOI_THANH)) {
				intCityItemIndex.add(i);
			} else {
				extCityItemIndex.add(i);
			}
		}
		for (int i : intCityItemIndex) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithInternalVehicle(I);
			}
		}

		for (int i : intCityItemIndex) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithExternalVehicle(I);
			}
		}

		for (int i : extCityItemIndex) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithInternalVehicle(I);
			}
		}

		for (int i : extCityItemIndex) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithExternalVehicle(I);
			}
		}

		/*
		 * for (int i = 0; i < sorted_items.length; i++) { Item I =
		 * sorted_items[i]; int item_index = mItem2Index.get(I); int req_index =
		 * mItemIndex2RequestIndex.get(item_index); PickupDeliveryRequest r =
		 * requests[req_index]; if (r.getSplitDelivery().equals("Y")) {
		 * processSplitOrderItemWithInternalVehicle(I); } }
		 * 
		 * for (int i = 0; i < sorted_items.length; i++) { Item I =
		 * sorted_items[i]; int item_index = mItem2Index.get(I); int req_index =
		 * mItemIndex2RequestIndex.get(item_index); PickupDeliveryRequest r =
		 * requests[req_index]; if (r.getSplitDelivery().equals("Y")) {
		 * processSplitOrderItemWithExternalVehicle(I); } }
		 */

		/*
		 * for (int i = 0; i < requests.length; i++) { PickupDeliveryRequest r =
		 * requests[i]; if (r.getSplitDelivery().equals("Y"))
		 * processSplitAnOrderWithInternalVehicle(r); }
		 * 
		 * for (int i = 0; i < requests.length; i++) { PickupDeliveryRequest r =
		 * requests[i]; if (r.getSplitDelivery().equals("Y"))
		 * processSplitAnOrderWithExternalVehicle(r); }
		 */
	}

	public void initMapData() {
		mLocationCode2Index = new HashMap<String, Integer>();
		HashSet<String> s_locationCodes = new HashSet<String>();
		mItemCode2OrderID = new HashMap<String, String>();
		mTrip2Items = new HashMap<Trip, Item[]>();
		mVehicle2Distance = new HashMap<Integer, Double>();
		mVehicle2OriginStartWoringTime = new HashMap<Integer, String>();
		mItemIndex2RequestIndex = new HashMap<Integer, Integer>();
		mItemCode2Index = new HashMap<String, Integer>();
		mLocation2Items = new HashMap<String, ArrayList<Item>>();
		mLocationCode2Config = new HashMap<String, LocationConfig>();
		
		items = new ArrayList<Item>();
		mItem2Index = new HashMap<Item, Integer>();
		if (requests != null)
			for (int i = 0; i < requests.length; i++) {
				for (int j = 0; j < requests[i].getItems().length; j++) {
					Item I = requests[i].getItems()[j];
					I.setOrderId(requests[i].getOrderID());
					int idx = items.size();
					mItem2Index.put(I, idx);
					items.add(I);
					mItemIndex2RequestIndex.put(idx, i);
					mItemCode2Index.put(I.getCode(), idx);
				}
			}
		if (requests != null)
			for (int i = 0; i < requests.length; i++) {
				s_locationCodes.add(requests[i].getPickupLocationCode());
				s_locationCodes.add(requests[i].getDeliveryLocationCode());
		
				if(mLocation2Items.get(requests[i].getDeliveryLocationCode())==null)
					mLocation2Items.put(requests[i].getDeliveryLocationCode(), new ArrayList<Item>());
				for(Item I: requests[i].getItems()){
					mLocation2Items.get(requests[i].getDeliveryLocationCode()).add(I);
				}
			}
		
		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		if (vehicles != null)
			for (int i = 0; i < nbIntVehicles; i++) {
				s_locationCodes.add(vehicles[i].getStartLocationCode());
				s_locationCodes.add(vehicles[i].getEndLocationCode());
				mVehicle2OriginStartWoringTime.put(i,
						vehicles[i].getStartWorkingTime());
			}
		if (externalVehicles != null)
			for (int i = 0; i < nbExtVehicles; i++) {
				s_locationCodes.add(externalVehicles[i].getStartLocationCode());
				s_locationCodes.add(externalVehicles[i].getEndLocationCode());
				mVehicle2OriginStartWoringTime.put(i + nbIntVehicles,
						externalVehicles[i].getStartWorkingTime());
			}

		if (distances != null)
			for (int i = 0; i < distances.length; i++) {
				s_locationCodes.add(distances[i].getSrcCode());
				s_locationCodes.add(distances[i].getDestCode());
			}
		int idx = -1;
		locationCodes = new ArrayList<String>();
		for (String lc : s_locationCodes) {
			idx++;
			mLocationCode2Index.put(lc, idx);
			locationCodes.add(lc);
		}
		a_travelTime = new double[idx + 1][idx + 1];
		a_distance = new double[idx + 1][idx + 1];
		for (int i = 0; i < distances.length; i++) {
			int from = mLocationCode2Index.get(distances[i].getSrcCode());
			int to = mLocationCode2Index.get(distances[i].getDestCode());
			a_distance[from][to] = distances[i].getDistance();
		}
		if (travelTimes != null)
			for (int i = 0; i < travelTimes.length; i++) {
				int from = mLocationCode2Index.get(travelTimes[i].getSrcCode());
				int to = mLocationCode2Index.get(travelTimes[i].getDestCode());
				a_travelTime[from][to] = travelTimes[i].getDistance();
			}

		if (requests != null)
			for (int i = 0; i < requests.length; i++) {
				for (int j = 0; j < requests[i].getItems().length; j++) {
					mItemCode2OrderID.put(requests[i].getItems()[j].getCode(),
							requests[i].getOrderID());
				}
			}

		mVehicle2Index = new HashMap<Vehicle, Integer>();
		int nbVehicles = 0;
		if (vehicles != null) {
			nbVehicles += vehicles.length;
			for (int i = 0; i < vehicles.length; i++)
				mVehicle2Index.put(vehicles[i], i);
		}
		if (externalVehicles != null) {
			for (int i = 0; i < externalVehicles.length; i++)
				mVehicle2Index.put(externalVehicles[i], i + nbVehicles);
			nbVehicles += externalVehicles.length;
		}

		if(input.getLocationConfigs() != null && input.getLocationConfigs().length > 0){
			for(int i = 0; i < input.getLocationConfigs().length; i++){
				LocationConfig lc = input.getLocationConfigs()[i];
				mLocationCode2Config.put(lc.getLocationCode(), lc);
			}
		}
	}

	public void initPoints() {
		System.out.println(name() + "::initPoints");

		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		pickupPoints = new ArrayList<Point>();
		deliveryPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();

		mPickupIndex2ScheduledVehicleIndex = new HashMap<Integer, Integer>();
		mPickupPoint2RequestIndex = new HashMap<Point, Integer>();
		mPoint2Index = new HashMap<Point, Integer>();
		mPoint2LocationCode = new HashMap<Point, String>();
		mPoint2Demand = new HashMap<Point, Double>();
		// mPoint2Request = new HashMap<Point, PickupDeliveryRequest>();
		mPoint2Request = new HashMap<Point, ArrayList<PickupDeliveryRequest>>();
		mPoint2Type = new HashMap<Point, String>();
		mPoint2PossibleVehicles = new HashMap<Point, HashSet<Integer>>();
		mPoint2IndexItems = new HashMap<Point, Integer[]>();
		pickup2DeliveryOfGood = new HashMap<Point, Point>();
		earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		lastestAllowedArrivalTime = new HashMap<Point, Integer>();
		serviceDuration = new HashMap<Point, Integer>();

		mPoint2Vehicle = new HashMap<Point, Vehicle>();
		// mVehicle2NotReachedLocations = new HashMap<String,
		// HashSet<String>>();
		mTrip2PickupPointIndices = new HashMap<Trip, ArrayList<Integer>>();

		mPickupPoint2PickupIndex = new HashMap<Point, Integer>();
		mDeliveryPoint2DeliveryIndex = new HashMap<Point, Integer>();

		int len = 0;
		if (externalVehicles != null)
			len = externalVehicles.length;

		if (vehicles != null)
			M = vehicles.length + len;// externalVehicles.length;
		else
			M = len;

		int idxPoint = -1;
		HashSet<String> IC = new HashSet<String>();

		ArrayList<PairInt> PI = new ArrayList<PairInt>();

		// create points from init FTL trips
		for (int k = 0; k < M; k++) {
			if (trips[k].size() > 0) {
				for (int j = 0; j < trips[k].size(); j++) {
					Trip t = trips[k].get(j);
					int idxVH = mVehicle2Index.get(t.getVh());

					if (t.type.equals("FTL")) {// create new points
						ArrayList<Integer> L = new ArrayList<Integer>();
						for (int i = 0; i < t.start.items.size(); i++) {
							// pickup
							idxPoint++;
							ItemAmount ta = t.start.items.get(i);
							int itemIndex = items.size();// ta.itemIndex;

							Item I = mTrip2Items.get(t)[0];// create new item
															// and
															// add it to items
							items.add(I);
							mItem2Index.put(I, itemIndex);
							IC.add(I.getCode());

							int reqIdx = mItemIndex2RequestIndex
									.get(ta.itemIndex);
							mItemIndex2RequestIndex.put(itemIndex, reqIdx);// map
																			// new
																			// created
																			// (splitted)
																			// item
																			// to
																			// request

							PickupDeliveryRequest r = requests[reqIdx];
							Point pickup = new Point(idxPoint);
							pickupPoints.add(pickup);
							L.add(pickupPoints.size() - 1);
							mPickupPoint2PickupIndex.put(pickup,
									pickupPoints.size() - 1);

							mPickupIndex2ScheduledVehicleIndex.put(idxPoint,
									t.start.vehicleIndex);

							// mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
							mPickupPoint2RequestIndex.put(pickup, reqIdx);

							mPoint2Index.put(pickup, idxPoint);
							mPoint2LocationCode.put(pickup,
									r.getPickupLocationCode());
							mPoint2Demand.put(pickup, (double) ta.amount);

							// mPoint2Request.put(pickup, r);
							if (mPoint2Request.get(pickup) == null)
								mPoint2Request.put(pickup,
										new ArrayList<PickupDeliveryRequest>());
							mPoint2Request.get(pickup).add(r);

							mPoint2Type.put(pickup, "P");
							mPoint2PossibleVehicles.put(pickup,
									new HashSet<Integer>());
							Integer[] ite = new Integer[1];
							ite[0] = itemIndex;
							mPoint2IndexItems.put(pickup, ite);

							// delivery
							String deliveryLocationCode = locationCodes
									.get(t.end.locationIndex);
							idxPoint++;
							Point delivery = new Point(idxPoint);
							deliveryPoints.add(delivery);
							mDeliveryPoint2DeliveryIndex.put(delivery,
									deliveryPoints.size() - 1);

							PI.add(new PairInt(idxVH,
									mDeliveryPoint2DeliveryIndex.get(delivery)));

							mPoint2Index.put(delivery, idxPoint);
							mPoint2LocationCode.put(delivery,
									deliveryLocationCode);
							mPoint2Demand.put(delivery, -(double) ta.amount);
							// mPoint2Request.put(delivery, r);
							if (mPoint2Request.get(delivery) == null)
								mPoint2Request.put(delivery,
										new ArrayList<PickupDeliveryRequest>());
							mPoint2Request.get(delivery).add(r);

							mPoint2Type.put(delivery, "D");
							mPoint2PossibleVehicles.put(delivery,
									new HashSet<Integer>());

							pickup2DeliveryOfGood.put(pickup, delivery);
							allPoints.add(pickup);
							allPoints.add(delivery);

							earliestAllowedArrivalTime.put(pickup,
									(int) DateTimeUtils.dateTime2Int(r
											.getEarlyPickupTime()));
							int pickupDuration = I.getPickupDuration();

							serviceDuration.put(pickup, pickupDuration);

							lastestAllowedArrivalTime.put(pickup,
									(int) DateTimeUtils.dateTime2Int(r
											.getLatePickupTime()));
							earliestAllowedArrivalTime.put(delivery,
									(int) DateTimeUtils.dateTime2Int(r
											.getEarlyDeliveryTime()));
							int deliveryDuration = I.getDeliveryDuration();

							serviceDuration.put(delivery, deliveryDuration);

							lastestAllowedArrivalTime.put(delivery,
									(int) DateTimeUtils.dateTime2Int(r
											.getLateDeliveryTime()));
						}
						mTrip2PickupPointIndices.put(t, L);
						// } else if (t.type.equals("COLLECT_TO_FTL")) {
					} else {
						ArrayList<Integer> L = new ArrayList<Integer>();
						for (int i = 0; i < t.start.items.size(); i++) {
							// pickup
							idxPoint++;
							ItemAmount ta = t.start.items.get(i);
							int itemIndex = ta.itemIndex;
							Item I = items.get(itemIndex);
							IC.add(I.getCode());

							int reqIdx = mItemIndex2RequestIndex
									.get(ta.itemIndex);

							PickupDeliveryRequest r = requests[reqIdx];
							Point pickup = new Point(idxPoint);
							pickupPoints.add(pickup);
							L.add(pickupPoints.size() - 1);// add pickup point
															// index to L
							mPickupPoint2PickupIndex.put(pickup,
									pickupPoints.size() - 1);

							mPickupIndex2ScheduledVehicleIndex.put(idxPoint,
									t.start.vehicleIndex);

							// mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
							mPickupPoint2RequestIndex.put(pickup, reqIdx);

							mPoint2Index.put(pickup, idxPoint);
							mPoint2LocationCode.put(pickup,
									r.getPickupLocationCode());
							mPoint2Demand.put(pickup, (double) ta.amount);
							// mPoint2Request.put(pickup, r);
							if (mPoint2Request.get(pickup) == null)
								mPoint2Request.put(pickup,
										new ArrayList<PickupDeliveryRequest>());
							mPoint2Request.get(pickup).add(r);

							mPoint2Type.put(pickup, "P");
							mPoint2PossibleVehicles.put(pickup,
									new HashSet<Integer>());
							Integer[] ite = new Integer[1];
							ite[0] = itemIndex;
							mPoint2IndexItems.put(pickup, ite);

							// delivery
							String deliveryLocationCode = locationCodes
									.get(t.end.locationIndex);
							idxPoint++;
							Point delivery = new Point(idxPoint);
							deliveryPoints.add(delivery);
							mDeliveryPoint2DeliveryIndex.put(delivery,
									deliveryPoints.size() - 1);
							mPoint2Index.put(delivery, idxPoint);
							mPoint2LocationCode.put(delivery,
									deliveryLocationCode);
							mPoint2Demand.put(delivery, -(double) ta.amount);
							// mPoint2Request.put(delivery, r);
							if (mPoint2Request.get(delivery) == null)
								mPoint2Request.put(delivery,
										new ArrayList<PickupDeliveryRequest>());
							mPoint2Request.get(delivery).add(r);

							mPoint2Type.put(delivery, "D");
							mPoint2PossibleVehicles.put(delivery,
									new HashSet<Integer>());

							pickup2DeliveryOfGood.put(pickup, delivery);
							allPoints.add(pickup);
							allPoints.add(delivery);

							earliestAllowedArrivalTime.put(pickup,
									(int) DateTimeUtils.dateTime2Int(r
											.getEarlyPickupTime()));
							int pickupDuration = I.getPickupDuration();

							serviceDuration.put(pickup, pickupDuration);

							lastestAllowedArrivalTime.put(pickup,
									(int) DateTimeUtils.dateTime2Int(r
											.getLatePickupTime()));
							earliestAllowedArrivalTime.put(delivery,
									(int) DateTimeUtils.dateTime2Int(r
											.getEarlyDeliveryTime()));
							int deliveryDuration = I.getDeliveryDuration();

							serviceDuration.put(delivery, deliveryDuration);

							lastestAllowedArrivalTime.put(delivery,
									(int) DateTimeUtils.dateTime2Int(r
											.getLateDeliveryTime()));
						}
						mTrip2PickupPointIndices.put(t, L);
					}
				}
			}
		}

		if (CHECK_AND_LOG) {
			log(name() + "::initPoints, nbVehicles = " + M + ", nbDelivery = "
					+ deliveryPoints.size() + ", PI.sz = " + PI.size());
		}
		fixVehiclePoint = new boolean[M][deliveryPoints.size()];
		for (int i = 0; i < M; i++)
			for (int j = 0; j < deliveryPoints.size(); j++)
				fixVehiclePoint[i][j] = false;
		for (PairInt pi : PI) {
			fixVehiclePoint[pi.i][pi.j] = true;
		}

		processDistinctPickupDeliveryLocationCodes();
		for (int I = 0; I < distinct_deliveryLocationCodes.size(); I++) {
			String pickupLocationCode = distinct_pickupLocationCodes.get(I);
			String deliveryLocationCode = distinct_deliveryLocationCodes.get(I);

			System.out.println("processMergeOrderItemsFTL("
					+ distinct_pickupLocationCodes.get(I) + " --> "
					+ distinct_deliveryLocationCodes.get(I) + ", RI = "
					+ distinct_request_indices.get(I).size());
			for (int i : distinct_request_indices.get(I)) {
				System.out.print(i + ",");
			}
			System.out.println();
			for (int i : distinct_request_indices.get(I)) {
				System.out.print(i + ": ");
				for (int jj = 0; jj < requests[i].getItems().length; jj++)
					System.out.print("[Order" + requests[i].getOrderID()
							+ ", Item" + requests[i].getItems()[jj].getCode()
							+ "," + requests[i].getItems()[jj].getWeight()
							+ "] ");
				System.out.println();
			}

			ConflictBasedExtractor extractor = new ConflictBasedExtractor(this,
					log);
			ArrayList<HashSet<Integer>> lstNonConflictRequests = extractor
					.clusterRequestBasedItemConflict(distinct_request_indices
							.get(I));

			for (HashSet<Integer> C : lstNonConflictRequests) {

				idxPoint++;
				Point pickup = new Point(idxPoint);
				pickupPoints.add(pickup);
				mPickupPoint2PickupIndex.put(pickup, pickupPoints.size() - 1);

				// mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
				// mPickupPoint2RequestIndex.put(pickup, i);

				mPoint2Index.put(pickup, idxPoint);
				// mPoint2LocationCode.put(pickup,
				// requests[i].getPickupLocationCode());
				mPoint2LocationCode.put(pickup, pickupLocationCode);

				double demand = 0;
				int pickupDuration = 0;
				int deliveryDuration = 0;
				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					for (int j = 0; j < requests[i].getItems().length; j++) {
						demand = demand + requests[i].getItems()[j].getWeight();
						deliveryDuration = deliveryDuration
								+ requests[i].getItems()[j]
										.getDeliveryDuration();
						pickupDuration = pickupDuration
								+ requests[i].getItems()[j].getPickupDuration();
					}
				}

				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					if (mPoint2Request.get(pickup) == null)
						mPoint2Request.put(pickup,
								new ArrayList<PickupDeliveryRequest>());
					mPoint2Request.get(pickup).add(requests[i]);
				}

				mPoint2Demand.put(pickup, demand);
				// mPoint2Request.put(pickup, requests[i]);

				mPoint2Type.put(pickup, "P");
				mPoint2PossibleVehicles.put(pickup, new HashSet<Integer>());

				int sz = 0;
				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					sz += requests[i].getItems().length;
				}
				Integer[] L = new Integer[sz];

				int idx = -1;
				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					for (int ii = 0; ii < requests[i].getItems().length; ii++) {
						Item item = requests[i].getItems()[ii];
						IC.add(item.getCode());
						idx++;
						L[idx] = mItem2Index.get(item);
					}
				}
				mPoint2IndexItems.put(pickup, L);

				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					for (int j = 0; j < requests[i].getItems().length; j++)
						mItem2ExclusiveItems.put(
								requests[i].getItems()[j].getCode(),
								new HashSet<String>());
				}

				// delivery
				idxPoint++;
				Point delivery = new Point(idxPoint);
				deliveryPoints.add(delivery);
				mDeliveryPoint2DeliveryIndex.put(delivery,
						deliveryPoints.size() - 1);

				mPoint2Index.put(delivery, idxPoint);
				// mPoint2LocationCode.put(delivery,requests[i].getDeliveryLocationCode());
				mPoint2LocationCode.put(delivery, deliveryLocationCode);
				mPoint2Demand.put(delivery, -demand);
				// mPoint2Request.put(delivery, requests[i]);

				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					if (mPoint2Request.get(delivery) == null)
						mPoint2Request.put(delivery,
								new ArrayList<PickupDeliveryRequest>());
					mPoint2Request.get(delivery).add(requests[i]);
				}

				mPoint2Type.put(delivery, "D");
				mPoint2PossibleVehicles.put(delivery, new HashSet<Integer>());
				// mPoint2LoadedItems.put(delivery, new HashSet<String>());

				pickup2DeliveryOfGood.put(pickup, delivery);
				allPoints.add(pickup);
				allPoints.add(delivery);

				int earliestAllowedArrivalTimePickup = -1;
				int latestAllowedArrivalTimePickup = Integer.MAX_VALUE;
				int earliestAllowedArrivalTimeDelivery = -1;
				int latestAllowedArrivalTimeDelivery = Integer.MAX_VALUE;

				// for (int i : distinct_request_indices.get(I)) {
				for (int i : C) {
					if (earliestAllowedArrivalTimePickup < (int) DateTimeUtils
							.dateTime2Int(requests[i].getEarlyPickupTime()))
						earliestAllowedArrivalTimePickup = (int) DateTimeUtils
								.dateTime2Int(requests[i].getEarlyPickupTime());

					if (earliestAllowedArrivalTimeDelivery < (int) DateTimeUtils
							.dateTime2Int(requests[i].getEarlyDeliveryTime()))
						earliestAllowedArrivalTimeDelivery = (int) DateTimeUtils
								.dateTime2Int(requests[i]
										.getEarlyDeliveryTime());

					if (latestAllowedArrivalTimePickup > (int) DateTimeUtils
							.dateTime2Int(requests[i].getLatePickupTime()))
						latestAllowedArrivalTimePickup = (int) DateTimeUtils
								.dateTime2Int(requests[i].getLatePickupTime());

					if (latestAllowedArrivalTimeDelivery > (int) DateTimeUtils
							.dateTime2Int(requests[i].getLateDeliveryTime()))
						latestAllowedArrivalTimeDelivery = (int) DateTimeUtils
								.dateTime2Int(requests[i].getLateDeliveryTime());

				}
				earliestAllowedArrivalTime.put(pickup,
						earliestAllowedArrivalTimePickup);
				serviceDuration.put(pickup, pickupDuration);
				lastestAllowedArrivalTime.put(pickup,
						latestAllowedArrivalTimePickup);

				earliestAllowedArrivalTime.put(delivery,
						earliestAllowedArrivalTimeDelivery);
				serviceDuration.put(delivery, deliveryDuration);
				lastestAllowedArrivalTime.put(delivery,
						latestAllowedArrivalTimeDelivery);
			}
		}
		log(name() + "::initPoint, set of item codes = IC.sz = " + IC.size());

		// init start-end points for vehicles
		cap = new double[M];
		for (int k = 0; k < M; k++) {
			Vehicle vh = getVehicle(k);//null;
			//if (k < vehicles.length)
			//	vh = vehicles[k];
			//else
			//	vh = externalVehicles[k - vehicles.length];

			cap[k] = vh.getWeight();// vehicles[k].getWeight();

			idxPoint++;
			Point s = new Point(idxPoint);
			idxPoint++;
			Point t = new Point(idxPoint);
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
			// mVehicle2NotReachedLocations.put(vh.getCode(),new
			// HashSet<String>());

			allPoints.add(s);
			allPoints.add(t);

			// earliestAllowedArrivalTime.put(s,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			earliestAllowedArrivalTime.put(s,
					(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
			serviceDuration.put(s, 0);// load-unload is 30 minutes
			// lastestAllowedArrivalTime.put(s,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			if (vh.getEndWorkingTime() != null)
				lastestAllowedArrivalTime.put(s, (int) DateTimeUtils
						.dateTime2Int(vh.getEndWorkingTime()));
			// earliestAllowedArrivalTime.put(t,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			earliestAllowedArrivalTime.put(t,
					(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
			serviceDuration.put(t, 0);// load-unload is 30 minutes
			// lastestAllowedArrivalTime.put(t,
			// (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));

			if (vh.getEndWorkingTime() != null)
				lastestAllowedArrivalTime.put(t, (int) DateTimeUtils
						.dateTime2Int(vh.getEndWorkingTime()));

			// System.out.println("mapData, startWorkingTime = " +
			// vh.getStartWorkingTime() + ", end working time = " +
			// vh.getEndWorkingTime());
		}

	}

	public void initItemVehicleConflicts() {
		itemConflict = new boolean[items.size()][items.size()];
		for (int ii = 0; ii < items.size(); ii++)
			for (int jj = 0; jj < items.size(); jj++)
				itemConflict[ii][jj] = false;

		mItem2ExclusiveItems = new HashMap<String, HashSet<String>>();
		ExclusiveItem[] exclusiveItems = input.getExclusiveItemPairs();
		if (exclusiveItems != null)
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
					itemConflict[mItemCode2Index.get(I1)][mItemCode2Index
							.get(I2)] = true;
					itemConflict[mItemCode2Index.get(I2)][mItemCode2Index
							.get(I1)] = true;
				}
			}

		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		
		mVehicle2NotReachedLocations = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < nbIntVehicles; i++) {
			Vehicle vh = vehicles[i];
			mVehicle2NotReachedLocations.put(vh.getCode(),
					new HashSet<String>());
		}
		if (externalVehicles != null)
			for (int i = 0; i < nbExtVehicles; i++) {
				Vehicle vh = externalVehicles[i];
				mVehicle2NotReachedLocations.put(vh.getCode(),
						new HashSet<String>());
			}

		ExclusiveVehicleLocation[] exclusiveVehicleLocations = input
				.getExclusiveVehicleLocations();

		if (exclusiveVehicleLocations != null)
			for (int i = 0; i < exclusiveVehicleLocations.length; i++) {
				String vehicleCode = exclusiveVehicleLocations[i]
						.getVehicleCode();
				String locationCode = exclusiveVehicleLocations[i]
						.getLocationCode();

				if (mVehicle2NotReachedLocations.get(vehicleCode) == null)
					mVehicle2NotReachedLocations.put(vehicleCode,
							new HashSet<String>());

				mVehicle2NotReachedLocations.get(vehicleCode).add(locationCode);
			}

		mVehicleCategory2NotReachedLocations = new HashMap<String, HashSet<String>>();
		if (input.getVehicleCategories() != null)
			for (int i = 0; i < input.getVehicleCategories().length; i++) {
				Vehicle vh = input.getVehicleCategories()[i];
				mVehicleCategory2NotReachedLocations.put(
						vh.getVehicleCategory(), new HashSet<String>());
			}
		ExclusiveVehicleLocation[] exl = input
				.getExclusiveVehicleCategoryLocations();
		if (exl != null)
			for (int i = 0; i < exl.length; i++) {
				String vehicleCategory = exl[i].getVehicleCode();
				String locationCode = exl[i].getLocationCode();
				if (mVehicleCategory2NotReachedLocations.get(vehicleCategory) == null) {
					mVehicleCategory2NotReachedLocations.put(vehicleCategory,
							new HashSet<String>());
				}

				mVehicleCategory2NotReachedLocations.get(vehicleCategory).add(
						locationCode);

			}

		mapLocation2Type();
		if (CHECK_AND_LOG) {
			for (String lc : locationCodes) {
				log(name() + "::initItemVehicleConflicts, location " + lc
						+ ": " + mLocation2Type.get(lc));
			}
		}
	}

	public void initDistanceTravelTime() {
		awm = new ArcWeightsManager(allPoints);
		nwm = new NodeWeightsManager(allPoints);
		travelTime = new ArcWeightsManager(allPoints);

		for (Point p : allPoints) {
			String lp = mPoint2LocationCode.get(p);
			int ip = mLocationCode2Index.get(lp);
			for (Point q : allPoints) {
				String lq = mPoint2LocationCode.get(q);
				int iq = mLocationCode2Index.get(lq);

				double d = a_distance[ip][iq];// mDistance.get(code(lp, lq));
				awm.setWeight(p, q, d);
				// travelTime.setWeight(p, q,
				// (d*1000)/input.getParams().getAverageSpeed());// meter per
				// second
				double t = a_travelTime[ip][iq];// mTravelTime.get(code(lp,
												// lq));
				travelTime.setWeight(p, q, t);
			}
		}
		for (Point p : allPoints) {
			nwm.setWeight(p, mPoint2Demand.get(p));
			// System.out.println(module + "::compute, nwm.setWeight(" + p.ID
			// + "," + mPoint2Demand.get(p));
		}

	}

	public void initModel() {
		mPoint2ArrivalTime = new HashMap<Point, Integer>();
		mPoint2DepartureTime = new HashMap<Point, Integer>();
		mPoint2IndexLoadedItems = new HashMap<Point, HashSet<Integer>>();
		for (Point p : allPoints)
			mPoint2IndexLoadedItems.put(p, new HashSet<Integer>());

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

		cost = new TotalCostVR(XR, awm);

		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(cost);

		mgr.close();

	}

	public void mapData() {
		initPoints();
		initItemVehicleConflicts();
		initDistanceTravelTime();

		initModel();

	}

	public HashSet<Integer> greedyConstructMaintainConstraintFTL() {
		// initializeLog();
		HashSet<Integer> cand = new HashSet<Integer>();
		/*
		 * for (int i = 0; i < pickupPoints.size(); i++) { if
		 * (mPickupIndex2ScheduledVehicleIndex.get(i) != null) { int vi =
		 * mPickupIndex2ScheduledVehicleIndex.get(i); Point s = XR.startPoint(vi
		 * + 1); Point sel_pickup = pickupPoints.get(i); Point sel_delivery =
		 * deliveryPoints.get(i); mgr.performAddOnePoint(sel_delivery, s);
		 * mgr.performAddOnePoint(sel_pickup, s);
		 * propagateArrivalDepartureTime(vi + 1, true); } else { cand.add(i); }
		 * }
		 */
		HashSet<Integer> scheduled_pickup_point_index = new HashSet<Integer>();

		log(name()
				+ "::greedyConstructMaintainConstraintFTL, INIT CONVERT TRIP to XR, pickupPoint.sz = "
				+ pickupPoints.size());
		double totalItemW = 0;
		HashSet<String> tripItemCode = new HashSet<String>();
		int nbVehicles = computeNbVehicles();
		//for (int k = 0; k < vehicles.length + externalVehicles.length; k++) {
		for (int k = 0; k < nbVehicles; k++) {
			if (trips[k].size() > 0) {
				// log trip infos
				for (int j = 0; j < trips[k].size(); j++) {
					Trip trip = trips[k].get(j);

					for (Item I : trip.getItems()) {
						totalItemW += I.getWeight();
						tripItemCode.add(I.getCode());
					}
					log("trip[" + j + "] = " + trip.toStringShort());
					log("-------------------------------------");
				}
				log(name()
						+ "::greedyConstructMaintainConstraintFTL*********************************************");

				Point s = XR.startPoint(k + 1);
				for (int j = 0; j < trips[k].size(); j++) {

					Trip trip = trips[k].get(j);
					int vehicle_index = trip.start.vehicleIndex;
					if (vehicle_index != k) {
						log(name()
								+ "::greedyConstructMaintainConstraintFTL, vehicle_index = "
								+ vehicle_index + " != k = " + k
								+ ", SERIOUS BUG????????????");
					}

					Point p = XR.prev(XR.endPoint(vehicle_index + 1));
					ArrayList<Integer> pickup_index = mTrip2PickupPointIndices
							.get(trip);
					log(name()
							+ "::greedyConstructMaintainConstraintFTL, pickup_index = "
							+ pickup_index.size());
					// System.out.println("greedyConstructMaintainConstraintFTL, consider vehicle["
					// + vehicle_index + "] = " +
					// getVehicle(vehicle_index).getCode() + ", pickup_index = "
					// + pickup_index.size() + ", p = " + p.ID);

					for (int i = 0; i < pickup_index.size(); i++) {
						int pi = pickup_index.get(i);
						scheduled_pickup_point_index.add(pi);
						Point pickup = pickupPoints.get(pi);
						Point delivery = deliveryPoints.get(pi);
						// System.out.println("greedyConstructMaintainConstraintFTL, prepare addPickupPoint("
						// + pickup.ID + "," + p.ID + ")");

						/*
						 * mgr.performAddOnePoint(pickup, p);
						 * mgr.performAddOnePoint(delivery, pickup);
						 * propagateArrivalDepartureTime(XR,k+1, false);
						 */
						performAddOnePoint(XR, pickup, p);
						performAddOnePoint(XR, delivery, pickup);

						for (int ii : mPoint2IndexLoadedItems.get(p)) {
							mPoint2IndexLoadedItems.get(pickup).add(ii);
							mPoint2IndexLoadedItems.get(delivery).add(ii);
						}
						for (int ii : mPoint2IndexItems.get(pickup)) {
							mPoint2IndexLoadedItems.get(pickup).add(ii);
						}

						p = pickup;
					}
					/*
					 * for(int i = pickup_index.size()-1; i >= 0; i--){ int pi =
					 * pickup_index.get(i); Point delivery =
					 * deliveryPoints.get(pi); System.out.println(
					 * "greedyConstructMaintainConstraintFTL, prepare addDeliveryPoint("
					 * + delivery.ID + "," + p.ID + ")");
					 * mgr.performAddOnePoint(delivery, p); p = delivery; }
					 */
					log(name()
							+ "::greedyConstructMaintainConstraintFTL, scheduled_pickup_point_index = "
							+ scheduled_pickup_point_index.size());
				}
			}
		}
		log(name()
				+ "::::greedyConstructMaintainConstraintFTL, AFTER SET XR from trips"
				+ ", XR = " + toStringShort(XR));
		int nbIntVehicles = computeInternalVehicles();
		
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				continue;
			Vehicle vh = mPoint2Vehicle.get(XR.startPoint(k));
			double load = 0;
			logNotln("vehicle cap = " + vh.getWeight() + ": ");
			for (Point p = XR.next(XR.startPoint(k)); p != XR.endPoint(k); p = XR
					.next(p)) {
				double d = mPoint2Demand.get(p);
				;
				load += d;
				logNotln("[" + p.ID + "," + mPoint2Type.get(p) + "," + d + ","
						+ load + "] ");
				if (load > vh.getWeight())
					logNotln(" FAILED-LOAD ");
			}
			log("");
		}

		if (!checkLoadOnRoute(XR)) {
			log(name()
					+ "::::greedyConstructMaintainConstraintFTL, AFTER SET XR from trips, checkRouteLoadOnRoute FAILED, BUG???");
		}

		if (!checkAllSolution(XR)) {
			log(name()
					+ "::greedyConstructMaintainConstraintFTL, AFTER CONVERTING TRIPS to XR, FAILED, BUG???????");
		}
		for (int i = 0; i < pickupPoints.size(); i++)
			if (!scheduled_pickup_point_index.contains(i))
				cand.add(i);
		log(name() + "::greedyConstructMaintainConstraintFTL, totalItemW = "
				+ totalItemW + ", tripItemCode.sz = " + tripItemCode.size());

		log(name()
				+ "::greedyConstructMaintainConstraintFTL PREPARE Search cand.sz = "
				+ cand.size() + "--------------BEGIN---------------");

		for (int i : cand) {
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			String deliveryLocationCode = mPoint2LocationCode.get(delivery);
			double demand_pickup = mPoint2Demand.get(pickup);
			boolean consistent = checkExclusiveItemsAtAPoint(pickup);
			ArrayList<Vehicle> L = findPossibleVehicle(pickup, delivery);
			// log(name() + "::greedyConstructMaintainConstraintFTL, delivery "
			// + deliveryLocationCode + ", demand = " + demand_pickup
			// + ", possible vehicle L = " + L.size() + ": ");
			// for (Vehicle vh : L)
			// log("[" + vh.getCode() + ", cap = " + vh.getWeight() + "]");
			if (!consistent) {
				log(name()
						+ "::greedyConstructMaintainConstraintFTL, delivery "
						+ deliveryLocationCode + ", demand = " + demand_pickup
						+ " INPUT NOT CONSISTENT EXCLUSIVE ITEMS, BUG???");
			}
		}
		// for (int k = 0; k < vehicles.length + externalVehicles.length; k++) {
		// Vehicle vh = getVehicle(k);
		// log(name() + "::greedyConstructMaintainConstraintFTL, vehicle "
		// + vh.getCode() + ", cap = " + vh.getWeight());
		// }
		// log(name()
		// +
		// "::greedyConstructMaintainConstraintFTL PREPARE Search ---------------END--------------");
		while (cand.size() > 0) {
			Point sel_pickup = null;
			Point sel_delivery = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			int eval_newOrderLoaded = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_d = null;
			int sel_i = -1;
			int sel_k = -1;

			if (log != null) {
				for (int k = 1; k <= XR.getNbRoutes(); k++) {
					Vehicle vh = getVehicle(k-1);//null;
					//if (k > vehicles.length)
					//	vh = externalVehicles[k - vehicles.length - 1];
					//else
					//	vh = vehicles[k - 1];
					
					if (!vh.getCode().equals("51C-636.69"))
						continue;
					log.println("Route " + k + ", vehicle "
							+ vehicles[k - 1].getCode());
					for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
							.next(p)) {
						log.println("point "
								+ p.ID
								// + ", location = "
								// + mPoint2LocationCode.get(p) + ", arr = "
								// + mPoint2ArrivalTime.get(p) + ", dep = "
								// + mPoint2DepartureTime.get(p) +
								// ", duration = "
								// + serviceDuration.get(p)
								+ ", type = " + mPoint2Type.get(p)
								+ ", weight = " + awn.getWeights(p)
								+ ", sumweight = " + awn.getSumWeights(p));
					}
					log.println("---------------------------------------");
				}
			}

			/*
			 * for(int i: cand){ Point pickup = pickupPoints.get(i); Point
			 * delivery = deliveryPoints.get(i); double demand =
			 * mPoint2Demand.get(pickup); String deliveryLocationCode =
			 * mPoint2LocationCode.get(delivery); log(name() +
			 * "::greedyConstructMaintainConstraintFTL CHECK point " + i +
			 * ", delivery location " + deliveryLocationCode + ", demand = " +
			 * demand); boolean ok = false; for(int j = 0; j <
			 * input.getVehicleCategories().length; j++){ Vehicle vh =
			 * input.getVehicleCategories()[j]; if(vh.getWeight() >= demand){
			 * log(name() +
			 * "::greedyConstructMaintainConstraintFTL, FIND enough cap vehicle "
			 * + vh.getCode() + ", cap " + vh.getWeight());
			 * //if(vehicleCanGoToPoint(vh, delivery)){
			 * if(vehicleCategoryCanDoToLocationCode(vh, deliveryLocationCode)){
			 * ok = true; break; }else{ log(name() +
			 * "::greedyConstructMaintainConstraintFTL, FIND enough cap vehicle "
			 * + vh.getCode() + ", cap " + vh.getWeight() +
			 * " BUT CAN NOT GO TO " + deliveryLocationCode); } } } if(!ok){
			 * log(name() +
			 * "::greedyConstructMaintainConstraintFTL CHECK point " + i +
			 * ", delivery location " + deliveryLocationCode + ", demand = " +
			 * demand + " CANNOT FIND SUITABLE vehicle???????");
			 * 
			 * }else{ log(name() +
			 * "::greedyConstructMaintainConstraintFTL CHECK point " + i +
			 * ", delivery location " + deliveryLocationCode + ", demand = " +
			 * demand + " FOUND vehicleCategory");
			 * 
			 * } }
			 */

			for (int i : cand) {
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				double demand = mPoint2Demand.get(pickup);
				/*
				 * log(name() +
				 * "::greedyConstructMaintainConstraintFTL LOOP consider point "
				 * + i + ", delivery location " +
				 * mPoint2LocationCode.get(delivery) + ", demand = " + demand);
				 */

				// Item I = items.get(mPoint2IndexItems.get(pickup)[0]);

				// for(int k = 1; k <= XR.getNbRoutes(); k++){
				// try internal vehicles FIRST
				for (int k = 1; k <= nbIntVehicles; k++) {
					/*
					 * // check conflict items boolean checkConflict = true;
					 * for(Point p = XR.startPoint(k); p != XR.endPoint(k); p =
					 * XR.next(p)){ if(mPoint2Type.get(p).equals("P")){
					 * if(!checkExclusiveItemsAtPoints(pickup, p)) {
					 * checkConflict = false; break; } } } if(!checkConflict)
					 * continue;
					 */

					String vehicleCode = vehicles[k - 1].getCode();
					String pickupLocation = mPoint2LocationCode.get(pickup);
					String deliveryLocation = mPoint2LocationCode.get(delivery);

					// check points cannot be visited
					// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
					// !mPoint2PossibleVehicles.get(delivery).contains(k))
					// continue;

					// if
					// (mVehicle2NotReachedLocations.get(vehicleCode).contains(
					// pickupLocation)
					// || mVehicle2NotReachedLocations.get(vehicleCode)
					// .contains(deliveryLocation))
					if (!vehicleCanGoToPoint(vehicles[k - 1], pickup)
							|| !vehicleCanGoToPoint(vehicles[k - 1], delivery))
						continue;

					for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
							.next(p)) {
						// check exclusive items
						boolean okExclusiveItems = true;

						okExclusiveItems = checkExclusiveItems(p, pickup);

						if (!okExclusiveItems)
							continue;

						// check if after deliverying some items, the vehicle is
						// still loaded,
						// then it must be unloaded (until empty) before
						// pickingup new items
						if (log != null) {
							/*
							 * Vehicle vh = null; if (k > vehicles.length) vh =
							 * externalVehicles[k - vehicles.length - 1]; else
							 * vh = vehicles[k - 1]; if
							 * (vh.getCode().equals("51C-636.69") &&
							 * (I.getCode().equals("10111") || I
							 * .getCode().equals("10110"))) { boolean tmpok =
							 * mPoint2Type.get(p).equals("D") &&
							 * awn.getSumWeights(p) > 0;
							 * log.println("CHECK-P-D, point " + p.ID +
							 * ", type = " + mPoint2Type.get(p) + ", load = " +
							 * awn.getWeights(p) + ", sumW = " +
							 * awn.getSumWeights(p) + ", item " + I.getCode() +
							 * ", weight = " + I.getWeight() + ", RS_CHECK = " +
							 * tmpok);
							 * 
							 * }
							 */
						}
						if (mPoint2Type.get(p).equals("D")
								&& awn.getSumWeights(p) > 0) {
							// cannot pickup any more if there are still items
							// on the vehicle
							continue;
						}

						for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
							// new trial items will be unloaded after d --> need
							// check exclusive items

							if (!checkExclusiveItemAddPoint2Route(XR, k,
									pickup, p, delivery, d)) {
								continue;
							}

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

							if (mPoint2Type.get(XR.next(d)).equals("P")
									&& awn.getSumWeights(d) > 0) {
								// after delivery (accumulated load > 0), there
								// is a pickup --> IGNORE
								continue;
							}

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

							// double ec = CS.evaluateAddTwoPoints(pickup, p,
							// delivery, d);
							double ec = evaluateTimeViolationsAddTwoPoints(k,
									pickup, p, delivery, d);

							double ef = cost.evaluateAddTwoPoints(pickup, p,
									delivery, d);

							int e_o = evaluateNewOrderLoad(k, pickup, p,
									delivery, d);

							// System.out.println("consider i = " + i
							// + ", vehicle k = " + k + ", pickup = "
							// + pickup.ID + ", delivery = " + delivery.ID
							// + ", p = " + p.ID + ", d = " + d.ID
							// + ", ec = " + ec + ", e_o = " + e_o
							// + ", ef = " + ef);

							if (ec > 0)
								continue;// ensure constraint always satisfied

							if (better(ec, e_o, ef, eval_violations,
									eval_newOrderLoaded, eval_cost)) {
								eval_violations = ec;
								eval_cost = ef;
								eval_newOrderLoaded = e_o;
								sel_p = p;
								sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
								sel_i = i;
								sel_k = k;
							}
							/*
							 * if (ec < eval_violations) { eval_violations = ec;
							 * eval_cost = ef; sel_p = p; sel_d = d; sel_pickup
							 * = pickup; sel_delivery = delivery; sel_i = i;
							 * sel_k = k; } else if (ec == eval_violations && ef
							 * < eval_cost) { eval_cost = ef; sel_p = p; sel_d =
							 * d; sel_pickup = pickup; sel_delivery = delivery;
							 * sel_i = i; sel_k = k; }
							 */
						}
					}
				}

				// NO internal vehicles are possible --> TRY external vehicles
				if (sel_p == null) {

					for (int k = vehicles.length + 1; k <= M; k++) {

						// check conflict items
						/*
						 * boolean checkConflict = true; for(Point p =
						 * XR.startPoint(k); p != XR.endPoint(k); p =
						 * XR.next(p)){ if(mPoint2Type.get(p).equals("P")){
						 * if(!checkExclusiveItemsAtPoints(pickup, p)) {
						 * checkConflict = false; break; } } }
						 * if(!checkConflict) continue;
						 */

						Vehicle vh = getVehicle(k - 1);
						String vehicleCode = vh.getCode();// externalVehicles[k
															// - vehicles.length
															// - 1].getCode();
						/*
						 * log(name() +
						 * "::greedyConstructMaintainConstraintFTL NO internal vehicle for point "
						 * + i + " --> TRY external vehicle " + vh.getCode() +
						 * ", cap = " + vh.getWeight());
						 */
						String pickupLocation = mPoint2LocationCode.get(pickup);
						String deliveryLocation = mPoint2LocationCode
								.get(delivery);

						// check points cannot be visited
						// if(!mPoint2PossibleVehicles.get(pickup).contains(k)
						// ||
						// !mPoint2PossibleVehicles.get(delivery).contains(k))
						// continue;
						// if (mVehicle2NotReachedLocations.get(vehicleCode)
						// .contains(pickupLocation)
						// || mVehicle2NotReachedLocations
						// .get(vehicleCode).contains(
						// deliveryLocation))
						// continue;
						if (!vehicleCanGoToPoint(vh, pickup)
								|| !vehicleCanGoToPoint(vh, delivery))
							continue;

						for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
								.next(p)) {
							// check exclusive items
							boolean okExclusiveItems = true;

							okExclusiveItems = checkExclusiveItems(p, pickup);

							if (!okExclusiveItems)
								continue;

							// check if after deliverying some items, the
							// vehicle is still loaded,
							// then it must be unloaded (until empty) before
							// pickingup new items
							if (mPoint2Type.get(p).equals("D")
									&& awn.getSumWeights(p) > 0) {
								// cannot pickup any more if there are still
								// items on the vehicle
								continue;
							}

							for (Point d = p; d != XR.endPoint(k); d = XR
									.next(d)) {

								if (!checkExclusiveItemAddPoint2Route(XR, k,
										pickup, p, delivery, d)) {
									continue;
								}

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

								if (mPoint2Type.get(XR.next(d)).equals("P")
										&& awn.getSumWeights(d) > 0) {
									// after delivery (accumulated load > 0),
									// there is a pickup --> IGNORE
									continue;
								}
								boolean ok = true;
								for (Point tmp = p; tmp != XR.next(d); tmp = XR
										.next(tmp)) {
									if (nwm.getWeight(pickup)
											+ awn.getSumWeights(tmp) > cap[k - 1]) {
										/*
										 * log(name() +
										 * "::greedyConstructMaintainConstraintFTL NO internal vehicle for point "
										 * + i + " --> TRY external vehicle " +
										 * vh.getCode() + ", cap = " +
										 * vh.getWeight() +
										 * " NOT-POSSIBLE demand = " +
										 * nwm.getWeight(pickup) +
										 * ", cap[k-1] = " + cap[k - 1]);
										 */
										ok = false;
										break;
									}
								}
								if (!ok)
									continue;

								double ec = evaluateTimeViolationsAddTwoPoints(
										k, pickup, p, delivery, d);
								double ef = cost.evaluateAddTwoPoints(pickup,
										p, delivery, d);

								int e_o = evaluateNewOrderLoad(k, pickup, p,
										delivery, d);
								/*
								 * log(name() +
								 * "::greedyConstructMaintainConstraintFTL NO internal vehicle for point "
								 * + i + " --> TRY external vehicle " +
								 * vh.getCode() + ", cap = " + vh.getWeight() +
								 * ", pickup = " + pickup.ID + ", p = " + p.ID +
								 * ", delivery = " + delivery.ID + ", d = " +
								 * d.ID + ", timeConstraint ec = " + ec +
								 * ", route = " + XR.toStringRoute(k));
								 */
								// System.out.println("consider i = " + i
								// + ", vehicle k = " + k + ", pickup = "
								// + pickup.ID + ", delivery = "
								// + delivery.ID + ", p = " + p.ID
								// + ", d = " + d.ID + ", ec = " + ec
								// + ", e_o = " + e_o + ", ef = " + ef);

								if (ec > 0)
									continue;// ensure constraint always
												// satisfied

								if (better(ec, e_o, ef, eval_violations,
										eval_newOrderLoaded, eval_cost)) {
									eval_violations = ec;
									eval_cost = ef;
									eval_newOrderLoaded = e_o;
									sel_p = p;
									sel_d = d;
									sel_pickup = pickup;
									sel_delivery = delivery;
									sel_i = i;
									sel_k = k;
								}
								/*
								 * if (ec < eval_violations) { eval_violations =
								 * ec; eval_cost = ef; sel_p = p; sel_d = d;
								 * sel_pickup = pickup; sel_delivery = delivery;
								 * sel_i = i; sel_k = k; } else if (ec ==
								 * eval_violations && ef < eval_cost) {
								 * eval_cost = ef; sel_p = p; sel_d = d;
								 * sel_pickup = pickup; sel_delivery = delivery;
								 * sel_i = i; sel_k = k; }
								 */
							}
						}
					}
					if (sel_k < 0) {
						/*
						 * log(name() +
						 * "::greedyConstructMaintainConstraintFTL NO external vehicle for point "
						 * + i);
						 */
					}
				}
				if (sel_i < 0) {
					/*
					 * log(name() +
					 * "::greedyConstructMaintainConstraintFTL NOT POSSIBLE for point "
					 * + i + ", delivery location = " +
					 * mPoint2LocationCode.get(delivery));
					 */
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

				log(name()
						+ "::greedyConstructMaintainConstraintFTL, performAddOnePoint sel_pickup = "
						+ sel_pickup.ID + ", sel_p = " + sel_p.ID
						+ ", sel_delivery = " + sel_delivery.ID + ", sel_d = "
						+ sel_d.ID + ", XR[" + sel_k + "] = "
						+ XR.toStringRoute(sel_k));

				if (!checkExclusiveItemsAtAPoint(sel_pickup)) {
					log(name()
							+ "::greedyConstructMaintainConstraintFTL checkConflictItemsAtPoint("
							+ sel_pickup.ID + ") BUG???");
				}
				if (sel_pickup.ID == 178) {
					String ic = "";
					for (int ii : mPoint2IndexItems.get(sel_pickup)) {
						Item I = items.get(ii);
						ic += "[" + I.getCode() + ", orderID " + I.getOrderId()
								+ "], ";
					}
					log(name()
							+ "::greedyConstructMaintainConstraintFTL, items at point 178 = "
							+ ic);
				}

				if (!checkAllSolution(XR)) {
					System.out
							.println(name()
									+ "::greedyConstructMaintainConstraintFTL, after performAddOnePoint sel_pickup = "
									+ sel_pickup.ID + ", sel_p = " + sel_p.ID
									+ ", sel_delivery = " + sel_delivery.ID
									+ ", sel_d = " + sel_d.ID
									+ ", checkAllSolution FAILED, XR = "
									+ XR.toString());

					log(name()
							+ "::greedyConstructMaintainConstraintFTL, after performAddOnePoint sel_pickup = "
							+ sel_pickup.ID + ", sel_p = " + sel_p.ID
							+ ", sel_delivery = " + sel_delivery.ID
							+ ", sel_d = " + sel_d.ID
							+ ", checkAllSolution FAILED, XR = "
							+ XR.toString());
				}
				// log.println("add delivery " + sel_delivery.ID + " after "+
				// sel_d.ID +
				// " AND pickup " + sel_pickup.ID + " after " + sel_p.ID);

				// System.out.println("init addOnePoint(" + sel_pickup.ID + ","
				// + sel_p.ID + "), and (" + sel_delivery.ID + ","
				// + sel_d.ID + ", XR = " + XR.toString() + ", CS = "
				// + CS.violations() + ", cost = " + cost.getValue());

				cand.remove(sel_i);
				System.out
						.println(name()
								+ "::greedyConstructMaintainConstraintFTL, XR = "
								+ XR.toStringShort() + ", REMAIN cand = "
								+ cand.size());

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
				log(name()
						+ "::greedyConstructMaintainConstraintFTL NO MOVE BREAK");
				break;

			}
		}
		if (!checkAllSolution(XR)) {
			System.out
					.println(name()
							+ "::greedyConstructMaintainConstraintFTL FINIHSED BUT FAILED, XR = "
							+ XR.toString());
		}
		if (cand.size() > 0) {
			// return list of un-scheduled items
			System.out.println("number of unscheduled items is " + cand.size());

		}
		log(name()
				+ "::greedyConstructMaintainConstraintFTL FINISHED, REMAIN cand = "
				+ cand.size());
		// finalizeLog();
		return cand;
	}

	public HashSet<Integer> greedyConstructMaintainConstraintFTLNoConstraints() {
		// initializeLog();
		HashSet<Integer> cand = new HashSet<Integer>();
		HashSet<Integer> scheduled_pickup_point_index = new HashSet<Integer>();

		log(name()
				+ "::greedyConstructMaintainConstraintFTLNoConstraints, INIT CONVERT TRIP to XR, pickupPoint.sz = "
				+ pickupPoints.size());
		double totalItemW = 0;
		HashSet<String> tripItemCode = new HashSet<String>();
		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		for (int k = 0; k < nbIntVehicles + nbExtVehicles; k++) {
			if (trips[k].size() > 0) {
				// log trip infos
				for (int j = 0; j < trips[k].size(); j++) {
					Trip trip = trips[k].get(j);
					for (Item I : trip.getItems()) {
						totalItemW += I.getWeight();
						tripItemCode.add(I.getCode());
					}
					log("trip[" + j + "] = " + trip.toStringShort());
					log("-------------------------------------");
				}
				log(name()
						+ "::greedyConstructMaintainConstraintFTLNoConstraints*********************************************");

				for (int j = 0; j < trips[k].size(); j++) {
					Trip trip = trips[k].get(j);
					int vehicle_index = trip.start.vehicleIndex;
					if (vehicle_index != k) {
						log(name()
								+ "::greedyConstructMaintainConstraintFTLNoConstraints, vehicle_index = "
								+ vehicle_index + " != k = " + k
								+ ", SERIOUS BUG????????????");
					}
					Point p = XR.prev(XR.endPoint(vehicle_index + 1));
					ArrayList<Integer> pickup_index = mTrip2PickupPointIndices
							.get(trip);
					log(name()
							+ "::greedyConstructMaintainConstraintFTL, pickup_index = "
							+ pickup_index.size());
					// System.out.println("greedyConstructMaintainConstraintFTL, consider vehicle["
					// + vehicle_index + "] = " +
					// getVehicle(vehicle_index).getCode() + ", pickup_index = "
					// + pickup_index.size() + ", p = " + p.ID);

					for (int i = 0; i < pickup_index.size(); i++) {
						int pi = pickup_index.get(i);
						scheduled_pickup_point_index.add(pi);
						Point pickup = pickupPoints.get(pi);
						Point delivery = deliveryPoints.get(pi);
						// System.out.println("greedyConstructMaintainConstraintFTL, prepare addPickupPoint("
						// + pickup.ID + "," + p.ID + ")");

						/*
						 * mgr.performAddOnePoint(pickup, p);
						 * mgr.performAddOnePoint(delivery, pickup);
						 * propagateArrivalDepartureTime(XR,k+1, false);
						 */
						performAddOnePoint(XR, pickup, p);
						performAddOnePoint(XR, delivery, pickup);

						for (int ii : mPoint2IndexLoadedItems.get(p)) {
							mPoint2IndexLoadedItems.get(pickup).add(ii);
							mPoint2IndexLoadedItems.get(delivery).add(ii);
						}
						for (int ii : mPoint2IndexItems.get(pickup)) {
							mPoint2IndexLoadedItems.get(pickup).add(ii);
						}

						p = pickup;
					}
					/*
					 * for(int i = pickup_index.size()-1; i >= 0; i--){ int pi =
					 * pickup_index.get(i); Point delivery =
					 * deliveryPoints.get(pi); System.out.println(
					 * "greedyConstructMaintainConstraintFTL, prepare addDeliveryPoint("
					 * + delivery.ID + "," + p.ID + ")");
					 * mgr.performAddOnePoint(delivery, p); p = delivery; }
					 */
					log(name()
							+ "::greedyConstructMaintainConstraintFTL, scheduled_pickup_point_index = "
							+ scheduled_pickup_point_index.size());
				}
			}
		}

		for (int i = 0; i < pickupPoints.size(); i++)
			if (!scheduled_pickup_point_index.contains(i))
				cand.add(i);
		log(name() + "::greedyConstructMaintainConstraintFTL, totalItemW = "
				+ totalItemW + ", tripItemCode.sz = " + tripItemCode.size());

		log(name()
				+ "::greedyConstructMaintainConstraintFTL PREPARE Search cand.sz = "
				+ cand.size() + "--------------BEGIN---------------");

		for (int i : cand) {
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			String deliveryLocationCode = mPoint2LocationCode.get(delivery);
			double demand_pickup = mPoint2Demand.get(pickup);
			boolean consistent = checkExclusiveItemsAtAPoint(pickup);
			ArrayList<Vehicle> L = findPossibleVehicle(pickup, delivery);
			// log(name() + "::greedyConstructMaintainConstraintFTL, delivery "
			// + deliveryLocationCode + ", demand = " + demand_pickup
			// + ", possible vehicle L = " + L.size() + ": ");
			// for (Vehicle vh : L)
			// log("[" + vh.getCode() + ", cap = " + vh.getWeight() + "]");
			if (!consistent) {
				log(name()
						+ "::greedyConstructMaintainConstraintFTLNoConstraints, delivery "
						+ deliveryLocationCode + ", demand = " + demand_pickup
						+ " INPUT NOT CONSISTENT EXCLUSIVE ITEMS, BUG???");
			}
		}
		return cand;
	}

	@Override
	public HashSet<Integer> search() {
		// HashSet<Integer> remainUnScheduled =
		// greedyConstructMaintainConstraint();
		HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraintFTL();

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

	public RoutingElement buildRoutingElement(RouteNode s) {

		String arrT = DateTimeUtils.unixTimeStamp2DateTime(s.arrivalTime);
		String depT = DateTimeUtils.unixTimeStamp2DateTime(s.departureTime);
		String locationCode = locationCodes.get(s.locationIndex);
		Item[] e_items = new Item[s.items.size()];
		String orderId = "";
		for (int i = 0; i < s.items.size(); i++) {

			ItemAmount ia = s.items.get(i);
			orderId += items.get(ia.itemIndex).getOrderId();
			if (i < s.items.size() - 1)
				orderId += ",";

			e_items[i] = items.get(ia.itemIndex);
		}
		RoutingElement e = new RoutingElement();
		e.setCode(locationCode);
		e.setArrivalTime(arrT);
		e.setDepartureTime(depT);
		e.setDescription(s.type);
		e.setOrderId(orderId);
		e.setItems(e_items);
		return e;
	}

	public void updateLocationAndTimeVehicles() {
		int nbIntVehicles = computeInternalVehicles();
		int nbExtvehicles = computeExternalVehicles();
		
		for (int i = 0; i < nbIntVehicles; i++) {
			Vehicle v = vehicles[i];
			if (trips[i].size() > 0) {
				int startWorkingTime = getLastDepartureTimeOfVehicle(i);
				v.setStartWorkingTime(DateTimeUtils
						.unixTimeStamp2DateTime(startWorkingTime));
				String startLocationCode = locationCodes
						.get(getLastLocationIndex(i));
				v.setStartLocationCode(startLocationCode);
			}
		}
		for (int j = 0; j < nbExtvehicles; j++) {
			Vehicle v = externalVehicles[j];
			int i = j + nbIntVehicles;
			if (trips[i].size() > 0) {
				int startWorkingTime = getLastDepartureTimeOfVehicle(i);
				v.setStartWorkingTime(DateTimeUtils
						.unixTimeStamp2DateTime(startWorkingTime));
				String startLocationCode = locationCodes
						.get(getLastLocationIndex(i));
				v.setStartLocationCode(startLocationCode);
			}
		}

	}

	public ArrayList<RoutingSolution> createNewFTLRoutes(
			PickupDeliverySolution sol) {
		int nbIntVehicles = computeInternalVehicles();
		ArrayList<RoutingSolution> newRoutes = new ArrayList<RoutingSolution>();
		for (int i = 0; i < nbIntVehicles; i++) {
			if (trips[i].size() > 0) {
				RoutingSolution r = sol.getRoute(vehicles[i].getCode());
				ArrayList<RoutingElement> L = new ArrayList<RoutingElement>();

				double distance = 0;
				double max_load = 0;
				for (int j = 0; j < trips[i].size(); j++) {
					Trip t = trips[i].get(j);
					RoutingElement e1 = buildRoutingElement(t.start);
					e1.setDescription("FTL, orderID: " + e1.getOrderId()
							+ ", type = " + t.start.type);
					e1.setLoad(t.start.computeTotalLoad());
					e1.setDistance(distance);
					e1.setItems(mTrip2Items.get(t));

					RoutingElement e2 = buildRoutingElement(t.end);
					e2.setLoad(0);
					e2.setDescription("FTL, orderID: " + e2.getOrderId()
							+ ", type = " + t.end.type);
					L.add(e1);
					L.add(e2);
					distance += a_distance[t.start.locationIndex][t.end.locationIndex];
					e2.setDistance(distance);
					if (j < trips[i].size() - 1) {
						Trip t1 = trips[i].get(j + 1);
						distance += a_distance[t.end.locationIndex][t1.start.locationIndex];
					}
				}
				if (r != null) {
					r.insertHead(L);
				} else {
					RoutingElement[] RE = new RoutingElement[L.size()];
					for (int j = 0; j < L.size(); j++) {
						RE[j] = L.get(j);
					}
					RoutingSolution route = new RoutingSolution(vehicles[i],
							RE, max_load, distance);
					newRoutes.add(route);
				}
			}
		}

		int nbExtVehicles = computeExternalVehicles();
		// external vehicles
		for (int j = 0; j < nbExtVehicles; j++) {
			int i = j + nbIntVehicles;
			Vehicle vh = externalVehicles[j];
			if (trips[i].size() > 0) {
				RoutingSolution r = sol.getRoute(vh.getCode());
				ArrayList<RoutingElement> L = new ArrayList<RoutingElement>();

				double distance = 0;
				double max_load = 0;
				for (int ii = 0; ii < trips[i].size(); ii++) {
					Trip t = trips[i].get(ii);
					RoutingElement e1 = buildRoutingElement(t.start);
					e1.setDescription("FTL-" + t.start.type);
					e1.setLoad(t.start.computeTotalLoad());
					if (max_load < e1.getLoad())
						;
					RoutingElement e2 = buildRoutingElement(t.end);
					e2.setLoad(0);
					e2.setDescription("FTL-" + t.end.type);
					L.add(e1);
					L.add(e2);
					distance += a_distance[t.start.locationIndex][t.end.locationIndex];
					if (j < trips[i].size() - 1) {
						Trip t1 = trips[i].get(j + 1);
						distance += a_distance[t.end.locationIndex][t1.start.locationIndex];
					}
				}
				if (r != null) {
					r.insertHead(L);
				} else {
					RoutingElement[] RE = new RoutingElement[L.size()];
					for (int ii = 0; ii < L.size(); ii++) {
						RE[ii] = L.get(ii);
					}
					RoutingSolution route = new RoutingSolution(vh, RE,
							max_load, distance);
					newRoutes.add(route);
				}
			}
		}

		// sol.insertHead(newRoutes);
		return newRoutes;
	}

	/*
	public PickupDeliverySolution compute(BrennTagPickupDeliveryInput input) {
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		this.travelTimes = input.getTravelTime();
		this.externalVehicles = input.getExternalVehicles();

		initMapData();
		processSplitOrders();
		updateLocationAndTimeVehicles();
		// if(true) return null;

		mapData();
		HashSet<Integer> remainUnScheduled = search();
		int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		for (int k = 0; k < XR.getNbRoutes(); k++)
			scheduled_vehicle[k] = k;
		PickupDeliverySolution sol = buildSolution(XR, scheduled_vehicle,
				remainUnScheduled);
		ArrayList<RoutingSolution> newRoutes = createNewFTLRoutes(sol);

		sol.insertHead(newRoutes);

		for (int i = 0; i < vehicles.length; i++) {
			vehicles[i].setStartWorkingTime(mVehicle2OriginStartWoringTime
					.get(i));
		}
		for (int i = 0; i < externalVehicles.length; i++) {
			externalVehicles[i]
					.setStartWorkingTime(mVehicle2OriginStartWoringTime.get(i
							+ vehicles.length));
		}
		return sol;
	}
	*/
	
	public int[] getScheduleVehicle() {
		// keep order from 0 ,. . ., nbRoutes-1
		int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		for (int i = 0; i < scheduled_vehicle.length; i++)
			scheduled_vehicle[i] = i;
		return scheduled_vehicle;
	}

	public int[] reassignOptimizeLoadTruck() {
		if (!checkExclusiveVehicleLocation(XR)) {
			log(name()
					+ "::reassignOptimizeLoadTruck, BEFORE check EXCLUSIVE VEHICLE-LOCATION FAILED");
		} else {
			log(name()
					+ "::reassignOptimizeLoadTruck, BEFORE check EXCLUSIVE VEHICLE-LOCATION OK");
		}
		// reassign routes to vehicles in order to optimize the load on trucks
		int K = XR.getNbRoutes();
		double[] load = new double[K];
		double[] cap = new double[K];
		for (int k = 0; k < K; k++)
			cap[k] = getVehicle(k).getWeight();
		int[] truck = new int[K];
		boolean[][] forbidden = new boolean[K][K];

		for (int k = 1; k <= K; k++) {
			load[k - 1] = 0;
			for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
					.next(p)) {
				double L = awn.getSumWeights(p);
				if (load[k - 1] < L)
					load[k - 1] = L;
			}

			if (XR.next(XR.startPoint(k)) == XR.endPoint(k))
				truck[k - 1] = -1;
			else
				truck[k - 1] = k - 1;
		}

		for (int i = 0; i < K; i++)
			for (int j = 0; j < K; j++)
				forbidden[i][j] = false;

		for (int v = 0; v < K; v++) {
			for (int k = 1; k <= K; k++) {
				// String vehicleCode = getVehicle(k - 1).getCode();
				Vehicle vh = getVehicle(v);// mPoint2Vehicle.get(XR.startPoint(k));
				String vehicleCode = vh.getCode();
				Point s = XR.startPoint(k);
				Vehicle vhr = mPoint2Vehicle.get(s);
				if (!isInternalVehicle(vh) && isInternalVehicle(vhr)) {
					forbidden[v][k - 1] = true;
				}
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String locationCode = mPoint2LocationCode.get(p);
					if (mVehicle2NotReachedLocations.get(vehicleCode) != null)
						if (mVehicle2NotReachedLocations.get(vehicleCode)
								.contains(locationCode)) {
							forbidden[v][k - 1] = true;
						}
				}
			}
		}

		OptimizeLoadTruckAssignment solver = new OptimizeLoadTruckAssignment();
		solver.solve(truck, load, cap, forbidden, 2000);
		int[] s = solver.getSolution();
		return s;
	}

	public void disableSplitRequests() {
		for (int i = 0; i < requests.length; i++) {
			requests[i].setSplitDelivery("N");
			// System.out.print("Order " + requests[i].getOrderID() + ": ");
			// for (int j = 0; j < requests[i].getItems().length; j++) {
			// System.out.print(requests[i].getItems()[j].getWeight() + ", ");
			// }
			// System.out.println();
		}
	}

	public void generateVehicles(BrennTagPickupDeliveryInput input, int rep) {
		// from vehicle category, generate vehicles with a replication factor
		// and add to external vehicles list
		if (rep == 0)
			return;
		int idx = 0;

		ArrayList<Vehicle> l_externalVehicles = new ArrayList<Vehicle>();
		if (input.getExternalVehicles() != null)
			for (int i = 0; i < input.getExternalVehicles().length; i++) {
				l_externalVehicles.add(input.getExternalVehicles()[i]);
			}
		idx = l_externalVehicles.size();

		ArrayList<ExclusiveVehicleLocation> LE = new ArrayList<ExclusiveVehicleLocation>();
		if (input.getExclusiveVehicleLocations() != null)
			for (int k = 0; k < input.getExclusiveVehicleLocations().length; k++) {
				LE.add(input.getExclusiveVehicleLocations()[k]);
			}

		if (input.getVehicleCategories() != null)
			for (int i = 0; i < input.getVehicleCategories().length; i++) {
				for (int j = 0; j < rep; j++) {
					Vehicle v = input.getVehicleCategories()[i].clone();
					v.setDescription("SUGGESTED");
					idx++;
					v.setCode("SUGGESTED-"
							+ idx
							+ "-"
							+ input.getVehicleCategories()[i]
									.getVehicleCategory());
					l_externalVehicles.add(v);
					if (log != null)
						log.println("GENERATE VEHICLE code = " + v.getCode()
								+ ", category = " + v.getVehicleCategory()
								+ "," + " l_externalVehicles = "
								+ l_externalVehicles.size());

					for (int k = 0; k < input
							.getExclusiveVehicleCategoryLocations().length; k++) {
						ExclusiveVehicleLocation e = input
								.getExclusiveVehicleCategoryLocations()[k];
						if (e.getVehicleCode().equals(v.getVehicleCategory())) {
							ExclusiveVehicleLocation ne = new ExclusiveVehicleLocation(
									v.getCode(), e.getLocationCode());
							LE.add(ne);
						}
					}
				}
			}

		Vehicle[] new_externalVehicles = new Vehicle[l_externalVehicles.size()];
		for (int i = 0; i < new_externalVehicles.length; i++)
			new_externalVehicles[i] = l_externalVehicles.get(i);
		input.setExternalVehicles(new_externalVehicles);

		// set exclusive vehicle-location
		ExclusiveVehicleLocation[] EVL = new ExclusiveVehicleLocation[LE.size()];
		for (int i = 0; i < EVL.length; i++)
			EVL[i] = LE.get(i);
		input.setExclusiveVehicleLocations(EVL);
	}

	public double getTotalItemWeight() {
		double totalItemWeights = 0;
		if (input.getRequests() != null)
			for (int i = 0; i < input.getRequests().length; i++) {
				if (input.getRequests()[i].getItems() != null)
					for (int j = 0; j < input.getRequests()[i].getItems().length; j++) {
						totalItemWeights += input.getRequests()[i].getItems()[j]
								.getWeight();
					}
			}
		return totalItemWeights;
	}

	public VarRoutesVR createNewVarRouteFromTrip(VehicleTrip trip, Vehicle vh) {
		VRManager new_mgr = new VRManager();
		VarRoutesVR newXR = new VarRoutesVR(new_mgr);
		int nextPointID = getNextPointID();
		Point s = new Point(nextPointID);
		Point t = new Point(nextPointID + 1);
		allPoints.add(s);
		allPoints.add(t);
		mPoint2Type.put(s, "S");
		mPoint2Type.put(t, "T");

		mPoint2Demand.put(s, 0.0);
		mPoint2Demand.put(t, 0.0);
		mPoint2Vehicle.put(s, vh);
		mPoint2Vehicle.put(t, vh);

		mPoint2LocationCode.put(s, vh.getStartLocationCode());
		mPoint2LocationCode.put(t, vh.getEndLocationCode());
		earliestAllowedArrivalTime.put(s,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		serviceDuration.put(s, 0);// load-unload is 30 minutes
		if (vh.getEndWorkingTime() != null)
			lastestAllowedArrivalTime.put(s,
					(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));
		earliestAllowedArrivalTime.put(t,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		serviceDuration.put(t, 0);// load-unload is 30 minutes
		if (vh.getEndWorkingTime() != null)
			lastestAllowedArrivalTime.put(t,
					(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));

		newXR.addRoute(s, t);
		for (int i = 0; i < pickupPoints.size(); i++) {
			newXR.addClientPoint(pickupPoints.get(i));
			newXR.addClientPoint(deliveryPoints.get(i));
		}
		new_mgr.close();

		VarRoutesVR oldXR = mTrip2VarRoute.get(trip);
		VRManager oldMGR = oldXR.getVRManager();
		System.out.println(name() + "::createNewVarRouteFromTrip, trip = "
				+ trip.seqPointString() + ", oldXR = " + oldXR.toString());

		Point curPoint = newXR.startPoint(1);
		for (int i = 0; i < trip.seqPoints.size(); i++) {
			Point p = trip.seqPoints.get(i);
			log(name() + "::createNewVarRouteFromTrip, addOnePoint(p = " + p.ID
					+ ")");
			// System.out.println(name()
			// + "::createNewVarRouteFromTrip, addOnePoint(p = " + p.ID
			// + ")");

			// new_mgr.performAddOnePoint(p, curPoint);
			performAddOnePoint(new_mgr.getVarRoutesVR(), p, curPoint);

			curPoint = p;

			log(name() + "::createNewVarRouteFromTrip, removeOnePoint(p = "
					+ p.ID + ") from old route");
			// System.out.println(name()
			// + "::createNewVarRouteFromTrip, removeOnePoint(p = " + p.ID
			// + ") from old route");
			// remove p from old VarRoute

			// oldMGR.performRemoveOnePoint(p);
			performRemoveOnePoint(oldMGR.getVarRoutesVR(), p);
		}

		/*
		 * propagateArrivalDepartureTime(newXR, false);
		 * propagateArrivalDepartureTime(oldXR, false);
		 */

		return newXR;
	}

	public void performMoveMergeTrip(Point pickup, Point delivery,
			VehicleTrip trip) {
		// remove pickup and delivery for its current routes and re-insert them
		// into new trip
		Point sel_p = null;
		Point sel_d = null;

		VarRoutesVR XR = mTrip2VarRoute.get(trip);
		if (XR == null) {
			System.out.println(name()
					+ "::performMoveMergeTrip, XR NULL, BUG????");
		}

		trip.setSolver(this);
		int k = XR.route(pickup);
		/*
		 * mgr.performRemoveOnePoint(pickup);
		 * mgr.performRemoveOnePoint(delivery);
		 * propagateArrivalDepartureTime(XR, k, false);
		 */
		performRemoveOnePoint(XR, pickup);
		performRemoveOnePoint(XR, delivery);

		System.out.println(name() + "::performMoveMergeTrip START, pickup = "
				+ pickup.ID + ", delivery = " + delivery.ID + ", trip = "
				+ trip.seqPointString());
		System.out.println(name() + "::performMoveMergeTrip, XR of trip = "
				+ XR.toStringShort());

		double min_e = Integer.MAX_VALUE;
		// for (int i = 0; i < trip.seqPoints.size(); i++) {
		// Point p = trip.seqPoints.get(i);
		// for (int j = i; j < trip.seqPoints.size(); j++) {
		// Point d = trip.seqPoints.get(j);
		Point fp = trip.seqPoints.get(0);
		k = XR.route(fp);

		for (Point p = XR.prev(fp); p != XR.endPoint(k); p = XR.next(p)) {
			for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
				// int k = XR.route(p);
				// System.out.println(name() +
				// "::performMoveMergeTrip, BEGIN CHECK EXCLUSIVE");
				boolean ok = checkExclusiveItemAddPoint2Route(XR, k, pickup, p,
						delivery, d);
				// System.out.println(name() +
				// "::performMoveMergeTrip, END CHECK EXCLUSIVE, OK = " + ok);

				if (!ok)
					continue;
				else {
					if (pickup.ID == 18 && delivery.ID == 19) {
						System.out
								.println(name()
										+ "::performMoveMergeTrip, pickup = "
										+ pickup.ID
										+ ", delivery = "
										+ delivery.ID
										+ ", After checkExclusiveItemAddPoint2Route --> ACCEPT p = "
										+ p.ID + ", d = " + d.ID);
					}
				}
				if (mPoint2Type.get(p).equals("D") && awn.getSumWeights(p) > 0) {
					// cannot pickup any more if there are still items
					// on the vehicle
					continue;
				}

				if (mPoint2Type.get(XR.next(d)).equals("P")
						&& awn.getSumWeights(d) > 0) {
					// after delivery (accumulated load > 0), there
					// is a pickup --> IGNORE
					continue;
				}

				double ec = evaluateTimeViolationsAddTwoPoints(k, pickup, p,
						delivery, d);

				if (ec > 0)
					continue;

				double e = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
				if (e < min_e) {
					min_e = e;
					sel_p = p;
					sel_d = d;
				}
			}
		}
		if (sel_p == null) {
			System.out.println(name()
					+ "::performMoveMergeTrip, sel_p = NULL BUG???");
			log(name() + "::performMoveMergeTrip, sel_p = NULL BUG???");
		} else {
			System.out.println(name() + "::performMoveMergeTrip, sel_p = "
					+ sel_p.ID + ", sel_d = " + sel_d.ID);
			/*
			 * mgr.performAddOnePoint(delivery, sel_d);
			 * mgr.performAddOnePoint(pickup, sel_p);
			 * propagateArrivalDepartureTime(XR, k, false);
			 */
			performAddOnePoint(XR, delivery, sel_d);
			performAddOnePoint(XR, pickup, sel_p);

			if (!checkTimeConstraint(XR)) {
				System.out.println(name() + "::performMoveMergeTrip, vehicle["
						+ k + "," + getVehicle(k - 1).getCode() + "], sel_p = "
						+ sel_p.ID + ", sel_d = " + sel_d.ID
						+ ", checkTimeConstraint FAILED");
				log(name() + "::performMoveMergeTrip, vehicle[" + k + ","
						+ getVehicle(k - 1).getCode() + "], sel_p = "
						+ sel_p.ID + ", sel_d = " + sel_d.ID
						+ ", checkTimeConstraint FAILED");
				log(printRouteAndTime(XR, k));

			}
		}

		if (!checkAllSolution(XR)) {
			log(name()
					+ "::performMoveMergeTrip, AFTER MERGE, checkAllSolution FAILED, BUG????");
		}
	}

	public MoveMergeTrip findMoveMergeTrip(Point pickup, Point delivery,
			VehicleTrip trip) {
		// remove pickup and delivery for its current routes and re-insert them
		// into new trip
		Point sel_p = null;
		Point sel_d = null;

		VarRoutesVR XR = mTrip2VarRoute.get(trip);
		if (XR == null) {
			System.out
					.println(name() + "::findMoveMergeTrip, XR NULL, BUG????");
			log(name() + "::findMoveMergeTrip, XR NULL, BUG????");
		}

		trip.setSolver(this);
		int k = XR.route(pickup);
		/*
		 * mgr.performRemoveOnePoint(pickup);
		 * mgr.performRemoveOnePoint(delivery);
		 * propagateArrivalDepartureTime(XR, k, false);
		 */
		Point prev_pickup = XR.prev(pickup);
		Point prev_delivery = XR.prev(delivery);
		if (prev_delivery == pickup) {
			prev_delivery = prev_pickup;
		}
		performRemoveOnePoint(XR, pickup);
		performRemoveOnePoint(XR, delivery);

		System.out.println(name() + "::findMoveMergeTrip START, pickup = "
				+ pickup.ID + ", delivery = " + delivery.ID + ", trip = "
				+ trip.seqPointString());
		System.out.println(name() + "::findMoveMergeTrip, XR of trip = "
				+ XR.toStringShort());

		double min_e = Integer.MAX_VALUE;
		// for (int i = 0; i < trip.seqPoints.size(); i++) {
		// Point p = trip.seqPoints.get(i);
		// for (int j = i; j < trip.seqPoints.size(); j++) {
		// Point d = trip.seqPoints.get(j);
		Point fp = trip.seqPoints.get(0);
		k = XR.route(fp);

		for (Point p = XR.prev(fp); p != XR.endPoint(k); p = XR.next(p)) {
			for (Point d = p; d != XR.endPoint(k); d = XR.next(d)) {
				// int k = XR.route(p);
				// System.out.println(name() +
				// "::performMoveMergeTrip, BEGIN CHECK EXCLUSIVE");
				boolean ok = checkExclusiveItemAddPoint2Route(XR, k, pickup, p,
						delivery, d);
				// System.out.println(name() +
				// "::performMoveMergeTrip, END CHECK EXCLUSIVE, OK = " + ok);

				if (!ok)
					continue;
				else {
					if (pickup.ID == 18 && delivery.ID == 19) {
						System.out
								.println(name()
										+ "::findMoveMergeTrip, pickup = "
										+ pickup.ID
										+ ", delivery = "
										+ delivery.ID
										+ ", After checkExclusiveItemAddPoint2Route --> ACCEPT p = "
										+ p.ID + ", d = " + d.ID);
					}
				}
				if (mPoint2Type.get(p).equals("D") && awn.getSumWeights(p) > 0) {
					// cannot pickup any more if there are still items
					// on the vehicle
					continue;
				}

				if (mPoint2Type.get(XR.next(d)).equals("P")
						&& awn.getSumWeights(d) > 0) {
					// after delivery (accumulated load > 0), there
					// is a pickup --> IGNORE
					continue;
				}

				double ec = evaluateTimeViolationsAddTwoPoints(k, pickup, p,
						delivery, d);

				if (ec > 0)
					continue;

				double e = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
				if (e < min_e) {
					min_e = e;
					sel_p = p;
					sel_d = d;
				}
			}
		}
		// recover
		performAddOnePoint(XR, delivery, prev_delivery);
		performAddOnePoint(XR, pickup, prev_pickup);

		if (sel_p == null) {

			return null;
			// System.out.println(name()
			// + "::performMoveMergeTrip, sel_p = NULL BUG???");
			// log(name()
			// + "::performMoveMergeTrip, sel_p = NULL BUG???");
		} else {
			System.out.println(name() + "::findMoveMergeTrip, sel_p = "
					+ sel_p.ID + ", sel_d = " + sel_d.ID);

			// recover
			// performAddOnePoint(XR,delivery,prev_delivery);
			// performAddOnePoint(XR,pickup,prev_pickup);

			if (true)
				return new MoveMergeTrip(pickup, delivery, trip, sel_p, sel_d,
						min_e);

			/*
			 * mgr.performAddOnePoint(delivery, sel_d);
			 * mgr.performAddOnePoint(pickup, sel_p);
			 * propagateArrivalDepartureTime(XR, k, false);
			 */
			performAddOnePoint(XR, delivery, sel_d);
			performAddOnePoint(XR, pickup, sel_p);

			if (!checkTimeConstraint(XR)) {
				System.out.println(name() + "::findMoveMergeTrip, vehicle[" + k
						+ "," + getVehicle(k - 1).getCode() + "], sel_p = "
						+ sel_p.ID + ", sel_d = " + sel_d.ID
						+ ", checkTimeConstraint FAILED");
				log(name() + "::findMoveMergeTrip, vehicle[" + k + ","
						+ getVehicle(k - 1).getCode() + "], sel_p = "
						+ sel_p.ID + ", sel_d = " + sel_d.ID
						+ ", checkTimeConstraint FAILED");
				log(printRouteAndTime(XR, k));

			}

		}
		return null;

		// if (!checkAllSolution(XR)) {
		// log(name()
		// +
		// "::performMoveMergeTrip, AFTER MERGE, checkAllSolution FAILED, BUG????");
		// }
	}

	public void logTrips(VarRoutesVR XR) {
		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;
		logTrips(trips);

	}

	public void printTrips(VarRoutesVR XR) {
		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;
		printTrips(trips);

	}

	public void logTrips(ArrayList<VehicleTrip> trips) {
		VehicleTrip[] t = new VehicleTrip[trips.size()];
		for (int i = 0; i < t.length; i++)
			t[i] = trips.get(i);
		// sort
		for (int i = 0; i < t.length; i++) {
			for (int j = i + 1; j < t.length; j++) {
				if (t[i].vehicle.getCode().compareTo(t[j].vehicle.getCode()) > 0) {
					VehicleTrip tmp = t[i];
					t[i] = t[j];
					t[j] = tmp;
				}
			}
		}

		for (int i = 0; i < t.length; i++) {
			log(name() + "::improveMergeLongTrip, trips[" + i + "].sz = "
					+ t[i].seqPoints.size() + ": " + t[i].seqPointString()
					+ ", load = " + t[i].load + ", vehicle "
					+ t[i].vehicle.getCode() + ", weight = "
					+ t[i].vehicle.getWeight());
		}

	}

	public void printTrips(ArrayList<VehicleTrip> trips) {
		VehicleTrip[] t = new VehicleTrip[trips.size()];
		for (int i = 0; i < t.length; i++)
			t[i] = trips.get(i);
		// sort
		for (int i = 0; i < t.length; i++) {
			for (int j = i + 1; j < t.length; j++) {
				if (t[i].vehicle.getCode().compareTo(t[j].vehicle.getCode()) > 0) {
					VehicleTrip tmp = t[i];
					t[i] = t[j];
					t[j] = tmp;
				}
			}
		}

		for (int i = 0; i < t.length; i++) {
			System.out.println(name() + "::printTrips, trips[" + i + "].sz = "
					+ t[i].seqPoints.size() + ": " + t[i].seqPointString()
					+ ", load = " + t[i].load + ", vehicle "
					+ t[i].vehicle.getCode() + ", weight = "
					+ t[i].vehicle.getWeight());
		}

	}

	public boolean improveMergeLongTrip() {
		// System.out.println(name() + "::improveMergeTrip START");
		log(name() + "::improveMergeLongTrip START");
		VehicleTripCollection tripCollection = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = tripCollection.trips;

		logTrips(trips);

		int n = trips.size();
		int[] X = new int[n];
		int[] Y = new int[n];
		System.out.println(name() + "::improveMergeLongTrip START, nbTrips = "
				+ n);
		log(name() + "::improveMergeLongTrip START, nbTrips = " + n);
		for (int i = 0; i < trips.size(); i++) {
			if (mTrip2VarRoute.get(trips.get(i)) == null) {
				log(name() + "::improveMergeLongTrip START, nbTrips = " + n
						+ ", BUG??? trip has no varRoute");
				System.out.println(name()
						+ "::improveMergeLongTrip START, nbTrips = " + n
						+ ", BUG??? trip has no varRoute");
			}
		}
		ArrayList<Integer> l_edgeX = new ArrayList<Integer>();
		ArrayList<Integer> l_edgeY = new ArrayList<Integer>();

		double maxCap = 0;
		// if(log != null){
		// log.println(name() + "::improveMergeTrip, externalVehicles.length = "
		// + input.getExternalVehicles().length);
		// }
		for (int i = 0; i < input.getVehicleCategories().length; i++) {
			double cap = input.getVehicleCategories()[i].getWeight();
			if (maxCap < cap)
				maxCap = cap;
			// if(log != null){
			// log.println(name() +
			// "::improveMergeTrip, capacity external vehicles = " + cap +
			// ", maxCap = " + maxCap);
			// }
		}
		for (int i = 0; i < n; i++) {
			if (true) {// trip with any length
				int ki = tripCollection.mTrip2Route.get(trips.get(i));
				Point pickup = trips.get(i).seqPoints.get(0);
				Point delivery = trips.get(i).seqPoints.get(1);
				for (int j = 0; j < n; j++) {
					int kj = tripCollection.mTrip2Route.get(trips.get(j));
					if (ki == kj)
						continue;

					if (feasibleMergeTrip(trips.get(i), trips.get(j))
							&& trips.get(i).load + trips.get(j).load <= maxCap) {
						// TEST
						l_edgeX.add(i);
						l_edgeY.add(j);
						if (log != null) {
							log.println(name()
									+ "::improveMergeLongTrip, MaxCap = "
									+ maxCap + ", DETECT MATCH (" + i + "," + j
									+ ")" + " trip ["
									+ trips.get(i).vehicle.getCode() + ", "
									+ trips.get(i).seqPointString()
									+ " amount " + trips.get(i).load + "] TO "
									+ trips.get(j).vehicle.getCode() + ", "
									+ trips.get(j).seqPointString());

						}
						System.out.println(name()
								+ "::improveMergeLongTrip, MaxCap = " + maxCap
								+ ", DETECT MATCH (" + i + "," + j + ")"
								+ " trip [" + trips.get(i).vehicle.getCode()
								+ ", " + trips.get(i).seqPointString()
								+ " amount " + trips.get(i).load + "] TO "
								+ trips.get(j).vehicle.getCode() + ", "
								+ trips.get(j).seqPointString());

					}
				}
			}
		}
		for (int i = 0; i < n; i++) {
			X[i] = i;
			Y[i] = i;
		}
		int[] edgeX = new int[l_edgeX.size()];
		int[] edgeY = new int[l_edgeX.size()];
		double[] w = new double[l_edgeX.size()];
		for (int i = 0; i < l_edgeX.size(); i++) {
			edgeX[i] = l_edgeX.get(i);
			edgeY[i] = l_edgeY.get(i);
			w[i] = 1;
		}
		if (l_edgeX.size() <= 0)
			return false;

		MaxMatching matching = new MaxMatching();
		matching.solve(X, Y, edgeX, edgeY, w);

		int[] solX = matching.getSolutionX();
		int[] solY = matching.getSolutionY();
		if (solX == null || solX.length == 0)
			return false;

		if (log != null) {
			log.println(name() + "::improveMergeLongTrip, matching solution = ");
			for (int i = 0; i < solX.length; i++)
				log.println(solX[i] + " --- " + solY[i]);
		}
		System.out.println(name()
				+ "::improveMergeLongTrip, matching solution = ");
		for (int i = 0; i < solX.length; i++)
			System.out.println(solX[i] + " --- " + solY[i]);
		// perform improvement to reduce trips;
		boolean ok = false;
		for (int i = 0; i < solX.length; i++) {
			VehicleTrip tripi = trips.get(solX[i]);

			Point pickup = tripi.seqPoints.get(0);
			Point delivery = tripi.seqPoints.get(1);

			VehicleTrip tripj = trips.get(solY[i]);
			log(name() + "::improveMergeLongTrip START, nbTrips = " + n
					+ ", solY[" + i + "] = " + solY[i]);
			System.out.println(name()
					+ "::improveMergeLongTrip START, nbTrips = " + n
					+ ", solY[" + i + "] = " + solY[i]);
			if (mTrip2VarRoute.get(tripj) == null) {
				log(name() + "::improveMergeLongTrip START, nbTrips = " + n
						+ ", solY[" + i + "] = " + solY[i]
						+ ", EXCEPTION, Trip has no VVARROUTE");
				System.out.println(name()
						+ "::improveMergeLongTrip START, nbTrips = " + n
						+ ", solY[" + i + "] = " + solY[i]
						+ ", EXCEPTION, Trip has no VVARROUTE");
			}
			// performMoveMergeTrip(pickup, delivery, tripj);

			if (log != null) {
				log.println(name() + "::improveMergeLongTrip, SUCCESSFULLY");
			}
			ok = true;
		}
		return ok;

	}

	public boolean improveMergeTrip() {
		// System.out.println(name() + "::improveMergeTrip START");
		VehicleTripCollection tripCollection = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = tripCollection.trips;

		int n = trips.size();
		int[] X = new int[n];
		int[] Y = new int[n];
		System.out.println(name() + "::improveMergeTrip START, nbTrips = " + n);
		log(name() + "::improveMergeTrip START, nbTrips = " + n);
		for (int i = 0; i < trips.size(); i++) {
			if (mTrip2VarRoute.get(trips.get(i)) == null) {
				log(name() + "::improveMergeTrip START, nbTrips = " + n
						+ ", BUG??? trip has no varRoute");
				System.out.println(name()
						+ "::improveMergeTrip START, nbTrips = " + n
						+ ", BUG??? trip has no varRoute");
			}
		}
		ArrayList<Integer> l_edgeX = new ArrayList<Integer>();
		ArrayList<Integer> l_edgeY = new ArrayList<Integer>();

		double maxCap = 0;
		// if(log != null){
		// log.println(name() + "::improveMergeTrip, externalVehicles.length = "
		// + input.getExternalVehicles().length);
		// }
		for (int i = 0; i < input.getVehicleCategories().length; i++) {
			double cap = input.getVehicleCategories()[i].getWeight();
			if (maxCap < cap)
				maxCap = cap;
			// if(log != null){
			// log.println(name() +
			// "::improveMergeTrip, capacity external vehicles = " + cap +
			// ", maxCap = " + maxCap);
			// }
		}

		MoveMergeTrip[][] M = new MoveMergeTrip[n][n];

		for (int i = 0; i < n; i++) {
			if (trips.get(i).seqPoints.size() == 2) {
				int ki = tripCollection.mTrip2Route.get(trips.get(i));
				Point pickup = trips.get(i).seqPoints.get(0);
				Point delivery = trips.get(i).seqPoints.get(1);
				for (int j = 0; j < n; j++) {
					int kj = tripCollection.mTrip2Route.get(trips.get(j));
					if (ki == kj)
						continue;
					// check exclusive vehicle-location
					VehicleTrip vtj = trips.get(j);
					String pickupLocationCode = mPoint2LocationCode.get(pickup);
					String deliveryLocationCode = mPoint2LocationCode
							.get(delivery);
					if (mVehicle2NotReachedLocations.get(vtj.vehicle.getCode())
							.contains(pickupLocationCode)
							|| mVehicle2NotReachedLocations.get(
									vtj.vehicle.getCode()).contains(
									deliveryLocationCode))
						continue;

					// if(feasibleMergeTrip(trips.get(i), trips.get(j))){
					// TEST
					// }

					MoveMergeTrip mmt = findMoveMergeTrip(pickup, delivery,
							trips.get(j));

					// if (feasibleMove(pickup, delivery, kj)
					if (mmt != null
							&& trips.get(i).load + trips.get(j).load <= maxCap) {
						l_edgeX.add(i);
						l_edgeY.add(j);
						M[i][j] = mmt;
						if (log != null) {
							log.println(name()
									+ "::improveMergeTrip, MaxCap = " + maxCap
									+ ", DETECT MATCH (" + i + "," + j + ")"
									+ " trip ["
									+ trips.get(i).vehicle.getCode() + ", "
									+ trips.get(i).seqPointString()
									+ " amount " + trips.get(i).load + "] TO "
									+ trips.get(j).vehicle.getCode() + ", "
									+ trips.get(j).seqPointString());

						}
						System.out.println(name()
								+ "::improveMergeTrip, MaxCap = " + maxCap
								+ ", DETECT MATCH (" + i + "," + j + ")"
								+ " trip [" + trips.get(i).vehicle.getCode()
								+ ", " + trips.get(i).seqPointString()
								+ " amount " + trips.get(i).load + "] TO "
								+ trips.get(j).vehicle.getCode() + ", "
								+ trips.get(j).seqPointString());

					}
				}
			}
		}
		for (int i = 0; i < n; i++) {
			X[i] = i;
			Y[i] = i;
		}
		int[] edgeX = new int[l_edgeX.size()];
		int[] edgeY = new int[l_edgeX.size()];
		double[] w = new double[l_edgeX.size()];
		for (int i = 0; i < l_edgeX.size(); i++) {
			edgeX[i] = l_edgeX.get(i);
			edgeY[i] = l_edgeY.get(i);
			w[i] = 1;
		}
		if (l_edgeX.size() <= 0)
			return false;

		// MaxMatching matching = new MaxMatching();
		WeightedMaxMatching matching = new WeightedMaxMatching();
		matching.solve(X, Y, edgeX, edgeY, w);

		int[] solX = matching.getSolutionX();
		int[] solY = matching.getSolutionY();
		if (solX == null || solX.length == 0)
			return false;

		if (log != null) {
			log.println(name() + "::improveMergeTrip, matching solution = ");
			for (int i = 0; i < solX.length; i++)
				log.println(solX[i] + " --- " + solY[i]);
		}
		System.out.println(name() + "::improveMergeTrip, matching solution = ");
		for (int i = 0; i < solX.length; i++)
			System.out.println(solX[i] + " --- " + solY[i]);
		// perform improvement to reduce trips;
		boolean ok = false;
		for (int i = 0; i < solX.length; i++) {
			VehicleTrip tripi = trips.get(solX[i]);
			Point pickup = tripi.seqPoints.get(0);
			Point delivery = tripi.seqPoints.get(1);

			VehicleTrip tripj = trips.get(solY[i]);
			log(name() + "::improveMergeTrip START, nbTrips = " + n + ", solY["
					+ i + "] = " + solY[i]);
			System.out.println(name() + "::improveMergeTrip START, nbTrips = "
					+ n + ", solY[" + i + "] = " + solY[i]);
			if (mTrip2VarRoute.get(tripj) == null) {
				log(name() + "::improveMergeTrip START, nbTrips = " + n
						+ ", solY[" + i + "] = " + solY[i]
						+ ", EXCEPTION, Trip has no VARROUTE");
				System.out.println(name()
						+ "::improveMergeTrip START, nbTrips = " + n
						+ ", solY[" + i + "] = " + solY[i]
						+ ", EXCEPTION, Trip has no VARROUTE");
			}
			// performMoveMergeTrip(pickup, delivery, tripj);
			MoveMergeTrip mmt = M[solX[i]][solY[i]];

			log(name() + "::improveMergeTrip, performMoveMerge(" + solX[i]
					+ "-" + solY[i] + "), START, pickup = " + pickup.ID
					+ ", delivery = " + delivery.ID + ", trip = "
					+ mmt.trip.seqPointString());
			// performMoveMergeTrip(mmt.pickup, mmt.delivery, mmt.trip);
			performRemoveOnePoint(XR, mmt.pickup);
			performRemoveOnePoint(XR, mmt.delivery);
			performAddOnePoint(XR, mmt.delivery, mmt.sel_d);
			performAddOnePoint(XR, mmt.pickup, mmt.sel_p);
			log(name() + "::improveMergeTrip, performMoveMerge(" + solX[i]
					+ "-" + solY[i] + "), SUCCESSFULLY, trip = "
					+ mmt.trip.seqPointString());

			ok = true;
		}

		// check exclusive vehicle-location
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
								+ "::improveMergeTrip, EXCLUSIVE VEHICLE-LOCATION FAILED, BUG ?????, vehicle "
								+ vh.getCode() + " <-> location " + lc);
					}
				}
			}
		}
		return ok;
	}

	public void logStateAfterSplittingOrder() {
		log(name() + "::logStateAfterSplittingOrder");
		for (int k = 0; k < vehicles.length; k++) {
			if (trips[k].size() > 0) {
				log("internal trips[" + k + ", code = "
						+ getVehicle(k).getCode() + "].sz = " + trips[k].size());
				for (int j = 0; j < trips[k].size(); j++) {
					Trip t = trips[k].get(j);
					log(t.toStringShort());
					if (!t.checkTime()) {
						log(name()
								+ "::logStateAfterSplittingOrder, TRIP VIOLATIONS TIME ?????????????");
					} else {
						log(name()
								+ "::logStateAfterSplittingOrder, TRIP TIME OK");
					}

					log("--------------------------------------------------");
				}
			}
		}
		if (externalVehicles != null && externalVehicles.length > 0)
			for (int k = 0; k < externalVehicles.length; k++) {
				int k1 = k + vehicles.length;
				if (trips[k1].size() > 0) {
					log("external trips[" + k1 + ", code = "
							+ getVehicle(k1).getCode() + "].sz = "
							+ trips[k1].size());
					for (int j = 0; j < trips[k1].size(); j++) {
						Trip t = trips[k1].get(j);
						log(t.toStringShort());
						if (!t.checkTime()) {
							log(name()
									+ "::logStateAfterSplittingOrder, TRIP VIOLATIONS TIME ?????????????");
						} else {
							log(name()
									+ "::logStateAfterSplittingOrder, TRIP TIME OK");
						}

						log("--------------------------------------------------");
					}

				}
			}
		double itemW = 0;
		log(name() + "::logStateAfterSplittingOrder --> REMAIN request");
		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			String lc = distinct_deliveryLocationCodes.get(i);
			log("Location " + lc);
			double W = 0;
			for (int j : distinct_request_indices.get(i)) {
				PickupDeliveryRequest r = requests[j];
				log("OrderID" + r.getOrderID());
				for (int k = 0; k < r.getItems().length; k++) {
					log("[Item " + r.getItems()[k].getCode() + ", weight "
							+ r.getItems()[k].getWeight() + "]");
					W += r.getItems()[k].getWeight();
				}
			}
			itemW += W;
			log("total weight at location " + lc + " = " + W);
			log("------------------------");
		}
		for (int k = 0; k < vehicles.length; k++) {
			log("Internal Vehicle " + vehicles[k].getCode() + ", cap = "
					+ vehicles[k].getWeight());
		}
		if (input.getVehicleCategories() != null)
			for (int i = 0; i < input.getVehicleCategories().length; i++) {
				log("VehicleCategory "
						+ input.getVehicleCategories()[i].getVehicleCategory()
						+ ", cap = "
						+ input.getVehicleCategories()[i].getWeight());
			}
		double W = computeItemWeightOnTrips();
		double totalItemW = W + itemW;
		log(name() + "::logStateAfterSplittingOrder---------  totalItemW = "
				+ totalItemW + ", itemW on Trips = " + W
				+ ", itemW on requests = " + itemW
				+ "-------------------------");
	}

	public void logStateAfterMergingFTLOrder() {
		log(name() + "::logStateAfterMergingFTLOrder");
		for (int k = 0; k < vehicles.length; k++) {

			if (trips[k].size() > 0) {
				log("internal trips[" + k + ", code = "
						+ getVehicle(k).getCode() + "].sz = " + trips[k].size());
				for (int j = 0; j < trips[k].size(); j++) {
					Trip t = trips[k].get(j);
					log(t.toStringShort());
					if (!t.checkTime()) {
						log(name()
								+ "::logStateAfterMergingFTLOrder, TRIP VIOLATIONS TIME ?????????????");
					} else {
						log(name()
								+ "::logStateAfterMergingFTLOrder, TRIP TIME OK");
					}
				}
				log("--------------------------------------------------");
			}
		}
		if (externalVehicles != null && externalVehicles.length > 0)
			for (int k = 0; k < externalVehicles.length; k++) {
				int k1 = k + vehicles.length;
				if (trips[k1].size() > 0) {
					log("external trips[" + k1 + ", code = "
							+ getVehicle(k1).getCode() + "].sz = "
							+ trips[k1].size());
					for (int j = 0; j < trips[k1].size(); j++) {
						Trip t = trips[k1].get(j);
						log(t.toStringShort());
						if (!t.checkTime()) {
							log(name()
									+ "::logStateAfterMergingFTLOrder, TRIP VIOLATIONS TIME ?????????????");
						} else {
							log(name()
									+ "::logStateAfterMergingFTLOrder, TRIP TIME OK");
						}
					}
					log("--------------------------------------------------");
				}
			}
		log(name() + "::logStateAfterMergingFTLOrder --> REMAIN request");
		double itemW = 0;
		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			String lc = distinct_deliveryLocationCodes.get(i);
			log("Location " + lc);
			double W = 0;
			for (int j : distinct_request_indices.get(i)) {
				PickupDeliveryRequest r = requests[j];
				log("OrderID" + r.getOrderID());
				for (int k = 0; k < r.getItems().length; k++) {
					log("[Item " + r.getItems()[k].getCode() + ", weight "
							+ r.getItems()[k].getWeight() + "]");
					W += r.getItems()[k].getWeight();
				}
			}
			log("total weight at location " + lc + " = " + W);
			log("------------------------");
			itemW += W;
		}
		double itemWTrip = computeItemWeightOnTrips();
		double totalItemW = itemW + itemWTrip;
		log(name()
				+ "::logStateAfterMergingFTLOrder, itemW = "
				+ itemW
				+ ", itemWTrip = "
				+ itemWTrip
				+ ", totalItemW = "
				+ totalItemW
				+ "-----------------------------------------------------------------------");
	}

	public boolean feasibleMergeTrip(VehicleTrip t1, VehicleTrip t2) {
		// find earliest available vehicles having enough capacity
		double load = t1.load + t2.load;
		for (Point p : t1.seqPoints) {
			for (Point q : t2.seqPoints) {
				if (!checkExclusiveItemsAtPoints(p, q))
					return false;
			}
		}

		for (int i = 0; i < input.getVehicleCategories().length; i++) {
			Vehicle vh = input.getVehicleCategories()[i];
			if (vh.getWeight() < load)
				continue;
			if (!vehicleCategoryCanGoToPointsTrip(vh, t1)
					|| !vehicleCategoryCanGoToPointsTrip(vh, t2))
				continue;

			HashSet<Integer> pickupIndices = new HashSet<Integer>();
			for (Point p : t1.seqPoints) {
				if (mPoint2Type.get(p).equals("P")) {
					int ip = mPickupPoint2PickupIndex.get(p);
					pickupIndices.add(ip);
				}
			}
			for (Point p : t2.seqPoints) {
				if (mPoint2Type.get(p).equals("P")) {
					int ip = mPickupPoint2PickupIndex.get(p);
					pickupIndices.add(ip);
				}
			}

			BrennTagRouteSolverForOneVehicle S = new BrennTagRouteSolverForOneVehicle(
					this);
			HashSet<Integer> remain = S.solve(vh, pickupIndices);

			if (remain.size() == 0) {
				log(name() + "::feasibleMergeTrip, MERGE-OK for "
						+ t1.seqPointString() + " AND " + t2.seqPointString());
			} else {
				log(name() + "::feasibleMergeTrip, MERGE-KO for "
						+ t1.seqPointString() + " AND " + t2.seqPointString());
			}

		}
		return true;
	}

	public int countItems(PickupDeliveryRequest[] R) {
		int c = 0;
		for (int i = 0; i < R.length; i++) {
			c += R[i].getItems().length;
		}
		return c;
	}

	public String analyzeDistanceTravelTime() {
		String s = "OK";
		boolean ok = true;
		HashSet<String> setLocationCodes = new HashSet<String>();
		HashMap<String, Integer> mCode2IndexLocationCode = new HashMap<String, Integer>();
		for (int i = 0; i < input.getVehicles().length; i++) {
			setLocationCodes.add(input.getVehicles()[i].getStartLocationCode());
			setLocationCodes.add(input.getVehicles()[i].getEndLocationCode());
		}
		if (input.getExternalVehicles() != null)
			for (int i = 0; i < input.getExternalVehicles().length; i++) {
				setLocationCodes.add(input.getExternalVehicles()[i]
						.getStartLocationCode());
				setLocationCodes.add(input.getExternalVehicles()[i]
						.getEndLocationCode());
			}
		if (input.getVehicleCategories() != null)
			for (int i = 0; i < input.getVehicleCategories().length; i++) {
				setLocationCodes.add(input.getVehicleCategories()[i]
						.getStartLocationCode());
				setLocationCodes.add(input.getVehicleCategories()[i]
						.getEndLocationCode());
			}
		if (input.getRequests() != null)
			for (int i = 0; i < input.getRequests().length; i++) {
				setLocationCodes.add(input.getRequests()[i]
						.getPickupLocationCode());
				setLocationCodes.add(input.getRequests()[i]
						.getDeliveryLocationCode());
			}
		int idx = -1;
		ArrayList<String> listLocationCodes = new ArrayList<String>();
		int n = setLocationCodes.size();
		for (String lc : setLocationCodes) {
			idx++;
			mCode2IndexLocationCode.put(lc, idx);
			listLocationCodes.add(lc);
		}
		boolean[][] hasDistance = new boolean[n][n];
		boolean[][] hasTravelTime = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				hasDistance[i][j] = false;
				hasTravelTime[i][j] = false;
			}
			hasDistance[i][i] = true;
			hasTravelTime[i][i] = true;
		}
		if (input.getDistances() == null) {
			s = "Không có thông tin về khoảng cách";
			ok = false;
			return s;
		}
		if (input.getTravelTime() == null) {
			s = "Không có thông tin về thời gian di chuyển";
			ok = false;
			return s;
		}

		for (int k = 0; k < input.getDistances().length; k++) {
			String src = input.getDistances()[k].getSrcCode();
			String des = input.getDistances()[k].getDestCode();
			if (mCode2IndexLocationCode.get(src) != null
					&& mCode2IndexLocationCode.get(des) != null) {
				int i = mCode2IndexLocationCode.get(src);
				int j = mCode2IndexLocationCode.get(des);
				hasDistance[i][j] = true;
			}
		}
		for (int k = 0; k < input.getTravelTime().length; k++) {
			String src = input.getTravelTime()[k].getSrcCode();
			String des = input.getTravelTime()[k].getDestCode();
			if (mCode2IndexLocationCode.get(src) != null
					&& mCode2IndexLocationCode.get(des) != null) {
				int i = mCode2IndexLocationCode.get(src);
				int j = mCode2IndexLocationCode.get(des);
				hasTravelTime[i][j] = true;
			}
		}
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				if (i != j && !hasDistance[i][j]) {
					String lci = listLocationCodes.get(i);
					String lcj = listLocationCodes.get(j);
					s = "Không có thông tin về khoảng cách giữa locationCode "
							+ lci + " -> " + lcj + ". ";
					ok = false;
					break;
				}
			if (!ok)
				break;
		}
		if (!ok)
			return s;
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++)
				if (i != j && !hasTravelTime[i][j]) {
					String lci = listLocationCodes.get(i);
					String lcj = listLocationCodes.get(j);
					s = "Không có thông tin về thời gian di chuyển từ locationCode "
							+ lci + " -> " + lcj + ". ";
					ok = false;
					break;
				}
			if (!ok)
				break;
		}

		return s;
	}

	public String analyzeInputConsistency() {
		String s = "";
		HashSet<String> itemCodes = new HashSet<String>();
		boolean ok = true;
		if (input.getRequests() != null) {
			for (int i = 0; i < input.getRequests().length; i++) {
				PickupDeliveryRequest r = input.getRequests()[i];
				Item[] I = r.getItems();
				for (int j = 0; j < I.length; j++) {
					if (itemCodes.contains(I[j].getCode())) {
						s = "Duplicate item code " + I[j].getCode();
						ok = false;
					} else {
						itemCodes.add(I[j].getCode());
					}
				}
				for (int j1 = 0; j1 < I.length - 1; j1++)
					for (int j2 = j1 + 1; j2 < I.length; j2++) {
						int idx1 = mItem2Index.get(I[j1]);
						int idx2 = mItem2Index.get(I[j2]);
						if (itemConflict[idx1][idx2]) {
							s = s + "Order " + r.getOrderCode()
									+ " has conflict items " + I[j1].getCode()
									+ "-" + I[j2].getCode() + ",";
							ok = false;
						}
					}
			}
		} else {
			s = s + " NO REQUESTS";
		}
		if (input.getRequests() != null) {
			if (input.getVehicleCategories() != null)
				for (int i = 0; i < input.getRequests().length; i++) {
					PickupDeliveryRequest r = input.getRequests()[i];
					for (int j = 0; j < r.getItems().length; j++) {
						Item I = r.getItems()[j];
						boolean okok = false;
						String desdes = "";
						// if (input.getVehicleCategories() != null)
						// for (int k = 0; k <
						// input.getVehicleCategories().length; k++) {
						for (int k = 0; k < input.getVehicles().length; k++) {
							Vehicle v = input.getVehicles()[k];// input.getVehicleCategories()[k];
							String des = checkFeasibleRequest(I, v);
							if (!des.equals("OK")) {
								desdes = desdes + des;
								// ok = false;
							} else {
								okok = true;
								break;
							}
						}
						for (int k = 0; k < input.getExternalVehicles().length; k++) {
							Vehicle v = input.getExternalVehicles()[k];// input.getVehicleCategories()[k];
							String des = checkFeasibleRequest(I, v);
							if (!des.equals("OK")) {
								desdes = desdes + des;
								// ok = false;
							} else {
								okok = true;
								break;
							}
						}

						if (!okok) {
							s = s + desdes;
							ok = false;
						}
					}
				}
		} else {
			s = s + " NO REQUESTS ";
		}
		/*
		 * if(input.getDistances() == null){ s = s +
		 * "Không có thông tin về khoảng cách di chuyển. "; ok = false; }
		 * if(input.getTravelTime() == null){ s = s +
		 * "Không có thông tin về thời gian di chuyển. "; ok = false; }
		 */
		String sdt = analyzeDistanceTravelTime();
		// System.out.println(name() + "::analyzeInputConsistency, sdt = " +
		// sdt);
		if (!sdt.equals("OK")) {
			ok = false;
			s = s + sdt;
		}
		// if (input.getVehicleCategories() == null
		// || input.getVehicleCategories().length == 0) {
		// s = s + "Thiếu danh mục xe ngoài. ";
		// ok = false;
		// }

		int nbVehicles = 0;
		if (vehicles != null)
			nbVehicles += vehicles.length;
		if (externalVehicles != null)
			nbVehicles += externalVehicles.length;

		for (int i = 0; i < input.getRequests().length; i++) {
			PickupDeliveryRequest r = input.getRequests()[i];
			int pickuplocationIndex = mLocationCode2Index.get(r
					.getPickupLocationCode());
			int deliveryLocationIndex = mLocationCode2Index.get(r
					.getDeliveryLocationCode());
			for (int j = 0; j < r.getItems().length; j++) {
				Item I = r.getItems()[j];
				boolean okdelivery = false;
				for (int k = 0; k < nbVehicles; k++) {
					// Trip t = createDirectTrip4OnItem(k, pickuplocationIndex,
					// deliveryLocationIndex, I, r.getFixLoadTime(),
					// r.getFixUnloadTime());
					boolean check = canVehicleDelivery(k, pickuplocationIndex,
							deliveryLocationIndex, I, r.getFixLoadTime(),
							r.getFixUnloadTime());

					if (check) {
						okdelivery = true;
						break;
					}
				}
				okdelivery = true;// IGNORE, TO BE CHECK canVehicleDelivery
				if (!okdelivery) {
					ok = false;
					s = s
							+ "Không thể tìm thấy xe có thể phục vụ giao Item hàng "
							+ I.getCode() + " của đơn hàng " + r.getOrderCode()
							+ ". ";
				}
			}
		}

		if (ok)
			s = "OK";
		else
			s = "INPUT ERROR: " + s;
		return s;
	}

	public double getDistance(Point p, Point q) {
		String lp = mPoint2LocationCode.get(p);
		String lq = mPoint2LocationCode.get(q);
		int ip = mLocationCode2Index.get(lp);
		int iq = mLocationCode2Index.get(lq);
		return a_distance[ip][iq];
	}
	public InputIndicator analyzeInput(){
		double shortestDistance = Integer.MAX_VALUE;
		double longestDistance = 0;
		double shortestTravelTime = Integer.MAX_VALUE;
		double longestTravelTime = 0;
		double minInternalTruckCapacity = Integer.MAX_VALUE;
		double maxInternalTruckCapacity = 0;
		double minExternalTruckCapacity = Integer.MAX_VALUE;
		double maxExternalTruckCapacity = 0;
		int nbIntCityLocations = 0;
		int nbExtCityLocations = 0;
		double totalItemWeights = 0;
		
		for(int i = 0; i < input.getDistances().length; i++){
			DistanceElement de = input.getDistances()[i];
			if(!de.getSrcCode().equals(de.getDestCode())){
				double d = de.getDistance();
				if(shortestDistance > d) shortestDistance = d;
				if(longestDistance < d) longestDistance = d;
			}
		}
		for(int i = 0; i < input.getTravelTime().length; i++){
			DistanceElement de = input.getTravelTime()[i];
			if(!de.getSrcCode().equals(de.getDestCode())){
				double d = de.getDistance();
				if(shortestTravelTime > d) shortestTravelTime = d;
				if(longestTravelTime < d) longestTravelTime = d;
			}
		}
		if(input.getVehicles() != null)for(int i = 0; i < input.getVehicles().length; i++){
			double w = input.getVehicles()[i].getWeight();
			if(minInternalTruckCapacity > w) minInternalTruckCapacity = w;
			if(maxInternalTruckCapacity < w) maxInternalTruckCapacity = w;
		}
		if(input.getVehicleCategories() != null)
			for(int i = 0; i < input.getVehicleCategories().length; i++){
				double w = input.getVehicleCategories()[i].getWeight();
				if(minExternalTruckCapacity > w) minExternalTruckCapacity = w;
				if(maxExternalTruckCapacity < w) maxExternalTruckCapacity = w;
			}
		for(int i = 0; i < locationCodes.size(); i++){
			String lc = locationCodes.get(i);
			if(mLocation2Type.get(lc).equals(NOI_THANH))
				nbIntCityLocations++;
			else
				nbExtCityLocations++;
		}
		if(input.getRequests() != null)
			for(int i = 0; i < input.getRequests().length; i++){
				for(int j = 0; j < input.getRequests()[i].getItems().length;j++){
					double w = input.getRequests()[i].getItems()[j].getWeight();
					totalItemWeights += w;
				}
			}
		
		return new InputIndicator(shortestDistance, longestDistance, shortestTravelTime, longestTravelTime, minInternalTruckCapacity, maxInternalTruckCapacity, minExternalTruckCapacity, maxExternalTruckCapacity, nbIntCityLocations, nbExtCityLocations, totalItemWeights);
		
	}

	public void reOrderDeliveryPointsPrioritizeFurthestPoint(VarRoutesVR XR) {
		VehicleTripCollection VTC = analyzeTrips(XR);
		for (int i = 0; i < VTC.trips.size(); i++) {
			ArrayList<Point> points = VTC.trips.get(i).seqPoints;
			log(name()
					+ "::reOrderDeliveryPointsPrioritizeFurthestPoint, consider trips["
					+ i + "] = " + VTC.trips.get(i).seqPointString());
			ArrayList<Point> P = new ArrayList<Point>();
			Point lastPickup = null;
			for (int j = 0; j < points.size(); j++) {
				Point p = points.get(j);
				if (mPoint2Type.get(p).equals("D")) {
					P.add(p);
				} else if (mPoint2Type.get(p).equals("P")) {
					lastPickup = p;
				}
			}
			Point[] s_points = new Point[P.size()];
			for (int j = 0; j < P.size(); j++)
				s_points[j] = P.get(j);
			for (int j1 = 0; j1 < s_points.length; j1++) {
				for (int j2 = j1 + 1; j2 < s_points.length; j2++) {
					if (getDistance(lastPickup, s_points[j1]) < getDistance(
							lastPickup, s_points[j2])) {
						Point tmp = s_points[j1];
						s_points[j1] = s_points[j2];
						s_points[j2] = tmp;
					}
				}
			}
			for (Point p : P) {
				performRemoveOnePoint(XR, p);
			}
			Point lastPoint = lastPickup;
			// go to the farthest point FIRST, then optimize remaining points
			if (s_points.length > 0) {
				Point p = s_points[0];
				performAddOnePoint(XR, p, lastPoint);
				lastPoint = p;
			}
			// optimize remaining points
			/*
			 * for (int j = 1; j < s_points.length; j++) { Point p =
			 * s_points[j]; performAddOnePoint(XR, p, lastPoint); lastPoint = p;
			 * }
			 */
			int n = s_points.length;
			double[][] D = new double[n + 1][n + 1];
			for (int j1 = 0; j1 <= n; j1++) {
				Point p1 = lastPickup;
				if (j1 < n)
					p1 = s_points[j1];
				for (int j2 = 0; j2 <= n; j2++) {
					Point p2 = lastPickup;
					if (j2 < n)
						p2 = s_points[j2];

					D[j1][j2] = getDistance(p1, p2);
				}
			}
			System.out
					.println(name()
							+ "::reOrderDeliveryPointsPrioritizeFurthestPoint, START BBTSP, n = "
							+ n + ", D.length = " + D.length);
			BBTSP tsp = new BBTSP();
			tsp.solve(D, 1000);
			int[] sol = tsp.getSolution();
			for (int j = 1; j < n; j++) {
				int idx = sol[j];
				Point p = s_points[idx];
				performAddOnePoint(XR, p, lastPoint);
				lastPoint = p;
			}
		}

		VTC = analyzeTrips(XR);
		log(name()
				+ "::reOrderDeliveryPointsPrioritizeFurthestPoint, after re-ordering, XR = "
				+ XR.toStringShort());
		for (int j = 0; j < VTC.trips.size(); j++) {
			log(name()
					+ "::reOrderDeliveryPointsPrioritizeFurthestPoint, after re-ordering, trips["
					+ j + "] = " + VTC.trips.get(j).seqPointString());
		}
	}

	public String checkFeasibleRequest(Item I, Vehicle v) {
		int idxItem = mItem2Index.get(I);
		int idxReq = mItemIndex2RequestIndex.get(idxItem);
		PickupDeliveryRequest r = requests[idxReq];
		// System.out.println(name() +
		// "::checkFeasibleRequest, mVehicle2NotReachedLocations = " +
		// mVehicle2NotReachedLocations.keySet().size());

		String des = "OK";
		HashSet<String> locations = mVehicle2NotReachedLocations.get(v
				.getCode());// mVehicleCategory2NotReachedLocations.get(v);
		if (locations != null) {
			if (locations.contains(r.getDeliveryLocationCode())) {
				des = "Xe " + v.getCode() + " không thể đến điểm "
						+ r.getDeliveryLocationCode() + " để giao hàng; ";
				return des;
			}
		}

		int fix_load_time = r.getFixLoadTime();
		int fix_unload_time = r.getFixUnloadTime();
		double w = I.getWeight();
		if (w > v.getWeight())
			w = v.getWeight();
		int pickupDuration = (int) (I.getPickupDuration() * w / I.getWeight())
				+ fix_load_time;
		int deliveryDuration = (int) (I.getDeliveryDuration() * w / I
				.getWeight()) + fix_unload_time;

		int startTime = (int) DateTimeUtils.dateTime2Int(v
				.getStartWorkingTime());
		int end_working_time = (int) DateTimeUtils.dateTime2Int(v
				.getEndWorkingTime());

		int arrivalTimePickup = startTime
				+ getTravelTime(v.getStartLocationCode(),
						r.getPickupLocationCode());
		int serviceTimePickup = MAX(arrivalTimePickup,
				(int) DateTimeUtils.dateTime2Int(r.getEarlyPickupTime()));

		int departureTimePickup = serviceTimePickup + pickupDuration;
		int arrivalTimeDelivery = departureTimePickup
				+ getTravelTime(r.getPickupLocationCode(),
						r.getDeliveryLocationCode());
		int serviceTimeDelivery = MAX(arrivalTimeDelivery,
				(int) DateTimeUtils.dateTime2Int(r.getEarlyDeliveryTime()));
		int departureTimeDelivery = serviceTimeDelivery + deliveryDuration;
		int arrivalTimeDepot = departureTimeDelivery
				+ getTravelTime(r.getDeliveryLocationCode(),
						v.getEndLocationCode());

		int latestAllowedArrivalTimeDelivery = (int) DateTimeUtils
				.dateTime2Int(r.getLateDeliveryTime());
		int latestAllowedArrivalTimePickup = (int) DateTimeUtils.dateTime2Int(r
				.getLatePickupTime());
		if (arrivalTimePickup > latestAllowedArrivalTimePickup) {
			des = "Thời gian đến điểm lấy hàng "
					+ I.getCode()
					+ " của đơn "
					+ r.getOrderCode()
					+ " ở địa điểm "
					+ r.getPickupLocationCode()
					+ " sớm nhất là "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTimePickup)
					+ " muộn hơn thời điểm lấy hàng muộn nhất cho phép là "
					+ DateTimeUtils
							.unixTimeStamp2DateTime(latestAllowedArrivalTimePickup)
					+ "; ";
		}
		if (arrivalTimeDelivery > latestAllowedArrivalTimeDelivery) {
			des = ""
					+ "Thời gian đến điểm trả hàng "
					+ I.getCode()
					+ " của đơn "
					+ r.getOrderCode()
					+ " ở địa điểm "
					+ r.getDeliveryLocationCode()
					+ "  sớm nhất là "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeDelivery)
					+ " muộn hơn thời điểm trả hàng muộn nhất cho phép là "
					+ DateTimeUtils
							.unixTimeStamp2DateTime(latestAllowedArrivalTimeDelivery)
					+ "; ";
		}
		if (arrivalTimeDepot > end_working_time) {
			des = "" + "Thời điểm quay về kho " + v.getEndLocationCode()
					+ " là "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeDepot)
					+ " lớn hơn thời gian kết thúc phiên làm việc là "
					+ v.getEndWorkingTime();

		}
		return des;
	}

	public boolean checkDeliveryAll() {
		// return TRUE if each cluster-item can be served by exactly one vehicle
		// greedy approach
		GreedyMatchingVehicleTrip solver = new GreedyMatchingVehicleTrip(this);
		solver.solve(matchTrips, clusterItems, externalVehicles);
		ArrayList<Integer> s_clusters = solver.getSolutionCluster();
		return s_clusters.size() == clusterItems.size();
	}

	public void logMatchTripVehicleCluster() {
		// log(name() + "::logMatchTripVehicleCluster");
		for (int i = 0; i < matchTrips.length; i++) {
			for (int j = 0; j < matchTrips[0].length; j++) {
				if (matchTrips[i][j] != null) {
					log(name() + "::logMatchTripVehicleCluster, match[" + i
							+ "," + j + "] = 1");// +
													// matchTrips[i][j].toString());
					// log(1 + " ");
				} else {
					// log(0 + " ");
					log(name() + "::logMatchTripVehicleCluster, match[" + i
							+ "," + j + "] = 0");
				}
			}

		}
	}

	public void logTripsOfVehicle(int vh_idx) {
		for (int i = 0; i < trips[vh_idx].size(); i++) {
			Trip t = trips[vh_idx].get(i);
			log(name() + "::logTripsOfVehicle, trips = " + t.toString());
		}
	}

	public PickupDeliverySolution computeVehicleSuggestion(
			BrennTagPickupDeliveryInput input) {
		initializeLog();

		// compute total capacity for estimation
		int totalCapacity = 0;
		if (input.getVehicles() != null)
			for (int i = 0; i < input.getVehicles().length; i++)
				totalCapacity += input.getVehicles()[i].getWeight();
		if (input.getExternalVehicles() != null)
			for (int i = 0; i < input.getExternalVehicles().length; i++) {
				totalCapacity += input.getExternalVehicles()[i].getWeight();
			}

		// compute total weight of orders for estimation
		double totalItemWeights = 0;
		int nbItems = 0;
		HashSet<String> itemCodes = new HashSet<String>();
		if (input.getRequests() != null) {
			log(name() + "::computeVehicleSuggestion, nbOrders = "
					+ input.getRequests().length);
			for (int i = 0; i < input.getRequests().length; i++) {
				PickupDeliveryRequest r = input.getRequests()[i];
				log(name() + "::computeVehicleSuggestion, Order "
						+ r.getOrderCode() + ", nbItems = "
						+ r.getItems().length);

				if (r.getItems() != null) {
					for (int j = 0; j < r.getItems().length; j++) {
						Item I = r.getItems()[j];
						if (itemCodes.contains(I.getCode())) {
							log(name()
									+ "::computeVehicleSuggestion, ITEM_CODE EXIST??????");
						} else {
							itemCodes.add(I.getCode());
						}
						totalItemWeights += I.getWeight();
						nbItems++;
						log(name()
								+ "::computeVehicleSuggestion, item "
								+ input.getRequests()[i].getItems()[j]
										.getCode()
								+ ", weight = "
								+ +input.getRequests()[i].getItems()[j]
										.getWeight());
					}
				}
			}
		}
		log(name() + "::computeVehicleSuggestion, nbItems = " + nbItems
				+ ", nbOrders = " + input.getRequests().length);

		int totalVehicleCategoryWeight = 0;
		if (input.getVehicleCategories() != null)
			for (int i = 0; i < input.getVehicleCategories().length; i++) {
				totalVehicleCategoryWeight += input.getVehicleCategories()[i]
						.getWeight();
				input.getVehicleCategories()[i].setDescription("SUGGESTED");
			}
		int rep = 0;
		while (totalCapacity + rep * totalVehicleCategoryWeight < totalItemWeights) {
			rep++;
		}
		if (log != null) {
			log.println(name() + "::computeVehicleSuggestion, totalCapacity = "
					+ totalCapacity + ", totalVehicleCategoryCapacity = "
					+ totalVehicleCategoryWeight + ", totalItemWeight = "
					+ totalItemWeights + ", rep = " + rep);
		}

		this.input = input;
		ConflictBasedExtractor CBE = new ConflictBasedExtractor(this, log);
		CBE.splitConflictItemsOfOrder();
		log(name() + "::computeVehicleSuggestion, AFTER SPLIT CONFLICT ITEMS");
		nbItems = 0;
		totalItemWeights = 0;
		if (input.getRequests() != null) {
			log(name() + "::computeVehicleSuggestion, nbOrders = "
					+ input.getRequests().length);
			for (int i = 0; i < input.getRequests().length; i++) {
				PickupDeliveryRequest r = input.getRequests()[i];
				log(name() + "::computeVehicleSuggestion, Order "
						+ r.getOrderCode() + ", nbItems = "
						+ r.getItems().length);

				if (r.getItems() != null) {
					for (int j = 0; j < r.getItems().length; j++) {
						Item I = r.getItems()[j];

						totalItemWeights += I.getWeight();
						nbItems++;
						log(name()
								+ "::computeVehicleSuggestion, item "
								+ input.getRequests()[i].getItems()[j]
										.getCode()
								+ ", weight = "
								+ input.getRequests()[i].getItems()[j]
										.getWeight());
					}
				}
			}
		}
		log(name() + "::computeVehicleSuggestion, nbItems = " + nbItems
				+ ", nbOrders = " + input.getRequests().length
				+ ", totalItemWeight = " + totalItemWeights);
		log(name()
				+ "::computeVehicleSuggestion---------------------------------------------------------");

		int lastRemain = -1;
		double lastRemainItemWeight = -1;
		boolean firstTrial = true;
		// boolean firstTrial = false;
		Gson gson = new Gson();
		String jsoninput = gson.toJson(input);
		PickupDeliveryRequest[] bkReq = new PickupDeliveryRequest[input
				.getRequests().length];
		for (int i = 0; i < input.getRequests().length; i++) {
			bkReq[i] = input.getRequests()[i].clone();
			Item[] I = new Item[input.getRequests()[i].getItems().length];
			for (int j = 0; j < input.getRequests()[i].getItems().length; j++)
				I[j] = input.getRequests()[i].getItems()[j].clone();
			bkReq[i].setItems(I);
		}

		while (true) {
			if (firstTrial) {
				generateVehicles(input, rep);
				firstTrial = false;
			} else {
				generateVehicles(input, 1);
			}
			PickupDeliveryRequest[] usedReq = new PickupDeliveryRequest[input
					.getRequests().length];
			for (int i = 0; i < bkReq.length; i++) {
				usedReq[i] = bkReq[i].clone();
				Item[] I = new Item[bkReq[i].getItems().length];
				for (int j = 0; j < bkReq[i].getItems().length; j++)
					I[j] = bkReq[i].getItems()[j].clone();
				usedReq[i].setItems(I);
			}

			input.setRequests(usedReq);
			this.input = input;
			this.input.setRequests(usedReq);

			this.requests = usedReq;// input.getRequests();
			this.vehicles = input.getVehicles();
			this.distances = input.getDistances();
			this.travelTimes = input.getTravelTime();
			this.externalVehicles = input.getExternalVehicles();

			log(name()
					+ "::::computeVehicleSuggestion, CountItems of Requests = "
					+ countItems(bkReq));

			mRequest2Index = new HashMap<PickupDeliveryRequest, Integer>();

			for (int i = 0; i < requests.length; i++) {
				mRequest2Index.put(requests[i], i);
				System.out.println("Order " + requests[i].getOrderID());
				double W = 0;
				for (int j = 0; j < requests[i].getItems().length; j++) {
					W += requests[i].getItems()[j].getWeight();
				}
				System.out.println("W = " + W);
			}

			initMapData();
			initItemVehicleConflicts();
			// initDistanceTravelTime();

			String inputOK = analyzeInputConsistency();
			if (!inputOK.equals("OK")) {
				PickupDeliverySolution sol = new PickupDeliverySolution();
				// sol.setDescription(inputOK);
				sol.setErrorMSG(inputOK);
				return sol;
			}

			processSplitOrders();
			processDistinctPickupDeliveryLocationCodes();

			logStateAfterSplittingOrder();

			if (log != null) {
				log.println(name()
						+ "::computeVehicleSuggestion, external vehicles = "
						+ input.getExternalVehicles().length
						+ " AFTER SPLIT orders, REMAIN WEIGHT = "
						+ getTotalItemWeight());
			}

			// System.out.println("BEFORE MERGE FTL");
			// printRequestsOfDistinctLocations();

			processMergeOrderItemsFTL();
			logStateAfterMergingFTLOrder();

			GreedyMatchingVehicleTrip solver = new GreedyMatchingVehicleTrip(
					this);
			solver.solve(matchTrips, clusterItems, externalVehicles);
			ArrayList<Integer> sol_cluster = solver.getSolutionCluster();
			ArrayList<Integer> sol_vehicle = solver.getSolutionVehicle();
			// logMatchTripVehicleCluster();
			log(name()
					+ "::computeVehicleSuggestion, FOUND MATCH VEHICLE-CLUSTER");
			for (int k = 0; k < sol_cluster.size(); k++) {
				log(sol_vehicle.get(k) + " -- " + sol_cluster.get(k));
				Vehicle vh = getVehicle(sol_vehicle.get(k));
				ClusterItems ci = clusterItems.get(sol_cluster.get(k));
				log(vh.getCode() + ", weight " + vh.getWeight() + " -- "
						+ ci.weight);
			}

			if (sol_cluster.size() < clusterItems.size()) {
				continue;
			}

			// trips[8].add(matchTrips[8][2]);
			// log(name() + "::computeVehicleSuggestion, LOG-TRIPS");
			// logTripsOfVehicle(8);

			for (Item I : clusterItems.get(2).items) {
				removeScheduledItem(I);
			}

			if (log != null) {
				log.println(name()
						+ "::computeVehicleSuggestion, external vehicles = "
						+ input.getExternalVehicles().length
						+ " AFTER MERGE FTL orders, REMAIN WEIGHT = "
						+ getTotalItemWeight());
			}

			/*
			 * System.out.println("AFTER MERGE FTL"); for (int k = 0; k <
			 * vehicles.length + externalVehicles.length; k++) { Vehicle vh =
			 * getVehicle(k); if (trips[k].size() > 0) {
			 * System.out.println("Vehicle " + vh.getCode() + ", has " +
			 * trips[k].size() + ", COLLECT-FTL trips"); if (log != null)
			 * log.println("Vehicle " + vh.getCode() + ", has " +
			 * trips[k].size() + ", COLLECT-FTL trips"); for (int i = 0; i <
			 * trips[k].size(); i++) { System.out.println("Vehicle " +
			 * vh.getCode() + ", trips " + trips[k].get(i).toString()); if (log
			 * != null) log.println("Vehicle " + vh.getCode() + ", trips " +
			 * trips[k].get(i).toString());
			 * 
			 * if (!checkConflictItemsOnTrip(trips[k].get(i))) { System.out
			 * .println(name() +
			 * "::computeVehicleSuggestion, RESULT of init Split and Merge FTL FAILED"
			 * ); } }
			 * 
			 * } }
			 */

			disableSplitRequests();

			mapData();

			HashSet<Integer> remainUnScheduled = search();
			if (!checkAllSolution(XR)) {
				System.out
						.println(name()
								+ "::computeVehicleSuggestion, checkAllSolution after search FAILED");
			}

			unScheduledPointIndices = new HashSet<Integer>();
			for (int i : remainUnScheduled)
				unScheduledPointIndices.add(i);

			if (log != null) {
				log.println("LOOP, externalVehicles = "
						+ externalVehicles.length + ", REMAIN "
						+ remainUnScheduled.size());
			}
			System.out
					.println("LOOP, externalVehicles = "
							+ externalVehicles.length + ", REMAIN "
							+ remainUnScheduled.size() + ", lastRemain = "
							+ lastRemain);
			if (log != null)
				log.println("LOOP, externalVehicles = "
						+ externalVehicles.length + ", REMAIN "
						+ remainUnScheduled.size() + ", lastRemain = "
						+ lastRemain);
			if (remainUnScheduled.size() == 0)
				break;
			if (remainUnScheduled.size() == lastRemain
					&& Math.abs(lastRemainItemWeight - getTotalItemWeight()) < 0.00001)
				break;
			lastRemain = remainUnScheduled.size();
			lastRemainItemWeight = getTotalItemWeight();
		}

		int[] scheduled_vehicle = reassignOptimizeLoadTruck();

		mapRoute2Vehicles(scheduled_vehicle);
		// analyzeTrips();

		VehicleTripCollection VTC_trip = analyzeTrips(XR);
		log(name()
				+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS, XR = "
				+ XR.toString());
		for (int i = 0; i < VTC_trip.trips.size(); i++)
			log("TRIP: " + VTC_trip.trips.get(i).seqPointString());
		log("----------------------------------------------------------");
		System.out
				.println(name()
						+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS, XR = "
						+ XR.toString());
		for (int i = 0; i < VTC_trip.trips.size(); i++)
			System.out.println("TRIP[" + i + "]: "
					+ VTC_trip.trips.get(i).seqPointString());
		if (!checkAllSolution(XR)) {
			System.out
					.println(name()
							+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS --> SOLUTION FAILED");
			log(name()
					+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS --> SOLUTION FAILED");
		} else {
			System.out
					.println(name()
							+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS --> SOLUTION OK!!!!!!!!!!");
			log(name()
					+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS --> SOLUTION OK!!!!!!!!!! , route and time as follows");
			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				log(name() + "::computeVehicleSuggestion "
						+ printRouteAndTime(XR, k));
			}
			if (!checkTimeConstraint(XR)) {
				log(name()
						+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS TIME-CONSTRAINT FAILED, BUG???");
			}

		}
		System.out
				.println("----------------------------------------------------------");

		improveMergeTrip();

		// improveMergeLongTrip();

		VehicleTripCollection VTC = analyzeTrips(XR);

		ArrayList<ModelRoute> modelRoutes = extractAndAssignNewVehicle(VTC);

		if (input.getParams().getIntCity().equals("TRUE")) {
			reOrderDeliveryPointsPrioritizeFurthestPoint(XR);
			for (ModelRoute MR : modelRoutes) {
				reOrderDeliveryPointsPrioritizeFurthestPoint(MR.XR);
			}
		}

		log(name() + "::computeVehicleSuggestion, logTrip XR");
		logTrips(XR);
		for (ModelRoute MR : modelRoutes) {
			log(name() + "::computeVehicleSuggestion, logTrip MR.XR");
			logTrips(MR.XR);
		}

		if (log != null) {
			log.println(name()
					+ "::computeVehicleSuggestion, AFTER IMPROVE BY MERGE TRIPS----------------");
			analyzeTrips(XR);
		}
		// int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		// for(int k = 0; k < XR.getNbRoutes(); k++) scheduled_vehicle[k] = k;

		boolean solutionAllOK = true;
		if (!checkAllSolution(XR)) {
			System.out
					.println(name()
							+ "::computeVehicleSuggestion --> BUG??????????????????????????????????");
			solutionAllOK = false;
		}
		for (ModelRoute mr : modelRoutes) {
			if (!checkAllSolution(mr.XR)) {
				System.out
						.println(name()
								+ "::computeVehicleSuggestion --> BUG extended XR ??????????????????????????????????");
				solutionAllOK = false;
			}
		}

		if (log != null) {
			log.println("origin route = " + printRouteAndTime(XR));
			for (ModelRoute mr : modelRoutes) {
				log.println("extended route = " + printRouteAndTime(mr.XR));
			}
		}
		// PickupDeliverySolution sol = buildSolution(scheduled_vehicle,
		// remainUnScheduled);

		// ArrayList<RoutingSolution> ext_routes =
		// computeVehiclesForRemainingRequests(remainUnScheduled);

		PickupDeliverySolution sol = buildSolution(XR, scheduled_vehicle,
				unScheduledPointIndices);

		System.out.println(name()
				+ "::computeVehicleSuggestion, BEFORE APPEND, sz = "
				+ sol.getRoutes().length);
		for (ModelRoute mr : modelRoutes) {
			RoutingSolution[] ext_routes = buildRoutingSolution(mr);
			sol.append(ext_routes);
			System.out
					.println(name()
							+ "::computeVehicleSuggestion, APPEND solution route, sz = "
							+ sol.getRoutes().length);
		}

		ArrayList<PickupDeliveryRequest> list_UnScheduledRequests = new ArrayList<PickupDeliveryRequest>();
		for (int i : unScheduledPointIndices) {
			String description = "";
			Point pickup = pickupPoints.get(i);
			BrennTagRouteSolverForOneVehicle S = new BrennTagRouteSolverForOneVehicle(
					this);
			for (int j = 0; j < input.getVehicleCategories().length; j++) {
				description += S.analyzeNotServed(
						input.getVehicleCategories()[j], i);
			}
			for (int j = 0; j < mPoint2Request.get(pickup).size(); j++) {
				PickupDeliveryRequest r = mPoint2Request.get(pickup).get(j);
				r.setDescription(description);
				list_UnScheduledRequests.add(r);
			}
		}
		PickupDeliveryRequest[] unScheduledRequests = new PickupDeliveryRequest[list_UnScheduledRequests
				.size()];
		for (int i = 0; i < list_UnScheduledRequests.size(); i++)
			unScheduledRequests[i] = list_UnScheduledRequests.get(i);

		sol.setUnScheduledRequests(unScheduledRequests);
		sol.setUnScheduledItems(null);
		// sol.append(ext_routes);

		// ArrayList<RoutingSolution> newRoutes = createNewFTLRoutes(sol);
		// sol.insertHead(newRoutes);

		for (int i = 0; i < vehicles.length; i++) {
			vehicles[i].setStartWorkingTime(mVehicle2OriginStartWoringTime
					.get(i));
		}
		for (int i = 0; i < externalVehicles.length; i++) {
			externalVehicles[i]
					.setStartWorkingTime(mVehicle2OriginStartWoringTime.get(i
							+ vehicles.length));
		}

		log(name() + "::computeVehicleSuggestion, FINAL RESULT");
		int nbScheduledItems = 0;

		HashSet<String> scheduledItemCode = new HashSet<String>();

		for (int i = 0; i < sol.getRoutes().length; i++) {
			RoutingSolution rs = sol.getRoutes()[i];
			log(name() + "::computeVehicleSuggestion, route[" + i + "]");
			for (int j = 0; j < rs.getElements().length; j++) {
				RoutingElement e = rs.getElements()[j];
				if (e.getItems() != null)
					nbScheduledItems += e.getItems().length;
				log(name() + "::computeVehicleSuggestion, description = "
						+ e.getDescription());

				if (e.getItems() != null) {
					for (int k = 0; k < e.getItems().length; k++) {
						Item I = e.getItems()[k];
						scheduledItemCode.add(I.getCode());
					}
				}
			}
		}
		log(name() + "::computeVehicleSuggestion, nbScheduledItems = "
				+ nbScheduledItems + ", set of scheduled items = "
				+ scheduledItemCode.size());

		for (int i = 0; i < sol.getRoutes().length; i++) {
			RoutingSolution rs = sol.getRoutes()[i];
			log(name() + "::computeVehicleSuggestion, scheduled vehicle "
					+ rs.getVehicle().getCode() + ", "
					+ rs.getVehicle().getWeight());
		}

		reassignVehiclePrioritizeInternalVehicles(sol);

		finalizeLog();
		sol.setErrorMSG("OK");
		if (!solutionAllOK)
			sol.setDescription("KO");
		else
			sol.setDescription("OK");

		return sol;
	}

	public void rearrangeExploitAllInternalVehicles(VarRoutesVR XR) {
		// DO NOTHING
		if (true)
			return;
		HashSet<Vehicle> notUsedVehicle = new HashSet<Vehicle>();
		HashSet<Vehicle> usedVehicle = new HashSet<Vehicle>();
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if (isInternalVehicle(vh))
				usedVehicle.add(vh);
		}
		for (int i = 0; i < vehicles.length; i++) {
			Vehicle vh = vehicles[i];
			if (!usedVehicle.contains(vh))
				notUsedVehicle.add(vh);
		}

		VehicleTripCollection VTC = analyzeTrips(XR);
		ArrayList<VehicleTrip> trips = VTC.trips;

	}

	public void reassignExternalVehicleOptimizeLoad(VarRoutesVR XR) {
		HashSet<Vehicle> notUsedExternalVehicles = new HashSet<Vehicle>();
		HashMap<Vehicle, Integer> mVehicle2RouteIndex = new HashMap<Vehicle, Integer>();
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			mVehicle2RouteIndex.put(vh, k);
			if (XR.emptyRoute(k) && !isInternalVehicle(vh)) {
				notUsedExternalVehicles.add(vh);
			}
		}

		VehicleTripCollection VTC = analyzeTrips(XR);

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if (XR.emptyRoute(k))
				continue;
			if (isInternalVehicle(vh))
				continue;

			double load = 0;
			for (VehicleTrip t : VTC.mVehicle2Trips.get(vh)) {
				if (load < t.load)
					load = t.load;
			}
			Vehicle sel_v = null;
			double minW = -1;// Integer.MAX_VALUE;
			for (Vehicle v : notUsedExternalVehicles) {
				if (v.getWeight() >= load && v.getWeight() < vh.getWeight()) {
					if (sel_v == null) {
						sel_v = v;
						minW = v.getWeight();
					} else {
						if (minW > v.getWeight()) {
							sel_v = v;
							minW = v.getWeight();
						}
					}
				}
			}

			if (sel_v != null) {
				int idx = mVehicle2RouteIndex.get(sel_v);
				Point startPoint = XR.startPoint(idx);
				mPoint2Vehicle.put(startPoint, vh);
				mPoint2Vehicle.put(s, sel_v);
				mVehicle2RouteIndex.put(sel_v, k);
				mVehicle2RouteIndex.put(vh, idx);
			}
		}

	}

	public void reassignVehiclePrioritizeInternalVehicles(VarRoutesVR XR) {
		HashSet<Vehicle> notUsedVehicles = new HashSet<Vehicle>();// internal
		// vehicles
		// not-used
		HashSet<Vehicle> usedVehicles = new HashSet<Vehicle>();// internal
		// vehicles used
		HashMap<Vehicle, Integer> mVehicle2RouteIndex = new HashMap<Vehicle, Integer>();

		for (int i = 0; i < vehicles.length; i++)
			notUsedVehicles.add(vehicles[i]);

		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			mVehicle2RouteIndex.put(vh, k);
			if (XR.emptyRoute(k))
				continue;

			log(name()
					+ "::reassignVehiclePrioritizeInternalVehicles, routed Vehicle "
					+ vh.getCode() + ", cap = " + vh.getWeight());
			if (isInternalVehicle(vh)) {
				usedVehicles.add(vh);
				notUsedVehicles.remove(vh);
			}
		}
		log(name()
				+ "::reassignVehiclePrioritizeInternalVehicles, notUsedVehicle.sz = "
				+ notUsedVehicles.size());
		for (Vehicle v : notUsedVehicles) {
			log(name()
					+ "::reassignVehiclePrioritizeInternalVehicles, notUsedVehicle "
					+ v.getCode() + ", cap = " + v.getWeight());
		}
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			if (XR.emptyRoute(k))
				continue;

			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			if (!usedVehicles.contains(vh)) {
				log(name()
						+ "::reassignVehiclePrioritizeInternalVehicles, external vehicle "
						+ vh.getCode() + ", cap = " + vh.getWeight());
				for (Vehicle ivh : notUsedVehicles) {
					boolean ok = true;
					/*
					 * if(ivh.getCode().equals("51C-595.35")){
					 * if(mVehicle2NotReachedLocations.get(ivh.getCode()) ==
					 * null){ log(name() +
					 * "::reassignVehiclePrioritizeInternalVehicles, vehicle " +
					 * ivh.getCode() + " REACH ALL"); }else{ for(String lc:
					 * mVehicle2NotReachedLocations.get(ivh.getCode())){
					 * log(name() +
					 * "::reassignVehiclePrioritizeInternalVehicles, vehicle " +
					 * ivh.getCode() + " FORBID " + lc); } } }
					 */
					for (Point x = s; x != XR.endPoint(k); x = XR.next(x)) {
						String lcx = mPoint2LocationCode.get(x);
						if (mVehicle2NotReachedLocations.get(ivh.getCode()) != null)
							if (mVehicle2NotReachedLocations.get(ivh.getCode())
									.contains(lcx)) {
								/*
								 * if(ivh.getCode().equals("51C-595.20")){
								 * log(name() +
								 * "::reassignVehiclePrioritizeInternalVehicles vehicle "
								 * + ivh.getCode() + " FORBID location " + lcx +
								 * " -> IGNORE"); }
								 */
								ok = false;
								break;
							}
					}
					if (!ok)
						continue;

					if (equalVehicle(vh, ivh)
							|| ivh.getWeight() >= vh.getWeight()) {
						// if(Math.abs(vh.getWeight()-ivh.getWeight()) < 0.1){
						int idx = mVehicle2RouteIndex.get(ivh);
						usedVehicles.add(ivh);
						notUsedVehicles.remove(ivh);
						// rs.setVehicle(ivh);
						mPoint2Vehicle.put(s, ivh);
						Point startP = XR.startPoint(idx);
						mPoint2Vehicle.put(startP, vh);
						mVehicle2RouteIndex.put(ivh, k);
						mVehicle2RouteIndex.put(vh, idx);

						log(name()
								+ "::reassignVehiclePrioritizeInternalVehicles, REPLACE ["
								+ vh.getCode() + "," + vh.getWeight() + "] by"
								+ "  [" + ivh.getCode() + "," + ivh.getWeight());
						break;
					}
				}
			}
		}
	}

	public void reassignVehiclePrioritizeInternalVehicles(
			PickupDeliverySolution sol) {
		HashSet<Vehicle> notUsedVehicles = new HashSet<Vehicle>();// internal
																	// vehicles
																	// not-used
		HashSet<Vehicle> usedVehicles = new HashSet<Vehicle>();// internal
																// vehicles used

		for (int i = 0; i < vehicles.length; i++)
			notUsedVehicles.add(vehicles[i]);

		for (int i = 0; i < sol.getRoutes().length; i++) {
			RoutingSolution rs = sol.getRoutes()[i];
			if (notUsedVehicles.contains(rs.getVehicle())) {
				notUsedVehicles.remove(rs.getVehicle());
				usedVehicles.add(rs.getVehicle());
			}
		}
		for (Vehicle vh : notUsedVehicles) {
			log(name() + "::computeVehicleSuggestion, NOT-USED-VEHICLE "
					+ vh.getCode() + ", " + vh.getWeight());
		}
		for (int i = 0; i < sol.getRoutes().length; i++) {
			RoutingSolution rs = sol.getRoutes()[i];
			Vehicle vh = rs.getVehicle();
			if (!usedVehicles.contains(vh)) {
				for (Vehicle ivh : notUsedVehicles) {
					if (equalVehicle(vh, ivh)) {
						usedVehicles.add(ivh);
						notUsedVehicles.remove(ivh);
						rs.setVehicle(ivh);
						log(name() + "::computeVehicleSuggestion, REPLACE ["
								+ vh.getCode() + "," + vh.getWeight() + "] by"
								+ "  [" + ivh.getCode() + "," + ivh.getWeight());
						break;
					}
				}
			}
		}
		log(name() + "::computeVehicleSuggestion, AFTER REPLACE");
		for (Vehicle vh : notUsedVehicles) {
			log(name() + "::computeVehicleSuggestion, NOT-USED-VEHICLE "
					+ vh.getCode() + ", " + vh.getWeight());
		}

	}

	public void reassignVehicleOptimizeLoadExternalVehicles(
			PickupDeliverySolution sol) {

		for(int i = 0; i < sol.getRoutes().length; i++){
			RoutingSolution s = sol.getRoutes()[i];
			Vehicle vh = s.getVehicle();
			if(isInternalVehicle(vh)) continue;
			double maxLoad = s.computeMaxLoad();
			Vehicle sel_vc = null;
			double minCap = vh.getWeight();
			if(input.getVehicleCategories() != null){
				for(int j = 0; j < input.getVehicleCategories().length; j++){
					Vehicle vc = input.getVehicleCategories()[j];
					if(vc.getWeight() >= maxLoad){
						if(vc.getWeight() < minCap){
							sel_vc = vc;
							minCap = vc.getWeight();
						}
					}
				}
			}
			if(sel_vc != null){
				log(name() + "::reassignVehicleOptimizeLoadExternalVehicles, REPLACE vehicle " 
			+ s.getVehicle().getCode() + " - cap " + s.getVehicle().getWeight() + " BY vehicle-cap " + sel_vc.getWeight());
				System.out.println(name() + "::reassignVehicleOptimizeLoadExternalVehicles, REPLACE vehicle " 
						+ s.getVehicle().getCode() + " - cap " + s.getVehicle().getWeight() + " BY vehicle-cap " + sel_vc.getWeight());
							
				s.setVehicle(sel_vc);
			}
		}
	}

	public boolean equalVehicle(Vehicle v1, Vehicle v2) {
		boolean ok = true;
		if (v1.getEndWorkingTime() != null && v2.getEndWorkingTime() != null)
			ok = v1.getStartLocationCode().equals(v2.getStartLocationCode());

		return Math.abs(v1.getWeight() - v2.getWeight()) < 0.1
				&& v1.getStartWorkingTime().equals(v2.getStartWorkingTime())
				// && v1.getEndWorkingTime().equals(v2.getEndWorkingTime())
				&& ok
				&& v1.getEndLocationCode().equals(v2.getEndLocationCode());
	}

	public void collectUsedVehicles(VehicleTripCollection VTC) {
		ArrayList<VehicleTrip> trips = VTC.trips;
		usedInternalVehicles = new HashSet<Vehicle>();
		usedSugesstedVehicles = new HashSet<Vehicle>();

		for (int i = 0; i < trips.size(); i++) {
			VehicleTrip trip = trips.get(i);
			for (int k = 0; k < vehicles.length; k++) {
				Vehicle vh = vehicles[k];
				if (vh == trip.vehicle) {
					usedInternalVehicles.add(vh);
				}
			}
			for (int k = 0; k < externalVehicles.length; k++) {
				Vehicle vh = externalVehicles[k];
				if (vh == trip.vehicle) {
					usedSugesstedVehicles.add(vh);
				}
			}
		}
	}

	public ArrayList<ModelRoute> extractAndAssignNewVehicle(
			VehicleTripCollection VTC) {
		System.out.println(name() + "::extractAndAssignNewVehicle");

		collectUsedVehicles(VTC);

		ArrayList<VehicleTrip> trips = VTC.trips;
		ArrayList<ModelRoute> modelRoutes = new ArrayList<ModelRoute>();
		for (int i = 0; i < trips.size(); i++) {
			VehicleTrip trip = trips.get(i);
			if (trip.vehicle.getWeight() < trip.load) {
				Vehicle vh = findBestFitVehicle(trip);
				if (vh != null) {
					// vh.setDescription("SUGGESTED");
					VarRoutesVR newXR = createNewVarRouteFromTrip(trip, vh);
					mPoint2Vehicle.put(newXR.startPoint(1), vh);
					if (!checkExclusiveVehicleLocation(newXR)) {
						log(name()
								+ "::extractAndAssignNewVehicle, check EXCLUSIVE VEHICLE-LOCATION FAILED");
					}

					propagateArrivalDepartureTime(newXR, false);
					log(name()
							+ "::extractAndAssignNewVehicle, Find and Assign new Vehicle newXR = "
							+ newXR.toString());

					System.out
							.println(name()
									+ "::extractAndAssignNewVehicle, Find and Assign new Vehicle newXR = "
									+ newXR.toString());
					ModelRoute mr = new ModelRoute();
					mr.XR = newXR;

					modelRoutes.add(mr);

					if (isInternalVehicle(vh))
						usedInternalVehicles.add(vh);
				}
			} else {

				Vehicle vh = findBestFitVehicle(trip);
				if (vh != null)
					if (vh.getWeight() < trip.vehicle.getWeight()) {
						// assign to smaller and fit vehicle
						VarRoutesVR newXR = createNewVarRouteFromTrip(trip, vh);
						mPoint2Vehicle.put(newXR.startPoint(1), vh);
						if (!checkExclusiveVehicleLocation(newXR)) {
							log(name()
									+ "::extractAndAssignNewVehicle, check EXCLUSIVE VEHICLE-LOCATION FAILED");
						}

						propagateArrivalDepartureTime(newXR, false);
						if (log != null) {
							// log.println(name() +
							// "::extractAndAssignNewVehicle, Find and Assign new Vehicle OPTIMIZE LOAD newXR = "
							// + newXR.toString());
							log(name()
									+ "::extractAndAssignNewVehicle, Find and Assign new Vehicle OPTIMIZE LOAD "
									+ ", newVehicle code = " + vh.getCode()
									+ " weight = " + vh.getWeight()
									+ ", oldVehicle = "
									+ trip.vehicle.getWeight() + ", load = "
									+ trip.load + ", newXR = "
									+ newXR.toString());
						}
						System.out
								.println(name()
										+ "::extractAndAssignNewVehicle, Find and Assign new Vehicle OPTIMIZE LOAD "
										+ ", newVehicle = code = "
										+ vh.getCode() + " weight = "
										+ vh.getWeight() + ", oldVehicle = "
										+ trip.vehicle.getWeight()
										+ ", load = " + trip.load
										+ ", newXR = " + newXR.toString());
						ModelRoute mr = new ModelRoute();
						mr.XR = newXR;

						modelRoutes.add(mr);
						if (isInternalVehicle(vh))
							usedInternalVehicles.add(vh);
					}

			}
		}
		System.out.println(name()
				+ "::extractAndAssignNewVehicle FINISHED, collect "
				+ modelRoutes.size() + " new routes");
		return modelRoutes;
	}

	public PickupDeliverySolution computeNew(BrennTagPickupDeliveryInput input) {
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		this.travelTimes = input.getTravelTime();
		this.externalVehicles = input.getExternalVehicles();

		mRequest2Index = new HashMap<PickupDeliveryRequest, Integer>();

		for (int i = 0; i < requests.length; i++) {
			mRequest2Index.put(requests[i], i);
			System.out.println("Order " + requests[i].getOrderID());
			double W = 0;
			for (int j = 0; j < requests[i].getItems().length; j++) {
				W += requests[i].getItems()[j].getWeight();
			}
			System.out.println("W = " + W);
		}
		initMapData();
		initItemVehicleConflicts();
		// initDistanceTravelTime();

		processSplitOrders();

		processDistinctPickupDeliveryLocationCodes();

		// System.out.println("BEFORE MERGE FTL");
		// printRequestsOfDistinctLocations();

		processMergeOrderItemsFTL();
		System.out.println("AFTER MERGE FTL");
		for (int k = 0; k < vehicles.length + externalVehicles.length; k++) {
			Vehicle vh = getVehicle(k);
			if (trips[k].size() > 0) {
				System.out.println("Vehicle " + vh.getCode() + ", has "
						+ trips[k].size() + ", COLLECT-FTL trips");
				if (log != null)
					log.println("Vehicle " + vh.getCode() + ", has "
							+ trips[k].size() + ", COLLECT-FTL trips");
				for (int i = 0; i < trips[k].size(); i++) {
					System.out.println("Vehicle " + vh.getCode() + ", trips "
							+ trips[k].get(i).toString());
					if (log != null)
						log.println("Vehicle " + vh.getCode() + ", trips "
								+ trips[k].get(i).toString());
				}
			}
		}
		// System.out.println("AFTER merge FTL");
		// printRequestsOfDistinctLocations();
		/*
		 * for (int i = 0; i < requests.length; i++) {
		 * System.out.println("requests[" + i + "] = " +
		 * requests[i].getItems().length + ", OrderID = " +
		 * requests[i].getOrderID() + ", deliveryLocationCode = " +
		 * requests[i].getDeliveryLocationCode()); for (int j = 0; j <
		 * requests[i].getItems().length; j++) {
		 * System.out.println(requests[i].getItems()[j].getCode() + "," +
		 * requests[i].getItems()[j].getWeight()); } System.out
		 * .println("----------------------------------------------"); }
		 */
		disableSplitRequests();
		// updateLocationAndTimeVehicles();

		mapData();

		HashSet<Integer> remainUnScheduled = search();
		int[] scheduled_vehicle = reassignOptimizeLoadTruck();

		// int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		// for(int k = 0; k < XR.getNbRoutes(); k++) scheduled_vehicle[k] = k;

		if (!checkAllSolution(XR)) {
			System.out.println("BUG??????????????????????????????????");
		}

		// PickupDeliverySolution sol = buildSolution(scheduled_vehicle,
		// remainUnScheduled);

		ArrayList<RoutingSolution> ext_routes = computeVehiclesForRemainingRequests(remainUnScheduled);

		PickupDeliverySolution sol = buildSolution(XR, scheduled_vehicle,
				unScheduledPointIndices);
		ArrayList<PickupDeliveryRequest> list_UnScheduledRequests = new ArrayList<PickupDeliveryRequest>();
		for (int i : unScheduledPointIndices) {
			String description = "";
			Point pickup = pickupPoints.get(i);
			BrennTagRouteSolverForOneVehicle S = new BrennTagRouteSolverForOneVehicle(
					this);
			for (int j = 0; j < input.getVehicleCategories().length; j++) {
				description += S.analyzeNotServed(
						input.getVehicleCategories()[j], i);
			}
			for (int j = 0; j < mPoint2Request.get(pickup).size(); j++) {
				PickupDeliveryRequest r = mPoint2Request.get(pickup).get(j);
				r.setDescription(description);
				list_UnScheduledRequests.add(r);
			}
		}
		PickupDeliveryRequest[] unScheduledRequests = new PickupDeliveryRequest[list_UnScheduledRequests
				.size()];
		for (int i = 0; i < list_UnScheduledRequests.size(); i++)
			unScheduledRequests[i] = list_UnScheduledRequests.get(i);

		sol.setUnScheduledRequests(unScheduledRequests);
		sol.setUnScheduledItems(null);
		sol.append(ext_routes);

		// ArrayList<RoutingSolution> newRoutes = createNewFTLRoutes(sol);
		// sol.insertHead(newRoutes);

		for (int i = 0; i < vehicles.length; i++) {
			vehicles[i].setStartWorkingTime(mVehicle2OriginStartWoringTime
					.get(i));
		}
		for (int i = 0; i < externalVehicles.length; i++) {
			externalVehicles[i]
					.setStartWorkingTime(mVehicle2OriginStartWoringTime.get(i
							+ vehicles.length));
		}

		finalizeLog();
		return sol;
	}

	public String name() {
		return BrenntagPickupDeliverySolver.class.getName();
	}

	public ArrayList<RoutingSolution> computeVehiclesForRemainingRequests(
			HashSet<Integer> pointIndices) {

		unScheduledPointIndices = new HashSet<Integer>();
		for (int i : pointIndices)
			unScheduledPointIndices.add(i);

		Vehicle[] vehicleCategory = input.getVehicleCategories();

		// ArrayList<PickupDeliverySolution> SOL = new
		// ArrayList<PickupDeliverySolution>();
		ArrayList<RoutingSolution> L = new ArrayList<RoutingSolution>();

		if (vehicleCategory == null)
			return L;

		while (true) {
			VarRoutesVR sel_vr = null;
			double maxWeight = 0;
			double minCap = Integer.MAX_VALUE;
			Vehicle sel_vh = null;
			BrennTagRouteSolverForOneVehicle sel_solver = null;
			for (int i = 0; i < vehicleCategory.length; i++) {
				BrennTagRouteSolverForOneVehicle solverOneVehicle = new BrennTagRouteSolverForOneVehicle(
						this);
				HashSet<Integer> remainIndices = solverOneVehicle.solve(
						vehicleCategory[i], pointIndices);
				if (remainIndices.size() < pointIndices.size()) {
					if (Math.abs(solverOneVehicle.totalWeights - maxWeight) < 0.0001) {
						if (vehicleCategory[i].getWeight() < minCap) {
							maxWeight = solverOneVehicle.totalWeights;
							sel_vr = solverOneVehicle.XR;
							pointIndices = remainIndices;
							sel_vh = vehicleCategory[i];
							sel_solver = solverOneVehicle;
							minCap = vehicleCategory[i].getWeight();
						}
					} else if (solverOneVehicle.totalWeights > maxWeight) {
						maxWeight = solverOneVehicle.totalWeights;
						sel_vr = solverOneVehicle.XR;
						pointIndices = remainIndices;
						sel_vh = vehicleCategory[i];
						sel_solver = solverOneVehicle;
						minCap = vehicleCategory[i].getWeight();
					}
				}
			}
			if (sel_vr == null) {
				System.out.println(name()
						+ "::computeVehiclesForRemainingRequests ==> BREAK");
				break;
			} else {
				System.out.println(name()
						+ "::computeVehiclesForRemainingRequests, GOT "
						+ maxWeight + " for vehicle " + sel_vh.getWeight()
						+ ", remainItems = " + pointIndices.size());
				unScheduledPointIndices.clear();
				for (int i : pointIndices)
					unScheduledPointIndices.add(i);

				sel_vh.setDescription("SUGGESTED");
				PickupDeliverySolution s = buildSolution(sel_vr, sel_vh,
						sel_solver.awn, sel_solver.awe, sel_solver.awm);
				for (int j = 0; j < s.getRoutes().length; j++)
					L.add(s.getRoutes()[j]);

				// SOL.add(s);
			}
		}

		return L;
	}

}
