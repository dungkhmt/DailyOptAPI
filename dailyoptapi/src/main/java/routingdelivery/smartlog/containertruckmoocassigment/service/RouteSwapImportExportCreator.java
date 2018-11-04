package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import utils.DateTimeUtils;

public class RouteSwapImportExportCreator {
	public ContainerTruckMoocSolver solver;

	public RouteSwapImportExportCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public boolean checkSwapImportExport(Truck truck, Mooc mooc,
			ImportContainerRequest ir, ExportContainerRequest er) {
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20)) {
			if (ir.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY40))
				return false;
			if (ir.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				return false;
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY40))
				return false;
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				return false;
			return true;
		} else if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY40)) {
			if (ir.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				return false;
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				return false;
			return true;
		}
		return true;
	}

	public String name(){
		return "RouteSwapImportExportCreator";
	}
	public TruckRouteInfo4Request createSwapImportExport(Truck truck,
			Mooc mooc, ImportContainerRequest ir, ExportContainerRequest er) {
		// try to create route with truck-mooc serving ir and then er
		// truck-mooc -> port(ir) -> warehouse(ir) -> warehouse(er) -> port(er)
		// return null if violating constraint
		if (!checkSwapImportExport(truck, mooc, ir, er))
			return null;
		int startTime = solver.mTruck2LastTime.get(truck);
		String truckLocationCode = solver.mTruck2LastDepot.get(truck)
				.getLocationCode();
		String moocLocationCode = solver.mMooc2LastDepot.get(mooc)
				.getLocationCode();

		String portImCode = ir.getPortCode();
		Port portIm = solver.mCode2Port.get(portImCode);
		String portImLocationCode = portIm.getLocationCode();

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		double distance = -1;
		int lastUsedIndex = -1;

		if (combo.routeElement == null) {
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
			arrivalTime = startTime
					+ solver.getTravelTime(truckLocationCode, moocLocationCode);
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);

			lastElement = e1;
		} else {
			TruckItinerary I = solver.getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setPort(portIm);
		e2.setImportRequest(ir);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getDepotMooc()
						.getLocationCode(), e2.getPort().getLocationCode());

		distance = combo.extraDistance
				+ solver.getDistance(combo.lastLocationCode,
						e2.getLocationCode());

		if(ir.getLateDateTimePickupAtPort() != null){
			if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort()))
				return null;
		}
		
		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(ir.getEarlyDateTimePickupAtPort()));
		duration = ir.getLoadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);

		Port port = solver.getPortFromCode(er.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(e2.getLocationCode(), departureTime, ir, er, port.getLocationCode());
		
		if(sol == null) return null;
		int ni = ir.getDeliveryWarehouses().length;
		int ne = er.getPickupWarehouses().length;

		RouteElement[] e = new RouteElement[2*(ni+ne)];
		int idx = -1;
		RouteElement le = e2;
		for(int i = 0; i < ni; i++){
			DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[sol.seq[i]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + i + "] = " + sol.seq[i] + ", dwi = " + dwi.getWareHouseCode());
			
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(solver.getWarehouseFromCode(dwi.getWareHouseCode()));
			arrivalTime = departureTime
					+ solver.getTravelTime(le,e[idx]);

			if (arrivalTime > DateTimeUtils.dateTime2Int(
					dwi.getLateDateTimeUnloadAtWarehouse()))
				return null;

			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(dwi.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;// ir.getUnloadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
			
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(le.getWarehouse());
			arrivalTime = departureTime + dwi.getUnloadDuration();//ir.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
		}
		
		for(int i = 0; i < ne; i++){
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[sol.seq[i + ni]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + (i+ni) + "] = " + sol.seq[i+ni] + ", pwi = " + pwi.getWareHouseCode());
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(solver.getWarehouseFromCode(pwi.getWareHouseCode()));
			e[idx].setExportRequest(er);
			arrivalTime = departureTime
					+ solver.getTravelTime(le,e[idx]);

			if (arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(pwi.getEarlyDateTimeLoadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
			
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(le.getWarehouse());
			arrivalTime = departureTime + pwi.getLoadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
		}
		
		//System.out.println(name() + "::createSwapImportExport, L.sz = " + L.size());
		//for(int i = 0; i < L.size(); i++){
		//	System.out.println(name() + "::createSwapImportExport, L[" + i + "] = " + L.get(i).getLocationCode());
		//}
		
		


		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(le);
		e7.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e7.setPort(solver.mCode2Port.get(er.getPortCode()));
		arrivalTime = departureTime
				+ solver.getTravelTime(le,e7);
		if (arrivalTime > DateTimeUtils.dateTime2Int(er
				.getLateDateTimeUnloadAtPort()))
			return null;
		serviceTime = arrivalTime;
		duration = er.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e7, arrivalTime);
		solver.mPoint2DepartureTime.put(e7, departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		RouteElement e8 = new RouteElement();
		L.add(e8);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(ir, e7, mooc);
		e8.deriveFrom(e7);
		e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e8.setDepotMooc(depotMooc);
		arrivalTime = departureTime
				+ solver.getTravelTime(e7.getPort().getLocationCode(), e8
						.getDepotMooc().getLocationCode());
		serviceTime = arrivalTime;
		duration = e8.getDepotMooc().getDeliveryMoocDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e8, arrivalTime);
		solver.mPoint2DepartureTime.put(e8, departureTime);
		//solver.mMooc2LastDepot.put(mooc, e8.getDepotMooc());
		//solver.mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, depotMooc);
		tri.setLastTimeMooc(mooc, departureTime);
		
		RouteElement e9 = new RouteElement();
		L.add(e9);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(ir, e8, truck);
		e9.deriveFrom(e8);
		e9.setAction(ActionEnum.REST_AT_DEPOT);
		e9.setDepotTruck(depotTruck);
		arrivalTime = departureTime
				+ solver.getTravelTime(e8.getDepotMooc().getLocationCode(), e9
						.getDepotTruck().getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e9, arrivalTime);
		solver.mPoint2DepartureTime.put(e9, departureTime);
		//solver.mTruck2LastDepot.put(truck, e9.getDepotTruck());
		//solver.mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, depotTruck);
		tri.setLastTimeTruck(truck, departureTime);
		
		//System.out.println(name() + "::createSwapImportExport, L.sz = " + L.size());
		//for(int i = 0; i < L.size(); i++){
		//	System.out.println(name() + "::createSwapImportExport, L[" + i + "] = " + L.get(i).getLocationCode());
		//}
		
		TruckRoute r = new TruckRoute();
		RouteElement[] lst_e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			lst_e[i] = L.get(i);
		r.setNodes(lst_e);
		r.setTruck(truck);
		r.setType(TruckRoute.SWAP);
		solver.propagate(r);

		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;
		return tri;

	}

}
