/*
 * Created on Sep 23, 2003
 *
 */

/**
 * @author ganesh
 *
 */
public class QuickSort {
    
    public static void quicksort(Comparable elements[],int start,int end) {
    	if(start+1<end) {
	    Middle middle=partition(elements[start],elements,start,end);
	    quicksort(elements,start,middle.left);
	    quicksort(elements,middle.right,end);
    	}
    }
    private static class Middle {
    	int left,right;
    	Middle(int left,int right) {this.left=left; this.right=right;}
    }
    
    private static Middle partition(Comparable pivot,Comparable elements[],
				    int start,int end) {
    	int middle=start;
    	while(middle<end) {
	    if(pivot.compareTo(elements[middle]) < 0) {
		swap(elements,middle,end-1);
		end--;
	    } else if(pivot.compareTo(elements[middle]) == 0) {
		middle++;
	    } else {
		swap(elements,start,middle);
		start++; middle++;
	    }
	}
    	return new Middle(start,end);
    }
    private static void swap(Object elements[],int p1,int p2) {
    	Object temp=elements[p1];
    	elements[p1]=elements[p2];
    	elements[p2]=temp;
    }
}
