package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model;

import localsearch.domainspecific.vehiclerouting.vrp.IFunctionVR;
import localsearch.domainspecific.vehiclerouting.vrp.VRManager;
import localsearch.domainspecific.vehiclerouting.vrp.VarRoutesVR;
import localsearch.domainspecific.vehiclerouting.vrp.entities.ArcWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.entities.NodeWeightsManager;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightEdgesVR;
import localsearch.domainspecific.vehiclerouting.vrp.invariants.AccumulatedWeightNodesVR;

public class ModelRoute {
	public VRManager mgr;
	public VarRoutesVR XR;
	public ArcWeightsManager awm;
	public NodeWeightsManager nwm;
	public AccumulatedWeightEdgesVR awe;
	public AccumulatedWeightNodesVR awn;
	public IFunctionVR cost;
}
