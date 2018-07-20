package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ExportContainerRequest {
	private String orderItemID;
	
	private String shipCompanyCode;
	private String depotCode;
	private String containerCategory;// 20, 40, 45
	private double weight;
	private String earlyDateTimePickupAtDepot;
	private String lateDateTimePickupAtDepot;
	
	private String wareHouseCode;
	private String earlyDateTimeLoadAtWarehouse;
	private String lateDateTimeLoadAtWarehouse;
	private int loadDuration = 3600;
	
	private String portCode;
	private String earlyDateTimeUnloadAtPort;
	private String lateDateTimeUnloadAtPort;
	private int unloadDuration = 3600;
	public ExportContainerRequest(String orderItemID, String shipCompanyCode,
			String depotCode, String containerCategory, double weight,
			String earlyDateTimePickupAtDepot,
			String lateDateTimePickupAtDepot, String wareHouseCode,
			String earlyDateTimeLoadAtWarehouse,
			String lateDateTimeLoadAtWarehouse, int loadDuration,
			String portCode, String earlyDateTimeUnloadAtPort,
			String lateDateTimeUnloadAtPort, int unloadDuration) {
		super();
		this.orderItemID = orderItemID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotCode = depotCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.earlyDateTimePickupAtDepot = earlyDateTimePickupAtDepot;
		this.lateDateTimePickupAtDepot = lateDateTimePickupAtDepot;
		this.wareHouseCode = wareHouseCode;
		this.earlyDateTimeLoadAtWarehouse = earlyDateTimeLoadAtWarehouse;
		this.lateDateTimeLoadAtWarehouse = lateDateTimeLoadAtWarehouse;
		this.loadDuration = loadDuration;
		this.portCode = portCode;
		this.earlyDateTimeUnloadAtPort = earlyDateTimeUnloadAtPort;
		this.lateDateTimeUnloadAtPort = lateDateTimeUnloadAtPort;
		this.unloadDuration = unloadDuration;
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
	public String getDepotCode() {
		return depotCode;
	}
	public void setDepotCode(String depotCode) {
		this.depotCode = depotCode;
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
	public String getEarlyDateTimePickupAtDepot() {
		return earlyDateTimePickupAtDepot;
	}
	public void setEarlyDateTimePickupAtDepot(String earlyDateTimePickupAtDepot) {
		this.earlyDateTimePickupAtDepot = earlyDateTimePickupAtDepot;
	}
	public String getLateDateTimePickupAtDepot() {
		return lateDateTimePickupAtDepot;
	}
	public void setLateDateTimePickupAtDepot(String lateDateTimePickupAtDepot) {
		this.lateDateTimePickupAtDepot = lateDateTimePickupAtDepot;
	}
	public String getWareHouseCode() {
		return wareHouseCode;
	}
	public void setWareHouseCode(String wareHouseCode) {
		this.wareHouseCode = wareHouseCode;
	}
	public String getEarlyDateTimeLoadAtWarehouse() {
		return earlyDateTimeLoadAtWarehouse;
	}
	public void setEarlyDateTimeLoadAtWarehouse(String earlyDateTimeLoadAtWarehouse) {
		this.earlyDateTimeLoadAtWarehouse = earlyDateTimeLoadAtWarehouse;
	}
	public String getLateDateTimeLoadAtWarehouse() {
		return lateDateTimeLoadAtWarehouse;
	}
	public void setLateDateTimeLoadAtWarehouse(String lateDateTimeLoadAtWarehouse) {
		this.lateDateTimeLoadAtWarehouse = lateDateTimeLoadAtWarehouse;
	}
	public int getLoadDuration() {
		return loadDuration;
	}
	public void setLoadDuration(int loadDuration) {
		this.loadDuration = loadDuration;
	}
	public String getPortCode() {
		return portCode;
	}
	public void setPortCode(String portCode) {
		this.portCode = portCode;
	}
	public String getEarlyDateTimeUnloadAtPort() {
		return earlyDateTimeUnloadAtPort;
	}
	public void setEarlyDateTimeUnloadAtPort(String earlyDateTimeUnloadAtPort) {
		this.earlyDateTimeUnloadAtPort = earlyDateTimeUnloadAtPort;
	}
	public String getLateDateTimeUnloadAtPort() {
		return lateDateTimeUnloadAtPort;
	}
	public void setLateDateTimeUnloadAtPort(String lateDateTimeUnloadAtPort) {
		this.lateDateTimeUnloadAtPort = lateDateTimeUnloadAtPort;
	}
	public int getUnloadDuration() {
		return unloadDuration;
	}
	public void setUnloadDuration(int unloadDuration) {
		this.unloadDuration = unloadDuration;
	}
	
	
	
	
}
