package havestplanning.model.vn;

public class RunParametersVN {
	private int thoi_gian_chay;
	private String ngay_dat_dau;
	public int getThoi_gian_chay() {
		return thoi_gian_chay;
	}
	public void setThoi_gian_chay(int thoi_gian_chay) {
		this.thoi_gian_chay = thoi_gian_chay;
	}
	public String getNgay_dat_dau() {
		return ngay_dat_dau;
	}
	public void setNgay_dat_dau(String ngay_dat_dau) {
		this.ngay_dat_dau = ngay_dat_dau;
	}
	public RunParametersVN(int thoi_gian_chay, String ngay_dat_dau) {
		super();
		this.thoi_gian_chay = thoi_gian_chay;
		this.ngay_dat_dau = ngay_dat_dau;
	}
	public RunParametersVN() {
		super();
		// TODO Auto-generated constructor stub
	}
	
}
