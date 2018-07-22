package routingdelivery.smartlog.containertruckmoocassigment.model;

public class SwapEmptyContainerMooc extends MoveOperator {
	public TruckRoute tr1;
	public TruckRoute tr2;
	public int i1;
	public int i21;
	public int i22;
	public double eval;// evaluation = differentiation between new value and old value
	
	// replace tr1 and tr2 by tr1(:,i1) + (tr1[i1],tr2[i21]) + tr2(i21,i22) + (tr2[i22],tr1[i1+1]) + tr1(i1+1,:)
	public SwapEmptyContainerMooc(TruckRoute tr1, TruckRoute tr2, int i1, int i21, int i22, double eval){
		this.tr1 = tr1;
		this.tr2 = tr2;
		this.i1 = i1;
		this.i21 = i21;
		this.i22 = i22;
		this.eval = eval;
	}
}
