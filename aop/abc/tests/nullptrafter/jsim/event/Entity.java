
/******************************************************************
 * @(#) Entity.java        1.0
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
 * This class represents a single simulation entity for event-scheduling 
 * simulation.
 *
 * @version   1.0, 16 Dec 1996
 * @author    Zhiwei Zhang, John Miller
 */

public class Entity
{
    //////////////////////// Variables \\\\\\\\\\\\\\\\\\\\\\\\\\\\
    /**
     * Entity counter.
     */
    private static long  counter = 0;

    /**
     * Entity identifier.
     */
    private long         entityId;

    /**
     * Entity creation time.
     */
    private double       createTime;


    /**************************************************************
     * Constructs a simulation entity, e.g., a customer.
     * @param  createTime  time entity is created
     */
    public Entity (double createTime)
    {
        entityId         = ++counter;
        this.createTime  = createTime;

    }; // Entity


    /**************************************************************
     * Get the identifier for the entity.
     * @return  long  entity identifier
     */
    public long getEntityId ()
    {
        return entityId;

    }; // getEntityId        


    /**************************************************************
     * Get the creation time for the entity.
     * @return  double  time entity is created
     */
    public double getCreateTime ()
    {
        return createTime;

    }; // getCreateTime        


}; // class

