 

/************************************************************************
 * @(#) Test.java           1.0     99/1/14
 *
 * Copyright (c) 1999 John A. Miller, Xuewei Xiang
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


/************************************************************************
 * Test class is used to test temporal and priority queues.
 *
 * @version   1.0    99/1/14
 * @author    John Miller, Xuewei Xiang
 */

public class Test
{
    /********************************************************************
     * The main function performs several enqueues followed by dequeues.
     * @param  args  arguments
     */
    public static void main (String [] args)
    {
        String        item;
        //PriorityQueue q = new PriorityQueue ();
        TemporalQueue q = new TemporalQueue ();
 
        //q.enqueue("Item a", 1);
        //q.enqueue("Item b", 5);
        //q.enqueue("Item c", 7);
        //q.enqueue("Item d", 2);
        //q.enqueue("Item e", 3);
        //q.enqueue("Item f", 0);
        //q.enqueue("Item g", 6);
        //q.enqueue("Item h", 4);
        q.enqueue("Item a", 8.0, 1);
        q.enqueue("Item b", 5.0, 5);
        q.enqueue("Item c", 6.0, 7);
        q.enqueue("Item d", 2.0, 2);
        q.enqueue("Item e", 3.0, 3);
        q.enqueue("Item f", 0.0, 0);
        q.enqueue("Item g", 6.0, 6);
        q.enqueue("Item h", 4.0, 4);
        q.printQueue ();

        try {
            while (true) {
                item = (String) (q.dequeue ().getItem ());
                System.out.println ("dequeue " + item);
                q.printQueue ();
            } // while 
        } catch (EmptyQueueException e) {
            System.out.println ("The queue is now empty!");
        }; // try

    }; // main


}; // class

