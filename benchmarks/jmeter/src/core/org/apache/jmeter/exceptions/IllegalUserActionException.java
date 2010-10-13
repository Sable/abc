package org.apache.jmeter.exceptions;

/**
 * @author Michael Stover
 * @version $Revision: 1.1 $
 */
public class IllegalUserActionException extends Exception
{
    public IllegalUserActionException()
    {
    }

    public IllegalUserActionException(String name)
    {
        super(name);
    }
}
