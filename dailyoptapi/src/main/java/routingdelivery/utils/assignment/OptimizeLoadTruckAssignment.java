package routingdelivery.utils.assignment;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;

public class OptimizeLoadTruckAssignment {
	protected int[] truck;
	protected double[] load;
	protected double[] cap;
	protected boolean[][] forbidden;
	protected int[] x;// x[i] is the vehicle assigned to route (load) i in the reassignment
	protected double[] evaluation;
	protected int[] best_x;
	protected double[] best_evaluation;
	protected int n;
	protected boolean[] truckUsed;
	Random R = new Random();
	PrintWriter log = null;
	
	protected void initLog(){
		try{
			log = new PrintWriter("C:/tmp/OptimizeTruckLoadAssignment.txt");
		}catch(Exception ex){
			ex.printStackTrace();
			log = null;
		}
	}
	protected void finalize(){
		if(log != null) log.close();
	}
	protected double[] evaluation(){
		double load_ratio[] = {0,0};
		
		for(int i = 0; i < n; i++){
			if(x[i] >= 0 && load[i] > 0){
				//double r = cap[x[i]]*1.0/load[i];
				double r = cap[x[i]]-load[i];
				if(load_ratio[0] < r) load_ratio[0] = r;
				load_ratio[1] += r;
			}
		}
		return load_ratio;
	}
	protected double[] evaluationAssign(int i, int v){
		int oldi = x[i];
		x[i] = v;
		double[] e= evaluation();
		x[i]=oldi;
		return e;
	}
	protected double[] evaluationSwap(int i, int j){
		// return the evaluation if x[i] <-> x[j]
		int oldi = x[i]; 
		int oldj = x[j];
		int tmp = x[i]; x[i] = x[j]; x[j] = tmp;
		double[] e = evaluation();
		x[i] = oldi; x[j] = oldj;
		return e;
	}
	protected boolean betterThan(double[] a, double[] b){
		for(int i=0; i < a.length; i++)
			if(a[i] < b[i]) return true;
			else if(a[i] > b[i]) return false;
		return false;
	}
	protected boolean moveAssign(){
		// return true if found an assign move improvement
		double[] bestEval = {Integer.MAX_VALUE,Integer.MAX_VALUE};
		int sel_i = -1;
		int sel_v = -1;
		boolean found = false;
		for(int i = 0; i < n; i++)if(x[i] >= 0){
			for(int v = 0; v < n; v++)if(!truckUsed[v]){
				if(forbidden[v][i]) continue;
				if(load[i] > cap[v]) continue;
				double[] e = evaluationAssign(i, v);
				//System.out.println("moveAssign(" + i + "," + v + "), e = " + e[0] + "," + e[1]);
				if(log != null)log.println("moveAssign(" + i + "," + v + "), e = " + e[0] + "," + e[1]);
				if(betterThan(e,bestEval) && betterThan(e,evaluation)){
					bestEval = e;
					sel_i = i; sel_v = v;
					found = true;
				}
			}
		}
		if(found){
			truckUsed[x[sel_i]] = false;
			truckUsed[sel_v] = true;
			x[sel_i] = sel_v;
			evaluation = evaluation();
			updateBest();
		}
		return found;
	}
	protected boolean moveSwap(){
		// return true if found a swap move improvement
		double[] bestEval = {Integer.MAX_VALUE,Integer.MAX_VALUE};
		int sel_i = -1;
		int sel_j = -1;
		boolean found = false;
		for(int i = 0; i < n; i++)if(x[i] >= 0){
			for(int j = i+1; j < n; j++)if(x[j] >= 0){
				if(forbidden[x[i]][j] || forbidden[x[j]][i]) continue;
				if(load[i] > cap[x[j]] || load[j] > cap[x[i]]) continue;
				
				double[] e = evaluationSwap(i, j);
				//System.out.println("moveSwap(" + i + "," + j + "), e = " + e[0] + "," + e[1]);
				if(log != null)log.println("moveSwap(" + i + "," + j + "), e = " + e[0] + "," + e[1]);
				if(betterThan(e,bestEval) && betterThan(e,evaluation)){
					bestEval = e;
					sel_i = i; sel_j = j;
					found = true;
				}
			}
		}
		if(found){
			int tmp = x[sel_i];
			x[sel_i] = x[sel_j];
			x[sel_j] = tmp;
			evaluation = evaluation();
			updateBest();
		}
		return found;
	}
	public int[] solve(int[] truck, double[] load, double[] cap, boolean[][] forbidden, int maxTime){
		System.out.println("OptimizeLoadTruckAssignment::solve + maxTime = " + maxTime);
		initLog();
		// input: load[i] of route i, route[i] is served by vehicle i
		// input: cap[i] are load and capacity on vehicle i (load[i] = 0 means vehicle is not in service)
		// input: truck[i] is the index of vehicle serving route i
		// input: forbidden[i][j] = T if vehicle i cannot serve route (load) i
		// output: x[i] the vehicle serving route i
		this.load = load;
		this.cap = cap;
		this.forbidden = forbidden;
		this.truck = truck;
		n = load.length;
		x = new int[n];
		best_x = new int[n];
		truckUsed = new boolean[n];
		for(int v = 0; v < n; v++) truckUsed[v] = false;
		for(int i = 0; i < n; i++){
			x[i] = truck[i];
			if(x[i] >= 0)
			truckUsed[x[i]] = true;
			best_x[i] = x[i];
			evaluation = evaluation();
			best_evaluation = evaluation;
			if(log != null)log.println("load[" + i + "] = " + load[i] + ", cap[" + i + "] = " + cap[i]);
		}
		if(log != null)log.println("init evaluation = " + evaluation[0] + "," + evaluation[1]);
		//restart();
		
		double t0 = System.currentTimeMillis();
		while(true){
			double t = System.currentTimeMillis() - t0;
			//if(t > maxTime) break;
			boolean improve = moveAssign();
			if(!improve)
				improve = moveSwap();
			
			if(!improve)	break;
			//if(!improve) restart();
		}
		finalize();
		System.out.println("OptimizeLoadTruckAssignment::solve, result = " + getSolutionString());
		return null;
	}
	protected void updateBest(){
		if(betterThan(evaluation, best_evaluation)){
			best_evaluation = evaluation;
			for(int i = 0; i < n; i++) best_x[i] = x[i];
			System.out.println("OptimizeLoadTruckAssignment::updateBest evaluation = " + best_evaluation[0] + "," + evaluation[1]);
		}

	}
	protected void restart(){
		for(int k = 0; k < 20; k++){
			for(int iter = 0; iter < 50; iter++){
				int i = R.nextInt(n);
				int j = R.nextInt(n);
				if(i != j && x[i] >= 0 && x[j] >= 0 && !forbidden[x[i]][j] && 
						!forbidden[x[j]][i] && load[i] <= cap[x[j]] && load[j] <= cap[x[i]]){
					int tmp = x[i]; x[i] = x[j]; x[j] = tmp;
					break;
				}
			}
		}
		evaluation = evaluation();
		updateBest();
		/*
		for(int v = 0; v < n; v++) truckUsed[v] = false;
		ArrayList<Integer> cand = new ArrayList<Integer>();
		for(int v = 0;v < n; v++)
			cand.add(v);
		
		for(int i = 0; i < n; i++)if(x[i] >= 0){
			int idx = R.nextInt(cand.size());
			x[i] = cand.get(idx);
			truckUsed[x[i]] = true;
			cand.remove(idx);
			evaluation = evaluation();
			updateBest();
		}
		*/
	}
	public String getSolutionString(){
		String s= "";
		for(int i = 0; i < best_x.length; i++)
			s += best_x[i] + ",";
		return s;
	}
	public int[] getSolution(){
		return best_x;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		double[] load = {4,0,5,3,4,0};
		double[] cap = {10,6,5,20,30,7};
		int[] truck = {2,-1,4,1,3,-1};
		boolean[][] forbidden = {
				{false,false,false,false,false,false},
				{false,false,false,false,false,false},
				{false,false,false,false,false,false},
				{false,false,false,false,false,false},
				{false,false,false,false,false,false},
				{false,false,false,false,false,false}
		};
		OptimizeLoadTruckAssignment solver = new OptimizeLoadTruckAssignment();
		solver.solve(truck, load, cap, forbidden, 5000);
		int[] s = solver.getSolution();
		for(int i = 0; i < s.length; i++)if(s[i] >=0){
			System.out.println("s[" + i + "] = " + s[i] + ", " + load[i] + "/" + cap[s[i]]);
		}
	}

}
