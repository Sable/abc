
/*********************************************************************
 * @(#) FullQueueException.java           1.0     96/06/20
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
 * This class signals a full queue exception.
 *
 * @version    1.0    96/06/20
 * @author     Zhiwei Zhang
 */

public class FullQueueException extends RuntimeException
{
    /*****************************************************************
     * Constructor without detail message.
     */
    public FullQueueException ()
    {
        super ();    

    }; // FullQueueException


    /*****************************************************************
     * Constructor with a message.
     * @param  message  further information
     */
    public FullQueueException (String message)
    {
        super (message);

    }; // FullQueueException


}; // class

