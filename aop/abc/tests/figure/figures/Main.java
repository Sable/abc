
package figures;

class Main {

    public static void main(String [] args) {

	Figure fig = new Figure();

	Point p1 = fig.makePoint(2, 2);
	Point p2 = fig.makePoint(4, 4);

	Line  l1 = fig.makeLine(p1, p2);

	/*
	 * Uncomment the next line of code and re-compile the whole example
	 * to see the  FactoryEnforcement aspect do its thing.
	 */
	//Point p3 = new Point(2, 2);

	int i;
	for (i=0; i < 1000000; i++)
	{
	   // System.out.println();
	   // System.out.println("Main is about to mark display dirty.");
	   Display.needsRepaint();
	
	   // System.out.println();
	   // System.out.println("Main is about to move p1.");
	   p1.setX(10);
	
	   // System.out.println();
	   // System.out.println("Main is about to move l1.");
	   l1.moveBy(0, 0);
	}
	Display.report();
    }
}
