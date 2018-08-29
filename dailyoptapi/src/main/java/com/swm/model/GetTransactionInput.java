package com.swm.model;

import com.google.gson.Gson;

public class GetTransactionInput {
	private String dbName;
	Parameters params;
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	public Parameters getParams() {
		return params;
	}
	public void setParams(Parameters params) {
		this.params = params;
	}
	public GetTransactionInput(String dbName, Parameters params) {
		super();
		this.dbName = dbName;
		this.params = params;
	}
	public GetTransactionInput() {
		super();
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args){
		String dbName = "transaction";
		Parameters p= new Parameters(0.2, 0.8);
		
		GetTransactionInput I = new GetTransactionInput(dbName, p);
		Gson gson = new Gson();
		String json = gson.toJson(I);
		System.out.println(json);
	}
}
