package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ExportEmptyRequests {
	private boolean isBreakRomooc;
	private String containerCategory;
	private String containerNo;
	private String orderCode;
	private String customerCode;
	private String requestDate;
	private String earlyDateTimePickupAtDepot;
	private String lateDateTimePickupAtDepot;
	private String earlyDateTimeLoadAtWarehouse;
	private String lateDateTimeLoadAtWarehouse;
	private String moocCode;
	private String depotContainerCode;
	private String wareHouseCode;
	private int linkContainerDuration;// thoi gian de dua cont. rong len mooc
	
	
	public ExportEmptyRequests(boolean isBreakRomooc, String containerCategory,
			String containerNo, String orderCode, String customerCode,
			String requestDate, String earlyDateTimePickupAtDepot,
			String lateDateTimePickupAtDepot,
			String earlyDateTimeLoadAtWarehouse,
			String lateDateTimeLoadAtWarehouse, String moocCode,
			String depotContainerCode, String wareHouseCode,
			int linkContainerDuration) {
		super();
		this.isBreakRomooc = isBreakRomooc;
		this.containerCategory = containerCategory;
		this.containerNo = containerNo;
		this.orderCode = orderCode;
		this.customerCode = customerCode;
		this.requestDate = requestDate;
		this.earlyDateTimePickupAtDepot = earlyDateTimePickupAtDepot;
		this.lateDateTimePickupAtDepot = lateDateTimePickupAtDepot;
		this.earlyDateTimeLoadAtWarehouse = earlyDateTimeLoadAtWarehouse;
		this.lateDateTimeLoadAtWarehouse = lateDateTimeLoadAtWarehouse;
		this.moocCode = moocCode;
		this.depotContainerCode = depotContainerCode;
		this.wareHouseCode = wareHouseCode;
		this.linkContainerDuration = linkContainerDuration;
	}
	public int getLinkContainerDuration() {
		return linkContainerDuration;
	}
	public void setLinkContainerDuration(int linkContainerDuration) {
		this.linkContainerDuration = linkContainerDuration;
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
	public String getMoocCode() {
		return moocCode;
	}
	public void setMoocCode(String moocCode) {
		this.moocCode = moocCode;
	}
	public String getDepotContainerCode() {
		return depotContainerCode;
	}
	public void setDepotContainerCode(String depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}
	public String getWareHouseCode() {
		return wareHouseCode;
	}
	public void setWareHouseCode(String wareHouseCode) {
		this.wareHouseCode = wareHouseCode;
	}
	public ExportEmptyRequests(boolean isBreakRomooc, String containerCategory,
			String containerNo, String orderCode, String customerCode,
			String requestDate, String earlyDateTimePickupAtDepot,
			String lateDateTimePickupAtDepot,
			String earlyDateTimeLoadAtWarehouse,
			String lateDateTimeLoadAtWarehouse, String moocCode,
			String depotContainerCode, String wareHouseCode) {
		super();
		this.isBreakRomooc = isBreakRomooc;
		this.containerCategory = containerCategory;
		this.containerNo = containerNo;
		this.orderCode = orderCode;
		this.customerCode = customerCode;
		this.requestDate = requestDate;
		this.earlyDateTimePickupAtDepot = earlyDateTimePickupAtDepot;
		this.lateDateTimePickupAtDepot = lateDateTimePickupAtDepot;
		this.earlyDateTimeLoadAtWarehouse = earlyDateTimeLoadAtWarehouse;
		this.lateDateTimeLoadAtWarehouse = lateDateTimeLoadAtWarehouse;
		this.moocCode = moocCode;
		this.depotContainerCode = depotContainerCode;
		this.wareHouseCode = wareHouseCode;
	}
	public ExportEmptyRequests() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
