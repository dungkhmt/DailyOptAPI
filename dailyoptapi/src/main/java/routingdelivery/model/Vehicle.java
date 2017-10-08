package routingdelivery.model;

public class Vehicle {
	private int width;
	private int length;
	private int height;
	private String code;
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
	public Vehicle(int width, int length, int height, String code) {
		super();
		this.width = width;
		this.length = length;
		this.height = height;
		this.code = code;
	}
	public Vehicle() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
