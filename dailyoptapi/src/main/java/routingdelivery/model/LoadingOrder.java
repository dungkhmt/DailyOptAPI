package routingdelivery.model;

public class LoadingOrder {
	private String orderID;
	private LoadingElement[] loadElements;
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public LoadingElement[] getLoadElements() {
		return loadElements;
	}
	public void setLoadElements(LoadingElement[] loadElements) {
		this.loadElements = loadElements;
	}
	public LoadingOrder(String orderID, LoadingElement[] loadElements) {
		super();
		this.orderID = orderID;
		this.loadElements = loadElements;
	}
	public LoadingOrder() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
