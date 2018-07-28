package routingdelivery.smartlog.brenntag.model;

import java.util.ArrayList;

import routingdelivery.model.Vehicle;
import routingdelivery.smartlog.brenntag.service.BrenntagPickupDeliverySolver;
import routingdelivery.smartlog.brenntag.service.Trip;

public class GreedyMatchingVehicleTrip {
	private Trip[][] A;
	private Vehicle[] vehicles;
	private ArrayList<ClusterItems> clusterItems;
	private int n;
	private int m;
	private boolean[] used_vehicle;
	private boolean[] used_cluster;
	private int[] d_vehicles;
	private int[] d_clusters;
	private ArrayList<Integer> s_vehicles;
	private ArrayList<Integer> s_clusters;
	
	BrenntagPickupDeliverySolver solver = null;
	
	public GreedyMatchingVehicleTrip(BrenntagPickupDeliverySolver solver){
		this.solver = solver;
	}
	private int selectMinDegreeNotUsedVehicle(int sel_c){
		int min = Integer.MAX_VALUE;
		int sel_v = -1;
		for(int i = 0; i < n; i++){
			if(!used_vehicle[i] && A[i][sel_c] != null){
				if(d_vehicles[i] < min){
					min = d_vehicles[i];
					sel_v = i;
				}
			}
		}
		return sel_v;
	}
	private int selectMinDegreeNotUsedCluster(){
		int min = Integer.MAX_VALUE;
		int sel_c = -1;
		//solver.log.println(name() + "::selectMinDegreeNotUsedCluster, m= " + m);
		for(int i = 0; i < m; i++){
			//solver.log.println(name() + "::selectMinDegreeNotUsedCluster, i = " + i + 
			//		", !used_cluster = " + (!used_cluster[i]) + ", d_cluster = " + d_clusters[i] + ", min = " + min);
			if(!used_cluster[i]){
				if(d_clusters[i] < min){
					min = d_clusters[i];
					sel_c = i;
				//	solver.log.println(name() + "::selectMinDegreeNotUsedCluster, i = " + i + 
				//			", !used_cluster = " + (!used_cluster[i]) + ", d_cluster = " + d_clusters[i] + 
				//			", min = " + min + ", update sel_c = " + sel_c + ", min = " + min);
							
				}
			}
		}
		return sel_c;
	}
	
	public void solve(Trip[][] A, ArrayList<ClusterItems> clusterItems, Vehicle[] vehicles){
		this.A = A;
		this.clusterItems = clusterItems;
		this.vehicles = vehicles;
		n = vehicles.length;
		m = clusterItems.size();
		used_vehicle = new boolean[n];
		used_cluster = new boolean[m];
		d_vehicles = new int[n];
		d_clusters = new int[m];
		for(int i = 0; i < n; i++){
			used_vehicle[i]=false;
			d_vehicles[i] = 0;
		}
		for(int j = 0; j < m; j++){
			used_cluster[j] = false;
			d_clusters[j] = 0;
		}
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m; j++){
				if(A[i][j] != null){
					d_vehicles[i]++;
					//d_clusters[j]++;
				}
			}
		}
		for(int j = 0; j < m; j++){
			for(int i = 0; i < n; i++){
				if(A[i][j] != null){
					d_clusters[j]++;
				}
			}
		}
		//for(int i = 0; i < n; i++){
		//	solver.log.println(name() + "::solve, d_vehicle[" + i + "] = " + d_vehicles[i] + ", used_vehicle[" + i + "] = " + used_vehicle[i]);
		//}
		//for(int j = 0; j < n; j++){
		//	solver.log.println(name() + "::solve, d_cluster[" + j + "] = " + d_clusters[j] + 
		//			", used_clsuter[" + j + "] = " + used_cluster[j]);
		//}

		s_vehicles = new ArrayList<Integer>();
		s_clusters = new ArrayList<Integer>();
		for(int i = 0; i < n; i++){
			for(int j = 0; j < m ;j++){
				int x = 0;
				if(A[i][j] != null) x = 1;
				solver.log.print(x + " ");
			}
			solver.log.println();
		}
		while(true){
			int sel_c = selectMinDegreeNotUsedCluster();
			//solver.log.println(name() + "::solve, sel_c = " + sel_c);
			if(sel_c < 0) break;
			//System.out.println(name() + "::solve, sel_c = " + sel_c);
			
			int sel_v = selectMinDegreeNotUsedVehicle(sel_c);
			//System.out.println(name() + "::solve, sel_v(" + sel_c + ") = " + sel_v);
			//solver.log.println(name() + "::solve, sel_v(" + sel_c + ") = " + sel_v);
			if(sel_v >= 0){
				s_vehicles.add(sel_v);
				s_clusters.add(sel_c);
				used_vehicle[sel_v] = true;
			}else{
				
			}
			used_cluster[sel_c] = true;
		}
	}
	public String name(){
		return "GreedyMatchingVehicleCluster";
	}
	public ArrayList<Integer> getSolutionCluster(){
		return s_clusters;
	}
	public ArrayList<Integer> getSolutionVehicle(){
		return s_vehicles;
	}
}
