package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;

import routingdelivery.model.ConfigParams;
import routingdelivery.model.PickupDeliverySolution;

public class SolutionCollection {
	private int maxSize;
	private ArrayList<PickupDeliverySolution> solutions;
	
	
	public SolutionCollection(int maxSize){
		this.maxSize = maxSize;
		solutions = new ArrayList<PickupDeliverySolution>();
	}
	
	public PickupDeliverySolution selectBest(ConfigParams params){
		if(size() == 0) return null;
		PickupDeliverySolution sel_sol = solutions.get(0);
		
		if(params.getInternalVehicleFirst().equals("Y")){
			for(int i = 1; i < solutions.size(); i++){
				PickupDeliverySolution s = solutions.get(i);
				if(s.getStatistic().getIndicator().getNbInternalTrucks() > sel_sol.getStatistic().getIndicator().getNbInternalTrucks()){
					sel_sol = s;
				}else if(s.getStatistic().getIndicator().getNbInternalTrucks() == 
						sel_sol.getStatistic().getIndicator().getNbInternalTrucks() &&
						s.getStatistic().getIndicator().getDistance() < sel_sol.getStatistic().getIndicator().getDistance()){
					sel_sol = s;
				}
			}
		}else{
			for(int i = 1; i < solutions.size(); i++){
				PickupDeliverySolution s = solutions.get(i);
					if(s.getStatistic().getIndicator().getDistance() < sel_sol.getStatistic().getIndicator().getDistance()){
					sel_sol = s;
				}
			}
		}
		return sel_sol;
	}
	public void add(PickupDeliverySolution s, ConfigParams params){
		if(solutions.size() < maxSize){
			solutions.add(s);
		}else{
			for(int i = 0; i < solutions.size(); i++){
				PickupDeliverySolution si = solutions.get(i);
				if(s.getStatistic().getIndicator().better(si.getStatistic().getIndicator(), params)){
					solutions.remove(i);
					solutions.add(s);
					break;
				}
			}
		}
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
