
package figures;

class Display {
    static int count;    
    static void needsRepaint() {
        count++;
    }
    static void report() {
	    System.out.println("count="+count);
    }
}
    
