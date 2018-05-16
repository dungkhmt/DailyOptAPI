package routingdelivery.model;

public class Item {
	private int w;
	private int l;
	private int h;
	private String name;
	private int quantity;
	private double weight;
	
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
