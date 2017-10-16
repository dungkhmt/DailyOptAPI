package com.havestplanning.model;

public class ReturnAddFields {
	private int size;
	private String description;
	private FieldList fields;
	
	
	public FieldList getFields() {
		return fields;
	}

	public void setFields(FieldList fields) {
		this.fields = fields;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public ReturnAddFields(int size) {
		super();
		this.size = size;
	}

	public ReturnAddFields() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ReturnAddFields(int size, String description) {
		super();
		this.size = size;
		this.description = description;
	}

	public ReturnAddFields(int size, String description, FieldList fields) {
		super();
		this.size = size;
		this.description = description;
		this.fields = fields;
	}
	
}
