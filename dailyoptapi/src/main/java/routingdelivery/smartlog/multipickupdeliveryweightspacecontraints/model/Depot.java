

package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

public class Depot {
	private String code;
	private double lat;
	private double lng;
	
	private Vehicle[] vehicles;
	
	public Depot(String code, double lat, double lng, Vehicle[] vehicles) {
		super();
		this.code = code;
		this.lat = lat;
		this.lng = lng;
		this.vehicles = vehicles;
	}
	public Vehicle[] getVehicles() {
		return vehicles;
	}
	public void setVehicles(Vehicle[] vehicles) {
		this.vehicles = vehicles;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public double getLat() {
		return lat;
	}
	public void setLat(double lat) {
		this.lat = lat;
	}
	public double getLng() {
		return lng;
	}
	public void setLng(double lng) {
		this.lng = lng;
	}
	public Depot(String code, double lat, double lng) {
		super();
		this.code = code;
		this.lat = lat;
		this.lng = lng;
	}
	public Depot() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
