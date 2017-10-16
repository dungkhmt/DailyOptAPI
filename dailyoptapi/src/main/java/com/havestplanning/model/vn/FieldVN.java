package com.havestplanning.model.vn;

import com.havestplanning.model.Field;

public class FieldVN {
	private String ma_ruongmia;
	private String ngay_trongdon;
	private float dien_tich;
	private float nang_suat;
	private float san_luong;
	private String ma_giongmia;
	private String ma_gocmia;
	private String ma_loaimia;
	private String ma_loaidat;
	private String tt_ngaydon;
	private float tt_ccs;
	
	public String convertCategory(){
		return ma_giongmia + "-" + ma_gocmia + "-" + ma_loaimia;
	}
	public Field convert(){
		int int_san_luong = (int)san_luong;
		
		return new Field(ma_ruongmia,"-","-",dien_tich,ngay_trongdon,int_san_luong,
				convertCategory(),ma_loaidat,0);
	}
	
	
	public FieldVN(String ma_ruongmia, String ngay_trongdon, float dien_tich,
			float nang_suat, float san_luong, String ma_giongmia,
			String ma_gocmia, String ma_loaimia, String ma_loaidat,
			String tt_ngaydon, float tt_ccs) {
		super();
		this.ma_ruongmia = ma_ruongmia;
		this.ngay_trongdon = ngay_trongdon;
		this.dien_tich = dien_tich;
		this.nang_suat = nang_suat;
		this.san_luong = san_luong;
		this.ma_giongmia = ma_giongmia;
		this.ma_gocmia = ma_gocmia;
		this.ma_loaimia = ma_loaimia;
		this.ma_loaidat = ma_loaidat;
		this.tt_ngaydon = tt_ngaydon;
		this.tt_ccs = tt_ccs;
	}
	public String getMa_ruongmia() {
		return ma_ruongmia;
	}
	public void setMa_ruongmia(String ma_ruongmia) {
		this.ma_ruongmia = ma_ruongmia;
	}
	public String getNgay_trongdon() {
		return ngay_trongdon;
	}
	public void setNgay_trongdon(String ngay_trongdon) {
		this.ngay_trongdon = ngay_trongdon;
	}
	public float getDien_tich() {
		return dien_tich;
	}
	public void setDien_tich(float dien_tich) {
		this.dien_tich = dien_tich;
	}
	public float getNang_suat() {
		return nang_suat;
	}
	public void setNang_suat(float nang_suat) {
		this.nang_suat = nang_suat;
	}
	public float getSan_luong() {
		return san_luong;
	}
	public void setSan_luong(float san_luong) {
		this.san_luong = san_luong;
	}
	public String getMa_giongmia() {
		return ma_giongmia;
	}
	public void setMa_giongmia(String ma_giongmia) {
		this.ma_giongmia = ma_giongmia;
	}
	public String getMa_gocmia() {
		return ma_gocmia;
	}
	public void setMa_gocmia(String ma_gocmia) {
		this.ma_gocmia = ma_gocmia;
	}
	public String getMa_loaimia() {
		return ma_loaimia;
	}
	public void setMa_loaimia(String ma_loaimia) {
		this.ma_loaimia = ma_loaimia;
	}
	public String getMa_loaidat() {
		return ma_loaidat;
	}
	public void setMa_loaidat(String ma_loaidat) {
		this.ma_loaidat = ma_loaidat;
	}
	public String getTt_ngaydon() {
		return tt_ngaydon;
	}
	public void setTt_ngaydon(String tt_ngaydon) {
		this.tt_ngaydon = tt_ngaydon;
	}
	public float getTt_ccs() {
		return tt_ccs;
	}
	public void setTt_ccs(float tt_ccs) {
		this.tt_ccs = tt_ccs;
	}
	public FieldVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
