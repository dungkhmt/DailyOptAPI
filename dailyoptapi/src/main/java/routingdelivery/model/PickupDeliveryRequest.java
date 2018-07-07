package routingdelivery.model;

public class PickupDeliveryRequest {
	private String orderID;
	private Item[] items;

	private String pickupLocationCode;
	private String pickupAddr;
	private double pickupLat;
	private double pickupLng;
	private String earlyPickupTime;
	private String latePickupTime;
	private int pickupDuration;// not used case brenntag
	private int fixLoadTime;

	private String deliveryLocationCode;
	private String deliveryAddr;
	private double deliveryLat;
	private double deliveryLng;
	private String earlyDeliveryTime;
	private String lateDeliveryTime;
	private int deliveryDuration;// not use case brenntag
	private int fixUnloadTime;
	
	
	private String splitDelivery;// "Y" or "N"
	private String description;
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public PickupDeliveryRequest(String orderID, Item[] items,
			String pickupLocationCode, String pickupAddr, double pickupLat,
			double pickupLng, String earlyPickupTime, String latePickupTime,
			int pickupDuration, int fixLoadTime, String deliveryLocationCode,
			String deliveryAddr, double deliveryLat, double deliveryLng,
			String earlyDeliveryTime, String lateDeliveryTime,
			int deliveryDuration, int fixUnloadTime, String splitDelivery) {
		super();
		this.orderID = orderID;
		this.items = items;
		this.pickupLocationCode = pickupLocationCode;
		this.pickupAddr = pickupAddr;
		this.pickupLat = pickupLat;
		this.pickupLng = pickupLng;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.pickupDuration = pickupDuration;
		this.fixLoadTime = fixLoadTime;
		this.deliveryLocationCode = deliveryLocationCode;
		this.deliveryAddr = deliveryAddr;
		this.deliveryLat = deliveryLat;
		this.deliveryLng = deliveryLng;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.deliveryDuration = deliveryDuration;
		this.fixUnloadTime = fixUnloadTime;
		this.splitDelivery = splitDelivery;
	}
	public int getFixLoadTime() {
		return fixLoadTime;
	}
	public void setFixLoadTime(int fixLoadTime) {
		this.fixLoadTime = fixLoadTime;
	}
	public int getFixUnloadTime() {
		return fixUnloadTime;
	}
	public void setFixUnloadTime(int fixUnloadTime) {
		this.fixUnloadTime = fixUnloadTime;
	}
	public PickupDeliveryRequest(String orderID, Item[] items,
			String pickupLocationCode, String pickupAddr, double pickupLat,
			double pickupLng, String earlyPickupTime, String latePickupTime,
			int pickupDuration, String deliveryLocationCode,
			String deliveryAddr, double deliveryLat, double deliveryLng,
			String earlyDeliveryTime, String lateDeliveryTime,
			int deliveryDuration, String splitDelivery) {
		super();
		this.orderID = orderID;
		this.items = items;
		this.pickupLocationCode = pickupLocationCode;
		this.pickupAddr = pickupAddr;
		this.pickupLat = pickupLat;
		this.pickupLng = pickupLng;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.pickupDuration = pickupDuration;
		this.deliveryLocationCode = deliveryLocationCode;
		this.deliveryAddr = deliveryAddr;
		this.deliveryLat = deliveryLat;
		this.deliveryLng = deliveryLng;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.deliveryDuration = deliveryDuration;
		this.splitDelivery = splitDelivery;
	}
	public String getSplitDelivery() {
		return splitDelivery;
	}
	public void setSplitDelivery(String splitDelivery) {
		this.splitDelivery = splitDelivery;
	}
	public PickupDeliveryRequest clone(){
		Item[] I = new Item[items.length];
		for(int i = 0; i < items.length; i++){
			I[i] = items[i].clone();
		}
		return new PickupDeliveryRequest(orderID, I,
				pickupLocationCode, pickupAddr, pickupLat,
				pickupLng, earlyPickupTime, latePickupTime,
				pickupDuration, deliveryLocationCode,
				deliveryAddr, deliveryLat, deliveryLng,
				earlyDeliveryTime, lateDeliveryTime,
				deliveryDuration, splitDelivery);
	}
	public PickupDeliveryRequest(String orderID, Item[] items,
			String pickupLocationCode, String pickupAddr, double pickupLat,
			double pickupLng, String earlyPickupTime, String latePickupTime,
			int pickupDuration, String deliveryLocationCode,
			String deliveryAddr, double deliveryLat, double deliveryLng,
			String earlyDeliveryTime, String lateDeliveryTime,
			int deliveryDuration) {
		super();
		this.orderID = orderID;
		this.items = items;
		this.pickupLocationCode = pickupLocationCode;
		this.pickupAddr = pickupAddr;
		this.pickupLat = pickupLat;
		this.pickupLng = pickupLng;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.pickupDuration = pickupDuration;
		this.deliveryLocationCode = deliveryLocationCode;
		this.deliveryAddr = deliveryAddr;
		this.deliveryLat = deliveryLat;
		this.deliveryLng = deliveryLng;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
		this.deliveryDuration = deliveryDuration;
	}
	public int getPickupDuration() {
		return pickupDuration;
	}
	public void setPickupDuration(int pickupDuration) {
		this.pickupDuration = pickupDuration;
	}
	public int getDeliveryDuration() {
		return deliveryDuration;
	}
	public void setDeliveryDuration(int deliveryDuration) {
		this.deliveryDuration = deliveryDuration;
	}
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public Item[] getItems() {
		return items;
	}
	public void setItems(Item[] items) {
		this.items = items;
	}
	public String getPickupAddr() {
		return pickupAddr;
	}
	public void setPickupAddr(String pickupAddr) {
		this.pickupAddr = pickupAddr;
	}
	public double getPickupLat() {
		return pickupLat;
	}
	public void setPickupLat(double pickupLat) {
		this.pickupLat = pickupLat;
	}
	public double getPickupLng() {
		return pickupLng;
	}
	public void setPickupLng(double pickupLng) {
		this.pickupLng = pickupLng;
	}
	public String getEarlyPickupTime() {
		return earlyPickupTime;
	}
	public void setEarlyPickupTime(String earlyPickupTime) {
		this.earlyPickupTime = earlyPickupTime;
	}
	public String getLatePickupTime() {
		return latePickupTime;
	}
	public void setLatePickupTime(String latePickupTime) {
		this.latePickupTime = latePickupTime;
	}
	public String getDeliveryAddr() {
		return deliveryAddr;
	}
	public void setDeliveryAddr(String deliveryAddr) {
		this.deliveryAddr = deliveryAddr;
	}
	public double getDeliveryLat() {
		return deliveryLat;
	}
	public void setDeliveryLat(double deliveryLat) {
		this.deliveryLat = deliveryLat;
	}
	public double getDeliveryLng() {
		return deliveryLng;
	}
	public void setDeliveryLng(double deliveryLng) {
		this.deliveryLng = deliveryLng;
	}
	public String getEarlyDeliveryTime() {
		return earlyDeliveryTime;
	}
	public void setEarlyDeliveryTime(String earlyDeliveryTime) {
		this.earlyDeliveryTime = earlyDeliveryTime;
	}
	public String getLateDeliveryTime() {
		return lateDeliveryTime;
	}
	public void setLateDeliveryTime(String lateDeliveryTime) {
		this.lateDeliveryTime = lateDeliveryTime;
	}
	
	
	public PickupDeliveryRequest(String orderID, Item[] items,
			String pickupLocationCode, String pickupAddr, double pickupLat,
			double pickupLng, String earlyPickupTime, String latePickupTime,
			String deliveryLocationCode, String deliveryAddr,
			double deliveryLat, double deliveryLng, String earlyDeliveryTime,
			String lateDeliveryTime) {
		super();
		this.orderID = orderID;
		this.items = items;
		this.pickupLocationCode = pickupLocationCode;
		this.pickupAddr = pickupAddr;
		this.pickupLat = pickupLat;
		this.pickupLng = pickupLng;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.deliveryLocationCode = deliveryLocationCode;
		this.deliveryAddr = deliveryAddr;
		this.deliveryLat = deliveryLat;
		this.deliveryLng = deliveryLng;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
	}
	public String getPickupLocationCode() {
		return pickupLocationCode;
	}
	public void setPickupLocationCode(String pickupLocationCode) {
		this.pickupLocationCode = pickupLocationCode;
	}
	public String getDeliveryLocationCode() {
		return deliveryLocationCode;
	}
	public void setDeliveryLocationCode(String deliveryLocationCode) {
		this.deliveryLocationCode = deliveryLocationCode;
	}
	public PickupDeliveryRequest(String orderID, Item[] items,
			String pickupAddr, double pickupLat, double pickupLng,
			String earlyPickupTime, String latePickupTime, String deliveryAddr,
			double deliveryLat, double deliveryLng, String earlyDeliveryTime,
			String lateDeliveryTime) {
		super();
		this.orderID = orderID;
		this.items = items;
		this.pickupAddr = pickupAddr;
		this.pickupLat = pickupLat;
		this.pickupLng = pickupLng;
		this.earlyPickupTime = earlyPickupTime;
		this.latePickupTime = latePickupTime;
		this.deliveryAddr = deliveryAddr;
		this.deliveryLat = deliveryLat;
		this.deliveryLng = deliveryLng;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
	}
	public PickupDeliveryRequest() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
