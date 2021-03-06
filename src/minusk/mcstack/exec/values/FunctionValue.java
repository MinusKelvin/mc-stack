package minusk.mcstack.exec.values;

import minusk.mcstack.exec.Context;
import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;
import minusk.mcstack.exec.VM;

/**
 * @author MinusKelvin
 */
public class FunctionValue extends Value {
	public static final StringValue TYPE = new StringValue("function");
	
	private final byte[] code;
	private final Context context;
	private final String name;
	private final NativeFunction func;
	
	public FunctionValue(byte[] code, Context context, String name) {
		this.code = code;
		this.context = context;
		this.name = name;
		func = null;
	}
	
	public FunctionValue(StackFrame frame, int lengthBytes) {
		func = null;
		int nameLength = frame.nextOpcode();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < nameLength; i++)
			builder.append((char) frame.nextOpcode());
		name = builder.toString();
		
		int l = 0;
		for (int i = 0; i < lengthBytes; i++)
			l |= frame.nextOpcode() << (i*8);
		code = new byte[l];
		for (int i = 0; i < l; i++)
			code[i] = (byte) frame.nextOpcode();
		context = frame.context;
	}
	
	public FunctionValue(NativeFunction func) {
		code = null;
		context = null;
		name = null;
		this.func = func;
	}
	
	public StackFrame call(VM machine) {
		if (func == null)
			return new StackFrame(code, new Context(context), name);
		func.call(machine);
		return null;
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
	
	public interface NativeFunction {
		void call(VM machine);
	}
}
