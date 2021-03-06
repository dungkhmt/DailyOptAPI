package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;

public class ContainerTruckMoocService {

	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		//GreedyDirectServiceSolver solver = new GreedyDirectServiceSolver();
		//return solver.solve(input);
		
		//if(true) return new ContainerTruckMoocSolution();
		InitGreedyImproveSpecialOperatorSolver solver = new InitGreedyImproveSpecialOperatorSolver();
		return solver.solve(input);
		
		//return solver.solveDirect(input);
	}
	
	public ContainerTruckMoocSolution compare(ContainerTruckMoocInput input) {
		//GreedyDirectServiceSolver solver = new GreedyDirectServiceSolver();
		//return solver.solve(input);
		
		//if(true) return new ContainerTruckMoocSolution();
		InitGreedyImproveSpecialOperatorSolver solver = new InitGreedyImproveSpecialOperatorSolver();
		return solver.compare(input);
		
		//return solver.solveDirect(input);
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
