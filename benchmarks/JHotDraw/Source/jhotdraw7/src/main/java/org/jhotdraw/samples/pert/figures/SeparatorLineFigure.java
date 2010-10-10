/*
 * @(#)SeparatorLineFigure.java
 *
 * Copyright (c) 1996-2010 by the original authors of JHotDraw
 * and all its contributors.
 * All rights reserved.
 *
 * The copyright of this software is owned by the authors and  
 * contributors of the JHotDraw project ("the copyright holders").  
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * the copyright holders. For details see accompanying license terms. 
 */

package org.jhotdraw.samples.pert.figures;

import java.awt.Graphics2D;
import java.awt.geom.*;
import org.jhotdraw.draw.AttributeKeys;
import static org.jhotdraw.draw.AttributeKeys.*;
import org.jhotdraw.draw.RectangleFigure;
import org.jhotdraw.geom.*;
/**
 * A horizontal line with a preferred size of 1,1.
 *
 * @author  Werner Randelshofer
 * @version $Id: SeparatorLineFigure.java 648 2010-03-21 12:55:45Z rawcoder $
 */
public class SeparatorLineFigure 
extends RectangleFigure {

    /** Creates a new instance. */
    public SeparatorLineFigure() {
    }

    
    public Dimension2DDouble getPreferredSize() {
        double width = Math.ceil(STROKE_WIDTH.get(this));
        return new Dimension2DDouble(width, width);
    }

    
    protected void drawFill(Graphics2D g) {
        // no fill
    }
    
    protected void drawStroke(Graphics2D g) {
        Rectangle2D.Double r = (Rectangle2D.Double) rectangle.clone();
        double grow = AttributeKeys.getPerpendicularDrawGrowth(this);
       Geom.grow(r, grow, grow);

        g.draw(new Line2D.Double(r.x,r.y,r.x+r.width-1,r.y));
    }
}
