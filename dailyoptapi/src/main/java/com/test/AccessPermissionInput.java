package com.test;

public class AccessPermissionInput {
	private String a;
	private String b;
	
	public AccessPermissionInput(String a, String b){
		super();
		this.a = a;
		this.a = b;
	}
	public AccessPermissionInput(){
		super();
	}
	public String getA() {
		return a;
	}
	public void setA(String a) {
		this.a = a;
	}
	public String getB() {
		return b;
	}
	public void setB(String b) {
		this.b = b;
	}
}
