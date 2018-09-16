package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TransportContainerRequest {
	private String orderID;
	private String containerCode;
	private String containerCategory;
	private double weight;

	private int attachContainerDuration;// time duration for attaching the truck-mooc to the container at first location
	private TransportContainerLocationInfo[] locations;// list of location the container have to be passed
	private int detachContainerDuration;// time duration for detaching the container from truck-mooc at last location
	//private String originLocationCode;
	//private String earlyArrivalDateTimeOrigin;
	//private String lateArrivalDateTimeOrigin;
	//private int attachContainerDuration;
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
	public int getAttachContainerDuration() {
		return attachContainerDuration;
	}
	public void setAttachContainerDuration(int attachContainerDuration) {
		this.attachContainerDuration = attachContainerDuration;
	}
	public TransportContainerLocationInfo[] getLocations() {
		return locations;
	}
	public void setLocations(TransportContainerLocationInfo[] locations) {
		this.locations = locations;
	}
	public int getDetachContainerDuration() {
		return detachContainerDuration;
	}
	public void setDetachContainerDuration(int detachContainerDuration) {
		this.detachContainerDuration = detachContainerDuration;
	}
	public TransportContainerRequest(String orderID, String containerCode,
			String containerCategory, double weight,
			int attachContainerDuration,
			TransportContainerLocationInfo[] locations,
			int detachContainerDuration) {
		super();
		this.orderID = orderID;
		this.containerCode = containerCode;
		this.containerCategory = containerCategory;
		this.weight = weight;
		this.attachContainerDuration = attachContainerDuration;
		this.locations = locations;
		this.detachContainerDuration = detachContainerDuration;
	}
	public TransportContainerRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	//private String destinationLocationCode;
	//private String earlyArrivalDateTimeDestination;
	//private String lateArrivalDateTimeDestination;
	//private int detachContainerDuration;
	
	
	
	
}
