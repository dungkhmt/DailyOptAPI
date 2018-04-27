package routingdelivery.model;

import java.util.ArrayList;



public class Request {
	private String addr;
	private double lat;
	private double lng;
	
	private String orderID;
	private Item[] items;
	private String earlyDeliveryTime;
	private String lateDeliveryTime;
	
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
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
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
	
	public Request(String addr, double lat, double lng, String orderID,
			Item[] items, String earlyDeliveryTime, String lateDeliveryTime) {
		super();
		this.addr = addr;
		this.lat = lat;
		this.lng = lng;
		this.orderID = orderID;
		this.items = items;
		this.earlyDeliveryTime = earlyDeliveryTime;
		this.lateDeliveryTime = lateDeliveryTime;
	}
	public Request() {
		super();
		// TODO Auto-generated constructor stub
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
	
	
	
}
