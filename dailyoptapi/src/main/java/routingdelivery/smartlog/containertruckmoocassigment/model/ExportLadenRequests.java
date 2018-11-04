package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ExportLadenRequests {
	private boolean isBreakRomooc;
	private String containerCategory;
	private String containerNo;
	private String orderCode;
	private String customerCode;
	private String requestDate;
	private String lateDateTimeAttachAtWarehouse;
	private String earlyDateTimeUnloadAtPort;
	private String lateDateTimeUnloadAtPort;
	private String moocCode;
	private double weight;
	private String wareHouseCode;
	private String portCode;
	private int linkContainerAtWarehouseDuration;
	private int releaseLoadedContainerAtPortDuration;
	

	public ExportLadenRequests(boolean isBreakRomooc, String containerCategory,
			String containerNo, String orderCode, String customerCode,
			String requestDate, String lateDateTimeAttachAtWarehouse,
			String earlyDateTimeUnloadAtPort, String lateDateTimeUnloadAtPort,
			String moocCode, double weight, String wareHouseCode,
			String portCode, int linkContainerAtWarehouseDuration,
			int releaseLoadedContainerAtPortDuration) {
		super();
		this.isBreakRomooc = isBreakRomooc;
		this.containerCategory = containerCategory;
		this.containerNo = containerNo;
		this.orderCode = orderCode;
		this.customerCode = customerCode;
		this.requestDate = requestDate;
		this.lateDateTimeAttachAtWarehouse = lateDateTimeAttachAtWarehouse;
		this.earlyDateTimeUnloadAtPort = earlyDateTimeUnloadAtPort;
		this.lateDateTimeUnloadAtPort = lateDateTimeUnloadAtPort;
		this.moocCode = moocCode;
		this.weight = weight;
		this.wareHouseCode = wareHouseCode;
		this.portCode = portCode;
		this.linkContainerAtWarehouseDuration = linkContainerAtWarehouseDuration;
		this.releaseLoadedContainerAtPortDuration = releaseLoadedContainerAtPortDuration;
	}


	public boolean isBreakRomooc() {
		return isBreakRomooc;
	}


	public void setBreakRomooc(boolean isBreakRomooc) {
		this.isBreakRomooc = isBreakRomooc;
	}


	public String getContainerCategory() {
		return containerCategory;
	}


	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
	}


	public String getContainerNo() {
		return containerNo;
	}


	public void setContainerNo(String containerNo) {
		this.containerNo = containerNo;
	}


	public String getOrderCode() {
		return orderCode;
	}


	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}


	public String getCustomerCode() {
		return customerCode;
	}


	public void setCustomerCode(String customerCode) {
		this.customerCode = customerCode;
	}


	public String getRequestDate() {
		return requestDate;
	}


	public void setRequestDate(String requestDate) {
		this.requestDate = requestDate;
	}


	public String getLateDateTimeAttachAtWarehouse() {
		return lateDateTimeAttachAtWarehouse;
	}


	public void setLateDateTimeAttachAtWarehouse(
			String lateDateTimeAttachAtWarehouse) {
		this.lateDateTimeAttachAtWarehouse = lateDateTimeAttachAtWarehouse;
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


	public String getMoocCode() {
		return moocCode;
	}


	public void setMoocCode(String moocCode) {
		this.moocCode = moocCode;
	}


	public double getWeight() {
		return weight;
	}


	public void setWeight(double weight) {
		this.weight = weight;
	}


	public String getWareHouseCode() {
		return wareHouseCode;
	}


	public void setWareHouseCode(String wareHouseCode) {
		this.wareHouseCode = wareHouseCode;
	}


	public String getPortCode() {
		return portCode;
	}


	public void setPortCode(String portCode) {
		this.portCode = portCode;
	}


	public int getLinkContainerAtWarehouseDuration() {
		return linkContainerAtWarehouseDuration;
	}


	public void setLinkContainerAtWarehouseDuration(
			int linkContainerAtWarehouseDuration) {
		this.linkContainerAtWarehouseDuration = linkContainerAtWarehouseDuration;
	}


	public int getReleaseLoadedContainerAtPortDuration() {
		return releaseLoadedContainerAtPortDuration;
	}


	public void setReleaseLoadedContainerAtPortDuration(
			int releaseLoadedContainerAtPortDuration) {
		this.releaseLoadedContainerAtPortDuration = releaseLoadedContainerAtPortDuration;
	}


	public ExportLadenRequests() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}