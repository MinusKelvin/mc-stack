package minusk.mcstack.exec.values;

import minusk.mcstack.exec.ByteCode;
import minusk.mcstack.exec.Context;
import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;

/**
 * @author MinusKelvin
 */
public class FunctionValue extends Value {
	public static final StringValue TYPE = new StringValue("function");
	
	private final ByteCode code;
	private final Context context;
	private final String name;
	
	public FunctionValue(ByteCode code, Context context, String name) {
		this.code = code;
		this.context = context;
		this.name = name;
	}
	
	public FunctionValue(StackFrame frame, int lengthBytes) {
		int nameLength = frame.nextOpcode();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < nameLength; i++)
			builder.append((char) frame.nextOpcode());
		name = builder.toString();
		
		int l = 0;
		for (int i = 0; i < lengthBytes; i++)
			l |= frame.nextOpcode() << (i*8);
		byte[] c = new byte[l];
		for (int i = 0; i < l; i++)
			c[i] = (byte) frame.nextOpcode();
		code = new ByteCode(c);
		context = frame.context;
	}
	
	public StackFrame call() {
		return new StackFrame(code, new Context(context), name);
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		throw new MCStackException(new StringValue("cannot convert function to string"));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert function to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert function to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert function to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		return this;
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert function to coroutine"));
	}
	
	@Override
	public String toString() {
		return "function "+name+": "+hashCode();
	}
}
