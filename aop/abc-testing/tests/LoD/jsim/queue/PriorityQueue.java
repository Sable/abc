
/*********************************************************************
 * @(#) PriorityQueue.java           1.0     96/06/20
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
 * PriorityQueue class maintains a priority queue using self-adjusting 
 * binary tree (splay tree). The splay part is based on Daniel Sleator's C 
 * implementation of splay tree (http://gs213.sp.cs.cmu.edu/prog/splay/). 
 * The data structure is first proposed by Sleator and Tarjan in their 
 * article "Self-adjusting Binary Search Tree", JACM, 32(3):652-686, 1985.
 *
 * @version   1.0    96/06/20
 * @author    John Miller, Zhiwei Zhang
 */

public class PriorityQueue extends Queue
{
    /**
     * Root (front) of the queue
     */
    protected PQ_Node root;


    /*****************************************************************
     * Constructs an empty priority queue with unlimited capacity.
     */
    public PriorityQueue ()
    {
        root = null;

    };  // PriorityQueue


    /*****************************************************************
     * Constructs an empty priority queue with limited capacity.
     * @param   capacity   maximum number of items queue can hold
     */
    public PriorityQueue (int capacity)
    {
        root = null;
        this.capacity = capacity;

    };  // PriorityQueue


    /*****************************************************************
     * Print the tree holding the prior queue.
     * Indent based on level of tree.
     */
    public  void printQueue () { printTree (root); }
    private int level = 0;
    private void printTree (PQ_Node node)
    {
        level++;
        if (node != null) {
            for (int i = 0; i < level; i++) {
                 System.out.print ("  ");
            }; // for
            System.out.println ("[ . " + node.priority + " . ]");
            printTree (node.left);
            printTree ((PQ_Node) node.right);
        }; // if
        level--;

    }; // printTree
 

    /*****************************************************************
     * Insert item into the queue with default priority.
     * @param   item                 new item to be inserted
     * @return  Q_Node               node holding new item
     * @throws  FullQueueException   if the queue is full
    */
    public Q_Node enqueue (Object item) throws FullQueueException
    {
        return enqueue (item, Thread.NORM_PRIORITY);    

    }; // enqueue


    /*****************************************************************
     * Insert item and its priority into the queue.
     * @param   item                 new item to be inserted
     * @param   priority             priority associated with the new item
     * @return  Q_Node               node holding new item
     * @throws  FullQueueException   if the queue is full
     */
    public Q_Node enqueue (Object item, int priority) throws FullQueueException
    {
        if (size >= capacity) {
            throw new FullQueueException ("The queue is full");
        }; // if

        PQ_Node newNode = new PQ_Node (item, priority);
        if (root != null) {

            splayForInsert (newNode);

            /*********************************************************
             * case: newNode < root
             * make old root the right child of newNode
             */
            if (newNode.compare (root) < 0) {
                newNode.left  = root.left;
                newNode.right = root;
                root.left     = null;

            /*********************************************************
             * case: newNode >= root
             * make old root the left child of newNode
             */
            } else {
                newNode.left  = root;
                newNode.right = root.right;
                root.right    = null;
            }; // if

        }; // if

        size++;
        return root = newNode;    // make newNode the new root

    }; // enqueue
    

    /*****************************************************************
     * Splay node with its left child.
     * @param   node     node to splay
     * @param   lChild   left child node
     * @return  PQ_Node  left child
     */
    protected PQ_Node splayLeft (PQ_Node node, PQ_Node lChild)
    {
        node.left      = (PQ_Node) lChild.right;
        lChild.right   = node;
        return lChild;
        
    }; // splayLeft


    /*****************************************************************
     * Splay node with its right child.
     * @param   node     node to splay
     * @param   lChild   right child node
     * @return  PQ_Node  right child
     */
    protected PQ_Node splayRight (PQ_Node node, PQ_Node rChild)
    {
        node.right     = rChild.left;
        rChild.left    = node;
        return rChild;
        
    }; // splayRight


    /*****************************************************************
     * Splay the tree until newNode can be inserted either between
     * lChild and root, or root and rChild.
     * @param  newNode    node to be inserted
     */
    protected void splayForInsert (PQ_Node newNode)
    {
        PQ_Node lChild;       // left child of root
        PQ_Node rChild;       // right child of root
            
        for ( ; ; ) {

            /*********************************************************
             * case: newNode <= root
             * want lChild == null or
             *      lChild <= newNode and lChild.right == null
             */
            if (newNode.compare (root) < 0) {

                if ((lChild = root.left) == null) break;
                if (newNode.compare (lChild) < 0) {
                    root = splayLeft (root, lChild);
                } else {
                    if (lChild.right == null) break;
                    lChild = splayRight (lChild, (PQ_Node) lChild.right);
                }; //if

            /*********************************************************
             * case: newNode >= root
             * want root <= newNode <= rChild and rChild.left == null
             */
            } else if (newNode.compare (root) >= 0) {

                if ((rChild = (PQ_Node) root.right) == null) break;
                if (newNode.compare (rChild) > 0) {
                    root = splayRight (root, rChild);
                } else {
                    if (rChild.left == null) break;
                    lChild = splayLeft (rChild, rChild.left);
                }; //if

            }; // if

        }; // for

    }; // splayForInsert

 
    /***************************************************************
     * Remove and return the first element in the queue.
     * If first node has been cancelled (not present), continue to dequeue.
     * @return  Q_Node               first node (holds min item)
     * @throws  EmptyQueueException  if the queue is empty
     */  
    public Q_Node dequeue () throws EmptyQueueException
    {
        PQ_Node first = null;

        for (boolean done = false; ! done; ) {
 
            if (root == null)  {
                throw new EmptyQueueException ("The queue is empty");
            }; // if
   
            first = getMin ();             // node containing min item
            done  = first.present;         // node has not been cancelled
            root  = (PQ_Node) root.right;  // bypass min node
            size--;                        // shrink tree
 
        }; // for
 
        return first;                      // return node with minimum item;
 
    }; // dequeue


    /*****************************************************************
     * Iteratively splay left until the root has no left child.
     * This means that the root will be the minimum element in the tree.
     * @return  PQ_Node  root after splaying (holds min item)
     */
    protected PQ_Node getMin ()
    {
        for (PQ_Node lChild = null; (lChild = root.left) != null; ) {
            root = splayLeft (root, lChild);
        }; // for

        return root;

    }; // getMin
            

}; // class

