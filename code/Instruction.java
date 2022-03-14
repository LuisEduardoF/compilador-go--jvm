package code;

import java.util.Formatter;

// Instruction quadruple.
public final class Instruction {

	// Público para não precisar de getter/setter.
	public final OpCode op;
    public int in;

	public Instruction(OpCode op, int in) {
		this.op = op;
        this.in = in;
    }
    
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("%s_", this.op.toString());
        f.format("%d", this.in);
		f.close();
        
		return sb.toString();
	}
}