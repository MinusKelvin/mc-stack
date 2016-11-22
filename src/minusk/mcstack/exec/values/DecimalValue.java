package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;

/**
 * @author MinusKelvin
 */
public class DecimalValue extends Value {
	public static final StringValue TYPE = new StringValue("decimal");
	
	public final double value;
	
	public DecimalValue(double v) {
		value = v;
	}
	
	public DecimalValue(StackFrame frame) {
		long v = frame.nextOpcode();
		v |= frame.nextOpcode() << 8;
		v |= frame.nextOpcode() << 16;
		v |= frame.nextOpcode() << 24;
		v |= (long) frame.nextOpcode() << 32;
		v |= (long) frame.nextOpcode() << 40;
		v |= (long) frame.nextOpcode() << 48;
		value = Double.longBitsToDouble(v | (long) frame.nextOpcode() << 56);
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		return new StringValue(Double.toString(value));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		return new IntegerValue((long) value);
	}
	
	@Override
	public DecimalValue toDecimal() {
		return this;
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert decimal to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert decimal to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert decimal to coroutine"));
	}
	
	@Override
	public int hashCode() {
		return Double.hashCode(value);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof DecimalValue))
			return false;
		return ((DecimalValue) other).value == value;
	}
}
