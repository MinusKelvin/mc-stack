package minusk.mcstack.exec;

import minusk.mcstack.exec.values.*;

import java.util.ArrayList;

/**
 * @author MinusKelvin
 */
public class VM {
	private static CoroutineValue runningCoroutine;
	
	/**
	 * 
	 * @param coroutine the coroutine to resume
	 * @param instructionsBeforeYield execute no more than this many instructions before yielding
	 * @return the error 
	 */
	public static Value resume(CoroutineValue coroutine, long instructionsBeforeYield) {
		ArrayList<CoroutineValue> coroutineStack = new ArrayList<>();
		runningCoroutine = coroutine;
		while (runningCoroutine.child != null) {
			runningCoroutine.status = CoroutineValue.Status.NORMAL;
			coroutineStack.add(runningCoroutine);
			runningCoroutine = runningCoroutine.child;
			runningCoroutine.status = CoroutineValue.Status.RUNNING;
		}
		long count = 0;
		
		try {
			while (true) {
				StackFrame frame = runningCoroutine.frames.get(runningCoroutine.frames.size() - 1);
				switch (frame.nextOpcode()) {
					case 0x00: // Return
						runningCoroutine.frames.remove(runningCoroutine.frames.size() - 1);
						break;
					case 0x01: // Tail call
						runningCoroutine.frames.remove(runningCoroutine.frames.size() - 1);
						// Falls through
					case 0x02: // Call
						frame = stackPop().toFunction().call();
						if (frame != null)
							runningCoroutine.frames.add(frame);
						break;
					case 0x03: // Yield
						runningCoroutine.status = CoroutineValue.Status.SUSPENDED;
						if (coroutineStack.size() == 0)
							return null;
						runningCoroutine = coroutineStack.get(coroutineStack.size()-1);
						runningCoroutine.child = null;
						runningCoroutine.status = CoroutineValue.Status.RUNNING;
						stackPush(BooleanValue.TRUE);
						break;
					case 0x04: // Resume
						CoroutineValue toResume = stackPop().toCoroutine();
						if (toResume.status != CoroutineValue.Status.SUSPENDED)
							throw new MCStackException(new StringValue("tried to resume a coroutine that was not suspended"));
						runningCoroutine.child = toResume;
						runningCoroutine.instructionsBeforeYield = 0;
						runningCoroutine.count = 0;
						while (runningCoroutine.child != null) {
							runningCoroutine.status = CoroutineValue.Status.NORMAL;
							coroutineStack.add(runningCoroutine);
							runningCoroutine = runningCoroutine.child;
							runningCoroutine.status = CoroutineValue.Status.RUNNING;
						}
						break;
					case 0x05: // Resume with automatic yield
						toResume = stackPop().toCoroutine();
						if (toResume.status != CoroutineValue.Status.SUSPENDED)
							throw new MCStackException(new StringValue("tried to resume a coroutine that was not suspended"));
						runningCoroutine.child = toResume;
						runningCoroutine.instructionsBeforeYield = stackPop().toInt().value;
						runningCoroutine.count = 0;
						while (runningCoroutine.child != null) {
							runningCoroutine.status = CoroutineValue.Status.NORMAL;
							coroutineStack.add(runningCoroutine);
							runningCoroutine = runningCoroutine.child;
							runningCoroutine.status = CoroutineValue.Status.RUNNING;
						}
						break;
					case 0x10: // Push null
						stackPush(NullValue.INSTANCE);
						break;
					case 0x11: // Push true
						stackPush(BooleanValue.TRUE);
						break;
					case 0x12: // Push false
						stackPush(BooleanValue.FALSE);
						break;
					case 0x13: // Push a decimal
						stackPush(new DecimalValue(frame));
						break;
					case 0x14: // Push a string (length is 1 byte)
						stackPush(new StringValue(frame, 1));
						break;
					case 0x15: // Push a string (length is 2 bytes)
						stackPush(new StringValue(frame, 2));
						break;
					case 0x16: // Push a string (length is 4 bytes)
						stackPush(new StringValue(frame, 4));
						break;
					case 0x17: // Push an int (1 byte)
						stackPush(new IntegerValue(frame, 1));
						break;
					case 0x18: // Push an int (2 bytes)
						stackPush(new IntegerValue(frame, 2));
						break;
					case 0x19: // Push an int (4 bytes)
						stackPush(new IntegerValue(frame, 4));
						break;
					case 0x1A: // Push an int (8 byte)
						stackPush(new IntegerValue(frame, 8));
						break;
					case 0x1B: // Push an empty table
						stackPush(new TableValue());
						break;
					case 0x1C: // Push a function (length is 1 byte)
						stackPush(new FunctionValue(frame, 1));
						break;
					case 0x1D: // Push a function (length is 2 bytes)
						stackPush(new FunctionValue(frame, 2));
						break;
					case 0x1E: // Push a function (length is 4 bytes)
						stackPush(new FunctionValue(frame, 4));
						break;
					case 0x20: // Pop
						stackPop();
						break;
					case 0x21: // Duplicate
						Value v = stackPop();
						stackPush(v);
						stackPush(v);
						break;
					case 0x22: // Swap
						Value v1 = stackPop();
						Value v2 = stackPop();
						stackPush(v1);
						stackPush(v2);
						break;
					case 0x30: // Load a global variable
						stackPush(frame.context.getGlobal(stackPop()));
						break;
					case 0x31: // Store a global variable
						frame.context.setGlobal(stackPop(), stackPop());
						break;
					case 0x32: // Load a local variable
						int compound = stackPop().toInt().getInt();
						stackPush(frame.context.getLocal(compound & 0xFFFF, compound >> 16 & 0xFFFF));
						break;
					case 0x33: // Store a local variable
						compound = stackPop().toInt().getInt();
						frame.context.setLocal(compound & 0xFFFF, compound >> 16 & 0xFFFF, stackPop());
						break;
					case 0x34: // Declare a local variable
						frame.context.declareLocal();
						break;
					case 0x40: // Add
						Value rhs = stackPop();
						Value lhs = stackPop();
						if (lhs instanceof DecimalValue || rhs instanceof DecimalValue)
							stackPush(new DecimalValue(lhs.toDecimal().value + rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(lhs.toInt().value + rhs.toInt().value));
						break;
					case 0x41: // Subtract
						rhs = stackPop();
						lhs = stackPop();
						if (lhs instanceof DecimalValue || rhs instanceof DecimalValue)
							stackPush(new DecimalValue(lhs.toDecimal().value - rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(lhs.toInt().value - rhs.toInt().value));
						break;
					case 0x42: // Multiply
						rhs = stackPop();
						lhs = stackPop();
						if (lhs instanceof DecimalValue || rhs instanceof DecimalValue)
							stackPush(new DecimalValue(lhs.toDecimal().value * rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(lhs.toInt().value * rhs.toInt().value));
						break;
					case 0x43: // Divide
						rhs = stackPop();
						lhs = stackPop();
						if (lhs instanceof DecimalValue || rhs instanceof DecimalValue)
							stackPush(new DecimalValue(lhs.toDecimal().value / rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(lhs.toInt().value / rhs.toInt().value));
						break;
					case 0x44: // Modulo
						rhs = stackPop();
						lhs = stackPop();
						if (lhs instanceof DecimalValue || rhs instanceof DecimalValue)
							stackPush(new DecimalValue(lhs.toDecimal().value % rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(lhs.toInt().value % rhs.toInt().value));
						break;
					case 0x45: // Concatenate
						StringValue right = stackPop().toStringValue();
						StringValue left = stackPop().toStringValue();
						stackPush(new StringValue(left, right));
						break;
					case 0x46: // Negate
						rhs = stackPop();
						if (rhs instanceof DecimalValue)
							stackPush(new DecimalValue(-rhs.toDecimal().value));
						else
							stackPush(new IntegerValue(-rhs.toInt().value));
						break;
					case 0x47: // Table get index
						rhs = stackPop();
						TableValue table = stackPop().toTable();
						table.get(rhs);
						break;
					case 0x48: // Table set index
						rhs = stackPop();
						table = stackPop().toTable();
						table.set(rhs, stackPop());
						break;
					case 0x50: // Bitwise or
						stackPush(new IntegerValue(stackPop().toInt().value | stackPop().toInt().value));
						break;
					case 0x51: // Bitwise and
						stackPush(new IntegerValue(stackPop().toInt().value & stackPop().toInt().value));
						break;
					case 0x52: // Bitwise or
						stackPush(new IntegerValue(stackPop().toInt().value ^ stackPop().toInt().value));
						break;
					case 0x53: // Bitwise not
						stackPush(new IntegerValue(~stackPop().toInt().value));
						break;
					case 0x54: // Bitshift left
						rhs = stackPop();
						lhs = stackPop();
						stackPush(new IntegerValue(lhs.toInt().value << rhs.toInt().value));
						break;
					case 0x55: // Bitshift arithmetic right
						rhs = stackPop();
						lhs = stackPop();
						stackPush(new IntegerValue(lhs.toInt().value >> rhs.toInt().value));
						break;
					case 0x56: // Bitshift logical right
						rhs = stackPop();
						lhs = stackPop();
						stackPush(new IntegerValue(lhs.toInt().value >>> rhs.toInt().value));
						break;
					case 0x60: // Compare ==
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(lhs.equals(rhs)));
						break;
					case 0x61: // Compare !=
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(!lhs.equals(rhs)));
						break;
					case 0x62: // Compare >
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(lhs.toDecimal().value > rhs.toDecimal().value));
						break;
					case 0x63: // Compare >=
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(lhs.toDecimal().value >= rhs.toDecimal().value));
						break;
					case 0x64: // Compare <
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(lhs.toDecimal().value < rhs.toDecimal().value));
						break;
					case 0x65: // Compare <=
						rhs = stackPop();
						lhs = stackPop();
						stackPush(BooleanValue.get(lhs.toDecimal().value <= rhs.toDecimal().value));
						break;
					case 0x66: // Logical and
						rhs = stackPop();
						lhs = stackPop();
						if (lhs.toBoolean().value)
							stackPush(rhs);
						else
							stackPush(lhs);
						break;
					case 0x67: // Logical or
						rhs = stackPop();
						lhs = stackPop();
						if (lhs.toBoolean().value)
							stackPush(lhs);
						else
							stackPush(rhs);
						break;
					case 0x68: // Logical not
						stackPush(BooleanValue.get(!stackPop().toBoolean().value));
						break;
					case 0x70: // Conditional tail call
						runningCoroutine.frames.remove(runningCoroutine.frames.size() - 1);
						// Falls through
					case 0x71: // Conditional call (condition ifTrue 0x61)
						FunctionValue ifTrue = stackPop().toFunction();
						if (stackPop().toBoolean().value) {
							frame = ifTrue.call();
							if (frame != null)
								runningCoroutine.frames.add(frame);
						}
						break;
					case 0x72: // Conditional with else tail call
						runningCoroutine.frames.remove(runningCoroutine.frames.size() - 1);
						// Falls through
					case 0x73: // Conditional with else call (condition ifTrue ifFalse 0x63)
						FunctionValue ifFalse = stackPop().toFunction();
						ifTrue = stackPop().toFunction();
						if (stackPop().toBoolean().value) {
							frame = ifTrue.call();
							if (frame != null)
								runningCoroutine.frames.add(frame);
						} else {
							frame = ifFalse.call();
							if (frame != null)
								runningCoroutine.frames.add(frame);
						}
						break;
					default:
						throw new MCStackException(new StringValue("invalid opcode"));
				}
				
				for (int i = coroutineStack.size()-1; i >= 0; i--) {
					if (++coroutineStack.get(i).count == coroutineStack.get(i).instructionsBeforeYield) {
						int limit = coroutineStack.size();
						for (int j = i+1; j < limit; j++)
							coroutineStack.remove(i+1).status = CoroutineValue.Status.SUSPENDED;
						runningCoroutine = coroutineStack.get(i);
						runningCoroutine.child = null;
						runningCoroutine.status = CoroutineValue.Status.RUNNING;
						stackPush(BooleanValue.TRUE);
					}
				}
				
				if (runningCoroutine.frames.size() == 0) {
					runningCoroutine.status = CoroutineValue.Status.DEAD;
					if (coroutineStack.size() == 0)
						return null;
					runningCoroutine = coroutineStack.get(coroutineStack.size()-1);
					runningCoroutine.child = null;
					runningCoroutine.status = CoroutineValue.Status.RUNNING;
					stackPush(BooleanValue.TRUE);
				}
				
				if (++count == instructionsBeforeYield) {
					coroutineStack.forEach(c -> c.status = CoroutineValue.Status.SUSPENDED);
					return null;
				}
			}
		} catch (MCStackException e) {
			runningCoroutine.status = CoroutineValue.Status.DEAD;
			if (coroutineStack.size() == 0)
				return e.errorMessage;
			runningCoroutine = coroutineStack.remove(coroutineStack.size()-1);
			stackPush(e.errorMessage);
			stackPush(BooleanValue.FALSE);
			runningCoroutine.status = CoroutineValue.Status.RUNNING;
		}
		return null;
	}
	
	public static void stackPush(Value value) {
		runningCoroutine.stack.add(value);
	}
	
	public static Value stackPop() {
		if (runningCoroutine.stack.size() == 0)
			throw new MCStackException(new StringValue("stack is empty"));
		return runningCoroutine.stack.remove(runningCoroutine.stack.size()-1);
	}
	
	public static CoroutineValue getRunningCoroutine() {
		return runningCoroutine;
	}
}
