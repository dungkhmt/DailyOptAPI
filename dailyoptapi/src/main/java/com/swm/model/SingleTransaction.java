package com.swm.model;

public class SingleTransaction  implements Comparable{
	public SingleTransaction() {
		super();
		// TODO Auto-generated constructor stub
	}

	private String orderCode;
	private String itemCode;
	
	public SingleTransaction(String orderCode, String itemCode) {
		super();
		this.orderCode = orderCode;
		this.itemCode = itemCode;
	}

	public String getOrderCode() {
		return orderCode;
	}

	public void setOrderCode(String orderCode) {
		this.orderCode = orderCode;
	}

	public String getItemCode() {
		return itemCode;
	}

	public void setItemCode(String itemCode) {
		this.itemCode = itemCode;
	}
	
	@Override
    public boolean equals(Object obj) {
        return ((SingleTransaction) obj).getOrderCode().equals(getOrderCode());
    }
 
    @Override
    public int compareTo(Object o) {
        SingleTransaction e = (SingleTransaction) o;
        return getOrderCode().compareTo(e.getOrderCode());
    }
    
    public void print() {
    	System.out.println("order: " + orderCode + ", item: " + itemCode);
    }

}
