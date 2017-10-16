package com.havestplanning.model.vn;

import java.util.Date;

public class HavestPlanningClusterVN {
	private String kh_ngaydon;
	private int kh_san_luong;
	private int kh_so_ruong_mia;
	private HavestPlanningFieldVN[] fields;
	private double kh_ccs;

	
	public String getKh_ngaydon() {
		return kh_ngaydon;
	}


	public void setKh_ngaydon(String kh_ngaydon) {
		this.kh_ngaydon = kh_ngaydon;
	}


	public int getKh_san_luong() {
		return kh_san_luong;
	}


	public void setKh_san_luong(int kh_san_luong) {
		this.kh_san_luong = kh_san_luong;
	}


	public int getKh_so_ruong_mia() {
		return kh_so_ruong_mia;
	}


	public void setKh_so_ruong_mia(int kh_so_ruong_mia) {
		this.kh_so_ruong_mia = kh_so_ruong_mia;
	}


	public HavestPlanningFieldVN[] getFields() {
		return fields;
	}


	public void setFields(HavestPlanningFieldVN[] fields) {
		this.fields = fields;
	}


	public double getKh_ccs() {
		return kh_ccs;
	}


	public void setKh_ccs(double kh_ccs) {
		this.kh_ccs = kh_ccs;
	}


	public HavestPlanningClusterVN(String kh_ngaydon, int kh_san_luong,
			int kh_so_ruong_mia, HavestPlanningFieldVN[] fields, double kh_ccs) {
		super();
		this.kh_ngaydon = kh_ngaydon;
		this.kh_san_luong = kh_san_luong;
		this.kh_so_ruong_mia = kh_so_ruong_mia;
		this.fields = fields;
		this.kh_ccs = kh_ccs;
	}


	public HavestPlanningClusterVN() {
		super();
		// TODO Auto-generated constructor stub
	}

	
}
