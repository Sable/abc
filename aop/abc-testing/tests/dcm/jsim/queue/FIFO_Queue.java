
/********************************************************************
 * @(#) FIFO_Queue.java           1.0     96/06/20
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
 * FIFO_Queue class implements the First_In_First_Out queue. The 
 * implementation is based on the Pascal implementation in Data 
 * Structures by Rick Decker.
 *
 * @version   1.0
 * @author    John Miller, Zhiwei Zhang
 */

public class FIFO_Queue extends Queue
{ 
    /***************************************************************
     * Tail (last node) of queue
     */
    private Q_Node tail;


    /***************************************************************
     * Constructs an empty FIFO queue with unlimited capacity.
     */
    public FIFO_Queue ()
    {
        tail = null;

    }; // FIFO_Queue


    /***************************************************************
     * Constructs an empty FIFO queue with limited capacity.
     * @param   capacity   the maximum number of items queue can hold.
     */
    public FIFO_Queue (int capacity)
    {
        tail = null;
        this.capacity = capacity;

    }; // FIFO_Queue


    /***************************************************************
     * Insert an element into the queue.
     * @param   item                new item to be inserted
     * @return  Q_Node              node holding the new item
     * @throws  FullQueueException  if queue is full
     */
    public Q_Node enqueue (Object item) throws FullQueueException
    {
        if (size >= capacity) {
            throw new FullQueueException ("The queue is full");
        }; // if        

        Q_Node newNode = new Q_Node (item);    // new node at back

        if (tail != null) {
            tail.right = newNode;
        } else {
            root = newNode;
        }; // if

        size++;
        return tail = newNode;

    }; // enqueue


    /***************************************************************
     * Remove the node containing the minimum/first element from the queue.
     */
    protected void removeMin ()
    {
        if (root == tail) {
            tail = null;
        }; // if

        root = root.right;
 
    }; // removeMin


    /***************************************************************
     * Clear all elements from the queue.
     */
    public void clear ()
    {
        root = tail = null;

    }; // clear


}; // class

