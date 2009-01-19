package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Bridge;
import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.Island;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.SOF;

public class LeftBrace extends Island {
	public LeftBrace(LexicalNode previous, Interval interval) {
		super(previous, interval, "{");
	}

	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof RightBrace && 
			(((RightBrace)island).indent().equalTo(indent()));
			// || ((RightBrace)island).indent().lessThan(indent(), 1));
	}
	public Bridge buildBridge(Island target, int tol) {
		if (!hasBridge()) {
			bridge = new BraceBridge(this, (RightBrace)target);
			target.setBridge(bridge);
		}
		return bridge;
	}
	public String toString() {
		return "LBrace\t" + super.toString();
	}
	public LexicalNode clone(LexicalNode previous) {
		return new LeftBrace(previous, getInterval().clone());
	}
	public boolean possibleConstructionSite(LexicalNode node) {
		/* DEBUG System.out.println("LeftBrace: possible match node = " + node); */
		if (node instanceof Indent) {
			Indent ind = (Indent)node;
			return (ind.lessThan(indent()) && !(ind.getNext() instanceof RightBrace)) 
					|| ind.equalTo(indent()) ;
		}
		else if (node instanceof RightBrace) {
			RightBrace rb = (RightBrace)node;
			return !rb.hasBridge() && rb.indent().lessThan(indent(), 1);
		}
		return false; 
	}
	public Island constructFakeIsland(LexicalNode node, boolean intervalEnd) {
		if (!intervalEnd && node instanceof RightBrace) {
			return (RightBrace)node;
		} else {
			Interval nodeInterval = node.getInterval();
			Interval interval = new Interval(nodeInterval.getStart(), nodeInterval.getEnd());
			Island island = new RightBrace(null, interval);
			island.setFake(true);
			return island;
		}
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		if (island.isFake()) {
			Recovery.insertBefore(island, node);
		}
	}
	public boolean startOfBridge() {
		return true;
	}



	public static final char[] TOKEN = {'{'};
	private Indent indent;

	public Indent indent() {
		if (indent == null) {
			LexicalNode node = getPrevious().getPreviousOfType(Indent.class);
			if (node != null) {
				indent = (Indent)node;
			} else {
				SOF sof = (SOF)getPrevious().getPreviousOfType(SOF.class);
				indent = new Indent(null, sof.getInterval(), "", false);
				Recovery.insertAfter(indent, sof);
			}
		}
		return indent;
	}
}
