package havestplanning.model.vn;

import java.util.Date;

public class HavestPlanningClusterVN {
	private String KH_NGAYDON;
	private int KH_SAN_LUONG;
	private int KH_SO_RUONG_MIA;
	private HavestPlanningFieldVN[] fields;
	private double KH_CCS;
	public String getKH_NGAYDON() {
		return KH_NGAYDON;
	}
	public void setKH_NGAYDON(String kH_NGAYDON) {
		KH_NGAYDON = kH_NGAYDON;
	}
	public int getKH_SAN_LUONG() {
		return KH_SAN_LUONG;
	}
	public void setKH_SAN_LUONG(int kH_SAN_LUONG) {
		KH_SAN_LUONG = kH_SAN_LUONG;
	}
	public int getKH_SO_RUONG_MIA() {
		return KH_SO_RUONG_MIA;
	}
	public void setKH_SO_RUONG_MIA(int kH_SO_RUONG_MIA) {
		KH_SO_RUONG_MIA = kH_SO_RUONG_MIA;
	}
	public HavestPlanningFieldVN[] getFields() {
		return fields;
	}
	public void setFields(HavestPlanningFieldVN[] fields) {
		this.fields = fields;
	}
	public double getKH_CCS() {
		return KH_CCS;
	}
	public void setKH_CCS(double kH_CCS) {
		KH_CCS = kH_CCS;
	}
	public HavestPlanningClusterVN(String kH_NGAYDON, int kH_SAN_LUONG,
			int kH_SO_RUONG_MIA, HavestPlanningFieldVN[] fields, double kH_CCS) {
		super();
		KH_NGAYDON = kH_NGAYDON;
		KH_SAN_LUONG = kH_SAN_LUONG;
		KH_SO_RUONG_MIA = kH_SO_RUONG_MIA;
		this.fields = fields;
		KH_CCS = kH_CCS;
	}
	public HavestPlanningClusterVN() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
