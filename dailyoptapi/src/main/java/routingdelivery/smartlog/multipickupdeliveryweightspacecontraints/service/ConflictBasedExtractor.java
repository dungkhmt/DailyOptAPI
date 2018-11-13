package routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.service;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.ExclusiveItem;
import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.Item;
import routingdelivery.smartlog.multipickupdeliveryweightspacecontraints.model.PickupDeliveryRequest;



public class ConflictBasedExtractor {
	PickupDeliverySolver solver;
	PickupDeliveryRequest[] requests;
	ArrayList<Item> items;
	HashMap<Item, Integer> mItem2Index;
	HashMap<String, Item> mCode2Item;
	boolean[][] conflict;
	PrintWriter log = null;
	public ConflictBasedExtractor(PickupDeliverySolver solver, PrintWriter log){
		this.solver = solver;
		this.requests = solver.input.getRequests();
		this.log = log;
		mapData();
	}
	public void mapData(){
		items = new ArrayList<Item>();
		mItem2Index = new HashMap<Item, Integer>();
		mCode2Item = new HashMap<String,Item>();
		for(int i = 0; i < requests.length; i++){
			PickupDeliveryRequest r = requests[i];
			Item[] I = r.getItems();
			for(int j = 0;  j < I.length; j++){
				items.add(I[j]);
				mItem2Index.put(I[j], items.size()-1);
				mCode2Item.put(I[j].getCode(), I[j]);
			}
		}
		conflict = new boolean[items.size()][items.size()];
		for(int i = 0; i < items.size(); i++)
			for(int j = 0; j < items.size(); j++)
				conflict[i][j] = false;
		ExclusiveItem[] EI = solver.input.getExclusiveItemPairs();
		for(int k = 0; k < EI.length; k++){
			ExclusiveItem ei = EI[k];
			if(mCode2Item.get(ei.getCode1()) == null) continue;
			Item I1 = mCode2Item.get(ei.getCode1());
			if(mCode2Item.get(ei.getCode2()) == null) continue;
			Item I2 = mCode2Item.get(ei.getCode2());
			int i = mItem2Index.get(I1);
			int j = mItem2Index.get(I2);
			conflict[i][j] = true;
			conflict[j][i] = true;
			
		}
	}
	public boolean isConflict(Item I1, Item I2){
		if(mItem2Index.get(I1) == null) return false;
		if(mItem2Index.get(I2) == null) return false;
		return conflict[mItem2Index.get(I1)][mItem2Index.get(I2)];
	}
	public boolean conflict(HashSet<Item> S, Item I){
		for(Item J: S)
			if(isConflict(I, J)) return true;
		return false;
	}
	public ArrayList<PickupDeliveryRequest> splitConflictItemsOfOrder(PickupDeliveryRequest r){
		ArrayList<PickupDeliveryRequest> L = new ArrayList<PickupDeliveryRequest>();
		Item[] I = r.getItems();
		for(int i = 0; i < I.length; i++)
			for(int j = i+1; j < I.length; j++){
				if(isConflict(I[i], I[j])){
					log(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + ", DISCOVER CONFLICT-ITEMS " + 
				I[i].getCode() + "," + I[j].getCode());
				}
			}
		
		HashSet<HashSet<Item>> C = new HashSet<HashSet<Item>>();
		HashSet<Item> S = new HashSet<Item>();
		for(int i = 0; i < I.length; i++) S.add(I[i]);
		
		HashSet<Item> cluster = new HashSet<Item>();
		while(S.size() > 0){
			boolean newCluster = true;
			for(Item i: S){
				if(!conflict(cluster, i)){
					cluster.add(i);S.remove(i); newCluster = false; break;
				}
			}
			if(newCluster){
				C.add(cluster);
				cluster = new HashSet<Item>();
			}
		}
		if(cluster.size() > 0) C.add(cluster);
		for(HashSet<Item> cl: C){
			PickupDeliveryRequest ri = r.clone();
			Item[] II = new Item[cl.size()];
			int idx = -1;
			for(Item i: cl){
				idx++; II[idx] = i;
			}
			ri.setItems(II);
			L.add(ri);
		}
		if(L.size() > 1){
			log(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + ", has items:");
			System.out.println(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + ", has items:");
			for(int j = 0; j < r.getItems().length; j++){
				log(name() + "::splitConflictItemsOfOrder " + r.getItems()[j].getCode());
				System.out.println(name() + "::splitConflictItemsOfOrder " + r.getItems()[j].getCode());
			}
			
			log(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + " is splitted into " + L.size() + " orders");
			System.out.println(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + " is splitted into " + L.size() + " orders");
			for(int i = 0; i < L.size(); i++){
				PickupDeliveryRequest ri = L.get(i);
				log(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + " is splitted into " + ri.getOrderCode() + ", items:");
				System.out.println(name() + "::splitConflictItemsOfOrder, order " + r.getOrderCode() + " is splitted into " + ri.getOrderCode() + ", items:");
				for(int j = 0; j < ri.getItems().length; j++){
					log(name() + "::splitConflictItemsOfOrder " + ri.getItems()[j].getCode());
					System.out.println(name() + "::splitConflictItemsOfOrder " + ri.getItems()[j].getCode());
				}
			}
			log(name() + "::splitConflictItemsOfOrder-----------------------------------------------");
		}
		return L;
	}
	public void log(String s){
		if(log != null)
			log.println(s);
	}
	public void splitConflictItemsOfOrder(){
		ArrayList<PickupDeliveryRequest> L = new ArrayList<PickupDeliveryRequest>();
		for(int i = 0; i < requests.length; i++){
			ArrayList<PickupDeliveryRequest> Li = splitConflictItemsOfOrder(requests[i]);
			for(PickupDeliveryRequest ri: Li)
				L.add(ri);
		}
		PickupDeliveryRequest[] newR = new PickupDeliveryRequest[L.size()];
		for(int i = 0; i < L.size(); i++)
			newR[i] = L.get(i);
		solver.input.setRequests(newR);
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
