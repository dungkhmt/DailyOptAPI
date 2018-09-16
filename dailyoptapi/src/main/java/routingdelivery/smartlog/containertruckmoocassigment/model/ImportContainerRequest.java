package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ImportContainerRequest {
	private String orderItemID;
	
	// 1st segment (Port -> warehouse, Laden)
	private String shipCompanyCode;
	private String[] depotContainerCode;
	private String containerCategory;// 20, 40, 45
	private String containerCode;
	private double weight;
	private String portCode;
	private String earlyDateTimePickupAtPort;
	private String lateDateTimePickupAtPort;
	private int loadDuration;
	
	
	//private String wareHouseCode;
	//private String earlyDateTimeUnloadAtWarehouse;
	//private String lateDateTimeUnloadAtWarehouse;
	//private int unloadDuration;
	//private int detachLoadedMoocContainerDuration;
	
	// 2nd segment (Warehouse -> depot, Empty)
	//private String earlyPickupEmptyContainerAtWarehouse;
	//private String latePickupEmptyContainerAtWarehouse;
	//private int attachEmptyMoocContainerDuration;
	private DeliveryWarehouseInfo[] deliveryWarehouses;
	
	
	private String earlyDateTimeDeliveryAtDepot;
	private String lateDateTimeDeliveryAtDepot;
	
	//private String levelRequest;// "1": only 1st requests, "2": only 2nd request, "12": both 1st and 2nd requests
	
	

	public ImportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ImportContainerRequest(String orderItemID, String shipCompanyCode,
			String[] depotContainerCode, String containerCategory,
			String containerCode, double weight, String portCode,
			String earlyDateTimePickupAtPort, String lateDateTimePickupAtPort,
			int loadDuration, DeliveryWarehouseInfo[] deliveryWarehouses,
			String earlyDateTimeDeliveryAtDepot,
			String lateDateTimeDeliveryAtDepot) {
		super();
		this.orderItemID = orderItemID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotContainerCode = depotContainerCode;
		this.containerCategory = containerCategory;
		this.containerCode = containerCode;
		this.weight = weight;
		this.portCode = portCode;
		this.earlyDateTimePickupAtPort = earlyDateTimePickupAtPort;
		this.lateDateTimePickupAtPort = lateDateTimePickupAtPort;
		this.loadDuration = loadDuration;
		this.deliveryWarehouses = deliveryWarehouses;
		this.earlyDateTimeDeliveryAtDepot = earlyDateTimeDeliveryAtDepot;
		this.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
	}

	public String getOrderItemID() {
		return orderItemID;
	}

	public void setOrderItemID(String orderItemID) {
		this.orderItemID = orderItemID;
	}

	public String getShipCompanyCode() {
		return shipCompanyCode;
	}

	public void setShipCompanyCode(String shipCompanyCode) {
		this.shipCompanyCode = shipCompanyCode;
	}

	public String[] getDepotContainerCode() {
		return depotContainerCode;
	}

	public void setDepotContainerCode(String[] depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}

	public String getContainerCategory() {
		return containerCategory;
	}

	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
	}

	public String getContainerCode() {
		return containerCode;
	}

	public void setContainerCode(String containerCode) {
		this.containerCode = containerCode;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public String getPortCode() {
		return portCode;
	}

	public void setPortCode(String portCode) {
		this.portCode = portCode;
	}

	public String getEarlyDateTimePickupAtPort() {
		return earlyDateTimePickupAtPort;
	}

	public void setEarlyDateTimePickupAtPort(String earlyDateTimePickupAtPort) {
		this.earlyDateTimePickupAtPort = earlyDateTimePickupAtPort;
	}

	public String getLateDateTimePickupAtPort() {
		return lateDateTimePickupAtPort;
	}

	public void setLateDateTimePickupAtPort(String lateDateTimePickupAtPort) {
		this.lateDateTimePickupAtPort = lateDateTimePickupAtPort;
	}

	public int getLoadDuration() {
		return loadDuration;
	}

	public void setLoadDuration(int loadDuration) {
		this.loadDuration = loadDuration;
	}

	public DeliveryWarehouseInfo[] getDeliveryWarehouses() {
		return deliveryWarehouses;
	}

	public void setDeliveryWarehouses(DeliveryWarehouseInfo[] deliveryWarehouses) {
		this.deliveryWarehouses = deliveryWarehouses;
	}

	public String getEarlyDateTimeDeliveryAtDepot() {
		return earlyDateTimeDeliveryAtDepot;
	}

	public void setEarlyDateTimeDeliveryAtDepot(String earlyDateTimeDeliveryAtDepot) {
		this.earlyDateTimeDeliveryAtDepot = earlyDateTimeDeliveryAtDepot;
	}

	public String getLateDateTimeDeliveryAtDepot() {
		return lateDateTimeDeliveryAtDepot;
	}

	public void setLateDateTimeDeliveryAtDepot(String lateDateTimeDeliveryAtDepot) {
		this.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
	}
	
	
	
}
