package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import routingdelivery.model.DistanceElement;
import routingdelivery.model.RoutingElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerTruckMoocRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
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

	public TruckRoute createDirectRouteForExportRequest(
			ExportContainerRequest req) {
		TruckRoute r = new TruckRoute();
		DepotContainer depotContainer = mCode2DepotContainer.get(req
				.getDepotCode());
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
		Truck sel_truck = input.getTrucks()[sel_truck_index];
		Mooc sel_mooc = input.getMoocs()[sel_mooc_index];
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depar from the depot of the truck
		int startTime = getLastDepartureTime(sel_truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		mPoint2DepartureTime.put(e[0], startTime);

		// arrive at the depot mooc, take a mooc
		e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);

		// arrive at the depot container
		e[2].setDepotContainer(depotContainer);
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		travelTime = getTravelTime(e[1], e[2]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[2].getDepotContainer().getPickupContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[2], arrivalTime);
		mPoint2DepartureTime.put(e[2], departureTime);

		e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
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

		e[4].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime, finishedServiceTime);
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);

		e[5].setPort(mCode2Port.get(req.getPortCode()));
		e[5].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = req.getUnloadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);

		e[6].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);

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

		return r;
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

	public void propagateArrivalDepartureTime(TruckRoute r) {
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
		
		TruckRoute[] TR = new TruckRoute[lst_truckRoutes.size()];
		for(int i = 0; i < TR.length; i++)
			TR[i] = lst_truckRoutes.get(i);
		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR, "OK");
		return sol;
	}
}
