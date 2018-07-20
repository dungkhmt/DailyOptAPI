package routingdelivery.smartlog.containertruckmoocassigment.model;

public class DepotTruck {
	private String code;
	private String locationCode;
	public DepotTruck(String code, String locationCode) {
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
