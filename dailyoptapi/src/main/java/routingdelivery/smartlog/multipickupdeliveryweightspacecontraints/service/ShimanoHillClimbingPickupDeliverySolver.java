package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;

public class ShimanoHillClimbingPickupDeliverySolver {
	ShimanoPickupDeliverySolver solver;
	ArrayList<Point> clsPickupPoints = new ArrayList<Point>();// point bieu dien 1 tap cac pickup points cung location
	ArrayList<Point> clsDeliveryPoints = new ArrayList<Point>();
	//HashMap<Point, Integer> mClsPickup2Duration = new HashMap<Point, Integer>();
	//HashMap<Point, Integer> mClsDelivery2Duration = new HashMap<Point, Integer>();
	HashMap<Point, Integer> mClsPoint2Duration = new HashMap<Point, Integer>();
	HashMap<Point, String> mClsPoint2LocationCode = new HashMap<Point, String>();
	HashMap<Point, ArrayList<Point>> mClsPointCluster = new HashMap<Point, ArrayList<Point>>();
	HashMap<Point, Double> mClsPoint2Demand = new HashMap<Point, Double>();
	HashMap<Point, String> mClsPoint2Type = new HashMap<Point, String>();
	HashMap<Point, Integer> mClsPoint2LatestAllowedArrivalTime = new HashMap<Point, Integer>();
	HashMap<Point, Integer> mClsPoint2EarliestAllowedArrivalTime = new HashMap<Point, Integer>();
	ArrayList<Point> starts = new ArrayList<Point>();
	ArrayList<Point> ends = new ArrayList<Point>();
	ArrayList<Point> c_allPoints = new ArrayList<Point>(); 
	ArcWeightsManager clsDistances;
	ArcWeightsManager clsTravelTime;
	
	// modelling
	VRManager c_mgr;
	VarRoutesVR c_XR;
	IFunctionVR c_cost;
	
	
	public ShimanoHillClimbingPickupDeliverySolver(
			ShimanoPickupDeliverySolver solver,
			ArrayList<Point> clsPickupPoints,
			ArrayList<Point> clsDeliveryPoints,
			HashMap<Point, Integer> mClsPoint2Duration,
			HashMap<Point, String> mClsPoint2LocationCode,
			HashMap<Point, ArrayList<Point>> mClsPointCluster,
			HashMap<Point, Double> mClsPoint2Demand,
			HashMap<Point, String> mClsPoint2Type,
			HashMap<Point, Integer> mClsPoint2LatestAllowedArrivalTime,
			HashMap<Point, Integer> mClsPoint2EarliestAllowedArrivalTime,
			ArrayList<Point> starts, ArrayList<Point> ends,
			ArrayList<Point> c_allPoints, ArcWeightsManager clsDistances,
			ArcWeightsManager clsTravelTime, VRManager c_mgr, VarRoutesVR c_XR,
			IFunctionVR c_cost) {
		super();
		this.solver = solver;
		this.clsPickupPoints = clsPickupPoints;
		this.clsDeliveryPoints = clsDeliveryPoints;
		this.mClsPoint2Duration = mClsPoint2Duration;
		this.mClsPoint2LocationCode = mClsPoint2LocationCode;
		this.mClsPointCluster = mClsPointCluster;
		this.mClsPoint2Demand = mClsPoint2Demand;
		this.mClsPoint2Type = mClsPoint2Type;
		this.mClsPoint2LatestAllowedArrivalTime = mClsPoint2LatestAllowedArrivalTime;
		this.mClsPoint2EarliestAllowedArrivalTime = mClsPoint2EarliestAllowedArrivalTime;
		this.starts = starts;
		this.ends = ends;
		this.c_allPoints = c_allPoints;
		this.clsDistances = clsDistances;
		this.clsTravelTime = clsTravelTime;
		this.c_mgr = c_mgr;
		this.c_XR = c_XR;
		this.c_cost = c_cost;
	}
	public void solve(){
		c_mgr = new VRManager();
		c_XR = new VarRoutesVR(c_mgr);
		for(int i = 0; i < starts.size(); i++){
			c_XR.addRoute(starts.get(i), ends.get(i));
		}
		for(int i = 0; i < clsPickupPoints.size(); i++){
			c_XR.addClientPoint(clsPickupPoints.get(i));
			c_XR.addClientPoint(clsDeliveryPoints.get(i));
		}
				
		c_cost = new TotalCostVR(c_XR, clsDistances);
		c_mgr.close();
		
		HashSet<Integer> cand = new HashSet<Integer>();
		for(int i = 0; i < clsPickupPoints.size(); i++)
			cand.add(i);
		while(cand.size() > 0){
			
			//for(int i = 0; i < clsPickupPoints.size(); i++){
			double minEval = Integer.MAX_VALUE;
			int sel_i = -1;
			Point sel_p = null;
			Point sel_d = null;
			for(int i: cand){
				Point cp = clsPickupPoints.get(i);
				Point cd = clsDeliveryPoints.get(i);
				for(int k = 1; k <= c_XR.getNbRoutes(); k++){
					for(Point p = c_XR.startPoint(k); p != c_XR.endPoint(k); p = c_XR.next(p)){
						for(Point d = p; d != c_XR.endPoint(k); d = c_XR.next(d)){
							if(!c_XR.performAddOnePoint(cd, d)) return;
							if(!c_XR.performAddOnePoint(cp, p)) return;
							boolean ok = checkFeasibility();
							double eval = c_cost.getValue();
							//double eval = c_cost.evaluateAddTwoPoints(cp, p, cd, d);
							if(ok){
								if(eval < minEval){
									minEval = eval;
									sel_i = i;
									sel_p = p;
									sel_d = d;
								}
							}
							if(!c_XR.performRemoveOnePoint(cp)) return;
							if(!c_XR.performRemoveOnePoint(cd)) return;
						}
					}
				}
			}
			if(sel_i == -1){
				solver.log(name() + "::solve, cannot complete the solution construction, BUG?????");
				System.out.println(name() + "::solve, cannot complete the solution construction, BUG?????");
				break;
			}
			
			cand.remove(sel_i);
			Point sel_pickup = clsPickupPoints.get(sel_i);
			Point sel_delivery = clsDeliveryPoints.get(sel_i);
			
			c_mgr.performAddOnePoint(sel_delivery, sel_d);
			c_mgr.performAddOnePoint(sel_pickup, sel_p);
			
			solver.log(name() + "::mergeTripsGreedy, construct add points, c_XR = " + solver.toStringShort(c_XR,mClsPoint2LocationCode,mClsPoint2Type));
			System.out.println(name() + "::mergeTripsGreedy, construct add points, c_XR = " + solver.toStringShort(c_XR,mClsPoint2LocationCode,mClsPoint2Type));
		}
		
		improveLS();
		//improveDeliveryPickupSameLocation();
	}
	
	public boolean improveLS(){
		boolean hasChanged = false;
		while(true){
			Point sel_pickup = null;
			Point sel_delivery = null;
			Point sel_p = null;
			Point sel_d = null;
			double currentCost = c_cost.getValue();
			double minEval = Integer.MAX_VALUE;
			for(int i = 0; i < clsPickupPoints.size(); i++){
				Point pickup = clsPickupPoints.get(i);
				Point delivery = clsDeliveryPoints.get(i);
				Point prev_pickup = c_XR.prev(pickup);
				Point prev_delivery = c_XR.prev(delivery);
				for(int k = 1; k <= c_XR.getNbRoutes(); k++){
					for(Point p = c_XR.startPoint(k); p != c_XR.endPoint(k); p = c_XR.next(p)){
						for(Point d = p; d != c_XR.endPoint(k); d = c_XR.next(d)){
							//System.out.println(name() + "::improveLS, pickup = " + pickup.ID + ", delivery = " + delivery.ID
							//		+ ", p = " + p.ID + ", d = " + d.ID + ", prev_pickup = " + 
							//		prev_pickup.ID + ", prev_delivery = " + prev_delivery.ID);
							if(p == pickup || p == delivery || d == pickup || d == delivery) continue;
							if(!c_mgr.performRemoveOnePoint(pickup)) return hasChanged;
							if(!c_mgr.performRemoveOnePoint(delivery))return hasChanged;
							//System.out.println(name() + "::improveLS remove pickup, delivery OK");
							
							if(!c_mgr.performAddOnePoint(delivery, d))return hasChanged;
							//System.out.println(name() + "::improveLS add (pickup,p) OK");
							if(!c_mgr.performAddOnePoint(pickup, p)) return hasChanged;
							//System.out.println(name() + "::improveLS add (delivery,d) OK");
							
							boolean ok = checkFeasibility();
							if(ok && c_cost.getValue() < currentCost){
								if(minEval > c_cost.getValue()){
									minEval = c_cost.getValue();
									sel_pickup = pickup; sel_delivery = delivery;
									sel_p = p; sel_d = d;
								}
							}
							if(!c_mgr.performRemoveOnePoint(pickup)) return hasChanged;
							if(!c_mgr.performRemoveOnePoint(delivery)) return hasChanged;
							
							if(!c_mgr.performAddOnePoint(pickup, prev_pickup)) return hasChanged;
							//System.out.println(name() + "::improveLS add (pickup,prev_pickup) OK");
							
							if(!c_mgr.performAddOnePoint(delivery, prev_delivery)) return hasChanged;
							//System.out.println(name() + "::improveLS add (delivery,prev_delivery) OK");
							
						}
					}
				}
			}
			if(sel_pickup != null){
				if(!c_mgr.performRemoveOnePoint(sel_pickup)) return hasChanged;
				if(!c_mgr.performRemoveOnePoint(sel_delivery)) return hasChanged;
				if(!c_mgr.performAddOnePoint(sel_delivery, sel_d)) return hasChanged;
				//System.out.println(name() + "::improveLS add (sel_delivery,sel_d) OK");
				if(!c_mgr.performAddOnePoint(sel_pickup, sel_p)) return hasChanged;
				//System.out.println(name() + "::improveLS add (sel_pickup,sel_p) OK");
				System.out.println(name() + "::improveLS, IMPROVE new cost = " + c_cost.getValue() + " c_XR = " + solver.toStringShort(c_XR,mClsPoint2LocationCode,mClsPoint2Type));
				hasChanged = true;
			}else{
				System.out.println(name() + "::improveLS BREAK");
				break;
			}
		}
		return hasChanged;
	}
	
	public boolean improveDeliveryPickupSameLocation(){
		System.out.println(name() + "::improveDeliveryPickupSameLocation, current cost = " + c_cost.getValue());
		boolean hasChanged = false;
		while(true){
			Point sel_pickup = null;
			Point sel_delivery = null;
			Point sel_p = null;
			Point sel_d = null;
			double currentCost = c_cost.getValue();
			double minEval = Integer.MAX_VALUE;
			for(int i = 0; i < clsPickupPoints.size(); i++){
				Point pickup = clsPickupPoints.get(i);
				Point delivery = clsDeliveryPoints.get(i);
				Point prev_pickup = c_XR.prev(pickup);
				Point prev_delivery = c_XR.prev(delivery);
				for(int k = 1; k <= c_XR.getNbRoutes(); k++){
					for(Point p = c_XR.startPoint(k); p != c_XR.endPoint(k); p = c_XR.next(p)){
						for(Point d = p; d != c_XR.endPoint(k); d = c_XR.next(d)){
							System.out.println(name() + "::improveLS, pickup = " + pickup.ID + ", delivery = " + delivery.ID
									+ ", p = " + p.ID + ", d = " + d.ID + ", prev_pickup = " + 
									prev_pickup.ID + ", prev_delivery = " + prev_delivery.ID);
							if(p == pickup || p == delivery || d == pickup || d == delivery) continue;
							if(!mClsPoint2Type.get(p).equals("D")) continue;
							if(!mClsPoint2LocationCode.get(pickup).equals(mClsPoint2LocationCode.get(p))) continue;
							
							if(!c_mgr.performRemoveOnePoint(pickup)) return hasChanged;
							if(!c_mgr.performRemoveOnePoint(delivery))return hasChanged;
							//System.out.println(name() + "::improveLS remove pickup, delivery OK");
							
							if(!c_mgr.performAddOnePoint(delivery, d))return hasChanged;
							System.out.println(name() + "::improveLS add (pickup,p) OK");
							if(!c_mgr.performAddOnePoint(pickup, p)) return hasChanged;
							System.out.println(name() + "::improveLS add (delivery,d) OK");
							
							boolean ok = checkFeasibility();
							System.out.println(name() + "::improveDeliveryPickupSameLocation, ok = " + ok + ", cost = " + c_cost.getValue());
							if(ok ){
									//&& c_cost.getValue() < currentCost){
								if(minEval > c_cost.getValue()){
									minEval = c_cost.getValue();
									sel_pickup = pickup; sel_delivery = delivery;
									sel_p = p; sel_d = d;
								}
							}
							if(!c_mgr.performRemoveOnePoint(pickup)) return hasChanged;
							if(!c_mgr.performRemoveOnePoint(delivery)) return hasChanged;
							
							if(!c_mgr.performAddOnePoint(pickup, prev_pickup)) return hasChanged;
							System.out.println(name() + "::improveLS add (pickup,prev_pickup) OK");
							
							if(!c_mgr.performAddOnePoint(delivery, prev_delivery)) return hasChanged;
							System.out.println(name() + "::improveLS add (delivery,prev_delivery) OK");
							
						}
					}
				}
			}
			if(sel_pickup != null){
				if(!c_mgr.performRemoveOnePoint(sel_pickup)) return hasChanged;
				if(!c_mgr.performRemoveOnePoint(sel_delivery)) return hasChanged;
				if(!c_mgr.performAddOnePoint(sel_delivery, sel_d)) return hasChanged;
				System.out.println(name() + "::improveLS add (sel_delivery,sel_d) OK");
				if(!c_mgr.performAddOnePoint(sel_pickup, sel_p)) return hasChanged;
				System.out.println(name() + "::improveLS add (sel_pickup,sel_p) OK");
				System.out.println(name() + "::improveLS, IMPROVE new cost = " + c_cost.getValue());
				hasChanged = true;
				return hasChanged;
			}else{
				System.out.println(name() + "::improveLS BREAK");
				break;
			}
		}
		return hasChanged;
	}

	public boolean checkFeasibility(){
		for(int k = 1; k <= c_XR.getNbRoutes(); k++){
			if(c_XR.emptyRoute(k)) continue;
			Point s  = c_XR.startPoint(k);
			Point lastPoint = s;
			int arrivalTime = mClsPoint2EarliestAllowedArrivalTime.get(s);
			int serviceTime = arrivalTime;
			int departureTime = arrivalTime;
			double load = 0;
			double cap = mClsPoint2Demand.get(s);
			for(Point p = c_XR.next(s); p != c_XR.endPoint(k); p = c_XR.next(p)){
				arrivalTime = departureTime + solver.getTravelTime(mClsPoint2LocationCode.get(lastPoint), 
						mClsPoint2LocationCode.get(p));
				if(arrivalTime > mClsPoint2LatestAllowedArrivalTime.get(p)) return false;
				load += mClsPoint2Demand.get(p);
				if(load > cap) return false;
				
				serviceTime = arrivalTime;
				if(serviceTime < mClsPoint2EarliestAllowedArrivalTime.get(p))
					serviceTime = mClsPoint2EarliestAllowedArrivalTime.get(p);
				departureTime = serviceTime + mClsPoint2Duration.get(p);
				lastPoint = p;
			}
		}
		return true;
	}
	public VarRoutesVR getVarRoutesVR(){
		return c_XR;
	}
	public String name(){
		return "ShimanoHillClimbingPickupDeliverySolver";
	}
}
