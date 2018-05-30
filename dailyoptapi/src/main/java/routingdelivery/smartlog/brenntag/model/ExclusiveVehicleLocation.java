package routingdelivery.smartlog.brenntag.model;

public class ExclusiveVehicleLocation {
	private String vehicleCode;
	private String locationCode;// vehicleCode cannot arrive at locationCode
	public String getVehicleCode() {
		return vehicleCode;
	}
	public void setVehicleCode(String vehicleCode) {
		this.vehicleCode = vehicleCode;
	}
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public ExclusiveVehicleLocation(String vehicleCode, String locationCode) {
		super();
		this.vehicleCode = vehicleCode;
		this.locationCode = locationCode;
	}
	public ExclusiveVehicleLocation() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
