package routingdelivery.service;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.model.RoutingElement;
import routingdelivery.model.RoutingSolution;
import routingdelivery.model.Vehicle;
import utils.DateTimeUtils;

public class PickupDeliverySolver {
	public static final String module = PickupDeliverySolver.class.getName();
	
	private PickupDeliveryRequest[] requests;
	private Vehicle[] vehicles;
	private DistanceElement[] distances;
	
	private HashMap<String, Double> mDistance;
	private int N;// pickup points: 0, 1, 2, ..., N-1 delivery points: N,...,N+N-1
	private int M;// start points of vehicles 2N,...,2N+M-1, end points of vehicles are 2N+M,..., 2N+2M-1
	private double[][] dis;// dis[i][j] is the distance from point i to point j
	
	
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
		N = requests.length;
		M = vehicles.length;
		System.out.println(module + "::mapData, requests = " + N + ", vehicles = " + M);
		
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
		
		
	}
	public PickupDeliverySolution compute(PickupDeliveryInput input){
		this.requests = input.getRequests();
		this.vehicles = input.getVehicles();
		this.distances = input.getDistances();
		
		mapData();
		
		return null;
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
