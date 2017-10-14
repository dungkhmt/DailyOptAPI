package havestplanning.model.vn;

import java.util.Date;

public class HavestPlanningFieldVN {
	private String MA_RUONGMIA;
	private String NGAY_TRONGDON;
	private float DIEN_TICH;
	private float NANG_SUAT;
	private float SAN_LUONG;
	private String KH_NGAYDON;
	private float KH_CCS;
	public String getMA_RUONGMIA() {
		return MA_RUONGMIA;
	}
	public void setMA_RUONGMIA(String mA_RUONGMIA) {
		MA_RUONGMIA = mA_RUONGMIA;
	}
	public String getNGAY_TRONGDON() {
		return NGAY_TRONGDON;
	}
	public void setNGAY_TRONGDON(String nGAY_TRONGDON) {
		NGAY_TRONGDON = nGAY_TRONGDON;
	}
	public float getDIEN_TICH() {
		return DIEN_TICH;
	}
	public void setDIEN_TICH(float dIEN_TICH) {
		DIEN_TICH = dIEN_TICH;
	}
	public float getNANG_SUAT() {
		return NANG_SUAT;
	}
	public void setNANG_SUAT(float nANG_SUAT) {
		NANG_SUAT = nANG_SUAT;
	}
	public float getSAN_LUONG() {
		return SAN_LUONG;
	}
	public void setSAN_LUONG(float sAN_LUONG) {
		SAN_LUONG = sAN_LUONG;
	}
	public String getKH_NGAYDON() {
		return KH_NGAYDON;
	}
	public void setKH_NGAYDON(String kH_NGAYDON) {
		KH_NGAYDON = kH_NGAYDON;
	}
	public float getKH_CCS() {
		return KH_CCS;
	}
	public void setKH_CCS(float kH_CCS) {
		KH_CCS = kH_CCS;
	}
	public HavestPlanningFieldVN(String mA_RUONGMIA, String nGAY_TRONGDON,
			float dIEN_TICH, float nANG_SUAT, float sAN_LUONG,
			String kH_NGAYDON, float kH_CCS) {
		super();
		MA_RUONGMIA = mA_RUONGMIA;
		NGAY_TRONGDON = nGAY_TRONGDON;
		DIEN_TICH = dIEN_TICH;
		NANG_SUAT = nANG_SUAT;
		SAN_LUONG = sAN_LUONG;
		KH_NGAYDON = kH_NGAYDON;
		KH_CCS = kH_CCS;
	}
	public HavestPlanningFieldVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
	
}
