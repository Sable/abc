
/******************************************************************
 * @(#) Event.java        1.0
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

/******************************************************************
 * This class provides facilities for producing simulation events.  
 *
 * @version   1.0, 16 Dec 1996
 * @author    Zhiwei Zhang, John A. Miller
 */

public abstract class Event
{
    /**
     * Event counter.
     */
    private static long  counter = 0;

    /**
     * Event identifier.
     */
    private long         eventId;

    /**
     * Entity involved.
     */
    protected Entity     entity;
    

    /**************************************************************
     * Constructs a simulation event.
     * @param  ent  entity involved in event
     */
    public Event (Entity ent)
    { 
        eventId = counter++;
        entity  = ent;

    }; // Event


    /**************************************************************
     * Executes the event.
     */
    public abstract void occur ();


}; // class

