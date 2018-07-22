package routingdelivery.smartlog.containertruckmoocassigment.model;

public class RouteElement {
	private DepotTruck depotTruck;
	private DepotMooc depotMooc;
	private DepotContainer depotContainer;
	private Warehouse warehouse;
	private Port port;
	private Mooc mooc;
	private Container container;
	private Truck truck;
	
	private String action;// LAY_MOOC, TRA_MOOC, LAY_CONTAINER, TRA_CONTAINER, 
						//   LOAD_HANG, UNLOAD_HANG, CAT_MOOC, 
						// 
	private ExportContainerRequest exportRequest;
	private ImportContainerRequest importRequest;
	private WarehouseContainerTransportRequest warehouseRequest;
	
	private String arrivalTime;
	private String departureTime;
	private double distance;
	
	public void deriveFrom(RouteElement e){
		//this.depotTruck = e.depotTruck;
		//this.depotMooc = e.depotMooc;
		//this.depotContainer = e.depotContainer;
		//this.warehouse = e.warehouse;
		//this.port = e.port;
		this.mooc = e.mooc;
		this.container = e.container;
		this.truck = e.truck;
		this.exportRequest = e.exportRequest;
		this.importRequest = e.importRequest;
		this.warehouseRequest = e.warehouseRequest;
		
	}
	public Truck getTruck() {
		return truck;
	}
	public void setTruck(Truck truck) {
		this.truck = truck;
	}
	public RouteElement(DepotTruck depotTruck, DepotMooc depotMooc,
			DepotContainer depotContainer, Warehouse warehouse, Port port,
			Mooc mooc, Container container, String action,
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
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public String getLocationCode(){
		if(depotTruck != null) return depotTruck.getLocationCode();
		else if(depotMooc != null) return depotMooc.getLocationCode();
		else if(depotContainer != null) return depotContainer.getLocationCode();
		else if(warehouse != null) return warehouse.getLocationCode();
		else if(port != null) return port.getLocationCode();
		return null;
	}
	public DepotTruck getDepotTruck() {
		return depotTruck;
	}
	public void setDepotTruck(DepotTruck depotTruck) {
		this.depotTruck = depotTruck;
	}
	public DepotMooc getDepotMooc() {
		return depotMooc;
	}
	public void setDepotMooc(DepotMooc depotMooc) {
		this.depotMooc = depotMooc;
	}
	public DepotContainer getDepotContainer() {
		return depotContainer;
	}
	public void setDepotContainer(DepotContainer depotContainer) {
		this.depotContainer = depotContainer;
	}
	public Warehouse getWarehouse() {
		return warehouse;
	}
	public void setWarehouse(Warehouse warehouse) {
		this.warehouse = warehouse;
	}
	public Port getPort() {
		return port;
	}
	public void setPort(Port port) {
		this.port = port;
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
