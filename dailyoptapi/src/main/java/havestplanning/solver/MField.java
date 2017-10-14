package havestplanning.solver;

import java.util.Date;

import havestplanning.model.Field;

public class MField extends Field {
	private Date mDate;

	public Date getmDate() {
		return mDate;
	}

	public void setmDate(Date mDate) {
		this.mDate = mDate;
		
	}
	
	public MField(String code, String districtCode, String ownerCode,
			double area, String date, int quantity, int deltaDays, String plantType, String category, Date mDate){
		super(code,districtCode,ownerCode,area,date,quantity,category,plantType,deltaDays);
		
		this.mDate = mDate;
	}
}
