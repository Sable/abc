package abc.weaving.residues;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import soot.Local;
import soot.SootMethod;
import soot.jimple.Stmt;
import soot.util.Chain;
import abc.soot.util.LocalGeneratorEx;
import abc.weaving.weaver.WeavingContext;

/** The base class defining dynamic residues of pointcuts
 *  @author Ganesh Sittampalam
 *  @date 28-Apr-04
 */ 

public abstract class Residue {
    /** Generate the code for this dynamic residue.
     *  @param method The method the code is being inserted into
     *  @param localgen A local generator for the method
     *  @param units The chain the code is being inserted into
     *  @param begin Code will be inserted just after this statement
     *  @param sense If this is false, inverts the meaning of failure and success for the residue
     *  @param fail  If the residue "fails", the inserted code will jump to this point; 
     *               otherwise it will fall through
     *  @param wc    The weaving context
     *  @author Ganesh Sittampalam
     */
    public abstract Stmt codeGen(SootMethod method,LocalGeneratorEx localgen,
				 Chain units,Stmt begin,Stmt fail,boolean sense,
				 WeavingContext wc);

    /** Must provide a toString method */
    public abstract String toString();

	/**
	 * Fills the Bindings object with information of possible
	 * advice-formal bindings. 
	 * Has to be overwritten by all Residue classes that have children.
	 */
	public void getAdviceFormalBindings(Bindings bindings) {
	}
	
	/**
	 * Returns residue that replaces the old residue. 
	 * This way, BindMaskResidue is inserted throughout the tree in the 
	 * appropriate places.
	 * Has to be overridden by all Residue classes that have children.
	 */
	public Residue restructureToCreateBindingsMask(Local bindingsMaskLocal, Bindings bindings) {
		return this;
	}

	/**
	 * 
	 * @author Sascha Kuzins
	 *
	 * Represents the possible bindings of locals to advice-formals.
	 */
	public static class Bindings {
		/*Bindings(int maxSize) {
			arrayList.ensureCapacity(maxSize);
		}*/
		/**
		 * Add binding of Local local to advice-formal at position pos.
		 */
		public void set(int pos, Local local) {
			// resize
			while (arrayList.size()<pos+1)
				arrayList.add(null);
				
			if (arrayList.get(pos) == null) {
				arrayList.set(pos, new LinkedList());
			} else {
				//if (!ambiguous) {
				//	debug("  (ambiguous variable binding)");
					ambiguous=true;
				//}
			}
			((List)arrayList.get(pos)).add(local);
		}
		/**
		 * Find last index of advice-formal which is bound to local 
		 */
		public int lastIndexOf(Local local) {
			for (int i=arrayList.size()-1;i>=0;i--) {
				List list=localsFromIndex(i);
				if (list!=null) {
					if (list.contains(local))
						return i;
				}
			}
			return -1;
		}
		public boolean contains(Local local) {
			return lastIndexOf(local)!=-1;
		}
		public boolean isAmbiguous() {
			return ambiguous;	
		}
		public List localsFromIndex(int i) {
			 return (List)arrayList.get(i);
		}
		/**
		 * Calculate bits to be or'd into the mask
		 * if this local is bound.
		 */
		public int getMaskValue(Local local, int i) {
			//int i=lastIndexOf(local);
			//if (i==-1)
			//	throw new RuntimeException();
			int pos=bitPositions[i];
			//System.out.println("pos: " + pos);
			List list=localsFromIndex(i);
			int index=list.indexOf(local);
			//System.out.println("index: " + index);
			if (index==-1)
				throw new RuntimeException();
			index=index << pos;
			return index;
		}
		public int numOfFormals() {
			return arrayList.size();
		}
		/**
		 * Sets the bits of the mask which belong to
		 * the advice-formal at position index 
		 */
		public int getMaskBits(int index) {
			int mask=0;
			int pos=bitPositions[index];
			int size=bitCounts[index];
			while (size-->0) {
				mask=(mask << 1) | 1;
			}
			mask=mask << pos;
			return mask;
		}
		/**
		 * Position of the bits of the mask that belong to 
		 * the advice-formal at position index
		 */
		public int getMaskPos(int index) {
			return bitPositions[index];
		}
		/**
		 * Calculate the mask layout for the bindings
		 *
		 */
		public void calculateBitMaskLayout() {
			bitPositions=new int[arrayList.size()];
			bitCounts=new int[arrayList.size()];
			int bit=1;
			for (int i=0; i<arrayList.size();i++) {
				List list=(List)arrayList.get(i);
				if (list!=null) {
					int size=list.size()-1;
					int bits=0;
					while (size>0) {
						size=size >> 1;
						bits++;
					}
					bitPositions[i]=bit;
					bitCounts[i]=bits;
					bit+=bits;
					if (bit>32)
						throw new RuntimeException("Compiler limitation: two many ambiguities in bindings"); // TODO: fix message
				}
			}	
		}
		/**
		 * Positions of the bits corresponding to the advice-formals
		 */
		private int[] bitPositions;
		/**
		 * Number of bits reserved for each advice-formal
		 */
		private int[] bitCounts;
		
		/**
		 * One list of locals for each advice-formal
		 */
		final private ArrayList /*List /Local/ */ arrayList=new ArrayList();
		private boolean ambiguous=false;
		public String toString() {
			String result="Bindings\n";
			for (int i=0; i<arrayList.size(); i++) {
				result+="Advice-Formal: " + i + ": ";
				if (bitPositions!=null) {
					result+="pos: " + bitPositions[i] + " ";
				}
				if (bitCounts!=null) {
					result+="count: " + bitCounts[i] + " ";
				}
				List list=localsFromIndex(i);
				if (list!=null) {
					for (Iterator it=list.iterator();it.hasNext();) {
						Local l=(Local)it.next();
						result+= l + " ";
					}
				}
				result+="\n";
			}
			return result;
		}
	}
        public List/*ResidueBox*/ getResidueBoxes() { return new ArrayList(); }
}
