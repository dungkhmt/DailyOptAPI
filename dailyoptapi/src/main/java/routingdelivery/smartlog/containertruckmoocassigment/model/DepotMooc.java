package routingdelivery.smartlog.containertruckmoocassigment.model;

public class DepotMooc{
	private String code;
	private String locationCode;
	private int pickupMoocDuration;
	private int deliveryMoocDuration;
	
	
	public int getPickupMoocDuration() {
		return pickupMoocDuration;
	}
	public void setPickupMoocDuration(int pickupMoocDuration) {
		this.pickupMoocDuration = pickupMoocDuration;
	}
	public int getDeliveryMoocDuration() {
		return deliveryMoocDuration;
	}
	public void setDeliveryMoocDuration(int deliveryMoocDuration) {
		this.deliveryMoocDuration = deliveryMoocDuration;
	}
	public DepotMooc(String code, String locationCode) {
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
