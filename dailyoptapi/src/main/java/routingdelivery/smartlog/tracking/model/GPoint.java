package routingdelivery.smartlog.tracking.model;

public class GPoint {
	private double lat;
	private double lng;
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
	public GPoint(double lat, double lng) {
		super();
		this.lat = lat;
		this.lng = lng;
	}
	public GPoint() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
