package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.io.PrintWriter;
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
	public double[][] distance;// distance[i][j] is the distance from location index i to location index j
	public double[][] travelTime;// travelTime[i][j] is the travel time from location index i to location index j
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
	
	public void initLog(){
		try{
			log = new PrintWriter(ROOT_DIR + "/log.txt");
		}catch(Exception ex){
			ex.printStackTrace();
			log = null;
		}
	}
	public void finalizeLog(){
		if(log != null)
			log.close();
		else{
			
		}
	}
	public void log(String s){
		if(log != null)
			log.println(s);
	}
	public String mapData(){
		String ret = "OK";
		HashSet<String> s_locationCode = new HashSet<String>();
		for(int i = 0; i < input.getDistance().length; i++){
			DistanceElement e = input.getDistance()[i];
			String src = e.getSrcCode();
			String dest = e.getDestCode();
			s_locationCode.add(src);
			s_locationCode.add(dest);
		}
		locationCodes = new String[s_locationCode.size()];
		int idx = -1;
		for(String lc: s_locationCode){
			idx++;
			locationCodes[idx] = lc;
			mLocationCode2Index.put(lc, idx);
		}
		distance = new double[s_locationCode.size()][s_locationCode.size()];
		travelTime = new double[s_locationCode.size()][s_locationCode.size()];
		
		for(int i = 0; i < input.getDistance().length; i++){
			DistanceElement e = input.getDistance()[i];
			String src = e.getSrcCode();
			String dest = e.getDestCode();
			double d = e.getDistance();
			int is = mLocationCode2Index.get(src);
			int id = mLocationCode2Index.get(dest);
			distance[is][id] = d;
		
			DistanceElement et= input.getTravelTime()[i];
			src = et.getSrcCode();
			dest = et.getDestCode();
			is = mLocationCode2Index.get(src);
			id = mLocationCode2Index.get(dest);
			travelTime[is][id] = et.getDistance();
		}
		
		
		
		return ret;
	}
	
	public double getDistance(String src, String dest){
		int is = mLocationCode2Index.get(src);
		int id = mLocationCode2Index.get(dest);
		return distance[is][id];
	}
	public int getTravelTime(String src, String dest){
		int is = mLocationCode2Index.get(src);
		int id = mLocationCode2Index.get(dest);
		return (int)travelTime[is][id];
	}
	public boolean fitMoocContainer(Mooc m, Container c){
		return m.getWeight() >= c.getWeight();
	}
	public String getCurrentLocationOfTruck(Truck t){
		DepotTruck depot = mCode2DepotTruck.get(t.getDepotTruckCode());
		return depot.getLocationCode();
	}
	public TruckRoute createDirectRouteForExportRequest(ExportContainerRequest req){
		TruckRoute r = new TruckRoute();
		DepotContainer depotContainer = mCode2DepotContainer.get(req.getDepotCode());
		double minDistance = Integer.MAX_VALUE;
		int sel_truck_index = -1;
		int sel_mooc_index = -1;
		for(int i = 0; i < input.getTrucks().length; i++){
			String lt = getCurrentLocationOfTruck(input.getTrucks()[i]);
			for(int j = 0; j < input.getMoocs().length; j++){
				DepotMooc depotMooc = mCode2DepotMooc.get(input.getMoocs()[j].getDepotMoocCode());
				String lm = depotMooc.getLocationCode();
				double d = getDistance(lt, lm) + getDistance(lm, depotContainer.getLocationCode());
				if(d < minDistance){
					minDistance = d;
					sel_truck_index = i;
					sel_mooc_index = j;
				}
			}
		}
		Truck sel_truck = input.getTrucks()[sel_truck_index];
		Mooc sel_mooc = input.getMoocs()[sel_mooc_index];
		RouteElement[] e = new RouteElement[7];
		for(int i = 0; i < e.length; i++) e[i]=new RouteElement();
		
		e[0].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[0].setAction(ActionEnum.DEPART_FROM_DEPOT);
		
		e[1].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[1].setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
		
		e[2].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[2].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
		
		e[3].setWarehouse(mCode2Warehouse.get(req.getWareHouseCode()));
		e[3].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
		
		e[4].setPort(mCode2Port.get(req.getPortCode()));
		e[4].setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
		
		e[5].setDepotMooc(mCode2DepotMooc.get(sel_mooc.getDepotMoocCode()));
		e[5].setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		
		e[6].setDepotTruck(mCode2DepotTruck.get(sel_truck.getDepotTruckCode()));
		e[6].setAction(ActionEnum.REST_AT_DEPOT);
		
		
		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);
		
		return r;
	}
	public int getLastDepartureTime(Truck t){
		long dept = DateTimeUtils.dateTime2Int(t.getStartWorkingTime());
		return (int)dept;
	}
	public String name(){
		return "ContainerTruckMoocSolver";
	}
	
	public void propagateArrivalDepartureTime(TruckRoute r){
		int startTime = getLastDepartureTime(r.getTruck());
		RouteElement[] e = r.getNodes();
		mPoint2DepartureTime.put(e[0], startTime);
		for(int i = 0; i < e.length-1; i++){
			String ls = e[i].getLocationCode();
			String ld = e[i+1].getLocationCode();
			if(ls == null){
				log(name() + "::propagateArrivalDepartureTime, BUG???, e[" + i + "] has no locationCode");
				return;
			}
			if(ld == null){
				log(name() + "::propagateArrivalDepartureTime, BUG???, e[" + (i+1) + "] has no locationCode");
				return;
			}
			int t = getTravelTime(ls, ld);
			int arrivalTime = t + startTime;
			mPoint2ArrivalTime.put(e[i+1], arrivalTime);
			int duration = 0;
			int startServiceTime = arrivalTime;
			if(e[i+1].getAction().equals(ActionEnum.TAKE_MOOC_AT_DEPOT)){
				duration = e[i+1].getDepotMooc().getPickupMoocDuration();
			}else if(e[i+1].getAction().equals(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE)){
				duration = 0;
			}else if(e[i+1].getAction().equals(ActionEnum.CARRY_LOADED_CONTAINER_AT_WAREHOUSE)){
				duration = 0;
			}else if(e[i+1].getAction().equals(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE)){
				duration = input.getParams().getLinkMoocDuration();
			}else if(e[i+1].getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)){
				duration = 0;
			}else if(e[i+1].getAction().equals(ActionEnum.LINK_EMPTY_MOOC_AT_PORT)){
				duration = 0;
			}else if(e[i+1].getAction().equals(ActionEnum.RELEASE_MOOC_AT_DEPOT)){
				duration = e[i+1].getDepotMooc().getDeliveryMoocDuration();
			}else if(e[i+1].getAction().equals(ActionEnum.REST_AT_DEPOT)){
				// do nothing
			}
			
			int departureTime = startServiceTime + duration;
			mPoint2DepartureTime.put(e[i+1], departureTime);
			startTime = departureTime;
		}
	}
	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input){
		this.input = input;
		
		return null;
	}
}
