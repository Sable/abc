
/***********************************************************************
 * @(#) Scheduler.java             1.0
 *
 * Copyright (c) 1996 John A. Miller, Zhiwei Zhang.
 * All Rights Reserved.
 *-----------------------------------------------------------------
 * Permission to use, copy, modify and distribute this software and
 * its documentation without fee is hereby granted provided that
 * this copyright notice appears in all copies.
 * WE MAKE NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY
 * OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. WE SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY ANY USER AS A RESULT OFUSING,
 * MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 *-----------------------------------------------------------------
 */

package jsim.event;

import jsim.queue.*;

/***********************************************************************
 * This class schedules events and implements the time advance mechanism.
 * No constructor is needed for the class since all data members are static.
 *
 * @version   1.0, 27 Feb 1997
 * @author    Zhiwei Zhang
 */

public class Scheduler
{
    //////////////////////// Constants \\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /**
     * Middle priority
     */
    private final static int      MID_PRIORITY = 100;

    //////////////////////// Variables \\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /**
     * Current simulation time
     */
    private static double         currentTime = 0.0;

    /**
     * The future event list
     */
    private static TemporalQueue  eventList = new TemporalQueue ();


    /*********************************************************************
     * This schedule method places an event on the Future Event List (FEL)
     * for later execution.  Event are ordered first by their event time,
     * and next by priority.  In this case all events have the same
     * priority (MID_PRIORITY).
     * @param  event       event to schedule
     * @param  timeDelay   how far in the future to schedule the event
     */
    public static void schedule (Event event, double timeDelay)
    {
        eventList.enqueue (event, currentTime + timeDelay, MID_PRIORITY);

    }; // schedule


    /*********************************************************************
     * This schedule method places an event on the Future Event List (FEL)
     * for later execution.  Event are ordered first by their event time,
     * and next by priority.
     * @param  event       event to schedule
     * @param  timeDelay   how far in the future to schedule the event
     * @param  priority    priority of the event (low = 0, high = 10)
     */
    public static void schedule (Event event, double timeDelay, int priority)
    {
        eventList.enqueue (event, currentTime + timeDelay, priority);

    }; // schedule


    /*********************************************************************
     * Start the simulation by processing the first event and subsequent
     * events.
     */
    public static void startSim ()
    {
        TQ_Node  nextNode;
        Event    nextEvent;

        while ( ! eventList.empty () ) {

            /*************************************************************
             * Remove the imminent event from the Future Event List (FEL).
             */
            nextNode    = (TQ_Node) eventList.dequeue ();
            nextEvent   = (Event) nextNode.getItem ();
            currentTime = nextNode.getTime ();

            /*************************************************************
             * Execute the imminent event.
             */
            nextEvent.occur ();

        }; // while

    }; // startSim


    /*********************************************************************
     * Return the current time in the simulation.
     * @return  double  the current time
     */
    public static double currentTime ()
    {
        return currentTime;

    }; // currentTime        


}; // class

