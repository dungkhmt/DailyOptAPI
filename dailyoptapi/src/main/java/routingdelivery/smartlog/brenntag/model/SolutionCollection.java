package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;

import routingdelivery.model.PickupDeliverySolution;

public class SolutionCollection {
	public SolutionCollection(){
		solutions = new ArrayList<PickupDeliverySolution>();
	}
	private ArrayList<PickupDeliverySolution> solutions;
	public void add(PickupDeliverySolution s){
		solutions.add(s);
	}
	public void remove(int i){
		solutions.remove(i);
	}
	public PickupDeliverySolution get(int i){
		return solutions.get(i);
	}
	public PickupDeliverySolution getLast(){
		if(size() == 0) return null;
		return solutions.get(size()-1);
	}
	
	public int size(){
		return solutions.size();
	}
	
}
