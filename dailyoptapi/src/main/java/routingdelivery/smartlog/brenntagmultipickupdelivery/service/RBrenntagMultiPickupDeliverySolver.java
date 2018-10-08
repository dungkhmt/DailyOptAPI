package routingdelivery.smartlog.brenntagmultipickupdelivery.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.LogRecord;

import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.StatisticInformation;
import routingdelivery.model.Vehicle;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;
import routingdelivery.smartlog.brenntag.model.ClusterItems;
import routingdelivery.smartlog.brenntag.model.ExclusiveVehicleLocation;
import routingdelivery.smartlog.brenntag.model.ModelRoute;
import routingdelivery.smartlog.brenntag.model.PairInt;
import routingdelivery.smartlog.brenntag.model.SolutionCollection;
import routingdelivery.smartlog.brenntag.model.VehicleTrip;
import routingdelivery.smartlog.brenntag.model.VehicleTripCollection;
import routingdelivery.smartlog.brenntag.service.BrennTagRouteSolverForOneVehicle;
import routingdelivery.smartlog.brenntag.service.BrenntagPickupDeliverySolver;
import routingdelivery.smartlog.brenntag.service.ConflictBasedExtractor;
import routingdelivery.smartlog.brenntag.service.ItemAmount;
import routingdelivery.smartlog.brenntag.service.Trip;
import utils.DateTimeUtils;

public class RBrenntagMultiPickupDeliverySolver extends
		BrenntagPickupDeliverySolver {

	public static int DAYS_MOVE = 100;
	public static double THRESHOLD_DELTA_NEGATIVE = -0.1;

	public SolutionCollection solutionCollection;

	public String name() {
		return "RBrenntagMultiPickupDeliverySolver";
	}

	public RBrenntagMultiPickupDeliverySolver() {
		startExecutionTime = System.currentTimeMillis();
		timeLimitExpired = false;

	}

	public PickupDeliverySolution[] collectSolutions() {
		PickupDeliverySolution[] s = new PickupDeliverySolution[solutionCollection
				.size()];
		for (int i = 0; i < s.length; i++)
			s[i] = solutionCollection.get(i);
		return s;
	}

	public void processMergeOrderItemsFTL(String pickuplocationCode,
			String deliveryLocationCode, HashSet<Integer> RI) {
		// pre-schedule FTL for items of requests in RI from pickupLocationCode
		// -> deliveryLocationCode

		System.out.println(name() + "::processMergeOrderItemsFTL("
				+ pickuplocationCode + "," + deliveryLocationCode
				+ ", RI.sz = " + RI.size() + ") NEWNEWNEW");

		log(name() + "::processMergeOrderItemsFTL(" + pickuplocationCode + ","
				+ deliveryLocationCode + ", RI.sz = " + RI.size()
				+ ") RBrenntagMultiPickupDelivery");

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
		int len1 = idx1.length;
		int len2 = 0;
		if (idx2 != null)
			len2 = idx2.length;
		int[] vehicle_index = new int[len1 + len2];
		for (int i = 0; i < len1; i++)
			vehicle_index[i] = idx1[i];

		for (int i = 0; i < len2; i++)
			vehicle_index[i + len1] = idx2[i];

		int nbVehicles = vehicle_index.length;
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
				Vehicle vh = getVehicle(vehicle_index[k1]);
				if (t != null) {
					T.add(t);

					log(name()
							+ "::processMergeOrderItemsFTL, FOUND FTL trip for vehicle "
							+ vh.getCode());
				}
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
					// System.out.println(name()
					// + "::processMergeOrderItemsFTL, T.sz = " + T.size() +
					// ", vehicle weight = " + vh.getWeight()
					// + ", MAX_INT = " + minW);

					if (vh.getWeight() / 1000 < minW) {
						minW = vh.getWeight() / 1000;
						sel_trip = t;
					}
				}
				// System.out.println(name()
				// + "::processMergeOrderItemsFTL, T.sz = " + T.size());

				// System.out.println(name()
				// + "::processMergeOrderItemsFTL, vehicle = " +
				// sel_trip.start.vehicleIndex);

				Vehicle vh = getVehicle(sel_trip.start.vehicleIndex);
				trips[sel_trip.start.vehicleIndex].add(sel_trip);
				log(name() + "::processMergeOrderItemsFTL, ADD-TRIP trips["
						+ vh.getCode() + "].sz = "
						+ trips[sel_trip.start.vehicleIndex].size());
				System.out.println(name()
						+ "::processMergeOrderItemsFTL, ADD-TRIP trips["
						+ vh.getCode() + "].sz = "
						+ trips[sel_trip.start.vehicleIndex].size());
				System.out
						.println(name()
								+ "::processMergeOrderItemsFTL, ADD-TRIP vehicle of trip = "
								+ sel_trip.getVh().getCode());

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
				// System.out.println(name()
				// + "::processMergeOrderItemsFTL, vehicle " + k + "/" +
				// nbVehicles + " -> loadFTLToVehicle FAILED");
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
		if (input.getVehicles() != null)
			for (int k = 0; k < input.getVehicles().length; k++) {
				Vehicle vh = input.getVehicles()[k];
				if (vh.getEndWorkingTime() == null) {
					vh.setEndWorkingTime(DateTimeUtils.next(
							vh.getStartWorkingTime(), DAYS_MOVE));
				}
			}
		if (input.getExternalVehicles() != null)
			for (int k = 0; k < input.getExternalVehicles().length; k++) {
				Vehicle vh = input.getExternalVehicles()[k];
				if (vh.getEndWorkingTime() == null) {
					vh.setEndWorkingTime(DateTimeUtils.next(
							vh.getStartWorkingTime(), DAYS_MOVE));
				}
			}
		if (input.getVehicleCategories() != null)
			for (int k = 0; k < input.getVehicleCategories().length; k++) {
				Vehicle vh = input.getVehicleCategories()[k];
				if (vh.getEndWorkingTime() == null) {
					vh.setEndWorkingTime(DateTimeUtils.next(
							vh.getStartWorkingTime(), DAYS_MOVE));
				}
			}

	}

	public PickupDeliverySolution computeVehicleSuggestion(
			BrennTagPickupDeliveryInput input) {
		this.input = input;
		// startExecutionTime = System.currentTimeMillis();
		// timeLimitExpired = false;

		initEndWorkingTimeOfVehicles();

		initializeLog();

		if (CHECK_AND_LOG) {
			String vehicleCode = "60C-424.90";
			String locationCode = "10008674";
			if (input.findVehicleLocationConflict(vehicleCode, locationCode)) {
				log(name()
						+ "::computeVehicleSuggestion -> setting exclusiveVehicleLocation contains  "
						+ vehicleCode + " - " + locationCode);
			} else {
				log(name()
						+ "::computeVehicleSuggestion -> setting exclusiveVehicleLocation NOT contains 60C-433.24, 10010805");
			}
		}

		if (CHECK_AND_LOG) {
			String rs = input.analyzeDistanceAndTravelTime();
			log(name()
					+ "::computeVehicleSuggestion, analyze Distance and TravelTime GOT "
					+ rs);
		}

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
		if (totalVehicleCategoryWeight > EPS) {
			while (totalCapacity + rep * totalVehicleCategoryWeight < totalItemWeights) {
				rep++;
			}
			if (rep == 0)
				rep = 1;
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
		solutionCollection = new SolutionCollection(10);
		while (true) {
			System.out.println(name()
					+ "::computeVehicleSuggestion, while(true)...");

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
				// System.out.println("Order " + requests[i].getOrderID());
				double W = 0;
				for (int j = 0; j < requests[i].getItems().length; j++) {
					W += requests[i].getItems()[j].getWeight();
				}
				// System.out.println("W = " + W);
			}

			initMapData();
			initItemVehicleConflicts();
			// initDistanceTravelTime();

			String inputOK = analyzeInputConsistency();
			if (!inputOK.equals("OK")) {
				PickupDeliverySolution sol = new PickupDeliverySolution();
				// sol.setDescription(inputOK);
				sol.setErrorMSG(inputOK);
				sol.setDescription("OK");
				System.out
						.println(name()
								+ "::computeVehicleSuggestion, input NOT CONSISTENT???");
				return sol;
			}

			processSplitOrders();
			processDistinctPickupDeliveryLocationCodes();

			// logStateAfterSplittingOrder();

			if (log != null) {
				log.println(name()
						+ "::computeVehicleSuggestion, external vehicles = "
						// + input.getExternalVehicles().length
						+ " AFTER SPLIT orders, REMAIN WEIGHT = "
						+ getTotalItemWeight());
			}

			// System.out.println("BEFORE MERGE FTL");
			// printRequestsOfDistinctLocations();

			processMergeOrderItemsFTL();
			// logTripsAfterMergingOrderAtLocations();
			logStateAfterMergingFTLOrder();

			int remainItemRequests = computeRemainItemOnRequest();
			log(name() + "::computeVehicleSuggestion"
					+ ", after mergeOrderItemsFTL -> remainItemRequests = "
					+ remainItemRequests);
			System.out.println(name() + "::computeVehicleSuggestion"
					+ ", after mergeOrderItemsFTL -> remainItemRequests = "
					+ remainItemRequests);

			if (remainItemRequests > 0) {
				// if(totalVehicleCategoryWeight < EPS) break;// do not have
				// external vehicle ->not need LOOP
				// continue;
				if (totalVehicleCategoryWeight < EPS) {
					PickupDeliverySolution sol = new PickupDeliverySolution();
					sol.setErrorMSG("Không đủ xe để vận chuyển hết đơn, tổng khả năng vận chuyển của xe nhà = "
							+ totalCapacity
							+ ", tổng khối lượng hàng hoá = "
							+ totalItemWeights);
					sol.setDescription("KO");
					return sol;
				} else {
					System.out
							.println(name()
									+ "::computeVehicleSuggestion"
									+ ", after mergeOrderItemsFTL -> remainItemRequests = "
									+ remainItemRequests + " --> continue");
					continue;
				}
			}

			// if (log != null) {
			// log.println(name()
			// + "::computeVehicleSuggestion, external vehicles = "
			// + input.getExternalVehicles().length
			// + " AFTER MERGE FTL orders, REMAIN WEIGHT = "
			// + getTotalItemWeight());
			// }

			/*
			 * System.out.println("AFTER MERGE FTL"); int len = 0; if
			 * (externalVehicles != null) len = externalVehicles.length; for
			 * (int k = 0; k < vehicles.length + len; k++) { Vehicle vh =
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

			// System.out.println("AFTER merge FTL");

			disableSplitRequests();

			System.out.println(name()
					+ "::computeVehicleSuggestion, START mapData");
			mapData();

			HashSet<Integer> remainUnScheduled = greedyConstructMaintainConstraintFTL();
			if (!checkLoadOnRoute(XR)) {
				log(name()
						+ "::search, BEFORE hillClibing, checkLoadOnRoute Failed, BUG????");
			}

			solutionCollection = new SolutionCollection(10);
			PickupDeliverySolution sol = buildSolution(XR);
			solutionCollection.add(sol, input.getParams());

			// hillClimbing(true);

			if (CHECK_AND_LOG) {
				if (!checkAllSolution(XR)) {
					System.out
							.println(name()
									+ "::computeVehicleSuggestion, checkAllSolution after search FAILED");
				}
			}
			unScheduledPointIndices = new HashSet<Integer>();
			for (int i : remainUnScheduled)
				unScheduledPointIndices.add(i);

			if (log != null) {
				log.println("LOOP, externalVehicles = "
						+ computeExternalVehicles() + ", REMAIN "
						+ remainUnScheduled.size());
			}
			System.out
					.println(name()
							+ "::computeVehicleSuggestion, LOOP, externalVehicles = "
							+ computeExternalVehicles() + ", REMAIN "
							+ remainUnScheduled.size() + ", lastRemain = "
							+ lastRemain);
			if (log != null)
				log.println("LOOP, externalVehicles = "
						+ computeExternalVehicles() + ", REMAIN "
						+ remainUnScheduled.size() + ", lastRemain = "
						+ lastRemain);

			// solutionCollection = new SolutionCollection();

			if (remainUnScheduled.size() == 0)
				break;
			if (remainUnScheduled.size() == lastRemain
					&& Math.abs(lastRemainItemWeight - getTotalItemWeight()) < 0.00001)
				break;
			lastRemain = remainUnScheduled.size();
			lastRemainItemWeight = getTotalItemWeight();

			if (totalVehicleCategoryWeight < EPS)
				break;// do not have external vehicle ->not need LOOP
		}

		logTripsAfterMergingOrderAtLocations();

		inputIndicator = analyzeInput();

		long time1 = System.currentTimeMillis() - startExecutionTime;

		System.out.println("STARTING HillClimbing, time = " + (time1 * 0.001)
				+ ", timeLimit = " + input.getParams().getTimeLimit());

		// if internalVehicleFirst, then rearrange such that all internal
		// vehicles are scheduled

		// if (input.getParams().getInternalVehicleFirst().equals("Y")) {
		// hillClimbingScheduleAllInternalVehicles(true);
		// }

		/*
		 * if (input.getParams().getInternalVehicleFirst().equals("Y")) {
		 * HashSet<Vehicle> unusedInternalVehicles =
		 * getUnusedInternalVehicles(XR); if (unusedInternalVehicles.size() > 0)
		 * { log(name() +
		 * "::computeVehicleSuggestion, after hillClimbingScheduleAllInternalVehicles"
		 * + " -> unusedInternalVehicles.sz = " + unusedInternalVehicles.size()
		 * + " BUG??? "); } }
		 */

		boolean hasChanged = false;
		solutionCollection = new SolutionCollection(10);

		if (CHECK_AND_LOG) {
			if (!checkAllSolution(XR)) {
				log(name()
						+ "::computeVehicleSuggestion, BEFORE hillClimbing FAILED????");
			} else {
				log(name()
						+ "::computeVehicleSuggestion, BEFORE hillClimbing OK");
			}
		}
		log(name() + "::computeVehicleSuggestion, BEFORE hillClimbing");
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			log(name() + "::computeVehicleSuggestion, vehicleRoute[" + k
					+ "] = " + vh.getCode() + ", "
					+ (XR.emptyRoute(k) ? "EMPTY" : "NOT_EMPTY"));
		}

		log(name() + "::AFTER INIT POINT, BEFORE HillClimbing");
		logVehicleRoutes(XR);

		reassignVehiclePrioritizeInternalVehicles(XR);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution solution0 = buildSolution(XR);
		solutionCollection.add(solution0, input.getParams());

		if (CHECK_AND_LOG) {
			if (!checkAllSolution(XR)) {
				log(name()
						+ "::computeVehicleSuggestion, BEFORE WHILE FAILED????");
			} else {
				log(name() + "::computeVehicleSuggestion, BEFORE WHILE OK");
			}
		}

		if (input.getParams().getInternalVehicleFirst().equals("Y")) {
			int iter = 0;
			while (iter <= 2) {

				double time = System.currentTimeMillis() - startExecutionTime;
				if (time > input.getParams().getTimeLimit() * 60 * 1000) {
					System.out
							.println(name()
									+ "::computeVehicleSuggestion + TIME LIMIT EXPIRED, BREAK");
					timeLimitExpired = true;
					break;
				}

				hasChanged = hillClimbingMerge4EachVehicle(true);
				// reassignExternalVehicleOptimizeLoad(XR);
				PickupDeliverySolution solution01 = buildSolution(XR);
				// solutionCollection.add(solution01);

				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMerge4EachVehicle FAILED????");
					} else {
						log(name() + "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMerge4EachVehicle OK");
					}
				}

				log(name()
						+ "::computeVehicleSuggestion, AFTER MERGING 4EachVehicle, INFO-TRIP:");
				logVehicleRoutes(XR);

				boolean ok1 = hillClimbingExchangeTrips2InternalVehicles(true);
				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingExchangeTrips2InternalVehicles FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingExchangeTrips2InternalVehicles OK");
					}
				}

				hasChanged = hasChanged || ok1;

				// reassignExternalVehicleOptimizeLoad(XR);
				PickupDeliverySolution solution011 = buildSolution(XR);
				// solutionCollection.add(solution011);

				log(name()
						+ "::computeVehicleSuggestion, AFTER hillClimbingExchangeTrips2InternalVehicles, INFO-TRIP:");
				logVehicleRoutes(XR);

				boolean ok2 = hillClimbingOptimizeDistanceInternalVehicleTrips(true);
				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingOptimizeDistanceInternalVehicleTrips FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingOptimizeDistanceInternalVehicleTrips OK");
					}
				}
				// System.out.println("FFF");
				// hasChanged = hasChanged || ok2;
				// reassignExternalVehicleOptimizeLoad(XR);
				// PickupDeliverySolution solution02 = buildSolution(XR);
				// solutionCollection.add(solution02);

				boolean ok3 = hillClimbingMoveRequestFromExternalToInternalVehicle(true);
				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMoveRequestFromExternalToInternalVehicle FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMoveRequestFromExternalToInternalVehicle OK");
					}
				}
				hasChanged = hasChanged || ok3;
				reassignExternalVehicleOptimizeLoad(XR);
				PickupDeliverySolution solution03 = buildSolution(XR);
				// solutionCollection.add(solution03);

				log(name()
						+ "::computeVehicleSuggestion, AFTER hillClimbingMoveRequestFromExternalToInternalVehicle, INFO-TRIP:");
				logVehicleRoutes(XR);


				log(name()
						+ "::computeVehicleSuggestion, AFTER hillClimbingMoveRequestFromExternalToInternalVehicle, INFO-TRIP:");
				logVehicleRoutes(XR);
				
				
				boolean ok4 = hillClimbingNewVehicleOptimizeDistanceExternalVehicle(true);
				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingNewVehicleOptimizeDistanceExternalVehicle FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingNewVehicleOptimizeDistanceExternalVehicle OK");
					}
				}
				hasChanged = hasChanged || ok4;

				// boolean ok5 = hillClimbingExchangeRequestPoints2Trips(true);
				// PickupDeliverySolution solution031 = buildSolution(XR);
				// reassignExternalVehicleOptimizeLoad(XR);

				PickupDeliverySolution solution04 = buildSolution(XR);
				reassignVehicleOptimizeLoadExternalVehicles(solution04);
				solutionCollection.add(solution04, input.getParams());

				boolean ok31 = hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance(true);
				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMoveRequestFromExternalToInternalVehicle FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingMoveRequestFromExternalToInternalVehicle OK");
					}
				}
				hasChanged = hasChanged || ok31;
				reassignExternalVehicleOptimizeLoad(XR);
				PickupDeliverySolution solution05 = buildSolution(XR);
				solutionCollection.add(solution05, input.getParams());

				log(name()
						+ "::computeVehicleSuggestion, AFTER hillClimbingNewVehicleOptimizeDistanceExternalVehicle, INFO-TRIP:");
				logVehicleRoutes(XR);

				if (!hasChanged) {
					System.out
							.println(name()
									+ "::computeVehicleSuggestion, CANNOT IMPROVE -> BREAK");
					break;
				}

				if (CHECK_AND_LOG) {
					if (!checkAllSolution(XR)) {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingNewVehicleOptimizeDistanceExternalVehicle endofwhile FAILED????");
					} else {
						log(name()
								+ "::computeVehicleSuggestion, iter = "
								+ iter
								+ ", AFTER hillClimbingNewVehicleOptimizeDistanceExternalVehicle endofwhile OK");
					}
				}

				iter++;
			}

		}

		boolean ok6 = hillClimbing(true);
		logArrivalDepartureTime();
		hasChanged = hasChanged || ok6;
		log(name()
				+ "::computeVehicleSuggestion, BEFORE hillClimbingNewVehicle");
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			log(name() + "::computeVehicleSuggestion, vehicleRoute[" + k
					+ "] = " + vh.getCode() + ", "
					+ (XR.emptyRoute(k) ? "EMPTY" : "NOT_EMPTY"));
		}

		log(name()
				+ "::computeVehicleSuggestion, BEFORE hillClimbingNewVehicle START");
		boolean ok7 = hillClimbingNewVehicle(true);
		hasChanged = hasChanged || ok7;
		log(name()
				+ "::computeVehicleSuggestion, AFTER hillClimbingNewVehicle, logVehicleNotReachLocations");
		// logVehicleNotReachedLocations();

		reassignVehiclePrioritizeInternalVehicles(XR);
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::computeVehicleSuggestion, AFTER-reassignVehiclePrioritizeInternalVehicles, CHECK FAILED??");
		} else {
			log(name()
					+ "::computeVehicleSuggestion, AFTER-reassignVehiclePrioritizeInternalVehicles, CHECK OK");
		}
		reassignExternalVehicleOptimizeLoad(XR);
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::computeVehicleSuggestion, AFTER-reassignExternalVehicleOptimizeLoad, CHECK FAILED??");
		} else {
			log(name()
					+ "::computeVehicleSuggestion, AFTER-reassignExternalVehicleOptimizeLoad, CHECK OK");
		}

		PickupDeliverySolution solution1 = buildSolution(XR);
		reassignVehicleOptimizeLoadExternalVehicles(solution1);
		solutionCollection.add(solution1, input.getParams());

		if (input.getParams().getInternalVehicleFirst().equals("Y")) {
			if (solution1.getStatistic().getNumberInternalTrucks() == input
					.getVehicles().length) {
				// remove first solution becuase the later is better and all
				// internal vehicles is used
				solutionCollection.remove(0);
			}
		}

		PickupDeliverySolution sol = solutionCollection.selectBest(input
				.getParams());

		relaxLoadTimeConstraintAndSolve();

		/*
		 * if (input.getParams().getInternalVehicleFirst().equals("Y")) {
		 * hillClimbingScheduleAllInternalVehicles(true);
		 * reassignExternalVehicleOptimizeLoad(XR); PickupDeliverySolution
		 * solution2 = buildSolution(XR); solutionCollection.add(solution2);
		 * 
		 * }
		 */

		if (CHECK_AND_LOG) {

			if (!checkLoadOnRoute(XR)) {
				log(name()
						+ "::computeVehicleSuggestion, BEFORE reassignOptimizeLoadTruck, checkLoadOnRoute FAILED, BUG???");
			}
		}
		/*
		 * if (input.getParams().getInternalVehicleFirst().equals("Y")) {
		 * HashSet<Vehicle> unusedInternalVehicles =
		 * getUnusedInternalVehicles(XR); if (unusedInternalVehicles.size() > 0)
		 * { String uuv = ""; for(Vehicle v: unusedInternalVehicles) uuv +=
		 * v.getCode() + ", "; log(name() +
		 * "::computeVehicleSuggestion, after hillClimbingNewVehicle" +
		 * " -> unusedInternalVehicles.sz = " + unusedInternalVehicles.size() +
		 * ": " + uuv + ", BUG??? "); } }
		 */

		// int[] scheduled_vehicle = reassignOptimizeLoadTruck();
		// int[] scheduled_vehicle = getScheduleVehicle();
		// int[] scheduled_vehicle = new int[XR.getNbRoutes()];
		// for(int i = 0; i < scheduled_vehicle.length; i++)
		// scheduled_vehicle[i] = i;

		// mapRoute2Vehicles(scheduled_vehicle);
		// analyzeTrips();

		if (CHECK_AND_LOG) {
			if (!checkLoadOnRoute(XR)) {
				log(name()
						+ "::computeVehicleSuggestion, AFTER reassignOptimizeLoadTruck, checkLoadOnRoute FAILED, BUG???");
			}
		}

		VehicleTripCollection VTC_trip = analyzeTrips(XR);
		log(name()
				+ "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS, XR = "
				+ XR.toString());
		for (int i = 0; i < VTC_trip.trips.size(); i++)
			log("TRIP: " + VTC_trip.trips.get(i).seqPointString());
		log("----------------------------------------------------------");
		/*
		 * System.out .println(name() +
		 * "::computeVehicleSuggestion, BEFORE IMPROVE MERGE TRIPS, XR = " +
		 * XR.toString()); for (int i = 0; i < VTC_trip.trips.size(); i++)
		 * System.out.println("TRIP[" + i + "]: " +
		 * VTC_trip.trips.get(i).seqPointString());
		 */

		if (CHECK_AND_LOG) {
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
		}
		System.out
				.println("----------------------------------------------------------");

		// improveMergeTrip();

		if (!checkLoadOnRoute(XR)) {
			log(name()
					+ "::computeVehicleSuggestion, AFTER improveMergeTrip, checkLoadOnRoute, FAILED, BUG???");
		}

		// improveMergeLongTrip();

		VehicleTripCollection VTC = analyzeTrips(XR);
		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();

		for (int i = 0; i < nbIntVehicles + nbExtVehicles; i++) {
			Vehicle vh = getVehicle(i);
			if (mVehicle2NotReachedLocations.get(vh.getCode()) == null) {
				System.out.println(name()
						+ "::computeVehicleSuggestion, vehicle " + vh.getCode()
						+ " NOT REACHES "
						+ mVehicle2NotReachedLocations.get(vh.getCode()));
			}
		}

		// ArrayList<ModelRoute> modelRoutes = extractAndAssignNewVehicle(VTC);

		ArrayList<ModelRoute> modelRoutes = new ArrayList<ModelRoute>();
		/*
		 * if (input.getParams().getIntCity().equals("TRUE")) {
		 * reOrderDeliveryPointsPrioritizeFurthestPoint(XR); for (ModelRoute MR
		 * : modelRoutes) { reOrderDeliveryPointsPrioritizeFurthestPoint(MR.XR);
		 * 
		 * if (!checkLoadOnRoute(MR.XR)) { log(name() +
		 * "::computeVehicleSuggestion, AFETR extractAndAssignNewVehicle checkLoadOnRoute(MR.XR), FAILED, BUG???"
		 * ); }
		 * 
		 * } }
		 */

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

		if (unScheduledPointIndices.size() > 0) {
			solutionAllOK = false;
		}

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

		// rearrangeExploitAllInternalVehicles(XR);

		// reassignVehiclePrioritizeInternalVehicles(XR);

		if (input.getParams().getInternalVehicleFirst().equals("Y")) {
			HashSet<Vehicle> unusedInternalVehicles = getUnusedInternalVehicles(XR);
			if (unusedInternalVehicles.size() > 0) {
				log(name()
						+ "::computeVehicleSuggestion, unusedInternalVehicles.sz = "
						+ unusedInternalVehicles.size() + " BUG??? ");
				for (Vehicle v : unusedInternalVehicles) {
					log(name() + "::computeVehicleSuggestion Un-Used Vehicle "
							+ v.getCode() + ", cap = " + v.getWeight());
				}
				solutionAllOK = false;
			}
		}

		// PickupDeliverySolution sol = buildSolution(XR, scheduled_vehicle,
		// unScheduledPointIndices);

		// PickupDeliverySolution sol = solutionCollection.getLast();

		// PickupDeliverySolution sol = solutionCollection.get(0);
		// if (!input.getParams().getInternalVehicleFirst().equals("Y")) {
		// sol = solutionCollection.getLast();
		// }

		// PickupDeliverySolution sol =
		// solutionCollection.selectBest(input.getParams());

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

		for (int i = 0; i < nbIntVehicles; i++) {
			vehicles[i].setStartWorkingTime(mVehicle2OriginStartWoringTime
					.get(i));
		}
		for (int i = 0; i < nbExtVehicles; i++) {
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

		// reassignVehiclePrioritizeInternalVehicles(sol);
		reassignVehicleOptimizeLoadExternalVehicles(sol);

		if (!sol.checkLoadConstraint())
			solutionAllOK = false;

		finalizeLog();

		double totalDistance = 0;
		int nbInternalTrucks = 0;
		int nbExternalTrucks = 0;

		for (int i = 0; i < sol.getRoutes().length; i++) {
			totalDistance += sol.getRoutes()[i].getDistance();
			RoutingSolution s = sol.getRoutes()[i];
			Vehicle vh = s.getVehicle();
			if (isInternalVehicle(vh))
				nbInternalTrucks++;
			else
				nbExternalTrucks++;
		}
		// StatisticInformation info = new
		// StatisticInformation(totalDistance,sol.getRoutes().length);
		// sol.setStatistic(info);

		sol.setErrorMSG("OK");
		if (!solutionAllOK)
			sol.setDescription("KO");
		else
			sol.setDescription("OK");

		// PickupDeliverySolution sol1 = solutionCollection.get(0);
		// System.out.println(name()
		// + "::computeVehicleSuggestion, statistic: nrRoutes = "
		// + sol1.getStatistic().getNumberTrucks() + ", distance = "
		// + sol1.getStatistic().getTotalDistance());

		long time = System.currentTimeMillis() - startExecutionTime;
		time = time / 1000;
		String hms = DateTimeUtils.second2HMS((int) time);
		sol.getStatistic().setExecutionTime(hms);
		sol.getStatistic().setNumberInternalTrucks(nbInternalTrucks);
		sol.getStatistic().setNumberExternalTrucks(nbExternalTrucks);

		if (timeLimitExpired) {
			sol.getStatistic().setTimeLimitExpired("Y");
		} else {
			sol.getStatistic().setTimeLimitExpired("N");
		}
		return sol;
	}

	public void relaxLoadTimeConstraintAndSolve() {
		log(name() + "::relaxLoadTimeConstraintAndSolve START");
		if (input.getParams().getExtendCapacity() == 0
				&& input.getParams().getExtendLateDelivery() == 0)
			return;
		// input.getParams().setExtendCapacity(200000);
		// input.getParams().setExtendLateDelivery(36000);

		int nbIntVehicles = computeInternalVehicles();
		int nbExtVehicles = computeExternalVehicles();
		for (int i = 0; i < nbIntVehicles; i++) {
			Vehicle vh = vehicles[i];
			double extCap = vh.getWeight()
					* input.getParams().getExtendCapacity() * 1.0 / 100;
			// vh.setWeight(vh.getWeight() +
			// input.getParams().getExtendCapacity());
			vh.setWeight(vh.getWeight() + extCap);
		}
		for (int i = 0; i < nbExtVehicles; i++) {
			Vehicle vh = externalVehicles[i];
			double extCap = vh.getWeight()
					* input.getParams().getExtendCapacity() * 1.0 / 100;
			// vh.setWeight(vh.getWeight() +
			// input.getParams().getExtendCapacity());
			vh.setWeight(vh.getWeight() + extCap);
		}

		if (input.getRequests() != null) {
			for (int i = 0; i < input.getRequests().length; i++) {
				PickupDeliveryRequest r = input.getRequests()[i];
				r.setLatePickupTime(DateTimeUtils.extendDateTime(r
						.getLatePickupTime(), input.getParams()
						.getExtendLateDelivery()));
				r.setLateDeliveryTime(DateTimeUtils.extendDateTime(r
						.getLateDeliveryTime(), input.getParams()
						.getExtendLateDelivery()));
			}
		}
		for (Point p : pickupPoints) {
			ArrayList<PickupDeliveryRequest> lr = mPoint2Request.get(p);
			int newLatestAllowedArrivalTime = Integer.MAX_VALUE;
			for (PickupDeliveryRequest r : lr) {
				int d = (int) DateTimeUtils.dateTime2Int(r.getLatePickupTime());
				if (d < newLatestAllowedArrivalTime)
					newLatestAllowedArrivalTime = d;
			}
			lastestAllowedArrivalTime.put(p, newLatestAllowedArrivalTime);
		}

		for (Point p : deliveryPoints) {
			ArrayList<PickupDeliveryRequest> lr = mPoint2Request.get(p);
			int newLatestAllowedArrivalTime = Integer.MAX_VALUE;
			for (PickupDeliveryRequest r : lr) {
				int d = (int) DateTimeUtils.dateTime2Int(r
						.getLateDeliveryTime());
				if (d < newLatestAllowedArrivalTime)
					newLatestAllowedArrivalTime = d;
			}
			lastestAllowedArrivalTime.put(p, newLatestAllowedArrivalTime);
		}

		boolean ok1 = hillClimbingMoveRequestFromExternalToInternalVehicle(true);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution sol1 = buildSolution(XR);
		// solutionCollection.add(sol1, input.getParams());
		sol1.setDescription(sol1.getDescription() + " EXTEND");

		boolean ok2 = hillClimbingOptimizeDistanceInternalVehicleTrips(true);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution sol2 = buildSolution(XR);
		// solutionCollection.add(sol2, input.getParams());
		sol2.setDescription(sol2.getDescription() + " EXTEND");

		boolean ok21 = hillClimbingNewVehicleOptimizeDistanceExternalVehicle(true);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution sol21 = buildSolution(XR);
		solutionCollection.add(sol21, input.getParams());
		sol21.setDescription(sol21.getDescription() + " EXTEND");

		boolean ok3 = hillClimbing(true);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution sol3 = buildSolution(XR);
		// solutionCollection.add(sol3, input.getParams());
		sol3.setDescription(sol3.getDescription() + " EXTEND");

		boolean ok4 = hillClimbingNewVehicle(true);
		reassignExternalVehicleOptimizeLoad(XR);
		PickupDeliverySolution sol4 = buildSolution(XR);
		solutionCollection.add(sol4, input.getParams());
		sol4.setDescription(sol4.getDescription() + " EXTEND");
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

		M = 0;
		int nbInternalVehicles = 0;
		if (vehicles != null) {
			M += vehicles.length;
			nbInternalVehicles = vehicles.length;
		}
		if (externalVehicles != null)
			M += externalVehicles.length;

		mVehicle2Index = new HashMap<Vehicle, Integer>();
		if (vehicles != null) {
			for (int i = 0; i < vehicles.length; i++) {
				mVehicle2Index.put(vehicles[i], i);
				// System.out.println(name() +
				// "::initPoints, mVehicle2Index.put(" + vehicles[i].getCode() +
				// "," + i);
			}
		}
		if (externalVehicles != null) {
			for (int i = 0; i < externalVehicles.length; i++) {
				mVehicle2Index.put(externalVehicles[i], i + nbInternalVehicles);
				// System.out.println(name() +
				// "::initPoints, mVehicle2Index.put(" +
				// externalVehicles[i].getCode() + "," +
				// mVehicle2Index.get(externalVehicles[i]));
			}
		}
		int idxPoint = -1;
		HashSet<String> IC = new HashSet<String>();
		ArrayList<PairInt> PI = new ArrayList<PairInt>();

		// create points from init FTL trips
		for (int k = 0; k < M; k++) {
			if (trips[k].size() > 0) {
				for (int j = 0; j < trips[k].size(); j++) {
					Trip t = trips[k].get(j);
					if (t.getVh() == null) {
						System.out.println(name() + "::initPoints, trip "
								+ t.toStringShort()
								+ ", DOES NOT MAP VEHICLE, BUG???");
						log(name() + "::initPoints, trip " + t.toStringShort()
								+ ", DOES NOT MAP VEHICLE, BUG???");
					}
					if (mVehicle2Index.get(t.getVh()) == null) {
						System.out.println(name() + "::initPoints, vehicle "
								+ t.getVh().getCode()
								+ ", NOT MAP INDEX, BUG????");
						log(name() + "::initPoints, vehicle "
								+ t.getVh().getCode()
								+ ", NOT MAP INDEX, BUG????");
					}
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

							if (t.type.equals("COLLECT_TO_FTL")) {
								PI.add(new PairInt(idxVH,
										mDeliveryPoint2DeliveryIndex
												.get(delivery)));
							}

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
		if (!checkLoadOnRoute(XR)) {
			log(name()
					+ "::search, BEFORE hillClibing, checkLoadOnRoute Failed, BUG????");
		}

		solutionCollection = new SolutionCollection(10);
		PickupDeliverySolution sol = buildSolution(XR);
		solutionCollection.add(sol, input.getParams());

		hillClimbing(true);// consider load constraint
		// hillClimbing(false);// not consider load constraint, allow violation,
		// find other bigger vehicle
		return remainUnScheduled;
	}

	public void hillClimbingScheduleAllInternalVehicles(boolean loadConstraint) {
		log(name() + "::hillClimbingScheduleAllInternalVehicles START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingScheduleAllInternalVehicles, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingScheduleAllInternalVehicles, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingScheduleAllInternalVehicles + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			int sel_route = -1;// store index of vehicle-route that is not
								// scheduled
			double maxWeight = -1;// find biggest not-scheduled internal truck
			int count = 0;
			String intVS = "";
			String intVES = "";
			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				Point s = XR.startPoint(k);
				Vehicle vh = mPoint2Vehicle.get(s);
				log(name()
						+ "::hillClimbingScheduleAllInternalVehicles, vehicleRoute["
						+ k + "] = " + vh.getCode() + ", "
						+ (XR.emptyRoute(k) ? "EMPTY" : "NOT_EMPTY"));
				if (isInternalVehicle(vh)) {
					if (XR.emptyRoute(k)) {
						if (maxWeight < vh.getWeight()) {
							sel_route = k;
							maxWeight = vh.getWeight();
						}
						intVES = intVS + vh.getCode() + ", ";
						count++;
					}
					intVS = intVS + vh.getCode() + ", ";
				}
			}
			if (sel_route == -1)
				break;

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
			Point sel_pickup = null;
			Point sel_delivery = null;
			Vehicle sel_vi = null;
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan
			Point s = XR.startPoint(sel_route);
			Vehicle vh = mPoint2Vehicle.get(s);

			System.out
					.println(name()
							+ "::hillClimbingScheduleAllInternalVehicles, nbInternal not-scheduled vehicles = "
							+ count + ", sel vehicle = " + vh.getCode()
							+ ", cap = " + vh.getWeight());
			log(name()
					+ "::hillClimbingScheduleAllInternalVehicles, nbInternal not-scheduled vehicles = "
					+ count + ", sel vehicle = " + vh.getCode() + ", cap = "
					+ vh.getWeight() + ", intVehicle = " + intVS
					+ ", internal-not-used-vehicle = " + intVES);

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (isInternalVehicle(vi))
					continue;
				/*
				 * if (input.getParams().getInternalVehicleFirst().equals("Y"))
				 * { if (isInternalVehicle(vi)) if
				 * (mVehicle2Trips.get(vi).size() == 1)// vehicle vi // will not
				 * be // scheduled by // this move continue; }
				 */

				for (Point pickup : t[i].seqPoints) {
					if (mPoint2Type.get(pickup).equals("D"))
						continue;
					// int idx = mPickupPoint2PickupIndex.get(pickup);
					Point delivery = getDeliveryOfPickup(pickup);// deliveryPoints.get(idx);
					// log(name() +
					// "::hillClimbingScheduleAllInternalVehicles, BEFORE evaluateMoveTrip("
					// + vh.getCode() + "," +
					// pickup.ID + "," + delivery.ID + "), cost = " +
					// cost.getValue() + ", t[i] = " + t[i].seqPointString()
					// + ", sel_route = " + XR.toStringRoute(sel_route));
					double delta = evaluateMoveTrip(XR, pickup, delivery, s,
							DIXAVEGAN, loadConstraint);
					// log(name() +
					// "::hillClimbingScheduleAllInternalVehicles, AFTER evaluateMoveTrip("
					// + vh.getCode() + "," + pickup.ID +
					// "," + delivery.ID + "), cost = " + cost.getValue()
					// + ", delta = " + delta + ", t[i] = " +
					// t[i].seqPointString() + ", sel_route = " +
					// XR.toStringRoute(sel_route));
					// double delta = evaluateMoveTrip(XR, t[i], sel_route,
					// DIXAVEGAN,
					// loadConstraint);

					// if (delta < 0) {
					if (delta < minDelta) {
						minDelta = delta;
						sel_i = i;
						sel_pickup = pickup;
						sel_delivery = delivery;
						sel_vi = vi;
					}
					// }
				}
			}
			if (sel_i >= 0) {
				hasMove = true;
				log(name()
						+ "::hillClimbingScheduleAllInternalVehicles, PERFORM HILL CLIMBING, ("
						+ sel_pickup.ID + "," + sel_delivery.ID + ")"
						+ ", START delta = " + minDelta);
				System.out.println(name()
						+ "::hillClimbingScheduleAllInternalVehicles, time = "
						+ (time * 0.001) + ", timeLimit = "
						+ input.getParams().getTimeLimit()
						+ ", PERFORM HILL CLIMBING, (" + sel_pickup.ID + ","
						+ sel_delivery.ID + ")" + ", from external vehicle "
						+ sel_vi.getCode() + " to internal vehicle "
						+ vh.getCode() + ", START delta = " + minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				// performMoveTrip(XR, t[sel_i], sel_route, DIXAVEGAN);
				performMoveTrip(XR, sel_pickup, sel_delivery, s, DIXAVEGAN);
				/*
				 * if (!checkAllSolution(XR)) { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
				 * ); } else { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
				 * ); } System.out.println(name() +
				 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = " +
				 * cost.getValue());
				 */
			}
			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbingScheduleAllInternalVehicles FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);
	}

	public boolean hillClimbingMoveRequestFromExternalToInternalVehicle(
			boolean loadConstraint) {
		System.out
				.println(name()
						+ "::hillClimbingMoveRequestFromExternalToInternalVehicle START XR = "
						+ toStringShort(XR) + ", START-COST = "
						+ cost.getValue());
		boolean hasChanged = false;
		log(name()
				+ "::hillClimbingMoveRequestFromExternalToInternalVehicle START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {
			System.out
					.println(name()
							+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, info = "
							+ shortInfo());
			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicle + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
			ArrayList<Point> sel_lst_pickup = null;
			ArrayList<Point> sel_lst_delivery = null;
			Vehicle sel_vi = null;
			Vehicle sel_vh = null;
			Point sel_s = null;
			VehicleTrip sel_tj = null;
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan
			int nbr = computeNbExternalVehicleRoutes(XR);

			if (nbr == 1) {
				log(name()
						+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, analyze items from external vehicles");
				for (int i = 0; i < t.length; i++) {
					Vehicle vi = t[i].vehicle;
					if (isInternalVehicle(vi))
						continue;

					ArrayList<ArrayList<Point>> L = t[i]
							.computeDeliveryPointsSameLocation();

					log(name()
							+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, vehicle "
							+ vi.getCode() + " has " + L.size()
							+ ", sequence points");
					for (ArrayList<Point> lst_delivery : L) {
						ArrayList<Point> lst_pickup = new ArrayList<Point>();
						for (int ii = lst_delivery.size() - 1; ii >= 0; ii--) {
							Point delivery = lst_delivery.get(ii);
							Point pickup = getPickupOfDelivery(delivery);
							lst_pickup.add(pickup);
						}
						double w = 0;
						for (Point p : lst_pickup)
							w += mPoint2Demand.get(p);
						log(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, sequence sz = "
								+ lst_delivery.size() + " W = " + w);
					}
				}
			}

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (isInternalVehicle(vi))
					continue;

				ArrayList<ArrayList<Point>> L = t[i]
						.computeDeliveryPointsSameLocation();

				/*
				 * log(name() +
				 * "::hillClimbingMoveRequestFromExternalToInternalVehicle, vehicle "
				 * + vi.getCode() + " has " + L.size() + ", sequence points");
				 * for (ArrayList<Point> lst_delivery : L) { ArrayList<Point>
				 * lst_pickup = new ArrayList<Point>(); for (int ii =
				 * lst_delivery.size() - 1; ii >= 0; ii--) { Point delivery =
				 * lst_delivery.get(ii); Point pickup =
				 * getPickupOfDelivery(delivery); lst_pickup.add(pickup); }
				 * double w = 0; for(Point p: lst_pickup) w +=
				 * mPoint2Demand.get(p); log(name() +
				 * "::hillClimbingMoveRequestFromExternalToInternalVehicle, sequence sz = "
				 * + lst_delivery.size() + " W = " + w); }
				 */

				for (int k = 1; k <= XR.getNbRoutes(); k++) {
					Point start = XR.startPoint(k);
					Vehicle vj = mPoint2Vehicle.get(start);
					if (!isInternalVehicle(vj))
						continue;

					for (ArrayList<Point> lst_delivery : L) {
						boolean trial = true;
						for (Point delivery : lst_delivery) {
							String lc = mPoint2LocationCode.get(delivery);
							if (mVehicle2NotReachedLocations.get(vj.getCode())
									.contains(lc)) {
								trial = false;
								break;
							}

							if (vi != vj
									&& fixVehicleDeliveryPoint(vi, delivery)) {
								trial = false;
								break;
							}
						}
						if (!trial)
							continue;

						ArrayList<Point> lst_pickup = new ArrayList<Point>();
						for (int ii = lst_delivery.size() - 1; ii >= 0; ii--) {
							Point delivery = lst_delivery.get(ii);
							Point pickup = getPickupOfDelivery(delivery);
							lst_pickup.add(pickup);
						}

						for (int j = 0; j < VTC.mVehicle2Trips.get(vj).size(); j++) {
							VehicleTrip tj = VTC.mVehicle2Trips.get(vj).get(j);
							Point s = tj.getLastPickupPoint();

							double delta = evaluateMoveSequencePoints(XR,
									lst_pickup, lst_delivery, tj, s, DIXAVEGAN,
									loadConstraint);

							// if (delta < 0) {
							if (delta < minDelta - 1) {
								minDelta = delta;
								sel_lst_pickup = lst_pickup;
								sel_lst_delivery = lst_delivery;
								sel_vi = vi;
								sel_tj = tj;
								sel_s = s;
								sel_vh = vj;
							}

						}

						// evaluate when move lst_pickup, lst_delivery to the
						// begining of a route, creating new VehicleTrip
						double delta = evaluateMoveSequencePoints(XR,
								lst_pickup, lst_delivery, null, start,
								DIXAVEGAN, loadConstraint);

						if (XR.emptyRoute(k)) {
							double w = 0;
							for (Point p : lst_pickup)
								w += mPoint2Demand.get(p);
							log(name()
									+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, w = "
									+ w + ", vehicle vj = " + vj.getCode()
									+ ", cap " + vj.getWeight() + ", delta = "
									+ delta + ", minDelta = " + minDelta);
						}

						// if (delta < 0) {
						if (delta < minDelta - 1) {
							minDelta = delta;
							sel_lst_pickup = lst_pickup;
							sel_lst_delivery = lst_delivery;
							sel_vi = vi;
							sel_tj = null;
							sel_s = start;
							sel_vh = vj;
						}

					}
				}
			}

			if (sel_lst_pickup != null) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, PERFORM HILL CLIMBING, ("
						+ toStringListPoints(sel_lst_pickup) + ","
						+ toStringListPoints(sel_lst_delivery) + ")"
						+ ", START delta = " + minDelta + ", print-XR = "
						+ XR.toStringShort());
				System.out
						.println(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicle, time = "
								+ (time * 0.001) + ", timeLimit = "
								+ input.getParams().getTimeLimit()
								+ ", PERFORM HILL CLIMBING, ("
								+ toStringListPoints(sel_lst_pickup) + ","
								+ toStringListPoints(sel_lst_delivery) + ")"
								+ ", from external vehicle " + sel_vi.getCode()
								+ " to internal vehicle " + sel_vh.getCode()
								+ ", START delta = " + minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				// performMoveTrip(XR, t[sel_i], sel_route, DIXAVEGAN);
				performMoveSequencePoints(XR, sel_lst_pickup, sel_lst_delivery,
						sel_tj, sel_s, DIXAVEGAN, loadConstraint);

			}
			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingMoveRequestFromExternalToInternalVehicle FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);
		return hasChanged;
	}

	public boolean hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance(
			boolean loadConstraint) {
		boolean hasChanged = false;
		log(name()
				+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		HashMap<Vehicle, Integer> mVehicle2Route = new HashMap<Vehicle, Integer>();
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			mVehicle2Route.put(vh, k);
		}
		while (true) {
			System.out
					.println(name()
							+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, info = "
							+ shortInfo());
			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
			double maxLoad = 0;
			int sel_i = -1;
			ArrayList<Point> sel_lst_pickup = null;
			ArrayList<Point> sel_lst_delivery = null;
			Vehicle sel_vi = null;
			Vehicle sel_vh = null;
			Point sel_s = null;
			VehicleTrip sel_tj = null;
			Vehicle sel_vk = null;
			double maxWeightItems = 0;
			int sel_route_index = -1;
			
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan
			int nbr = computeNbExternalVehicleRoutes(XR);

			if (nbr == 1) {
				log(name()
						+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, analyze items from external vehicles");
				for (int i = 0; i < t.length; i++) {
					Vehicle vi = t[i].vehicle;
					if (isInternalVehicle(vi))
						continue;

					ArrayList<ArrayList<Point>> L = t[i]
							.computeDeliveryPointsSameLocation();

					log(name()
							+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, vehicle "
							+ vi.getCode() + " has " + L.size()
							+ ", sequence points");
					for (ArrayList<Point> lst_delivery : L) {
						ArrayList<Point> lst_pickup = createPickupListOfDelivery(lst_delivery);
						double w = 0;
						String sp = "";
						for (Point p : lst_pickup){
							w += mPoint2Demand.get(p);
							sp = sp + "[" + p.ID + ", w = " + mPoint2Demand.get(p) + "], ";
						}
							log(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, sequence sz = "
								+ lst_delivery.size() + " W = " + w + ", items weight = " + sp);
					}
					
					Vehicle[] freeVehicles = getSortedFreeInternalVehicles(XR);
					for(Vehicle vh: freeVehicles){
						ArrayList<Point>  lst_delivery = t[i].getDeliverySeqPoints();
						for(Point p: lst_delivery){
							String lc = mPoint2LocationCode.get(p);
							if(mVehicle2NotReachedLocations.get(vh.getCode()).contains(lc)){
								log(name()
										+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, "
										+ "free-vehicle " + vh.getCode() + ", cannot REACH point " + p.ID);
										
							}else{
								log(name()
										+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, "
										+ "free-vehicle " + vh.getCode() + ", REACH point " + p.ID);
							}
						}
					}
				}
				
				Vehicle[] freeVehicles = getSortedFreeInternalVehicles(XR);
				for(Vehicle vh: freeVehicles){
					log(name()
							+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, free-vehicle " + vh.getCode() + ", cap " + vh.getWeight());
					
				}
				
			}

			// collect list of free vehicles sorted deceasing order of capacity
			Vehicle[] freeVehicles = getSortedFreeInternalVehicles(XR);
			boolean STOP = false;
			
			for (int k = 0; k < freeVehicles.length; k++) {
				if(STOP) break;
				Vehicle vk = freeVehicles[k];
				int r_index = mVehicle2Route.get(vk);

				for (int i = 0; i < t.length; i++) {
					if(STOP) break;
					Vehicle vi = t[i].vehicle;
					if (isInternalVehicle(vi))
						continue;
					ArrayList<ArrayList<Point>> L = t[i]
							.computeDeliveryPointsSameLocation();

					for (ArrayList<Point> lst_delivery : L) {
						if(STOP) break;
						if (lst_delivery.size() == 0)
							continue;
						String lc = mPoint2LocationCode
								.get(lst_delivery.get(0));
						if (mVehicle2NotReachedLocations.get(vk.getCode())
								.contains(lc))
							continue;
						Point[] sDelivery = getSortedDecreasingWeight(lst_delivery);
						for (int j = 0; j < sDelivery.length; j++) {
							if(STOP) break;
							ArrayList<Point> s_lst_delivery = new ArrayList<Point>();
							for (int jj = 0; jj <= j; jj++)
								s_lst_delivery.add(sDelivery[jj]);
							ArrayList<Point> s_lst_pickup = createPickupListOfDelivery(s_lst_delivery);
							double load = computeLoad(s_lst_pickup);

							double[] eval = evaluateMoveSetPointsNewRoute(XR,
									s_lst_pickup, s_lst_delivery,
									r_index, DIXAVEGAN,
									loadConstraint);
							
							if(eval[0] < 0) STOP = true;
							
							double delta = eval[1];
							
							if (delta < Integer.MAX_VALUE - 1) {// do not violate constraint
								if (Math.abs(maxLoad - load) < EPS) {
									if (delta < minDelta) {
										minDelta = delta;
										maxLoad = load;
										sel_lst_pickup = s_lst_pickup;
										sel_lst_delivery = s_lst_delivery;
										sel_s = XR.startPoint(r_index);
										sel_route_index = r_index;
										sel_vk = vk;
										sel_vi = vi;
									}
								} else if (load > maxLoad + EPS) {
									maxLoad = load;
									minDelta = delta;
									sel_lst_pickup = s_lst_pickup;
									sel_lst_delivery = s_lst_delivery;
									sel_s = XR.startPoint(r_index);
									sel_route_index = r_index;
									sel_vk = vk;
									sel_vi = vi;
								}
							}

						}

					}
				}
			}

			if (sel_lst_pickup != null) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, PERFORM HILL CLIMBING, ("
						+ toStringListPoints(sel_lst_pickup) + ","
						+ toStringListPoints(sel_lst_delivery) + ")"
						+ ", START delta = " + minDelta);
				System.out
						.println(name()
								+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance, time = "
								+ (time * 0.001) + ", timeLimit = "
								+ input.getParams().getTimeLimit()
								+ ", PERFORM HILL CLIMBING, ("
								+ toStringListPoints(sel_lst_pickup) + ","
								+ toStringListPoints(sel_lst_delivery) + ")"
								+ ", from external vehicle " + sel_vi.getCode()
								+ " to internal vehicle " + sel_vk.getCode()
								+ ", START delta = " + minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				// performMoveTrip(XR, t[sel_i], sel_route, DIXAVEGAN);
				boolean ok = performMoveSetPointsNewRoute(XR, sel_lst_pickup, sel_lst_delivery,
						sel_route_index, DIXAVEGAN, loadConstraint);
				if(!ok){
					log(name() + "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance"
							+ ", move NOT-OK, BREAK");
					System.out.println(name() + "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance"
							+ ", move NOT-OK, BREAK");
					break;
				}

			}
			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingMoveRequestFromExternalToInternalVehicleSplittingItemsAtSameLocationMinimizeDistance FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		//logTrips(XR);
		return hasChanged;
	}

	public boolean hillClimbingKeepScheduleAllInternalVehicles(
			boolean loadConstraint) {
		boolean hasChanged = false;
		log(name()
				+ "::hillClimbingKeepScheduleAllInternalVehicles START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingKeepScheduleAllInternalVehicles, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingKeepScheduleAllInternalVehicles, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingKeepScheduleAllInternalVehicles + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
			VehicleTrip[] t = new VehicleTrip[trips.size()];
			for (int i = 0; i < t.length; i++) {
				t[i] = trips.get(i);
			}
			// sort
			for (int i = 0; i < t.length; i++) {
				for (int j = i + 1; j < t.length; j++) {
					// if (t[i].load > t[j].load) {
					if (t[i].seqPoints.size() > t[j].seqPoints.size()) {
						VehicleTrip tmp = t[i];
						t[i] = t[j];
						t[j] = tmp;
					}
				}
			}
			boolean hasMove = false;
			double minDelta = Integer.MAX_VALUE;
			int sel_i = -1;
			Point sel_pickup = null;
			Point sel_delivery = null;
			Point sel_s = null;
			Vehicle sel_from_vehicle = null;
			Vehicle sel_to_vehicle = null;
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (isInternalVehicle(vi))
					continue;
				for (int j = 0; j < t.length; j++) {
					Vehicle vj = t[j].vehicle;
					// if (!isInternalVehicle(vj))
					// continue;
					if (vi == vj)
						continue;

					ArrayList<Point> lst_pickups = t[j].getPickupSeqPoints();
					Point s = lst_pickups.get(lst_pickups.size() - 1);
					int sel_route = XR.route(s);

					for (Point pickup : t[i].seqPoints) {
						if (mPoint2Type.get(pickup).equals("D"))
							continue;
						if (mPoint2Demand.get(pickup) + t[j].load > vj
								.getWeight())
							continue;

						// int idx = mPickupPoint2PickupIndex.get(pickup);
						Point delivery = getDeliveryOfPickup(pickup);// deliveryPoints.get(idx);
						log(name()
								+ "::hillClimbingKeepScheduleAllInternalVehicles, BEFORE evaluateMoveTrip("
								+ vi.getCode() + "," + pickup.ID + ","
								+ delivery.ID + "), cost = " + cost.getValue()
								+ ", t[i] = " + t[i].seqPointString()
								+ ", vj = " + vj.getCode() + ", sel_route = "
								+ XR.toStringRoute(sel_route));
						double delta = evaluateMoveTrip(XR, pickup, delivery,
								s, DIXAVEGAN, loadConstraint);
						log(name()
								+ "::hillClimbingKeepScheduleAllInternalVehicles, AFTER evaluateMoveTrip("
								+ vi.getCode() + "," + pickup.ID + ","
								+ delivery.ID + "), cost = " + cost.getValue()
								+ ", delta = " + delta + ", t[i] = "
								+ t[i].seqPointString() + ", vj = "
								+ vj.getCode() + ", sel_route = "
								+ XR.toStringRoute(sel_route));
						// double delta = evaluateMoveTrip(XR, t[i], sel_route,
						// DIXAVEGAN,
						// loadConstraint);

						// if (delta < 0) {
						if (delta < minDelta) {
							minDelta = delta;
							sel_i = i;
							sel_pickup = pickup;
							sel_delivery = delivery;
							sel_s = s;
							sel_from_vehicle = vi;
							sel_to_vehicle = vj;
						}
						// }
					}
				}
			}
			if (sel_i >= 0) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingKeepScheduleAllInternalVehicles, PERFORM HILL CLIMBING, ("
						+ sel_pickup.ID + "," + sel_delivery.ID + ")"
						+ ", START delta = " + minDelta);
				System.out
						.println(name()
								+ "::hillClimbingKeepScheduleAllInternalVehicles, time = "
								+ (time * 0.001) + ", timeLimit = "
								+ input.getParams().getTimeLimit()
								+ ", PERFORM HILL CLIMBING, (" + sel_pickup.ID
								+ "," + sel_delivery.ID + ")"
								+ " from vehicle " + sel_from_vehicle.getCode()
								+ " to vehicle " + sel_to_vehicle.getCode()
								+ ", START delta = " + minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				// performMoveTrip(XR, t[sel_i], sel_route, DIXAVEGAN);
				performMoveTrip(XR, sel_pickup, sel_delivery, sel_s, DIXAVEGAN);
				/*
				 * if (!checkAllSolution(XR)) { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
				 * ); } else { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
				 * ); } System.out.println(name() +
				 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = " +
				 * cost.getValue());
				 */
			}
			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingKeepScheduleAllInternalVehicles FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);
		return hasChanged;
	}

	public boolean hillClimbing(boolean loadConstraint) {
		boolean hasChanged = false;
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

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out.println(name() + "::hillClimbing, time = " + time
						+ " timeLimit = "
						+ (input.getParams().getTimeLimit() * 60 * 1000)
						+ " TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
				Vehicle vi = t[i].vehicle;
				/*
				 * if (input.getParams().getInternalVehicleFirst().equals("Y"))
				 * { if (isInternalVehicle(vi)) if
				 * (mVehicle2Trips.get(vi).size() == 1)// vehicle vi // will not
				 * be // scheduled by // this move continue; }
				 */
				if (fixVehicleTrip(vi, t[i]))
					continue;

				for (int j = i + 1; j < t.length; j++) {
					// double delta = evaluateMoveTrip(XR, t[i], t[j]);
					// t[i] is removed from its vehicle, and is merged with t[j]
					double delta = evaluateMoveTrip(XR, t[i], t[j], DIXAVEGAN,
							loadConstraint);

					// log(name() + "::hillClimbing, delta(" + i + "," + j +
					// ") = " + delta + ", cost = " + cost.getValue());
					// System.out.println(name() + "::hillClimbing, delta(" + i
					// + "," + j + ") = " + delta + ", cost = " +
					// cost.getValue() +
					// ", XR = " + XR.toStringShort());
					if (delta < THRESHOLD_DELTA_NEGATIVE) {
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
				hasChanged = true;
				log(name() + "::hillClimbing, PERFORM HILL CLIMBING, t["
						+ sel_i + "] = " + t[sel_i].seqPointString() + ", t["
						+ sel_j + "] = " + t[sel_j].seqPointString()
						+ ", START delta = " + minDelta);
				System.out.println(name() + "::hillClimbing, time = "
						+ (time * 0.001) + ", timeLimit = "
						+ input.getParams().getTimeLimit()
						+ ", PERFORM HILL CLIMBING, t[" + sel_i + "] = "
						+ t[sel_i].seqPointString() + ", t[" + sel_j + "] = "
						+ t[sel_j].seqPointString() + ", START delta = "
						+ minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				performMoveTrip(XR, t[sel_i], t[sel_j], DIXAVEGAN);
				/*
				 * if (!checkAllSolution(XR)) { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
				 * ); } else { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
				 * ); } System.out.println(name() +
				 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = " +
				 * cost.getValue());
				 */
			}
			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbing FINISHED XR = " + toStringShort(XR)
				+ ", START-COST = " + cost.getValue());
		logTrips(XR);
		return hasChanged;
	}

	public boolean hillClimbingMerge4EachVehicle(boolean loadConstraint) {
		log(name() + "::hillClimbingMerge4EachVehicle START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());

		boolean hasChanged = false;

		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingMerge4EachVehicle, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingMerge4EachVehicle, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingMerge4EachVehicle + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
			VehicleTrip sel_ti = null;
			VehicleTrip sel_tj = null;
			System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan
			String debugCode = "51C-586.32";
			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				if (XR.emptyRoute(k))
					continue;
				Point s = XR.startPoint(k);
				Vehicle vh = mPoint2Vehicle.get(s);
				ArrayList<VehicleTrip> vt = mVehicle2Trips.get(vh);
				for (int i = 0; i < vt.size(); i++) {
					for (int j = i + 1; j < vt.size(); j++) {
						if (conflictTrips(vt.get(i), vt.get(j)))
							continue;

						double[] eval = evaluateMoveTripOneVehicle(XR,
								vt.get(i), vt.get(j), DIXAVEGAN, loadConstraint);
						if (vh.getCode().equals(debugCode)) {
							log(name()
									+ "::hillClimbingMerge4EachVehicle, eval["
									+ i + "," + j + "] = " + eval[0] + ","
									+ eval[1]);
						}
						// if (delta < THRESHOLD_DELTA_NEGATIVE) {
						// if(eval[0] <= 0){// totalCost not increase; HIEN
						// NHIEN cost giam khi merge
						if (eval[0] < Integer.MAX_VALUE && eval[1] < minDelta) {// minimize
																				// cost
																				// of
																				// the
																				// merged
							// trip
							minDelta = eval[1];
							sel_i = i;
							sel_j = j;
							sel_ti = vt.get(i);
							sel_tj = vt.get(j);
						}
						// }
					}
				}
			}

			if (sel_ti != null) {
				hasMove = true;
				log(name()
						+ "::hillClimbingMerge4EachVehicle, PERFORM HILL CLIMBING, t["
						+ sel_i + "] = " + sel_ti.seqPointString() + ", t["
						+ sel_j + "] = " + sel_tj.seqPointString()
						+ ", START delta = " + minDelta);
				System.out.println(name()
						+ "::hillClimbingMerge4EachVehicle, time = "
						+ (time * 0.001) + ", timeLimit = "
						+ input.getParams().getTimeLimit()
						+ ", PERFORM HILL CLIMBING, t[" + sel_i + "] = "
						+ sel_ti.seqPointString() + ", t[" + sel_j + "] = "
						+ sel_tj.seqPointString() + ", START delta = "
						+ minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				if (!checkAllSolution(XR)) {
					log(name()
							+ "::hillClimbingMerge4EachVehicle, JUST BEFORE performMoveTrip FAILED???");
				} else {
					log(name()
							+ "::hillClimbingMerge4EachVehicle, JUST BEFORE performMoveTrip OK");
				}
				// performMoveTrip(XR, sel_ti, sel_tj, DIXAVEGAN);
				performMoveTripOneVehicle(XR, sel_ti, sel_tj, DIXAVEGAN,
						loadConstraint);

				if (!checkAllSolution(XR)) {
					log(name()
							+ "::hillClimbingMerge4EachVehicle, JUST AFTER performMoveTrip FAILED???");
				} else {
					log(name()
							+ "::hillClimbingMerge4EachVehicle, JUST AFTER performMoveTrip OK");
				}

				hasChanged = true;
			}
			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbingMerge4EachVehicle FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);

		return hasChanged;
	}

	public boolean hillClimbingOptimizeDistanceInternalVehicleTrips(
			boolean loadConstraint) {
		boolean hasChanged = false;
		log(name()
				+ "::hillClimbingOptimizeDistanceInternalVehicleTrips START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, before hillClimbing, checkAllSolution OK");
		}
		// logTrips(XR);

		// if(true) return;
		double bestCost = cost.getValue();
		while (true) {
			System.out
					.println(name()
							+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, info = "
							+ shortInfo());

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingOptimizeDistanceInternalVehicleTrips + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
			VehicleTrip[] t = new VehicleTrip[trips.size()];
			boolean vehicleTripCheck = true;
			for (int i = 0; i < t.length; i++) {
				t[i] = trips.get(i);
				if (t[i].seqPoints.size() % 2 == 1) {
					log(name()
							+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, CHUOI?????? "
							+ ", t[i] = " + t[i].seqPointString() + ", XR = "
							+ XR.toStringShort());

					return false;
				} else {
					// log(name() +
					// "::hillClimbingOptimizeDistanceInternalVehicleTrips, NOT-CHUOI, VehicleTrip OK, XR = "
					// + XR.toStringShort());
				}
			}
			// log(name() +
			// "::hillClimbingOptimizeDistanceInternalVehicleTrips, NOT-CHUOI, VehicleTrip OK, XR = "
			// + XR.toStringShort());

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
			VehicleTrip sel_ti = null;
			VehicleTrip sel_tj = null;
			Vehicle sel_from_vehicle = null;
			Vehicle sel_to_vehicle = null;
			ArrayList<Point> sel_lst_pickup = null;
			ArrayList<Point> sel_lst_delivery = null;
			Point sel_s = null;
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (!isInternalVehicle(vi))
					continue;
				for (int j = 0; j < t.length; j++) {
					Vehicle vj = t[j].vehicle;
					if (!isInternalVehicle(vj))
						continue;
					// if(vi == vj) continue;

					ArrayList<Point> lst_pickups = t[j].getPickupSeqPoints();
					Point s = lst_pickups.get(lst_pickups.size() - 1);
					int sel_route = XR.route(s);

					ArrayList<ArrayList<Point>> L = t[i]
							.computeDeliveryPointsSameLocation();
					for (ArrayList<Point> lst_delivery : L) {
						boolean trial = true;
						for (Point delivery : lst_delivery) {
							if (vi != vj
									&& fixVehicleDeliveryPoint(vi, delivery)) {
								trial = false;
								break;
							}
						}
						if (!trial)
							continue;

						ArrayList<Point> lst_pickup = new ArrayList<Point>();
						for (int ii = lst_delivery.size() - 1; ii >= 0; ii--) {
							Point delivery = lst_delivery.get(ii);
							Point pickup = getPickupOfDelivery(delivery);
							lst_pickup.add(pickup);
						}
						// log(name()
						// +
						// "::hillClimbingOptimizeDistanceInternalVehicleTrips, XR = "
						// + XR.toStringShort());
						// log(name()
						// +
						// "::hillClimbingOptimizeDistanceInternalVehicleTrips, evaluateMoveSequencePoints("
						// + toStringListPoints(lst_pickup) + ","
						// + toStringListPoints(lst_delivery) + ", t[j] = " +
						// t[j].seqPointString() + ", s = " + s.ID + ")");
						double old_cost = cost.getValue();
						double delta = evaluateMoveSequencePoints(XR,
								lst_pickup, lst_delivery, t[j], s, DIXAVEGAN,
								loadConstraint);
						if (Math.abs(old_cost - cost.getValue()) > 0.01) {
							log(name()
									+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, BUG???, old_cost = "
									+ old_cost + ", cost = " + cost.getValue());
						}
						if (!checkVehicleTripConsistent(XR)) {
							log(name()
									+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, evaluateMoveSequencePoints("
									+ toStringListPoints(sel_lst_pickup) + ","
									+ toStringListPoints(sel_lst_delivery)
									+ ", t[j] = " + t[j].seqPointString()
									+ ", s = " + s.ID
									+ ") BUG-evaluateMoveSequencePoints");

						}
						if (delta < THRESHOLD_DELTA_NEGATIVE) {
							if (delta < minDelta) {
								minDelta = delta;
								sel_i = i;
								sel_lst_pickup = lst_pickup;
								sel_lst_delivery = lst_delivery;
								sel_s = s;
								sel_tj = t[j];
								sel_from_vehicle = vi;
								sel_to_vehicle = vj;
							}
						}

					}

				}
			}
			if (sel_lst_pickup != null) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, PERFORM HILL CLIMBING, ("
						+ toStringListPoints(sel_lst_pickup) + ","
						+ toStringListPoints(sel_lst_delivery) + ")"
						+ ", START delta = " + minDelta + ", print-XR = "
						+ XR.toStringShort());
				System.out
						.println(name()
								+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, time = "
								+ (time * 0.001) + ", timeLimit = "
								+ input.getParams().getTimeLimit()
								+ ", PERFORM HILL CLIMBING, ("
								+ toStringListPoints(sel_lst_pickup) + ","
								+ toStringListPoints(sel_lst_delivery) + ")"
								+ " from vehicle " + sel_from_vehicle.getCode()
								+ " to vehicle " + sel_to_vehicle.getCode()
								+ ", START delta = " + minDelta);

				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				// performMoveTrip(XR, t[sel_i], sel_route, DIXAVEGAN);
				performMoveSequencePoints(XR, sel_lst_pickup, sel_lst_delivery,
						sel_tj, sel_s, DIXAVEGAN, true);

				if (!checkAllSolution(XR)) {
					log(name()
							+ "::hillClimbingOptimizeDistanceInternalVehicleTrips, after PERFORM HILL-CLIMBING, checkAllSolution FAILED, BUG????");
					return hasChanged;
				} else {
					log(name()
							+ "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK");
				}
				System.out.println(name()
						+ "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = "
						+ cost.getValue());

			}

			if (bestCost > cost.getValue()) {
				bestCost = cost.getValue();
			} else {
				break;
			}
			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingOptimizeDistanceInternalVehicleTrips FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		// logTrips(XR);

		return hasChanged;
	}

	public boolean hillClimbingExchangeRequestPoints2Trips(
			boolean loadConstraint) {
		boolean hasChanged = false;
		log(name() + "::hillClimbingExchangeRequestPoints2Trips START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingExchangeRequestPoints2Trips, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingExchangeRequestPoints2Trips, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {

			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingExchangeRequestPoints2Trips + TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			HashMap<Vehicle, ArrayList<VehicleTrip>> mVehicle2Trips = VTC.mVehicle2Trips;

			// printTrips(XR);
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
			VehicleTrip sel_vt1 = null;
			VehicleTrip sel_vt2 = null;
			Point sel_pickup1 = null;
			Point sel_delivery1 = null;
			Point sel_pickup2 = null;
			Point sel_delivery2 = null;

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				VehicleTrip vt1 = t[i];
				if (!isInternalVehicle(vi))
					continue;

				for (int j = i + 1; j < t.length; j++) {
					Vehicle vj = t[j].vehicle;
					VehicleTrip vt2 = t[j];
					if (!isInternalVehicle(vj))
						continue;
					if (vi == vj)
						continue;

					if (conflictTrips(vt1, vt2))
						continue;

					for (Point pickup1 : vt1.seqPoints) {
						if (mPoint2Type.get(pickup1).equals("D"))
							continue;

						Point delivery1 = getDeliveryOfPickup(pickup1);// deliveryPoints.get(idx);

						if (vi != vj && fixVehicleDeliveryPoint(vi, delivery1))
							continue;

						for (Point pickup2 : vt2.seqPoints) {
							if (mPoint2Type.get(pickup2).equals("D"))
								continue;

							Point delivery2 = getDeliveryOfPickup(pickup2);// deliveryPoints.get(idx);

							if (vi != vj
									&& fixVehicleDeliveryPoint(vj, delivery2))
								continue;

							double delta = evaluateExchangeRequestPoints(XR,
									pickup1, delivery1, vt2, pickup2,
									delivery2, vt1, DIXAVEGAN, loadConstraint);

							if (delta < THRESHOLD_DELTA_NEGATIVE) {
								if (delta < minDelta) {
									minDelta = delta;
									sel_vt1 = vt1;
									sel_vt2 = vt2;
									sel_pickup1 = pickup1;
									sel_delivery1 = delivery1;
									sel_pickup2 = pickup2;
									sel_delivery2 = delivery2;

								}
							}
						}

					}
				}
			}
			if (sel_vt1 != null) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingExchangeRequestPoints2Trips, PERFORM HILL CLIMBING, ("
						+ sel_pickup1.ID + "," + sel_delivery1.ID + ","
						+ sel_pickup2.ID + "," + sel_delivery2.ID + ")"
						+ ", START delta = " + minDelta);
				System.out.println(name()
						+ "::hillClimbingExchangeRequestPoints2Trips, time = "
						+ (time * 0.001) + ", timeLimit = "
						+ input.getParams().getTimeLimit()
						+ ", PERFORM HILL CLIMBING, (" + sel_pickup1.ID + ","
						+ sel_delivery1.ID + "," + sel_vt2.seqPointString()
						+ "," + sel_pickup2.ID + "," + sel_delivery2.ID + ","
						+ sel_vt1.seqPointString() + ")" + ", START delta = "
						+ minDelta);

				performExchangeRequestPoints(XR, sel_pickup1, sel_delivery1,
						sel_vt2, sel_pickup2, sel_delivery2, sel_vt1,
						DIXAVEGAN, loadConstraint);
				System.out
						.println(name()
								+ "::hillClimbingExchangeRequestPoints2Trips, after move, cost = "
								+ cost.getValue());
			}

			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbingExchangeRequestPoints2Trips FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);

		return hasChanged;
	}

	public boolean hillClimbingNewVehicleOptimizeDistanceExternalVehicle(
			boolean loadConstraint) {
		// timelimit in seconds
		boolean hasChanged = false;
		log(name()
				+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		HashMap<Vehicle, Integer> mVehicle2RouteIndex = new HashMap<Vehicle, Integer>();
		for (int k = 1; k <= XR.getNbRoutes(); k++) {
			Point s = XR.startPoint(k);
			Vehicle vh = mPoint2Vehicle.get(s);
			mVehicle2RouteIndex.put(vh, k);
		}

		while (true) {
			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, time = "
								+ time
								+ " timeLimit = "
								+ (input.getParams().getTimeLimit() * 60 * 1000)
								+ " TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			// log(VTC,XR);

			// printTrips(XR);
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
			int sel_k = -1;
			// System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan

			// for (int k = 1; k <= XR.getNbRoutes(); k++) {
			// if (XR.emptyRoute(k)) {
			// log(name() +
			// "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, emptyRoute["
			// + k
			// + "], vehicle "
			// + mPoint2Vehicle.get(XR.startPoint(k)).getWeight());
			// }
			// }

			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (isInternalVehicle(vi))
					continue;
				/*
				 * if (input.getParams().getInternalVehicleFirst().equals("Y"))
				 * { if (isInternalVehicle(vi)) { if
				 * (VTC.mVehicle2Trips.get(vi).size() == 1) continue; } }
				 */
				for (int j = i + 1; j < t.length; j++) {
					Vehicle vj = t[j].vehicle;
					if (isInternalVehicle(vj))
						continue;
					/*
					 * if
					 * (input.getParams().getInternalVehicleFirst().equals("Y"))
					 * { if (isInternalVehicle(vj)) { if
					 * (VTC.mVehicle2Trips.get(vj).size() == 1) continue; if(vi
					 * == vj && VTC.mVehicle2Trips.get(vj).size() == 2)
					 * continue; } }
					 */

					// collect empty trip

					ArrayList<Integer> L = new ArrayList<Integer>();
					HashSet<String> vehicleCategory = new HashSet<String>();
					for (int k = 1; k < XR.getNbRoutes(); k++) {
						if (!XR.emptyRoute(k))
							continue;
						Point startPoint = XR.startPoint(k);
						Vehicle vk = mPoint2Vehicle.get(startPoint);
						if (isInternalVehicle(vk))
							continue;
						if (!vehicleCategory.contains(vk.getVehicleCategory())) {
							vehicleCategory.add(vk.getVehicleCategory());
							L.add(k);
						}
					}

					// for (int k = 1; k < XR.getNbRoutes(); k++) {
					for (int k : L) {
						if (!XR.emptyRoute(k))
							continue;
						Point startPoint = XR.startPoint(k);
						Vehicle vk = mPoint2Vehicle.get(startPoint);
						if (isInternalVehicle(vk))
							continue;

						double delta = evaluateMoveTripNewVehicle(XR, t[i],
								t[j], XR.startPoint(k), DIXAVEGAN,
								loadConstraint);
						if (delta < THRESHOLD_DELTA_NEGATIVE) {
							if (delta < minDelta) {
								minDelta = delta;
								sel_i = i;
								sel_j = j;
								sel_k = k;
							}
						}
					}
				}
			}
			if (sel_i >= 0) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, PERFORM HILL CLIMBING, t["
						+ sel_i + "] = " + t[sel_i].seqPointString() + ", t["
						+ sel_j + "] = " + t[sel_j].seqPointString()
						+ ", vehicle " + t[sel_i].vehicle.getCode() + " has t["
						+ sel_i + "].sz = "
						+ VTC.mVehicle2Trips.get(t[sel_i].vehicle).size()
						+ ", vehicle " + t[sel_j].vehicle.getCode() + " has t["
						+ sel_j + "].sz = "
						+ VTC.mVehicle2Trips.get(t[sel_j].vehicle).size()
						+ ", START delta = " + minDelta + ", select vehicle "
						+ mPoint2Vehicle.get(XR.startPoint(sel_k)).getWeight());
				System.out
						.println(name()
								+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle, time = "
								+ (time * 0.001)
								+ ", timeLimit = "
								+ input.getParams().getTimeLimit()
								+ ", PERFORM HILL CLIMBING, "
								// + "t[" + sel_i
								// + "] = " + t[sel_i].seqPointString() + ", t["
								// + sel_j
								// + "] = " + t[sel_j].seqPointString()
								+ ", START delta = "
								+ minDelta
								+ ", select vehicle "
								+ mPoint2Vehicle.get(XR.startPoint(sel_k))
										.getWeight());
				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				performMoveTripNewVehicle(XR, t[sel_i], t[sel_j],
						XR.startPoint(sel_k), DIXAVEGAN);

				/*
				 * HashSet<Vehicle> U = getUnusedInternalVehicles(XR);
				 * if(U.size() > 0){ String msg = ""; for(Vehicle v: U) msg +=
				 * v.getCode() + ", "; log(name() +
				 * "::hillClimbingNewVehicle, BUG??? U.sz = " + U.size() +
				 * ", msg = " + msg); }
				 */

				/*
				 * if (!checkAllSolution(XR)) { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
				 * ); } else { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
				 * ); } System.out.println(name() +
				 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = " +
				 * cost.getValue());
				 */
			}
			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingNewVehicleOptimizeDistanceExternalVehicle FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);

		return hasChanged;
	}

	public boolean hillClimbingNewVehicle(boolean loadConstraint) {
		// timelimit in seconds
		boolean hasChanged = false;
		log(name() + "::hillClimbingNewVehicle START XR = " + toStringShort(XR)
				+ ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingNewVehicle, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingNewVehicle, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {
			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out.println(name() + "::hillClimbingNewVehicle, time = "
						+ time + " timeLimit = "
						+ (input.getParams().getTimeLimit() * 60 * 1000)
						+ " TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			// log(VTC,XR);

			// printTrips(XR);
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
			int sel_k = -1;
			System.out.println(name() + "init cost = " + cost.getValue());

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa
																				// ve
			// gan

			for (int k = 1; k <= XR.getNbRoutes(); k++) {
				if (XR.emptyRoute(k)) {
					log(name() + "::hillClimbingNewVehicle, emptyRoute[" + k
							+ "], vehicle "
							+ mPoint2Vehicle.get(XR.startPoint(k)).getWeight());
				}
			}
			ArrayList<Integer> L = new ArrayList<Integer>();
			HashSet<String> vehicleCategory = new HashSet<String>();
			for (int k = 1; k < XR.getNbRoutes(); k++) {
				if (!XR.emptyRoute(k))
					continue;
				Point startPoint = XR.startPoint(k);
				Vehicle vk = mPoint2Vehicle.get(startPoint);
				if (isInternalVehicle(vk)) {
					L.add(k);
				} else {
					if (!vehicleCategory.contains(vk.getVehicleCategory())) {
						vehicleCategory.add(vk.getVehicleCategory());
						L.add(k);
					}
				}
			}
			System.out.println(name()
					+ "::hillClimbingNewVehicle, empty vehicle L.sz = "
					+ L.size() + ", nbTrips = " + t.length);
			for (int i = 0; i < t.length; i++) {
				Vehicle vi = t[i].vehicle;
				if (fixVehicleTrip(vi, t[i]))
					continue;

				/*
				 * if (input.getParams().getInternalVehicleFirst().equals("Y"))
				 * { if (isInternalVehicle(vi)) { if
				 * (VTC.mVehicle2Trips.get(vi).size() == 1) continue; } }
				 */
				for (int j = i + 1; j < t.length; j++) {
					Vehicle vj = t[j].vehicle;
					if (fixVehicleTrip(vj, t[j]))
						continue;

					if (conflictTrips(t[i], t[j]))
						continue;
					/*
					 * if
					 * (input.getParams().getInternalVehicleFirst().equals("Y"))
					 * { if (isInternalVehicle(vj)) { if
					 * (VTC.mVehicle2Trips.get(vj).size() == 1) continue; if(vi
					 * == vj && VTC.mVehicle2Trips.get(vj).size() == 2)
					 * continue; } }
					 */

					// for (int k = 1; k < XR.getNbRoutes(); k++) {
					for (int k : L) {
						if (!XR.emptyRoute(k))
							continue;

						double delta = evaluateMoveTripNewVehicle(XR, t[i],
								t[j], XR.startPoint(k), DIXAVEGAN,
								loadConstraint);
						if (delta < THRESHOLD_DELTA_NEGATIVE) {
							if (delta < minDelta) {
								minDelta = delta;
								sel_i = i;
								sel_j = j;
								sel_k = k;
							}
						}
					}
				}
			}
			if (sel_i >= 0) {
				hasMove = true;
				hasChanged = true;
				log(name()
						+ "::hillClimbingNewVehicle, PERFORM HILL CLIMBING, t["
						+ sel_i + "] = " + t[sel_i].seqPointString() + ", t["
						+ sel_j + "] = " + t[sel_j].seqPointString()
						+ ", vehicle " + t[sel_i].vehicle.getCode() + " has t["
						+ sel_i + "].sz = "
						+ VTC.mVehicle2Trips.get(t[sel_i].vehicle).size()
						+ ", vehicle " + t[sel_j].vehicle.getCode() + " has t["
						+ sel_j + "].sz = "
						+ VTC.mVehicle2Trips.get(t[sel_j].vehicle).size()
						+ ", START delta = " + minDelta + ", select vehicle "
						+ mPoint2Vehicle.get(XR.startPoint(sel_k)).getWeight());
				System.out.println(name()
						+ "::hillClimbingNewVehicle, time = "
						+ (time * 0.001)
						+ ", timeLimit = "
						+ input.getParams().getTimeLimit()
						+ ", PERFORM HILL CLIMBING, "
						// + "t[" + sel_i
						// + "] = " + t[sel_i].seqPointString() + ", t[" + sel_j
						// + "] = " + t[sel_j].seqPointString()
						+ ", START delta = " + minDelta + ", select vehicle "
						+ mPoint2Vehicle.get(XR.startPoint(sel_k)).getWeight());
				// performMoveTrip(XR, t[sel_i],t[sel_j]);
				performMoveTripNewVehicle(XR, t[sel_i], t[sel_j],
						XR.startPoint(sel_k), DIXAVEGAN);

				/*
				 * HashSet<Vehicle> U = getUnusedInternalVehicles(XR);
				 * if(U.size() > 0){ String msg = ""; for(Vehicle v: U) msg +=
				 * v.getCode() + ", "; log(name() +
				 * "::hillClimbingNewVehicle, BUG??? U.sz = " + U.size() +
				 * ", msg = " + msg); }
				 */

				/*
				 * if (!checkAllSolution(XR)) { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution FAILED, BUG????"
				 * ); } else { log(name() +
				 * "::hillClimbing, after PERFORM HILL CLIMBING, checkAllSolution OK"
				 * ); } System.out.println(name() +
				 * "::hillClimbing, PERFORM HILL CLIMBING, OK, cost = " +
				 * cost.getValue());
				 */
			}
			if (!hasMove)
				break;
		}
		log(name() + "::hillClimbingNewVehicle FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);

		return hasChanged;
	}

	public boolean hillClimbingExchangeTrips2InternalVehicles(
			boolean loadConstraint) {
		// timelimit in seconds
		boolean hasChanged = false;
		log(name() + "::hillClimbingExchangeTrips2InternalVehicles START XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		if (!checkAllSolution(XR)) {
			log(name()
					+ "::hillClimbingExchangeTrips2InternalVehicles, before hillClimbing, checkAllSolution FAILED, BUG?????");
		} else {
			log(name()
					+ "::hillClimbingExchangeTrips2InternalVehicles, before hillClimbing, checkAllSolution OK");
		}
		logTrips(XR);

		// if(true) return;

		while (true) {
			double time = System.currentTimeMillis() - startExecutionTime;
			if (time > input.getParams().getTimeLimit() * 60 * 1000) {
				System.out
						.println(name()
								+ "::hillClimbingExchangeTrips2InternalVehicles, time = "
								+ time
								+ " timeLimit = "
								+ (input.getParams().getTimeLimit() * 60 * 1000)
								+ " TIME LIMIT EXPIRED, BREAK");
				timeLimitExpired = true;
				break;
			}

			VehicleTripCollection VTC = analyzeTrips(XR);
			ArrayList<VehicleTrip> trips = VTC.trips;
			// log(VTC,XR);

			// printTrips(XR);
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
			VehicleTrip sel_vt11 = null;
			VehicleTrip sel_vt12 = null;
			VehicleTrip sel_vt21 = null;
			VehicleTrip sel_vt22 = null;

			boolean DIXAVEGAN = input.getParams().getIntCity().equals("TRUE");// di
																				// xa

			for (int i = 1; i <= XR.getNbRoutes(); i++) {
				Point si = XR.startPoint(i);
				Vehicle vh1 = mPoint2Vehicle.get(si);
				// if(!isInternalVehicle(vh1)) continue;
				if (VTC.mVehicle2Trips.get(vh1).size() < 2)
					continue;

				for (int j = 1; j <= XR.getNbRoutes(); j++)
					if (i != j) {
						Point sj = XR.startPoint(j);
						Vehicle vh2 = mPoint2Vehicle.get(sj);
						// if(!isInternalVehicle(vh2)) continue;
						if (VTC.mVehicle2Trips.get(vh2).size() < 2)
							continue;

						for (int i11 = 0; i11 < VTC.mVehicle2Trips.get(vh1)
								.size(); i11++) {
							VehicleTrip vt11 = VTC.mVehicle2Trips.get(vh1).get(
									i11);
							if (fixVehicleTrip(vh1, vt11))
								continue;

							for (int i12 = 0; i12 < VTC.mVehicle2Trips.get(vh1)
									.size(); i12++)
								if (i11 != i12) {
									VehicleTrip vt12 = VTC.mVehicle2Trips.get(
											vh1).get(i12);

									for (int i21 = 0; i21 < VTC.mVehicle2Trips
											.get(vh2).size(); i21++) {
										VehicleTrip vt21 = VTC.mVehicle2Trips
												.get(vh2).get(i21);
										for (int i22 = 0; i22 < VTC.mVehicle2Trips
												.get(vh2).size(); i22++)
											if (i21 != i22) {
												VehicleTrip vt22 = VTC.mVehicle2Trips
														.get(vh2).get(i22);
												if (fixVehicleTrip(vh2, vt22))
													continue;

												// System.out.println(name() +
												// "::hillClimbingExchangeTrips2Vehicles, before evaluate, XR = "
												// + XR.toStringShort());
												double delta = evaluateExchangeTrip22Vehicles(
														XR, vt11, vt21, vt22,
														vt12, DIXAVEGAN,
														loadConstraint);
												// System.out.println(name() +
												// "::hillClimbingExchangeTrips2Vehicles, after evaluate, XR = "
												// + XR.toStringShort());
												if (delta < THRESHOLD_DELTA_NEGATIVE) {
													if (delta < minDelta) {
														minDelta = delta;
														sel_vt11 = vt11;
														sel_vt12 = vt12;
														sel_vt21 = vt21;
														sel_vt22 = vt22;
													}
												}
											}
									}
								}
						}
					}
			}

			if (sel_vt11 != null) {
				hasMove = true;
				hasChanged = true;
				performExchangeTrip22Vehicles(XR, sel_vt11, sel_vt21, sel_vt22,
						sel_vt12, DIXAVEGAN, loadConstraint);
				log(name()
						+ "::hillClimbingExchangeTrips2InternalVehicles, vt11 = "
						+ sel_vt11.seqPointString() + ", vt21 = "
						+ sel_vt21.seqPointString() + ", vt22 = "
						+ sel_vt22.seqPointString() + ", vt12 = "
						+ sel_vt12.seqPointString() + ", delta = " + minDelta);
				System.out
						.println(name()
								+ "::hillClimbingExchangeTrips2InternalVehicles, vt11 = "
								+ sel_vt11.seqPointString() + ", vt21 = "
								+ sel_vt21.seqPointString() + ", vt22 = "
								+ sel_vt22.seqPointString() + ", vt12 = "
								+ sel_vt12.seqPointString() + ", delta = "
								+ minDelta);
				System.out
						.println(name()
								+ "::hillClimbingExchangeTrips2InternalVehicles, newCost = "
								+ cost.getValue());

			}

			if (!hasMove)
				break;
		}
		log(name()
				+ "::hillClimbingExchangeTrips2InternalVehicles FINISHED XR = "
				+ toStringShort(XR) + ", START-COST = " + cost.getValue());
		logTrips(XR);
		return hasChanged;
	}

}
