package com.havestplanning.model;

public class FieldClusterIndices {
	private FieldCluster[] cluster;
	private int startIdx;
	private int endIdx;
	public FieldCluster[] getCluster() {
		return cluster;
	}
	public void setCluster(FieldCluster[] cluster) {
		this.cluster = cluster;
	}
	public int getStartIdx() {
		return startIdx;
	}
	public void setStartIdx(int startIdx) {
		this.startIdx = startIdx;
	}
	public int getEndIdx() {
		return endIdx;
	}
	public void setEndIdx(int endIdx) {
		this.endIdx = endIdx;
	}
	public FieldClusterIndices(FieldCluster[] cluster, int startIdx, int endIdx) {
		super();
		this.cluster = cluster;
		this.startIdx = startIdx;
		this.endIdx = endIdx;
	}
	
}
