package routingdelivery.model;

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
			for(int i = 0; i < sols.length; i++)
				L.add(sols[i]);
		solutions = new PickupDeliverySolution[L.size()];
		for(int i = 0; i < L.size(); i++)
			solutions[i] = L.get(i);
	}
	
	public PickupDeliveryMultiSolutions() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
