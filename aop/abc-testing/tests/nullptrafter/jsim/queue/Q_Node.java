
/********************************************************************
 * @(#) Q_Node.java           1.0     96/06/20
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


/********************************************************************
 * Class Q_Node deriving from Object implements a node to be
 * inserted into a queue.
 *
 * @version    1.0
 * @author    Zhiwei Zhang, John Miller
 */

public class Q_Node
{
    /**
     * Item containing data
     */
    Object  item    = null;

    /**
     * Item cancelled from queue (lazy delete)
     */
    boolean present = true;

    /**
     * Next node (right child)
     */
    Q_Node  right   = null;


    /****************************************************************
     * Constructs an empty queue node.
     */
    Q_Node ()
    {
    }; // Q_Node


    /****************************************************************
     * Constructs an unlinked queue node.
     * @param  item    item for the queue node
     */
    Q_Node (Object item)
    {
        this.item = item;

    }; // Q_Node


    /****************************************************************
     * Constructs an queue node.
     * @param  item    item for the queue node
     * @param  right   next node for the queue node
     */
    Q_Node (Object item, Q_Node right)
    {
        this.item  = item;
        this.right = right;

    }; // Q_Node


    /****************************************************************
     * Get the item out of the node.
     * @return  Object  item in node
     */
    public Object getItem ()
    {
        return item;

    }; // getItem


}; // class

