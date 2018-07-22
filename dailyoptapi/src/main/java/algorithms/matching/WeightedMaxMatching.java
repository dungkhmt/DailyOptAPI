package algorithms.matching;

public class WeightedMaxMatching extends MaxMatching {
	
	public double wf;
	public double best_wf;
	
	public WeightedMaxMatching(){
		super();
	}
	@Override
	protected void solution(){
		if(wf > best_wf){
			
			for(int i = 0; i < Z.length; i++)
				Z_best[i] = Z[i];
			best_wf = wf;
			System.out.print(name()+ "::solution, best_wf = " + best_wf + ", Z_best = ");
			for(int i = 0; i < Z_best.length; i++) System.out.print(Z_best[i] + ","); System.out.println();
			
		}
	}
	public void solve(int[] X, int[] Y, int[] edgeX, int[] edgeY, double[] w){
		// list of points X, Y
		// list of connection from X to Y: w[k] is the weight of (edgeX[k], edgeY[k])
		this.X = X; this.Y = Y; this.edgeX = edgeX; this.edgeY = edgeY; this.w = w;
		
		solveBackTrack(5000);
	}
	
	@Override
	public void solveBackTrack(int maxTime){
		this.maxTime = maxTime;
		mapData();
		Z = new int[nX];
		Z_best = new int[nX];
		wf = 0;
		best_wf = -1;
		used = new boolean[nY+1];
		for(int v = 0; v <= nY; v++) used[v] = false;
		t0 = System.currentTimeMillis();
		TRY(0);
		System.out.println(name() + "::solveBackTrack, f_best = " + f_best);
		
		if(best_wf > 0){
			int sz = 0;
			for(int i = 0; i < Z_best.length; i++)
				if(Z_best[i] < nY){
					sz++;
				}
			solX = new int[sz];
			solY = new int[sz];
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
	protected boolean check(int v, int i){
		if(v == nY) return true;
		if(used[i] && v != nY) return false;
		return !used[v] && a[i][v];
	}

	@Override
	protected void TRY(int i){
		
		double t = System.currentTimeMillis() - t0;
		if(t > maxTime) return;
		
		for(int v = 0; v <= nY; v++){
			if(check(v,i)){
				Z[i] = v;
				used[v] = true;
				if(v < nY){
					wf = wf + c[i][v];
				}
				
				if( i == nX-1){
					solution();
				}else{
					TRY(i+1);
				}
				//if(v < nY) f--;
				if(v < nY){
					wf = wf - c[i][v];
				}
				used[v] = false;
			}
		}
	}
	
	
	public String name(){
		return "WeightMaxMatching";
	}

}
