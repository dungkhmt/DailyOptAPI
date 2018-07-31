package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;

public class ContainerTruckMoocService {

	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		GreedyDirectServiceSolver solver = new GreedyDirectServiceSolver();
		return solver.solve(input);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
