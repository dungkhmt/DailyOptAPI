package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ExportContainerRequest {
	private String orderItemID;
	
	private String shipCompanyCode;
	private String depotContainerCode;// depotContainer
	private String containerCategory;// 20, 40, 45
	private double weight;
	private String earlyDateTimePickupAtDepot;
	private String lateDateTimePickupAtDepot;
	
	private String wareHouseCode;
	private String earlyDateTimeLoadAtWarehouse;
	private String lateDateTimeLoadAtWarehouse;
	private int loadDuration;
	private int detachEmptyMoocContainerDuration;
	
	private String earlyDateTimePickupLoadedContainerAtWarehouse;
	private String lateDateTimePickupLoadedContainerAtWarehouse;
	private int attachLoadedMoocContainerDuration;
	
	private String portCode;
	private String earlyDateTimeUnloadAtPort;
	private String lateDateTimeUnloadAtPort;
	private int unloadDuration;
	
	private String planSegment;// "1","2","12","13","123",...
	
	
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
	public int getDetachEmptyMoocContainerDuration() {
		return detachEmptyMoocContainerDuration;
	}
	public void setDetachEmptyMoocContainerDuration(
			int detachEmptyMoocContainerDuration) {
		this.detachEmptyMoocContainerDuration = detachEmptyMoocContainerDuration;
	}
	public String getEarlyDateTimePickupLoadedContainerAtWarehouse() {
		return earlyDateTimePickupLoadedContainerAtWarehouse;
	}
	public void setEarlyDateTimePickupLoadedContainerAtWarehouse(
			String earlyDateTimePickupLoadedContainerAtWarehouse) {
		this.earlyDateTimePickupLoadedContainerAtWarehouse = earlyDateTimePickupLoadedContainerAtWarehouse;
	}
	public String getLateDateTimePickupLoadedContainerAtWarehouse() {
		return lateDateTimePickupLoadedContainerAtWarehouse;
	}
	public void setLateDateTimePickupLoadedContainerAtWarehouse(
			String lateDateTimePickupLoadedContainerAtWarehouse) {
		this.lateDateTimePickupLoadedContainerAtWarehouse = lateDateTimePickupLoadedContainerAtWarehouse;
	}
	public int getAttachLoadedMoocContainerDuration() {
		return attachLoadedMoocContainerDuration;
	}
	public void setAttachLoadedMoocContainerDuration(
			int attachLoadedMoocContainerDuration) {
		this.attachLoadedMoocContainerDuration = attachLoadedMoocContainerDuration;
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
	public ExportContainerRequest(String orderItemID, String shipCompanyCode,
			String depotContainerCode, String containerCategory, double weight,
			String earlyDateTimePickupAtDepot,
			String lateDateTimePickupAtDepot, String wareHouseCode,
			String earlyDateTimeLoadAtWarehouse,
			String lateDateTimeLoadAtWarehouse, int loadDuration,
			int detachEmptyMoocContainerDuration,
			String earlyDateTimePickupLoadedContainerAtWarehouse,
			String lateDateTimePickupLoadedContainerAtWarehouse,
			int attachLoadedMoocContainerDuration, String portCode,
			String earlyDateTimeUnloadAtPort, String lateDateTimeUnloadAtPort,
			int unloadDuration) {
		super();
		this.orderItemID = orderItemID;
		this.shipCompanyCode = shipCompanyCode;
		this.depotContainerCode = depotContainerCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.earlyDateTimePickupAtDepot = earlyDateTimePickupAtDepot;
		this.lateDateTimePickupAtDepot = lateDateTimePickupAtDepot;
		this.wareHouseCode = wareHouseCode;
		this.earlyDateTimeLoadAtWarehouse = earlyDateTimeLoadAtWarehouse;
		this.lateDateTimeLoadAtWarehouse = lateDateTimeLoadAtWarehouse;
		this.loadDuration = loadDuration;
		this.detachEmptyMoocContainerDuration = detachEmptyMoocContainerDuration;
		this.earlyDateTimePickupLoadedContainerAtWarehouse = earlyDateTimePickupLoadedContainerAtWarehouse;
		this.lateDateTimePickupLoadedContainerAtWarehouse = lateDateTimePickupLoadedContainerAtWarehouse;
		this.attachLoadedMoocContainerDuration = attachLoadedMoocContainerDuration;
		this.portCode = portCode;
		this.earlyDateTimeUnloadAtPort = earlyDateTimeUnloadAtPort;
		this.lateDateTimeUnloadAtPort = lateDateTimeUnloadAtPort;
		this.unloadDuration = unloadDuration;
	}
	public ExportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
	
}
