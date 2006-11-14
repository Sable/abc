package abc.ja.bytecode;

import abc.ja.jrag.DoubleLiteral;
import abc.ja.jrag.Expr;


class CONSTANT_Double_Info extends CONSTANT_Info {
	public double value;

	public CONSTANT_Double_Info(Parser parser) {
		super(parser);
		value = this.p.readDouble();
	}

	public String toString() {
		return "DoubleInfo: " + Double.toString(value);
	}

	public Expr expr() {
		return new DoubleLiteral(Double.toString(value));
	}
}