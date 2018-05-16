package algorithms.graphs;

public class Arc{
	public int from;
	public int to;
	public double w;// weight
	public double f;// flow
	public Arc(int from, int to, double w, double f){
		this.from = from; this.to = to; this.w = w; this.f = f;
	}
}
