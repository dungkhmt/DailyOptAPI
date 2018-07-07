package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import routingdelivery.model.Vehicle;
import utils.DateTimeUtils;

public class BrennTagRouteSolverForOneVehicle {
	BrenntagPickupDeliverySolver solver;
	VRManager mgr;
	VarRoutesVR XR;
	AccumulatedWeightNodesVR awn;
	AccumulatedWeightEdgesVR awe;
	Point s;
	Point t;
	Vehicle vh;
	HashSet<Integer> pickupPointIndices;
	// ArrayList<Point> pickupPoints;
	// ArrayList<Point> deliveryPoints;
	ArrayList<Point> allPoints;
	ArcWeightsManager awm;
	NodeWeightsManager nwm;
	IFunctionVR totalCost;
	double totalWeights;

	public BrennTagRouteSolverForOneVehicle(BrenntagPickupDeliverySolver solver) {
		this.solver = solver;

	}

	public String name() {
		return BrennTagRouteSolverForOneVehicle.class.getName();
	}
	
	public String analyzeNotServed(Vehicle vh, int index) {
		this.vh = vh;
		allPoints = new ArrayList<Point>();
		allPoints.add(solver.pickupPoints.get(index));
		allPoints.add(solver.deliveryPoints.get(index));
		s = new Point();
		t = new Point();
		allPoints.add(s);
		allPoints.add(t);
		solver.mPoint2LocationCode.put(s, vh.getStartLocationCode());
		solver.mPoint2LocationCode.put(t, vh.getEndLocationCode());
		solver.mPoint2IndexLoadedItems.put(s, new HashSet<Integer>());
		solver.mPoint2IndexLoadedItems.put(t, new HashSet<Integer>());
		solver.mPoint2Type.put(s, "S");
		solver.mPoint2Type.put(t, "T");
		awm = new ArcWeightsManager(allPoints);
		for (Point p : allPoints) {
			for (Point q : allPoints) {
				String lp = solver.mPoint2LocationCode.get(p);
				String lq = solver.mPoint2LocationCode.get(q);
				int ip = solver.mLocationCode2Index.get(lp);
				int iq = solver.mLocationCode2Index.get(lq);
				awm.setWeight(p, q, solver.a_distance[ip][iq]);
			}
		}

		nwm = new NodeWeightsManager(allPoints);

		for (Point p : allPoints) {
			if (p == s || p == t)
				nwm.setWeight(p, 0);
			else {
				nwm.setWeight(p, solver.mPoint2Demand.get(p));
			}
		}
		solver.earliestAllowedArrivalTime.put(s,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		solver.serviceDuration.put(s, 0);
		solver.lastestAllowedArrivalTime.put(s,
				(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));

		solver.earliestAllowedArrivalTime.put(t,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		solver.serviceDuration.put(t, 0);
		solver.lastestAllowedArrivalTime.put(t,
				(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));

		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);

		XR.addRoute(s, t);
		XR.addClientPoint(solver.pickupPoints.get(index));
		XR.addClientPoint(solver.deliveryPoints.get(index));
		totalCost = new TotalCostVR(XR, awm);

		awn = new AccumulatedWeightNodesVR(XR, nwm);
		awe = new AccumulatedWeightEdgesVR(XR, awm);
		mgr.close();

		String retDes = "";
		HashSet<String> cannotReachLocationCodes = new HashSet<String>();
		for (int i = 0; i < solver.input.getExclusiveVehicleCategoryLocations().length; i++) {
			String vehicleCategory = solver.input
					.getExclusiveVehicleCategoryLocations()[i].getVehicleCode();
			String locationCode = solver.input
					.getExclusiveVehicleCategoryLocations()[i]
					.getLocationCode();
			if (vehicleCategory.equals(vh.getVehicleCategory()))
				cannotReachLocationCodes.add(locationCode);
		}

		Point pickup = solver.pickupPoints.get(index);
		Point delivery = solver.deliveryPoints.get(index);

		String pickupLocation = solver.mPoint2LocationCode.get(pickup);
		String deliveryLocation = solver.mPoint2LocationCode.get(delivery);

		// check points cannot be visited
		// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
		// !mPoint2PossibleVehicles.get(delivery).contains(k))
		// continue;
		if (cannotReachLocationCodes.contains(pickupLocation)
				|| cannotReachLocationCodes.contains(deliveryLocation)) {

			retDes = "\nXe " + vh.getVehicleCategory()
					+ " không thể đi đến điểm đón hoặc trả hàng "
					+ solver.getOrderIDAtPoint(pickup);
			return retDes;
		}
		if(vh.getWeight() < solver.mPoint2Demand.get(pickup)){
			retDes = "\nTổng đơn hàng = " + solver.mPoint2Demand.get(pickup) + " vượt quá tải trọng xe (" + 
		vh.getVehicleCategory() + ")" + vh.getWeight();
			return retDes;
		}
		Point p = XR.startPoint(1);
		Point d = p;
		double ec = solver.evaluateTimeViolationsAddTwoPoints(
				XR, 1, pickup, p, delivery, d);
		if(ec > 0){
			retDes = "\nKhông thể phục vụ đơn hàng " + solver.getOrderIDAtPoint(pickup) + 
					" do vi phạm khung thời gian làm việc";
		}
		return retDes;

	}
	
	public HashSet<Integer> solve(Vehicle vh,
			HashSet<Integer> pickupPointIndices) {
		this.vh = vh;
		// pickupPoints = new ArrayList<Point>();
		// deliveryPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		for (int i : pickupPointIndices) {
			// pickupPoints.add(solver.pickupPoints.get(i));
			// deliveryPoints.add(solver.deliveryPoints.get(i));
			allPoints.add(solver.pickupPoints.get(i));
			allPoints.add(solver.deliveryPoints.get(i));
		}
		s = new Point();
		t = new Point();
		allPoints.add(s);
		allPoints.add(t);
		solver.mPoint2LocationCode.put(s, vh.getStartLocationCode());
		solver.mPoint2LocationCode.put(t, vh.getEndLocationCode());
		solver.mPoint2IndexLoadedItems.put(s, new HashSet<Integer>());
		solver.mPoint2IndexLoadedItems.put(t, new HashSet<Integer>());
		solver.mPoint2Type.put(s, "S");
		solver.mPoint2Type.put(t, "T");
		awm = new ArcWeightsManager(allPoints);
		for (Point p : allPoints) {
			for (Point q : allPoints) {
				String lp = solver.mPoint2LocationCode.get(p);
				String lq = solver.mPoint2LocationCode.get(q);
				int ip = solver.mLocationCode2Index.get(lp);
				int iq = solver.mLocationCode2Index.get(lq);
				awm.setWeight(p, q, solver.a_distance[ip][iq]);
			}
		}

		nwm = new NodeWeightsManager(allPoints);

		for (Point p : allPoints) {
			if (p == s || p == t)
				nwm.setWeight(p, 0);
			else {
				nwm.setWeight(p, solver.mPoint2Demand.get(p));
			}
		}
		solver.earliestAllowedArrivalTime.put(s,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		solver.serviceDuration.put(s, 0);
		solver.lastestAllowedArrivalTime.put(s,
				(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));

		solver.earliestAllowedArrivalTime.put(t,
				(int) DateTimeUtils.dateTime2Int(vh.getStartWorkingTime()));
		solver.serviceDuration.put(t, 0);
		solver.lastestAllowedArrivalTime.put(t,
				(int) DateTimeUtils.dateTime2Int(vh.getEndWorkingTime()));

		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);

		XR.addRoute(s, t);
		for (int i : pickupPointIndices) {
			XR.addClientPoint(solver.pickupPoints.get(i));
			XR.addClientPoint(solver.deliveryPoints.get(i));
		}
		totalCost = new TotalCostVR(XR, awm);

		awn = new AccumulatedWeightNodesVR(XR, nwm);
		awe = new AccumulatedWeightEdgesVR(XR, awm);
		mgr.close();

		HashSet<String> cannotReachLocationCodes = new HashSet<String>();
		for (int i = 0; i < solver.input.getExclusiveVehicleCategoryLocations().length; i++) {
			String vehicleCategory = solver.input
					.getExclusiveVehicleCategoryLocations()[i].getVehicleCode();
			String locationCode = solver.input
					.getExclusiveVehicleCategoryLocations()[i]
					.getLocationCode();
			if (vehicleCategory.equals(vh.getVehicleCategory()))
				cannotReachLocationCodes.add(locationCode);
		}

		HashSet<Integer> cand = new HashSet<Integer>();
		for (int i : pickupPointIndices)
			cand.add(i);
		totalWeights = 0;
		System.out.println(name() + "::solve, vehicle category = "
				+ vh.getWeight() + ", cand = " + cand.size());
		while (cand.size() > 0) {
			Point sel_pickup = null;
			Point sel_delivery = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			int eval_newOrderLoaded = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_d = null;
			int sel_i = -1;

			for (int i : cand) {
				Point pickup = solver.pickupPoints.get(i);
				Point delivery = solver.deliveryPoints.get(i);

				String pickupLocation = solver.mPoint2LocationCode.get(pickup);
				String deliveryLocation = solver.mPoint2LocationCode
						.get(delivery);

				// check points cannot be visited
				// if(!mPoint2PossibleVehicles.get(pickup).contains(k) ||
				// !mPoint2PossibleVehicles.get(delivery).contains(k))
				// continue;
				if (cannotReachLocationCodes.contains(pickupLocation)
						|| cannotReachLocationCodes.contains(deliveryLocation))
					continue;

				for (Point p = XR.startPoint(1); p != XR.endPoint(1); p = XR
						.next(p)) {
					// check exclusive items
					boolean okExclusiveItems = true;

					okExclusiveItems = solver.checkExclusiveItems(p, pickup);

					if (!okExclusiveItems)
						continue;

					// check if after deliverying some items, the vehicle is
					// still loaded,
					// then it must be unloaded (until empty) before
					// pickingup new items
					if (solver.mPoint2Type.get(p).equals("D")
							&& awn.getSumWeights(p) > 0) {
						// cannot pickup any more if there are still items
						// on the vehicle
						continue;
					}

					for (Point d = p; d != XR.endPoint(1); d = XR.next(d)) {
						// new trial items will be unloaded after d --> need
						// check exclusive items

						okExclusiveItems = true;
						for (Point tp = XR.startPoint(1); tp != XR.next(d); tp = XR
								.next(tp)) {
							okExclusiveItems = solver.checkExclusiveItems(tp,
									pickup);
							if (!okExclusiveItems)
								break;
						}
						if (!okExclusiveItems)
							continue;

						if (solver.mPoint2Type.get(XR.next(d)).equals("P")
								&& awn.getSumWeights(d) > 0) {
							// after delivery (accumulated load > 0), there
							// is a pickup --> IGNORE
							continue;
						}

						boolean ok = true;
						for (Point tmp = p; tmp != XR.next(d); tmp = XR
								.next(tmp)) {
							if (nwm.getWeight(pickup) + awn.getSumWeights(tmp) > vh
									.getWeight()) {
								ok = false;
								break;
							}
						}
						if (!ok)
							continue;

						// double ec = CS.evaluateAddTwoPoints(pickup, p,
						// delivery, d);
						double ec = solver.evaluateTimeViolationsAddTwoPoints(
								XR, 1, pickup, p, delivery, d);

						double ef = totalCost.evaluateAddTwoPoints(pickup, p,
								delivery, d);

						int e_o = solver.evaluateNewOrderLoad(XR, 1, pickup, p,
								delivery, d);

						// System.out.println("consider i = " + i
						// + ", vehicle k = " + k + ", pickup = "
						// + pickup.ID + ", delivery = " + delivery.ID
						// + ", p = " + p.ID + ", d = " + d.ID
						// + ", ec = " + ec + ", e_o = " + e_o
						// + ", ef = " + ef);

						if (ec > 0)
							continue;// ensure constraint always satisfied

						if (solver.better(ec, e_o, ef, eval_violations,
								eval_newOrderLoaded, eval_cost)) {
							eval_violations = ec;
							eval_cost = ef;
							eval_newOrderLoaded = e_o;
							sel_p = p;
							sel_d = d;
							sel_pickup = pickup;
							sel_delivery = delivery;
							sel_i = i;

						}
						/*
						 * if (ec < eval_violations) { eval_violations = ec;
						 * eval_cost = ef; sel_p = p; sel_d = d; sel_pickup =
						 * pickup; sel_delivery = delivery; sel_i = i; sel_k =
						 * k; } else if (ec == eval_violations && ef <
						 * eval_cost) { eval_cost = ef; sel_p = p; sel_d = d;
						 * sel_pickup = pickup; sel_delivery = delivery; sel_i =
						 * i; sel_k = k; }
						 */
					}
				}

			}
			if (sel_i < 0) {
				System.out.println("CANNOT MOVE --> BREAK");
				break;
			} else {
				mgr.performAddOnePoint(sel_delivery, sel_d);
				mgr.performAddOnePoint(sel_pickup, sel_p);
				solver.propagateArrivalDepartureTime(XR, 1, true);

				// log.println("add delivery " + sel_delivery.ID + " after "+
				// sel_d.ID +
				// " AND pickup " + sel_pickup.ID + " after " + sel_p.ID);

				// System.out.println("init addOnePoint(" + sel_pickup.ID + ","
				// + sel_p.ID + "), and (" + sel_delivery.ID + ","
				// + sel_d.ID + ", XR = " + XR.toString() + ", CS = "
				// + CS.violations() + ", cost = " + cost.getValue());

				cand.remove(sel_i);
				totalWeights += nwm.getWeight(solver.pickupPoints.get(sel_i));

				// update loaded items
				for (int I : solver.mPoint2IndexLoadedItems.get(sel_p)) {
					solver.mPoint2IndexLoadedItems.get(sel_pickup).add(I);
				}
				for (int I : solver.mPoint2IndexLoadedItems.get(sel_d)) {
					solver.mPoint2IndexLoadedItems.get(sel_delivery).add(I);
				}
				for (Point p = sel_pickup; p != sel_delivery; p = XR.next(p)) {
					// mPoint2LoadedItems.get(p).add(
					// mPoint2ItemCode.get(sel_pickup));
					for (int ite : solver.mPoint2IndexItems.get(sel_pickup)) {
						solver.mPoint2IndexLoadedItems.get(p).add(ite);
					}
				}
				System.out.println(name() + "::solve, vehicle category = "
						+ vh.getWeight() + ", ACCEPT index " + sel_i
						+ ", REMAIN cand = " + cand.size() + ", XR = "
						+ XR.toString());
			}

		}
		return cand;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
