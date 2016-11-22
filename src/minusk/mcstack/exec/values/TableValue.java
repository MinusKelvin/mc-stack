package minusk.mcstack.exec.values;

import minusk.mcstack.exec.MCStackException;

import java.util.HashMap;

/**
 * @author MinusKelvin
 */
public class TableValue extends Value {
	public static final StringValue TYPE = new StringValue("table");
	
	private final HashMap<Value, Value> hashtable = new HashMap<>();
	
	public Value get(Value key) {
		if (key.equals(NullValue.INSTANCE))
			throw new MCStackException(new StringValue("cannot index table with null"));
		Value v = hashtable.get(key);
		if (v == null)
			return NullValue.INSTANCE;
		return v;
	}
	
	public void set(Value key, Value value) {
		if (key.equals(NullValue.INSTANCE))
			throw new MCStackException(new StringValue("cannot index table with null"));
		if (value.equals(NullValue.INSTANCE))
			hashtable.remove(key);
		else
			hashtable.put(key, value);
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		throw new MCStackException(new StringValue("cannot convert table to string"));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert table to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert table to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		return this;
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert table to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		throw new MCStackException(new StringValue("cannot convert table to coroutine"));
	}
	
	@Override
	public String toString() {
		return "table: "+hashCode();
	}
}
