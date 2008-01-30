
/***********************************************************************
 * @(#) Queue.java           1.0     96/12/20
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


/***********************************************************************
 * Class Queue is an abstract class which pure virtual functions must be 
 * implemented in subclasses.
 *
 * @version   1.0    96/07/10
 * @author    Zhiwei Zhang, John Miller
 */

public abstract class Queue
{
    /***************************************************************
     * Root (front) of queue
     */
    protected Q_Node root     = null;

    /***************************************************************
     * Current number of elements
     */
    protected int    size     = 0;

    /***************************************************************
     * Maximum number of elements allowed
     */
    protected int    capacity = (int) 1E31 - 1;


    /***************************************************************
     * Check if the queue is empty.  Return true if it is empty,
     * otherwise, return false.
     * @return  boolean  whether queue is empty
     */
    public boolean empty ()
    {
        return size == 0;

    }; // empty


    /***************************************************************
     * Check if the queue is empty.  Return true if it is empty,
     * otherwise, return false.
     * @return  boolean  whether queue is empty
     */
    public boolean full ()
    {
        return size >= capacity;

    }; // full


    /***************************************************************
     * Return the number of elements in the queue.
     * @return  int  queue length
     */
    public int length ()
    {
        return size;

    }; // length


    /***************************************************************
     * Return the first element in the queue without removing it.
     * @return  Object               first item
     * @throws  EmptyQueueException  if the queue is empty.
     */
    public Object front () throws EmptyQueueException
    {
        if (root == null) {
           throw new EmptyQueueException ("The queue is empty");
        }; // if

        return root.item;

    }; // front


    /***************************************************************
     * Cancel (remove) node from the queue.
     * @param  node  node to be cancelled
     */
    public void cancel (Q_Node node)
    {
        node.present = false;

    }; // cancel 


    /***************************************************************
     * Remove and return the first element in the queue.
     * If first node has been cancelled (not present), continue to dequeue.
     * @return  Q_Node               first node
     * @throws  EmptyQueueException  if the queue is empty.
     */
    public Q_Node dequeue () throws EmptyQueueException
    {
        Q_Node first = null;

        for (boolean done = false; ! done; ) { 
 
            if (root == null)  {
                throw new EmptyQueueException ("The queue is empty");
            }; // if 
    
            first = root;             // node containing min item
            done  = first.present;    // node has not been cancelled
            removeMin ();             // bypass min node
            size--;                   // shrink tree  
 
        }; // for 
 
        return first;                 // return node with minimum item;
 
    }; // dequeue


    /***************************************************************
     * Remove the node containing the minimum/first element from the queue.
     */
    protected void removeMin ()
    {
        root = root.right;
 
    }; // removeMin


    /***************************************************************
     * Remove all elements from the queue.
     */
    public void clear ()
    { 
        root = null;
 
    }; // clear


    /***************************************************************
     * Insert an element into the queue. Throws FullQueueException if 
     * the size of the queue is out of capacity.
     * @param   item                new item to be added
     * @return  item                node holding the new item
     * @throws  FullQueueException  if the queue is full.
     */
    public abstract Q_Node enqueue (Object item) throws FullQueueException;


}; // class

