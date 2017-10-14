package havestplanning.model;

import java.util.*;
public class FieldCluster {
	public FieldCluster(){
		list = new ArrayList<Integer>();
	}
	public FieldCluster(ArrayList<Integer> L){
		list = new ArrayList<Integer>();
		for(int i: L)
			list.add(i);
	}
	private ArrayList<Integer> list;
	private int weight;
	
	public ArrayList<Integer> getList() {
		return list;
	}
	public void setList(ArrayList<Integer> list) {
		this.list = list;
	}
	public int getWeight() {
		return weight;
	}
	public void setWeight(int weight) {
		this.weight = weight;
	}
	public void add(int e){
		list.add(e);
	}
	public void remove(int e){
		int idx = list.indexOf(e);
		if(idx >= 0)
			list.remove(idx);
	}
	public int get(int j){
		return list.get(j);
	}
	public int size(){
		return list.size();
	}
}
