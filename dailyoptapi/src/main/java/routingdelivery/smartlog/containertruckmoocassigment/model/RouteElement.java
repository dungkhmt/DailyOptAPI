package routingdelivery.smartlog.containertruckmoocassigment.model;

public class RouteElement {
	private DepotTruck depotTruck;
	private DepotMooc depotMooc;
	private DepotContainer depotContainer;
	private Warehouse warehouse;
	private Port port;
	private Mooc mooc;
	private Container container;
	private Container containerKep;
	private Truck truck;
	
	private String locationCode;
	private String action;// LAY_MOOC, TRA_MOOC, LAY_CONTAINER, TRA_CONTAINER, 
						//   LOAD_HANG, UNLOAD_HANG, CAT_MOOC, 
						// 
	private ExportContainerRequest exportRequest;
	private ImportContainerRequest importRequest;
	private ExportContainerRequest exportRequestKep;
	private ImportContainerRequest importRequestKep;
	private WarehouseContainerTransportRequest warehouseRequest;
	private WarehouseContainerTransportRequest warehouseRequestKep;
	private EmptyContainerFromDepotRequest emptyContainerFromDepotRequest;
	private EmptyContainerToDepotRequest emptyContainerToDepotRequest;
	private TransportContainerRequest transportContainerRequest;
	private ExportLadenRequests exportLadenRequest;
	private ExportEmptyRequests exportEmptyRequest;
	private ImportLadenRequests importLadenRequest;
	private ImportEmptyRequests importEmptyRequest;
	private ExportLadenRequests exportLadenRequestKep;
	private ExportEmptyRequests exportEmptyRequestKep;
	private ImportLadenRequests importLadenRequestKep;
	private ImportEmptyRequests importEmptyRequestKep;
	
	private String earliestArrivalTime;
	private String latestArrivalTime;
	private int serviceDuration;
	private int index;
	private boolean kep;
	
	
	public RouteElement(DepotTruck depotTruck, DepotMooc depotMooc,
			DepotContainer depotContainer, Warehouse warehouse, Port port,
			Mooc mooc, Container container, Container containerKep, Truck truck, String locationCode,
			String action, ExportContainerRequest exportRequest,
			ImportContainerRequest importRequest,
			WarehouseContainerTransportRequest warehouseRequest,
			EmptyContainerFromDepotRequest emptyContainerFromDepotRequest,
			EmptyContainerToDepotRequest emptyContainerToDepotRequest,
			TransportContainerRequest transportContainerRequest,
			ExportLadenRequests exportLadenRequest,
			ExportEmptyRequests exportEmptyRequest,
			ImportLadenRequests importLadenRequest,
			ImportEmptyRequests importEmptyRequest, String arrivalTime,
			String departureTime, double distance) {
		super();
		this.depotTruck = depotTruck;
		this.depotMooc = depotMooc;
		this.depotContainer = depotContainer;
		this.warehouse = warehouse;
		this.port = port;
		this.mooc = mooc;
		this.container = container;
		this.containerKep = containerKep;
		this.truck = truck;
		this.locationCode = locationCode;
		this.action = action;
		this.exportRequest = exportRequest;
		this.importRequest = importRequest;
		this.warehouseRequest = warehouseRequest;
		this.emptyContainerFromDepotRequest = emptyContainerFromDepotRequest;
		this.emptyContainerToDepotRequest = emptyContainerToDepotRequest;
		this.transportContainerRequest = transportContainerRequest;
		this.exportLadenRequest = exportLadenRequest;
		this.exportEmptyRequest = exportEmptyRequest;
		this.importLadenRequest = importLadenRequest;
		this.importEmptyRequest = importEmptyRequest;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.distance = distance;
	}
	public ExportLadenRequests getExportLadenRequest() {
		return exportLadenRequest;
	}
	public void setExportLadenRequest(ExportLadenRequests exportLadenRequest) {
		this.exportLadenRequest = exportLadenRequest;
	}
	public ExportEmptyRequests getExportEmptyRequest() {
		return exportEmptyRequest;
	}
	public void setExportEmptyRequest(ExportEmptyRequests exportEmptyRequest) {
		this.exportEmptyRequest = exportEmptyRequest;
	}
	public ImportLadenRequests getImportLadenRequest() {
		return importLadenRequest;
	}
	public void setImportLadenRequest(ImportLadenRequests importLadenRequest) {
		this.importLadenRequest = importLadenRequest;
	}
	public ImportEmptyRequests getImportEmptyRequest() {
		return importEmptyRequest;
	}
	public void setImportEmptyRequest(ImportEmptyRequests importEmptyRequest) {
		this.importEmptyRequest = importEmptyRequest;
	}

	public ExportLadenRequests getExportLadenRequestKep() {
		return exportLadenRequestKep;
	}
	public void setExportLadenRequestKep(ExportLadenRequests exportLadenRequestKep) {
		this.exportLadenRequestKep = exportLadenRequestKep;
	}
	public ExportEmptyRequests getExportEmptyRequestKep() {
		return exportEmptyRequestKep;
	}
	public void setExportEmptyRequestKep(ExportEmptyRequests exportEmptyRequestKep) {
		this.exportEmptyRequestKep = exportEmptyRequestKep;
	}
	public ImportLadenRequests getImportLadenRequestKep() {
		return importLadenRequestKep;
	}
	public void setImportLadenRequestKep(ImportLadenRequests importLadenRequestKep) {
		this.importLadenRequestKep = importLadenRequestKep;
	}
	public ImportEmptyRequests getImportEmptyRequestKep() {
		return importEmptyRequestKep;
	}
	public void setImportEmptyRequestKep(ImportEmptyRequests importEmptyRequestKep) {
		this.importEmptyRequestKep = importEmptyRequestKep;
	}
	public EmptyContainerFromDepotRequest getEmptyContainerFromDepotRequest() {
		return emptyContainerFromDepotRequest;
	}
	public void setEmptyContainerFromDepotRequest(
			EmptyContainerFromDepotRequest emptyContainerFromDepotRequest) {
		this.emptyContainerFromDepotRequest = emptyContainerFromDepotRequest;
	}
	
	public EmptyContainerToDepotRequest getEmptyContainerToDepotRequest() {
		return emptyContainerToDepotRequest;
	}
	public void setEmptyContainerToDepotRequest(
			EmptyContainerToDepotRequest emptyContainerToDepotRequest) {
		this.emptyContainerToDepotRequest = emptyContainerToDepotRequest;
	}
	public TransportContainerRequest getTransportContainerRequest() {
		return transportContainerRequest;
	}
	public void setTransportContainerRequest(
			TransportContainerRequest transportContainerRequest) {
		this.transportContainerRequest = transportContainerRequest;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	private String arrivalTime;
	private String departureTime;
	private double distance;
	
	public String toString(){
		String s = "";
		s = s + "[" + getLocationCode() + ", Action " + getAction() + "]";
		return s;
	}
	public void deriveFrom(RouteElement e){
		//this.depotTruck = e.depotTruck;
		//this.depotMooc = e.depotMooc;
		//this.depotContainer = e.depotContainer;
		//this.warehouse = e.warehouse;
		//this.port = e.port;
		this.mooc = e.mooc;
		this.container = e.container;
		this.containerKep = e.containerKep;
		this.truck = e.truck;
		this.exportRequest = e.exportRequest;
		this.importRequest = e.importRequest;
		this.warehouseRequest = e.warehouseRequest;
		this.emptyContainerFromDepotRequest = e.emptyContainerFromDepotRequest;
		this.emptyContainerToDepotRequest = e.emptyContainerToDepotRequest;
		this.transportContainerRequest = e.transportContainerRequest;
		
	}
	public Truck getTruck() {
		return truck;
	}
	public void setTruck(Truck truck) {
		this.truck = truck;
	}
	public RouteElement(DepotTruck depotTruck, DepotMooc depotMooc,
			DepotContainer depotContainer, Warehouse warehouse, Port port,
			Mooc mooc, Container container, Container containerKep, String action,
			ExportContainerRequest exportRequest,
			ImportContainerRequest importRequest,
			WarehouseContainerTransportRequest warehouseRequest,
			String arrivalTime, String departureTime, double distance) {
		super();
		this.depotTruck = depotTruck;
		this.depotMooc = depotMooc;
		this.depotContainer = depotContainer;
		this.warehouse = warehouse;
		this.port = port;
		this.mooc = mooc;
		this.container = container;
		this.containerKep = containerKep;
		this.action = action;
		this.exportRequest = exportRequest;
		this.importRequest = importRequest;
		this.warehouseRequest = warehouseRequest;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.distance = distance;
	}
	public Mooc getMooc() {
		return mooc;
	}
	public void setMooc(Mooc mooc) {
		this.mooc = mooc;
	}
	public Container getContainer() {
		return container;
	}
	public void setContainer(Container container) {
		this.container = container;
	}
	public Container getContainerKep() {
		return containerKep;
	}
	public void setContainerKep(Container containerKep) {
		this.containerKep = containerKep;
	}
	public RouteElement(DepotTruck depotTruck, DepotMooc depotMooc,
			DepotContainer depotContainer, Warehouse warehouse, Port port,
			String action, ExportContainerRequest exportRequest,
			ImportContainerRequest importRequest,
			WarehouseContainerTransportRequest warehouseRequest,
			String arrivalTime, String departureTime, double distance) {
		super();
		this.depotTruck = depotTruck;
		this.depotMooc = depotMooc;
		this.depotContainer = depotContainer;
		this.warehouse = warehouse;
		this.port = port;
		this.action = action;
		this.exportRequest = exportRequest;
		this.importRequest = importRequest;
		this.warehouseRequest = warehouseRequest;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.distance = distance;
	}
	public ExportContainerRequest getExportRequest() {
		return exportRequest;
	}
	public void setExportRequest(ExportContainerRequest exportRequest) {
		this.exportRequest = exportRequest;
	}
	public ImportContainerRequest getImportRequest() {
		return importRequest;
	}
	public void setImportRequest(ImportContainerRequest importRequest) {
		this.importRequest = importRequest;
	}
	public WarehouseContainerTransportRequest getWarehouseRequest() {
		return warehouseRequest;
	}
	public void setWarehouseRequest(
			WarehouseContainerTransportRequest warehouseRequest) {
		this.warehouseRequest = warehouseRequest;
	}
	public ExportContainerRequest getExportRequestKep() {
		return exportRequestKep;
	}
	public void setExportRequestKep(ExportContainerRequest exportRequestKep) {
		this.exportRequestKep = exportRequestKep;
	}
	public ImportContainerRequest getImportRequestKep() {
		return importRequestKep;
	}
	public void setImportRequestKep(ImportContainerRequest importRequestKep) {
		this.importRequestKep = importRequestKep;
	}
	public WarehouseContainerTransportRequest getWarehouseRequestKep() {
		return warehouseRequestKep;
	}
	public void setWarehouseRequestKep(
			WarehouseContainerTransportRequest warehouseRequestKep) {
		this.warehouseRequestKep = warehouseRequestKep;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public String getLocationCode(){
		/*
		if(depotTruck != null) return depotTruck.getLocationCode();
		else if(depotMooc != null) return depotMooc.getLocationCode();
		else if(depotContainer != null) return depotContainer.getLocationCode();
		else if(warehouse != null) return warehouse.getLocationCode();
		else if(port != null) return port.getLocationCode();
		return null;
		*/
		return locationCode;
	}
	public DepotTruck getDepotTruck() {
		return depotTruck;
	}
	public void setDepotTruck(DepotTruck depotTruck) {
		this.depotTruck = depotTruck;
		locationCode = depotTruck.getLocationCode();
	}
	public DepotMooc getDepotMooc() {
		return depotMooc;
	}
	public void setDepotMooc(DepotMooc depotMooc) {
		this.depotMooc = depotMooc;
		locationCode = depotMooc.getLocationCode();
	}
	public DepotContainer getDepotContainer() {
		return depotContainer;
	}
	public void setDepotContainer(DepotContainer depotContainer) {
		this.depotContainer = depotContainer;
		locationCode = depotContainer.getLocationCode();
	}
	public Warehouse getWarehouse() {
		return warehouse;
	}
	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
		locationCode = warehouse.getLocationCode();
	}
	public Port getPort() {
		return port;
	}
	public void setPort(Port port) {
		this.port = port;
		locationCode = port.getLocationCode();
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(String arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public String getDepartureTime() {
		return departureTime;
	}
	public void setDepartureTime(String departureTime) {
		this.departureTime = departureTime;
	}
	public String getEarliestArrivalTime() {
		return earliestArrivalTime;
	}
	public void setEarliestArrivalTime(String earliestArrivalTime) {
		this.earliestArrivalTime = earliestArrivalTime;
	}
	public String getLatestArrivalTime() {
		return latestArrivalTime;
	}
	public void setLatestArrivalTime(String latestArrivalTime) {
		this.latestArrivalTime = latestArrivalTime;
	}
	public int getServiceDuration() {
		return serviceDuration;
	}
	public void setServiceDuration(int serviceDuration) {
		this.serviceDuration = serviceDuration;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public boolean isKep() {
		return kep;
	}
	public void setKepValue(boolean kep) {
		this.kep = kep;
	}
	public RouteElement() {
		super();
		// TODO Auto-generated constructor stub
		depotTruck = null;
		depotMooc = null;
		depotContainer = null;
		warehouse = null;
		port = null;
	}
	public RouteElement(DepotTruck depotTruck, DepotMooc depotMooc,
			DepotContainer depotContainer, Warehouse warehouse, Port port,
			String action, String arrivalTime, String departureTime) {
		super();
		this.depotTruck = depotTruck;
		this.depotMooc = depotMooc;
		this.depotContainer = depotContainer;
		this.warehouse = warehouse;
		this.port = port;
		this.action = action;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
	}
	
	
	
}
