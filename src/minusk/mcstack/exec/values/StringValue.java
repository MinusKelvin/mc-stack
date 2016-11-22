package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;

import java.io.UnsupportedEncodingException;

/**
 * @author MinusKelvin
 */
public class StringValue extends Value {
	public static final StringValue TYPE = new StringValue("string");
	
	private final byte[] value;
	private final int hash;
	
	public StringValue(String v) {
		try {
			value = v.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Apparently ISO-8859-1 is not supported. The heck?", e);
		}
		int h = 0;
		for (byte b : value)
			h = 31 * h + b;
		hash = h;
	}
	
	public StringValue(StackFrame frame, int lengthBytes) {
		int l = 0;
		for (int i = 0; i < lengthBytes; i++)
			l |= frame.nextOpcode() << (i*8);
		value = new byte[l];
		for (int i = 0; i < l; i++)
			value[i] = (byte) frame.nextOpcode();
		int h = 0;
		for (byte b : value)
			h = 31 * h + b;
		hash = h;
	}
	
	public StringValue(StringValue left, StringValue right) {
		value = new byte[left.value.length + right.value.length];
		System.arraycopy(left.value, 0, value, 0, left.value.length);
		System.arraycopy(right.value, 0, value, left.value.length, right.value.length);
		int h = 0;
		for (byte b : value)
			h = 31 * h + b;
		hash = h;
	}
	
	@Override
	public String toString() {
		try {
			return new String(value, "ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Apparently ISO-8859-1 is not supported. The heck?", e);
		}
	}
	
	@Override
	public StringValue getType() {
		return StringValue.TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		return this;
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert string to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert string to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert string to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert string to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert string to thread"));
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof StringValue))
			return false;
		byte[] o = ((StringValue) other).value;
		if (o.length != value.length)
			return false;
		for (int i = 0; i < o.length; i++)
			if (o[i] != value[i])
				return false;
		return true;
	}
}
