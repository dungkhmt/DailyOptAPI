package havestplanning.solver;

import havestplanning.model.FieldCluster;

import localsearch.constraints.basic.LessOrEqual;
import localsearch.functions.conditionalsum.ConditionalSum;
import localsearch.model.*;
import localsearch.search.TabuSearch;

public class MultiKnapsackSolver {
	LocalSearchManager mgr;
	VarIntLS[] x;
	ConstraintSystem S;
	IFunction[] load;
	int W;
	
	public String name(){
		return "MultiKnapsackSolver";
	}
	public FieldCluster[] solve(int[] w, int minCapacity, int maxCapacity){
		int nbBins = 0;
		W = 0;
		for(int i = 0; i < w.length; i++) W += w[i];
		
		nbBins = W/maxCapacity;
		if(W % maxCapacity != 0) nbBins++;
		
		System.out.println(name() + "::solve, nbBins = " + nbBins + ", W = " + W + ", number items = " + w.length + 
				", minCapacity = " + minCapacity + ", maxCapacity = " + maxCapacity);
		
		mgr = new LocalSearchManager();
		x = new VarIntLS[w.length];
		S = new ConstraintSystem(mgr);
		for(int i = 0; i < x.length; i++){
			x[i] = new VarIntLS(mgr,0,nbBins-1);
		}
		load = new IFunction[nbBins];
		
		for(int i = 0; i < nbBins; i++){
			ConditionalSum y = new ConditionalSum(x, w, i);
			load[i] = y;
			S.post(new LessOrEqual(y, maxCapacity));
			S.post(new LessOrEqual(minCapacity, y));
		}
		mgr.close();
		//for(int i = 0; i < x.length; i++){
		//	System.out.print(x[i].getValue() + " ");
		//}
		//System.out.println("Init S = " + S.violations());
		
		TabuSearch ts = new TabuSearch();
		ts.search(S, 20, 5, 10000, 200);
		FieldCluster[] clusters = new FieldCluster[nbBins];
		for(int i = 0; i < nbBins; i++){
			clusters[i] = new FieldCluster();
			for(int j = 0; j < x.length; j++){
				
				if(x[j].getValue() == i)
					clusters[i].add(j);
			}
			clusters[i].setWeight(load[i].getValue());
		}
		return clusters;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] w = new int[]{1,2,3,3,4,5,5,6,7};
		int minC = 8; 
		int maxC = 15;
		
		MultiKnapsackSolver S = new MultiKnapsackSolver();
		S.solve(w, minC, maxC);
		
	}

}
