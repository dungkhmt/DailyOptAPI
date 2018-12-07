package com.havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;

import localsearch.constraints.multiknapsack.MultiKnapsack;
import localsearch.model.ConstraintSystem;
import localsearch.model.LocalSearchManager;
import localsearch.model.VarIntLS;
import localsearch.search.TabuSearch;

public class GreedyConstrainedMultiKnapsackSolver extends ConstrainedMultiKnapsackSolver{

	public GreedyConstrainedMultiKnapsackSolver(SolverMultiStepSplitFields solver) {
		//this.solver = solver;
		super(solver);
	}


	public int violations(int q){
		if(minLoad <= q && q <= maxLoad) return 0;
		if(q < minLoad) return minLoad - q;
		return q - maxLoad;
	}
	public void search(int startDatePlan){
		// greedy constructive
		int aq = 0;
		HashSet<Integer> C = new HashSet<Integer>();
		for(int i = 0; i < n; i++) C.add(i);
		int step = 1;
		while(true){
			int min_d = Integer.MAX_VALUE;
			int max_q = -1;
			int min_violations = Integer.MAX_VALUE;
			int sel_i = -1;
			for(int i: C){
				if(expectedHavestDate[i] < min_d){
					min_d = expectedHavestDate[i];
					max_q = qtt[i];
					//min_violations = violations(aq + qtt[i]);
					sel_i = i;
				}else if(min_d == expectedHavestDate[i]){
					if(qtt[i] > maxLoad){
						sel_i = i;
					}else{
						if(violations(aq+qtt[i]) < min_violations){
							sel_i = i;
							min_violations = violations(aq+qtt[i]); 
						}else if(violations(aq + qtt[i]) == min_violations){
							if(max_q < qtt[i]){
								max_q = qtt[i];
								sel_i = i;
							}
						}
					}
				}
			}
			if(sel_i == -1) break;
			aq += qtt[sel_i];
			x[sel_i] = startDatePlan;
			C.remove(sel_i);
			
			System.out.println("step " + step + " C = " + C.size() + " -> admit field " + sel_i + ", aq = " + aq);
			
			step++;
			if(aq > minLoad && min_violations == 0){
				if(C.size() == 0) break;
				startDatePlan++;
				aq = 0;
				System.out.println("------------------date " + startDatePlan + "----------------");
				
			}
		}
	}
	public LeveledHavestPlanSolution solve(int[] preload, int[] qtt,
			int[] minDate, int[] maxDate, int[] expected_date, int minLoad,
			int maxLoad, int startDatePlan, int timeLimit) {

		/*
		 * m = preload.length: number of bins (days), bins are numbered 0, 1,
		 * ..., m-1 n = qtt.length: number of items (fields), items are numbered
		 * 0, 1, ..., n-1 preload[i]: pre-load of bin i qtt[j]: quantity
		 * (weight) of item j minDate[j], maxDate[j]: item j can be loaded in
		 * bins minDate[j],...,maxDate[j] The total load of a bin is between
		 * [minLoad..maxLoad] or the load of a bin is 0 Bin i s.t. load[i] =
		 * maxLoad is forbidden to load other items objective: allocate each
		 * item i in a bin from {minDate[j],...,maxDate[j]} minimizing
		 * violations
		 */

		this.preload = preload;
		this.qtt = qtt;
		this.minDate = minDate;
		this.maxDate = maxDate;
		this.minLoad = minLoad;
		this.maxLoad = maxLoad;
		this.m = preload.length;
		this.n = qtt.length;

		expectedHavestDate = expected_date;
		/*
		 * expectedHavestDate = new int[minDate.length]; for (int i = 0; i <
		 * expectedHavestDate.length; i++) expectedHavestDate[i] = (minDate[i] +
		 * maxDate[i]) / 2;
		 */

		startDate = 10000000;
		endDate = -10000000;
		int max_range = 0;
		for (int i = 0; i < n; i++) {
			if (startDate > minDate[i])
				startDate = minDate[i];
			if (endDate < maxDate[i])
				endDate = maxDate[i];
			if(max_range < maxDate[i] - minDate[i]) max_range = maxDate[i] - minDate[i];
		}
		R = new Random();

		System.out
				.println(name() + "::solve start.... n = " + n + ", m = " + m + ", max_range = " + max_range);

		//getSolver().getLog().println(name() + "::solve, EXP_DATE[0] = " + expected_date[0]);
		
		stateModel();
		search(startDatePlan);
		
		
//		if (getSolver().getDEBUG())
//			getSolver().getLog().println("SOLUTION:");

		for (int i = 0; i < m; i++) {

			int sz = 0;
			String des = "";
			for (int j = 0; j < n; j++) {
				if (x[j] == i) {
					sz++;
					des += "(" + j + ",q[" + qtt[j] + "], d[" + minDate[j]
							+ "-" + maxDate[j] + "]) ";
				}
			}
			if (sz > 0) {
				System.out.print(name() + "::solve, date " + i + " : ");
				System.out.println("sz = " + sz + ", load = " + load[i]
						+ ", violations_packing = " + violations_packing[i]
						+ ", des = " + des);

//				if (getSolver().getDEBUG()) {
//					getSolver().getLog().print(name() + "::solve, date " + i + " : ");
//					getSolver().getLog().println(
//							"sz = " + sz + ", load = " + load[i]
//									+ ", violations_packing = "
//									+ violations_packing[i] + ", des = ");// +
//																			// des);
//				}

			}
		}

		int[] xd = new int[x.length];
		int[] quantity = new int[x.length];

		for (int d = 0; d < m; d++) {
			ArrayList<Integer> I = getItemsOfBin(d);
			int[] q = new int[I.size()];
			for (int i = 0; i < I.size(); i++)
				q[i] = qtt[I.get(i)];
			double[] sq = selectQuantity(q, minLoad, maxLoad);
			for (int i = 0; i < I.size(); i++) {
				int f = I.get(i);
				quantity[f] = (int) sq[i];
				xd[f] = d;
			}
		}

		return new LeveledHavestPlanSolution(xd, quantity);
	}


	public String name() {
		return "GreedyConstrainedMultiKnapsackSolver";
	}

	public static void main(String[] args) {
	}
}
