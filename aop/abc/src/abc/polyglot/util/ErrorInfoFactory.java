/* abc - The AspectBench Compiler
 * Copyright (C) 2004 Ganesh Sittampalam
 *
 * This compiler is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This compiler is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this compiler, in the file LESSER-GPL;
 * if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package abc.polyglot.util;

import polyglot.util.Position;
import polyglot.util.ErrorInfo;
import soot.SootMethod;
import soot.tagkit.*;

/** A helper class for constructing polyglot errors
 *  @author Ganesh Sittampalam
 */
public class ErrorInfoFactory {
    public static Position getPosition(SootMethod container,Host host) {
        Position pos=null;
        if(container.getDeclaringClass().hasTag("SourceFileTag")) {
            SourceFileTag sfTag=(SourceFileTag)
                container.getDeclaringClass().getTag("SourceFileTag");
            String filename=sfTag.getAbsolutePath();
            if(filename==null) filename=sfTag.getSourceFile();
            if(host.hasTag("SourceLnPosTag")) {
                SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");
                pos=new Position(filename,
                                 slpTag.startLn(),slpTag.startPos(),
                                 slpTag.endLn(),slpTag.endPos());
            } else if(host.hasTag("LineNumberTag")) {
                LineNumberTag lnTag=(LineNumberTag) host.getTag("LineNumberTag");
                pos=new Position(filename,
                                 lnTag.getLineNumber());
            } else {
                if(abc.main.Debug.v().warnUntaggedSourceInfo)
                    System.err.println("Getting position for a untagged source line "+host);
                pos=new Position(filename);
            }
        } else {
            if(abc.main.Debug.v().warnUntaggedSourceInfo)
                System.err.println("Getting source file for an untagged class "
                                   +container.getDeclaringClass());
        }
        return pos;
    }

    public static ErrorInfo newErrorInfo(int kind,String message,SootMethod container,Host host) {
        Position pos=null;
        if(container.getDeclaringClass().hasTag("SourceFileTag")) {
            SourceFileTag sfTag=(SourceFileTag)
                container.getDeclaringClass().getTag("SourceFileTag");
            String filename=sfTag.getAbsolutePath();
            if(filename==null) filename=sfTag.getSourceFile();
            if(host.hasTag("SourceLnPosTag")) {
                SourceLnPosTag slpTag=(SourceLnPosTag) host.getTag("SourceLnPosTag");
                pos=new Position(filename,
                                 slpTag.startLn(),slpTag.startPos(),
                                 slpTag.endLn(),slpTag.endPos());
            } else if(host.hasTag("LineNumberTag")) {
                LineNumberTag lnTag=(LineNumberTag) host.getTag("LineNumberTag");
                pos=new Position(filename,
                                 lnTag.getLineNumber());
            } else {
                if(abc.main.Debug.v().warnUntaggedSourceInfo)
                    System.err.println("Getting position for a untagged source line "+host);
                pos=new Position(filename);
                message+=" in method "+container;
            }
        } else {
            if(abc.main.Debug.v().warnUntaggedSourceInfo)
                System.err.println("Getting source file for an untagged class "
                                   +container.getDeclaringClass());
            message+=" in method "+container
                +" in class "+container.getDeclaringClass();
        }

        return new ErrorInfo(kind,message,pos);
    }
}
