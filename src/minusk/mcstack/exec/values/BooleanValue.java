package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;

/**
 * @author MinusKelvin
 */
public class BooleanValue extends Value {
	public static final BooleanValue TRUE = new BooleanValue(true);
	public static final BooleanValue FALSE = new BooleanValue(false);
	public static final StringValue TRUE_STR = new StringValue("true");
	public static final StringValue FALSE_STR = new StringValue("false");
	public static final StringValue TYPE = new StringValue("boolean");
	
	public final boolean value;
	
	private BooleanValue(boolean v) {
		value = v;
	}
	
	public static BooleanValue get(boolean v) {
		return v ? TRUE : FALSE;
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		return value ? TRUE_STR : FALSE_STR;
	}
	
	@Override
	public BooleanValue toBoolean() {
		return this;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert boolean to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert boolean to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert boolean to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert boolean to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert boolean to coroutine"));
	}
	
	@Override
	public int hashCode() {
		return Boolean.hashCode(value);
	}
	
	@Override
	public boolean equals(Object other) {
		if (!(other instanceof BooleanValue))
			return false;
		return ((BooleanValue) other).value == value;
	}
}
