package minusk.mcstack;

import minusk.mcstack.exec.*;
import minusk.mcstack.exec.values.*;

/**
 * @author MinusKelvin
 */
public class Main {
	public static void main(String[] args) {
		FunctionValue print = new FunctionValue(null, null, "print") {
			@Override
			public StackFrame call() {
				System.out.println(VM.stackPop().toString());
				return null;
			}
		};
		TableValue globals = new TableValue();
		globals.set(Context.GLOBAL_GLOBAL, globals);
		globals.set(new StringValue("print"), print);
		CoroutineValue thread = new CoroutineValue(new FunctionValue(
				new ByteCode(new byte[] {
						0x34, // Declare local (#0)
						0x34, // Declare local (#1)
						0x1C, 4, 'l', 'o', 'o', 'p', 36, // Push function length int 1 byte
								0x19, 1, 0, 1, 0, // Push integer 4 bytes (65537)
								0x32, // Load local (#1)
								0x21, // Dup
								0x14, 5, 'p', 'r', 'i', 'n', 't', // Push "print"
								0x30, // Load global
								0x02, // Call
								0x17, 1, // Push integer 1 byte (1)
								0x41, // Subtract
								0x21, // Dup
								0x19, 1, 0, 1, 0, // Push integer 4 bytes (65537)
								0x33, // Store local (#1)
								0x17, 0, // Push integer 1 byte (0)
								0x61, // Comparison !=
								0x19, 0, 0, 1, 0, // Push integer 4 bytes (65536)
								0x32, // Load local (#0)
								0x70, // Conditional tail call
						0x21, // Dup
						0x17, 0, // Push integer (0)
						0x33, // Store local (#0)
						0x17, 10, // Push integer (10)
						0x17, 1, // Push integer (1)
						0x33, // Store local (#1)
						0x02, // Call
						
						0x14, 5, 't', 'e', 's', 't', ' ', // Push "test "
						0x14, 5, 'w', 'o', 'r', 'l', 'd', // Push "world"
						0x45, // Concatenate
						0x14, 5, 'p', 'r', 'i', 'n', 't', // Push "print"
						0x30, // Load global
						0x02, // Call
						
						0x1C, 5, 'o', 'u', 't', 'e', 'r', 29, // Push function length int 1 byte
								0x34, // Declare local (1#0)
								0x17, 0, // Push integer (0)
								0x33, // Store local (1#0)
								0x1C, 7, 'c', 'l', 'o', 's', 'u', 'r', 'e', 15, // Push function length int 1 byte
										0x19, 0, 0, 1, 0, // Push integer (65537)
										0x32, // Load local (1#0)
										0x14, 5, 'p', 'r', 'i', 'n', 't', // Push "print"
										0x30, // Load global
										0x01, // Tail call
						0x21, // Dup                 // Func A
						0x17, 0, // Push integer (0)
						0x22, // Swap
						0x02, // Call                // Func A Closure 0
						0x21, // Dup                 // Func A Closure 0 Closure 0
						0x02, // Call                // PRINT 0
						0x22, // Swap                // Closure 0 Func A
						0x17, 35, // Push integer (35)
						0x22, // Swap
						0x02, // Call                // Closure 0 Closure 35
						0x21, // Dup
						0x02, // Call                // PRINT 35
						0x22, // Swap
						0x02, // Call                // PRINT 0
						0x02, // Call                // PRINT 35
				}),
				new Context(globals), "main"));
		
		while (thread.status != CoroutineValue.Status.DEAD) {
			System.out.println("Stack: "+thread.stack);
			System.out.println("Callstack: "+thread.frames);
			Value error = VM.resume(thread, 1);
			if (error != null)
				throw new MCStackException(error);
		}
	}
}
