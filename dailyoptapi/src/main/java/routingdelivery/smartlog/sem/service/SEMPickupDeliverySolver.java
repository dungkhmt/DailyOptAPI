package routingdelivery.smartlog.sem.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.EarliestArrivalTimeVR;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.smartlog.sem.model.SEMPickupDeliveryInput;
import routingdelivery.smartlog.sem.model.SEMPickupDeliveryRequest;
import routingdelivery.smartlog.sem.model.SEMPickupDeliverySolution;
import routingdelivery.smartlog.sem.model.SEMRoutingElement;
import routingdelivery.smartlog.sem.model.SEMRoutingSolution;
import routingdelivery.smartlog.sem.model.SEMShipper;
import utils.DateTimeUtils;

public class SEMPickupDeliverySolver {
	protected SEMPickupDeliveryInput input;
	protected SEMPickupDeliveryRequest[] requests;
	protected SEMShipper[] shippers;
	protected DistanceElement[] a_distances;
	protected DistanceElement[] a_traveltimes;

	// map data
	protected ArrayList<Point> startPoints;
	protected ArrayList<Point> endPoints;
	protected ArrayList<Point> pickupPoints;
	protected ArrayList<Point> deliveryPoints;
	protected ArrayList<Point> allPoints;
	protected ArrayList<String> locationCodes;
	protected HashMap<String, Integer> mLocationCode2Index;
	protected HashMap<Point, String> mPoint2LocationCode;
	protected HashMap<Point, SEMPickupDeliveryRequest> mPoint2Request;
	protected HashMap<Point, String> mPoint2Type;
	protected double[][] m_travel_times;
	protected double[][] m_distances;

	protected ArcWeightsManager awm;
	protected ArcWeightsManager travelTime;
	protected NodeWeightsManager nwm;
	protected NodeWeightsManager moneyPointManager;
	protected NodeWeightsManager weightPointManager;
	protected HashMap<Point, Integer> earliestAllowedArrivalTime;
	protected HashMap<Point, Integer> serviceDuration;
	protected HashMap<Point, Integer> lastestAllowedArrivalTime;

	// model
	protected VRManager mgr;
	protected VarRoutesVR XR;
	protected ConstraintSystemVR CS;
	protected EarliestArrivalTimeVR eat;
	protected CEarliestArrivalTimeVR ceat;

	protected AccumulatedWeightNodesVR accWeightPoint;
	protected AccumulatedWeightNodesVR accMoneyPoint;
	protected AccumulatedWeightNodesVR accNbPoints;
	protected AccumulatedWeightEdgesVR accDistance;

	protected IFunctionVR[] distanceRoutes;// distance of scheduled routes
	protected IFunctionVR[] nbOrderOfShipper;// accumulate weights on nodes
	protected IFunctionVR[] amountMoneyOfShipper;// accumulate money on nodes
	protected IFunctionVR[] weightTripOfShipper;
	protected IFunctionVR obj;

	protected Random R = new Random();
	protected HashSet<Integer> cand;

	private RoutingElement createRoutingElementFirstPoint(int s){
		String code = shippers[s].getStartLocationCode();
		String orderId = "";
		String address = "";
		String latlng = "";
		double lat = shippers[s].getStartLat();
		double lng = shippers[s].getStartLng();
		String arrivalTime = DateTimeUtils.unixTimeStamp2DateTime((long)eat.getEarliestArrivalTime(XR.startPoint(s+1)));
		String departureTime = DateTimeUtils.unixTimeStamp2DateTime((long)eat.getEarliestArrivalTime(XR.startPoint(s+1)));
		String description = "S";
		double load = 0;
		double distance = 0;

		RoutingElement e = new RoutingElement(code, address, latlng,
				lat, lng, arrivalTime, departureTime, description,
				orderId, load, distance);
		return e;
	}
	
	private RoutingElement createRoutingElementEndPoint(int s){
		String code = shippers[s].getEndLocationCode();
		String orderId = "";
		String address = "";
		String latlng = "";
		double lat = shippers[s].getEndLat();
		double lng = shippers[s].getEndLng();
		String arrivalTime = DateTimeUtils.unixTimeStamp2DateTime((long)eat.getEarliestArrivalTime(XR.endPoint(s+1)));
		String departureTime = DateTimeUtils.unixTimeStamp2DateTime((long)eat.getEarliestArrivalTime(XR.endPoint(s+1)));
		String description = "S";
		double load = weightTripOfShipper[s].getValue();
		double distance = distanceRoutes[s].getValue();

		RoutingElement e = new RoutingElement(code, address, latlng,
				lat, lng, arrivalTime, departureTime, description,
				orderId, load, distance);
		return e;
	}
	private RoutingElement createRoutingElementForPoint(Point p){
		SEMPickupDeliveryRequest r = mPoint2Request.get(p);
		String code = r.getPickupLocationCode();
		String orderId = r.getOrderID();
		String address = "";
		String latlng = "";
		double lat = r.getPickupLat();
		double lng = r.getPickupLng();
		long at = (long)eat.getEarliestArrivalTime(p);
		if(at < earliestAllowedArrivalTime.get(p)) at = earliestAllowedArrivalTime.get(p);
		long dt = at + serviceDuration.get(p);
		String arrivalTime = DateTimeUtils.unixTimeStamp2DateTime(at);
		String departureTime = DateTimeUtils.unixTimeStamp2DateTime(dt);
		String description = mPoint2Type.get(p);
		double load = accWeightPoint.getSumWeights(p);
		double distance = accDistance.getCostRight(p);
		
		if (mPoint2Type.get(p).equals("D")) {
			code = r.getDeliveryLocationCode();
			lat = r.getDeliveryLat();
			lng = r.getDeliveryLng();
		}

		RoutingElement e = new RoutingElement(code, address, latlng,
				lat, lng, arrivalTime, departureTime, description,
				orderId, load, distance);
		return e;
	}

	public SEMPickupDeliverySolution compute(SEMPickupDeliveryInput input) {
		this.input = input;
		this.requests = input.getRequests();
		this.shippers = input.getShippers();
		this.a_distances = this.input.getDistances();
		this.a_traveltimes = this.input.getTraveltimes();
		mapLocationCodeDistances();
		mapPoints();
		stateModel();
		searchGreedyConstructive();

		SEMRoutingSolution[] routes = new SEMRoutingSolution[XR.getNbRoutes()];
		for (int s = 0; s < XR.getNbRoutes(); s++) {
			double maxWeight = 0;
			double totalWeight = 0;
			ArrayList<RoutingElement> l_elements = new ArrayList<RoutingElement>();
			Point p = XR.startPoint(s + 1);
			RoutingElement fe = createRoutingElementFirstPoint(s);
			for (p = XR.next(XR.startPoint(s + 1)); p != XR.endPoint(s + 1); p = XR
					.next(p)) {
				if(mPoint2Type.get(p).equals("P")){
					totalWeight += weightPointManager.getWeight(p);
					if(maxWeight < accWeightPoint.getSumWeights(p))
						maxWeight = accWeightPoint.getSumWeights(p);
				}
				
				RoutingElement e = createRoutingElementForPoint(p);
				l_elements.add(e);
			}
			p = XR.endPoint(s+1);
			RoutingElement te = createRoutingElementEndPoint(s);
			
			RoutingElement[] elements = new RoutingElement[l_elements.size()];
			for (int i = 0; i < l_elements.size(); i++)
				elements[i] = l_elements.get(i);
			routes[s] = new SEMRoutingSolution(shippers[s], elements, (int)nbOrderOfShipper[s].getValue(), 
					amountMoneyOfShipper[s].getValue(), maxWeight, totalWeight, distanceRoutes[s].getValue());
		}
		SEMPickupDeliveryRequest[] unServedRequests = new SEMPickupDeliveryRequest[cand.size()];
		int idx = -1;
		for(int i: cand){
			idx++;
			unServedRequests[idx] = requests[i];
		}
		SEMPickupDeliverySolution sol = new SEMPickupDeliverySolution(routes, unServedRequests);
		return sol;
	}

	public void mapLocationCodeDistances() {
		locationCodes = new ArrayList<String>();
		mLocationCode2Index = new HashMap<String, Integer>();
		HashSet<String> set_location_codes = new HashSet<String>();
		for (int i = 0; i < requests.length; i++) {
			set_location_codes.add(requests[i].getPickupLocationCode());
			set_location_codes.add(requests[i].getDeliveryLocationCode());
		}
		for (int i = 0; i < shippers.length; i++) {
			set_location_codes.add(shippers[i].getStartLocationCode());
			set_location_codes.add(shippers[i].getEndLocationCode());
		}

		for (String lc : set_location_codes) {
			locationCodes.add(lc);
			mLocationCode2Index.put(lc, locationCodes.size() - 1);
			System.out.println("MAP locationCode " + lc + " to "
					+ mLocationCode2Index.get(lc));
		}

		m_distances = new double[locationCodes.size()][locationCodes.size()];
		m_travel_times = new double[locationCodes.size()][locationCodes.size()];
		for (int k = 0; k < a_distances.length; k++) {
			DistanceElement de = a_distances[k];
			int i = mLocationCode2Index.get(de.getSrcCode());
			int j = mLocationCode2Index.get(de.getDestCode());
			m_distances[i][j] = de.getDistance();
		}
		for (int k = 0; k < a_traveltimes.length; k++) {
			DistanceElement de = a_traveltimes[k];
			int i = mLocationCode2Index.get(de.getSrcCode());
			int j = mLocationCode2Index.get(de.getDestCode());
			m_travel_times[i][j] = de.getDistance();
		}
	}

	public void mapPoints() {
		int idxPoint = -1;
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		pickupPoints = new ArrayList<Point>();
		deliveryPoints = new ArrayList<Point>();
		mPoint2LocationCode = new HashMap<Point, String>();
		mPoint2Request = new HashMap<Point, SEMPickupDeliveryRequest>();
		mPoint2Type = new HashMap<Point, String>();

		for (int i = 0; i < shippers.length; i++) {
			idxPoint++;
			Point s = new Point(idxPoint);
			idxPoint++;
			Point t = new Point(idxPoint);
			startPoints.add(s);
			endPoints.add(t);
			allPoints.add(s);
			allPoints.add(t);

			mPoint2LocationCode.put(s, shippers[i].getStartLocationCode());
			mPoint2LocationCode.put(t, shippers[i].getEndLocationCode());
			mPoint2Type.put(s, "S");
			mPoint2Type.put(t, "T");
		}

		for (int i = 0; i < requests.length; i++) {
			idxPoint++;
			Point pickup = new Point(idxPoint);
			idxPoint++;
			Point delivery = new Point(idxPoint);
			pickupPoints.add(pickup);
			deliveryPoints.add(delivery);
			allPoints.add(pickup);
			allPoints.add(delivery);

			mPoint2LocationCode
					.put(pickup, requests[i].getPickupLocationCode());
			mPoint2LocationCode.put(delivery,
					requests[i].getDeliveryLocationCode());

			mPoint2Request.put(pickup, requests[i]);
			mPoint2Request.put(delivery, requests[i]);
			mPoint2Type.put(pickup, "P");
			mPoint2Type.put(delivery, "D");
		}

		awm = new ArcWeightsManager(allPoints);
		travelTime = new ArcWeightsManager(allPoints);
		for (Point p : allPoints) {
			for (Point q : allPoints) {
				String lp = mPoint2LocationCode.get(p);
				String lq = mPoint2LocationCode.get(q);
				// System.out.println("lp = " + lp + ", lq = " + lq);
				int ip = mLocationCode2Index.get(lp);
				int iq = mLocationCode2Index.get(lq);
				double d = m_distances[ip][iq];
				double t = m_travel_times[ip][iq];

				awm.setWeight(p, q, d);
				travelTime.setWeight(p, q, t);
			}
		}

		nwm = new NodeWeightsManager(allPoints);
		moneyPointManager = new NodeWeightsManager(allPoints);
		weightPointManager = new NodeWeightsManager(allPoints);

		for (int i = 0; i < requests.length; i++) {
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			double w = 0;
			for (int j = 0; j < requests[i].getItems().length; j++)
				w += requests[i].getItems()[j].getWeight();
			nwm.setWeight(pickup, 1);
			nwm.setWeight(delivery, 0);

			moneyPointManager.setWeight(pickup, requests[i].getAmountMoney());
			moneyPointManager.setWeight(delivery, 0);

			weightPointManager.setWeight(pickup, w);
			weightPointManager.setWeight(delivery, -w);
		}
		for (int i = 0; i < shippers.length; i++) {
			Point s = startPoints.get(i);
			Point t = endPoints.get(i);
			nwm.setWeight(s, 0);
			nwm.setWeight(t, 0);
			moneyPointManager.setWeight(s, 0);
			moneyPointManager.setWeight(t, 0);
		}

		earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		lastestAllowedArrivalTime = new HashMap<Point, Integer>();
		serviceDuration = new HashMap<Point, Integer>();

		for (int i = 0; i < requests.length; i++) {
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);

			earliestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
					.dateTime2Int(requests[i].getEarlyPickupTime()));
			earliestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
					.dateTime2Int(requests[i].getEarlyDeliveryTime()));
			lastestAllowedArrivalTime.put(pickup, (int) DateTimeUtils
					.dateTime2Int(requests[i].getLatePickupTime()));
			lastestAllowedArrivalTime.put(delivery, (int) DateTimeUtils
					.dateTime2Int(requests[i].getLateDeliveryTime()));
			serviceDuration.put(pickup, requests[i].getPickupDuration());
			serviceDuration.put(delivery, requests[i].getDeliveryDuration());
		}

		for (int i = 0; i < shippers.length; i++) {
			Point s = startPoints.get(i);
			Point t = endPoints.get(i);

			earliestAllowedArrivalTime.put(s, (int) DateTimeUtils
					.dateTime2Int(shippers[i].getStartWorkingTime()));
			lastestAllowedArrivalTime.put(s, (int) DateTimeUtils
					.dateTime2Int(shippers[i].getEndWorkingTime()));
			serviceDuration.put(s, 0);

			earliestAllowedArrivalTime.put(t, (int) DateTimeUtils
					.dateTime2Int(shippers[i].getStartWorkingTime()));
			lastestAllowedArrivalTime.put(t, (int) DateTimeUtils
					.dateTime2Int(shippers[i].getEndWorkingTime()));
			serviceDuration.put(t, 0);
		}
	}

	public void stateModel() {
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for (int i = 0; i < startPoints.size(); i++) {
			Point s = startPoints.get(i);
			Point t = endPoints.get(i);
			XR.addRoute(s, t);
		}
		for (Point p : allPoints) {
			XR.addClientPoint(p);
		}
		CS = new ConstraintSystemVR(mgr);

		accWeightPoint = new AccumulatedWeightNodesVR(
				XR, weightPointManager);
		accMoneyPoint = new AccumulatedWeightNodesVR(
				XR, moneyPointManager);
		accNbPoints = new AccumulatedWeightNodesVR(XR,
				nwm);
		accDistance = new AccumulatedWeightEdgesVR(XR, awm);
		
		nbOrderOfShipper = new IFunctionVR[XR.getNbRoutes()];
		amountMoneyOfShipper = new IFunctionVR[XR.getNbRoutes()];
		weightTripOfShipper = new IFunctionVR[XR.getNbRoutes()];
		distanceRoutes = new IFunctionVR[XR.getNbRoutes()];
		
		for (int i = 0; i < XR.getNbRoutes(); i++) {
			nbOrderOfShipper[i] = new AccumulatedNodeWeightsOnPathVR(
					accNbPoints, XR.endPoint(i + 1));
			amountMoneyOfShipper[i] = new AccumulatedNodeWeightsOnPathVR(
					accMoneyPoint, XR.endPoint(i + 1));
			weightTripOfShipper[i] = new AccumulatedNodeWeightsOnPathVR(
					accWeightPoint, XR.endPoint(i + 1));
			distanceRoutes[i] = new AccumulatedEdgeWeightsOnPathVR(accDistance, XR.endPoint(i+1));
			
			CS.post(new Leq(nbOrderOfShipper[i], shippers[i].getMaxOrder() - shippers[i].getCurrentNbOrders()));
			CS.post(new Leq(amountMoneyOfShipper[i], shippers[i]
					.getMaxAmountMoney() - shippers[i].getCurrentMoney()));
			CS.post(new Leq(weightTripOfShipper[i], shippers[i]
					.getWeightCapacity() - shippers[i].getCurrentWeight()));
		}

		eat = new EarliestArrivalTimeVR(XR, travelTime,
				earliestAllowedArrivalTime, serviceDuration);
		ceat = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
		CS.post(ceat);

		obj = new TotalCostVR(XR, awm);
		mgr.close();
	}

	public void searchGreedyConstructive() {
		cand = new HashSet<Integer>();
		for (int i = 0; i < pickupPoints.size(); i++) {
			cand.add(i);
		}
		ArrayList<Integer> cand_s = new ArrayList<Integer>();
		while (cand.size() > 0) {
			int sel_i = -1;
			int sel_s = -1;
			Point sel_p = null;
			Point sel_d = null;
			Point sel_pickup = null;
			Point sel_delivery = null;

			// select shipper carrying smallest number of orders
			int min_order = Integer.MAX_VALUE;
			cand_s.clear();
			for (int s = 0; s < XR.getNbRoutes(); s++) {
				int nbo = (int) nbOrderOfShipper[s].getValue();
				if (nbo < min_order) {
					cand_s.clear();
					cand_s.add(s);
					min_order = (int) nbOrderOfShipper[s].getValue();
				} else if (nbo == min_order) {
					cand_s.add(s);
				}
			}
			if (cand_s.size() == 0) {
				System.out.println("Cannot select shipper --> BREAK");
				break;
			}
			sel_s = cand_s.get(R.nextInt(cand_s.size()));
			System.out.println("sel_s = " + sel_s);
			// select requests
			double min_eval_distance = Integer.MAX_VALUE;
			for (int i : cand) {
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				for (Point p = XR.startPoint(sel_s + 1); p != XR
						.endPoint(sel_s + 1); p = XR.next(p)) {
					for (Point d = p; d != XR.endPoint(sel_s + 1); d = XR
							.next(d)) {
						double eval_cs = CS.evaluateAddTwoPoints(pickup, p,
								delivery, d);
						System.out.println("sel_s = " + sel_s + ", eval_cs = "
								+ eval_cs);
						if (eval_cs > 0)
							continue;

						double eval_distance = obj.evaluateAddTwoPoints(pickup,
								p, delivery, d);
						if (eval_distance < min_eval_distance) {
							min_eval_distance = eval_distance;
							sel_p = p;
							sel_d = d;
							sel_pickup = pickup;
							sel_delivery = delivery;
							sel_i = i;
						}
					}
				}
			}
			if (sel_p == null) {
				System.out
						.println("Cannot select request to be served --> BREAK");
				break;
			}

			mgr.performAddOnePoint(sel_delivery, sel_d);
			mgr.performAddOnePoint(sel_pickup, sel_p);
			cand.remove(sel_i);
			print();
		}
	}

	public void print() {
		System.out.println(XR.toString());
		System.out.println("Cand = " + cand.size());
	}
}
