package routingdelivery.smartlog.containertruckmoocassigment.model;

public class TransportContainerLocationInfo {
	private String locationCode;
	private String earlyArrivalTime;
	private String lateArrivalTime;
	private int duration;
	public String getLocationCode() {
		return locationCode;
	}
	public void setLocationCode(String locationCode) {
		this.locationCode = locationCode;
	}
	public String getEarlyArrivalTime() {
		return earlyArrivalTime;
	}
	public void setEarlyArrivalTime(String earlyArrivalTime) {
		this.earlyArrivalTime = earlyArrivalTime;
	}
	public String getLateArrivalTime() {
		return lateArrivalTime;
	}
	public void setLateArrivalTime(String lateArrivalTime) {
		this.lateArrivalTime = lateArrivalTime;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public TransportContainerLocationInfo(String locationCode,
			String earlyArrivalTime, String lateArrivalTime, int duration) {
		super();
		this.locationCode = locationCode;
		this.earlyArrivalTime = earlyArrivalTime;
		this.lateArrivalTime = lateArrivalTime;
		this.duration = duration;
	}
	public TransportContainerLocationInfo() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}	
