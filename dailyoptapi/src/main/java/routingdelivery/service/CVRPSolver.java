package routingdelivery.service;

import routingdelivery.model.Depot;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.Request;
import routingdelivery.model.RoutingDeliveryMultiDepotInput;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingLoad3DInput;
import routingdelivery.model.RoutingLoad3DSolution;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;

import java.util.*;


import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
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

class CVRPSearch extends GenericLocalSearch{
	public CVRPSearch(VRManager mgr){
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

public class CVRPSolver {
	public static final String module = CVRPSolver.class.getName();
	private RoutingDeliveryMultiDepotInput input;
	
	
	public RoutingLoad3DSolution solve(RoutingDeliveryMultiDepotInput input){
		this.input = input;
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		
		Depot[] depots = input.getDepots();
		ArrayList<Vehicle> lstVehicles = new ArrayList<Vehicle>();
		for(int i = 0; i < depots.length; i++){
			Vehicle[] v = depots[i].getVehicles();
			for(int j = 0; j < v.length; j++)
			lstVehicles.add(v[j]);
		}
		Vehicle[] vehicles = new Vehicle[lstVehicles.size()];
		for(int i = 0; i < lstVehicles.size(); i++){
			vehicles[i] = lstVehicles.get(i);
		}
		Request[] req = input.getRequests();
		Map<String, Point> mCode2Point = new HashMap<String, Point>();
		Map<Point, String> mPoint2LatLng = new HashMap<Point, String>();
		Map<Point, String> mPoint2Code = new HashMap<Point, String>();
		
		for(int i = 0; i < vehicles.length; i++){
			Point s = new Point(0);
			startPoints.add(s);
			Point t = new Point(0);
			endPoints.add(t);
			
			allPoints.add(s);
			allPoints.add(t);
			
			mCode2Point.put(vehicles[i].getCode(), s);
			mPoint2Code.put(s, vehicles[i].getCode());
			mPoint2Code.put(t, vehicles[i].getCode());
			mPoint2LatLng.put(s, vehicles[i].getLat() + "," + vehicles[i].getLng());
			mPoint2LatLng.put(t, vehicles[i].getLat() + "," + vehicles[i].getLng());
		}
		for(int i = 0; i < req.length; i++){
			Point p = new Point(i+1);
			clientPoints.add(p);
			allPoints.add(p);
			mCode2Point.put(req[i].getOrderID(), p);
			mPoint2Code.put(p, req[i].getOrderID());
			mPoint2LatLng.put(p, req[i].getLat() + "," + req[i].getLng());
		}
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(int i = 0; i < input.getDistances().length; i++){
			DistanceElement de = input.getDistances()[i];
			Point p1 = mCode2Point.get(de.getSrcCode());
			Point p2 = mCode2Point.get(de.getDestCode());
			System.out.println(module + "::solve, de, src = " + de.getSrcCode() + ", dest = " + de.getDestCode() + 
					", p1 = " + p1 + ", p2 = " + p2);
			double d = de.getDistance();
			awm.setWeight(p1, p2, d);
		}
		for(int i = 0; i < startPoints.size(); i++){
			Point t = endPoints.get(i);
			Point s = startPoints.get(i);
			for(Point p: clientPoints){
				awm.setWeight(t, p, awm.getWeight(s, p));
				awm.setWeight(p, t, awm.getWeight(p, t));
			}
			for(Point si: startPoints){
				awm.setWeight(t, si, 0);
				awm.setWeight(si, t, 0);
			}
		}
		
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		for(int i = 0; i < startPoints.size(); i++){
			XR.addRoute(startPoints.get(i), endPoints.get(i));
		}
		for(Point p: clientPoints)
			XR.addClientPoint(p);
		
		IFunctionVR cost = new TotalCostVR(XR, awm);
		
		LexMultiFunctions F = new LexMultiFunctions();
		
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

		CVRPSearch se = new CVRPSearch(mgr);
		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.setMaxStable(50);

		se.search(1000, 100);

		/*
		for(Point p: clientPoints){
			double min = Integer.MAX_VALUE;
			Point sel_x = null;
			for(int k = 1; k <= XR.getNbRoutes(); k++){
				for(Point x = XR.startPoint(k); x != XR.endPoint(k); x = XR.next(x)){
					double e = cost.evaluateAddOnePoint(p, x);
					if(e < min){
						min = e;sel_x = x;
					}
				}
			}
			mgr.performAddOnePoint(p,sel_x);
			System.out.println(module + "::solve, XR = " + XR.toString() + ", cost = " + cost.getValue());
		}
		*/
		
		RoutingSolution[] rs = new RoutingSolution[XR.getNbRoutes()];
		for(int k = 1; k <= XR.getNbRoutes(); k++){
			ArrayList<Point> P = new ArrayList<Point>();
			for(Point p = XR.startPoint(k); p != XR.endPoint(k); p = XR.next(p)){
				P.add(p);
			}
			P.add(XR.endPoint(k));
			RoutingElement[] re = new RoutingElement[P.size()];
			System.out.print(module + "::solve, route[" + k + "] = ");
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
		return new RoutingLoad3DSolution(rs, null);
	}
}
