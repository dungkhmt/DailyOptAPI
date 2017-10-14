package havestplanning.model.vn;

import havestplanning.model.PlantStandardElement;

public class PlantStandardElementVN {
	private String GIONG_MIA;
	private String GOC_MIA;
	private String LOAI_MIA;
	private String LOAI_DAT;
	private int SONGAY_TRONGDON;
	private float CCS;
	
	public String convertCategory(){
		return GIONG_MIA + "-" + GOC_MIA + "-" + LOAI_MIA;
	}
	public PlantStandardElement convert(){
		return new PlantStandardElement(convertCategory(), LOAI_DAT, SONGAY_TRONGDON, CCS);
	}
	public String getLOAI_MIA() {
		return LOAI_MIA;
	}
	public void setLOAI_MIA(String lOAI_MIA) {
		LOAI_MIA = lOAI_MIA;
	}
	public String getLOAI_DAT() {
		return LOAI_DAT;
	}
	public void setLOAI_DAT(String lOAI_DAT) {
		LOAI_DAT = lOAI_DAT;
	}
	public int getSONGAY_TRONGDON() {
		return SONGAY_TRONGDON;
	}
	public void setSONGAY_TRONGDON(int sONGAY_TRONGDON) {
		SONGAY_TRONGDON = sONGAY_TRONGDON;
	}
	public float getCCS() {
		return CCS;
	}
	public void setCCS(float cCS) {
		CCS = cCS;
	}
	public String getGIONG_MIA() {
		return GIONG_MIA;
	}
	public void setGIONG_MIA(String gIONG_MIA) {
		GIONG_MIA = gIONG_MIA;
	}
	public String getGOC_MIA() {
		return GOC_MIA;
	}
	public void setGOC_MIA(String gOC_MIA) {
		GOC_MIA = gOC_MIA;
	}
	public PlantStandardElementVN(String gIONG_MIA, String gOC_MIA,
			String lOAI_MIA, String lOAI_DAT, int sONGAY_TRONGDON, float cCS) {
		super();
		GIONG_MIA = gIONG_MIA;
		GOC_MIA = gOC_MIA;
		LOAI_MIA = lOAI_MIA;
		LOAI_DAT = lOAI_DAT;
		SONGAY_TRONGDON = sONGAY_TRONGDON;
		CCS = cCS;
	}
	public PlantStandardElementVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
