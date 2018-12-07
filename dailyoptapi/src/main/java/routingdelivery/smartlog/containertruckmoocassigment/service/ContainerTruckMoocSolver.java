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
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerFromDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerToDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerTruckMoocRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerTruckMoocRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.Intervals;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocGroup;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoveOperator;
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.ShipCompany;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapEmptyContainerMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.TransportContainerLocationInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.TransportContainerRequest;
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
	
	public HashMap<String, ArrayList<Truck>> mDepot2TruckList;
	public HashMap<String, ArrayList<Mooc>> mDepot2MoocList;
	public HashMap<String, ArrayList<Container>> mDepot2ContainerList;

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

	public Warehouse getWarehouseFromCode(String code) {
		return mCode2Warehouse.get(code);
	}

	public Port getPortFromCode(String code) {
		return mCode2Port.get(code);
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
		if (log != null) {
			log.close();
			log = null;
		} else {

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

				// String depotContainerCode = r.getDepotContainerCode();
				String containerCategory = r.getContainerCategory();
				idxCode++;
				String code = "A-" + idxCode;
				Container c = new Container(code, (int) r.getWeight(),
						r.getContainerCategory(), null,
						r.getDepotContainerCode());
				additionalContainers.add(c);
				// c.setImportedContainer(true);
				r.setContainerCode(code);
			}
		}
		Container[] L = new Container[input.getContainers().length
				+ additionalContainers.size()];
		for (int i = 0; i < input.getContainers().length; i++) {
			L[i] = input.getContainers()[i];
			L[i].setImportedContainer(false);
		}
		for (int i = 0; i < additionalContainers.size(); i++) {
			L[i + input.getContainers().length] = additionalContainers.get(i);
			L[i].setImportedContainer(true);
		}
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
			System.out.println(name() + "::mapData, mLocationCode2Index.put("
			+ lc + "," + idx + ")");
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

			// DistanceElement et = input.getTravelTime()[i];
			// src = et.getSrcCode();
			// dest = et.getDestCode();
			// is = mLocationCode2Index.get(src);
			// id = mLocationCode2Index.get(dest);
			// travelTime[is][id] = et.getDistance();
			travelTime[is][id] = e.getTravelTime();
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
			System.out.println(name() + "::mapData, warehouse put("
					+ input.getWarehouses()[i].getCode());
		}
		mCode2Mooc = new HashMap<String, Mooc>();
		mDepot2MoocList = new HashMap<String, ArrayList<Mooc>>();
		for (int i = 0; i < input.getMoocs().length; i++) {
			Mooc mooc = input.getMoocs()[i];
			mCode2Mooc.put(mooc.getCode(), mooc);
			String depotMoocCode = mooc.getDepotMoocCode();
			if(depotMoocCode == null)
				depotMoocCode = "isScheduled";
			ArrayList<Mooc> moocList = mDepot2MoocList.get(depotMoocCode);
			if(moocList == null)
				moocList = new ArrayList<Mooc>();
			moocList.add(mooc);
			mDepot2MoocList.put(depotMoocCode, moocList);
		}
		if(mDepot2MoocList.get("isScheduled") == null)
			mDepot2MoocList.put("isScheduled", new ArrayList<Mooc>());
		
		mCode2Truck = new HashMap<String, Truck>();
		mDepot2TruckList = new HashMap<String, ArrayList<Truck>>();
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			mCode2Truck.put(truck.getCode(),
					truck);
			String depotTruckCode = truck.getDepotTruckCode();
			if(depotTruckCode == null)
				depotTruckCode = "isScheduled";
			ArrayList<Truck> truckList = mDepot2TruckList.get(depotTruckCode);
			if(truckList == null)
				truckList = new ArrayList<Truck>();
			truckList.add(truck);
			mDepot2TruckList.put(depotTruckCode, truckList);
		}
		if(mDepot2TruckList.get("isScheduled") == null)
			mDepot2TruckList.put("isScheduled", new ArrayList<Truck>());
		
		mCode2Container = new HashMap<String, Container>();
		mDepot2ContainerList = new HashMap<String, ArrayList<Container>>();
		for (int i = 0; i < input.getContainers().length; i++) {
			Container c = input.getContainers()[i];
			mCode2Container.put(c.getCode(), c);
			String depotContainerCode = c.getDepotContainerCode();
			if(depotContainerCode == null)
				depotContainerCode = "isScheduled";
			ArrayList<Container> contList = mDepot2ContainerList.get(depotContainerCode);
			if(contList == null)
				contList = new ArrayList<Container>();
			contList.add(c);
			mDepot2ContainerList.put(depotContainerCode, contList);
		}
		if(mDepot2ContainerList.get("isScheduled") == null)
			mDepot2ContainerList.put("isScheduled", new ArrayList<Container>());
		
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
		// if (mLocationCode2Index.get(dest) == null) {
		// System.out.println(name() + "::getDistance, location dest = "
		// + dest + " has no index BUG???????");
		// }
		if (mLocationCode2Index.get(src) == null
				|| mLocationCode2Index.get(dest) == null) {
			System.out.println(name() + "::getDistance, src " + src +
			 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			//return 1000;
		}

		int is = mLocationCode2Index.get(src);
		int id = mLocationCode2Index.get(dest);
		return distance[is][id];
	}

	public int getTravelTime(String src, String dest) {
		if (mLocationCode2Index.get(src) == null
				|| mLocationCode2Index.get(dest) == null) {
			 System.out.println(name() + "::getTravelTime, src " + src +
			 " OR dest " + dest + " NOT COMPLETE, INPUT ERROR??????");
			//return 1000;

		}

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
	
	public boolean fitContMoocType(String requestContType, String contType, String moocType){
		if(!contType.equals(requestContType))
			return false;
		
		boolean contTypeinMoocGroup = false;
		MoocGroup[] moocGroup = input.getMoocGroup();
		for(int i = 0; i < moocGroup.length; i++){
			MoocGroup group = moocGroup[i];
			if(group.getCode().equals(moocType)){
				for(int j = 0; j < group.getPacking().length; j++){
					if(group.getPacking()[j].getContTypeCode().equals(contType)){
						contTypeinMoocGroup = true;
						break;
					}
				}
				break;
			}
		}
		
		if(!contTypeinMoocGroup)
			return false;
		return true;
	}
	
	public boolean checkWeight(double wReq, double wCont, double wMooc, double wTruck){
		double minW = wTruck < wMooc ? wTruck : wMooc;
		if(wReq > wCont || wCont > minW)
			return false;
		return true;
	}
	
	public boolean checkAvailableIntervalsTruck(Truck truck, 
			int startServiceTime, int endServiceTime){
		boolean avai = true;
		Intervals[] intervals = truck.getIntervals();
		for(int i = 0; i < intervals.length; i++){
			long e = DateTimeUtils.dateTime2Int(
					intervals[i].getDateStart())
					- input.getParams().getHourPrev()*3600;
			long l = DateTimeUtils.dateTime2Int(
					intervals[i].getDateEnd())
					- input.getParams().getHourPost()*3600;
			if(startServiceTime >= e && startServiceTime <= l)
				return false;
			if(endServiceTime >= e && endServiceTime <= l)
				return false;
			if(startServiceTime <= e && endServiceTime >= l)
				return false;
		}
		
		return avai;
	}
	
	public boolean checkAvailableIntervalsMooc(Mooc mooc, 
			int startServiceTime, int endServiceTime){
		boolean avai = true;
		Intervals[] intervals = mooc.getIntervals();
		for(int i = 0; i < intervals.length; i++){
			long e = DateTimeUtils.dateTime2Int(
					intervals[i].getDateStart())
					- input.getParams().getHourPrev()*3600;
			long l = DateTimeUtils.dateTime2Int(
					intervals[i].getDateEnd())
					- input.getParams().getHourPost()*3600;
			if(startServiceTime >= e && startServiceTime <= l)
				return false;
			if(endServiceTime >= e && endServiceTime <= l)
				return false;
			if(startServiceTime <= e && endServiceTime >= l)
				return false;
		}
		
		return avai;
	}

	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			Mooc mooc, Container container) {
		int startTimeTruck = mTruck2LastTime.get(truck);// (int)
														// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		// String locationTruck =
		// mCode2DepotTruck.get(truck.getDepotTruckCode())
		// .getLocationCode();
		DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		String locationTruck = depotTruck.getLocationCode();

		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		String locationContainer = mContainer2LastDepot.get(container)
				.getLocationCode();
		// System.out.println(name() +
		// "::createLastAvailableCombo, container = " + container.getCode() +
		// ", location = " + locationContainer);
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

//		if (truck.getCode().equals("Truck0001")
//				&& mooc.getCode().equals("Mooc0002")
//				&& container.getCode().equals("Container002"))
//			System.out.println(name()
//					+ "::createLastAvailableCombo, lastTimeTruck = "
//					+ DateTimeUtils.unixTimeStamp2DateTime(mTruck2LastTime
//							.get(truck))
//					+ ", departureTime = "
//					+ DateTimeUtils
//							.unixTimeStamp2DateTime(departureTimeContainer));

		double distance = getDistance(locationTruck, locationMooc)
				+ getDistance(locationMooc, locationContainer);

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(this, truck,
				mooc, container, locationContainer, startTimeTruck, departureTimeMooc,
				departureTimeContainer, null, null, distance);
		return ret;

	}

	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			String locationTruck, int departureTime, double extraDistance,
			Mooc mooc, Container container) {
		int startTimeTruck = departureTime;// mTruck2LastTime.get(truck);//
											// (int)
											// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		// String locationTruck =
		// mCode2DepotTruck.get(truck.getDepotTruckCode())
		// .getLocationCode();
		// DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		// String locationTruck = depotTruck.getLocationCode();

		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		String locationContainer = mContainer2LastDepot.get(container)
				.getLocationCode();
		// System.out.println(name() +
		// "::createLastAvailableCombo, container = " + container.getCode() +
		// ", location = " + locationContainer);
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

//		if (truck.getCode().equals("Truck0001")
//				&& mooc.getCode().equals("Mooc0002")
//				&& container.getCode().equals("Container002"))
//			System.out.println(name()
//					+ "::createLastAvailableCombo, lastTimeTruck = "
//					+ DateTimeUtils.unixTimeStamp2DateTime(mTruck2LastTime
//							.get(truck))
//					+ ", departureTime = "
//					+ DateTimeUtils
//							.unixTimeStamp2DateTime(departureTimeContainer));

		double distance = getDistance(locationTruck, locationMooc)
				+ getDistance(locationMooc, locationContainer) + extraDistance;

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(this, truck,
				mooc, container, locationContainer, startTimeTruck, departureTimeMooc,
				departureTimeContainer,
				null, null, distance);
		return ret;

	}

	public DepotMooc findDepotForReleaseMooc(Mooc mooc) {
		return mCode2DepotMooc.get(mooc.getDepotMoocCode());
	}

	public DepotTruck findDepotForReleaseTruck(Truck truck) {
		return mCode2DepotTruck.get(truck.getDepotTruckCode());
	}

	public DepotContainer findDepotForReleaseContainer(RouteElement e,
			Container container) {
		// System.out.println(name() + "::findDepotForReleaseContainer, "
		// + "depotContainerCode = " + container.getDepotContainerCode());
		// return mCode2DepotContainer.get(container.getDepotContainerCode());
		DepotContainer sel_depot = null;
		double minDistance = Integer.MAX_VALUE;
		if (container.getReturnDepotCodes() != null) {
			for (int i = 0; i < container.getReturnDepotCodes().length; i++) {
				String depotCode = container.getReturnDepotCodes()[i];
				DepotContainer depot = mCode2DepotContainer.get(depotCode);
				if (depot != null) {
					String lc = depot.getLocationCode();
					//System.out.println(name() + "::findDepotForReleaseContainer, depotContainer " + 
					//depotCode + ", locationCode = " + lc);
					double d = getDistance(e.getLocationCode(), lc);
					if (d < minDistance) {
						minDistance = d;
						sel_depot = depot;
					}
				}
			}
		}
		return sel_depot;
	}
	
	public DepotContainer findDepotForReleaseContainer(String fromLocationCode,
			Container container) {
		// System.out.println(name() + "::findDepotForReleaseContainer, "
		// + "depotContainerCode = " + container.getDepotContainerCode());
		// return mCode2DepotContainer.get(container.getDepotContainerCode());
		DepotContainer sel_depot = null;
		double minDistance = Integer.MAX_VALUE;
		if (container.getReturnDepotCodes() != null) {
			for (int i = 0; i < container.getReturnDepotCodes().length; i++) {
				String depotCode = container.getReturnDepotCodes()[i];
				DepotContainer depot = mCode2DepotContainer.get(depotCode);
				if (depot != null) {
					String lc = depot.getLocationCode();
					//System.out.println(name() + "::findDepotForReleaseContainer, depotContainer " + 
					//depotCode + ", locationCode = " + lc);
					double d = getDistance(fromLocationCode, lc);
					if (d < minDistance) {
						minDistance = d;
						sel_depot = depot;
					}
				}
			}
		}
		return sel_depot;
	}

	/*
	 * public TruckRoute createTangboImportWarehouse(Truck truck, Mooc mooc,
	 * ImportContainerRequest ir, WarehouseContainerTransportRequest wr) { //
	 * truck-mooc -> port(ir) -> warehouse(ir) -> from_warehouse(wr) -> //
	 * to_warehouse(wr) ArrayList<RouteElement> L = new
	 * ArrayList<RouteElement>(); RouteElement e0 = new RouteElement();
	 * L.add(e0); e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
	 * e0.setDepotTruck(mTruck2LastDepot.get(truck)); int departureTime =
	 * mTruck2LastTime.get(truck); mPoint2DepartureTime.put(e0, departureTime);
	 * 
	 * RouteElement e1 = new RouteElement(); L.add(e1); e1.deriveFrom(e0);
	 * e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
	 * e1.setDepotMooc(mMooc2LastDepot.get(mooc)); int arrivalTime =
	 * departureTime + getTravelTime(e0.getDepotTruck().getLocationCode(), e1
	 * .getDepotMooc().getLocationCode()); int serviceTime = MAX(arrivalTime,
	 * mMooc2LastTime.get(mooc)); int duration =
	 * e1.getDepotMooc().getPickupMoocDuration(); departureTime = serviceTime +
	 * duration; mPoint2ArrivalTime.put(e1, arrivalTime);
	 * mPoint2DepartureTime.put(e1, departureTime);
	 * 
	 * RouteElement e2 = new RouteElement(); L.add(e2); e2.deriveFrom(e1);
	 * e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
	 * e2.setContainer(mCode2Container.get(ir.getContainerCode())); Container
	 * container = e2.getContainer();
	 * e2.setPort(mCode2Port.get(ir.getPortCode())); e2.setImportRequest(ir);
	 * arrivalTime = departureTime +
	 * getTravelTime(e1.getDepotMooc().getLocationCode(), e2
	 * .getPort().getLocationCode()); if (arrivalTime >
	 * DateTimeUtils.dateTime2Int(ir .getLateDateTimePickupAtPort())) return
	 * null; serviceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
	 * .getEarlyDateTimePickupAtPort())); duration = ir.getLoadDuration();
	 * departureTime = serviceTime + duration; mPoint2ArrivalTime.put(e2,
	 * arrivalTime); mPoint2DepartureTime.put(e2, departureTime);
	 * 
	 * 
	 * RouteElement e3 = new RouteElement(); L.add(e3); e3.deriveFrom(e2);
	 * e3.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
	 * e3.setWarehouse(mCode2Warehouse.get(ir.getWareHouseCode())); arrivalTime
	 * = departureTime + getTravelTime(e2.getPort().getLocationCode(), e3
	 * .getWarehouse().getLocationCode()); if (arrivalTime >
	 * DateTimeUtils.dateTime2Int(ir .getLateDateTimeUnloadAtWarehouse()))
	 * return null;
	 * 
	 * serviceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(ir
	 * .getEarlyDateTimeUnloadAtWarehouse())); duration = 0; departureTime =
	 * serviceTime + duration; mPoint2ArrivalTime.put(e3, arrivalTime);
	 * mPoint2DepartureTime.put(e3, departureTime);
	 * 
	 * RouteElement e4 = new RouteElement(); L.add(e4); e4.deriveFrom(e3);
	 * e4.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE); arrivalTime =
	 * departureTime + ir.getUnloadDuration(); serviceTime = arrivalTime;
	 * duration = 0; departureTime = serviceTime + duration;
	 * mPoint2ArrivalTime.put(e4, arrivalTime); mPoint2DepartureTime.put(e4,
	 * departureTime);
	 * 
	 * RouteElement e5 = new RouteElement(); L.add(e5); e5.deriveFrom(e4);
	 * e5.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
	 * e5.setWarehouseRequest(wr);
	 * e5.setWarehouse(mCode2Warehouse.get(wr.getFromWarehouseCode()));
	 * arrivalTime = departureTime +
	 * getTravelTime(e4.getWarehouse().getLocationCode(), e5
	 * .getWarehouse().getLocationCode()); if (arrivalTime >
	 * DateTimeUtils.dateTime2Int(wr.getLateDateTimeLoad())) return null;
	 * 
	 * serviceTime = MAX(arrivalTime, (int)
	 * DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeLoad())); duration = 0;
	 * departureTime = serviceTime + duration; mPoint2ArrivalTime.put(e5,
	 * arrivalTime); mPoint2DepartureTime.put(e5, departureTime);
	 * 
	 * RouteElement e6 = new RouteElement(); L.add(e6); e6.deriveFrom(e5);
	 * e6.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE); arrivalTime
	 * = departureTime + wr.getLoadDuration(); serviceTime = arrivalTime;
	 * duration = 0; departureTime = serviceTime + duration;
	 * mPoint2ArrivalTime.put(e6, arrivalTime); mPoint2DepartureTime.put(e6,
	 * departureTime);
	 * 
	 * RouteElement e7 = new RouteElement(); L.add(e7); e7.deriveFrom(e6);
	 * e7.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
	 * e7.setWarehouse(mCode2Warehouse.get(wr.getToWarehouseCode()));
	 * arrivalTime = departureTime +
	 * getTravelTime(e6.getWarehouse().getLocationCode(), e7
	 * .getWarehouse().getLocationCode()); if (arrivalTime > DateTimeUtils
	 * .dateTime2Int(wr.getLateDateTimeUnload())) return null;
	 * 
	 * serviceTime = MAX(arrivalTime, (int)
	 * DateTimeUtils.dateTime2Int(wr.getEarlyDateTimeUnload())); duration = 0;
	 * departureTime = serviceTime + duration; mPoint2ArrivalTime.put(e7,
	 * arrivalTime); mPoint2DepartureTime.put(e7, departureTime);
	 * 
	 * RouteElement e8 = new RouteElement(); L.add(e8); e8.deriveFrom(e7);
	 * e8.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE); arrivalTime =
	 * departureTime + wr.getUnloadDuration(); serviceTime = arrivalTime;
	 * duration = 0; departureTime = serviceTime + duration;
	 * mPoint2ArrivalTime.put(e8, arrivalTime); mPoint2DepartureTime.put(e8,
	 * departureTime);
	 * 
	 * RouteElement e9 = new RouteElement(); L.add(e9); e9.deriveFrom(e8);
	 * e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
	 * e9.setDepotContainer(findDepotForReleaseContainer(container));
	 * arrivalTime = departureTime +
	 * getTravelTime(e8.getWarehouse().getLocationCode(), e9
	 * .getDepotContainer().getLocationCode()); serviceTime = arrivalTime;
	 * duration = e9.getDepotContainer().getDeliveryContainerDuration();
	 * departureTime = serviceTime + duration; mPoint2ArrivalTime.put(e9,
	 * arrivalTime); mPoint2DepartureTime.put(e9, departureTime);
	 * 
	 * RouteElement e10 = new RouteElement(); L.add(e10); e10.deriveFrom(e9);
	 * e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
	 * e10.setDepotMooc(findDepotForReleaseMooc(mooc)); arrivalTime =
	 * departureTime + getTravelTime(e9.getDepotContainer().getLocationCode(),
	 * e10 .getDepotMooc().getLocationCode()); serviceTime = arrivalTime;
	 * duration = e10.getDepotMooc().getDeliveryMoocDuration(); departureTime =
	 * serviceTime + duration; mPoint2ArrivalTime.put(e10, arrivalTime);
	 * mPoint2DepartureTime.put(e10, departureTime);
	 * 
	 * RouteElement e11 = new RouteElement(); L.add(e11); e11.deriveFrom(e10);
	 * e11.setAction(ActionEnum.REST_AT_DEPOT);
	 * e11.setDepotTruck(findDepotForReleaseTruck(truck)); arrivalTime =
	 * departureTime + getTravelTime(e10.getDepotMooc().getLocationCode(), e11
	 * .getDepotTruck().getLocationCode()); serviceTime = arrivalTime; duration
	 * = 0; departureTime = serviceTime + duration; mPoint2ArrivalTime.put(e11,
	 * arrivalTime); mPoint2DepartureTime.put(e11, departureTime);
	 * 
	 * TruckRoute r = new TruckRoute(); RouteElement[] e = new
	 * RouteElement[L.size()]; for (int i = 0; i < e.length; i++) e[i] =
	 * L.get(i); r.setNodes(e); r.setTruck(truck);
	 * 
	 * propagate(r);
	 * 
	 * return r;
	 * 
	 * }
	 */
	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			Mooc mooc) {
		int startTimeTruck = mTruck2LastTime.get(truck);// (int)
														// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		double distance = 0;
		// String locationTruck =
		// mCode2DepotTruck.get(truck.getDepotTruckCode())
		// .getLocationCode();
		DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		String locationTruck = depotTruck.getLocationCode();

		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		int timeMooc = mMooc2LastTime.get(mooc);
		// System.out.println(name() +
		// "::createLastAvailableCombo(locationTruck = " + locationTruck +
		// ", locationMooc = " + locationMooc);
		int arrivalTimeMooc = startTimeTruck
				+ getTravelTime(locationTruck, locationMooc);
		
		int departureTimeMooc = MAX(arrivalTimeMooc, timeMooc)
				+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();

		distance = getDistance(locationTruck, locationMooc);

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(this, truck,
				mooc, null, locationMooc, startTimeTruck, departureTimeMooc, 
				departureTimeMooc, null, null, distance);
		return ret;

	}

	public ComboContainerMoocTruck createLastAvailableCombo(Truck truck,
			String locationTruck, int departureTime, double extraDistance,
			Mooc mooc) {
		int startTimeTruck = departureTime;// mTruck2LastTime.get(truck);//
											// (int)
											// DateTimeUtils.dateTime2Int(truck.getStartWorkingTime());
		double distance = 0;
		// String locationTruck =
		// mCode2DepotTruck.get(truck.getDepotTruckCode())
		// .getLocationCode();
		// DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		// String locationTruck = depotTruck.getLocationCode();

		String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
		int timeMooc = mMooc2LastTime.get(mooc);
		int arrivalTimeMooc = startTimeTruck
				+ getTravelTime(locationTruck, locationMooc);
		int departureTimeMooc = MAX(arrivalTimeMooc, timeMooc)
				+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();

		distance = getDistance(locationTruck, locationMooc) + extraDistance;

		ComboContainerMoocTruck ret = new ComboContainerMoocTruck(this, truck,
				mooc, null, locationMooc, startTimeTruck, departureTimeMooc,
				departureTimeMooc, null, null,
				distance);
		return ret;

	}

	public ComboContainerMoocTruck findLastAvailable(Truck truck, Mooc mooc,
			Container container) {
		// find the last position where these combo are available together, and
		// is ready for serving other request
		if (mContainer2LastDepot.get(container) == null ||
				mMooc2LastDepot.get(mooc) == null){
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
			for (int i = 0; i < tr.getNodes().length - 1; i++) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						int departureTimeOfTruckMooc = departureTimePort;
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
								this, truck, mooc, null,
								locationContainer, departureTimeOfTruckMooc,
								departureTimeOfTruckMooc, 
								departureTime, e, tr,
								extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
					if (e.getMooc() == mooc && e.getContainer() == container) {
						departureTime = mPoint2DepartureTime.get(e);
						int departureTimeOfTruckMooc = departureTime;
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getDistance();// negative -> distance is
													// reduced
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								this, truck, mooc, container, locationCode,
								departureTimeOfTruckMooc, departureTimeOfTruckMooc,
								departureTime, e, tr, extraDistance);
						return ret;
					}
				} else if (e.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {
					if (e.getMooc() == mooc) {
						departureTime = mPoint2DepartureTime.get(e);
						int departureTimeOfTruckMooc = departureTime;
						locationCode = e.getLocationCode();
						String locationContainer = mContainer2LastDepot.get(
								container).getLocationCode();

						int arrivalTimeContainer = departureTime
								+ getTravelTime(locationCode, locationContainer);
						int timeContainer = mContainer2LastTime.get(container);
						departureTime = MAX(arrivalTimeContainer, timeContainer)
								+ mContainer2LastDepot.get(container)
										.getPickupContainerDuration();

						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();// negative ->
																	// distance
																	// is
																	// reduced
						extraDistance += getDistance(locationCode,
								locationContainer);

						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								this, truck, mooc, null,
								locationContainer,
								departureTimeOfTruckMooc,
								departureTimeOfTruckMooc,
								departureTime, e, tr,
								extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.RELEASE_MOOC_AT_DEPOT)) {
					departureTime = mPoint2DepartureTime.get(e);
					int departureTimeOfTruck = departureTime;
					locationCode = e.getLocationCode();
					
					double extraDistance = e.getDistance()
							- tr.getLastNode().getDistance();// negative ->
																// distance
																// is
																// reduced
					
					String locationMooc = mMooc2LastDepot.get(mooc).getLocationCode();
					int arrivalTimeMooc = departureTime
							+ getTravelTime(locationCode, locationMooc);
					int timeMooc = mMooc2LastTime.get(mooc);
					departureTime = MAX(arrivalTimeMooc, timeMooc)
							+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
					int departureTimeOfMooc = departureTime;
					extraDistance += getDistance(locationCode,
							locationMooc);
					
					String locationContainer = mContainer2LastDepot.get(
							container).getLocationCode();
					int arrivalTimeContainer = departureTime
							+ getTravelTime(locationMooc, locationContainer);
					int timeContainer = mContainer2LastTime.get(container);
					departureTime = MAX(arrivalTimeContainer, timeContainer)
							+ mContainer2LastDepot.get(container)
									.getPickupContainerDuration();

					
					extraDistance += getDistance(locationMooc,
							locationContainer);

					ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
							this, truck, null, null,
							locationContainer, 
							departureTimeOfTruck,
							departureTimeOfMooc,
							departureTime, e, tr,
							extraDistance);
					return ret;
					//return createLastAvailableCombo(truck, locationCode,
						//	departureTime, extraDistance, mooc, container);
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
								this, truck, mooc, container,
								locationContainer, 
								departureTimePort,
								departureTimePort,
								departureTime, e, tr,
								extraDistance);
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
								this, truck, mooc, container, locationCode,
								departureTime, departureTime, departureTime,
								e, tr, extraDistance);
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
			for (int i = 0; i < tr.getNodes().length; i++) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
					if (e.getMooc() == mooc) {
						// truck and mooc available, go to last depot of
						// container
						int departureTimePort = mPoint2DepartureTime.get(e);
						int departureTimeOfTruckMooc = departureTimePort;
						String locationCodePort = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								this, truck, mooc, null, locationCodePort,
								departureTimeOfTruckMooc, departureTimeOfTruckMooc,
								departureTimePort, e, tr, extraDistance);
						return ret;

					}
				} else if (e.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {
					if (e.getMooc() == mooc) {
						departureTime = mPoint2DepartureTime.get(e);
						int departureTimeOfTruckMooc = departureTime;
						locationCode = e.getLocationCode();
						double extraDistance = e.getDistance()
								- tr.getLastNode().getDistance();
						ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
								this, truck, mooc, null, locationCode,
								departureTimeOfTruckMooc, departureTimeOfTruckMooc,
								departureTime, e, tr, extraDistance);
						return ret;
					}

				} else if (e.getAction().equals(
						ActionEnum.RELEASE_MOOC_AT_DEPOT)) {
					departureTime = mPoint2DepartureTime.get(e);
					int departureTimeOfTruck = departureTime;
					locationCode = e.getLocationCode();
					double extraDistance = e.getDistance()
							- tr.getLastNode().getDistance();
					
					String locationMooc = mMooc2LastDepot.get(
							mooc).getLocationCode();
					int arrivalTimeMooc = departureTime
							+ getTravelTime(locationCode, locationMooc);
					int timeMooc = mMooc2LastTime.get(mooc);
					departureTime = MAX(arrivalTimeMooc, timeMooc)
							+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
					int departureTimeOfMooc = departureTime;
					
					extraDistance += getDistance(locationCode,
							locationMooc);
					ComboContainerMoocTruck ret = new ComboContainerMoocTruck(
							this, truck, null, null, locationMooc,
							departureTimeOfTruck, departureTimeOfMooc,
							departureTime, e, tr, extraDistance);
					return ret;
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
								this, truck, mooc, null, locationCodePort,
								departureTimePort, departureTimePort,
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
								this, truck, mooc, null, locationCode,
								departureTime, departureTime,
								departureTime, e, tr, extraDistance);
						return ret;
					}

				}
			}
			if (locationCode == null)
				return createLastAvailableCombo(truck, mooc);
		}
		return null;
	}
	
	public ComboContainerMoocTruck findLastAvailable(Truck truck) {
		// find the last position where these combo are available together, and
		// is ready for serving other request
		TruckItinerary I = mTruck2Itinerary.get(truck);
		ComboContainerMoocTruck ret = null;
		if (I == null || I.size() == 0) {
			ret = new ComboContainerMoocTruck(this, truck,
					null, null, mTruck2LastDepot.get(truck).getLocationCode(),
					mTruck2LastTime.get(truck), Integer.MAX_VALUE,
					mTruck2LastTime.get(truck),
					null, null, 0);
		} else {
			TruckRoute tr = I.getLastTruckRoute();
			// find
			String locationCode = null;
			int departureTime = -1;
			for (int i = tr.getNodes().length - 1; i >= 0; i--) {
				RouteElement e = tr.getNodes()[i];
				if (e.getAction().equals(
						ActionEnum.RELEASE_MOOC_AT_DEPOT)) {
					departureTime = mPoint2DepartureTime.get(e);
					locationCode = e.getLocationCode();
					double extraDistance = e.getDistance()
							- tr.getLastNode().getDistance();
					ret = new ComboContainerMoocTruck(this, truck,
							null, null, locationCode, 
							departureTime, Integer.MAX_VALUE, 
							departureTime,
							e, tr, extraDistance);
					return ret;
				}
			}
			if (locationCode == null)
				ret = new ComboContainerMoocTruck(this, truck,
						null, null, mTruck2LastDepot.get(truck).getLocationCode(),
						mTruck2LastTime.get(truck), Integer.MAX_VALUE,
						mTruck2LastTime.get(truck),
						null, null, 0);

		}
		return ret;
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

	/*
	 * public TruckRoute createDirectRouteForExportRequest(
	 * ExportContainerRequest req) { TruckRoute r = new TruckRoute();
	 * DepotContainer depotContainer = mCode2DepotContainer.get(req
	 * .getDepotContainerCode()); System.out.println(name() +
	 * "::createDirectRouteForExportRequest, depotContainerCode = " +
	 * req.getDepotContainerCode() + ", depotContainer = " + depotContainer);
	 * 
	 * double minDistance = Integer.MAX_VALUE; int sel_truck_index = -1; int
	 * sel_mooc_index = -1; for (int i = 0; i < input.getTrucks().length; i++) {
	 * String lt = getCurrentLocationOfTruck(input.getTrucks()[i]); for (int j =
	 * 0; j < input.getMoocs().length; j++) { DepotMooc depotMooc =
	 * mCode2DepotMooc.get(input.getMoocs()[j] .getDepotMoocCode()); String lm =
	 * depotMooc.getLocationCode(); double d = getDistance(lt, lm) +
	 * getDistance(lm, depotContainer.getLocationCode()); if (d < minDistance) {
	 * minDistance = d; sel_truck_index = i; sel_mooc_index = j; } } } Container
	 * sel_container = selectContainer(req.getContainerCategory(),
	 * req.getDepotContainerCode());
	 * 
	 * Truck sel_truck = input.getTrucks()[sel_truck_index]; Mooc sel_mooc =
	 * input.getMoocs()[sel_mooc_index]; RouteElement[] e = new RouteElement[8];
	 * for (int i = 0; i < e.length; i++) e[i] = new RouteElement();
	 * 
	 * // depart from the depot of the truck int startTime =
	 * getLastDepartureTime(sel_truck);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(sel_truck);
	 * mPoint2DepartureTime.put(e[0], startTime);
	 * 
	 * // arrive at the depot mooc, take a mooc e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(sel_mooc);
	 * 
	 * int travelTime = getTravelTime(e[0], e[1]); int arrivalTime = startTime +
	 * travelTime; int startServiceTime = arrivalTime; int duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); int departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * // arrive at the depot container e[2].deriveFrom(e[1]);
	 * e[2].setDepotContainer(depotContainer);
	 * e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
	 * e[2].setContainer(sel_container); travelTime = getTravelTime(e[1], e[2]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = e[2].getDepotContainer().getPickupContainerDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[2],
	 * arrivalTime); mPoint2DepartureTime.put(e[2], departureTime);
	 * 
	 * e[3].deriveFrom(e[2]);
	 * e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
	 * e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
	 * e[3].setExportRequest(req);
	 * 
	 * travelTime = getTravelTime(e[2], e[3]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = MAX(arrivalTime, (int)
	 * DateTimeUtils.dateTime2Int(req .getEarlyDateTimeLoadAtWarehouse())); int
	 * finishedServiceTime = startServiceTime + req.getLoadDuration(); duration
	 * = 0; departureTime = startServiceTime + duration;
	 * mPoint2ArrivalTime.put(e[3], arrivalTime); mPoint2DepartureTime.put(e[3],
	 * departureTime);
	 * 
	 * e[4].deriveFrom(e[3]);
	 * e[4].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
	 * e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE); travelTime
	 * = getTravelTime(e[3], e[4]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = MAX(arrivalTime, finishedServiceTime); duration = 0;
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[4],
	 * arrivalTime); mPoint2DepartureTime.put(e[4], departureTime);
	 * 
	 * e[5].deriveFrom(e[4]); e[5].setPort(mCode2Port.get(req.getPortCode()));
	 * e[5].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
	 * e[5].setContainer(null); e[5].setExportRequest(null); travelTime =
	 * getTravelTime(e[4], e[5]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = req.getUnloadDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[5],
	 * arrivalTime); mPoint2DepartureTime.put(e[5], departureTime);
	 * 
	 * e[6].deriveFrom(e[5]);
	 * e[6].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
	 * e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[6].setMooc(null);
	 * travelTime = getTravelTime(e[5], e[6]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[6].getDepotMooc().getDeliveryMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[6], arrivalTime);
	 * mPoint2DepartureTime.put(e[6], departureTime);
	 * 
	 * e[7].deriveFrom(e[6]);
	 * e[7].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
	 * e[7].setAction(ActionEnum.REST_AT_DEPOT);
	 * 
	 * travelTime = getTravelTime(e[6], e[7]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[7], arrivalTime);
	 * mPoint2DepartureTime.put(e[7], departureTime);
	 * 
	 * r = new TruckRoute(); r.setNodes(e); r.setTruck(sel_truck);
	 * 
	 * propagate(r);
	 * 
	 * return r; }
	 */

	public TruckRoute createDirectRouteForImportRequest(
			ImportContainerRequest req, Truck truck, Mooc mooc) {
		// truck and mooc are REST at their depots

		TruckRoute r = new TruckRoute();
		Port port = mCode2Port.get(req.getPortCode());

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();

		// ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);

		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		// depart from the depot of the truck
		int startTime = getLastDepartureTime(truck);
		L.add(e[0]);
		e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		e[0].setTruck(truck);
		mPoint2DepartureTime.put(e[0], startTime);

		// arrive at the depot mooc, take a mooc
		L.add(e[1]);
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
		L.add(e[2]);
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
		RouteElement lastElement = e[2];

		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(lastElement.getLocationCode(),
				departureTime, req, null);
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[2 * seq.length];
		int idx = -1;
		for (int i = 0; i < re.length; i++) {
			DeliveryWarehouseInfo dwi = req.getDeliveryWarehouses()[seq[i]];
			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(mCode2Warehouse.get(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(mCode2Warehouse.get(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			re[idx].setImportRequest(null);
			travelTime = getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
		}

		L.add(e[5]);
		e[5].deriveFrom(lastElement);
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

		L.add(e[6]);
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

		L.add(e[7]);
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

		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);

		propagate(r);

		return r;
	}

	public TruckRouteInfo4Request createRouteForEmptyContainerToDepotRequest(
			EmptyContainerToDepotRequest req, Truck truck, Mooc mooc,
			Container container) {

		String header = name() + "::createRouteForEmptyContainerToDepotRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return null;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		double distance = -1;

		if (combo.routeElement == null) {
			L.add(e[0]);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e[0].setDepotTruck(depotTruck);
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e[1].setDepotMooc(depotMooc);
			e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e[1].setMooc(mooc);

			travelTime = getTravelTime(e[0], e[1]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[1].getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[1], arrivalTime);
			mPoint2DepartureTime.put(e[1], departureTime);

			lastElement = e[1];
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		L.add(e[2]);
		e[2].deriveFrom(lastElement);
		e[2].setLocationCode(req.getFromLocationCode());
		e[2].setEmptyContainerToDepotRequest(req);
		e[2].setAction(ActionEnum.PICKUP_CONTAINER);
		travelTime = getTravelTime(lastElement, e[2]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateArrivalDateTime()))
			return null;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyArrivalDateTime()));
		duration = req.getAttachContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[2], arrivalTime);
		mPoint2DepartureTime.put(e[2], departureTime);

		L.add(e[3]);
		e[3].deriveFrom(e[2]);
		DepotContainer depotContainer = findDepotContainer4Deposit(req, e[2],
				container);
		e[3].setDepotContainer(depotContainer);
		e[3].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		travelTime = getTravelTime(e[2], e[3]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);
		tri.setLastDepotContainer(container, depotContainer);
		tri.setLastTimeContainer(container, departureTime);

		L.add(e[4]);
		e[4].deriveFrom(e[3]);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e[3], mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[4].setDepotMooc(depotMooc);
		e[4].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[4].setMooc(null);
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[4].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		L.add(e[5]);
		e[5].deriveFrom(e[4]);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e[4], truck);
		e[5].setDepotTruck(depotTruck);
		e[5].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[5], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		// update last depot and last time of truck
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e[5].getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_EXPORT);
		propagate(r);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRouteInfo4Request createRouteForTransportContainerRequest(
			TransportContainerRequest req, Truck truck, Mooc mooc,
			Container container) {

		String header = name()
				+ "::createRouteForEmptyContainerFromDepotRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);

		if (combo == null)
			return null;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		double distance = -1;

		if (combo.routeElement == null) {
			L.add(e[0]);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e[0].setDepotTruck(depotTruck);
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e[1].setDepotMooc(depotMooc);
			e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e[1].setMooc(mooc);

			travelTime = getTravelTime(e[0], e[1]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[1].getDepotMooc().getPickupMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[1], arrivalTime);
			mPoint2DepartureTime.put(e[1], departureTime);

			lastElement = e[1];
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		for (int i = 0; i < req.getLocations().length; i++) {
			TransportContainerLocationInfo tci = req.getLocations()[i];
			RouteElement re = new RouteElement();
			L.add(re);

			re.deriveFrom(lastElement);
			re.setTransportContainerRequest(req);
			re.setLocationCode(req.getLocations()[i].getLocationCode());
			travelTime = getTravelTime(lastElement, re);
			arrivalTime = travelTime + departureTime;
			if (arrivalTime > DateTimeUtils.dateTime2Int(tci
					.getLateArrivalTime()))
				return null;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(tci.getEarlyArrivalTime()));
			duration = req.getAttachContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(re, arrivalTime);
			mPoint2DepartureTime.put(re, departureTime);
			lastElement = re;
		}

		L.add(e[4]);
		e[4].deriveFrom(lastElement);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, lastElement, mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[4].setDepotMooc(depotMooc);
		e[4].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[4].setMooc(null);
		travelTime = getTravelTime(lastElement, e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[4].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		L.add(e[5]);
		e[5].deriveFrom(e[4]);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e[4], truck);
		e[5].setDepotTruck(depotTruck);
		e[5].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[5], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		// update last depot and last time of truck
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e[5].getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_EXPORT);
		propagate(r);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRouteInfo4Request createRouteForExportEmptyRequest(
			ExportEmptyRequests r, Truck truck, Mooc mooc) {
		String header = name() + "::createRouteForExportEmptyRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
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
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e1.setDepotMooc(depotMooc);
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

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.PICKUP_CONTAINER);
		DepotContainer depot = mCode2DepotContainer.get(r
				.getDepotContainerCode());
		e2.setExportEmptyRequest(r);
		e2.setDepotContainer(depot);

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimePickupAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimePickupAtDepot()))
				return null;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimePickupAtDepot()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkContainerDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		Warehouse wh = mCode2Warehouse.get(r.getWareHouseCode());
		e3.setAction(ActionEnum.UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e3.setWarehouse(wh);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeLoadAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeLoadAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime, (int)DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeLoadAtWarehouse()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		if (!r.isBreakRomooc()) {
			duration = r.getLinkContainerDuration();//pending: get unlinkEmptyCont
			finishedServiceTime = startServiceTime
					+ duration;
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);
			lastElement = e3;
			
			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			DepotMooc depotMooc = findDepotMooc4Deposit(e3, mooc);
			// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
			e4.setDepotMooc(depotMooc);
			e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e4.setMooc(null);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e4.getDepotMooc().getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);
			// update last depot and lastTime of mooc
			// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
			// mMooc2LastTime.put(mooc, departureTime);
			tri.setLastDepotMooc(mooc, e4.getDepotMooc());
			tri.setLastTimeMooc(mooc, departureTime);

			tri.setLastDepotMooc(mooc, e4.getDepotMooc());
			tri.setLastTimeMooc(mooc, departureTime);
			lastElement = e4;
		}
		else{
			finishedServiceTime = startServiceTime
					+ input.getParams().getCutMoocDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);
			lastElement = e3;
		}

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_EXPORT_EMPTY);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		//updateContainerAtDepot(container);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRouteInfo4Request createRouteForExportLadenRequest(
			ExportLadenRequests r, Truck truck, Mooc mooc) {
		String header = name() + "::createRouteForExportLadenRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
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
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e1.setDepotMooc(depotMooc);
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
			if(combo.mooc == null){
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(mooc);
				travelTime = getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e1, arrivalTime);
				mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		Warehouse wh = mCode2Warehouse.get(r.getWareHouseCode());
		e2.setExportLadenRequest(r);
		e2.setWarehouse(wh);

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(r.getRequestDate()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkContainerAtWarehouseDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		Port port = mCode2Port.get(r.getPortCode());
		e3.setAction(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT);
		e3.setPort(port);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeUnloadAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeUnloadAtPort()))
				return null;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeUnloadAtPort()));
		distance += getDistance(lastElement, e3);

		finishedServiceTime = startServiceTime
				+ input.getParams().getCutMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);
		lastElement = e3;

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		DepotMooc depotMooc = findDepotMooc4Deposit(e3, mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e4.setDepotMooc(depotMooc);
		e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e4.setMooc(null);
		travelTime = getTravelTime(e3, e4);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e4.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e4, arrivalTime);
		mPoint2DepartureTime.put(e4, departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);
		distance += getDistance(lastElement, e4);
		lastElement = e4;

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);
		distance += getDistance(lastElement, e5);
		
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_EXPORT_LADEN);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		//updateContainerAtDepot(container);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}
	
	public TruckRouteInfo4Request createRouteForExportLadenRequest(
			ExportLadenRequests r, Truck truck) {
		String header = name() + "::createRouteForExportLadenRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck);
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
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);
			lastElement = e0;
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		Warehouse wh = mCode2Warehouse.get(r.getWareHouseCode());
		e2.setExportLadenRequest(r);
		e2.setWarehouse(wh);

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(r.getRequestDate()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkContainerAtWarehouseDuration()
				+ input.getParams().getLinkMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		Port port = mCode2Port.get(r.getPortCode());
		e3.setAction(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT);
		e3.setPort(port);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeUnloadAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeUnloadAtPort()))
				return null;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeUnloadAtPort()));
		distance += getDistance(lastElement, e3);

		finishedServiceTime = startServiceTime
				+ input.getParams().getCutMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);
		//tri.setLastDepotContainer(container, null);//pending...getContainerCode
		//tri.setLastTimeContainer(container, Integer.MAX_VALUE);
		lastElement = e3;

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		Mooc mooc = mCode2Mooc.get(r.getMoocCode());
		DepotMooc depotMooc = findDepotMooc4Deposit(e3, mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e4.setDepotMooc(depotMooc);
		e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e4.setMooc(null);
		travelTime = getTravelTime(e3, e4);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e4.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e4, arrivalTime);
		mPoint2DepartureTime.put(e4, departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		distance += getDistance(lastElement, e4);
		lastElement = e4;

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);
		distance += getDistance(lastElement, e5);
		
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_EXPORT_LADEN);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		//updateContainerAtDepot(container);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	public TruckRouteInfo4Request createRouteForImportEmptyRequest(
			ImportEmptyRequests r, Truck truck, Mooc mooc) {
		String header = name() + "::createRouteForImportEmptyRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
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
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e1.setDepotMooc(depotMooc);
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
			if(combo.mooc == null){
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(mooc);
				travelTime = getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e1, arrivalTime);
				mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e2.setImportEmptyRequest(r);
		System.out.println(name()
				+ "::createRouteForImportEmptyRequest, warehouseCode = "
				+ r.getWareHouseCode());
		e2.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(r.getRequestDate()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkContainerDuration()
				+ input.getParams().getLinkMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		System.out.println(name()
				+ "::createRouteForImportEmptyRequest, imEmptyRequest = "
				+ r.getOrderCode() + ", containerCode = "
				+ r.getContainerCode());

		Container container = mCode2Container.get(r.getContainerCode());
		DepotContainer depot = findDepotForReleaseContainer(lastElement,
				container);
		e3.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e3.setDepotContainer(depot);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;

		startServiceTime = arrivalTime;
		distance = combo.extraDistance + getDistance(lastElement, e2);

		finishedServiceTime = startServiceTime
				+ input.getParams().getCutMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);
		tri.setLastDepotContainer(container, depot);
		tri.setLastTimeContainer(container, departureTime);
		lastElement = e3;

		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		DepotMooc depotMooc = findDepotMooc4Deposit(e3, mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e4.setDepotMooc(depotMooc);
		e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e4.setMooc(null);
		travelTime = getTravelTime(e3, e4);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e4.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e4, arrivalTime);
		mPoint2DepartureTime.put(e4, departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);
		lastElement = e4;

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_IMPORT_EMPTY);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		updateContainerAtDepot(container);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	
	public TruckRouteInfo4Request createRouteForImportEmptyRequest(
			ImportEmptyRequests r, Truck truck) {
		String header = name() + "::createRouteForImportEmptyRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck);
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
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			lastElement = e0;
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		//from truck depot to wh
		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e2.setImportEmptyRequest(r);
		System.out.println(name()
				+ "::createRouteForImportEmptyRequest, warehouseCode = "
				+ r.getWareHouseCode());
		e2.setWarehouse(mCode2Warehouse.get(r.getWareHouseCode()));

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeAttachAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(r.getRequestDate()));
		distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkContainerDuration()
				+ input.getParams().getLinkMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		//from wh to container depot
		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		System.out.println(name()
				+ "::createRouteForImportEmptyRequest, imEmptyRequest = "
				+ r.getOrderCode() + ", containerCode = "
				+ r.getContainerCode());

		Container container = mCode2Container.get(r.getContainerCode());
		DepotContainer depot = findDepotForReleaseContainer(lastElement,
				container);
		e3.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e3.setDepotContainer(depot);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeReturnEmptyAtDepot()))
				return null;

		startServiceTime = arrivalTime;
		distance += getDistance(lastElement, e3);

		finishedServiceTime = startServiceTime
				+ input.getParams().getCutMoocDuration();//fix: get unlink container
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e3, arrivalTime);
		mPoint2DepartureTime.put(e3, departureTime);
		tri.setLastDepotContainer(container, e3.getDepotContainer());
		tri.setLastTimeContainer(container, departureTime);
		lastElement = e3;

		//from container depot to mooc depot
		RouteElement e4 = new RouteElement();
		L.add(e4);
		e4.deriveFrom(e3);
		Mooc mooc = mCode2Mooc.get(r.getMoocCode());
		DepotMooc depotMooc = findDepotMooc4Deposit(e3, mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e4.setDepotMooc(depotMooc);
		e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e4.setMooc(null);
		travelTime = getTravelTime(e3, e4);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e4.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e4, arrivalTime);
		mPoint2DepartureTime.put(e4, departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e4.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		lastElement = e4;

		//from mooc depot to truck depot
		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);

		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_IMPORT_EMPTY);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		updateContainerAtDepot(container);

		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}
	
	public TruckRouteInfo4Request createRouteForImportLadenRequest(
			ImportLadenRequests r, Truck truck, Mooc mooc) {
		String header = name() + "::createRouteForImportLadenRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
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
		//double distance = -1;

		if (combo.routeElement == null) {
			RouteElement e0 = new RouteElement();
			L.add(e0);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e0.setDepotTruck(depotTruck);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(truck);
			mPoint2DepartureTime.put(e0, departureTime);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e1.setDepotMooc(depotMooc);
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
			if(combo.mooc == null){
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(mooc);
				travelTime = getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e1, arrivalTime);
				mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		RouteElement e2 = new RouteElement();
		L.add(e2);
		e2.deriveFrom(lastElement);
		e2.setAction(ActionEnum.PICKUP_CONTAINER);
		e2.setImportLadenRequest(r);
		e2.setPort(mCode2Port.get(r.getPortCode()));

		travelTime = getTravelTime(lastElement, e2);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimePickupAtPort()))
				return null;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimePickupAtPort()));
		//distance = combo.extraDistance + getDistance(lastElement, e2);

		int finishedServiceTime = startServiceTime
				+ r.getLinkLoadedContainerAtPortDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		RouteElement e3 = new RouteElement();
		L.add(e3);
		e3.deriveFrom(lastElement);
		e3.setAction(ActionEnum.UNLINK_LOADED_CONTAINER_AT_WAREHOUSE);
		Warehouse wh = mCode2Warehouse.get(r.getWareHouseCode());
		if (wh == null)
			System.out.println(name()
					+ "::createRouteForImportLadenRequest, warehouseCode "
					+ r.getWareHouseCode() + " of imLadenRequest "
					+ r.getOrderCode() + " NULL???");
		e3.setWarehouse(wh);

		travelTime = getTravelTime(lastElement, e3);
		arrivalTime = departureTime + travelTime;
		// check time
		if (r.getLateDateTimeUnloadAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(r
					.getLateDateTimeUnloadAtWarehouse()))
				return null;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeUnloadAtWarehouse()));
		
		lastElement = e3;
		if (!r.isBreakRomooc()) {
			duration = 0;//pending: unlinkContDuration
			finishedServiceTime = startServiceTime + duration;
			departureTime = finishedServiceTime;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);
			
			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(lastElement);
			DepotMooc depotMooc = findDepotMooc4Deposit(lastElement, mooc);
			// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
			e4.setDepotMooc(depotMooc);
			e4.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e4.setMooc(null);
			travelTime = getTravelTime(lastElement, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e4.getDepotMooc().getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);
			// update last depot and lastTime of mooc
			// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
			// mMooc2LastTime.put(mooc, departureTime);
			tri.setLastDepotMooc(mooc, e4.getDepotMooc());
			tri.setLastTimeMooc(mooc, departureTime);
			lastElement = e4;
		}
		else{
			finishedServiceTime = startServiceTime + input.getParams().getCutMoocDuration();
			departureTime = finishedServiceTime;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);
			tri.setLastDepotMooc(mooc, null);
			tri.setLastTimeMooc(mooc, -1);
		}

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(lastElement, truck);
		e5.setDepotTruck(depotTruck);
		e5.setAction(ActionEnum.REST_AT_DEPOT);
		e5.setMooc(null);

		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);
		// update last depot and last time of truck
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e5.getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute tr = new TruckRoute();
		tr.setNodes(e);
		tr.setTruck(truck);
		tr.setType(TruckRoute.DIRECT_IMPORT_LADEN);
		propagate(tr);
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		//updateContainerAtDepot(container);
		
		tri.route = tr;
		tri.lastUsedIndex = lastUsedIndex;
		//tri.additionalDistance = distance;

		return tri;
	}

	public TruckRouteInfo4Request createRouteForEmptyContainerFromDepotRequest(
			EmptyContainerFromDepotRequest req, Truck truck, Mooc mooc,
			Container container) {
		String header = name()
				+ "::createRouteForEmptyContainerFromDepotRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc,
				container);
		if (combo == null)
			return null;
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		double distance = -1;

		if (combo.routeElement == null) {
			L.add(e[0]);
			// depart from the depot of the truck
			// int startTime = mTruck2LastTime.get(truck);//
			// getLastDepartureTime(sel_truck);
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e[0].setDepotTruck(depotTruck);
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e[1].setDepotMooc(depotMooc);
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
			if (mContainer2LastDepot.get(container) == null) {
				System.out.println(header + ", container = "
						+ container.getCode() + " has no depot??????");
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
		e[3].deriveFrom(lastElement);
		e[3].setAction(ActionEnum.DELIVERY_CONTAINER);
		e[3].setEmptyContainerFromDepotRequest(req);
		e[3].setLocationCode(req.getToLocationCode());

		travelTime = getTravelTime(lastElement, e[3]);
		arrivalTime = departureTime + travelTime;
		// check time
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateArrivalDateTime()))
			return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyArrivalDateTime()));
		distance = combo.extraDistance + getDistance(lastElement, e[3]);

		int finishedServiceTime = startServiceTime
				+ req.getDetachLoadedMoocContainerDuration();// req.getLoadDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		L.add(e[4]);
		e[4].deriveFrom(e[3]);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e[3], mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[4].setDepotMooc(depotMooc);
		e[4].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[4].setMooc(null);
		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[4].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);
		// update last depot and lastTime of mooc
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e[4].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		L.add(e[5]);
		e[5].deriveFrom(e[4]);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e[4], truck);
		e[5].setDepotTruck(depotTruck);
		e[5].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		// update last depot and last time of truck
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
		tri.setLastDepotTruck(truck, e[5].getDepotTruck());
		tri.setLastTimeTruck(truck, departureTime);

		e = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			e[i] = L.get(i);

		TruckRoute r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);
		r.setType(TruckRoute.DIRECT_EXPORT);
		propagate(r);

		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;

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
			if(combo.mooc == null){
				RouteElement e1 = new RouteElement();
				L.add(e1);
				e1.deriveFrom(lastElement);
				e1.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(mooc);
				travelTime = getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e1, arrivalTime);
				mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}
		
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
		if (req.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimePickupAtPort()))
				return null;
		startServiceTime = arrivalTime;
		duration = req.getLoadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e2, arrivalTime);
		mPoint2DepartureTime.put(e2, departureTime);
		lastElement = e2;

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(lastElement.getLocationCode(),
				departureTime, req, null);
		int[] seq = ss.seq;
		RouteElement[] re = new RouteElement[seq.length * 2];

		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			DeliveryWarehouseInfo dwi = req.getDeliveryWarehouses()[seq[i]];

			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(getWarehouseFromCode(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(mCode2Warehouse.get(dwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			re[idx].setImportRequest(null);
			travelTime = getTravelTime(lastElement, re[idx]);
			arrivalTime = departureTime + travelTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return null;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
		}

		RouteElement e5 = new RouteElement();
		L.add(e5);
		e5.deriveFrom(lastElement);
		DepotContainer depotContainer = findDepotContainer4Deposit(req,
				lastElement.getLocationCode(), container);
		// e5.setDepotContainer(mCode2DepotContainer.get(req.getDepotContainerCode()));
		e5.setDepotContainer(depotContainer);

		e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
		e5.setContainer(null);
		travelTime = getTravelTime(lastElement, e5);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e5.getDepotContainer().getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e5, arrivalTime);
		mPoint2DepartureTime.put(e5, departureTime);
		// mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
		// mContainer2LastTime.put(e5.getContainer(), departureTime);
		tri.setLastDepotContainer(container, e5.getDepotContainer());
		tri.setLastTimeContainer(container, departureTime);

		RouteElement e6 = new RouteElement();
		L.add(e6);
		e6.deriveFrom(e5);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e5, mooc);
		e6.setDepotMooc(depotMooc);
		// e6.setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));

		e6.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e6.setMooc(mooc);
		travelTime = getTravelTime(e5, e6);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e6.getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e6, arrivalTime);
		mPoint2DepartureTime.put(e6, departureTime);
		// mMooc2LastDepot.put(mooc, e6.getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e6.getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		RouteElement e7 = new RouteElement();
		L.add(e7);
		e7.deriveFrom(e6);
		DepotTruck depotTruck = findDepotTruck4Deposit(req, e6, truck);
		// e7.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e7.setDepotTruck(depotTruck);

		e7.setAction(ActionEnum.REST_AT_DEPOT);
		travelTime = getTravelTime(e6, e7);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e7, arrivalTime);
		mPoint2DepartureTime.put(e7, departureTime);
		// mTruck2LastDepot.put(truck, e7.getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
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
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		updateContainerAtDepot(container);
		
		tri.route = r;
		tri.additionalDistance = distance;
		tri.lastUsedIndex = lastUsedIndex;

		return tri;
	}
	public double evaluateImportRequest(
			ImportContainerRequest req, Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		Port port = mCode2Port.get(req.getPortCode());

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		double distance = -1;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		distance = combo.extraDistance;

		//from mooc depot to port
		distance += getDistance(lastLocationCode, port.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, port.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimePickupAtPort()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		duration = req.getLoadDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = port.getLocationCode();


		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, req, null);
		int[] seq = ss.seq;

		Warehouse wh = null;
		for (int i = 0; i < seq.length; i++) {
			DeliveryWarehouseInfo dwi = req.getDeliveryWarehouses()[seq[i]];
			wh = mCode2Warehouse.get(dwi.getWareHouseCode());
			travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
			distance += getDistance(lastLocationCode, wh.getLocationCode());
			
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(dwi
							.getEarlyDateTimeUnloadAtWarehouse()));
			int finishedServiceTime = startServiceTime
					+ dwi.getDetachLoadedMoocContainerDuration();// req.getUnloadDuration();
			duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			lastLocationCode= wh.getLocationCode();

			arrivalTime = departureTime;

			if (dwi.getLatePickupEmptyContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(dwi
						.getLatePickupEmptyContainerAtWarehouse()))
					return Integer.MAX_VALUE;

			if (dwi.getEarlyPickupEmptyContainerAtWarehouse() != null)
				startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyPickupEmptyContainerAtWarehouse()));
			else {
				startServiceTime = arrivalTime;
			}
			duration = 0;
			departureTime = startServiceTime
					+ dwi.getAttachEmptyMoocContainerDuration();// duration;
		}
		Container container = mCode2Container.get(req.getContainerCode());
		DepotContainer depotContainer = findDepotContainer4Deposit(req,
				lastLocationCode, container);
		distance += getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		return distance;
	}


	public TruckRoute createDirectRouteForExportRequest(
			ExportContainerRequest req, int[] seq, Truck truck, Mooc mooc,
			Container container) {
		String header = name() + "::createDirectRouteForExportRequest";
		// truck, mooc, container are REST at their depots

		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[8 + 2 * seq.length];
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

		int idx = 2;
		for (int i = 0; i < seq.length; i++) {
			PickupWarehouseInfo pwh = req.getPickupWarehouses()[seq[i]];
			idx++;
			e[idx].deriveFrom(e[idx - 1]);
			Warehouse wh = mCode2Warehouse.get(pwh.getWareHouseCode());
			e[idx].setWarehouse(wh);
			e[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e[idx].setExportRequest(req);

			travelTime = getTravelTime(e[idx - 1], e[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwh
							.getEarlyDateTimeLoadAtWarehouse()));
			int finishedServiceTime = startServiceTime + pwh.getLoadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[idx], arrivalTime);
			mPoint2DepartureTime.put(e[idx], departureTime);

			idx++;
			e[idx].deriveFrom(e[idx - 1]);
			e[idx].setWarehouse(wh);
			e[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e[idx - 1], e[idx]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[idx], arrivalTime);
			mPoint2DepartureTime.put(e[idx], departureTime);
		}

		idx++;
		e[idx].deriveFrom(e[idx - 1]);
		e[idx].setPort(mCode2Port.get(req.getPortCode()));
		e[idx].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e[idx].setContainer(null);
		e[idx].setExportRequest(null);
		travelTime = getTravelTime(e[idx - 1], e[idx]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = req.getUnloadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[idx], arrivalTime);
		mPoint2DepartureTime.put(e[idx], departureTime);
		// update last depot container, set to not-available
		mContainer2LastDepot.put(container, null);
		mContainer2LastTime.put(container, Integer.MAX_VALUE);

		idx++;
		e[idx].deriveFrom(e[idx - 1]);
		e[idx].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
		e[idx].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e[idx].setMooc(null);
		travelTime = getTravelTime(e[idx - 1], e[idx]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = e[idx].getDepotMooc().getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[idx], arrivalTime);
		mPoint2DepartureTime.put(e[idx], departureTime);
		// update last depot and lastTime of mooc
		mMooc2LastDepot.put(mooc, e[idx].getDepotMooc());
		mMooc2LastTime.put(mooc, departureTime);

		idx++;
		e[idx].deriveFrom(e[idx - 1]);
		e[idx].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
		e[idx].setAction(ActionEnum.REST_AT_DEPOT);

		travelTime = getTravelTime(e[idx - 1], e[idx]);
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[idx], arrivalTime);
		mPoint2DepartureTime.put(e[idx], departureTime);
		// update last depot and last time of truck
		mTruck2LastDepot.put(truck, e[idx].getDepotTruck());
		mTruck2LastTime.put(truck, departureTime);

		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(truck);

		propagate(r);

		return r;
	}

	public DepotMooc findDepotMooc4Deposit(RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}
	
	public DepotMooc findDepotMooc4Deposit(String fromLocationCode, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromLocationCode,
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotMooc findDepotMooc4Deposit(ExportContainerRequest r,
			RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotMooc findDepotMooc4Deposit(
			WarehouseContainerTransportRequest r, RouteElement fromElement,
			Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotMooc findDepotMooc4Deposit(ImportContainerRequest r,
			RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotMooc findDepotMooc4Deposit(EmptyContainerFromDepotRequest r,
			RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotMooc findDepotMooc4Deposit(EmptyContainerToDepotRequest r,
			RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotContainer findDepotContainer4Deposit(ImportContainerRequest r,
			String fromLocationCode, Container container) {
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for (String lc : r.getDepotContainerCode()) {
			DepotContainer dc = mCode2DepotContainer.get(lc);
			double d = getTravelTime(fromLocationCode,
					dc.getLocationCode());
			if (minDis > d) {
				minDis = d;
				sel_depot = dc;
			}
		}
		return sel_depot;// mCode2DepotContainer.get(container.getDepotContainerCode());
	}

	public DepotMooc findDepotMooc4Deposit(TransportContainerRequest r,
			RouteElement fromElement, Mooc mooc) {
		// return mCode2DepotMooc.get(mooc.getDepotMoocCode());
		double minDis = Integer.MAX_VALUE;
		DepotMooc sel_depot = null;
		for (String lc : mooc.getReturnDepotCodes()) {
			DepotMooc depotMooc = mCode2DepotMooc.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotMooc.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotMooc;
			}
		}
		return sel_depot;
	}

	public DepotContainer findDepotContainer4Deposit(
			EmptyContainerToDepotRequest r, RouteElement fromElement,
			Container container) {
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for (String lc : r.getReturnDepotContainerCodes()) {
			DepotContainer dc = mCode2DepotContainer.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					dc.getLocationCode());
			if (minDis > d) {
				minDis = d;
				sel_depot = dc;
			}
		}
		return sel_depot;// mCode2DepotContainer.get(container.getDepotContainerCode());
	}

	public DepotContainer findDepotContainer4Deposit(
			WarehouseContainerTransportRequest r, RouteElement fromElement,
			Container container) {
		// return mCode2DepotContainer.get(container.getDepotContainerCode());
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for (String lc : container.getReturnDepotCodes()) {
			DepotContainer depotContainer = mCode2DepotContainer.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotContainer.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotContainer;
			}
		}
		return sel_depot;
	}

	public DepotContainer findDepotContainer4Deposit(ExportContainerRequest r,
			RouteElement fromElement, Container container) {
		// return mCode2DepotContainer.get(container.getDepotContainerCode());
		double minDis = Integer.MAX_VALUE;
		DepotContainer sel_depot = null;
		for (String lc : container.getReturnDepotCodes()) {
			DepotContainer depotContainer = mCode2DepotContainer.get(lc);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotContainer.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotContainer;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(RouteElement fromElement,
			Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			if (depotTruck == null)
				System.out.println(name()
						+ "::findDepotTruck4Deposit, truck + "
						+ truck.getCode() + ", depotTruckCode " + c
						+ " NULL????");
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}
	
	public DepotTruck findDepotTruck4Deposit(String fromLocationCode,
			Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			if (depotTruck == null)
				System.out.println(name()
						+ "::findDepotTruck4Deposit, truck + "
						+ truck.getCode() + ", depotTruckCode " + c
						+ " NULL????");
			double d = getTravelTime(fromLocationCode,
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(ExportContainerRequest r,
			RouteElement fromElement, Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(
			WarehouseContainerTransportRequest r, RouteElement fromElement,
			Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(ImportContainerRequest r,
			RouteElement fromElement, Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(EmptyContainerFromDepotRequest r,
			RouteElement fromElement, Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(EmptyContainerToDepotRequest r,
			RouteElement fromElement, Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public DepotTruck findDepotTruck4Deposit(TransportContainerRequest r,
			RouteElement fromElement, Truck truck) {
		// return mCode2DepotTruck.get(truck.getDepotTruckCode());
		double minDis = Integer.MAX_VALUE;
		DepotTruck sel_depot = null;
		for (String c : truck.getReturnDepotCodes()) {
			DepotTruck depotTruck = mCode2DepotTruck.get(c);
			double d = getTravelTime(fromElement.getLocationCode(),
					depotTruck.getLocationCode());
			if (d < minDis) {
				minDis = d;
				sel_depot = depotTruck;
			}
		}
		return sel_depot;
	}

	public double evaluateExportRoute(
			ExportContainerRequest req, Truck truck, Mooc mooc,
			Container container) {
		String header = name() + "::evaluateExportRoute";
		// truck, mooc, container are REST at their depots
		if (mContainer2LastDepot.get(container) == null) {
			return Integer.MAX_VALUE;
		}

		//ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc, container);
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
		DepotTruck depotTruck = mTruck2LastDepot.get(truck);
		DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
		DepotContainer depotContainer = mCode2DepotContainer.get(req.getDepotContainerCode());
		
//		if (combo.routeElement == null) {
//			travelTime = getTravelTime(depotTruck.getLocationCode(), depotMooc.getLocationCode());
//			arrivalTime = departureTime + travelTime;
//			startServiceTime = arrivalTime;
//			duration = depotMooc.getPickupMoocDuration();
//			departureTime = startServiceTime + duration;
//			travelTime = getTravelTime(depotMooc.getLocationCode(), depotContainer.getLocationCode());
//			arrivalTime = departureTime + travelTime;
//			startServiceTime = arrivalTime;
//			duration = depotContainer.getPickupContainerDuration();
//			departureTime = startServiceTime + duration;
//		}
		distance = combo.extraDistance;
		
		Port port = getPortFromCode(req.getPortCode());
		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, req, port.getLocationCode());
		int[] seq = ss.seq;

		for (int i = 0; i < seq.length; i++) {
			PickupWarehouseInfo pwi = req.getPickupWarehouses()[seq[i]];
			Warehouse wh = mCode2Warehouse.get(pwi.getWareHouseCode());

			travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeLoadAtWarehouse()))
				return Integer.MAX_VALUE;

			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance += getDistance(lastLocationCode, wh.getLocationCode());
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
				startServiceTime = MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
			else
				startServiceTime = arrivalTime;

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();
			departureTime = finishedServiceTime;

		}
		distance += getDistance(lastLocationCode, port.getLocationCode());
		lastLocationCode = port.getLocationCode();
		arrivalTime = departureTime
				+ getTravelTime(lastLocationCode, port.getLocationCode());
		if(req.getLateDateTimeUnloadAtPort() != null){
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeUnloadAtPort()))
				return Integer.MAX_VALUE;
		}

		DepotMooc returnDepotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, returnDepotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, returnDepotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = returnDepotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = returnDepotMooc.getLocationCode();
		
		//from mooc depot to truck depot
		DepotTruck returnDepotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, returnDepotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, returnDepotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		
		return distance;
	}

	public TruckRouteInfo4Request createRouteForExportRequest(
			ExportContainerRequest req, Truck truck, Mooc mooc,
			Container container) {
		String header = name() + "::createRouteForExportRequest";
		// truck, mooc, container are REST at their depots
		if (mContainer2LastDepot.get(container) == null) {
			return null;
		}
		TruckRoute r = new TruckRoute();
		RouteElement[] e = new RouteElement[8];
		for (int i = 0; i < e.length; i++)
			e[i] = new RouteElement();

		//ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc, container);
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
			DepotTruck depotTruck = mTruck2LastDepot.get(truck);
			e[0].setDepotTruck(depotTruck);
			e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
			e[0].setTruck(truck);
			mPoint2DepartureTime.put(e[0], departureTime);

			// arrive at the depot mooc, take a mooc
			L.add(e[1]);
			e[1].deriveFrom(e[0]);
			DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
			e[1].setDepotMooc(depotMooc);
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
			if (mContainer2LastDepot.get(container) == null) {
				System.out.println(header + ", container = "
						+ container.getCode() + " has no depot??????");
			}
			travelTime = getTravelTime(e[1], e[2]);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e[2].getDepotContainer().getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e[2], arrivalTime);
			mPoint2DepartureTime.put(e[2], departureTime);

			lastElement = e[2];
			lastLocationCode = e[2].getLocationCode();
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
			if(combo.mooc == null){
				departureTime = mPoint2DepartureTime.get(lastElement);
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
				e[0].setDepotMooc(depotMooc);
				e[0].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e[0].setMooc(mooc);

				travelTime = getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e[0].getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[0], arrivalTime);
				mPoint2DepartureTime.put(e[0], departureTime);
				
				L.add(e[1]);
				e[1].deriveFrom(e[1]);
				e[1].setDepotContainer(mContainer2LastDepot.get(container));
				e[1].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[1].setContainer(container);
				if (mContainer2LastDepot.get(container) == null) {
					System.out.println(header + ", container = "
							+ container.getCode() + " has no depot??????");
				}
				travelTime = getTravelTime(e[0], e[1]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = mContainer2LastTime.get(container);
				startServiceTime = MAX(arrivalTime, timeContainer);
				duration = e[1].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[1], arrivalTime);
				mPoint2DepartureTime.put(e[1], departureTime);
				lastElement = e[1];
				lastLocationCode = e[1].getLocationCode();
			}
			else if(combo.mooc != null && combo.container == null){
				departureTime = mPoint2DepartureTime.get(lastElement);				
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				e[0].setDepotContainer(mContainer2LastDepot.get(container));
				e[0].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[0].setContainer(container);
				if (mContainer2LastDepot.get(container) == null) {
					System.out.println(header + ", container = "
							+ container.getCode() + " has no depot??????");
				}
				travelTime = getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = mContainer2LastTime.get(container);
				startServiceTime = MAX(arrivalTime, timeContainer);
				duration = e[0].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[0], arrivalTime);
				mPoint2DepartureTime.put(e[0], departureTime);
				lastElement = e[0];
				lastLocationCode = e[0].getLocationCode();
			}
		}
		
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		Port port = getPortFromCode(req.getPortCode());
		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(lastLocationCode,
				departureTime, req, port.getLocationCode());
		int[] seq = ss.seq;

		RouteElement[] re = new RouteElement[ss.seq.length * 2];
		int idx = -1;
		for (int i = 0; i < seq.length; i++) {
			idx++;
			PickupWarehouseInfo pwi = req.getPickupWarehouses()[seq[i]];
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			re[idx].setExportRequest(req);

			travelTime = getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			// check time
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeLoadAtWarehouse()))
				return null;

			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(pwi
							.getEarlyDateTimeLoadAtWarehouse()));
			distance = combo.extraDistance + getDistance(lastLocationCode, re[idx].getLocationCode());
			int finishedServiceTime = startServiceTime
					+ pwi.getDetachEmptyMoocContainerDuration();// req.getLoadDuration();
			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();

			idx++;
			re[idx] = new RouteElement();
			L.add(re[idx]);
			re[idx].deriveFrom(lastElement);
			re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
			re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(lastLocationCode, re[idx].getLocationCode());
			arrivalTime = departureTime + travelTime;
			if (pwi.getLateDateTimePickupLoadedContainerAtWarehouse() != null)
				if (arrivalTime > DateTimeUtils.dateTime2Int(pwi
						.getLateDateTimePickupLoadedContainerAtWarehouse()))
					return null;

			if (pwi.getEarlyDateTimePickupLoadedContainerAtWarehouse() != null)
				startServiceTime = MAX(
						arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimePickupLoadedContainerAtWarehouse()));
			else
				startServiceTime = arrivalTime;

			finishedServiceTime = startServiceTime
					+ pwi.getAttachLoadedMoocContainerDuration();

			// duration = 0;
			departureTime = finishedServiceTime;// startServiceTime + duration;
			mPoint2ArrivalTime.put(re[idx], arrivalTime);
			mPoint2DepartureTime.put(re[idx], departureTime);
			lastElement = re[idx];
			lastLocationCode = re[idx].getLocationCode();
		}

		L.add(e[5]);
		e[5].deriveFrom(lastElement);
		e[5].setPort(mCode2Port.get(req.getPortCode()));
		e[5].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		e[5].setContainer(null);
		e[5].setExportRequest(null);
		travelTime = getTravelTime(lastElement, e[5]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimeUnloadAtPort()))
			return null;

		startServiceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
				.dateTime2Int(req.getEarlyDateTimeUnloadAtPort()));
		duration = req.getUnloadDuration();
		departureTime = startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);
		// update last depot container, set to not-available
		//mContainer2LastDepot.put(container, null);
		//mContainer2LastTime.put(container, Integer.MAX_VALUE);
		tri.setLastDepotContainer(container, null);
		tri.setLastTimeContainer(container, Integer.MAX_VALUE);

		L.add(e[6]);
		e[6].deriveFrom(e[5]);
		DepotMooc depotMooc = findDepotMooc4Deposit(req, e[5], mooc);
		// e[6].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
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
		// mMooc2LastDepot.put(mooc, e[6].getDepotMooc());
		// mMooc2LastTime.put(mooc, departureTime);
		tri.setLastDepotMooc(mooc, e[6].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		tri.setLastDepotMooc(mooc, e[6].getDepotMooc());
		tri.setLastTimeMooc(mooc, departureTime);

		L.add(e[7]);
		e[7].deriveFrom(e[6]);
		// e[7].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
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
		// mTruck2LastDepot.put(truck, e[7].getDepotTruck());
		// mTruck2LastTime.put(truck, departureTime);
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
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		updateContainerAtDepot(container);
		
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

		// System.out.println(name() +
		// "::createRouteForWarehouseWarehouseRequest, combo = " +
		// combo.toString());
		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
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
			DepotContainer depotContainer = mCode2DepotContainer.get(container
					.getDepotContainerCode());
			if (depotContainer == null)
				return null;

			e[2].setDepotContainer(depotContainer);
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
			if(combo.mooc == null){
				departureTime = mPoint2DepartureTime.get(lastElement);
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				DepotMooc depotMooc = mMooc2LastDepot.get(mooc);
				e[0].setDepotMooc(depotMooc);
				e[0].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e[0].setMooc(mooc);

				travelTime = getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeMooc = mMooc2LastTime.get(mooc);
				startServiceTime = MAX(arrivalTime, timeMooc);
				duration = e[0].getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[0], arrivalTime);
				mPoint2DepartureTime.put(e[0], departureTime);
				
				L.add(e[1]);
				e[1].deriveFrom(e[1]);
				e[1].setDepotContainer(mContainer2LastDepot.get(container));
				e[1].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[1].setContainer(container);
		
				travelTime = getTravelTime(e[0], e[1]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = mContainer2LastTime.get(container);
				startServiceTime = MAX(arrivalTime, timeContainer);
				duration = e[1].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[1], arrivalTime);
				mPoint2DepartureTime.put(e[1], departureTime);
				lastElement = e[1];
				lastLocationCode = e[1].getLocationCode();
			}
			else if(combo.mooc != null && combo.container == null){
				departureTime = mPoint2DepartureTime.get(lastElement);				
				L.add(e[0]);
				e[0].deriveFrom(lastElement);
				e[0].setDepotContainer(mContainer2LastDepot.get(container));
				e[0].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e[0].setContainer(container);
				
				travelTime = getTravelTime(lastElement, e[0]);
				arrivalTime = departureTime + travelTime;
				int timeContainer = mContainer2LastTime.get(container);
				startServiceTime = MAX(arrivalTime, timeContainer);
				duration = e[0].getDepotContainer().getPickupContainerDuration();
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(e[0], arrivalTime);
				mPoint2DepartureTime.put(e[0], departureTime);
				lastElement = e[0];
				lastLocationCode = e[0].getLocationCode();
			}
		}

		L.add(e[3]);
		e[3].deriveFrom(e[2]);
		e[3].setWarehouse(pickupWarehouse);
		e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		e[3].setWarehouseRequest(req);
		travelTime = getTravelTime(lastElement, e[3]);
		distance = combo.extraDistance + getDistance(lastElement, e[3]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req.getLateDateTimeLoad()))
			return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyDateTimeLoad()));

		int finishedServiceTime = startServiceTime
				+ req.getDetachEmptyMoocContainerDurationFromWarehouse();// req.getLoadDuration();
		duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e[3], arrivalTime);
		mPoint2DepartureTime.put(e[3], departureTime);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();

		L.add(e[4]);
		e[4].deriveFrom(e[3]);
		e[4].setWarehouse(pickupWarehouse);
		e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);

		travelTime = getTravelTime(e[3], e[4]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimePickupLoadedContainerFromWarehouse()))
			return null;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimePickupLoadedContainerFromWarehouse()));// finishedServiceTime;
		duration = 0;
		departureTime = startServiceTime
				+ req.getAttachLoadedMoocContainerDurationFromWarehouse();// duration;
		mPoint2ArrivalTime.put(e[4], arrivalTime);
		mPoint2DepartureTime.put(e[4], departureTime);

		L.add(e[5]);
		e[5].deriveFrom(e[4]);
		e[5].setWarehouse(deliveryWarehouse);
		e[5].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
		travelTime = getTravelTime(e[4], e[5]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimeUnload()))
			return null;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyDateTimeUnload()));
		finishedServiceTime = startServiceTime
				+ req.getDetachLoadedMoocContainerDurationToWarehouse();// req.getUnloadDuration();
		duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		mPoint2ArrivalTime.put(e[5], arrivalTime);
		mPoint2DepartureTime.put(e[5], departureTime);

		L.add(e[6]);
		e[6].deriveFrom(e[5]);
		e[6].setWarehouse(deliveryWarehouse);
		e[6].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
		e[6].setWarehouseRequest(null);
		travelTime = getTravelTime(e[5], e[6]);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getEarlyDateTimePickupEmptyContainerToWarehouse()))
			return null;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimePickupEmptyContainerToWarehouse()));// finishedServiceTime;
		duration = 0;
		departureTime = startServiceTime
				+ req.getAttachEmptyMoocContainerDurationToWarehouse();// duration;
		mPoint2ArrivalTime.put(e[6], arrivalTime);
		mPoint2DepartureTime.put(e[6], departureTime);

		L.add(e[7]);
		e[7].deriveFrom(e[6]);
		// e[7].setDepotContainer(mCode2DepotContainer.get(container.getDepotContainerCode()));
		DepotContainer depotContainer = findDepotContainer4Deposit(req, e[6],
				container);
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
		// e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
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
		// e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
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
		updateTruckAtDepot(truck);
		updateMoocAtDepot(mooc);
		updateContainerAtDepot(container);
		
		tri.route = r;
		tri.lastUsedIndex = lastUsedIndex;
		tri.additionalDistance = distance;

		return tri;
	}

	/*
	 * public TruckRouteInfo4Request
	 * createRouteForWarehouseWarehouseRequestSegments123(
	 * WarehouseContainerTransportRequest req, Truck truck, Mooc mooc, Container
	 * container) {
	 * 
	 * Warehouse pickupWarehouse = mCode2Warehouse.get(req
	 * .getFromWarehouseCode()); Warehouse deliveryWarehouse =
	 * mCode2Warehouse.get(req .getToWarehouseCode()); String pickupLocationCode
	 * = pickupWarehouse.getLocationCode(); String deliveryLocationCode =
	 * deliveryWarehouse.getLocationCode();
	 * 
	 * ComboContainerMoocTruck combo = findLastAvailable(truck, mooc,
	 * container); if (combo == null) return null;
	 * 
	 * //System.out.println(name() +
	 * "::createRouteForWarehouseWarehouseRequest, combo = " +
	 * combo.toString()); double distance = -1;
	 * 
	 * ArrayList<RouteElement> L = new ArrayList<RouteElement>(); RouteElement
	 * lastElement = combo.routeElement; int departureTime = combo.startTime;
	 * int arrivalTime = -1; int startServiceTime = -1; int duration = -1; int
	 * lastUsedIndex = -1; int travelTime = -1; RouteElement[] e = new
	 * RouteElement[10]; for (int i = 0; i < e.length; i++) e[i] = new
	 * RouteElement();
	 * 
	 * if (combo.routeElement == null) { L.add(e[0]);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(truck);
	 * mPoint2DepartureTime.put(e[0], departureTime);
	 * 
	 * L.add(e[1]); e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(mooc);
	 * travelTime = getTravelTime(e[0], e[1]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * e[2].deriveFrom(e[1]); L.add(e[2]); DepotContainer depotContainer =
	 * mCode2DepotContainer.get(container .getDepotContainerCode());
	 * if(depotContainer == null) return null;
	 * 
	 * e[2].setDepotContainer(depotContainer);
	 * e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
	 * e[2].setContainer(container); travelTime = getTravelTime(e[1], e[2]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = e[2].getDepotContainer().getPickupContainerDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[2],
	 * arrivalTime); mPoint2DepartureTime.put(e[2], departureTime);
	 * 
	 * lastElement = e[2]; } else { TruckItinerary I = getItinerary(truck);
	 * TruckRoute tr = I.getLastTruckRoute(); lastUsedIndex =
	 * tr.indexOf(combo.routeElement); }
	 * 
	 * L.add(e[3]); e[3].deriveFrom(e[2]); e[3].setWarehouse(pickupWarehouse);
	 * e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
	 * e[3].setWarehouseRequest(req); travelTime = getTravelTime(lastElement,
	 * e[3]); distance = combo.extraDistance + getDistance(lastElement, e[3]);
	 * arrivalTime = departureTime + travelTime; if(arrivalTime >
	 * DateTimeUtils.dateTime2Int(req.getLateDateTimeLoad())) return null;
	 * 
	 * startServiceTime =
	 * MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int(req.getEarlyDateTimeLoad
	 * ()));
	 * 
	 * int finishedServiceTime = startServiceTime +
	 * req.getDetachEmptyMoocContainerDurationFromWarehouse();//
	 * req.getLoadDuration(); duration = 0; departureTime =
	 * finishedServiceTime;//startServiceTime + duration;
	 * mPoint2ArrivalTime.put(e[3], arrivalTime); mPoint2DepartureTime.put(e[3],
	 * departureTime);
	 * 
	 * TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
	 * 
	 * L.add(e[4]); e[4].deriveFrom(e[3]); e[4].setWarehouse(pickupWarehouse);
	 * e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
	 * 
	 * travelTime = getTravelTime(e[3], e[4]); arrivalTime = departureTime +
	 * travelTime; if(arrivalTime > DateTimeUtils.dateTime2Int(req.
	 * getLateDateTimePickupLoadedContainerFromWarehouse())) return null;
	 * startServiceTime = MAX(arrivalTime, (int)DateTimeUtils.dateTime2Int(req.
	 * getEarlyDateTimePickupLoadedContainerFromWarehouse
	 * ()));//finishedServiceTime; duration = 0; departureTime =
	 * startServiceTime +
	 * req.getAttachLoadedMoocContainerDurationFromWarehouse();//duration;
	 * mPoint2ArrivalTime.put(e[4], arrivalTime); mPoint2DepartureTime.put(e[4],
	 * departureTime);
	 * 
	 * L.add(e[5]); e[5].deriveFrom(e[4]); e[5].setWarehouse(deliveryWarehouse);
	 * e[5].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE); travelTime
	 * = getTravelTime(e[4], e[5]); arrivalTime = departureTime + travelTime;
	 * if(arrivalTime > DateTimeUtils.dateTime2Int(req.getLateDateTimeUnload()))
	 * return null;
	 * 
	 * startServiceTime =
	 * MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int(req.getEarlyDateTimeUnload
	 * ())); finishedServiceTime = startServiceTime +
	 * req.getDetachLoadedMoocContainerDurationToWarehouse
	 * ();//req.getUnloadDuration(); duration = 0; departureTime =
	 * finishedServiceTime;//startServiceTime + duration;
	 * mPoint2ArrivalTime.put(e[5], arrivalTime); mPoint2DepartureTime.put(e[5],
	 * departureTime);
	 * 
	 * L.add(e[6]); e[6].deriveFrom(e[5]); e[6].setWarehouse(deliveryWarehouse);
	 * e[6].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
	 * e[6].setWarehouseRequest(null); travelTime = getTravelTime(e[5], e[6]);
	 * arrivalTime = departureTime + travelTime; if(arrivalTime >
	 * DateTimeUtils.dateTime2Int
	 * (req.getEarlyDateTimePickupEmptyContainerToWarehouse())) return null;
	 * startServiceTime = MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int(req.
	 * getEarlyDateTimePickupEmptyContainerToWarehouse
	 * ()));//finishedServiceTime; duration = 0; departureTime =
	 * startServiceTime +
	 * req.getAttachEmptyMoocContainerDurationToWarehouse();//duration;
	 * mPoint2ArrivalTime.put(e[6], arrivalTime); mPoint2DepartureTime.put(e[6],
	 * departureTime);
	 * 
	 * L.add(e[7]); e[7].deriveFrom(e[6]);
	 * //e[7].setDepotContainer(mCode2DepotContainer
	 * .get(container.getDepotContainerCode())); DepotContainer depotContainer =
	 * findDepotContainer4Deposit(req, e[6], container);
	 * e[7].setDepotContainer(depotContainer);
	 * 
	 * e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
	 * e[7].setContainer(null); travelTime = getTravelTime(e[6], e[7]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = mCode2DepotContainer.get(container.getDepotContainerCode())
	 * .getDeliveryContainerDuration(); departureTime = startServiceTime +
	 * duration; mPoint2ArrivalTime.put(e[7], arrivalTime);
	 * mPoint2DepartureTime.put(e[7], departureTime);
	 * tri.setLastDepotContainer(container, e[7].getDepotContainer());
	 * tri.setLastTimeContainer(container, departureTime);
	 * 
	 * L.add(e[8]); e[8].deriveFrom(e[7]);
	 * //e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * DepotMooc depotMooc = findDepotMooc4Deposit(req, e[7], mooc);
	 * e[8].setDepotMooc(depotMooc);
	 * 
	 * e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[8].setMooc(mooc);
	 * travelTime = getTravelTime(e[7], e[8]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * mCode2DepotMooc.get(mooc.getDepotMoocCode()) .getDeliveryMoocDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[8],
	 * arrivalTime); mPoint2DepartureTime.put(e[8], departureTime);
	 * tri.setLastDepotMooc(mooc, e[8].getDepotMooc());
	 * tri.setLastTimeMooc(mooc, departureTime);
	 * 
	 * L.add(e[9]); e[9].deriveFrom(e[8]);
	 * //e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * DepotTruck depotTruck = findDepotTruck4Deposit(req, e[8], truck);
	 * e[9].setDepotTruck(depotTruck);
	 * 
	 * e[9].setAction(ActionEnum.REST_AT_DEPOT); travelTime =
	 * getTravelTime(e[8], e[9]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[9], arrivalTime);
	 * mPoint2DepartureTime.put(e[9], departureTime);
	 * tri.setLastDepotTruck(truck, e[9].getDepotTruck());
	 * tri.setLastTimeTruck(truck, departureTime);
	 * 
	 * e = new RouteElement[L.size()]; for (int i = 0; i < L.size(); i++) e[i] =
	 * L.get(i);
	 * 
	 * TruckRoute r = new TruckRoute(); r.setNodes(e); r.setTruck(truck);
	 * r.setType(TruckRoute.DIRECT_WAREHOUSE); propagate(r);
	 * 
	 * 
	 * tri.route = r; tri.lastUsedIndex = lastUsedIndex; tri.additionalDistance
	 * = distance;
	 * 
	 * return tri; } public TruckRouteInfo4Request
	 * createRouteForWarehouseWarehouseRequestSegment1(
	 * WarehouseContainerTransportRequest req, Truck truck, Mooc mooc, Container
	 * container) {
	 * 
	 * Warehouse pickupWarehouse = mCode2Warehouse.get(req
	 * .getFromWarehouseCode()); Warehouse deliveryWarehouse =
	 * mCode2Warehouse.get(req .getToWarehouseCode()); String pickupLocationCode
	 * = pickupWarehouse.getLocationCode(); String deliveryLocationCode =
	 * deliveryWarehouse.getLocationCode();
	 * 
	 * ComboContainerMoocTruck combo = findLastAvailable(truck, mooc,
	 * container); if (combo == null) return null;
	 * 
	 * //System.out.println(name() +
	 * "::createRouteForWarehouseWarehouseRequest, combo = " +
	 * combo.toString()); double distance = -1;
	 * 
	 * ArrayList<RouteElement> L = new ArrayList<RouteElement>(); RouteElement
	 * lastElement = combo.routeElement; int departureTime = combo.startTime;
	 * int arrivalTime = -1; int startServiceTime = -1; int duration = -1; int
	 * lastUsedIndex = -1; int travelTime = -1; RouteElement[] e = new
	 * RouteElement[10]; for (int i = 0; i < e.length; i++) e[i] = new
	 * RouteElement();
	 * 
	 * if (combo.routeElement == null) { L.add(e[0]);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(truck);
	 * mPoint2DepartureTime.put(e[0], departureTime);
	 * 
	 * L.add(e[1]); e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(mooc);
	 * travelTime = getTravelTime(e[0], e[1]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * e[2].deriveFrom(e[1]); L.add(e[2]); DepotContainer depotContainer =
	 * mCode2DepotContainer.get(container .getDepotContainerCode());
	 * if(depotContainer == null) return null;
	 * 
	 * e[2].setDepotContainer(depotContainer);
	 * e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
	 * e[2].setContainer(container); travelTime = getTravelTime(e[1], e[2]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = e[2].getDepotContainer().getPickupContainerDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[2],
	 * arrivalTime); mPoint2DepartureTime.put(e[2], departureTime);
	 * 
	 * lastElement = e[2]; } else { TruckItinerary I = getItinerary(truck);
	 * TruckRoute tr = I.getLastTruckRoute(); lastUsedIndex =
	 * tr.indexOf(combo.routeElement); }
	 * 
	 * L.add(e[3]); e[3].deriveFrom(e[2]); e[3].setWarehouse(pickupWarehouse);
	 * e[3].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
	 * e[3].setWarehouseRequest(req); travelTime = getTravelTime(lastElement,
	 * e[3]); distance = combo.extraDistance + getDistance(lastElement, e[3]);
	 * arrivalTime = departureTime + travelTime; if(arrivalTime >
	 * DateTimeUtils.dateTime2Int(req.getLateDateTimeLoad())) return null;
	 * 
	 * startServiceTime =
	 * MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int(req.getEarlyDateTimeLoad
	 * ()));
	 * 
	 * int finishedServiceTime = startServiceTime +
	 * req.getDetachEmptyMoocContainerDurationFromWarehouse();//
	 * req.getLoadDuration(); duration = 0; departureTime =
	 * finishedServiceTime;//startServiceTime + duration;
	 * mPoint2ArrivalTime.put(e[3], arrivalTime); mPoint2DepartureTime.put(e[3],
	 * departureTime);
	 * 
	 * TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
	 * 
	 * 
	 * L.add(e[8]); e[8].deriveFrom(e[3]);
	 * //e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * DepotMooc depotMooc = findDepotMooc4Deposit(req, e[7], mooc);
	 * e[8].setDepotMooc(depotMooc);
	 * 
	 * e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[8].setMooc(mooc);
	 * travelTime = getTravelTime(e[3], e[8]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * mCode2DepotMooc.get(mooc.getDepotMoocCode()) .getDeliveryMoocDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[8],
	 * arrivalTime); mPoint2DepartureTime.put(e[8], departureTime);
	 * tri.setLastDepotMooc(mooc, e[8].getDepotMooc());
	 * tri.setLastTimeMooc(mooc, departureTime);
	 * 
	 * L.add(e[9]); e[9].deriveFrom(e[8]);
	 * //e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * DepotTruck depotTruck = findDepotTruck4Deposit(req, e[8], truck);
	 * e[9].setDepotTruck(depotTruck);
	 * 
	 * e[9].setAction(ActionEnum.REST_AT_DEPOT); travelTime =
	 * getTravelTime(e[8], e[9]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[9], arrivalTime);
	 * mPoint2DepartureTime.put(e[9], departureTime);
	 * tri.setLastDepotTruck(truck, e[9].getDepotTruck());
	 * tri.setLastTimeTruck(truck, departureTime);
	 * 
	 * e = new RouteElement[L.size()]; for (int i = 0; i < L.size(); i++) e[i] =
	 * L.get(i);
	 * 
	 * TruckRoute r = new TruckRoute(); r.setNodes(e); r.setTruck(truck);
	 * r.setType(TruckRoute.DIRECT_WAREHOUSE); propagate(r);
	 * 
	 * 
	 * tri.route = r; tri.lastUsedIndex = lastUsedIndex; tri.additionalDistance
	 * = distance;
	 * 
	 * return tri; } public TruckRouteInfo4Request
	 * createRouteForWarehouseWarehouseRequestSegment2(
	 * WarehouseContainerTransportRequest req, Truck truck, Mooc mooc, Container
	 * container) {
	 * 
	 * Warehouse pickupWarehouse = mCode2Warehouse.get(req
	 * .getFromWarehouseCode()); Warehouse deliveryWarehouse =
	 * mCode2Warehouse.get(req .getToWarehouseCode()); String pickupLocationCode
	 * = pickupWarehouse.getLocationCode(); String deliveryLocationCode =
	 * deliveryWarehouse.getLocationCode();
	 * 
	 * ComboContainerMoocTruck combo = findLastAvailable(truck, mooc); if (combo
	 * == null) return null;
	 * 
	 * //System.out.println(name() +
	 * "::createRouteForWarehouseWarehouseRequest, combo = " +
	 * combo.toString()); double distance = -1;
	 * 
	 * ArrayList<RouteElement> L = new ArrayList<RouteElement>(); RouteElement
	 * lastElement = combo.routeElement; int departureTime = combo.startTime;
	 * int arrivalTime = -1; int startServiceTime = -1; int duration = -1; int
	 * lastUsedIndex = -1; int travelTime = -1; RouteElement[] e = new
	 * RouteElement[10]; for (int i = 0; i < e.length; i++) e[i] = new
	 * RouteElement();
	 * 
	 * if (combo.routeElement == null) { L.add(e[0]);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(truck);
	 * mPoint2DepartureTime.put(e[0], departureTime);
	 * 
	 * L.add(e[1]); e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(mooc);
	 * travelTime = getTravelTime(e[0], e[1]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * lastElement = e[1]; } else { TruckItinerary I = getItinerary(truck);
	 * TruckRoute tr = I.getLastTruckRoute(); lastUsedIndex =
	 * tr.indexOf(combo.routeElement); }
	 * 
	 * 
	 * TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
	 * 
	 * L.add(e[4]); e[4].deriveFrom(lastElement);
	 * e[4].setWarehouse(pickupWarehouse);
	 * e[4].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
	 * 
	 * travelTime = getTravelTime(lastElement, e[4]); arrivalTime =
	 * departureTime + travelTime; if(arrivalTime >
	 * DateTimeUtils.dateTime2Int(req
	 * .getLateDateTimePickupLoadedContainerFromWarehouse())) return null;
	 * startServiceTime = MAX(arrivalTime, (int)DateTimeUtils.dateTime2Int(req.
	 * getEarlyDateTimePickupLoadedContainerFromWarehouse
	 * ()));//finishedServiceTime; duration = 0; departureTime =
	 * startServiceTime +
	 * req.getAttachLoadedMoocContainerDurationFromWarehouse();//duration;
	 * mPoint2ArrivalTime.put(e[4], arrivalTime); mPoint2DepartureTime.put(e[4],
	 * departureTime);
	 * 
	 * L.add(e[5]); e[5].deriveFrom(e[4]); e[5].setWarehouse(deliveryWarehouse);
	 * e[5].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE); travelTime
	 * = getTravelTime(e[4], e[5]); arrivalTime = departureTime + travelTime;
	 * if(arrivalTime > DateTimeUtils.dateTime2Int(req.getLateDateTimeUnload()))
	 * return null;
	 * 
	 * startServiceTime =
	 * MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int(req.getEarlyDateTimeUnload
	 * ())); int finishedServiceTime = startServiceTime +
	 * req.getDetachLoadedMoocContainerDurationToWarehouse
	 * ();//req.getUnloadDuration(); duration = 0; departureTime =
	 * finishedServiceTime;//startServiceTime + duration;
	 * mPoint2ArrivalTime.put(e[5], arrivalTime); mPoint2DepartureTime.put(e[5],
	 * departureTime);
	 * 
	 * 
	 * L.add(e[8]); e[8].deriveFrom(e[5]);
	 * //e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * DepotMooc depotMooc = findDepotMooc4Deposit(req, e[5], mooc);
	 * e[8].setDepotMooc(depotMooc);
	 * 
	 * e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[8].setMooc(mooc);
	 * travelTime = getTravelTime(e[5], e[8]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * mCode2DepotMooc.get(mooc.getDepotMoocCode()) .getDeliveryMoocDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[8],
	 * arrivalTime); mPoint2DepartureTime.put(e[8], departureTime);
	 * tri.setLastDepotMooc(mooc, e[8].getDepotMooc());
	 * tri.setLastTimeMooc(mooc, departureTime);
	 * 
	 * L.add(e[9]); e[9].deriveFrom(e[8]);
	 * //e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * DepotTruck depotTruck = findDepotTruck4Deposit(req, e[8], truck);
	 * e[9].setDepotTruck(depotTruck);
	 * 
	 * e[9].setAction(ActionEnum.REST_AT_DEPOT); travelTime =
	 * getTravelTime(e[8], e[9]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[9], arrivalTime);
	 * mPoint2DepartureTime.put(e[9], departureTime);
	 * tri.setLastDepotTruck(truck, e[9].getDepotTruck());
	 * tri.setLastTimeTruck(truck, departureTime);
	 * 
	 * e = new RouteElement[L.size()]; for (int i = 0; i < L.size(); i++) e[i] =
	 * L.get(i);
	 * 
	 * TruckRoute r = new TruckRoute(); r.setNodes(e); r.setTruck(truck);
	 * r.setType(TruckRoute.DIRECT_WAREHOUSE); propagate(r);
	 * 
	 * 
	 * tri.route = r; tri.lastUsedIndex = lastUsedIndex; tri.additionalDistance
	 * = distance;
	 * 
	 * return tri; } public TruckRouteInfo4Request
	 * createRouteForWarehouseWarehouseRequestSegment3(
	 * WarehouseContainerTransportRequest req, Truck truck, Mooc mooc, Container
	 * container) {
	 * 
	 * Warehouse pickupWarehouse = mCode2Warehouse.get(req
	 * .getFromWarehouseCode()); Warehouse deliveryWarehouse =
	 * mCode2Warehouse.get(req .getToWarehouseCode()); String pickupLocationCode
	 * = pickupWarehouse.getLocationCode(); String deliveryLocationCode =
	 * deliveryWarehouse.getLocationCode();
	 * 
	 * ComboContainerMoocTruck combo = findLastAvailable(truck, mooc); if (combo
	 * == null) return null;
	 * 
	 * //System.out.println(name() +
	 * "::createRouteForWarehouseWarehouseRequest, combo = " +
	 * combo.toString()); double distance = -1;
	 * 
	 * ArrayList<RouteElement> L = new ArrayList<RouteElement>(); RouteElement
	 * lastElement = combo.routeElement; int departureTime = combo.startTime;
	 * int arrivalTime = -1; int startServiceTime = -1; int duration = -1; int
	 * lastUsedIndex = -1; int travelTime = -1; RouteElement[] e = new
	 * RouteElement[10]; for (int i = 0; i < e.length; i++) e[i] = new
	 * RouteElement();
	 * 
	 * if (combo.routeElement == null) { L.add(e[0]);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(truck);
	 * mPoint2DepartureTime.put(e[0], departureTime);
	 * 
	 * L.add(e[1]); e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(mooc);
	 * travelTime = getTravelTime(e[0], e[1]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * 
	 * 
	 * lastElement = e[1]; } else { TruckItinerary I = getItinerary(truck);
	 * TruckRoute tr = I.getLastTruckRoute(); lastUsedIndex =
	 * tr.indexOf(combo.routeElement); }
	 * 
	 * TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
	 * 
	 * L.add(e[6]); e[6].deriveFrom(lastElement);
	 * e[6].setWarehouse(deliveryWarehouse);
	 * e[6].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
	 * e[6].setWarehouseRequest(null); travelTime = getTravelTime(lastElement,
	 * e[6]); arrivalTime = departureTime + travelTime; if(arrivalTime >
	 * DateTimeUtils
	 * .dateTime2Int(req.getEarlyDateTimePickupEmptyContainerToWarehouse()))
	 * return null; startServiceTime =
	 * MAX(arrivalTime,(int)DateTimeUtils.dateTime2Int
	 * (req.getEarlyDateTimePickupEmptyContainerToWarehouse
	 * ()));//finishedServiceTime; duration = 0; departureTime =
	 * startServiceTime +
	 * req.getAttachEmptyMoocContainerDurationToWarehouse();//duration;
	 * mPoint2ArrivalTime.put(e[6], arrivalTime); mPoint2DepartureTime.put(e[6],
	 * departureTime);
	 * 
	 * L.add(e[7]); e[7].deriveFrom(e[6]);
	 * //e[7].setDepotContainer(mCode2DepotContainer
	 * .get(container.getDepotContainerCode())); DepotContainer depotContainer =
	 * findDepotContainer4Deposit(req, e[6], container);
	 * e[7].setDepotContainer(depotContainer);
	 * 
	 * e[7].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
	 * e[7].setContainer(null); travelTime = getTravelTime(e[6], e[7]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = mCode2DepotContainer.get(container.getDepotContainerCode())
	 * .getDeliveryContainerDuration(); departureTime = startServiceTime +
	 * duration; mPoint2ArrivalTime.put(e[7], arrivalTime);
	 * mPoint2DepartureTime.put(e[7], departureTime);
	 * tri.setLastDepotContainer(container, e[7].getDepotContainer());
	 * tri.setLastTimeContainer(container, departureTime);
	 * 
	 * L.add(e[8]); e[8].deriveFrom(e[7]);
	 * //e[8].setDepotMooc(mCode2DepotMooc.get(mooc.getDepotMoocCode()));
	 * DepotMooc depotMooc = findDepotMooc4Deposit(req, e[7], mooc);
	 * e[8].setDepotMooc(depotMooc);
	 * 
	 * e[8].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[8].setMooc(mooc);
	 * travelTime = getTravelTime(e[7], e[8]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * mCode2DepotMooc.get(mooc.getDepotMoocCode()) .getDeliveryMoocDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[8],
	 * arrivalTime); mPoint2DepartureTime.put(e[8], departureTime);
	 * tri.setLastDepotMooc(mooc, e[8].getDepotMooc());
	 * tri.setLastTimeMooc(mooc, departureTime);
	 * 
	 * L.add(e[9]); e[9].deriveFrom(e[8]);
	 * //e[9].setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
	 * DepotTruck depotTruck = findDepotTruck4Deposit(req, e[8], truck);
	 * e[9].setDepotTruck(depotTruck);
	 * 
	 * e[9].setAction(ActionEnum.REST_AT_DEPOT); travelTime =
	 * getTravelTime(e[8], e[9]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[9], arrivalTime);
	 * mPoint2DepartureTime.put(e[9], departureTime);
	 * tri.setLastDepotTruck(truck, e[9].getDepotTruck());
	 * tri.setLastTimeTruck(truck, departureTime);
	 * 
	 * e = new RouteElement[L.size()]; for (int i = 0; i < L.size(); i++) e[i] =
	 * L.get(i);
	 * 
	 * TruckRoute r = new TruckRoute(); r.setNodes(e); r.setTruck(truck);
	 * r.setType(TruckRoute.DIRECT_WAREHOUSE); propagate(r);
	 * 
	 * 
	 * tri.route = r; tri.lastUsedIndex = lastUsedIndex; tri.additionalDistance
	 * = distance;
	 * 
	 * return tri; }
	 */

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
	
	public double evaluateImportLadenRequest(
			ImportLadenRequests req, Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		Port port = mCode2Port.get(req.getPortCode());

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		double distance = -1;

		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;

		distance = combo.extraDistance + getDistance(lastLocationCode, port.getLocationCode());

		travelTime = getTravelTime(lastLocationCode, port.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimePickupAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimePickupAtPort()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		duration = req.getLinkLoadedContainerAtPortDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = port.getLocationCode();

		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());
		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		//distance += getDistance(lastLocationCode, wh.getLocationCode()); //not include
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimeUnloadAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeUnloadAtWarehouse()))
				return Integer.MAX_VALUE;
		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(req
				.getEarlyDateTimeUnloadAtWarehouse()));
		lastLocationCode = wh.getLocationCode();
		
		if(!req.isBreakRomooc()){
			departureTime = startServiceTime;//+ unlinkcont
			DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
			distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
			travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = depotMooc.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
				return Integer.MAX_VALUE;
			lastLocationCode = depotMooc.getLocationCode();
		}
		else
			departureTime = startServiceTime + input.getParams().getCutMoocDuration();
		
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		
		return distance;
	}

	public double evaluateImportEmptyRequest(
			ImportEmptyRequests req, Truck truck, Mooc mooc) {
		// truck and mooc are possibly not REST at their depots

		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		double distance = -1;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		distance = combo.extraDistance;
		
		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());
		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		distance += getDistance(lastLocationCode, wh.getLocationCode());
		
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeAttachAtWarehouse()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		int finishedServiceTime = startServiceTime
				+ req.getLinkContainerDuration();
				//+ mMooc2LastDepot.get(mooc).getPickupMoocDuration();
		departureTime = finishedServiceTime;
		lastLocationCode= wh.getLocationCode();

		RouteElement e0 = new RouteElement();
		e0.setWarehouse(wh);
		lastElement = e0;
		
		//from wh to container depot
		DepotContainer depotContainer = findDepotForReleaseContainer(lastLocationCode,
				mCode2Container.get(req.getContainerCode()));
		distance += getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeReturnEmptyAtDepot()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		int duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;	
		
		return distance;
	}
	
	public double evaluateImportEmptyRequest(
			ImportEmptyRequests req, Truck truck) {
		
		ComboContainerMoocTruck combo = findLastAvailable(truck);
		if (combo == null)
			return Integer.MAX_VALUE;
		
		double distance = -1;
		int departureTime =  combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		
		//from truck to wh
		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());
		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		distance += getDistance(lastLocationCode, wh.getLocationCode());
		
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeAttachAtWarehouse()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		
		Mooc mooc = mCode2Mooc.get(req.getMoocCode());
		int finishedServiceTime = startServiceTime
				+ input.getParams().getLinkMoocDuration();
		departureTime = finishedServiceTime;
		lastLocationCode= wh.getLocationCode();
		
		//from wh to container depot
		DepotContainer depotContainer = findDepotForReleaseContainer(lastLocationCode,
				mCode2Container.get(req.getContainerCode()));
		distance += getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;
		if (req.getLateDateTimeReturnEmptyAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeReturnEmptyAtDepot()))
				return Integer.MAX_VALUE;
		startServiceTime = arrivalTime;
		int duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;		
		
		return distance;
	}
	
	public double evaluateExportLadenRequest(
			ExportLadenRequests req, Truck truck) {
		String header = name() + "::evaluateRouteForExportLadenRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck);
		if (combo == null)
			return Integer.MAX_VALUE;

		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		
		double distance = combo.extraDistance;
		String lastLocationCode = combo.lastLocationCode;
		
		//from truck depot to wh
		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());

		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeAttachAtWarehouse()))
				return Integer.MAX_VALUE;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getRequestDate()));
		distance += getDistance(lastLocationCode, wh.getLocationCode());
		lastLocationCode = wh.getLocationCode();
		int finishedServiceTime = startServiceTime
				+ req.getLinkContainerAtWarehouseDuration()
				+ input.getParams().getLinkMoocDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;

		//from wh to port
		Port port = mCode2Port.get(req.getPortCode());
		travelTime = getTravelTime(lastLocationCode, port.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimeUnloadAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeUnloadAtPort()))
				return Integer.MAX_VALUE;

		distance += getDistance(lastLocationCode, port.getLocationCode());
		lastLocationCode = port.getLocationCode();

		//from port to mooc depot
		Mooc mooc = mCode2Mooc.get(req.getMoocCode());
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		
		return distance;
	}
	
	public double evaluateExportLadenRequest(
			ExportLadenRequests req, Truck truck, Mooc mooc) {
		
		String header = name() + "::evaluateRouteForExportLadenRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		
		double distance = combo.extraDistance;
		String lastLocationCode = combo.lastLocationCode;
		
		//from mooc depot to wh
		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());

		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimeAttachAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeAttachAtWarehouse()))
				return Integer.MAX_VALUE;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getRequestDate()));
		distance += getDistance(lastLocationCode, wh.getLocationCode());
		lastLocationCode = wh.getLocationCode();
		int finishedServiceTime = startServiceTime
				+ req.getLinkContainerAtWarehouseDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;

		//from wh to port
		Port port = mCode2Port.get(req.getPortCode());
		travelTime = getTravelTime(lastLocationCode, port.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimeUnloadAtPort() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeUnloadAtPort()))
				return Integer.MAX_VALUE;

		distance += getDistance(lastLocationCode, port.getLocationCode());
		lastLocationCode = port.getLocationCode();

		//from port to mooc depot
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		
		return distance;
	}
	
	public double evaluateExportEmptyRequest(
			ExportEmptyRequests req, Truck truck, Mooc mooc) {
		String header = name() + "::evaluateRouteForExportEmptyRequest";
		ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		if (combo == null)
			return Integer.MAX_VALUE;

		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int travelTime = -1;
		double distance = combo.extraDistance;
		String lastLocationCode = combo.lastLocationCode;

		//from mooc depot to cont depot
		DepotContainer depotCont = mCode2DepotContainer.get(req
				.getDepotContainerCode());

		travelTime = getTravelTime(lastLocationCode, depotCont.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimePickupAtDepot() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimePickupAtDepot()))
				return Integer.MAX_VALUE;

		startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(req
				.getEarlyDateTimePickupAtDepot()));
		distance += getDistance(lastLocationCode, depotCont.getLocationCode());

		int finishedServiceTime = startServiceTime
				+ req.getLinkContainerDuration();
		// duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		lastLocationCode = depotCont.getLocationCode();

		//from cont depot to wh
		Warehouse wh = mCode2Warehouse.get(req.getWareHouseCode());

		travelTime = getTravelTime(lastLocationCode, wh.getLocationCode());
		arrivalTime = departureTime + travelTime;
		// check time
		if (req.getLateDateTimeLoadAtWarehouse() != null)
			if (arrivalTime > DateTimeUtils.dateTime2Int(req
					.getLateDateTimeLoadAtWarehouse()))
				return Integer.MAX_VALUE;
		
		distance += getDistance(lastLocationCode, wh.getLocationCode());
		startServiceTime = MAX(arrivalTime, (int)DateTimeUtils.dateTime2Int(req
				.getEarlyDateTimeLoadAtWarehouse()));
		lastLocationCode = wh.getLocationCode();

		if (!req.isBreakRomooc()) {
			departureTime = startServiceTime;//+ unlinkcont
			DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
			distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
			travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = depotMooc.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
				return Integer.MAX_VALUE;
			lastLocationCode = depotMooc.getLocationCode();
		}
		else
			departureTime = startServiceTime + input.getParams().getCutMoocDuration();

		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;
		
		return distance;
	}
	
	public double evaluateDirectRouteForWarehouseWarehouseRequest(
			WarehouseContainerTransportRequest req, Truck truck, Mooc mooc,
			Container container) {

		Warehouse pickupWarehouse = mCode2Warehouse.get(req
				.getFromWarehouseCode());
		Warehouse deliveryWarehouse = mCode2Warehouse.get(req
				.getToWarehouseCode());

		double distance = 0;
		distance += getDistance(mCode2DepotTruck.get(truck.getDepotTruckCode()).getLocationCode(),
				mCode2DepotMooc.get(mooc.getDepotMoocCode()).getLocationCode());
		distance += getDistance(mCode2DepotMooc.get(mooc.getDepotMoocCode()).getLocationCode(),
				mCode2DepotContainer.get(container.getDepotContainerCode()).getLocationCode());
		distance += getDistance(mCode2DepotContainer.get(container.getDepotContainerCode()).getLocationCode(),
				pickupWarehouse.getLocationCode());
		distance += getDistance(pickupWarehouse.getLocationCode(),
				deliveryWarehouse.getLocationCode());
		distance += getDistance(deliveryWarehouse.getLocationCode(),
				mCode2DepotContainer.get(container.getDepotContainerCode()).getLocationCode());
		distance += getDistance(mCode2DepotContainer.get(container.getDepotContainerCode()).getLocationCode(),
				mCode2DepotMooc.get(mooc.getDepotMoocCode()).getLocationCode());
		distance += getDistance(mCode2DepotMooc.get(mooc.getDepotMoocCode()).getLocationCode(),
				mCode2DepotTruck.get(truck.getDepotTruckCode()).getLocationCode());
		return distance;
	}
	
	public double evaluateWarehouseWarehouseRequest(
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
			return Integer.MAX_VALUE;

		// System.out.println(name() +
		// "::createRouteForWarehouseWarehouseRequest, combo = " +
		// combo.toString());
		double distance = -1;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		String lastLocationCode = combo.lastLocationCode;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;

		if (combo.routeElement == null) {
			DepotTruck depotTruck = mCode2DepotTruck.get(truck.getDepotTruckCode());
			DepotMooc depotMooc = mCode2DepotMooc.get(mooc.getDepotMoocCode());
			travelTime = getTravelTime(depotTruck.getLocationCode(), depotMooc.getLocationCode());
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = depotMooc.getPickupMoocDuration();
			departureTime = startServiceTime + duration;

			DepotContainer depotContainer = mCode2DepotContainer.get(container
					.getDepotContainerCode());
			if (depotContainer == null)
				return Integer.MAX_VALUE;

			travelTime = getTravelTime(depotMooc.getLocationCode(), depotContainer.getLocationCode());
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = depotContainer.getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			lastLocationCode = depotContainer.getLocationCode();
		} else {
			TruckItinerary I = getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}

		travelTime = getTravelTime(lastLocationCode, pickupLocationCode);
		distance = combo.extraDistance + getDistance(lastLocationCode, pickupLocationCode);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req.getLateDateTimeLoad()))
			return Integer.MAX_VALUE;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyDateTimeLoad()));

		int finishedServiceTime = startServiceTime
				+ req.getDetachEmptyMoocContainerDurationFromWarehouse();// req.getLoadDuration();
		duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		arrivalTime = departureTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimePickupLoadedContainerFromWarehouse()))
			return Integer.MAX_VALUE;
		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req
						.getEarlyDateTimePickupLoadedContainerFromWarehouse()));// finishedServiceTime;
		duration = 0;
		departureTime = startServiceTime
				+ req.getAttachLoadedMoocContainerDurationFromWarehouse();// duration;

		travelTime = getTravelTime(pickupLocationCode, deliveryLocationCode);
		arrivalTime = departureTime + travelTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimeUnload()))
			return Integer.MAX_VALUE;

		startServiceTime = MAX(arrivalTime,
				(int) DateTimeUtils.dateTime2Int(req.getEarlyDateTimeUnload()));
		finishedServiceTime = startServiceTime
				+ req.getDetachLoadedMoocContainerDurationToWarehouse();// req.getUnloadDuration();
		duration = 0;
		departureTime = finishedServiceTime;// startServiceTime + duration;
		distance += getDistance(pickupLocationCode, deliveryLocationCode);
		arrivalTime = departureTime;
		if (arrivalTime > DateTimeUtils.dateTime2Int(req
				.getLateDateTimePickupEmptyContainerToWarehouse()))
			return Integer.MAX_VALUE;

		DepotContainer depotContainer = findDepotForReleaseContainer(deliveryLocationCode,
				container);
		distance += getDistance(lastLocationCode, depotContainer.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotContainer.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotContainer.getDeliveryContainerDuration();
		departureTime = startServiceTime + duration;
		lastLocationCode = depotContainer.getLocationCode();
		
		DepotMooc depotMooc = findDepotMooc4Deposit(lastLocationCode, mooc);
		distance += getDistance(lastLocationCode, depotMooc.getLocationCode());
		travelTime = getTravelTime(lastLocationCode, depotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = depotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc, departureTime))
			return Integer.MAX_VALUE;
		lastLocationCode = depotMooc.getLocationCode();
		
		DepotTruck depotTruck = findDepotTruck4Deposit(lastLocationCode, truck);
		distance += getDistance(lastLocationCode, depotTruck.getLocationCode());		
		travelTime = getTravelTime(lastLocationCode, depotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!checkAvailableIntervalsTruck(truck, combo.startTimeOfTruck, departureTime))
			return Integer.MAX_VALUE;

		return distance;
	}
	
	/*
	 * public TruckRoute createDirectRouteForImportRequest(
	 * ImportContainerRequest req) { TruckRoute r = new TruckRoute(); Port port
	 * = mCode2Port.get(req.getPortCode());
	 * 
	 * double minDistance = Integer.MAX_VALUE; int sel_truck_index = -1; int
	 * sel_mooc_index = -1; for (int i = 0; i < input.getTrucks().length; i++) {
	 * String lt = getCurrentLocationOfTruck(input.getTrucks()[i]); for (int j =
	 * 0; j < input.getMoocs().length; j++) { DepotMooc depotMooc =
	 * mCode2DepotMooc.get(input.getMoocs()[j] .getDepotMoocCode()); String lm =
	 * depotMooc.getLocationCode(); double d = getDistance(lt, lm) +
	 * getDistance(lm, port.getLocationCode()); if (d < minDistance) {
	 * minDistance = d; sel_truck_index = i; sel_mooc_index = j; } } } Truck
	 * sel_truck = input.getTrucks()[sel_truck_index]; Mooc sel_mooc =
	 * input.getMoocs()[sel_mooc_index]; RouteElement[] e = new RouteElement[8];
	 * for (int i = 0; i < e.length; i++) e[i] = new RouteElement();
	 * 
	 * // depart from the depot of the truck int startTime =
	 * getLastDepartureTime(sel_truck);
	 * e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
	 * e[0].setAction(ActionEnum.DEPART_FROM_DEPOT); e[0].setTruck(sel_truck);
	 * mPoint2DepartureTime.put(e[0], startTime);
	 * 
	 * // arrive at the depot mooc, take a mooc e[1].deriveFrom(e[0]);
	 * e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
	 * e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT); e[1].setMooc(sel_mooc);
	 * int travelTime = getTravelTime(e[0], e[1]); int arrivalTime = startTime +
	 * travelTime; int startServiceTime = arrivalTime; int duration =
	 * e[1].getDepotMooc().getPickupMoocDuration(); int departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[1], arrivalTime);
	 * mPoint2DepartureTime.put(e[1], departureTime);
	 * 
	 * // arrive at the depot container e[2].deriveFrom(e[1]);
	 * e[2].setPort(port); e[2].setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
	 * e[2].setImportRequest(req); travelTime = getTravelTime(e[1], e[2]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = req.getLoadDuration(); departureTime = startServiceTime +
	 * duration; mPoint2ArrivalTime.put(e[2], arrivalTime);
	 * mPoint2DepartureTime.put(e[2], departureTime);
	 * 
	 * e[3].deriveFrom(e[2]);
	 * e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
	 * e[3].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE); travelTime
	 * = getTravelTime(e[2], e[3]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(req
	 * .getEarlyDateTimeUnloadAtWarehouse())); int finishedServiceTime =
	 * startServiceTime + req.getUnloadDuration(); duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[3], arrivalTime);
	 * mPoint2DepartureTime.put(e[3], departureTime);
	 * 
	 * e[4].deriveFrom(e[3]);
	 * e[4].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
	 * e[4].setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
	 * e[4].setImportRequest(null); travelTime = getTravelTime(e[3], e[4]);
	 * arrivalTime = departureTime + travelTime; startServiceTime =
	 * MAX(arrivalTime, finishedServiceTime); duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[4], arrivalTime);
	 * mPoint2DepartureTime.put(e[4], departureTime);
	 * 
	 * e[5].deriveFrom(e[4]);
	 * e[5].setDepotContainer(mCode2DepotContainer.get(req
	 * .getDepotContainerCode()));
	 * e[5].setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
	 * e[5].setContainer(null); travelTime = getTravelTime(e[4], e[5]);
	 * arrivalTime = departureTime + travelTime; startServiceTime = arrivalTime;
	 * duration = e[5].getDepotContainer().getDeliveryContainerDuration();
	 * departureTime = startServiceTime + duration; mPoint2ArrivalTime.put(e[5],
	 * arrivalTime); mPoint2DepartureTime.put(e[5], departureTime);
	 * 
	 * e[6].deriveFrom(e[5]);
	 * e[6].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
	 * e[6].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT); e[6].setMooc(sel_mooc);
	 * travelTime = getTravelTime(e[5], e[6]); arrivalTime = departureTime +
	 * travelTime; startServiceTime = arrivalTime; duration =
	 * e[6].getDepotMooc().getDeliveryMoocDuration(); departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[6], arrivalTime);
	 * mPoint2DepartureTime.put(e[6], departureTime);
	 * 
	 * e[7].deriveFrom(e[6]);
	 * e[7].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
	 * e[7].setAction(ActionEnum.REST_AT_DEPOT); travelTime =
	 * getTravelTime(e[6], e[7]); arrivalTime = departureTime + travelTime;
	 * startServiceTime = arrivalTime; duration = 0; departureTime =
	 * startServiceTime + duration; mPoint2ArrivalTime.put(e[7], arrivalTime);
	 * mPoint2DepartureTime.put(e[7], departureTime);
	 * 
	 * r = new TruckRoute(); r.setNodes(e); r.setTruck(sel_truck);
	 * 
	 * propagate(r);
	 * 
	 * return r; }
	 */

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
		// System.out.println(name() + "::propagateDistance, nodes.sz = "+
		// e.length);
		// for(int i = 0; i < e.length; i++)
		// System.out.println(name() + "::propagateDistance, e[" + i + "] = " +
		// e[i].getLocationCode());

		e[0].setDistance(0);
		for (int i = 1; i < e.length; i++) {
			// System.out.println(name() + "::propagateDistance, e[" + (i-1) +
			// "] = " + e[i-1].getLocationCode() +
			// ", e[" + i + "] = " + e[i].getLocationCode());
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
		if (lc1 == null || lc2 == null)
			System.out.println(name() + "::getTravelTime, lc1 = " + lc1
					+ ", lc2 = " + lc2);
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

	public EmptyContainerFromDepotRequest[] getSortedEmptyContainerFromDepotRequests() {
		if (input.getEmptyContainerFromDepotRequests() == null
				|| input.getEmptyContainerFromDepotRequests().length == 0)
			return null;
		EmptyContainerFromDepotRequest[] L = new EmptyContainerFromDepotRequest[input
				.getEmptyContainerFromDepotRequests().length];

		for (int i = 0; i < L.length; i++) {
			L[i] = input.getEmptyContainerFromDepotRequests()[i];
		}
		for (int i = 0; i < L.length - 1; i++) {
			for (int j = i + 1; j < L.length; j++) {
				long ti = DateTimeUtils.dateTime2Int(L[i]
						.getLateArrivalDateTime());
				long tj = DateTimeUtils.dateTime2Int(L[j]
						.getLateArrivalDateTime());
				if (ti > tj) {
					EmptyContainerFromDepotRequest tmp = L[i];
					L[i] = L[j];
					L[j] = tmp;
				}
			}
		}
		return L;
	}

	public EmptyContainerToDepotRequest[] getSortedEmptyContainerToDepotRequests() {
		if (input.getEmptyContainerToDepotRequests() == null
				|| input.getEmptyContainerToDepotRequests().length == 0)
			return null;
		EmptyContainerToDepotRequest[] L = new EmptyContainerToDepotRequest[input
				.getEmptyContainerToDepotRequests().length];

		for (int i = 0; i < L.length; i++) {
			L[i] = input.getEmptyContainerToDepotRequests()[i];
		}
		for (int i = 0; i < L.length - 1; i++) {
			for (int j = i + 1; j < L.length; j++) {
				long ti = DateTimeUtils.dateTime2Int(L[i]
						.getLateArrivalDateTime());
				long tj = DateTimeUtils.dateTime2Int(L[j]
						.getLateArrivalDateTime());
				if (ti > tj) {
					EmptyContainerToDepotRequest tmp = L[i];
					L[i] = L[j];
					L[j] = tmp;
				}
			}
		}
		return L;
	}

	public ExportLadenRequests[] getSortedExportLadenRequests() {
		if (input.getExLadenRequests() == null
				|| input.getExLadenRequests().length == 0)
			return null;
		ExportLadenRequests[] R = new ExportLadenRequests[input
				.getExLadenRequests().length];
		for (int i = 0; i < R.length; i++) {
			R[i] = input.getExLadenRequests()[i];
		}
		for (int i = 0; i < R.length; i++) {
			int ti = (int) DateTimeUtils.dateTime2Int(R[i].getRequestDate());
			for (int j = i + 1; j < R.length; j++) {
				int tj = (int) DateTimeUtils
						.dateTime2Int(R[j].getRequestDate());
				if (ti > tj) {
					ExportLadenRequests tr = R[i];
					R[i] = R[j];
					R[j] = tr;
				}
			}
		}
		return R;
	}

	public ExportEmptyRequests[] getSortedExportEmptyRequests() {
		if (input.getExEmptyRequests() == null
				|| input.getExEmptyRequests().length == 0)
			return null;
		ExportEmptyRequests[] R = new ExportEmptyRequests[input
				.getExEmptyRequests().length];
		for (int i = 0; i < R.length; i++) {
			R[i] = input.getExEmptyRequests()[i];
		}
		for (int i = 0; i < R.length; i++) {
			int ti = (int) DateTimeUtils.dateTime2Int(R[i].getRequestDate());
			for (int j = i + 1; j < R.length; j++) {
				int tj = (int) DateTimeUtils
						.dateTime2Int(R[j].getRequestDate());
				if (ti > tj) {
					ExportEmptyRequests tr = R[i];
					R[i] = R[j];
					R[j] = tr;
				}
			}
		}
		return R;
	}

	public ImportLadenRequests[] getSortedImportLadenRequests() {
		if (input.getExLadenRequests() == null
				|| input.getExLadenRequests().length == 0)
			return null;
		ImportLadenRequests[] R = new ImportLadenRequests[input
				.getImLadenRequests().length];
		for (int i = 0; i < R.length; i++) {
			R[i] = input.getImLadenRequests()[i];
		}
		for (int i = 0; i < R.length; i++) {
			int ti = (int) DateTimeUtils.dateTime2Int(R[i].getRequestDate());
			for (int j = i + 1; j < R.length; j++) {
				int tj = (int) DateTimeUtils
						.dateTime2Int(R[j].getRequestDate());
				if (ti > tj) {
					ImportLadenRequests tr = R[i];
					R[i] = R[j];
					R[j] = tr;
				}
			}
		}
		return R;
	}

	public ImportEmptyRequests[] getSortedImportEmptyRequests() {
		if (input.getImEmptyRequests() == null
				|| input.getImEmptyRequests().length == 0)
			return null;
		ImportEmptyRequests[] R = new ImportEmptyRequests[input
				.getImEmptyRequests().length];
		for (int i = 0; i < R.length; i++) {
			R[i] = input.getImEmptyRequests()[i];
		}
		for (int i = 0; i < R.length; i++) {
			int ti = (int) DateTimeUtils.dateTime2Int(R[i].getRequestDate());
			for (int j = i + 1; j < R.length; j++) {
				int tj = (int) DateTimeUtils
						.dateTime2Int(R[j].getRequestDate());
				if (ti > tj) {
					ImportEmptyRequests tr = R[i];
					R[i] = R[j];
					R[j] = tr;
				}
			}
		}
		return R;
	}

	public TransportContainerRequest[] getSortedTransportContainerRequests() {
		if (input.getTransportContainerRequests() == null
				|| input.getTransportContainerRequests().length == 0)
			return null;
		TransportContainerRequest[] L = new TransportContainerRequest[input
				.getTransportContainerRequests().length];
		for (int i = 0; i < L.length; i++)
			L[i] = input.getTransportContainerRequests()[i];
		/*
		 * for(int i = 0; i < L.length-1; i++){ for(int j = i+1; j<L.length;
		 * j++){ long ti =
		 * DateTimeUtils.dateTime2Int(L[i].getLateArrivalDateTimeOrigin()); long
		 * tj = DateTimeUtils.dateTime2Int(L[j].getLateArrivalDateTimeOrigin());
		 * if(ti > tj){ TransportContainerRequest tmp = L[i]; L[i] = L[j]; L[j]
		 * = tmp; } } }
		 */

		return L;

	}

	public ExportContainerRequest[] getSortedExportRequests() {
		if (input.getExRequests() == null || input.getExRequests().length == 0)
			return null;
		ArrayList<ExportContainerRequest> L = new ArrayList<ExportContainerRequest>();
		for (int i = 0; i < input.getExRequests().length; i++) {
			ExportContainerTruckMoocRequest R = input.getExRequests()[i];
			if (R.getContainerRequest() == null
					|| R.getContainerRequest().length == 0)
				continue;
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ExportContainerRequest r = R.getContainerRequest()[j];
				r.setOrderID(R.getOrderID());
				r.setOrderCode(R.getOrderCode());
				L.add(r);
			}
		}
		if (L.size() == 0)
			return null;
		ExportContainerRequest[] SL = new ExportContainerRequest[L.size()];
		for (int i = 0; i < SL.length; i++)
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
		if (input.getImRequests() == null || input.getImRequests().length == 0)
			return null;
		ArrayList<ImportContainerRequest> L = new ArrayList<ImportContainerRequest>();
		for (int i = 0; i < input.getImRequests().length; i++) {
			ImportContainerTruckMoocRequest R = input.getImRequests()[i];
			if (R.getContainerRequest() == null
					|| R.getContainerRequest().length == 0)
				continue;
			for (int j = 0; j < R.getContainerRequest().length; j++) {
				ImportContainerRequest r = R.getContainerRequest()[j];
				r.setOrderID(R.getOrderID());
				r.setOrderCode(R.getOrderCode());
				L.add(r);
			}
		}
		if (L.size() == 0)
			return null;
		ImportContainerRequest[] SL = new ImportContainerRequest[L.size()];
		for (int i = 0; i < L.size(); i++)
			SL[i] = L.get(i);
		for (int i = 0; i < SL.length; i++) {
			for (int j = i + 1; j < SL.length; j++) {
				int ti = (int) DateTimeUtils.dateTime2Int(SL[i]
						.getEarlyDateTimePickupAtPort());
				int tj = (int) DateTimeUtils.dateTime2Int(SL[j]
						.getEarlyDateTimePickupAtPort());
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
		if (input.getWarehouseRequests() == null
				|| input.getWarehouseRequests().length == 0)
			return null;
		ArrayList<WarehouseContainerTransportRequest> L = new ArrayList<WarehouseContainerTransportRequest>();
		for (int i = 0; i < input.getWarehouseRequests().length; i++) {
			WarehouseTransportRequest R = input.getWarehouseRequests()[i];
			if (R.getWarehouseContainerTransportRequests() == null
					|| R.getWarehouseContainerTransportRequests().length == 0)
				continue;
			for (int j = 0; j < R.getWarehouseContainerTransportRequests().length; j++) {
				WarehouseContainerTransportRequest r = R
						.getWarehouseContainerTransportRequests()[j];
				L.add(r);
			}
		}
		if (L.size() == 0)
			return null;
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
	
	public ArrayList<Container> getAvailableContainerAtDepot(double wReq, String requestContainerType, String depot){
		ArrayList<Container> availableContList = new ArrayList<Container>();
		ArrayList<Container> contListAtDepot = mDepot2ContainerList.get(depot);
		if(!depot.equals("isScheduled")){
			for(int i = 0; i < contListAtDepot.size(); i++){
				Container c = contListAtDepot.get(i);
				DepotContainer c1 = mContainer2LastDepot.get(c);
				if(c.getWeight() >= wReq && requestContainerType.equals(c.getCategoryCode())
						&& mContainer2LastDepot.get(c) != null){
					availableContList.add(c);
					break;
				}
			}
		}
		else{
			for(int i = 0; i < contListAtDepot.size(); i++){
				Container c = contListAtDepot.get(i);
				DepotContainer c1 = mContainer2LastDepot.get(c);
				if(c.getWeight() >= wReq && requestContainerType.equals(c.getCategoryCode())
						&& mContainer2LastDepot.get(c) != null){
					availableContList.add(c);
				}
			}
		}
		return availableContList;
	}
	
	public ArrayList<Mooc> getAvailableMoocAtDepot(double wReq, String contType, String depot){
		ArrayList<Mooc> availableMoocList = new ArrayList<Mooc>();
		ArrayList<Mooc> moocListAtDepot = mDepot2MoocList.get(depot);
		if(!depot.equals("isScheduled")){
			for(int i = 0; i < moocListAtDepot.size(); i++){
				Mooc mooc = moocListAtDepot.get(i);
				boolean contTypeinMoocGroup = false;
				MoocGroup[] moocGroup = input.getMoocGroup();
				for(int j = 0; j < moocGroup.length; j++){
					MoocGroup group = moocGroup[j];
					if(group.getCode().equals(mooc.getCategory())){
						for(int k = 0; k < group.getPacking().length; k++){
							if(group.getPacking()[k].getContTypeCode().equals(contType)){
								contTypeinMoocGroup = true;
								break;
							}
						}
						break;
					}
				}
				if(mooc.getWeight() > wReq && contTypeinMoocGroup
						&& mMooc2LastDepot.get(mooc) != null){
					availableMoocList.add(mooc);
					break;
				}
			}
		}
		else{
			for(int i = 0; i < moocListAtDepot.size(); i++){
				Mooc mooc = moocListAtDepot.get(i);
				boolean contTypeinMoocGroup = false;
				MoocGroup[] moocGroup = input.getMoocGroup();
				for(int j = 0; j < moocGroup.length; j++){
					MoocGroup group = moocGroup[j];
					if(group.getCode().equals(mooc.getCategory())){
						for(int k = 0; k < group.getPacking().length; k++){
							if(group.getPacking()[k].getContTypeCode().equals(contType)){
								contTypeinMoocGroup = true;
								break;
							}
						}
						break;
					}
				}
				if(mooc.getWeight() >= wReq && contTypeinMoocGroup
						&& mMooc2LastDepot.get(mooc) != null){
					availableMoocList.add(mooc);
				}
			}
		}
		return availableMoocList;
	}
	
	public ArrayList<Mooc> getAvailableMoocAtDepotForKep(double wCont, String depot){
		ArrayList<Mooc> availableMoocList = new ArrayList<Mooc>();
		ArrayList<Mooc> moocListAtDepot = mDepot2MoocList.get(depot);
		if(!depot.equals("isScheduled")){
			for(int i = 0; i < moocListAtDepot.size(); i++){
				Mooc mooc = moocListAtDepot.get(i);
				boolean contTypeinMoocGroup = false;
				MoocGroup[] moocGroup = input.getMoocGroup();
				for(int j = 0; j < moocGroup.length; j++){
					MoocGroup group = moocGroup[j];
					if(group.getCode().equals(mooc.getCategory())){
						for(int k = 0; k < group.getPacking().length; k++){
							if(group.getPacking()[k].getContTypeCode().contains(ContainerCategoryEnum.CATEGORY40)||
									group.getPacking()[k].getContTypeCode().contains(ContainerCategoryEnum.CATEGORY45)){
								contTypeinMoocGroup = true;
								break;
							}
						}
						break;
					}
				}
				if(mooc.getWeight() >= wCont && contTypeinMoocGroup
						&& mMooc2LastDepot.get(mooc) != null){
					availableMoocList.add(mooc);
					break;
				}
			}
		}
		else{
			for(int i = 0; i < moocListAtDepot.size(); i++){
				Mooc mooc = moocListAtDepot.get(i);
				boolean contTypeinMoocGroup = false;
				MoocGroup[] moocGroup = input.getMoocGroup();
				for(int j = 0; j < moocGroup.length; j++){
					MoocGroup group = moocGroup[j];
					if(group.getCode().equals(mooc.getCategory())){
						for(int k = 0; k < group.getPacking().length; k++){
							if(group.getPacking()[k].getContTypeCode().contains(ContainerCategoryEnum.CATEGORY40)
							|| group.getPacking()[k].getContTypeCode().contains(ContainerCategoryEnum.CATEGORY45)){
								contTypeinMoocGroup = true;
								break;
							}
						}
						break;
					}
				}
				if(mooc.getWeight() >= wCont && contTypeinMoocGroup
						&& mMooc2LastDepot.get(mooc) != null){
					availableMoocList.add(mooc);
				}
			}
		}
		return availableMoocList;
	}
	
	public ArrayList<Truck> getAvailableTruckAtDepot(double wReq, String depot){
		ArrayList<Truck> availableTruckList = new ArrayList<Truck>();
		ArrayList<Truck> truckListAtDepot = mDepot2TruckList.get(depot);
		if(!depot.equals("isScheduled")){
			for(int i = 0; i < truckListAtDepot.size(); i++){
				if(truckListAtDepot.get(i).getWeight() >= wReq){			
					availableTruckList.add(truckListAtDepot.get(0));
					break;
				}
			}
		}
		else{
			for(int i = 0; i < truckListAtDepot.size(); i++){
				if(truckListAtDepot.get(i).getWeight() >= wReq)
					availableTruckList.add(truckListAtDepot.get(i));
			}
		}
		return availableTruckList;
	}

	public void updateTruckAtDepot(Truck truck){
		String depotTruckCode = truck.getDepotTruckCode();
		if(depotTruckCode == null)
			return;
		ArrayList<Truck> availableTruckAtDepot = mDepot2TruckList.get(depotTruckCode);
		if(availableTruckAtDepot.contains(truck)){
			availableTruckAtDepot.remove(truck);
			mDepot2TruckList.put(depotTruckCode, availableTruckAtDepot);
			ArrayList<Truck> truckIsScheduled = mDepot2TruckList.get("isScheduled");
			truckIsScheduled.add(truck);
			mDepot2TruckList.put("isScheduled", truckIsScheduled);
		}
	}
	
	public void updateMoocAtDepot(Mooc mooc){
		String depotMoocCode = mooc.getDepotMoocCode();
		if(depotMoocCode == null)
			return;
		ArrayList<Mooc> availableMoocAtDepot = mDepot2MoocList.get(depotMoocCode);
		if(availableMoocAtDepot.contains(mooc)){
			availableMoocAtDepot.remove(mooc);
			mDepot2MoocList.put(depotMoocCode, availableMoocAtDepot);
			ArrayList<Mooc> moocIsScheduled = mDepot2MoocList.get("isScheduled");
			moocIsScheduled.add(mooc);
			mDepot2MoocList.put("isScheduled", moocIsScheduled);
		}
	}
	
	public void updateContainerAtDepot(Container cont){
		String depotContainerCode = cont.getDepotContainerCode();
		if(depotContainerCode == null)
			return;
		ArrayList<Container> availableContainerAtDepot = mDepot2ContainerList.get(depotContainerCode);
		if(availableContainerAtDepot.contains(cont)){
			availableContainerAtDepot.remove(cont);
			mDepot2ContainerList.put(depotContainerCode, availableContainerAtDepot);
			ArrayList<Container> contIsScheduled = mDepot2ContainerList.get("isScheduled");
			contIsScheduled.add(cont);
			mDepot2ContainerList.put("isScheduled", contIsScheduled);
		}
	}
}
