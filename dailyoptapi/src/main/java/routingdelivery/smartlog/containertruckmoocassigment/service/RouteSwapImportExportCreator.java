package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import utils.DateTimeUtils;

public class RouteSwapImportExportCreator {
	public ContainerTruckMoocSolver solver;
	
	public RouteSwapImportExportCreator(ContainerTruckMoocSolver solver){
		this.solver = solver;
	}
	public boolean checkSwapImportExport(Truck truck, Mooc mooc,
			ImportContainerRequest ir, ExportContainerRequest er){
		if(mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20)){
				if(ir.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY40)) return false;
				if(ir.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY45)) return false;
				if(er.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY40)) return false;
				if(er.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY45)) return false;
				return true;
		}else if(mooc.getCategory().equals(MoocCategoryEnum.CATEGORY40)){
			if(ir.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY45)) return false;
			if(er.getContainerCategory().equals(ContainerCategoryEnum.CATEGORY45)) return false;
			return true;
		} 
		return true;
	}
	public TruckRoute createSwapImportExport(Truck truck, Mooc mooc,
			ImportContainerRequest ir, ExportContainerRequest er) {
		// try to create route with truck-mooc serving ir and then er
		// truck-mooc -> port(ir) -> warehouse(ir) -> warehouse(er) -> port(er)
		// return null if violating constraint
		if(!checkSwapImportExport(truck, mooc, ir, er)) return null;
		int startTime = solver.mTruck2LastTime.get(truck);
		String truckLocationCode = solver.mTruck2LastDepot.get(truck)
				.getLocationCode();
		String moocLocationCode = solver.mMooc2LastDepot.get(mooc).getLocationCode();

		String portImCode = ir.getPortCode();
		Port portIm = solver.mCode2Port.get(portImCode);
		String portImLocationCode = portIm.getLocationCode();

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();

		RouteElement e0 = new RouteElement();
		L.add(e0);
		e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
		e0.setDepotTruck(solver.mTruck2LastDepot.get(truck));
		solver.mPoint2DepartureTime.put(e0, startTime);

		RouteElement e1 = new RouteElement();
		L.add(e1);
		e1.deriveFrom(e0);
		e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e1.setDepotMooc(solver.mMooc2LastDepot.get(mooc));
		int arrivalTime = startTime
				+ solver.getTravelTime(truckLocationCode, moocLocationCode);
		int serviceTime = Utils.MAX(arrivalTime, solver.mMooc2LastTime.get(mooc));
		int duration = e1.getDepotMooc().getPickupMoocDuration();
		int departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e1, arrivalTime);
		solver.mPoint2DepartureTime.put(e1, departureTime);

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(e1);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setPort(portIm);
		e2.setImportRequest(ir);
		arrivalTime = departureTime
				+ solver.getTravelTime(e1.getDepotMooc().getLocationCode(), e2
						.getPort().getLocationCode());

		if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort()))
			return null;

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
				.getEarlyDateTimePickupAtPort()));
		duration = ir.getLoadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(e2);
		e3.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		e3.setWarehouse(solver.mCode2Warehouse.get(ir.getWareHouseCode()));
		arrivalTime = departureTime
				+ solver.getTravelTime(e2.getPort().getLocationCode(), e3
						.getWarehouse().getLocationCode());

		if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimeUnloadAtWarehouse()))
			return null;

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
				.getEarlyDateTimeUnloadAtWarehouse()));
		duration = 0;// ir.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e3, arrivalTime);
		solver.mPoint2DepartureTime.put(e3, departureTime);

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		e4.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e4.setWarehouse(e3.getWarehouse());
		arrivalTime = departureTime + ir.getUnloadDuration();
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e4, arrivalTime);
		solver.mPoint2DepartureTime.put(e4, departureTime);

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(e4);
		e5.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e5.setWarehouse(solver.mCode2Warehouse.get(er.getWareHouseCode()));
		e5.setExportRequest(er);
		arrivalTime = departureTime
				+ solver.getTravelTime(e4.getWarehouse().getLocationCode(), e5
						.getWarehouse().getLocationCode());

		if (arrivalTime > DateTimeUtils.dateTime2Int(er
				.getLateDateTimeLoadAtWarehouse()))
			return null;

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(er
				.getEarlyDateTimeLoadAtWarehouse()));
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);

		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		e6.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		e6.setWarehouse(e5.getWarehouse());
		arrivalTime = departureTime + er.getLoadDuration();
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e6, arrivalTime);
		solver.mPoint2DepartureTime.put(e6, departureTime);

		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(e6);
		e7.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e7.setPort(solver.mCode2Port.get(er.getPortCode()));
		arrivalTime = departureTime
				+ solver.getTravelTime(e6.getWarehouse().getLocationCode(), e7
						.getPort().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(er
				.getLateDateTimeUnloadAtPort()))
			return null;
		serviceTime = arrivalTime;
		duration = er.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e7, arrivalTime);
		solver.mPoint2DepartureTime.put(e7, departureTime);

		RouteElement e8 = new RouteElement();
		L.add(e8);
		e8.deriveFrom(e7);
		e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e8.setDepotMooc(solver.findDepotForReleaseMooc(mooc));
		arrivalTime = departureTime
				+ solver.getTravelTime(e7.getPort().getLocationCode(), e8
						.getDepotMooc().getLocationCode());
		serviceTime = arrivalTime;
		duration = e8.getDepotMooc().getDeliveryMoocDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e8, arrivalTime);
		solver.mPoint2DepartureTime.put(e8, departureTime);
		solver.mMooc2LastDepot.put(mooc, e8.getDepotMooc());
		solver.mMooc2LastTime.put(mooc, departureTime);

		RouteElement e9 = new RouteElement();
		L.add(e9);
		e9.deriveFrom(e8);
		e9.setAction(ActionEnum.REST_AT_DEPOT);
		e9.setDepotTruck(solver.findDepotForReleaseTruck(truck));
		arrivalTime = departureTime
				+ solver.getTravelTime(e8.getDepotMooc().getLocationCode(), e9
						.getDepotTruck().getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e9, arrivalTime);
		solver.mPoint2DepartureTime.put(e9, departureTime);
		solver.mTruck2LastDepot.put(truck, e9.getDepotTruck());
		solver.mTruck2LastTime.put(truck, departureTime);

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(truck);

		solver.propagate(r);

		return r;

	}

}
