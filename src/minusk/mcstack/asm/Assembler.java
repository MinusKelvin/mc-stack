package minusk.mcstack.asm;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * @author MinusKelvin
 */
public class Assembler {
	private static final HashMap<String, Byte> singleParts = new HashMap<>();
	
	static {
		singleParts.put("ret", (byte) 0x00);
		singleParts.put("tailcall", (byte) 0x01);
		singleParts.put("call", (byte) 0x02);
		singleParts.put("yield", (byte) 0x03);
		singleParts.put("resume", (byte) 0x04);
		singleParts.put("lresume", (byte) 0x05);
		singleParts.put("lret", (byte) 0x06);
		
		singleParts.put("pop", (byte) 0x20);
		singleParts.put("dup", (byte) 0x21);
		singleParts.put("swap", (byte) 0x22);
		
		singleParts.put("ldglobal", (byte) 0x30);
		singleParts.put("stglobal", (byte) 0x31);
		singleParts.put("ldlocal", (byte) 0x32);
		singleParts.put("stlocal", (byte) 0x33);
		singleParts.put("declare", (byte) 0x34);
		singleParts.put("ldindex", (byte) 0x35);
		singleParts.put("stindex", (byte) 0x36);
		
		singleParts.put("add", (byte) 0x40);
		singleParts.put("sub", (byte) 0x41);
		singleParts.put("mul", (byte) 0x42);
		singleParts.put("div", (byte) 0x43);
		singleParts.put("mod", (byte) 0x44);
		singleParts.put("concat", (byte) 0x45);
		singleParts.put("neg", (byte) 0x46);
		
		singleParts.put("bitor", (byte) 0x50);
		singleParts.put("bitand", (byte) 0x51);
		singleParts.put("bitxor", (byte) 0x52);
		singleParts.put("bitnot", (byte) 0x53);
		singleParts.put("lshift", (byte) 0x54);
		singleParts.put("arshift", (byte) 0x55);
		singleParts.put("rshift", (byte) 0x56);
		
		singleParts.put("eq", (byte) 0x60);
		singleParts.put("neq", (byte) 0x61);
		singleParts.put("gt", (byte) 0x62);
		singleParts.put("gteq", (byte) 0x63);
		singleParts.put("lt", (byte) 0x64);
		singleParts.put("lteq", (byte) 0x65);
		singleParts.put("and", (byte) 0x66);
		singleParts.put("or", (byte) 0x67);
		singleParts.put("not", (byte) 0x68);
		
		singleParts.put("tailif", (byte) 0x70);
		singleParts.put("if", (byte) 0x71);
		singleParts.put("tailifelse", (byte) 0x72);
		singleParts.put("ifelse", (byte) 0x73);
	}
	
	public static byte[] assemble(Reader assembly) throws IOException {
		ArrayList<Byte> code = assembleFunction(new LineStream(assembly), 0);
		byte[] bytecode = new byte[code.size()];
		for (int i = 0; i < bytecode.length; i++) {
			bytecode[i] = code.get(i);
//			System.out.printf("0x%X%n", bytecode[i]);
		}
		return bytecode;
	}
	
	private static ArrayList<Byte> assembleFunction(LineStream stream, int indentation) {
		ArrayList<Byte> code = new ArrayList<>();
		while (true) {
			String line = stream.next();
			if (line == null)
				return code;
			
			if (line.trim().isEmpty())
				continue;
			if (line.trim().charAt(0) == '#')
				continue;
			
			int indent = 0;
			for (int i = 0; i < line.length(); i++, indent++)
				if (line.charAt(i) != '\t')
					break;
			if (indent > indentation)
				throw new IllegalStateException("Indented more than it should be at this line (line="+stream.getLineNumber()+")");
			if (indent < indentation) {
				stream.push(line);
				return code;
			}
			
			line = line.trim();
			if (!line.contains(" ")) {
				code.add(getSinglePart(line.toLowerCase(), stream.getLineNumber()));
				continue;
			}
			
			for (int i = 0; i < line.length(); i++) {
				if (line.charAt(i) == ' ') {
					indent = i;
					break;
				}
			}
			
			String mnemonic = line.substring(0,indent).toLowerCase();
			line = line.substring(indent).trim();
			switch (mnemonic) {
				case "push":
					if (line.equalsIgnoreCase("null"))
						code.add((byte) 0x10);
					else if (line.equalsIgnoreCase("true"))
						code.add((byte) 0x11);
					else if (line.equalsIgnoreCase("false"))
						code.add((byte) 0x12);
					else if (line.equalsIgnoreCase("table"))
						code.add((byte) 0x1B);
					else if (line.matches("^[+-]?(?:0|[1-9]\\d*)$")) {
						// Decimal number
						pushNumber(code, Long.parseLong(line));
					} else if (line.matches("^[+-]?0\\d+$")) {
						// Octal number
						pushNumber(code, Long.parseLong(line, 8));
					} else if (line.matches("^[+-]?0x[\\dA-Fa-f]+$")) {
						// Hexadecimal number
						if (line.charAt(0) != '0')
							pushNumber(code, Long.parseLong(line.charAt(0) + line.substring(3), 16));
						else
							pushNumber(code, Long.parseLong(line.substring(2), 16));
					} else if (line.matches("^[+-]?0b[01]+$")) {
						// Binary number
						if (line.charAt(0) != '0')
							pushNumber(code, Long.parseLong(line.charAt(0) + line.substring(3), 2));
						else
							pushNumber(code, Long.parseLong(line.substring(2), 2));
					} else if (line.matches("^[+-]?\\d*\\.\\d+$")) {
						// Decimal
						code.add((byte) 0x13);
						long doublebits = Double.doubleToRawLongBits(Double.parseDouble(line));
						for (int i = 0; i < 8; i++)
							code.add((byte) (doublebits >> i*8));
					} else if (line.matches("^\".*\"$")) {
						// String
						line = line.substring(1, line.length()-1);
						// Escape escapes
						line = line.replaceAll("\\n","\n");
						line = line.replaceAll("\\t","\t");
						line = line.replaceAll("\\\\","\\");
						pushString(code, line);
					}
					break;
				case "function":
					ArrayList<Byte> functionCode = assembleFunction(stream, indentation+1);
					int bytes = functionCode.size();
					
					if (bytes > 65535)
						code.add((byte) 0x1E);
					else if (bytes > 255)
						code.add((byte) 0x1D);
					else
						code.add((byte) 0x1C);
					
					if (line.length() > 255)
						throw new IllegalStateException("Function name too long (length > 255) on line "+stream.getLineNumber());
					code.add((byte) line.length());
					
					for (int i = 0; i < line.length(); i++)
						code.add((byte) line.charAt(i));
					
					if (bytes > 65535)
						for (int i = 0; i < 4; i++)
							code.add((byte) (bytes >> i*8));
					else if (bytes > 255)
						for (int i = 0; i < 2; i++)
							code.add((byte) (bytes >> i*8));
					else
						code.add((byte) bytes);
					
					code.addAll(functionCode);
					break;
				default:
					throw new IllegalStateException("Invalid mnemonic: "+line+" on line "+stream.getLineNumber());
			}
		}
	}
	
	private static void pushNumber(ArrayList<Byte> code, long number) {
		if (number > Integer.MAX_VALUE || number < Integer.MIN_VALUE) {
			code.add((byte) 0x1A);
			for (int i = 0; i < 8; i++)
				code.add((byte) (number >> 8*i));
		} else if (number > Short.MAX_VALUE || number < Short.MIN_VALUE) {
			code.add((byte) 0x19);
			for (int i = 0; i < 4; i++)
				code.add((byte) (number >> 8*i));
		} else if (number > Byte.MAX_VALUE || number < Byte.MIN_VALUE) {
			code.add((byte) 0x18);
			for (int i = 0; i < 2; i++)
				code.add((byte) (number >> 8*i));
		} else {
			code.add((byte) 0x17);
			code.add((byte) number);
		}
	}
	
	private static void pushString(ArrayList<Byte> code, String string) {
		if (string.length() > 65535) {
			code.add((byte) 0x16);
			for (int i = 0; i < 4; i++)
				code.add((byte) (string.length() >> 8*i));
		} else if (string.length() > 255) {
			code.add((byte) 0x15);
			for (int i = 0; i < 2; i++)
				code.add((byte) (string.length() >> 8*i));
		} else {
			code.add((byte) 0x14);
			code.add((byte) string.length());
		}
		for (int i = 0; i < string.length(); i++)
			code.add((byte) string.charAt(i));
	}
	
	private static Byte getSinglePart(String line, int lineNumber) {
		if (singleParts.containsKey(line))
			return singleParts.get(line);
		throw new IllegalStateException("Invalid mnemonic: "+line+" on line "+lineNumber);
	}
	
	private static class LineStream {
		private final Scanner scanner;
		private String lastLine;
		private int line = 0;
		
		public LineStream(Reader input) {
			scanner = new Scanner(input);
		}
		
		public String next() {
			line++;
			if (lastLine != null) {
				String tmp = lastLine;
				lastLine = null;
				return tmp;
			}
			if (scanner.hasNextLine())
				return scanner.nextLine();
			return null;
		}
		
		public void push(String str) {
			lastLine = str;
			line--;
		}
		
		public int getLineNumber() {
			return line;
		}
	}
}
