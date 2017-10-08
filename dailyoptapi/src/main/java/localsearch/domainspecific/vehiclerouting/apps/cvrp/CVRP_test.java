package localsearch.domainspecific.vehiclerouting.apps.cvrp;

import java.util.ArrayList;
import java.util.HashMap;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;

public class CVRP_test {

	VarRoutesVR XR;
	VRManager mgr;
	ArrayList<Point> startPoints;
	ArrayList<Point> endPoints;
	ArrayList<Point> clientPoints;

	int K;// number of vehicles;
	int N;// number of clients;
	ArcWeightsManager awm;
	IFunctionVR[] cost;
	public void initData(){
		K = 2;
		N = 10;
		double[][] d = new double[][]{
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1},
				{1,1,1,1,1,1,1,1,1,1,1,1}
		};
		startPoints = new ArrayList<Point>();
		endPoints = new ArrayList<Point>();
		clientPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		//HashMap<Point, Integer> mPoint2ID = new HashMap<Point, Integer>();
		
		for(int k = 1; k <= K; k++){
			Point s = new Point(10+k);
			Point t = new Point(20+k);
			//mPoint2ID.put(s, 0);
			//mPoint2ID.put(t, 0);
			startPoints.add(s);
			endPoints.add(t);
			allPoints.add(s);
			allPoints.add(t);
		}
		for(int i = 0; i < N; i++){
			Point p = new Point(i+1);
			//mPoint2ID.put(p, i+1);
			clientPoints.add(p);
			allPoints.add(p);
		}
		awm = new ArcWeightsManager(allPoints);
		for(Point p1: clientPoints){
			for(Point p2: clientPoints)
				awm.setWeight(p1, p2, d[p1.ID][p2.ID]);
		}
		
	}
	public void stateModel(){
		mgr = new VRManager();
		XR = new VarRoutesVR(mgr);
		for(int k = 0; k < K; k++){
			XR.addRoute(startPoints.get(k),endPoints.get(k));
		}
		for(int i = 0; i < clientPoints.size(); i++)
			XR.addClientPoint(clientPoints.get(i));
		
		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		cost = new IFunctionVR[K];
		for(int k = 1; k <= K; k++){
			cost[k-1] = new AccumulatedEdgeWeightsOnPathVR(awe, XR.endPoint(k));
		}
		mgr.close();
	}
	public Point clientPoint(int ID){
		return clientPoints.get(ID-1);
	}
	public void print(){
		System.out.println(XR);
//		for(int k = 1; k <= K; k++){
//			System.out.println(cost[k-1].getValue());
//		}
		
	}
	public void search(){
		mgr.performAddOnePoint(clientPoint(1), XR.startPoint(1));
		mgr.performAddOnePoint(clientPoint(2), clientPoint(1));
		mgr.performAddOnePoint(clientPoint(3), clientPoint(2));
		mgr.performAddOnePoint(clientPoint(4), clientPoint(3));
		mgr.performAddOnePoint(clientPoint(5), clientPoint(4));
		//mgr.performAddOnePoint(clientPoint(6), clientPoint(5));
		
		mgr.performAddOnePoint(clientPoint(6), XR.startPoint(2));
		mgr.performAddOnePoint(clientPoint(7), clientPoint(6));
		mgr.performAddOnePoint(clientPoint(8), clientPoint(7));
		mgr.performAddOnePoint(clientPoint(9), clientPoint(8));
		mgr.performAddOnePoint(clientPoint(10), clientPoint(9));
		
		//XR.setRandom();
		print();
		//mgr.performOnePointMove(clientPoint(1), clientPoint(4));
		//mgr.performTwoOptMove1(clientPoint(2), clientPoint(6));
		//mgr.performTwoOptMove2(clientPoint(2), clientPoint(6));
		//mgr.performTwoOptMove3(clientPoint(2), clientPoint(6));
		mgr.performCrossExchangeMove(clientPoint(2), clientPoint(4), clientPoint(7), clientPoint(9));
		
		print();
		
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		CVRP_test cvrp = new CVRP_test();
		cvrp.initData();
		cvrp.stateModel();
		cvrp.search();
	}

}
