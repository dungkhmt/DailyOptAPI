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

public class ConstrainedMultiKnapsackSolver {
	public String name;
	public static double EPS = -0.0001;
	
	protected double[][] p;// p[i,d] is the amount of sugar gained per unit quantity if field i is planned on day d
	
	protected int[] preload;// preload[i] is the pre-load in date i
	protected int[] qtt;// qtt[i] is the remain quantity of field i
	protected int[] minDate;
	protected int[] maxDate;
	protected int[] expectedHavestDate;

	protected int minLoad;
	protected int maxLoad;
	protected int m;
	protected int n;

	protected int maxDeltaDay = 5;
	protected int startDate;
	protected int endDate;

	// variables, invariants
	protected int[] x;// x[i] is the bin where item i is allocated (minDate[i]
						// <= x[i] <= maxDate[i])
	protected int[] load;// load[i] is the current load of bin i

	protected int[] violations_packing;// violations[i] is the violations of bin
										// i
	protected int total_violations_packing;
	protected int max_violations_packing;

	protected int[] violations_havest;// violations_havest[i] is the violations
										// of the havest date of field i
	protected int total_violations_havest;

	protected double[] amount_sugar_gained;
	protected double total_amount_sugar_gained;
	
	protected Random R;

	private SolverMultiStepSplitFields solver;

	public SolverMultiStepSplitFields getSolver() {
		return solver;
	}

	public ConstrainedMultiKnapsackSolver(SolverMultiStepSplitFields solver) {
		this.solver = solver;
	}

	public void setInput(int[] preload, int[] qtt, int[] minDate,
			int[] maxDate, int[] expected_date, int minLoad, int maxLoad, double[][] productivity) {

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
		this.p = productivity;
		expectedHavestDate = expected_date;
	}

	public int[] cbls(int maxTime, int maxIter){
		LocalSearchManager mgr = new LocalSearchManager();
		VarIntLS[] y = new VarIntLS[n];
		for(int i = 0; i < n; i++) 
			y[i] = new VarIntLS(mgr,minDate[i],maxDate[i]);
		ConstraintSystem CS = new ConstraintSystem(mgr);
		int[] cap = new int[m];
		for(int i = 0; i < m; i++)
			cap[i] = maxLoad - preload[i];
		CS.post(new MultiKnapsack(y, qtt, cap));
		mgr.close();
		
		TabuSearch ts = new TabuSearch();
		ts.search(CS, 50, maxTime, maxIter, 200);
		int[] sol = new int[n];
		for(int i = 0; i < n; i++){
			sol[i] = y[i].getValue();
			//if(Math.abs(sol[i] - expectedHavestDate[i]) > 50){
			//	System.out.println(name() + "::cbls, BUG sol[" + i + "] = " + sol[i] + ", expected_harvest = " + 
			//expectedHavestDate[i]); System.exit(-1);
			//}
		}
		return sol;
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
		search(timeLimit);

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

	public int[] getX() {
		return x;
	}

	public double[] selectQuantity(int[] q, int minQ, int maxQ) {
		double[] sq = new double[q.length];
		int k = 0;
		int s = 0;
		int maxQ1 = maxQ;
		for (int i = 0; i < q.length; i++)
			s = s + q[i];

		if (s <= maxQ) {
			for (int i = 0; i < q.length; i++)
				sq[i] = q[i];
			return sq;
		}
		if (q.length == 1) {// take full (maximum) quantity
			sq[0] = maxQ;
			return sq;
		}

		// for(int i = 0; i < q.length; i++) System.out.print(q[i] + " ");
		// System.out.println();

		int[] idx = new int[q.length];
		for (int i = 0; i < q.length; i++)
			idx[i] = i;
		for (int i = 0; i < q.length - 1; i++) {
			for (int j = i + 1; j < q.length; j++) {
				if (q[i] > q[j]) {
					int tmp = q[i];
					q[i] = q[j];
					q[j] = tmp;
					tmp = idx[i];
					idx[i] = idx[j];
					idx[j] = tmp;
				}
			}
		}
		// for(int i = 0; i < q.length; i++) System.out.print(q[i] + " ");
		// System.out.println();
		// for(int i = 0; i < q.length; i++) System.out.print(idx[i] + " ");
		// System.out.println();

		while (k < q.length - 1) {
			s = s - q[k];
			maxQ1 = maxQ1 - q[k];
			int r = s - maxQ1;
			// System.out.println("k = " + k + ", s = " + s + ", maxQ1 = " +
			// maxQ1 + ", r = " + r +
			// ", q[k] = " + q[k] + ", next = " + (q[k+1]*(1-r*1.0/s)));
			if (q[k] > q[k + 1] * (1 - r * 1.0 / s)) {
				s += q[k];
				maxQ1 += q[k];
				// System.out.println("BREAK recover s = " + s + ", maxQ1 = " +
				// maxQ1 + ", k = " + k);
				break;
			} else {
				sq[idx[k]] = q[k];
				// System.out.println("k = " + k + ", idx[" + k + "] = " +
				// idx[k] +
				// " ACCEPT sq[" + idx[k] + "] = " + q[k] + ", s = " + s +
				// ", maxQ1 = " + maxQ1);
			}
			k++;
		}
		// double r = (s - maxQ1)*1.0/s;

		for (int i = k; i < q.length; i++) {
			double r = (s - maxQ1) * 1.0 / s;
			sq[idx[i]] = q[i] * (1 - r);
			/*
			 * int a = (int)sq[idx[i]]; if(a == q[i] - 1) a = q[i];// rounding s
			 * = s - a; maxQ1 = maxQ1 - a; sq[idx[i]] = a;
			 */

			// System.out.println("i = " + i + ", idx[" + i + "] = " + idx[i] +
			// ", q[idx[i]] = " + q[idx[i]] +
			// ", r = " + r + ", ACCEPT sq[" + idx[i] + "] = " + sq[idx[i]]);
		}
		double t = 0;
		for (int i = 0; i < sq.length; i++)
			t = t + sq[i];
		// System.out.println("check t = " + t );

		return sq;
	}

	private HashMap<Integer, Integer> selectQuantity(int d) {
		// return foreach field i, the quantity to be havested
		HashMap<Integer, Integer> havestQuantity = new HashMap<Integer, Integer>();
		ArrayList<Integer> I = getItemsOfBin(d);

		return havestQuantity;
	}

	public void stateModel() {
		x = new int[n];
		load = new int[m];
		violations_packing = new int[m];
		violations_havest = new int[n];
		amount_sugar_gained = new double[n];
	}

	public void initPropagate() {
		for (int i = 0; i < m; i++) {
			load[i] = preload[i];
		}

		for (int j = 0; j < n; j++) {
			int d = x[j];
			load[d] += qtt[j];
		}

		max_violations_packing = 0;// Integer.MAX_VALUE
		total_violations_packing = 0;
		for (int i = 0; i < m; i++) {
			violations_packing[i] = violations_packing(load[i]);
			total_violations_packing += violations_packing[i];
			if (max_violations_packing < violations_packing[i])
				max_violations_packing = violations_packing[i];
		}

		total_violations_havest = 0;
		for (int i = 0; i < n; i++) {
			violations_havest[i] = qtt[i]
					* (Math.abs(x[i] - expectedHavestDate[i]));
			total_violations_havest += violations_havest[i];
		}
		
		total_amount_sugar_gained = 0;
		for(int i = 0; i < n; i++){
			amount_sugar_gained[i] = -qtt[i]*p[i][x[i]];
			total_amount_sugar_gained += amount_sugar_gained[i];
		}
	}

	private void computeMaxViolationsPacking() {
		max_violations_packing = 0;// Integer.MAX_VALUE
		for (int i = 0; i < m; i++) {
			if (max_violations_packing < violations_packing[i])
				max_violations_packing = violations_packing[i];
		}

	}

	private int violations_packing(int aLoad) {
		if (aLoad == 0)
			return 0;
		else {
			if (aLoad < minLoad)
				return minLoad - aLoad;
			else if (aLoad > maxLoad)
				return aLoad - maxLoad;
			else
				return 0;
		}
	}

	public void assign(int i, int j) {
		// allocate item i into bin j and propagate
		int ob = x[i];
		if (ob == j)
			return;
		load[ob] -= qtt[i];
		load[j] += qtt[i];

		// update violations of packing
		total_violations_packing -= violations_packing[ob];
		total_violations_packing -= violations_packing[j];

		violations_packing[ob] = violations_packing(load[ob]);
		violations_packing[j] = violations_packing(load[j]);

		total_violations_packing += violations_packing[ob];
		total_violations_packing += violations_packing[j];

		// update violations of havest date
		total_violations_havest -= violations_havest[i];
		violations_havest[i] = qtt[i] * (Math.abs(expectedHavestDate[i] - j));
		total_violations_havest += violations_havest[i];

		// update amount_augar
		total_amount_sugar_gained -= amount_sugar_gained[i];
		amount_sugar_gained[i] = -qtt[i]*p[i][j];
		total_amount_sugar_gained += amount_sugar_gained[i];
		
		computeMaxViolationsPacking();

		x[i] = j;
	}

	public int getAssignDeltaMaxViolationsPacking(int i, int j) {
		if (x[i] == j)
			return 0;
		int ob = x[i];// old bin of i
		int loadob = load[ob] - qtt[i];
		int loadj = load[j] + qtt[i];

		int vob = violations_packing(loadob);
		int vj = violations_packing(loadj);

		int nv = 0;
		for (int k = 0; k < m; k++) {
			if (k == ob) {
				if (nv < vob)
					nv = vob;
			} else if (k == j) {
				if (nv < vj)
					nv = vj;
			} else if (nv < violations_packing[k])
				nv = violations_packing[k];
		}
		return nv - max_violations_packing;
	}

	public int getSwapDeltaPacking(int i, int j) {
		if (x[i] == x[j])
			return 0;
		int pi = x[i];
		int pj = x[j];
		int loadbi = load[pi] - qtt[i] + qtt[j];
		int loadbj = load[pj] - qtt[j] + qtt[i];
		int vi = violations_packing(loadbi);
		int vj = violations_packing(loadbj);
		int new_violations = total_violations_packing - violations_packing[pi]
				+ vi - violations_packing[pj] + vj;
		return new_violations - total_violations_packing;
	}

	public int getSwapDeltaHavest(int i, int j) {
		if (x[i] == x[j])
			return 0;
		int pi = x[i];
		int pj = x[j];
		int vi = qtt[i] * (Math.abs(expectedHavestDate[i] - pj));
		int vj = qtt[j] * (Math.abs(expectedHavestDate[j] - pi));
		return vi - violations_havest[i] + vj - violations_havest[j];
	}

	public double getSwapDeltaAmountSugar(int i, int j){
		if(x[i] == x[j]) return 0;
		double new_i = -qtt[i]*p[i][x[j]];
		double new_j = -qtt[j]*p[j][x[i]];
		return new_i + new_j - amount_sugar_gained[i] - amount_sugar_gained[j];
	}
	public int getAssignDeltaPacking(int i, int j) {
		// return the difference between old evaluation and new evaluation
		// (after assign item i to bin j)
		if (x[i] == j)
			return 0;
		int ob = x[i];// old bin of i
		int loadob = load[ob] - qtt[i];
		int loadj = load[j] + qtt[i];

		int vob = violations_packing(loadob);
		int vj = violations_packing(loadj);

		return (vob - violations_packing[ob]) + (vj - violations_packing[j]);
	}

	public int getAssignDeltaHavest(int i, int j) {
		if (x[i] == j)
			return 0;
		int h = qtt[i] * (Math.abs(expectedHavestDate[i] - j));
		return h - violations_havest[i];
	}

	public double getAssignDeltaAmountSugar(int i, int j){
		if(x[i] == j) return 0;
		double new_i = -qtt[i]*p[i][j];
		return new_i - amount_sugar_gained[i];
	}
	
	public int[] getLoads() {
		return load;
	}

	public void initSolutionExpectedDate() {
		for (int i = 0; i < n; i++) {
			System.out.println("minDate, maxDate [" + i + "] = " + minDate[i] + ", " + maxDate[i] + 
					", preload.length = " + preload.length + ", category = " + getSolver().getInput().getFields()[i].getCategory() + 
					", plantType = " + getSolver().getInput().getFields()[i].getPlantType());
			
			// assign(i,expectedHavestDate[i]);
			HashSet<Integer> S = new HashSet<Integer>();
			for (int j = minDate[i]; j <= maxDate[i]; j++){
				if (preload[j] < maxLoad)
					S.add(j);
			}
			
			int minD = Integer.MAX_VALUE;
			int sel_j = -1;
			for (int j : S) {
				if (minD > Math.abs(j - expectedHavestDate[i])) {
					minD = Math.abs(j - expectedHavestDate[i]);
					sel_j = j;
				}
			}
			x[i] = sel_j;// expectedHavestDate[i];
		}
		initPropagate();
	}

	public void initSolution() {
		for (int i = 0; i < n; i++) {
			// assign(i,expectedHavestDate[i]);
			HashSet<Integer> S = new HashSet<Integer>();
			for (int j = minDate[i]; j <= maxDate[i]; j++)
				if (preload[j] < maxLoad)
					S.add(j);

			int minD = Integer.MAX_VALUE;
			int sel_j = -1;
			for (int j : S) {
				if (minD > Math.abs(j - expectedHavestDate[i])) {
					minD = Math.abs(j - expectedHavestDate[i]);
					sel_j = j;
				}
			}
			x[i] = sel_j;// expectedHavestDate[i];
		}
		initPropagate();
	}

	public int getRemoveDeltaPackingViolations(int i) {
		// return the variation of violations of packing when removing item i
		// from its current bin
		int bi = x[i];
		int new_load = load[bi] - qtt[i];
		return violations_packing(new_load) - violations_packing[bi];
	}

	public int getReplaceDeltaPackingViolations(int i1, int i2) {
		// b2 = x[i2]: bin of i2
		// return the variation of violations of packing when adding item i1
		// into b2 and removing
		// i2 from its current bin b2
		int b2 = x[i2];
		int new_load = load[b2] + qtt[i1] - qtt[i2];
		return violations_packing(new_load) - violations_packing[b2];
	}

	public int getAddDeltaPackingViolations(int i, int b) {
		// return the variation of violations of packing when item i is added
		// into bin b
		int new_load = load[b] + qtt[i];
		return violations_packing(new_load) - violations_packing[b];
	}

	public ArrayList<Integer> getItemsOfBin(int b) {
		ArrayList<Integer> L = new ArrayList<Integer>();
		for (int i = 0; i < n; i++)
			if (x[i] == b)
				L.add(i);
		return L;
	}

	public boolean acceptDate(int i, int b) {
		// return true if item i can be placed in bin b
		return !forbiddenDate(b) && minDate[i] <= b && b <= maxDate[i];
	}

	public boolean forbiddenDate(int d) {
		return preload[d] >= maxLoad;
	}

	public void print() {
		for (int i = 0; i < m; i++) {
			ArrayList<Integer> L = getItemsOfBin(i);
			if (L.size() > 0) {
				System.out.print(name() + "::print, Bin " + i + " : ");
				for (int j = 0; j < n; j++)
					if (x[j] == i)
						System.out.print(j + "(" + qtt[j] + ") ");
				System.out.println("load = " + load[i]
						+ ", violations_packing = " + violations_packing[i]);
			}
		}

		for (int i = 0; i < n; i++) {
			System.out.println(name() + "::print, x[" + i + "] = " + x[i]
					+ ", expectedDate[" + i + "] = " + expectedHavestDate[i]
					+ ", violations_havest[" + i + "] = "
					+ violations_havest[i]);
		}

		System.out.println("eval = " + eval());
	}

	public void performMoveSequence(ArrayList<Integer> moves, int d) {
		for (int k = moves.size() - 1; k >= 0; k--) {
			int i = moves.get(k);
			int j = d;
			if (k > 0) {
				j = x[moves.get(k - 1)];
			}
			int ob = x[i];
			assign(i, j);

//			if (getSolver().getDEBUG())
//				getSolver().getLog().println(
//						name() + "::performMoveSequence, assign(" + i
//								+ "(code-"
//								+ solver.getInput().getFields()[i].getCode()
//								+ ", qtt-" + qtt[i] + ")  from " + ob + " -> "
//								+ j + ") " + "eval = " + eval() + ", load[" + j
//								+ "] = " + load[j] + ", violations_packing["
//								+ j + "] = " + violations_packing[j]);
		}
	}

	public void moveSequence(int maxIter) {
		maxIter = 1;
		PathMove PM = new PathMove(this);
		int count = 0;
		while (true) {
			PM.findOptimalMovePath(true);
			ArrayList<Integer> moves = PM.getMovedItems();
			int d = PM.getGlobalFinalDate();
			int best = PM.getGlobalBest();
			if (moves.size() <= 0)
				break;

			// if (best >= 0) break;
			// System.out.print(name() + "::moveSequence, step " + count +
			// ", d = " + d + ", moves = ");
//			if (getSolver().getDEBUG())
//				getSolver().getLog().print(
//						name() + "::moveSequence, step " + count + ", d = " + d
//								+ ", moves = ");
			for (int k = moves.size() - 1; k >= 0; k--) {
				int i = moves.get(k);
				int bi = x[i];
				// System.out.print(i + "[q-" + qtt[i] + ", d-" + bi + "], ");
//				if (getSolver().getDEBUG())
//					getSolver().getLog().print(
//							i + "[q-" + qtt[i] + ", d-" + bi + "], ");
			}
			// System.out.println();
//			if (getSolver().getDEBUG())
//				getSolver().getLog().println();
			/*
			 * for (int k = moves.size() - 1; k >= 0; k--) { int i =
			 * moves.get(k); int j = d; if (k > 0) { j = x[moves.get(k - 1)]; }
			 * int ob = x[i]; assign(i, j); //System.out.println(name() +
			 * "::moveSequence, assign(" + i + "(code-" +
			 * solver.getInput().getFields()[i].getCode() + ", qtt-" + qtt[i] +
			 * ")  from " + ob + " -> " + j + ") " // + "eval = " + eval() +
			 * ", load[" + j + "] = " + load[j] + // ", violations_packing[" + j
			 * + "] = " + violations_packing[j]); }
			 */

			performMoveSequence(moves, d);

			// System.out.println(name() +
			// "::moveSequence FINISH A LOOP best = "
			// + best + " -------------------------");
//			if (getSolver().getDEBUG())
//				getSolver().getLog().println(
//						name() + "::moveSequence FINISH A LOOP best = " + best
//								+ " -------------------------");
			count++;
			if (count >= maxIter)
				break;
		}
//		if (getSolver().getDEBUG())
//			getSolver().getLog().println(name() + "::moveSequence, POST");
		for (int i = 1; i <= 50; i++) {
			PM.findOptimalMovePath(20, -1, false);
			performMoveSequence(PM.getMovedItems(), PM.getGlobalFinalDate());
		}
	}

	public void search(int timeLimit) {
		int timeLimit1 = timeLimit/2 < 120 ? timeLimit/2 : 120;
		int timeLimit2 = timeLimit - timeLimit1;
		
		int[] sol = cbls(timeLimit1,100000);
		for(int i = 0; i < n; i++) x[i] = sol[i];
		initPropagate();
		
		/*
		for(int i = 0; i < n; i++){
			getSolver().getLog().println(name() + "::search, minDate-maxDate = " + minDate[i] + "-" + maxDate[i]
					+ ", expectedDate = " + expectedHavestDate[i]);
			for(int d = 0; d < m; d++){
				if(p[i][d] > 0) getSolver().getLog().println(name() + "::search, p[" + i + "," + d + "] = " + p[i][d]);
			}
		}
		*/
		
		//initSolution();

		System.out.println(name() + "::search, initial solution: eval = "
				+ eval()); // print();

		//moveSequence(200);

		//if(true)return;
		
		
		System.out.println(name()
				+ "::search, after moveSequence solution: eval = " + eval());// print();

		// if(true)return;
		// maxIter = 0;
		/*
		 * ArrayList<AssignMove> moves = new ArrayList<AssignMove>(); for (int
		 * it = 0; it < maxIter; it++) { moves.clear(); int
		 * min_delta_violations_packing = Integer.MAX_VALUE; int
		 * min_delta_violations_havest = Integer.MAX_VALUE; int
		 * min_delta_max_violations_packing = Integer.MAX_VALUE;
		 * 
		 * for (int i = 0; i < n; i++) { for (int j = minDate[i]; j <=
		 * maxDate[i]; j++) { if (preload[j] == maxLoad) continue;// ignore full
		 * date (bin)
		 * 
		 * int delta_violations_packing = getAssignDeltaPacking(i, j); int
		 * delta_violations_havest = getAssignDeltaHavest(i, j); int
		 * delta_max_violations_packing = getAssignDeltaMaxViolationsPacking( i,
		 * j);
		 * 
		 * if (min_delta_max_violations_packing > delta_max_violations_packing)
		 * { min_delta_max_violations_packing = delta_max_violations_packing;
		 * min_delta_violations_packing = delta_violations_packing;
		 * min_delta_violations_havest = delta_violations_havest;
		 * 
		 * moves.clear(); moves.add(new AssignMove(i, j)); } else if
		 * (min_delta_max_violations_packing == delta_max_violations_packing &&
		 * min_delta_violations_packing > delta_violations_packing) {
		 * min_delta_max_violations_packing = delta_max_violations_packing;
		 * min_delta_violations_havest = delta_violations_havest;
		 * 
		 * moves.clear(); moves.add(new AssignMove(i, j)); } else if
		 * (min_delta_max_violations_packing == delta_max_violations_packing &&
		 * min_delta_violations_packing == delta_violations_packing &&
		 * min_delta_violations_havest > delta_violations_havest) {
		 * 
		 * min_delta_violations_havest = delta_violations_havest;
		 * 
		 * moves.clear(); moves.add(new AssignMove(i, j)); } else if
		 * (min_delta_max_violations_packing == delta_max_violations_packing &&
		 * min_delta_violations_packing == delta_violations_packing &&
		 * min_delta_violations_havest == delta_violations_havest) {
		 * moves.add(new AssignMove(i, j)); } } }
		 * 
		 * // perform the move if (moves.size() <= 0) {
		 * System.out.println(name() + "::search, NO MOVE --> BREAK"); break; }
		 * else { AssignMove m = moves.get(R.nextInt(moves.size()));
		 * 
		 * int o = x[m.i]; int l1 = load[o]; int l2 = load[m.v];
		 * 
		 * assign(m.i, m.v);
		 * 
		 * System.out.println(name() + "::search, Step " + it + " -> move(" +
		 * m.i + " q(" + qtt[m.i] + "  from " + o + " -> " + m.v +
		 * "), old_load[" + o + "] = " + l1 + ", old_load[" + m.v + "] = " + l2
		 * + ", load[" + o + "] = " + load[o] + ", load[" + m.v + "] = " +
		 * load[m.v] + ", eval = " + eval());
		 * 
		 * getSolver().getLog().println( name() + "::search, Step " + it +
		 * " -> move(" + m.i + " q(" + qtt[m.i] + "  from " + o + " -> " + m.v +
		 * "), old_load[" + o + "] = " + l1 + ", old_load[" + m.v + "] = " + l2
		 * + ", load[" + o + "] = " + load[o] + ", load[" + m.v + "] = " +
		 * load[m.v] + ", eval = " + eval()); } }
		 */

		//searchReduceTotalPackingViolations(10000);
		
		searchReduceTotalPackingViolationsAmountSugar(timeLimit2/3, 10000);
		
		System.out
				.println(name()
						+ "::search, after FIRST searchReduceTotalPackingViolations solution: eval = "
						+ eval());

		//if(true) return;
		searchAggregateDates(10000, timeLimit2/3);
		System.out.println(name()
				+ "::search, after searchAggregateDates solution: eval = "
				+ eval());

		//searchReduceTotalPackingViolations(10000);
		searchReduceTotalPackingViolationsAmountSugar(timeLimit2/3, 10000);
		
		System.out
				.println(name()
						+ "::search, after SECOND searchReduceTotalPackingViolations solution: eval = "
						+ eval());

		/*
		 * searchReduceTotalPackingViolations(10); System.out.println(name() +
		 * "::search, after searchReduceTotalPackingViolations: "); print();
		 * searchAggregateDates(10); System.out.println(name() +
		 * "::search, after searchAggregateDates: "); print();
		 * searchReduceTotalPackingViolations(10); System.out.println(name() +
		 * "::search, after searchReduceTotalPackingViolations: "); print();
		 * 
		 * System.out.println(name() +
		 * "::search END ------------------------------------------------------"
		 * );
		 */
	}

	private int sumQTT(ArrayList<Integer> I) {
		int t = 0;
		for (int i : I)
			t += qtt[i];
		return t;
	}

	private boolean canMove(ArrayList<Integer> I, int d) {
		if (I == null || I.size() == 0)
			return false;
		for (int i : I)
			if (!acceptDate(i, d))
				return false;
		return true;
	}

	public void searchReduceTotalPackingViolations(int maxIter) {
		ArrayList<AssignMove> moves = new ArrayList<AssignMove>();
		ArrayList<SwapMove> swap_moves = new ArrayList<SwapMove>();

		for (int it = 0; it < maxIter; it++) {
			moves.clear();
			swap_moves.clear();

			int min_delta_violations_packing = Integer.MAX_VALUE;
			int min_delta_violations_havest = Integer.MAX_VALUE;
			int min_swap_delta_violations_packing = Integer.MAX_VALUE;
			int min_swap_delta_violations_havest = Integer.MAX_VALUE;

			// explore swap moves
			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					if (acceptDate(i, x[j]) && acceptDate(j, x[i])) {
						int delta_swap_violations_packing = getSwapDeltaPacking(
								i, j);
						int delta_swap_violations_havest = getSwapDeltaHavest(
								i, j);
						if (delta_swap_violations_packing < min_swap_delta_violations_packing) {
							swap_moves.clear();
							swap_moves.add(new SwapMove(i, j));
							min_swap_delta_violations_packing = delta_swap_violations_packing;
							min_swap_delta_violations_havest = delta_swap_violations_havest;

						} else if (delta_swap_violations_packing == min_swap_delta_violations_packing
								&& delta_swap_violations_havest < min_swap_delta_violations_havest) {
							swap_moves.clear();
							swap_moves.add(new SwapMove(i, j));
							min_swap_delta_violations_havest = delta_swap_violations_havest;
						} else if (delta_swap_violations_packing == min_swap_delta_violations_packing
								&& delta_swap_violations_havest == min_swap_delta_violations_havest) {
							swap_moves.add(new SwapMove(i, j));
						}
					}
				}
			}

			// explore assign move
			for (int i = 0; i < n; i++) {
				for (int j = minDate[i]; j <= maxDate[i]; j++) {
					if (preload[j] == maxLoad)
						continue;// ignore full date (bin)

					int delta_violations_packing = getAssignDeltaPacking(i, j);
					int delta_violations_havest = getAssignDeltaHavest(i, j);

					if (min_delta_violations_packing > delta_violations_packing) {
						min_delta_violations_packing = delta_violations_packing;
						min_delta_violations_havest = delta_violations_havest;
						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_violations_packing == delta_violations_packing
							&& min_delta_violations_havest > delta_violations_havest) {
						min_delta_violations_havest = delta_violations_havest;
						moves.clear();

						moves.add(new AssignMove(i, j));
					} else if (min_delta_violations_packing == delta_violations_packing
							&& min_delta_violations_havest == delta_violations_havest) {
						moves.add(new AssignMove(i, j));
					}
				}
			}

			// perform the move
			// if (moves.size() <= 0 || min_delta_violations_packing >= 0) {
			if (min_delta_violations_packing >= 0
					&& min_delta_violations_havest >= 0
					&& min_swap_delta_violations_packing >= 0
					&& min_swap_delta_violations_havest >= 0
					) {
				System.out
						.println(name()
								+ "::searchReduceTotalPackingViolations, NO MOVE --> BREAK");
				break;
			} else {
				if (min_delta_violations_packing < min_swap_delta_violations_packing
						|| min_delta_violations_packing == min_swap_delta_violations_packing
						&& min_delta_violations_havest < min_swap_delta_violations_havest) {
				
					AssignMove m = moves.get(R.nextInt(moves.size()));

					int o = x[m.i];
					int l1 = load[o];
					int l2 = load[m.v];

					assign(m.i, m.v);

					System.out.println(name()
							+ "::searchReducePackingViolations, Step " + it
							+ " -> move(" + m.i + " q(" + qtt[m.i] + "  from " + o
							+ " -> " + m.v + "), old_load[" + o + "] = " + l1
							+ ", old_load[" + m.v + "] = " + l2 + ", load[" + o
							+ "] = " + load[o] + ", load[" + m.v + "] = "
							+ load[m.v] + ", eval = " + eval() + ", delta_violations_packing = "
							+ min_delta_violations_packing + ", delta_violations_havest = " + min_delta_violations_havest);

//					if (getSolver().getDEBUG())
//						getSolver().getLog().println(
//								name() + "::searchReducePackingViolations, Step "
//										+ it + " -> move(" + m.i + " q(" + qtt[m.i]
//										+ "  from " + o + " -> " + m.v
//										+ "), old_load[" + o + "] = " + l1
//										+ ", old_load[" + m.v + "] = " + l2
//										+ ", load[" + o + "] = " + load[o]
//										+ ", load[" + m.v + "] = " + load[m.v]
//										+ ", eval = " + eval() + ", delta_violations_packing = "
//										+ min_delta_violations_packing + ", delta_violations_havest = " + min_delta_violations_havest);
				} else {
					SwapMove m = swap_moves.get(R.nextInt(swap_moves.size()));
					int pi = x[m.j];
					int pj = x[m.i];
					int old_load_i = load[x[m.i]];
					int old_load_j = load[x[m.j]];
					
					assign(m.i, pi);
					assign(m.j, pj);

					System.out.println(name()
							+ "::searchReducePackingViolations, Step " + it
							+ " -> swap(" + m.i + " q(" + qtt[m.i] + ") at " + pj + "  , " + m.j
							+ " " + qtt[m.j] + " at " + pi + "), old_load[" + pj + "] = " + old_load_i
							+ ", old_load[" + pi + "] = " + old_load_j + ", load[" + pj
							+ "] = " + load[pj] + ", load[" + pi + "] = "
							+ load[pi] + ", eval = " + eval() + ", delta_swap_violations_packing = "
							+ min_swap_delta_violations_packing + ", delta_swap_violations_havest = " + min_swap_delta_violations_havest);

//					if (getSolver().getDEBUG())
//						getSolver().getLog().println(name()
//								+ "::searchReducePackingViolations, Step " + it
//								+ " -> swap(" + m.i + " q(" + qtt[m.i] + ") at " + pj + "  , " + m.j
//								+ " " + qtt[m.j] + " at " + pi + "), old_load[" + pj + "] = " + old_load_i
//								+ ", old_load[" + pi + "] = " + old_load_j + ", load[" + pj
//								+ "] = " + load[pj] + ", load[" + pi + "] = "
//								+ load[pi] + ", eval = " + eval() + ", delta_swap_violations_packing = "
//								+ min_swap_delta_violations_packing + ", delta_swap_violations_havest = " + min_swap_delta_violations_havest);
	
				}

			}
		}

	}

	public void searchReduceTotalPackingViolationsAmountSugar(int timeLimit, int maxIter) {
		ArrayList<AssignMove> moves = new ArrayList<AssignMove>();
		ArrayList<SwapMove> swap_moves = new ArrayList<SwapMove>();
		
		timeLimit = timeLimit*1000;
		double t0 = System.currentTimeMillis();
		
		for (int it = 0; it < maxIter; it++) {
			double t = System.currentTimeMillis() - t0;
			if(t > timeLimit) break;
			
			System.out.println(name() + "::searchReduceTotalPackingViolationsAmountSugar, time = " + 
			t + " timeLimit = " + timeLimit);
			
			
			moves.clear();
			swap_moves.clear();

			int min_delta_violations_packing = Integer.MAX_VALUE;
			double min_delta_sugar_harvest = Integer.MAX_VALUE;
			int min_swap_delta_violations_packing = Integer.MAX_VALUE;
			double min_swap_delta_sugar_havest = Integer.MAX_VALUE;

			// explore swap moves
			for (int i = 0; i < n - 1; i++) {
				for (int j = i + 1; j < n; j++) {
					if (acceptDate(i, x[j]) && acceptDate(j, x[i])) {
						int delta_swap_violations_packing = getSwapDeltaPacking(
								i, j);
						double delta_swap_sugar_harvest = getSwapDeltaAmountSugar(
								i, j);
						if (delta_swap_violations_packing < min_swap_delta_violations_packing) {
							swap_moves.clear();
							swap_moves.add(new SwapMove(i, j));
							min_swap_delta_violations_packing = delta_swap_violations_packing;
							min_swap_delta_sugar_havest = delta_swap_sugar_harvest;

						} else if (delta_swap_violations_packing == min_swap_delta_violations_packing
								&& delta_swap_sugar_harvest < min_swap_delta_sugar_havest) {
							swap_moves.clear();
							swap_moves.add(new SwapMove(i, j));
							min_swap_delta_sugar_havest = delta_swap_sugar_harvest;
						} else if (delta_swap_violations_packing == min_swap_delta_violations_packing
								&& delta_swap_sugar_harvest == min_swap_delta_sugar_havest) {
							swap_moves.add(new SwapMove(i, j));
						}
					}
				}
			}

			// explore assign move
			for (int i = 0; i < n; i++) {
				for (int j = minDate[i]; j <= maxDate[i]; j++) {
					if (preload[j] == maxLoad)
						continue;// ignore full date (bin)

					int delta_violations_packing = getAssignDeltaPacking(i, j);
					double delta_sugar_havest = getAssignDeltaAmountSugar(i, j);

					if (min_delta_violations_packing > delta_violations_packing) {
						min_delta_violations_packing = delta_violations_packing;
						min_delta_sugar_harvest = delta_sugar_havest;
						moves.clear();
						moves.add(new AssignMove(i, j));
					} else if (min_delta_violations_packing == delta_violations_packing
							&& min_delta_sugar_harvest > delta_sugar_havest) {
						min_delta_sugar_harvest = delta_sugar_havest;
						moves.clear();

						moves.add(new AssignMove(i, j));
					} else if (min_delta_violations_packing == delta_violations_packing
							&& min_delta_sugar_harvest == delta_sugar_havest) {
						moves.add(new AssignMove(i, j));
					}
				}
			}

			// perform the move
			// if (moves.size() <= 0 || min_delta_violations_packing >= 0) {
			if (min_delta_violations_packing >= EPS
					&& min_delta_sugar_harvest >= EPS
					&& min_swap_delta_violations_packing >= EPS
					&& min_swap_delta_sugar_havest >= EPS
					) {
				System.out
						.println(name()
								+ "::searchReduceTotalPackingViolations, NO MOVE --> BREAK");
				break;
			} else {
				if (min_delta_violations_packing < min_swap_delta_violations_packing
						|| min_delta_violations_packing == min_swap_delta_violations_packing
						&& min_delta_sugar_harvest < min_swap_delta_sugar_havest) {
				
					AssignMove m = moves.get(R.nextInt(moves.size()));

					int o = x[m.i];
					int l1 = load[o];
					int l2 = load[m.v];

					assign(m.i, m.v);

					System.out.println(name()
							+ "::searchReducePackingViolations, Step " + it
							+ " -> move(" + m.i + " q(" + qtt[m.i] + "  from " + o
							+ " -> " + m.v + "), old_load[" + o + "] = " + l1
							+ ", old_load[" + m.v + "] = " + l2 + ", load[" + o
							+ "] = " + load[o] + ", load[" + m.v + "] = "
							+ load[m.v] + ", eval = " + eval() + ", delta_violations_packing = "
							+ min_delta_violations_packing + ", delta_sugar_havest = " + min_delta_sugar_harvest);

//					if (getSolver().getDEBUG())
//						getSolver().getLog().println(
//								name() + "::searchReducePackingViolations, Step "
//										+ it + " -> move(" + m.i + " q(" + qtt[m.i]
//										+ "  from " + o + " -> " + m.v
//										+ "), old_load[" + o + "] = " + l1
//										+ ", old_load[" + m.v + "] = " + l2
//										+ ", load[" + o + "] = " + load[o]
//										+ ", load[" + m.v + "] = " + load[m.v]
//										+ ", eval = " + eval() + ", delta_violations_packing = "
//										+ min_delta_violations_packing + ", delta_sugar_havest = " + min_delta_sugar_harvest);
				} else {
					SwapMove m = swap_moves.get(R.nextInt(swap_moves.size()));
					int pi = x[m.j];
					int pj = x[m.i];
					int old_load_i = load[x[m.i]];
					int old_load_j = load[x[m.j]];
					
					assign(m.i, pi);
					assign(m.j, pj);

					//if(Math.abs(x[m.i] - expectedHavestDate[m.i]) > 50){
					//	System.out.println(name() + "::searchReduceTotalPackingViolationsAmountSugar, BUG");
					//	System.exit(-1);
					//}
					
					//if(Math.abs(x[m.j] - expectedHavestDate[m.j]) > 50){
					//	System.out.println(name() + "::searchReduceTotalPackingViolationsAmountSugar, BUG");
					//	System.exit(-1);
					//}
					
					System.out.println(name()
							+ "::searchReducePackingViolations, Step " + it
							+ " -> swap(" + m.i + " q(" + qtt[m.i] + ") at " + pj + "  , " + m.j
							+ " " + qtt[m.j] + " at " + pi + "), old_load[" + pj + "] = " + old_load_i
							+ ", old_load[" + pi + "] = " + old_load_j + ", load[" + pj
							+ "] = " + load[pj] + ", load[" + pi + "] = "
							+ load[pi] + ", eval = " + eval() + ", delta_swap_violations_packing = "
							+ min_swap_delta_violations_packing + ", delta_swap_sugar_havest = " + 
							min_swap_delta_sugar_havest);

//					if (getSolver().getDEBUG())
//						getSolver().getLog().println(name()
//								+ "::searchReducePackingViolations, Step " + it
//								+ " -> swap(" + m.i + " q(" + qtt[m.i] + ") at " + pj + "  , " + m.j
//								+ " " + qtt[m.j] + " at " + pi + "), old_load[" + pj + "] = " + old_load_i
//								+ ", old_load[" + pi + "] = " + old_load_j + ", load[" + pj
//								+ "] = " + load[pj] + ", load[" + pi + "] = "
//								+ load[pi] + ", eval = " + eval() + ", delta_swap_violations_packing = "
//								+ min_swap_delta_violations_packing + ", delta_swap_sugar_havest = " + 
//								min_swap_delta_sugar_havest);
	
				}

			}
		}

	}

	
	public void searchAggregateDates(int maxIter, int maxTime) {
		// maxIter = 1;
		double t0 = System.currentTimeMillis();
		for (int it = 0; it < maxIter; it++) {
			double t = System.currentTimeMillis() - t0;
			if(t > maxTime) break;
			
			int minDelta = Integer.MAX_VALUE;
			int min_delta_havest = Integer.MAX_VALUE;

			AggregateDatesMove sel_move = null;
			int sel_date = -1;
			for (int d = startDate; d <= endDate; d++) {
				// System.out.println(name() + "::searchAggregateDates, NAME = "
				// + name + ", it = " + it + "/" + maxIter + ", d = " + d +
				// ", startEvaluate");

				AggregateDatesMove m = evaluateAggregateMove(d);

				// if(d >= 550 && d <= 560 )
				// System.out.println(name() + "::searchAggregateDates, NAME = "
				// + name + ", it = " + it + "/" + maxIter + ", d = " + d +
				// ", delta = " + m.getDelta() +
				// ", delta_havest = " + m.getDelta_havest() + ", minDelta = " +
				// minDelta + ", min_delta_havest = " +
				// min_delta_havest + ", sz = " + m.getDates().size());
				if (m.getDates().size() > 0)
					if (minDelta > m.getDelta() || minDelta == m.getDelta()
							&& min_delta_havest > m.getDelta_havest()) {
						minDelta = m.getDelta();
						min_delta_havest = m.getDelta_havest();
						sel_move = m;
						sel_date = d;
						System.out.println(name()
								+ "::searchAggregateDates, it = " + it + "/"
								+ maxIter + ", UPDATE sel_date = " + d +

								", minDelta = " + minDelta + ", sel_dates = "
								+ sel_move.getDates().size());
					}
			}
			if (sel_move != null) {
				if (minDelta >= 0) {
					System.out.println(name()
							+ "::searchAggregateDates, no improvement, BREAK");
					break;
				} else {
					// getSolver().getLog().println(name() +
					// "::searchAggregateDates, sel_date = " + sel_date +
					// ", load[" + sel_date + "] = " + load[sel_date] +
					// ", dates = ");

					for (int d : sel_move.getDates()) {
						ArrayList<Integer> I = getItemsOfBin(d);
						// getSolver().getLog().print(" date[" + d + ", load-" +
						// load[d] + "]: ");
						for (int i : I) {
							// getSolver().getLog().print("(" + i + ", qtt[" +
							// qtt[i] + "]) ");
							assign(i, sel_date);
						}
						// getSolver().getLog().println();
					}

//					if (getSolver().getDEBUG())
//						getSolver().getLog().println(
//								name() + "::searchAggregateDates, iter " + it
//										+ ", move aggregate, eval = " + eval()
//										+ ", load[" + sel_date + "] = "
//										+ load[sel_date] + ", delta = "
//										+ minDelta + ", dates.sz = "
//										+ sel_move.getDates().size());

					System.out.println(name() + "::searchAggregateDates, iter "
							+ it + ", move aggregate, eval = " + eval()
							+ ", load[" + sel_date + "] = " + load[sel_date]
							+ ", delta = " + minDelta + ", dates.sz = "
							+ sel_move.getDates().size());

				}
			} else {
				System.out.println(name()
						+ "::searchAggregateDates, sel_move = NULL, BREAK");
				break;
			}

			// break;
		}
	}

	public AggregateDatesMove evaluateAggregateMove(int d) {
		// from date d, try to aggregate fields of adjacent dates di (before and
		// after) of d to d
		// such that violation_packing[di] reduces ai > 0
		// total violations_packing reduces delta
		// return delta
		int loadd = load[d];
		int turn = 0;
		int dl = d - 1;
		int dr = d + 1;
		int reducedViolations = 0;

		/*
		 * maxDeltaDay = 0; for (int i = 0; i <
		 * getSolver().getInput().getFields().length; i++) { int del =
		 * getSolver().getInput().getFields()[i].getDeltaDays(); maxDeltaDay =
		 * maxDeltaDay > del ? maxDeltaDay : del; }
		 */

		maxDeltaDay = getSolver().getInput().getPlantStandard().getMaxRange();

		ArrayList<Integer> agg_dates = new ArrayList<Integer>();
		ArrayList<Integer> items = new ArrayList<Integer>();
		boolean hasLeft = true;
		boolean hasRight = true;
		while (hasLeft && hasRight) {
			if (turn % 2 == 0) {// find fields in preceding dates
				if (dl >= d - maxDeltaDay) {
					ArrayList<Integer> I = getItemsOfBin(dl);
					if (canMove(I, d)) {
						int l = sumQTT(I);
						if (l < minLoad && l > 0) {
							loadd += l;
							reducedViolations += (minLoad - l);
							agg_dates.add(dl);

							for (int i : I)
								items.add(i);

							if (loadd >= minLoad)
								break;
						}
					}
					dl--;
				} else {
					hasLeft = false;
				}
			} else {// find fields in successing dates
				if (dr <= d + maxDeltaDay) {
					ArrayList<Integer> I = getItemsOfBin(dr);
					if (canMove(I, d)) {
						int l = sumQTT(I);
						if (l < minLoad && l > 0) {
							loadd += l;
							reducedViolations += (minLoad - l);
							agg_dates.add(dr);

							for (int i : I)
								items.add(i);

							if (loadd >= minLoad)
								break;
						}
					}
					dr++;
				} else {
					hasRight = false;
				}
			}
			turn = 1 - turn;
		}

		int vd = violations_packing(loadd);

		int delta = (vd - reducedViolations - violations_packing[d]);

		// compute delta_violations_havest
		int delta_violations_havest = 0;
		for (int i : items) {
			int di = getAssignDeltaHavest(i, d);
			delta_violations_havest += di;
			// System.out.println(name() +
			// "::getDeltaAggregate, violations_havest = " +
			// violations_havest[i] +
			// ", delta_violations_havest = " + delta_violations_havest);
		}

		return new AggregateDatesMove(agg_dates, delta, delta_violations_havest);
	}

	public String eval() {
		return "(" + max_violations_packing + "," + total_violations_packing
				+ "," + total_violations_havest + "," + total_amount_sugar_gained + ")";
	}

	public String name() {
		return "ConstrainedMultiKnapsackSolver";
	}

	public static void main(String[] args) {
		/*
		 * int[] qtt = new int[] { 3, 7, 9, 2, 6, 4, 4, 8, 5, 10, 3, 12, 6, 6, 7
		 * }; int minLoad = 10; int maxLoad = 18; int[] minDate = new int[] { 2,
		 * 5, 0, 2, 6, 0, 0, 3, 3, 2, 5, 6, 5, 4, 0 }; int[] maxDate = new int[]
		 * { 4, 7, 1, 5, 8, 2, 3, 6, 6, 4, 7, 8, 8, 6, 3 }; int[] preload = new
		 * int[] { 0, 0, 18, 0, 18, 0, 0, 18, 0 };
		 * 
		 * ConstrainedMultiKnapsackSolver solver = new
		 * ConstrainedMultiKnapsackSolver( null); solver.solve(preload, qtt,
		 * minDate, maxDate, minLoad, maxLoad);
		 */
		int[] q = new int[] { 30, 5, 45, 20, 10, 35 };
		int maxQ = 130;
		ConstrainedMultiKnapsackSolver S = new ConstrainedMultiKnapsackSolver(
				null);
		double[] sq = S.selectQuantity(q, 100, maxQ);
		for (int i = 0; i < sq.length; i++)
			System.out.println(sq[i] + " ");
	}
}
