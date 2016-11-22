package minusk.mcstack.exec;

import minusk.mcstack.exec.values.Value;

/**
 * @author MinusKelvin
 */
public class MCStackException extends RuntimeException {
	public final Value errorMessage;
	
	public MCStackException(Value message) {
		super(message.toString());
		errorMessage = message;
	}
}
