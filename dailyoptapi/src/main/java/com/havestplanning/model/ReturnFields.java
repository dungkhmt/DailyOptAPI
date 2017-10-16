package com.havestplanning.model;

public class ReturnFields {
	private int size;
	private String description;
	private FieldList fields;
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public FieldList getFields() {
		return fields;
	}
	public void setFields(FieldList fields) {
		this.fields = fields;
	}
	public ReturnFields(int size, String description, FieldList fields) {
		super();
		this.size = size;
		this.description = description;
		this.fields = fields;
	}
	public ReturnFields() {
		super();
		// TODO Auto-generated constructor stub
	}

}
