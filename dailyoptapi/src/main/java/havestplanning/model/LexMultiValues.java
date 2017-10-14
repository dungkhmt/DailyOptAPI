package havestplanning.model;

import java.util.ArrayList;

public class LexMultiValues {
	private ArrayList<Integer> values;
	public LexMultiValues(ArrayList<Integer> values){
		this.values = values;
	}
	public LexMultiValues(int v1, int v2, int v3){
		values = new ArrayList<Integer>();
		values.add(v1);
		values.add(v2);
		values.add(v3);
	}
	public void set(int v1, int v2, int v3){
		values.set(0, v1);
		values.set(1, v2);
		values.set(2, v3);
	}
	public void add(int e){
		values.add(e);
	}
	public void reset(){
		values.clear();
	}
	public int get(int i){
		return values.get(i);
	}
	public int size(){ return values.size();}
	public int compare(LexMultiValues V){
		for(int i = 0; i < values.size(); i++){
			if(values.get(i) > V.get(i)) return 1;
			else if(values.get(i) < V.get(i)) return -1;
		}
		return 0;
	}
	public String toString(){
		String s = "";
		for(int i = 0; i < values.size(); i++)
			s += values.get(i) + ", ";
		return s;
	}
}
