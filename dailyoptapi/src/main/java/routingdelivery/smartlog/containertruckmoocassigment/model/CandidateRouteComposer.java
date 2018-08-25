package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.ArrayList;

public class CandidateRouteComposer {
	public ArrayList<RouteComposer> routeComposers;
	public CandidateRouteComposer(){
		routeComposers = new ArrayList<RouteComposer>();
	}
	public void add(RouteComposer rc){
		routeComposers.add(rc);
	}
	public void clear(){
		routeComposers.clear();
	}
	public int size(){
		return routeComposers.size();
	}
	public void performBestRouteComposer(){
		double minD = Integer.MAX_VALUE;
		int sel_i = -1;
		for(int i = 0; i < size(); i++){
			RouteComposer cp = routeComposers.get(i);
			if(cp.evaluation() < minD){
				sel_i = i;
			}
		}
		if(sel_i >= 0){
			routeComposers.get(sel_i).acceptRoute();
		}
	}
}
