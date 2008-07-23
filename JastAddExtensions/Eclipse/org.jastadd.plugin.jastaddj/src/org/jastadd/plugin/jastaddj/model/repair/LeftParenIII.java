package org.jastadd.plugin.jastaddj.model.repair;

import org.jastadd.plugin.model.repair.Interval;
import org.jastadd.plugin.model.repair.Island;
import org.jastadd.plugin.model.repair.LexicalNode;
import org.jastadd.plugin.model.repair.Recovery;
import org.jastadd.plugin.model.repair.Water;

public class LeftParenIII extends LeftParen {
	public LeftParenIII(LexicalNode previous, Interval interval) {
		super(previous, interval);
	}

	public boolean bridgeMatch(Island island, int tol) {
		return island instanceof RightParenIII &&
			(((RightParenIII)island).indent().equalTo(indent()) ||
			indent().lessThan(((RightParenIII)island).indent()));
	}	
	public boolean possibleConstructionSite(LexicalNode node) {
		return (node instanceof Indent && (((Indent)node).equalTo(indent()) || 
			((Indent)node).lessThan(indent()))) ||
			node instanceof Island ||
			node instanceof Semicolon || 
			(node instanceof Comma && !(node.getPrevious() instanceof Water)) || 
			(node instanceof Dot && !(node.getPrevious() instanceof Water)) || 
			node instanceof JavaKeyword;
	}
	public Island constructFakeIsland(LexicalNode node) {
		Interval nodeInterval = node.getInterval();
		Interval interval = new Interval(nodeInterval.getEnd(), nodeInterval.getEnd());
		Island island = new RightParenIII(null, interval);
		island.setFake(true);
		return island;
	}
	public void insertFakeIsland(Island island, LexicalNode node) {
		Recovery.insertBefore(island, node);
	}

	public LexicalNode clone(LexicalNode previous) {
		return new LeftParenIII(previous, getInterval().clone());
	}
	public static final char[] TOKEN = {'('};
}
