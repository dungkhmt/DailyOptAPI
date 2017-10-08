package routingdelivery.utils.clustering;

import java.util.*;
public class Cluster {
	private ArrayList<Integer> S;
	
	public Cluster(){
		S = new ArrayList<Integer>();
	}
	public void add(int x){
		S.add(x);
	}
	public int get(int i){
		return S.get(i);
	}
	public int size(){
		return S.size();
	}
	public boolean empty(){
		return S.size() == 0;
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
