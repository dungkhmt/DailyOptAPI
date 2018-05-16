package routingdelivery.service;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.timewindows.CEarliestArrivalTimeVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
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
import localsearch.domainspecific.vehiclerouting.vrp.neighborhoodexploration.INeighborhoodExplorer;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import utils.DateTimeUtils;

class CVRPTWSearch extends GenericLocalSearch{
	public CVRPTWSearch(VRManager mgr){
		super(mgr);
	}
	public String name(){
		return "MySearch";
	}
	@Override
	public void generateInitialSolution(){
		System.out.println(name() + "::generateInitialSolution.....");
		super.generateInitialSolution();
		//System.exit(-1);
	}
}

public class CVRPTWSolver {
	public static final String module = CVRPTWSolver.class.getName();
	private PickupDeliveryInput input;
	private PickupDeliveryRequest[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	
	private HashMap<String, Double> mDistance;
	private int N;// points: 0 (depot), clients 1, 2, ..., N
	private int K;// number of vehicles
	private double[][] dis;// dis[i][j] is the distance from point i to point j
	private double[] demand;
	private double[] cap;
	private HashMap<Integer, Integer> mOrderItem2Request;
	
	
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> clientPoints;
	private ArrayList<Point> allPoints;
	private HashMap<Point, Integer> mPoint2Index;
	HashMap<Point, Integer> earliestAllowedArrivalTime;
	HashMap<Point, Integer> serviceDuration;
	HashMap<Point, Integer> lastestAllowedArrivalTime;
	private ArcWeightsManager awm;
	private ArcWeightsManager travelTime;
	private NodeWeightsManager nwm;
	private VRManager mgr;
	private VarRoutesVR XR;
	private ConstraintSystemVR CS;
	public CEarliestArrivalTimeVR ceat;
	private IFunctionVR cost;
	private IFunctionVR[] load;
	private LexMultiFunctions F;
	
	private String code(String from, String to){
		return from + "-" + to;
	}
	private void mapData(){
		mDistance = new HashMap<String, Double>();
		for(int i = 0; i < distances.length; i++){
			String src = distances[i].getSrcCode();
			String dest = distances[i].getDestCode();
			mDistance.put(code(src,dest), distances[i].getDistance());
			//System.out.println(module + "::mapData, mDistance.put(" + code(src,dest) + "," + distances[i].getDistance() + ")");
		}
		String depotCode = vehicles[0].getStartLocationCode();
		
		mOrderItem2Request = new HashMap<Integer, Integer>();
		earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		lastestAllowedArrivalTime = new HashMap<Point, Integer>();
		serviceDuration = new HashMap<Point, Integer>();
		
		mPoint2Index = new HashMap<Point, Integer>();
		N = 0;
		for(int i = 0; i < requests.length; i++){
			N += requests[i].getItems().length;
		}
		demand = new double[N+1];
		K = vehicles.length;
		cap = new double[K];
		for(int k = 0; k < vehicles.length; k++)
			cap[k] = vehicles[k].getWeight();
		
		System.out.println(module + "::mapData, requests = " + N);
		
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		dis = new double[N+1][N+1];
		int idxOrderItem = 0;
		demand[0] = 0;
		for(int i = 0; i < requests.length; i++){
			for(int j = 0; j < requests[i].getItems().length; j++){
				idxOrderItem++;
				mOrderItem2Request.put(idxOrderItem, i);
				Point p = new Point(idxOrderItem);
				clientPoints.add(p);
				allPoints.add(p);
				mPoint2Index.put(p, idxOrderItem);
				
				demand[idxOrderItem] = requests[i].getItems()[j].getWeight();
				earliestAllowedArrivalTime.put(p, (int)DateTimeUtils.dateTime2Int(requests[i].getEarlyDeliveryTime()));
				serviceDuration.put(p, 1800);// load-unload is 30 minutes
				lastestAllowedArrivalTime.put(p, (int)DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryTime()));
			}
			
		}
		
		
		for(int k = 0; k < vehicles.length; k++){
			Point s = new Point(0);
			Point t = new Point(0);
			startPoints.add(s);
			endPoints.add(t);
			allPoints.add(s);
			allPoints.add(t);
			mPoint2Index.put(s, 0);
			mPoint2Index.put(t, 0);
			
			earliestAllowedArrivalTime.put(s, (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			lastestAllowedArrivalTime.put(s, (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			earliestAllowedArrivalTime.put(t, (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			lastestAllowedArrivalTime.put(t, (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			serviceDuration.put(s, 0);
			serviceDuration.put(t, 0);
		}
		awm = new ArcWeightsManager(allPoints);
		nwm = new NodeWeightsManager(allPoints);
		travelTime = new ArcWeightsManager(allPoints);
		
		for(Point p: allPoints){
			int ip = mPoint2Index.get(p);
			String lp = depotCode;
			if(ip > 0){
				int ir = mOrderItem2Request.get(ip);
				lp = requests[ir].getDeliveryLocationCode();
			}
			for(Point q: allPoints){
				int iq = mPoint2Index.get(q);
				String lq = depotCode;
				if(iq > 0){
					int ir = mOrderItem2Request.get(iq);
					lq = requests[ir].getDeliveryLocationCode();
					double d = mDistance.get(code(lp,lq));
					awm.setWeight(p, q, d);
					travelTime.setWeight(p, q, (d*1000)/input.getParams().getAverageSpeed());// meter per second
				}
			}
			if(ip == 0) nwm.setWeight(p, 0);
			else {
				nwm.setWeight(p, demand[ip]);
			}
		}
		
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for(int i = 0; i < startPoints.size(); i++){
			Point s = startPoints.get(i);
			Point t = endPoints.get(i);
			XR.addRoute(s, t);
		}
		for(Point p: clientPoints)
			XR.addClientPoint(p);
		
		CS = new ConstraintSystemVR(mgr);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		EarliestArrivalTimeVR eat = new EarliestArrivalTimeVR(XR, travelTime, earliestAllowedArrivalTime, serviceDuration);
		ceat = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
		
		load = new IFunctionVR[XR.getNbRoutes()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			load[k-1] = new AccumulatedNodeWeightsOnPathVR(awn, XR.endPoint(k));
			CS.post(new Leq(load[k-1], cap[k-1]));
		}
		CS.post(ceat);
		
		cost = new TotalCostVR(XR, awm);
	
		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(cost);
		
		mgr.close();
	
		ArrayList<INeighborhoodExplorer> NE = new ArrayList<INeighborhoodExplorer>();
		NE.add(new GreedyOnePointMoveExplorer(XR, F));
		NE.add(new GreedyOrOptMove1Explorer(XR, F));
		NE.add(new GreedyOrOptMove2Explorer(XR, F));
		NE.add(new GreedyThreeOptMove1Explorer(XR, F));
		NE.add(new GreedyThreeOptMove2Explorer(XR, F));
		NE.add(new GreedyThreeOptMove3Explorer(XR, F));
		NE.add(new GreedyThreeOptMove4Explorer(XR, F));
		NE.add(new GreedyThreeOptMove5Explorer(XR, F));
		NE.add(new GreedyThreeOptMove6Explorer(XR, F));
		NE.add(new GreedyThreeOptMove7Explorer(XR, F));
		NE.add(new GreedyThreeOptMove8Explorer(XR, F));
		NE.add(new GreedyTwoOptMove1Explorer(XR, F));
		NE.add(new GreedyTwoOptMove2Explorer(XR, F));
		NE.add(new GreedyTwoOptMove3Explorer(XR, F));
		NE.add(new GreedyTwoOptMove4Explorer(XR, F));
		NE.add(new GreedyTwoOptMove5Explorer(XR, F));
		NE.add(new GreedyTwoOptMove6Explorer(XR, F));
		NE.add(new GreedyTwoOptMove7Explorer(XR, F));
		NE.add(new GreedyTwoOptMove8Explorer(XR, F));
		// NE.add(new GreedyTwoPointsMoveExplorer(XR, F));
		NE.add(new GreedyCrossExchangeMoveExplorer(XR, F));
		// NE.add(new GreedyAddOnePointMoveExplorer(XR, F));

		CVRPTWSearch se = new CVRPTWSearch(mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(1000, 100);

		System.out.println("solution XR = " + XR.toString() + ", cost = " + cost.getValue());
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			System.out.println("Route[" + k + "]");
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				Point np = XR.next(p);
				double tt = travelTime.getWeight(p,np);
				long at = (long)eat.getEarliestArrivalTime(p);
				System.out.println("point " + p.getID() + ", demand = " + nwm.getWeight(p) + ", t2n = " + tt + ", eat = " + DateTimeUtils.unixTimeStamp2DateTime(at));
			}
			System.out.println(", load = " + load[k-1].getValue() + ", cap = " + cap[k-1]);
			System.out.println("------------------------------------");
		}
		
	}
	
	
	public PickupDeliverySolution compute(PickupDeliveryInput input){
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		
		mapData();
		
		int nbr = 0;
		for(int k = 1; k <= XR.getNbRoutes(); k++)
			if(XR.next(XR.startPoint(k)) != XR.endPoint(k)) nbr++;
		
		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.next(XR.startPoint(k)) != XR.endPoint(k)){
				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
					int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if(ip == 0){
						// depot
						double d_lat = vehicles[0].getLat();
						double d_lng = vehicles[0].getLng();
						e = new RoutingElement(vehicles[0].getStartLocationCode(), "-", d_lat + "," + d_lng, d_lat, d_lng);
					}else{
						int ir = mOrderItem2Request.get(ip);
						double lat = requests[ir].getDeliveryLat();
						double lng = requests[ir].getDeliveryLng();
						e = new RoutingElement(requests[ir].getDeliveryLocationCode(), "-", lat + "," + lng,lat,lng);
					}
					lst.add(e);
				}
				double d_lat = vehicles[0].getLat();
				double d_lng = vehicles[0].getLng();
				RoutingElement e = new RoutingElement(vehicles[0].getStartLocationCode(), "-", d_lat + "," + d_lng, d_lat, d_lng);
				lst.add(e);
				
				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for(int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				routes[nbr] = new RoutingSolution(a_route);
			}
		}
		for(int i = 0; i < requests.length; i++){
			PickupDeliveryRequest r = requests[i];
			System.out.println("req[" + i + "], pickup = " + r.getPickupLat() + "," + r.getPickupLng() +
					", delivery = " + r.getDeliveryLat() + "," + r.getDeliveryLng());
			
		}
		return new PickupDeliverySolution(routes);
	}
	
}
