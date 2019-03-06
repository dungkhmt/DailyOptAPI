package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.havestplanning.solver.Solver;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.CandidateRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.CandidateSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.DoubleExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DoubleImportEmptyRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DoubleImportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerFromDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.EmptyContainerToDepotRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportEmptyRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportLadenRequests;
import routingdelivery.smartlog.containertruckmoocassigment.model.Individual2ImportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualDoubleImportEmptyRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualEmptyContainerFromDepotRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualEmptyContainerToDepotRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportEmptyRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportLadenRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportEmptyRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportImportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportLadenRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportWarehouseRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualTransportContainerRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.KepLechRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.KepRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.Measure;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapImportExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.TangboImportWarehouseRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.TangboWarehouseExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.TransportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteList;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseTransportRequest;
import utils.DateTimeUtils;

public class InitGreedyImproveSpecialOperatorSolver extends
		ContainerTruckMoocSolver {

	public CandidateSolution cand_sol;
	public ArrayList<ExportContainerRequest> queeuExportRequest;
	public ArrayList<ImportContainerRequest> queeuImportRequest;
	public ArrayList<WarehouseContainerTransportRequest> queeuWarehouseRequest;
	public HashMap<TruckRoute, HashSet<TruckRoute>> influcencedRoutes;

	Truck[] trucks;
	Mooc[] moocs;
	Container[] containers;
	ExportContainerRequest[] exReq;
	ImportContainerRequest[] imReq;
	WarehouseContainerTransportRequest[] whReq;
	EmptyContainerFromDepotRequest[] emptyContainerFromDepotReq;
	EmptyContainerToDepotRequest[] emptyContainerToDepotReq;
	TransportContainerRequest[] transportContainerReq;
	ExportLadenRequests[] exLadenReq;
	ExportEmptyRequests[] exEmptyReq;
	ImportLadenRequests[] imLadenReq;
	ImportEmptyRequests[] imEmptyReq;
	Container20Requests cont20reqs;
	
	int nbExReqs;
	int nbImReqs;
	int nbWhReqs;
	int nbEmptyContainerFromDepotReqs;
	int nbEmptyContainerToDepotReqs;
	int nbTransportContainerReqs;
	int nbExLadenReqs;
	int nbExEmptyReqs;
	int nbImLadenReqs;
	int nbImEmptyReqs;
	
	boolean[] exReqScheduled;
	boolean[] imReqScheduled;
	boolean[] whReqScheduled;
	boolean[] emptyContainerFromDepotReqScheduled;
	boolean[] emptyContainerToDepotReqScheduled;
	boolean[] transportContainerReqScheduled;
	boolean[] imLadenReqScheduled;
	boolean[] imEmptyReqScheduled;
	boolean[] exLadenReqScheduled;
	boolean[] exEmptyReqScheduled;
	

	HashMap<ExportContainerRequest, Integer> mExReq2Index;
	HashMap<ImportContainerRequest, Integer> mImReq2Index;
	HashMap<WarehouseContainerTransportRequest, Integer> mWhReq2Index;
	HashMap<EmptyContainerFromDepotRequest, Integer> mEmptyContainerFromDepotReq2Index;
	HashMap<EmptyContainerToDepotRequest, Integer> mEmptyContainerToDepotReq2Index;
	HashMap<TransportContainerRequest, Integer> mTransportContainerReq2Index;
	HashMap<ImportLadenRequests, Integer> mImportLadenRequest2Index;
	HashMap<ImportEmptyRequests, Integer> mImportEmptyRequest2Index;
	HashMap<ExportLadenRequests, Integer> mExportLadenRequest2Index;
	HashMap<ExportEmptyRequests, Integer> mExportEmptyRequest2Index;

	HashMap<ExportContainerRequest, Integer> mExReq2Code;
	HashMap<ImportContainerRequest, Integer> mImReq2Code;
	HashMap<WarehouseContainerTransportRequest, Integer> mWhReq2Code;
	HashMap<EmptyContainerFromDepotRequest, Integer> mEmptyContainerFromDepotReq2Code;
	HashMap<EmptyContainerToDepotRequest, Integer> mEmptyContainerToDepotReq2Code;
	HashMap<TransportContainerRequest, Integer> mTransportContainerReq2Code;
	HashMap<ImportLadenRequests, Integer> mImportLadenRequest2Code;
	HashMap<ImportEmptyRequests, Integer> mImportEmptyRequest2Code;
	HashMap<ExportLadenRequests, Integer> mExportLadenRequest2Code;
	HashMap<ExportEmptyRequests, Integer> mExportEmptyRequest2Code;
	
	public String name() {
		return "InitGreedyImproveSpecialOperatorSolver";
	}

	
	public void initDirectRoutes() {
		ArrayList<ExportContainerRequest> s_exReq = new ArrayList<ExportContainerRequest>();
		ArrayList<ImportContainerRequest> s_imReq = new ArrayList<ImportContainerRequest>();
		ArrayList<WarehouseContainerTransportRequest> s_whReq = new ArrayList<WarehouseContainerTransportRequest>();

		Truck[] trucks = input.getTrucks();
		Mooc[] moocs = input.getMoocs();
		Container[] containers = input.getContainers();
		ExportContainerRequest[] exReq = getSortedExportRequests();
		ImportContainerRequest[] imReq = getSortedImportRequests();
		WarehouseContainerTransportRequest[] whReq = getSortedWarehouseTransportRequests();

		for (int i = 0; i < exReq.length; i++)
			s_exReq.add(exReq[i]);
		for (int i = 0; i < imReq.length; i++)
			s_imReq.add(imReq[i]);
		for (int i = 0; i < whReq.length; i++)
			s_whReq.add(whReq[i]);

		while (s_exReq.size() + s_imReq.size() + s_whReq.size() > 0) {
			ExportContainerRequest er = null;
			if (s_exReq.size() > 0)
				er = s_exReq.get(0);

			ImportContainerRequest ir = null;
			if (s_imReq.size() > 0)
				ir = s_imReq.get(0);

			WarehouseContainerTransportRequest wr = null;
			if (s_whReq.size() > 0)
				wr = s_whReq.get(0);

			if (processBefore(er, ir)) {
				if (processBefore(er, wr)) {
					TruckRouteInfo4Request tri = createNewDirectRoute4ExportRequest(er);
					if (tri == null)
						break;
					commitItinerary(tri);
					s_exReq.remove(0);
				} else {
					TruckRouteInfo4Request tri = createNewDirectRoute4WarehouseRequest(wr);
					if (tri == null)
						break;
					commitItinerary(tri);
					s_whReq.remove(0);
				}
			} else {
				if (processBefore(ir, wr)) {
					TruckRouteInfo4Request tri = createNewDirectRoute4ImportRequest(ir);
					if (tri == null)
						break;
					commitItinerary(tri);
					s_imReq.remove(0);
				} else {
					TruckRouteInfo4Request tri = createNewDirectRoute4WarehouseRequest(wr);
					if (tri == null)
						break;
					commitItinerary(tri);
					s_whReq.remove(0);
				}
			}
		}

	}

	/*
	public void exploreDoubleImport(
			CandidateRouteComposer candidateRouteComposer) {
		ImportContainerRequest sel_imReq_k = null;
		ImportContainerRequest sel_imReq_q = null;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		TruckRouteInfo4Request sel_tri = null;
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_route = null;

		RouteDoubleImportCreator routeDoubleImportCreator = new RouteDoubleImportCreator(
				this);

		for (int i = 0; i < trucks.length; i++) {
			for (int j = 0; j < moocs.length; j++) {
				for (int k = 0; k < imReq.length; k++) {
					for (int q = k + 1; q < imReq.length; q++) {
						if (imReqScheduled[k] || imReqScheduled[q])
							continue;

						backup();
						TruckRouteInfo4Request tri = routeDoubleImportCreator
								.createDoubleImportRequest(trucks[i], moocs[j],
										imReq[k], imReq[q]);
						if (tri == null) {
							// System.out.println(name() +
							// "::solve, cannot find routeDoubleImport for (" +
							// i + "," + j + "," + k + "," + q + ")");
							logln(name()
									+ "::solve, cannot find routeDoubleImport for ("
									+ i + "," + j + "," + k + "," + q + ")");
						} else {
							TruckRoute tr = tri.route;
							// System.out.println(name() +
							// "::solve, FOUND routeDoubleImport for (" + i +
							// "," + j + "," + k + "," + q + "), route = " +
							// tr.toString() + ", distance = " +
							// tr.getDistance());
							logln(name()
									+ "::solve, FOUND routeDoubleImport for ("
									+ i + "," + j + "," + k + "," + q
									+ "), route = " + tr.toString()
									+ ", distance = " + tr.getDistance());
							if (minDistance > tr.getDistance()) {
								minDistance = tr.getDistance();
								sel_imReq_k = imReq[k];
								sel_imReq_q = imReq[q];
								sel_truck = trucks[i];
								sel_mooc = moocs[j];
								sel_route = tr;
								sel_tri = tri;
							}
						}
						restore();
					}
				}
			}
		}
		if (sel_imReq_k != null) {
			DoubleImportRouteComposer cp = new DoubleImportRouteComposer(this,
					sel_truck, sel_mooc, sel_route, sel_imReq_k, sel_imReq_q,
					sel_tri, minDistance);
			System.out.println(name() + "::exploreDoubleImport, minDistace = "
					+ minDistance);

			TruckRoute sel_tr_k = null;
			TruckRoute sel_tr_q = null;
			TruckRouteInfo4Request sel_tri_k = null;
			TruckRouteInfo4Request sel_tri_q = null;
			double minDistance1 = Integer.MAX_VALUE;
			for (int i = 0; i < trucks.length; i++) {
				for (int j = 0; j < moocs.length; j++) {
					for (int i1 = 0; i1 < trucks.length; i1++) {
						for (int j1 = 0; j1 < moocs.length; j1++) {
							backup();
							TruckRouteInfo4Request tri_k = createRouteForImportRequest(
									sel_imReq_k, trucks[i], moocs[j]);
							TruckRouteInfo4Request tri_q = createRouteForImportRequest(
									sel_imReq_q, trucks[i1], moocs[j1]);
							// compute additional distance when creating these
							// routes
							TruckRoute tr_k = tri_k.route;
							TruckRoute tr_q = tri_q.route;
							double dis = tr_k.getDistance()
									- tr_k.getReducedDistance()
									+ tr_q.getDistance()
									- tr_q.getReducedDistance();
							if (dis < minDistance1) {
								minDistance1 = dis;
								sel_tr_k = tr_k;
								sel_tri_k = tri_k;
								sel_tri_q = tri_q;
							}
							restore();
						}
					}
				}
			}
			System.out.println(name() + "::exploreDoubleImport, minDistace1 = "
					+ minDistance1);

			if (minDistance1 < minDistance) {
				Individual2ImportRoutesComposer icp = new Individual2ImportRoutesComposer(
						this, sel_tr_k, sel_tr_q, sel_imReq_k, sel_imReq_q,
						sel_tri_k, sel_tri_q, minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(cp);
			}
		}

	}
	*/
	public Measure evaluateSwapImportExport(RouteSwapImportExportCreator routeSwapImportExportCreator){
		Measure minMs = null;

		for (int a = 0; a < nbExReqs; a++) {
			if (exReqScheduled[a] || !exReq[a].getIsSwap())
				continue;
			//if(checkConstraintWarehouseVendor(exReq[a]) == false)
				//continue;
			for (int b = 0; b < nbImReqs; b++) {
				if (imReqScheduled[b] || !imReq[b].getIsSwap())
					continue;
				//if(checkConstraintWarehouseVendor(imReq[b]) == false)
					//continue;
				if(!exReq[a].getOrderItemSwapID().equals(imReq[b].getOrderItemID()))
					continue;
				//if(!exReq[a].getShipCompanyCode().equals(imReq[b].getShipCompanyCode()))
					//continue;
				//if(!exReq[a].getContainerCategory().equals(imReq[b].getContainerCategory()))
					//continue;
				
				double maxW = imReq[b].getWeight() > exReq[a].getWeight() ?
						imReq[b].getWeight() : exReq[a].getWeight();
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < exReq[a].getPickupWarehouses().length; t++)
					whLocationCode.add(exReq[a].getPickupWarehouses()[t].getWareHouseCode());
				for(int t = 0; t < imReq[b].getDeliveryWarehouses().length; t++)
					whLocationCode.add(imReq[b].getDeliveryWarehouses()[t].getWareHouseCode());
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(imReq[b].getOrderCode(),
							exReq[a].getOrderCode(), maxW, 
							imReq[b].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									imReq[b].getOrderCode(),
									exReq[a].getOrderCode(),
									maxW, whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = routeSwapImportExportCreator.evaluateSwapImportExport(
										avaiTruckList.get(j), avaiMoocList.get(k), imReq[b], exReq[a]);
								if(checkVehicleConstraintType(
										routeSwapImportExportCreator.sel_exReq_a,
										exReq[a], minMs, ms)
									&& checkVehicleConstraintType(
											routeSwapImportExportCreator.sel_imReq_b,
											imReq[b], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									routeSwapImportExportCreator.sel_truck = avaiTruckList.get(j);
									routeSwapImportExportCreator.sel_mooc = avaiMoocList.get(k);
									routeSwapImportExportCreator.sel_exReq_a = exReq[a];
									routeSwapImportExportCreator.sel_imReq_b = imReq[b];
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public void exploreSwapImportExport(
			CandidateRouteComposer candidateRouteComposer) {
		RouteSwapImportExportCreator routeSwapImportExportCreator = new RouteSwapImportExportCreator(
				this);
		Measure minMsSwap = null;
		minMsSwap = evaluateSwapImportExport(routeSwapImportExportCreator);
		if(minMsSwap != null && routeSwapImportExportCreator.sel_truck != null){
			TruckRouteInfo4Request tri = routeSwapImportExportCreator
					.createSwapImportExport();
			if(tri != null){
				SwapImportExportRouteComposer cp = new SwapImportExportRouteComposer(
						this, routeSwapImportExportCreator.sel_truck, 
						routeSwapImportExportCreator.sel_mooc, 
						routeSwapImportExportCreator.sel_exReq_a, 
						routeSwapImportExportCreator.sel_imReq_b,
						tri, minMsSwap.distance);
				candidateRouteComposer.add(cp);
//				Truck sel_truck_ex = null;
//				Truck sel_truck_im = null;
//				Mooc sel_mooc_ex = null;
//				Mooc sel_mooc_im = null;
//				Container sel_cont = null;
//				Measure minMs1 = null; 
//				Measure minMs2 = null; 
//				double minDistance1 = Integer.MAX_VALUE;
//				
//				ArrayList<String> whLocationCodeEx = new ArrayList<String>();
//				for(int t = 0; t < routeSwapImportExportCreator.sel_exReq_a.getPickupWarehouses().length; t++)
//					whLocationCodeEx.add(routeSwapImportExportCreator.sel_exReq_a.getPickupWarehouses()[t].getWareHouseCode());
//				
//				ArrayList<String> whLocationCodeIm = new ArrayList<String>();
//				for(int t = 0; t < routeSwapImportExportCreator.sel_imReq_b.getDeliveryWarehouses().length; t++)
//					whLocationCodeIm.add(routeSwapImportExportCreator.sel_imReq_b.getDeliveryWarehouses()[t].getWareHouseCode());
//				
//				for (String keyC : mDepot2ContainerList.keySet()) {
//					ArrayList<Container> avaiContList = getAvailableContainerAtDepot(
//							routeSwapImportExportCreator.sel_exReq_a.getWeight(), 
//							routeSwapImportExportCreator.sel_exReq_a.getContainerCategory(), 
//							routeSwapImportExportCreator.sel_exReq_a.getShipCompanyCode(),
//							keyC);
//					for(int q = 0; q < avaiContList.size(); q++){
//						for (String keyM : mDepot2MoocList.keySet()) {
//							ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
//									routeSwapImportExportCreator.sel_exReq_a.getWeight(), 
//									routeSwapImportExportCreator.sel_exReq_a.getContainerCategory(), keyM);
//							for(int k = 0; k < avaiMoocList.size(); k++){
//								for(String keyT : mDepot2TruckList.keySet()) {
//									ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
//											routeSwapImportExportCreator.sel_exReq_a.getWeight(), 
//											whLocationCodeEx, keyT);
//									for(int j = 0; j < avaiTruckList.size(); j++){
//										for (String keyMIm : mDepot2MoocList.keySet()) {
//											ArrayList<Mooc> avaiMoocListIm = getAvailableMoocAtDepot(
//													routeSwapImportExportCreator.sel_imReq_b.getWeight(), 
//													routeSwapImportExportCreator.sel_imReq_b.getContainerCategory(), keyMIm);
//											for(int kI = 0; kI < avaiMoocListIm.size(); kI++){
//												for(String keyTIm : mDepot2TruckList.keySet()) {
//													ArrayList<Truck> avaiTruckListIm = getAvailableTruckAtDepot(
//															routeSwapImportExportCreator.sel_imReq_b.getWeight(),
//															whLocationCodeIm, keyTIm);
//													for(int jI = 0; jI < avaiTruckListIm.size(); jI++){
//														Measure msIm = evaluateImportRequest(
//																routeSwapImportExportCreator.sel_imReq_b, avaiTruckListIm.get(jI), avaiMoocListIm.get(kI));
//														Measure msEx = evaluateExportRoute(
//																routeSwapImportExportCreator.sel_exReq_a, 
//																avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
//														if(msIm != null && msEx != null){
//															double dis = msEx.distance + msIm.distance;
//															if (dis < minDistance1) {
//																minMs1 = msIm;
//																minMs2 = msEx;
//																minDistance1 = dis;
//																sel_truck_ex = avaiTruckList.get(j);
//																sel_truck_im = avaiTruckListIm.get(jI);
//																sel_mooc_ex = avaiMoocList.get(k);
//																sel_mooc_im = avaiMoocListIm.get(kI);
//																sel_cont = avaiContList.get(q);
//															}
//														}
//													}
//												}
//											}
//										}
//									}
//								}
//							}
//						}
//					}
//				}
//				if(sel_truck_ex != null && sel_truck_im != null && minDistance1 < minMsSwap.distance){
//					TruckRouteInfo4Request tri_im = createRouteForImportRequest(
//							routeSwapImportExportCreator.sel_imReq_b, sel_truck_im, sel_mooc_im);
//					TruckRouteInfo4Request tri_ex = createRouteForExportRequest(
//							routeSwapImportExportCreator.sel_exReq_a, sel_truck_ex, sel_mooc_ex,	sel_cont);
//					if (tri_im != null && tri_ex != null) {
//						IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(
//								this, minMs1, minMs2, tri_im.route, tri_ex.route, 
//								routeSwapImportExportCreator.sel_imReq_b, routeSwapImportExportCreator.sel_exReq_a,
//								tri_im, tri_ex, minDistance1);
//						candidateRouteComposer.add(icp);
//					}
//				}
//				else{
//					candidateRouteComposer.add(cp);
//				}
			}
		}
	}

	public Measure evaluateKepLech(RouteKeplechCreator routeKeplechCreator){
		Measure minMs = null;
		for (int a = 0; a < nbExReqs; a++) {
			if (exReqScheduled[a] || !exReq[a].getContainerCategory()
					.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(exReq[a]) == false)
				continue;
			for (int b = 0; b < nbImReqs; b++) {
				if (imReqScheduled[b] || !imReq[b].getContainerCategory()
						.contains(ContainerCategoryEnum.CATEGORY20)
					|| checkConstraintWarehouseVendor(imReq[b]) == false)
					continue;
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < exReq[a].getPickupWarehouses().length; t++)
					whLocationCode.add(exReq[a].getPickupWarehouses()[t].getWareHouseCode());
				for(int t = 0; t < imReq[b].getDeliveryWarehouses().length; t++)
					whLocationCode.add(imReq[b].getDeliveryWarehouses()[t].getWareHouseCode());
				
				for (String keyC : mDepot2ContainerList.keySet()) {
					ArrayList<Container> avaiContList = getAvailableContainerAtDepot(
							exReq[a].getWeight(), 
							exReq[a].getContainerCategory(),
							exReq[a].getShipCompanyCode(),
							keyC);
					for(int q = 0; q < avaiContList.size(); q++){
						for (String keyM : mDepot2MoocList.keySet()) {
							ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
									exReq[a].getOrderCode(), imReq[b].getOrderCode(),
									exReq[a].getWeight() + imReq[b].getWeight(), keyM);
							for(int k = 0; k < avaiMoocList.size(); k++){
								for(String keyT : mDepot2TruckList.keySet()) {
									ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
											exReq[a].getOrderCode(), imReq[b].getOrderCode(),
											exReq[a].getWeight() + imReq[b].getWeight(), 
											whLocationCode, keyT);
									for(int j = 0; j < avaiTruckList.size(); j++){
										Measure ms = routeKeplechCreator.evaluateKeplechRoute(avaiTruckList.get(j),
												avaiMoocList.get(k), avaiContList.get(q),
												exReq[a], imReq[b]);
										if(checkVehicleConstraintType(
												routeKeplechCreator.sel_exReq_a,
												exReq[a], minMs, ms)
											&& checkVehicleConstraintType(
													routeKeplechCreator.sel_imReq_b,
													imReq[b], minMs, ms)){
											minMs = ms;
											routeKeplechCreator.sel_truck = avaiTruckList.get(j);
											routeKeplechCreator.sel_mooc = avaiMoocList.get(k);
											routeKeplechCreator.sel_container = avaiContList.get(q);
											routeKeplechCreator.sel_exReq_a = exReq[a];
											routeKeplechCreator.sel_imReq_b = imReq[b];
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public Measure evaluateKepImportRequests(RouteDoubleImportCreator routeImImCreator){
		Measure minMs = null;
		
		for (int a = 0; a < nbImReqs; a++) {
			if (imReqScheduled[a] || !imReq[a].getContainerCategory()
					.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(imReq[a]) == false)
				continue;
			for (int b = 0; b < nbImReqs && b != a; b++) {
				if (imReqScheduled[b] || !imReq[b].getContainerCategory()
						.contains(ContainerCategoryEnum.CATEGORY20)
					|| checkConstraintWarehouseVendor(imReq[b]) == false)
					continue;
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < imReq[a].getDeliveryWarehouses().length; t++)
					whLocationCode.add(imReq[a].getDeliveryWarehouses()[t].getWareHouseCode());
				for(int t = 0; t < imReq[b].getDeliveryWarehouses().length; t++)
					whLocationCode.add(imReq[b].getDeliveryWarehouses()[t].getWareHouseCode());
				
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
							imReq[a].getOrderCode(), imReq[b].getOrderCode(),
							imReq[a].getWeight() + imReq[b].getWeight(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									imReq[a].getOrderCode(), imReq[b].getOrderCode(),
									imReq[a].getWeight() + imReq[b].getWeight(), 
									whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = routeImImCreator.evaluateImportImportRequest(
										imReq[a], imReq[b],
										avaiTruckList.get(j), avaiMoocList.get(k));
								if(checkVehicleConstraintType(
										routeImImCreator.sel_imReq_a,
										imReq[a], minMs, ms)
									&& checkVehicleConstraintType(
											routeImImCreator.sel_imReq_b,
											imReq[b], minMs, ms)){
									minMs = ms;
									routeImImCreator.sel_imReq_a = imReq[a];
									routeImImCreator.sel_imReq_b = imReq[b];
									routeImImCreator.sel_truck = avaiTruckList.get(j);
									routeImImCreator.sel_mooc = avaiMoocList.get(k);
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public Measure evaluateKepExportRequests(RouteDoubleExportCreator routeExExCreator){
		Measure minMs = null;
		
		for (int a = 0; a < nbExReqs; a++) {
			if (exReqScheduled[a] || !exReq[a].getContainerCategory()
					.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(exReq[a]) == false)
				continue;
			for (int b = 0; b < nbExReqs && b != a; b++) {
				if (exReqScheduled[b] || !exReq[b].getContainerCategory()
						.contains(ContainerCategoryEnum.CATEGORY20)
					|| checkConstraintWarehouseVendor(exReq[b]) == false)
					continue;
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < exReq[a].getPickupWarehouses().length; t++)
					whLocationCode.add(exReq[a].getPickupWarehouses()[t].getWareHouseCode());
				for(int t = 0; t < exReq[b].getPickupWarehouses().length; t++)
					whLocationCode.add(exReq[b].getPickupWarehouses()[t].getWareHouseCode());
				
				for (String keyC_a : mDepot2ContainerList.keySet()) {
					ArrayList<Container> avaiContList_a = getAvailableContainerAtDepot(
							exReq[a].getWeight(), 
							exReq[a].getContainerCategory(),
							exReq[a].getShipCompanyCode(),
							keyC_a);
					for(int qa = 0; qa < avaiContList_a.size(); qa++){
						for (String keyC_b : mDepot2ContainerList.keySet()) {
							ArrayList<Container> avaiContList_b = getAvailableContainerAtDepot(
									exReq[b].getWeight(), 
									exReq[b].getContainerCategory(),
									exReq[b].getShipCompanyCode(),
									keyC_b);
							for(int qb = 0; qb < avaiContList_b.size(); qb++){
								for (String keyM : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
											exReq[a].getOrderCode(), exReq[b].getOrderCode(),
											exReq[a].getWeight() + exReq[b].getWeight(), keyM);
									for(int k = 0; k < avaiMoocList.size(); k++){
										for(String keyT : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
													exReq[a].getOrderCode(), exReq[b].getOrderCode(),
													exReq[a].getWeight() + exReq[b].getWeight(),
													whLocationCode, keyT);
											for(int j = 0; j < avaiTruckList.size(); j++){
												Measure ms = routeExExCreator.evaluateExportExportRequest(
														exReq[a], exReq[b],
														avaiTruckList.get(j), avaiMoocList.get(k),
														avaiContList_a.get(qa), avaiContList_b.get(qb));
												if(checkVehicleConstraintType(
														routeExExCreator.sel_exReq_a,
														exReq[a], minMs, ms)
													&& checkVehicleConstraintType(
															routeExExCreator.sel_exReq_b,
															exReq[b], minMs, ms)){
													minMs = ms;
													routeExExCreator.sel_exReq_a = exReq[a];
													routeExExCreator.sel_exReq_b = exReq[b];
													routeExExCreator.sel_truck = avaiTruckList.get(j);
													routeExExCreator.sel_mooc = avaiMoocList.get(k);
													routeExExCreator.sel_container_a = avaiContList_a.get(qa);
													routeExExCreator.sel_container_b = avaiContList_a.get(qb);
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public Measure evaluateKepImEmptyRequests(RouteDoubleImportEmptyCreator routeDoubleImEmptyCreator){
		Measure minMs = null;
		
		for (int a = 0; a < nbImEmptyReqs; a++) {
			if (imEmptyReqScheduled[a] 
					|| !imEmptyReq[a].getContainerCategory()
					.contains(ContainerCategoryEnum.CATEGORY20)
					|| imEmptyReq[a].getIsBreakRomooc()
					|| checkConstraintWarehouseVendor(imEmptyReq[a]) == false)
				continue;
			for (int b = 0; b < nbImEmptyReqs && b != a; b++) {
				if (imEmptyReqScheduled[b] || !imEmptyReq[b].getContainerCategory()
						.contains(ContainerCategoryEnum.CATEGORY20)
						|| imEmptyReq[b].getIsBreakRomooc()
						|| checkConstraintWarehouseVendor(imEmptyReq[b]) == false)
					continue;
				ArrayList<String> whLocationCode = new ArrayList<String>();
				whLocationCode.add(imEmptyReq[a].getWareHouseCode());
				whLocationCode.add(imEmptyReq[b].getWareHouseCode());
				
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
							imEmptyReq[a].getOrderCode(), imEmptyReq[b].getOrderCode(),
							imEmptyReq[a].getWeight() + imEmptyReq[b].getWeight(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									imEmptyReq[a].getOrderCode(), imEmptyReq[b].getOrderCode(),
									imEmptyReq[a].getWeight() + imEmptyReq[b].getWeight(),
									whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = routeDoubleImEmptyCreator.evaluateImportEmptyImportEmptyRequest(
										imEmptyReq[a], imEmptyReq[b],
										avaiTruckList.get(j), avaiMoocList.get(k));
								if(checkVehicleConstraintType(
										routeDoubleImEmptyCreator.sel_imEmptyReq_a,
										imEmptyReq[a], minMs, ms)
									&& checkVehicleConstraintType(
											routeDoubleImEmptyCreator.sel_imEmptyReq_b,
											imEmptyReq[b], minMs, ms)){
									minMs = ms;
									routeDoubleImEmptyCreator.sel_imEmptyReq_a = imEmptyReq[a];
									routeDoubleImEmptyCreator.sel_imEmptyReq_b = imEmptyReq[b];
									routeDoubleImEmptyCreator.sel_truck = avaiTruckList.get(j);
									routeDoubleImEmptyCreator.sel_mooc = avaiMoocList.get(k);
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public void exploreKep(CandidateRouteComposer candidateRouteComposer) {
		TruckRouteInfo4Request sel_tri = null;
		
		Measure minMsKepImportReqs = null;
		Measure minMsKepExportReqs = null;
		Measure minMsKepLech 		= null;
		
		Measure minMsKepImEmptyReqs = null;
		Measure minMsKepImLadenReqs = null;
		Measure minMsKepExEmptyReqs = null;
		Measure minMsKepExLadenReqs = null;

		RouteKeplechCreator routeKepLechCreator 	= new RouteKeplechCreator(this);
		RouteDoubleImportCreator routeImImCreator 	= new RouteDoubleImportCreator(this);
		RouteDoubleExportCreator routeExExCreator 	= new RouteDoubleExportCreator(this);
		
		RouteDoubleImportEmptyCreator routeDoubleImEmptyCreator 	= new RouteDoubleImportEmptyCreator(this);
		RouteDoubleImportLadenCreator routeDoubleImLadenCreator 	= new RouteDoubleImportLadenCreator(this);
		RouteDoubleExportEmptyCreator routeDoubleExEmptyCreator 	= new RouteDoubleExportEmptyCreator(this);
		RouteDoubleExportLadenCreator routeDoubleExLadenCreator 	= new RouteDoubleExportLadenCreator(this);
		
		minMsKepImportReqs 	= evaluateKepImportRequests(routeImImCreator);
		minMsKepExportReqs 	= evaluateKepExportRequests(routeExExCreator);
		minMsKepLech 		= evaluateKepLech(routeKepLechCreator);
		
		minMsKepImEmptyReqs = evaluateKepImEmptyRequests(routeDoubleImEmptyCreator);
		String choose = "KEP_LECH";
		Measure minMs = Utils.MIN(minMsKepImportReqs, minMsKepExportReqs);
		minMs = Utils.MIN(minMs, minMsKepLech);
		minMs = Utils.MIN(minMs, minMsKepImEmptyReqs);
		
		if (minMs == minMsKepLech && routeKepLechCreator.sel_truck != null) {
			sel_tri = routeKepLechCreator
					.createKeplech();

			if(sel_tri != null){
				KepLechRouteComposer kcp = new KepLechRouteComposer(this,
						routeKepLechCreator.sel_truck, routeKepLechCreator.sel_mooc,
						routeKepLechCreator.sel_container, routeKepLechCreator.sel_imReq_b,
						routeKepLechCreator.sel_exReq_a, sel_tri, sel_tri.route.getDistance());

				Truck sel_truck_ex = null;
				Truck sel_truck_im = null;
				Mooc sel_mooc_ex = null;
				Mooc sel_mooc_im = null;
				Container sel_cont = null;
				TruckRouteInfo4Request sel_tri_ex = null;
				TruckRouteInfo4Request sel_tri_im = null;
				Measure ms1 = null;
				Measure ms2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeEx = new ArrayList<String>();
				for(int t = 0; t < routeKepLechCreator.sel_exReq_a.getPickupWarehouses().length; t++)
					whLocationCodeEx.add(routeKepLechCreator.sel_exReq_a.getPickupWarehouses()[t].getWareHouseCode());
				
				ArrayList<String> whLocationCodeIm = new ArrayList<String>();
				for(int t = 0; t < routeKepLechCreator.sel_imReq_b.getDeliveryWarehouses().length; t++)
					whLocationCodeIm.add(routeKepLechCreator.sel_imReq_b.getDeliveryWarehouses()[t].getWareHouseCode());
				
				for (String keyC : mDepot2ContainerList.keySet()) {
					ArrayList<Container> avaiContList = getAvailableContainerAtDepot(
							routeKepLechCreator.sel_exReq_a.getWeight(), 
							routeKepLechCreator.sel_exReq_a.getContainerCategory(),
							routeKepLechCreator.sel_exReq_a.getShipCompanyCode(),
							keyC);
					for(int q = 0; q < avaiContList.size(); q++){
						for (String keyM : mDepot2MoocList.keySet()) {
							ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
									routeKepLechCreator.sel_exReq_a.getOrderCode(), "",
									routeKepLechCreator.sel_exReq_a.getWeight(), 
									routeKepLechCreator.sel_exReq_a.getContainerCategory(), keyM);
							for(int k = 0; k < avaiMoocList.size(); k++){
								for(String keyT : mDepot2TruckList.keySet()) {
									ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
											routeKepLechCreator.sel_exReq_a.getOrderCode(), "",
											routeKepLechCreator.sel_exReq_a.getWeight(), 
											whLocationCodeEx, keyT);
									for(int j = 0; j < avaiTruckList.size(); j++){
										for (String keyMIm : mDepot2MoocList.keySet()) {
											ArrayList<Mooc> avaiMoocListIm = getAvailableMoocAtDepot(
													routeKepLechCreator.sel_imReq_b.getOrderCode(), "",
													routeKepLechCreator.sel_imReq_b.getWeight(), 
													routeKepLechCreator.sel_imReq_b.getContainerCategory(), keyMIm);
											for(int kI = 0; kI < avaiMoocListIm.size(); kI++){
												for(String keyTIm : mDepot2TruckList.keySet()) {
													ArrayList<Truck> avaiTruckListIm = getAvailableTruckAtDepot(
															routeKepLechCreator.sel_imReq_b.getOrderCode(), "",
															routeKepLechCreator.sel_imReq_b.getWeight(), 
															whLocationCodeIm, keyTIm);
													for(int jI = 0; jI < avaiTruckListIm.size(); jI++){
														Measure msIm = evaluateImportRequest(
																routeKepLechCreator.sel_imReq_b, avaiTruckListIm.get(jI), avaiMoocListIm.get(kI));
														Measure msEx = evaluateExportRoute(
																routeKepLechCreator.sel_exReq_a, 
																avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
														if(msIm != null
															&& msEx != null){
															double dis = msIm.distance + msEx.distance;
															if (dis < minDistance1) {
																ms1 = msIm;
																ms2 = msEx;
																minDistance1 = dis;
																sel_truck_ex = avaiTruckList.get(j);
																sel_truck_im = avaiTruckListIm.get(jI);
																sel_mooc_ex = avaiMoocList.get(k);
																sel_mooc_im = avaiMoocListIm.get(kI);
																sel_cont = avaiContList.get(q);
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_ex != null && sel_truck_im != null && minDistance1 < minMsKepLech.distance){
					TruckRouteInfo4Request tri_im = createRouteForImportRequest(
							routeKepLechCreator.sel_imReq_b, sel_truck_im, sel_mooc_im);
					TruckRouteInfo4Request tri_ex = createRouteForExportRequest(
							routeKepLechCreator.sel_exReq_a, sel_truck_ex, sel_mooc_ex,	sel_cont);
					if (tri_im != null && tri_ex != null) {
						IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(
								this, ms1, ms2, tri_im.route, tri_ex.route, 
								routeKepLechCreator.sel_imReq_b, routeKepLechCreator.sel_exReq_a,
								tri_im, tri_ex, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(kcp);
				}
			}
		}
		
		else if (minMs == minMsKepImportReqs && routeImImCreator.sel_truck != null) {
			sel_tri = routeImImCreator.createRouteForImportImportRequest();

			if(sel_tri != null){
				DoubleImportRouteComposer kcp = new DoubleImportRouteComposer(this,
						routeImImCreator.sel_truck, routeImImCreator.sel_mooc, sel_tri.route,
						routeImImCreator.sel_imReq_a, routeImImCreator.sel_imReq_b,
						sel_tri, sel_tri.route.getDistance());

				Truck sel_truck_im1 = null;
				Truck sel_truck_im2 = null;
				Mooc sel_mooc_im1 = null;
				Mooc sel_mooc_im2 = null;
				TruckRouteInfo4Request sel_tri_im1 = null;
				TruckRouteInfo4Request sel_tri_im2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeIm_a = new ArrayList<String>();
				for(int t = 0; t < routeImImCreator.sel_imReq_a.getDeliveryWarehouses().length; t++)
					whLocationCodeIm_a.add(routeImImCreator.sel_imReq_a.getDeliveryWarehouses()[t].getWareHouseCode());
				
				ArrayList<String> whLocationCodeIm_b = new ArrayList<String>();
				for(int t = 0; t < routeImImCreator.sel_imReq_b.getDeliveryWarehouses().length; t++)
					whLocationCodeIm_b.add(routeImImCreator.sel_imReq_b.getDeliveryWarehouses()[t].getWareHouseCode());
				
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							routeImImCreator.sel_imReq_a.getOrderCode(), "",
							routeImImCreator.sel_imReq_a.getWeight(), 
							routeImImCreator.sel_imReq_a.getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									routeImImCreator.sel_imReq_a.getOrderCode(), "",
									routeImImCreator.sel_imReq_a.getWeight(), 
									whLocationCodeIm_a, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								for (String keyMIm : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocListIm = getAvailableMoocAtDepot(
											routeImImCreator.sel_imReq_b.getOrderCode(), "",
											routeImImCreator.sel_imReq_b.getWeight(), 
											routeImImCreator.sel_imReq_b.getContainerCategory(), keyMIm);
									for(int kI = 0; kI < avaiMoocListIm.size(); kI++){
										for(String keyTIm : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckListIm = getAvailableTruckAtDepot(
													routeImImCreator.sel_imReq_b.getOrderCode(), "",
													routeImImCreator.sel_imReq_b.getWeight(),
													whLocationCodeIm_b, keyTIm);
											for(int jI = 0; jI < avaiTruckListIm.size(); jI++){
												Measure msIm1 = evaluateImportRequest(
														routeImImCreator.sel_imReq_a, 
														avaiTruckList.get(j), avaiMoocList.get(k));
												Measure msIm2 = evaluateImportRequest(
														routeImImCreator.sel_imReq_b, 
														avaiTruckListIm.get(jI), avaiMoocListIm.get(kI));
												if(msIm1 != null
													&& msIm2 != null){													
													double dis = msIm1.distance + msIm2.distance;
													if (dis < minDistance1) {
														minDistance1 = dis;
														sel_truck_im1 = avaiTruckList.get(j);
														sel_truck_im2 = avaiTruckListIm.get(jI);
														sel_mooc_im1 = avaiMoocList.get(k);
														sel_mooc_im2 = avaiMoocListIm.get(kI);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_im1 != null && sel_truck_im2 != null && minDistance1 < minMsKepImportReqs.distance){
					TruckRouteInfo4Request tri_im1 = createRouteForImportRequest(
							routeImImCreator.sel_imReq_a, sel_truck_im1, sel_mooc_im1);
					TruckRouteInfo4Request tri_im2 = createRouteForImportRequest(
							routeImImCreator.sel_imReq_b, sel_truck_im2, sel_mooc_im2);
					if (tri_im1 != null && tri_im2 != null) {
						IndividualImportImportRoutesComposer icp = new IndividualImportImportRoutesComposer(
								this, tri_im1.route, tri_im2.route, 
								routeImImCreator.sel_imReq_a, routeImImCreator.sel_imReq_b,
								tri_im1, tri_im2, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(kcp);
				}
			}
		}
		else if (minMs == minMsKepExportReqs && routeExExCreator.sel_truck != null) {
			sel_tri = routeExExCreator.createRouteForExportExportRequest();

			if(sel_tri != null){
				DoubleExportRouteComposer kcp = new DoubleExportRouteComposer(this,
						routeExExCreator.sel_truck, routeExExCreator.sel_mooc, sel_tri.route,
						routeExExCreator.sel_exReq_a, routeExExCreator.sel_exReq_b,
						sel_tri, sel_tri.route.getDistance());

				Truck sel_truck_ex1 = null;
				Truck sel_truck_ex2 = null;
				Mooc sel_mooc_ex1 = null;
				Mooc sel_mooc_ex2 = null;
				Container sel_container_ex1 = null;
				Container sel_container_ex2 = null;
				TruckRouteInfo4Request sel_tri_ex1 = null;
				TruckRouteInfo4Request sel_tri_ex2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeEx_a = new ArrayList<String>();
				for(int t = 0; t < routeExExCreator.sel_exReq_a.getPickupWarehouses().length; t++)
					whLocationCodeEx_a.add(routeExExCreator.sel_exReq_a.getPickupWarehouses()[t].getWareHouseCode());
				
				ArrayList<String> whLocationCodeEx_b = new ArrayList<String>();
				for(int t = 0; t < routeExExCreator.sel_exReq_b.getPickupWarehouses().length; t++)
					whLocationCodeEx_b.add(routeExExCreator.sel_exReq_b.getPickupWarehouses()[t].getWareHouseCode());
				
				for (String keyC1 : mDepot2ContainerList.keySet()) {
					ArrayList<Container> avaiContList1 = getAvailableContainerAtDepot(
							routeExExCreator.sel_exReq_a.getWeight(), 
							routeExExCreator.sel_exReq_a.getContainerCategory(),
							routeExExCreator.sel_exReq_a.getShipCompanyCode(),
							keyC1);
					for(int q1 = 0; q1 < avaiContList1.size(); q1++){
						for (String keyM1 : mDepot2MoocList.keySet()) {
							ArrayList<Mooc> avaiMoocList1 = getAvailableMoocAtDepot(
									routeExExCreator.sel_exReq_a.getOrderCode(), "",
									routeExExCreator.sel_exReq_a.getWeight(), 
									routeExExCreator.sel_exReq_a.getContainerCategory(), keyM1);
							for(int k1 = 0; k1 < avaiMoocList1.size(); k1++){
								for(String keyT1 : mDepot2TruckList.keySet()) {
									ArrayList<Truck> avaiTruckList1 = getAvailableTruckAtDepot(
											routeExExCreator.sel_exReq_a.getOrderCode(), "",
											routeExExCreator.sel_exReq_a.getWeight(), 
											whLocationCodeEx_a, keyT1);
									for(int j1 = 0; j1 < avaiTruckList1.size(); j1++){
										for (String keyC2 : mDepot2ContainerList.keySet()) {
											ArrayList<Container> avaiContList2 = getAvailableContainerAtDepot(
													routeExExCreator.sel_exReq_b.getWeight(), 
													routeExExCreator.sel_exReq_b.getContainerCategory(),
													routeExExCreator.sel_exReq_b.getShipCompanyCode(),
													keyC2);
											for(int q2 = 0; q2 < avaiContList2.size(); q2++){
												for (String keyM2 : mDepot2MoocList.keySet()) {
													ArrayList<Mooc> avaiMoocList2 = getAvailableMoocAtDepot(
															routeExExCreator.sel_exReq_b.getOrderCode(), "",
															routeExExCreator.sel_exReq_b.getWeight(), 
															routeExExCreator.sel_exReq_b.getContainerCategory(), keyM2);
													for(int k2 = 0; k2 < avaiMoocList2.size(); k2++){
														for(String keyT2 : mDepot2TruckList.keySet()) {
															ArrayList<Truck> avaiTruckList2 = getAvailableTruckAtDepot(
																	routeExExCreator.sel_exReq_b.getOrderCode(), "",
																	routeExExCreator.sel_exReq_b.getWeight(),
																	whLocationCodeEx_b, keyT2);
															for(int j2 = 0; j2 < avaiTruckList2.size(); j2++){
																Measure msEx1 = evaluateExportRoute(
																		routeExExCreator.sel_exReq_a, 
																		avaiTruckList1.get(j1), avaiMoocList1.get(k1),
																		avaiContList1.get(q1));
																Measure msEx2 = evaluateExportRoute(
																		routeExExCreator.sel_exReq_b,
																		avaiTruckList2.get(j2), avaiMoocList2.get(k2),
																		avaiContList2.get(q2));
																if(msEx1 != null
																	&& msEx2 != null){
																	double dis = msEx1.distance + msEx2.distance;
																	if (dis < minDistance1) {
																		minDistance1 = dis;
																		sel_truck_ex1 = avaiTruckList1.get(j1);
																		sel_truck_ex2 = avaiTruckList2.get(j2);
																		sel_mooc_ex1 = avaiMoocList1.get(k1);
																		sel_mooc_ex2 = avaiMoocList2.get(k2);
																		sel_container_ex1 = avaiContList1.get(q1);
																		sel_container_ex2 = avaiContList2.get(q2);
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_ex1 != null && sel_truck_ex2 != null && minDistance1 < minMsKepExportReqs.distance){
					TruckRouteInfo4Request tri_ex1 = createRouteForExportRequest(
							routeExExCreator.sel_exReq_a, sel_truck_ex1, sel_mooc_ex1, sel_container_ex1);
					TruckRouteInfo4Request tri_ex2 = createRouteForExportRequest(
							routeExExCreator.sel_exReq_b, sel_truck_ex2, sel_mooc_ex2, sel_container_ex2);
					if (tri_ex1 != null && tri_ex2 != null) {
						IndividualExportExportRoutesComposer icp = new IndividualExportExportRoutesComposer(
								this, tri_ex1.route, tri_ex2.route, 
								routeExExCreator.sel_exReq_a, routeExExCreator.sel_exReq_b,
								tri_ex1, tri_ex2, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(kcp);
				}
			}
		}
		else if (minMs == minMsKepImEmptyReqs && routeDoubleImEmptyCreator.sel_truck != null) {
			sel_tri = routeDoubleImEmptyCreator
					.createRouteForImportEmptyImportEmptyRequest();

			if(sel_tri != null){
				DoubleImportEmptyRouteComposer kcp = new DoubleImportEmptyRouteComposer(this,
						routeDoubleImEmptyCreator.sel_truck, routeDoubleImEmptyCreator.sel_mooc, sel_tri.route,
						routeDoubleImEmptyCreator.sel_imEmptyReq_a, routeDoubleImEmptyCreator.sel_imEmptyReq_b,
						sel_tri, sel_tri.route.getDistance());

				Truck sel_truck_imEmpty1 = null;
				Truck sel_truck_imEmpty2 = null;
				Mooc sel_mooc_imEmpty1 = null;
				Mooc sel_mooc_imEmpty2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeExImEmpty_a = new ArrayList<String>();
				whLocationCodeExImEmpty_a.add(routeDoubleImEmptyCreator.sel_imEmptyReq_a.getWareHouseCode());
				
				ArrayList<String> whLocationCodeExImEmpty_b = new ArrayList<String>();
				whLocationCodeExImEmpty_b.add(routeDoubleImEmptyCreator.sel_imEmptyReq_b.getWareHouseCode());
				
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							routeDoubleImEmptyCreator.sel_imEmptyReq_a.getOrderCode(), "",
							routeDoubleImEmptyCreator.sel_imEmptyReq_a.getWeight(), 
							routeDoubleImEmptyCreator.sel_imEmptyReq_a.getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									routeDoubleImEmptyCreator.sel_imEmptyReq_a.getOrderCode(), "",
									routeDoubleImEmptyCreator.sel_imEmptyReq_a.getWeight(),
									whLocationCodeExImEmpty_a, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								for (String keyMIm : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocListIm = getAvailableMoocAtDepot(
											routeDoubleImEmptyCreator.sel_imEmptyReq_b.getOrderCode(), "",
											routeDoubleImEmptyCreator.sel_imEmptyReq_b.getWeight(), 
											routeDoubleImEmptyCreator.sel_imEmptyReq_b.getContainerCategory(), keyMIm);
									for(int kI = 0; kI < avaiMoocListIm.size(); kI++){
										for(String keyTIm : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckListIm = getAvailableTruckAtDepot(
													routeDoubleImEmptyCreator.sel_imEmptyReq_b.getOrderCode(), "",
													routeDoubleImEmptyCreator.sel_imEmptyReq_b.getWeight(),
													whLocationCodeExImEmpty_b, keyTIm);
											for(int jI = 0; jI < avaiTruckListIm.size(); jI++){
												Measure msIm1 = evaluateImportEmptyRequest(
														routeDoubleImEmptyCreator.sel_imEmptyReq_a, 
														avaiTruckList.get(j), avaiMoocList.get(k));
												Measure msIm2 = evaluateImportEmptyRequest(
														routeDoubleImEmptyCreator.sel_imEmptyReq_b, 
														avaiTruckListIm.get(jI), avaiMoocListIm.get(kI));		
												if(msIm1 != null
													&& msIm2 != null){
													double dis = msIm1.distance + msIm2.distance;
													if (dis < minDistance1) {
														minDistance1 = dis;
														sel_truck_imEmpty1 = avaiTruckList.get(j);
														sel_truck_imEmpty2 = avaiTruckListIm.get(jI);
														sel_mooc_imEmpty1 = avaiMoocList.get(k);
														sel_mooc_imEmpty2 = avaiMoocListIm.get(kI);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_imEmpty1 != null && sel_truck_imEmpty2 != null
						&& minDistance1 < minMsKepImEmptyReqs.distance){
					TruckRouteInfo4Request tri_imEmpty1 = createRouteForImportEmptyRequest(
							routeDoubleImEmptyCreator.sel_imEmptyReq_a, sel_truck_imEmpty1, sel_mooc_imEmpty1);
					TruckRouteInfo4Request tri_imEmpty2 = createRouteForImportEmptyRequest(
							routeDoubleImEmptyCreator.sel_imEmptyReq_b, sel_truck_imEmpty2, sel_mooc_imEmpty2);
					if (tri_imEmpty1 != null && tri_imEmpty2 != null) {
						IndividualDoubleImportEmptyRoutesComposer icp = new IndividualDoubleImportEmptyRoutesComposer(
								this, tri_imEmpty1.route, tri_imEmpty2.route, 
								routeDoubleImEmptyCreator.sel_imEmptyReq_a, routeDoubleImEmptyCreator.sel_imEmptyReq_b,
								tri_imEmpty1, tri_imEmpty2, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(kcp);
				}
			}
		}
	}
	
	public void exploreKepContainer(Container20Requests cont20reqs, CandidateRouteComposer candidateRouteComposer){
			TruckRouteInfo4Request sel_tri = null;
			Measure minMs = null;
			KepGenerator sel_kg = null;
			for(int kRE_a : cont20reqs.cont20RE.keySet()){
				for(int kRE_b : cont20reqs.cont20RE.keySet()){
					if(kRE_b == kRE_a) continue;
					
					RouteElement[] RE = new RouteElement[
					    cont20reqs.cont20RE.get(kRE_a).size() + cont20reqs.cont20RE.get(kRE_b).size()];
					for(int i = 0; i < cont20reqs.cont20RE.get(kRE_a).size(); i++){
						RE[i] = cont20reqs.cont20RE.get(kRE_a).get(i);
						RE[i].setKepValue(false);
					}
					for(int i = 0; i < cont20reqs.cont20RE.get(kRE_b).size(); i++){
						RE[i + cont20reqs.cont20RE.get(kRE_a).size()] = cont20reqs.cont20RE.get(kRE_b).get(i);
						RE[i + cont20reqs.cont20RE.get(kRE_a).size()].setKepValue(true);
					}

					if(cont20reqs.isNeedCont.get(kRE_a) == 1 && cont20reqs.isNeedCont.get(kRE_b) == 1){
						ArrayList<String> whLocationCode = new ArrayList<String>();
						for(int t = 0; t < cont20reqs.whCode.get(kRE_a).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_a).get(t));
						for(int t = 0; t < cont20reqs.whCode.get(kRE_b).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_b).get(t));			
						for (String keyC_a : mDepot2ContainerList.keySet()) {
							ArrayList<Container> avaiContList_a = getAvailableContainerAtDepot(
									cont20reqs.weights.get(kRE_a), 
									cont20reqs.contCategory.get(kRE_a),
									cont20reqs.shipCompanyCode.get(kRE_a),
									keyC_a);
							for(int qa = 0; qa < avaiContList_a.size(); qa++){
								for (String keyC_b : mDepot2ContainerList.keySet()) {
									ArrayList<Container> avaiContList_b = getAvailableContainerAtDepot(
											cont20reqs.weights.get(kRE_b), 
											cont20reqs.contCategory.get(kRE_b),
											cont20reqs.shipCompanyCode.get(kRE_b),
											keyC_b);
									for(int qb = 0; qb < avaiContList_b.size(); qb++){
										if(avaiContList_b.get(qb) == avaiContList_a.get(qa))
											continue;
										for (String keyM : mDepot2MoocList.keySet()) {
											ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
													cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
													cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b), keyM);
											for(int k = 0; k < avaiMoocList.size(); k++){
												for(String keyT : mDepot2TruckList.keySet()) {
													ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
															cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
															cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b),
															whLocationCode, keyT);
													for(int j = 0; j < avaiTruckList.size(); j++){
														KepGenerator kg = new KepGenerator(this);
														Measure ms = kg.evaluateKepRoute(
																avaiTruckList.get(j),
																avaiMoocList.get(k),
																avaiContList_a.get(qa),
																avaiContList_b.get(qb),
																cont20reqs.isNeedCont.get(kRE_a),
																cont20reqs.isNeedCont.get(kRE_b),
																cont20reqs.isNeedReturnCont.get(kRE_a),
																cont20reqs.isNeedReturnCont.get(kRE_b),
																cont20reqs.returnContDepotCode.get(kRE_a),
																cont20reqs.returnContDepotCode.get(kRE_b),
																cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a),
																cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b),
																RE);
														if((sel_kg == null && ms != null)
															|| (ms != null && checkVehicleConstraintType(
																sel_kg.sel_whCode_a,
																cont20reqs.whCode.get(kRE_a), minMs, ms)
															&& checkVehicleConstraintType(
																	sel_kg.sel_whCode_b,
																	cont20reqs.whCode.get(kRE_b), minMs, ms))
															&& checkConstraintWarehouseHard(minMs, ms)
															&& checkConstraintDriverBalance(minMs, ms)){
															minMs = ms;
															kg.sel_truck = avaiTruckList.get(j);
															kg.sel_mooc = avaiMoocList.get(k);
															kg.sel_container_a = avaiContList_a.get(qa);
															kg.sel_container_b = avaiContList_a.get(qb);
															kg.sel_whCode_a = cont20reqs.whCode.get(kRE_a);
															kg.sel_whCode_b = cont20reqs.whCode.get(kRE_b);
															kg.returnContDepotCode_a = cont20reqs.returnContDepotCode.get(kRE_a);
															kg.returnContDepotCode_b = cont20reqs.returnContDepotCode.get(kRE_b);
															kg.lateDateTimeDeliveryAtDepot_a = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a);
															kg.lateDateTimeDeliveryAtDepot_b = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b);
															sel_kg = kg;
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
					else if(cont20reqs.isNeedCont.get(kRE_a) == 1 && cont20reqs.isNeedCont.get(kRE_b) == 0){
						ArrayList<String> whLocationCode = new ArrayList<String>();
						for(int t = 0; t < cont20reqs.whCode.get(kRE_a).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_a).get(t));
						for(int t = 0; t < cont20reqs.whCode.get(kRE_b).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_b).get(t));			
						for (String keyC_a : mDepot2ContainerList.keySet()) {
							ArrayList<Container> avaiContList_a = getAvailableContainerAtDepot(
									cont20reqs.weights.get(kRE_a), 
									cont20reqs.contCategory.get(kRE_a),
									cont20reqs.shipCompanyCode.get(kRE_a),
									keyC_a);
							for(int qa = 0; qa < avaiContList_a.size(); qa++){
								for (String keyM : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
											cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
											cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b), keyM);
									for(int k = 0; k < avaiMoocList.size(); k++){
										for(String keyT : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
													cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
													cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b),
													whLocationCode, keyT);
											for(int j = 0; j < avaiTruckList.size(); j++){
												KepGenerator kg = new KepGenerator(this);
												Container container_b = mCode2Container
														.get(cont20reqs.contCode.get(kRE_b));
												Measure ms = kg.evaluateKepRoute(
														avaiTruckList.get(j),
														avaiMoocList.get(k),
														avaiContList_a.get(qa),
														container_b,
														cont20reqs.isNeedCont.get(kRE_a),
														cont20reqs.isNeedCont.get(kRE_b),
														cont20reqs.isNeedReturnCont.get(kRE_a),
														cont20reqs.isNeedReturnCont.get(kRE_b),
														cont20reqs.returnContDepotCode.get(kRE_a),
														cont20reqs.returnContDepotCode.get(kRE_b),
														cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a),
														cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b),
														RE);
												if((sel_kg == null && ms != null)
													|| (ms != null && checkVehicleConstraintType(
														sel_kg.sel_whCode_a,
														cont20reqs.whCode.get(kRE_a), minMs, ms)
													&& checkVehicleConstraintType(
															sel_kg.sel_whCode_b,
															cont20reqs.whCode.get(kRE_b), minMs, ms))
													&& checkConstraintWarehouseHard(minMs, ms)
													&& checkConstraintDriverBalance(minMs, ms)){
													minMs = ms;
													kg.sel_truck = avaiTruckList.get(j);
													kg.sel_mooc = avaiMoocList.get(k);
													kg.sel_container_a = avaiContList_a.get(qa);
													kg.sel_container_b = container_b;
													kg.sel_whCode_a = cont20reqs.whCode.get(kRE_a);
													kg.sel_whCode_b = cont20reqs.whCode.get(kRE_b);
													kg.returnContDepotCode_a = cont20reqs.returnContDepotCode.get(kRE_a);
													kg.returnContDepotCode_b = cont20reqs.returnContDepotCode.get(kRE_b);
													kg.lateDateTimeDeliveryAtDepot_a = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a);
													kg.lateDateTimeDeliveryAtDepot_b = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b);
													sel_kg = kg;
												}
											}
										}
									}
								}
							}
						}
					}
					else if(cont20reqs.isNeedCont.get(kRE_a) == 0 && cont20reqs.isNeedCont.get(kRE_b) == 1){
						ArrayList<String> whLocationCode = new ArrayList<String>();
						for(int t = 0; t < cont20reqs.whCode.get(kRE_a).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_a).get(t));
						for(int t = 0; t < cont20reqs.whCode.get(kRE_b).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_b).get(t));			
						for (String keyC_b : mDepot2ContainerList.keySet()) {
							ArrayList<Container> avaiContList_b = getAvailableContainerAtDepot(
									cont20reqs.weights.get(kRE_b), 
									cont20reqs.contCategory.get(kRE_b),
									cont20reqs.shipCompanyCode.get(kRE_b),
									keyC_b);
							for(int qb = 0; qb < avaiContList_b.size(); qb++){
								for (String keyM : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
											cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
											cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b), keyM);
									for(int k = 0; k < avaiMoocList.size(); k++){
										for(String keyT : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
													cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
													cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b),
													whLocationCode, keyT);
											for(int j = 0; j < avaiTruckList.size(); j++){
												KepGenerator kg = new KepGenerator(this);
												Container container_a = mCode2Container
														.get(cont20reqs.contCode.get(kRE_a));
												Measure ms = kg.evaluateKepRoute(
														avaiTruckList.get(j),
														avaiMoocList.get(k),
														container_a,
														avaiContList_b.get(qb),
														cont20reqs.isNeedCont.get(kRE_a),
														cont20reqs.isNeedCont.get(kRE_b),
														cont20reqs.isNeedReturnCont.get(kRE_a),
														cont20reqs.isNeedReturnCont.get(kRE_b),
														cont20reqs.returnContDepotCode.get(kRE_a),
														cont20reqs.returnContDepotCode.get(kRE_b),
														cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a),
														cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b),
														RE);
												if((sel_kg == null && ms != null)
													|| (ms != null && checkVehicleConstraintType(
														sel_kg.sel_whCode_a,
														cont20reqs.whCode.get(kRE_a), minMs, ms)
													&& checkVehicleConstraintType(
															sel_kg.sel_whCode_b,
															cont20reqs.whCode.get(kRE_b), minMs, ms))
													&& checkConstraintWarehouseHard(minMs, ms)
													&& checkConstraintDriverBalance(minMs, ms)){
													minMs = ms;
													kg.sel_truck = avaiTruckList.get(j);
													kg.sel_mooc = avaiMoocList.get(k);
													kg.sel_container_a = container_a;
													kg.sel_container_b = avaiContList_b.get(qb);
													kg.sel_whCode_a = cont20reqs.whCode.get(kRE_a);
													kg.sel_whCode_b = cont20reqs.whCode.get(kRE_b);
													kg.returnContDepotCode_a = cont20reqs.returnContDepotCode.get(kRE_a);
													kg.returnContDepotCode_b = cont20reqs.returnContDepotCode.get(kRE_b);
													kg.lateDateTimeDeliveryAtDepot_a = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a);
													kg.lateDateTimeDeliveryAtDepot_b = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b);
													sel_kg = kg;
												}
											}
										}
									}
								}
							}
						}
					}
					else{
						ArrayList<String> whLocationCode = new ArrayList<String>();
						for(int t = 0; t < cont20reqs.whCode.get(kRE_a).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_a).get(t));
						for(int t = 0; t < cont20reqs.whCode.get(kRE_b).size(); t++)
							whLocationCode.add(cont20reqs.whCode.get(kRE_b).get(t));			
						
						for (String keyM : mDepot2MoocList.keySet()) {
							ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepotForKep(
									cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
									cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b), keyM);
							for(int k = 0; k < avaiMoocList.size(); k++){
								for(String keyT : mDepot2TruckList.keySet()) {
									ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
											cont20reqs.orderCode.get(kRE_a), cont20reqs.orderCode.get(kRE_b),
											cont20reqs.weights.get(kRE_a) + cont20reqs.weights.get(kRE_b),
											whLocationCode, keyT);
									for(int j = 0; j < avaiTruckList.size(); j++){
										KepGenerator kg = new KepGenerator(this);
										Container container_a = mCode2Container
												.get(cont20reqs.contCode.get(kRE_a));
										Container container_b = mCode2Container
												.get(cont20reqs.contCode.get(kRE_b));
										Measure ms = kg.evaluateKepRoute(
												avaiTruckList.get(j),
												avaiMoocList.get(k),
												container_a,
												container_b,
												cont20reqs.isNeedCont.get(kRE_a),
												cont20reqs.isNeedCont.get(kRE_b),
												cont20reqs.isNeedReturnCont.get(kRE_a),
												cont20reqs.isNeedReturnCont.get(kRE_b),
												cont20reqs.returnContDepotCode.get(kRE_a),
												cont20reqs.returnContDepotCode.get(kRE_b),
												cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a),
												cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b),
												RE);
										if((sel_kg == null && ms != null)
											|| (ms != null && checkVehicleConstraintType(
												sel_kg.sel_whCode_a,
												cont20reqs.whCode.get(kRE_a), minMs, ms)
											&& checkVehicleConstraintType(
													sel_kg.sel_whCode_b,
													cont20reqs.whCode.get(kRE_b), minMs, ms))
											&& checkConstraintWarehouseHard(minMs, ms)
											&& checkConstraintDriverBalance(minMs, ms)){
											minMs = ms;
											kg.sel_truck = avaiTruckList.get(j);
											kg.sel_mooc = avaiMoocList.get(k);
											kg.sel_container_a = container_a;
											kg.sel_container_b = container_b;
											kg.sel_whCode_a = cont20reqs.whCode.get(kRE_a);
											kg.sel_whCode_b = cont20reqs.whCode.get(kRE_b);
											kg.returnContDepotCode_a = cont20reqs.returnContDepotCode.get(kRE_a);
											kg.returnContDepotCode_b = cont20reqs.returnContDepotCode.get(kRE_b);
											kg.lateDateTimeDeliveryAtDepot_a = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_a);
											kg.lateDateTimeDeliveryAtDepot_b = cont20reqs.lateDateTimeDeliveryAtDepot.get(kRE_b);
											sel_kg = kg;
										}
									}
								}
							}
						}
					}
				}
			}
			
			if(sel_kg != null && sel_kg.sel_truck != null){
				sel_tri = sel_kg.createRoute();
				if(sel_tri != null){
					KepRouteComposer kcp = new KepRouteComposer(this, minMs,
							sel_kg, sel_tri, sel_tri.route.getDistance());
					candidateRouteComposer.add(kcp);
				}
			}
	}

	public Measure evaluateTangboWarehouseExport(RouteTangboWarehouseExport routeTangboWarehouseExport){
		Measure minMs = null;
		for (int a = 0; a < nbWhReqs; a++) {
			if(whReqScheduled[a]
				|| whReq[a].getGetDepotContainerCode() != null
				|| whReq[a].getReturnDepotContainerCode() != null
				|| checkConstraintWarehouseVendor(whReq[a]) == false)
				continue;
			for (int b = 0; b < nbExReqs; b++) {
				if (exReqScheduled[b]
					|| exReq[b].getIsSwap()
					|| checkConstraintWarehouseVendor(exReq[b]) == false)
					continue;
				if(!whReq[a].getContainerCategory().equals(exReq[b].getContainerCategory()))
					continue;
				double maxW = whReq[a].getWeight() > exReq[b].getWeight() ?
						whReq[a].getWeight() : exReq[b].getWeight();
						
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < exReq[b].getPickupWarehouses().length; t++)
					whLocationCode.add(exReq[b].getPickupWarehouses()[t].getWareHouseCode());
				whLocationCode.add(whReq[a].getFromWarehouseCode());
				whLocationCode.add(whReq[a].getToWarehouseCode());				
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							whReq[a].getOrderCode(), exReq[b].getOrderCode(), maxW, 
							whReq[a].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									whReq[a].getOrderCode(), exReq[b].getOrderCode(), 
									maxW, whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = routeTangboWarehouseExport.evaluateTangboWarehouseExport(
										avaiTruckList.get(j), avaiMoocList.get(k), 
										whReq[a], exReq[b]);
								if(checkVehicleConstraintType(
										routeTangboWarehouseExport.sel_whReq,
										whReq[a], minMs, ms)
									&& checkVehicleConstraintType(
											routeTangboWarehouseExport.sel_exReq,
											exReq[b], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									routeTangboWarehouseExport.sel_truck = avaiTruckList.get(j);
									routeTangboWarehouseExport.sel_mooc = avaiMoocList.get(k);
									routeTangboWarehouseExport.sel_whReq = whReq[a];
									routeTangboWarehouseExport.sel_exReq = exReq[b];
								}
							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public Measure evaluateTangboImportWarehouse(RouteTangboImportWarehouse routeTangboImportWarehouse){
		Measure minMs = null;
		for (int a = 0; a < nbWhReqs; a++) {
			if(whReqScheduled[a]
				|| whReq[a].getGetDepotContainerCode() != null
				|| whReq[a].getReturnDepotContainerCode() != null
				|| checkConstraintWarehouseVendor(whReq[a]) == false)
				continue;
			for (int b = 0; b < nbImReqs; b++) {
				if (imReqScheduled[b]
					|| imReq[b].getIsSwap()
					|| checkConstraintWarehouseVendor(imReq[b]) == false)
					continue;
				if(!whReq[a].getContainerCategory().equals(imReq[b].getContainerCategory()))
					continue;
				double maxW = whReq[a].getWeight() > imReq[b].getWeight() ?
						whReq[a].getWeight() : imReq[b].getWeight();
						
				ArrayList<String> whLocationCode = new ArrayList<String>();
				for(int t = 0; t < imReq[b].getDeliveryWarehouses().length; t++)
					whLocationCode.add(imReq[b].getDeliveryWarehouses()[t].getWareHouseCode());
				whLocationCode.add(whReq[a].getFromWarehouseCode());
				whLocationCode.add(whReq[a].getToWarehouseCode());
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							whReq[a].getOrderCode(), imReq[b].getOrderCode(), 
							maxW, imReq[b].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									whReq[a].getOrderCode(), imReq[b].getOrderCode(), 
									maxW, whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = routeTangboImportWarehouse.evaluateTangboImportWarehouse(
										avaiTruckList.get(j), avaiMoocList.get(k), 
										whReq[a], imReq[b]);
								if(checkVehicleConstraintType(
										routeTangboImportWarehouse.sel_whReq,
										whReq[a], minMs, ms)
									&& checkVehicleConstraintType(
											routeTangboImportWarehouse.sel_imReq,
											imReq[b], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									routeTangboImportWarehouse.sel_truck = avaiTruckList.get(j);
									routeTangboImportWarehouse.sel_mooc = avaiMoocList.get(k);
									routeTangboImportWarehouse.sel_whReq = whReq[a];
									routeTangboImportWarehouse.sel_imReq = imReq[b];
								}

							}
						}
					}
				}
			}
		}
		return minMs;
	}
	
	public void exploreTangBo(CandidateRouteComposer candidateRouteComposer) {
		
		RouteTangboWarehouseExport routeTangboWarehouseExport = new RouteTangboWarehouseExport(this);
		RouteTangboImportWarehouse routeTangboImportWarehouse = new RouteTangboImportWarehouse(this);
		Measure minMsWarehouseExport = evaluateTangboWarehouseExport(routeTangboWarehouseExport);
		Measure minMsImportWarehouse = evaluateTangboImportWarehouse(routeTangboImportWarehouse);
		Measure minMs = minMsWarehouseExport;
		if(minMs == null)
			minMs = minMsImportWarehouse;
		if(minMs == null)
			return;
		if(minMsImportWarehouse != null && minMsWarehouseExport != null)
			if(minMsWarehouseExport.distance > minMsImportWarehouse.distance
				|| (minMsWarehouseExport.distance == minMsImportWarehouse.distance
						&& minMsWarehouseExport.time > minMsImportWarehouse.time))
				minMs = minMsImportWarehouse;
		
		if(minMsWarehouseExport == minMs && routeTangboWarehouseExport.sel_truck != null){
			TruckRouteInfo4Request tri = routeTangboWarehouseExport
					.createTangboWarehouseExport();
			if(tri != null){
				TangboWarehouseExportRouteComposer tcp = new TangboWarehouseExportRouteComposer(
						this,minMsWarehouseExport, routeTangboWarehouseExport.sel_truck,
						routeTangboWarehouseExport.sel_mooc,
						routeTangboWarehouseExport.sel_container,
						tri.route, routeTangboWarehouseExport.sel_whReq,
						routeTangboWarehouseExport.sel_exReq,
						tri, minMsWarehouseExport.distance);

				Truck sel_truck_ex1 = null;
				Truck sel_truck_wh2 = null;
				Mooc sel_mooc_ex1 = null;
				Mooc sel_mooc_wh2 = null;
				Container sel_container_ex1 = null;
				Container sel_container_wh2 = null;
				Measure ms1 = null;
				Measure ms2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeEx = new ArrayList<String>();
				for(int t = 0; t < routeTangboWarehouseExport.sel_exReq.getPickupWarehouses().length; t++)
					whLocationCodeEx.add(routeTangboWarehouseExport.sel_exReq.getPickupWarehouses()[t].getWareHouseCode());
				ArrayList<String> whLocationCodeWh = new ArrayList<String>();
				whLocationCodeWh.add(routeTangboWarehouseExport.sel_whReq.getFromWarehouseCode());
				whLocationCodeWh.add(routeTangboWarehouseExport.sel_whReq.getToWarehouseCode());
				
				for (String keyC1 : mDepot2ContainerList.keySet()) {
					ArrayList<Container> avaiContList1 = getAvailableContainerAtDepot(
							routeTangboWarehouseExport.sel_exReq.getWeight(), 
							routeTangboWarehouseExport.sel_exReq.getContainerCategory(),
							routeTangboWarehouseExport.sel_exReq.getShipCompanyCode(), 
							keyC1);
					for(int q1 = 0; q1 < avaiContList1.size(); q1++){
						for (String keyM1 : mDepot2MoocList.keySet()) {
							ArrayList<Mooc> avaiMoocList1 = getAvailableMoocAtDepot(
									routeTangboWarehouseExport.sel_exReq.getOrderCode(), "",
									routeTangboWarehouseExport.sel_exReq.getWeight(), 
									routeTangboWarehouseExport.sel_exReq.getContainerCategory(), keyM1);
							for(int k1 = 0; k1 < avaiMoocList1.size(); k1++){
								for(String keyT1 : mDepot2TruckList.keySet()) {
									ArrayList<Truck> avaiTruckList1 = getAvailableTruckAtDepot(
											routeTangboWarehouseExport.sel_exReq.getOrderCode(), "",
											routeTangboWarehouseExport.sel_exReq.getWeight(),
											whLocationCodeEx, keyT1);
									for(int j1 = 0; j1 < avaiTruckList1.size(); j1++){
										for (String keyM2 : mDepot2MoocList.keySet()) {
											ArrayList<Mooc> avaiMoocList2 = getAvailableMoocAtDepot(
													routeTangboWarehouseExport.sel_whReq.getOrderCode(), "",
													routeTangboWarehouseExport.sel_whReq.getWeight(), 
													routeTangboWarehouseExport.sel_whReq.getContainerCategory(), keyM2);
											for(int k2 = 0; k2 < avaiMoocList2.size(); k2++){
												for(String keyT2 : mDepot2TruckList.keySet()) {
													ArrayList<Truck> avaiTruckList2 = getAvailableTruckAtDepot(
															routeTangboWarehouseExport.sel_whReq.getOrderCode(), "",
															routeTangboWarehouseExport.sel_whReq.getWeight(),
															whLocationCodeWh, keyT2);
													for(int j2 = 0; j2 < avaiTruckList2.size(); j2++){
														Measure msEx = evaluateExportRoute(
																routeTangboWarehouseExport.sel_exReq, 
																avaiTruckList1.get(j1), avaiMoocList1.get(k1),
																avaiContList1.get(q1));
														Measure msWh = evaluateWarehouseWarehouseRequest(
																routeTangboWarehouseExport.sel_whReq,
																avaiTruckList2.get(j2), avaiMoocList2.get(k2));
														if(msEx != null
															&& msWh != null){
															double dis = msEx.distance + msWh.distance;
															if (dis < minDistance1) {
																ms1 = msEx;
																ms2 = msWh;
																minDistance1 = dis;
																sel_truck_ex1 = avaiTruckList1.get(j1);
																sel_truck_wh2 = avaiTruckList2.get(j2);
																sel_mooc_ex1 = avaiMoocList1.get(k1);
																sel_mooc_wh2 = avaiMoocList2.get(k2);
																sel_container_ex1 = avaiContList1.get(q1);
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_ex1 != null && sel_truck_wh2 != null && minDistance1 < minMsWarehouseExport.distance){
					TruckRouteInfo4Request tri_ex = createRouteForExportRequest(
							routeTangboWarehouseExport.sel_exReq, sel_truck_ex1, sel_mooc_ex1, sel_container_ex1);
					TruckRouteInfo4Request tri_wh = createRouteForWarehouseWarehouseRequest(
							routeTangboWarehouseExport.sel_whReq, sel_truck_wh2, sel_mooc_wh2);
					if (tri_wh != null && tri_ex != null) {
						IndividualWarehouseExportRoutesComposer icp = new IndividualWarehouseExportRoutesComposer(
								this, ms1, ms2, tri_wh.route, tri_ex.route, routeTangboWarehouseExport.sel_whReq, routeTangboWarehouseExport.sel_exReq,
								tri_wh, tri_ex, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(tcp);
				}
			}
		}
		else if(minMsImportWarehouse == minMs && routeTangboImportWarehouse.sel_truck != null){
			TruckRouteInfo4Request tri = routeTangboImportWarehouse
					.createTangboImportWarehouse();
			if(tri != null){
				TangboImportWarehouseRouteComposer tcp = new TangboImportWarehouseRouteComposer(
						this, routeTangboImportWarehouse.sel_truck,
						routeTangboImportWarehouse.sel_mooc,
						tri.route, routeTangboImportWarehouse.sel_whReq,
						routeTangboImportWarehouse.sel_imReq,
						tri, minMsImportWarehouse.distance);

				Truck sel_truck_im1 = null;
				Truck sel_truck_wh2 = null;
				Mooc sel_mooc_im1 = null;
				Mooc sel_mooc_wh2 = null;
				Measure ms1 = null;
				Measure ms2 = null;
				Container sel_container_im1 = null;
				Container sel_container_wh2 = null;
				double minDistance1 = Integer.MAX_VALUE;
				
				ArrayList<String> whLocationCodeIm = new ArrayList<String>();
				for(int t = 0; t < routeTangboImportWarehouse.sel_imReq.getDeliveryWarehouses().length; t++)
					whLocationCodeIm.add(routeTangboImportWarehouse.sel_imReq.getDeliveryWarehouses()[t].getWareHouseCode());
				ArrayList<String> whLocationCodeWh = new ArrayList<String>();
				whLocationCodeWh.add(routeTangboImportWarehouse.sel_whReq.getFromWarehouseCode());
				whLocationCodeWh.add(routeTangboImportWarehouse.sel_whReq.getToWarehouseCode());
				
				for (String keyM1 : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList1 = getAvailableMoocAtDepot(
							routeTangboImportWarehouse.sel_imReq.getOrderCode(), "",
							routeTangboImportWarehouse.sel_imReq.getWeight(), 
							routeTangboImportWarehouse.sel_imReq.getContainerCategory(), keyM1);
					for(int k1 = 0; k1 < avaiMoocList1.size(); k1++){
						for(String keyT1 : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList1 = getAvailableTruckAtDepot(
									routeTangboImportWarehouse.sel_imReq.getOrderCode(), "",
									routeTangboImportWarehouse.sel_imReq.getWeight(),
									whLocationCodeIm, keyT1);
							for(int j1 = 0; j1 < avaiTruckList1.size(); j1++){
								for (String keyM2 : mDepot2MoocList.keySet()) {
									ArrayList<Mooc> avaiMoocList2 = getAvailableMoocAtDepot(
											routeTangboImportWarehouse.sel_whReq.getOrderCode(), "",
											routeTangboImportWarehouse.sel_whReq.getWeight(), 
											routeTangboImportWarehouse.sel_whReq.getContainerCategory(), keyM2);
									for(int k2 = 0; k2 < avaiMoocList2.size(); k2++){
										for(String keyT2 : mDepot2TruckList.keySet()) {
											ArrayList<Truck> avaiTruckList2 = getAvailableTruckAtDepot(
													routeTangboImportWarehouse.sel_whReq.getOrderCode(), "",
													routeTangboImportWarehouse.sel_whReq.getWeight(),
													whLocationCodeWh, keyT2);
											for(int j2 = 0; j2 < avaiTruckList2.size(); j2++){
												Measure msIm = evaluateImportRequest(
														routeTangboImportWarehouse.sel_imReq, 
														avaiTruckList1.get(j1), avaiMoocList1.get(k1));
												Measure msWh = evaluateWarehouseWarehouseRequest(
														routeTangboImportWarehouse.sel_whReq,
														avaiTruckList2.get(j2), avaiMoocList2.get(k2));
												if(msIm != null
													&& msWh != null){
													double dis = msIm.distance + msWh.distance;
													if (dis < minDistance1) {
														ms1 = msIm;
														ms2 = msWh;
														minDistance1 = dis;
														sel_truck_im1 = avaiTruckList1.get(j1);
														sel_truck_wh2 = avaiTruckList2.get(j2);
														sel_mooc_im1 = avaiMoocList1.get(k1);
														sel_mooc_wh2 = avaiMoocList2.get(k2);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
				if(sel_truck_im1 != null && sel_truck_wh2 != null && minDistance1 < minMsImportWarehouse.distance){
					TruckRouteInfo4Request tri_im = createRouteForImportRequest(
							routeTangboImportWarehouse.sel_imReq, sel_truck_im1, sel_mooc_im1);
					TruckRouteInfo4Request tri_wh = createRouteForWarehouseWarehouseRequest(
							routeTangboImportWarehouse.sel_whReq, sel_truck_wh2, sel_mooc_wh2);
					if (tri_wh != null && tri_im != null) {
						IndividualImportWarehouseRoutesComposer icp = new IndividualImportWarehouseRoutesComposer(
								this, ms1, ms2, tri_wh.route, tri_im.route, routeTangboImportWarehouse.sel_whReq, routeTangboImportWarehouse.sel_imReq,
								tri_wh, tri_im, minDistance1);
						candidateRouteComposer.add(icp);
					}
				}
				else{
					candidateRouteComposer.add(tcp);
				}
			}
		}
		
	}
	public void exploreDirectRouteImportLadenRequest(CandidateRouteComposer candidateRouteComposer){
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportLadenRequests sel_imLadenReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < nbImLadenReqs; i++){
			if(imLadenReqScheduled[i])
				continue;
			if(checkConstraintWarehouseVendor(imLadenReq[i]) == false){
				imLadenReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}
			ArrayList<String> whLocationCode = new ArrayList<String>();
			whLocationCode.add(imLadenReq[i].getWareHouseCode());
			
			for (String keyM : mDepot2MoocList.keySet()) {
				ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
						imLadenReq[i].getOrderCode(), "",
						imLadenReq[i].getWeight(), 
						imLadenReq[i].getContainerCategory(), keyM);
				for(int k = 0; k < avaiMoocList.size(); k++){
					for(String keyT : mDepot2TruckList.keySet()) {
						ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
								imLadenReq[i].getOrderCode(), "",
								imLadenReq[i].getWeight(), whLocationCode, keyT);
						for(int j = 0; j < avaiTruckList.size(); j++){
							Measure ms = evaluateImportLadenRequest(imLadenReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
							if(checkVehicleConstraintType(
									sel_imLadenReq, imLadenReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
								minMs = ms;
								sel_imLadenReq = imLadenReq[i];
								sel_truck = avaiTruckList.get(j);
								sel_mooc = avaiMoocList.get(k);
							}
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			sel_tri = createRouteForImportLadenRequest(sel_imLadenReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualImportLadenRouteComposer icp = new IndividualImportLadenRouteComposer(
						this, minMs, sel_tr, sel_imLadenReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}
	
	public void exploreDirectRouteImportEmptyRequest(CandidateRouteComposer candidateRouteComposer){
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportEmptyRequests sel_imEmptyReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < nbImEmptyReqs; i++){
			if(imEmptyReqScheduled[i])
				continue;
			if(checkConstraintWarehouseVendor(imEmptyReq[i]) == false){
				imEmptyReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}
			if(mCode2Mooc.get(imEmptyReq[i].getMoocCode()) == null){
				imEmptyReq[i].setRejectCode(Utils.CANNOT_FIND_ROMOOC);
				continue;
			}
			
			ArrayList<String> whLocationCode = new ArrayList<String>();
			whLocationCode.add(imEmptyReq[i].getWareHouseCode());
			if(imEmptyReq[i].getIsBreakRomooc()
				&& imEmptyReq[i].getMoocCode() != null){
				for(String keyT : mDepot2TruckList.keySet()) {
					ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
							imEmptyReq[i].getOrderCode(), "",
							0, whLocationCode, keyT);
					for(int j = 0; j < avaiTruckList.size(); j++){
						Measure ms = evaluateImportEmptyRequest(imEmptyReq[i], avaiTruckList.get(j));
						if(checkVehicleConstraintType(
								sel_imEmptyReq, imEmptyReq[i], minMs, ms)
							&& checkConstraintWarehouseHard(minMs, ms)
							&& checkConstraintDriverBalance(minMs, ms)){
							minMs = ms;
							sel_imEmptyReq = imEmptyReq[i];
							sel_truck = avaiTruckList.get(j);
						}
					}
				}
			}
			else{
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							imEmptyReq[i].getOrderCode(), "",
							0, imEmptyReq[i].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									imEmptyReq[i].getOrderCode(), "",
									0, whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = evaluateImportEmptyRequest(imEmptyReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
								if(checkVehicleConstraintType(
										sel_imEmptyReq, imEmptyReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									sel_imEmptyReq = imEmptyReq[i];
									sel_truck = avaiTruckList.get(j);
									sel_mooc = avaiMoocList.get(k);
								}
							}
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			if(sel_imEmptyReq.getIsBreakRomooc()
				&& sel_imEmptyReq.getMoocCode() != null)
				sel_tri = createRouteForImportEmptyRequest(sel_imEmptyReq, sel_truck);
			else
				sel_tri = createRouteForImportEmptyRequest(sel_imEmptyReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualImportEmptyRouteComposer icp = new IndividualImportEmptyRouteComposer(
						this, minMs, sel_tr, sel_imEmptyReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}
	
	public void exploreDirectRouteExportEmptyRequest(CandidateRouteComposer candidateRouteComposer){
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportEmptyRequests sel_exEmptyReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < nbExEmptyReqs; i++){
			if(exEmptyReqScheduled[i])
				continue;
			if(checkConstraintWarehouseVendor(exEmptyReq[i]) == false){
				exEmptyReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}
			
			ArrayList<String> whLocationCode = new ArrayList<String>();
			whLocationCode.add(exEmptyReq[i].getWareHouseCode());
			
			for (String keyM : mDepot2MoocList.keySet()) {
				ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
						exEmptyReq[i].getOrderCode(), "",
						0, exEmptyReq[i].getContainerCategory(), keyM);
				for(int k = 0; k < avaiMoocList.size(); k++){
					for(String keyT : mDepot2TruckList.keySet()) {
						ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
								exEmptyReq[i].getOrderCode(), "",
								0, whLocationCode, keyT);
						for(int j = 0; j < avaiTruckList.size(); j++){
							Measure ms = evaluateExportEmptyRequest(exEmptyReq[i], 
									avaiTruckList.get(j), avaiMoocList.get(k));
							if(checkVehicleConstraintType(
									sel_exEmptyReq, exEmptyReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
								minMs = ms;
								sel_exEmptyReq = exEmptyReq[i];
								sel_truck = avaiTruckList.get(j);
								sel_mooc = avaiMoocList.get(k);
							}
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			sel_tri = createRouteForExportEmptyRequest(sel_exEmptyReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualExportEmptyRouteComposer icp = new IndividualExportEmptyRouteComposer(
						this, minMs, sel_tr, sel_exEmptyReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}

	public void exploreDirectRouteExportLadenRequest(CandidateRouteComposer candidateRouteComposer){
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportLadenRequests sel_exLadenReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < nbExLadenReqs; i++){
			if(exLadenReqScheduled[i])
				continue;
			if(mCode2Mooc.get(exLadenReq[i].getMoocCode()) == null){
				exLadenReq[i].setRejectCode(Utils.CANNOT_FIND_ROMOOC);
				continue;
			}
			if(checkConstraintWarehouseVendor(exLadenReq[i]) == false){
				exLadenReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}
			
			ArrayList<String> whLocationCode = new ArrayList<String>();
			whLocationCode.add(exLadenReq[i].getWareHouseCode());
			
			if(exLadenReq[i].getIsBreakRomooc()
				&& exLadenReq[i].getMoocCode() != null){
				for(String keyT : mDepot2TruckList.keySet()) {
					ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
							exLadenReq[i].getOrderCode(), "",
							0, whLocationCode, keyT);
					for(int j = 0; j < avaiTruckList.size(); j++){
						Measure ms = evaluateExportLadenRequest(exLadenReq[i], avaiTruckList.get(j));
						if(checkVehicleConstraintType(
								sel_exLadenReq, exLadenReq[i], minMs, ms)
								&& checkConstraintWarehouseHard(minMs, ms)
								&& checkConstraintDriverBalance(minMs, ms)){
							minMs = ms;
							sel_exLadenReq = exLadenReq[i];
							sel_truck = avaiTruckList.get(j);
						}
					}
				}
			}
			else{
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							exLadenReq[i].getOrderCode(), "",
							0, exLadenReq[i].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									exLadenReq[i].getOrderCode(), "",
									0, whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = evaluateExportLadenRequest(exLadenReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
								if(checkVehicleConstraintType(
										sel_exLadenReq, exLadenReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									sel_exLadenReq = exLadenReq[i];
									sel_truck = avaiTruckList.get(j);
									sel_mooc = avaiMoocList.get(k);
								}
							}
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			if(sel_exLadenReq.getIsBreakRomooc()
					&& sel_exLadenReq.getMoocCode() != null)
				sel_tri = createRouteForExportLadenRequest(sel_exLadenReq, sel_truck);
			else
				sel_tri = createRouteForExportLadenRequest(sel_exLadenReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualExportLadenRouteComposer icp = new IndividualExportLadenRouteComposer(
						this, minMs, sel_tr, sel_exLadenReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}

	public void exploreDirectRouteEmptyContainerFromDepotRequest(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		EmptyContainerFromDepotRequest sel_req = null;
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++) {
			if (emptyContainerFromDepotReqScheduled[i])
				continue;

			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					for (int q = 0; q < containers.length; q++) {
						if (mContainer2LastDepot.get(containers[q]) == null ||
								mMooc2LastDepot.get(moocs[k]) == null)
							continue;
						backup();
						TruckRouteInfo4Request tri = createRouteForEmptyContainerFromDepotRequest(
								emptyContainerFromDepotReq[i], trucks[j],
								moocs[k], containers[q]);

						if (tri != null) {
							TruckRoute tr = tri.route;
							double dis = tr.getDistance()
									- tr.getReducedDistance();
							if (dis < minDistance) {
								minDistance = dis;
								sel_tr = tr;
								sel_tri = tri;
								sel_req = emptyContainerFromDepotReq[i];
							}
						}
						restore();
					}
				}
			}
		}
		if (sel_tr != null) {
			IndividualEmptyContainerFromDepotRouteComposer icp = new IndividualEmptyContainerFromDepotRouteComposer(
					this, sel_tr, sel_req, sel_tri, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}

	public void exploreDirectRouteEmptyContainerToDepotRequest(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		EmptyContainerToDepotRequest sel_req = null;
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++) {
			if (emptyContainerToDepotReqScheduled[i])
				continue;

			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					if (mMooc2LastDepot.get(moocs[k]) == null)
						continue;
					Container container = mCode2Container
							.get(emptyContainerToDepotReq[i].getContainerCode());
					backup();
					TruckRouteInfo4Request tri = createRouteForEmptyContainerToDepotRequest(
							emptyContainerToDepotReq[i], trucks[j], moocs[k],
							container);

					if (tri != null) {
						TruckRoute tr = tri.route;
						double dis = tr.getDistance() - tr.getReducedDistance();
						if (dis < minDistance) {
							minDistance = dis;
							sel_tr = tr;
							sel_tri = tri;
							sel_req = emptyContainerToDepotReq[i];
						}
					}
					restore();
				}
			}
		}
		if (sel_tr != null) {
			IndividualEmptyContainerToDepotRouteComposer icp = new IndividualEmptyContainerToDepotRouteComposer(
					this, sel_tr, sel_req, sel_tri, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}
	
	public void exploreDirectRouteTransportContainerRequest(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		TransportContainerRequest sel_req = null;
		for (int i = 0; i < nbTransportContainerReqs; i++) {
			if (transportContainerReqScheduled[i])
				continue;

			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					if (mMooc2LastDepot.get(moocs[k]) == null)
						continue;
					Container container = mCode2Container
							.get(transportContainerReq[i].getContainerCode());
					backup();
					TruckRouteInfo4Request tri = createRouteForTransportContainerRequest(
							transportContainerReq[i], trucks[j], moocs[k],
							container);

					if (tri != null) {
						TruckRoute tr = tri.route;
						double dis = tr.getDistance() - tr.getReducedDistance();
						if (dis < minDistance) {
							minDistance = dis;
							sel_tr = tr;
							sel_tri = tri;
							sel_req = transportContainerReq[i];
						}
					}
					restore();
				}
			}
		}
		if (sel_tr != null) {
			IndividualTransportContainerRouteComposer icp = new IndividualTransportContainerRouteComposer(
					this, sel_tr, sel_req, sel_tri, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}
	

	public void exploreDirectRouteExport(
			CandidateRouteComposer candidateRouteComposer) {
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportContainerRequest sel_exReq = null;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		Container sel_container = null;
		
		for (int i = 0; i < nbExReqs; i++) {
			if (exReqScheduled[i])
				continue;
			if(exReq[i].getIsSwap()){
				//isSwap == true, exploreSwapRequest return null
				exReq[i].setRejectCode(Utils.CANNOt_FIND_SWAP_REQUEST);
				continue;
			}
			if(checkConstraintWarehouseVendor(exReq[i]) == false){
				exReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}

			//get location code of warehouse for checking constraint Warehouse tractor
			ArrayList<String> whLocationCode = new ArrayList<String>();
			for(int t = 0; t < exReq[i].getPickupWarehouses().length; t++)
				whLocationCode.add(exReq[i].getPickupWarehouses()[t].getWareHouseCode());
			
			for (String keyC : mDepot2ContainerList.keySet()) {
				ArrayList<Container> avaiContList = getAvailableContainerAtDepot(exReq[i].getWeight(), 
						exReq[i].getContainerCategory(), exReq[i].getShipCompanyCode(), keyC);
				for(int q = 0; q < avaiContList.size(); q++){
					for (String keyM : mDepot2MoocList.keySet()) {
						ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
								exReq[i].getOrderCode(), "",
								exReq[i].getWeight(), 
								avaiContList.get(q).getCategoryCode(), keyM);
						for(int k = 0; k < avaiMoocList.size(); k++){
							for(String keyT : mDepot2TruckList.keySet()) {
								ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
										exReq[i].getOrderCode(), "",
										exReq[i].getWeight(), whLocationCode, keyT);
								for(int j = 0; j < avaiTruckList.size(); j++){
									Measure ms = evaluateExportRoute(exReq[i], avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
									if(checkVehicleConstraintType(
											sel_exReq, exReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
										minMs = ms;
										sel_truck = avaiTruckList.get(j);
										sel_mooc = avaiMoocList.get(k);
										sel_container = avaiContList.get(q);
										sel_exReq = exReq[i];
									}
								}
							}
						}
					}
				}
			}
		}
		if (sel_truck != null) {
			sel_tri = createRouteForExportRequest(
					sel_exReq, sel_truck, sel_mooc, sel_container);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualExportRouteComposer icp = new IndividualExportRouteComposer(
					this, minMs, sel_tr, sel_exReq, sel_tri, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
			}
		}
		
		/*
		if (sel_tr != null) {
			IndividualExportRouteComposer icp = new IndividualExportRouteComposer(
					this, sel_tr, sel_exReq, sel_tri, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
		*/
	}

	public void exploreDirectRouteImport(
			CandidateRouteComposer candidateRouteComposer) {
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportContainerRequest sel_imReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for (int i = 0; i < nbImReqs; i++) {
			if (imReqScheduled[i])
				continue;
			if(imReq[i].getIsSwap()){
				imReq[i].setRejectCode(Utils.CANNOt_FIND_SWAP_REQUEST);
				continue;
			}
			if(checkConstraintWarehouseVendor(imReq[i]) == false){
				imReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}

			ArrayList<String> whLocationCode = new ArrayList<String>();
			for(int t = 0; t < imReq[i].getDeliveryWarehouses().length; t++)
				whLocationCode.add(imReq[i].getDeliveryWarehouses()[t].getWareHouseCode());
			
			for (String keyM : mDepot2MoocList.keySet()) {
				ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
						imReq[i].getOrderCode(), "",
						imReq[i].getWeight(), 
						imReq[i].getContainerCategory(), keyM);
				for(int k = 0; k < avaiMoocList.size(); k++){
					for(String keyT : mDepot2TruckList.keySet()) {
						ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
								imReq[i].getOrderCode(), "",
								imReq[i].getWeight(), whLocationCode, keyT);
						for(int j = 0; j < avaiTruckList.size(); j++){
							Measure ms = evaluateImportRequest(
									imReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
							if(checkVehicleConstraintType(
									sel_imReq, imReq[i], minMs, ms)
								&& checkConstraintWarehouseHard(minMs, ms)
								&& checkConstraintDriverBalance(minMs, ms)){
								minMs = ms;
								sel_imReq = imReq[i];
								sel_truck = avaiTruckList.get(j);
								sel_mooc = avaiMoocList.get(k);
							}
						}
					}
				}
			}
		}
		
		if(sel_truck != null){
			sel_tri = createRouteForImportRequest(
					sel_imReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				
				IndividualImportRouteComposer icp = new IndividualImportRouteComposer(
						this, minMs, sel_tr, sel_tri, sel_imReq, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
		/*
		if (sel_tr != null) {
			IndividualImportRouteComposer icp = new IndividualImportRouteComposer(
					this, sel_tr, sel_tri, sel_imReq, sel_tr.getDistance()
							- sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
		*/
	}

	public void exploreDirectRouteWarehouseWarehouse(
			CandidateRouteComposer candidateRouteComposer) {
		Measure minMs = null;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		Container sel_cont = null;
		WarehouseContainerTransportRequest sel_whReq = null;

		for (int i = 0; i < nbWhReqs; i++) {
			if (whReqScheduled[i])
				continue;
			if(checkConstraintWarehouseVendor(whReq[i]) == false){
				whReq[i].setRejectCode(Utils.CANNOT_ACCESS_HARD_WH);
				continue;
			}
			ArrayList<String> whLocationCode = new ArrayList<String>();
			whLocationCode.add(whReq[i].getFromWarehouseCode());
			whLocationCode.add(whReq[i].getToWarehouseCode());
			if(whReq[i].getGetDepotContainerCode() != null){
				ArrayList<Container> avaiContList = getAvailableContainerAtDepot(whReq[i].getWeight(), 
						whReq[i].getContainerCategory(), null, whReq[i].getGetDepotContainerCode());
				for(int q = 0; q < avaiContList.size(); q++){
					for (String keyM : mDepot2MoocList.keySet()) {
						ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
								whReq[i].getOrderCode(), "",
								whReq[i].getWeight(), 
								avaiContList.get(q).getCategoryCode(), keyM);
						for(int k = 0; k < avaiMoocList.size(); k++){
							for(String keyT : mDepot2TruckList.keySet()) {
								ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
										whReq[i].getOrderCode(), "",
										whReq[i].getWeight(), whLocationCode, keyT);
								for(int j = 0; j < avaiTruckList.size(); j++){
									Measure ms = evaluateWarehouseWarehouseRequest(whReq[i],
											avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
									if(checkVehicleConstraintType(
											sel_whReq, whReq[i], minMs, ms)
										&& checkConstraintWarehouseHard(minMs, ms)
										&& checkConstraintDriverBalance(minMs, ms)){
										minMs = ms;
										sel_truck = avaiTruckList.get(j);
										sel_mooc = avaiMoocList.get(k);
										sel_cont = avaiContList.get(q);
										sel_whReq = whReq[i];
									}
								}
							}
						}
					}
				}
			}
			else{
				for (String keyM : mDepot2MoocList.keySet()) {
					ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(
							whReq[i].getOrderCode(), "",
							whReq[i].getWeight(), 
							whReq[i].getContainerCategory(), keyM);
					for(int k = 0; k < avaiMoocList.size(); k++){
						for(String keyT : mDepot2TruckList.keySet()) {
							ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
									whReq[i].getOrderCode(), "",
									whReq[i].getWeight(), whLocationCode, keyT);
							for(int j = 0; j < avaiTruckList.size(); j++){
								Measure ms = evaluateWarehouseWarehouseRequest(whReq[i],
										avaiTruckList.get(j), avaiMoocList.get(k));
								if(checkVehicleConstraintType(
										sel_whReq, whReq[i], minMs, ms)
									&& checkConstraintWarehouseHard(minMs, ms)
									&& checkConstraintDriverBalance(minMs, ms)){
									minMs = ms;
									sel_truck = avaiTruckList.get(j);
									sel_mooc = avaiMoocList.get(k);
									sel_cont = null;
									sel_whReq = whReq[i];
								}
							}
						}
					}
				}
			}
		}
		if(sel_truck != null){
			if(sel_whReq.getGetDepotContainerCode() != null)
				sel_tri = createRouteForWarehouseWarehouseRequest(
						sel_whReq, sel_truck, sel_mooc, sel_cont);
			else
				sel_tri = createRouteForWarehouseWarehouseRequest(
						sel_whReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualWarehouseRouteComposer icp = new IndividualWarehouseRouteComposer(
						this, minMs, sel_tr, sel_tri, sel_whReq, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}

	public void markServed(EmptyContainerFromDepotRequest req) {
		if (mEmptyContainerFromDepotReq2Index.get(req) == null)
			return;
		int idx = mEmptyContainerFromDepotReq2Index.get(req);
		emptyContainerFromDepotReqScheduled[idx] = true;
	}

	public void markServed(EmptyContainerToDepotRequest req) {
		if (mEmptyContainerToDepotReq2Index.get(req) == null)
			return;
		int idx = mEmptyContainerToDepotReq2Index.get(req);
		emptyContainerToDepotReqScheduled[idx] = true;
	}

	public void markServed(TransportContainerRequest req) {
		if (mTransportContainerReq2Index.get(req) == null)
			return;
		int idx = mTransportContainerReq2Index.get(req);
		transportContainerReqScheduled[idx] = true;
	}

	public void markServed(ExportContainerRequest exR) {
		if (mExReq2Index.get(exR) == null)
			return;
		int idx = mExReq2Index.get(exR);
		exReqScheduled[idx] = true;
		cont20reqs.removeRequest(mExReq2Code.get(exR));
	}

	public void markServed(ImportContainerRequest imR) {
		if (mImReq2Index.get(imR) == null)
			return;
		int idx = mImReq2Index.get(imR);
		imReqScheduled[idx] = true;
		cont20reqs.removeRequest(mImReq2Code.get(imR));
	}

	public void markServed(WarehouseContainerTransportRequest whR) {
		if (mWhReq2Index.get(whR) == null)
			return;
		int idx = mWhReq2Index.get(whR);
		whReqScheduled[idx] = true;
		cont20reqs.removeRequest(mWhReq2Code.get(whR));
	}
	public void markServed(ImportLadenRequests req){
		if(mImportLadenRequest2Index.get(req) == null)
			return;
		int idx = mImportLadenRequest2Index.get(req);
		imLadenReqScheduled[idx] = true;
		cont20reqs.removeRequest(mImportLadenRequest2Code.get(req));
	}
	public void markServed(ImportEmptyRequests req){
		if(mImportEmptyRequest2Index.get(req) == null)
			return;
		int idx = mImportEmptyRequest2Index.get(req);
		imEmptyReqScheduled[idx] = true;
		cont20reqs.removeRequest(mImportEmptyRequest2Code.get(req));
	}
	public void markServed(ExportLadenRequests req){
		if(mExportLadenRequest2Index.get(req) == null)
			return;
		int idx = mExportLadenRequest2Index.get(req);
		exLadenReqScheduled[idx] = true;
		cont20reqs.removeRequest(mExportLadenRequest2Code.get(req));
	}
	public void markServed(ExportEmptyRequests req){
		if(mExportEmptyRequest2Index.get(req) == null)
			return;
		int idx = mExportEmptyRequest2Index.get(req);
		exEmptyReqScheduled[idx] = true;
		cont20reqs.removeRequest(mExportEmptyRequest2Code.get(req));
	}

	public void addRoute(TruckRoute tr, int lastIndex) {
		Truck truck = tr.getTruck();
		if (mTruck2Itinerary.get(truck) == null) {
			mTruck2Itinerary.put(truck, new TruckItinerary());
		}
		mTruck2Itinerary.get(truck).addRoute(tr, lastIndex);
	}

	public void print() {
		int nbUnScheduledExReq = 0;
		int nbUnScheduledImReq = 0;
		int nbUnScheduledWhReq = 0;
		for (int i = 0; i < nbExReqs; i++) {
			if (!exReqScheduled[i])
				nbUnScheduledExReq++;
		}
		for (int i = 0; i < nbImReqs; i++) {
			if (!imReqScheduled[i])
				nbUnScheduledImReq++;
		}
		for (int i = 0; i < nbWhReqs; i++) {
			if (!whReqScheduled[i])
				nbUnScheduledWhReq++;
		}
		System.out.println("nbUnScheduledExReq = " + nbUnScheduledExReq
				+ ", nbUnScheduledImReq = " + nbUnScheduledImReq
				+ ", nbUnScheduledWhReq = " + nbUnScheduledWhReq);
		for (int i = 0; i < trucks.length; i++) {
			System.out.println(getLastInfos(trucks[i]));
		}
		for (int i = 0; i < moocs.length; i++) {
			System.out.println(getLastInfos(moocs[i]));
		}
		for (int i = 0; i < containers.length; i++) {
			System.out.println(getLastInfos(containers[i]));
		}

	}

	public String getLastInfos(Truck truck) {
		String s = "";
		String locationCode = "NULL";
		DepotTruck depot = mTruck2LastDepot.get(truck);
		if (depot != null)
			locationCode = depot.getLocationCode();
		int lastTime = mTruck2LastTime.get(truck);
		s = "truck " + truck.getCode() + " at " + locationCode + " time = "
				+ DateTimeUtils.unixTimeStamp2DateTime(lastTime);
		return s;
	}

	public String getLastInfos(Mooc mooc) {
		String s = "";
		String locationCode = "NULL";
		DepotMooc depot = mMooc2LastDepot.get(mooc);
		if (depot != null)
			locationCode = depot.getLocationCode();
		int lastTime = mMooc2LastTime.get(mooc);
		s = "mooc " + mooc.getCode() + " at " + locationCode + " time = "
				+ DateTimeUtils.unixTimeStamp2DateTime(lastTime);
		return s;
	}

	public String getLastInfos(Container container) {
		String s = "";
		String locationCode = "NULL";
		DepotContainer depot = mContainer2LastDepot.get(container);
		if (depot != null)
			locationCode = depot.getLocationCode();
		String s_time = "NULL";
		if (mContainer2LastTime.get(container) != null) {
			int lastTime = mContainer2LastTime.get(container);
			s_time = DateTimeUtils.unixTimeStamp2DateTime(lastTime);
		}
		s = "container " + container.getCode() + " at " + locationCode
				+ " time = " + s_time;
		return s;
	}

	public Container20Requests getContainer20Requests(){
		HashMap<Integer, ArrayList<RouteElement>> cont20RE = new HashMap<Integer, ArrayList<RouteElement>>();
		HashMap<Integer, Integer> isNeedCont = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> isNeedReturnCont = new HashMap<Integer, Integer>();
		HashMap<Integer, ArrayList<String>> returnContDepotCode = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, String> lateDateTimeDeliveryAtDepot = new HashMap<Integer, String>();
		HashMap<Integer, ArrayList<String>> whCode = new HashMap<Integer, ArrayList<String>>();
		HashMap<Integer, Double> weights = new HashMap<Integer, Double>();
		HashMap<Integer, String> contCategory = new HashMap<Integer, String>();
		HashMap<Integer, String> contCode = new HashMap<Integer, String>();
		HashMap<Integer, String> shipCompanyCode = new HashMap<Integer, String>();
		HashMap<Integer, String> orderCode = new HashMap<Integer, String>();
	
		for(int i = 0; i < nbExReqs; i++){
			int idx = 0;
			if (exReqScheduled[i] 
				|| exReq[i].getIsSwap()
				|| !exReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(exReq[i]) == false)
				continue;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();
			RouteElement e0 = new RouteElement();
			e0.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e0.setServiceDuration(input.getParams().getLinkEmptyContainerDuration());
			e0.setIndex(idx);
			L.add(e0);
			PickupWarehouseInfo[] pwi = exReq[i].getPickupWarehouses();
			for (int j = 0; j < pwi.length; j++) {
				RouteElement e = new RouteElement();
				e.setWarehouse(mCode2Warehouse.get(pwi[j].getWareHouseCode()));
				e.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
				e.setExportRequest(exReq[i]);
				e.setEarliestArrivalTime(pwi[j]
						.getEarlyDateTimeLoadAtWarehouse());
				e.setLatestArrivalTime(pwi[j]
						.getLateDateTimeLoadAtWarehouse());
				e.setServiceDuration(pwi[j].getLoadDuration());
				idx++;
				e.setIndex(idx);
				L.add(e);
				whCodeList.add(e.getWarehouse().getCode());
			}
			RouteElement e = new RouteElement();
			e.setPort(mCode2Port.get(exReq[i].getPortCode()));
			e.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e.setContainer(null);
			e.setExportRequest(null);
			e.setEarliestArrivalTime(exReq[i]
					.getEarlyDateTimeUnloadAtPort());
			e.setLatestArrivalTime(exReq[i]
					.getLateDateTimeUnloadAtPort());
			e.setServiceDuration(exReq[i]
					.getUnloadDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			
			isNeedCont.put(mExReq2Code.get(exReq[i]), 1);
			isNeedReturnCont.put(mExReq2Code.get(exReq[i]), 0);
			returnContDepotCode.put(mExReq2Code.get(exReq[i]), new ArrayList<String>());
			lateDateTimeDeliveryAtDepot.put(mExReq2Code.get(exReq[i]), "");
			cont20RE.put(mExReq2Code.get(exReq[i]), L);
			whCode.put(mExReq2Code.get(exReq[i]), whCodeList);
			weights.put(mExReq2Code.get(exReq[i]), exReq[i].getWeight());
			contCategory.put(mExReq2Code.get(exReq[i]), exReq[i].getContainerCategory());
			contCode.put(mExReq2Code.get(exReq[i]), exReq[i].getContainerCode());
			shipCompanyCode.put(mExReq2Code.get(exReq[i]), exReq[i].getShipCompanyCode());
			orderCode.put(mExReq2Code.get(exReq[i]), exReq[i].getOrderCode());
			
		}
		
		for(int i = 0; i < nbImReqs; i++){
			if (imReqScheduled[i]
				|| imReq[i].getIsSwap()
				|| !imReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(imReq[i]) == false)
				continue;
			int idx = 0;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();
			Container container = mCode2Container.get(imReq[i].getContainerCode());
			RouteElement e = new RouteElement();
			e.setPort(mCode2Port.get(imReq[i].getPortCode()));
			e.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
			e.setImportRequest(imReq[i]);
			e.setContainer(container);
			e.setEarliestArrivalTime(imReq[i].getEarlyDateTimePickupAtPort());
			e.setLatestArrivalTime(imReq[i].getLateDateTimePickupAtPort());
			e.setServiceDuration(imReq[i].getLoadDuration());
			e.setIndex(idx);
			L.add(e);
			DeliveryWarehouseInfo[] dwi = imReq[i].getDeliveryWarehouses();
			for (int j = 0; j < dwi.length; j++) {
				e = new RouteElement();
				e.setWarehouse(mCode2Warehouse.get(dwi[j].getWareHouseCode()));
				e.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
				e.setImportRequest(imReq[i]);
				e.setEarliestArrivalTime(dwi[j].getEarlyDateTimeUnloadAtWarehouse());
				e.setLatestArrivalTime(dwi[j].getLateDateTimeUnloadAtWarehouse());
				e.setServiceDuration(dwi[j].getUnloadDuration());
				idx++;
				e.setIndex(idx);
				L.add(e);
				whCodeList.add(e.getWarehouse().getCode());
			}
			e = new RouteElement();
			
			e.setContainer(null);
			e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e.setServiceDuration(input.getParams().getUnlinkEmptyContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			
			isNeedCont.put(mImReq2Code.get(imReq[i]), 0);
			isNeedReturnCont.put(mImReq2Code.get(imReq[i]), 1);
			returnContDepotCode.put(mImReq2Code.get(imReq[i]), new ArrayList<>(Arrays.asList(
					imReq[i].getDepotContainerCode())));
			String lateDelivery = "";
			if(imReq[i].getLateDateTimeDeliveryAtDepot() != null)
				lateDelivery = imReq[i].getLateDateTimeDeliveryAtDepot();
			lateDateTimeDeliveryAtDepot.put(mImReq2Code.get(imReq[i]), lateDelivery);
			cont20RE.put(mImReq2Code.get(imReq[i]), L);
			whCode.put(mImReq2Code.get(imReq[i]), whCodeList);
			weights.put(mImReq2Code.get(imReq[i]), imReq[i].getWeight());
			contCategory.put(mImReq2Code.get(imReq[i]), imReq[i].getContainerCategory());
			contCode.put(mImReq2Code.get(imReq[i]), imReq[i].getContainerCode());
			shipCompanyCode.put(mImReq2Code.get(imReq[i]), imReq[i].getShipCompanyCode());
			orderCode.put(mImReq2Code.get(imReq[i]), imReq[i].getOrderCode());
		}
		
		for(int i = 0; i < nbWhReqs; i++){
			int idx = 0;
			if (whReqScheduled[i] || !whReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| checkConstraintWarehouseVendor(whReq[i]) == false)
				continue;
			if (whReqScheduled[i]
					|| checkConstraintWarehouseVendor(whReq[i]) == false)
					continue;
			ArrayList<String> whCodeList = new ArrayList<String>();
			whCodeList.add(whReq[i].getFromWarehouseCode());
			whCodeList.add(whReq[i].getToWarehouseCode());
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			int needCont = 0;
			if(whReq[i].getGetDepotContainerCode() != null){
				//String gd = whReq[i].getGetDepotContainerCode();
				RouteElement e0 = new RouteElement();
				DepotContainer depotContainer = mCode2DepotContainer.get(
						whReq[i].getGetDepotContainerCode());
				e0.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
				e0.setDepotContainer(depotContainer);
				e0.setServiceDuration(input.getParams().getLinkEmptyContainerDuration());
				e0.setIndex(idx);
				idx++;
				L.add(e0);
				needCont = 1;
			}
			
			RouteElement e1 = new RouteElement();
			Warehouse pickupWarehouse = mCode2Warehouse.get(whReq[i]
					.getFromWarehouseCode());
			e1.setWarehouse(pickupWarehouse);
			if(whReq[i].getGetDepotContainerCode() != null)
				e1.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			else
				e1.setAction(ActionEnum.PICKUP_CONTAINER);
			e1.setWarehouseRequest(whReq[i]);
			e1.setEarliestArrivalTime(whReq[i].getEarlyDateTimeLoad());
			e1.setLatestArrivalTime(whReq[i].getLateDateTimeLoad());
			e1.setServiceDuration(whReq[i].getLoadDuration());
			e1.setIndex(idx);
			idx++;
			L.add(e1);
			
			RouteElement e2 = new RouteElement();
			Warehouse deliveryWarehouse = mCode2Warehouse.get(whReq[i]
					.getToWarehouseCode());
			e2.setWarehouse(deliveryWarehouse);
			if(whReq[i].getReturnDepotContainerCode() != null)
				e2.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			else
				e2.setAction(ActionEnum.DELIVERY_CONTAINER);
			e2.setWarehouseRequest(whReq[i]);
			e2.setEarliestArrivalTime(whReq[i].getEarlyDateTimeUnload());
			e2.setLatestArrivalTime(whReq[i].getLateDateTimeUnload());
			e2.setServiceDuration(whReq[i].getUnloadDuration());
			e2.setIndex(idx);
			idx++;
			L.add(e2);
			
			int needReturn = 0;
			if(whReq[i].getReturnDepotContainerCode() != null){
				RouteElement e3 = new RouteElement();
				DepotContainer depotContainer = mCode2DepotContainer.get(
						whReq[i].getReturnDepotContainerCode());
				e3.setDepotContainer(depotContainer);
				e3.setContainer(null);
				e3.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
				e3.setServiceDuration(input.getParams().getUnlinkEmptyContainerDuration());
				e3.setIndex(idx);
				idx++;
				L.add(e3);
				needReturn = 1;
			}
			
			isNeedCont.put(mWhReq2Code.get(whReq[i]), needCont);
			isNeedReturnCont.put(mWhReq2Code.get(whReq[i]), needReturn);
			if(needReturn == 1){
				ArrayList<String> rtd = new ArrayList<String>();
				rtd.add(whReq[i].getReturnDepotContainerCode());
				returnContDepotCode.put(mWhReq2Code.get(whReq[i]), rtd);
			}
			else
				returnContDepotCode.put(mWhReq2Code.get(whReq[i]), new ArrayList<String>());	
			lateDateTimeDeliveryAtDepot.put(mWhReq2Code.get(whReq[i]), "");
			cont20RE.put(mWhReq2Code.get(whReq[i]), L);
			whCode.put(mWhReq2Code.get(whReq[i]), whCodeList);
			weights.put(mWhReq2Code.get(whReq[i]), whReq[i].getWeight());
			contCategory.put(mWhReq2Code.get(whReq[i]), whReq[i].getContainerCategory());
			contCode.put(mWhReq2Code.get(whReq[i]), whReq[i].getContainerCode());
			shipCompanyCode.put(mWhReq2Code.get(whReq[i]), whReq[i].getShipCompanyCode());
			orderCode.put(mWhReq2Code.get(whReq[i]), whReq[i].getOrderCode());
		}
		
		for(int i = 0; i < nbExEmptyReqs; i++){
			int idx = 0;
			if (exEmptyReqScheduled[i] 
				|| !exEmptyReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| exEmptyReq[i].getIsBreakRomooc()
				|| checkConstraintWarehouseVendor(exEmptyReq[i]) == false)
				continue;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();
			RouteElement e0 = new RouteElement();
			e0.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e0.setEarliestArrivalTime(exEmptyReq[i].getEarlyDateTimePickupAtDepot());
			e0.setLatestArrivalTime(exEmptyReq[i].getLateDateTimePickupAtDepot());
			e0.setServiceDuration(input.getParams().getLinkEmptyContainerDuration());
			e0.setIndex(idx);
			L.add(e0);

			RouteElement e = new RouteElement();
			e.setWarehouse(mCode2Warehouse.get(exEmptyReq[i].getWareHouseCode()));
			e.setAction(ActionEnum.UNLINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e.setExportEmptyRequest(exEmptyReq[i]);
			e.setEarliestArrivalTime(exEmptyReq[i].getEarlyDateTimeLoadAtWarehouse());
			e.setLatestArrivalTime(exEmptyReq[i].getLateDateTimeLoadAtWarehouse());
			e.setServiceDuration(input.getParams().getLinkEmptyContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			whCodeList.add(e.getWarehouse().getCode());

			isNeedCont.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), 1);
			isNeedReturnCont.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), 0);
			returnContDepotCode.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), new ArrayList<String>());
			lateDateTimeDeliveryAtDepot.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), "");
			cont20RE.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), L);
			whCode.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), whCodeList);
			weights.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), exEmptyReq[i].getWeight());
			contCategory.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), exEmptyReq[i].getContainerCategory());
			contCode.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), exEmptyReq[i].getContainerCode());
			shipCompanyCode.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), "");
			orderCode.put(mExportEmptyRequest2Code.get(exEmptyReq[i]), exEmptyReq[i].getOrderCode());
		}
		
		for(int i = 0; i < nbExLadenReqs; i++){
			int idx = 0;
			if (exLadenReqScheduled[i] 
				|| !exLadenReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| exLadenReq[i].getIsBreakRomooc()
				|| checkConstraintWarehouseVendor(exLadenReq[i]) == false)
				continue;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();

			RouteElement e = new RouteElement();
			e.setWarehouse(mCode2Warehouse.get(exLadenReq[i].getWareHouseCode()));
			e.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			e.setExportLadenRequest(exLadenReq[i]);
			//e.setEarliestArrivalTime(exLadenReq[i].getRequestDate());
			//e.setLatestArrivalTime(exLadenReq[i].getLateDateTimeAttachAtWarehouse());
			e.setServiceDuration(input.getParams().getLinkLoadedContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			whCodeList.add(e.getWarehouse().getCode());
			
			e = new RouteElement();
			e.setPort(mCode2Port.get(exLadenReq[i].getPortCode()));
			e.setAction(ActionEnum.RELEASE_LOADED_CONTAINER_AT_PORT);
			e.setContainer(null);
			e.setExportRequest(null);
			e.setLatestArrivalTime(exLadenReq[i]
					.getLateDateTimeUnloadAtPort());
			e.setServiceDuration(input.getParams().getUnlinkLoadedContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);

			isNeedCont.put(mExportLadenRequest2Code.get(exLadenReq[i]), 0);
			isNeedReturnCont.put(mExportLadenRequest2Code.get(exLadenReq[i]), 0);
			returnContDepotCode.put(mExportLadenRequest2Code.get(exLadenReq[i]), new ArrayList<String>());
			lateDateTimeDeliveryAtDepot.put(mExportLadenRequest2Code.get(exLadenReq[i]), "");
			cont20RE.put(mExportLadenRequest2Code.get(exLadenReq[i]), L);
			whCode.put(mExportLadenRequest2Code.get(exLadenReq[i]), whCodeList);
			weights.put(mExportLadenRequest2Code.get(exLadenReq[i]), exLadenReq[i].getWeight());
			contCategory.put(mExportLadenRequest2Code.get(exLadenReq[i]), exLadenReq[i].getContainerCategory());
			contCode.put(mExportLadenRequest2Code.get(exLadenReq[i]), exLadenReq[i].getContainerCode());
			shipCompanyCode.put(mExportLadenRequest2Code.get(exLadenReq[i]), "");
			orderCode.put(mExportLadenRequest2Code.get(exLadenReq[i]), exLadenReq[i].getOrderCode());
		}
		
		for(int i = 0; i < nbImEmptyReqs; i++){
			if (imEmptyReqScheduled[i]
				|| !imEmptyReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| imEmptyReq[i].getIsBreakRomooc()
				|| checkConstraintWarehouseVendor(imEmptyReq[i]) == false)
				continue;
			int idx = 0;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();
			Container container = mCode2Container.get(imEmptyReq[i].getContainerCode());
			
			RouteElement e = new RouteElement();
			e.setWarehouse(mCode2Warehouse.get(imEmptyReq[i].getWareHouseCode()));
			e.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e.setImportEmptyRequest(imEmptyReq[i]);
			//e.setEarliestArrivalTime(imEmptyReq[i].getRequestDate());
			//e.setLatestArrivalTime(imEmptyReq[i].getLateDateTimeAttachAtWarehouse());
			e.setServiceDuration(input.getParams().getLinkEmptyContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			whCodeList.add(e.getWarehouse().getCode());

			e = new RouteElement();
			
			e.setContainer(null);
			e.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e.setServiceDuration(input.getParams().getUnlinkEmptyContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			
			isNeedCont.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), 0);
			isNeedReturnCont.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), 1);
			ArrayList<String> rtd = new ArrayList<String>();
			rtd.add(imEmptyReq[i].getDepotContainerCode());
			returnContDepotCode.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), rtd);
			String lateDelivery = "";
			if(imEmptyReq[i].getLateDateTimeReturnEmptyAtDepot() != null)
				lateDelivery = imEmptyReq[i].getLateDateTimeReturnEmptyAtDepot();
			lateDateTimeDeliveryAtDepot.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), lateDelivery);
			cont20RE.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), L);
			whCode.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), whCodeList);
			weights.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), imEmptyReq[i].getWeight());
			contCategory.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), imEmptyReq[i].getContainerCategory());
			contCode.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), imEmptyReq[i].getContainerCode());
			shipCompanyCode.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), "");
			orderCode.put(mImportEmptyRequest2Code.get(imEmptyReq[i]), imEmptyReq[i].getOrderCode());
		}
		
		for(int i = 0; i < nbImLadenReqs; i++){
			if (imLadenReqScheduled[i]
				|| !imLadenReq[i].getContainerCategory()
				.contains(ContainerCategoryEnum.CATEGORY20)
				|| imLadenReq[i].getIsBreakRomooc()	
				|| checkConstraintWarehouseVendor(imLadenReq[i]) == false)
				continue;
			int idx = 0;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			ArrayList<String> whCodeList = new ArrayList<String>();
			Container container = mCode2Container.get(imLadenReq[i].getContainerCode());
			
			RouteElement e = new RouteElement();
			e.setWarehouse(mCode2Warehouse.get(imLadenReq[i].getWareHouseCode()));
			e.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);
			e.setImportLadenRequest(imLadenReq[i]);
			e.setEarliestArrivalTime(imLadenReq[i].getEarlyDateTimePickupAtPort());
			e.setLatestArrivalTime(imLadenReq[i].getLateDateTimePickupAtPort());
			e.setServiceDuration(input.getParams().getLinkLoadedContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			whCodeList.add(e.getWarehouse().getCode());

			e = new RouteElement();
			
			e.setContainer(null);
			e.setAction(ActionEnum.UNLINK_LOADED_CONTAINER_AT_WAREHOUSE);
			e.setEarliestArrivalTime(imLadenReq[i].getEarlyDateTimeUnloadAtWarehouse());
			e.setLatestArrivalTime(imLadenReq[i].getLateDateTimeUnloadAtWarehouse());
			e.setServiceDuration(input.getParams().getUnlinkLoadedContainerDuration());
			idx++;
			e.setIndex(idx);
			L.add(e);
			
			isNeedCont.put(mImportLadenRequest2Code.get(imLadenReq[i]), 0);
			isNeedReturnCont.put(mImportLadenRequest2Code.get(imLadenReq[i]), 0);
			returnContDepotCode.put(mImportLadenRequest2Code.get(imLadenReq[i]), new ArrayList<String>());
			lateDateTimeDeliveryAtDepot.put(mImportLadenRequest2Code.get(imLadenReq[i]), "");
			cont20RE.put(mImportLadenRequest2Code.get(imLadenReq[i]), L);
			whCode.put(mImportLadenRequest2Code.get(imLadenReq[i]), whCodeList);
			weights.put(mImportLadenRequest2Code.get(imLadenReq[i]), imLadenReq[i].getWeight());
			contCategory.put(mImportLadenRequest2Code.get(imLadenReq[i]), imLadenReq[i].getContainerCategory());
			contCode.put(mImportLadenRequest2Code.get(imLadenReq[i]), imLadenReq[i].getContainerCode());
			shipCompanyCode.put(mImportLadenRequest2Code.get(imLadenReq[i]), "");
			orderCode.put(mImportLadenRequest2Code.get(imLadenReq[i]), imLadenReq[i].getOrderCode());
		}
		Container20Requests cont20Reqs = new Container20Requests();
		
		cont20Reqs.cont20RE = cont20RE;
		cont20Reqs.isNeedCont = isNeedCont;
		cont20Reqs.isNeedReturnCont = isNeedReturnCont;
		cont20Reqs.returnContDepotCode = returnContDepotCode;
		cont20Reqs.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
		cont20Reqs.whCode = whCode;
		cont20Reqs.weights = weights;
		cont20Reqs.contCategory = contCategory;
		cont20Reqs.contCode = contCode;
		cont20Reqs.shipCompanyCode = shipCompanyCode;
		cont20Reqs.orderCode = orderCode;
		return cont20Reqs;
	}
	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		this.input = input;

		InputAnalyzer IA = new InputAnalyzer();
		IA.standardize(input);
		
		initLog();
		
		//modifyContainerCode();
		
		mapData();
		init();

		mTruck2Route = new HashMap<Truck, TruckRoute>();
		mTruck2Itinerary = new HashMap<Truck, TruckItinerary>();

		trucks = input.getTrucks();
		moocs = input.getMoocs();
		containers = input.getContainers();
		
		nbExReqs = 0;
		nbImReqs = 0;
		nbWhReqs = 0;
		nbEmptyContainerFromDepotReqs = 0;
		nbEmptyContainerToDepotReqs = 0;
		nbTransportContainerReqs = 0;
		nbExLadenReqs = 0;
		nbExEmptyReqs = 0;
		nbImEmptyReqs = 0;
		nbImLadenReqs = 0;
		
		exReq = getSortedExportRequests();
		imReq = getSortedImportRequests();
		whReq = getSortedWarehouseTransportRequests();
		emptyContainerFromDepotReq = getSortedEmptyContainerFromDepotRequests();
		emptyContainerToDepotReq = getSortedEmptyContainerToDepotRequests();
		transportContainerReq = getSortedTransportContainerRequests();
		exLadenReq = getSortedExportLadenRequests();
		exEmptyReq = getSortedExportEmptyRequests();
		imLadenReq = getSortedImportLadenRequests();
		imEmptyReq = getSortedImportEmptyRequests();
		
		if(exReq != null) nbExReqs = exReq.length;
		if(imReq != null) nbImReqs = imReq.length;
		if(whReq != null) nbWhReqs = whReq.length;
		if(emptyContainerFromDepotReq != null) nbEmptyContainerFromDepotReqs = emptyContainerFromDepotReq.length;
		if(emptyContainerToDepotReq != null) nbEmptyContainerToDepotReqs = emptyContainerToDepotReq.length;
		if(transportContainerReq != null) nbTransportContainerReqs = transportContainerReq.length;
		if(exLadenReq != null) nbExLadenReqs = exLadenReq.length;
		if(exEmptyReq != null) nbExEmptyReqs = exEmptyReq.length;
		if(imLadenReq != null) nbImLadenReqs = imLadenReq.length;
		if(imEmptyReq != null) nbImEmptyReqs = imEmptyReq.length;
		
		exReqScheduled = new boolean[nbExReqs];
		imReqScheduled = new boolean[nbImReqs];
		whReqScheduled = new boolean[nbWhReqs];
		emptyContainerFromDepotReqScheduled = new boolean[nbEmptyContainerFromDepotReqs];
		emptyContainerToDepotReqScheduled = new boolean[nbEmptyContainerToDepotReqs];
		transportContainerReqScheduled = new boolean[nbTransportContainerReqs];
		exLadenReqScheduled = new boolean[nbExLadenReqs];
		exEmptyReqScheduled = new boolean[nbExEmptyReqs];
		imLadenReqScheduled = new boolean[nbImLadenReqs];
		imEmptyReqScheduled = new boolean[nbImEmptyReqs];
		
		
		for (int i = 0; i < nbExReqs; i++)
			exReqScheduled[i] = false;
		for (int i = 0; i < nbImReqs; i++)
			imReqScheduled[i] = false;
		for (int i = 0; i < nbWhReqs; i++)
			whReqScheduled[i] = false;
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++)
			emptyContainerFromDepotReqScheduled[i] = false;
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++)
			emptyContainerToDepotReqScheduled[i] = false;
		for (int i = 0; i < nbTransportContainerReqs; i++)
			transportContainerReqScheduled[i] = false;
		for(int i = 0; i < nbExLadenReqs; i++)
			exLadenReqScheduled[i] = false;
		for(int i = 0; i < nbExEmptyReqs; i++)
			exEmptyReqScheduled[i] = false;
		for(int i = 0; i < nbImLadenReqs; i++)
			imLadenReqScheduled[i] = false;
		for(int i = 0; i < nbImEmptyReqs; i++)
			imEmptyReqScheduled[i] = false;
		
		
		mExReq2Index = new HashMap<ExportContainerRequest, Integer>();
		mImReq2Index = new HashMap<ImportContainerRequest, Integer>();
		mWhReq2Index = new HashMap<WarehouseContainerTransportRequest, Integer>();
		mEmptyContainerFromDepotReq2Index = new HashMap<EmptyContainerFromDepotRequest, Integer>();
		mEmptyContainerToDepotReq2Index = new HashMap<EmptyContainerToDepotRequest, Integer>();
		mTransportContainerReq2Index = new HashMap<TransportContainerRequest, Integer>();
		mExportLadenRequest2Index = new HashMap<ExportLadenRequests, Integer>();
		mExportEmptyRequest2Index = new HashMap<ExportEmptyRequests, Integer>();
		mImportLadenRequest2Index = new HashMap<ImportLadenRequests, Integer>();
		mImportEmptyRequest2Index = new HashMap<ImportEmptyRequests, Integer>();
		
		mExReq2Code = new HashMap<ExportContainerRequest, Integer>();
		mImReq2Code = new HashMap<ImportContainerRequest, Integer>();
		mWhReq2Code = new HashMap<WarehouseContainerTransportRequest, Integer>();
		mEmptyContainerFromDepotReq2Code = new HashMap<EmptyContainerFromDepotRequest, Integer>();
		mEmptyContainerToDepotReq2Code = new HashMap<EmptyContainerToDepotRequest, Integer>();
		mTransportContainerReq2Code = new HashMap<TransportContainerRequest, Integer>();
		mExportLadenRequest2Code = new HashMap<ExportLadenRequests, Integer>();
		mExportEmptyRequest2Code = new HashMap<ExportEmptyRequests, Integer>();
		mImportLadenRequest2Code = new HashMap<ImportLadenRequests, Integer>();
		mImportEmptyRequest2Code = new HashMap<ImportEmptyRequests, Integer>();
		
		int t = -1;
		for (int i = 0; i < nbExReqs; i++) {
			mExReq2Index.put(exReq[i], i);
			t++;
			mExReq2Code.put(exReq[i], t);
		}
		for (int i = 0; i < nbImReqs; i++) {
			mImReq2Index.put(imReq[i], i);
			t++;
			mImReq2Code.put(imReq[i], t);
		}
		for (int i = 0; i < nbWhReqs; i++) {
			mWhReq2Index.put(whReq[i], i);
			t++;
			mWhReq2Code.put(whReq[i], t);
		}
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++){
			mEmptyContainerFromDepotReq2Index.put(
					emptyContainerFromDepotReq[i], i);
			t++;
			mEmptyContainerFromDepotReq2Code.put(
					emptyContainerFromDepotReq[i], t);
		}
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++){
			mEmptyContainerToDepotReq2Index.put(emptyContainerToDepotReq[i], i);
			t++;
			mEmptyContainerToDepotReq2Code.put(emptyContainerToDepotReq[i], t);
		}
		for (int i = 0; i < nbTransportContainerReqs; i++){
			mTransportContainerReq2Index.put(transportContainerReq[i], i);
			t++;
			mTransportContainerReq2Code.put(transportContainerReq[i], t);
		}
		for(int i = 0; i < nbExLadenReqs; i++){
			mExportLadenRequest2Index.put(exLadenReq[i], i);
			t++;
			mExportLadenRequest2Code.put(exLadenReq[i], t);
		}
		for(int i = 0; i < nbExEmptyReqs; i++){
			mExportEmptyRequest2Index.put(exEmptyReq[i], i);
			t++;
			mExportEmptyRequest2Code.put(exEmptyReq[i], t);
		}
		for(int i = 0; i < nbImLadenReqs; i++){
			mImportLadenRequest2Index.put(imLadenReq[i], i);
			t++;
			mImportLadenRequest2Code.put(imLadenReq[i], t);
		}
		for(int i = 0; i < nbImEmptyReqs; i++){
			mImportEmptyRequest2Index.put(imEmptyReq[i], i);
			t++;
			mImportEmptyRequest2Code.put(imEmptyReq[i], t);
		}
		
		
		cand_sol = new CandidateSolution();

		CandidateRouteComposer candidate_routes = new CandidateRouteComposer();

		cont20reqs = getContainer20Requests();
		boolean specPosible = true;
		while (true) {
			candidate_routes.clear();
			if(specPosible){
				exploreKepContainer(cont20reqs, candidate_routes);
			//exploreKep(candidate_routes);
				exploreTangBo(candidate_routes);
				exploreSwapImportExport(candidate_routes);
			}
			System.out.println(name()
					+ "::solve, special operators, candidates_routes.sz = "
					+ candidate_routes.size());
			if (candidate_routes.size() == 0) {
				specPosible = false;
				System.out.println(name() + "::solve, start exploreDirectRouteExport");
				exploreDirectRouteExport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImport");
				exploreDirectRouteImport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteWarehouseWarehouse");
				exploreDirectRouteWarehouseWarehouse(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteWarehouseWarehouse");
				
//				System.out.println(name() + "::solve, start exploreDirectRouteEmptyContainerFromDepotRequest");
//				exploreDirectRouteEmptyContainerFromDepotRequest(candidate_routes);
//				System.out.println(name() + "::solve, finish exploreDirectRouteEmptyContainerFromDepotRequest");
//				
//				System.out.println(name() + "::solve, start exploreDirectRouteEmptyContainerToDepotRequest");
//				exploreDirectRouteEmptyContainerToDepotRequest(candidate_routes);
//				System.out.println(name() + "::solve, finish exploreDirectRouteEmptyContainerToDepotRequest");
//				
//				System.out.println(name() + "::solve, start exploreDirectRouteTransportContainerRequest");
//				exploreDirectRouteTransportContainerRequest(candidate_routes);
//				System.out.println(name() + "::solve, finish exploreDirectRouteTransportContainerRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImportLadenRequest");
				exploreDirectRouteImportLadenRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImportLadenRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImportEmptyRequest");
				exploreDirectRouteImportEmptyRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImportEmptyRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteExportLadenRequest");
				exploreDirectRouteExportLadenRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExportLadenRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteExportEmptyRequest");
				exploreDirectRouteExportEmptyRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExportEmptyRequest");
				
				System.out.println(name()
						+ "::solve, direct operators, candidates_routes.sz = "
						+ candidate_routes.size());
				if (candidate_routes.size() == 0) {
					break;
				} else {
					System.out.println(name()
							+ "::solve, START direct performBestRouteComposer");
					candidate_routes.performBestRouteComposer();
				}
			} else {
				System.out.println(name()
						+ "::solve, START special performBestRouteComposer");
				candidate_routes.performBestRouteComposer();
			}

			print();
			System.out.println("------------------------------------");

		}

		recoverContainerCode();

		TruckRouteList[] TR = new TruckRouteList[trucks.length];
		for (int i = 0; i < trucks.length; i++) {
			TruckItinerary I = mTruck2Itinerary.get(trucks[i]);
			if (I != null)
				TR[i] = I.establishRoute();
		}

		double totalDistance = 0;
		for (int i = 0; i < TR.length; i++) {
			if (TR[i] == null)
				continue;
			TruckRouteList tr = TR[i];
			totalDistance += TR[i].getDistance();
			// RouteElement[] e = tr.getNodes();
			// if (e != null && e.length > 0) {
			// totalDistance = totalDistance + e[e.length - 1].getDistance();
			// }
		}

		StatisticInformation infos = new StatisticInformation(totalDistance,
				TR.length);

		ArrayList<ExportContainerRequest> lstUExReq = new ArrayList<ExportContainerRequest>();
		ArrayList<ImportContainerRequest> lstUImReq = new ArrayList<ImportContainerRequest>();
		ArrayList<WarehouseContainerTransportRequest> lstUWhReq = new ArrayList<WarehouseContainerTransportRequest>();
		ArrayList<ExportLadenRequests> lstUExLadenReq = new ArrayList<ExportLadenRequests>();
		ArrayList<ExportEmptyRequests> lstUExEmptyReq = new ArrayList<ExportEmptyRequests>();
		ArrayList<ImportLadenRequests> lstUImLadenReq = new ArrayList<ImportLadenRequests>();
		ArrayList<ImportEmptyRequests> lstUImEmptyReq = new ArrayList<ImportEmptyRequests>();
		
		
		//for (int i = 0; i < exReqScheduled.length; i++)
		for (int i = 0; i < nbExReqs; i++)
			if (exReqScheduled[i] == false)
				lstUExReq.add(exReq[i]);

		//for (int i = 0; i < imReqScheduled.length; i++)
		for (int i = 0; i < nbImReqs; i++)
			if (imReqScheduled[i] == false)
				lstUImReq.add(imReq[i]);

		//for (int i = 0; i < whReqScheduled.length; i++)
		for (int i = 0; i < nbWhReqs; i++)
			if (whReqScheduled[i] == false)
				lstUWhReq.add(whReq[i]);

		for(int i = 0; i < nbExLadenReqs; i++)
			if(exLadenReqScheduled[i] == false)
				lstUExLadenReq.add(exLadenReq[i]);
		for(int i = 0; i < nbExEmptyReqs; i++)
			if(exEmptyReqScheduled[i] == false)
				lstUExEmptyReq.add(exEmptyReq[i]);
		for(int i = 0; i < nbImLadenReqs; i++)
			if(imLadenReqScheduled[i] == false)
				lstUImLadenReq.add(imLadenReq[i]);
		for(int i = 0; i < nbImEmptyReqs; i++)
			if(imEmptyReqScheduled[i] == false)
				lstUImEmptyReq.add(imEmptyReq[i]);
		
		
		
		ExportContainerRequest[] unScheduledExportRequests = new ExportContainerRequest[lstUExReq
				.size()];
		ImportContainerRequest[] unScheduledImportRequests = new ImportContainerRequest[lstUImReq
				.size()];
		WarehouseContainerTransportRequest[] unScheduledWarehouseRequests = new WarehouseContainerTransportRequest[lstUWhReq
				.size()];
		ExportLadenRequests[] unScheduledExportLadenRequests = new ExportLadenRequests[lstUExLadenReq.size()];
		ExportEmptyRequests[] unScheduledExportEmptyRequests = new ExportEmptyRequests[lstUExEmptyReq.size()];
		ImportLadenRequests[] unScheduledImportLadenRequests = new ImportLadenRequests[lstUImLadenReq.size()];
		ImportEmptyRequests[] unScheduledImportEmptyRequests = new ImportEmptyRequests[lstUImEmptyReq.size()];
		
		for (int i = 0; i < lstUExReq.size(); i++)
			unScheduledExportRequests[i] = lstUExReq.get(i);
		for (int i = 0; i < lstUImReq.size(); i++)
			unScheduledImportRequests[i] = lstUImReq.get(i);
		for (int i = 0; i < lstUWhReq.size(); i++)
			unScheduledWarehouseRequests[i] = lstUWhReq.get(i);

		for(int i = 0; i < lstUExLadenReq.size(); i++)
			unScheduledExportLadenRequests[i] = lstUExLadenReq.get(i);
		for(int i = 0; i < lstUExEmptyReq.size(); i++)
			unScheduledExportEmptyRequests[i] = lstUExEmptyReq.get(i);
		for(int i = 0; i < lstUImLadenReq.size(); i++)
			unScheduledImportLadenRequests[i] = lstUImLadenReq.get(i);
		for(int i = 0; i < lstUImEmptyReq.size(); i++)
			unScheduledImportEmptyRequests[i] = lstUImEmptyReq.get(i);
		
		
		// ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
		// infos, "OK");
		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
				unScheduledExportRequests, unScheduledImportRequests,
				unScheduledWarehouseRequests, unScheduledExportLadenRequests, unScheduledExportEmptyRequests,
				unScheduledImportLadenRequests, unScheduledImportEmptyRequests, infos, "OK");

		finalizeLog();
		return sol;
	}
	
	public void readScheduleFile(){
		mOrderCode2truckCode = new HashMap<String, String>();
		mOrderCode2moocCode = new HashMap<String, String>();
		
		String schedulefile = "E:/Project/smartlog/doc/data/input/KH/" + "190129_ANC-adapt.xlsx";
    	try{
	    	File file = new File(schedulefile);
	    	FileInputStream fin = new FileInputStream(file);
	    	XSSFWorkbook mbook = new XSSFWorkbook (fin); 
	    	XSSFSheet scheduleSheet = mbook.getSheet("Sheet1");
	    	if(scheduleSheet == null){
	    		mbook.close();
	    		return;
	    	}
			int nbRows = scheduleSheet.getLastRowNum();
	
			for(int i = 1; i <= nbRows; i++){
				Row row = scheduleSheet.getRow(i);
				String truckCode = null;
				Cell cell = row.getCell(6);
				if(cell != null)
					truckCode = cell.getStringCellValue();
				String moocCode = null;
				cell = row.getCell(7);
				if(cell != null)
					moocCode = cell.getStringCellValue();
				String orderCode = null;
				cell = row.getCell(10);
				if(cell != null)
					orderCode = cell.getStringCellValue();
				mOrderCode2truckCode.put(orderCode, truckCode);
				mOrderCode2moocCode.put(orderCode, moocCode);
			}
			
			mbook.close();
    	} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	public void createManualRoute(CandidateRouteComposer candidateRouteComposer) {
		
	}
	
	public ContainerTruckMoocSolution compare(ContainerTruckMoocInput input) {
		this.input = input;
		this.test = true;
		//System.out.println("comparison solution");

		InputAnalyzer IA = new InputAnalyzer();
		IA.standardize(input);
		
		initLog();
		
		//modifyContainerCode();
		
		mapData();
		init();

		mTruck2Route = new HashMap<Truck, TruckRoute>();
		mTruck2Itinerary = new HashMap<Truck, TruckItinerary>();

		trucks = input.getTrucks();
		moocs = input.getMoocs();
		containers = input.getContainers();
		
		nbExReqs = 0;
		nbImReqs = 0;
		nbWhReqs = 0;
		nbEmptyContainerFromDepotReqs = 0;
		nbEmptyContainerToDepotReqs = 0;
		nbTransportContainerReqs = 0;
		nbExLadenReqs = 0;
		nbExEmptyReqs = 0;
		nbImEmptyReqs = 0;
		nbImLadenReqs = 0;
		
		exReq = getSortedExportRequests();
		imReq = getSortedImportRequests();
		whReq = getSortedWarehouseTransportRequests();
		emptyContainerFromDepotReq = getSortedEmptyContainerFromDepotRequests();
		emptyContainerToDepotReq = getSortedEmptyContainerToDepotRequests();
		transportContainerReq = getSortedTransportContainerRequests();
		exLadenReq = getSortedExportLadenRequests();
		exEmptyReq = getSortedExportEmptyRequests();
		imLadenReq = getSortedImportLadenRequests();
		imEmptyReq = getSortedImportEmptyRequests();
		
		if(exReq != null) nbExReqs = exReq.length;
		if(imReq != null) nbImReqs = imReq.length;
		if(whReq != null) nbWhReqs = whReq.length;
		if(emptyContainerFromDepotReq != null) nbEmptyContainerFromDepotReqs = emptyContainerFromDepotReq.length;
		if(emptyContainerToDepotReq != null) nbEmptyContainerToDepotReqs = emptyContainerToDepotReq.length;
		if(transportContainerReq != null) nbTransportContainerReqs = transportContainerReq.length;
		if(exLadenReq != null) nbExLadenReqs = exLadenReq.length;
		if(exEmptyReq != null) nbExEmptyReqs = exEmptyReq.length;
		if(imLadenReq != null) nbImLadenReqs = imLadenReq.length;
		if(imEmptyReq != null) nbImEmptyReqs = imEmptyReq.length;
		
		exReqScheduled = new boolean[nbExReqs];
		imReqScheduled = new boolean[nbImReqs];
		whReqScheduled = new boolean[nbWhReqs];
		emptyContainerFromDepotReqScheduled = new boolean[nbEmptyContainerFromDepotReqs];
		emptyContainerToDepotReqScheduled = new boolean[nbEmptyContainerToDepotReqs];
		transportContainerReqScheduled = new boolean[nbTransportContainerReqs];
		exLadenReqScheduled = new boolean[nbExLadenReqs];
		exEmptyReqScheduled = new boolean[nbExEmptyReqs];
		imLadenReqScheduled = new boolean[nbImLadenReqs];
		imEmptyReqScheduled = new boolean[nbImEmptyReqs];
		
		
		for (int i = 0; i < nbExReqs; i++)
			exReqScheduled[i] = false;
		for (int i = 0; i < nbImReqs; i++)
			imReqScheduled[i] = false;
		for (int i = 0; i < nbWhReqs; i++)
			whReqScheduled[i] = false;
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++)
			emptyContainerFromDepotReqScheduled[i] = false;
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++)
			emptyContainerToDepotReqScheduled[i] = false;
		for (int i = 0; i < nbTransportContainerReqs; i++)
			transportContainerReqScheduled[i] = false;
		for(int i = 0; i < nbExLadenReqs; i++)
			exLadenReqScheduled[i] = false;
		for(int i = 0; i < nbExEmptyReqs; i++)
			exEmptyReqScheduled[i] = false;
		for(int i = 0; i < nbImLadenReqs; i++)
			imLadenReqScheduled[i] = false;
		for(int i = 0; i < nbImEmptyReqs; i++)
			imEmptyReqScheduled[i] = false;
		
		
		mExReq2Index = new HashMap<ExportContainerRequest, Integer>();
		mImReq2Index = new HashMap<ImportContainerRequest, Integer>();
		mWhReq2Index = new HashMap<WarehouseContainerTransportRequest, Integer>();
		mEmptyContainerFromDepotReq2Index = new HashMap<EmptyContainerFromDepotRequest, Integer>();
		mEmptyContainerToDepotReq2Index = new HashMap<EmptyContainerToDepotRequest, Integer>();
		mTransportContainerReq2Index = new HashMap<TransportContainerRequest, Integer>();
		mExportLadenRequest2Index = new HashMap<ExportLadenRequests, Integer>();
		mExportEmptyRequest2Index = new HashMap<ExportEmptyRequests, Integer>();
		mImportLadenRequest2Index = new HashMap<ImportLadenRequests, Integer>();
		mImportEmptyRequest2Index = new HashMap<ImportEmptyRequests, Integer>();
		
		mExReq2Code = new HashMap<ExportContainerRequest, Integer>();
		mImReq2Code = new HashMap<ImportContainerRequest, Integer>();
		mWhReq2Code = new HashMap<WarehouseContainerTransportRequest, Integer>();
		mEmptyContainerFromDepotReq2Code = new HashMap<EmptyContainerFromDepotRequest, Integer>();
		mEmptyContainerToDepotReq2Code = new HashMap<EmptyContainerToDepotRequest, Integer>();
		mTransportContainerReq2Code = new HashMap<TransportContainerRequest, Integer>();
		mExportLadenRequest2Code = new HashMap<ExportLadenRequests, Integer>();
		mExportEmptyRequest2Code = new HashMap<ExportEmptyRequests, Integer>();
		mImportLadenRequest2Code = new HashMap<ImportLadenRequests, Integer>();
		mImportEmptyRequest2Code = new HashMap<ImportEmptyRequests, Integer>();
		
		int t = -1;
		for (int i = 0; i < nbExReqs; i++) {
			mExReq2Index.put(exReq[i], i);
			t++;
			mExReq2Code.put(exReq[i], t);
		}
		for (int i = 0; i < nbImReqs; i++) {
			mImReq2Index.put(imReq[i], i);
			t++;
			mImReq2Code.put(imReq[i], t);
		}
		for (int i = 0; i < nbWhReqs; i++) {
			mWhReq2Index.put(whReq[i], i);
			t++;
			mWhReq2Code.put(whReq[i], t);
		}
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++){
			mEmptyContainerFromDepotReq2Index.put(
					emptyContainerFromDepotReq[i], i);
			t++;
			mEmptyContainerFromDepotReq2Code.put(
					emptyContainerFromDepotReq[i], t);
		}
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++){
			mEmptyContainerToDepotReq2Index.put(emptyContainerToDepotReq[i], i);
			t++;
			mEmptyContainerToDepotReq2Code.put(emptyContainerToDepotReq[i], t);
		}
		for (int i = 0; i < nbTransportContainerReqs; i++){
			mTransportContainerReq2Index.put(transportContainerReq[i], i);
			t++;
			mTransportContainerReq2Code.put(transportContainerReq[i], t);
		}
		for(int i = 0; i < nbExLadenReqs; i++){
			mExportLadenRequest2Index.put(exLadenReq[i], i);
			t++;
			mExportLadenRequest2Code.put(exLadenReq[i], t);
		}
		for(int i = 0; i < nbExEmptyReqs; i++){
			mExportEmptyRequest2Index.put(exEmptyReq[i], i);
			t++;
			mExportEmptyRequest2Code.put(exEmptyReq[i], t);
		}
		for(int i = 0; i < nbImLadenReqs; i++){
			mImportLadenRequest2Index.put(imLadenReq[i], i);
			t++;
			mImportLadenRequest2Code.put(imLadenReq[i], t);
		}
		for(int i = 0; i < nbImEmptyReqs; i++){
			mImportEmptyRequest2Index.put(imEmptyReq[i], i);
			t++;
			mImportEmptyRequest2Code.put(imEmptyReq[i], t);
		}
		
		
		cand_sol = new CandidateSolution();
		
		readScheduleFile();

		CandidateRouteComposer candidate_routes = new CandidateRouteComposer();

		cont20reqs = getContainer20Requests();
		boolean specPosible = true;
		while (true) {
			candidate_routes.clear();
			if(specPosible){
				exploreKepContainer(cont20reqs, candidate_routes);
			//exploreKep(candidate_routes);
				exploreTangBo(candidate_routes);
				exploreSwapImportExport(candidate_routes);
			}
			System.out.println(name()
					+ "::solve, special operators, candidates_routes.sz = "
					+ candidate_routes.size());
			if (candidate_routes.size() == 0) {
				specPosible = false;
				System.out.println(name() + "::solve, start exploreDirectRouteExport");
				exploreDirectRouteExport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImport");
				exploreDirectRouteImport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteWarehouseWarehouse");
				exploreDirectRouteWarehouseWarehouse(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteWarehouseWarehouse");
			
				System.out.println(name() + "::solve, start exploreDirectRouteImportLadenRequest");
				exploreDirectRouteImportLadenRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImportLadenRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImportEmptyRequest");
				exploreDirectRouteImportEmptyRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImportEmptyRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteExportLadenRequest");
				exploreDirectRouteExportLadenRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExportLadenRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteExportEmptyRequest");
				exploreDirectRouteExportEmptyRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExportEmptyRequest");
				
				System.out.println(name()
						+ "::solve, direct operators, candidates_routes.sz = "
						+ candidate_routes.size());
				if (candidate_routes.size() == 0) {
					break;
				} else {
					System.out.println(name()
							+ "::solve, START direct performBestRouteComposer");
					candidate_routes.performBestRouteComposer();
				}
			} else {
				System.out.println(name()
						+ "::solve, START special performBestRouteComposer");
				candidate_routes.performBestRouteComposer();
			}

			print();
			System.out.println("------------------------------------");

		}

		recoverContainerCode();

		TruckRouteList[] TR = new TruckRouteList[trucks.length];
		for (int i = 0; i < trucks.length; i++) {
			TruckItinerary I = mTruck2Itinerary.get(trucks[i]);
			if (I != null)
				TR[i] = I.establishRoute();
		}

		double totalDistance = 0;
		for (int i = 0; i < TR.length; i++) {
			if (TR[i] == null)
				continue;
			TruckRouteList tr = TR[i];
			totalDistance += TR[i].getDistance();
			// RouteElement[] e = tr.getNodes();
			// if (e != null && e.length > 0) {
			// totalDistance = totalDistance + e[e.length - 1].getDistance();
			// }
		}

		StatisticInformation infos = new StatisticInformation(totalDistance,
				TR.length);

		ArrayList<ExportContainerRequest> lstUExReq = new ArrayList<ExportContainerRequest>();
		ArrayList<ImportContainerRequest> lstUImReq = new ArrayList<ImportContainerRequest>();
		ArrayList<WarehouseContainerTransportRequest> lstUWhReq = new ArrayList<WarehouseContainerTransportRequest>();
		ArrayList<ExportLadenRequests> lstUExLadenReq = new ArrayList<ExportLadenRequests>();
		ArrayList<ExportEmptyRequests> lstUExEmptyReq = new ArrayList<ExportEmptyRequests>();
		ArrayList<ImportLadenRequests> lstUImLadenReq = new ArrayList<ImportLadenRequests>();
		ArrayList<ImportEmptyRequests> lstUImEmptyReq = new ArrayList<ImportEmptyRequests>();
		
		
		//for (int i = 0; i < exReqScheduled.length; i++)
		for (int i = 0; i < nbExReqs; i++)
			if (exReqScheduled[i] == false)
				lstUExReq.add(exReq[i]);

		//for (int i = 0; i < imReqScheduled.length; i++)
		for (int i = 0; i < nbImReqs; i++)
			if (imReqScheduled[i] == false)
				lstUImReq.add(imReq[i]);

		//for (int i = 0; i < whReqScheduled.length; i++)
		for (int i = 0; i < nbWhReqs; i++)
			if (whReqScheduled[i] == false)
				lstUWhReq.add(whReq[i]);

		for(int i = 0; i < nbExLadenReqs; i++)
			if(exLadenReqScheduled[i] == false)
				lstUExLadenReq.add(exLadenReq[i]);
		for(int i = 0; i < nbExEmptyReqs; i++)
			if(exEmptyReqScheduled[i] == false)
				lstUExEmptyReq.add(exEmptyReq[i]);
		for(int i = 0; i < nbImLadenReqs; i++)
			if(imLadenReqScheduled[i] == false)
				lstUImLadenReq.add(imLadenReq[i]);
		for(int i = 0; i < nbImEmptyReqs; i++)
			if(imEmptyReqScheduled[i] == false)
				lstUImEmptyReq.add(imEmptyReq[i]);
		
		
		
		ExportContainerRequest[] unScheduledExportRequests = new ExportContainerRequest[lstUExReq
				.size()];
		ImportContainerRequest[] unScheduledImportRequests = new ImportContainerRequest[lstUImReq
				.size()];
		WarehouseContainerTransportRequest[] unScheduledWarehouseRequests = new WarehouseContainerTransportRequest[lstUWhReq
				.size()];
		ExportLadenRequests[] unScheduledExportLadenRequests = new ExportLadenRequests[lstUExLadenReq.size()];
		ExportEmptyRequests[] unScheduledExportEmptyRequests = new ExportEmptyRequests[lstUExEmptyReq.size()];
		ImportLadenRequests[] unScheduledImportLadenRequests = new ImportLadenRequests[lstUImLadenReq.size()];
		ImportEmptyRequests[] unScheduledImportEmptyRequests = new ImportEmptyRequests[lstUImEmptyReq.size()];
		
		for (int i = 0; i < lstUExReq.size(); i++)
			unScheduledExportRequests[i] = lstUExReq.get(i);
		for (int i = 0; i < lstUImReq.size(); i++)
			unScheduledImportRequests[i] = lstUImReq.get(i);
		for (int i = 0; i < lstUWhReq.size(); i++)
			unScheduledWarehouseRequests[i] = lstUWhReq.get(i);

		for(int i = 0; i < lstUExLadenReq.size(); i++)
			unScheduledExportLadenRequests[i] = lstUExLadenReq.get(i);
		for(int i = 0; i < lstUExEmptyReq.size(); i++)
			unScheduledExportEmptyRequests[i] = lstUExEmptyReq.get(i);
		for(int i = 0; i < lstUImLadenReq.size(); i++)
			unScheduledImportLadenRequests[i] = lstUImLadenReq.get(i);
		for(int i = 0; i < lstUImEmptyReq.size(); i++)
			unScheduledImportEmptyRequests[i] = lstUImEmptyReq.get(i);
		
		
		// ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
		// infos, "OK");
		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
				unScheduledExportRequests, unScheduledImportRequests,
				unScheduledWarehouseRequests, unScheduledExportLadenRequests, unScheduledExportEmptyRequests,
				unScheduledImportLadenRequests, unScheduledImportEmptyRequests, infos, "OK");

		finalizeLog();
		return sol;
	}

	public void commitItinerary(TruckRouteInfo4Request tri) {
		cand_sol.commitItinerary(tri);
	}

	public void improveSpecialOperators() {
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck ti = input.getTrucks()[i];
			TruckItinerary Ii = cand_sol.getItineraryOfTruck(ti);
			if (Ii == null || Ii.size() == 0)
				continue;
			TruckRoute tri = Ii.getLastTruckRoute();

			for (int j = i + 1; j < input.getTrucks().length; j++) {
				Truck tj = input.getTrucks()[j];
				TruckItinerary Ij = cand_sol.getItineraryOfTruck(tj);
				if (Ij == null || Ij.size() == 0)
					continue;
			}
		}
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
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			TruckItinerary I = mTruck2Itinerary.get(truck);
			if (I != null) {
				TruckRoute tr = I.establishTruckRoute();
				if (tr != null) {
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

		// ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(null,
		// null,null,null, infos, "OK");
		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution();

		return sol;

	}
	/*
	public TruckRouteInfo4Request createNewDirectRoute4ExportRequest(
			ExportContainerRequest r) {
		String warehouseCode = r.getWareHouseCode();
		logln(name() + "::createNewDirectRoute4ExportRequest, warehouseCode = "
				+ warehouseCode);

		Warehouse warehouse = mCode2Warehouse.get(warehouseCode);

		String warehouseLocationCode = warehouse.getLocationCode();
		String portCode = r.getPortCode();
		String portLocationCode = mCode2Port.get(portCode).getLocationCode();
		int earlyPickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeLoadAtWarehouse());
		int latePickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimeLoadAtWarehouse());

		Port port = mCode2Port.get(r.getPortCode());
		
		ComboContainerMoocTruck sel_combo = null;
		double minDistance = Integer.MAX_VALUE;

		ExportSequenceSolver sequenceSolver = new ExportSequenceSolver();
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			for (int j = 0; j < input.getMoocs().length; j++) {
				Mooc mooc = input.getMoocs()[j];
				for (int k = 0; k < input.getContainers().length; k++) {
					Container container = input.getContainers()[k];
					// String depotContainerCode =
					// container.getDepotContainerCode();
					// String shipCompanyCode =
					// mDepotContainerCode2ShipCompanyCode.get(depotContainerCode);
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, depotContainer = " +
					// depotContainerCode + ", shipCompanyCode = " +
					// shipCompanyCode +
					// ", r.getShipCompanyCode = " + r.getShipCompanyCode());
					HashSet<Container> C = mShipCompanyCode2Containers.get(r
							.getShipCompanyCode());

					if (!C.contains(container))
						continue;
					// if(!r.getShipCompanyCode().equals(shipCompanyCode))
					// continue;

					ComboContainerMoocTruck combo = findLastAvailable(truck,
							mooc, container);
					if (combo == null)
						continue;

					int startTime = combo.startTime;
					String startLocationCode = combo.lastLocationCode;
					
					int arrivalTimeWarehouse = startTime
							+ getTravelTime(startLocationCode,
									warehouseLocationCode);

					

					if (arrivalTimeWarehouse > latePickupWarehouse)
						continue;

					double distance = combo.extraDistance
							+ getDistance(combo.lastLocationCode,
									warehouseLocationCode);
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = "
					// + arrivalTimeWarehouse +
					// ", latePickupWarehouse = " + latePickupWarehouse +
					// ", distance = " + distance);
					if (distance < minDistance) {
						minDistance = distance;
						sel_combo = combo;
					}
				}
			}
		}
		if (sel_combo == null) {
			logln(name()
					+ "::createNewDirectRoute4ExportRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if (sel_combo.routeElement == null) {
			TruckRoute tr = createDirectRouteForExportRequest(r,
					sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out
					.println(name()
							+ "::createNewDirectRoute4ExportRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode() + ", container = "
							+ sel_combo.container.getCode());
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
			System.out
					.println(name()
							+ "::createDirectRoute4ExportRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");

			Truck truck = sel_combo.truck;
			// TruckRoute tr = mTruck2Route.get(truck);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container)
					.getPickupContainerDuration();
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
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
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
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		}
		return null;
	}
	*/
	public TruckRouteInfo4Request createNewDirectRoute4ExportRequest(
			ExportContainerRequest r) {
		

		Port port = mCode2Port.get(r.getPortCode());
		
		ComboContainerMoocTruck sel_combo = null;
		SequenceSolution sel_seq = null;
		double minDistance = Integer.MAX_VALUE;

		SequenceSolver sequenceSolver = new SequenceSolver(this);
		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			for (int j = 0; j < input.getMoocs().length; j++) {
				Mooc mooc = input.getMoocs()[j];
				for (int k = 0; k < input.getContainers().length; k++) {
					Container container = input.getContainers()[k];
					HashSet<Container> C = mShipCompanyCode2Containers.get(r
							.getShipCompanyCode());

					if (!C.contains(container))
						continue;
					// if(!r.getShipCompanyCode().equals(shipCompanyCode))
					// continue;

					ComboContainerMoocTruck combo = findLastAvailable(truck,
							mooc, container);
					if (combo == null)
						continue;

					SequenceSolution seq_sol = sequenceSolver.solve(combo, r.getPickupWarehouses(), port);
					
					
					if (seq_sol.evaluation < minDistance) {
						minDistance = seq_sol.evaluation;
						sel_combo = combo;
						sel_seq = seq_sol;
					}
				}
			}
		}
		if (sel_combo == null) {
			logln(name()
					+ "::createNewDirectRoute4ExportRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if (sel_combo.routeElement == null) {
			TruckRoute tr = createDirectRouteForExportRequest(r, sel_seq.seq,
					sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out
					.println(name()
							+ "::createNewDirectRoute4ExportRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode() + ", container = "
							+ sel_combo.container.getCode());
			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
			System.out
					.println(name()
							+ "::createDirectRoute4ExportRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");

			Truck truck = sel_combo.truck;
			// TruckRoute tr = mTruck2Route.get(truck);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container)
					.getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e2, arrivalTime);
			mPoint2DepartureTime.put(e2, departureTime);
			RouteElement lastElement = e2;
			
			//Port port = getPortFromCode(r.getPortCode());
			SequenceSolver SS = new SequenceSolver(this);
			SequenceSolution sol = SS.solve(e2.getLocationCode(), departureTime, r, port.getLocationCode());
			int[] seq = sol.seq;
			
			RouteElement[] re = new RouteElement[seq.length*2];
			int idx = -1;
			for(int i = 0; i < seq.length; i++){
				PickupWarehouseInfo pwi = r.getPickupWarehouses()[seq[i]];
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(lastElement);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
				re[idx].setExportRequest(r);

				travelTime = getTravelTime(lastElement, re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimeLoadAtWarehouse()));
				int finishedServiceTime = startServiceTime + pwi.getLoadDuration();
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
				
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(re[idx-1]);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
				travelTime = getTravelTime(re[idx-1], re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime, finishedServiceTime);
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
			}
			

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			travelTime = getTravelTime(lastElement, e5);
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
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
			Truck truck = sel_combo.truck;
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = sel_combo.startTime;
			RouteElement lastElement = e;
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();
			SequenceSolver SS = new SequenceSolver(this);
			SequenceSolution sol = SS.solve(e.getLocationCode(), departureTime, r, port.getLocationCode());
			int[] seq = sol.seq;
			
			RouteElement[] re = new RouteElement[seq.length*2];
			int idx = -1;
			for(int i = 0; i < seq.length; i++){
				PickupWarehouseInfo pwi = r.getPickupWarehouses()[seq[i]];
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(lastElement);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
				re[idx].setExportRequest(r);

				int travelTime = getTravelTime(lastElement, re[idx]);
				int arrivalTime = departureTime + travelTime;
				int startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimeLoadAtWarehouse()));
				int finishedServiceTime = startServiceTime + pwi.getLoadDuration();
				int duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
				
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(re[idx-1]);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
				travelTime = getTravelTime(re[idx-1], re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime, finishedServiceTime);
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
			}


			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			int travelTime = getTravelTime(lastElement, e5);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = arrivalTime;
			int duration = r.getUnloadDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			// update last depot container, set to not-available
			mContainer2LastDepot.put(container, null);
			mContainer2LastTime.put(container, Integer.MAX_VALUE);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		}
		return null;
	}
	
	
	public TruckRouteInfo4Request createNewDirectRoute4ExportRequest(
			Truck truck, Mooc mooc, Container container,
			ExportContainerRequest r) {
				String portCode = r.getPortCode();
		String portLocationCode = mCode2Port.get(portCode).getLocationCode();
		
		HashSet<Container> C = mShipCompanyCode2Containers.get(r
				.getShipCompanyCode());

		if (!C.contains(container))
			return null;
		// if(!r.getShipCompanyCode().equals(shipCompanyCode))
		// continue;

		ComboContainerMoocTruck sel_combo = findLastAvailable(truck, mooc,
				container);
		if (sel_combo == null)
			return null;

		int startTime = sel_combo.startTime;
		String startLocationCode = sel_combo.lastLocationCode;
		
		SequenceSolver SS = new SequenceSolver(this);
		SequenceSolution ss = SS.solve(startLocationCode, startTime, r, portLocationCode);
		if(ss == null) return null;
		int[] seq = ss.seq;
		Warehouse firstWarehouse = getWarehouseFromCode(r.getPickupWarehouses()[seq[0]].getWareHouseCode());
		
		double distance = sel_combo.extraDistance
				+ getDistance(sel_combo.lastLocationCode, firstWarehouse.getLocationCode());
		// System.out.println(name() +
		// "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = "
		// + arrivalTimeWarehouse +
		// ", latePickupWarehouse = " + latePickupWarehouse +
		// ", distance = " + distance);
		// create route
		if (sel_combo.routeElement == null) {
			TruckRoute tr = createDirectRouteForExportRequest(r,seq,
					sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out
					.println(name()
							+ "::createNewDirectRoute4ExportRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode() + ", container = "
							+ sel_combo.container.getCode());

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = distance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
			System.out
					.println(name()
							+ "::createDirectRoute4ExportRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");

			TruckItinerary I = mTruck2Itinerary.get(truck);
			RouteElement e = sel_combo.routeElement;
			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container)
					.getPickupContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e2, arrivalTime);
			mPoint2DepartureTime.put(e2, departureTime);
			RouteElement lastElement = e2;
			
			RouteElement[] re = new RouteElement[seq.length*2];
			int idx = -1;
			for(int i = 0; i < seq.length; i++){
				PickupWarehouseInfo pwi = r.getPickupWarehouses()[seq[i]];
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(lastElement);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
				re[idx].setExportRequest(r);

				travelTime = getTravelTime(lastElement, re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimeLoadAtWarehouse()));
				int finishedServiceTime = startServiceTime + pwi.getLoadDuration();
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
				
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(re[idx-1]);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
				travelTime = getTravelTime(re[idx-1], re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime, finishedServiceTime);
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
			}


			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			travelTime = getTravelTime(lastElement, e5);
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
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = distance;

			return tri;
		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
			int departureTime = sel_combo.startTime;
			RouteElement lastElement = e;
			
			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement[] re = new RouteElement[seq.length*2];
			int idx = -1;
			for(int i = 0; i < seq.length; i++){
				PickupWarehouseInfo pwi = r.getPickupWarehouses()[seq[i]];
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(lastElement);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
				re[idx].setExportRequest(r);

				int travelTime = getTravelTime(lastElement, re[idx]);
				int arrivalTime = departureTime + travelTime;
				int startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(pwi
								.getEarlyDateTimeLoadAtWarehouse()));
				int finishedServiceTime = startServiceTime + pwi.getLoadDuration();
				int duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
				
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(re[idx-1]);
				re[idx].setWarehouse(mCode2Warehouse.get(pwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
				travelTime = getTravelTime(re[idx-1], re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime, finishedServiceTime);
				duration = 0;
				departureTime = startServiceTime + duration;
				mPoint2ArrivalTime.put(re[idx], arrivalTime);
				mPoint2DepartureTime.put(re[idx], departureTime);
				lastElement = re[idx];
			}

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setPort(mCode2Port.get(r.getPortCode()));
			e5.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e5.setContainer(null);
			e5.setExportRequest(null);
			int travelTime = getTravelTime(lastElement, e5);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = arrivalTime;
			int duration = r.getUnloadDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			// update last depot container, set to not-available
			mContainer2LastDepot.put(container, null);
			mContainer2LastTime.put(container, Integer.MAX_VALUE);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = distance;

			return tri;
		}
		return null;
	}

	public TruckRouteInfo4Request createNewDirectRoute4WarehouseRequest(
			WarehouseContainerTransportRequest r) {
		String fromWarehouseCode = r.getFromWarehouseCode();
		String toWarehouseCode = r.getToWarehouseCode();
		logln(name()
				+ "::createDirectRoute4WarehouseRequest, fromWarehouseCode = "
				+ fromWarehouseCode + ", toWarehouseCode = " + toWarehouseCode);

		Warehouse fromWarehouse = mCode2Warehouse.get(fromWarehouseCode);
		Warehouse toWarehouse = mCode2Warehouse.get(toWarehouseCode);

		String fromWarehouseLocationCode = fromWarehouse.getLocationCode();
		String toWarehouseLocationCode = toWarehouse.getLocationCode();

		int earlyPickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeLoad());
		int latePickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimeLoad());

		ComboContainerMoocTruck sel_combo = null;
		double minDistance = Integer.MAX_VALUE;

		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			for (int j = 0; j < input.getMoocs().length; j++) {
				Mooc mooc = input.getMoocs()[j];
				for (int k = 0; k < input.getContainers().length; k++) {
					Container container = input.getContainers()[k];
					// String depotContainerCode =
					// container.getDepotContainerCode();
					// String shipCompanyCode =
					// mDepotContainerCode2ShipCompanyCode.get(depotContainerCode);
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, depotContainer = " +
					// depotContainerCode + ", shipCompanyCode = " +
					// shipCompanyCode +
					// ", r.getShipCompanyCode = " + r.getShipCompanyCode());
					// HashSet<Container> C =
					// mShipCompanyCode2Containers.get(r.getShipCompanyCode());

					// if(!C.contains(container)) continue;
					// if(!r.getShipCompanyCode().equals(shipCompanyCode))
					// continue;

					ComboContainerMoocTruck combo = findLastAvailable(truck,
							mooc, container);
					if (combo == null)
						continue;

					int startTime = combo.startTime;
					String startLocationCode = combo.lastLocationCode;
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, lastLocationCode = "
					// + combo.lastLocationCode);

					int arrivalTimeWarehouse = startTime
							+ getTravelTime(startLocationCode,
									fromWarehouseLocationCode);

					// if(combo.truck.getCode().equals("Truck0001") &&
					// combo.mooc.getCode().equals("Mooc0002") &&
					// combo.container.getCode().equals("Container002"))
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = "
					// +
					// DateTimeUtils.unixTimeStamp2DateTime(arrivalTimeWarehouse)
					// +
					// ", latePickupWarehouse = " +
					// DateTimeUtils.unixTimeStamp2DateTime(latePickupWarehouse));

					if (arrivalTimeWarehouse > latePickupWarehouse)
						continue;

					double distance = combo.extraDistance
							+ getDistance(combo.lastLocationCode,
									fromWarehouseLocationCode);
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = "
					// + arrivalTimeWarehouse +
					// ", latePickupWarehouse = " + latePickupWarehouse +
					// ", distance = " + distance);
					if (distance < minDistance) {
						minDistance = distance;
						sel_combo = combo;
					}
				}
			}
		}
		if (sel_combo == null) {
			logln(name()
					+ "::createDirectRoute4WarehouseRequest, sel_combo is NULL????");
			return null;
		}
		// create route
		if (sel_combo.routeElement == null) {

			TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r,
					sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out
					.println(name()
							+ "::createDirectRoute4WarehouseRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode() + ", container = "
							+ sel_combo.container.getCode());
			// if(mTruck2Route.get(sel_combo.truck) == null)
			// return tr;
			// TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			// old_tr.concat(tr);

			// return old_tr;

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
			System.out
					.println(name()
							+ "::createDirectRoute4WarehouseRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");

			Truck truck = sel_combo.truck;
			// TruckRoute tr = mTruck2Route.get(truck);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			RouteElement e = sel_combo.routeElement;
			Container container = sel_combo.container;
			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container)
					.getPickupContainerDuration();
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
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
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
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;

		}
		return null;
	}

	public TruckRouteInfo4Request createNewDirectRoute4WarehouseRequest(
			Truck truck, Mooc mooc, Container container,
			WarehouseContainerTransportRequest r) {
		String fromWarehouseCode = r.getFromWarehouseCode();
		String toWarehouseCode = r.getToWarehouseCode();
		logln(name()
				+ "::createDirectRoute4WarehouseRequest, fromWarehouseCode = "
				+ fromWarehouseCode + ", toWarehouseCode = " + toWarehouseCode);

		Warehouse fromWarehouse = mCode2Warehouse.get(fromWarehouseCode);
		Warehouse toWarehouse = mCode2Warehouse.get(toWarehouseCode);

		String fromWarehouseLocationCode = fromWarehouse.getLocationCode();
		String toWarehouseLocationCode = toWarehouse.getLocationCode();

		int earlyPickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getEarlyDateTimeLoad());
		int latePickupWarehouse = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimeLoad());

		ComboContainerMoocTruck sel_combo = findLastAvailable(truck, mooc,
				container);
		if (sel_combo == null) {
			logln(name()
					+ "::createDirectRoute4WarehouseRequest, sel_combo is NULL????");
			return null;
		}

		int startTime = sel_combo.startTime;
		String startLocationCode = sel_combo.lastLocationCode;

		int arrivalTimeWarehouse = startTime
				+ getTravelTime(startLocationCode, fromWarehouseLocationCode);

		if (arrivalTimeWarehouse > latePickupWarehouse)
			return null;

		double distance = sel_combo.extraDistance
				+ getDistance(sel_combo.lastLocationCode,
						fromWarehouseLocationCode);
		// create route
		if (sel_combo.routeElement == null) {

			TruckRoute tr = createDirectRouteForWarehouseWarehouseRequest(r,
					sel_combo.truck, sel_combo.mooc, sel_combo.container);
			System.out
					.println(name()
							+ "::createDirectRoute4WarehouseRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode() + ", container = "
							+ sel_combo.container.getCode());
			// if(mTruck2Route.get(sel_combo.truck) == null)
			// return tr;
			// TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			// old_tr.concat(tr);

			// return old_tr;

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = distance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)) {
			System.out
					.println(name()
							+ "::createDirectRoute4WarehouseRequest, create route from WAIT_RELEASE_LOADED_CONTAINER_AT_PORT");

			TruckItinerary I = mTruck2Itinerary.get(truck);
			RouteElement e = sel_combo.routeElement;
			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

			ArrayList<RouteElement> L = new ArrayList<RouteElement>();

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e);
			e2.setDepotContainer(mContainer2LastDepot.get(container));
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setContainer(container);
			int travelTime = getTravelTime(e, e2);
			int arrivalTime = departureTime + travelTime;
			int startServiceTime = MAX(arrivalTime,
					mContainer2LastTime.get(container));
			int duration = mContainer2LastDepot.get(container)
					.getPickupContainerDuration();
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
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = distance;

			return tri;
		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE)) {
			TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;
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
			e8.setDepotMooc(mCode2DepotMooc.get(sel_combo.mooc
					.getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = tr.indexOf(e);
			tri.additionalDistance = distance;
			return tri;
		}
		return null;
	}

	public TruckRouteInfo4Request createNewDirectRoute4ImportRequest(
			ImportContainerRequest r) {
		String portCode = r.getPortCode();
		Port port = mCode2Port.get(portCode);
		String locationCodePort = mCode2Port.get(portCode).getLocationCode();
		int latePickupPort = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimePickupAtPort());

		ComboContainerMoocTruck sel_combo = null;
		double minDistance = Integer.MAX_VALUE;

		for (int i = 0; i < input.getTrucks().length; i++) {
			Truck truck = input.getTrucks()[i];
			for (int j = 0; j < input.getMoocs().length; j++) {
				Mooc mooc = input.getMoocs()[j];
				ComboContainerMoocTruck combo = findLastAvailable(truck, mooc);
				int startTime = combo.startTime;
				String startLocationCode = combo.lastLocationCode;
				int arrivalTimePort = startTime
						+ getTravelTime(startLocationCode, locationCodePort);
				if (arrivalTimePort > latePickupPort)
					continue;
				double distance = combo.extraDistance
						+ getDistance(startLocationCode, locationCodePort);
				if (distance < minDistance) {
					minDistance = distance;
					sel_combo = combo;
				}
			}
		}
		if (sel_combo == null) {
			System.out
					.println(name()
							+ "::createDirectRoute4ImportRequest, cannot find any combo");
			return null;
		}
		System.out.println(name()
				+ "::createDirectRoute4ImportRequest, FOUND combo, truck = "
				+ sel_combo.truck.getCode() + ", mooc = "
				+ sel_combo.mooc.getCode() + ", lastTime = "
				+ DateTimeUtils.unixTimeStamp2DateTime(sel_combo.startTime));

		if (sel_combo.routeElement == null) {
			TruckRoute tr = createDirectRouteForImportRequest(r,
					sel_combo.truck, sel_combo.mooc);
			System.out
					.println(name()
							+ "::createDirectRoute4ImportRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode());
			// if(mTruck2Route.get(sel_combo.truck) == null)
			// return tr;
			// TruckRoute old_tr = mTruck2Route.get(sel_combo.truck);
			// old_tr.concat(tr);
			// System.out.println(name() +
			// "::createDirectRoute4ImportRequest, concat truck-route, length = "
			// + old_tr.getNodes().length);
			// return old_tr;

			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = minDistance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)
				|| sel_combo.routeElement.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {
			Truck truck = sel_combo.truck;
			// TruckRoute tr = mTruck2Route.get(truck);
			RouteElement e = sel_combo.routeElement;

			System.out.println(name()
					+ "::createDirectRoute4ImportRequest, routeElement e = "
					+ e.toString());

			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

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
			RouteElement lastElement = e2;
			
			SequenceSolver SS = new SequenceSolver(this);
			SequenceSolution ss = SS.solve(lastElement.getLocationCode(), departureTime, r, null);
			int[]seq = ss.seq;
			RouteElement[] re = new RouteElement[2*seq.length];
			int idx = -1;
			for(int i = 0; i < re.length; i++){
				DeliveryWarehouseInfo dwi = r.getDeliveryWarehouses()[seq[i]];
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
				int finishedServiceTime = startServiceTime + dwi.getUnloadDuration();
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
			

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setDepotContainer(mCode2DepotContainer.get(r
					.getDepotContainerCode()));
			e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e5.setContainer(null);
			travelTime = getTravelTime(lastElement, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e5.getDepotContainer().getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
			mContainer2LastTime.put(e5.getContainer(), departureTime);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc
					.get(e6.getMooc().getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = minDistance;

			return tri;
		}
		return null;
	}

	public TruckRouteInfo4Request createNewDirectRoute4ImportRequest(
			Truck truck, Mooc mooc, ImportContainerRequest r) {
		String portCode = r.getPortCode();
		Port port = mCode2Port.get(portCode);
		String locationCodePort = mCode2Port.get(portCode).getLocationCode();
		int latePickupPort = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimePickupAtPort());

		ComboContainerMoocTruck sel_combo = findLastAvailable(truck, mooc);
		if (sel_combo == null)
			return null;

		int startTime = sel_combo.startTime;
		String startLocationCode = sel_combo.lastLocationCode;
		int arrivalTimePort = startTime
				+ getTravelTime(startLocationCode, locationCodePort);
		if (arrivalTimePort > latePickupPort)
			return null;

		double distance = sel_combo.extraDistance
				+ getDistance(startLocationCode, locationCodePort);

		if (sel_combo.routeElement == null) {
			TruckRoute tr = createDirectRouteForImportRequest(r,
					sel_combo.truck, sel_combo.mooc);
			System.out
					.println(name()
							+ "::createDirectRoute4ImportRequest, create route from all depots, truck = "
							+ sel_combo.truck.getCode() + ", mooc = "
							+ sel_combo.mooc.getCode());
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = tr;
			tri.lastUsedIndex = -1;
			tri.additionalDistance = distance;
			return tri;

		} else if (sel_combo.routeElement.getAction().equals(
				ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT)
				|| sel_combo.routeElement.getAction().equals(
						ActionEnum.RELEASE_CONTAINER_AT_DEPOT)) {

			RouteElement e = sel_combo.routeElement;

			System.out.println(name()
					+ "::createDirectRoute4ImportRequest, routeElement e = "
					+ e.toString());

			int departureTime = (int) DateTimeUtils.dateTime2Int(e
					.getDepartureTime());

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
			RouteElement lastElement = e2;
			
			SequenceSolver SS = new SequenceSolver(this);
			SequenceSolution ss = SS.solve(lastElement.getLocationCode(), departureTime, r, null);
			int[]seq = ss.seq;
			RouteElement[] re = new RouteElement[2*seq.length];
			int idx = -1;
			for(int i = 0; i < re.length; i++){
				DeliveryWarehouseInfo dwi = r.getDeliveryWarehouses()[seq[i]];
				idx++;
				re[idx] = new RouteElement();
				L.add(re[idx]);
				re[idx].deriveFrom(e2);
				re[idx].setWarehouse(mCode2Warehouse.get(dwi.getWareHouseCode()));
				re[idx].setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
				travelTime = getTravelTime(lastElement, re[idx]);
				arrivalTime = departureTime + travelTime;
				startServiceTime = MAX(arrivalTime,
						(int) DateTimeUtils.dateTime2Int(dwi
								.getEarlyDateTimeUnloadAtWarehouse()));
				int finishedServiceTime = startServiceTime + dwi.getUnloadDuration();
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


			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(lastElement);
			e5.setDepotContainer(mCode2DepotContainer.get(r
					.getDepotContainerCode()));
			e5.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e5.setContainer(null);
			travelTime = getTravelTime(lastElement, e5);
			arrivalTime = departureTime + travelTime;
			startServiceTime = arrivalTime;
			duration = e5.getDepotContainer().getDeliveryContainerDuration();
			departureTime = startServiceTime + duration;
			mPoint2ArrivalTime.put(e5, arrivalTime);
			mPoint2DepartureTime.put(e5, departureTime);
			mContainer2LastDepot.put(e5.getContainer(), e5.getDepotContainer());
			mContainer2LastTime.put(e5.getContainer(), departureTime);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setDepotMooc(mCode2DepotMooc
					.get(e6.getMooc().getDepotMoocCode()));
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

			// tr.removeNodesAfter(e);
			// tr.addNodes(L);
			// return tr;

			TruckRoute newTr = new TruckRoute(truck, L);
			TruckItinerary I = mTruck2Itinerary.get(truck);
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = newTr;
			tri.lastUsedIndex = I.indexOf(e);
			tri.additionalDistance = distance;

			return tri;
		}
		return null;
	}

}
