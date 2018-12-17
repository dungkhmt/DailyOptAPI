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
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.Intervals;
import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;
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
	public ImportEmptyRequests sel_imEmptyReq_a;
	public ImportEmptyRequests sel_imEmptyReq_b;
	public Truck sel_truck;
	public Mooc sel_mooc;

	public RouteDoubleImportEmptyCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportEmptyCreator";
	}
	
	public boolean checkCapacityForDoubleImportEmpty(Truck truck, Mooc mooc,
			ImportEmptyRequests ir1, ImportEmptyRequests ir2) {
		if (!ir1.getContainerCategory()
				.equals(ContainerCategoryEnum.CATEGORY20)
				|| !ir2.getContainerCategory().equals(
						ContainerCategoryEnum.CATEGORY20))
			return false;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20))
			return false;

		return true;
	}
	
	public Measure evaluateImportEmptyImportEmptyRequest(
			ImportEmptyRequests req_a, ImportEmptyRequests req_b,
			Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		double distance = -1;
		int extraTime = 0;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		distance = combo.extraDistance;
		
		Warehouse wh_a = solver.mCode2Warehouse.get(req_a.getWareHouseCode());
		travelTime = solver.getTravelTime(lastLocationCode, wh_a.getLocationCode());
		distance += solver.getDistance(lastLocationCode, wh_a.getLocationCode());
		
		arrivalTime = departureTime + travelTime;
		if (req_a.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_a
					.getLateDateTimeAttachAtWarehouse()))
				return null;
		startServiceTime = arrivalTime;
		if(wh_a.getBreaktimes() != null){
			for(int k = 0; k < wh_a.getBreaktimes().length; k++){
				Intervals interval = wh_a.getBreaktimes()[k];
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
				+ solver.input.getParams().getLinkEmptyContainerDuration();
				//+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
		if(wh_a.getBreaktimes() != null){
			for(int k = 0; k < wh_a.getBreaktimes().length; k++){
				Intervals interval = wh_a.getBreaktimes()[k];
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
		lastLocationCode= wh_a.getLocationCode();
		
		Warehouse wh_b = solver.mCode2Warehouse.get(req_b.getWareHouseCode());
		travelTime = solver.getTravelTime(lastLocationCode, wh_b.getLocationCode());
		distance += solver.getDistance(lastLocationCode, wh_b.getLocationCode());
		
		arrivalTime = departureTime + travelTime;
		if (req_b.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_b
					.getLateDateTimeAttachAtWarehouse()))
				return null;
		startServiceTime = arrivalTime;
		if(wh_b.getBreaktimes() != null){
			for(int k = 0; k < wh_b.getBreaktimes().length; k++){
				Intervals interval = wh_b.getBreaktimes()[k];
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
				+ solver.input.getParams().getLinkEmptyContainerDuration();
				//+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
		if(wh_b.getBreaktimes() != null){
			for(int k = 0; k < wh_b.getBreaktimes().length; k++){
				Intervals interval = wh_b.getBreaktimes()[k];
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
		lastLocationCode= wh_b.getLocationCode();
		
		//from wh to container depot
		DepotContainer depotContainer_a = solver.findDepotForReleaseContainer(lastLocationCode,
				solver.mCode2Container.get(req_a.getContainerCode()));
		distance += solver.getDistance(lastLocationCode, depotContainer_a.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer_a.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req_a.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_a
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;
		startServiceTime = arrivalTime;
		int duration = depotContainer_a.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer_a.getLocationCode();
		
		
		//from container depot a to container depot b
		DepotContainer depotContainer_b = solver.findDepotForReleaseContainer(lastLocationCode,
				solver.mCode2Container.get(req_b.getContainerCode()));
		distance += solver.getDistance(lastLocationCode, depotContainer_b.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, depotContainer_b.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req_b.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req_b
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;
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
			return null;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		distance += solver.getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = solver.getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return null;	
		
		return new Measure(distance, extraTime);
	}
	
	public TruckRouteInfo4Request createRouteForImportEmptyImportEmptyRequest() {
		// truck and mooc are possibly not REST at their depots

		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc);
		if (combo == null)
			return null;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		double distance = -1;

		if (combo.routeElement == null) {
			departureTime = solver.mTruck2LastTime.get(sel_truck);
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = solver.mTruck2LastDepot.get(sel_truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(sel_truck);
			solver.mPoint2DepartureTime.put(e0, combo.startTimeOfTruck);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			DepotMooc depotMooc = solver.mMooc2LastDepot.get(sel_mooc);
			e1.setDepotMooc(depotMooc);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setMooc(sel_mooc);

			travelTime = solver.getTravelTime(e0, e1);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
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
				solver.mPoint2DepartureTime.put(e1, combo.startTimeOfMooc);
				lastElement = e1;
			}
		}
		departureTime = combo.startTime;
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		Container container_a = solver.mCode2Container.get(sel_imEmptyReq_a.getContainerCode());
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e2.setImportEmptyRequest(sel_imEmptyReq_a);
		e2.setContainer(container_a);
		e2.setWarehouse(solver.mCode2Warehouse.get(sel_imEmptyReq_a.getWareHouseCode()));

		travelTime = solver.getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (sel_imEmptyReq_a.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imEmptyReq_a
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = solver.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(sel_imEmptyReq_a.getRequestDate()));
		distance = combo.extraDistance + solver.getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ solver.input.getParams().getLinkEmptyContainerDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e2, arrivalTime);
		solver.mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;
		
		Container container_b = solver.mCode2Container.get(sel_imEmptyReq_b.getContainerCode());
		RouteElement e22 = new RouteElement();
		L.add(e22);
		e22.deriveFrom(lastElement);
		e22.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e22.setImportEmptyRequestKep(sel_imEmptyReq_b);
		e22.setContainerKep(container_b);
		e22.setWarehouse(solver.mCode2Warehouse.get(sel_imEmptyReq_b.getWareHouseCode()));

		travelTime = solver.getTravelTime(lastElement, e22);
		arrivalTime = departureTime + travelTime;
		// check time
		if (sel_imEmptyReq_b.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imEmptyReq_b
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = solver.MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(sel_imEmptyReq_b.getRequestDate()));
		distance += solver.getDistance(lastElement, e22);

		finishedServiceTime = startServiceTime
				+ solver.input.getParams().getLinkEmptyContainerDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e22, arrivalTime);
		solver.mPoint2DepartureTime.put(e22, departureTime);
		lastElement = e22;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		DepotContainer depot_a = solver.findDepotForReleaseContainer(lastElement,
				container_a);
		e3.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e3.setDepotContainer(depot_a);
		e3.setContainer(null);
		e3.setImportEmptyRequest(null);

		travelTime = solver.getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (sel_imEmptyReq_a.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imEmptyReq_a
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;

		startServiceTime = arrivalTime;
		distance += solver.getDistance(lastElement, e3);

		finishedServiceTime = startServiceTime
				+ solver.input.getParams().getCutMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e3, arrivalTime);
		solver.mPoint2DepartureTime.put(e3, departureTime);
		tri.setLastDepotContainer(container_a, depot_a);
		tri.setLastTimeContainer(container_a, departureTime);
		lastElement = e3;
		
		RouteElement e33 = new RouteElement();
		L.add(e33);
		e33.deriveFrom(lastElement);
		DepotContainer depot_b = solver.findDepotForReleaseContainer(lastElement,
				container_b);
		e33.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e33.setDepotContainer(depot_b);
		e33.setContainerKep(null);
		e33.setImportEmptyRequestKep(null);

		travelTime = solver.getTravelTime(lastElement, e33);
		arrivalTime = departureTime + travelTime;
		// check time
		if (sel_imEmptyReq_b.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(sel_imEmptyReq_b
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;

		startServiceTime = arrivalTime;
		distance += solver.getDistance(lastElement, e33);

		finishedServiceTime = startServiceTime
				+ solver.input.getParams().getCutMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e33, arrivalTime);
		solver.mPoint2DepartureTime.put(e33, departureTime);
		tri.setLastDepotContainer(container_b, depot_b);
		tri.setLastTimeContainer(container_b, departureTime);
		lastElement = e33;

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		DepotMooc depotMooc = solver.findDepotMooc4Deposit(e3, sel_mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e4.setDepotMooc(depotMooc);
		e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e4.setMooc(null);
		travelTime = solver.getTravelTime(e3, e4);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e4.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e4, arrivalTime);
		solver.mPoint2DepartureTime.put(e4, departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(sel_mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(sel_mooc, departureTime);

		tri.setLastDepotMooc(sel_mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(sel_mooc, departureTime);
		lastElement = e4;

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = solver.findDepotTruck4Deposit(lastElement, sel_truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = solver.getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		solver.mPoint2ArrivalTime.put(e5, arrivalTime);
		solver.mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(sel_truck, e5.getDepotTruck());
		tri.setLastTimeTruck(sel_truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(sel_truck);
		tr.setType(TruckRoute.DIRECT_IMPORT_EMPTY);
		solver.propagate(tr);
		solver.updateTruckAtDepot(sel_truck);
		solver.updateMoocAtDepot(sel_mooc);
		solver.updateContainerAtDepot(container_a);
		solver.updateContainerAtDepot(container_b);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}
}
