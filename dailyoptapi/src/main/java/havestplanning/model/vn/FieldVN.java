package havestplanning.model.vn;

import havestplanning.model.Field;

public class FieldVN {
	private String MA_RUONGMIA;
	private String NGAY_TRONGDON;
	private float DIEN_TICH;
	private float NANG_SUAT;
	private float SAN_LUONG;
	private String MA_GIONGMIA;
	private String MA_GOCMIA;
	private String MA_LOAIMIA;
	private String MA_LOAIDAT;
	private String TT_NGAYDON;
	private float TT_CCS;
	
	public String convertCategory(){
		return MA_GIONGMIA + "-" + MA_GOCMIA + "-" + MA_LOAIMIA;
	}
	public Field convert(){
		int int_san_luong = (int)SAN_LUONG;
		
		return new Field(MA_RUONGMIA,"-","-",DIEN_TICH,NGAY_TRONGDON,int_san_luong,
				convertCategory(),MA_LOAIDAT,0);
	}
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
	public String getMA_GIONGMIA() {
		return MA_GIONGMIA;
	}
	public void setMA_GIONGMIA(String mA_GIONGMIA) {
		MA_GIONGMIA = mA_GIONGMIA;
	}
	public String getMA_GOCMIA() {
		return MA_GOCMIA;
	}
	public void setMA_GOCMIA(String mA_GOCMIA) {
		MA_GOCMIA = mA_GOCMIA;
	}
	public String getMA_LOAIMIA() {
		return MA_LOAIMIA;
	}
	public void setMA_LOAIMIA(String mA_LOAIMIA) {
		MA_LOAIMIA = mA_LOAIMIA;
	}
	public String getMA_LOAIDAT() {
		return MA_LOAIDAT;
	}
	public void setMA_LOAIDAT(String mA_LOAIDAT) {
		MA_LOAIDAT = mA_LOAIDAT;
	}
	public String getTT_NGAYDON() {
		return TT_NGAYDON;
	}
	public void setTT_NGAYDON(String tT_NGAYDON) {
		TT_NGAYDON = tT_NGAYDON;
	}
	public float getTT_CCS() {
		return TT_CCS;
	}
	public void setTT_CCS(float tT_CCS) {
		TT_CCS = tT_CCS;
	}
	public FieldVN(String mA_RUONGMIA, String nGAY_TRONGDON, float dIEN_TICH,
			float nANG_SUAT, float sAN_LUONG, String mA_GIONGMIA,
			String mA_GOCMIA, String mA_LOAIMIA, String mA_LOAIDAT,
			String tT_NGAYDON, float tT_CCS) {
		super();
		MA_RUONGMIA = mA_RUONGMIA;
		NGAY_TRONGDON = nGAY_TRONGDON;
		DIEN_TICH = dIEN_TICH;
		NANG_SUAT = nANG_SUAT;
		SAN_LUONG = sAN_LUONG;
		MA_GIONGMIA = mA_GIONGMIA;
		MA_GOCMIA = mA_GOCMIA;
		MA_LOAIMIA = mA_LOAIMIA;
		MA_LOAIDAT = mA_LOAIDAT;
		TT_NGAYDON = tT_NGAYDON;
		TT_CCS = tT_CCS;
	}
	public FieldVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	
}
