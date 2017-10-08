package routingdelivery.model;

public class DistanceElement {
	private String srcCode;
	private String destCode;
	private double distance;
	public String getSrcCode() {
		return srcCode;
	}
	public void setSrcCode(String srcCode) {
		this.srcCode = srcCode;
	}
	public String getDestCode() {
		return destCode;
	}
	public void setDestCode(String destCode) {
		this.destCode = destCode;
	}
	public double getDistance() {
		return distance;
	}
	public void setDistance(double distance) {
		this.distance = distance;
	}
	public DistanceElement(String srcCode, String destCode, double distance) {
		super();
		this.srcCode = srcCode;
		this.destCode = destCode;
		this.distance = distance;
	}
	public DistanceElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
