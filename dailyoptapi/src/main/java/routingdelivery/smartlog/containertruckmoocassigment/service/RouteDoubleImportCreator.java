package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import utils.DateTimeUtils;

public class RouteDoubleImportCreator {

	public ContainerTruckMoocSolver solver;
	public ImportContainerRequest sel_imReq_a;
	public ImportContainerRequest sel_imReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;

	public RouteDoubleImportCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportCreator";
	}

	public boolean checkCapacityForDoubleImport(Truck truck, Mooc mooc,
			ImportContainerRequest ir1, ImportContainerRequest ir2) {
		if (!ir1.getContainerCategory()
				.equals(ContainerCategoryEnum.CATEGORY20)
				|| !ir2.getContainerCategory().equals(
						ContainerCategoryEnum.CATEGORY20))
			return false;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20))
			return false;

		return true;
	}
	
	public double evaluateImportImportRequest(
			ImportContainerRequest req_a, ImportContainerRequest req_b,
			Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		Port port_a = solver.mCode2Port.get(req_a.getPortCode());
		Port port_b = solver.mCode2Port.get(req_b.getPortCode());

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		double distance = -1;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		distance = combo.extraDistance;

		//from mooc depot to port
		distance += solver.getDistance(lastLocationCode, port_a.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, port_a.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req_a.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_a
					.getLateDateTimePickupAtPort()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		duration = req_a.getLoadDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = port_a.getLocationCode();
		
		//from port_a to port_b
		distance += solver.getDistance(lastLocationCode, port_b.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, port_b.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req_b.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_b
					.getLateDateTimePickupAtPort()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		duration = req_b.getLoadDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = port_b.getLocationCode();


		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, req_a, null);
		int[] seq = ss.seq;

		Warehouse wh = null;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = req_a.getDeliveryWarehouses()[seq[i]];
			wh = solver.mCode2Warehouse.get(dwi.getWareHouseCode());
			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			
			arrivalTime = departureTime + travelTime;
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			lastLocationCode= wh.getLocationCode();

			arrivalTime = departureTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return Integer.MAX_VALUE;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
		}
		
		SS = new SequenceSolver(solver);
		ss = SS.solve(lastLocationCode,
				departureTime, req_b, null);
		seq = ss.seq;

		wh = null;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = req_b.getDeliveryWarehouses()[seq[i]];
			wh = solver.mCode2Warehouse.get(dwi.getWareHouseCode());
			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			
			arrivalTime = departureTime + travelTime;
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			lastLocationCode= wh.getLocationCode();

			arrivalTime = departureTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return Integer.MAX_VALUE;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
		}
		
		Container container_a = solver.mCode2Container.get(req_a.getContainerCode());
		DepotContainer depotContainer_a = solver.findDepotContainer4Deposit(req_a,
				lastLocationCode, container_a);
		distance += solver.getDistance(lastLocationCode, depotContainer_a.getLocationCode());
		lastLocationCode = depotContainer_a.getLocationCode();
		
		Container container_b = solver.mCode2Container.get(req_a.getContainerCode());
		DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(req_a,
				lastLocationCode, container_b);
		distance += solver.getDistance(lastLocationCode, depotContainer_b.getLocationCode());
		lastLocationCode = depotContainer_b.getLocationCode();
		
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += solver.getDistance(lastLocationCode, depotMooc.getLocationCode());
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, depotTruck.getLocationCode());		
		
		return distance;
	}
	
	public TruckRouteInfo4Request createRouteForImportImportRequest() {
		// truck and mooc are possibly not REST at their depots

		TruckRoute r = new TruckRoute();
		Port port_a = solver.mCode2Port.get(sel_imReq_a.getPortCode());
		Port port_b = solver.mCode2Port.get(sel_imReq_b.getPortCode());

		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		if (combo.routeElement == null) {

			// depart from the depot of the truck
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setDepotTruck(solver.mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(sel_truck);
			solver.mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setDepotMooc(solver.mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setMooc(sel_mooc);
			travelTime = solver.getTravelTime(e0, e1);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);
			lastElement = e1;
		} else {
			TruckItinerary I = solver.getItinerary(sel_truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
			if(combo.mooc == null){
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(solver.mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(sel_mooc);
				travelTime = solver.getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = solver.mMooc2LastTime.get(sel_mooc);
				startServiceTime = solver.MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e1, arrivalTime);
				solver.mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}
		
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setPort(port_a);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setImportRequest(sel_imReq_a);
		Container container_a = solver.mCode2Container.get(sel_imReq_a.getContainerCode());
		e2.setContainer(container_a);
		distance = combo.extraDistance + solver.getTravelTime(lastElement, e2);

		travelTime = solver.getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		if (sel_imReq_a.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imReq_a
					.getLateDateTimePickupAtPort()))
				return null;
		startServiceTime = arrivalTime;
		duration = sel_imReq_a.getLoadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;
		
		RouteElement e22 = new RouteElement();
		L.add(e22);
		e22.deriveFrom(lastElement);
		e22.setPort(port_b);
		e22.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e22.setImportRequest(sel_imReq_b);
		Container container_b = solver.mCode2Container.get(sel_imReq_b.getContainerCode());
		e22.setContainer(container_b);
		distance = combo.extraDistance + solver.getTravelTime(lastElement, e22);

		travelTime = solver.getTravelTime(lastElement, e22);
		arrivalTime = departureTime + travelTime;
		if (sel_imReq_b.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imReq_b
					.getLateDateTimePickupAtPort()))
				return null;
		startServiceTime = arrivalTime;
		duration = sel_imReq_b.getLoadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e22, arrivalTime);
		solver.mPoint2DepartureTime.put(e22, departureTime);
		lastElement = e22;

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastElement.getLocationCode(),
				departureTime, sel_imReq_a, null);
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[seq.length * 2];

		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			DeliveryWarehouseInfo dwi = sel_imReq_a.getDeliveryWarehouses()[seq[i]];

			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.getWarehouseFromCode(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = solver.getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			re[idx].setImportRequest(null);
			travelTime = solver.getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return null;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
		}
		
		SS = new SequenceSolver(solver);
		ss = SS.solve(lastElement.getLocationCode(),
				departureTime, sel_imReq_b, null);
		seq = ss.seq;
		re = new RouteElement[seq.length * 2];

		idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			DeliveryWarehouseInfo dwi = sel_imReq_b.getDeliveryWarehouses()[seq[i]];

			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.getWarehouseFromCode(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = solver.getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			re[idx].setImportRequest(null);
			travelTime = solver.getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return null;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
		}

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		DepotContainer depotContainer_a = solver.findDepotContainer4Deposit(sel_imReq_a,
				lastElement.getLocationCode(), container_a);
		// e5.setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e5.setDepotContainer(depotContainer_a);

		e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e5.setContainer(null);
		travelTime = solver.getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e5.getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);
		// mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
		// mContainer2LastTime.put(e5.getContainer(), departureTime);
		tri.setLastDepotContainer(container_a, e5.getDepotContainer());
		tri.setLastTimeContainer(container_a, departureTime);

		RouteElement e55 = new RouteElement();
		L.add(e55);
		e55.deriveFrom(lastElement);
		DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(sel_imReq_b,
				lastElement.getLocationCode(), container_b);
		// e5.setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e55.setDepotContainer(depotContainer_b);

		e55.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e55.setContainer(null);
		travelTime = solver.getTravelTime(lastElement, e55);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e55.getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e55, arrivalTime);
		solver.mPoint2DepartureTime.put(e55, departureTime);
		// mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
		// mContainer2LastTime.put(e5.getContainer(), departureTime);
		tri.setLastDepotContainer(container_b, e55.getDepotContainer());
		tri.setLastTimeContainer(container_b, departureTime);
		
		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e55);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_imReq_b, e55, sel_mooc);
		e6.setDepotMooc(depotMooc);
		// e6.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));

		e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e6.setMooc(sel_mooc);
		travelTime = solver.getTravelTime(e55, e6);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e6.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e6, arrivalTime);
		solver.mPoint2DepartureTime.put(e6, departureTime);
		// mMooc2LastDepot.put(mooc, e6.getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(sel_mooc, e6.getDepotMooc());
		tri.setLastTimeMooc(sel_mooc, departureTime);

		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(e6);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_imReq_b, e6, sel_truck);
		// e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e7.setDepotTruck(depotTruck);

		e7.setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = solver.getTravelTime(e6, e7);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e7, arrivalTime);
		solver.mPoint2DepartureTime.put(e7, departureTime);
		// mTruck2LastDepot.put(truck, e7.getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(sel_truck, e7.getDepotTruck());
		tri.setLastTimeTruck(sel_truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.DOUBLE_IMPORT);
		solver.propagate(r);
		solver.updateTruckAtDepot(sel_truck);
		solver.updateMoocAtDepot(sel_mooc);
		solver.updateContainerAtDepot(container_a);
		solver.updateContainerAtDepot(container_b);
		
		tri.route = r;
		tri.additionalDistance = distance;
		tri.lastUsedIndex = lastUsedIndex;

		return tri;
	}
	/*
	public TruckRouteInfo4Request createDoubleImportRequest(Truck truck,
			Mooc mooc, ImportContainerRequest ir1, ImportContainerRequest ir2) {
		// truck-mooc -> port(ir1) -> port(ir2) -> warehouse(ir1/ir2) ->
		// warehouse(ir2/ir1)
		// System.out.println(name() +
		// "::createDoubleImportRequest, ir1.portCode = " + ir1.getPortCode());

		if (!checkCapacityForDoubleImport(truck, mooc, ir1, ir2))
			return null;

		Port port1 = solver.mCode2Port.get(ir1.getPortCode());
		Port port2 = solver.mCode2Port.get(ir2.getPortCode());
		//Warehouse wh1 = solver.mCode2Warehouse.get(ir1.getWareHouseCode());
		//Warehouse wh2 = solver.mCode2Warehouse.get(ir2.getWareHouseCode());
		Container container1 = solver.mCode2Container.get(ir1
				.getContainerCode());
		Container container2 = solver.mCode2Container.get(ir2
				.getContainerCode());

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
		
		if (combo.routeElement == null) {

			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setDepotTruck(solver.mTruck2LastDepot.get(truck));
			departureTime = solver.mTruck2LastTime.get(truck);
			solver.mPoint2DepartureTime.put(e0, departureTime);

			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(mooc));
			arrivalTime = departureTime
					+ solver.getTravelTime(e0,e1);
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);
			
			lastElement = e1;
		}

		boolean ir1First = true;
		if (DateTimeUtils.dateTime2Int(ir1.getLateDateTimePickupAtPort()) > DateTimeUtils
				.dateTime2Int(ir2.getLateDateTimePickupAtPort())) {
			ir1First = false;
		}
		RouteElement e2 = new RouteElement();
		L.add(e2);
		RouteElement e3 = new RouteElement();
		L.add(e3);

		if (ir1First) {
			e2.deriveFrom(lastElement);
			e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e2.setPort(port1);
			e2.setImportRequest(ir1);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement,
							e2);
			
			distance = combo.extraDistance
					+ solver.getDistance(combo.lastLocationCode,
							e2.getLocationCode());
			
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimePickupAtPort()));
			duration = ir1.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			e3.deriveFrom(e2);
			e3.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e3.setPort(port2);
			e3.setImportRequest(ir2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e2, e3
							);
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimePickupAtPort()));
			duration = ir2.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e3, arrivalTime);
			solver.mPoint2DepartureTime.put(e3, departureTime);

		} else {
			e2.deriveFrom(lastElement);
			e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e2.setPort(port2);
			e2.setImportRequest(ir2);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement, e2);
					//+ solver.getTravelTime(lastElement.getDepotMooc().getLocationCode(),
					//		e2.getPort().getLocationCode());
			
			distance = combo.extraDistance
					+ solver.getDistance(combo.lastLocationCode,
							e2.getLocationCode());
			
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimePickupAtPort()));
			duration = ir2.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			e3.deriveFrom(e2);
			e3.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e3.setPort(port1);
			e3.setImportRequest(ir1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e2, e3
							);
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimePickupAtPort()));
			duration = ir1.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e3, arrivalTime);
			solver.mPoint2DepartureTime.put(e3, departureTime);

		}

		ir1First = true;
		if (solver.getDistance(e3.getPort().getLocationCode(),
				wh1.getLocationCode()) > solver.getDistance(e3.getPort()
				.getLocationCode(), wh2.getLocationCode())) {
			ir1First = false;
		}
		RouteElement e4 = new RouteElement();
		L.add(e4);
		RouteElement e5 = new RouteElement();
		L.add(e5);
		RouteElement e6 = new RouteElement();
		L.add(e6);
		RouteElement e7 = new RouteElement();
		L.add(e7);

		if (ir1First) {
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e4.setWarehouse(wh1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e3.getPort().getLocationCode(), e4
							.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e4, arrivalTime);
			solver.mPoint2DepartureTime.put(e4, departureTime);

			e5.deriveFrom(e4);
			e5.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e5.setWarehouse(wh1);
			arrivalTime = departureTime + ir1.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e5, arrivalTime);
			solver.mPoint2DepartureTime.put(e5, departureTime);

			// release ir2
			e6.deriveFrom(e5);
			e6.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouse(wh2);
			// if(e5.getWarehouse() == null){
			// System.out.println(name() +
			// "::createDoubleImportRequest, warehouse e5 = NULL");
			// }else{
			// System.out.println(name() +
			// "::createDoubleImportRequest, warehouse e5 = " +
			// e5.getWarehouse().getLocationCode());
			// }
			arrivalTime = departureTime
					+ solver.getTravelTime(e5,
							e6);
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e6, arrivalTime);
			solver.mPoint2DepartureTime.put(e6, departureTime);

			e7.deriveFrom(e6);
			e7.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e7.setWarehouse(wh2);
			arrivalTime = departureTime + ir2.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e7, arrivalTime);
			solver.mPoint2DepartureTime.put(e7, departureTime);

		} else {// ir2 first
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e4.setWarehouse(wh2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e3.getPort().getLocationCode(), e4
							.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e4, arrivalTime);
			solver.mPoint2DepartureTime.put(e4, departureTime);

			e5.deriveFrom(e4);
			e5.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e5.setWarehouse(wh2);
			arrivalTime = departureTime + ir2.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e5, arrivalTime);
			solver.mPoint2DepartureTime.put(e5, departureTime);

			// release ir1
			e6.deriveFrom(e5);
			e6.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouse(wh1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e5.getWarehouse().getLocationCode(),
							e6.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e6, arrivalTime);
			solver.mPoint2DepartureTime.put(e6, departureTime);

			e7.deriveFrom(e6);
			e7.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e7.setWarehouse(wh1);
			arrivalTime = departureTime + ir1.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e7, arrivalTime);
			solver.mPoint2DepartureTime.put(e7, departureTime);

		}
		DepotContainer depotContainer1 = solver.findDepotContainer4Deposit(ir1, e7, container1);
		DepotContainer depotContainer2 = solver.findDepotContainer4Deposit(ir2, e7, container2);
	
		// if(depotContainer1 == null){
		// System.out.println(name() +
		// "::createDoubleImportRequest depotContainer1 = NULL");
		// }else{
		// System.out.println(name() +
		// "::createDoubleImportRequest depotContainer1 = " +
		// depotContainer1.getLocationCode());
		// }
		ir1First = true;
		if (solver.getDistance(e7.getWarehouse().getLocationCode(),
				depotContainer1.getLocationCode()) > solver.getDistance(e7
				.getWarehouse().getLocationCode(), depotContainer2
				.getLocationCode())) {
			ir1First = false;
		}
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		RouteElement e8 = new RouteElement();
		L.add(e8);
		RouteElement e9 = new RouteElement();
		L.add(e9);

		if (ir1First) {
			e8.deriveFrom(e7);
			e8.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e8.setDepotContainer(depotContainer1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e7.getWarehouse().getLocationCode(),
							e8.getDepotContainer().getLocationCode());
			serviceTime = arrivalTime;
			duration = e8.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e8, arrivalTime);
			solver.mPoint2DepartureTime.put(e8, departureTime);
			tri.setLastDepotContainer(container1, depotContainer1);
			tri.setLastTimeContainer(container1, departureTime);
			
			// release container2
			e9.deriveFrom(e8);
			e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e9.setDepotContainer(depotContainer2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e8.getDepotContainer()
							.getLocationCode(), e9.getDepotContainer()
							.getLocationCode());
			serviceTime = arrivalTime;
			duration = e9.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e9, arrivalTime);
			solver.mPoint2DepartureTime.put(e9, departureTime);
			tri.setLastDepotContainer(container2, depotContainer2);
			tri.setLastTimeContainer(container2, departureTime);
		} else {
			e8.deriveFrom(e7);
			e8.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e8.setDepotContainer(depotContainer2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e7.getWarehouse().getLocationCode(),
							e8.getDepotContainer().getLocationCode());
			serviceTime = arrivalTime;
			duration = e8.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e8, arrivalTime);
			solver.mPoint2DepartureTime.put(e8, departureTime);
			tri.setLastDepotContainer(container2, depotContainer2);
			tri.setLastTimeContainer(container2, departureTime);
			
			// release container2
			e9.deriveFrom(e8);
			e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e9.setDepotContainer(depotContainer1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e8.getDepotContainer()
							.getLocationCode(), e9.getDepotContainer()
							.getLocationCode());
			serviceTime = arrivalTime;
			duration = e9.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e9, arrivalTime);
			solver.mPoint2DepartureTime.put(e9, departureTime);
			tri.setLastDepotContainer(container1, depotContainer1);
			tri.setLastTimeContainer(container1, departureTime);
		}
		RouteElement e10 = new RouteElement();
		L.add(e10);
		e10.deriveFrom(e9);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(ir1, e9, mooc);
		e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e10.setDepotMooc(depotMooc);
		arrivalTime = departureTime
				+ solver.getTravelTime(
						e9.getDepotContainer().getLocationCode(), e10
								.getDepotMooc().getLocationCode());
		serviceTime = arrivalTime;
		duration = e10.getDepotMooc().getDeliveryMoocDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e10, arrivalTime);
		solver.mPoint2DepartureTime.put(e10, departureTime);
		tri.setLastDepotMooc(mooc, depotMooc);
		tri.setLastTimeMooc(mooc, departureTime);
		
		RouteElement e11 = new RouteElement();
		L.add(e11);
		e11.deriveFrom(e10);
		e11.setAction(ActionEnum.REST_AT_DEPOT);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(ir1, e10, truck);
		e11.setDepotTruck(depotTruck);
		arrivalTime = departureTime
				+ solver.getTravelTime(e10.getDepotMooc().getLocationCode(),
						e11.getDepotTruck().getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e11, arrivalTime);
		solver.mPoint2DepartureTime.put(e11, departureTime);
		tri.setLastDepotTruck(truck, depotTruck);
		tri.setLastTimeTruck(truck, departureTime);
		
		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DOUBLE_IMPORT);
		solver.propagate(r);

		
		tri.route = r;
		tri.lastUsedIndex = -1;
		tri.additionalDistance = distance;
		return tri;

	}
	*/
}
