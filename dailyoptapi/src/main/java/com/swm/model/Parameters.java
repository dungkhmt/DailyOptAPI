package com.swm.model;

public class Parameters {
	private double minSupport;
	private double minConfidence;
	public Parameters(double minSupport, double minConfidence) {
		super();
		this.minSupport = minSupport;
		this.minConfidence = minConfidence;
	}
	
	public Parameters() {
		super();
		// TODO Auto-generated constructor stub
	}

	public double getMinSupport() {
		return minSupport;
	}
	public void setMinSupport(double minSupport) {
		this.minSupport = minSupport;
	}
	public double getMinConfidence() {
		return minConfidence;
	}
	public void setMinConfidence(double minConfidence) {
		this.minConfidence = minConfidence;
	}
	
	
}
