package routingdelivery.smartlog.containertruckmoocassigment.model;

public class WarehouseContainerTransportRequest {
	private String orderItemID;
	
	private String containerCategory;
	private double weight;
	
	private String shipCompanyCode;
	private String fromWarehouseCode;
	private String earlyDateTimeLoad;
	private String lateDateTimeLoad;
	private int loadDuration;
	private int detachEmptyMoocContainerDurationFromWarehouse;
	
	private String earlyDateTimePickupLoadedContainerFromWarehouse;
	private String lateDateTimePickupLoadedContainerFromWarehouse;
	private int attachLoadedMoocContainerDurationFromWarehouse;
	
	private String toWarehouseCode;
	private String earlyDateTimeUnload;
	private String lateDateTimeUnload;
	private int unloadDuration;
	private int detachLoadedMoocContainerDurationToWarehouse;
	
	private String earlyDateTimePickupEmptyContainerToWarehouse;
	private String lateDateTimePickupEmptyContainerToWarehouse;
	private int attachEmptyMoocContainerDurationToWarehouse;
	public String getOrderItemID() {
		return orderItemID;
	}
	public void setOrderItemID(String orderItemID) {
		this.orderItemID = orderItemID;
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
	public String getShipCompanyCode() {
		return shipCompanyCode;
	}
	public void setShipCompanyCode(String shipCompanyCode) {
		this.shipCompanyCode = shipCompanyCode;
	}
	public String getFromWarehouseCode() {
		return fromWarehouseCode;
	}
	public void setFromWarehouseCode(String fromWarehouseCode) {
		this.fromWarehouseCode = fromWarehouseCode;
	}
	public String getEarlyDateTimeLoad() {
		return earlyDateTimeLoad;
	}
	public void setEarlyDateTimeLoad(String earlyDateTimeLoad) {
		this.earlyDateTimeLoad = earlyDateTimeLoad;
	}
	public String getLateDateTimeLoad() {
		return lateDateTimeLoad;
	}
	public void setLateDateTimeLoad(String lateDateTimeLoad) {
		this.lateDateTimeLoad = lateDateTimeLoad;
	}
	public int getLoadDuration() {
		return loadDuration;
	}
	public void setLoadDuration(int loadDuration) {
		this.loadDuration = loadDuration;
	}
	public int getDetachEmptyMoocContainerDurationFromWarehouse() {
		return detachEmptyMoocContainerDurationFromWarehouse;
	}
	public void setDetachEmptyMoocContainerDurationFromWarehouse(
			int detachEmptyMoocContainerDurationFromWarehouse) {
		this.detachEmptyMoocContainerDurationFromWarehouse = detachEmptyMoocContainerDurationFromWarehouse;
	}
	public String getEarlyDateTimePickupLoadedContainerFromWarehouse() {
		return earlyDateTimePickupLoadedContainerFromWarehouse;
	}
	public void setEarlyDateTimePickupLoadedContainerFromWarehouse(
			String earlyDateTimePickupLoadedContainerFromWarehouse) {
		this.earlyDateTimePickupLoadedContainerFromWarehouse = earlyDateTimePickupLoadedContainerFromWarehouse;
	}
	public String getLateDateTimePickupLoadedContainerFromWarehouse() {
		return lateDateTimePickupLoadedContainerFromWarehouse;
	}
	public void setLateDateTimePickupLoadedContainerFromWarehouse(
			String lateDateTimePickupLoadedContainerFromWarehouse) {
		this.lateDateTimePickupLoadedContainerFromWarehouse = lateDateTimePickupLoadedContainerFromWarehouse;
	}
	public int getAttachLoadedMoocContainerDurationFromWarehouse() {
		return attachLoadedMoocContainerDurationFromWarehouse;
	}
	public void setAttachLoadedMoocContainerDurationFromWarehouse(
			int attachLoadedMoocContainerDurationFromWarehouse) {
		this.attachLoadedMoocContainerDurationFromWarehouse = attachLoadedMoocContainerDurationFromWarehouse;
	}
	public String getToWarehouseCode() {
		return toWarehouseCode;
	}
	public void setToWarehouseCode(String toWarehouseCode) {
		this.toWarehouseCode = toWarehouseCode;
	}
	public String getEarlyDateTimeUnload() {
		return earlyDateTimeUnload;
	}
	public void setEarlyDateTimeUnload(String earlyDateTimeUnload) {
		this.earlyDateTimeUnload = earlyDateTimeUnload;
	}
	public String getLateDateTimeUnload() {
		return lateDateTimeUnload;
	}
	public void setLateDateTimeUnload(String lateDateTimeUnload) {
		this.lateDateTimeUnload = lateDateTimeUnload;
	}
	public int getUnloadDuration() {
		return unloadDuration;
	}
	public void setUnloadDuration(int unloadDuration) {
		this.unloadDuration = unloadDuration;
	}
	public int getDetachLoadedMoocContainerDurationToWarehouse() {
		return detachLoadedMoocContainerDurationToWarehouse;
	}
	public void setDetachLoadedMoocContainerDurationToWarehouse(
			int detachLoadedMoocContainerDurationToWarehouse) {
		this.detachLoadedMoocContainerDurationToWarehouse = detachLoadedMoocContainerDurationToWarehouse;
	}
	public String getEarlyDateTimePickupEmptyContainerToWarehouse() {
		return earlyDateTimePickupEmptyContainerToWarehouse;
	}
	public void setEarlyDateTimePickupEmptyContainerToWarehouse(
			String earlyDateTimePickupEmptyContainerToWarehouse) {
		this.earlyDateTimePickupEmptyContainerToWarehouse = earlyDateTimePickupEmptyContainerToWarehouse;
	}
	public String getLateDateTimePickupEmptyContainerToWarehouse() {
		return lateDateTimePickupEmptyContainerToWarehouse;
	}
	public void setLateDateTimePickupEmptyContainerToWarehouse(
			String lateDateTimePickupEmptyContainerToWarehouse) {
		this.lateDateTimePickupEmptyContainerToWarehouse = lateDateTimePickupEmptyContainerToWarehouse;
	}
	public int getAttachEmptyMoocContainerDurationToWarehouse() {
		return attachEmptyMoocContainerDurationToWarehouse;
	}
	public void setAttachEmptyMoocContainerDurationToWarehouse(
			int attachEmptyMoocContainerDurationToWarehouse) {
		this.attachEmptyMoocContainerDurationToWarehouse = attachEmptyMoocContainerDurationToWarehouse;
	}
	public WarehouseContainerTransportRequest(String orderItemID,
			String containerCategory, double weight, String shipCompanyCode,
			String fromWarehouseCode, String earlyDateTimeLoad,
			String lateDateTimeLoad, int loadDuration,
			int detachEmptyMoocContainerDurationFromWarehouse,
			String earlyDateTimePickupLoadedContainerFromWarehouse,
			String lateDateTimePickupLoadedContainerFromWarehouse,
			int attachLoadedMoocContainerDurationFromWarehouse,
			String toWarehouseCode, String earlyDateTimeUnload,
			String lateDateTimeUnload, int unloadDuration,
			int detachLoadedMoocContainerDurationToWarehouse,
			String earlyDateTimePickupEmptyContainerToWarehouse,
			String lateDateTimePickupEmptyContainerToWarehouse,
			int attachEmptyMoocContainerDurationToWarehouse) {
		super();
		this.orderItemID = orderItemID;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.shipCompanyCode = shipCompanyCode;
		this.fromWarehouseCode = fromWarehouseCode;
		this.earlyDateTimeLoad = earlyDateTimeLoad;
		this.lateDateTimeLoad = lateDateTimeLoad;
		this.loadDuration = loadDuration;
		this.detachEmptyMoocContainerDurationFromWarehouse = detachEmptyMoocContainerDurationFromWarehouse;
		this.earlyDateTimePickupLoadedContainerFromWarehouse = earlyDateTimePickupLoadedContainerFromWarehouse;
		this.lateDateTimePickupLoadedContainerFromWarehouse = lateDateTimePickupLoadedContainerFromWarehouse;
		this.attachLoadedMoocContainerDurationFromWarehouse = attachLoadedMoocContainerDurationFromWarehouse;
		this.toWarehouseCode = toWarehouseCode;
		this.earlyDateTimeUnload = earlyDateTimeUnload;
		this.lateDateTimeUnload = lateDateTimeUnload;
		this.unloadDuration = unloadDuration;
		this.detachLoadedMoocContainerDurationToWarehouse = detachLoadedMoocContainerDurationToWarehouse;
		this.earlyDateTimePickupEmptyContainerToWarehouse = earlyDateTimePickupEmptyContainerToWarehouse;
		this.lateDateTimePickupEmptyContainerToWarehouse = lateDateTimePickupEmptyContainerToWarehouse;
		this.attachEmptyMoocContainerDurationToWarehouse = attachEmptyMoocContainerDurationToWarehouse;
	}
	public WarehouseContainerTransportRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
