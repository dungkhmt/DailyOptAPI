package routingdelivery.smartlog.brenntag.service;

import java.util.ArrayList;
import java.util.HashSet;

import routingdelivery.model.Item;
import routingdelivery.model.PickupDeliveryInput;
import routingdelivery.model.PickupDeliveryRequest;
import routingdelivery.service.PickupDeliverySolver;

public class ConflictBasedExtractor {
	PickupDeliverySolver solver;
	PickupDeliveryRequest[] requests;
	public ConflictBasedExtractor(PickupDeliverySolver solver){
		this.solver = solver;
		this.requests = solver.input.getRequests();
	}
	public boolean ItemConflict(Item I1, Item I2){
		int i1 = solver.mItemCode2Index.get(I1.getCode());
		int i2 = solver.mItemCode2Index.get(I2.getCode());
		return solver.itemConflict[i1][i2];
	}
	public boolean requestConflict(PickupDeliveryRequest r1, PickupDeliveryRequest r2){
		for(int i = 0; i < r1.getItems().length; i++){
			for(int j = 0; j < r2.getItems().length; j++){
				if(ItemConflict(r1.getItems()[i], r2.getItems()[j])) return true;
			}
		}
		return false;
	}
	public int getNbConflictRequests(int r, HashSet<Integer> R){
		int rs = 0;
		for(int i: R) if(requestConflict(requests[r], requests[i]))rs++;
		return rs;
	}
	public int getNbNonConflictRequests(int r, HashSet<Integer> R){
		int rs = 0;
		for(int i: R) if(r != i && !requestConflict(requests[r], requests[i]))rs++;
		return rs;
	}
	public int getMaxNonConflict(HashSet<Integer> R){
		int sel_r = -1;
		int maxNonConflict = -1;
		for(int r: R){
			int nb = getNbNonConflictRequests(r, R);
			if(nb > maxNonConflict){
				maxNonConflict = nb;
				sel_r = r;
			}
		}
		return sel_r;
	}
	public boolean conflict(int i, HashSet<Integer> I){
		for(int j: I) if(requestConflict(requests[i], requests[j])) return true;
		return false;
	}
	public String name(){
		return "ConflictBasedExtractor";
	}
	public ArrayList<HashSet<Integer>> clusterRequestBasedItemConflict(HashSet<Integer> R){
		ArrayList<HashSet<Integer>> L = new ArrayList<HashSet<Integer>>();
		HashSet<Integer> tmp = new HashSet<Integer>();
		for(int i: R) tmp.add(i);
		
		System.out.print(name() + "::clusterRequestBasedItemConflict, R = ");
		for(int i: tmp) System.out.print(i + ", "); System.out.println();
		
		while(tmp.size() > 0){
			HashSet<Integer> C = new HashSet<Integer>();
			int first_r = getMaxNonConflict(tmp);
			C.add(first_r);
			System.out.println(name() + "::clusterRequestBasedItemConflict, first_r = " + first_r);
			tmp.remove(first_r);
			while(true){
				int sel_i = -1;
				for(int i: tmp){
					if(!conflict(i, C)){
						sel_i = i; break;
					}
				}
				if(sel_i == -1) break;
				C.add(sel_i); tmp.remove(sel_i);
				System.out.println(name() + "::clusterRequestBasedItemConflict, ACCEPT sel_i = " + sel_i);
			}
			
			L.add(C);
			System.out.println(name() + "::clusterRequestBasedItemConflict, FINISHED new C, sz = " + C.size() + ", L.sz = " + L.size());
		}
		
		return L;
	}
}
