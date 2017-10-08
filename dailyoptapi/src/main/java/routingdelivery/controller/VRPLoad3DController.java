package routingdelivery.controller;


import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import routingdelivery.model.RoutingLoad3DInput;
import routingdelivery.model.RoutingLoad3DSolution;
import routingdelivery.service.RoutingLoad3DSolver;

import javax.servlet.http.HttpServletRequest;

@RestController
public class VRPLoad3DController {
	String ROOT_DIR = "C:/ezRoutingAPIRoot/";

	public String name() {
		return "VRPLoad3DController";
	}


	@RequestMapping(value = "/vrp-load3d", method = RequestMethod.POST)
	public RoutingLoad3DSolution computeVRPLoad3DSolution(
			HttpServletRequest request, @RequestBody RoutingLoad3DInput input) {
		String path = request.getServletContext().getRealPath(
				"ezRoutingAPIROOT");
		System.out.println(name() + "::computeVRP3DSolution, path = "
				+ path);
		
		double t0 = System.currentTimeMillis();
		RoutingLoad3DSolver solver = new RoutingLoad3DSolver();
		//return solver.solve(input);
		RoutingLoad3DSolution sol =  solver.solveBig(input);
		t0 = System.currentTimeMillis() - t0;
		t0 = t0*0.001;
		
		System.out.println("time = " + t0);
		return sol;

	}

}
