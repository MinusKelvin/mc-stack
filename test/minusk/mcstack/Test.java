package minusk.mcstack;

import minusk.mcstack.asm.Assembler;
import minusk.mcstack.exec.Context;
import minusk.mcstack.exec.MCStackException;
import minusk.mcstack.exec.VM;
import minusk.mcstack.exec.values.CoroutineValue;
import minusk.mcstack.exec.values.FunctionValue;
import minusk.mcstack.exec.values.StringValue;
import minusk.mcstack.exec.values.TableValue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author MinusKelvin
 */
public class Test {
	public static void main(String[] args) throws IOException {
		ArrayList<String> tests = new ArrayList<>();
		tests.add("empty-stack.test");
		tests.add("nonexistant-local-get.test");
		tests.add("nonexistant-local-set.test");
		tests.add("nonexistant-upvalue-get.test");
		tests.add("nonexistant-upvalue-set.test");
		tests.add("undeclared-upvalue-get.test");
		tests.add("undeclared-upvalue-set.test");
		
		for (int i = 0; i < tests.size(); i++) {
			if (test(tests.get(i)))
				tests.set(i, tests.get(i)+" passed.");
			else
				tests.set(i, tests.get(i)+" failed!");
		}
		
		tests.forEach(System.out::println);
	}
	
	public static boolean test(String test) throws IOException {
		System.out.println("Running test "+test+". Description:");
		
		Reader stream = new InputStreamReader(Test.class.getResourceAsStream("/minusk/mcstack/tests/"+test));
		Scanner lines = new Scanner(stream);
		
		String line;
		while (!(line=lines.nextLine()).equals("%%"))
			System.out.println(line);
		
		boolean expectError;
		String errorMessage = null;
		
		System.out.print("Pass condition: ");
		switch (lines.nextLine()) {
			case "fail":
				expectError = true;
				errorMessage = lines.nextLine();
				System.out.println("Fail with error message \""+errorMessage+"\"");
				if (!lines.nextLine().equals("%%"))
					throw new IllegalStateException("Malformed test ("+test+")");
				break;
			case "succeed":
				expectError = false;
				System.out.println("Succeed");
				break;
			default:
				throw new IllegalStateException("Malformed test ("+test+")");
		}
		
		VM vm = new VM();
		
		TableValue globals = new TableValue();
		globals.set(new StringValue("print"), new FunctionValue(Test::print));
		globals.set(new StringValue("_G"), globals);
		
		byte[] assembly = Assembler.assemble(lines);
		System.out.println("Assembly (length "+assembly.length+"):");
		for (int i = 0; i < assembly.length; i++)
			System.out.printf("%02X%n", assembly[i]);
		
		CoroutineValue coroutine = new CoroutineValue(new FunctionValue(assembly, new Context(globals), test));
		
		while (true) {
			MCStackException e = vm.resume(coroutine, 0);
			if (e != null) {
				System.out.println("Error: "+e.errorMessage.toString());
				if (expectError && errorMessage.equals(e.errorMessage.toString())) {
					System.out.println("Test "+test+" passed!\n");
					return true;
				} else {
					System.out.println("Test "+test+" failed (error). Traceback:");
					e.printStackTrace(System.out);
					System.out.println();
					return false;
				}
			}
			if (coroutine.status == CoroutineValue.Status.DEAD)
				break;
		}
		if (expectError) {
			System.out.println("Test "+test+" failed (no error)!\n");
			return false;
		}
		System.out.println();
		return true;
	}
	
	private static void print(VM vm) {
		System.out.println(vm.stackPop().toString());
	}
}
