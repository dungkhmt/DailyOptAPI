package com.havestplanning.model;

public class QualityFunction {
	private double top;
	private double bottom;
	private int length;
	public double getTop() {
		return top;
	}
	public void setTop(double top) {
		this.top = top;
	}
	public double getBottom() {
		return bottom;
	}
	public void setBottom(double bottom) {
		this.bottom = bottom;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public QualityFunction(double top, double bottom, int length) {
		super();
		this.top = top;
		this.bottom = bottom;
		this.length = length;
	}
	public QualityFunction() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
