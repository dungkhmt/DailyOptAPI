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
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import utils.DateTimeUtils;

public class RouteKeplechCreator {
	public ContainerTruckMoocSolver solver;
	public ExportContainerRequest sel_exReq_a;
	public ImportContainerRequest sel_imReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;
	public Container sel_container;

	public RouteKeplechCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public boolean checkKeplech(Truck truck, Mooc mooc, Container container,
			ExportContainerRequest er, ImportContainerRequest ir) {
		boolean ok = true;
		if (container.getCategoryCode()
				.equals(ContainerCategoryEnum.CATEGORY20)) {
			if (!er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (container.getCategoryCode().equals(
				ContainerCategoryEnum.CATEGORY40)) {
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;

		}
		if (!ok)
			return ok;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20)) {
			if (!ir.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY40)) {
			if (ir.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
		}

		return ok;
	}

	public double evaluateKeplechRoute(Truck truck, Mooc mooc,
			Container container, ExportContainerRequest er,
			ImportContainerRequest ir) {
		// truck-mooc-container -> warehouse(er) -> port(ir) -> port(er) ->
		// warehouse(ir)

		if (solver.mContainer2LastDepot.get(container) == null) {
			// this is imported container, thus does not has depot
			return Integer.MAX_VALUE;
		}

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc,
				container);
		if (combo == null)
			return Integer.MAX_VALUE;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int travelTime = -1;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		String lastLocationCode = combo.lastLocationCode;
		double distance = combo.extraDistance;
		
		Port portEr = solver.getPortFromCode(er.getPortCode());

		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(lastLocationCode,
				departureTime, er, portEr.getLocationCode());

		if (sol == null)
			return Integer.MAX_VALUE;
		int[] seq = sol.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		for (int i = 0; i < re.length; i++)
			re[i] = new RouteElement();
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[seq[i]];
			Warehouse wh = solver.mCode2Warehouse.get(pwi.getWareHouseCode());
			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (er.getLateDateTimeLoadAtWarehouse() != null &&
					arrivalTime > DateTimeUtils.dateTime2Int(er.getLateDateTimeLoadAtWarehouse()))
				return Integer.MAX_VALUE;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			arrivalTime = departureTime;
			if (pwi.getLateDateTimePickupLoadedContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(pwi
						.getLateDateTimePickupLoadedContainerAtWarehouse()))
					return Integer.MAX_VALUE;

			if (pwi.getEarlyDateTimePickupLoadedContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
			else
				startServiceTime = arrivalTime;

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();
			lastLocationCode = wh.getLocationCode();
		}

		Port portIr = solver.getPortFromCode(ir.getPortCode());

		arrivalTime = departureTime + solver.getTravelTime(lastLocationCode, portIr.getLocationCode());

		if (ir.getLateDateTimePickupAtPort() != null && arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort()))
			return Integer.MAX_VALUE;

		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(ir.getEarlyDateTimePickupAtPort()));
		duration = ir.getLoadDuration();
		departureTime = startServiceTime + duration;
		distance += solver.getDistance(lastLocationCode, portIr.getLocationCode());
		lastLocationCode = portIr.getLocationCode();

		arrivalTime = departureTime
				+ solver.getTravelTime(lastLocationCode, portEr.getLocationCode());
		if (er.getLateDateTimeUnloadAtPort() != null && arrivalTime > DateTimeUtils.dateTime2Int(er
				.getLateDateTimeUnloadAtPort()))
			return Integer.MAX_VALUE;

		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(er.getEarlyDateTimeUnloadAtPort()));
		duration = er.getUnloadDuration();
		departureTime = startServiceTime + duration;
		distance += solver.getDistance(lastLocationCode, portEr.getLocationCode());
		lastLocationCode = portEr.getLocationCode();

		sol = SS.solve(lastLocationCode, departureTime, ir, null);
		seq = sol.seq;

		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[seq[i]];
			Warehouse wh = solver.mCode2Warehouse.get(dwi.getWareHouseCode());
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

		/*
		 * DepotContainer depotContainer = solver
		 * .findDepotForReleaseContainer(import_container); if (depotContainer
		 * == null) { depotContainer = solver.mCode2DepotContainer.get(ir
		 * .getDepotContainerCode()); }
		 */
		
		Container import_container = solver.mCode2Container.get(ir
				.getContainerCode());
		DepotContainer depotContainer = solver.findDepotContainer4Deposit(ir,
				lastLocationCode, import_container);
		distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(
				depotContainer.getLocationCode(), mooc);
		distance += solver.getDistance(depotContainer.getLocationCode(),
				depotMooc.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(
				depotMooc.getLocationCode(), truck);
		distance += solver.getDistance(depotMooc.getLocationCode(),
				depotTruck.getLocationCode());
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
	
	public TruckRouteInfo4Request createKeplech() {
		// truck-mooc-container -> warehouse(er) -> port(ir) -> port(er) ->
		// warehouse(ir)
		if (!checkKeplech(sel_truck, sel_mooc, sel_container, sel_exReq_a, sel_imReq_b))
			return null;

		if (solver.mContainer2LastDepot.get(sel_container) == null) {
			// this is imported container, thus does not has depot
			return null;
		}

		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc,
				sel_container);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		String lastLocationCode = combo.lastLocationCode;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;

		if (combo.routeElement == null) {
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setDepotTruck(solver.mTruck2LastDepot.get(sel_truck));
			departureTime = solver.mTruck2LastTime.get(sel_truck);
			solver.mPoint2DepartureTime.put(e0, departureTime);

			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
			arrivalTime = departureTime
					+ solver.getTravelTime(
							e0.getDepotTruck().getLocationCode(), e1
									.getDepotMooc().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(sel_mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e1);
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setDepotContainer(solver.mContainer2LastDepot.get(sel_container));
			arrivalTime = departureTime
					+ solver.getTravelTime(e1.getDepotMooc().getLocationCode(),
							e2.getDepotContainer().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mContainer2LastTime.get(sel_container));
			duration = e2.getDepotContainer().getPickupContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			lastElement = e2;
			lastLocationCode = e2.getDepotContainer().getLocationCode();
		} else {
			TruckItinerary I = solver.getItinerary(sel_truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		Port port_a = solver.getPortFromCode(sel_exReq_a.getPortCode());

		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(lastElement.getLocationCode(),
				departureTime, sel_exReq_a, port_a.getLocationCode());

		if (sol == null)
			return null;
		int[] seq = sol.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		for (int i = 0; i < re.length; i++)
			re[i] = new RouteElement();
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = sel_exReq_a.getPickupWarehouses()[seq[idx]];
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi
					.getWareHouseCode()));
			re[idx].setExportRequest(sel_exReq_a);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastLocationCode, re[idx].getWarehouse()
							.getLocationCode());

			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);

			idx++;
			L.add(re[idx]);
			re[idx].deriveFrom(re[idx - 1]);
			re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(re[idx - 1].getWarehouse());
			arrivalTime = departureTime + pwi.getLoadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);

			lastElement = re[idx];
			lastLocationCode = re[idx].getWarehouse().getLocationCode();
		}

		Port port_b = solver.getPortFromCode(sel_imReq_b.getPortCode());
		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		e5.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e5.setPort(port_b);
		Container import_container = solver.mCode2Container.get(sel_imReq_b
				.getContainerCode());
		e5.setContainer(import_container);
		e5.setImportRequest(sel_imReq_b);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastLocationCode, port_b.getLocationCode());
		if (sel_imReq_b.getLateDateTimePickupAtPort() != null 
				&& arrivalTime > DateTimeUtils.dateTime2Int(sel_imReq_b.getLateDateTimePickupAtPort()))
			return null;

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(sel_imReq_b.getEarlyDateTimePickupAtPort()));
		duration = sel_imReq_b.getLoadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);
		
		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		e6.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e6.setPort(solver.mCode2Port.get(sel_exReq_a.getPortCode()));
		arrivalTime = departureTime + solver.getTravelTime(e5, e6);
		if (sel_exReq_a.getLateDateTimeUnloadAtPort() != null && arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq_a
				.getLateDateTimeUnloadAtPort()))
			return null;

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(sel_exReq_a.getEarlyDateTimeUnloadAtPort()));
		duration = sel_exReq_a.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e6, arrivalTime);
		solver.mPoint2DepartureTime.put(e6, departureTime);
		// solver.mContainer2LastDepot.put(container, null);
		// solver.mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(sel_container, null);
		tri.setLastTimeContainer(sel_container, Integer.MAX_VALUE);

		

		sol = SS.solve(e6.getLocationCode(), departureTime, sel_imReq_b, null);
		seq = sol.seq;

		re = new RouteElement[2 * seq.length];
		idx = -1;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = sel_imReq_b.getDeliveryWarehouses()[seq[i]];
			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(e6);
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(dwi.getWareHouseCode()));
			arrivalTime = departureTime
					+ solver.getTravelTime(e6, re[idx]);

			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(dwi.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;// ir.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(re[idx-1]);
			re[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(re[idx-1].getWarehouse());
			arrivalTime = departureTime + dwi.getUnloadDuration();

			serviceTime = arrivalTime;
			duration = 0;// ir.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);

			lastElement = re[idx];
		}

		/*
		 * DepotContainer depotContainer = solver
		 * .findDepotForReleaseContainer(import_container); if (depotContainer
		 * == null) { depotContainer = solver.mCode2DepotContainer.get(ir
		 * .getDepotContainerCode()); }
		 */
		DepotContainer depotContainer = solver.findDepotContainer4Deposit(sel_imReq_b,
				lastElement.getLocationCode(), import_container);

		RouteElement e9 = new RouteElement();
		L.add(e9);
		e9.deriveFrom(lastElement);
		e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e9.setDepotContainer(depotContainer);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getWarehouse().getLocationCode(), e9
						.getDepotContainer().getLocationCode());
		serviceTime = arrivalTime;
		duration = e9.getDepotContainer().getDeliveryContainerDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e9, arrivalTime);
		solver.mPoint2DepartureTime.put(e9, departureTime);
		tri.setLastDepotContainer(import_container, depotContainer);
		tri.setLastTimeContainer(import_container, departureTime);

		RouteElement e10 = new RouteElement();
		L.add(e10);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_imReq_b, e9, sel_mooc);
		e10.deriveFrom(e9);
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
		tri.setLastDepotMooc(sel_mooc, depotMooc);
		tri.setLastTimeMooc(sel_mooc, departureTime);

		RouteElement e11 = new RouteElement();
		L.add(e11);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_imReq_b, e10, sel_truck);
		e11.deriveFrom(e10);
		e11.setAction(ActionEnum.REST_AT_DEPOT);
		e11.setDepotTruck(depotTruck);
		arrivalTime = departureTime
				+ solver.getTravelTime(e10.getDepotMooc().getLocationCode(),
						e11.getDepotTruck().getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e11, arrivalTime);
		solver.mPoint2DepartureTime.put(e11, departureTime);
		tri.setLastDepotTruck(sel_truck, depotTruck);
		tri.setLastTimeTruck(sel_truck, departureTime);

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.KEP_LECH);
		solver.propagate(r);
		solver.updateTruckAtDepot(sel_truck);
		solver.updateMoocAtDepot(sel_mooc);
		solver.updateContainerAtDepot(sel_container);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;
		return tri;

	}

}
