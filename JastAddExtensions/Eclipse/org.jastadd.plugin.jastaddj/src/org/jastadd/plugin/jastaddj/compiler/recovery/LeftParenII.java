package org.jastadd.plugin.jastaddj.compiler.recovery;

import org.jastadd.plugin.compiler.recovery.Interval;
import org.jastadd.plugin.compiler.recovery.Island;
import org.jastadd.plugin.compiler.recovery.LexicalNode;
import org.jastadd.plugin.compiler.recovery.Recovery;
import org.jastadd.plugin.compiler.recovery.Water;

public class LeftParenII extends LeftParen {
	public LeftParenII(LexicalNode previous, Interval interval) {
		super(previous, interval);
	}

	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof RightParenII &&
			(((RightParenII)island).indent().equalTo(indent()) ||
			indent().lessThan(((RightParenII)island).indent()));
	}	
	public boolean possibleConstructionSite(LexicalNode node) {
		if (node instanceof Indent) {
			Indent ind = (Indent)node;
			return ind.equalTo(indent()) || indent().lessThan(ind);
		}
		return node instanceof Island ||
			node instanceof Semicolon ||
			((node instanceof Comma) && !(node.getPrevious() instanceof Water)) ||
			((node instanceof Dot) && !(node.getPrevious() instanceof Water));
	}
	public Island constructFakeIsland(LexicalNode node) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getEnd(), nodeInterval.getEnd());
		Island island = new RightParenII(null, interval);
		island.setFake(true);
		return island;
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertBefore(island, node);
	}

	public LexicalNode clone(LexicalNode previous) {
		return new LeftParenII(previous, getInterval().clone());
	}
	public static final char[] TOKEN = {'('};
}
