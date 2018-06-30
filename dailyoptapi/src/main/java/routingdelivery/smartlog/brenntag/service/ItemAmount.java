package routingdelivery.smartlog.brenntag.service;

public class ItemAmount {
	public int itemIndex;
	public double amount;
	public int getItemIndex() {
		return itemIndex;
	}
	public void setItemIndex(int itemIndex) {
		this.itemIndex = itemIndex;
	}
	public double getAmount() {
		return amount;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	public ItemAmount(int itemIndex, double amount) {
		super();
		this.itemIndex = itemIndex;
		this.amount = amount;
	}

}
