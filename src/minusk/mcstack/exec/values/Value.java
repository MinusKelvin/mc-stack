package minusk.mcstack.exec.values;

/**
 * @author MinusKelvin
 */
public abstract class Value {
	public abstract StringValue getType();
	public abstract StringValue toStringValue();
	public abstract BooleanValue toBoolean();
	public abstract IntegerValue toInt();
	public abstract DecimalValue toDecimal();
	public abstract TableValue toTable();
	public abstract FunctionValue toFunction();
	public abstract CoroutineValue toCoroutine();
	
	public String toString() {
		return toStringValue().toString();
	}
}
