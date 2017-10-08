package routingdelivery.model;

public class LoadingElement {
	private Item item;
	private int itemID;
	private int posWidth;
	private int posLength;
	private int posHeight;
	private String description;
	
	private String orderID;
	private String addr;
	
	
	public int getItemID() {
		return itemID;
	}
	public void setItemID(int itemID) {
		this.itemID = itemID;
	}
	public String getOrderID() {
		return orderID;
	}
	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}
	public String getAddr() {
		return addr;
	}
	public void setAddr(String addr) {
		this.addr = addr;
	}
	public Item getItem() {
		return item;
	}
	public void setItem(Item item) {
		this.item = item;
	}
	public int getPosWidth() {
		return posWidth;
	}
	public void setPosWidth(int posWidth) {
		this.posWidth = posWidth;
	}
	public int getPosLength() {
		return posLength;
	}
	public void setPosLength(int posLength) {
		this.posLength = posLength;
	}
	public int getPosHeight() {
		return posHeight;
	}
	public void setPosHeight(int posHeight) {
		this.posHeight = posHeight;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	public LoadingElement(Item item, int itemID, int posWidth, int posLength,
			int posHeight, String description, String orderID, String addr) {
		super();
		this.item = item;
		this.itemID = itemID;
		this.posWidth = posWidth;
		this.posLength = posLength;
		this.posHeight = posHeight;
		this.description = description;
		this.orderID = orderID;
		this.addr = addr;
	}
	public LoadingElement() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
