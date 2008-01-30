/*
Copyright (c) Xerox Corporation 1998-2002.  All rights reserved.

Use and copying of this software and preparation of derivative works based
upon this software are permitted.  Any distribution of this software or
derivative works must comply with all applicable United States export control
laws.
*/

/* main added to loop to do some real work,  and propertyChange modified to
 * count changes rather than do I/O everytime.
 * Laurie Hendren, Nov. 26, 2003
 */

package bean;

import java.beans.*;
import java.io.*;

public class Demo implements PropertyChangeListener {

    static final String fileName = "test.tmp";
    static int changeCount = 0;
    static final int NREPS = 100000; // number of interations

    /**
     * when Demo is playing the listener role,
     * this method reports that a propery has changed
     */
    public void propertyChange(PropertyChangeEvent e){
        /* System.out.println("Property " + e.getPropertyName() + 
	 * " changed from " + e.getOldValue() + " to " + e.getNewValue() );
	 */
      changeCount++;
    }

    /**
     * main: test the program
     */
    public static void main(String[] args){
        Point p1 = null;
        Demo d = new Demo();
	// this loop added so some real work can happen, LJH
	// printlns commented out, since we don't want huge output file
	for (int i=0; i < NREPS; i++)
	{ p1 = new Point();
          p1.addPropertyChangeListener(d);
          // System.out.println("p1 =" + p1);
          p1.setRectangular(i+5,i+2);
          // System.out.println("p1 =" + p1);
          p1.setX( i+6 );
          p1.setY( i+3 );
          // System.out.println("p1 =" + p1);
          p1.offset(i+6,i+4);
	  //System.out.println("p1 =" + p1);
	}
        save(p1, fileName);
        Point p2 = (Point) restore(fileName);
        System.out.println("Had: " + p1);
        System.out.println("Got: " + p2);
    }

    /**
     * Save a serializable object to a file
     */
    static void save(Serializable p, String fn){
        try {
            System.out.println("Writing to file: " + p);
            FileOutputStream fo = new FileOutputStream(fn);
            ObjectOutputStream so = new ObjectOutputStream(fo);
            so.writeObject(p);
            so.flush();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    /**
     * Restore a serializable object from the file
     */
    static Object restore(String fn){
        try {
            Object result;
            System.out.println("Reading from file: " + fn);
            FileInputStream fi = new FileInputStream(fn);
            ObjectInputStream si = new ObjectInputStream(fi);
            return si.readObject();
        } catch (Exception e) {
            System.out.println(e);
            System.exit(1);
        }
        return null;
    }
}
