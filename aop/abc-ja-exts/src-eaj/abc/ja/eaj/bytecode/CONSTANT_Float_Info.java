package abc.ja.eaj.bytecode;

import abc.ja.eaj.jrag.Expr;
import abc.ja.eaj.jrag.FloatingPointLiteral;


class CONSTANT_Float_Info extends CONSTANT_Info {
	public float value;

	public CONSTANT_Float_Info(Parser parser) {
		super(parser);
		value = p.readFloat();
	}

	public String toString() {
		return "FloatInfo: " + Float.toString(value);
	}

	public Expr expr() {
		return new FloatingPointLiteral(Float.toString(value));
	}
}
