package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;



public class LocationConfig {
	public static final String INT_CITY = "INT_CITY";
	public static final String EXT_CITY = "EXT_CITY";
	
	private String locationCode;
	private DateTimePeriod[] interupPeriods;
	private String type;// "INT_CITY": noi thanh, "EXT_CITY": ngoai thanh
	private String districtCode;
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public DateTimePeriod[] getInterupPeriods() {
		return interupPeriods;
	}
	public void setInterupPeriods(DateTimePeriod[] interupPeriods) {
		this.interupPeriods = interupPeriods;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDistrictCode() {
		return districtCode;
	}
	public void setDistrictCode(String districtCode) {
		this.districtCode = districtCode;
	}
	public LocationConfig(String locationCode, DateTimePeriod[] interupPeriods,
			String type, String districtCode) {
		super();
		this.locationCode = locationCode;
		this.interupPeriods = interupPeriods;
		this.type = type;
		this.districtCode = districtCode;
	}
	public LocationConfig() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
