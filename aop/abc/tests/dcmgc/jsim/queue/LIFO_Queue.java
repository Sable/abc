
/*********************************************************************
 * @(#) LIFO_Queue.java           1.0     96/06/20
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
 * LIFO_Queue class implements the Last_In_First_Out queue (or stack).
 *
 * @version   1.0    96/06/20
 * @author    John Miller, Zhiwei Zhang
 */

public class LIFO_Queue extends Queue
{ 
    /*****************************************************************
     * Constructs an empty LIFO queue with unlimited capacity.
     */
    public LIFO_Queue ()
    {
    }; // LIFO_Queue


    /*****************************************************************
     * Constructs an empty LIFO queue with limited capacity.
     * @param  capacity   the maximum number of items queue can hold.
     */
    public LIFO_Queue (int capacity)
    {
        this.capacity = capacity;

    }; // LIFO_Queue


    /*****************************************************************
     * Insert an element into the queue.
     * @param   item                new item to be inserted
     * @return  Q_Node              node holding new item
     * @throws  FullQueueException  if the queue is full
     */
    public Q_Node enqueue (Object item) throws FullQueueException
    {
        if (size >= capacity) {
            throw new FullQueueException ("The queue is full");
        }; // if

        size++;
        return root = new Q_Node (item, root);    // new node at front

    }; // enqueue


}; // class

