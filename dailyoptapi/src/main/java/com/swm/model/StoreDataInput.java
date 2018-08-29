package com.swm.model;

import com.google.gson.Gson;



public class StoreDataInput {
	private SingleTransaction[] transactionList;
	private String dbName;
	public SingleTransaction[] getTransactionList() {
		return transactionList;
	}
	public void setTransactionList(SingleTransaction[] transactionList) {
		this.transactionList = transactionList;
	}
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public StoreDataInput(SingleTransaction[] transactionList, String dbName) {
		super();
		this.transactionList = transactionList;
		this.dbName = dbName;
	}
	public StoreDataInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void main(String[] args){
		SingleTransaction[] gt = new SingleTransaction[4];
		gt[0] = new SingleTransaction("01", "I01");
		gt[1] = new SingleTransaction("01", "I02");
		gt[2] = new SingleTransaction("02", "I01");
		gt[3] = new SingleTransaction("02", "I02");
		
		StoreDataInput I = new StoreDataInput(gt, "transaction");
		Gson gson = new Gson();
		String json = gson.toJson(I);
		System.out.println(json);
		
	}
	
}
