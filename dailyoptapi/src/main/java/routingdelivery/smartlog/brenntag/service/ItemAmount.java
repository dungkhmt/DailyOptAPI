package routingdelivery.smartlog.brenntag.service;

public class ItemAmount {
	public int itemIndex;
	public int amount;
	public int getItemIndex() {
		return itemIndex;
	}
	public void setItemIndex(int itemIndex) {
		this.itemIndex = itemIndex;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public ItemAmount(int itemIndex, int amount) {
		super();
		this.itemIndex = itemIndex;
		this.amount = amount;
	}

}
