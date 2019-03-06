package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerFromDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerToDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.Intervals;
import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Permutation;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.TransportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import utils.DateTimeUtils;

public class KepGenerator {
	public ContainerTruckMoocSolver solver;
	public Truck sel_truck = null;
	public Mooc sel_mooc = null;
	public Container sel_container_a = null;
	public Container sel_container_b = null;
	public ArrayList<String> sel_whCode_a = null;
	public ArrayList<String> sel_whCode_b = null;
	public RouteElement[] sel_R;
	
	public RouteElement[] routeElements;
	public int startTime;
	public String startLocationCode;
	public boolean isStartAtDepot = false;
	
	public int extraStartTime;
	public int endTime;
	public String endLocationCode;
	
	public ExportContainerRequest exportRequest;
	public ImportContainerRequest importRequest;
	public ExportContainerRequest exportRequestKep;
	public ImportContainerRequest importRequestKep;
	public WarehouseContainerTransportRequest warehouseRequest;
	public WarehouseContainerTransportRequest warehouseRequestKep;
//	public EmptyContainerFromDepotRequest emptyContainerFromDepotRequest;
//	public EmptyContainerToDepotRequest emptyContainerToDepotRequest;
//	public TransportContainerRequest transportContainerRequest;
	public ExportLadenRequests exportLadenRequest;
	public ExportEmptyRequests exportEmptyRequest;
	public ImportLadenRequests importLadenRequest;
	public ImportEmptyRequests importEmptyRequest;
	public ExportLadenRequests exportLadenRequestKep;
	public ExportEmptyRequests exportEmptyRequestKep;
	public ImportLadenRequests importLadenRequestKep;
	public ImportEmptyRequests importEmptyRequestKep;
	
	public ArrayList<String> returnContDepotCode_a;
	public String lateDateTimeDeliveryAtDepot_a;
	
	public ArrayList<String> returnContDepotCode_b;
	public String lateDateTimeDeliveryAtDepot_b;
	
	public ArrayList<Boolean> idxKep;
	
	public KepGenerator(ContainerTruckMoocSolver solver){
		this.solver = solver;
	}
	
	public void getRequest(RouteElement e){
		if(e.getExportRequest() != null){
			if(exportRequest == null)
				exportRequest = e.getExportRequest();
			else
				exportRequestKep = e.getExportRequest();
			return;
		}
		if(e.getImportRequest() != null){
			if(importRequest == null)
				importRequest = e.getImportRequest();
			else
				importRequestKep = e.getImportRequest();
			return;
		}
		
		if(e.getWarehouseRequest() != null){
			if(warehouseRequest == null)
				warehouseRequest = e.getWarehouseRequest();
			else
				warehouseRequestKep = e.getWarehouseRequest();
			return;
		}

		if(e.getExportLadenRequest() != null){
			if(exportLadenRequest == null)
				exportLadenRequest = e.getExportLadenRequest();
			else
				exportLadenRequestKep = e.getExportLadenRequest();
			return;
		}
		
		if(e.getExportEmptyRequest() != null){
			if(exportEmptyRequest == null)
				exportEmptyRequest = e.getExportEmptyRequest();
			else
				exportEmptyRequestKep = e.getExportEmptyRequest();
			return;
		}
		
		if(e.getImportLadenRequest() != null){
			if(importLadenRequest == null)
				importLadenRequest = e.getImportLadenRequest();
			else
				importLadenRequestKep = e.getImportLadenRequest();
			return;
		}
		
		if(e.getImportEmptyRequest() != null){
			if(importEmptyRequest == null)
				importEmptyRequest = e.getImportEmptyRequest();
			else
				importEmptyRequestKep = e.getImportEmptyRequest();
			return;
		}			
	}
	
	public Measure checkRoute(
			Truck truck,
			Container container_a, Container container_b,
			int isNeedReturnCont_a, int isNeedReturnCont_b,
			ArrayList<String> returnConDepotCode_a,
			ArrayList<String> returnConDepotCode_b,
			String lateDateTimeDeliveryAtDepot_a,
			String lateDateTimeDeliveryAtDepot_b,
			RouteElement[] L){
		int departureTime = startTime;
		int arrivalTime = 0;
		int startServiceTime = 0;
		int travelTime = 0;
		int extraTime = 0;
		double distance = 0;
		String lastLocationCode = startLocationCode;
		int pre_idx_1 = -1;
		int pre_idx_2 = -1;
		boolean isRealease = false;
		
		int nbCheckinAtHardWarehouse = 0;
		boolean isBalance = false;
		HashMap<String, String> srcdest = new HashMap<String, String>();
		ArrayList<Warehouse> hardWh = new ArrayList<Warehouse>();
		for(int i = 0; i < L.length; i++){
			if(L[i].isKep() == false){
				if(L[i].getIndex() < pre_idx_1)
					return null;
				pre_idx_1 = L[i].getIndex();
			}
			else{
				if(L[i].getIndex() < pre_idx_2)
					return null;
				pre_idx_2 = L[i].getIndex();
			}
			RouteElement e = L[i];
			
			if(e.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)){
				if(e.isKep() == false){
					if(e.getDepotContainer() == null){
						DepotContainer depotContainer_a = solver.findDepotContainer4Deposit(returnConDepotCode_a,
								lastLocationCode, container_a);
						e.setDepotContainer(depotContainer_a);
					}
					e.setServiceDuration(e.getDepotContainer().getDeliveryContainerDuration());
					e.setLatestArrivalTime(lateDateTimeDeliveryAtDepot_a);
					e.setContainer(null);
					if(!isRealease)
						e.setContainer(container_b);
					isRealease = true;
				}
				else{
					if(e.getDepotContainer() == null){
						DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(returnConDepotCode_b,
								lastLocationCode, container_b);
						e.setDepotContainer(depotContainer_b);
					}
					e.setServiceDuration(e.getDepotContainer().getDeliveryContainerDuration());
					e.setLatestArrivalTime(lateDateTimeDeliveryAtDepot_b);
					e.setContainer(null);
					if(!isRealease)
						e.setContainer(container_a);
					isRealease = true;
				}
			}
			if(e.getAction().equals(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT)
				|| e.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)
				|| e.getAction().equals(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE)
				|| e.getAction().equals(ActionEnum.PICKUP_CONTAINER)
				|| e.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)
				|| e.getAction().equals(ActionEnum.DELIVERY_CONTAINER)
				|| e.getAction().equals(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE)){
				if(solver.input.getParams().getConstraintDriverBalance()){
					if(solver.getIsDriverBalance(lastLocationCode, e.getLocationCode())){
						ArrayList<Integer> drl = solver.getDrivers(lastLocationCode,
								e.getLocationCode());
						for(int k = 0; k < drl.size(); k++)
							if(drl.get(k) == truck.getDriverID()){
								isBalance = true;
								srcdest.put(lastLocationCode, e.getLocationCode());
							}
					}
				}
			}
			
			travelTime = solver.getTravelTime(lastLocationCode, e.getLocationCode());
			arrivalTime = departureTime + travelTime;
			//String a = DateTimeUtils.unixTimeStamp2DateTime(arrivalTime);
			if(e.getLatestArrivalTime() != null && !e.getLatestArrivalTime().equals("")
				&& arrivalTime > DateTimeUtils.dateTime2Int(e.getLatestArrivalTime()))
				return null;
			if(e.getEarliestArrivalTime() != null){
				startServiceTime = solver.MAX(arrivalTime,
					(int)DateTimeUtils.dateTime2Int(e.getEarliestArrivalTime()));
				extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(
						e.getEarliestArrivalTime()) - arrivalTime);
			}
			else
				startServiceTime = arrivalTime;
			
			if(i == 0 && isStartAtDepot)
				extraStartTime = extraTime;

			Warehouse wh = e.getWarehouse();
			if(wh != null){
				int act = Utils.HARD_DELIVERY_WH;
				String actElement = e.getAction();
				if(actElement.equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| actElement.equals(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE)
					|| actElement.equals(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE))
					act = Utils.HARD_PICKUP_WH;
				if(solver.input.getParams().getConstraintWarehouseHard()){
					if(wh.getHardConstraintType() == act
						|| wh.getHardConstraintType() == Utils.HARD_PICKUP_AND_DELIVERY_WH){
						nbCheckinAtHardWarehouse += solver.getCheckinAtWarehouse(wh.getCheckin(),
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
				
				if(wh.getBreaktimes() != null){
					for(int k = 0; k < wh.getBreaktimes().length; k++){
						Intervals interval = wh.getBreaktimes()[k];
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
			distance += solver.getDistance(lastLocationCode, e.getLocationCode());
			int finishedServiceTime = startServiceTime
					+ e.getServiceDuration();
			if(wh != null && wh.getBreaktimes() != null){
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
			departureTime = finishedServiceTime;
			lastLocationCode = e.getLocationCode();
		}
		endTime = departureTime;
		endLocationCode = lastLocationCode;
		return new Measure(distance, extraTime,
				nbCheckinAtHardWarehouse, isBalance, truck.getDriverID(),
				hardWh, srcdest);
	}
	
	public Measure sortElements(
			Truck truck,
			Container container_a, Container container_b,
			int isNeedReturnCont_a, int isNeedReturnCont_b,
			ArrayList<String> returnConDepotCode_a,
			ArrayList<String> returnConDepotCode_b,
			String lateDateTimeDeliveryAtDepot_a,
			String lateDateTimeDeliveryAtDepot_b,
			RouteElement[] L){
		if(L == null || L.length == 0)
			return null;
		Measure minMs = null;
		RouteElement[] minR = null;
		int extraTime = 0;
		int earliesEndTime = -1;
		String lastLocationCode = null;
		boolean isKep = false;
		
		PermutationGenerator G = new PermutationGenerator();
		Permutation[] P = G.generate(L.length);
		
		for(int i = 0; i < P.length; i++){
			int nbCont = 0;
			boolean kep = false;
			Permutation hv = P[i];
			//if(hv.p[0] == 0 && hv.p[1] == 2 && hv.p[2] == 1 && hv.p[3] == 3)
				//System.out.println("ok");
			
			RouteElement[] R = new RouteElement[L.length];
			for(int j = 0; j < hv.p.length; j++){
				R[j] = L[hv.p[j]];

				if(R[j].getAction().equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| R[j].getAction().equals(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT)
					|| R[j].getAction().equals(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE)
					|| R[j].getAction().equals(ActionEnum.PICKUP_CONTAINER)
					|| R[j].getAction().equals(ActionEnum.TAKE_CONTAINER_AT_DEPOT))
					nbCont++;
				if(nbCont > 1)
					kep = true;
				if(R[j].getAction().equals(ActionEnum.DELIVERY_CONTAINER)
					|| R[j].getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)
					|| R[j].getAction().equals(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT)
					|| R[j].getAction().equals(ActionEnum.UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| R[j].getAction().equals(ActionEnum.UNLINK_LOADED_CONTAINER_AT_WAREHOUSE))
					nbCont--;
			}
			Measure ms = checkRoute(
					truck,
					container_a, container_b,
					isNeedReturnCont_a, isNeedReturnCont_b,
					returnConDepotCode_a, returnConDepotCode_b,
					lateDateTimeDeliveryAtDepot_a,
					lateDateTimeDeliveryAtDepot_b,
					R);
			if(ms != null){
				if(minMs == null 
					|| ms.distance < minMs.distance
					|| (ms.distance == minMs.distance
					&& ms.time < minMs.time)){
					minMs = ms;
					minR = R;
					extraTime = extraStartTime;
					earliesEndTime = endTime;
					lastLocationCode = endLocationCode;
					isKep = kep;
				}
			}
		}
		if(!isKep)
			return null;
		sel_R = minR;
		extraStartTime = extraTime;
		endTime = earliesEndTime;
		endLocationCode = lastLocationCode;
		
		return minMs;
	}
	
	public Measure evaluateKepRoute(
			Truck truck, Mooc mooc,
			Container container_a, Container container_b,
			int isNeedCont_a, int isNeedCont_b,
			int isNeedReturnCont_a, int isNeedReturnCont_b,
			ArrayList<String> returnConDepotCode_a,
			ArrayList<String> returnConDepotCode_b,
			String lateDateTimeDeliveryAtDepot_a,
			String lateDateTimeDeliveryAtDepot_b,
			RouteElement[] L){
		if ((isNeedCont_a == 1 && solver.mContainer2LastDepot.get(container_a) == null)
				|| (isNeedCont_b == 1 && solver.mContainer2LastDepot.get(container_b) == null)) {
			return null;
		}

		//ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		double distance = 0;
		int extraTime = 0;
		
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int startServiceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		int travelTime = -1;
		String lastLocationCode = combo.lastLocationCode;
		
		distance = combo.extraDistance;
		startTime = combo.startTime;
		startLocationCode = combo.lastLocationCode;
		if(combo.routeElement == null)
			isStartAtDepot = true;
		
		for(int i = 0; i < L.length; i++){
			if(L[i].getAction().equals(ActionEnum.TAKE_CONTAINER_AT_DEPOT)){
				if(L[i].isKep() == false){
					L[i].setDepotContainer(solver.mContainer2LastDepot.get(container_a));
					L[i].setContainer(container_a);
					L[i].setEarliestArrivalTime(DateTimeUtils.unixTimeStamp2DateTime(
							solver.mContainer2LastTime.get(container_a)));
					L[i].setServiceDuration(L[i].getDepotContainer().getPickupContainerDuration());
				}
				else{
					L[i].setDepotContainer(solver.mContainer2LastDepot.get(container_b));
					L[i].setContainer(container_b);
					L[i].setEarliestArrivalTime(DateTimeUtils.unixTimeStamp2DateTime(
							solver.mContainer2LastTime.get(container_b)));
					L[i].setServiceDuration(L[i].getDepotContainer().getPickupContainerDuration());
				}
			}
		}
		Measure ms = sortElements(
				truck,
				container_a, container_b,
				isNeedReturnCont_a, isNeedReturnCont_b,
				returnConDepotCode_a, returnConDepotCode_b,
				lateDateTimeDeliveryAtDepot_a,
				lateDateTimeDeliveryAtDepot_b,
				L);
		if(ms == null)
			return null;
		
		departureTime = endTime;
		lastLocationCode = endLocationCode;
		
		if(sel_R == null)
			return null;
		boolean ok = false;
//		if(isNeedReturnCont_a == 1 && isNeedReturnCont_b == 1){
//			DepotContainer depotContainer_a = solver.findDepotContainer4Deposit(returnConDepotCode_a,
//					lastLocationCode, container_a);
//			DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(returnConDepotCode_b,
//					lastLocationCode, container_b);
//			double d1 = solver.getDistance(lastLocationCode, depotContainer_a.getLocationCode());
//			double d2 = solver.getDistance(lastLocationCode, depotContainer_b.getLocationCode());
//			if(d1 > d2)
//				ok = true;				
//		}
//		if(ok == false){
//			if(isNeedReturnCont_a == 1){
//				RouteElement[] RE = new RouteElement[sel_R.length + 1];
//				for(int i = 0; i < sel_R.length; i++)
//					RE[i] = sel_R[i];
//				RouteElement e = new RouteElement();
//				DepotContainer depotContainer = solver.findDepotContainer4Deposit(returnConDepotCode_a,
//						lastLocationCode, container_a);
//				if(depotContainer == null)
//					return null;
//				e.setDepotContainer(depotContainer);
//				e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
//				e.setContainer(container_a);
//				travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
//				distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
//				arrivalTime = departureTime + travelTime;
//				if(lateDateTimeDeliveryAtDepot_a != ""){
//					if(arrivalTime > DateTimeUtils.dateTime2Int(lateDateTimeDeliveryAtDepot_a))
//						return null;
//				}
//				startServiceTime = arrivalTime;
//				duration = depotContainer.getDeliveryContainerDuration();
//				departureTime = startServiceTime + duration;
//				lastLocationCode = depotContainer.getLocationCode();
//				RE[sel_R.length] = e;
//				sel_R = RE;
//			}
//			
//			if(isNeedReturnCont_b == 1){
//				RouteElement[] RE = new RouteElement[sel_R.length + 1];
//				for(int i = 0; i < sel_R.length; i++)
//					RE[i] = sel_R[i];
//				RouteElement e = new RouteElement();
//				DepotContainer depotContainer = solver.findDepotContainer4Deposit(returnConDepotCode_b,
//						lastLocationCode, container_b);
//				if(depotContainer == null)
//					return null;
//				e.setDepotContainer(depotContainer);
//				e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
//				e.setContainer(container_b);
//				travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
//				distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
//				arrivalTime = departureTime + travelTime;
//				if(lateDateTimeDeliveryAtDepot_b != ""){
//					if(arrivalTime > DateTimeUtils.dateTime2Int(lateDateTimeDeliveryAtDepot_b))
//						return null;
//				}
//				startServiceTime = arrivalTime;
//				duration = depotContainer.getDeliveryContainerDuration();
//				departureTime = startServiceTime + duration;
//				lastLocationCode = depotContainer.getLocationCode();
//				RE[sel_R.length] = e;
//				sel_R = RE;
//			}
//		}
//		else{
//			if(isNeedReturnCont_b == 1){
//				RouteElement[] RE = new RouteElement[sel_R.length + 1];
//				for(int i = 0; i < sel_R.length; i++)
//					RE[i] = sel_R[i];
//				RouteElement e = new RouteElement();
//				DepotContainer depotContainer = solver.findDepotContainer4Deposit(returnConDepotCode_b,
//						lastLocationCode, container_b);
//				if(depotContainer == null)
//					return null;
//				e.setDepotContainer(depotContainer);
//				e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
//				e.setContainer(container_b);
//				travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
//				distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
//				arrivalTime = departureTime + travelTime;
//				if(lateDateTimeDeliveryAtDepot_b != ""){
//					if(arrivalTime > DateTimeUtils.dateTime2Int(lateDateTimeDeliveryAtDepot_b))
//						return null;
//				}
//				startServiceTime = arrivalTime;
//				duration = depotContainer.getDeliveryContainerDuration();
//				departureTime = startServiceTime + duration;
//				lastLocationCode = depotContainer.getLocationCode();
//				RE[sel_R.length] = e;
//				sel_R = RE;
//			}
//			
//			if(isNeedReturnCont_a == 1){
//				RouteElement[] RE = new RouteElement[sel_R.length + 1];
//				for(int i = 0; i < sel_R.length; i++)
//					RE[i] = sel_R[i];
//				RouteElement e = new RouteElement();
//				DepotContainer depotContainer = solver.findDepotContainer4Deposit(returnConDepotCode_a,
//						lastLocationCode, container_a);
//				if(depotContainer == null)
//					return null;
//				e.setDepotContainer(depotContainer);
//				e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
//				e.setContainer(container_a);
//				travelTime = solver.getTravelTime(lastLocationCode, depotContainer.getLocationCode());
//				distance += solver.getDistance(lastLocationCode, depotContainer.getLocationCode());
//				arrivalTime = departureTime + travelTime;
//				if(lateDateTimeDeliveryAtDepot_a != ""){
//					if(arrivalTime > DateTimeUtils.dateTime2Int(lateDateTimeDeliveryAtDepot_a))
//						return null;
//				}
//				startServiceTime = arrivalTime;
//				duration = depotContainer.getDeliveryContainerDuration();
//				departureTime = startServiceTime + duration;
//				lastLocationCode = depotContainer.getLocationCode();
//				RE[sel_R.length] = e;
//				sel_R = RE;
//			}
//		}
		
		RouteElement[] RE = new RouteElement[sel_R.length + 2];
		for(int i = 0; i < sel_R.length; i++)
			RE[i] = sel_R[i];
		RouteElement e = new RouteElement();
		DepotMooc returnDepotMooc = solver.findDepotMooc4Deposit(lastLocationCode, mooc);
		e.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e.setMooc(mooc);
		e.setDepotMooc(returnDepotMooc);
		e.setKepValue(false);
		distance += solver.getDistance(lastLocationCode, returnDepotMooc.getLocationCode());
		travelTime = solver.getTravelTime(lastLocationCode, returnDepotMooc.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = returnDepotMooc.getDeliveryMoocDuration();
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsMooc(mooc, combo.startTimeOfMooc - extraStartTime, departureTime))
			return null;
		RE[sel_R.length] = e;
		lastLocationCode = returnDepotMooc.getLocationCode();
		
		RouteElement e1 = new RouteElement();
		DepotTruck returnDepotTruck = solver.findDepotTruck4Deposit(lastLocationCode, truck);
		e1.setDepotTruck(returnDepotTruck);
		e1.setAction(ActionEnum.REST_AT_DEPOT);
		e1.setKepValue(false);
		
		distance += solver.getDistance(lastLocationCode, returnDepotTruck.getLocationCode());		
		travelTime = solver.getTravelTime(lastLocationCode, returnDepotTruck.getLocationCode());
		arrivalTime = departureTime + travelTime;
		startServiceTime = arrivalTime;
		duration = 0;
		departureTime = startServiceTime + duration;
		if(!solver.checkAvailableIntervalsTruck(truck, 
				combo.startTimeOfTruck - extraStartTime, departureTime))
			return null;
		
		RE[sel_R.length + 1] = e1;
		sel_R = RE;
		idxKep = new ArrayList<Boolean>();
		for(int i = 0; i < sel_R.length; i++)
			idxKep.add(sel_R[i].isKep());
		
		return new Measure(distance + ms.distance, extraTime + ms.time, ms.hardWarehouse,
				ms.isBalance, truck.getDriverID(),
				ms.wh, ms.srcdest);
	}
	
	public TruckRouteInfo4Request createRoute(){
		TruckRoute r = new TruckRoute();
		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		
		if(sel_R == null)
			return null;
		
		ComboContainerMoocTruck combo = solver.findLastAvailable(sel_truck, sel_mooc);
		if (combo == null)
			return null;

		double distance = 0;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = 0;
		int startServiceTime = 0;
		int duration = 0;
		int lastUsedIndex = 0;
		int travelTime = 0;
		int extraTime = 0;
		if (combo.routeElement == null) {
			departureTime = solver.mTruck2LastTime.get(sel_truck);
			// depart from the depot of the truck
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setDepotTruck(solver.mTruck2LastDepot.get(sel_truck));
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setTruck(sel_truck);
			solver.mPoint2DepartureTime.put(e0, combo.startTimeOfTruck);

			// arrive at the depot mooc, take a mooc
			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
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
				e1.setDepotMooc(solver.mMooc2LastDepot.get(sel_mooc));
				e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
				e1.setMooc(sel_mooc);
				travelTime = solver.getTravelTime(lastElement, e1);
				arrivalTime = departureTime + travelTime;
				int timeMooc = solver.mMooc2LastTime.get(sel_mooc);
				startServiceTime = solver.MAX(arrivalTime, timeMooc);
				duration = e1.getDepotMooc().getPickupMoocDuration();
				departureTime = startServiceTime + duration;
				solver.mPoint2ArrivalTime.put(e1, arrivalTime);
				solver.mPoint2DepartureTime.put(e1, departureTime);
				lastElement = e1;
			}
		}
		departureTime = combo.startTime;
		startTime = combo.startTime;
		startLocationCode = combo.lastLocationCode;
		if(combo.routeElement == null)
			isStartAtDepot = true;
		
		String lastLocationCode = startLocationCode;
		Container releaseCont_1 = null;
		Container releaseCont_2 = null;
		boolean isRealease = false;
		int nbCont = 0;
		for(int i = 0; i < sel_R.length; i++){
			RouteElement e = sel_R[i];
			e.setKepValue(idxKep.get(i));
			if(e.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)){
				if(e.isKep() == false){
					if(e.getDepotContainer() == null){
						DepotContainer depotContainer_a = solver.findDepotContainer4Deposit(returnContDepotCode_a,
								lastLocationCode, sel_container_a);
						e.setDepotContainer(depotContainer_a);
					}
					e.setServiceDuration(e.getDepotContainer().getDeliveryContainerDuration());
					e.setLatestArrivalTime(lateDateTimeDeliveryAtDepot_a);
					e.setContainer(null);
					if(!isRealease)
						e.setContainer(sel_container_b);
					isRealease = true;
				}
				else{
					if(e.getDepotContainer() == null){
						DepotContainer depotContainer_b = solver.findDepotContainer4Deposit(returnContDepotCode_b,
								lastLocationCode, sel_container_b);
						e.setDepotContainer(depotContainer_b);
					}
					e.setServiceDuration(e.getDepotContainer().getDeliveryContainerDuration());
					e.setLatestArrivalTime(lateDateTimeDeliveryAtDepot_b);
					e.setContainer(null);
					if(!isRealease)
						e.setContainer(sel_container_a);
					isRealease = true;
				}
			}
			
			if(e.getAction().equals(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)
					|| e.getAction().equals(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT)
					|| e.getAction().equals(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE)
					|| e.getAction().equals(ActionEnum.PICKUP_CONTAINER)
					|| e.getAction().equals(ActionEnum.TAKE_CONTAINER_AT_DEPOT)){
				nbCont++;
				if(e.isKep() == false)
					e.setContainer(sel_container_a);
				else
					e.setContainer(sel_container_b);
			}
			else if(e.getAction().equals(ActionEnum.DELIVERY_CONTAINER)
				|| e.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)
				|| e.getAction().equals(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT)
				|| e.getAction().equals(ActionEnum.UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE)
				|| e.getAction().equals(ActionEnum.UNLINK_LOADED_CONTAINER_AT_WAREHOUSE)){
				nbCont--;
				if(nbCont == 1 && e.isKep() == false)
					e.setContainer(sel_container_b);
				else if(nbCont == 1 && e.isKep())
					e.setContainer(sel_container_a);
				else
					e.setContainer(null);
			}
			
			travelTime = solver.getTravelTime(lastLocationCode, e.getLocationCode());
			arrivalTime = departureTime + travelTime;
			if(e.getLatestArrivalTime() != null && !e.getLatestArrivalTime().equals("")
				&& arrivalTime > DateTimeUtils.dateTime2Int(e.getLatestArrivalTime()))
				return null;
			if(e.getEarliestArrivalTime() != null){
				startServiceTime = solver.MAX(arrivalTime,
					(int)DateTimeUtils.dateTime2Int(e.getEarliestArrivalTime()));
				extraTime += solver.MAX(0, (int) DateTimeUtils.dateTime2Int(
						e.getEarliestArrivalTime()) - arrivalTime);
			}
			else
				startServiceTime = arrivalTime;
			
			if(i == 0 && isStartAtDepot)
				extraStartTime = extraTime;

			Warehouse wh = e.getWarehouse();
			if(wh != null && wh.getBreaktimes() != null){
				for(int k = 0; k < wh.getBreaktimes().length; k++){
					Intervals interval = wh.getBreaktimes()[k];
					if(startServiceTime >= (int)DateTimeUtils.dateTime2Int(interval.getDateStart())
						&& startServiceTime < (int)DateTimeUtils.dateTime2Int(interval.getDateEnd())){
						extraTime += (int)DateTimeUtils
								.dateTime2Int(interval.getDateEnd()) - startServiceTime;
						startServiceTime = (int)DateTimeUtils.
								dateTime2Int(interval.getDateEnd());
					}
				}
			}
			distance += solver.getDistance(lastLocationCode, e.getLocationCode());
			int finishedServiceTime = startServiceTime
					+ e.getServiceDuration();
			if(wh != null && wh.getBreaktimes() != null){
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
			departureTime = finishedServiceTime;
			lastLocationCode = e.getLocationCode();
			
			solver.mPoint2ArrivalTime.put(e, arrivalTime);
			solver.mPoint2DepartureTime.put(e, departureTime);
			e.setTruck(sel_truck);
			e.setMooc(sel_mooc);
			if(e.getAction().equals(ActionEnum.REST_AT_DEPOT)){
				e.setTruck(null);
				e.setMooc(null);
				tri.setLastDepotTruck(sel_truck, e.getDepotTruck());
				tri.setLastTimeTruck(sel_truck, departureTime);
			}
			if(e.getAction().equals(ActionEnum.RELEASE_MOOC_AT_DEPOT)){
				e.setMooc(null);
				tri.setLastDepotMooc(sel_mooc, e.getDepotMooc());
				tri.setLastTimeMooc(sel_mooc, departureTime);
			}
			if(e.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)){
				Container container = sel_container_b;
				if(e.isKep() == false){
					releaseCont_1 = sel_container_a;
					container = sel_container_a;
				}
				else
					releaseCont_2 = sel_container_b;
				tri.setLastDepotContainer(container, e.getDepotContainer());
				tri.setLastTimeContainer(container, departureTime);
			}
			L.add(e);
			getRequest(e);
		}
		
		if(releaseCont_1 == null){
			tri.setLastDepotContainer(sel_container_a, null);
			tri.setLastTimeContainer(sel_container_a, Integer.MAX_VALUE);
		}
		if(releaseCont_2 == null){
			tri.setLastDepotContainer(sel_container_b, null);
			tri.setLastTimeContainer(sel_container_b, Integer.MAX_VALUE);
		}
		
		RouteElement[] e = new RouteElement[L.size()];
		for (int i = 0; i < e.length; i++)
			e[i] = L.get(i);
		r = new TruckRoute();
		r.setNodes(e);
		r.setTruck(sel_truck);
		r.setType(TruckRoute.KEP);
		solver.propagate(r);
		
		tri.route = r;
		tri.additionalDistance = distance;
		tri.lastUsedIndex = lastUsedIndex;
		
		return tri;
	}
	
	public ArrayList<String> getWhCode_a(){
		if(sel_whCode_a != null)
			return sel_whCode_a;
		return null;
	}
	public ArrayList<String> getWhCode_b(){
		if(sel_whCode_b != null)
			return sel_whCode_b;
		return null;
	}
}
