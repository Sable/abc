package abc.ja.eaj.bytecode;

import abc.ja.eaj.jrag.Expr;
import abc.ja.eaj.jrag.LongLiteral;


class CONSTANT_Long_Info extends CONSTANT_Info {
	public long value;

	public CONSTANT_Long_Info(Parser parser) {
		super(parser);
		value = p.readLong();
	}

	public String toString() {
		return "LongInfo: " + Long.toString(value);
	}

	public Expr expr() {
		//return new LongLiteral(Long.toString(value));
		return new LongLiteral("0x" + Long.toHexString(value));
	}
}
