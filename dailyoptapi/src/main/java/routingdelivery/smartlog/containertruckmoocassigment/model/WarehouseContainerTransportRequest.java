package routingdelivery.smartlog.containertruckmoocassigment.model;

public class WarehouseContainerTransportRequest {
	private String orderItemID;
	
	// 1st segment (Depot to warehouse: empty)
	private String containerCategory;
	private double weight;
	
	private String shipCompanyCode;
	private String fromWarehouseCode;
	private String earlyDateTimeLoad;
	private String lateDateTimeLoad;
	private int loadDuration;
	private int detachEmptyMoocContainerDurationFromWarehouse;
	
	// 2nd segment (warehouse 1 -> warehouse 2: laden)
	private String earlyDateTimePickupLoadedContainerFromWarehouse;
	private String lateDateTimePickupLoadedContainerFromWarehouse;
	private int attachLoadedMoocContainerDurationFromWarehouse;
	
	private String toWarehouseCode;
	private String earlyDateTimeUnload;
	private String lateDateTimeUnload;
	private int unloadDuration;
	private int detachLoadedMoocContainerDurationToWarehouse;
	
	// 3th segment (warehouse -> depot: empty)
	private String earlyDateTimePickupEmptyContainerToWarehouse;
	private String lateDateTimePickupEmptyContainerToWarehouse;
	private int attachEmptyMoocContainerDurationToWarehouse;
	private String[] returnDepotContainerCodes;
	private String customerCode;
	
	private String levelRequest;// "1": only 1st, "2": only 2nd segment, "3": only 3th segment
								// "12": only 1st and 2nd, "23": only 2nd and 3th segments
								// "123": both 1st, 2nd and 3th segments
	

	public WarehouseContainerTransportRequest() {
		super();
		// TODO Auto-generated constructor stub
	}


	

	public WarehouseContainerTransportRequest(
										String orderItemID,
										String containerCategory,
										double weight,
										String shipCompanyCode,
										String fromWarehouseCode,
										String earlyDateTimeLoad,
										String lateDateTimeLoad,
										int loadDuration,
										int detachEmptyMoocContainerDurationFromWarehouse,
										String earlyDateTimePickupLoadedContainerFromWarehouse,
										String lateDateTimePickupLoadedContainerFromWarehouse,
										int attachLoadedMoocContainerDurationFromWarehouse,
										String toWarehouseCode,
										String earlyDateTimeUnload,
										String lateDateTimeUnload,
										int unloadDuration,
										int detachLoadedMoocContainerDurationToWarehouse,
										String earlyDateTimePickupEmptyContainerToWarehouse,
										String lateDateTimePickupEmptyContainerToWarehouse,
										int attachEmptyMoocContainerDurationToWarehouse,
										String[] returnDepotContainerCodes,
										String levelRequest,
										String customerCode) {
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
									this.returnDepotContainerCodes = returnDepotContainerCodes;
									this.levelRequest = levelRequest;
									this.customerCode = customerCode;
								}




	public String[] getReturnDepotContainerCodes() {
		return returnDepotContainerCodes;
	}




	public void setReturnDepotContainerCodes(String[] returnDepotContainerCodes) {
		this.returnDepotContainerCodes = returnDepotContainerCodes;
	}




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


	public String getLevelRequest() {
		return levelRequest;
	}


	public void setLevelRequest(String levelRequest) {
		this.levelRequest = levelRequest;
	}
	
	public String getCustomerCode() {
		return customerCode;
	}
	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}
	
	
	
}
