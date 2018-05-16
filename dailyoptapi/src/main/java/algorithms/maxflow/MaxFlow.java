package algorithms.maxflow;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import algorithms.graphs.Arc;
public class MaxFlow {
	public static final double EPS = 0.0000001;
	private Set<Integer> V;
	private Map<Integer, Set<Arc>> A;
	private int src;
	private int dest;
	
	private Map<Integer, Set<Arc>> Ar;// residual arcs
	private Map<Integer, Arc> pred;
	private Map<Arc, Arc> mResidualArc2OriginalArc;
	
	private boolean zero(double x){
		return Math.abs(x) < EPS;
	}
	private boolean equal(double x, double y){
		return Math.abs(x-y) < EPS;
	}
	private void buildResidualGraph(){
		Ar.clear();
		mResidualArc2OriginalArc.clear();
		for(int v: V) Ar.put(v,new HashSet<Arc>());
		for(int v: V){
			for(Arc a: A.get(v)){
				if(zero(a.f)){
					Arc ar = new Arc(v,a.to,a.w,0);
					Ar.get(v).add(ar);
					mResidualArc2OriginalArc.put(ar, a);
				}else if(equal(a.w,a.f)){
					Arc ar = new Arc(a.to,a.from,a.w,0);
					Ar.get(a.to).add(ar);
					mResidualArc2OriginalArc.put(ar,a);
				}else{
					Arc arf = new Arc(a.from,a.to,a.w-a.f,0);
					Arc arb = new Arc(a.to,a.from,a.f,0);
					Ar.get(v).add(arf);
					Ar.get(a.to).add(arb);
					mResidualArc2OriginalArc.put(arf, a);
					mResidualArc2OriginalArc.put(arb, a);
				}
			}
		}
	}
	private void findPathBFS(int s, int t, Map<Integer, Set<Arc>> A){
		System.out.println("findPathBFS");
		
		Queue Q = new LinkedList();
		pred.clear();
		pred.put(s, new Arc(0,0,0,0));
		try{
			Q.add(s);
			while(Q.size() > 0){
				int v = (int)Q.remove();
				for(Arc a: A.get(v)){
					int u = a.to;
					if(pred.get(u) == null){
						pred.put(u, a);
						Q.add(u);
						if(u == t) break;
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private void printResidualGraph(){
		for(int v: V){
			System.out.print("node " + v + ": ");
			for(Arc a: Ar.get(v)){
				System.out.print("[" + a.to + ", w = " + a.w + "] ");
			}
			System.out.println();
		}
	}
	private void printOrgGraph(){
		for(int v: V){
			System.out.print("node " + v + ": ");
			for(Arc a: A.get(v)){
				System.out.print("[" + a.to + ", w = " + a.w + ", f = " + a.f + "] ");
			}
			System.out.println();
		}
	}
	
	public void solve(Set<Integer> V, Map<Integer, Set<Arc>> A, int src, int dest){
		System.out.println("Solve.....");
		this.V = V; this.A = A; this.src = src; this.dest = dest;
		Ar = new HashMap<Integer, Set<Arc>>();
		mResidualArc2OriginalArc = new HashMap<Arc, Arc>();
		pred = new HashMap<Integer, Arc>();
		// init flow 0
		for(int v: V){
			for(Arc a: A.get(v)){
				a.f = 0;
			}
		}
		double f = 0;
		while(true){
			buildResidualGraph();
			
			//System.out.println("residual"); printResidualGraph();
			findPathBFS(src,dest,Ar);
			if(pred.get(dest) == null){
				System.out.println("Cannot find path --> BREAK");
				break;
			}
			
			// augment the flow
			double delta = Integer.MAX_VALUE;
			int x = dest;
			while(x != src){
				Arc a = pred.get(x);
				delta = delta > a.w ? a.w : delta;
				x = a.from;
			}
			f = f + delta;
			System.out.println("Augmenting " + delta + ", f = " + f);
			x = dest;
			while(x != src){
				Arc a = pred.get(x);
				Arc ma = mResidualArc2OriginalArc.get(a);
				if(a.from == ma.from && a.to == ma.to){
					// forward arc
					ma.f = ma.f + delta;
				}else{
					// backward arc
					ma.f = ma.f - delta;
				}
				x = a.from;
			}
			//System.out.println("flow"); printOrgGraph();
			System.out.println("----------------------------------");
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Queue Q= new LinkedList();
		for(int i = 1; i <= 5; i++){
			Q.add(i);
		}
		while(Q.size() > 0){
			int x = (int)Q.remove();
			System.out.println(x);
		}
	}

}
