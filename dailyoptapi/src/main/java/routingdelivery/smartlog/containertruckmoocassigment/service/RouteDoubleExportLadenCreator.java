package routingdelivery.smartlog.containertruckmoocassigment.service;

import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;

public class RouteDoubleExportLadenCreator {
	public ContainerTruckMoocSolver solver;
	public ExportContainerRequest sel_exReq_a;
	public ExportContainerRequest sel_exReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;
	public Container sel_container_a;
	public Container sel_container_b;

	public RouteDoubleExportLadenCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleExportLadenCreator";
	}
}
