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
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseTransportRequest;
import utils.DateTimeUtils;

public class GreedyDirectServiceSolver extends ContainerTruckMoocSolver{
	//public ContainerTruckMoocSolver solver;
	
	public String name(){
		return "GreedyDirectServiceSolver";
	}
	
	public void test(){
		RouteDoubleImportCreator routeDoubleImportCreator = new RouteDoubleImportCreator(this);
		RouteKeplechCreator routeKeplechCreator = new RouteKeplechCreator(this);
		RouteSwapImportExportCreator routeSwapImportExportCreator = new RouteSwapImportExportCreator(this);
		RouteTangboWarehouseExport routeTangboWarehouseExport = new RouteTangboWarehouseExport(this);
		Truck[] trucks = input.getTrucks();
		Mooc[] moocs = input.getMoocs();
		Container[] containers = input.getContainers();
		ExportContainerRequest[] exReq = getSortedExportRequests();
		ImportContainerRequest[] imReq = getSortedImportRequests();
		WarehouseContainerTransportRequest[] whReq = getSortedWarehouseTransportRequests();

		for(int i = 0; i < trucks.length; i++){
			for(int j = 0; j < moocs.length; j++){
				for(int k = 0; k < imReq.length; k++){
					for(int q = k+1; q < imReq.length; q++){
						backup();
						TruckRoute tr = routeDoubleImportCreator.createDoubleImportRequest(trucks[i], moocs[j], imReq[k], imReq[q]);
						if(tr == null){
							System.out.println(name() + "::solve, cannot find routeDoubleImport for (" + i + "," + j + "," + k + "," + q + ")");
							logln(name() + "::solve, cannot find routeDoubleImport for (" + i + "," + j + "," + k + "," + q + ")");
						}else{
							System.out.println(name() + "::solve, FOUND routeDoubleImport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
							logln(name() + "::solve, FOUND routeDoubleImport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
						}
						restore();
					}
				}
			}
		}
		for(int i = 0; i < trucks.length; i++){
			for(int j = 0; j < moocs.length; j++){
				for(int k = 0; k < imReq.length; k++){
					for(int q = 0; q < exReq.length; q++){
						backup();
						TruckRoute tr = routeSwapImportExportCreator.createSwapImportExport(trucks[i], moocs[j], imReq[k], exReq[q]);
						if(tr == null){
							System.out.println(name() + "::solve, cannot find routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + ")");
							logln(name() + "::solve, cannot find routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + ")");
						}else{
							System.out.println(name() + "::solve, FOUND routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
							logln(name() + "::solve, FOUND routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
						}
						restore();
					}
				}
			}
		}
		
		for(int i = 0; i < trucks.length; i++){
			for(int j = 0; j < moocs.length; j++){
				for(int k =0; k < containers.length; k++){
					for(int a = 0; a < exReq.length; a++){
						for(int b = 0; b < imReq.length; b++){
							backup();
							TruckRoute tr = routeKeplechCreator.createKeplech(trucks[i], moocs[j], containers[k], exReq[a], imReq[b]);
							if(tr == null){
								System.out.println(name() + "::solve, cannot find routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
								logln(name() + "::solve, cannot find routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
							}else{
								System.out.println(name() + "::solve, FOUND routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								logln(name() + "::solve, FOUND routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
							}
							restore();
						}
					}
				}
			}
		}
		for(int i = 0; i < trucks.length; i++){
			for(int j = 0; j < moocs.length; j++){
				for(int k =0; k < containers.length; k++){
					for(int a = 0; a < whReq.length; a++){
						for(int b = 0; b < exReq.length; b++){
							backup();
							TruckRoute tr = routeTangboWarehouseExport.createTangboWarehouseExport(trucks[i], moocs[j], containers[k], whReq[a], exReq[b]);
							if(tr == null){
								System.out.println(name() + "::solve, cannot find routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
								logln(name() + "::solve, cannot find routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
							}else{
								System.out.println(name() + "::solve, FOUND routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								logln(name() + "::solve, FOUND routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
							}
							restore();
						}
					}
				}
			}
		}
		
	}
	
	public void initDirectRoutes(){
		ArrayList<ExportContainerRequest> s_exReq = new ArrayList<ExportContainerRequest>();
		ArrayList<ImportContainerRequest> s_imReq = new ArrayList<ImportContainerRequest>();
		ArrayList<WarehouseContainerTransportRequest> s_whReq = new ArrayList<WarehouseContainerTransportRequest>();
	
		Truck[] trucks = input.getTrucks();
		Mooc[] moocs = input.getMoocs();
		Container[] containers = input.getContainers();
		ExportContainerRequest[] exReq = getSortedExportRequests();
		ImportContainerRequest[] imReq = getSortedImportRequests();
		WarehouseContainerTransportRequest[] whReq = getSortedWarehouseTransportRequests();
		
		for(int i = 0; i < exReq.length; i++) s_exReq.add(exReq[i]);
		for(int i = 0; i < imReq.length; i++) s_imReq.add(imReq[i]);
		for(int i = 0; i < whReq.length; i++) s_whReq.add(whReq[i]);
		
		while(s_exReq.size() + s_imReq.size() + s_whReq.size() > 0){
			ExportContainerRequest er = null;
			if(s_exReq.size() > 0) er = s_exReq.get(0);
			
			ImportContainerRequest ir = null;
			if(s_imReq.size() > 0) ir = s_imReq.get(0);
			
			WarehouseContainerTransportRequest wr = null;
			if(s_whReq.size() > 0) wr = s_whReq.get(0);
			
			if(processBefore(er, ir)){
				if(processBefore(er, wr)){
					TruckRouteInfo4Request tri = createNewDirectRoute4ExportRequest(er);
					if(tri == null) break;
					commitItinerary(tri);
					s_exReq.remove(0);
				}else{
					TruckRouteInfo4Request tri = createNewDirectRoute4WarehouseRequest(wr);
					if(tri == null) break;
					commitItinerary(tri);
					s_whReq.remove(0);
				}
			}else{
				if(processBefore(ir, wr)){
					TruckRouteInfo4Request tri = createNewDirectRoute4ImportRequest(ir);
					if(tri == null) break;
					commitItinerary(tri);
					s_imReq.remove(0);
				}else{
					TruckRouteInfo4Request tri = createNewDirectRoute4WarehouseRequest(wr);
					if(tri == null) break;
					commitItinerary(tri);
					s_whReq.remove(0);
				}
			}
		}
		
	}
	public void commitItinerary(TruckRouteInfo4Request tri){
		if(tri == null) return;
		System.out.println(name() + "::commitItinerary, route = " + tri.route.toString());
		Truck truck = tri.route.getTruck();
		TruckItinerary I = mTruck2Itinerary.get(truck);
		if(I == null){
			I = new TruckItinerary();
			mTruck2Itinerary.put(truck, I);
		}
		I.addRoute(tri.route, tri.lastUsedIndex);
	}
	
	public ContainerTruckMoocSolution solveDirect(ContainerTruckMoocInput input) {
		this.input = input;
		
		initLog();
		modifyContainerCode();
		mapData();
		init();
		mTruck2Itinerary = new HashMap<Truck, TruckItinerary>();
		initDirectRoutes();
		
		recoverContainerCode();
		
		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();
		for(int i = 0; i < input.getTrucks().length; i++){
			Truck truck = input.getTrucks()[i];
			TruckItinerary I = mTruck2Itinerary.get(truck);
			if(I != null){
				TruckRoute tr = I.establishTruckRoute();
				if(tr != null){
					lst_truckRoutes.add(tr);
				}
			}
		}
		
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
	
	public ContainerTruckMoocSolution solveInitDirectImproveSpecialOperator(ContainerTruckMoocInput input) {
		this.input = input;
		
		initLog();
		modifyContainerCode();
		mapData();
		init();
		mTruck2Itinerary = new HashMap<Truck, TruckItinerary>();
		initDirectRoutes();
		
		
		
		recoverContainerCode();
		
		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();
		for(int i = 0; i < input.getTrucks().length; i++){
			Truck truck = input.getTrucks()[i];
			TruckItinerary I = mTruck2Itinerary.get(truck);
			if(I != null){
				TruckRoute tr = I.establishTruckRoute();
				if(tr != null){
					lst_truckRoutes.add(tr);
				}
			}
		}
		
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

	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		this.input = input;
		
		initLog();
		modifyContainerCode();
		mapData();
		init();
		
		
		mTruck2Route = new HashMap<Truck, TruckRoute>();
		
		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();
		
		RouteDoubleImportCreator routeDoubleImportCreator = new RouteDoubleImportCreator(this);
		RouteKeplechCreator routeKeplechCreator = new RouteKeplechCreator(this);
		RouteSwapImportExportCreator routeSwapImportExportCreator = new RouteSwapImportExportCreator(this);
		RouteTangboWarehouseExport routeTangboWarehouseExport = new RouteTangboWarehouseExport(this);
		Truck[] trucks = input.getTrucks();
		Mooc[] moocs = input.getMoocs();
		Container[] containers = input.getContainers();
		ExportContainerRequest[] exReq = getSortedExportRequests();
		ImportContainerRequest[] imReq = getSortedImportRequests();
		WarehouseContainerTransportRequest[] whReq = getSortedWarehouseTransportRequests();

		boolean[] exReqScheduled = new boolean[exReq.length];
		boolean[] imReqScheduled = new boolean[imReq.length];
		boolean[] whReqScheduled = new boolean[whReq.length];
		for(int i= 0; i < exReqScheduled.length; i++) exReqScheduled[i] = false;
		for(int i= 0; i < imReqScheduled.length; i++) imReqScheduled[i] = false;
		for(int i= 0; i < whReqScheduled.length; i++) whReqScheduled[i] = false;
		
		while(true){
			int sel_imReq_k = -1;
			int sel_imReq_q = -1;
			int sel_truck = -1;
			int sel_mooc = -1;
			int sel_container = -1;
			double minDistance = Integer.MAX_VALUE;
			boolean hasMove = false;
			
			for(int i = 0; i < trucks.length; i++){
				for(int j = 0; j < moocs.length; j++){
					for(int k = 0; k < imReq.length; k++){
						for(int q = k+1; q < imReq.length; q++){
							if(imReqScheduled[k] || imReqScheduled[q]) continue;
							
							backup();
							TruckRoute tr = routeDoubleImportCreator.createDoubleImportRequest(trucks[i], moocs[j], imReq[k], imReq[q]);
							if(tr == null){
								//System.out.println(name() + "::solve, cannot find routeDoubleImport for (" + i + "," + j + "," + k + "," + q + ")");
								logln(name() + "::solve, cannot find routeDoubleImport for (" + i + "," + j + "," + k + "," + q + ")");
							}else{
								//System.out.println(name() + "::solve, FOUND routeDoubleImport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								logln(name() + "::solve, FOUND routeDoubleImport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								if(minDistance > tr.getDistance()){
									minDistance = tr.getDistance();
									sel_imReq_k = k;
									sel_imReq_q = q;
									sel_truck = i;
									sel_mooc = j;
									
								}
							}
							restore();
						}
					}
				}
			}
			if(sel_imReq_k >= 0){
				TruckRoute tr = routeDoubleImportCreator.createDoubleImportRequest(trucks[sel_truck], moocs[sel_mooc], 
						imReq[sel_imReq_k], imReq[sel_imReq_q]);
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}
				hasMove = true;
				imReqScheduled[sel_imReq_k] = true;
				imReqScheduled[sel_imReq_q] = true;
				System.out.println(name() + "::solve, FOUND routeDoubleImport for import " + sel_imReq_k + "," + sel_imReq_q);
			}
			
			minDistance = Integer.MAX_VALUE;
			sel_truck = -1;
			sel_mooc = -1;
			sel_imReq_k = -1;
			int sel_exReq_q = -1;
			for(int i = 0; i < trucks.length; i++){
				for(int j = 0; j < moocs.length; j++){
					for(int k = 0; k < imReq.length; k++){
						for(int q = 0; q < exReq.length; q++){
							if(imReqScheduled[k] || exReqScheduled[q]) continue;
							
							backup();
							TruckRoute tr = routeSwapImportExportCreator.createSwapImportExport(trucks[i], moocs[j], imReq[k], exReq[q]);
							if(tr == null){
								//System.out.println(name() + "::solve, cannot find routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + ")");
								logln(name() + "::solve, cannot find routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + ")");
							}else{
								//System.out.println(name() + "::solve, FOUND routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								logln(name() + "::solve, FOUND routeSwapImportExport for (" + i + "," + j + "," + k + "," + q + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
								if(minDistance > tr.getDistance()){
									minDistance = tr.getDistance();
									sel_truck = i;
									sel_mooc = j;
									sel_imReq_k = k;
									sel_exReq_q = q;
								}
							}
							restore();
						}
					}
				}
			}
			if(sel_truck >= 0){
				TruckRoute tr = routeSwapImportExportCreator.createSwapImportExport(trucks[sel_truck], moocs[sel_mooc], 
						imReq[sel_imReq_k], exReq[sel_exReq_q]);
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}
				hasMove = true;
				imReqScheduled[sel_imReq_k] = true;
				exReqScheduled[sel_exReq_q] = true;
				System.out.println(name() + "::solve, FOUND routeSwapImportExport for import " + sel_imReq_k + " and export " + sel_exReq_q);
			}
			
			minDistance = Integer.MAX_VALUE;
			sel_truck = -1;
			sel_mooc = -1;
			sel_container = -1;
			int sel_exReq_a = -1;
			int sel_imReq_b = -1;
			for(int i = 0; i < trucks.length; i++){
				for(int j = 0; j < moocs.length; j++){
					for(int k =0; k < containers.length; k++){
						for(int a = 0; a < exReq.length; a++){
							for(int b = 0; b < imReq.length; b++){
								if(exReqScheduled[a] || imReqScheduled[b]) continue;
								backup();
								TruckRoute tr = routeKeplechCreator.createKeplech(trucks[i], moocs[j], containers[k], exReq[a], imReq[b]);
								if(tr == null){
									//System.out.println(name() + "::solve, cannot find routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
									logln(name() + "::solve, cannot find routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
								}else{
									//System.out.println(name() + "::solve, FOUND routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
									logln(name() + "::solve, FOUND routeKepLech for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
									if(minDistance > tr.getDistance()){
										minDistance = tr.getDistance();
										sel_truck = i;
										sel_mooc = j;
										sel_container = k;
										sel_exReq_a = a;
										sel_imReq_b = b;
									}
								}
								restore();
							}
						}
					}
				}
			}
			if(sel_truck >= 0){
				TruckRoute tr = routeKeplechCreator.createKeplech(trucks[sel_truck], moocs[sel_mooc], containers[sel_container], 
						exReq[sel_exReq_a], imReq[sel_imReq_b]);
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}
				hasMove = true;
				exReqScheduled[sel_exReq_a] = true;
				imReqScheduled[sel_imReq_b] = true;
				System.out.println(name() + "::solve, FOUND routeKepLech for export " + sel_exReq_a + " AND import " + sel_imReq_b);
			}
			
			minDistance = Integer.MAX_VALUE;
			sel_truck = -1;
			sel_mooc = -1;
			sel_container = -1;
			int sel_whReq_a = -1;
			int sel_exReq_b = -1;
			for(int i = 0; i < trucks.length; i++){
				for(int j = 0; j < moocs.length; j++){
					for(int k =0; k < containers.length; k++){
						for(int a = 0; a < whReq.length; a++){
							for(int b = 0; b < exReq.length; b++){
								if(whReqScheduled[a] || exReqScheduled[b]) continue;
								backup();
								TruckRoute tr = routeTangboWarehouseExport.createTangboWarehouseExport(trucks[i], moocs[j], containers[k], whReq[a], exReq[b]);
								if(tr == null){
									//System.out.println(name() + "::solve, cannot find routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
									logln(name() + "::solve, cannot find routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b +  ")");
								}else{
									//System.out.println(name() + "::solve, FOUND routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
									logln(name() + "::solve, FOUND routeTangboWarehouseExport for (" + i + "," + j + "," + k + "," + a + "," + b + "), route = " + tr.toString() + ", distance = " + tr.getDistance());
									if(minDistance > tr.getDistance()){
										sel_truck = i;
										sel_mooc = j;
										sel_container = k;
										sel_whReq_a = a;
										sel_exReq_b = b;
									}
								}
								restore();
							}
						}
					}
				}
			}
			if(sel_truck >= 0){
				TruckRoute tr = routeTangboWarehouseExport.createTangboWarehouseExport(trucks[sel_truck], moocs[sel_mooc], 
						containers[sel_container], whReq[sel_whReq_a], exReq[sel_exReq_b]);
				if(mTruck2Route.get(tr.getTruck())== null){
					mTruck2Route.put(tr.getTruck(), tr);				
					lst_truckRoutes.add(tr);
				}				
				hasMove = true;
				whReqScheduled[sel_whReq_a] = true;
				exReqScheduled[sel_exReq_b] = true;
				System.out.println(name() + "::solve, FOUND routeTangboWarehouseExport for warehouse " + sel_whReq_a + 
						" AND export " + sel_exReq_b);
			}
			
			if(!hasMove){
				break;
			}
			
		}
		
		
		/*
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
		*/
		
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
d
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
	
	public TruckRouteInfo4Request createNewDirectRoute4ImportRequest(ImportContainerRequest r){
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
			//if(mTruck2Route.get(sel_combo.truck) == null)
			//	return tr;
			//TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			//old_tr.concat(tr);
			//System.out.println(name() + "::createDirectRoute4ImportRequest, concat truck-route, length = " + old_tr.getNodes().length);
			//return old_tr;
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;
			
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)
				|| sel_combo.routeElement.getAction().equals(ActionEnum.RELEASE_CONTAINER_AT_DEPOT)){
			Truck truck = sel_combo.truck;
			//TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
			
			System.out.println(name() + "::createDirectRoute4ImportRequest, routeElement e = " + e.toString()
					+ ", e.getDepartureTime = " + e.getDepartureTime());
			
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
			
			//tr.removeNodesAfter(e);
			//tr.addNodes(L);
			//return tr;
			
			TruckRoute newTr = new TruckRoute(truck,L);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;
			
			return tri;
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


	public TruckRouteInfo4Request createNewDirectRoute4ExportRequest(ExportContainerRequest r){
		String warehouseCode = r.getWareHouseCode();
		logln(name() + "::createNewDirectRoute4ExportRequest, warehouseCode = " + warehouseCode);
		
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
					
					//if(combo.truck.getCode().equals("Truck0001") && combo.mooc.getCode().equals("Mooc0002") &&
					//		combo.container.getCode().equals("Container002"))
					//	System.out.println(name() + "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = " + 
					//DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeWarehouse) + 
					//		", latePickupWarehouse = " + DateTimeUtils.unixTimeStamp2DateTime(latePickupWarehouse));
					
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
			logln(name() + "::createNewDirectRoute4ExportRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if(sel_combo.routeElement == null){
			TruckRoute tr = createDirectRouteForExportRequest(r, sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out.println(name() + "::createNewDirectRoute4ExportRequest, create route from all depots, truck = " + sel_combo.truck.getCode()
					+ ", mooc = " + sel_combo.mooc.getCode() + ", container = " + sel_combo.container.getCode());
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
				tri.route = tr;
				tri.lastUsedIndex = -1;
				tri.additionalDistance = minDistance;
				return tri;
			
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)){
			System.out.println(name() + "::createDirectRoute4ExportRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");
			
			Truck truck = sel_combo.truck;
			//TruckRoute tr = mTruck2Route.get(truck);
			TruckItinerary I = mTruck2Itinerary.get(truck);
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
			
			
			TruckRoute newTr = new TruckRoute(truck,L);
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;
			
			return tri;
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
			
			
			TruckRoute newTr = new TruckRoute(truck,L);
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = minDistance;
			
			return tri;
		}
		return null;
	}

	public TruckRoute createDirectRoute4WarehouseRequest(WarehouseContainerTransportRequest r){
		String fromWarehouseCode = r.getFromWarehouseCode();
		String toWarehouseCode = r.getToWarehouseCode();
		logln(name() + "::createDirectRoute4WarehouseRequest, fromWarehouseCode = " + fromWarehouseCode + 
				", toWarehouseCode = " + toWarehouseCode);
		
		Warehouse fromWarehouse = mCode2Warehouse.get(fromWarehouseCode);
		Warehouse toWarehouse = mCode2Warehouse.get(toWarehouseCode);
		
		String fromWarehouseLocationCode = fromWarehouse.getLocationCode();
		String toWarehouseLocationCode = toWarehouse.getLocationCode();
		
		int earlyPickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getEarlyDateTimeLoad());
		int latePickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getLateDateTimeLoad());
		
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
					//HashSet<Container> C = mShipCompanyCode2Containers.get(r.getShipCompanyCode());
					
					//if(!C.contains(container)) continue;
					//if(!r.getShipCompanyCode().equals(shipCompanyCode)) continue;
					
					ComboContainerMoocTruck combo = findLastAvailable(truck, mooc, container);
					if(combo == null) continue;
					
					int startTime = combo.startTime;
					String startLocationCode = combo.lastLocationCode;
					//System.out.println(name() + "::createDirectRoute4ExportRequest, lastLocationCode = " + combo.lastLocationCode);
					
					int arrivalTimeWarehouse = startTime + getTravelTime(startLocationCode, fromWarehouseLocationCode);
					
					//if(combo.truck.getCode().equals("Truck0001") && combo.mooc.getCode().equals("Mooc0002") &&
					//		combo.container.getCode().equals("Container002"))
					//	System.out.println(name() + "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = " + 
					//DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeWarehouse) + 
					//		", latePickupWarehouse = " + DateTimeUtils.unixTimeStamp2DateTime(latePickupWarehouse));
					
					if(arrivalTimeWarehouse > latePickupWarehouse) continue;
					
					double distance = combo.extraDistance + getDistance(combo.lastLocationCode, fromWarehouseLocationCode);
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
			logln(name() + "::createDirectRoute4WarehouseRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if(sel_combo.routeElement == null){
			
			TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r, sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out.println(name() + "::createDirectRoute4WarehouseRequest, create route from all depots, truck = " + sel_combo.truck.getCode()
					+ ", mooc = " + sel_combo.mooc.getCode() + ", container = " + sel_combo.container.getCode());
			if(mTruck2Route.get(sel_combo.truck) == null)
				return tr;
			TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			old_tr.concat(tr);
			
			return old_tr;
			
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)){
			System.out.println(name() + "::createDirectRoute4WarehouseRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");
			
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
			e3.setWarehouse(fromWarehouse);
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setWarehouseRequest(r);

			travelTime = getTravelTime(e2, e3);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r.getLateDateTimeLoad()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			e5.deriveFrom(e4);
			e5.setWarehouse(toWarehouse);
			e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			finishedServiceTime = startServiceTime + r.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);

			RouteElement e6 = new RouteElement();
			e6.deriveFrom(e5);
			e6.setWarehouse(toWarehouse);
			e6.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouseRequest(null);
			travelTime = getTravelTime(e5, e6);
			arrivalTime = departureTime + travelTime;
			startServiceTime = finishedServiceTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);

			RouteElement e7 = new RouteElement();
			e7.deriveFrom(e6);
			e7.setDepotContainer(mCode2DepotContainer.get(container
					.getDepotContainerCode()));
			e7.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e7.setContainer(null);
			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotContainer.get(
					container.getDepotContainerCode())
					.getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);

			RouteElement e8 = new RouteElement();
			e8.deriveFrom(e7);
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e8.setMooc(sel_combo.mooc);
			travelTime = getTravelTime(e7, e8);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode())
					.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e8, arrivalTime);
			mPoint2DepartureTime.put(e8, departureTime);

			RouteElement e9 = new RouteElement();
			e9.deriveFrom(e8);
			e9.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e9.setAction(ActionEnum.REST_AT_DEPOT);
			travelTime = getTravelTime(e8, e9);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e9, arrivalTime);
			mPoint2DepartureTime.put(e9, departureTime);
			
			
			
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
			e3.setWarehouse(mCode2Warehouse.get(fromWarehouse));
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setWarehouseRequest(r);

			int travelTime = getTravelTime(e, e3);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r.getEarlyDateTimeLoad()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			int duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			e5.deriveFrom(e4);
			e5.setWarehouse(toWarehouse);
			e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			finishedServiceTime = startServiceTime + r.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);

			RouteElement e6 = new RouteElement();
			e6.deriveFrom(e5);
			e6.setWarehouse(toWarehouse);
			e6.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouseRequest(null);
			travelTime = getTravelTime(e5, e6);
			arrivalTime = departureTime + travelTime;
			startServiceTime = finishedServiceTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);

			RouteElement e7 = new RouteElement();
			e7.deriveFrom(e6);
			e7.setDepotContainer(mCode2DepotContainer.get(container
					.getDepotContainerCode()));
			e7.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e7.setContainer(null);
			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotContainer.get(
					container.getDepotContainerCode())
					.getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);

			RouteElement e8 = new RouteElement();
			e8.deriveFrom(e7);
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e8.setMooc(sel_combo.mooc);
			travelTime = getTravelTime(e7, e8);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode())
					.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e8, arrivalTime);
			mPoint2DepartureTime.put(e8, departureTime);

			RouteElement e9 = new RouteElement();
			e9.deriveFrom(e8);
			e9.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e9.setAction(ActionEnum.REST_AT_DEPOT);
			travelTime = getTravelTime(e8, e9);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e9, arrivalTime);
			mPoint2DepartureTime.put(e9, departureTime);
			
			
			
			// update last depot and last time of truck
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			tr.removeNodesAfter(e);
			tr.addNodes(L);
			
			return tr;
			
		}
		return null;
	}

	public TruckRouteInfo4Request createNewDirectRoute4WarehouseRequest(WarehouseContainerTransportRequest r){
		String fromWarehouseCode = r.getFromWarehouseCode();
		String toWarehouseCode = r.getToWarehouseCode();
		logln(name() + "::createDirectRoute4WarehouseRequest, fromWarehouseCode = " + fromWarehouseCode + 
				", toWarehouseCode = " + toWarehouseCode);
		
		Warehouse fromWarehouse = mCode2Warehouse.get(fromWarehouseCode);
		Warehouse toWarehouse = mCode2Warehouse.get(toWarehouseCode);
		
		String fromWarehouseLocationCode = fromWarehouse.getLocationCode();
		String toWarehouseLocationCode = toWarehouse.getLocationCode();
		
		int earlyPickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getEarlyDateTimeLoad());
		int latePickupWarehouse = (int)DateTimeUtils.dateTime2Int(r.getLateDateTimeLoad());
		
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
					//HashSet<Container> C = mShipCompanyCode2Containers.get(r.getShipCompanyCode());
					
					//if(!C.contains(container)) continue;
					//if(!r.getShipCompanyCode().equals(shipCompanyCode)) continue;
					
					ComboContainerMoocTruck combo = findLastAvailable(truck, mooc, container);
					if(combo == null) continue;
					
					int startTime = combo.startTime;
					String startLocationCode = combo.lastLocationCode;
					//System.out.println(name() + "::createDirectRoute4ExportRequest, lastLocationCode = " + combo.lastLocationCode);
					
					int arrivalTimeWarehouse = startTime + getTravelTime(startLocationCode, fromWarehouseLocationCode);
					
					//if(combo.truck.getCode().equals("Truck0001") && combo.mooc.getCode().equals("Mooc0002") &&
					//		combo.container.getCode().equals("Container002"))
					//	System.out.println(name() + "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = " + 
					//DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeWarehouse) + 
					//		", latePickupWarehouse = " + DateTimeUtils.unixTimeStamp2DateTime(latePickupWarehouse));
					
					if(arrivalTimeWarehouse > latePickupWarehouse) continue;
					
					double distance = combo.extraDistance + getDistance(combo.lastLocationCode, fromWarehouseLocationCode);
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
			logln(name() + "::createDirectRoute4WarehouseRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if(sel_combo.routeElement == null){
			
			TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r, sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out.println(name() + "::createDirectRoute4WarehouseRequest, create route from all depots, truck = " + sel_combo.truck.getCode()
					+ ", mooc = " + sel_combo.mooc.getCode() + ", container = " + sel_combo.container.getCode());
			//if(mTruck2Route.get(sel_combo.truck) == null)
			//	return tr;
			//TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			//old_tr.concat(tr);
			
			//return old_tr;
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;
			
		}else if(sel_combo.routeElement.getAction().equals(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)){
			System.out.println(name() + "::createDirectRoute4WarehouseRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");
			
			Truck truck = sel_combo.truck;
			//TruckRoute tr = mTruck2Route.get(truck);
			TruckItinerary I = mTruck2Itinerary.get(truck);
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
			e3.setWarehouse(fromWarehouse);
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setWarehouseRequest(r);

			travelTime = getTravelTime(e2, e3);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r.getLateDateTimeLoad()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			e5.deriveFrom(e4);
			e5.setWarehouse(toWarehouse);
			e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			finishedServiceTime = startServiceTime + r.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);

			RouteElement e6 = new RouteElement();
			e6.deriveFrom(e5);
			e6.setWarehouse(toWarehouse);
			e6.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouseRequest(null);
			travelTime = getTravelTime(e5, e6);
			arrivalTime = departureTime + travelTime;
			startServiceTime = finishedServiceTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);

			RouteElement e7 = new RouteElement();
			e7.deriveFrom(e6);
			e7.setDepotContainer(mCode2DepotContainer.get(container
					.getDepotContainerCode()));
			e7.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e7.setContainer(null);
			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotContainer.get(
					container.getDepotContainerCode())
					.getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);

			RouteElement e8 = new RouteElement();
			e8.deriveFrom(e7);
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e8.setMooc(sel_combo.mooc);
			travelTime = getTravelTime(e7, e8);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode())
					.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e8, arrivalTime);
			mPoint2DepartureTime.put(e8, departureTime);

			RouteElement e9 = new RouteElement();
			e9.deriveFrom(e8);
			e9.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e9.setAction(ActionEnum.REST_AT_DEPOT);
			travelTime = getTravelTime(e8, e9);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e9, arrivalTime);
			mPoint2DepartureTime.put(e9, departureTime);
			
			
			
			// update last depot and last time of truck
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			//tr.removeNodesAfter(e);
			//tr.addNodes(L);
			//return tr;
			
			TruckRoute newTr = new TruckRoute(truck,L);
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;
			
			return tri;
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
			e3.setWarehouse(mCode2Warehouse.get(fromWarehouse));
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setWarehouseRequest(r);

			int travelTime = getTravelTime(e, e3);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					(int) DateTimeUtils.dateTime2Int(r.getEarlyDateTimeLoad()));
			int finishedServiceTime = startServiceTime + r.getLoadDuration();
			int duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e3, arrivalTime);
			mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e3, e4);
			arrivalTime = departureTime + travelTime;
			startServiceTime = MAX(arrivalTime, finishedServiceTime);
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e4, arrivalTime);
			mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			e5.deriveFrom(e4);
			e5.setWarehouse(toWarehouse);
			e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			travelTime = getTravelTime(e4, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			finishedServiceTime = startServiceTime + r.getUnloadDuration();
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);

			RouteElement e6 = new RouteElement();
			e6.deriveFrom(e5);
			e6.setWarehouse(toWarehouse);
			e6.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouseRequest(null);
			travelTime = getTravelTime(e5, e6);
			arrivalTime = departureTime + travelTime;
			startServiceTime = finishedServiceTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e6, arrivalTime);
			mPoint2DepartureTime.put(e6, departureTime);

			RouteElement e7 = new RouteElement();
			e7.deriveFrom(e6);
			e7.setDepotContainer(mCode2DepotContainer.get(container
					.getDepotContainerCode()));
			e7.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e7.setContainer(null);
			travelTime = getTravelTime(e6, e7);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotContainer.get(
					container.getDepotContainerCode())
					.getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e7, arrivalTime);
			mPoint2DepartureTime.put(e7, departureTime);

			RouteElement e8 = new RouteElement();
			e8.deriveFrom(e7);
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode()));
			e8.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e8.setMooc(sel_combo.mooc);
			travelTime = getTravelTime(e7, e8);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = mCode2DepotMooc.get(sel_combo.mooc.getDepotMoocCode())
					.getDeliveryMoocDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e8, arrivalTime);
			mPoint2DepartureTime.put(e8, departureTime);

			RouteElement e9 = new RouteElement();
			e9.deriveFrom(e8);
			e9.setDepotTruck(mCode2DepotTruck.get(truck.getDepotTruckCode()));
			e9.setAction(ActionEnum.REST_AT_DEPOT);
			travelTime = getTravelTime(e8, e9);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = 0;
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e9, arrivalTime);
			mPoint2DepartureTime.put(e9, departureTime);
			
			
			
			// update last depot and last time of truck
			mTruck2LastDepot.put(truck, e7.getDepotTruck());
			mTruck2LastTime.put(truck, departureTime);
			
			//tr.removeNodesAfter(e);
			//tr.addNodes(L);
			//return tr;
			
			TruckRoute newTr = new TruckRoute(truck,L);
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = minDistance;
			
			return tri;
			
		}
		return null;
	}

}
