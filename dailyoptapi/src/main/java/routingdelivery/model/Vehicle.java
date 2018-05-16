package routingdelivery.model;

public class Vehicle {
	private int width;
	private int length;
	private int height;
	private String code;
	private double lat;
	private double lng;
	private double endLat;
	private double endLng;
	private double weight;
	private String startLocationCode;
	private String endLocationCode;
	private String startWorkingTime;
	private String endWorkingTime;
	public Vehicle(int width, int length, int height, String code,
			double lat, double lng, double endLat, double endLng,
			double weight, String startLocationCode, String endLocationCode,
			String startWorkingTime, String endWorkingTime) {
		super();
		this.width = width;
		this.length = length;
		this.height = height;
		this.code = code;
		this.lat = lat;
		this.lng = lng;
		this.endLat = endLat;
		this.endLng = endLng;
		this.weight = weight;
		this.startLocationCode = startLocationCode;
		this.endLocationCode = endLocationCode;
		this.startWorkingTime = startWorkingTime;
		this.endWorkingTime = endWorkingTime;
	}
	public Vehicle(int width, int length, int height, String code,
			double lat, double lng) {
		super();
		this.width = width;
		this.length = length;
		this.height = height;
		this.code = code;
		this.lat = lat;
		this.lng = lng;
	}

	public Vehicle() {
		super();
		// TODO Auto-generated constructor stub
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
	public double getEndLat() {
		return endLat;
	}
	public void setEndLat(double endLat) {
		this.endLat = endLat;
	}
	public double getEndLng() {
		return endLng;
	}
	public void setEndLng(double endLng) {
		this.endLng = endLng;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getStartLocationCode() {
		return startLocationCode;
	}
	public void setStartLocationCode(String startLocationCode) {
		this.startLocationCode = startLocationCode;
	}
	public String getEndLocationCode() {
		return endLocationCode;
	}
	public void setEndLocationCode(String endLocationCode) {
		this.endLocationCode = endLocationCode;
	}
	public String getStartWorkingTime() {
		return startWorkingTime;
	}
	public void setStartWorkingTime(String startWorkingTime) {
		this.startWorkingTime = startWorkingTime;
	}
	public String getEndWorkingTime() {
		return endWorkingTime;
	}
	public void setEndWorkingTime(String endWorkingTime) {
		this.endWorkingTime = endWorkingTime;
	}
	
	
	
}
