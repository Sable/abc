
package command;

aspect Comment {

	String Command.comment;
	
	String Command.getComment() {
		return comment;
	}
	
	void Command.setComment(String c) {
		comment = c;
	}
	
	String around(Command c) : execution(String Command+.toString()) && this(c){
		if (c.comment == null) {
			return proceed(c);
		} else {
			return proceed(c) + "   ; "+c.comment;
		}
	}
	
	private String parser.CommandParser.parseComment() {
		String next = nextToken();
		if (next==null)
			return null;
		return next.substring(1);
	}
	
	after(parser.CommandParser cp) returning(Command c) : 
	           execution(Command parser.CommandParser.parseCommand(..))  && this(cp) {
		c.comment = cp.parseComment();
	}
	
}
