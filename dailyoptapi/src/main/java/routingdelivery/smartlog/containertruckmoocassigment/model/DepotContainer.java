package routingdelivery.smartlog.containertruckmoocassigment.model;

public class DepotContainer {
	private String code;
	private String locationCode;
	private int pickupContainerDuration;
	private int deliveryContainerDuration;
	
	public DepotContainer(String code, String locationCode) {
		super();
		this.code = code;
		this.locationCode = locationCode;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	
}
