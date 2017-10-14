package havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;

public class PathMove {
	private ConstrainedMultiKnapsackSolver solver;
	private int[] f;// f[i] is the maximal benefits (reduction of violations of packing)
					// of the path from the depot to item i
	private int[] p;// p[i] is the predecessor of the optimal path from the depot to i
	
	private int[] fd;// fd[d] is the maximal benefits (reduction of violations of packing)
					// of the path from the depot until date d
	private int[] pd;// pd[d] is the predecessor item of date d in the optimal path from the depot to date d
	
	private int global_best;
	private ArrayList<Integer> moved_items;
	private int global_final_date;
	
	// local data structures
	private int local_best;
	private ArrayList<Integer> local_moved_items;
	private int local_final_date;
	
	public PathMove(ConstrainedMultiKnapsackSolver solver){
		this.solver = solver;
		f = new int[solver.n];
		p = new int[solver.n];
		fd = new int[solver.m];
		pd = new int[solver.m];
		
		moved_items = new ArrayList<Integer>();
		local_moved_items = new ArrayList<Integer>();
	}
	
	public int getGlobalFinalDate(){
		return global_final_date;
	}
	public int getGlobalBest(){
		return global_best;
	}
	public ArrayList<Integer> getMovedItems(){
		return moved_items;
	}
	
	public String name(){
		return "PathMove";
	}
	public void findOptimalMovePath(boolean maxLength){
		global_best = Integer.MAX_VALUE;
		moved_items.clear();
		
		int maxV = -1;
		int sel_d = -1;
		for(int d = 0; d < solver.m; d++){
			if(maxV < solver.violations_packing[d] && solver.violations_packing[d] > 0){
				maxV = solver.violations_packing[d];
				sel_d = d;
			}
		}
		if(sel_d < 0) return;
		
		int d = sel_d;
		
		System.out.println(name() + "::findOptimalMovePath(" + maxLength + "), start date = " + d + 
				", load[" + d + "] = " + solver.load[d] + ", violations = " + solver.violations_packing[d]);
		solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + maxLength + "), start date = " + d + 
				", load[" + d + "] = " + solver.load[d] + ", violations = " + solver.violations_packing[d]);
		
			int inc = 1;
			findOptimalMovePathPrivate(d, inc, maxLength);
			if(global_best > local_best){
				global_best = local_best;
				//System.out.println(name() + "::findOptimalMovePath(" + maxLength + "), inc = 1, UPDATE global_best = " + global_best);
				solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + maxLength + "), inc = 1, UPDATE global_best = " + global_best);
				global_final_date = local_final_date;
				moved_items.clear();
				for(int i: local_moved_items)
					moved_items.add(i);
			}
			
			inc = -1;
			findOptimalMovePathPrivate(d, inc, maxLength);
			if(global_best > local_best){
				global_best = local_best;
				//System.out.println(name() + "::findOptimalMovePath(" + maxLength + "), inc = -1, UPDATE global_best = " + global_best);
				solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + maxLength + "), inc = -1, UPDATE global_best = " + global_best);
				
				global_final_date = local_final_date;
				moved_items.clear();
				for(int i: local_moved_items)
					moved_items.add(i);
			}			
		
	}
	public void findOptimalMovePath(int d, int inc, boolean maxLength){
		findOptimalMovePathPrivate(d, inc, maxLength);
		global_best = local_best;
		global_final_date = local_final_date;
		moved_items.clear();
		for(int i: local_moved_items)
			moved_items.add(i);
	}
	
	/*
	 * unsafe 
	 */
	private void findOptimalMovePathPrivate(int d, int inc, boolean maxLength){
		// compute and return the path: f1(d1), f2(d2), f3(d3), .., fk(dk)
		// f1 is in date d
		// move f1 to date d1, f2 (in d1) to date d2,. . ., fk (in d_{k-1}) to date dk
		// d > d1 > d2 > ... > dk OR d < d1 < d2 < ... depends on the increment inc
		// inc is the increment unit
		
		//f = new int[solver.n];
		//p = new int[solver.n];
		//fd = new int[solver.m];
		//pd = new int[solver.m];
		int i_d = d;
		int best = Integer.MAX_VALUE;
		int sel_date = -1;
		local_best = best;
		local_moved_items.clear();
		//moved_items.clear();
		ArrayList<Integer> L = solver.getItemsOfBin(d);
		
		if(L.size() == 1){
			int f = L.get(0);
			//if(solver.qtt[f] >= solver.maxLoad)
				return ;
		}
		
		//solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "," + maxLength
		//		+ "), START, L.sz = " + L.size());
		
		for(int i: L){
			f[i] = solver.getRemoveDeltaPackingViolations(i);
			//if(i == 2746){
			//	solver.getSolver().getLog().println(name() + "::findOptimalMovePath, f[2746] = " + f[2746]);
			//}
			p[i] = -1;
			solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + ", init p[" + i + "] = -1, f[" + i + "] = " + f[i]);
		}
		ArrayList<Integer> L1;
		int d1 = d;
		
		ArrayList<Integer> D = new ArrayList<Integer>();
		D.add(d);
		
		while(true){
			d1 = findNextAvailableDate(d, inc);
			if(d1 < 0) break;
			
			if(L.size() <= 0){
				//solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "), "
				//		+ "d = " + d + ", d1 = " + d1 + ", innormal_L, sz = 0");
			}
			L1 = solver.getItemsOfBin(d1);
			
			// compute fd[d1]
			fd[d1] = Integer.MAX_VALUE;
			for(int i: L){
				if(solver.acceptDate(i, d1) && f[i] < Integer.MAX_VALUE){
					int delta1 = solver.getAddDeltaPackingViolations(i, d1);
					int delta = delta1 + f[i];
					if(delta1 == Integer.MAX_VALUE || f[i] == Integer.MAX_VALUE){
						//solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + 
						//		"), i = " + i + ", d = " + d + ", d1 = " + d1 + ", inormal_delta, delta1 = " + delta1 + ", f[" + i + "] = " + f[i]);
					}
					//if(i == 3056)
					//	solver.getSolver().getLog().println(name() + "::findOptimalMovePath, f[3056] = " + f[3056] + 
					//			", delta1 = " + delta1);
					if(fd[d1] > delta){
						fd[d1] = delta;
						pd[d1] = i; 
						solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + 
								"), update pd[" + d1 + "] = " + pd[d1]);
					}
				}
			}
			
			if(fd[d1] == Integer.MAX_VALUE){
				if(maxLength){
					sel_date = D.get(D.size()-1);
					best = fd[sel_date];
					solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "), fd[" + 
					d1 + "] = " + fd[d1] + ", BREAK");
				}
				break;
			}
			
			// date d1 is extended
			D.add(d1);
			
			
			solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "," + maxLength
					+ "), FIX fd[" + d1 + "] = " + fd[d1] + ", L.sz = " + L.size());
			
			// compute f[i] forall i in d1
			for(int i1: L1){
				f[i1] = Integer.MAX_VALUE;
				for(int i : L){
					if(solver.acceptDate(i, d1) && f[i] < Integer.MAX_VALUE){
						int delta1 = solver.getReplaceDeltaPackingViolations(i, i1);
						int delta = delta1 + f[i];
						///if(i1 == 3011 && i == 2746)
						//	solver.getSolver().getLog().println(name() + "::findOptimalMovePath, f[2746] = " + f[2746] + 
						//			", delta1 = " + delta1);
						//if(i1 == 3056 && i == 3011)
						//	solver.getSolver().getLog().println(name() + "::findOptimalMovePath, f[3011] = " + f[3011] +
						//			", delta1 = " + delta1);
						
						if(f[i1] > delta){
							f[i1] = delta;
							p[i1] = i;
							//solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "), update p[" + i1 + "] = " + p[i1]);
						}
					}
				}
			}
			
			// update best
			if(!maxLength){
				if(fd[d1] < best){
					best = fd[d1];
					sel_date = d1;
				}
			}else{
				// update only there is no item in date (bin) d1
				int d2 = findNextAvailableDate(d1, inc);
				//if(L1.size() == 0 || d2 < 0){
				if(solver.load[d1] <= 0 || L1.size() == 0 || d2 < 0){
					if(fd[d1] < best){
						best = fd[d1];
						sel_date = d1;
						solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + 
								"), final update sel_date = " + sel_date);
					}	
				}
			}
			//if(L1.size() == 0) break;
			if(solver.load[d1] <= 0 || L1.size() == 0) break;// reach empty date (bin)
			
			d = d1;
			L = L1;
		}
		if(sel_date >= 0){
			local_best = best;
			local_final_date = sel_date;
			local_moved_items.clear();
			int di = pd[sel_date];
			//local_moved_items.add(di);
			//System.out.println(name()+ "::findOptimalMovePath, establish moves, sel_d = " + sel_date + ", di = " + di);
			//solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "), establish moves, sel_d = " + sel_date + ", di = " + di);
			int count_inormal = 100000;
			int cc = 0;
			while(di >= 0){
				di = p[di];
				cc++;
				if(cc >= count_inormal) break;
			}
			boolean inormal = cc >= count_inormal;
			cc = 0;
			di = pd[sel_date];
			while(di >= 0){
				local_moved_items.add(di);
				di = p[di];
				cc++;
				//System.out.println(name()+ "::findOptimalMovePath, establish moves, sel_d = " + sel_date + 
				//		", continue di = " + di);
				
				if(inormal){
					solver.getSolver().getLog().println(name() + "::findOptimalMovePath(" + i_d + "," + inc + "), establish moves, sel_d = " + sel_date + 
							", inormal cc = " + cc + "  continue di = " + di);
				}
				if(cc >= count_inormal) break;
			}
		}		
	}
	private int findNextAvailableDate(int d, int inc){
		int pd = d+inc;
		while(pd >= 0 && pd < solver.m){
			if(!solver.forbiddenDate(pd)) break;
			pd = pd + inc;
		}
		if(pd >= solver.m || pd < 0) pd = -1;
		return pd;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
