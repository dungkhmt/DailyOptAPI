package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Permutation {
	public int[] p;
	public Permutation(int[] p){
		this.p = p;
	}
	public String toString(){
		String s = "";
		for(int i = 0; i < p.length; i++)
			s = s + p[i] + " ";
		return s;
	}
}
