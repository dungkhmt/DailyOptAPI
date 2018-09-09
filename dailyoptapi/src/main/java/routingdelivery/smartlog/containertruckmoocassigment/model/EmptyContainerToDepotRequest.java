package routingdelivery.smartlog.containertruckmoocassigment.model;

public class EmptyContainerToDepotRequest {
	private String orderID;
	
	private String containerCode;
	private String containerCategory;
	private String fromLocationCode;// source location of container
	private String[] returnDepotContainerCodes;// list of possible depots of container
	private String earlyArrivalDateTime;
	private String lateArrivalDateTime;// early and late time to fromLocationCode
	private int attachContainerDuration;// duration for attaching container to the truck
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getContainerCode() {
		return containerCode;
	}
	public void setContainerCode(String containerCode) {
		this.containerCode = containerCode;
	}
	public String getContainerCategory() {
		return containerCategory;
	}
	public void setContainerCategory(String containerCategory) {
		this.containerCategory = containerCategory;
	}
	public String getFromLocationCode() {
		return fromLocationCode;
	}
	public void setFromLocationCode(String fromLocationCode) {
		this.fromLocationCode = fromLocationCode;
	}
	public String[] getReturnDepotContainerCodes() {
		return returnDepotContainerCodes;
	}
	public void setReturnDepotContainerCodes(String[] returnDepotContainerCodes) {
		this.returnDepotContainerCodes = returnDepotContainerCodes;
	}
	public String getEarlyArrivalDateTime() {
		return earlyArrivalDateTime;
	}
	public void setEarlyArrivalDateTime(String earlyArrivalDateTime) {
		this.earlyArrivalDateTime = earlyArrivalDateTime;
	}
	public String getLateArrivalDateTime() {
		return lateArrivalDateTime;
	}
	public void setLateArrivalDateTime(String lateArrivalDateTime) {
		this.lateArrivalDateTime = lateArrivalDateTime;
	}
	public int getAttachContainerDuration() {
		return attachContainerDuration;
	}
	public void setAttachContainerDuration(int attachContainerDuration) {
		this.attachContainerDuration = attachContainerDuration;
	}
	public EmptyContainerToDepotRequest(String orderID, String containerCode,
			String containerCategory, String fromLocationCode,
			String[] returnDepotContainerCodes, String earlyArrivalDateTime,
			String lateArrivalDateTime, int attachContainerDuration) {
		super();
		this.orderID = orderID;
		this.containerCode = containerCode;
		this.containerCategory = containerCategory;
		this.fromLocationCode = fromLocationCode;
		this.returnDepotContainerCodes = returnDepotContainerCodes;
		this.earlyArrivalDateTime = earlyArrivalDateTime;
		this.lateArrivalDateTime = lateArrivalDateTime;
		this.attachContainerDuration = attachContainerDuration;
	}
	public EmptyContainerToDepotRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
