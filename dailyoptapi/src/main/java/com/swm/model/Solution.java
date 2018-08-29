package com.swm.model;
import java.util.ArrayList;

public class Solution {
	private int ruleNum;
	private double support;
	private double confidence;
	private ArrayList<Rule> ruleLst;
	
	public Solution(int ruleNum, double support, double confidence, ArrayList<Rule> ruleLst) {
		super();
		this.ruleNum = ruleNum;
		this.support = support;
		this.confidence = confidence;
		this.ruleLst = ruleLst;
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

	public ArrayList<Rule> getRuleLst() {
		return ruleLst;
	}

	public void setRuleLst(ArrayList<Rule> ruleLst) {
		this.ruleLst = ruleLst;
	}

}
