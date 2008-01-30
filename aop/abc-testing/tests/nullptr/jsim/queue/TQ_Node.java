
/*********************************************************************
 * @(#) TQ_Node.java           1.0     96/07/16
 *
 * Copyright (c) 1996 John A. Miller, Rajesh S. Nair, Zhiwei Zhang.
 * All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software
 * and its documentation for NON-COMMERCIAL purposes and without
 * fee is hereby granted provided that this copyright notice
 * appears in all copies.
 *
 * WE MAKE NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. WE SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY ANY USER AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package jsim.queue;

/*********************************************************************
 * Temporal_Node class stores data and children for the TemporalQueue.
 *
 * @version    1.0    96/07/16
 * @author    Zhiwei Zhang, John Miller
 */

public class TQ_Node extends PQ_Node
{
    /**
     * Node timestam
     */
    protected double time;
    

    /*****************************************************************
     * Constructor to initialize node.
     */
    TQ_Node ()
    {
        super ();
        time = 0.0;

    }; // TQ_Node
    

    /*****************************************************************
     * Constructor to set data.
     * @param  item      new item to be inserted
     * @param  time      the activation time for the new item
     * @param  priority  the priority of the new item
     */
    TQ_Node (Object item, double time, int priority)
    {
        super (item, priority);
        this.time = time;

    }; // TQ_Node


    /*****************************************************************
     * Compare the priority of two nodes.  If this.time > that.time or
     * this.time == that.time and this.priority > that.priority
     * return 1, 0 if they are equal, and -1 otherwise.
     * @param   that  the other node to compare with this
     * @return  int   as 1 (greater), 0 (equal), -1 (less)
     */
    int compare (TQ_Node that)
    {
       // System.out.println ("TQ_Node compare");
        if (time > that.time || time == that.time && priority > that.priority) {
            return  1;   // this > that

        } else if (time == that.time && priority == that.priority) {
            return  0;   // this == that

        } else {
            return -1;   // this < that
        } // if

    }; // compare


    /*****************************************************************
     * Get the node's timestamp.
     * @return  double  node's timestamp
     */
    public double getTime ()
    {
        return time;

    }; // getTime


}; // class

