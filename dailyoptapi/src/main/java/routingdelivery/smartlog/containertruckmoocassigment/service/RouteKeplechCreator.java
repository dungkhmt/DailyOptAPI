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
import utils.DateTimeUtils;

public class RouteKeplechCreator {
	public ContainerTruckMoocSolver solver;

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

	public TruckRouteInfo4Request createKeplech(Truck truck, Mooc mooc,
			Container container, ExportContainerRequest er,
			ImportContainerRequest ir) {
		// truck-mooc-container -> warehouse(er) -> port(ir) -> port(er) ->
		// warehouse(ir)
		if (!checkKeplech(truck, mooc, container, er, ir))
			return null;

		if (solver.mContainer2LastDepot.get(container) == null) {
			// this is imported container, thus does not has depot
			return null;
		}

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc,
				container);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;

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
					+ solver.getTravelTime(
							e0.getDepotTruck().getLocationCode(), e1
									.getDepotMooc().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e1);
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setDepotContainer(solver.mContainer2LastDepot.get(container));
			arrivalTime = departureTime
					+ solver.getTravelTime(e1.getDepotMooc().getLocationCode(),
							e2.getDepotContainer().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mContainer2LastTime.get(container));
			duration = e2.getDepotContainer().getPickupContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			lastElement = e2;
		} else {
			TruckItinerary I = solver.getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		Port port = solver.getPortFromCode(er.getPortCode());

		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(lastElement.getLocationCode(),
				departureTime, er, port.getLocationCode());

		if (sol == null)
			return null;
		int[] seq = sol.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		for (int i = 0; i < re.length; i++)
			re[i] = new RouteElement();
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[seq[idx]];
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi
					.getWareHouseCode()));
			re[idx].setExportRequest(er);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement.getDepotContainer()
							.getLocationCode(), re[idx].getWarehouse()
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
		}

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e5.setPort(solver.mCode2Port.get(er.getPortCode()));
		arrivalTime = departureTime + solver.getTravelTime(lastElement, e5);
		if (arrivalTime > DateTimeUtils.dateTime2Int(er
				.getLateDateTimeUnloadAtPort()))
			return null;

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(er.getEarlyDateTimeUnloadAtPort()));
		duration = er.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);
		// solver.mContainer2LastDepot.put(container, null);
		// solver.mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(container, null);
		tri.setLastTimeContainer(container, Integer.MAX_VALUE);

		port = solver.getPortFromCode(ir.getPortCode());
		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		e6.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e6.setPort(port);
		Container import_container = solver.mCode2Container.get(ir
				.getContainerCode());
		e6.setContainer(import_container);
		e6.setImportRequest(ir);
		arrivalTime = departureTime
				+ solver.getTravelTime(e5.getPort().getLocationCode(), e6
						.getPort().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort()))
			return null;

		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(ir.getEarlyDateTimePickupAtPort()));
		duration = ir.getLoadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e6, arrivalTime);
		solver.mPoint2DepartureTime.put(e6, departureTime);

		sol = SS.solve(e6.getLocationCode(), departureTime, ir, null);
		seq = sol.seq;

		re = new RouteElement[2 * seq.length];
		idx = -1;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[seq[i]];
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
		DepotContainer depotContainer = solver.findDepotContainer4Deposit(ir,
				lastElement, import_container);

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
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(ir, e9, mooc);
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
		tri.setLastDepotMooc(mooc, depotMooc);
		tri.setLastTimeMooc(mooc, departureTime);

		RouteElement e11 = new RouteElement();
		L.add(e11);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(ir, e10, truck);
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
		tri.setLastDepotTruck(truck, depotTruck);
		tri.setLastTimeTruck(truck, departureTime);

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.KEP_LECH);
		solver.propagate(r);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;
		return tri;

	}

}
