package routingdelivery.smartlog.containertruckmoocassigment.service;

import java.util.ArrayList;

import routingdelivery.smartlog.containertruckmoocassigment.model.ActionEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.ComboContainerMoocTruck;
import routingdelivery.smartlog.containertruckmoocassigment.model.Container;
import routingdelivery.smartlog.containertruckmoocassigment.model.ContainerCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.DepotContainer;
import routingdelivery.smartlog.containertruckmoocassigment.model.ImportContainerRequest;
import routingdelivery.smartlog.containertruckmoocassigment.model.Mooc;
import routingdelivery.smartlog.containertruckmoocassigment.model.MoocCategoryEnum;
import routingdelivery.smartlog.containertruckmoocassigment.model.Port;
import routingdelivery.smartlog.containertruckmoocassigment.model.RouteElement;
import routingdelivery.smartlog.containertruckmoocassigment.model.Truck;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRoute;
import routingdelivery.smartlog.containertruckmoocassigment.model.TruckRouteInfo4Request;
import routingdelivery.smartlog.containertruckmoocassigment.model.Warehouse;
import utils.DateTimeUtils;

public class RouteDoubleImportCreator {

	public ContainerTruckMoocSolver solver;

	public RouteDoubleImportCreator(ContainerTruckMoocSolver solver) {
		this.solver = solver;
	}

	public String name() {
		return "RouteDoubleImportCreator";
	}

	public boolean checkCapacityForDoubleImport(Truck truck, Mooc mooc,
			ImportContainerRequest ir1, ImportContainerRequest ir2) {
		if (!ir1.getContainerCategory()
				.equals(ContainerCategoryEnum.CATEGORY20)
				|| !ir2.getContainerCategory().equals(
						ContainerCategoryEnum.CATEGORY20))
			return false;
		if (mooc.getCategory().equals(MoocCategoryEnum.CATEGORY20))
			return false;

		return true;
	}

	public TruckRouteInfo4Request createDoubleImportRequest(Truck truck,
			Mooc mooc, ImportContainerRequest ir1, ImportContainerRequest ir2) {
		// truck-mooc -> port(ir1) -> port(ir2) -> warehouse(ir1/ir2) ->
		// warehouse(ir2/ir1)
		// System.out.println(name() +
		// "::createDoubleImportRequest, ir1.portCode = " + ir1.getPortCode());

		if (!checkCapacityForDoubleImport(truck, mooc, ir1, ir2))
			return null;

		Port port1 = solver.mCode2Port.get(ir1.getPortCode());
		Port port2 = solver.mCode2Port.get(ir2.getPortCode());
		Warehouse wh1 = solver.mCode2Warehouse.get(ir1.getWareHouseCode());
		Warehouse wh2 = solver.mCode2Warehouse.get(ir2.getWareHouseCode());
		Container container1 = solver.mCode2Container.get(ir1
				.getContainerCode());
		Container container2 = solver.mCode2Container.get(ir2
				.getContainerCode());

		ComboContainerMoocTruck combo = solver.findLastAvailable(truck, mooc);
		if (combo == null)
			return null;

		ArrayList<RouteElement> L = new ArrayList<RouteElement>();
		RouteElement lastElement = combo.routeElement;
		int departureTime = combo.startTime;
		int arrivalTime = -1;
		int serviceTime = -1;
		int duration = -1;
		double distance = -1;
		
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
			
			lastElement = e1;
		}

		boolean ir1First = true;
		if (DateTimeUtils.dateTime2Int(ir1.getLateDateTimePickupAtPort()) > DateTimeUtils
				.dateTime2Int(ir2.getLateDateTimePickupAtPort())) {
			ir1First = false;
		}
		RouteElement e2 = new RouteElement();
		L.add(e2);
		RouteElement e3 = new RouteElement();
		L.add(e3);

		if (ir1First) {
			e2.deriveFrom(lastElement);
			e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e2.setPort(port1);
			e2.setImportRequest(ir1);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement.getDepotMooc().getLocationCode(),
							e2.getPort().getLocationCode());
			distance = combo.extraDistance
					+ solver.getDistance(combo.lastLocationCode,
							e2.getLocationCode());
			
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimePickupAtPort()));
			duration = ir1.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			e3.deriveFrom(e2);
			e3.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e3.setPort(port2);
			e3.setImportRequest(ir2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e2.getPort().getLocationCode(), e3
							.getPort().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimePickupAtPort()));
			duration = ir2.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e3, arrivalTime);
			solver.mPoint2DepartureTime.put(e3, departureTime);

		} else {
			e2.deriveFrom(lastElement);
			e2.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e2.setPort(port2);
			e2.setImportRequest(ir2);
			arrivalTime = departureTime
					+ solver.getTravelTime(lastElement.getDepotMooc().getLocationCode(),
							e2.getPort().getLocationCode());
			
			distance = combo.extraDistance
					+ solver.getDistance(combo.lastLocationCode,
							e2.getLocationCode());
			
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimePickupAtPort()));
			duration = ir2.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e2, arrivalTime);
			solver.mPoint2DepartureTime.put(e2, departureTime);

			e3.deriveFrom(e2);
			e3.setAction(ActionEnum.LINK_LOADED_CONTAINER_AT_PORT);

			e3.setPort(port1);
			e3.setImportRequest(ir1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e2.getPort().getLocationCode(), e3
							.getPort().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimePickupAtPort()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimePickupAtPort()));
			duration = ir1.getLoadDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e3, arrivalTime);
			solver.mPoint2DepartureTime.put(e3, departureTime);

		}

		ir1First = true;
		if (solver.getDistance(e3.getPort().getLocationCode(),
				wh1.getLocationCode()) > solver.getDistance(e3.getPort()
				.getLocationCode(), wh2.getLocationCode())) {
			ir1First = false;
		}
		RouteElement e4 = new RouteElement();
		L.add(e4);
		RouteElement e5 = new RouteElement();
		L.add(e5);
		RouteElement e6 = new RouteElement();
		L.add(e6);
		RouteElement e7 = new RouteElement();
		L.add(e7);

		if (ir1First) {
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e4.setWarehouse(wh1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e3.getPort().getLocationCode(), e4
							.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e4, arrivalTime);
			solver.mPoint2DepartureTime.put(e4, departureTime);

			e5.deriveFrom(e4);
			e5.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e5.setWarehouse(wh1);
			arrivalTime = departureTime + ir1.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e5, arrivalTime);
			solver.mPoint2DepartureTime.put(e5, departureTime);

			// release ir2
			e6.deriveFrom(e5);
			e6.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouse(wh2);
			// if(e5.getWarehouse() == null){
			// System.out.println(name() +
			// "::createDoubleImportRequest, warehouse e5 = NULL");
			// }else{
			// System.out.println(name() +
			// "::createDoubleImportRequest, warehouse e5 = " +
			// e5.getWarehouse().getLocationCode());
			// }
			arrivalTime = departureTime
					+ solver.getTravelTime(e5.getWarehouse().getLocationCode(),
							e6.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e6, arrivalTime);
			solver.mPoint2DepartureTime.put(e6, departureTime);

			e7.deriveFrom(e6);
			e7.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e7.setWarehouse(wh2);
			arrivalTime = departureTime + ir2.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e7, arrivalTime);
			solver.mPoint2DepartureTime.put(e7, departureTime);

		} else {// ir2 first
			e4.deriveFrom(e3);
			e4.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e4.setWarehouse(wh2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e3.getPort().getLocationCode(), e4
							.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir2
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir2.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e4, arrivalTime);
			solver.mPoint2DepartureTime.put(e4, departureTime);

			e5.deriveFrom(e4);
			e5.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e5.setWarehouse(wh2);
			arrivalTime = departureTime + ir2.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e5, arrivalTime);
			solver.mPoint2DepartureTime.put(e5, departureTime);

			// release ir1
			e6.deriveFrom(e5);
			e6.setAction(ActionEnum.WAIT_UNLOAD_CONTAINER_AT_WAREHOUSE);
			e6.setWarehouse(wh1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e5.getWarehouse().getLocationCode(),
							e6.getWarehouse().getLocationCode());
			if (arrivalTime > (int) DateTimeUtils.dateTime2Int(ir1
					.getLateDateTimeUnloadAtWarehouse()))
				return null;
			serviceTime = Utils.MAX(arrivalTime, (int) DateTimeUtils
					.dateTime2Int(ir1.getEarlyDateTimeUnloadAtWarehouse()));
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e6, arrivalTime);
			solver.mPoint2DepartureTime.put(e6, departureTime);

			e7.deriveFrom(e6);
			e7.setAction(ActionEnum.LINK_EMPTY_CONTAINER_AT_WAREHOUSE);
			e7.setWarehouse(wh1);
			arrivalTime = departureTime + ir1.getUnloadDuration();
			serviceTime = arrivalTime;
			duration = 0;
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e7, arrivalTime);
			solver.mPoint2DepartureTime.put(e7, departureTime);

		}
		DepotContainer depotContainer1 = solver
				.findDepotForReleaseContainer(container1);
		DepotContainer depotContainer2 = solver
				.findDepotForReleaseContainer(container2);

		if (depotContainer1 == null) {
			depotContainer1 = solver.mCode2DepotContainer.get(ir1
					.getDepotContainerCode());
		}
		if (depotContainer2 == null) {
			depotContainer2 = solver.mCode2DepotContainer.get(ir2
					.getDepotContainerCode());
		}

		// if(depotContainer1 == null){
		// System.out.println(name() +
		// "::createDoubleImportRequest depotContainer1 = NULL");
		// }else{
		// System.out.println(name() +
		// "::createDoubleImportRequest depotContainer1 = " +
		// depotContainer1.getLocationCode());
		// }
		ir1First = true;
		if (solver.getDistance(e7.getWarehouse().getLocationCode(),
				depotContainer1.getLocationCode()) > solver.getDistance(e7
				.getWarehouse().getLocationCode(), depotContainer2
				.getLocationCode())) {
			ir1First = false;
		}

		RouteElement e8 = new RouteElement();
		L.add(e8);
		RouteElement e9 = new RouteElement();
		L.add(e9);

		if (ir1First) {
			e8.deriveFrom(e7);
			e8.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e8.setDepotContainer(depotContainer1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e7.getWarehouse().getLocationCode(),
							e8.getDepotContainer().getLocationCode());
			serviceTime = arrivalTime;
			duration = e8.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e8, arrivalTime);
			solver.mPoint2DepartureTime.put(e8, departureTime);

			// release container2
			e9.deriveFrom(e8);
			e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e9.setDepotContainer(depotContainer2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e8.getDepotContainer()
							.getLocationCode(), e9.getDepotContainer()
							.getLocationCode());
			serviceTime = arrivalTime;
			duration = e9.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e9, arrivalTime);
			solver.mPoint2DepartureTime.put(e9, departureTime);

		} else {
			e8.deriveFrom(e7);
			e8.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e8.setDepotContainer(depotContainer2);
			arrivalTime = departureTime
					+ solver.getTravelTime(e7.getWarehouse().getLocationCode(),
							e8.getDepotContainer().getLocationCode());
			serviceTime = arrivalTime;
			duration = e8.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e8, arrivalTime);
			solver.mPoint2DepartureTime.put(e8, departureTime);

			// release container2
			e9.deriveFrom(e8);
			e9.setAction(ActionEnum.RELEASE_CONTAINER_AT_DEPOT);
			e9.setDepotContainer(depotContainer1);
			arrivalTime = departureTime
					+ solver.getTravelTime(e8.getDepotContainer()
							.getLocationCode(), e9.getDepotContainer()
							.getLocationCode());
			serviceTime = arrivalTime;
			duration = e9.getDepotContainer().getDeliveryContainerDuration();
			departureTime = serviceTime + duration;
			solver.mPoint2ArrivalTime.put(e9, arrivalTime);
			solver.mPoint2DepartureTime.put(e9, departureTime);

		}
		RouteElement e10 = new RouteElement();
		L.add(e10);
		e10.deriveFrom(e9);
		e10.setAction(ActionEnum.RELEASE_MOOC_AT_DEPOT);
		e10.setDepotMooc(solver.findDepotForReleaseMooc(mooc));
		arrivalTime = departureTime
				+ solver.getTravelTime(
						e9.getDepotContainer().getLocationCode(), e10
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
				+ solver.getTravelTime(e10.getDepotMooc().getLocationCode(),
						e11.getDepotTruck().getLocationCode());
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
		r.setType(TruckRoute.DOUBLE_IMPORT);
		solver.propagate(r);

		TruckRouteInfo4Request tri = new TruckRouteInfo4Request();
		tri.route = r;
		tri.lastUsedIndex = -1;
		tri.additionalDistance = distance;
		return tri;

	}

}
