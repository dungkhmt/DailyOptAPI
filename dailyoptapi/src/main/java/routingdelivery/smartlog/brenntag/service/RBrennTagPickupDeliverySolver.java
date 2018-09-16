package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;

import com.google.gson.Gson;

import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ClusterItems;
import routingdelivery.smartlog.brenntag.model.GreedyMatchingVehicleTrip;
import routingdelivery.smartlog.brenntag.model.ModelRoute;
import routingdelivery.smartlog.brenntag.model.VehicleTrip;
import routingdelivery.smartlog.brenntag.model.VehicleTripCollection;
import utils.DateTimeUtils;

public class RBrennTagPickupDeliverySolver extends BrenntagPickupDeliverySolver {

	public static int DAYS_MOVE = 100;

	public String name() {
		return "RBrennTagPickupDeliverySolver";
	}

	public void processMergeOrderItemsFTL(String pickuplocationCode,
			String deliveryLocationCode, HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode
		/*
		 * System.out.println(name() + "::processMergeOrderItemsFTL(" +
		 * pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		 * RI.size() + ")"); log(name() + "::processMergeOrderItemsFTL(" +
		 * pickuplocationCode + "," + deliveryLocationCode + ", RI.sz = " +
		 * RI.size() + ")");
		 */

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
			/*
			 * // find if a vehicle can load ALL for (int k1 = 0; k1 <
			 * vehicle_index.length; k1++) { boolean ok =
			 * canLoadAllFTLToVehicle(vehicle_index[k1], pi, di, a_items,
			 * fix_load_time, fix_unload_time); if (ok) {// return if find ONE,
			 * try to optimize this later if (deliveryLocationCode
			 * .equals(debug_delivery_location_code) ||
			 * debug_delivery_location_code == null) System.out .println(name()
			 * + "::processMergeOrderItemsFTL at location " +
			 * deliveryLocationCode +
			 * " -> Can load all order to a vehicle --> RETURN"); return; } }
			 */
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
				Trip sel_trip = null;
				double minW = Integer.MAX_VALUE;
				for (Trip t : T) {
					int vh_idx = t.start.vehicleIndex;
					matchTrips[vh_idx][clusterItems.size() - 1] = t;

					Vehicle vh = getVehicle(vh_idx);
					if (vh.getWeight() < minW) {
						minW = vh.getWeight();
						sel_trip = t;
					}
				}
				trips[sel_trip.start.vehicleIndex].add(sel_trip);
				ArrayList<ItemAmount> IA = sel_trip.start.items;
				Item[] items_of_trip = new Item[IA.size()];
				for (int ii = 0; ii < IA.size(); ii++) {
					items_of_trip[ii] = items.get(IA.get(ii).itemIndex);
				}
				mTrip2Items.put(sel_trip, items_of_trip);

				for (Item I : a_items) {
					removeScheduledItem(I);
				}
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

	public void initEndWorkingTimeOfVehicles() {
		for (int k = 0; k < input.getVehicles().length; k++) {
			Vehicle vh = input.getVehicles()[k];
			if (vh.getEndWorkingTime() == null) {
				vh.setEndWorkingTime(DateTimeUtils.next(
						vh.getStartWorkingTime(), DAYS_MOVE));
			}
		}
		for (int k = 0; k < input.getExternalVehicles().length; k++) {
			Vehicle vh = input.getExternalVehicles()[k];
			if (vh.getEndWorkingTime() == null) {
				vh.setEndWorkingTime(DateTimeUtils.next(
						vh.getStartWorkingTime(), DAYS_MOVE));
			}
		}
		for (int k = 0; k < input.getVehicleCategories().length; k++) {
			Vehicle vh = input.getVehicleCategories()[k];
			if (vh.getEndWorkingTime() == null) {
				vh.setEndWorkingTime(DateTimeUtils.next(
						vh.getStartWorkingTime(), DAYS_MOVE));
			}
		}

	}

	public PickupDeliverySolution computeSequenceRoute(
			BrennTagPickupDeliveryInput input) {
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = new Vehicle[1];
		this.vehicles[0] = input.getVehicles()[0];
		this.externalVehicles = new Vehicle[0];//input.getExternalVehicles();
		this.distances = input.getDistances();
		this.travelTimes = input.getTravelTime();
		
		initEndWorkingTimeOfVehicles();

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

		trips = new ArrayList[vehicles.length + externalVehicles.length];
		for (int i = 0; i < vehicles.length + externalVehicles.length; i++)
			trips[i] = new ArrayList<Trip>();
		
		processMergeOrderItemsFTLNoConstraints();

		//logStateAfterMergingFTLOrder();

		if (log != null) {
			log.println(name()
					+ "::computeVehicleSuggestion, external vehicles = "
					+ input.getExternalVehicles().length
					+ " AFTER MERGE FTL orders, REMAIN WEIGHT = "
					+ getTotalItemWeight());
		}

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

					if (!checkConflictItemsOnTrip(trips[k].get(i))) {
						System.out
								.println(name()
										+ "::computeVehicleSuggestion, RESULT of init Split and Merge FTL FAILED");
					}
				}

			}
		}

		System.out.println("AFTER merge FTL");
		printRequestsOfDistinctLocations();

		mapData();

		HashSet<Integer> remainUnScheduled = searchNoConstraints();

		unScheduledPointIndices = new HashSet<Integer>();
		for (int i : remainUnScheduled)
			unScheduledPointIndices.add(i);

		int[] scheduled_vehicle = {0};
		mapRoute2Vehicles(scheduled_vehicle);
		
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

		boolean solutionAllOK = true;
		
		finalizeLog();
		sol.setErrorMSG("OK");
		if (!solutionAllOK)
			sol.setDescription("KO");
		else
			sol.setDescription("OK");

		return sol;

	}

	public PickupDeliverySolution computeVehicleSuggestion(
			BrennTagPickupDeliveryInput input) {
		this.input = input;

		initEndWorkingTimeOfVehicles();

		initializeLog();
		//if(input.getVehicleCategories() == null){
		//	log(name() + "::computeVehicleSuggestion NULL");
		//}else{
		//	log(name() + "::computeVehicleSuggestion NOT NULL????");
		//}
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
		if(totalVehicleCategoryWeight > 0)
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
				+ ", totalItemWeight = " + totalItemWeights + ", totalVehicleCategoryWeight = " + 
				totalVehicleCategoryWeight + ", ");
		log(name()
				+ "::computeVehicleSuggestion---------------------------------------------------------");

		int lastRemain = -1;
		double lastRemainItemWeight = -1;
		boolean firstTrial = true;
		// boolean firstTrial = false;
		// Gson gson = new Gson();
		// String jsoninput = gson.toJson(input);
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

			System.out.println("BEFORE MERGE FTL");
			printRequestsOfDistinctLocations();

			processMergeOrderItemsFTL();

			logStateAfterMergingFTLOrder();

			int remainItemRequests = computeRemainItemOnRequest();
			log(name()
					+ "::computeVehicleSuggestion, number vehicles = "
					+ (input.getVehicles().length + input.getExternalVehicles().length)
					+ ", remainItemRequests = " + remainItemRequests);
			if (remainItemRequests > 0) {
				continue;
			}

			/*
			 * GreedyMatchingVehicleTrip solver= new
			 * GreedyMatchingVehicleTrip(this); solver.solve(matchTrips,
			 * clusterItems, externalVehicles); ArrayList<Integer> sol_cluster =
			 * solver.getSolutionCluster(); ArrayList<Integer> sol_vehicle =
			 * solver.getSolutionVehicle(); //logMatchTripVehicleCluster();
			 * log(name() +
			 * "::computeVehicleSuggestion, FOUND MATCH VEHICLE-CLUSTER");
			 * for(int k = 0; k < sol_cluster.size(); k++){
			 * log(sol_vehicle.get(k) + " -- " + sol_cluster.get(k)); Vehicle vh
			 * = getVehicle(sol_vehicle.get(k)); ClusterItems ci =
			 * clusterItems.get(sol_cluster.get(k)); log(vh.getCode() +
			 * ", weight " + vh.getWeight() + " -- " + ci.weight); }
			 * 
			 * if(sol_cluster.size() < clusterItems.size()){ continue; }
			 */

			// trips[8].add(matchTrips[8][2]);
			// log(name() + "::computeVehicleSuggestion, LOG-TRIPS");
			// logTripsOfVehicle(8);

			// for(Item I: clusterItems.get(2).items){
			// removeScheduledItem(I);
			// }

			if (log != null) {
				log.println(name()
						+ "::computeVehicleSuggestion, external vehicles = "
						+ input.getExternalVehicles().length
						+ " AFTER MERGE FTL orders, REMAIN WEIGHT = "
						+ getTotalItemWeight());
			}

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
						System.out.println("Vehicle " + vh.getCode()
								+ ", trips " + trips[k].get(i).toString());
						if (log != null)
							log.println("Vehicle " + vh.getCode() + ", trips "
									+ trips[k].get(i).toString());

						if (!checkConflictItemsOnTrip(trips[k].get(i))) {
							System.out
									.println(name()
											+ "::computeVehicleSuggestion, RESULT of init Split and Merge FTL FAILED");
						}
					}

				}
			}

			System.out.println("AFTER merge FTL");
			printRequestsOfDistinctLocations();

			// processSeparateItemsAtEachLocation();

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
			if(totalVehicleCategoryWeight <= EPS) break;// do not have external vehicle -> do not continue LOOP
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
		for(int i = 0; i < vehicles.length + externalVehicles.length; i++){
			Vehicle vh = getVehicle(i);
			if(mVehicle2NotReachedLocations.get(vh.getCode()) == null){
				System.out.println(name() + "::computeVehicleSuggestion, vehicle " + vh.getCode() + " NOT REACHES "
						+ mVehicle2NotReachedLocations.get(vh.getCode()));
			}
		}
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

	public void initPoints() {
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

		mPickupPoint2PickupIndex = new HashMap<Point, Integer>();
		mDeliveryPoint2DeliveryIndex = new HashMap<Point, Integer>();

		M = vehicles.length + externalVehicles.length;
		int idxPoint = -1;
		HashSet<String> IC = new HashSet<String>();

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

		/*
		 * processDistinctPickupDeliveryLocationCodes(); for (int I = 0; I <
		 * distinct_deliveryLocationCodes.size(); I++) { String
		 * pickupLocationCode = distinct_pickupLocationCodes.get(I); String
		 * deliveryLocationCode = distinct_deliveryLocationCodes.get(I);
		 * 
		 * System.out.println("processMergeOrderItemsFTL(" +
		 * distinct_pickupLocationCodes.get(I) + " --> " +
		 * distinct_deliveryLocationCodes.get(I) + ", RI = " +
		 * distinct_request_indices.get(I).size()); for (int i :
		 * distinct_request_indices.get(I)) { System.out.print(i + ","); }
		 * System.out.println(); for (int i : distinct_request_indices.get(I)) {
		 * System.out.print(i + ": "); for (int jj = 0; jj <
		 * requests[i].getItems().length; jj++) System.out.print("[Order" +
		 * requests[i].getOrderID() + ", Item" +
		 * requests[i].getItems()[jj].getCode() + "," +
		 * requests[i].getItems()[jj].getWeight() + "] "); System.out.println();
		 * }
		 * 
		 * ConflictBasedExtractor extractor = new ConflictBasedExtractor(this,
		 * log); ArrayList<HashSet<Integer>> lstNonConflictRequests = extractor
		 * .clusterRequestBasedItemConflict(distinct_request_indices .get(I));
		 * 
		 * for (HashSet<Integer> C : lstNonConflictRequests) {
		 * 
		 * idxPoint++; Point pickup = new Point(idxPoint);
		 * pickupPoints.add(pickup); mPickupPoint2PickupIndex.put(pickup,
		 * pickupPoints.size() - 1);
		 * 
		 * // mRequest2PointIndices.get(i).add(pickupPoints.size()-1); //
		 * mPickupPoint2RequestIndex.put(pickup, i);
		 * 
		 * mPoint2Index.put(pickup, idxPoint); //
		 * mPoint2LocationCode.put(pickup, //
		 * requests[i].getPickupLocationCode()); mPoint2LocationCode.put(pickup,
		 * pickupLocationCode);
		 * 
		 * double demand = 0; int pickupDuration = 0; int deliveryDuration = 0;
		 * // for (int i : distinct_request_indices.get(I)) { for (int i : C) {
		 * for (int j = 0; j < requests[i].getItems().length; j++) { demand =
		 * demand + requests[i].getItems()[j].getWeight(); deliveryDuration =
		 * deliveryDuration + requests[i].getItems()[j] .getDeliveryDuration();
		 * pickupDuration = pickupDuration +
		 * requests[i].getItems()[j].getPickupDuration(); } }
		 * 
		 * // for (int i : distinct_request_indices.get(I)) { for (int i : C) {
		 * if (mPoint2Request.get(pickup) == null) mPoint2Request.put(pickup,
		 * new ArrayList<PickupDeliveryRequest>());
		 * mPoint2Request.get(pickup).add(requests[i]); }
		 * 
		 * mPoint2Demand.put(pickup, demand); // mPoint2Request.put(pickup,
		 * requests[i]);
		 * 
		 * mPoint2Type.put(pickup, "P"); mPoint2PossibleVehicles.put(pickup, new
		 * HashSet<Integer>());
		 * 
		 * int sz = 0; // for (int i : distinct_request_indices.get(I)) { for
		 * (int i : C) { sz += requests[i].getItems().length; } Integer[] L =
		 * new Integer[sz];
		 * 
		 * int idx = -1; // for (int i : distinct_request_indices.get(I)) { for
		 * (int i : C) { for (int ii = 0; ii < requests[i].getItems().length;
		 * ii++) { Item item = requests[i].getItems()[ii];
		 * IC.add(item.getCode()); idx++; L[idx] = mItem2Index.get(item); } }
		 * mPoint2IndexItems.put(pickup, L);
		 * 
		 * // for (int i : distinct_request_indices.get(I)) { for (int i : C) {
		 * for (int j = 0; j < requests[i].getItems().length; j++)
		 * mItem2ExclusiveItems.put( requests[i].getItems()[j].getCode(), new
		 * HashSet<String>()); }
		 * 
		 * // delivery idxPoint++; Point delivery = new Point(idxPoint);
		 * deliveryPoints.add(delivery);
		 * mDeliveryPoint2DeliveryIndex.put(delivery, deliveryPoints.size() -
		 * 1);
		 * 
		 * mPoint2Index.put(delivery, idxPoint); //
		 * mPoint2LocationCode.put(delivery
		 * ,requests[i].getDeliveryLocationCode());
		 * mPoint2LocationCode.put(delivery, deliveryLocationCode);
		 * mPoint2Demand.put(delivery, -demand); // mPoint2Request.put(delivery,
		 * requests[i]);
		 * 
		 * // for (int i : distinct_request_indices.get(I)) { for (int i : C) {
		 * if (mPoint2Request.get(delivery) == null)
		 * mPoint2Request.put(delivery, new ArrayList<PickupDeliveryRequest>());
		 * mPoint2Request.get(delivery).add(requests[i]); }
		 * 
		 * mPoint2Type.put(delivery, "D"); mPoint2PossibleVehicles.put(delivery,
		 * new HashSet<Integer>()); // mPoint2LoadedItems.put(delivery, new
		 * HashSet<String>());
		 * 
		 * pickup2DeliveryOfGood.put(pickup, delivery); allPoints.add(pickup);
		 * allPoints.add(delivery);
		 * 
		 * int earliestAllowedArrivalTimePickup = -1; int
		 * latestAllowedArrivalTimePickup = Integer.MAX_VALUE; int
		 * earliestAllowedArrivalTimeDelivery = -1; int
		 * latestAllowedArrivalTimeDelivery = Integer.MAX_VALUE;
		 * 
		 * // for (int i : distinct_request_indices.get(I)) { for (int i : C) {
		 * if (earliestAllowedArrivalTimePickup < (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getEarlyPickupTime()))
		 * earliestAllowedArrivalTimePickup = (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getEarlyPickupTime());
		 * 
		 * if (earliestAllowedArrivalTimeDelivery < (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getEarlyDeliveryTime()))
		 * earliestAllowedArrivalTimeDelivery = (int) DateTimeUtils
		 * .dateTime2Int(requests[i] .getEarlyDeliveryTime());
		 * 
		 * if (latestAllowedArrivalTimePickup > (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLatePickupTime()))
		 * latestAllowedArrivalTimePickup = (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLatePickupTime());
		 * 
		 * if (latestAllowedArrivalTimeDelivery > (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLateDeliveryTime()))
		 * latestAllowedArrivalTimeDelivery = (int) DateTimeUtils
		 * .dateTime2Int(requests[i].getLateDeliveryTime());
		 * 
		 * } earliestAllowedArrivalTime.put(pickup,
		 * earliestAllowedArrivalTimePickup); serviceDuration.put(pickup,
		 * pickupDuration); lastestAllowedArrivalTime.put(pickup,
		 * latestAllowedArrivalTimePickup);
		 * 
		 * earliestAllowedArrivalTime.put(delivery,
		 * earliestAllowedArrivalTimeDelivery); serviceDuration.put(delivery,
		 * deliveryDuration); lastestAllowedArrivalTime.put(delivery,
		 * latestAllowedArrivalTimeDelivery); } }
		 */
		log(name() + "::initPoint, set of item codes = IC.sz = " + IC.size());

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

	public HashSet<Integer> search() {
		HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraintFTL();
		hillClimbing();
		return remainUnScheduled;
	}

	public HashSet<Integer> searchNoConstraints() {
		HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraintFTLNoConstraints();
		optimizeSequenceNoConstraints();
		
		return remainUnScheduled;
	}
	public void optimizeSequenceNoConstraints(){
		boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
		// xa
		// ve
		// gan
		//System.out.println(name() + "::optimizeSequenceNoConstraints, XR = " + XR.toString());
		for(Point p = XR.startPoint(1); p != XR.endPoint(1); p = XR.next(p)){
			System.out.print(p.ID + mPoint2Type.get(p) + " -> ");
		}
		System.out.println(name() + "::optimizeSequenceNoConstraints, start cost = " + cost.getValue());
		HashMap<String, HashSet<Point>> pointOfLocation = new HashMap<String, HashSet<Point>>();
		for(Point p = XR.next(XR.startPoint(1)); p != XR.endPoint(1); p = XR.next(p)){
			String lc = mPoint2LocationCode.get(p);
			if(pointOfLocation.get(lc) == null){
				pointOfLocation.put(lc, new HashSet<Point>());
			}
			pointOfLocation.get(lc).add(p);
		}
		
		for(int i = 0; i < pickupPoints.size(); i++){
			Point p = pickupPoints.get(i);
			Point d = deliveryPoints.get(i);
			performRemoveOnePoint(XR, p);
			performRemoveOnePoint(XR, d);
		}
		propagate(XR, 1);
		System.out.println(name() + "::optimizeSequenceNoConstraints, after reset, XR = " + XR.toString());
		
		HashSet<Integer> cand = new HashSet<Integer>();
		for(int i = 0; i < pickupPoints.size(); i++){
			cand.add(i);
		}
		
		while(cand.size() > 0){
			int sel_i = -1;
			double minD = Integer.MAX_VALUE;
			for(int i: cand){
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				for(Point p = XR.startPoint(1); p != XR.endPoint(1); p = XR.next(p)){
					for(Point d = p; d != XR.endPoint(1); d = XR.next(d)){
						double e = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
						if(e < minD ){
							minD = e;
							sel_i = i;
						}
					}
				}
			}
			String lc = mPoint2LocationCode.get(pickupPoints.get(sel_i));
			for(Point pickup: pointOfLocation.get(lc)){
				int idx = mPickupPoint2PickupIndex.get(pickup);
				Point delivery = deliveryPoints.get(idx);
				Point sel_p = null;
				Point sel_d = null;
				double min_eval = Integer.MAX_VALUE;
				for(Point p = XR.startPoint(1); p != XR.endPoint(1); p = XR.next(p)){
					for(Point d = p; d != XR.endPoint(1); d = XR.next(d)){
						double e = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
						if(e < min_eval){
							min_eval = e;
							sel_p = p;
							sel_d = d;
						}
					}
				}
				performAddOnePoint(XR, delivery, sel_d);
				performAddOnePoint(XR, pickup, sel_p);
				cand.remove(idx);
				//System.out.println(name() + "::optimizeSequenceNoConstraints, idx = " + idx + ", add " + pickup.ID + ", " + delivery.ID + ", XR = " + XR.toString());
			}
		}
		propagate(XR, 1);
		for(Point p = XR.startPoint(1); p != XR.endPoint(1); p = XR.next(p)){
			System.out.print(p.ID + mPoint2Type.get(p) + " -> ");
		}
		System.out.println(name() + "::optimizeSequenceNoConstraints, finished cost = " + cost.getValue());
		//System.out.println(XR.toString());
	}
	public void hillClimbing() {
		log(name() + "::hillClimbing START XR = " + toStringShort(XR)
				+ ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbing, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbing, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {
			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			printTrips(XR);
			VehicleTrip[] t = new VehicleTrip[trips.size()];
			for (int i = 0; i < t.length; i++) {
				t[i] = trips.get(i);
			}
			// sort
			for (int i = 0; i < t.length; i++) {
				for (int j = i + 1; j < t.length; j++) {
					if (t[i].load > t[j].load) {
						VehicleTrip tmp = t[i];
						t[i] = t[j];
						t[j] = tmp;
					}
				}
			}
			boolean hasMove = false;
			double minDelta = Integer.MAX_VALUE;
			int sel_i = -1;
			int sel_j = -1;
			System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
																				// gan

			for (int i = 0; i < t.length; i++) {
				for (int j = i + 1; j < t.length; j++) {
					// check if t[i] and be moved to t[j] w.r.t exclusiveVehicleLocations
					
					
					// double delta = evaluateMoveTrip(XR, t[i], t[j]);
					double delta = evaluateMoveTrip(XR, t[i], t[j], DIXAVEGAN, true);

					// log(name() + "::hillClimbing, delta(" + i + "," + j +
					// ") = " + delta + ", cost = " + cost.getValue());
					// System.out.println(name() + "::hillClimbing, delta(" + i
					// + "," + j + ") = " + delta + ", cost = " +
					// cost.getValue() +
					// ", XR = " + XR.toStringShort());
					if (delta < 0) {
						if (delta < minDelta) {
							minDelta = delta;
							sel_i = i;
							sel_j = j;
							// log(name() + "::hillClimbing, delta(" + i + "," +
							// j + ") = " + delta +
							// ", UPDATE minDelta = " + minDelta + ", cost = " +
							// cost.getValue());
						}
						/*
						 * hasMove = true; log(name() +
						 * "::hillClimbing, PERFORM HILL CLIMBING, t[" + i +
						 * "] = " + t[i].seqPointString() + ", t[" + j + "] = "
						 * + t[j].seqPointString() + ", START delta = " +
						 * delta); System.out.println(name() +
						 * "::hillClimbing, PERFORM HILL CLIMBING, t[" + i +
						 * "] = " + t[i].seqPointString() + ", t[" + j + "] = "
						 * + t[j].seqPointString() + ", START delta = " +
						 * delta); performMoveTrip(XR, t[i],t[j]);
						 * if(!checkAllSolution(XR)){ log(name() +
						 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
						 * ); }else{ log(name() +
						 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
						 * ); } System.out.println(name() +
						 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = "
						 * + cost.getValue()); break;
						 */
					}
				}
				// if(hasMove) break;
			}
			if (sel_i >= 0) {
				hasMove = true;
				log(name() + "::hillClimbing, PERFORM HILL CLIMBING, t["
						+ sel_i + "] = " + t[sel_i].seqPointString() + ", t["
						+ sel_j + "] = " + t[sel_j].seqPointString()
						+ ", START delta = " + minDelta);
				System.out.println(name()
						+ "::hillClimbing, PERFORM HILL CLIMBING, t[" + sel_i
						+ "] = " + t[sel_i].seqPointString() + ", t[" + sel_j
						+ "] = " + t[sel_j].seqPointString()
						+ ", START delta = " + minDelta);
				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				performMoveTrip(XR, t[sel_i], t[sel_j], DIXAVEGAN);

				if (!checkAllSolution(XR)) {
					log(name()
							+ "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????");
				} else {
					log(name()
							+ "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK");
				}
				System.out.println(name()
						+ "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = "
						+ cost.getValue());

			}
			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbing FINISHED XR = " + toStringShort(XR)
				+ ", START-COST = " + cost.getValue());
		logTrips(XR);
	}

}
