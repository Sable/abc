module org.jhotdraw.samples.simple;
package org.jhotdraw.samples.simple;

import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jhotdraw.draw.*;
import org.jhotdraw.geom.*;
import jhotdraw::batik::org.apache.batik.*;

public class Main {
    public static void main(String[] args) {
    	//references batik version in jhotdraw
        final String batikVersion = Version.version;
        final String nanoXMLVersion = new jhotdraw::nanoxml::net.n3.nanoxml.Version().version;
        final String jhotdrawVersion = new org.jhotdraw.Version().version;
        
    	//code is taken from EditorSample
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                // Create a simple drawing consisting of three
                // text areas and an elbow connection.
                TextAreaFigure ta = new TextAreaFigure(jhotdrawVersion);
                ta.setBounds(new Point2D.Double(10,10),new Point2D.Double(100,100));
                TextAreaFigure tb = new TextAreaFigure(nanoXMLVersion);
                tb.setBounds(new Point2D.Double(220,120),new Point2D.Double(310,210));
                TextAreaFigure tc = new TextAreaFigure(batikVersion);
                tc.setBounds(new Point2D.Double(220,10),new Point2D.Double(310,100));
                
                ConnectionFigure cf = new LineConnectionFigure();
                cf.setLiner(new ElbowLiner());
                cf.setStartConnector(ta.findConnector(Geom.center(ta.getBounds()), cf));
                cf.setEndConnector(tb.findConnector(Geom.center(tb.getBounds()), cf));
                
                Drawing drawing = new DefaultDrawing();
                drawing.add(ta);
                drawing.add(tb);
                drawing.add(tc);
                drawing.add(cf);
                
                // Show the drawing
                JFrame f = new JFrame("My Drawing");
                f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                f.setSize(400,300);
                
                // Set up the drawing view
                DrawingView view = new DefaultDrawingView();
                view.setDrawing(drawing);
                f.getContentPane().add(view.getComponent());
                
                // Set up the drawing editor
                DrawingEditor editor = new DefaultDrawingEditor();
                editor.add(view);
                editor.setTool(new DelegationSelectionTool());
                
                f.show();
            }
        });
    }
}