package routingdelivery.model;

public class Item {
	private int w;
	private int l;
	private int h;
	private String name;
	private String code;
	private int quantity;
	private double weight;
	private int pickupDuration;
	private int deliveryDuration;
	
	public Item(int w, int l, int h, String name, String code, int quantity,
			double weight, int pickupDuration, int deliveryDuration) {
		super();
		this.w = w;
		this.l = l;
		this.h = h;
		this.name = name;
		this.code = code;
		this.quantity = quantity;
		this.weight = weight;
		this.pickupDuration = pickupDuration;
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
	public Item(int w, int l, int h, String name, String code, int quantity,
			double weight) {
		super();
		this.w = w;
		this.l = l;
		this.h = h;
		this.name = name;
		this.code = code;
		this.quantity = quantity;
		this.weight = weight;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Item(int w, int l, int h, String name, int quantity, double weight) {
		super();
		this.w = w;
		this.l = l;
		this.h = h;
		this.name = name;
		this.quantity = quantity;
		this.weight = weight;
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public int getW() {
		return w;
	}
	public void setW(int w) {
		this.w = w;
	}
	public int getL() {
		return l;
	}
	public void setL(int l) {
		this.l = l;
	}
	public int getH() {
		return h;
	}
	public void setH(int h) {
		this.h = h;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getQuantity() {
		return quantity;
	}
	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}
	public Item(int w, int l, int h, String name, int quantity) {
		super();
		this.w = w;
		this.l = l;
		this.h = h;
		this.name = name;
		this.quantity = quantity;
	}
	public Item() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
