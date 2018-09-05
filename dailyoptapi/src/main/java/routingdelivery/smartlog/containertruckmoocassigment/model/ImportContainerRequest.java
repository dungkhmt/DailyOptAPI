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
	
	
	private String wareHouseCode;
	private String earlyDateTimeUnloadAtWarehouse;
	private String lateDateTimeUnloadAtWarehouse;
	private int unloadDuration;
	private int detachLoadedMoocContainerDuration;
	
	// 2nd segment (Warehouse -> depot, Empty)
	private String earlyPickupEmptyContainerAtWarehouse;
	private String latePickupEmptyContainerAtWarehouse;
	private int attachEmptyMoocContainerDuration;
	
	private String earlyDateTimeDeliveryAtDepot;
	private String lateDateTimeDeliveryAtDepot;
	
	private String levelRequest;// "1": only 1st requests, "2": only 2nd request, "12": both 1st and 2nd requests
	
	
	public ImportContainerRequest(String orderItemID, String shipCompanyCode,
			String[] depotContainerCode, String containerCategory,
			String containerCode, double weight, String portCode,
			String earlyDateTimePickupAtPort, String lateDateTimePickupAtPort,
			int loadDuration, String wareHouseCode,
			String earlyDateTimeUnloadAtWarehouse,
			String lateDateTimeUnloadAtWarehouse, int unloadDuration,
			int detachLoadedMoocContainerDuration,
			String earlyPickupEmptyContainerAtWarehouse,
			String latePickupEmptyContainerAtWarehouse,
			int attachEmptyMoocContainerDuration,
			String earlyDateTimeDeliveryAtDepot,
			String lateDateTimeDeliveryAtDepot, String levelRequest) {
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
		this.wareHouseCode = wareHouseCode;
		this.earlyDateTimeUnloadAtWarehouse = earlyDateTimeUnloadAtWarehouse;
		this.lateDateTimeUnloadAtWarehouse = lateDateTimeUnloadAtWarehouse;
		this.unloadDuration = unloadDuration;
		this.detachLoadedMoocContainerDuration = detachLoadedMoocContainerDuration;
		this.earlyPickupEmptyContainerAtWarehouse = earlyPickupEmptyContainerAtWarehouse;
		this.latePickupEmptyContainerAtWarehouse = latePickupEmptyContainerAtWarehouse;
		this.attachEmptyMoocContainerDuration = attachEmptyMoocContainerDuration;
		this.earlyDateTimeDeliveryAtDepot = earlyDateTimeDeliveryAtDepot;
		this.lateDateTimeDeliveryAtDepot = lateDateTimeDeliveryAtDepot;
		this.levelRequest = levelRequest;
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


	public int getDetachLoadedMoocContainerDuration() {
		return detachLoadedMoocContainerDuration;
	}


	public void setDetachLoadedMoocContainerDuration(
			int detachLoadedMoocContainerDuration) {
		this.detachLoadedMoocContainerDuration = detachLoadedMoocContainerDuration;
	}


	public String getEarlyPickupEmptyContainerAtWarehouse() {
		return earlyPickupEmptyContainerAtWarehouse;
	}


	public void setEarlyPickupEmptyContainerAtWarehouse(
			String earlyPickupEmptyContainerAtWarehouse) {
		this.earlyPickupEmptyContainerAtWarehouse = earlyPickupEmptyContainerAtWarehouse;
	}


	public String getLatePickupEmptyContainerAtWarehouse() {
		return latePickupEmptyContainerAtWarehouse;
	}


	public void setLatePickupEmptyContainerAtWarehouse(
			String latePickupEmptyContainerAtWarehouse) {
		this.latePickupEmptyContainerAtWarehouse = latePickupEmptyContainerAtWarehouse;
	}


	public int getAttachEmptyMoocContainerDuration() {
		return attachEmptyMoocContainerDuration;
	}


	public void setAttachEmptyMoocContainerDuration(
			int attachEmptyMoocContainerDuration) {
		this.attachEmptyMoocContainerDuration = attachEmptyMoocContainerDuration;
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


	public String getLevelRequest() {
		return levelRequest;
	}


	public void setLevelRequest(String levelRequest) {
		this.levelRequest = levelRequest;
	}


	public ImportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
