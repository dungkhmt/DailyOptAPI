package routingdelivery.utils.clustering;

public class KMedoids {

	public Cluster[] cluster(double[][] d, int sz){
		// cluster items 0, 1, ..., n-1 into clusters of size at most sz
		int n = d.length;
		int K = n/sz;
		if(n%sz > 0) K = K + 1;
		Cluster[] S = new Cluster[K];
		
		
		
		return S;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
