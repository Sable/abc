package abc.eaj.util;

import java.util.regex.Pattern;

import polyglot.ast.Prefix;
import polyglot.ast.Receiver;
import polyglot.util.Position;

import abc.eaj.ast.EAJNodeFactory;

/**
 * @author Julian Tibble
 */
public class ToReceiver
{
    final private static Pattern dot = Pattern.compile("\\.");
    public static Receiver fromString(EAJNodeFactory nf, Position pos, String longName)
    {
        String[] parts = dot.split(longName);

        if (parts.length == 1)
            return nf.AmbReceiver(pos, parts[0]);

        Prefix p = nf.AmbPrefix(pos, parts[0]);

        for (int i = 1; i < parts.length - 1; i++)
            p = nf.AmbPrefix(pos, p, parts[i]);

        return nf.AmbReceiver(pos, p, parts[parts.length - 1]);
    }
}
