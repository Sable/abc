package org.apache.jmeter.functions;

/**
 * @author mstover
 *
 * @version $Revision: 1.1 $
 */
public class InvalidVariableException extends Exception
{
    public InvalidVariableException()
    {
    }

    public InvalidVariableException(String msg)
    {
        super(msg);
    }
}
