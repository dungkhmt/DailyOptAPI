package com.swm.model;
public class Rule {
	private String consequence ;
	private String premise;
	private double support;
	private double confidence;
	private double lift;
	
	public Rule(String consequence , String premise, double support, double confidence, double lift) {
		super();
		this.consequence  = consequence ;
		this.premise = premise;
		this.support = support;
		this.confidence = confidence;
		this.lift = lift;
	}

	public String getConsequence () {
		return consequence ;
	}

	public void setConsequence (String consequence ) {
		this.consequence  = consequence ;
	}

	public String getPremise() {
		return premise;
	}

	public void setPremise(String premise) {
		this.premise = premise;
	}

	public double getSupport() {
		return support;
	}

	public void setSupport(double support) {
		this.support = support;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = confidence;
	}

	public double getLift() {
		return lift;
	}

	public void setLift(float lift) {
		this.lift = lift;
	}
	
	

}
