package com.swm.model;

public class GetTransactionOutput {
	private int ruleNum;
	private double support;
	private double confidence;
	private Rule[] rules;
	private String msg;
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public int getRuleNum() {
		return ruleNum;
	}
	public void setRuleNum(int ruleNum) {
		this.ruleNum = ruleNum;
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
	public Rule[] getRules() {
		return rules;
	}
	public void setRules(Rule[] rules) {
		this.rules = rules;
	}
	public GetTransactionOutput(int ruleNum, double support, double confidence,
			Rule[] rules) {
		super();
		this.ruleNum = ruleNum;
		this.support = support;
		this.confidence = confidence;
		this.rules = rules;
	}
	public GetTransactionOutput() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
