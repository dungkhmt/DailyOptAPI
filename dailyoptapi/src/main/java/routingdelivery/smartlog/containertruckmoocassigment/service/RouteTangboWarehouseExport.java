package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
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
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseTransportRequest;
import utils.DateTimeUtils;

public class RouteTangboWarehouseExport {
	ContainerTruckMoocSolver solver;
	public WarehouseContainerTransportRequest sel_whReq;
	public ExportContainerRequest sel_exReq;
	public Truck sel_truck;
	public Mooc sel_mooc;
	public Container sel_container;

	public RouteTangboWarehouseExport(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteTangboWarehouseExport";
	}

	public boolean checkTangboWarehouseExport(Truck truck, Mooc mooc,
			Container container, WarehouseContainerTransportRequest wr,
			ExportContainerRequest er) {
		boolean ok = true;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20)) {
			if (!container.getCategoryCode().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY40)) {
			if (container.getCategoryCode().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
		}
		if (!ok)
			return ok;

		if (container.getCategoryCode()
				.equals(ContainerCategoryEnum.CATEGORY20)) {
			if (!wr.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
			if (!er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (container.getCategoryCode().equals(
				ContainerCategoryEnum.CATEGORY40)) {
			if (wr.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
		}

		return ok;
	}

	public TruckRouteInfo4Request createTangboWarehouseExport() {
		// try to create route with truck-mooc-container serving wr and then er
		// truck-mooc-container -> from_warehouse(wr) -> to_warehouse(wr) ->
		// warehouse(er) -> port(er)
		ComboContainerMoocTruck combo = null;
		combo = solver.findLastAvailable(sel_truck, sel_mooc);
		sel_container = solver.mCode2Container.get(sel_whReq.getContainerCode());
		
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
		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e3.setWarehouse(solver.mCode2Warehouse.get(sel_whReq.getFromWarehouseCode()));
		e3.setWarehouseRequest(sel_whReq);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getLocationCode(), e3.getWarehouse().getLocationCode());
		distance = combo.extraDistance + solver.getDistance(lastElement, e3);

		if (arrivalTime > DateTimeUtils.dateTime2Int(sel_whReq.getLateDateTimeLoad())) {
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

		Port port = solver.getPortFromCode(sel_exReq.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(e5.getLocationCode(), departureTime, sel_exReq,
				port.getLocationCode());
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = sel_exReq.getPickupWarehouses()[seq[i]];
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setWarehouse(solver.mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setExportRequest(sel_exReq);
			re[idx].setWarehouseRequest(null);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement, re[idx]);
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq
					.getLateDateTimeLoadAtWarehouse())) {
				solver.logln(name()
						+ "::createTangboWarehouseExport, violation time arrival = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
						+ " > er.getLateDateTimeLoadAtWarehouse() = "
						+ sel_exReq.getLateDateTimeLoadAtWarehouse());
				return null;
			}

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

			startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(pwi.getEarlyDateTimeLoadAtWarehouse()));
			finishedServiceTime = startServiceTime
					+ sel_exReq.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;
			solver.mPoint2ArrivalTime.put(re[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();
		}

		RouteElement e9 = new RouteElement();
		L.add(e9);
		e9.deriveFrom(lastElement);
		e9.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e9.setPort(solver.mCode2Port.get(sel_exReq.getPortCode()));
		e9.setContainer(null);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getWarehouse().getLocationCode(), e9
						.getPort().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq
				.getLateDateTimeUnloadAtPort())) {
			solver.logln(name()
					+ "::createTangboWarehouseExport, violation time arrival = "
					+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
					+ " > er.getLateDateTimeUnloadAtPort() = "
					+ sel_exReq.getLateDateTimeUnloadAtPort());
			return null;
		}
		
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(sel_exReq.getEarlyDateTimeUnloadAtPort()));
		duration = sel_exReq.getUnloadDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e9, arrivalTime);
		solver.mPoint2DepartureTime.put(e9, departureTime);
		// solver.mContainer2LastDepot.put(container, null);
		// solver.mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(sel_container, null);
		tri.setLastTimeContainer(sel_container, Integer.MAX_VALUE);

		RouteElement e10 = new RouteElement();
		L.add(e10);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_exReq, e9, sel_mooc);
		e10.deriveFrom(e9);
		e10.setExportRequest(null);
		e10.setWarehouseRequest(null);
		e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e10.setDepotMooc(depotMooc);
		e10.setMooc(null);
		arrivalTime = departureTime
				+ solver.getTravelTime(e9.getPort().getLocationCode(), e10
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
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_exReq, e10, sel_truck);

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

		TruckRoute r = new TruckRoute();
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
	
	public Measure evaluateTangboWarehouseExport(Truck truck,
			Mooc mooc, Container container,
			WarehouseContainerTransportRequest wr, ExportContainerRequest er) {
		// try to create route with truck-mooc-container serving wr and then er
		// truck-mooc-container -> from_warehouse(wr) -> to_warehouse(wr) ->
		// warehouse(er) -> port(er)
		if (solver.mContainer2LastDepot.get(container) == null) {
			// this is imported container, does not has depot
			solver.logln(name()
					+ "::createTangboWarehouseExport, imported container null");
			return null;
		}

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc,
				container);
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
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		String lastLocationCode = combo.lastLocationCode;

		//from wh1
		Warehouse whFromWh = solver.mCode2Warehouse.get(wr.getFromWarehouseCode());
				
		int nbCheckinAtHardWarehouse = 0;
		boolean isBalance = false;
		HashMap<String, String> srcdest = new HashMap<String, String>();
		ArrayList<Warehouse> hardWh = new ArrayList<Warehouse>();
		
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
		int travelTime = solver.getTravelTime(lastLocationCode, whFromWh.getLocationCode());
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
		if(combo.routeElement == null){
			int mooc2cont = combo.startTime - combo.startTimeOfMooc;
			int truck2moocdepot = combo.startTimeOfMooc - combo.startTimeOfTruck;
			combo.startTime = startServiceTime - travelTime;
			combo.startTimeOfMooc = combo.startTime - mooc2cont;
			combo.startTimeOfTruck = combo.startTimeOfMooc - truck2moocdepot;
			extraTime = 0;
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
		if (wr.getLateDateTimeUnload() != null && arrivalTime > DateTimeUtils.dateTime2Int(wr
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

		Port port = solver.getPortFromCode(er.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode, departureTime, er,
				port.getLocationCode());
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[seq[i]];
			Warehouse whEx = solver.mCode2Warehouse.get(pwi.getWareHouseCode());

			if(solver.input.getParams().getConstraintWarehouseHard()){
				if(whEx.getHardConstraintType() == Utils.HARD_PICKUP_WH
					|| whEx.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
					nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(whEx.getCheckin(), truck.getDriverID());
					hardWh.add(whEx);
				}
			}
			if(solver.input.getParams().getConstraintDriverBalance()){
				if(solver.getIsDriverBalance(lastLocationCode, whEx.getLocationCode())){
					ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
							whEx.getLocationCode());
					for(int k = 0; k < drl.size(); k++)
						if(drl.get(k) == truck.getDriverID()){
							isBalance = true;
							srcdest.put(lastLocationCode, whEx.getLocationCode());
						}
				}
			}
			
			
			arrivalTime = departureTime 
				+ solver.getTravelTime(lastLocationCode, whEx.getLocationCode());;
			// check time
			if (er.getLateDateTimeLoadAtWarehouse() != null && arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(pwi
					.getEarlyDateTimeLoadAtWarehouse()) - arrivalTime);
			if(whEx.getBreaktimes() != null){
				for(int k = 0; k < whEx.getBreaktimes().length; k++){
					Intervals interval = whEx.getBreaktimes()[k];
					if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
						&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - startServiceTime;
						startServiceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}
			distance += solver.getDistance(lastLocationCode, whEx.getLocationCode());
			finishedServiceTime = startServiceTime
					+ er.getLoadDuration();
			if(whEx.getBreaktimes() != null){
				for(int k = 0; k < whEx.getBreaktimes().length; k++){
					Intervals interval = whEx.getBreaktimes()[k];
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
			lastLocationCode = whEx.getLocationCode();
		}
		
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, port.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						port.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, port.getLocationCode());
					}
			}
		}
		
		distance += solver.getDistance(lastLocationCode, port.getLocationCode());
		lastLocationCode = port.getLocationCode();
		arrivalTime = departureTime
				+ solver.getTravelTime(lastLocationCode, port.getLocationCode());
		if(er.getLateDateTimeUnloadAtPort() != null){
			if (arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeUnloadAtPort()))
				return null;
		}
		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(er.getEarlyDateTimeUnloadAtPort()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(er
				.getEarlyDateTimeUnloadAtPort()) - arrivalTime);
		duration = er.getUnloadDuration();
		departureTime = startServiceTime + duration;

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
	
	public Measure evaluateTangboWarehouseExport(Truck truck,
			Mooc mooc, WarehouseContainerTransportRequest wr,
			ExportContainerRequest er) {
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
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		String lastLocationCode = combo.lastLocationCode;

		//from wh1
		Warehouse whFromWh = solver.mCode2Warehouse.get(wr.getFromWarehouseCode());
		
		int nbCheckinAtHardWarehouse = 0;
		boolean isBalance = false;
		HashMap<String, String> srcdest = new HashMap<String, String>();
		ArrayList<Warehouse> hardWh = new ArrayList<Warehouse>();
		
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
		int travelTime = solver.getTravelTime(lastLocationCode, whFromWh.getLocationCode());
		arrivalTime = departureTime
				+ travelTime;
		

		if (arrivalTime > DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad())) {
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
		if(combo.routeElement == null){
			int mooc2cont = combo.startTime - combo.startTimeOfMooc;
			int truck2moocdepot = combo.startTimeOfMooc - combo.startTimeOfTruck;
			combo.startTime = startServiceTime - travelTime;
			combo.startTimeOfMooc = combo.startTime - mooc2cont;
			combo.startTimeOfTruck = combo.startTimeOfMooc - truck2moocdepot;
			extraTime = 0;
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
		if (wr.getLateDateTimeUnload() != null && arrivalTime > DateTimeUtils.dateTime2Int(wr
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

		//from wh2 to warehouse of export req
		Port port = solver.getPortFromCode(er.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution ss = SS.solve(lastLocationCode, departureTime, er,
				port.getLocationCode());
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[seq[i]];
			Warehouse whEx = solver.mCode2Warehouse.get(pwi.getWareHouseCode());

			if(solver.input.getParams().getConstraintWarehouseHard()){
				if(whEx.getHardConstraintType() == Utils.HARD_PICKUP_WH
					|| whEx.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
					nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(whEx.getCheckin(), truck.getDriverID());
					hardWh.add(whEx);
				}
			}
			if(solver.input.getParams().getConstraintDriverBalance()){
				if(solver.getIsDriverBalance(lastLocationCode, whEx.getLocationCode())){
					ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
							whEx.getLocationCode());
					for(int k = 0; k < drl.size(); k++)
						if(drl.get(k) == truck.getDriverID()){
							isBalance = true;
							srcdest.put(lastLocationCode, whEx.getLocationCode());
						}
				}
			}
			
			
			arrivalTime = departureTime 
				+ solver.getTravelTime(lastLocationCode, whEx.getLocationCode());;
			// check time
			if (er.getLateDateTimeLoadAtWarehouse() != null && arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			startServiceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(pwi
					.getEarlyDateTimeLoadAtWarehouse()) - arrivalTime);
			if(whEx.getBreaktimes() != null){
				for(int k = 0; k < whEx.getBreaktimes().length; k++){
					Intervals interval = whEx.getBreaktimes()[k];
					if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
						&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - startServiceTime;
						startServiceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}
			distance += solver.getDistance(lastLocationCode, whEx.getLocationCode());
			finishedServiceTime = startServiceTime
					+ er.getLoadDuration();
			if(whEx.getBreaktimes() != null){
				for(int k = 0; k < whEx.getBreaktimes().length; k++){
					Intervals interval = whEx.getBreaktimes()[k];
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
			lastLocationCode = whEx.getLocationCode();

			arrivalTime = departureTime;
			if (pwi.getLateDateTimePickupLoadedContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(pwi
						.getLateDateTimePickupLoadedContainerAtWarehouse()))
					return null;

			if (pwi.getEarlyDateTimePickupLoadedContainerAtWarehouse() != null){
				startServiceTime = solver.MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
				extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(pwi
						.getEarlyDateTimePickupLoadedContainerAtWarehouse()) - arrivalTime);
				if(whEx.getBreaktimes() != null){
					for(int k = 0; k < whEx.getBreaktimes().length; k++){
						Intervals interval = whEx.getBreaktimes()[k];
						if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
							&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
							extraTime += (int)DateTimeUtils
									.dateTime2Int(interval.getDateEnd()) - startServiceTime;
							startServiceTime = (int)DateTimeUtils.
									dateTime2Int(interval.getDateEnd());
						}
					}
				}
			}
			else{
				startServiceTime = arrivalTime;
				if(whEx.getBreaktimes() != null){
					for(int k = 0; k < whEx.getBreaktimes().length; k++){
						Intervals interval = whEx.getBreaktimes()[k];
						if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
							&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
							extraTime += (int)DateTimeUtils
									.dateTime2Int(interval.getDateEnd()) - startServiceTime;
							startServiceTime = (int)DateTimeUtils.
									dateTime2Int(interval.getDateEnd());
						}
					}
				}
			}

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();
			if(whEx.getBreaktimes() != null){
				for(int k = 0; k < whEx.getBreaktimes().length; k++){
					Intervals interval = whEx.getBreaktimes()[k];
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
		}
		
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, port.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						port.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, port.getLocationCode());
					}
			}
		}
		
		distance += solver.getDistance(lastLocationCode, port.getLocationCode());
		lastLocationCode = port.getLocationCode();
		arrivalTime = departureTime
				+ solver.getTravelTime(lastLocationCode, port.getLocationCode());
		if(er.getLateDateTimeUnloadAtPort() != null){
			if (arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeUnloadAtPort()))
				return null;
		}
		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(er.getEarlyDateTimeUnloadAtPort()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(er
				.getEarlyDateTimeUnloadAtPort()) - arrivalTime);
		duration = er.getUnloadDuration();
		departureTime = startServiceTime + duration;

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
