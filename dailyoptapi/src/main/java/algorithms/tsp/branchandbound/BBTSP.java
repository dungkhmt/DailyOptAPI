package algorithms.tsp.branchandbound;

public class BBTSP {
	private double[][] D;
	private int maxTime;
	private int[] X;
	private double f;
	private int[] best_X;
	private double best_f;
	private boolean[] visited;
	private int n;
	private long t0;
	private double d_min;
	public String name(){
		return "BBTSP";
	}
	private void solution(){
		if(f + D[X[n-2]][X[n-1]] < best_f){
			best_f = f + D[X[n-2]][X[n-1]];
			for(int i = 1; i <= n-1; i++) best_X[i] = X[i];
			System.out.println(name() + "::solution updateBest best_f = " + best_f);
		}
	}
	private void TRY(int k){
		long t = System.currentTimeMillis() - t0;
		if(t > maxTime) return;
		for(int v = 1; v <= n-2; v++) if(!visited[v]){
			X[k] = v;
			visited[v] = true;
			f += D[X[k-1]][X[k]];
			if(k == n-2){
				solution();
			}else{
				double g = f + d_min*(n-1-k);
				if(g < best_f)
					TRY(k+1);
			}
			f -= D[X[k-1]][X[k]];
			visited[v] = false;
		}
	}
	public void solve(double[][] D, int maxTime){
		this.D = D;
		n = D.length;
		this.maxTime = maxTime;
		d_min = Integer.MAX_VALUE;
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++){
				if(i != j && D[i][j] < d_min) d_min = D[i][j];
			}
		
		t0 = System.currentTimeMillis();
		X = new int[n];
		best_X = new int[n];
		X[0] = 0; X[n-1] = n-1;
		visited = new boolean[n];
		for(int v = 0;  v < n; v++) visited[v] = false;
		visited[0] = true; visited[n-1] = true;
		f = 0;
		best_f = Integer.MAX_VALUE;
		TRY(1);
		System.out.print(name() + "::solve, n = " + n + ", D.length = " + D.length + ", d_min = " + d_min + ", best solution = ");
		for(int i = 0; i < best_X.length; i++) System.out.print(best_X[i] + " - ");
		System.out.println();
	}
	public int[] getSolution(){
		return best_X;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
