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

public class RouteDoubleExportCreator {
	
	public ContainerTruckMoocSolver solver;
	public ExportContainerRequest sel_exReq_a;
	public ExportContainerRequest sel_exReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;
	public Container sel_container_a;
	public Container sel_container_b;

	public RouteDoubleExportCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportCreator";
	}
	
	public double evaluateExportExportRequest(
			ExportContainerRequest req_a, ExportContainerRequest req_b,
			Truck truck, Mooc mooc,
			Container container_a, Container container_b){
		String header = name() + "::evaluateExportRoute";
		// truck, mooc, container are REST at their depots
		if (solver.mContainer2LastDepot.get(container_a) == null
				|| solver.mContainer2LastDepot.get(container_b) == null) {
			return Integer.MAX_VALUE;
		}

		//ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		ComboContainerMoocTruck combo = solver.findLastAvailable(
				truck, mooc, container_a);
		if (combo == null)
			return Integer.MAX_VALUE;

		double distance = -1;
		
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		DepotTruck depotTruck = solver.mTruck2LastDepot.get(truck);
		DepotMooc depotMooc = solver.mMooc2LastDepot.get(mooc);
		DepotContainer depotContainer_a = solver.mCode2DepotContainer.get(req_a.getDepotContainerCode());
		DepotContainer depotContainer_b = solver.mCode2DepotContainer.get(req_b.getDepotContainerCode());
		
		
		distance = combo.extraDistance;
		
		int arrivalTimeContainer = departureTime
				+ solver.getTravelTime(lastLocationCode,
						depotContainer_b.getLocationCode());
		int timeContainer = solver.mContainer2LastTime.get(container_b);
		departureTime = solver.MAX(arrivalTimeContainer, timeContainer)
				+ solver.mContainer2LastDepot.get(container_b)
						.getPickupContainerDuration();
		distance += solver.getDistance(lastLocationCode,
				depotContainer_b.getLocationCode());
		
		Port port_a = solver.getPortFromCode(req_a.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, req_a, port_a.getLocationCode());
		int[] seq = ss.seq;

		for (int i = 0; i < seq.length; i++) {
			PickupWarehouseInfo pwi = req_a.getPickupWarehouses()[seq[i]];
			Warehouse wh = solver.mCode2Warehouse.get(pwi.getWareHouseCode());

			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_a
					.getLateDateTimeLoadAtWarehouse()))
				return Integer.MAX_VALUE;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			lastLocationCode = wh.getLocationCode();

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
		
		Port port_b = solver.getPortFromCode(req_b.getPortCode());
		SS = new SequenceSolver(solver);
		ss = SS.solve(lastLocationCode,
				departureTime, req_b, port_b.getLocationCode());
		seq = ss.seq;

		for (int i = 0; i < seq.length; i++) {
			PickupWarehouseInfo pwi = req_b.getPickupWarehouses()[seq[i]];
			Warehouse wh = solver.mCode2Warehouse.get(pwi.getWareHouseCode());

			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_b
					.getLateDateTimeLoadAtWarehouse()))
				return Integer.MAX_VALUE;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			lastLocationCode = wh.getLocationCode();

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
		
		
		distance += solver.getDistance(lastLocationCode, port_a.getLocationCode());
		lastLocationCode = port_a.getLocationCode();
		
		distance += solver.getDistance(lastLocationCode, port_b.getLocationCode());
		lastLocationCode = port_b.getLocationCode();
		
		DepotMooc returnDepotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += solver.getDistance(lastLocationCode, returnDepotMooc.getLocationCode());
		lastLocationCode = returnDepotMooc.getLocationCode();
		
		//from mooc depot to truck depot
		DepotTruck returnDepotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, returnDepotTruck.getLocationCode());		

		return distance;
	}
	
	public TruckRouteInfo4Request createRouteForExportExportRequest() {
		// truck and mooc are possibly not REST at their depots
		String header = name() + "::createRouteForExportRequest";
		// truck, mooc, container are REST at their depots
		if (solver.mContainer2LastDepot.get(sel_container_a) == null) {
			return null;
		}
		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		//ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		ComboContainerMoocTruck combo = solver.findLastAvailable(
				sel_truck, sel_mooc, sel_container_a);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		String lastLocationCode = combo.lastLocationCode;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		if (combo.routeElement == null) {
			L.add(e[0]);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = solver.mTruck2LastDepot.get(sel_truck);
			e[0].setDepotTruck(depotTruck);
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(sel_truck);
			solver.mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			DepotMooc depotMooc = solver.mMooc2LastDepot.get(sel_mooc);
			e[1].setDepotMooc(depotMooc);
			e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e[1].setMooc(sel_mooc);

			travelTime = solver.getTravelTime(e[0], e[1]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[1].getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(e[1], arrivalTime);
			solver.mPoint2DepartureTime.put(e[1], departureTime);

			// arrive at the depot container
			L.add(e[2]);
			e[2].deriveFrom(e[1]);
			e[2].setDepotContainer(solver.mContainer2LastDepot.get(sel_container_a));
			e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e[2].setContainer(sel_container_a);
			if (solver.mContainer2LastDepot.get(sel_container_a) == null) {
				System.out.println(header + ", container = "
						+ sel_container_a.getCode() + " has no depot??????");
			}
			travelTime = solver.getTravelTime(e[1], e[2]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[2].getDepotContainer().getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(e[2], arrivalTime);
			solver.mPoint2DepartureTime.put(e[2], departureTime);

			lastElement = e[2];
			lastLocationCode = e[2].getLocationCode();
		} else {
			TruckItinerary I = solver.getItinerary(sel_truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
			if(combo.mooc == null){
				departureTime = solver.mPoint2DepartureTime.get(lastElement);
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				DepotMooc depotMooc = solver.mMooc2LastDepot.get(sel_mooc);
				e[0].setDepotMooc(depotMooc);
				e[0].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e[0].setMooc(sel_mooc);

				travelTime = solver.getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeMooc = solver.mMooc2LastTime.get(sel_mooc);
				startServiceTime = solver.MAX(arrivalTime, timeMooc);
				duration = e[0].getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e[0], arrivalTime);
				solver.mPoint2DepartureTime.put(e[0], departureTime);
				
				L.add(e[1]);
				e[1].deriveFrom(e[1]);
				e[1].setDepotContainer(solver.mContainer2LastDepot.get(sel_container_a));
				e[1].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[1].setContainer(sel_container_a);
				if (solver.mContainer2LastDepot.get(sel_container_a) == null) {
					System.out.println(header + ", container = "
							+ sel_container_a.getCode() + " has no depot??????");
				}
				travelTime = solver.getTravelTime(e[0], e[1]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = solver.mContainer2LastTime.get(sel_container_a);
				startServiceTime = solver.MAX(arrivalTime, timeContainer);
				duration = e[1].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e[1], arrivalTime);
				solver.mPoint2DepartureTime.put(e[1], departureTime);
				lastElement = e[1];
				lastLocationCode = e[1].getLocationCode();
			}
			else if(combo.mooc != null && combo.container == null){
				departureTime = solver.mPoint2DepartureTime.get(lastElement);				
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				e[0].setDepotContainer(solver.mContainer2LastDepot.get(sel_container_a));
				e[0].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[0].setContainer(sel_container_a);
				if (solver.mContainer2LastDepot.get(sel_container_a) == null) {
					System.out.println(header + ", container = "
							+ sel_container_a.getCode() + " has no depot??????");
				}
				travelTime = solver.getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = solver.mContainer2LastTime.get(sel_container_a);
				startServiceTime = solver.MAX(arrivalTime, timeContainer);
				duration = e[0].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e[0], arrivalTime);
				solver.mPoint2DepartureTime.put(e[0], departureTime);
				lastElement = e[0];
				lastLocationCode = e[0].getLocationCode();
			}
		}
		
		RouteElement ee = new RouteElement();
		L.add(ee);
		ee.deriveFrom(lastElement);
		ee.setDepotContainer(solver.mContainer2LastDepot.get(sel_container_b));
		ee.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		ee.setContainer(sel_container_b);
		if (solver.mContainer2LastDepot.get(sel_container_a) == null) {
			System.out.println(header + ", container = "
					+ sel_container_b.getCode() + " has no depot??????");
		}
		travelTime = solver.getTravelTime(lastElement, ee);
		arrivalTime = departureTime + travelTime;
		int timeContainer = solver.mContainer2LastTime.get(sel_container_b);
		startServiceTime = solver.MAX(arrivalTime, timeContainer);
		duration = ee.getDepotContainer().getPickupContainerDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(ee, arrivalTime);
		solver.mPoint2DepartureTime.put(ee, departureTime);
		lastElement = ee;
		lastLocationCode = ee.getLocationCode();
		
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		Port port_a = solver.getPortFromCode(sel_exReq_a.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, sel_exReq_a, port_a.getLocationCode());
		int[] seq = ss.seq;

		RouteElement[] re = new RouteElement[ss.seq.length * 2];
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = sel_exReq_a.getPickupWarehouses()[seq[i]];
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setExportRequest(sel_exReq_a);

			travelTime = solver.getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq_a
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance = combo.extraDistance + solver.getDistance(lastLocationCode, re[idx].getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = solver.getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			if (pwi.getLateDateTimePickupLoadedContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(pwi
						.getLateDateTimePickupLoadedContainerAtWarehouse()))
					return null;

			if (pwi.getEarlyDateTimePickupLoadedContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
			else
				startServiceTime = arrivalTime;

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();

			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();
		}
		
		Port port_b = solver.getPortFromCode(sel_exReq_b.getPortCode());
		SS = new SequenceSolver(solver);
		ss = SS.solve(lastLocationCode,
				departureTime, sel_exReq_b, port_b.getLocationCode());
		seq = ss.seq;

		re = new RouteElement[ss.seq.length * 2];
		idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = sel_exReq_b.getPickupWarehouses()[seq[i]];
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setExportRequest(sel_exReq_b);

			travelTime = solver.getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq_b
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance = combo.extraDistance + solver.getDistance(lastLocationCode, re[idx].getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = solver.getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			if (pwi.getLateDateTimePickupLoadedContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(pwi
						.getLateDateTimePickupLoadedContainerAtWarehouse()))
					return null;

			if (pwi.getEarlyDateTimePickupLoadedContainerAtWarehouse() != null)
				startServiceTime = solver.MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
			else
				startServiceTime = arrivalTime;

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();

			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();
		}

		L.add(e[5]);
		e[5].deriveFrom(lastElement);
		e[5].setPort(solver.mCode2Port.get(sel_exReq_a.getPortCode()));
		e[5].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e[5].setContainer(null);
		e[5].setExportRequest(null);
		travelTime = solver.getTravelTime(lastElement, e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = sel_exReq_a.getUnloadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e[5], arrivalTime);
		solver.mPoint2DepartureTime.put(e[5], departureTime);
		// update last depot container, set to not-available
		//mContainer2LastDepot.put(container, null);
		//mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(sel_container_a, null);
		tri.setLastTimeContainer(sel_container_a, Integer.MAX_VALUE);
		lastElement = e[5];

		RouteElement ed = new RouteElement();
		L.add(ed);
		ed.deriveFrom(lastElement);
		ed.setPort(solver.mCode2Port.get(sel_exReq_b.getPortCode()));
		ed.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		ed.setContainer(null);
		ed.setExportRequest(null);
		travelTime = solver.getTravelTime(lastElement, ed);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = sel_exReq_b.getUnloadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(ed, arrivalTime);
		solver.mPoint2DepartureTime.put(ed, departureTime);
		// update last depot container, set to not-available
		//mContainer2LastDepot.put(container, null);
		//mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(sel_container_b, null);
		tri.setLastTimeContainer(sel_container_b, Integer.MAX_VALUE);
		
		L.add(e[6]);
		e[6].deriveFrom(ed);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_exReq_b, ed, sel_mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[6].setDepotMooc(depotMooc);
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(null);
		travelTime = solver.getTravelTime(ed, e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e[6], arrivalTime);
		solver.mPoint2DepartureTime.put(e[6], departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(sel_mooc, e[6].getDepotMooc());
		tri.setLastTimeMooc(sel_mooc, departureTime);

		L.add(e[7]);
		e[7].deriveFrom(e[6]);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_exReq_b, e[6], sel_truck);
		e[7].setDepotTruck(depotTruck);
		e[7].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = solver.getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e[7], arrivalTime);
		solver.mPoint2DepartureTime.put(e[7], departureTime);
		// update last depot and last time of truck
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(sel_truck, e[7].getDepotTruck());
		tri.setLastTimeTruck(sel_truck, departureTime);

		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.DOUBLE_EXPORT);
		solver.propagate(r);
		solver.updateTruckAtDepot(sel_truck);
		solver.updateMoocAtDepot(sel_mooc);
		solver.updateContainerAtDepot(sel_container_a);
		solver.updateContainerAtDepot(sel_container_b);
		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}
}
