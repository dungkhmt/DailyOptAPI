package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
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
import utils.DateTimeUtils;

public class RouteSwapImportExportCreator {
	public ContainerTruckMoocSolver solver;

	public ExportContainerRequest sel_exReq_a;
	public ImportContainerRequest sel_imReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;
	
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
	public TruckRouteInfo4Request createSwapImportExport() {
		// try to create route with truck-mooc serving ir and then er
		// truck-mooc -> port(ir) -> warehouse(ir) -> warehouse(er) -> port(er)
		// return null if violating constraint
//		if (!checkSwapImportExport(sel_truck, sel_mooc, sel_imReq_b, sel_exReq_a))
			//return null;
		int startTime = solver.mTruck2LastTime.get(sel_truck);
		String truckLocationCode = solver.mTruck2LastDepot.get(sel_truck)
				.getLocationCode();
		String moocLocationCode = solver.mMooc2LastDepot.get(sel_mooc)
				.getLocationCode();

		String portImCode = sel_imReq_b.getPortCode();
		Port portIm = solver.mCode2Port.get(portImCode);

		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc);
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
			e0.setDepotTruck(solver.mTruck2LastDepot.get(sel_truck));
			e0.setTruck(sel_truck);
			solver.mPoint2DepartureTime.put(e0, startTime);

			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
			e1.setMooc(sel_mooc);
			arrivalTime = startTime
					+ solver.getTravelTime(truckLocationCode, moocLocationCode);
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(sel_mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, combo.startTimeOfMooc);

			lastElement = e1;
		} else {
			TruckItinerary I = solver.getItinerary(sel_truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
			if(combo.mooc == null){
				departureTime = solver.mPoint2DepartureTime.get(lastElement);
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(sel_mooc);
				arrivalTime = departureTime + solver.getTravelTime(lastElement, e1);;
				int timeMooc = solver.mMooc2LastTime.get(sel_mooc);
				serviceTime = solver.MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = serviceTime + duration;
				solver.mPoint2ArrivalTime.put(e1, arrivalTime);
				solver.mPoint2DepartureTime.put(e1, combo.startTimeOfMooc);
				lastElement = e1;
			}
		}
		departureTime = combo.startTime;
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setPort(portIm);
		Container container = solver.mCode2Container.get(sel_imReq_b.getContainerCode());
		e2.setContainer(container);
		e2.setImportRequest(sel_imReq_b);
		arrivalTime = departureTime
				+ solver.getTravelTime(lastElement.getDepotMooc()
						.getLocationCode(), e2.getPort().getLocationCode());

		distance = combo.extraDistance
				+ solver.getDistance(combo.lastLocationCode,
						e2.getLocationCode());

//		if(sel_imReq_b.getLateDateTimePickupAtPort() != null){
//			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imReq_b
//				.getLateDateTimePickupAtPort()))
//				return null;
//		}
		
		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(sel_imReq_b.getEarlyDateTimePickupAtPort()));
		duration = sel_imReq_b.getLoadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);

		Port port = solver.getPortFromCode(sel_exReq_a.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(e2.getLocationCode(), departureTime, sel_imReq_b, sel_exReq_a, port.getLocationCode());
		
		if(sol == null) return null;
		int ni = sel_imReq_b.getDeliveryWarehouses().length;
		int ne = sel_exReq_a.getPickupWarehouses().length;

		RouteElement[] e = new RouteElement[2*(ni+ne)];
		int idx = -1;
		RouteElement le = e2;
		for(int i = 0; i < ni; i++){
			DeliveryWarehouseInfo dwi = sel_imReq_b.getDeliveryWarehouses()[sol.seq[i]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + i + "] = " + sol.seq[i] + ", dwi = " + dwi.getWareHouseCode());
			
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(solver.getWarehouseFromCode(dwi.getWareHouseCode()));
			e[idx].setImportRequest(sel_imReq_b);
			arrivalTime = departureTime
					+ solver.getTravelTime(le,e[idx]);
			
//			if (dwi.getLateDateTimeUnloadAtWarehouse() != null && arrivalTime > DateTimeUtils.dateTime2Int(
//					dwi.getLateDateTimeUnloadAtWarehouse()))
//				return null;

			serviceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = serviceTime
					+ sel_imReq_b.getUnloadDuration();
			departureTime = finishedServiceTime;// startServiceTime + duration;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
		}
		
		for(int i = 0; i < ne; i++){
			PickupWarehouseInfo pwi = sel_exReq_a.getPickupWarehouses()[sol.seq[i + ni]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + (i+ni) + "] = " + sol.seq[i+ni] + ", pwi = " + pwi.getWareHouseCode());
			idx++;
			e[idx] = new RouteElement();
			L.add(e[idx]);
			e[idx].deriveFrom(le);
			e[idx].setImportRequest(null);
			e[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e[idx].setWarehouse(solver.getWarehouseFromCode(pwi.getWareHouseCode()));
			e[idx].setExportRequest(sel_exReq_a);
			arrivalTime = departureTime
					+ solver.getTravelTime(le,e[idx]);

//			if (sel_exReq_a.getLateDateTimeLoadAtWarehouse() != null && arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq_a
//					.getLateDateTimeLoadAtWarehouse()))
//				return null;

			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(pwi.getEarlyDateTimeLoadAtWarehouse()));
			int finishedServiceTime = serviceTime
					+ sel_exReq_a.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;
			solver.mPoint2ArrivalTime.put(e[idx], arrivalTime);
			solver.mPoint2DepartureTime.put(e[idx], departureTime);
			le = e[idx];
		}
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(le);
		e7.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e7.setPort(solver.mCode2Port.get(sel_exReq_a.getPortCode()));
		e7.setContainer(null);
		e7.setExportRequest(null);
		arrivalTime = departureTime
				+ solver.getTravelTime(le,e7);
//		if (sel_exReq_a.getLateDateTimeUnloadAtPort() != null && arrivalTime > DateTimeUtils.dateTime2Int(sel_exReq_a
//				.getLateDateTimeUnloadAtPort()))
//			return null;
		serviceTime = solver.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(sel_exReq_a
				.getEarlyDateTimeUnloadAtPort()));
		duration = sel_exReq_a.getUnloadDuration();
		departureTime = serviceTime + duration;
		solver.mPoint2ArrivalTime.put(e7, arrivalTime);
		solver.mPoint2DepartureTime.put(e7, departureTime);
		tri.setLastDepotContainer(container, null);
		tri.setLastTimeContainer(container, Integer.MAX_VALUE);
		
		RouteElement e8 = new RouteElement();
		L.add(e8);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(sel_imReq_b, e7, sel_mooc);
		e8.deriveFrom(e7);
		e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e8.setDepotMooc(depotMooc);
		e8.setMooc(null);
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
		tri.setLastDepotMooc(sel_mooc, depotMooc);
		tri.setLastTimeMooc(sel_mooc, departureTime);
		
		RouteElement e9 = new RouteElement();
		L.add(e9);
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(sel_imReq_b, e8, sel_truck);
		e9.deriveFrom(e8);
		e9.setTruck(null);
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
		tri.setLastDepotTruck(sel_truck, depotTruck);
		tri.setLastTimeTruck(sel_truck, departureTime);
		
		//System.out.println(name() + "::createSwapImportExport, L.sz = " + L.size());
		//for(int i = 0; i < L.size(); i++){
		//	System.out.println(name() + "::createSwapImportExport, L[" + i + "] = " + L.get(i).getLocationCode());
		//}
		
		TruckRoute r = new TruckRoute();
		RouteElement[] lst_e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			lst_e[i] = L.get(i);
		r.setNodes(lst_e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.SWAP);
		solver.propagate(r);
		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;
		return tri;

	}
	
	public Measure evaluateSwapImportExport(Truck truck,
			Mooc mooc, ImportContainerRequest ir, ExportContainerRequest er) {
		// try to create route with truck-mooc serving ir and then er
		// truck-mooc -> port(ir) -> warehouse(ir) -> warehouse(er) -> port(er)
		// return null if violating constraint

		String portImCode = ir.getPortCode();
		Port portIm = solver.mCode2Port.get(portImCode);

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		int extraTime = 0;
		double distance = -1;
		String lastLocationCode = combo.lastLocationCode;
		distance = combo.extraDistance;

		//to port im
		distance += solver.getDistance(lastLocationCode, portIm.getLocationCode());
		int travelTime = solver.getTravelTime(lastLocationCode, portIm.getLocationCode());
		arrivalTime = departureTime
				+ travelTime;

//		if(ir.getLateDateTimePickupAtPort() != null){
//			if (arrivalTime > DateTimeUtils.dateTime2Int(ir
//				.getLateDateTimePickupAtPort()))
//				return null;
//		}
		
		serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(ir.getEarlyDateTimePickupAtPort()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(ir
				.getEarlyDateTimePickupAtPort()) - arrivalTime);
		if(combo.routeElement == null){
			combo.startTime -= extraTime;
			combo.startTimeOfMooc -= extraTime;
			combo.startTimeOfTruck -= extraTime;
			extraTime = 0;
		}
		duration = ir.getLoadDuration();
		departureTime = serviceTime + duration;
		lastLocationCode = portIm.getLocationCode();

		Port portEx = solver.getPortFromCode(er.getPortCode());
		SequenceSolver SS = new SequenceSolver(solver);
		SequenceSolution sol = SS.solve(lastLocationCode, departureTime,
				ir, er, portEx.getLocationCode());
		
		if(sol == null) return null;
		int ni = ir.getDeliveryWarehouses().length;
		int ne = er.getPickupWarehouses().length;

		
		int nbCheckinAtHardWarehouse = 0;
		boolean isBalance = false;
		HashMap<String, String> srcdest = new HashMap<String, String>();
		ArrayList<Warehouse> hardWh = new ArrayList<Warehouse>();
		for(int i = 0; i < ni; i++){
			DeliveryWarehouseInfo dwi = ir.getDeliveryWarehouses()[sol.seq[i]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + i + "] = " + sol.seq[i] + ", dwi = " + dwi.getWareHouseCode());
			Warehouse wh = solver.getWarehouseFromCode(dwi.getWareHouseCode());

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
			
			arrivalTime = departureTime
					+ solver.getTravelTime(lastLocationCode, wh.getLocationCode());

//			if (dwi.getLateDateTimeUnloadAtWarehouse() != null
//				&& arrivalTime > DateTimeUtils.dateTime2Int(
//					dwi.getLateDateTimeUnloadAtWarehouse()))
//				return null;

			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(dwi.getEarlyDateTimeUnloadAtWarehouse()));
			extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(dwi
					.getEarlyDateTimeUnloadAtWarehouse()) - arrivalTime);
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(serviceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
						&& serviceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - serviceTime;
						serviceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}
			int finishedServiceTime = serviceTime
					+ ir.getUnloadDuration();
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(finishedServiceTime > (int)DateTimeUtils
						.dateTime2Int(interval.getDateStart())
						&& serviceTime < (int)DateTimeUtils
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
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			lastLocationCode= wh.getLocationCode();
			
		}
		
		for(int i = 0; i < ne; i++){
			PickupWarehouseInfo pwi = er.getPickupWarehouses()[sol.seq[i + ni]];
			//System.out.println(name() + "::createSwapImportExport, seq[" + (i+ni) + "] = " + sol.seq[i+ni] + ", pwi = " + pwi.getWareHouseCode());
			Warehouse wh = solver.getWarehouseFromCode(pwi.getWareHouseCode());

			if(solver.input.getParams().getConstraintWarehouseHard()){
				if(wh.getHardConstraintType() == Utils.HARD_PICKUP_WH
					|| wh.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
					nbCheckinAtHardWarehouse = solver.getCheckinAtWarehouse(wh.getCheckin(), truck.getDriverID());
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
			
			
			arrivalTime = departureTime + solver.getTravelTime(lastLocationCode, wh.getLocationCode());
			// check time
//			if (er.getLateDateTimeLoadAtWarehouse() != null
//				&& arrivalTime > DateTimeUtils.dateTime2Int(er
//					.getLateDateTimeLoadAtWarehouse()))
//				return null;

			serviceTime = solver.MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(pwi
					.getEarlyDateTimeLoadAtWarehouse()) - arrivalTime);
			distance += solver.getDistance(lastLocationCode, wh.getLocationCode());
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(serviceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
						&& serviceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - serviceTime;
						serviceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}
			
			int finishedServiceTime = serviceTime
					+ er.getLoadDuration();
			if(wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(finishedServiceTime > (int)DateTimeUtils
						.dateTime2Int(interval.getDateStart())
						&& serviceTime < (int)DateTimeUtils
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
			lastLocationCode = wh.getLocationCode();
		}
		
		//System.out.println(name() + "::createSwapImportExport, L.sz = " + L.size());
		//for(int i = 0; i < L.size(); i++){
		//	System.out.println(name() + "::createSwapImportExport, L[" + i + "] = " + L.get(i).getLocationCode());
		//}
		
		if(solver.input.getParams().getConstraintDriverBalance()){
			if(solver.getIsDriverBalance(lastLocationCode, portEx.getLocationCode())){
				ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
						portEx.getLocationCode());
				for(int k = 0; k < drl.size(); k++)
					if(drl.get(k) == truck.getDriverID()){
						isBalance = true;
						srcdest.put(lastLocationCode, portEx.getLocationCode());
					}
			}
		}
		
		distance += solver.getDistance(lastLocationCode, portEx.getLocationCode());
		arrivalTime = departureTime
				+ solver.getTravelTime(lastLocationCode, portEx.getLocationCode());
//		if (er.getLateDateTimeUnloadAtPort() != null
//			&& arrivalTime > DateTimeUtils.dateTime2Int(er
//				.getLateDateTimeUnloadAtPort()))
//			return null;
		serviceTime = solver.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(er
				.getEarlyDateTimeUnloadAtPort()));
		extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(er
				.getEarlyDateTimeUnloadAtPort()) - arrivalTime);
		duration = er.getUnloadDuration();
		departureTime = serviceTime + duration;
		lastLocationCode = portEx.getLocationCode();
		
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += solver.getDistance(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + 
				solver.getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		serviceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = serviceTime + duration;
		if(!solver.checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return null;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, depotTruck.getLocationCode());		
		arrivalTime = departureTime + solver.getTravelTime(
				lastLocationCode, depotTruck.getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		if(!solver.checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return null;
		return new Measure(distance, extraTime,
				nbCheckinAtHardWarehouse, isBalance, truck.getDriverID(),
				hardWh, srcdest);

	}

}
