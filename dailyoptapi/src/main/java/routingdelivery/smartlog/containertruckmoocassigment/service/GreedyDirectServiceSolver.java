package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import utils.DateTimeUtils;

public class GreedyDirectServiceSolver extends ContainerTruckMoocSolver{
	//public ContainerTruckMoocSolver solver;
	
	public String name(){
		return "GreedyDirectServiceSolver";
	}
	
	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		this.input = input;
		
		initLog();
		modifyContainerCode();
		mapData();
		init();
		mTruck2Route = new HashMap<Truck, TruckRoute>();
		
		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();
		
		
		ExportContainerRequest[] exReq = getSortedExportRequests();
		for(int i = 0; i < exReq.length; i++){
			TruckRoute tr = createDirectRoute4ExportRequest(exReq[i]);
			
			
			if(tr != null){
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}
				System.out.println(name() + "::solve, FIND new TruckRoute for export request");
			}else{
				System.out.println(name() + "::solve, CANNOT FIND new TruckRoute for export request");
			}
		}
		
		ImportContainerRequest[] imReq = getSortedImportRequests();
		for(int i = 0; i < imReq.length; i++){
			TruckRoute tr = createDirectRoute4ImportRequest(imReq[i]);
			
			
			if(tr != null){
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}
				System.out.println(name() + "::solve, FIND new TruckRoute for import request");
			}else{
				System.out.println(name() + "::solve, CANNOT FIND new TruckRoute for import request");
			}
		}
		
		/*
		for (int i = 0; i < input.getExRequests().length; i++) {
			ExportContainerTruckMoocRequest R = input.getExRequests()[i];
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ExportContainerRequest r = R.getContainerRequest()[j];
				TruckRoute tr = createDirectRouteForExportRequest(r);
				lst_truckRoutes.add(tr);
			}
		}
		
		for (int i = 0; i < input.getImRequests().length; i++) {
			ImportContainerTruckMoocRequest R = input.getImRequests()[i];
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ImportContainerRequest r = R.getContainerRequest()[j];
				TruckRoute tr = createDirectRouteForImportRequest(r);
				lst_truckRoutes.add(tr);
			}
		}

		for (int i = 0; i < input.getWarehouseRequests().length; i++) {
			WarehouseTransportRequest R = input.getWarehouseRequests()[i];
			for (int j = 0; j < R.getWarehouseContainerTransportRequests().length; j++) {
				WarehouseContainerTransportRequest r = R
						.getWarehouseContainerTransportRequests()[j];
				TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r);
				lst_truckRoutes.add(tr);
			}
		}

		improveSwap(lst_truckRoutes);
		*/
		
		recoverContainerCode();
		
		TruckRoute[] TR = new TruckRoute[lst_truckRoutes.size()];
		for (int i = 0; i < TR.length; i++)
			TR[i] = lst_truckRoutes.get(i);

		double totalDistance = 0;
		for (int i = 0; i < TR.length; i++) {
			TruckRoute tr = TR[i];
			RouteElement[] e = tr.getNodes();
			if (e != null && e.length > 0) {
				totalDistance = totalDistance + e[e.length - 1].getDistance();
			}
		}

		StatisticInformation infos = new StatisticInformation(totalDistance,
				TR.length);

		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
				infos, "OK");

		
		return sol;
	}

	public TruckRoute createDirectRoute4ImportRequest(ImportContainerRequest r){
		String portCode = r.getPortCode();
		Port port = mCode2Port.get(portCode);
		String locationCodePort = mCode2Port.get(portCode).getLocationCode();
		int latePickupPort = (int)DateTimeUtils.dateTime2Int(r.getLateDateTimePickupAtPort());
		
		ComboContainerMoocTruck sel_combo = null;
		double minDistance = Integer.MAX_VALUE;
		
		for(int i = 0; i < input.getTrucks().length; i++){
			Truck truck = input.getTrucks()[i];
			for(int j = 0; j < input.getMoocs().length; j++){
				Mooc mooc = input.getMoocs()[j];
				ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
				int startTime = combo.startTime;
				String startLocationCode = combo.lastLocationCode;
				int arrivalTimePort = startTime + getTravelTime(startLocationCode, locationCodePort);
				if(arrivalTimePort > latePickupPort) continue;
				double distance = combo.extraDistance + getDistance(startLocationCode, locationCodePort);
				if(distance < minDistance){
					minDistance = distance;
					sel_combo = combo;
				}
			}
		}
		if(sel_combo == null){
			System.out.println(name() + "::createDirectRoute4ImportRequest, cannot find any combo");
			return null;
		}
		System.out.println(name() + "::createDirectRoute4ImportRequest, FOUND combo, truck = " + 
		sel_combo.truck.getCode() + ", mooc = " + sel_combo.mooc.getCode() +  
				", lastTime = " + DateTimeUtils.unixTimeStamp2DateTime(sel_combo.startTime));
		
		if(sel_combo.routeElement == null){
			TruckRoute tr = createDirectRouteForImportRequest(r, sel_combo.truck, sel_combo.mooc);
			System.out.println(name() + "::createDirectRoute4ImportRequest, create route from all depots, truck = " + sel_combo.truck.getCode()
					+ ", mooc = " + sel_combo.mooc.getCode());
			if(mTruck2Route.get(sel_combo.truck) == null)
				return tr;
			TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			old_tr.concat(tr);
			System.out.println(name() + "::createDirectRoute4ImportRequest, concat truck-route, length = " + old_tr.getNodes().length);
			
			return old_tr;
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)
				|| sel_combo.routeElement.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)){
			Truck truck = sel_combo.truck;
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
		
			int departureTime = (int)DateTimeUtils.dateTime2Int(e.getDepartureTime());
			
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			
			RouteElement e2 = new RouteElement();
			L.add(e2);
			// arrive at the depot container
			e2.deriveFrom(e);
			e2.setPort(port);
			e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
			e2.setImportRequest(r);
			e2.setContainer(mCode2Container.get(r.getContainerCode()));
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = arrivalTime;
			int duration = r.getLoadDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e2, arrivalTime);
			mPoint2DepartureTime.put(e2, departureTime);

			RouteElement e3 = new RouteElement();
			L.add(e3);
			e3.deriveFrom(e2);
			e3.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e3.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e2, e3);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime + r.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e4.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e4.setImportRequest(null);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(e4);
			e5.setDepotContainer(mCode2DepotContainer.get(r
					.getDepotContainerCode()));
			e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e5.setContainer(null);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e5.getDepotContainer().getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
			mContainer2LastTime.put(e5.getContainer(),departureTime);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc.get(e6.getMooc().getDepotMoocCode()));
			e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e6.setMooc(e6.getMooc());
			travelTime = getTravelTime(e5, e6);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e6.getDepotMooc().getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);
			mMooc2LastDepot.put(e6.getMooc(), e6.getDepotMooc());
			mMooc2LastTime.put(e6.getMooc(), departureTime);

			RouteElement e7 = new RouteElement();
			L.add(e7);
			e7.deriveFrom(e6);
			e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e7.setAction(ActionEnum.REST_AT_DEPOT);
			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			tr.removeNodesAfter(e);
			tr.addNodes(L);
			
			return tr;
		}
		return null;
	}
	public TruckRoute createDirectRoute4ExportRequest(ExportContainerRequest r){
		String warehouseCode = r.getWareHouseCode();
		logln(name() + "::createDirectRoute4ExportRequest, warehouseCode = " + warehouseCode);
		
		Warehouse warehouse = mCode2Warehouse.get(warehouseCode);
		
		String warehouseLocationCode = warehouse.getLocationCode();
		String portCode = r.getPortCode();
		String portLocationCode = mCode2Port.get(portCode).getLocationCode();
		int earlyPickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getEarlyDateTimeLoadAtWarehouse());
		int latePickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getLateDateTimeLoadAtWarehouse());
		
		ComboContainerMoocTruck sel_combo = null;
		double minDistance = Integer.MAX_VALUE;
				
		for(int i = 0; i < input.getTrucks().length; i++){
			Truck truck = input.getTrucks()[i];
			for(int j = 0; j < input.getMoocs().length; j++){
				Mooc mooc = input.getMoocs()[j];
				for(int k = 0; k < input.getContainers().length; k++){
					Container container = input.getContainers()[k];
					//String depotContainerCode = container.getDepotContainerCode();
					//String shipCompanyCode = mDepotContainerCode2ShipCompanyCode.get(depotContainerCode);
					//System.out.println(name() + "::createDirectRoute4ExportRequest, depotContainer = " + depotContainerCode + ", shipCompanyCode = " + shipCompanyCode + 
					//		", r.getShipCompanyCode = " + r.getShipCompanyCode());
					HashSet<Container> C = mShipCompanyCode2Containers.get(r.getShipCompanyCode());
					
					if(!C.contains(container)) continue;
					//if(!r.getShipCompanyCode().equals(shipCompanyCode)) continue;
					
					ComboContainerMoocTruck combo = findLastAvailable(truck, mooc, container);
					if(combo == null) continue;
					
					int startTime = combo.startTime;
					String startLocationCode = combo.lastLocationCode;
					//System.out.println(name() + "::createDirectRoute4ExportRequest, lastLocationCode = " + combo.lastLocationCode);
					
					int arrivalTimeWarehouse = startTime + getTravelTime(startLocationCode, warehouseLocationCode);
					
					if(combo.truck.getCode().equals("Truck0001") && combo.mooc.getCode().equals("Mooc0002") &&
							combo.container.getCode().equals("Container002"))
						System.out.println(name() + "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = " + 
					DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeWarehouse) + 
							", latePickupWarehouse = " + DateTimeUtils.unixTimeStamp2DateTime(latePickupWarehouse));
					
					if(arrivalTimeWarehouse > latePickupWarehouse) continue;
					
					double distance = combo.extraDistance + getDistance(combo.lastLocationCode, warehouseLocationCode);
					//System.out.println(name() + "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = " + arrivalTimeWarehouse + 
					//		", latePickupWarehouse = " + latePickupWarehouse + ", distance = " + distance);
					if(distance < minDistance){
						minDistance = distance;
						sel_combo = combo;
					}
				}
			}
		}
		if(sel_combo == null){
			logln(name() + "::createDirectRoute4ExportRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if(sel_combo.routeElement == null){
			TruckRoute tr = createDirectRouteForExportRequest(r, sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out.println(name() + "::createDirectRoute4ExportRequest, create route from all depots, truck = " + sel_combo.truck.getCode()
					+ ", mooc = " + sel_combo.mooc.getCode() + ", container = " + sel_combo.container.getCode());
			if(mTruck2Route.get(sel_combo.truck) == null)
				return tr;
			TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			old_tr.concat(tr);
			
			return old_tr;
			
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)){
			System.out.println(name() + "::createDirectRoute4ExportRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");
			
			Truck truck = sel_combo.truck;
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = (int)DateTimeUtils.dateTime2Int(e.getDepartureTime());
			
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			
			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container).getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e2, arrivalTime);
			mPoint2DepartureTime.put(e2, departureTime);

			RouteElement e3 = new RouteElement();
			L.add(e3);
			e3.deriveFrom(e2);
			e3.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setExportRequest(r);

			travelTime = getTravelTime(e2, e3);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r
							.getEarlyDateTimeLoadAtWarehouse()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(e4);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = r.getUnloadDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			// update last depot container, set to not-available
			mContainer2LastDepot.put(container, null);
			mContainer2LastTime.put(container, Integer.MAX_VALUE);

			
			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e6.setMooc(null);
			travelTime = getTravelTime(e5, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e6.getDepotMooc().getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);
			// update last depot and lastTime of mooc
			mMooc2LastDepot.put(sel_combo.mooc, e6.getDepotMooc());
			mMooc2LastTime.put(sel_combo.mooc, departureTime);
			
			RouteElement e7 = new RouteElement();
			L.add(e7);
			e7.deriveFrom(e6);
			e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e7.setAction(ActionEnum.REST_AT_DEPOT);

			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);
			// update last depot and last time of truck
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			tr.removeNodesAfter(e);
			tr.addNodes(L);
			
			return tr;
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)){
			Truck truck = sel_combo.truck;
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = sel_combo.startTime;
			
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			
			RouteElement e3 = new RouteElement();
			L.add(e3);
			e3.deriveFrom(e);
			e3.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setExportRequest(r);

			int travelTime = getTravelTime(e, e3);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r
							.getEarlyDateTimeLoadAtWarehouse()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			int duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(e4);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = r.getUnloadDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			// update last depot container, set to not-available
			mContainer2LastDepot.put(container, null);
			mContainer2LastTime.put(container, Integer.MAX_VALUE);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e6.setMooc(null);
			travelTime = getTravelTime(e5, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e6.getDepotMooc().getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);
			// update last depot and lastTime of mooc
			mMooc2LastDepot.put(sel_combo.mooc, e6.getDepotMooc());
			mMooc2LastTime.put(sel_combo.mooc, departureTime);
						 
						
			RouteElement e7 = new RouteElement();
			L.add(e7);
			e7.deriveFrom(e6);
			e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e7.setAction(ActionEnum.REST_AT_DEPOT);

			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);
			// update last depot and last time of truck
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			tr.removeNodesAfter(e);
			tr.addNodes(L);
			
			return tr;
			
		}
		return null;
	}
}
