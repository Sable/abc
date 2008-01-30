
/*********************************************************************
 * @(#) PQ_Node.java           1.0     96/06/20
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
 * PQ_Node class stores data and children for the PriorityQueue.
 *
 * @version    1.0    96/06/20
 * @author    Zhiwei Zhang
 */

public class PQ_Node extends Q_Node
{
    /**
     * Priority of item (low = 0, higher > 0)
     */
    int      priority;

    /**
     * Left child node
     */
    PQ_Node  left;
    

    /*****************************************************************
     * Constructor to initialize node.
     */
    PQ_Node ()
    {
        super ();
        priority = 0;
        left = null;

    }; // PQ_Node


    /*****************************************************************
     * Constructs a node for a priority queue.
     * @param item        the data item 
     * @param priority    priority level (low = 0, higher > 0)
     */
    PQ_Node (Object item, int priority)
    {
        super (item);
        this.priority = priority;
        left = null;

    }; // PQ_Node

    
    /*****************************************************************
     * Compare the priority of two nodes.  If this.priority > that.priority
     * return 1, 0 if they are equal, and -1 otherwise.
     * @param  that   the other node to compare with this
     * @return int    as 1 (greater), 0 (equal) , or -1 (less)
     */
    int compare (PQ_Node that)
    {
        if      (priority >  that.priority) return 1;
        else if (priority == that.priority) return 0;
        else    return -1;

    }; // compare


}; // class

