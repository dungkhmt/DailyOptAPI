package routingdelivery.model;

public class Vehicle {
	private int width;
	private int length;
	private int height;
	private String code;
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
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public Vehicle() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Vehicle(int width, int length, int height, String code, double lat,
			double lng) {
		super();
		this.width = width;
		this.length = length;
		this.height = height;
		this.code = code;
		this.lat = lat;
		this.lng = lng;
	}
	
}
