package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.Arrays;

import routingdelivery.smartlog.containertruckmoocassigment.model.Permutation;

public class PermutationGenerator {
	private int n;
	private boolean[] mark; 
	private int[] x;
	private Permutation[] P;
	private int count;
	private void solution(){
		count++;
		int[] p = new int[n];
		System.arraycopy(x,0,p,0,n);
		P[count-1] = new Permutation(p);
		//System.out.println(count);
	}
	private void TRY(int k){
		for(int v = 0; v < n; v++){
			if(!mark[v]){
				x[k] = v;
				mark[v] = true;
				if(k == n-1) solution();
				else TRY(k+1);
				mark[v] = false;
			}
		}
	}
	public Permutation[] generate(int n){
		this.n = n;
		int sz = 1;
		for(int i = 1; i <= n; i++) sz = sz * i;
		x = new int[n];
		P = new Permutation[sz];
		count = 0;
		mark = new boolean[n];
		Arrays.fill(mark, false);
		TRY(0);	
		return P;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PermutationGenerator G = new PermutationGenerator();
		Permutation[] P = G.generate(4);
		for(int i = 0; i < P.length; i++){
			System.out.println(P[i].toString());
		}
	}

}
