package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;

public class RouteDoubleImportLadenCreator {
	public ContainerTruckMoocSolver solver;
	public ImportContainerRequest sel_imReq_a;
	public ImportContainerRequest sel_imReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;

	public RouteDoubleImportLadenCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportLadenCreator";
	}
}
