package changes;

/*
 * PullTogether(blk, start, end) is a change that, when applied, pulls the
 * statements "start" through "end" (inclusive) together into a block.
 */

import AST.Block;
import AST.Stmt;

public class PullTogether extends ASTChange {
	
	private Block block;
	private int index;
	private int len;
	private Stmt[] stmts;
	
	public PullTogether(Block block, int start, int end) {
		this.block = block;
		this.index = start;
		this.len = end-start+1;
		this.stmts = new Stmt[len];
		for(int i=start;i<=end;++i)
			this.stmts[i-start] = block.getStmt(i);
	}

	public String prettyprint() {
		return "pull together "+len+" statements starting from "+index;
	}

	public void undo() {
		if(len == 0) {
			block.getStmtList().removeChild(index);
		} else {
			block.setStmt(stmts[0], index);
			for(int i=1;i<len;++i)
				block.getStmtList().insertChild(stmts[i], index+i);
		}
	}

}
