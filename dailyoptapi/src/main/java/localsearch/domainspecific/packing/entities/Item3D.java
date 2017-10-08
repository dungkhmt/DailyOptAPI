package localsearch.domainspecific.packing.entities;

public class Item3D {
	private int width;
	private int length;
	private int height;
	private int itemID;
	
	public int getItemID() {
		return itemID;
	}
	public void setItemID(int itemID) {
		this.itemID = itemID;
	}
	public String toString(){
		return "(" + width + "," + length + "," + height + ")";
	}
	public Item3D(int itemID, int width, int length, int height) {
		super();
		this.itemID = itemID;
		this.width = width;
		this.length = length;
		this.height = height;
	}
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
	
	
}
