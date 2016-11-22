package minusk.mcstack.exec.values;

import minusk.mcstack.exec.ByteCode;
import minusk.mcstack.exec.Context;
import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.StackFrame;

import java.util.ArrayList;

/**
 * @author MinusKelvin
 */
public class CoroutineValue extends Value {
	public static final StringValue TYPE = new StringValue("coroutine");
	
	private static final ByteCode coroutineByteCode = new ByteCode(new byte[] {1}); // Tail call
	
	public final ArrayList<Value> stack = new ArrayList<>();
	public final ArrayList<StackFrame> frames = new ArrayList<>();
	public Status status = Status.SUSPENDED;
	public CoroutineValue child;
	public long count, instructionsBeforeYield;
	
	public CoroutineValue(FunctionValue func) {
		frames.add(new StackFrame(coroutineByteCode, new Context(), "__coroutine_launcher"));
		stack.add(func);
	}
	
	@Override
	public StringValue getType() {
		return TYPE;
	}
	
	@Override
	public StringValue toStringValue() {
		throw new MCStackException(new StringValue("cannot convert coroutine to string"));
	}
	
	@Override
	public BooleanValue toBoolean() {
		return BooleanValue.TRUE;
	}
	
	@Override
	public IntegerValue toInt() {
		throw new MCStackException(new StringValue("cannot convert coroutine to integer"));
	}
	
	@Override
	public DecimalValue toDecimal() {
		throw new MCStackException(new StringValue("cannot convert coroutine to decimal"));
	}
	
	@Override
	public TableValue toTable() {
		throw new MCStackException(new StringValue("cannot convert coroutine to table"));
	}
	
	@Override
	public FunctionValue toFunction() {
		throw new MCStackException(new StringValue("cannot convert coroutine to function"));
	}
	
	@Override
	public CoroutineValue toCoroutine() {
		return this;
	}
	
	@Override
	public String toString() {
		return "coroutine: "+hashCode();
	}
	
	public enum Status {
		DEAD, RUNNING, SUSPENDED, NORMAL
	}
}
