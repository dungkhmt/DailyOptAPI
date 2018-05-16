package routingdelivery.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.service.CVRPTWSolver;
import routingdelivery.service.PickupDeliverySolver;

@RestController
public class PickupDeliveryController {
	@RequestMapping(value = "/pickup-delivery", method = RequestMethod.POST)
	public PickupDeliverySolution computePickupDeliverySolution(HttpServletRequest request, 
			@RequestBody PickupDeliveryInput input){
		
		//PickupDeliverySolver solver = new PickupDeliverySolver();
		CVRPTWSolver solver = new CVRPTWSolver();
		return solver.compute(input);
		
	}
}
