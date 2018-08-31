package routingdelivery.smartlog.containertruckmoocassigment.model;

import java.util.ArrayList;

public class TruckItinerary {
	private ArrayList<TruckRoute> routes;
	private ArrayList<Integer> lastUsedIndex;// lastUsedIndex.get(i) is the
												// index of the last
												// RouteElement used
												// in the TruckRoute
												// routes.get(i);
												// by default,
												// lastUsedIndex.get(i) =
												// routes.get(i).size() - 1
												// if routes.get(i) has its
												// successor routes.get(i+1),
												// then
												// lastUsedIndex.get(i) may be
												// different from
												// routes.get(i).size() -1
												// as the truck does not return
												// to the depot, instead it goes
												// to the
												// successor routes.get(i+1)
												// from lastUsedIndex.get(i)

	public ArrayList<TruckRoute> getRoutes() {
		return routes;
	}

	public int indexOf(RouteElement e) {
		if (routes == null)
			return -1;
		for (int i = 0; i < routes.size(); i++) {
			int idx = routes.get(i).indexOf(e);
			if (idx >= 0)
				return idx;
		}
		return -1;
	}

	public TruckRoute remove() {
		if (routes == null || routes.size() == 0)
			return null;
		TruckRoute tr = getLastTruckRoute();
		routes.remove(routes.size() - 1);
		lastUsedIndex.remove(lastUsedIndex.size() - 1);
		lastUsedIndex.remove(lastUsedIndex.size() - 1);
		lastUsedIndex.add(getLastTruckRoute().getNodes().length - 1);
		return tr;
	}

	public TruckRoute getLastTruckRoute() {
		if (routes == null || routes.size() == 0)
			return null;
		return routes.get(size() - 1);
	}

	public void setRoutes(ArrayList<TruckRoute> routes) {
		this.routes = routes;
	}

	public ArrayList<Integer> getLastUsedIndex() {
		return lastUsedIndex;
	}

	public void setLastUsedIndex(ArrayList<Integer> lastUsedIndex) {
		this.lastUsedIndex = lastUsedIndex;
	}

	public TruckItinerary() {
		super();
		// TODO Auto-generated constructor stub
		routes = new ArrayList<TruckRoute>();
		lastUsedIndex = new ArrayList<Integer>();
	}

	public int size() {
		return routes.size();
	}

	public void addRoute(TruckRoute r, int lastIndex) {

		// update lastUsedIndex of the last route
		if (size() > 0) {
			if (lastIndex >= 0) {
				lastUsedIndex.remove(lastUsedIndex.size() - 1);
				lastUsedIndex.add(lastIndex);
			}
		}

		routes.add(r);

		// add lastUsedIndex of the new route r
		lastUsedIndex.add(r.length() - 1);
	}

	public TruckRoute establishTruckRoute() {
		if (routes.size() == 0)
			return null;
		Truck truck = routes.get(0).getTruck();
		ArrayList<RouteElement> L = new ArrayList<RouteElement>();

		for (int i = 0; i < routes.size(); i++) {
			int lastIndex = lastUsedIndex.get(i);
			TruckRoute r = routes.get(i);
			for (int j = 0; j <= lastIndex; j++) {
				L.add(r.getNodes()[j]);
			}
		}

		RouteElement[] nodes = new RouteElement[L.size()];
		for (int i = 0; i < L.size(); i++)
			nodes[i] = L.get(i);
		return new TruckRoute(truck, nodes);
	}
}
