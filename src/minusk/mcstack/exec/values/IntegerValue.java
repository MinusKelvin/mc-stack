package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;

/**
 * @author MinusKelvin
 */
public class IntegerValue extends Value {
	public static final StringValue TYPE = new StringValue("integer");
	
	public final long value;
	
	public IntegerValue(long v) {
		value = v;
	}
	
	public IntegerValue(StackFrame frame, int bytes) {
		long v = 0;
		for (int i = 0; i < bytes; i++)
			v |= (long) frame.nextOpcode() << (i*8);
		value = v;
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		return new StringValue(Long.toString(value));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		return this;
	}
	
	@Override
	public DecimalValue toDecimal() {
		return new DecimalValue(value);
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert integer to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert integer to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert integer to coroutine"));
	}
	
	@Override
	public int hashCode() {
		return Long.hashCode(value);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof IntegerValue))
			return false;
		return ((IntegerValue) other).value == value;
	}
	
	public int getInt() {
		if (Long.compareUnsigned(value & 0xFFFF_FFFFL, value) != 0)
			throw new MCStackException(new StringValue("integer out of range"));
		return (int) value;
	}
}
