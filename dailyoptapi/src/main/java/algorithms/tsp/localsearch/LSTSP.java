package algorithms.tsp.localsearch;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.TotalCostVR;
import localsearch.domainspecific.vehiclerouting.vrp.search.GenericLocalSearch;

import java.util.*;
public class LSTSP {
	private int maxIter;
	private int maxTime;
	private double[][] D;
	private int n;
	
	private ArrayList<Point> clientPoints;
	private ArrayList<Point> allPoints;
	private ArrayList<Point> startPoints;
	private ArrayList<Point> endPoints;
	
	private VRManager mgr;
	private VarRoutesVR XR;
	private IFunctionVR totalCost;
	
	public void solve(double[][] D, int maxIter, int maxTime){
		this.D = D;
		this.maxIter = maxIter;
		this.maxTime = maxTime;
		n = D.length;
	}
	public void stateMode(){
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		allPoints = new ArrayList<Point>();
		Point s = new Point(0);
		Point t = new Point(n-1);
		allPoints.add(s); allPoints.add(t);
		for(int i = 1; i <= n-2; i++){
			Point p = new Point(i);
			clientPoints.add(p);
			allPoints.add(p);
		}
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				Point pi = allPoints.get(i);
				Point pj = allPoints.get(j);
				awm.setWeight(pi, pj, D[i][j]);
			}
		}
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		XR.addRoute(s, t);
		for(Point p: clientPoints) XR.addClientPoint(p);
		totalCost = new TotalCostVR(XR, awm);
		mgr.close();
		
		
	}
	public void search(){
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
