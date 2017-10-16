package com.havestplanning.solver.multistepsplitfield;

import java.util.HashSet;

public class DisplacementConstraintedMultiKnapsackSolver {

	// input data structures
	private int n;// number of items
	private int m;// number of bins
	private int[] min_load;// min_load[i] is the minimum load allowed of  bin i
	private int[] max_load;// max_load[i] is the maximum load allowed of bin i
	private int[] w;// w[i] is the weight of item i
	private HashSet<Integer>[] D; // D[i] is the set of positions where item i can be placed
	private int[][] score;// score[i][j] is the score of item i when placed at position j
	private HashSet<Integer> X;// set of variables whose domain is not empty
	private int[] pre_load;// pre_load[i] is the predefined load of bin i
	
	// decision variable
	private int[] x; // x[i] is the position (0,...,m-1) where item i is placed, domain of x[i] is D[i]
	
	// invariants
	private int[] load;// load[i] is the load of bin i
	private int[] violations;// violations[i] = violations of bin i
	private int total_violations;
	public int violations(){
		return total_violations;
	}
	
	
	public int violations(int v, int min, int max){
		if(v == 0) return 0;
		if(v < min) return min - v;
		if(v > max) return v - max;
		return 0;
	}
	public void initPropagate(){
		for(int i = 0; i < m; i++) load[i] = 0;
		for(int i = 0;i < n; i++) load[i] += w[x[i]];
		for(int i = 0; i < m; i++){
			violations[i] = violations(load[i],min_load[i],max_load[i]);
			total_violations += violations[i];
		}		
	}
	
	public void initGreedy(){
		for(int i = 0; i < n; i++){
			int max_score = 1-Integer.MAX_VALUE;
			int sel_p = -1;
			for(int p: D[i]){
				if(score[i][p] > max_score){
					max_score = score[i][p];
					sel_p = p;
				}
			}
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
