package algorithms.matching;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import algorithms.graphs.Arc;
import algorithms.maxflow.MaxFlow;

public class MaxMatching {
	protected int[] X;
	protected	int[] Y;
	protected int[] edgeX;
	protected int[] edgeY;
	protected double[] w;
	
	protected int[] solX;
	protected int[] solY;// (solX[i], solY[i]) is a matching in the solution
	
	// mapped data
	protected int nX;
	protected int nY;
	protected boolean[][] a;// a[i][j] = T --> is edge(i,j)
	protected double[][] c;// c[i][j] = weight of (i,j)
	protected HashMap<Integer, Integer> mX2Index;
	protected HashMap<Integer, Integer> mY2Index;
	
	// data structure for backtrack search
	protected int[] Z;// Z[i] = k means that X[i] is matched with Y[k]
	protected int[] Z_best;
	protected boolean[] used;
	protected int f;
	protected int f_best;
	protected int maxTime;
	protected double t0;
	
	public void mapData(){
		System.out.println(name() + "::mapData INFO");
		System.out.print("X = "); for(int i = 0; i < X.length; i++) System.out.print(X[i] + ", "); System.out.println();
		System.out.print("Y = "); for(int i = 0; i < Y.length; i++) System.out.print(Y[i] + ", "); System.out.println();
		for(int k = 0; k < edgeX.length; k++) System.out.println("edge[" + k + "] = " + edgeX[k] + "," + edgeY[k]);
		
		nX = X.length;
		nY = Y.length;
		a = new boolean[nX][nY];
		c = new double[nX][nY];
		for(int i = 0; i < nX; i++)
			for(int j = 0; j < nY; j++){
				a[i][j] = false;
				c[i][j] = 0;
			}
		mX2Index = new HashMap<Integer, Integer>();
		mY2Index = new HashMap<Integer, Integer>();
		for(int i = 0; i < X.length; i++) mX2Index.put(X[i], i);
		for(int i = 0; i < Y.length; i++) mY2Index.put(Y[i], i);
		
		for(int k = 0; k < edgeX.length; k++){
			int i = mX2Index.get(edgeX[k]);
			int j = mY2Index.get(edgeY[k]);
			a[i][j] = true;
			c[i][j] = w[k];
			//a[j][i] = true;
		}
		//System.out.println(name() + "::mapData, a = ");
		//for(int i = 0; i < nX; i++){
		//	for(int j = 0; j < nY; j++)
		//		System.out.print(a[i][j] + " ");
		//	System.out.println();
		//}
			
	}
	protected boolean check(int v, int i){
		if(v == nY) return true;
		//if(used[i] && v != nY) return false;
		return !used[v] && a[i][v];
	}
	protected void solution(){
		if(f > f_best){
			
			for(int i = 0; i < Z.length; i++)
				Z_best[i] = Z[i];
			f_best = f;
			System.out.print(name()+ "::solution, f_best = " + f_best + ", Z_best = ");
			for(int i = 0; i < Z_best.length; i++) System.out.print(Z_best[i] + ","); System.out.println();
			
		}
	}
	protected void TRY(int i){
		double t = System.currentTimeMillis() - t0;
		if(t > maxTime) return;
		
		for(int v = 0; v <= nY; v++){
			if(check(v,i)){
				Z[i] = v;
				used[v] = true;
				if(v < nY) f++;
				
				if( i == nX-1){
					solution();
				}else{
					TRY(i+1);
				}
				if(v < nY) f--;
				used[v] = false;
			}
		}
	}
	public void solveBackTrack(int maxTime){
		this.maxTime = maxTime;
		mapData();
		Z = new int[nX];
		Z_best = new int[nX];
		f = 0;
		f_best = -1;
		used = new boolean[nY+1];
		for(int v = 0; v <= nY; v++) used[v] = false;
		t0 = System.currentTimeMillis();
		TRY(0);
		if(f_best > 0){
			solX = new int[f_best];
			solY = new int[f_best];
			int idx = -1;
			System.out.println(name() + "::solveBackTrack, FOUND solution");
			for(int i = 0; i < Z_best.length; i++)
				if(Z_best[i] < nY){
					idx++;
					solX[idx] = X[i];
					solY[idx] = Y[Z_best[i]];
					System.out.println(name() + "::solveBackTrack, FOUND solution: " + solX[idx] + " -- " + solY[idx]);
				}
		}
	}
	public String name(){
		return "MaxMatching";
	}
	public void solve(int[] X, int[] Y, int[] edgeX, int[] edgeY, double[] w){
		// list of points X, Y
		// list of connection from X to Y: w[k] is the weight of (edgeX[k], edgeY[k])
		this.X = X; this.Y = Y; this.edgeX = edgeX; this.edgeY = edgeY; this.w = w;
		
		solveBackTrack(5000);
		if(true) return;
		
		// map Y to Y1 so that nodes are different
		int max = 0;
		for(int i = 0; i < X.length; i++){
			if(max < X[i]) max = X[i];
			if(max < Y[i]) max = Y[i];
		}
		int[] Y1 = new int[Y.length];
		HashMap<Integer, Integer> mY2Y1 = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> mY12Y = new HashMap<Integer, Integer>();
		for(int i = 0; i < Y.length; i++){
			max++;
			Y1[i] = max;
			mY2Y1.put(Y[i], Y1[i]);
			mY12Y.put(Y1[i], Y[i]);
		}
		
		
		Set<Integer> V = new HashSet<Integer>();
		Map<Integer, Set<Arc>> A = new HashMap<Integer, Set<Arc>>();
		for(int i = 0; i < X.length; i++){
			V.add(X[i]);
			A.put(X[i], new HashSet<Arc>());
		}
		for(int i = 0; i < Y1.length; i++){
			V.add(Y1[i]);
			A.put(Y1[i], new HashSet<Arc>());
		}
		
		double maxW = 0;
		for(int k = 0; k < edgeX.length; k++){
			int x = edgeX[k];
			int y = edgeY[k];
			int y1 = mY2Y1.get(y);
			
			Arc a = new Arc(x,y1,w[k],0);
			A.get(x).add(a);
			if(maxW < w[k]) maxW = w[k];
		}
		int src = max + 1;
		int dest = max + 2;
		
		V.add(src);
		V.add(dest);
		A.put(src, new HashSet<Arc>());
		A.put(dest, new HashSet<Arc>());
		for(int i = 0; i < X.length; i++){
			Arc a = new Arc(src,X[i],maxW,0);
			A.get(src).add(a);
		}
		for(int i = 0; i < Y1.length; i++){
			Arc a = new Arc(Y1[i],dest,maxW,0);
			A.get(Y1[i]).add(a);
		}
		System.out.println(name() + "::solve, src = " + src + ", dest = " + dest);
		for(int v: V){
			System.out.print("Node " + v + ": ");
			for(Arc a: A.get(v)){
				System.out.print(a.toString() + ", ");
			}
			System.out.println();
		}
		MaxFlow MF = new MaxFlow();
		MF.solve(V, A, src, dest);
		
		ArrayList<Integer> l_sol_x = new ArrayList<Integer>();
		ArrayList<Integer> l_sol_y = new ArrayList<Integer>();
		
		System.out.println("RESULT");
		for(int i = 0; i < X.length; i++){
			int v = X[i];
			for(Arc a: A.get(v)){
				if(a.f > 0 && a.from != src && a.to != dest){
					System.out.println(a.from + "," + mY12Y.get(a.to));
					
					l_sol_x.add(a.from);
					l_sol_y.add(mY12Y.get(a.to));
				}
			}
		}
		solX = new int[l_sol_x.size()];
		solY = new int[l_sol_x.size()];
		for(int i = 0; i < l_sol_x.size(); i++){
			solX[i] = l_sol_x.get(i);
			solY[i] = l_sol_y.get(i);
		}
	}
	public int[] getSolutionX(){
		return solX;
	}
	public int[] getSolutionY(){
		return solY;
	}
	public void genGraph(String fn, int N, int M, int K){
		try{
			Random R = new Random();
			boolean[][] m = new boolean[5000][5000];
			for(int i = 0; i < 5000; i++)
				for(int j = 0; j < 5000; j++)
					m[i][j] = false;
			
			PrintWriter out= new PrintWriter(fn);
			for(int i = 1; i <= N; i++){
				out.println(i + " ");
			}
			out.println(-1);
			for(int j = 1; j <= M; j++){
				int x = j+N;
				out.println(x + " ");
				System.out.println(x);
			}
			out.println(-1);
			for(int k = 1; k <= K; k++){
				int x = -1;
				int y = -1;
				do{
					x = R.nextInt(N)+1;
					y = R.nextInt(M)+ 1 + N;
				}while(m[x][y-N] == true);
				out.println(x + " " + y);
				m[x][y-N] = true;
				
			}
			out.print(-1);
			out.close();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*
		int[] X = {1,2,3,4,5};
		int[] Y = {6,7,8,9,10,11};
		int[] edgeX = {1,  1, 1, 2,  2, 3, 3, 4, 4, 4, 5};
		int[] edgeY = {11, 6, 8, 11, 6, 7, 9, 6, 7, 8, 9};
		//double[] w =  {3,  1, 4, 2,  8, 9, 6, 2, 5, 7, 4};
		double[] w =  {1,  1, 1, 1,  1, 1, 1, 1, 1, 1, 1};
		
		MaxMatching s = new MaxMatching();
		s.solve(X, Y, edgeX, edgeY, w);
		*/
		//s.genGraph("matching-1000-1000.txt", 1000, 1000, 100000);
		
		ArrayList<Integer> tX = new ArrayList<Integer>();
		ArrayList<Integer> tY = new ArrayList<Integer>();
		ArrayList<Integer> eX = new ArrayList<Integer>();
		ArrayList<Integer> eY = new ArrayList<Integer>();
		
		try{
			Scanner in = new Scanner(new File("matching-1000-1000.txt"));
			while(true){
				int v = in.nextInt();
				if(v == -1) break;
				tX.add(v);
			}
			while(true){
				int v = in.nextInt();
				if(v == -1) break;
				tY.add(v);
			}
			while(true){
				int u;int v;
				u = in.nextInt();
				if(u == -1) break;
				v = in.nextInt();
				eX.add(u);
				eY.add(v);
			}
			int[] X = new int[tX.size()];
			int[] Y = new int[tY.size()];
			int[] edgeX = new int[eX.size()];
			int[] edgeY = new int[eY.size()];
			for(int i = 0; i < tX.size(); i++)
				X[i] = tX.get(i);
			for(int i = 0; i < tY.size(); i++)
				Y[i] = tY.get(i);
			for(int k = 0; k < eX.size(); k++){
				edgeX[k] = eX.get(k);
				edgeY[k] = eY.get(k);
			}
			in.close();
			double[] w = new double[edgeX.length];
			for(int i = 0; i < w.length; i++) w[i] = 1;
			MaxMatching s = new MaxMatching();
			s.solve(X, Y, edgeX, edgeY, w);
				
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

}
