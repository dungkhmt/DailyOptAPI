package com.havestplanning.model.vn;

public class RunParametersVN {
	private int thoi_gian_chay;
	private String ngay_bat_dau;
	public int getThoi_gian_chay() {
		return thoi_gian_chay;
	}
	public void setThoi_gian_chay(int thoi_gian_chay) {
		this.thoi_gian_chay = thoi_gian_chay;
	}
	public String getNgay_bat_dau() {
		return ngay_bat_dau;
	}
	public void setNgay_bat_dau(String ngay_bat_dau) {
		this.ngay_bat_dau = ngay_bat_dau;
	}
	public RunParametersVN(int thoi_gian_chay, String ngay_bat_dau) {
		super();
		this.thoi_gian_chay = thoi_gian_chay;
		this.ngay_bat_dau = ngay_bat_dau;
	}
	public RunParametersVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
