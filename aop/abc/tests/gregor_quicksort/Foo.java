import java.util.Random;

/*
 * Created on 24-Sep-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */

/**
 * @author ganesh
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class Foo {
	public final static int n=1000000;
	public static void main(String[] args) {
		Random r = new Random();
		Integer[] elems=new Integer[n];
		for(int i=0;i<n;i++) 
			elems[i] = new Integer(r.nextInt());
		long start = System.currentTimeMillis();
		QuickSort.quicksort(elems,0,elems.length);
		long duration = System.currentTimeMillis() - start;
	    System.out.println( " Elapsed " + Long.toString(duration));
		// for(int i=0;i<elems.length;i++) System.out.print(elems[i]+" ");
		// System.out.println("");
	}
}
