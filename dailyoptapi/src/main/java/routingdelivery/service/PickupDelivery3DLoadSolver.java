package routingdelivery.service;

import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliverySolution;

public class PickupDelivery3DLoadSolver extends PickupDeliverySolver{
	
	@Override
	protected void greedyConstructMaintainConstraint(){
		
	}
	@Override
	public PickupDeliverySolution compute(PickupDeliveryInput input){
		this.input = input;
		
		return null;
		
	}
}
