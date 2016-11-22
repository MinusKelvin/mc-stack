package minusk.mcstack.exec;

/**
 * @author MinusKelvin
 */
public class StackFrame {
	public final ByteCode code;
	public final Context context;
	public final String name;
	public int instruction = 0;
	
	public StackFrame(ByteCode toExec, Context context, String name) {
		code = toExec;
		this.context = context;
		this.name = name;
	}
	
	public int nextOpcode() {
		if (instruction >= code.code.length)
			return 0;
		return code.code[instruction++] & 0xFF;
	}
	
	@Override
	public String toString() {
		return "Stackframe in function "+name;
	}
}
