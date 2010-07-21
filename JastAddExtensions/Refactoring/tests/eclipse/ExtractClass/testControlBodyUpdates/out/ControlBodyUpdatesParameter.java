package p;

import java.util.ArrayList;

public class ControlBodyUpdatesParameter {
	private ArrayList<Integer> arraylists;
	private Integer[] ints;
	private int fNewStart;
	//constructors added
	public ControlBodyUpdatesParameter(ArrayList<Integer> arraylists, Integer[] ints, int fNewStart) {
	    super();
	    this.setArraylists(arraylists);
	    this.setInts(ints);
	    this.setFNewStart(fNewStart);
	  }
	  public ControlBodyUpdatesParameter() {
	    super();
	  }
	public ArrayList<Integer> getArraylists() {
		return arraylists;
	}
	public ArrayList<Integer> ///void 
			setArraylists(ArrayList<Integer> arraylists) {
		return ///
			this.arraylists = arraylists;
	}
	public Integer[] getInts() {
		return ints;
	}
	public Integer[] ///void 
	        setInts(Integer[] ints) {
		return ///
			this.ints = ints;
	}
	public int getFNewStart() {
		return fNewStart;
	}
	public int ///void 
		    setFNewStart(int fNewStart) {
		return ///
			this.fNewStart = fNewStart;
	}
}