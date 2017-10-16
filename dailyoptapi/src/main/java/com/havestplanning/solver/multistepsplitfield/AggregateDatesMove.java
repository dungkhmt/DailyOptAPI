package com.havestplanning.solver.multistepsplitfield;

import java.util.ArrayList;
public class AggregateDatesMove {
	private ArrayList<Integer> dates;// list of dates to be aggregated to the consider date
	private int delta;// variation of violations
	private int delta_havest;
	
	public AggregateDatesMove(ArrayList<Integer> dates, int delta){
		this.dates = dates;
		this.delta = delta;
	}

	public int getDelta_havest() {
		return delta_havest;
	}

	public void setDelta_havest(int delta_havest) {
		this.delta_havest = delta_havest;
	}

	public ArrayList<Integer> getDates() {
		return dates;
	}

	public void setDates(ArrayList<Integer> dates) {
		this.dates = dates;
	}

	public int getDelta() {
		return delta;
	}

	public void setDelta(int delta) {
		this.delta = delta;
	}

	public AggregateDatesMove(ArrayList<Integer> dates, int delta,
			int delta_havest) {
		super();
		this.dates = dates;
		this.delta = delta;
		this.delta_havest = delta_havest;
	}
	
}
