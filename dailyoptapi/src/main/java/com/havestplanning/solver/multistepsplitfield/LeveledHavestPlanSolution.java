package com.havestplanning.solver.multistepsplitfield;

public class LeveledHavestPlanSolution {
	protected int[] xd;// xd[i] is the date field i is havested
	protected int[] quantity; //quantity[i] is the amount/quantity of field i havested
								// field i is havested in date xd[i] and amount quantity[i]
	public int[] getXd() {
		return xd;
	}
	public void setXd(int[] xd) {
		this.xd = xd;
	}
	public int[] getQuantity() {
		return quantity;
	}
	public void setQuantity(int[] quantity) {
		this.quantity = quantity;
	}
	public LeveledHavestPlanSolution() {
		super();
		// TODO Auto-generated constructor stub
	}
	public LeveledHavestPlanSolution(int[] xd, int[] quantity) {
		super();
		this.xd = xd;
		this.quantity = quantity;
	}
	
	public String toString(){
		String s = "";
		for(int i = 0; i < xd.length; i++){
			s += "(xd[" + i + "] = " + xd[i] + ", qtt = " + quantity[i] + ") ";
		}
		
		return s;
	}
	
}
