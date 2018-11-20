package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.CandidateRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.CandidateSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocInput;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerTruckMoocSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.DeliveryWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotMooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotTruck;
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
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualEmptyContainerFromDepotRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualEmptyContainerToDepotRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportEmptyRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportLadenRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportEmptyRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportLadenRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualTransportContainerRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.KepLechRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.PickupWarehouseInfo;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.SequenceSolution;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapImportExportRouteComposer;
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
	
	public void exploreSwapImportExport(
			CandidateRouteComposer candidateRouteComposer) {
		RouteSwapImportExportCreator routeSwapImportExportCreator = new RouteSwapImportExportCreator(
				this);
		double minDistance = Integer.MAX_VALUE;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		ImportContainerRequest sel_imReq_k = null;
		ExportContainerRequest sel_exReq_q = null;
		TruckRouteInfo4Request sel_tri = null;

		for (int i = 0; i < trucks.length; i++) {
			for (int j = 0; j < moocs.length; j++) {
				for (int k = 0; k < imReq.length; k++) {
					for (int q = 0; q < exReq.length; q++) {
						if (imReqScheduled[k] || exReqScheduled[q])
							continue;

						backup();
						TruckRouteInfo4Request tri = routeSwapImportExportCreator
								.createSwapImportExport(trucks[i], moocs[j],
										imReq[k], exReq[q]);
						if (tri == null) {
							// System.out.println(name() +
							// "::solve, cannot find routeSwapImportExport for ("
							// + i + "," + j + "," + k + "," + q + ")");
							logln(name()
									+ "::solve, cannot find routeSwapImportExport for ("
									+ i + "," + j + "," + k + "," + q + ")");
						} else {
							TruckRoute tr = tri.route;
							// System.out.println(name() +
							// "::solve, FOUND routeSwapImportExport for (" + i
							// + "," + j + "," + k + "," + q + "), route = " +
							// tr.toString() + ", distance = " +
							// tr.getDistance());
							logln(name()
									+ "::solve, FOUND routeSwapImportExport for ("
									+ i + "," + j + "," + k + "," + q
									+ "), route = " + tr.toString()
									+ ", distance = " + tr.getDistance());
							if (minDistance > tr.getDistance()) {
								minDistance = tr.getDistance();
								sel_truck = trucks[i];
								sel_mooc = moocs[j];
								sel_imReq_k = imReq[k];
								sel_exReq_q = exReq[q];
								sel_tri = tri;
							}
						}
						restore();
					}
				}
			}
		}
		if (sel_truck != null) {
			SwapImportExportRouteComposer cp = new SwapImportExportRouteComposer(
					this, sel_truck, sel_mooc, sel_exReq_q, sel_imReq_k,
					sel_tri, minDistance);
			System.out
					.println(name()
							+ "::exploreSwapImportExport, minDistance = "
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
							for (int k = 0; k < containers.length; k++) {
								if (containers[k].getCode().startsWith("A"))
									continue;// imported container
								backup();
								TruckRouteInfo4Request tri_k = createRouteForImportRequest(
										sel_imReq_k, trucks[i], moocs[j]);
								if (tri_k == null) {
									restore();
									continue;
								}
								TruckRouteInfo4Request tri_q = createRouteForExportRequest(
										sel_exReq_q, trucks[i1], moocs[j1],
										containers[k]);
								if (tri_q == null) {
									restore();
									continue;
								}
								TruckRoute tr_k = tri_k.route;
								TruckRoute tr_q = tri_q.route;
								// compute additional distance when creating
								// these routes
								double dis = tr_k.getDistance()
										- tr_k.getReducedDistance()
										+ tr_q.getDistance()
										- tr_q.getReducedDistance();
								if (dis < minDistance1) {
									minDistance1 = dis;
									System.out
											.println(name()
													+ "::exploreSwapImportExport, update minDistance1 = "
													+ minDistance1);
									sel_tr_k = tr_k;
									sel_tr_q = tr_q;
									sel_tri_k = tri_k;
									sel_tri_q = tri_q;
								}
								restore();
							}
						}
					}
				}
			}
			if (minDistance1 < minDistance) {
				IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(
						this, sel_tr_k, sel_tr_q, sel_imReq_k, sel_exReq_q,
						sel_tri_k, sel_tri_q, minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(cp);
			}

		}

	}

	public void exploreKepLech(CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		Container sel_container = null;
		ExportContainerRequest sel_exReq_a = null;
		ImportContainerRequest sel_imReq_b = null;
		TruckRouteInfo4Request sel_tri = null;
		RouteKeplechCreator routeKeplechCreator = new RouteKeplechCreator(this);

		for (int i = 0; i < trucks.length; i++) {
			for (int j = 0; j < moocs.length; j++) {
				for (int k = 0; k < containers.length; k++) {
					for (int a = 0; a < exReq.length; a++) {
						for (int b = 0; b < imReq.length; b++) {
							if (exReqScheduled[a] || imReqScheduled[b])
								continue;
							backup();
							TruckRouteInfo4Request tri = routeKeplechCreator
									.createKeplech(trucks[i], moocs[j],
											containers[k], exReq[a], imReq[b]);
							if (tri == null) {
								// System.out.println(name() +
								// "::solve, cannot find routeKepLech for (" + i
								// + "," + j + "," + k + "," + a + "," + b +
								// ")");
								logln(name()
										+ "::solve, cannot find routeKepLech for ("
										+ i + "," + j + "," + k + "," + a + ","
										+ b + ")");
							} else {
								TruckRoute tr = tri.route;
								// System.out.println(name() +
								// "::solve, FOUND routeKepLech for (" + i + ","
								// + j + "," + k + "," + a + "," + b +
								// "), route = " + tr.toString() +
								// ", distance = " + tr.getDistance());
								logln(name()
										+ "::solve, FOUND routeKepLech for ("
										+ i + "," + j + "," + k + "," + a + ","
										+ b + "), route = " + tr.toString()
										+ ", distance = " + tr.getDistance());
								if (minDistance > tr.getDistance()) {
									minDistance = tr.getDistance();
									sel_truck = trucks[i];
									sel_mooc = moocs[j];
									sel_container = containers[k];
									sel_exReq_a = exReq[a];
									sel_imReq_b = imReq[b];
									sel_tri = tri;
								}
							}
							restore();
						}
					}
				}
			}
		}
		if (sel_truck != null) {
			KepLechRouteComposer kcp = new KepLechRouteComposer(this,
					sel_truck, sel_mooc, sel_container, sel_imReq_b,
					sel_exReq_a, sel_tri, minDistance);

			TruckRoute sel_tr_k = null;
			TruckRoute sel_tr_q = null;
			TruckRouteInfo4Request sel_tri_k = null;
			TruckRouteInfo4Request sel_tri_q = null;
			double minDistance1 = Integer.MAX_VALUE;
			for (int i = 0; i < trucks.length; i++) {
				for (int j = 0; j < moocs.length; j++) {
					for (int i1 = 0; i1 < trucks.length; i1++) {
						for (int j1 = 0; j1 < moocs.length; j1++) {
							for (int k = 0; k < containers.length; k++) {
								if (containers[k].getCode().startsWith("A"))
									continue;// imported container
								backup();
								TruckRouteInfo4Request tri_k = createRouteForImportRequest(
										sel_imReq_b, trucks[i], moocs[j]);
								if (tri_k == null) {
									restore();
									continue;
								}
								TruckRouteInfo4Request tri_q = createRouteForExportRequest(
										sel_exReq_a, trucks[i1], moocs[j1],
										containers[k]);
								if (tri_q == null) {
									restore();
									continue;
								}
								// compute additional distance when creating
								// these routes
								TruckRoute tr_k = tri_k.route;
								TruckRoute tr_q = tri_q.route;
								double dis = tr_k.getDistance()
										- tr_k.getReducedDistance()
										+ tr_q.getDistance()
										- tr_q.getReducedDistance();
								if (dis < minDistance1) {
									minDistance1 = dis;
									sel_tr_k = tr_k;
									sel_tr_q = tr_q;
									sel_tri_k = tri_k;
									sel_tri_q = tri_q;
								}
								restore();
							}
						}
					}
				}
			}
			if (minDistance1 < minDistance) {
				IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(
						this, sel_tr_k, sel_tr_q, sel_imReq_b, sel_exReq_a,
						sel_tri_k, sel_tri_q, minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(kcp);
			}

		}

	}

	public void exploreTangBo(CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		Container sel_container = null;
		WarehouseContainerTransportRequest sel_whReq_a = null;
		ExportContainerRequest sel_exReq_b = null;
		TruckRoute sel_route = null;
		TruckRouteInfo4Request sel_tri = null;
		RouteTangboWarehouseExport routeTangboWarehouseExport = new RouteTangboWarehouseExport(
				this);

		for (int i = 0; i < trucks.length; i++) {
			for (int j = 0; j < moocs.length; j++) {
				for (int k = 0; k < containers.length; k++) {
					for (int a = 0; a < whReq.length; a++) {
						for (int b = 0; b < exReq.length; b++) {
							if (whReqScheduled[a] || exReqScheduled[b])
								continue;
							backup();
							TruckRouteInfo4Request tri = routeTangboWarehouseExport
									.createTangboWarehouseExport(trucks[i],
											moocs[j], containers[k], whReq[a],
											exReq[b]);
							if (tri == null) {
								// System.out.println(name() +
								// "::solve, cannot find routeTangboWarehouseExport for ("
								// + i + "," + j + "," + k + "," + a + "," + b +
								// ")");
								logln(name()
										+ "::solve, cannot find routeTangboWarehouseExport for ("
										+ i + "," + j + "," + k + "," + a + ","
										+ b + ")");
							} else {
								TruckRoute tr = tri.route;
								// System.out.println(name() +
								// "::solve, FOUND routeTangboWarehouseExport for ("
								// + i + "," + j + "," + k + "," + a + "," + b +
								// "), route = " + tr.toString() +
								// ", distance = " + tr.getDistance());
								logln(name()
										+ "::solve, FOUND routeTangboWarehouseExport for ("
										+ i + "," + j + "," + k + "," + a + ","
										+ b + "), route = " + tr.toString()
										+ ", distance = " + tr.getDistance());
								if (minDistance > tr.getDistance()) {
									sel_truck = trucks[i];
									sel_mooc = moocs[j];
									sel_container = containers[k];
									sel_whReq_a = whReq[a];
									sel_exReq_b = exReq[b];
									sel_route = tr;
									sel_tri = tri;
								}
							}
							restore();
						}
					}
				}
			}
		}
		if (sel_truck != null) {
			TangboWarehouseExportRouteComposer tcp = new TangboWarehouseExportRouteComposer(
					this, sel_truck, sel_mooc, sel_container, sel_route,
					sel_whReq_a, sel_exReq_b, sel_tri, minDistance);

			TruckRoute sel_tr_k = null;
			TruckRoute sel_tr_q = null;
			TruckRouteInfo4Request sel_tri_k = null;
			TruckRouteInfo4Request sel_tri_q = null;
			double minDistance1 = Integer.MAX_VALUE;
			for (int i1 = 0; i1 < trucks.length; i1++) {
				for (int j1 = 0; j1 < moocs.length; j1++) {
					for (int k1 = 0; k1 < containers.length; k1++) {
						for (int i2 = 0; i2 < trucks.length; i2++) {
							for (int j2 = 0; j2 < moocs.length; j2++) {
								for (int k2 = 0; k2 < containers.length; k2++) {
									if (containers[k2].getCode()
											.startsWith("A")
											|| containers[k1].getCode()
													.startsWith("A"))
										continue;// imported container
									backup();
									TruckRouteInfo4Request tri_k = createRouteForWarehouseWarehouseRequest(
											sel_whReq_a, trucks[i1], moocs[j1],
											containers[k1]);
									if (tri_k == null) {
										restore();
										continue;
									}

									TruckRouteInfo4Request tri_q = createRouteForExportRequest(
											sel_exReq_b, trucks[i2], moocs[j2],
											containers[k2]);

									if (tri_q == null) {
										restore();
										continue;
									}

									TruckRoute tr_k = tri_k.route;
									TruckRoute tr_q = tri_q.route;

									double dis = tr_k.getDistance()
											- tr_k.getReducedDistance()
											+ tr_q.getDistance()
											- tr_q.getReducedDistance();
									if (dis < minDistance1) {
										minDistance1 = dis;
										sel_tr_k = tr_k;
										sel_tr_q = tr_q;
										sel_tri_k = tri_k;
										sel_tri_q = tri_q;
									}

									restore();
								}
							}
						}
					}
				}
			}
			if (minDistance1 < minDistance) {
				IndividualWarehouseExportRoutesComposer icp = new IndividualWarehouseExportRoutesComposer(
						this, sel_tr_k, sel_tr_q, sel_whReq_a, sel_exReq_b,
						sel_tri_k, sel_tri_q, minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(tcp);
			}
		}

	}
	public void exploreDirectRouteImportLadenRequest(CandidateRouteComposer candidateRouteComposer){
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportLadenRequests sel_imLadenReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < imLadenReq.length; i++){
			if(imLadenReqScheduled[i])
				continue;
			for (String keyM : mDepot2MoocList.keySet()) {
				ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(imLadenReq[i].getWeight(), 
						imLadenReq[i].getContainerCategory(), keyM);
				for(int k = 0; k < avaiMoocList.size(); k++){
					for(String keyT : mDepot2TruckList.keySet()) {
						ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
								avaiMoocList.get(k).getWeight(), keyT);
						for(int j = 0; j < avaiTruckList.size(); j++){
							double d = evaluateImportLadenRequest(imLadenReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
							if(d < minDistance){
								minDistance = d;
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
						this, sel_tr, sel_imLadenReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}
	
	public void exploreDirectRouteImportEmptyRequest(CandidateRouteComposer candidateRouteComposer){
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportEmptyRequests sel_imEmptyReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < imEmptyReq.length; i++){
			if(imEmptyReqScheduled[i]) continue;
			
			for(int j = 0; j < trucks.length; j++){
				if(imEmptyReq[i].isBreakRomooc()){
					double d = evaluateImportEmptyRequest(imEmptyReq[i], trucks[j]);
					if(d < minDistance){
						minDistance = d;
						sel_imEmptyReq = imEmptyReq[i];
						sel_truck = trucks[j];
					}
				}
				else{
					for(int k = 0; k < moocs.length; k++){
						if (mMooc2LastDepot.get(moocs[k]) == null)
							continue;
						if(!fitContMoocType(imEmptyReq[i].getContainerCategory(), imEmptyReq[i].getContainerCategory(),
								moocs[k].getCategory()))
							continue;
						double d = evaluateImportEmptyRequest(imEmptyReq[i], trucks[j], moocs[k]);
						if(d < minDistance){
							minDistance = d;
							sel_imEmptyReq = imEmptyReq[i];
							sel_truck = trucks[j];
							sel_mooc = moocs[k];
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			if(sel_imEmptyReq.isBreakRomooc())
				sel_tri = createRouteForImportEmptyRequest(sel_imEmptyReq, sel_truck);
			else
				sel_tri = createRouteForImportEmptyRequest(sel_imEmptyReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualImportEmptyRouteComposer icp = new IndividualImportEmptyRouteComposer(
						this, sel_tr, sel_imEmptyReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}
	
	public void exploreDirectRouteExportEmptyRequest(CandidateRouteComposer candidateRouteComposer){
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportEmptyRequests sel_exEmptyReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < exEmptyReq.length; i++){
			if(exEmptyReqScheduled[i]) continue;
			
			for (String keyC : mDepot2ContainerList.keySet()) {
				ArrayList<Container> avaiContList = getAvailableContainerAtDepot(0, 
						exEmptyReq[i].getContainerCategory(), keyC);
				for(int q = 0; q < avaiContList.size(); q++){
					for (String keyM : mDepot2MoocList.keySet()) {
						ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(avaiContList.get(q).getWeight(), 
								avaiContList.get(q).getCategoryCode(), keyM);
						for(int k = 0; k < avaiMoocList.size(); k++){
							for(String keyT : mDepot2TruckList.keySet()) {
								ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
										avaiMoocList.get(k).getWeight(), keyT);
								for(int j = 0; j < avaiTruckList.size(); j++){
									double d = evaluateExportEmptyRequest(exEmptyReq[i], 
											avaiTruckList.get(j), avaiMoocList.get(k));
									if(d < minDistance){
										minDistance = d;
										sel_exEmptyReq = exEmptyReq[i];
										sel_truck = avaiTruckList.get(j);
										sel_mooc = avaiMoocList.get(k);
									}
								}
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
						this, sel_tr, sel_exEmptyReq, sel_tri, sel_tr.getDistance()
								- sel_tr.getReducedDistance());
				candidateRouteComposer.add(icp);
			}
		}
	}

	public void exploreDirectRouteExportLadenRequest(CandidateRouteComposer candidateRouteComposer){
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportLadenRequests sel_exLadenReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for(int i = 0; i < exLadenReq.length; i++){
			if(exLadenReqScheduled[i]) continue;
			
			for(int j = 0; j < trucks.length; j++){
				if(exLadenReq[i].isBreakRomooc()){
					double d = evaluateExportLadenRequest(exLadenReq[i], trucks[j]);
					if(d < minDistance){
						minDistance = d;
						sel_exLadenReq = exLadenReq[i];
						sel_truck = trucks[j];
					}
				}
				else{
					for(int k = 0; k < moocs.length; k++){
						if (mMooc2LastDepot.get(moocs[k]) == null)
							continue;
						if(!fitContMoocType(exLadenReq[i].getContainerCategory(), exLadenReq[i].getContainerCategory(),
								moocs[k].getCategory()))
							continue;
						double d = evaluateExportLadenRequest(exLadenReq[i], trucks[j], moocs[k]);
						if(d < minDistance){
							minDistance = d;
							sel_exLadenReq = exLadenReq[i];
							sel_truck = trucks[j];
							sel_mooc = moocs[k];
						}
					}
				}
			}
		}
		
		if (sel_truck != null) {
			if(sel_exLadenReq.isBreakRomooc())
				sel_tri = createRouteForExportLadenRequest(sel_exLadenReq, sel_truck);
			else
				sel_tri = createRouteForExportLadenRequest(sel_exLadenReq, sel_truck, sel_mooc);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualExportLadenRouteComposer icp = new IndividualExportLadenRouteComposer(
						this, sel_tr, sel_exLadenReq, sel_tri, sel_tr.getDistance()
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
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportContainerRequest sel_exReq = null;
		Truck sel_truck = null;
		Mooc sel_mooc = null;
		Container sel_container = null;
		
		for (int i = 0; i < nbExReqs; i++) {
			if (exReqScheduled[i])
				continue;

			for (String keyC : mDepot2ContainerList.keySet()) {
				ArrayList<Container> avaiContList = getAvailableContainerAtDepot(exReq[i].getWeight(), 
						exReq[i].getContainerCategory(), keyC);
				for(int q = 0; q < avaiContList.size(); q++){
					for (String keyM : mDepot2MoocList.keySet()) {
						ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(avaiContList.get(q).getWeight(), 
								avaiContList.get(q).getCategoryCode(), keyM);
						for(int k = 0; k < avaiMoocList.size(); k++){
							for(String keyT : mDepot2TruckList.keySet()) {
								ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
										avaiMoocList.get(k).getWeight(), keyT);
								for(int j = 0; j < avaiTruckList.size(); j++){
									double d = evaluateExportRoute(exReq[i], avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
									if(d < minDistance){
										minDistance = d;
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
					this, sel_tr, sel_exReq, sel_tri, sel_tr.getDistance()
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
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportContainerRequest sel_imReq = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		for (int i = 0; i < nbImReqs; i++) {
			if (imReqScheduled[i])
				continue;

			for (String keyM : mDepot2MoocList.keySet()) {
				ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(imReq[i].getWeight(), 
						imReq[i].getContainerCategory(), keyM);
				for(int k = 0; k < avaiMoocList.size(); k++){
					for(String keyT : mDepot2TruckList.keySet()) {
						ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
								avaiMoocList.get(k).getWeight(), keyT);
						for(int j = 0; j < avaiTruckList.size(); j++){
							double d = evaluateImportRequest(
									imReq[i], avaiTruckList.get(j), avaiMoocList.get(k));
							if(d < minDistance){
								minDistance = d;
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
						this, sel_tr, sel_tri, sel_imReq, sel_tr.getDistance()
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
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		Truck sel_truck  = null;
		Mooc sel_mooc = null;
		Container sel_cont = null;
		WarehouseContainerTransportRequest sel_whReq = null;

		for (int i = 0; i < nbWhReqs; i++) {
			if (whReqScheduled[i])
				continue;
			for (String keyC : mDepot2ContainerList.keySet()) {
				ArrayList<Container> avaiContList = getAvailableContainerAtDepot(exReq[i].getWeight(), 
						exReq[i].getContainerCategory(), keyC);
				for(int q = 0; q < avaiContList.size(); q++){
					for (String keyM : mDepot2MoocList.keySet()) {
						ArrayList<Mooc> avaiMoocList = getAvailableMoocAtDepot(avaiContList.get(q).getWeight(), 
								avaiContList.get(q).getCategoryCode(), keyM);
						for(int k = 0; k < avaiMoocList.size(); k++){
							for(String keyT : mDepot2TruckList.keySet()) {
								ArrayList<Truck> avaiTruckList = getAvailableTruckAtDepot(
										avaiMoocList.get(k).getWeight(), keyT);
								for(int j = 0; j < avaiTruckList.size(); j++){
									double d = evaluateWarehouseWarehouseRequest(whReq[i],
											avaiTruckList.get(j), avaiMoocList.get(k), avaiContList.get(q));
									if(d < minDistance){
										minDistance = d;
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
		}
		if(sel_truck != null){
			sel_tri = createRouteForWarehouseWarehouseRequest(
					sel_whReq, sel_truck, sel_mooc, sel_cont);
			if(sel_tri != null){
				sel_tr = sel_tri.route;
				IndividualWarehouseRouteComposer icp = new IndividualWarehouseRouteComposer(
						this, sel_tr, sel_tri, sel_whReq, sel_tr.getDistance()
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
	}

	public void markServed(ImportContainerRequest imR) {
		if (mImReq2Index.get(imR) == null)
			return;
		int idx = mImReq2Index.get(imR);
		imReqScheduled[idx] = true;
	}

	public void markServed(WarehouseContainerTransportRequest whR) {
		if (mWhReq2Index.get(whR) == null)
			return;
		int idx = mWhReq2Index.get(whR);
		whReqScheduled[idx] = true;
	}
	public void markServed(ImportLadenRequests req){
		if(mImportLadenRequest2Index.get(req) == null)
			return;
		int idx = mImportLadenRequest2Index.get(req);
		imLadenReqScheduled[idx] = true;
	}
	public void markServed(ImportEmptyRequests req){
		if(mImportEmptyRequest2Index.get(req) == null)
			return;
		int idx = mImportEmptyRequest2Index.get(req);
		imEmptyReqScheduled[idx] = true;
	}
	public void markServed(ExportLadenRequests req){
		if(mExportLadenRequest2Index.get(req) == null)
			return;
		int idx = mExportLadenRequest2Index.get(req);
		exLadenReqScheduled[idx] = true;
	}
	public void markServed(ExportEmptyRequests req){
		if(mExportEmptyRequest2Index.get(req) == null)
			return;
		int idx = mExportEmptyRequest2Index.get(req);
		exEmptyReqScheduled[idx] = true;
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

		ArrayList<TruckRoute> lst_truckRoutes = new ArrayList<TruckRoute>();

		RouteDoubleImportCreator routeDoubleImportCreator = new RouteDoubleImportCreator(
				this);
		RouteKeplechCreator routeKeplechCreator = new RouteKeplechCreator(this);
		RouteSwapImportExportCreator routeSwapImportExportCreator = new RouteSwapImportExportCreator(
				this);
		RouteTangboWarehouseExport routeTangboWarehouseExport = new RouteTangboWarehouseExport(
				this);
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
		
		
		for (int i = 0; i < nbExReqs; i++) {
			mExReq2Index.put(exReq[i], i);
		}
		for (int i = 0; i < nbImReqs; i++) {
			mImReq2Index.put(imReq[i], i);
		}
		for (int i = 0; i < nbWhReqs; i++) {
			mWhReq2Index.put(whReq[i], i);
		}
		for (int i = 0; i < nbEmptyContainerFromDepotReqs; i++)
			mEmptyContainerFromDepotReq2Index.put(
					emptyContainerFromDepotReq[i], i);
		for (int i = 0; i < nbEmptyContainerToDepotReqs; i++)
			mEmptyContainerToDepotReq2Index.put(emptyContainerToDepotReq[i], i);
		for (int i = 0; i < nbTransportContainerReqs; i++)
			mTransportContainerReq2Index.put(transportContainerReq[i], i);
		for(int i = 0; i < nbExLadenReqs; i++)
			mExportLadenRequest2Index.put(exLadenReq[i], i);
		for(int i = 0; i < nbExEmptyReqs; i++)
			mExportEmptyRequest2Index.put(exEmptyReq[i], i);
		for(int i = 0; i < nbImLadenReqs; i++)
			mImportLadenRequest2Index.put(imLadenReq[i], i);
		for(int i = 0; i < nbImEmptyReqs; i++)
			mImportEmptyRequest2Index.put(imEmptyReq[i], i);
		
		
		cand_sol = new CandidateSolution();

		CandidateRouteComposer candidate_routes = new CandidateRouteComposer();

		while (true) {
			candidate_routes.clear();

			//exploreDoubleImport(candidate_routes);
			
			//exploreSwapImportExport(candidate_routes);
			//exploreKepLech(candidate_routes);
			//exploreTangBo(candidate_routes);
			System.out.println(name()
					+ "::solve, special operators, candidates_routes.sz = "
					+ candidate_routes.size());
			if (candidate_routes.size() == 0) {
				System.out.println(name() + "::solve, start exploreDirectRouteExport");
				exploreDirectRouteExport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteExport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteImport");
				exploreDirectRouteImport(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteImport");
				
				System.out.println(name() + "::solve, start exploreDirectRouteWarehouseWarehouse");
				exploreDirectRouteWarehouseWarehouse(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteWarehouseWarehouse");
				
				System.out.println(name() + "::solve, start exploreDirectRouteEmptyContainerFromDepotRequest");
				exploreDirectRouteEmptyContainerFromDepotRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteEmptyContainerFromDepotRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteEmptyContainerToDepotRequest");
				exploreDirectRouteEmptyContainerToDepotRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteEmptyContainerToDepotRequest");
				
				System.out.println(name() + "::solve, start exploreDirectRouteTransportContainerRequest");
				exploreDirectRouteTransportContainerRequest(candidate_routes);
				System.out.println(name() + "::solve, finish exploreDirectRouteTransportContainerRequest");
				
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
