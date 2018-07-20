package routingdelivery.smartlog.containertruckmoocassigment.model;

public class Warehouse {
	private String code;
	private String locationCode;
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
	public Warehouse(String code, String locationCode) {
		super();
		this.code = code;
		this.locationCode = locationCode;
	}
	public Warehouse() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
