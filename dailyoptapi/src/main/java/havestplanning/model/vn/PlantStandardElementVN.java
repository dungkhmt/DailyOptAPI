package havestplanning.model.vn;

import havestplanning.model.PlantStandardElement;

public class PlantStandardElementVN {
	private String giong_mia;
	private String goc_mia;
	private String loai_mia;
	private String loai_dat;
	private int songay_trongdon;
	private float ccs;
	
	public String convertCategory(){
		return giong_mia + "-" + goc_mia + "-" + loai_mia;
	}
	public PlantStandardElement convert(){
		return new PlantStandardElement(convertCategory(), loai_dat, songay_trongdon, ccs);
	}

	
	public PlantStandardElementVN(String giong_mia, String goc_mia,
			String loai_mia, String loai_dat, int songay_trongdon, float ccs) {
		super();
		this.giong_mia = giong_mia;
		this.goc_mia = goc_mia;
		this.loai_mia = loai_mia;
		this.loai_dat = loai_dat;
		this.songay_trongdon = songay_trongdon;
		this.ccs = ccs;
	}
	public String getGiong_mia() {
		return giong_mia;
	}
	public void setGiong_mia(String giong_mia) {
		this.giong_mia = giong_mia;
	}
	public String getGoc_mia() {
		return goc_mia;
	}
	public void setGoc_mia(String goc_mia) {
		this.goc_mia = goc_mia;
	}
	public String getLoai_mia() {
		return loai_mia;
	}
	public void setLoai_mia(String loai_mia) {
		this.loai_mia = loai_mia;
	}
	public String getLoai_dat() {
		return loai_dat;
	}
	public void setLoai_dat(String loai_dat) {
		this.loai_dat = loai_dat;
	}
	public int getSongay_trongdon() {
		return songay_trongdon;
	}
	public void setSongay_trongdon(int songay_trongdon) {
		this.songay_trongdon = songay_trongdon;
	}
	public float getCcs() {
		return ccs;
	}
	public void setCcs(float ccs) {
		this.ccs = ccs;
	}
	public PlantStandardElementVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
