package routingdelivery.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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
import routingdelivery.model.DistanceElement;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import utils.DateTimeUtils;

class PickupDeliveryTWSearch extends GenericLocalSearch{
	//private PickupDeliveryInput input;
	//private PickupDeliveryRequest[] req;
	private ArrayList<Point> pickupPoints;
	private ArrayList<Point> deliveryPoints;
	private LexMultiFunctions F;
	private IConstraintVR CS;
	private IFunctionVR cost;
	private VarRoutesVR XR;
	public PickupDeliveryTWSearch(VRManager mgr, LexMultiFunctions F, IConstraintVR CS, IFunctionVR cost,
			VarRoutesVR XR, ArrayList<Point> pickupPoints, ArrayList<Point> deliveryPoints){
		super(mgr);
		this.pickupPoints = pickupPoints;
		this.deliveryPoints = deliveryPoints;
		this.F = F;
		this.CS = CS;
		this.cost = cost;
		this.XR = XR;
	}
	public String name(){
		return "MySearch";
	}
	private void greedyConstructMaintainConstraint(){
		HashSet<Integer> cand = new HashSet<Integer>();
		for(int i = 0; i < pickupPoints.size(); i++) cand.add(i);
		
		while(cand.size() > 0){
			Point sel_pickup = null;
			Point sel_delivery = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_d = null;
			for(int i : cand){
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				for(int k = 1; k <= XR.getNbRoutes(); k++){
					for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
						for(Point d = p; d != XR.endPoint(k); d = XR.next(d)){
							double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d);
							double ef = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
							if(ec < eval_violations){
								eval_violations = ec;
								eval_cost = ef;
								sel_p = p; sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
							}else if(ec == eval_violations && ef < eval_cost){
								eval_cost = ef;
								sel_p = p; sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
							}
						}
					}
				}
				
			}
		}
	}
	
	@Override
	public void generateInitialSolution(){
		System.out.println(name() + "::generateInitialSolution.....");
		//super.generateInitialSolution();
		//System.exit(-1);
		/*
		for(int i = 0; i < pickupPoints.size(); i++){
			Point pickup = pickupPoints.get(i);
			Point delivery = deliveryPoints.get(i);
			int sel_r = -1;
			Point sel_p = null;
			Point sel_d = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			for(int k = 1; k <= XR.getNbRoutes(); k++){
				for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
					for(Point d = p; d != XR.endPoint(k); d = XR.next(d)){
						double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d);
						double ef = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
						if(ec < eval_violations){
							eval_violations = ec;
							eval_cost = ef;
							sel_p = p; sel_d = d;
						}else if(ec == eval_violations && ef < eval_cost){
							eval_cost = ef;
							sel_p = p; sel_d = d;
						}
					}
				}
			}
			if(sel_p != null && sel_d != null){
				//mgr.performAddTwoPoints(pickup, sel_p, delivery, sel_d);
				mgr.performAddOnePoint(delivery, sel_d);
				mgr.performAddOnePoint(pickup, sel_p);
				System.out.println("init addOnePoint(" + pickup.ID + ","+ sel_p.ID + "), and (" + delivery.ID + "," + sel_d.ID + 
						", XR = " + XR.toString() + ", CS = " + CS.violations() + ", cost = " + cost.getValue());
			}
		}
		*/
	}
	public void search(int maxIter, int timeLimit){
		bestSolution = new ValueRoutesVR(XR);
		currentIter = 0;
		generateInitialSolution();
		nic = 0;
		bestValue = new LexMultiValues(F.getValues());
		updateBest();
	}
}

public class PickupDeliverySolver {
	public static final String module = PickupDeliverySolver.class.getName();
	private PickupDeliveryInput input;
	private PickupDeliveryRequest[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	
	private HashMap<String, Double> mDistance;
	private int N;// pickup points: 0, 1, 2, ..., N-1 delivery points: N,...,N+N-1
	private int M;// start points of vehicles 2N,...,2N+M-1, end points of vehicles are 2N+M,..., 2N+2M-1
	private double[][] dis;// dis[i][j] is the distance from point i to point j
	private double[] cap;// capacity of vehicles
	private HashMap<Integer, Integer> mOrderItem2Request;
	private HashMap<Point, Vehicle> mPoint2Vehicle;
	private HashMap<Point, PickupDeliveryRequest> mPoint2Request;
	private HashMap<Point, String> mPoint2Type;// "S": start, "P": pickup, "D": delivery, "T": terminate 
	
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	private ArrayList<Point> pickupPoints;
	private ArrayList<Point> deliveryPoints;
	HashMap<Point,Point> pickup2DeliveryOfGood = new HashMap<Point, Point>();
	private ArrayList<Point> allPoints;
	private HashMap<Point, Integer> mPoint2Index;
	private HashMap<Point, String> mPoint2LocationCode;
	private HashMap<Point, Double> mPoint2Demand;
	HashMap<Point, Integer> earliestAllowedArrivalTime;
	HashMap<Point, Integer> serviceDuration;
	HashMap<Point, Integer> lastestAllowedArrivalTime;
	private ArcWeightsManager awm;
	private ArcWeightsManager travelTime;
	private NodeWeightsManager nwm;
	private VRManager mgr;
	private VarRoutesVR XR;
	AccumulatedWeightNodesVR awn;
	AccumulatedWeightEdgesVR awe;
	
	private ConstraintSystemVR CS;
	private CEarliestArrivalTimeVR ceat;
	private EarliestArrivalTimeVR eat;
	private IFunctionVR cost;
	private IFunctionVR[] load;
	private LexMultiFunctions F;

	
	private String code(String from, String to){
		return from + "-" + to;
	}
	private void greedyConstructMaintainConstraint(){
		HashSet<Integer> cand = new HashSet<Integer>();
		for(int i = 0; i < pickupPoints.size(); i++) cand.add(i);
		
		while(cand.size() > 0){
			Point sel_pickup = null;
			Point sel_delivery = null;
			double eval_violations = Integer.MAX_VALUE;
			double eval_cost = Integer.MAX_VALUE;
			Point sel_p = null;
			Point sel_d = null;
			int sel_i = -1;
			for(int i : cand){
				Point pickup = pickupPoints.get(i);
				Point delivery = deliveryPoints.get(i);
				for(int k = 1; k <= XR.getNbRoutes(); k++){
					for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
						
						for(Point d = p; d != XR.endPoint(k); d = XR.next(d)){					
							boolean ok = true;
							for(Point tmp = p; tmp != XR.next(d); tmp = XR.next(tmp)){
								if(nwm.getWeight(pickup) + awn.getSumWeights(tmp) > cap[k-1]){
									ok = false; break;
								}
							}
							if(!ok) continue;
							
							double ec = CS.evaluateAddTwoPoints(pickup, p, delivery, d);
							double ef = cost.evaluateAddTwoPoints(pickup, p, delivery, d);
							if(ec < eval_violations){
								eval_violations = ec;
								eval_cost = ef;
								sel_p = p; sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
								sel_i = i;
							}else if(ec == eval_violations && ef < eval_cost){
								eval_cost = ef;
								sel_p = p; sel_d = d;
								sel_pickup = pickup;
								sel_delivery = delivery;
								sel_i = i;
							}
						}
					}
				}
				
			}
			if(sel_i != -1){
				mgr.performAddOnePoint(sel_delivery, sel_d);
				mgr.performAddOnePoint(sel_pickup, sel_p);
				System.out.println("init addOnePoint(" + sel_pickup.ID + ","+ sel_p.ID + "), and (" + sel_delivery.ID + "," + sel_d.ID + 
						", XR = " + XR.toString() + ", CS = " + CS.violations() + ", cost = " + cost.getValue());
				cand.remove(sel_i);
			}
		}
	}

	private void mapData(){
		mDistance = new HashMap<String, Double>();
		for(int i = 0; i < distances.length; i++){
			String src = distances[i].getSrcCode();
			String dest = distances[i].getDestCode();
			mDistance.put(code(src,dest), distances[i].getDistance());
			//System.out.println(module + "::mapData, mDistance.put(" + code(src,dest) + "," + distances[i].getDistance() + ")");
		}
		N = requests.length;
		M = vehicles.length;
		System.out.println(module + "::mapData, requests = " + N + ", vehicles = " + M);
		/*
		dis = new double[2*N+2*M][2*N+2*M];
		for(int i = 0; i < requests.length; i++){
			
			String pi = requests[i].getPickupLocationCode();
			String di = requests[i].getDeliveryLocationCode();
			dis[i][i+N] = mDistance.get(code(pi,di));
			dis[i+N][i] = mDistance.get(code(di,pi));
			for(int j = 0; j < requests.length; j++)if(i != j){
				String pj = requests[j].getPickupLocationCode();
				String dj = requests[j].getDeliveryLocationCode();
				System.out.println(module + "::mapData, code pi = " + pi + ", pj = " + pj);
				dis[i][j] = mDistance.get(code(pi,pj));
				dis[i][j+N] = mDistance.get(code(pi,dj));
				dis[i+N][j] = mDistance.get(code(di,pj));
				dis[i+N][j+N] = mDistance.get(code(di,dj));
			}
			
			for(int j = 0; j < vehicles.length; j++){
				String sj = vehicles[j].getStartLocationCode();
				String tj = vehicles[j].getEndLocationCode();
				int svj = 2*N+j;// start point of vehicle j
				int tvj = 2*N+j+M;// end point of vehicle j
				dis[svj][tvj] = mDistance.get(code(sj,tj));
				dis[tvj][svj] = mDistance.get(code(tj,sj));
				
				dis[i][svj] = mDistance.get(code(pi,sj));
				dis[svj][i] = mDistance.get(code(sj,pi));
				
				dis[i][tvj] = mDistance.get(code(pi,tj));
				dis[tvj][i] = mDistance.get(code(tj,pi));
				
				dis[i+N][svj] = mDistance.get(code(di,sj));
				dis[svj][i+N] = mDistance.get(code(sj,di));
				
				dis[i+N][tvj] = mDistance.get(code(di,tj));
				dis[tvj][i+N] = mDistance.get(code(tj,di));
			}
		}
		*/
		
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		pickupPoints = new ArrayList<Point>();
		deliveryPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		N = 0;
		for(int i = 0; i < requests.length; i++) N += requests[i].getItems().length;
		mPoint2Index = new HashMap<Point, Integer>();
		mPoint2LocationCode = new HashMap<Point, String>();
		mPoint2Demand = new HashMap<Point, Double>();
		mPoint2Vehicle = new HashMap<Point, Vehicle>();
		mPoint2Request = new HashMap<Point, PickupDeliveryRequest>();
		mPoint2Type = new HashMap<Point, String>();
		
		pickup2DeliveryOfGood = new HashMap<Point, Point>();
		earliestAllowedArrivalTime = new HashMap<Point, Integer>();
		lastestAllowedArrivalTime = new HashMap<Point, Integer>();
		serviceDuration = new HashMap<Point, Integer>();

		int idxPoint = -1;
		for(int i = 0; i < requests.length; i++){
			for(int j = 0; j < requests[i].getItems().length; j++){
				idxPoint++;
				Point pickup = new Point(idxPoint);
				pickupPoints.add(pickup);
				mPoint2Index.put(pickup, idxPoint);
				mPoint2LocationCode.put(pickup, requests[i].getPickupLocationCode());
				mPoint2Demand.put(pickup, requests[i].getItems()[j].getWeight());
				mPoint2Request.put(pickup, requests[i]);
				mPoint2Type.put(pickup, "P");
				
				Point delivery = new Point(idxPoint + N);
				deliveryPoints.add(delivery);
				mPoint2Index.put(delivery, idxPoint + N);
				mPoint2LocationCode.put(delivery, requests[i].getDeliveryLocationCode());
				mPoint2Demand.put(delivery, -requests[i].getItems()[j].getWeight());
				mPoint2Request.put(delivery, requests[i]);
				mPoint2Type.put(delivery, "D");
				
				pickup2DeliveryOfGood.put(pickup, delivery);
				allPoints.add(pickup);
				allPoints.add(delivery);
				
				
				earliestAllowedArrivalTime.put(pickup, (int)DateTimeUtils.dateTime2Int(requests[i].getEarlyPickupTime()));
				serviceDuration.put(pickup, 1800);// load-unload is 30 minutes
				lastestAllowedArrivalTime.put(pickup, (int)DateTimeUtils.dateTime2Int(requests[i].getLatePickupTime()));
				earliestAllowedArrivalTime.put(delivery, (int)DateTimeUtils.dateTime2Int(requests[i].getEarlyDeliveryTime()));
				serviceDuration.put(delivery, 1800);// load-unload is 30 minutes
				lastestAllowedArrivalTime.put(delivery, (int)DateTimeUtils.dateTime2Int(requests[i].getLateDeliveryTime()));
			}
		}
		cap = new double[M];
		for(int k = 0; k < M; k++){
			cap[k] = vehicles[k].getWeight();
			
			Point s = new Point(2*N+k);
			Point t = new Point(2*N+M+k);
			mPoint2Index.put(s, s.ID);
			mPoint2Index.put(t, t.ID);
			startPoints.add(s);
			endPoints.add(t);
			mPoint2LocationCode.put(s, vehicles[k].getStartLocationCode());
			mPoint2LocationCode.put(t, vehicles[k].getEndLocationCode());
			mPoint2Demand.put(s, 0.0);
			mPoint2Demand.put(t, 0.0);
			mPoint2Vehicle.put(s,vehicles[k]);
			mPoint2Vehicle.put(t, vehicles[k]);
			
			allPoints.add(s);
			allPoints.add(t);
			
			earliestAllowedArrivalTime.put(s, (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			serviceDuration.put(s, 0);// load-unload is 30 minutes
			lastestAllowedArrivalTime.put(s, (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
			earliestAllowedArrivalTime.put(t, (int)DateTimeUtils.dateTime2Int(vehicles[k].getStartWorkingTime()));
			serviceDuration.put(t,0);// load-unload is 30 minutes
			lastestAllowedArrivalTime.put(t, (int)DateTimeUtils.dateTime2Int(vehicles[k].getEndWorkingTime()));
		}
		
		
		awm = new ArcWeightsManager(allPoints);
		nwm = new NodeWeightsManager(allPoints);
		travelTime = new ArcWeightsManager(allPoints);
		
		for(Point p: allPoints){
			String lp = mPoint2LocationCode.get(p);
			for(Point q: allPoints){
				String lq = mPoint2LocationCode.get(q);
					double d = mDistance.get(code(lp,lq));
					awm.setWeight(p, q, d);
					travelTime.setWeight(p, q, (d*1000)/input.getParams().getAverageSpeed());// meter per second
			}
		}
		for(Point p: allPoints){
			nwm.setWeight(p, mPoint2Demand.get(p));
			System.out.println(module + "::compute, nwm.setWeight(" + p.ID + "," + mPoint2Demand.get(p));
		}
		
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for(int k = 0; k < startPoints.size(); k++){
			XR.addRoute(startPoints.get(k), endPoints.get(k));
		}
		for(Point p: pickupPoints) XR.addClientPoint(p);
		for(Point p: deliveryPoints) XR.addClientPoint(p);
		
		CS = new ConstraintSystemVR(mgr);
		awn = new AccumulatedWeightNodesVR(XR, nwm);
		awe = new AccumulatedWeightEdgesVR(XR, awm);
		eat = new EarliestArrivalTimeVR(XR, travelTime, earliestAllowedArrivalTime, serviceDuration);
		ceat = new CEarliestArrivalTimeVR(eat, lastestAllowedArrivalTime);
		
		//IConstraintVR goodC = new CPickupDeliveryOfGoodVR(XR, pickup2DeliveryOfGood);
		//CS.post(goodC);
		
		//load = new IFunctionVR[XR.getNbRoutes()];
		//IFunctionVR[] routeIndex = new IFunctionVR[allPoints.size()];
		//for(Point p: allPoints){
			//routeIndex[mPoint2Index.get(p)] = new RouteIndex(XR, p);
			//IFunctionVR ld = new AccumulatedNodeWeightsOnPathVR(awn, p);
			//for(int k = 1; k <= M; k++){
			//	CS.post(new Implicate(new Eq(routeIndex[mPoint2Index.get(p)], k), new Leq(ld, cap[k-1])));
			//}
		//}
		
		CS.post(ceat);
		
		cost = new TotalCostVR(XR, awm);
	
		F = new LexMultiFunctions();
		F.add(new ConstraintViolationsVR(CS));
		F.add(cost);
		
		mgr.close();
		
		if(true) return;
		
		ArrayList<INeighborhoodExplorer> NE = new ArrayList<INeighborhoodExplorer>();
		/*
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
		*/
		NE.add(new GreedyTwoPointsMoveExplorer(XR, F));
		NE.add(new GreedyCrossExchangeMoveExplorer(XR, F));
		// NE.add(new GreedyAddOnePointMoveExplorer(XR, F));

		PickupDeliveryTWSearch se = new PickupDeliveryTWSearch(mgr,F,CS,cost,XR,pickupPoints,deliveryPoints);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(100000, input.getParams().getTimeLimit());

		System.out.println("solution XR = " + XR.toString() + ", cost = " + cost.getValue());
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			System.out.println("Route[" + k + "]");
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				Point np = XR.next(p);
				double tt = travelTime.getWeight(p,np);
				long at = (long)eat.getEarliestArrivalTime(p);
				System.out.println("point " + p.getID() + ", demand = " + nwm.getWeight(p) + ", t2n = " + tt + ", eat = " + DateTimeUtils.unixTimeStamp2DateTime(at));
			}
			//System.out.println(", load = " + load[k-1].getValue() + ", cap = " + cap[k-1]);
			System.out.println("------------------------------------");
		}

	}
	private void search(){
		greedyConstructMaintainConstraint();
		
		System.out.println("solution XR = " + XR.toString() + ", cost = " + cost.getValue());
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			System.out.println("Route[" + k + "]");
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				Point np = XR.next(p);
				double tt = travelTime.getWeight(p,np);
				long at = (long)eat.getEarliestArrivalTime(p);
				System.out.println("point " + p.getID() + ", demand = " + nwm.getWeight(p) + ", t2n = " + tt + ", eat = " + DateTimeUtils.unixTimeStamp2DateTime(at));
			}
			//System.out.println(", load = " + load[k-1].getValue() + ", cap = " + cap[k-1]);
			System.out.println("------------------------------------");
		}
	}
	public PickupDeliverySolution compute(PickupDeliveryInput input){
		this.input = input;
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		
		mapData();
		
		search();
		
		int nbr = 0;
		for(int k = 1; k <= XR.getNbRoutes(); k++)
			if(XR.next(XR.startPoint(k)) != XR.endPoint(k)) nbr++;
		
		RoutingSolution[] routes = new RoutingSolution[nbr];
		nbr = -1;
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			if(XR.next(XR.startPoint(k)) != XR.endPoint(k)){
				nbr++;
				ArrayList<RoutingElement> lst = new ArrayList<RoutingElement>();
				double distance = 0;
				for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
					int ip = mPoint2Index.get(p);
					RoutingElement e = null;
					if(p == XR.startPoint(k)){
						// depot
						long at = (long)eat.getEarliestArrivalTime(p);
						if(at < earliestAllowedArrivalTime.get(p)) at = earliestAllowedArrivalTime.get(p);
						long dt = at + serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);
						
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getLat();
						double d_lng = v.getLng();
						e = new RoutingElement(v.getStartLocationCode(), "-", d_lat + "," + d_lng, d_lat, d_lng,"-","-",s_at,s_dt);
						e.setDescription("weight: " + v.getWeight());
					}else if(p == XR.endPoint(k)){
						Vehicle v = mPoint2Vehicle.get(p);
						double d_lat = v.getEndLat();
						double d_lng = v.getEndLng();
						e = new RoutingElement(v.getEndLocationCode(), "-", d_lat + "," + d_lng, d_lat, d_lng,"-","-");
					}else{
						//int ir = mOrderItem2Request.get(ip);
						PickupDeliveryRequest r = mPoint2Request.get(p);
						double lat = r.getDeliveryLat();
						double lng = r.getDeliveryLng();
						String locationCode = r.getDeliveryLocationCode();
						if(mPoint2Type.get(p).equals("P")){
							lat = r.getPickupLat();
							lng = r.getPickupLng();
							locationCode = r.getPickupLocationCode();
						}
						long at = (long)eat.getEarliestArrivalTime(p);
						if(at < earliestAllowedArrivalTime.get(p)) at = earliestAllowedArrivalTime.get(p);
						long dt = at + serviceDuration.get(p);
						String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
						String s_dt = DateTimeUtils.unixTimeStamp2DateTime(dt);
						
						e = new RoutingElement(locationCode, "-", lat + "," + lng,lat,lng, s_at, s_dt);
						//e = new RoutingElement(requests[ir].getOrderID(), "-", lat + "," + lng,lat,lng, s_at, s_dt);
						e.setDescription("orderID: " + r.getOrderID() + ", type = " + mPoint2Type.get(p) + ", amount: " +
						mPoint2Demand.get(p) + ", accumulate load = " + awn.getSumWeights(p));
						e.setOrderId(r.getOrderID());
					}
					distance += awm.getWeight(p, XR.next(p));
					lst.add(e);
				}
				// add terminating point of route
				Point p = XR.endPoint(k);
				long at = (long)eat.getEarliestArrivalTime(p);
				if(at < earliestAllowedArrivalTime.get(p)) at = earliestAllowedArrivalTime.get(p);
				String s_at = DateTimeUtils.unixTimeStamp2DateTime(at);
				
				Vehicle v = mPoint2Vehicle.get(p);
				double d_lat = v.getEndLat();
				double d_lng = v.getEndLng();
				RoutingElement e = new RoutingElement(v.getEndLocationCode(), "-", d_lat + "," + d_lng, d_lat, d_lng, s_at,"-");
				lst.add(e);
				
				RoutingElement[] a_route = new RoutingElement[lst.size()];
				for(int j = 0; j < lst.size(); j++)
					a_route[j] = lst.get(j);
				//routes[nbr] = new RoutingSolution(a_route);
				routes[nbr] = new RoutingSolution(vehicles[k-1], a_route, 0.0, distance);
			}
		}
		for(int i = 0; i < requests.length; i++){
			PickupDeliveryRequest r = requests[i];
			System.out.println("req[" + i + "], pickup = " + r.getPickupLat() + "," + r.getPickupLng() +
					", delivery = " + r.getDeliveryLat() + "," + r.getDeliveryLng());
			
		}
		return new PickupDeliverySolution(routes);

	}
	/*
	// model
	private VRManager mgr;
	private VarRoutesVR XR;
	private ArrayList<Point> starts;
	private ArrayList<Point> ends;
	private ArrayList<Point> pickup;
	private ArrayList<Point> delivery;
	private ArrayList<Point> allPoints;
	private ArrayList<Point> clientPoints;
	private ArcWeightsManager awm;
	private HashMap<Point, String> mPoint2GeoPoint;
	private HashMap<String, Double> mCode2Distance;
	private HashMap<PickupDeliveryRequest, Point> mReq2PickupPoint;
	private HashMap<PickupDeliveryRequest, Point> mReq2DeliveryPoint;
	private HashMap<Point, PickupDeliveryRequest> mPoint2Request;
	private HashMap<Point, String> mPoint2LatLng;
	private HashMap<Point, String> mPoint2Code;
	
	private IFunctionVR obj;
	
	public void greedyConstruct(){
		for(int i = 0; i < requests.length; i++){
			Point p = mReq2PickupPoint.get(requests[i]);
			Point d = mReq2DeliveryPoint.get(requests[i]);
			double eval = Integer.MAX_VALUE;
			Point sel_x = null;
			for(int k = 1; k <= XR.getNbRoutes(); k++){
				for(Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)){
					double e = obj.evaluateAddOnePoint(p, x);
					if(e < eval){
						eval = e; sel_x = x;
					}
				}
			}
			mgr.performAddOnePoint(p, sel_x);
			mgr.performAddOnePoint(d, p);
			
			System.out.println(XR.toString() + ", obj = " + obj.getValue());
		}
	}
	public PickupDeliverySolution compute(PickupDeliveryInput input){
		requests = input.getRequests();
		vehicles = input.getVehicles();
		distances = input.getDistances();
		for(int i = 0; i < requests.length; i++){
			PickupDeliveryRequest r = requests[i];
			System.out.println(r.getOrderID() + ", pickupTime = " + r.getEarlyPickupTime() + " - "
					+ r.getLatePickupTime() + ", deliveryTime = " + r.getEarlyDeliveryTime() + " - " + 
					r.getLateDeliveryTime() + " travel time = " + DateTimeUtils.distanceDateTime(r.getEarlyDeliveryTime(),  r.getEarlyPickupTime()));
		}
		for(int i = 0; i < distances.length; i++){
			System.out.println("distance " + distances[i].getSrcCode() + " --> " + distances[i].getDestCode() + 
					" = " + distances[i].getDistance());
		}
		
		starts = new ArrayList<Point>();
		ends = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		mPoint2GeoPoint = new HashMap<Point, String>();
		mReq2PickupPoint = new HashMap<PickupDeliveryRequest, Point>();
		mReq2DeliveryPoint = new HashMap<PickupDeliveryRequest, Point>();
		mPoint2Request = new HashMap<Point, PickupDeliveryRequest>();
		mPoint2Code = new HashMap<Point, String>();
		mPoint2LatLng = new HashMap<Point, String>();
		
		for(int k = 0; k < vehicles.length; k++){
			Point s = new Point();
			Point t = new Point();
			starts.add(s);
			ends.add(t);
			allPoints.add(s);
			allPoints.add(t);
			mPoint2GeoPoint.put(s, vehicles[k].getStartLocationCode());
			mPoint2GeoPoint.put(t, vehicles[k].getEndLocationCode());
			mPoint2LatLng.put(s, vehicles[k].getLat() + "," + vehicles[k].getLng());
			mPoint2LatLng.put(t, vehicles[k].getEndLat() + "," + vehicles[k].getEndLng());
			mPoint2Code.put(s, vehicles[k].getStartLocationCode());
			mPoint2Code.put(t, vehicles[k].getEndLocationCode());
		}
		for(int i = 0; i < requests.length; i++){
			Point p = new Point();
			Point d = new Point();
			allPoints.add(p);
			allPoints.add(d);
			clientPoints.add(p);
			clientPoints.add(d);
			
			mPoint2GeoPoint.put(p, requests[i].getPickupLocationCode());
			mPoint2GeoPoint.put(d, requests[i].getDeliveryLocationCode());
			mReq2PickupPoint.put(requests[i], p);
			mReq2DeliveryPoint.put(requests[i], d);
			mPoint2Request.put(p, requests[i]);
			mPoint2Request.put(d, requests[i]);
			
			mPoint2LatLng.put(p, requests[i].getPickupLat() + "," + requests[i].getPickupLng());
			mPoint2LatLng.put(d, requests[i].getDeliveryLat() + "," + requests[i].getDeliveryLng());
			mPoint2Code.put(p, vehicles[i].getStartLocationCode());
			mPoint2Code.put(d, vehicles[i].getEndLocationCode());
		}
		mCode2Distance = new HashMap<String, Double>();
		for(int i = 0; i < distances.length; i++){
			String code = distances[i].getSrcCode() + "-" + distances[i].getDestCode();
			mCode2Distance.put(code, distances[i].getDistance());
		}
		awm = new ArcWeightsManager(allPoints);
		for(Point p1: allPoints){
			String src = mPoint2GeoPoint.get(p1);
			for(Point p2: allPoints){
				String dest = mPoint2GeoPoint.get(p2);
				String code = src + "-" + dest;
				double d = mCode2Distance.get(code);
				awm.setWeight(p1, p2, d);
			}
		}
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for(int k = 0; k < starts.size(); k++){
			XR.addRoute(starts.get(k), ends.get(k));
		}
		for(Point p: clientPoints)
			XR.addClientPoint(p);
		
		obj = new TotalCostVR(XR, awm);
		mgr.close();
		
		greedyConstruct();
		
		RoutingSolution[] rs = new RoutingSolution[XR.getNbRoutes()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			ArrayList<Point> P = new ArrayList<Point>();
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				P.add(p);
			}
			P.add(XR.endPoint(k));
			RoutingElement[] re = new RoutingElement[P.size()];
			
			
			for(int i = 0; i < P.size(); i++){
				Point p = P.get(i);
				String code = mPoint2Code.get(p);
				String latlng = mPoint2LatLng.get(p);
				String[] s = latlng.split(",");
				double lat = Double.valueOf(s[0]);
				double lng = Double.valueOf(s[1]);
				re[i] = new RoutingElement(code, "-", latlng,lat,lng);
				System.out.print(code + " -> ");
			}
			System.out.println();
			rs[k-1] = new RoutingSolution(re);
		}

		
		PickupDeliverySolution sol = new PickupDeliverySolution(rs);
		return sol;
	}
	*/
}
