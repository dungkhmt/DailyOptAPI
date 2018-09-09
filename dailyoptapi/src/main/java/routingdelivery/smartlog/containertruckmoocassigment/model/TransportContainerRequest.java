package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TransportContainerRequest {
	private String orderID;
	private String containerCode;
	private String containerCategory;
	private double weight;
	
	private String originLocationCode;
	private String earlyArrivalDateTimeOrigin;
	private String lateArrivalDateTimeOrigin;
	private int attachContainerDuration;
	
	private String destinationLocationCode;
	private String earlyArrivalDateTimeDestination;
	private String lateArrivalDateTimeDestination;
	private int detachContainerDuration;
	
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
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getOriginLocationCode() {
		return originLocationCode;
	}
	public void setOriginLocationCode(String originLocationCode) {
		this.originLocationCode = originLocationCode;
	}
	public String getEarlyArrivalDateTimeOrigin() {
		return earlyArrivalDateTimeOrigin;
	}
	public void setEarlyArrivalDateTimeOrigin(String earlyArrivalDateTimeOrigin) {
		this.earlyArrivalDateTimeOrigin = earlyArrivalDateTimeOrigin;
	}
	public String getLateArrivalDateTimeOrigin() {
		return lateArrivalDateTimeOrigin;
	}
	public void setLateArrivalDateTimeOrigin(String lateArrivalDateTimeOrigin) {
		this.lateArrivalDateTimeOrigin = lateArrivalDateTimeOrigin;
	}
	public int getAttachContainerDuration() {
		return attachContainerDuration;
	}
	public void setAttachContainerDuration(int attachContainerDuration) {
		this.attachContainerDuration = attachContainerDuration;
	}
	public String getDestinationLocationCode() {
		return destinationLocationCode;
	}
	public void setDestinationLocationCode(String destinationLocationCode) {
		this.destinationLocationCode = destinationLocationCode;
	}
	public String getEarlyArrivalDateTimeDestination() {
		return earlyArrivalDateTimeDestination;
	}
	public void setEarlyArrivalDateTimeDestination(
			String earlyArrivalDateTimeDestination) {
		this.earlyArrivalDateTimeDestination = earlyArrivalDateTimeDestination;
	}
	public String getLateArrivalDateTimeDestination() {
		return lateArrivalDateTimeDestination;
	}
	public void setLateArrivalDateTimeDestination(
			String lateArrivalDateTimeDestination) {
		this.lateArrivalDateTimeDestination = lateArrivalDateTimeDestination;
	}
	public int getDetachContainerDuration() {
		return detachContainerDuration;
	}
	public void setDetachContainerDuration(int detachContainerDuration) {
		this.detachContainerDuration = detachContainerDuration;
	}
	public TransportContainerRequest(String orderID, String containerCode,
			String containerCategory, double weight, String originLocationCode,
			String earlyArrivalDateTimeOrigin,
			String lateArrivalDateTimeOrigin, int attachContainerDuration,
			String destinationLocationCode,
			String earlyArrivalDateTimeDestination,
			String lateArrivalDateTimeDestination, int detachContainerDuration) {
		super();
		this.orderID = orderID;
		this.containerCode = containerCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.originLocationCode = originLocationCode;
		this.earlyArrivalDateTimeOrigin = earlyArrivalDateTimeOrigin;
		this.lateArrivalDateTimeOrigin = lateArrivalDateTimeOrigin;
		this.attachContainerDuration = attachContainerDuration;
		this.destinationLocationCode = destinationLocationCode;
		this.earlyArrivalDateTimeDestination = earlyArrivalDateTimeDestination;
		this.lateArrivalDateTimeDestination = lateArrivalDateTimeDestination;
		this.detachContainerDuration = detachContainerDuration;
	}
	public TransportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
