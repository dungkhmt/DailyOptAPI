package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import java.util.ArrayList;

public class PickupDeliveryMultiSolutions {
	private PickupDeliverySolution[] solutions;
	
	public PickupDeliverySolution[] getSolutions() {
		return solutions;
	}

	public void setSolutions(PickupDeliverySolution[] solutions) {
		this.solutions = solutions;
	}

	public PickupDeliveryMultiSolutions(PickupDeliverySolution[] solutions) {
		super();
		this.solutions = solutions;
	}
	public void addSolutions(PickupDeliverySolution[] sols){
		ArrayList<PickupDeliverySolution> L = new ArrayList<PickupDeliverySolution>();
		if(solutions != null)
			for(int i = 0; i<solutions.length; i++)
				L.add(solutions[i]);
		if(sols != null)
			for(int i = 0; i < sols.length; i++){
				boolean ok = true;
				for(PickupDeliverySolution s: L){
					if(sols[i].getStatistic().getIndicator().equal(s.getStatistic().getIndicator())){
						ok = false; break;
					}
				}
				if(ok)
					L.add(sols[i]);
			}
		solutions = new PickupDeliverySolution[L.size()];
		for(int i = 0; i < L.size(); i++)
			solutions[i] = L.get(i);
	}
	public void sortIncreasingOrderDistance(){
		if(solutions == null || solutions.length == 0) return;
		for(int i = 0; i < solutions.length; i++){
			for(int j = i+1; j < solutions.length; j++){
				if(solutions[i].getStatistic().getIndicator().getDistance() > solutions[j].getStatistic().getIndicator().getDistance()){
					PickupDeliverySolution tmp = solutions[i];
					solutions[i] = solutions[j];
					solutions[j] = tmp;
				}
			}
		}
	}
	public PickupDeliveryMultiSolutions() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
