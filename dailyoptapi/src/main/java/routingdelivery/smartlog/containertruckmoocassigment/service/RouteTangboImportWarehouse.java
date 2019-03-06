package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;

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
import routingdelivery.smartlog.containertruckmoocassigment.model.Intervals;
import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;
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
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import utils.DateTimeUtils;

public class RouteTangboImportWarehouse {
	ContainerTruckMoocSolver solver;
	public WarehouseContainerTransportRequest sel_whReq;
	public ImportContainerRequest sel_imReq;
	public Truck sel_truck;
	public Mooc sel_mooc;
	public Container sel_container;

	public RouteTangboImportWarehouse(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteTangboImportWarehouse";
	}

	public TruckRouteInfo4Request createTangboImportWarehouse() {
		// try to create route with truck-mooc-container serving wr and then er
		// truck-mooc-container -> from_warehouse(wr) -> to_warehouse(wr) ->
		// warehouse(er) -> port(er)
		
		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc);
		if (combo == null)
			return null;
		// System.out.println(name() +
		// "::createTangboWarehouseExport, combo.locationCode= " +
		// combo.lastLocationCode);

		double distance = -1;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int travelTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		String lastLocationCode = combo.lastLocationCode;

		if (combo.routeElement == null) {
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setDepotTruck(solver.mTruck2LastDepot.get(sel_truck));
			e0.setTruck(sel_truck);
			departureTime = solver.mTruck2LastTime.get(sel_truck);
			solver.mPoint2DepartureTime.put(e0, departureTime);

			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
			e1.setMooc(sel_mooc);
			arrivalTime = departureTime
					+ solver.getTravelTime(
							e0.getDepotTruck().getLocationCode(), e1
									.getDepotMooc().getLocationCode());
			startServiceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(sel_mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);
			lastElement = e1;
			lastLocationCode = e1.getLocationCode();
		} else {
			TruckItinerary I = solver.getItinerary(sel_truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
			if(combo.mooc == null){
				departureTime = solver.mPoint2DepartureTime.get(lastElement);
				RouteElement e0 = new RouteElement();
				L.add(e0);
				e0.deriveFrom(lastElement);
				DepotMooc depotMooc = solver.mMooc2LastDepot.get(sel_mooc);
				e0.setDepotMooc(depotMooc);
				e0.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e0.setMooc(sel_mooc);
				
				arrivalTime = departureTime + solver.getTravelTime(lastElement, e0);
				int timeMooc = solver.mMooc2LastTime.get(sel_mooc);
				startServiceTime = solver.MAX(arrivalTime, timeMooc);
				duration = e0.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e0, arrivalTime);
				solver.mPoint2DepartureTime.put(e0, departureTime);
				lastElement = e0;
				lastLocationCode = e0.getLocationCode();
			}
		}
		departureTime = combo.startTime;
		
		TruckRoute r = new TruckRoute();
		Port port = solver.mCode2Port.get(sel_imReq.getPortCode());
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setPort(port);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setImportRequest(sel_imReq);
		Container container = solver.mCode2Container.get(sel_imReq.getContainerCode());
		e2.setContainer(container);
		distance = combo.extraDistance + solver.getTravelTime(lastElement, e2);

		travelTime = solver.getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		if (sel_imReq.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imReq
					.getLateDateTimePickupAtPort()))
				return null;
		startServiceTime = solver.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(sel_imReq.
				getEarlyDateTimePickupAtPort()));
		duration = sel_imReq.getLoadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastElement.getLocationCode(),
				departureTime, sel_imReq, null);
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[seq.length * 2];

		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			DeliveryWarehouseInfo dwi = sel_imReq.getDeliveryWarehouses()[seq[i]];

			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(solver.getWarehouseFromCode(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = solver.getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			if(dwi.getLateDateTimeUnloadAtWarehouse() != null){
				if(arrivalTime > DateTimeUtils.dateTime2Int(dwi.
						getLateDateTimeUnloadAtWarehouse()))
					return null;
			}
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ sel_imReq.getUnloadDuration();

			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
		}
		
		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		e3.setImportRequest(null);
		e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e3.setWarehouse(solver.mCode2Warehouse.get(sel_whReq.getFromWarehouseCode()));
		e3.setWarehouseRequest(sel_whReq);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getLocationCode(), e3.getWarehouse().getLocationCode());
		distance += solver.getDistance(lastElement, e3);

		if (sel_whReq.getLateDateTimeLoad() != null && arrivalTime > DateTimeUtils.dateTime2Int(sel_whReq.getLateDateTimeLoad())) {
			solver.logln(name()
					+ "::createTangboWarehouseExport, violation time arrival = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
					+ " > wr.getLateDateTimeLoad() = "
					+ sel_whReq.getLateDateTimeLoad());
			return null;
		}
		startServiceTime = Utils.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(sel_whReq.getEarlyDateTimeLoad()));
		int finishedServiceTime = startServiceTime
				+ sel_whReq.getLoadDuration();
		departureTime = finishedServiceTime;
		solver.mPoint2ArrivalTime.put(e3, arrivalTime);
		solver.mPoint2DepartureTime.put(e3, departureTime);


		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(e3);
		e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		e5.setWarehouse(solver.mCode2Warehouse.get(sel_whReq.getToWarehouseCode()));
		
		e5.setWarehouseRequest(sel_whReq);
		arrivalTime = departureTime
				+ solver.getTravelTime(e3.getWarehouse().getLocationCode(), e5
						.getWarehouse().getLocationCode());
		if (sel_whReq.getLateDateTimeUnload() != null && arrivalTime > DateTimeUtils
				.dateTime2Int(sel_whReq.getLateDateTimeUnload())) {
			solver.logln(name()
					+ "::createTangboWarehouseExport, violation time arrival = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
					+ " > wr.getLateDateTimeUnload() = "
					+ sel_whReq.getLateDateTimeUnload());
			return null;
		}
		startServiceTime = Utils.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(sel_whReq.getEarlyDateTimeUnload()));
		finishedServiceTime = startServiceTime
				+ sel_whReq.getUnloadDuration();

		departureTime = finishedServiceTime;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);
		lastElement = e5;

		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.setWarehouseRequest(null);
		e7.deriveFrom(lastElement);
		DepotContainer depotContainer = solver.findDepotContainer4Deposit(
				sel_imReq, lastLocationCode, container);
		// e5.setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e7.setDepotContainer(depotContainer);

		e7.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e7.setContainer(null);
		travelTime = solver.getTravelTime(lastElement, e7);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e7.getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e7, arrivalTime);
		solver.mPoint2DepartureTime.put(e7, departureTime);
		// mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
		// mContainer2LastTime.put(e5.getContainer(), departureTime);
		tri.setLastDepotContainer(container, e5.getDepotContainer());
		tri.setLastTimeContainer(container, departureTime);
		lastElement = e7;
		
		RouteElement e10 = new RouteElement();
		L.add(e10);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_whReq, lastElement, sel_mooc);
		e10.deriveFrom(lastElement);
		e10.setWarehouseRequest(null);
		e10.setContainer(null);
		e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e10.setDepotMooc(depotMooc);
		e10.setMooc(null);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getLocationCode(), e10
						.getDepotMooc().getLocationCode());
		startServiceTime = arrivalTime;
		duration = e10.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e10, arrivalTime);
		solver.mPoint2DepartureTime.put(e10, departureTime);
		tri.setLastDepotMooc(sel_mooc, depotMooc);
		tri.setLastTimeMooc(sel_mooc, departureTime);

		RouteElement e11 = new RouteElement();
		L.add(e11);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_whReq, e10, sel_truck);

		e11.deriveFrom(e10);
		e11.setAction(ActionEnum.REST_AT_DEPOT);
		e11.setDepotTruck(depotTruck);
		e11.setTruck(null);
		
		arrivalTime = departureTime
				+ solver.getTravelTime(e10.getDepotMooc().getLocationCode(),
						e11.getDepotTruck().getLocationCode());
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e11, arrivalTime);
		solver.mPoint2DepartureTime.put(e11, departureTime);
		tri.setLastDepotTruck(sel_truck, depotTruck);
		tri.setLastTimeTruck(sel_truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.TANG_BO);
		solver.propagate(r);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;
		return tri;

	}
	
	public Measure evaluateTangboImportWarehouse(Truck truck,
			Mooc mooc, WarehouseContainerTransportRequest wr, ImportContainerRequest ir) {
		// try to create route with truck-mooc-container serving wr and then er
		// truck-mooc-container -> from_warehouse(wr) -> to_warehouse(wr) ->
		// warehouse(er) -> port(er)

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;
		// System.out.println(name() +
		// "::createTangboWarehouseExport, combo.locationCode= " +
		// combo.lastLocationCode);

		double distance = -1;
		int extraTime = 0;
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int travelTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		String lastLocationCode = combo.lastLocationCode;

		//from moocdepot to port
		Port port = solver.mCode2Port.get(ir.getPortCode());
		distance += solver.getDistance(lastLocationCode, port.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, port.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (ir.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(ir
					.getLateDateTimePickupAtPort()))
				return null;
		startServiceTime = solver.MAX(arrivalTime,(int) DateTimeUtils.dateTime2Int(ir.
				getEarlyDateTimePickupAtPort()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(ir.
				getEarlyDateTimePickupAtPort()) - arrivalTime);
		if(combo.routeElement == null){
			int mooc2cont = combo.startTime - combo.startTimeOfMooc;
			int truck2moocdepot = combo.startTimeOfMooc - combo.startTimeOfTruck;
			combo.startTime = startServiceTime - travelTime;
			combo.startTimeOfMooc = combo.startTime - mooc2cont;
			combo.startTimeOfTruck = combo.startTimeOfMooc - truck2moocdepot;
			extraTime = 0;
		}
		duration = ir.getLoadDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = port.getLocationCode();

		//from port to wh
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, ir, null);
		int[] seq = ss.seq;

		int nbCheckinAtHardWarehouse = 0;
		boolean isBalance = false;
		HashMap<String, String> srcdest = new HashMap<String, String>();
		ArrayList<Warehouse> hardWh = new ArrayList<Warehouse>();
		
		Warehouse wh = null;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[seq[i]];
			wh = solver.mCode2Warehouse.get(dwi.getWareHouseCode());
			
			if(solver.input.getParams().getConstraintWarehouseHard()){
				if(wh.getHardConstraintType() == Utils.HARD_DELIVERY_WH
					|| wh.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
					nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(wh.getCheckin(),
							truck.getDriverID());
					hardWh.add(wh);
				}
			}
			if(solver.input.getParams().getConstraintDriverBalance()){
				if(solver.getIsDriverBalance(lastLocationCode, wh.getLocationCode())){
					ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
							wh.getLocationCode());
					for(int k = 0; k < drl.size(); k++)
						if(drl.get(k) == truck.getDriverID()){
							isBalance = true;
							srcdest.put(lastLocationCode, wh.getLocationCode());
						}
				}
			}
			
			
			
			travelTime = solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			
			arrivalTime = departureTime + travelTime;
			if(dwi.getLateDateTimeUnloadAtWarehouse() != null){
				if(arrivalTime > DateTimeUtils.dateTime2Int(dwi.
						getLateDateTimeUnloadAtWarehouse()))
					return null;
			}
			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(dwi
					.getEarlyDateTimeUnloadAtWarehouse()) - arrivalTime);
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(
							interval.getDateStart())
						&& startServiceTime < (int)DateTimeUtils.dateTime2Int(
							interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - startServiceTime;
						startServiceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}

			int finishedServiceTime = startServiceTime
					+ dwi.getUnloadDuration();
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(finishedServiceTime > (int)DateTimeUtils
						.dateTime2Int(interval.getDateStart())
						&& startServiceTime < (int)DateTimeUtils
						.dateTime2Int(interval.getDateStart())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd())
								- (int)DateTimeUtils
								.dateTime2Int(interval.getDateStart());
						finishedServiceTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd())
								- (int)DateTimeUtils
								.dateTime2Int(interval.getDateStart());
					}
				}
			}
			
			departureTime = finishedServiceTime;// startServiceTime + duration;
			
			lastLocationCode= wh.getLocationCode();
		}
		
		//from wh import req to wh1
		Warehouse whFromWh = solver.mCode2Warehouse.get(wr.getFromWarehouseCode());
		
		if(solver.input.getParams().getConstraintWarehouseHard()){
			if(whFromWh.getHardConstraintType() == Utils.HARD_PICKUP_WH
				|| whFromWh.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
				nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(whFromWh.getCheckin(),
						truck.getDriverID());
				hardWh.add(whFromWh);
			}
		}
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, whFromWh.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						whFromWh.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, whFromWh.getLocationCode());
					}
			}
		}
		
		
		
		distance += + solver.getDistance(lastLocationCode, whFromWh.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, whFromWh.getLocationCode());
		arrivalTime = departureTime
				+ travelTime;
		if (wr.getLateDateTimeLoad() != null && arrivalTime > DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad())) {
			solver.logln(name()
					+ "::createTangboWarehouseExport, violation time arrival = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
					+ " > wr.getLateDateTimeLoad() = "
					+ wr.getLateDateTimeLoad());
			return null;
		}
		
		startServiceTime = Utils.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeLoad()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(wr
				.getEarlyDateTimeLoad()) - arrivalTime);
		if(whFromWh.getBreaktimes() != null){
			for(int k = 0; k < whFromWh.getBreaktimes().length; k++){
				Intervals interval = whFromWh.getBreaktimes()[k];
				if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
					&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
					extraTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd()) - startServiceTime;
					startServiceTime = (int)DateTimeUtils.
							dateTime2Int(interval.getDateEnd());
				}
			}
		}
		
		int finishedServiceTime = startServiceTime
				+ wr.getLoadDuration();
		if(whFromWh.getBreaktimes() != null){
			for(int k = 0; k < whFromWh.getBreaktimes().length; k++){
				Intervals interval = whFromWh.getBreaktimes()[k];
				if(finishedServiceTime > (int)DateTimeUtils
					.dateTime2Int(interval.getDateStart())
					&& startServiceTime < (int)DateTimeUtils
					.dateTime2Int(interval.getDateStart())){
					extraTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd())
							- (int)DateTimeUtils
							.dateTime2Int(interval.getDateStart());
					finishedServiceTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd())
							- (int)DateTimeUtils
							.dateTime2Int(interval.getDateStart());
				}
			}
		}
		departureTime = finishedServiceTime;
		
		lastLocationCode = whFromWh.getLocationCode();
		
		//delivery at wh2
		Warehouse whToWh = solver.mCode2Warehouse.get(wr.getToWarehouseCode());
		
		if(solver.input.getParams().getConstraintWarehouseHard()){
			if(whToWh.getHardConstraintType() == Utils.HARD_DELIVERY_WH
				|| whToWh.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
				nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(whToWh.getCheckin(),
						truck.getDriverID());
				hardWh.add(whToWh);
			}
		}
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, whToWh.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						whToWh.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, whToWh.getLocationCode());
					}
			}
		}
		
		
		distance += solver.getDistance(lastLocationCode, whToWh.getLocationCode());
		arrivalTime = departureTime 
				+ solver.getTravelTime(lastLocationCode, whToWh.getLocationCode());
		if(wr.getLateDateTimeUnload() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(wr
					.getLateDateTimeUnload()))
				return null;

		startServiceTime = solver.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeUnload()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(wr
				.getEarlyDateTimeUnload()) - arrivalTime);
		if(whToWh.getBreaktimes() != null){
			for(int k = 0; k < whToWh.getBreaktimes().length; k++){
				Intervals interval = whToWh.getBreaktimes()[k];
				if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
					&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
					extraTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd()) - startServiceTime;
					startServiceTime = (int)DateTimeUtils.
							dateTime2Int(interval.getDateEnd());
				}
			}
		}
		finishedServiceTime = startServiceTime
				+ wr.getUnloadDuration();
		if(whToWh.getBreaktimes() != null){
			for(int k = 0; k < whToWh.getBreaktimes().length; k++){
				Intervals interval = whToWh.getBreaktimes()[k];
				if(finishedServiceTime > (int)DateTimeUtils
					.dateTime2Int(interval.getDateStart())
					&& startServiceTime < (int)DateTimeUtils
					.dateTime2Int(interval.getDateStart())){
					extraTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd())
							- (int)DateTimeUtils
							.dateTime2Int(interval.getDateStart());
					finishedServiceTime += (int)DateTimeUtils
							.dateTime2Int(interval.getDateEnd())
							- (int)DateTimeUtils
							.dateTime2Int(interval.getDateStart());
				}
			}
		}
		departureTime = finishedServiceTime;
		lastLocationCode = whToWh.getLocationCode();
		Container container = solver.mCode2Container.get(ir.getContainerCode());
		DepotContainer depotContainer = solver.findDepotContainer4Deposit(ir, lastLocationCode, container);
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, depotContainer.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						depotContainer.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, depotContainer.getLocationCode());
					}
			}
		}
			
			
		distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;

		startServiceTime = arrivalTime;
		duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc returnDepotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += solver.getDistance(lastLocationCode, returnDepotMooc.getLocationCode());
		arrivalTime = departureTime +
				solver.getTravelTime(lastLocationCode, returnDepotMooc.getLocationCode());
		startServiceTime = arrivalTime;
		duration = returnDepotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return null;
		lastLocationCode = returnDepotMooc.getLocationCode();
		
		//from mooc depot to truck depot
		DepotTruck returnDepotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, returnDepotTruck.getLocationCode());		
		arrivalTime = departureTime +
				solver.getTravelTime(lastLocationCode, returnDepotTruck.getLocationCode());
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsTruck(truck, 
				combo.startTimeOfTruck, departureTime))
			return null;
		
		return new Measure(distance, extraTime,
				nbCheckinAtHardWarehouse, isBalance, truck.getDriverID(),
				hardWh, srcdest);
	}

}
