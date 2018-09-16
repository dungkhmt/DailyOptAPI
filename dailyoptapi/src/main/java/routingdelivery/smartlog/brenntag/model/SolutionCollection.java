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
	public PickupDeliverySolution get(int i){
		return solutions.get(i);
	}
	public int size(){
		return solutions.size();
	}
}
