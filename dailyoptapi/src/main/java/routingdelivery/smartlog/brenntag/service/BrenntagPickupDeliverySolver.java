package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.processing.Processor;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.ConstraintViolationsVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import routingdelivery.service.PickupDeliverySolver;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ExclusiveItem;
import routingdelivery.smartlog.brenntag.model.ExclusiveVehicleLocation;
import routingdelivery.utils.assignment.OptimizeLoadTruckAssignment;
import utils.DateTimeUtils;

public class BrenntagPickupDeliverySolver extends PickupDeliverySolver {
	protected HashMap<String, Integer> mLocationCode2Index;
	protected HashMap<Item, Integer> mItem2LocationIndex;
	protected double[][] a_distance;// distance between two location indices
	protected double[][] a_travelTime;// a_travelTime between two location
										// indices
	protected HashMap<String, HashSet<Integer>> mLocationCode2RequestIndex;

	protected HashMap<String, String> mItemCode2OrderID;

	protected ArrayList<Trip>[] trips; // trips[k] is the list of trip of kth
										// vehicle (k = 0,1,...)
	protected ArrayList<String> locationCodes;
	protected HashMap<Trip, Item[]> mTrip2Items;
	protected HashMap<Trip, ArrayList<Integer>> mTrip2PickupPointIndices;
	// protected HashMap<Trip, Point> mTrip2DeliveryPoint;

	protected HashMap<Integer, Double> mVehicle2Distance;
	protected HashMap<Integer, String> mVehicle2OriginStartWoringTime;
	protected HashMap<Integer, Integer> mPickupIndex2ScheduledVehicleIndex;

	ArrayList<String> distinct_pickupLocationCodes = new ArrayList<String>();
	ArrayList<String> distinct_deliveryLocationCodes = new ArrayList<String>();
	ArrayList<HashSet<Integer>> distinct_request_indices = new ArrayList<HashSet<Integer>>();

	public int getLastDepartureTimeOfVehicle(int vh) {
		if (trips[vh].size() > 0) {
			Trip last_trip = trips[vh].get(trips[vh].size() - 1);
			return last_trip.end.departureTime;
		} else {
			Vehicle v = null;
			if (vh >= vehicles.length)
				v = externalVehicles[vh - vehicles.length];
			else
				v = vehicles[vh];
			return (int) DateTimeUtils.dateTime2Int(v.getStartWorkingTime());
		}
	}

	public int getLastLocationIndex(int vh) {
		if (trips[vh].size() > 0) {
			Trip last_trip = trips[vh].get(trips[vh].size() - 1);
			return last_trip.end.locationIndex;
		} else {
			Vehicle v = null;
			if (vh >= vehicles.length)
				v = externalVehicles[vh - vehicles.length];
			else
				v = vehicles[vh];
			return mLocationCode2Index.get(v.getStartLocationCode());
		}
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
		int arrivalTimePickup = -1;
		int departureTimePickup = -1;
		for (int i = 0; i < vehicles.length; i++) {
			int locationIdx = getLastLocationIndex(i);
			int deptime = getLastDepartureTimeOfVehicle(i);
			if (deptime + a_travelTime[locationIdx][pickupLocationIdx] <= latestAllowArrivalTimePickup) {
				if (amount <= vehicles[i].getWeight())
					// has available vehicle with enough capacity
					return null;
				else {
					if (max_cap < vehicles[i].getWeight()) {
						sel_vehicle = i;
						max_cap = vehicles[i].getWeight();
						arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);
					}
				}
			}
		}
		if (sel_vehicle == -1)
			return null;

		ArrayList<ItemAmount> L = new ArrayList<ItemAmount>();
		L.add(new ItemAmount(itemIndex, max_cap));
		int serviceTimePickup = arrivalTimePickup < ealiestAllowArrivalTimePickup ? ealiestAllowArrivalTimePickup
				: arrivalTimePickup;
		double factor = 1;
		if (useDurationPerUnit) {
			factor = vehicles[sel_vehicle].getWeight();
		}

		departureTimePickup = (int) serviceTimePickup
				+ (int) (pickupDurationPerUnit * max_cap * factor / amount)
				+ fix_load_time;

		int arrivalTimeDelivery = departureTimePickup
				+ (int) a_travelTime[pickupLocationIdx][deliveryLocationIdx];
		int serviceTimeDelivery = arrivalTimeDelivery < earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery
				: arrivalTimeDelivery;
		int departureTimeDelivery = serviceTimeDelivery
				+ (int) (deliveryDurationPerUnit * max_cap * factor / amount)
				+ fix_unload_time;

		RouteNode start = new RouteNode(pickupLocationIdx, arrivalTimePickup,
				departureTimePickup, L, sel_vehicle, "P");// pickup
		RouteNode end = new RouteNode(deliveryLocationIdx, arrivalTimeDelivery,
				departureTimeDelivery, L, sel_vehicle, "D");// delivery

		return new Trip(start, end, "FTL");
	}

	public Trip splitLargeItemCreateATripWithExternalVehicle(int itemIndex,
			double amount, int pickupLocationIdx, int deliveryLocationIdx,
			int pickupDurationPerUnit, int deliveryDurationPerUnit,
			int fix_load_time, int fix_unload_time,
			int ealiestAllowArrivalTimePickup,
			int latestAllowArrivalTimePickup,
			int earliestAllowArrivalTimeDelivery,
			int latestAllowArrivalTimeDelivery, boolean useDurationPerUnit) {
		double max_cap = 0;
		int sel_vehicle = -1;
		int arrivalTimePickup = -1;
		int departureTimePickup = -1;
		for (int i = 0; i < externalVehicles.length; i++) {
			int locationIdx = getLastLocationIndex(i + vehicles.length);
			int deptime = getLastDepartureTimeOfVehicle(i + vehicles.length);
			if (deptime + a_travelTime[locationIdx][pickupLocationIdx] <= latestAllowArrivalTimePickup) {
				if (amount <= externalVehicles[i].getWeight())
					// has available vehicle with enough capacity
					return null;
				else {
					if (max_cap < externalVehicles[i].getWeight()) {
						sel_vehicle = i;
						max_cap = externalVehicles[i].getWeight();
						arrivalTimePickup = (int) (deptime + a_travelTime[locationIdx][pickupLocationIdx]);
					}
				}
			}
		}
		if (sel_vehicle == -1)
			return null;

		ArrayList<ItemAmount> L = new ArrayList<ItemAmount>();
		L.add(new ItemAmount(itemIndex, max_cap));
		int serviceTimePickup = arrivalTimePickup < ealiestAllowArrivalTimePickup ? ealiestAllowArrivalTimePickup
				: arrivalTimePickup;
		double factor = 1;
		if (useDurationPerUnit) {
			factor = externalVehicles[sel_vehicle].getWeight();
		}

		departureTimePickup = (int) serviceTimePickup
				+ (int) (pickupDurationPerUnit * max_cap * factor / amount)
				+ fix_load_time;

		int arrivalTimeDelivery = departureTimePickup
				+ (int) a_travelTime[pickupLocationIdx][deliveryLocationIdx];
		int serviceTimeDelivery = arrivalTimeDelivery < earliestAllowArrivalTimeDelivery ? earliestAllowArrivalTimeDelivery
				: arrivalTimeDelivery;
		int departureTimeDelivery = serviceTimeDelivery
				+ (int) (deliveryDurationPerUnit * max_cap * factor / amount)
				+ fix_unload_time;

		RouteNode start = new RouteNode(pickupLocationIdx, arrivalTimePickup,
				departureTimePickup, L, sel_vehicle, "P");// pickup
		RouteNode end = new RouteNode(deliveryLocationIdx, arrivalTimeDelivery,
				departureTimeDelivery, L, sel_vehicle, "D");// delivery

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

			System.out
					.println("processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
							+ a.toString()
							+ ", remain amount = "
							+ item.getWeight());
			Item[] ci = new Item[1];
			ci[0] = item.clone();
			ci[0].setWeight(cap);
			ci[0].setPickupDuration(ftl_pickup_duration);
			ci[0].setDeliveryDuration(ftl_delivery_duration);

			mTrip2Items.put(a, ci);
			trips[a.start.vehicleIndex].add(a);
		}

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

			System.out
					.println("processSplitOrderItemWithInternalVehicle -> SPLIT new trip "
							+ a.toString()
							+ ", remain amount = "
							+ item.getWeight());
			Item[] ci = new Item[1];
			ci[0] = item.clone();
			ci[0].setWeight(cap);
			ci[0].setPickupDuration(ftl_pickup_duration);
			ci[0].setDeliveryDuration(ftl_delivery_duration);

			mTrip2Items.put(a, ci);
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
						- vehicles.length].getWeight();

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

	public ArrayList<Item> loadFTLToVehicle(int vehicle_index,
			int pickuplocationIndex, int deliveryLocationIndex,
			ArrayList<Item> decreasing_weight_items, int fix_load_time,
			int fix_unload_time) {
		Vehicle vh = getVehicle(vehicle_index);

		String debug_vehicle_code = "60C-242.61";

		String deliveryLocationCode = locationCodes.get(deliveryLocationIndex);

		ArrayList<Item> storeL = new ArrayList<Item>();
		for (int i = 0; i < decreasing_weight_items.size(); i++)
			storeL.add(decreasing_weight_items.get(i));

		if (debug_vehicle_code.equals(vh.getCode()))
			System.out.println("loadFTLToVehicle(vehicle_index = "
					+ vehicle_index + ", code = " + vh.getCode() + ", cap = "
					+ vh.getWeight() + "), decreasing_items = ");
		if (log != null)
			log.println("loadFTLToVehicle(vehicle_index = " + vehicle_index
					+ "), decreasing_items = ");
		if (debug_vehicle_code.equals(vh.getCode()))

			for (int i = 0; i < decreasing_weight_items.size(); i++) {
				System.out.print("[" + decreasing_weight_items.get(i).getCode()
						+ "," + decreasing_weight_items.get(i).getWeight()
						+ "] ");

				if (log != null)
					log.print("[" + decreasing_weight_items.get(i).getCode()
							+ "," + decreasing_weight_items.get(i).getWeight()
							+ "] ");
			}

		// if(deliveryLocationCode.equals("60005447"))
		System.out.println();

		if (log != null)
			log.println();

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
		int end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
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
				if (arrival_depot > end_work_time_vehicle)
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

		if (IA.size() > 0) {
			if (decreasing_weight_items.size() > 0) {
				RouteNode start = new RouteNode(pickuplocationIndex,
						(int) arrival_pickup, (int) departure_pickup, IA,
						vehicle_index, "P");
				RouteNode end = new RouteNode(deliveryLocationIndex,
						(int) arrival_delivery, (int) departure_delivery, IA,
						vehicle_index, "D");
				Trip t = new Trip(start, end, "COLLECT_TO_FTL");
				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(t, items_of_trip);

				trips[vehicle_index].add(t);
				// System.out.println("CONSIDER OK********************");
				if (log != null)
					log.println("CONSIDER OK********************");
				return collectItems;
			} else {
				if (debug_vehicle_code.equals(vh.getCode())) {
					System.out.println("LOAD ALL items to vehicle "
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

	public boolean canLoadAllFTLToVehicle(int vehicle_index,
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
		if (log != null)
			log.println("loadFTLToVehicle(vehicle_index = " + vehicle_index
					+ "), decreasing_items = ");
		// if(deliveryLocationCode.equals("60005447"))
		for (int i = 0; i < decreasing_weight_items.size(); i++) {
			System.out.print("[" + decreasing_weight_items.get(i).getCode()
					+ "," + decreasing_weight_items.get(i).getWeight() + "] ");

			if (log != null)
				log.print("[" + decreasing_weight_items.get(i).getCode() + ","
						+ decreasing_weight_items.get(i).getWeight() + "] ");
		}

		// if(deliveryLocationCode.equals("60005447"))
		System.out.println();

		if (log != null)
			log.println();

		ArrayList<Item> collectItems = new ArrayList<Item>();

		Vehicle vh = getVehicle(vehicle_index);
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
		int end_work_time_vehicle = (int) DateTimeUtils.dateTime2Int(vh
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
				if (arrival_depot > end_work_time_vehicle)
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

	public int[] sortDecreaseVehicleIndex(Vehicle[] v) {
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

	public void processMergeOrderItemsFTL(String pickuplocationCode,
			String deliveryLocationCode, HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode

		String debug_delivery_location_code = "10000704";

		int pi = mLocationCode2Index.get(pickuplocationCode);
		int di = mLocationCode2Index.get(deliveryLocationCode);
		ArrayList<Item> a_items = new ArrayList<Item>();
		for (int i : RI) {
			for (int j = 0; j < requests[i].getItems().length; j++)
				a_items.add(requests[i].getItems()[j]);
		}
		a_items = sortDecreasing(a_items);

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

		int k = 0;
		while (k < vehicle_index.length) {
			if (a_items.size() == 0)
				break;

			if (deliveryLocationCode.equals(debug_delivery_location_code)
					|| debug_delivery_location_code == null) {
				double W = 0;
				for (Item I : a_items)
					W += I.getWeight();

				System.out.print("prepare LoadFTL for vehicle "
						+ getVehicle(vehicle_index[k]).getCode() + ", cap = "
						+ getVehicle(vehicle_index[k]).getWeight()
						+ " a_items = " + a_items.size() + ", W = " + W
						+ ", items = ");
				for (int jj = 0; jj < a_items.size(); jj++) {
					System.out.print(a_items.get(jj).getWeight() + ", ");
				}
				System.out.println();

			}
			// find if a vehicle can load ALL
			for (int k1 = 0; k1 < vehicle_index.length; k1++) {
				boolean ok = canLoadAllFTLToVehicle(vehicle_index[k1], pi, di,
						a_items, fix_load_time, fix_unload_time);
				if (ok) {// return if find ONE, try to optimize this later
					if (deliveryLocationCode
							.equals(debug_delivery_location_code)
							|| debug_delivery_location_code == null)
						System.out
								.println("Can load all order to a vehicle --> RETURN");
					return;
				}
			}

			if (log != null)
				log.println("prepare LoadFTL for vehicle "
						+ getVehicle(vehicle_index[k]).getCode()
						+ ", a_items = " + a_items.size());

			ArrayList<Item> collectItems = loadFTLToVehicle(vehicle_index[k],
					pi, di, a_items, fix_load_time, fix_unload_time);

			if (collectItems.size() == 0) {
				k++;
				// System.out
				// .println("FAILED -> TRY LoadFTL for new vehicle, a_items = "
				// + a_items.size());
			} else {
				if (deliveryLocationCode.equals(debug_delivery_location_code)
						|| debug_delivery_location_code == null) {
					System.out.print("LoadFTL success for vehicle "
							+ getVehicle(vehicle_index[k]).getCode()
							+ ", collectItems = " + collectItems.size() + ": ");
					for (int jj = 0; jj < collectItems.size(); jj++) {
						System.out.print(collectItems.get(jj).getWeight()
								+ ", ");
					}
					System.out.println();
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

	public void processDistinctPickupDeliveryLocationCodes() {
		mLocationCode2RequestIndex = new HashMap<String, HashSet<Integer>>();
		distinct_pickupLocationCodes = new ArrayList<String>();
		distinct_deliveryLocationCodes = new ArrayList<String>();
		distinct_request_indices = new ArrayList<HashSet<Integer>>();

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
				distinct_request_indices.get(idx).add(i);
				// System.out.println("processMergeOrderItemsFTL, add request "
				// + i + " to " + distinct_pickupLocationCodes.get(idx)
				// + "--" + distinct_deliveryLocationCodes.get(idx)
				// + ", sz = " + distinct_request_indices.get(idx).size());
			} else {
				distinct_pickupLocationCodes.add(r.getPickupLocationCode());
				distinct_deliveryLocationCodes.add(r.getDeliveryLocationCode());
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
		System.out.println("processMergeOrderItemsFTL, requests.sz = "
				+ requests.length + ", distinct.sz = "
				+ distinct_pickupLocationCodes.size());

	}

	public void processMergeOrderItemsFTL() {
		initializeLog();
		processDistinctPickupDeliveryLocationCodes();

		for (int i = 0; i < distinct_deliveryLocationCodes.size(); i++) {
			System.out.println("processMergeOrderItemsFTL("
					+ distinct_pickupLocationCodes.get(i) + " --> "
					+ distinct_deliveryLocationCodes.get(i) + ", RI = "
					+ distinct_request_indices.get(i).size());
			for (int j : distinct_request_indices.get(i)) {
				System.out.print(j + ",");
			}
			System.out.println();
			for (int j : distinct_request_indices.get(i)) {
				System.out.print(j + ": ");
				for (int jj = 0; jj < requests[j].getItems().length; jj++)
					System.out.print("[Order" + requests[j].getOrderID()
							+ ", Item" + requests[j].getItems()[jj].getCode()
							+ "," + requests[j].getItems()[jj].getWeight()
							+ "] ");
				System.out.println();
			}

			processMergeOrderItemsFTL(distinct_pickupLocationCodes.get(i),
					distinct_deliveryLocationCodes.get(i),
					distinct_request_indices.get(i));
		}
		// finalizeLog();
	}

	public void processSplitOrders() {
		trips = new ArrayList[vehicles.length + externalVehicles.length];
		for (int i = 0; i < vehicles.length + externalVehicles.length; i++)
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

		for (int i = 0; i < sorted_items.length; i++) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithInternalVehicle(I);
			}
		}

		for (int i = 0; i < sorted_items.length; i++) {
			Item I = sorted_items[i];
			int item_index = mItem2Index.get(I);
			int req_index = mItemIndex2RequestIndex.get(item_index);
			PickupDeliveryRequest r = requests[req_index];
			if (r.getSplitDelivery().equals("Y")) {
				processSplitOrderItemWithExternalVehicle(I);
			}
		}

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

		items = new ArrayList<Item>();
		mItem2Index = new HashMap<Item, Integer>();
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
		for (int i = 0; i < requests.length; i++) {
			s_locationCodes.add(requests[i].getPickupLocationCode());
			s_locationCodes.add(requests[i].getDeliveryLocationCode());
		}
		for (int i = 0; i < vehicles.length; i++) {
			s_locationCodes.add(vehicles[i].getStartLocationCode());
			s_locationCodes.add(vehicles[i].getEndLocationCode());
			mVehicle2OriginStartWoringTime.put(i,
					vehicles[i].getStartWorkingTime());
		}
		for (int i = 0; i < externalVehicles.length; i++) {
			s_locationCodes.add(externalVehicles[i].getStartLocationCode());
			s_locationCodes.add(externalVehicles[i].getEndLocationCode());
			mVehicle2OriginStartWoringTime.put(i + vehicles.length,
					externalVehicles[i].getStartWorkingTime());
		}

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
		for (int i = 0; i < travelTimes.length; i++) {
			int from = mLocationCode2Index.get(travelTimes[i].getSrcCode());
			int to = mLocationCode2Index.get(travelTimes[i].getDestCode());
			a_travelTime[from][to] = travelTimes[i].getDistance();
		}

		for (int i = 0; i < requests.length; i++) {
			for (int j = 0; j < requests[i].getItems().length; j++) {
				mItemCode2OrderID.put(requests[i].getItems()[j].getCode(),
						requests[i].getOrderID());
			}
		}
	}

	protected void initPoints() {
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
		mVehicle2NotReachedLocations = new HashMap<String, HashSet<String>>();
		mTrip2PickupPointIndices = new HashMap<Trip, ArrayList<Integer>>();

		M = vehicles.length + externalVehicles.length;
		int idxPoint = -1;
		// create points from init FTL trips
		for (int k = 0; k < M; k++) {
			if (trips[k].size() > 0) {
				for (int j = 0; j < trips[k].size(); j++) {
					Trip t = trips[k].get(j);
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

							int reqIdx = mItemIndex2RequestIndex
									.get(ta.itemIndex);

							PickupDeliveryRequest r = requests[reqIdx];
							Point pickup = new Point(idxPoint);
							pickupPoints.add(pickup);
							L.add(pickupPoints.size() - 1);

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
					} else if (t.type.equals("COLLECT_TO_FTL")) {
						ArrayList<Integer> L = new ArrayList<Integer>();
						for (int i = 0; i < t.start.items.size(); i++) {
							// pickup
							idxPoint++;
							ItemAmount ta = t.start.items.get(i);
							int itemIndex = ta.itemIndex;
							Item I = items.get(itemIndex);

							int reqIdx = mItemIndex2RequestIndex
									.get(ta.itemIndex);

							PickupDeliveryRequest r = requests[reqIdx];
							Point pickup = new Point(idxPoint);
							pickupPoints.add(pickup);
							L.add(pickupPoints.size() - 1);// add pickup point
															// index to L

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

			idxPoint++;
			Point pickup = new Point(idxPoint);
			pickupPoints.add(pickup);

			// mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
			// mPickupPoint2RequestIndex.put(pickup, i);

			mPoint2Index.put(pickup, idxPoint);
			// mPoint2LocationCode.put(pickup,
			// requests[i].getPickupLocationCode());
			mPoint2LocationCode.put(pickup, pickupLocationCode);

			double demand = 0;
			int pickupDuration = 0;
			int deliveryDuration = 0;
			for (int i : distinct_request_indices.get(I)) {
				for (int j = 0; j < requests[i].getItems().length; j++) {
					demand = demand + requests[i].getItems()[j].getWeight();
					deliveryDuration = deliveryDuration
							+ requests[i].getItems()[j].getDeliveryDuration();
					pickupDuration = pickupDuration
							+ requests[i].getItems()[j].getPickupDuration();
				}
			}

			for (int i : distinct_request_indices.get(I)) {
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
			for (int i : distinct_request_indices.get(I)) {
				sz += requests[i].getItems().length;
			}
			Integer[] L = new Integer[sz];

			int idx = -1;
			for (int i : distinct_request_indices.get(I)) {
				for (int ii = 0; ii < requests[i].getItems().length; ii++) {
					Item item = requests[i].getItems()[ii];
					idx++;
					L[idx] = mItem2Index.get(item);
				}
			}
			mPoint2IndexItems.put(pickup, L);

			for (int i : distinct_request_indices.get(I)) {

				for (int j = 0; j < requests[i].getItems().length; j++)
					mItem2ExclusiveItems.put(
							requests[i].getItems()[j].getCode(),
							new HashSet<String>());
			}

			// delivery
			idxPoint++;
			Point delivery = new Point(idxPoint);
			deliveryPoints.add(delivery);
			mPoint2Index.put(delivery, idxPoint);
			// mPoint2LocationCode.put(delivery,requests[i].getDeliveryLocationCode());
			mPoint2LocationCode.put(delivery, deliveryLocationCode);
			mPoint2Demand.put(delivery, -demand);
			// mPoint2Request.put(delivery, requests[i]);

			for (int i : distinct_request_indices.get(I)) {
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

			for (int i : distinct_request_indices.get(I)) {
				if (earliestAllowedArrivalTimePickup < (int) DateTimeUtils
						.dateTime2Int(requests[i].getEarlyPickupTime()))
					earliestAllowedArrivalTimePickup = (int) DateTimeUtils
							.dateTime2Int(requests[i].getEarlyPickupTime());

				if (earliestAllowedArrivalTimeDelivery < (int) DateTimeUtils
						.dateTime2Int(requests[i].getEarlyDeliveryTime()))
					earliestAllowedArrivalTimeDelivery = (int) DateTimeUtils
							.dateTime2Int(requests[i].getEarlyDeliveryTime());

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

		/*
		 * // create points from requests for (int i = 0; i < requests.length;
		 * i++) { // mRequest2PointIndices.put(i, new HashSet<Integer>());
		 * 
		 * if (requests[i].getSplitDelivery() != null &&
		 * requests[i].getSplitDelivery().equals("Y")) { for (int j = 0; j <
		 * requests[i].getItems().length; j++) { Item I =
		 * requests[i].getItems()[j]; if (I.getWeight() <= 0) continue;
		 * 
		 * idxPoint++; Point pickup = new Point(idxPoint);
		 * pickupPoints.add(pickup); //
		 * mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
		 * mPickupPoint2RequestIndex.put(pickup, i);
		 * 
		 * mPoint2Index.put(pickup, idxPoint); mPoint2LocationCode.put(pickup,
		 * requests[i].getPickupLocationCode()); mPoint2Demand.put(pickup,
		 * I.getWeight()); mPoint2Request.put(pickup, requests[i]);
		 * mPoint2Type.put(pickup, "P"); mPoint2PossibleVehicles.put(pickup, new
		 * HashSet<Integer>()); Integer[] ite = new Integer[1]; ite[0] =
		 * mItem2Index.get(I);
		 * 
		 * mPoint2IndexItems.put(pickup, ite);
		 * 
		 * mItem2ExclusiveItems.put( requests[i].getItems()[j].getCode(), new
		 * HashSet<String>());
		 * 
		 * // delivery idxPoint++; Point delivery = new Point(idxPoint);
		 * deliveryPoints.add(delivery); mPoint2Index.put(delivery, idxPoint);
		 * mPoint2LocationCode.put(delivery,
		 * requests[i].getDeliveryLocationCode()); mPoint2Demand.put(delivery,
		 * -I.getWeight()); mPoint2Request.put(delivery, requests[i]);
		 * mPoint2Type.put(delivery, "D"); mPoint2PossibleVehicles.put(delivery,
		 * new HashSet<Integer>());
		 * 
		 * pickup2DeliveryOfGood.put(pickup, delivery); allPoints.add(pickup);
		 * allPoints.add(delivery);
		 * 
		 * earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getEarlyPickupTime())); int pickupDuration
		 * = I.getPickupDuration();
		 * 
		 * serviceDuration.put(pickup, pickupDuration);
		 * 
		 * lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLatePickupTime()));
		 * earliestAllowedArrivalTime.put(delivery, (int)
		 * DateTimeUtils.dateTime2Int(requests[i] .getEarlyDeliveryTime())); int
		 * deliveryDuration = I.getDeliveryDuration();
		 * 
		 * serviceDuration.put(delivery, deliveryDuration);
		 * 
		 * lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLateDeliveryTime())); } } else { if
		 * (requests[i].getItems().length == 0) continue;
		 * 
		 * idxPoint++; Point pickup = new Point(idxPoint);
		 * pickupPoints.add(pickup);
		 * 
		 * // mRequest2PointIndices.get(i).add(pickupPoints.size()-1);
		 * mPickupPoint2RequestIndex.put(pickup, i);
		 * 
		 * mPoint2Index.put(pickup, idxPoint); mPoint2LocationCode.put(pickup,
		 * requests[i].getPickupLocationCode()); double demand = 0; int
		 * pickupDuration = 0; int deliveryDuration = 0; for (int j = 0; j <
		 * requests[i].getItems().length; j++) { demand = demand +
		 * requests[i].getItems()[j].getWeight(); deliveryDuration =
		 * deliveryDuration + requests[i].getItems()[j].getDeliveryDuration();
		 * pickupDuration = pickupDuration +
		 * requests[i].getItems()[j].getPickupDuration(); }
		 * 
		 * mPoint2Demand.put(pickup, demand); mPoint2Request.put(pickup,
		 * requests[i]); mPoint2Type.put(pickup, "P");
		 * mPoint2PossibleVehicles.put(pickup, new HashSet<Integer>());
		 * 
		 * Integer[] L = new Integer[requests[i].getItems().length]; for (int ii
		 * = 0; ii < requests[i].getItems().length; ii++) { Item I =
		 * requests[i].getItems()[ii]; L[ii] = mItem2Index.get(I); }
		 * mPoint2IndexItems.put(pickup, L);
		 * 
		 * for (int j = 0; j < requests[i].getItems().length; j++)
		 * mItem2ExclusiveItems.put( requests[i].getItems()[j].getCode(), new
		 * HashSet<String>());
		 * 
		 * // delivery idxPoint++; Point delivery = new Point(idxPoint);
		 * deliveryPoints.add(delivery); mPoint2Index.put(delivery, idxPoint);
		 * mPoint2LocationCode.put(delivery,
		 * requests[i].getDeliveryLocationCode()); mPoint2Demand.put(delivery,
		 * -demand); mPoint2Request.put(delivery, requests[i]);
		 * mPoint2Type.put(delivery, "D"); mPoint2PossibleVehicles.put(delivery,
		 * new HashSet<Integer>()); // mPoint2LoadedItems.put(delivery, new
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
		 * } }
		 */

		// init start-end points for vehicles
		cap = new double[M];
		for (int k = 0; k < M; k++) {
			Vehicle vh = null;
			if (k < vehicles.length)
				vh = vehicles[k];
			else
				vh = externalVehicles[k - vehicles.length];

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

			// System.out.println("mapData, startWorkingTime = " +
			// vh.getStartWorkingTime() + ", end working time = " +
			// vh.getEndWorkingTime());
		}

	}

	protected void initItemVehicleConflicts() {
		itemConflict = new boolean[items.size()][items.size()];
		for (int ii = 0; ii < items.size(); ii++)
			for (int jj = 0; jj < items.size(); jj++)
				itemConflict[ii][jj] = false;

		mItem2ExclusiveItems = new HashMap<String, HashSet<String>>();
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

	}

	protected void initDistanceTravelTime() {
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

	protected void initModel() {
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

	protected void mapData() {
		initPoints();
		initItemVehicleConflicts();
		initDistanceTravelTime();

		initModel();

	}

	protected HashSet<Integer> greedyConstructMaintainConstraintFTL() {
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

		for (int k = 0; k < vehicles.length + externalVehicles.length; k++) {
			if (trips[k].size() > 0) {
				for (int j = 0; j < trips[k].size(); j++) {
					Trip trip = trips[k].get(j);
					int vehicle_index = trip.start.vehicleIndex;
					Point p = XR.prev(XR.endPoint(vehicle_index + 1));
					ArrayList<Integer> pickup_index = mTrip2PickupPointIndices
							.get(trip);

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
						mgr.performAddOnePoint(pickup, p);
						mgr.performAddOnePoint(delivery, pickup);
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
				}
			}
		}
		for (int i = 0; i < pickupPoints.size(); i++)
			if (!scheduled_pickup_point_index.contains(i))
				cand.add(i);

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
					Vehicle vh = null;
					if (k > vehicles.length)
						vh = externalVehicles[k - vehicles.length - 1];
					else
						vh = vehicles[k - 1];
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

			for (int i : cand) {
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				// Item I = items.get(mPoint2IndexItems.get(pickup)[0]);

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

				}

			}
			if (sel_i != -1) {
				mgr.performAddOnePoint(sel_delivery, sel_d);
				mgr.performAddOnePoint(sel_pickup, sel_p);
				propagateArrivalDepartureTime(sel_k, true);

				// log.println("add delivery " + sel_delivery.ID + " after "+
				// sel_d.ID +
				// " AND pickup " + sel_pickup.ID + " after " + sel_p.ID);

				// System.out.println("init addOnePoint(" + sel_pickup.ID + ","
				// + sel_p.ID + "), and (" + sel_delivery.ID + ","
				// + sel_d.ID + ", XR = " + XR.toString() + ", CS = "
				// + CS.violations() + ", cost = " + cost.getValue());

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
		// finalizeLog();
		return cand;
	}

	@Override
	protected HashSet<Integer> search() {
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
		for (int i = 0; i < vehicles.length; i++) {
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
		for (int j = 0; j < externalVehicles.length; j++) {
			Vehicle v = externalVehicles[j];
			int i = j + vehicles.length;
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
		ArrayList<RoutingSolution> newRoutes = new ArrayList<RoutingSolution>();
		for (int i = 0; i < vehicles.length; i++) {
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

		// external vehicles
		for (int j = 0; j < externalVehicles.length; j++) {
			int i = j + vehicles.length;
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
		PickupDeliverySolution sol = buildSolution(scheduled_vehicle,
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

	public int[] reassignOptimizeLoadTruck() {
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
				String vehicleCode = getVehicle(k - 1).getCode();
				for (Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR
						.next(p)) {
					String locationCode = mPoint2LocationCode.get(p);
					if (mVehicle2NotReachedLocations.get(vehicleCode).contains(
							locationCode)) {
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
			System.out.print("Order " + requests[i].getOrderID() + ": ");
			for (int j = 0; j < requests[i].getItems().length; j++) {
				System.out.print(requests[i].getItems()[j].getWeight() + ", ");
			}
			System.out.println();
		}
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

		processMergeOrderItemsFTL();
		System.out.println("AFTER MERGE");
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
		System.out.println("AFTER merge FTL");
		for (int i = 0; i < requests.length; i++) {
			System.out.println("requests[" + i + "] = "
					+ requests[i].getItems().length);
			for (int j = 0; j < requests[i].getItems().length; j++) {
				System.out.println(requests[i].getItems()[j].getCode() + ","
						+ requests[i].getItems()[j].getWeight());
			}
			System.out
					.println("----------------------------------------------");
		}
		disableSplitRequests();
		// updateLocationAndTimeVehicles();

		mapData();

		HashSet<Integer> remainUnScheduled = search();
		int[] scheduled_vehicle = reassignOptimizeLoadTruck();

		// int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		// for(int k = 0; k < XR.getNbRoutes(); k++) scheduled_vehicle[k] = k;

		if (!checkAllSolution()) {
			System.out.println("BUG??????????????????????????????????");
		}

		PickupDeliverySolution sol = buildSolution(scheduled_vehicle,
				remainUnScheduled);
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

}
