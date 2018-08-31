package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ExportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckItinerary;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.WarehouseContainerTransportRequest;
import utils.DateTimeUtils;

public class RouteTangboWarehouseExport {
	ContainerTruckMoocSolver solver;

	public RouteTangboWarehouseExport(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteTangboWarehouseExport";
	}

	public boolean checkTangboWarehouseExport(Truck truck, Mooc mooc,
			Container container, WarehouseContainerTransportRequest wr,
			ExportContainerRequest er) {
		boolean ok = true;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20)) {
			if (!container.getCategoryCode().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY40)) {
			if (container.getCategoryCode().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
		}
		if (!ok)
			return ok;

		if (container.getCategoryCode()
				.equals(ContainerCategoryEnum.CATEGORY20)) {
			if (!wr.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
			if (!er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY20))
				ok = false;
		} else if (container.getCategoryCode().equals(
				ContainerCategoryEnum.CATEGORY40)) {
			if (wr.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
			if (er.getContainerCategory().equals(
					ContainerCategoryEnum.CATEGORY45))
				ok = false;
		}

		return ok;
	}

	public TruckRouteInfo4Request createTangboWarehouseExport(Truck truck,
			Mooc mooc, Container container,
			WarehouseContainerTransportRequest wr, ExportContainerRequest er) {
		// try to create route with truck-mooc-container serving wr and then er
		// truck-mooc-container -> from_warehouse(wr) -> to_warehouse(wr) ->
		// warehouse(er) -> port(er)
		if (!checkTangboWarehouseExport(truck, mooc, container, wr, er))
			return null;
		if (solver.mContainer2LastDepot.get(container) == null) {
			// this is imported container, does not has depot
			solver.logln(name()
					+ "::createTangboWarehouseExport, imported container null");
			return null;
		}

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc,
				container);
		if (combo == null)
			return null;

		double distance = combo.extraDistance
				+ solver.getDistance(combo.lastLocationCode,
						wr.getFromWarehouseCode());
		
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		int lastUsedIndex = -1;
		
		if (combo.routeElement == null) {
			
			RouteElement e0 = new RouteElement();
			L.add(e0);
			e0.setAction(ActionEnum.DEPART_FROM_DEPOT);
			e0.setDepotTruck(solver.mTruck2LastDepot.get(truck));
			departureTime = solver.mTruck2LastTime.get(truck);
			solver.mPoint2DepartureTime.put(e0, departureTime);

			RouteElement e1 = new RouteElement();
			L.add(e1);
			e1.deriveFrom(e0);
			e1.setAction(ActionEnum.TAKE_MOOC_AT_DEPOT);
			e1.setDepotMooc(solver.mMooc2LastDepot.get(mooc));
			arrivalTime = departureTime
					+ solver.getTravelTime(
							e0.getDepotTruck().getLocationCode(), e1
									.getDepotMooc().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mMooc2LastTime.get(mooc));
			duration = e1.getDepotMooc().getPickupMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e1, arrivalTime);
			solver.mPoint2DepartureTime.put(e1, departureTime);

			RouteElement e2 = new RouteElement();
			L.add(e2);
			e2.deriveFrom(e1);
			e2.setAction(ActionEnum.TAKE_CONTAINER_AT_DEPOT);
			e2.setDepotContainer(solver.mContainer2LastDepot.get(container));
			arrivalTime = departureTime
					+ solver.getTravelTime(e1.getDepotMooc().getLocationCode(),
							e2.getDepotContainer().getLocationCode());
			serviceTime = Utils.MAX(arrivalTime,
					solver.mContainer2LastTime.get(container));
			duration = e2.getDepotContainer().getPickupContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);
		
			lastElement = e2;
		}else{
			TruckItinerary I = solver.getItinerary(truck);
			TruckRoute tr = I.getLastTruckRoute();
			lastUsedIndex = tr.indexOf(combo.routeElement);
		}
		
			RouteElement e3 = new RouteElement();
			L.add(e3);
			e3.deriveFrom(lastElement);
			e3.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e3.setWarehouse(solver.mCode2Warehouse.get(wr
					.getFromWarehouseCode()));
			e3.setWarehouseRequest(wr);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement.getDepotContainer()
							.getLocationCode(), e3.getWarehouse()
							.getLocationCode());
			if (arrivalTime > DateTimeUtils.dateTime2Int(wr
					.getLateDateTimeLoad())) {
				solver.logln(name()
						+ "::createTangboWarehouseExport, violation time arrival = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
						+ " > wr.getLateDateTimeLoad() = "
						+ wr.getLateDateTimeLoad());
				return null;
			}
			serviceTime = Utils
					.MAX(arrivalTime, (int) DateTimeUtils.dateTime2Int(wr
							.getEarlyDateTimeLoad()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e3, arrivalTime);
			solver.mPoint2DepartureTime.put(e3, departureTime);

			RouteElement e4 = new RouteElement();
			L.add(e4);
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			e4.setWarehouse(solver.mCode2Warehouse.get(wr
					.getFromWarehouseCode()));
			arrivalTime = departureTime + wr.getLoadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e4, arrivalTime);
			solver.mPoint2DepartureTime.put(e4, departureTime);

			RouteElement e5 = new RouteElement();
			L.add(e5);
			e5.deriveFrom(e4);
			e5.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e5.setWarehouse(solver.mCode2Warehouse.get(wr.getToWarehouseCode()));
			arrivalTime = departureTime
					+ solver.getTravelTime(e4.getWarehouse().getLocationCode(),
							e5.getWarehouse().getLocationCode());
			if (arrivalTime > DateTimeUtils.dateTime2Int(wr
					.getLateDateTimeUnload())) {
				solver.logln(name()
						+ "::createTangboWarehouseExport, violation time arrival = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
						+ " > wr.getLateDateTimeUnload() = "
						+ wr.getLateDateTimeUnload());
				return null;
			}
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(wr.getEarlyDateTimeUnload()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e5, arrivalTime);
			solver.mPoint2DepartureTime.put(e5, departureTime);

			RouteElement e6 = new RouteElement();
			L.add(e6);
			e6.deriveFrom(e5);
			e6.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouse(solver.mCode2Warehouse.get(wr.getToWarehouseCode()));
			arrivalTime = departureTime + wr.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e6, arrivalTime);
			solver.mPoint2DepartureTime.put(e6, departureTime);

			RouteElement e7 = new RouteElement();
			L.add(e7);
			e7.deriveFrom(e6);
			e7.setAction(ActionEnum.WAIT_LOAD_CONTAINER_AT_WAREHOUSE);
			e7.setWarehouse(solver.mCode2Warehouse.get(er.getWareHouseCode()));
			e7.setExportRequest(er);
			arrivalTime = departureTime
					+ solver.getTravelTime(e6.getWarehouse().getLocationCode(),
							e7.getWarehouse().getLocationCode());
			if (arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeLoadAtWarehouse())) {
				solver.logln(name()
						+ "::createTangboWarehouseExport, violation time arrival = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
						+ " > er.getLateDateTimeLoadAtWarehouse() = "
						+ er.getLateDateTimeLoadAtWarehouse());
				return null;
			}
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(er.getEarlyDateTimeLoadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e7, arrivalTime);
			solver.mPoint2DepartureTime.put(e7, departureTime);

			RouteElement e8 = new RouteElement();
			L.add(e8);
			e8.deriveFrom(e7);
			e8.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_WAREHOUSE);
			e8.setWarehouse(solver.mCode2Warehouse.get(er.getWareHouseCode()));
			arrivalTime = departureTime + er.getLoadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e8, arrivalTime);
			solver.mPoint2DepartureTime.put(e8, departureTime);

			RouteElement e9 = new RouteElement();
			L.add(e9);
			e9.deriveFrom(e8);
			e9.setAction(ActionEnum.WAIT_RELEASE_LOADED_CONTAINER_AT_PORT);
			e9.setPort(solver.mCode2Port.get(er.getPortCode()));
			arrivalTime = departureTime
					+ solver.getTravelTime(e8.getWarehouse().getLocationCode(),
							e9.getPort().getLocationCode());
			if (arrivalTime > DateTimeUtils.dateTime2Int(er
					.getLateDateTimeUnloadAtPort())) {
				solver.logln(name()
						+ "::createTangboWarehouseExport, violation time arrival = "
						+ DateTimeUtils.unixTimeStamp2DateTime(arrivalTime)
						+ " > er.getLateDateTimeUnloadAtPort() = "
						+ er.getLateDateTimeUnloadAtPort());
				return null;
			}
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(er.getEarlyDateTimeUnloadAtPort()));
			duration = er.getUnloadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e9, arrivalTime);
			solver.mPoint2DepartureTime.put(e9, departureTime);
			solver.mContainer2LastDepot.put(container, null);
			solver.mContainer2LastTime.put(container, Integer.MAX_VALUE);

			RouteElement e10 = new RouteElement();
			L.add(e10);
			e10.deriveFrom(e9);
			e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
			e10.setDepotMooc(solver.findDepotForReleaseMooc(mooc));
			arrivalTime = departureTime
					+ solver.getTravelTime(e9.getPort().getLocationCode(), e10
							.getDepotMooc().getLocationCode());
			serviceTime = arrivalTime;
			duration = e10.getDepotMooc().getDeliveryMoocDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e10, arrivalTime);
			solver.mPoint2DepartureTime.put(e10, departureTime);

			RouteElement e11 = new RouteElement();
			L.add(e11);
			e11.deriveFrom(e10);
			e11.setAction(ActionEnum.REST_AT_DEPOT);
			e11.setDepotTruck(solver.findDepotForReleaseTruck(truck));
			arrivalTime = departureTime
					+ solver.getTravelTime(
							e10.getDepotMooc().getLocationCode(), e11
									.getDepotTruck().getLocationCode());
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e11, arrivalTime);
			solver.mPoint2DepartureTime.put(e11, departureTime);

			TruckRoute r = new TruckRoute();
			RouteElement[] e = new RouteElement[L.size()];
			for (int i = 0; i < e.length; i++)
				e[i] = L.get(i);
			r.setNodes(e);
			r.setTruck(truck);

			solver.propagate(r);

			
			TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
			tri.route = r;
			tri.lastUsedIndex = lastUsedIndex;
			tri.additionalDistance = distance;
			return tri;
		
	}

}
