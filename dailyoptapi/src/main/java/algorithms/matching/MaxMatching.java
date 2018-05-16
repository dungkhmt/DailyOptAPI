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
	private int[] X;
	private	int[] Y;
	private int[] edgeX;
	private int[] edgeY;
	private double[] w;
	
	public void solve(int[] X, int[] Y, int[] edgeX, int[] edgeY, double[] w){
		// list of points X, Y
		// list of connection from X to Y: w[k] is the weight of (edgeX[k], edgeY[k])
		this.X = X; this.Y = Y; this.edgeX = edgeX; this.edgeY = edgeY; this.w = w;
		
		Set<Integer> V = new HashSet<Integer>();
		Map<Integer, Set<Arc>> A = new HashMap<Integer, Set<Arc>>();
		for(int i = 0; i < X.length; i++){
			V.add(X[i]);
			A.put(X[i], new HashSet<Arc>());
		}
		for(int i = 0; i < Y.length; i++){
			V.add(Y[i]);
			A.put(Y[i], new HashSet<Arc>());
		}
		
		double maxW = 0;
		for(int k = 0; k < edgeX.length; k++){
			int x = edgeX[k];
			int y = edgeY[k];
			Arc a = new Arc(x,y,w[k],0);
			A.get(x).add(a);
			if(maxW < w[k]) maxW = w[k];
		}
		int src = 0;
		int dest = 12;
		V.add(src);
		V.add(dest);
		A.put(src, new HashSet<Arc>());
		A.put(dest, new HashSet<Arc>());
		for(int i = 0; i < X.length; i++){
			Arc a = new Arc(src,X[i],maxW,0);
			A.get(src).add(a);
		}
		for(int i = 0; i < Y.length; i++){
			Arc a = new Arc(Y[i],dest,maxW,0);
			A.get(Y[i]).add(a);
		}
		MaxFlow MF = new MaxFlow();
		MF.solve(V, A, src, dest);
		
		System.out.println("RESULT");
		for(int i = 0; i < X.length; i++){
			int v = X[i];
			for(Arc a: A.get(v)){
				if(a.f > 0){
					System.out.println(a.from + "," + a.to);
				}
			}
		}
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
