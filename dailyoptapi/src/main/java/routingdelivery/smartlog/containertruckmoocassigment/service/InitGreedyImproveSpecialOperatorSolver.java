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
import routingdelivery.smartlog.containertruckmoocassigment.model.DoubleImportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Individual2ImportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualImportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseExportRoutesComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.IndividualWarehouseRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.KepLechRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.StatisticInformation;
import routingdelivery.smartlog.containertruckmoocassigment.model.SwapImportExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.TangboWarehouseExportRouteComposer;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
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

	boolean[] exReqScheduled;
	boolean[] imReqScheduled;
	boolean[] whReqScheduled;
	HashMap<ExportContainerRequest, Integer> mExReq2Index;
	HashMap<ImportContainerRequest, Integer> mImReq2Index;
	HashMap<WarehouseContainerTransportRequest, Integer> mWhReq2Index;
	
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
					sel_truck, sel_mooc, sel_route, sel_imReq_k, sel_imReq_q,sel_tri,
					minDistance);
			System.out.println(name() + "::exploreDoubleImport, minDistace = " + minDistance);
			
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
			System.out.println(name() + "::exploreDoubleImport, minDistace1 = " + minDistance1);
			
			if (minDistance1 < minDistance) {
				Individual2ImportRoutesComposer icp = new Individual2ImportRoutesComposer(this,
						sel_tr_k, sel_tr_q, sel_imReq_k, sel_imReq_q,sel_tri_k,sel_tri_q,
						minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(cp);
			}
		}

	}

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
			SwapImportExportRouteComposer cp = new SwapImportExportRouteComposer(this,
					sel_truck, sel_mooc, sel_exReq_q, sel_imReq_k, sel_tri,minDistance);
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
								if(containers[k].getCode().startsWith("A")) continue;// imported container
								backup();
								TruckRouteInfo4Request tri_k = createRouteForImportRequest(
										sel_imReq_k, trucks[i], moocs[j]);
								TruckRouteInfo4Request tri_q = createRouteForExportRequest(
										sel_exReq_q, trucks[i1], moocs[j1],
										containers[k]);
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
									System.out.println(name() + "::exploreSwapImportExport, update minDistance1 = " + minDistance1);
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
				IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(this,
						sel_tr_k, sel_tr_q, sel_imReq_k, sel_exReq_q,sel_tri_k,sel_tri_q,
						minDistance1);
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
							TruckRouteInfo4Request tri = routeKeplechCreator.createKeplech(
									trucks[i], moocs[j], containers[k],
									exReq[a], imReq[b]);
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
			KepLechRouteComposer kcp = new KepLechRouteComposer(this,sel_truck,
					sel_mooc, sel_container, sel_imReq_b, sel_exReq_a,sel_tri,
					minDistance);

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
								if(containers[k].getCode().startsWith("A")) continue;// imported container
								backup();
								TruckRouteInfo4Request tri_k = createRouteForImportRequest(
										sel_imReq_b, trucks[i], moocs[j]);
								TruckRouteInfo4Request tri_q = createRouteForExportRequest(
										sel_exReq_a, trucks[i1], moocs[j1],
										containers[k]);
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
				IndividualImportExportRoutesComposer icp = new IndividualImportExportRoutesComposer(this,
						sel_tr_k, sel_tr_q, sel_imReq_b, sel_exReq_a,sel_tri_k,sel_tri_q,
						minDistance1);
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
			TangboWarehouseExportRouteComposer tcp = new TangboWarehouseExportRouteComposer(this,
					sel_truck, sel_mooc, sel_container, sel_route, sel_whReq_a,
					sel_exReq_b, sel_tri, minDistance);

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
									if(containers[k2].getCode().startsWith("A") || containers[k1].getCode().startsWith("A")) continue;// imported container
									backup();
									TruckRouteInfo4Request tri_k = createRouteForWarehouseWarehouseRequest(
											sel_whReq_a, trucks[i1], moocs[j1],
											containers[k1]);
									TruckRouteInfo4Request tri_q = createRouteForExportRequest(
											sel_exReq_b, trucks[i2], moocs[j2],
											containers[k2]);
									
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
				IndividualWarehouseExportRoutesComposer icp = new IndividualWarehouseExportRoutesComposer(this,
						sel_tr_k, sel_tr_q, sel_whReq_a, sel_exReq_b,sel_tri_k,sel_tri_q,
						minDistance1);
				candidateRouteComposer.add(icp);
			} else {
				candidateRouteComposer.add(tcp);
			}
		}

	}

	public void exploreDirectRouteExport(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ExportContainerRequest sel_exReq = null;
		for (int i = 0; i < exReq.length; i++) {
			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					for (int q = 0; q < containers.length; q++) {
						backup();
						TruckRouteInfo4Request tri = createRouteForExportRequest(
								exReq[i], trucks[j], moocs[k], containers[q]);
						TruckRoute tr = tri.route;
						
						double dis = tr.getDistance() - tr.getReducedDistance();
						if (dis < minDistance) {
							minDistance = dis;
							sel_tr = tr;
							sel_tri = tri;
							sel_exReq = exReq[i];
						}
						restore();
					}
				}
			}
		}
		if (sel_tr != null) {
			IndividualExportRouteComposer icp = new IndividualExportRouteComposer(this,
					sel_tr, sel_exReq, sel_tri, sel_tr.getDistance() - sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}

	public void exploreDirectRouteImport(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		ImportContainerRequest sel_imReq = null;
		for (int i = 0; i < imReq.length; i++) {
			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					backup();
					TruckRouteInfo4Request tri = createRouteForImportRequest(imReq[i],
							trucks[j], moocs[k]);
					TruckRoute tr = tri.route;
					
					double dis = tr.getDistance() - tr.getReducedDistance();
					if (dis < minDistance) {
						minDistance = dis;
						sel_tr = tr;
						sel_tri = tri;
						sel_imReq = imReq[i];
					}
					restore();
				}
			}
		}
		if (sel_tr != null) {
			IndividualImportRouteComposer icp = new IndividualImportRouteComposer(this,
					sel_tr, sel_tri, sel_imReq, sel_tr.getDistance() - sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}

	public void exploreDirectRouteWarehouseWarehouse(
			CandidateRouteComposer candidateRouteComposer) {
		double minDistance = Integer.MAX_VALUE;
		TruckRoute sel_tr = null;
		TruckRouteInfo4Request sel_tri = null;
		WarehouseContainerTransportRequest sel_whReq = null;
		
		for (int i = 0; i < whReq.length; i++) {
			for (int j = 0; j < trucks.length; j++) {
				for (int k = 0; k < moocs.length; k++) {
					for (int q = 0; q < containers.length; q++) {
						backup();
						TruckRouteInfo4Request tri = createRouteForWarehouseWarehouseRequest(
								whReq[i], trucks[j], moocs[k], containers[q]);
						TruckRoute tr = tri.route;
						
						double dis = tr.getDistance() - tr.getReducedDistance();
						if (dis < minDistance) {
							minDistance = dis;
							sel_tr = tr;
							sel_tri = tri;
							sel_whReq = whReq[i];
						}
						restore();
					}
				}
			}
		}
		if (sel_tr != null) {
			IndividualWarehouseRouteComposer icp = new IndividualWarehouseRouteComposer(this,
					sel_tr, sel_tri, sel_whReq, sel_tr.getDistance() - sel_tr.getReducedDistance());
			candidateRouteComposer.add(icp);
		}
	}
	public void markServed(ExportContainerRequest exR){
		if(mExReq2Index.get(exR) == null) return;
		int idx = mExReq2Index.get(exR);
		exReqScheduled[idx] = true;
	}
	public void markServed(ImportContainerRequest imR){
		if(mImReq2Index.get(imR) == null) return;
		int idx = mImReq2Index.get(imR);
		imReqScheduled[idx] = true;
	}
	public void markServed(WarehouseContainerTransportRequest whR){
		if(mWhReq2Index.get(whR) == null) return;
		int idx = mWhReq2Index.get(whR);
		whReqScheduled[idx] = true;
	}
	public void addRoute(TruckRoute tr, int lastIndex){
		Truck truck = tr.getTruck();
		if(mTruck2Itinerary.get(truck) == null){
			mTruck2Itinerary.put(truck, new TruckItinerary());
		}
		mTruck2Itinerary.get(truck).addRoute(tr, lastIndex);
	}
	public ContainerTruckMoocSolution solve(ContainerTruckMoocInput input) {
		this.input = input;

		initLog();
		modifyContainerCode();
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
		exReq = getSortedExportRequests();
		imReq = getSortedImportRequests();
		whReq = getSortedWarehouseTransportRequests();

		exReqScheduled = new boolean[exReq.length];
		imReqScheduled = new boolean[imReq.length];
		whReqScheduled = new boolean[whReq.length];
		for (int i = 0; i < exReqScheduled.length; i++)
			exReqScheduled[i] = false;
		for (int i = 0; i < imReqScheduled.length; i++)
			imReqScheduled[i] = false;
		for (int i = 0; i < whReqScheduled.length; i++)
			whReqScheduled[i] = false;

		mExReq2Index = new HashMap<ExportContainerRequest, Integer>();
		mImReq2Index = new HashMap<ImportContainerRequest, Integer>();
		mWhReq2Index = new HashMap<WarehouseContainerTransportRequest, Integer>();
		for(int i = 0; i < exReq.length; i++){
			mExReq2Index.put(exReq[i], i);
		}
		for(int i = 0; i < imReq.length; i++){
			mImReq2Index.put(imReq[i], i);
		}
		for(int i = 0; i < whReq.length; i++){
			mWhReq2Index.put(whReq[i], i);
		}
		
		
		
		cand_sol = new CandidateSolution();

		CandidateRouteComposer candidate_routes = new CandidateRouteComposer();

		while (true) {
			candidate_routes.clear();

			exploreDoubleImport(candidate_routes);
			exploreSwapImportExport(candidate_routes);
			exploreKepLech(candidate_routes);
			exploreTangBo(candidate_routes);

			if (candidate_routes.size() == 0) {
				exploreDirectRouteExport(candidate_routes);
				exploreDirectRouteImport(candidate_routes);
				exploreDirectRouteWarehouseWarehouse(candidate_routes);

				if (candidate_routes.size() == 0) {
					break;
				} else {
					candidate_routes.performBestRouteComposer();
				}
			} else {
				candidate_routes.performBestRouteComposer();
			}
		}

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

		ContainerTruckMoocSolution sol = new ContainerTruckMoocSolution(TR,
				infos, "OK");

		return sol;

	}

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
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, lastLocationCode = "
					// + combo.lastLocationCode);

					int arrivalTimeWarehouse = startTime
							+ getTravelTime(startLocationCode,
									warehouseLocationCode);

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

	public TruckRouteInfo4Request createNewDirectRoute4ExportRequest(Truck truck, Mooc mooc, Container container,
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


					HashSet<Container> C = mShipCompanyCode2Containers.get(r
							.getShipCompanyCode());

					if (!C.contains(container))
						return null;
					// if(!r.getShipCompanyCode().equals(shipCompanyCode))
					// continue;

					ComboContainerMoocTruck sel_combo = findLastAvailable(truck,
							mooc, container);
					if (sel_combo == null)
						return null;

					int startTime = sel_combo.startTime;
					String startLocationCode = sel_combo.lastLocationCode;
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, lastLocationCode = "
					// + combo.lastLocationCode);

					int arrivalTimeWarehouse = startTime
							+ getTravelTime(startLocationCode,
									warehouseLocationCode);

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
						return null;

					double distance = sel_combo.extraDistance
							+ getDistance(sel_combo.lastLocationCode,
									warehouseLocationCode);
					// System.out.println(name() +
					// "::createDirectRoute4ExportRequest, arrivalTimeWarehouse = "
					// + arrivalTimeWarehouse +
					// ", latePickupWarehouse = " + latePickupWarehouse +
					// ", distance = " + distance);
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

	public TruckRouteInfo4Request createNewDirectRoute4WarehouseRequest(Truck truck, Mooc mooc, Container container,
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


					ComboContainerMoocTruck sel_combo = findLastAvailable(truck,
							mooc, container);
					if (sel_combo == null) {
						logln(name()
								+ "::createDirectRoute4WarehouseRequest, sel_combo is NULL????");
						return null;
					}

					int startTime = sel_combo.startTime;
					String startLocationCode = sel_combo.lastLocationCode;

					int arrivalTimeWarehouse = startTime
							+ getTravelTime(startLocationCode,
									fromWarehouseLocationCode);


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

	public TruckRouteInfo4Request createNewDirectRoute4ImportRequest(Truck truck, Mooc mooc,
			ImportContainerRequest r) {
		String portCode = r.getPortCode();
		Port port = mCode2Port.get(portCode);
		String locationCodePort = mCode2Port.get(portCode).getLocationCode();
		int latePickupPort = (int) DateTimeUtils.dateTime2Int(r
				.getLateDateTimePickupAtPort());

		
				ComboContainerMoocTruck sel_combo = findLastAvailable(truck, mooc);
				if(sel_combo == null) return null;
				
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
