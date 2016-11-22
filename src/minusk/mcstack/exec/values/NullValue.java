package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;

/**
 * @author MinusKelvin
 */
public class NullValue extends Value {
	public static final NullValue INSTANCE = new NullValue();
	public static final StringValue TYPE = new StringValue("null");
	
	private NullValue() {}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		throw new MCStackException(new StringValue("cannot convert null to string"));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.FALSE;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert null to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert null to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert null to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert null to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert null to coroutine"));
	}
	
	@Override
	public String toString() {
		return "null";
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object other) {
		return other instanceof NullValue;
	}
}
