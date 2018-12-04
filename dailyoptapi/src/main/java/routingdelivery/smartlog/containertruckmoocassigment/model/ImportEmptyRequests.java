package routingdelivery.smartlog.containertruckmoocassigment.model;

public class ImportEmptyRequests {
	private boolean isBreakRomooc;
	private String containerCategory;
	private String containerCode;
	private String containerNo;
	private String orderCode;
	private String customerCode;
	private String customerName;
	private String requestDate;
	private String lateDateTimeAttachAtWarehouse;
	private String lateDateTimeReturnEmptyAtDepot;
	private String moocCode;
	private double weight;
	private String wareHouseCode;
	private String depotContainerCode;
	private int linkContainerDuration;// thoi gian dat cont. len mooc
	

	public ImportEmptyRequests(boolean isBreakRomooc, String containerCategory,
			String containerCode, String containerNo, String orderCode, String customerCode,
			String customerName, String requestDate, String lateDateTimeAttachAtWarehouse,
			String lateDateTimeReturnEmptyAtDepot, String moocCode,
			double weight, String wareHouseCode, String depotContainerCode,
			int linkContainerDuration) {
		super();
		this.isBreakRomooc = isBreakRomooc;
		this.containerCategory = containerCategory;
		this.containerCode = containerCode;
		this.containerNo = containerNo;
		this.orderCode = orderCode;
		this.customerCode = customerCode;
		this.customerName = customerName;
		this.requestDate = requestDate;
		this.lateDateTimeAttachAtWarehouse = lateDateTimeAttachAtWarehouse;
		this.lateDateTimeReturnEmptyAtDepot = lateDateTimeReturnEmptyAtDepot;
		this.moocCode = moocCode;
		this.weight = weight;
		this.wareHouseCode = wareHouseCode;
		this.depotContainerCode = depotContainerCode;
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


	public String getContainerCode() {
		return containerCode;
	}


	public void setContainerCode(String containerCode) {
		this.containerCode = containerCode;
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

	public String getCustomerName(){
		return this.customerName;
	}
	
	public void setCustomerName(String customerName){
		this.customerName = customerName;
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


	public String getLateDateTimeReturnEmptyAtDepot() {
		return lateDateTimeReturnEmptyAtDepot;
	}


	public void setLateDateTimeReturnEmptyAtDepot(
			String lateDateTimeReturnEmptyAtDepot) {
		this.lateDateTimeReturnEmptyAtDepot = lateDateTimeReturnEmptyAtDepot;
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


	public String getDepotContainerCode() {
		return depotContainerCode;
	}


	public void setDepotContainerCode(String depotContainerCode) {
		this.depotContainerCode = depotContainerCode;
	}


	public int getLinkContainerDuration() {
		return linkContainerDuration;
	}


	public void setLinkContainerDuration(int linkContainerDuration) {
		this.linkContainerDuration = linkContainerDuration;
	}


	public ImportEmptyRequests() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
