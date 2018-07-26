package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import algorithms.matching.WeightedMaxMatching;
import routingdelivery.model.DistanceElement;
import routingdelivery.model.RoutingElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerTruckMoocRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerTruckMoocRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoveOperator;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapEmptyContainerMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseTransportRequest;
import utils.DateTimeUtils;

public class ContainerTruckMoocSolver {
	public static final String ROOT_DIR = "C:/tmp/";

	public ContainerTruckMoocInput input;
	public String[] locationCodes;
	public HashMap<String, Integer> mLocationCode2Index;
	public double[][] distance;// distance[i][j] is the distance from location
								// index i to location index j
	public double[][] travelTime;// travelTime[i][j] is the travel time from
									// location index i to location index j
	public HashMap<String, Truck> mCode2Truck;
	public HashMap<String, Mooc> mCode2Mooc;
	public HashMap<String, DepotContainer> mCode2DepotContainer;
	public HashMap<String, DepotTruck> mCode2DepotTruck;
	public HashMap<String, DepotMooc> mCode2DepotMooc;
	public HashMap<String, Warehouse> mCode2Warehouse;
	public HashMap<String, Port> mCode2Port;

	public HashMap<RouteElement, Integer> mPoint2ArrivalTime;
	public HashMap<RouteElement, Integer> mPoint2DepartureTime;

	// additional data structures
	public HashMap<Container, Truck> mContainer2Truck;
	public HashMap<Mooc, Truck> mMooc2Truck;
	
	
	public PrintWriter log = null;

	public void initLog() {
		try {
			log = new PrintWriter(ROOT_DIR + "/log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
			log = null;
		}
	}

	public void finalizeLog() {
		if (log != null)
			log.close();
		else {

		}
	}

	public void log(String s) {
		if (log != null)
			log.println(s);
	}

	public String mapData() {
		String ret = "OK";
		HashSet<String> s_locationCode = new HashSet<String>();
		for (int i = 0; i < input.getDistance().length; i++) {
			DistanceElement e = input.getDistance()[i];
			String src = e.getSrcCode();
			String dest = e.getDestCode();
			s_locationCode.add(src);
			s_locationCode.add(dest);
		}
		locationCodes = new String[s_locationCode.size()];
		mLocationCode2Index = new HashMap<String, Integer>();
		int idx = -1;
		for (String lc : s_locationCode) {
			idx++;
			locationCodes[idx] = lc;
			mLocationCode2Index.put(lc, idx);
		}
		distance = new double[s_locationCode.size()][s_locationCode.size()];
		travelTime = new double[s_locationCode.size()][s_locationCode.size()];

		for (int i = 0; i < input.getDistance().length; i++) {
			DistanceElement e = input.getDistance()[i];
			String src = e.getSrcCode();
			String dest = e.getDestCode();
			double d = e.getDistance();
			int is = mLocationCode2Index.get(src);
			int id = mLocationCode2Index.get(dest);
			distance[is][id] = d;

			DistanceElement et = input.getTravelTime()[i];
			src = et.getSrcCode();
			dest = et.getDestCode();
			is = mLocationCode2Index.get(src);
			id = mLocationCode2Index.get(dest);
			travelTime[is][id] = et.getDistance();
		}
		mCode2DepotContainer = new HashMap<String, DepotContainer>();
		for(int i = 0; i < input.getDepotContainers().length; i++){
			mCode2DepotContainer.put(input.getDepotContainers()[i].getCode(), input.getDepotContainers()[i]);
		}
		
		mCode2DepotMooc = new HashMap<String, DepotMooc>();
		for(int i = 0; i < input.getDepotMoocs().length; i++){
			mCode2DepotMooc.put(input.getDepotMoocs()[i].getCode(), input.getDepotMoocs()[i]);
		}
		mCode2DepotTruck = new HashMap<String, DepotTruck>();
		for(int i = 0; i < input.getDepotTrucks().length; i++){
			mCode2DepotTruck.put(input.getDepotTrucks()[i].getCode(), input.getDepotTrucks()[i]);
		}
		mCode2Warehouse = new HashMap<String, Warehouse>();
		for(int i = 0; i < input.getWarehouses().length; i++){
			mCode2Warehouse.put(input.getWarehouses()[i].getCode(),input.getWarehouses()[i]);
		}
		mCode2Mooc = new HashMap<String, Mooc>();
		for(int i = 0; i < input.getMoocs().length; i++){
			mCode2Mooc.put(input.getMoocs()[i].getCode(), input.getMoocs()[i]);
		}
		mCode2Truck = new HashMap<String, Truck>();
		for(int i = 0; i < input.getTrucks().length; i++){
			mCode2Truck.put(input.getTrucks()[i].getCode(), input.getTrucks()[i]);
		}
		mCode2Port = new HashMap<String, Port>();
		for(int i = 0; i < input.getPorts().length; i++){
			mCode2Port.put(input.getPorts()[i].getCode(), input.getPorts()[i]);
		}
		mPoint2ArrivalTime = new HashMap<RouteElement, Integer>();
		mPoint2DepartureTime = new HashMap<RouteElement, Integer>();
		return ret;
	}
	public double getDistance(RouteElement e1, RouteElement e2){
		return getDistance(e1.getLocationCode(),e2.getLocationCode());
	}
	public double getDistance(String src, String dest) {
		int is = mLocationCode2Index.get(src);
		int id = mLocationCode2Index.get(dest);
		return distance[is][id];
	}

	public int getTravelTime(String src, String dest) {
		int is = mLocationCode2Index.get(src);
		int id = mLocationCode2Index.get(dest);
		return (int) travelTime[is][id];
	}

	public boolean fitMoocContainer(Mooc m, Container c) {
		return m.getWeight() >= c.getWeight();
	}

	public String getCurrentLocationOfTruck(Truck t) {
		DepotTruck depot = mCode2DepotTruck.get(t.getDepotTruckCode());
		return depot.getLocationCode();
	}

	public static int MIN(int a, int b) {
		return a < b ? a : b;
	}

	public static int MAX(int a, int b) {
		return a > b ? a : b;
	}

	public ComboContainerMoocTruck findBestFitContainerMoocTruck4ExportRequest(ExportContainerRequest req){
		//find and return available/feasible combo of truck-mooc-container for serving the request minimizing distance
				
		return null;
	}
	public ComboContainerMoocTruck findBestFitMoocTruck4ImportRequest(ImportContainerRequest req){
		return null;
	}
	public ComboContainerMoocTruck findBestFitContainerMoocTruck4WarehouseRequest(WarehouseContainerTransportRequest req){
		
		return null;
	}
	public ComboContainerMoocTruck findBestFitMoocTruckFor2ImportRequest(ImportContainerRequest req1, ImportContainerRequest req1){
		// req1 and req2 is of type 20, source is the same port and two destination are closed together
		return null;
	}
	
	public Container selectContainer(String containerCategory, String depotContainerCode){
		for(int i = 0; i < input.getContainers().length; i++){
			Container c = input.getContainers()[i];
			if(c.getCategoryCode().equals(containerCategory) && c.getDepotContainerCode().equals(depotContainerCode))
				return c;
		}
		return null;
	}
	public TruckRoute createDirectRouteForExportRequest(
			ExportContainerRequest req) {
		TruckRoute r = new TruckRoute();
		DepotContainer depotContainer = mCode2DepotContainer.get(req
				.getDepotContainerCode());
		System.out.println(name() + "::createDirectRouteForExportRequest, depotContainerCode = " + req.getDepotContainerCode() + 
				", depotContainer = " + depotContainer);
		
		double minDistance = Integer.MAX_VALUE;
		int sel_truck_index = -1;
		int sel_mooc_index = -1;
		for (int i = 0; i < input.getTrucks().length; i++) {
			String lt = getCurrentLocationOfTruck(input.getTrucks()[i]);
			for (int j = 0; j < input.getMoocs().length; j++) {
				DepotMooc depotMooc = mCode2DepotMooc.get(input.getMoocs()[j]
						.getDepotMoocCode());
				String lm = depotMooc.getLocationCode();
				double d = getDistance(lt, lm)
						+ getDistance(lm, depotContainer.getLocationCode());
				if (d < minDistance) {
					minDistance = d;
					sel_truck_index = i;
					sel_mooc_index = j;
				}
			}
		}
		Container sel_container = selectContainer(req.getContainerCategory(),req.getDepotContainerCode());
		
				
		Truck sel_truck = input.getTrucks()[sel_truck_index];
		Mooc sel_mooc = input.getMoocs()[sel_mooc_index];
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depar from the depot of the truck
		int startTime = getLastDepartureTime(sel_truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(sel_truck);
		mPoint2DepartureTime.put(e[0], startTime);
		
		// arrive at the depot mooc, take a mooc
		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(sel_mooc);
		
		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);
		
		// arrive at the depot container
		e[2].deriveFrom(e[1]);
		e[2].setDepotContainer(depotContainer);
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		e[2].setContainer(sel_container);
		travelTime = getTravelTime(e[1], e[2]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[2].getDepotContainer().getPickupContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[2], arrivalTime);
		mPoint2DepartureTime.put(e[2], departureTime);
		
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e[3].setExportRequest(req);
		
		travelTime = getTravelTime(e[2], e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimeLoadAtWarehouse()));
		int finishedServiceTime = startServiceTime + req.getLoadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);
		
		e[4].deriveFrom(e[3]);
		e[4].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime, finishedServiceTime);
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);

		e[5].deriveFrom(e[4]);
		e[5].setPort(mCode2Port.get(req.getPortCode()));
		e[5].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e[5].setContainer(null);
		e[5].setExportRequest(null);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = req.getUnloadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);

		e[6].deriveFrom(e[5]);
		e[6].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(null);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);

		
		e[7].deriveFrom(e[6]);
		e[7].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[7].setAction(ActionEnum.REST_AT_DEPOT);
		
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);

		propagate(r);
		
		return r;
	}
	public TruckRoute createDirectRouteForWarehouseWarehouseRequest(WarehouseContainerTransportRequest req){
		
		Warehouse pickupWarehouse = mCode2Warehouse.get(req.getFromWarehouseCode());
		Warehouse deliveryWarehouse = mCode2Warehouse.get(req.getToWarehouseCode());
		String pickupLocationCode = pickupWarehouse.getLocationCode();
		String deliveryLocationCode = deliveryWarehouse.getLocationCode();
				
		Truck sel_truck = null;
		Container sel_container = null;
		Mooc sel_mooc = null;
		double minDistance = Integer.MAX_VALUE;
		// select nearest truck, mooc, container
		for(int i = 0; i < input.getTrucks().length; i++){
			Truck truck = input.getTrucks()[i];
			DepotTruck depotTruck = mCode2DepotTruck.get(truck.getDepotTruckCode());
			String truckLocationCode = depotTruck.getLocationCode();
			
			for(int j = 0; j < input.getMoocs().length; j++){
				Mooc mooc = input.getMoocs()[j];
				DepotMooc depotMooc = mCode2DepotMooc.get(mooc.getDepotMoocCode());
				String moocLocationCode = depotMooc.getLocationCode();
				for(int k = 0; k < input.getContainers().length; k++){
					Container container = input.getContainers()[k];
					DepotContainer depotContainer = mCode2DepotContainer.get(container.getDepotContainerCode());
					String containerLocationCode = depotContainer.getLocationCode();
					
					double d = getDistance(truckLocationCode, moocLocationCode)
							+ getDistance(moocLocationCode, containerLocationCode)
							+ getDistance(containerLocationCode, pickupLocationCode);
					if(d < minDistance){
						minDistance = d;
						sel_truck = truck;
						sel_mooc = mooc;
						sel_container = container;
					}
				}
			}
		}
		
		RouteElement[] e= new RouteElement[10];
		for(int i = 0; i < e.length; i++) e[i] = new RouteElement();
		
		int startTime = getLastDepartureTime(sel_truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(sel_truck);
		mPoint2DepartureTime.put(e[0], startTime);
		

		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(sel_mooc);
		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);
		
		
		e[2].deriveFrom(e[1]);
		e[2].setDepotContainer(mCode2DepotContainer.get(sel_container.getDepotContainerCode()));
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		e[2].setContainer(sel_container);
		travelTime = getTravelTime(e[1], e[2]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[2].getDepotContainer().getPickupContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[2], arrivalTime);
		mPoint2DepartureTime.put(e[2], departureTime);
		
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(pickupWarehouse);
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e[3].setWarehouseRequest(req);
		travelTime = getTravelTime(e[2], e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		int finishedServiceTime = startServiceTime + req.getLoadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);
		
		e[4].deriveFrom(e[3]);
		e[4].setWarehouse(pickupWarehouse);
		e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = finishedServiceTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);
		
		
		e[5].deriveFrom(e[4]);
		e[5].setWarehouse(deliveryWarehouse);
		e[5].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		finishedServiceTime = startServiceTime + req.getUnloadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		
		e[6].deriveFrom(e[5]);
		e[6].setWarehouse(deliveryWarehouse);
		e[6].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e[6].setWarehouseRequest(null);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = finishedServiceTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);
		
		
		e[7].deriveFrom(e[6]);
		e[7].setDepotContainer(mCode2DepotContainer.get(sel_container.getDepotContainerCode()));
		e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[7].setContainer(null);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotContainer.get(sel_container.getDepotContainerCode()).getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);
		
		e[8].deriveFrom(e[7]);
		e[8].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[8].setMooc(sel_mooc);
		travelTime = getTravelTime(e[7], e[8]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()).getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[8], arrivalTime);
		mPoint2DepartureTime.put(e[8], departureTime);
		
		e[9].deriveFrom(e[8]);
		e[9].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[9].setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e[8], e[9]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[9], arrivalTime);
		mPoint2DepartureTime.put(e[9], departureTime);
		
		TruckRoute r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);

		propagate(r);
		
		return r;
	}
	public TruckRoute createDirectRouteForImportRequest(
			ImportContainerRequest req) {
		TruckRoute r = new TruckRoute();
		Port port = mCode2Port.get(req.getPortCode());
		
		double minDistance = Integer.MAX_VALUE;
		int sel_truck_index = -1;
		int sel_mooc_index = -1;
		for (int i = 0; i < input.getTrucks().length; i++) {
			String lt = getCurrentLocationOfTruck(input.getTrucks()[i]);
			for (int j = 0; j < input.getMoocs().length; j++) {
				DepotMooc depotMooc = mCode2DepotMooc.get(input.getMoocs()[j]
						.getDepotMoocCode());
				String lm = depotMooc.getLocationCode();
				double d = getDistance(lt, lm)
						+ getDistance(lm, port.getLocationCode());
				if (d < minDistance) {
					minDistance = d;
					sel_truck_index = i;
					sel_mooc_index = j;
				}
			}
		}
		Truck sel_truck = input.getTrucks()[sel_truck_index];
		Mooc sel_mooc = input.getMoocs()[sel_mooc_index];
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depart from the depot of the truck
		int startTime = getLastDepartureTime(sel_truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(sel_truck);
		mPoint2DepartureTime.put(e[0], startTime);
		
		// arrive at the depot mooc, take a mooc
		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(sel_mooc);
		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);
		
		
		// arrive at the depot container
		e[2].deriveFrom(e[1]);
		e[2].setPort(port);
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		e[2].setImportRequest(req);
		travelTime = getTravelTime(e[1], e[2]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = req.getLoadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[2], arrivalTime);
		mPoint2DepartureTime.put(e[2], departureTime);
		
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[3].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e[2], e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyDateTimeUnloadAtWarehouse()));
		int finishedServiceTime = startServiceTime + req.getUnloadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);
		
		e[4].deriveFrom(e[3]);
		e[4].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[4].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e[4].setImportRequest(null);
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime, finishedServiceTime);
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);

		e[5].deriveFrom(e[4]);
		e[5].setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e[5].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[5].setContainer(null);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[5].getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);

		e[6].deriveFrom(e[5]);
		e[6].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(sel_mooc);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);

		e[7].deriveFrom(e[6]);
		e[7].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[7].setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);

		propagate(r);
		
		return r;
	}

	public SwapEmptyContainerMooc evaluateSwap(TruckRoute tr1, TruckRoute tr2){
		// tr1 deposits loaded container at warehouse 1
		// tr2 carries loaded container from warehouse 2
		// try to merge these routes into one: after depositing container at the warehouse, 
		// truck tr1, mooc, empty container will go warehouse 2 to load goods and carry
		// remove tr2
		// return delta (new disance - old distance)
		RouteElement[] e1 = tr1.getNodes();
		RouteElement[] e2 = tr2.getNodes();
		int sel_i1 = -1;
		for(int i = 0; i < e1.length; i++){
			if(e1[i].getAction().equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)){
				sel_i1 = i; break;
			}
		}
		if(sel_i1 < 0) return null;//Integer.MAX_VALUE;
		
		int sel_i21 = -1;
		for(int i = 0; i < e2.length; i++){
			if(e2[i].getAction().equals(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE)){
				sel_i21 = i; break;
			}
		}
		if(sel_i21 < 0) return null;//Integer.MAX_VALUE;
		
		int sel_i22 = -1;
		for(int i = sel_i21; i < e2.length; i++){
			if(e2[i].getAction().equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| e2[i].getAction().equals(ActionEnum.LINK_EMPTY_MOOC_AT_PORT)){
				sel_i22 = i; break;
			}
		}
		if(sel_i22 < 0) return null;//Integer.MAX_VALUE;
		
		double newDis = e1[sel_i1].getDistance()
				+ getDistance(e1[sel_i1], e2[sel_i21])
				+ tr2.getDistanceSubRoute(sel_i21, sel_i22)
				+ getDistance(e2[sel_i22], e1[sel_i1+1])
				+ tr1.getDistanceFromPositionToEnd(sel_i1+1);
		double eval = newDis - tr1.getDistance() - tr2.getDistance();
		SwapEmptyContainerMooc m = new SwapEmptyContainerMooc(tr1, tr2, sel_i1, sel_i21, sel_i22,eval);
		
		return m;
	}
	
	public TruckRoute performSwap(SwapEmptyContainerMooc move){
		// tr1 deposits loaded container at warehouse 1
		// tr2 carries loaded container from warehouse 2
		// try to merge these routes into one: after depositing container at the warehouse, 
		// truck tr1, mooc, empty container will go warehouse 2 to load goods and carry
		// remove tr2
		// return delta (new disance - old distance)
		TruckRoute tr1 = move.tr1;
		TruckRoute tr2 = move.tr2;
		
		RouteElement[] e1 = tr1.getNodes();
		RouteElement[] e2 = tr2.getNodes();
		int sel_i1 = move.i1;
		int sel_i21 = move.i21;
		int sel_i22 = move.i22;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		for(int i = 0; i <= sel_i1; i++)
			L.add(e1[i]);
		for(int i = sel_i21; i <= sel_i22; i++)
			L.add(e2[i]);
		for(int i = sel_i1+1; i < e1.length; i++)
			L.add(e1[i]);
		/*
		double newDis = e1[sel_i1].getDistance()
				+ getDistance(e1[sel_i1], e2[sel_i21])
				+ tr2.getDistanceSubRoute(sel_i21, sel_i22)
				+ getDistance(e2[sel_i22], e1[sel_i1+1])
				+ tr1.getDistanceFromPositionToEnd(sel_i1+1);
		double eval = newDis - tr1.getDistance() - tr2.getDistance();
		SwapEmptyContainerMooc m = new SwapEmptyContainerMooc(tr1, tr2, sel_i1, sel_i21, sel_i22,eval);
		*/
		RouteElement[] e= new RouteElement[L.size()];
		for(int i = 0; i < L.size(); i++)
			e[i] = L.get(i);
		
		TruckRoute r = new TruckRoute(tr1.getTruck(),e);
		return r;
	}

	public void improveSwap(ArrayList<TruckRoute> TR){
		int n = TR.size();
		int[] X = new int[n];
		int[] Y = new int[n];
		for(int i = 0; i < n; i++){
			X[i] = i;
			Y[i] = i;
		}
		ArrayList<Integer> l_edgeX = new ArrayList<Integer>();
		ArrayList<Integer> l_edgeY = new ArrayList<Integer>();
		ArrayList<Double> l_w = new ArrayList<Double>();
		MoveOperator[][] moves = new MoveOperator[n][n];
		for(int i = 0; i < TR.size(); i++){
			for(int j = 0; j < TR.size(); j++)if(i != j){
				SwapEmptyContainerMooc m = evaluateSwap(TR.get(i), TR.get(j));
				double eval = Integer.MAX_VALUE;
				if(m != null){
					eval = m.eval;
					moves[i][j] = m;
				}
				if(eval < 0){
					System.out.println(name() + "::improveSwap, DETECT Swap(" + i + "," + j + "), eval = " + eval);
					l_edgeX.add(i);
					l_edgeY.add(j);
					l_w.add(eval);
				}else{
					System.out.println(name() + "::improveSwap, Swap(" + i + "," + j + "), eval = " + eval);
				}
			}
		}
		int[] edgeX = new int[l_edgeX.size()];
		int[] edgeY = new int[l_edgeY.size()];
		double[] w = new double[l_edgeX.size()];
		for(int i = 0; i < l_edgeX.size(); i++){
			edgeX[i] = l_edgeX.get(i);
			edgeY[i] = l_edgeY.get(i);
			w[i] = -l_w.get(i);
		}
		
		WeightedMaxMatching matching = new WeightedMaxMatching();
		matching.solve(X, Y, edgeX, edgeY, w);
		int[] solX = matching.getSolutionX();
		int[] solY = matching.getSolutionY();
		if (solX == null || solX.length == 0)
			return;

		for(int i = 0; i < solX.length; i++){
			System.out.println(name() + "::improveSwap, match " + solX[i] + "-" + solY[i]);
		}
		
		// perform swap route-truck
		ArrayList<TruckRoute> AL = new ArrayList<TruckRoute>();
		for(int k = 0; k < solX.length; k++){
			int i = solX[k];
			int j = solY[k];
			SwapEmptyContainerMooc m = (SwapEmptyContainerMooc)moves[i][j];
			TruckRoute r = performSwap(m);
			AL.add(r);
			int idx = TR.indexOf(m.tr1);
			TR.remove(idx);
			idx = TR.indexOf(m.tr2);
			TR.remove(idx);
		}
		for(TruckRoute tr: AL){
			TR.add(tr);
		}
	}
	public void propagate(TruckRoute tr){
		propagateArrivalDepartureTimeString(tr);
		propagateDistance(tr);
	}
	public void propagateDistance(TruckRoute tr){
		RouteElement[] e = tr.getNodes();
		e[0].setDistance(0);
		for(int i = 1; i < e.length; i++){
			e[i].setDistance(e[i-1].getDistance() + getDistance(e[i-1], e[i]));
		}
	}
	public void propagateArrivalDepartureTimeString(TruckRoute tr){
		RouteElement[] e = tr.getNodes();
		for(int i = 0; i < e.length; i++){
			if(mPoint2ArrivalTime.get(e[i]) != null){
				e[i].setArrivalTime(DateTimeUtils.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(e[i])));
			}
			if(mPoint2DepartureTime.get(e[i]) != null){
				e[i].setDepartureTime(DateTimeUtils.unixTimeStamp2DateTime(mPoint2DepartureTime.get(e[i])));
			}
			
		}
	}
	public int getLastDepartureTime(Truck t) {
		long dept = DateTimeUtils.dateTime2Int(t.getStartWorkingTime());
		return (int) dept;
	}

	public String name() {
		return "ContainerTruckMoocSolver";
	}

	public int getTravelTime(RouteElement e1, RouteElement e2) {
		String lc1 = e1.getLocationCode();
		String lc2 = e2.getLocationCode();
		return getTravelTime(lc1, lc2);
	}

	

	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		this.input = input;
		mapData();
		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();
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
		
		for(int i = 0; i < input.getWarehouseRequests().length; i++){
			WarehouseTransportRequest R = input.getWarehouseRequests()[i];
			for(int j = 0; j < R.getWarehouseContainerTransportRequests().length; j++){
				WarehouseContainerTransportRequest r = R.getWarehouseContainerTransportRequests()[j];
				TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r);
				lst_truckRoutes.add(tr);
			}
		}
		
		improveSwap(lst_truckRoutes);
		
		
		TruckRoute[] TR = new TruckRoute[lst_truckRoutes.size()];
		for(int i = 0; i < TR.length; i++)
			TR[i] = lst_truckRoutes.get(i);
		
		double totalDistance = 0;
		for(int i = 0; i < TR.length; i++){
			TruckRoute tr = TR[i];
			RouteElement[] e = tr.getNodes();
			if(e != null && e.length > 0){
				totalDistance = totalDistance + e[e.length-1].getDistance();
			}
		}
		
		
		StatisticInformation infos = new StatisticInformation(totalDistance, TR.length);
		
		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR, infos, "OK");
		
		
		
		
		return sol;
	}
}
