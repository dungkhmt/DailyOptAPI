package havestplanning.model.vn;

import java.util.Date;

public class HavestPlanningFieldVN {
	private String ma_ruongmia;
	private String ngay_trongdon;
	private float dien_tich;
	private float nang_suat;
	private float san_luong;
	private String kh_ngaydon;
	private float kh_ccs;

	
	
	public HavestPlanningFieldVN(String ma_ruongmia, String ngay_trongdon,
			float dien_tich, float nang_suat, float san_luong,
			String kh_ngaydon, float kh_ccs) {
		super();
		this.ma_ruongmia = ma_ruongmia;
		this.ngay_trongdon = ngay_trongdon;
		this.dien_tich = dien_tich;
		this.nang_suat = nang_suat;
		this.san_luong = san_luong;
		this.kh_ngaydon = kh_ngaydon;
		this.kh_ccs = kh_ccs;
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



	public String getKh_ngaydon() {
		return kh_ngaydon;
	}



	public void setKh_ngaydon(String kh_ngaydon) {
		this.kh_ngaydon = kh_ngaydon;
	}



	public float getKh_ccs() {
		return kh_ccs;
	}



	public void setKh_ccs(float kh_ccs) {
		this.kh_ccs = kh_ccs;
	}



	public HavestPlanningFieldVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
