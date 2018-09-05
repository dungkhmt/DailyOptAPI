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
import routingdelivery.smartlog.containertruckmoocassigment.model.ShipCompany;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapEmptyContainerMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
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
	public HashMap<String, Container> mCode2Container;
	public HashMap<String, DepotContainer> mCode2DepotContainer;
	public HashMap<String, DepotTruck> mCode2DepotTruck;
	public HashMap<String, DepotMooc> mCode2DepotMooc;
	public HashMap<String, Warehouse> mCode2Warehouse;
	public HashMap<String, Port> mCode2Port;
	public HashMap<String, HashSet<String>> mDepotContainerCode2ShipCompanyCode;

	public HashMap<String, HashSet<Container>> mShipCompanyCode2Containers;

	public HashMap<RouteElement, Integer> mPoint2ArrivalTime;
	public HashMap<RouteElement, Integer> mPoint2DepartureTime;

	// additional data structures
	public HashMap<Container, Truck> mContainer2Truck;
	public HashMap<Mooc, Truck> mMooc2Truck;
	public HashMap<Truck, TruckRoute> mTruck2Route;
	public HashMap<Truck, TruckItinerary> mTruck2Itinerary;

	public HashMap<Truck, DepotTruck> mTruck2LastDepot;// map each truck to the
														// last depot (after
														// service plan)
	public HashMap<Truck, DepotTruck> bku_mTruck2LastDepot;

	public HashMap<Truck, Integer> mTruck2LastTime;// the time where the truck
													// is available at last
													// depot
	public HashMap<Truck, Integer> bku_mTruck2LastTime;

	public HashMap<Mooc, DepotMooc> mMooc2LastDepot;// map each mooc to the last
													// depot (after service
													// plan)
	public HashMap<Mooc, DepotMooc> bku_mMooc2LastDepot;

	public HashMap<Mooc, Integer> mMooc2LastTime;// the time where mooc is
													// available at the last
													// depot
	public HashMap<Mooc, Integer> bku_mMooc2LastTime;

	public HashMap<Container, DepotContainer> mContainer2LastDepot; // map each
																	// container
																	// to the
																	// last
																	// depot
	public HashMap<Container, DepotContainer> bku_mContainer2LastDepot;

	public HashMap<Container, Integer> mContainer2LastTime;// the time where
															// container is
															// available at the
															// last depot
	public HashMap<Container, Integer> bku_mContainer2LastTime;

	public ArrayList<Container> additionalContainers;
	public PrintWriter log = null;

	public ContainerTruckMoocSolver() {
		bku_mTruck2LastDepot = new HashMap<Truck, DepotTruck>();
		bku_mTruck2LastTime = new HashMap<Truck, Integer>();
		bku_mMooc2LastDepot = new HashMap<Mooc, DepotMooc>();
		bku_mMooc2LastTime = new HashMap<Mooc, Integer>();
		bku_mContainer2LastDepot = new HashMap<Container, DepotContainer>();
		bku_mContainer2LastTime = new HashMap<Container, Integer>();

	}

	public void backup() {
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			bku_mTruck2LastDepot.put(truck, mTruck2LastDepot.get(truck));
			bku_mTruck2LastTime.put(truck, mTruck2LastTime.get(truck));
		}
		for (int i = 0; i < input.getMoocs().length; i++) {
			Mooc mooc = input.getMoocs()[i];
			bku_mMooc2LastDepot.put(mooc, mMooc2LastDepot.get(mooc));
			bku_mMooc2LastTime.put(mooc, mMooc2LastTime.get(mooc));
		}
		for (int i = 0; i < input.getContainers().length; i++) {
			Container container = input.getContainers()[i];
			bku_mContainer2LastDepot.put(container,
					mContainer2LastDepot.get(container));
			bku_mContainer2LastTime.put(container,
					mContainer2LastTime.get(container));
		}
	}

	public void restore() {
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			mTruck2LastDepot.put(truck, bku_mTruck2LastDepot.get(truck));
			mTruck2LastTime.put(truck, bku_mTruck2LastTime.get(truck));
		}
		for (int i = 0; i < input.getMoocs().length; i++) {
			Mooc mooc = input.getMoocs()[i];
			mMooc2LastDepot.put(mooc, bku_mMooc2LastDepot.get(mooc));
			mMooc2LastTime.put(mooc, bku_mMooc2LastTime.get(mooc));
		}
		for (int i = 0; i < input.getContainers().length; i++) {
			Container container = input.getContainers()[i];
			mContainer2LastDepot.put(container,
					bku_mContainer2LastDepot.get(container));
			mContainer2LastTime.put(container,
					bku_mContainer2LastTime.get(container));
		}
	}

	public TruckItinerary getItinerary(Truck truck) {
		return mTruck2Itinerary.get(truck);
	}

	public boolean processBefore(ExportContainerRequest er,
			ImportContainerRequest ir) {
		if (er == null)
			return false;
		if (ir == null)
			return true;
		int et = (int) DateTimeUtils.dateTime2Int(er
				.getLateDateTimeLoadAtWarehouse());
		int it = (int) DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort());
		return et < it;
	}

	public boolean processBefore(ImportContainerRequest ir,
			ExportContainerRequest er) {
		if (ir == null)
			return false;
		if (er == null)
			return true;
		int et = (int) DateTimeUtils.dateTime2Int(er
				.getLateDateTimeLoadAtWarehouse());
		int it = (int) DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort());
		return et > it;
	}

	public boolean processBefore(ExportContainerRequest er,
			WarehouseContainerTransportRequest wr) {
		if (er == null)
			return false;
		if (wr == null)
			return true;
		int et = (int) DateTimeUtils.dateTime2Int(er
				.getLateDateTimeLoadAtWarehouse());
		int wt = (int) DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad());
		return et < wt;
	}

	public boolean processBefore(WarehouseContainerTransportRequest wr,
			ExportContainerRequest er) {
		if (wr == null)
			return false;
		if (er == null)
			return true;
		int et = (int) DateTimeUtils.dateTime2Int(er
				.getLateDateTimeLoadAtWarehouse());
		int wt = (int) DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad());
		return et > wt;
	}

	public boolean processBefore(ImportContainerRequest ir,
			WarehouseContainerTransportRequest wr) {
		if (ir == null)
			return false;
		if (wr == null)
			return true;
		int it = (int) DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort());
		int wt = (int) DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad());
		return it < wt;
	}

	public boolean processBefore(WarehouseContainerTransportRequest wr,
			ImportContainerRequest ir) {
		if (wr == null)
			return false;
		if (ir == null)
			return true;
		int it = (int) DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort());
		int wt = (int) DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad());
		return it > wt;
	}

	public void initLog() {
		try {
			log = new PrintWriter(ROOT_DIR + "/log.txt");
		} catch (Exception ex) {
			ex.printStackTrace();
			log = null;
		}
	}

	public void finalizeLog() {
		if (log != null){
			log.close();
			log = null;
		}else {

		}
	}

	public void log(String s) {
		if (log != null)
			log.print(s);
	}

	public void logln(String s) {
		if (log != null)
			log.println(s);
	}

	public void modifyContainerCode() {
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			c.setCode("I-" + c.getCode());
		}
	}

	public void recoverContainerCode() {
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			String code = c.getCode();
			if (code.charAt(0) == 'I') {
				code = code.substring(2);
				c.setCode(code);
			}
		}
	}

	public String mapData() {
		String ret = "OK";

		// create artificial containers based on import request
		additionalContainers = new ArrayList<Container>();
		int idxCode = -1;
		for (int i = 0; i < input.getImRequests().length; i++) {
			ImportContainerTruckMoocRequest R = input.getImRequests()[i];
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ImportContainerRequest r = R.getContainerRequest()[j];
				
				//String depotContainerCode = r.getDepotContainerCode();
				String containerCategory = r.getContainerCategory();
				idxCode++;
				String code = "A-" + idxCode;
				Container c = new Container(code, (int) r.getWeight(),
						r.getContainerCategory(), null,r.getDepotContainerCode());
				additionalContainers.add(c);
				r.setContainerCode(code);
			}
		}
		Container[] L = new Container[input.getContainers().length
				+ additionalContainers.size()];
		for (int i = 0; i < input.getContainers().length; i++)
			L[i] = input.getContainers()[i];
		for (int i = 0; i < additionalContainers.size(); i++)
			L[i + input.getContainers().length] = additionalContainers.get(i);
		input.setContainers(L);

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
		for (int i = 0; i < input.getDepotContainers().length; i++) {
			mCode2DepotContainer.put(input.getDepotContainers()[i].getCode(),
					input.getDepotContainers()[i]);
		}

		mCode2DepotMooc = new HashMap<String, DepotMooc>();
		for (int i = 0; i < input.getDepotMoocs().length; i++) {
			mCode2DepotMooc.put(input.getDepotMoocs()[i].getCode(),
					input.getDepotMoocs()[i]);
		}
		mCode2DepotTruck = new HashMap<String, DepotTruck>();
		for (int i = 0; i < input.getDepotTrucks().length; i++) {
			mCode2DepotTruck.put(input.getDepotTrucks()[i].getCode(),
					input.getDepotTrucks()[i]);
		}
		mCode2Warehouse = new HashMap<String, Warehouse>();
		for (int i = 0; i < input.getWarehouses().length; i++) {
			mCode2Warehouse.put(input.getWarehouses()[i].getCode(),
					input.getWarehouses()[i]);
			logln(name() + "::mapData, warehouse put("
					+ input.getWarehouses()[i].getCode());
			// System.out.println(name() + "::mapData, warehouse put("
			// + input.getWarehouses()[i].getCode());
		}
		mCode2Mooc = new HashMap<String, Mooc>();
		for (int i = 0; i < input.getMoocs().length; i++) {
			mCode2Mooc.put(input.getMoocs()[i].getCode(), input.getMoocs()[i]);
		}
		mCode2Truck = new HashMap<String, Truck>();
		for (int i = 0; i < input.getTrucks().length; i++) {
			mCode2Truck.put(input.getTrucks()[i].getCode(),
					input.getTrucks()[i]);
		}
		mCode2Container = new HashMap<String, Container>();
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			mCode2Container.put(c.getCode(), c);
		}
		mCode2Port = new HashMap<String, Port>();
		for (int i = 0; i < input.getPorts().length; i++) {
			mCode2Port.put(input.getPorts()[i].getCode(), input.getPorts()[i]);
			System.out.println(name() + "::mapData, mCode2Port.put("
					+ input.getPorts()[i].getCode() + ")");
		}
		mPoint2ArrivalTime = new HashMap<RouteElement, Integer>();
		mPoint2DepartureTime = new HashMap<RouteElement, Integer>();

		mDepotContainerCode2ShipCompanyCode = new HashMap<String, HashSet<String>>();
		for (int i = 0; i < input.getCompanies().length; i++) {
			ShipCompany c = input.getCompanies()[i];
			for (int j = 0; j < c.getContainerDepotCodes().length; j++) {
				String depotContainerCode = c.getContainerDepotCodes()[j];
				if (mDepotContainerCode2ShipCompanyCode.get(depotContainerCode) == null)
					mDepotContainerCode2ShipCompanyCode.put(depotContainerCode,
							new HashSet<String>());
				mDepotContainerCode2ShipCompanyCode.get(depotContainerCode)
						.add(c.getCode());
			}
		}

		mShipCompanyCode2Containers = new HashMap<String, HashSet<Container>>();
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			String depotCode = c.getDepotContainerCode();
			if (mDepotContainerCode2ShipCompanyCode.get(depotCode) != null)
				for (String companyCode : mDepotContainerCode2ShipCompanyCode
						.get(depotCode)) {
					if (mShipCompanyCode2Containers.get(companyCode) == null)
						mShipCompanyCode2Containers.put(companyCode,
								new HashSet<Container>());
					mShipCompanyCode2Containers.get(companyCode).add(c);
				}

		}

		return ret;
	}

	public double getDistance(RouteElement e1, RouteElement e2) {
		return getDistance(e1.getLocationCode(), e2.getLocationCode());
	}

	public double getDistance(String src, String dest) {
		if(mLocationCode2Index.get(dest) == null){
			System.out.println(name() + "::getDistance, location dest = " + dest + " has no index ");
		}

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

	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			Mooc mooc, Container container) {
		int startTimeTruck = mTruck2LastTime.get(truck);// (int)
														// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		//String locationTruck = mCode2DepotTruck.get(truck.getDepotTruckCode())
		//		.getLocationCode();
		DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		String locationTruck = depotTruck.getLocationCode();
		
		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		String locationContainer = mContainer2LastDepot.get(container)
				.getLocationCode();
		//System.out.println(name() + "::createLastAvailableCombo, container = " + container.getCode() + ", location = " + locationContainer);
		int timeMooc = mMooc2LastTime.get(mooc);
		int timeContainer = mContainer2LastTime.get(container);
		int arrivalTimeMooc = startTimeTruck
				+ getTravelTime(locationTruck, locationMooc);
		int departureTimeMooc = MAX(arrivalTimeMooc, timeMooc)
				+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
		int arrivalTimeContainer = departureTimeMooc
				+ getTravelTime(locationMooc, locationContainer);
		int departureTimeContainer = MAX(timeContainer, arrivalTimeContainer)
				+ mContainer2LastDepot.get(container)
						.getPickupContainerDuration();

		if (truck.getCode().equals("Truck0001")
				&& mooc.getCode().equals("Mooc0002")
				&& container.getCode().equals("Container002"))
			System.out.println(name()
					+ "::createLastAvailableCombo, lastTimeTruck = "
					+ DateTimeUtils.unixTimeStamp2DateTime(mTruck2LastTime
							.get(truck))
					+ ", departureTime = "
					+ DateTimeUtils
							.unixTimeStamp2DateTime(departureTimeContainer));

		double distance = getDistance(locationTruck, locationMooc)
				+ getDistance(locationMooc, locationContainer);

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(truck, mooc,
				container, locationContainer, departureTimeContainer, null,
				null, distance);
		return ret;

	}

	public DepotMooc findDepotForReleaseMooc(Mooc mooc) {
		return mCode2DepotMooc.get(mooc.getDepotMoocCode());
	}

	public DepotTruck findDepotForReleaseTruck(Truck truck) {
		return mCode2DepotTruck.get(truck.getDepotTruckCode());
	}

	public DepotContainer findDepotForReleaseContainer(Container container) {
		return mCode2DepotContainer.get(container.getDepotContainerCode());
	}

	public TruckRoute createTangboImportWarehouse(Truck truck, Mooc mooc,
			ImportContainerRequest ir, WarehouseContainerTransportRequest wr) {
		// truck-mooc -> port(ir) -> warehouse(ir) -> from_warehouse(wr) ->
		// to_warehouse(wr)
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement e0 = new RouteElement();
		L.add(e0);
		e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
		e0.setDepotTruck(mTruck2LastDepot.get(truck));
		int departureTime = mTruck2LastTime.get(truck);
		mPoint2DepartureTime.put(e0, departureTime);

		RouteElement e1 = new RouteElement();
		L.add(e1);
		e1.deriveFrom(e0);
		e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e1.setDepotMooc(mMooc2LastDepot.get(mooc));
		int arrivalTime = departureTime
				+ getTravelTime(e0.getDepotTruck().getLocationCode(), e1
						.getDepotMooc().getLocationCode());
		int serviceTime = MAX(arrivalTime, mMooc2LastTime.get(mooc));
		int duration = e1.getDepotMooc().getPickupMoocDuration();
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e1, arrivalTime);
		mPoint2DepartureTime.put(e1, departureTime);

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(e1);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setContainer(mCode2Container.get(ir.getContainerCode()));
		Container container = e2.getContainer();
		e2.setPort(mCode2Port.get(ir.getPortCode()));
		e2.setImportRequest(ir);
		arrivalTime = departureTime
				+ getTravelTime(e1.getDepotMooc().getLocationCode(), e2
						.getPort().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimePickupAtPort()))
			return null;
		serviceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
				.getEarlyDateTimePickupAtPort()));
		duration = ir.getLoadDuration();
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(e2);
		e3.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		e3.setWarehouse(mCode2Warehouse.get(ir.getWareHouseCode()));
		arrivalTime = departureTime
				+ getTravelTime(e2.getPort().getLocationCode(), e3
						.getWarehouse().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(ir
				.getLateDateTimeUnloadAtWarehouse()))
			return null;

		serviceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
				.getEarlyDateTimeUnloadAtWarehouse()));
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		e4.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		arrivalTime = departureTime + ir.getUnloadDuration();
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e4, arrivalTime);
		mPoint2DepartureTime.put(e4, departureTime);

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(e4);
		e5.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e5.setWarehouseRequest(wr);
		e5.setWarehouse(mCode2Warehouse.get(wr.getFromWarehouseCode()));
		arrivalTime = departureTime
				+ getTravelTime(e4.getWarehouse().getLocationCode(), e5
						.getWarehouse().getLocationCode());
		if (arrivalTime > DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad()))
			return null;

		serviceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeLoad()));
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		e6.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		arrivalTime = departureTime + wr.getLoadDuration();
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e6, arrivalTime);
		mPoint2DepartureTime.put(e6, departureTime);

		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(e6);
		e7.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		e7.setWarehouse(mCode2Warehouse.get(wr.getToWarehouseCode()));
		arrivalTime = departureTime
				+ getTravelTime(e6.getWarehouse().getLocationCode(), e7
						.getWarehouse().getLocationCode());
		if (arrivalTime > DateTimeUtils
				.dateTime2Int(wr.getLateDateTimeUnload()))
			return null;

		serviceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeUnload()));
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e7, arrivalTime);
		mPoint2DepartureTime.put(e7, departureTime);

		RouteElement e8 = new RouteElement();
		L.add(e8);
		e8.deriveFrom(e7);
		e8.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		arrivalTime = departureTime + wr.getUnloadDuration();
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e8, arrivalTime);
		mPoint2DepartureTime.put(e8, departureTime);

		RouteElement e9 = new RouteElement();
		L.add(e9);
		e9.deriveFrom(e8);
		e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e9.setDepotContainer(findDepotForReleaseContainer(container));
		arrivalTime = departureTime
				+ getTravelTime(e8.getWarehouse().getLocationCode(), e9
						.getDepotContainer().getLocationCode());
		serviceTime = arrivalTime;
		duration = e9.getDepotContainer().getDeliveryContainerDuration();
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e9, arrivalTime);
		mPoint2DepartureTime.put(e9, departureTime);

		RouteElement e10 = new RouteElement();
		L.add(e10);
		e10.deriveFrom(e9);
		e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e10.setDepotMooc(findDepotForReleaseMooc(mooc));
		arrivalTime = departureTime
				+ getTravelTime(e9.getDepotContainer().getLocationCode(), e10
						.getDepotMooc().getLocationCode());
		serviceTime = arrivalTime;
		duration = e10.getDepotMooc().getDeliveryMoocDuration();
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e10, arrivalTime);
		mPoint2DepartureTime.put(e10, departureTime);

		RouteElement e11 = new RouteElement();
		L.add(e11);
		e11.deriveFrom(e10);
		e11.setAction(ActionEnum.REST_AT_DEPOT);
		e11.setDepotTruck(findDepotForReleaseTruck(truck));
		arrivalTime = departureTime
				+ getTravelTime(e10.getDepotMooc().getLocationCode(), e11
						.getDepotTruck().getLocationCode());
		serviceTime = arrivalTime;
		duration = 0;
		departureTime = serviceTime + duration;
		mPoint2ArrivalTime.put(e11, arrivalTime);
		mPoint2DepartureTime.put(e11, departureTime);

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r.setNodes(e);
		r.setTruck(truck);

		propagate(r);

		return r;

	}

	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			Mooc mooc) {
		int startTimeTruck = mTruck2LastTime.get(truck);// (int)
														// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		double distance = 0;
		//String locationTruck = mCode2DepotTruck.get(truck.getDepotTruckCode())
		//		.getLocationCode();
		DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		String locationTruck = depotTruck.getLocationCode(); 
		
		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		int timeMooc = mMooc2LastTime.get(mooc);
		int arrivalTimeMooc = startTimeTruck
				+ getTravelTime(locationTruck, locationMooc);
		int departureTimeMooc = MAX(arrivalTimeMooc, timeMooc)
				+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();

		distance = getDistance(locationTruck, locationMooc);

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(truck, mooc,
				null, locationMooc, departureTimeMooc, null, null, distance);
		return ret;

	}
	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck, String locationTruck, int departureTime, 
			double extraDistance, Mooc mooc) {
		int startTimeTruck = departureTime;//mTruck2LastTime.get(truck);// (int)
														// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		double distance = 0;
		//String locationTruck = mCode2DepotTruck.get(truck.getDepotTruckCode())
		//		.getLocationCode();
		//DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		//String locationTruck = depotTruck.getLocationCode(); 
		
		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		int timeMooc = mMooc2LastTime.get(mooc);
		int arrivalTimeMooc = startTimeTruck
				+ getTravelTime(locationTruck, locationMooc);
		int departureTimeMooc = MAX(arrivalTimeMooc, timeMooc)
				+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();

		distance = getDistance(locationTruck, locationMooc) + extraDistance;

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(truck, mooc,
				null, locationMooc, departureTimeMooc, null, null, distance);
		return ret;

	}


	public ComboContainerMoocTruck findLastAvailable(Truck truck, Mooc mooc,
			Container container) {
		// find the last position where these combo are available together, and
		// is ready for serving other request
		if (mContainer2LastDepot.get(container) == null) {
			// container is not available (has been deposited into ship at port
			return null;
		}
		TruckItinerary I = mTruck2Itinerary.get(truck);
		if (I == null || I.size() == 0) {
			// the truck has not been assigned to any route
			// establish route from depot of truck to the last depot of mooc and
			// then to the last depot of container
			return createLastAvailableCombo(truck, mooc, container);
		} else {
			TruckRoute tr = I.getLastTruckRoute();
			// find
			String locationCode = null;
			int departureTime = -1;
			for (int i = tr.getNodes().length - 1; i >= 0; i--) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						String locationCodePort = e.getLocationCode();
						String locationContainer = mContainer2LastDepot.get(
								container).getLocationCode();
						
						int arrivalTimeContainer = departureTimePort
								+ getTravelTime(locationCodePort,
										locationContainer);
						int timeContainer = mContainer2LastTime.get(container);
						departureTime = MAX(arrivalTimeContainer, timeContainer)
								+ mContainer2LastDepot.get(container)
										.getPickupContainerDuration();

						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();// negative ->
																	// distance
																	// is
																	// reduced
						extraDistance += getDistance(locationCodePort,
								locationContainer);

						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, container, locationContainer,
								departureTime, e, tr, extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
					if (e.getMooc() == mooc
							&& e.getContainer() == container) {
						departureTime = mPoint2DepartureTime.get(e);
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getDistance();// negative -> distance is
													// reduced
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, container, locationCode,
								departureTime, e, tr, extraDistance);
						return ret;
					}
				}
			}
			if (locationCode == null)
				return createLastAvailableCombo(truck, mooc, container);

		}

		if (true)
			return null;

		if (mContainer2LastDepot.get(container) == null) {
			// container is not available (has been deposited into ship at port
			return null;
		}
		TruckRoute tr = mTruck2Route.get(truck);
		if (tr == null) {
			// the truck has not been assigned to any route
			// establish route from depot of truck to the last depot of mooc and
			// then to the last depot of container
			return createLastAvailableCombo(truck, mooc, container);
		} else {
			// find
			String locationCode = null;
			int departureTime = -1;
			for (int i = tr.getNodes().length - 1; i >= 0; i--) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getTruck() == truck && e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						String locationCodePort = e.getLocationCode();
						String locationContainer = mContainer2LastDepot.get(
								container).getLocationCode();
						int arrivalTimeContainer = departureTimePort
								+ getTravelTime(locationCodePort,
										locationContainer);
						int timeContainer = mContainer2LastTime.get(container);
						departureTime = MAX(arrivalTimeContainer, timeContainer)
								+ mContainer2LastDepot.get(container)
										.getPickupContainerDuration();

						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();// negative ->
																	// distance
																	// is
																	// reduced
						extraDistance += getDistance(locationCodePort,
								locationContainer);

						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, container, locationContainer,
								departureTime, e, tr, extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
					if (e.getTruck() == truck && e.getMooc() == mooc
							&& e.getContainer() == container) {
						departureTime = mPoint2DepartureTime.get(e);
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getDistance();// negative -> distance is
													// reduced
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, container, locationCode,
								departureTime, e, tr, extraDistance);
						return ret;
					}

				}
			}
			if (locationCode == null)
				return createLastAvailableCombo(truck, mooc, container);
		}
		return null;
	}

	public ComboContainerMoocTruck findLastAvailable(Truck truck, Mooc mooc) {
		// find the last position where these combo are available together, and
		// is ready for serving other request
		TruckItinerary I = mTruck2Itinerary.get(truck);
		if (I == null || I.size() == 0) {
			// the truck has not been assigned to any route
			// establish route from depot of truck to the last depot of mooc
			return createLastAvailableCombo(truck, mooc);
		} else {
			TruckRoute tr = I.getLastTruckRoute();
			// find
			String locationCode = null;
			int departureTime = -1;
			for (int i = tr.getNodes().length - 1; i >= 0; i--) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						String locationCodePort = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, null, locationCodePort,
								departureTimePort, e, tr, extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {
					if (e.getMooc() == mooc) {
						departureTime = mPoint2DepartureTime.get(e);
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, null, locationCode, departureTime,
								e, tr, extraDistance);
						return ret;
					}

				}else if(e.getAction().equals(ActionEnum.RELEASE_MOOC_AT_DEPOT)){
					departureTime = mPoint2DepartureTime.get(e);
					locationCode = e.getLocationCode();
					double extraDistance = e.getDistance()
							- tr.getLastNode().getDistance();
					return createLastAvailableCombo(truck, locationCode, departureTime, extraDistance, mooc);
				}
			}
			if (locationCode == null)
				return createLastAvailableCombo(truck, mooc);

		}

		if (true)
			return null;

		TruckRoute tr = mTruck2Route.get(truck);
		if (tr == null) {
			// the truck has not been assigned to any route
			// establish route from depot of truck to the last depot of mooc
			return createLastAvailableCombo(truck, mooc);
		} else {
			// find
			String locationCode = null;
			int departureTime = -1;
			for (int i = tr.getNodes().length - 1; i >= 0; i--) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getTruck() == truck && e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						String locationCodePort = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, null, locationCodePort,
								departureTimePort, e, tr, extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {
					if (e.getTruck() == truck && e.getMooc() == mooc) {
						departureTime = mPoint2DepartureTime.get(e);
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								truck, mooc, null, locationCode, departureTime,
								e, tr, extraDistance);
						return ret;
					}

				}
			}
			if (locationCode == null)
				return createLastAvailableCombo(truck, mooc);
		}
		return null;
	}

	public ComboContainerMoocTruck findBestFitContainerMoocTruck4ExportRequest(
			ExportContainerRequest req) {
		// find and return available/feasible combo of truck-mooc-container for
		// serving the request minimizing distance

		return null;
	}

	public ComboContainerMoocTruck findBestFitMoocTruck4ImportRequest(
			ImportContainerRequest req) {
		return null;
	}

	public ComboContainerMoocTruck findBestFitContainerMoocTruck4WarehouseRequest(
			WarehouseContainerTransportRequest req) {

		return null;
	}

	public ComboContainerMoocTruck findBestFitMoocTruckFor2ImportRequest(
			ImportContainerRequest req1, ImportContainerRequest req2) {
		// req1 and req2 is of type 20, source is the same port and two
		// destination are closed together

		return null;
	}

	public Container selectContainer(String containerCategory,
			String depotContainerCode) {
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			if (c.getCategoryCode().equals(containerCategory)
					&& c.getDepotContainerCode().equals(depotContainerCode))
				return c;
		}
		return null;
	}

	public TruckRoute createDirectRouteForExportRequest(
			ExportContainerRequest req) {
		TruckRoute r = new TruckRoute();
		DepotContainer depotContainer = mCode2DepotContainer.get(req
				.getDepotContainerCode());
		System.out.println(name()
				+ "::createDirectRouteForExportRequest, depotContainerCode = "
				+ req.getDepotContainerCode() + ", depotContainer = "
				+ depotContainer);

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
		Container sel_container = selectContainer(req.getContainerCategory(),
				req.getDepotContainerCode());

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

	public TruckRoute createDirectRouteForImportRequest(
			ImportContainerRequest req, Truck truck, Mooc mooc) {
		// truck and mooc are REST at their depots

		TruckRoute r = new TruckRoute();
		Port port = mCode2Port.get(req.getPortCode());

		// ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);

		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depart from the depot of the truck
		int startTime = getLastDepartureTime(truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(truck);
		mPoint2DepartureTime.put(e[0], startTime);

		// arrive at the depot mooc, take a mooc
		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(mooc);
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
		e[2].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e[2].setImportRequest(req);
		e[2].setContainer(mCode2Container.get(req.getContainerCode()));
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
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimeUnloadAtWarehouse()));
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
		e[5].setDepotContainer(mCode2DepotContainer.get(req
				.getDepotContainerCode()));
		e[5].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[5].setContainer(null);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[5].getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		mContainer2LastDepot.put(e[5].getContainer(), e[5].getDepotContainer());
		mContainer2LastTime.put(e[5].getContainer(), departureTime);

		e[6].deriveFrom(e[5]);
		e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(mooc);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);
		mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		mMooc2LastTime.put(mooc, departureTime);

		e[7].deriveFrom(e[6]);
		e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[7].setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);
		mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		mTruck2LastTime.put(truck, departureTime);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);

		propagate(r);

		return r;
	}

	public TruckRouteInfo4Request createRouteForImportRequest(
			ImportContainerRequest req, Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		TruckRoute r = new TruckRoute();
		Port port = mCode2Port.get(req.getPortCode());

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		if (combo.routeElement == null) {

			// depart from the depot of the truck
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setMooc(mooc);
			travelTime = getTravelTime(e0, e1);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e1, arrivalTime);
			mPoint2DepartureTime.put(e1, departureTime);
			lastElement = e1;
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}
		// arrive at the depot container
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setPort(port);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
		e2.setImportRequest(req);
		Container container = mCode2Container.get(req.getContainerCode()); 
		e2.setContainer(container);
		distance = combo.extraDistance + getTravelTime(lastElement, e2);

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = req.getLoadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(e2);
		e3.setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e3.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e2, e3);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimeUnloadAtWarehouse()));
		int finishedServiceTime = startServiceTime + req.getUnloadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		e4.setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
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
		DepotContainer depotContainer = findDepotContainer4Deposit(req, e4, container);
		//e5.setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e5.setDepotContainer(depotContainer);
				
		e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e5.setContainer(null);
		travelTime = getTravelTime(e4, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e5.getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);
		//mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
		//mContainer2LastTime.put(e5.getContainer(), departureTime);
		tri.setLastDepotContainer(container, e5.getDepotContainer());
		tri.setLastTimeContainer(container, departureTime);
		
		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e5, mooc);
		e6.setDepotMooc(depotMooc);
		//e6.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		
		e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e6.setMooc(mooc);
		travelTime = getTravelTime(e5, e6);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e6.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e6, arrivalTime);
		mPoint2DepartureTime.put(e6, departureTime);
		//mMooc2LastDepot.put(mooc, e6.getDepotMooc());
		//mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc,  e6.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);
		
		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(e6);
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e6, truck);
		//e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e7.setDepotTruck(depotTruck);
		
		e7.setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e6, e7);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e7, arrivalTime);
		mPoint2DepartureTime.put(e7, departureTime);
		//mTruck2LastDepot.put(truck, e7.getDepotTruck());
		//mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e7.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);
		
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_IMPORT);
		propagate(r);

		
		tri.route = r;
		tri.additionalDistance = distance;
		tri.lastUsedIndex = lastUsedIndex;

		return tri;
	}

	public TruckRoute createDirectRouteForExportRequest(
			ExportContainerRequest req, Truck truck, Mooc mooc,
			Container container) {
		String header = name() + "::createDirectRouteForExportRequest";
		// truck, mooc, container are REST at their depots

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depart from the depot of the truck
		int startTime = mTruck2LastTime.get(truck);// getLastDepartureTime(sel_truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(truck);
		mPoint2DepartureTime.put(e[0], startTime);

		// arrive at the depot mooc, take a mooc
		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(mooc);

		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);

		// arrive at the depot container
		e[2].deriveFrom(e[1]);
		e[2].setDepotContainer(mContainer2LastDepot.get(container));
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		e[2].setContainer(container);
		// if(e[2].getDepotContainer() == null){
		// System.out.println(header + ", depotContainer of " +
		// container.getCode() + " = NULL");
		// }else{
		// System.out.println(header + ", depotContainer of " +
		// container.getCode() + " = " + e[2].getDepotContainer().getCode());
		// }
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
		// update last depot container, set to not-available
		mContainer2LastDepot.put(container, null);
		mContainer2LastTime.put(container, Integer.MAX_VALUE);

		e[6].deriveFrom(e[5]);
		e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(null);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);
		// update last depot and lastTime of mooc
		mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		mMooc2LastTime.put(mooc, departureTime);

		e[7].deriveFrom(e[6]);
		e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[7].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);
		// update last depot and last time of truck
		mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		mTruck2LastTime.put(truck, departureTime);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);

		propagate(r);

		return r;
	}

	public DepotMooc findDepotMooc4Deposit(ExportContainerRequest r, RouteElement fromElement, Mooc mooc){
		//return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for(String lc: mooc.getReturnDepotCodes()){
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d= getTravelTime(fromElement.getLocationCode(),depotMooc.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}
	public DepotMooc findDepotMooc4Deposit(WarehouseContainerTransportRequest r, RouteElement fromElement, Mooc mooc){
		//return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for(String lc: mooc.getReturnDepotCodes()){
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d= getTravelTime(fromElement.getLocationCode(),depotMooc.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}
	public DepotMooc findDepotMooc4Deposit(ImportContainerRequest r, RouteElement fromElement, Mooc mooc){
		//return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for(String lc: mooc.getReturnDepotCodes()){
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d= getTravelTime(fromElement.getLocationCode(),depotMooc.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}
	
	public DepotContainer findDepotContainer4Deposit(ImportContainerRequest r, RouteElement fromElement, Container container){
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for(String lc: r.getDepotContainerCode()){
			DepotContainer dc = mCode2DepotContainer.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(), dc.getLocationCode());
			if(minDis > d){
				minDis = d;
				sel_depot = dc;
			}
		}
		return sel_depot;//mCode2DepotContainer.get(container.getDepotContainerCode());
	}
	public DepotContainer findDepotContainer4Deposit(WarehouseContainerTransportRequest r, RouteElement fromElement, Container container){
		//return mCode2DepotContainer.get(container.getDepotContainerCode());
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for(String lc:container.getReturnDepotCodes()){
			DepotContainer depotContainer = mCode2DepotContainer.get(lc);
			double d= getTravelTime(fromElement.getLocationCode(),depotContainer.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotContainer;
			}
		}
		return sel_depot;
	}
	public DepotContainer findDepotContainer4Deposit(ExportContainerRequest r, RouteElement fromElement, Container container){
		//return mCode2DepotContainer.get(container.getDepotContainerCode());
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for(String lc:container.getReturnDepotCodes()){
			DepotContainer depotContainer = mCode2DepotContainer.get(lc);
			double d= getTravelTime(fromElement.getLocationCode(),depotContainer.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotContainer;
			}
		}
		return sel_depot;
	}
	
	public DepotTruck findDepotTruck4Deposit(ExportContainerRequest r, RouteElement fromElement, Truck truck){
		//return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for(String c: truck.getReturnDepotCodes()){
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),depotTruck.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}
	public DepotTruck findDepotTruck4Deposit(WarehouseContainerTransportRequest r, RouteElement fromElement, Truck truck){
		//return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for(String c: truck.getReturnDepotCodes()){
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),depotTruck.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}
	public DepotTruck findDepotTruck4Deposit(ImportContainerRequest r, RouteElement fromElement, Truck truck){
		//return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for(String c: truck.getReturnDepotCodes()){
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),depotTruck.getLocationCode());
			if(d < minDis){
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}
	
	
	public TruckRouteInfo4Request createRouteForExportRequest(
			ExportContainerRequest req, Truck truck, Mooc mooc,
			Container container) {
		String header = name() + "::createRouteForExportRequest";
		// truck, mooc, container are REST at their depots
		if(mContainer2LastDepot.get(container) == null){
			return null;
		}
		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
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
			e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
			e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e[1].setMooc(mooc);

			travelTime = getTravelTime(e[0], e[1]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[1].getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[1], arrivalTime);
			mPoint2DepartureTime.put(e[1], departureTime);

			// arrive at the depot container
			L.add(e[2]);
			e[2].deriveFrom(e[1]);
			e[2].setDepotContainer(mContainer2LastDepot.get(container));
			e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e[2].setContainer(container);
			if(mContainer2LastDepot.get(container) == null){
				System.out.println(header + ", container = " + container.getCode() + " has no depot??????");
			}
			travelTime = getTravelTime(e[1], e[2]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[2].getDepotContainer().getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[2], arrivalTime);
			mPoint2DepartureTime.put(e[2], departureTime);

			lastElement = e[2];
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		L.add(e[3]);
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e[3].setExportRequest(req);

		travelTime = getTravelTime(lastElement, e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimeLoadAtWarehouse()));
		distance = combo.extraDistance + getDistance(lastElement, e[3]);
		int finishedServiceTime = startServiceTime + req.getLoadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		L.add(e[4]);
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

		L.add(e[5]);
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
		// update last depot container, set to not-available
		mContainer2LastDepot.put(container, null);
		mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(container, null);
		tri.setLastTimeContainer(container, null);

		L.add(e[6]);
		e[6].deriveFrom(e[5]);
		DepotMooc depotMooc = findDepotMooc4Deposit(req,e[5],mooc);
		//e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[6].setDepotMooc(depotMooc);
		e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[6].setMooc(null);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[6].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);
		// update last depot and lastTime of mooc
		//mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		//mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e[6].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);
		
		tri.setLastDepotMooc(mooc, e[6].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		L.add(e[7]);
		e[7].deriveFrom(e[6]);
		//e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e[6], truck);
		e[7].setDepotTruck(depotTruck);
		e[7].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);
		// update last depot and last time of truck
		//mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		//mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e[7].getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);
		
		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_EXPORT);
		propagate(r);

		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRoute createDirectRouteForWarehouseWarehouseRequest(
			WarehouseContainerTransportRequest req, Truck truck, Mooc mooc,
			Container container) {

		Warehouse pickupWarehouse = mCode2Warehouse.get(req
				.getFromWarehouseCode());
		Warehouse deliveryWarehouse = mCode2Warehouse.get(req
				.getToWarehouseCode());
		String pickupLocationCode = pickupWarehouse.getLocationCode();
		String deliveryLocationCode = deliveryWarehouse.getLocationCode();

		RouteElement[] e = new RouteElement[10];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		int startTime = getLastDepartureTime(truck);
		e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(truck);
		mPoint2DepartureTime.put(e[0], startTime);

		e[1].deriveFrom(e[0]);
		e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		e[1].setMooc(mooc);
		int travelTime = getTravelTime(e[0], e[1]);
		int arrivalTime = startTime + travelTime;
		int startServiceTime = arrivalTime;
		int duration = e[1].getDepotMooc().getPickupMoocDuration();
		int departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[1], arrivalTime);
		mPoint2DepartureTime.put(e[1], departureTime);

		e[2].deriveFrom(e[1]);
		e[2].setDepotContainer(mCode2DepotContainer.get(container
				.getDepotContainerCode()));
		e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
		e[2].setContainer(container);
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
		e[7].setDepotContainer(mCode2DepotContainer.get(container
				.getDepotContainerCode()));
		e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[7].setContainer(null);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotContainer.get(container.getDepotContainerCode())
				.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);

		e[8].deriveFrom(e[7]);
		e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[8].setMooc(mooc);
		travelTime = getTravelTime(e[7], e[8]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotMooc.get(mooc.getDepotMoocCode())
				.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[8], arrivalTime);
		mPoint2DepartureTime.put(e[8], departureTime);

		e[9].deriveFrom(e[8]);
		e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
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
		r.setTruck(truck);

		propagate(r);

		return r;
	}


	public TruckRouteInfo4Request createRouteForWarehouseWarehouseRequest(
			WarehouseContainerTransportRequest req, Truck truck, Mooc mooc,
			Container container) {

		Warehouse pickupWarehouse = mCode2Warehouse.get(req
				.getFromWarehouseCode());
		Warehouse deliveryWarehouse = mCode2Warehouse.get(req
				.getToWarehouseCode());
		String pickupLocationCode = pickupWarehouse.getLocationCode();
		String deliveryLocationCode = deliveryWarehouse.getLocationCode();

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc,
				container);
		if (combo == null)
			return null;

		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		RouteElement[] e = new RouteElement[10];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		if (combo.routeElement == null) {
			L.add(e[0]);
			e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
			e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e[1].setMooc(mooc);
			travelTime = getTravelTime(e[0], e[1]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[1].getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[1], arrivalTime);
			mPoint2DepartureTime.put(e[1], departureTime);

			e[2].deriveFrom(e[1]);
			L.add(e[2]);
			e[2].setDepotContainer(mCode2DepotContainer.get(container
					.getDepotContainerCode()));
			e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e[2].setContainer(container);
			travelTime = getTravelTime(e[1], e[2]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[2].getDepotContainer().getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[2], arrivalTime);
			mPoint2DepartureTime.put(e[2], departureTime);

			lastElement = e[2];
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		L.add(e[3]);
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(pickupWarehouse);
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e[3].setWarehouseRequest(req);
		travelTime = getTravelTime(lastElement, e[3]);
		distance = combo.extraDistance + getDistance(lastElement, e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		int finishedServiceTime = startServiceTime + req.getLoadDuration();
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		L.add(e[4]);
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

		L.add(e[5]);
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

		L.add(e[6]);
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

		L.add(e[7]);
		e[7].deriveFrom(e[6]);
		//e[7].setDepotContainer(mCode2DepotContainer.get(container.getDepotContainerCode()));
		DepotContainer depotContainer = findDepotContainer4Deposit(req, e[6], container);
		e[7].setDepotContainer(depotContainer);
		
		e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[7].setContainer(null);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotContainer.get(container.getDepotContainerCode())
				.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[7], arrivalTime);
		mPoint2DepartureTime.put(e[7], departureTime);
		tri.setLastDepotContainer(container, e[7].getDepotContainer());
		tri.setLastTimeContainer(container, departureTime);
		
		L.add(e[8]);
		e[8].deriveFrom(e[7]);
		//e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e[7], mooc);
		e[8].setDepotMooc(depotMooc);
		
		e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[8].setMooc(mooc);
		travelTime = getTravelTime(e[7], e[8]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotMooc.get(mooc.getDepotMoocCode())
				.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[8], arrivalTime);
		mPoint2DepartureTime.put(e[8], departureTime);
		tri.setLastDepotMooc(mooc, e[8].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);
		
		L.add(e[9]);
		e[9].deriveFrom(e[8]);
		//e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e[8], truck);
		e[9].setDepotTruck(depotTruck);
		
		e[9].setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e[8], e[9]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[9], arrivalTime);
		mPoint2DepartureTime.put(e[9], departureTime);
		tri.setLastDepotTruck(truck, e[9].getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);
		
		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_WAREHOUSE);
		propagate(r);

		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRoute createDirectRouteForWarehouseWarehouseRequest(
			WarehouseContainerTransportRequest req) {

		Warehouse pickupWarehouse = mCode2Warehouse.get(req
				.getFromWarehouseCode());
		Warehouse deliveryWarehouse = mCode2Warehouse.get(req
				.getToWarehouseCode());
		String pickupLocationCode = pickupWarehouse.getLocationCode();
		String deliveryLocationCode = deliveryWarehouse.getLocationCode();

		Truck sel_truck = null;
		Container sel_container = null;
		Mooc sel_mooc = null;
		double minDistance = Integer.MAX_VALUE;
		// select nearest truck, mooc, container
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			DepotTruck depotTruck = mCode2DepotTruck.get(truck
					.getDepotTruckCode());
			String truckLocationCode = depotTruck.getLocationCode();

			for (int j = 0; j < input.getMoocs().length; j++) {
				Mooc mooc = input.getMoocs()[j];
				DepotMooc depotMooc = mCode2DepotMooc.get(mooc
						.getDepotMoocCode());
				String moocLocationCode = depotMooc.getLocationCode();
				for (int k = 0; k < input.getContainers().length; k++) {
					Container container = input.getContainers()[k];
					DepotContainer depotContainer = mCode2DepotContainer
							.get(container.getDepotContainerCode());
					String containerLocationCode = depotContainer
							.getLocationCode();

					double d = getDistance(truckLocationCode, moocLocationCode)
							+ getDistance(moocLocationCode,
									containerLocationCode)
							+ getDistance(containerLocationCode,
									pickupLocationCode);
					if (d < minDistance) {
						minDistance = d;
						sel_truck = truck;
						sel_mooc = mooc;
						sel_container = container;
					}
				}
			}
		}

		RouteElement[] e = new RouteElement[10];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

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
		e[2].setDepotContainer(mCode2DepotContainer.get(sel_container
				.getDepotContainerCode()));
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
		e[7].setDepotContainer(mCode2DepotContainer.get(sel_container
				.getDepotContainerCode()));
		e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e[7].setContainer(null);
		travelTime = getTravelTime(e[6], e[7]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = mCode2DepotContainer.get(
				sel_container.getDepotContainerCode())
				.getDeliveryContainerDuration();
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
		duration = mCode2DepotMooc.get(sel_mooc.getDepotMoocCode())
				.getDeliveryMoocDuration();
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
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimeUnloadAtWarehouse()));
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
		e[5].setDepotContainer(mCode2DepotContainer.get(req
				.getDepotContainerCode()));
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

	public SwapEmptyContainerMooc evaluateSwap(TruckRoute tr1, TruckRoute tr2) {
		// tr1 deposits loaded container at warehouse 1
		// tr2 carries loaded container from warehouse 2
		// try to merge these routes into one: after depositing container at the
		// warehouse,
		// truck tr1, mooc, empty container will go warehouse 2 to load goods
		// and carry
		// remove tr2
		// return delta (new disance - old distance)
		RouteElement[] e1 = tr1.getNodes();
		RouteElement[] e2 = tr2.getNodes();
		int sel_i1 = -1;
		for (int i = 0; i < e1.length; i++) {
			if (e1[i].getAction().equals(
					ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
				sel_i1 = i;
				break;
			}
		}
		if (sel_i1 < 0)
			return null;// Integer.MAX_VALUE;

		int sel_i21 = -1;
		for (int i = 0; i < e2.length; i++) {
			if (e2[i].getAction().equals(
					ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE)) {
				sel_i21 = i;
				break;
			}
		}
		if (sel_i21 < 0)
			return null;// Integer.MAX_VALUE;

		int sel_i22 = -1;
		for (int i = sel_i21; i < e2.length; i++) {
			if (e2[i].getAction().equals(
					ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| e2[i].getAction().equals(
							ActionEnum.LINK_EMPTY_MOOC_AT_PORT)) {
				sel_i22 = i;
				break;
			}
		}
		if (sel_i22 < 0)
			return null;// Integer.MAX_VALUE;

		double newDis = e1[sel_i1].getDistance()
				+ getDistance(e1[sel_i1], e2[sel_i21])
				+ tr2.getDistanceSubRoute(sel_i21, sel_i22)
				+ getDistance(e2[sel_i22], e1[sel_i1 + 1])
				+ tr1.getDistanceFromPositionToEnd(sel_i1 + 1);
		double eval = newDis - tr1.getDistance() - tr2.getDistance();
		SwapEmptyContainerMooc m = new SwapEmptyContainerMooc(tr1, tr2, sel_i1,
				sel_i21, sel_i22, eval);

		return m;
	}

	public TruckRoute performSwap(SwapEmptyContainerMooc move) {
		// tr1 deposits loaded container at warehouse 1
		// tr2 carries loaded container from warehouse 2
		// try to merge these routes into one: after depositing container at the
		// warehouse,
		// truck tr1, mooc, empty container will go warehouse 2 to load goods
		// and carry
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
		for (int i = 0; i <= sel_i1; i++)
			L.add(e1[i]);
		for (int i = sel_i21; i <= sel_i22; i++)
			L.add(e2[i]);
		for (int i = sel_i1 + 1; i < e1.length; i++)
			L.add(e1[i]);
		/*
		 * double newDis = e1[sel_i1].getDistance() + getDistance(e1[sel_i1],
		 * e2[sel_i21]) + tr2.getDistanceSubRoute(sel_i21, sel_i22) +
		 * getDistance(e2[sel_i22], e1[sel_i1+1]) +
		 * tr1.getDistanceFromPositionToEnd(sel_i1+1); double eval = newDis -
		 * tr1.getDistance() - tr2.getDistance(); SwapEmptyContainerMooc m = new
		 * SwapEmptyContainerMooc(tr1, tr2, sel_i1, sel_i21, sel_i22,eval);
		 */
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute r = new TruckRoute(tr1.getTruck(), e);
		return r;
	}

	public void improveSwap(ArrayList<TruckRoute> TR) {
		int n = TR.size();
		int[] X = new int[n];
		int[] Y = new int[n];
		for (int i = 0; i < n; i++) {
			X[i] = i;
			Y[i] = i;
		}
		ArrayList<Integer> l_edgeX = new ArrayList<Integer>();
		ArrayList<Integer> l_edgeY = new ArrayList<Integer>();
		ArrayList<Double> l_w = new ArrayList<Double>();
		MoveOperator[][] moves = new MoveOperator[n][n];
		for (int i = 0; i < TR.size(); i++) {
			for (int j = 0; j < TR.size(); j++)
				if (i != j) {
					SwapEmptyContainerMooc m = evaluateSwap(TR.get(i),
							TR.get(j));
					double eval = Integer.MAX_VALUE;
					if (m != null) {
						eval = m.eval;
						moves[i][j] = m;
					}
					if (eval < 0) {
						System.out.println(name()
								+ "::improveSwap, DETECT Swap(" + i + "," + j
								+ "), eval = " + eval);
						l_edgeX.add(i);
						l_edgeY.add(j);
						l_w.add(eval);
					} else {
						System.out.println(name() + "::improveSwap, Swap(" + i
								+ "," + j + "), eval = " + eval);
					}
				}
		}
		int[] edgeX = new int[l_edgeX.size()];
		int[] edgeY = new int[l_edgeY.size()];
		double[] w = new double[l_edgeX.size()];
		for (int i = 0; i < l_edgeX.size(); i++) {
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

		for (int i = 0; i < solX.length; i++) {
			System.out.println(name() + "::improveSwap, match " + solX[i] + "-"
					+ solY[i]);
		}

		// perform swap route-truck
		ArrayList<TruckRoute> AL = new ArrayList<TruckRoute>();
		for (int k = 0; k < solX.length; k++) {
			int i = solX[k];
			int j = solY[k];
			SwapEmptyContainerMooc m = (SwapEmptyContainerMooc) moves[i][j];
			TruckRoute r = performSwap(m);
			AL.add(r);
			int idx = TR.indexOf(m.tr1);
			TR.remove(idx);
			idx = TR.indexOf(m.tr2);
			TR.remove(idx);
		}
		for (TruckRoute tr : AL) {
			TR.add(tr);
		}
	}

	public void propagate(TruckRoute tr) {
		propagateArrivalDepartureTimeString(tr);
		propagateDistance(tr);
	}

	public void propagateDistance(TruckRoute tr) {
		RouteElement[] e = tr.getNodes();
		e[0].setDistance(0);
		for (int i = 1; i < e.length; i++) {
			e[i].setDistance(e[i - 1].getDistance()
					+ getDistance(e[i - 1], e[i]));
		}
	}

	public void propagateArrivalDepartureTimeString(TruckRoute tr) {
		RouteElement[] e = tr.getNodes();
		for (int i = 0; i < e.length; i++) {
			if (mPoint2ArrivalTime.get(e[i]) != null) {
				e[i].setArrivalTime(DateTimeUtils
						.unixTimeStamp2DateTime(mPoint2ArrivalTime.get(e[i])));
			}
			if (mPoint2DepartureTime.get(e[i]) != null) {
				e[i].setDepartureTime(DateTimeUtils
						.unixTimeStamp2DateTime(mPoint2DepartureTime.get(e[i])));
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
		if(lc1 == null || lc2 == null)
			System.out.println(name() + "::getTravelTime, lc1 = " + lc1 +
				", lc2 = " + lc2);
		return getTravelTime(lc1, lc2);
	}

	public String getLastLocationCode(Truck truck) {
		TruckRoute tr = mTruck2Route.get(truck);
		String locationCode = "";
		if (tr == null) {
			DepotTruck depot = mCode2DepotTruck.get(truck.getDepotTruckCode());
			return depot.getLocationCode();
		} else {

		}
		return locationCode;
	}

	public void init() {
		mTruck2LastDepot = new HashMap<Truck, DepotTruck>();
		mTruck2LastTime = new HashMap<Truck, Integer>();
		mMooc2LastDepot = new HashMap<Mooc, DepotMooc>();
		mMooc2LastTime = new HashMap<Mooc, Integer>();
		mContainer2LastDepot = new HashMap<Container, DepotContainer>();
		mContainer2LastTime = new HashMap<Container, Integer>();
		int minStartTime = Integer.MAX_VALUE;
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			DepotTruck depot = mCode2DepotTruck.get(truck.getDepotTruckCode());
			mTruck2LastDepot.put(truck, depot);
			int startTime = (int) DateTimeUtils.dateTime2Int(truck
					.getStartWorkingTime());
			mTruck2LastTime.put(truck, startTime);

			if (minStartTime > startTime)
				minStartTime = startTime;
		}
		for (int i = 0; i < input.getMoocs().length; i++) {
			Mooc mooc = input.getMoocs()[i];
			DepotMooc depot = mCode2DepotMooc.get(mooc.getDepotMoocCode());
			mMooc2LastDepot.put(mooc, depot);
			int startTime = minStartTime;
			mMooc2LastTime.put(mooc, startTime);
		}

		for (int i = 0; i < input.getContainers().length; i++) {
			Container container = input.getContainers()[i];
			DepotContainer depot = mCode2DepotContainer.get(container
					.getDepotContainerCode());
			mContainer2LastDepot.put(container, depot);
			int startTime = minStartTime;
			mContainer2LastTime.put(container, startTime);
		}

		for (int i = 0; i < additionalContainers.size(); i++) {
			Container c = additionalContainers.get(i);
			mContainer2LastDepot.put(c, null);
			mContainer2LastTime.put(c, Integer.MAX_VALUE);
		}
	}

	public ExportContainerRequest[] getSortedExportRequests() {
		ArrayList<ExportContainerRequest> L = new ArrayList<ExportContainerRequest>();
		for (int i = 0; i < input.getExRequests().length; i++) {
			ExportContainerTruckMoocRequest R = input.getExRequests()[i];
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ExportContainerRequest r = R.getContainerRequest()[j];
				L.add(r);
			}
		}
		ExportContainerRequest[] SL = new ExportContainerRequest[L.size()];
		for (int i = 0; i < L.size(); i++)
			SL[i] = L.get(i);
		for (int i = 0; i < SL.length; i++) {
			for (int j = i + 1; j < SL.length; j++) {
				int ti = (int) DateTimeUtils.dateTime2Int(SL[i]
						.getLateDateTimeLoadAtWarehouse());
				int tj = (int) DateTimeUtils.dateTime2Int(SL[j]
						.getLateDateTimeLoadAtWarehouse());
				if (ti > tj) {
					ExportContainerRequest tmp = SL[i];
					SL[i] = SL[j];
					SL[j] = tmp;
				}
			}
		}
		return SL;
	}

	public ImportContainerRequest[] getSortedImportRequests() {
		ArrayList<ImportContainerRequest> L = new ArrayList<ImportContainerRequest>();
		for (int i = 0; i < input.getImRequests().length; i++) {
			ImportContainerTruckMoocRequest R = input.getImRequests()[i];
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ImportContainerRequest r = R.getContainerRequest()[j];
				L.add(r);
			}
		}
		ImportContainerRequest[] SL = new ImportContainerRequest[L.size()];
		for (int i = 0; i < L.size(); i++)
			SL[i] = L.get(i);
		for (int i = 0; i < SL.length; i++) {
			for (int j = i + 1; j < SL.length; j++) {
				int ti = (int) DateTimeUtils.dateTime2Int(SL[i]
						.getLateDateTimePickupAtPort());
				int tj = (int) DateTimeUtils.dateTime2Int(SL[j]
						.getLateDateTimePickupAtPort());
				if (ti > tj) {
					ImportContainerRequest tmp = SL[i];
					SL[i] = SL[j];
					SL[j] = tmp;
				}
			}
		}
		return SL;
	}

	public WarehouseContainerTransportRequest[] getSortedWarehouseTransportRequests() {
		ArrayList<WarehouseContainerTransportRequest> L = new ArrayList<WarehouseContainerTransportRequest>();
		for (int i = 0; i < input.getWarehouseRequests().length; i++) {
			WarehouseTransportRequest R = input.getWarehouseRequests()[i];
			for (int j = 0; j < R.getWarehouseContainerTransportRequests().length; j++) {
				WarehouseContainerTransportRequest r = R
						.getWarehouseContainerTransportRequests()[j];
				L.add(r);
			}
		}
		WarehouseContainerTransportRequest[] SL = new WarehouseContainerTransportRequest[L
				.size()];
		for (int i = 0; i < L.size(); i++)
			SL[i] = L.get(i);
		for (int i = 0; i < SL.length; i++) {
			for (int j = i + 1; j < SL.length; j++) {
				int ti = (int) DateTimeUtils.dateTime2Int(SL[i]
						.getLateDateTimeLoad());
				int tj = (int) DateTimeUtils.dateTime2Int(SL[j]
						.getLateDateTimeLoad());
				if (ti > tj) {
					WarehouseContainerTransportRequest tmp = SL[i];
					SL[i] = SL[j];
					SL[j] = tmp;
				}
			}
		}
		return SL;
	}

}
