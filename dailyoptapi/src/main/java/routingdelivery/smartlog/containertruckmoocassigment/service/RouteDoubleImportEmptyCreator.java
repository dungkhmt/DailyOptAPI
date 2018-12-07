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

public class RouteDoubleImportEmptyCreator {
	public ContainerTruckMoocSolver solver;
	public ImportContainerRequest sel_imReq_a;
	public ImportContainerRequest sel_imReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;

	public RouteDoubleImportEmptyCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportEmptyCreator";
	}
	
	public boolean checkCapacityForDoubleImportEmpty(Truck truck, Mooc mooc,
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
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer_a.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer_a.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer_a.getLocationCode();
		
		Container container_b = solver.mCode2Container.get(req_b.getContainerCode());
		DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(req_b,
				lastLocationCode, container_b);
		distance += solver.getDistance(lastLocationCode, depotContainer_b.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer_b.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer_b.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer_b.getLocationCode();
		
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += solver.getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = solver.getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsTruck(truck, 
				combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
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
			re[idx].setImportRequest(sel_imReq_a);
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
			re[idx].setImportRequest(sel_imReq_b);
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
		tri.setLastDepotContainer(container_a, depotContainer_a);
		tri.setLastTimeContainer(container_a, departureTime);
		lastElement = e5;
		
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
		tri.setLastDepotContainer(container_b, depotContainer_b);
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
}
