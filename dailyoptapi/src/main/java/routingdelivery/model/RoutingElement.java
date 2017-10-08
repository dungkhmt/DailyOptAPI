package routingdelivery.model;

public class RoutingElement {
	private String code;
	private String address;
	private String latlng;
	
	public String getLatlng() {
		return latlng;
	}
	public void setLatlng(String latlng) {
		this.latlng = latlng;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public RoutingElement(String code, String address, String latlng) {
		super();
		this.code = code;
		this.address = address;
		this.latlng = latlng;
	}
	public RoutingElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
