package routingdelivery.utils.clustering;

import localsearch.domainspecific.vehiclerouting.vrp.ConstraintSystemVR;
import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.constraints.leq.Leq;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.Point;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedEdgeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.AccumulatedNodeWeightsOnPathVR;
import localsearch.domainspecific.vehiclerouting.vrp.functions.LexMultiFunctions;
import localsearch.domainspecific.vehiclerouting.vrp.functions.MaxVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;
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

import java.util.*;
public class VRPBasedClustering {
	
	public Cluster[] cluster(double[][] d, int sz){
		// n = d.length
		// item 0 is the depot
		// clients are items 1, 2, ..., n-1
		// cluster items 1, ..., n-1 into clusters of size at most sz
		int n = d.length;
		int K = n/sz;
		if(n%sz > 0) K = K + 1;
		Cluster[] S = new Cluster[K];
		
		ArrayList<Point> startPoints = new ArrayList<Point>();
		ArrayList<Point> endPoints = new ArrayList<Point>();
		ArrayList<Point> clientPoints = new ArrayList<Point>();
		ArrayList<Point> allPoints = new ArrayList<Point>();
		HashMap<Point, Integer> mPoint2Index = new HashMap<Point, Integer>();
		
		for(int k = 0; k < K; k++){
			Point s = new Point(0);
			startPoints.add(s);
			allPoints.add(s);
			mPoint2Index.put(s,0);
			
			Point t = new Point(0);
			endPoints.add(t);
			allPoints.add(t);
			mPoint2Index.put(t, 0);
		}
		for(int i = 1; i < n; i++){
			Point p = new Point(i);
			clientPoints.add(p);
			allPoints.add(p);
			mPoint2Index.put(p, i);
		}
		ArcWeightsManager awm = new ArcWeightsManager(allPoints);
		for(Point p1: allPoints){
			int i = mPoint2Index.get(p1);
			for(Point p2: allPoints){
				int j = mPoint2Index.get(p2);
				awm.setWeight(p1, p2, d[i][j]);
			}
		}
		
		NodeWeightsManager nwm = new NodeWeightsManager(allPoints);
		for(Point p: allPoints) nwm.setWeight(p, 1);
		
		VRManager mgr = new VRManager();
		VarRoutesVR XR = new VarRoutesVR(mgr);
		for(int k = 1; k <= K; k++){
			XR.addRoute(startPoints.get(k-1), endPoints.get(k-1));
		}
		for(Point p: clientPoints) XR.addClientPoint(p);

		AccumulatedWeightEdgesVR awe = new AccumulatedWeightEdgesVR(XR, awm);
		AccumulatedWeightNodesVR awn = new AccumulatedWeightNodesVR(XR, nwm);
		
		ConstraintSystemVR CS = new ConstraintSystemVR(mgr);
		
		IFunctionVR[] dis = new IFunctionVR[K];
		
		for(int k = 1; k <= K;k++){
			Point e = XR.endPoint(k);
			AccumulatedNodeWeightsOnPathVR load = new AccumulatedNodeWeightsOnPathVR(awn, e);
			CS.post(new Leq(load, sz));
		
			dis[k-1] = new AccumulatedEdgeWeightsOnPathVR(awe, e);
		}
		
		IFunctionVR obj = new MaxVR(dis);
		
		LexMultiFunctions F = new LexMultiFunctions();
		F.add(CS);
		F.add(obj);
		
		mgr.close();
		
		GenericLocalSearch se = new GenericLocalSearch(mgr);
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
		NE.add(new GreedyCrossExchangeMoveExplorer(XR, F));

		se.setNeighborhoodExplorer(NE);
		se.setObjectiveFunction(F);
		se.search(100000, 10);
		
		for(int k = 1; k <= K; k++){
			S[k-1] = new Cluster();
			for(Point p = XR.next(XR.startPoint(k)); p != XR.endPoint(k); p = XR.next(p)){
				int i = mPoint2Index.get(p);
				S[k-1].add(i);
			}
		}
		
		return S;
	}	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
