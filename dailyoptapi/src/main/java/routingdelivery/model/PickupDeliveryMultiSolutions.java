package routingdelivery.model;

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

	public PickupDeliveryMultiSolutions() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
