package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ImportContainerRequest {
	private String orderItemID;
	
	private String shipCompanyCode;
	private String depotContainerCode;
	private String containerCategory;// 20, 40, 45
	private double weight;
	private String portCode;
	private String earlyDateTimePickupAtPort;
	private String lateDateTimePickupAtPort;
	private int loadDuration;
	
	private String wareHouseCode;
	private String earlyDateTimeUnloadAtWarehouse;
	private String lateDateTimeUnloadAtWarehouse;
	private int unloadDuration;
	
	private String earlyDateTimeDeliveryAtDepot;
	private String lateDateTimeDeliveryAtDepot;
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
	
	public String getDepotContainerCode() {
		return depotContainerCode;
	}
	public void setDepotContainerCode(String depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}
	public String getContainerCategory() {
		return containerCategory;
	}
	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
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
	public String getWareHouseCode() {
		return wareHouseCode;
	}
	public void setWareHouseCode(String wareHouseCode) {
		this.wareHouseCode = wareHouseCode;
	}
	public String getEarlyDateTimeUnloadAtWarehouse() {
		return earlyDateTimeUnloadAtWarehouse;
	}
	public void setEarlyDateTimeUnloadAtWarehouse(
			String earlyDateTimeUnloadAtWarehouse) {
		this.earlyDateTimeUnloadAtWarehouse = earlyDateTimeUnloadAtWarehouse;
	}
	public String getLateDateTimeUnloadAtWarehouse() {
		return lateDateTimeUnloadAtWarehouse;
	}
	public void setLateDateTimeUnloadAtWarehouse(
			String lateDateTimeUnloadAtWarehouse) {
		this.lateDateTimeUnloadAtWarehouse = lateDateTimeUnloadAtWarehouse;
	}
	public int getUnloadDuration() {
		return unloadDuration;
	}
	public void setUnloadDuration(int unloadDuration) {
		this.unloadDuration = unloadDuration;
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

	
	public ImportContainerRequest(String orderItemID, String shipCompanyCode,
			String depotContainerCode, String containerCategory, double weight,
			String portCode, String earlyDateTimePickupAtPort,
			String lateDateTimePickupAtPort, int loadDuration,
			String wareHouseCode, String earlyDateTimeUnloadAtWarehouse,
			String lateDateTimeUnloadAtWarehouse, int unloadDuration,
			String earlyDateTimeDeliveryAtDepot,
			String lateDateTimeDeliveryAtDepot) {
		super();
		this.orderItemID = orderItemID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotContainerCode = depotContainerCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.portCode = portCode;
		this.earlyDateTimePickupAtPort = earlyDateTimePickupAtPort;
		this.lateDateTimePickupAtPort = lateDateTimePickupAtPort;
		this.loadDuration = loadDuration;
		this.wareHouseCode = wareHouseCode;
		this.earlyDateTimeUnloadAtWarehouse = earlyDateTimeUnloadAtWarehouse;
		this.lateDateTimeUnloadAtWarehouse = lateDateTimeUnloadAtWarehouse;
		this.unloadDuration = unloadDuration;
		this.earlyDateTimeDeliveryAtDepot = earlyDateTimeDeliveryAtDepot;
		this.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
	}
	public ImportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
