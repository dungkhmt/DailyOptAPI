package com.test;

import javax.servlet.http.HttpServletRequest;





//import org.apache.log4j.Logger;
//import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliverySolution;
import routingdelivery.service.PickupDeliverySolver;
import routingdelivery.smartlog.brenntag.model.BrennTagPickupDeliveryInput;


@RestController
public class TestAPI {
	@RequestMapping(value = "/basic", method = RequestMethod.POST)
	public TestSolution getFields(HttpServletRequest request, @RequestBody TestInput input) {
		int c = input.getA() + input.getB();
		c = c * 1000;
		return new TestSolution(c);
	}

	@RequestMapping(value = "/pickup-delivery", method = RequestMethod.POST)
	public PickupDeliverySolution computePickupDeliverySolution(HttpServletRequest request, 
			@RequestBody BrennTagPickupDeliveryInput input){
		
		PickupDeliverySolver solver = new PickupDeliverySolver();
		//CVRPTWSolver solver = new CVRPTWSolver();
		return solver.compute(input);
		
	}
	
}
