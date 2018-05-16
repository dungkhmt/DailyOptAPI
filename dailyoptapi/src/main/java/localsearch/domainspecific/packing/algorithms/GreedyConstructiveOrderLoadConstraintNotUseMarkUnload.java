package localsearch.domainspecific.packing.algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import localsearch.domainspecific.packing.entities.Container3D;
import localsearch.domainspecific.packing.entities.Item3D;
import localsearch.domainspecific.packing.entities.Move3D;
import localsearch.domainspecific.packing.entities.Position3D;
import localsearch.domainspecific.packing.models.Model3D;

public class GreedyConstructiveOrderLoadConstraintNotUseMarkUnload extends
		GreedyConstructiveOrderLoadConstraintNotUseMark {
	protected ArrayList<Item3D> loadedItems;
	protected ArrayList<Position3D> positions;// positions[i] is the position of loadedItems[i];
	protected HashMap<Item3D, Integer> mLoadedItem2Index;
	
	
	public GreedyConstructiveOrderLoadConstraintNotUseMarkUnload(Model3D model){
		super(model);
		mLoadedItem2Index = new HashMap<Item3D, Integer>();
	}
	public ArrayList<Position3D> findLoadPositions(ArrayList<Item3D> items){
		// try to find possible positions for loading items
		ArrayList<Position3D> positions = new ArrayList<Position3D>();
		backup();

		for (int i = 0; i < items.size(); i++) {
			Item3D item = items.get(i);
			ArrayList<Move3D> moves = new ArrayList<Move3D>();

			for (Position3D p : candidate_positions) {
				int w = item.getWidth();
				int l = item.getLength();
				int h = item.getHeight();

				// generate all permutations of (w,l,h)
				RotationGenerator RG = new RotationGenerator(w, l, h);
				RG.generate();
				ArrayList<Item3D> gen_items = RG.getItems();

				for (Item3D I : gen_items) {
					// System.out.println("Consider item " + item +
					// " with permutation " + I);
					if (feasiblePosition(p.getX_w(), p.getX_l(), p.getX_h(),
							I.getWidth(), I.getLength(), I.getHeight())) {
						moves.add(new Move3D(p, I.getWidth(), I.getLength(), I
								.getHeight(), item.getItemID()));
					}
				}
			}

			if (moves.size() <= 0) {// cannot load, restore and return false
				restore();
				return null;
			} else {
				//Move3D sel_move = selectBest(moves);
				Move3D sel_move = trySelectBest(moves);
				Position3D sel_p = sel_move.getPosition();
				//tryPlace(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
				place(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
				//solution.add(sel_move);
				positions.add(sel_p);
			}

		}

		restore();
		
		return positions;
	}
	public Position3D findLoadPositions(Item3D item){
		// try to find possible positions for loading items
		//ArrayList<Position3D> positions = new ArrayList<Position3D>();
		//backup();
		
			ArrayList<Move3D> moves = new ArrayList<Move3D>();

			for (Position3D p : candidate_positions) {
				int w = item.getWidth();
				int l = item.getLength();
				int h = item.getHeight();

				// generate all permutations of (w,l,h)
				RotationGenerator RG = new RotationGenerator(w, l, h);
				RG.generate();
				ArrayList<Item3D> gen_items = RG.getItems();

				for (Item3D I : gen_items) {
					// System.out.println("Consider item " + item +
					// " with permutation " + I);
					if (feasiblePosition(p.getX_w(), p.getX_l(), p.getX_h(),
							I.getWidth(), I.getLength(), I.getHeight())) {
						moves.add(new Move3D(p, I.getWidth(), I.getLength(), I
								.getHeight(), item.getItemID()));
					}
				}
			}

			if (moves.size() <= 0) {// cannot load, restore and return false
				//restore();
				return null;
			} else {
				//Move3D sel_move = selectBest(moves);
				Move3D sel_move = trySelectBest(moves);
				Position3D sel_p = sel_move.getPosition();
				return sel_p;
				//tryPlace(sel_move.getW(), sel_move.getL(), sel_move.getH(), sel_p);
				//solution.add(sel_move);
				//positions.add(sel_p);
			}

		
		//restore();
		
		
	}

	public void load(Item3D item, Position3D p){
		loadedItems.add(item);
		positions.add(p);
		mLoadedItem2Index.put(item,loadedItems.size()-1);
		
		//place(item.getWidth(), item.getLength(), item.getHeight(), p);
		candidate_positions.remove(candidate_positions.indexOf(p));
		place(item,p);
	}
	public void init(){
		super.init();
		loadedItems = new ArrayList<Item3D>();
		positions = new ArrayList<Position3D>();
	}
	protected void reset(){
		// reset data structure for recomputation
		candidate_positions.clear();
		LW.clear();
		LL.clear();
		LH.clear();
		Arrays.fill(markW, false);
		Arrays.fill(markL, false);
		Arrays.fill(markH, false);
		initCandidatePositions();
	}
	public Position3D getCandidatePosition(int xw, int xl, int xh){
		for(Position3D p: candidate_positions)if(p.equals(xw, xl, xh)){
			return p;
		}
		return null;
	}
	public void unload(Item3D item){
		//int idx = mLoadedItem2Index.get(item);
		reset();
		ArrayList<Item3D> tmpItem = new ArrayList<Item3D>();
		ArrayList<Position3D> tmpPositions = new ArrayList<Position3D>();
		for(int i = 0; i < loadedItems.size(); i++)if(loadedItems.get(i) != item){
			tmpItem.add(loadedItems.get(i));
			tmpPositions.add(positions.get(i));
			System.out.println("unload, tmp add item " + loadedItems.get(i).toString() + ", position = " + positions.get(i).toString());
		}
		
		//loadedItems.remove(idx);
		//positions.remove(idx);
		loadedItems.clear();
		positions.clear();
		for(int i = 0; i < tmpItem.size(); i++){
			// load and update data structures
			Position3D p = tmpPositions.get(i);
			Position3D pp = getCandidatePosition(p.getX_w(),p.getX_l(),p.getX_h());
			load(tmpItem.get(i),pp);
			//place(loadedItems.get(i),positions.get(i));
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Item3D[] items = new Item3D[5];
		items[0] = new Item3D(0,6,4,0);
		items[1] = new Item3D(0,2,8,0);
		items[2] = new Item3D(0,6,6,0);
		items[3] = new Item3D(0,4,8,0);
		items[4] = new Item3D(0,6,5,0);
		Container3D container = new Container3D(8, 12, 1);
		Model3D model = new Model3D(container, items);
		GreedyConstructiveOrderLoadConstraintNotUseMarkUnload S = 
				new GreedyConstructiveOrderLoadConstraintNotUseMarkUnload(model);
		
		S.init();
		
		ArrayList<Item3D> I = new ArrayList<Item3D>();
		I.add(items[0]);
		ArrayList<Position3D> pos = S.findLoadPositions(I);
		System.out.println("feasible positions: ");
		for(Position3D p: pos){
			System.out.println(p.toString());
		}
		S.load(items[0], pos.get(0));
		for(Position3D p: S.getCandidatePositions()){
			System.out.println("cand pos = " + p.toString());
		}
		System.out.println("---------------------");
		
		I.clear(); I.add(items[1]);
		pos = S.findLoadPositions(I);
		System.out.println("feasible positions: ");

		for(Position3D p: pos){
			System.out.println(p.toString());
		}
		S.load(items[1], pos.get(0));
		for(Position3D p: S.getCandidatePositions()){
			System.out.println("cand pos = " + p.toString());
		}
		System.out.println("---------------------");
		
		I.clear(); I.add(items[2]);
		pos = S.findLoadPositions(I);
		System.out.println("feasible positions: ");

		for(Position3D p: pos){
			System.out.println(p.toString());
		}
		S.load(items[2], pos.get(0));
		for(Position3D p: S.getCandidatePositions()){
			System.out.println("cand pos = " + p.toString());
		}
		System.out.println("---------------------");

		S.unload(items[2]);
		for(Position3D p: S.getCandidatePositions()){
			System.out.println("cand pos = " + p.toString());
		}
		System.out.println("---------------------");

		S.unload(items[1]);
		for(Position3D p: S.getCandidatePositions()){
			System.out.println("cand pos = " + p.toString());
		}
		System.out.println("---------------------");

	}

}
