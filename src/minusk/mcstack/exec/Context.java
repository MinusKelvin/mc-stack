package minusk.mcstack.exec;

import minusk.mcstack.exec.values.NullValue;
import minusk.mcstack.exec.values.StringValue;
import minusk.mcstack.exec.values.TableValue;
import minusk.mcstack.exec.values.Value;

import java.util.ArrayList;

/**
 * @author MinusKelvin
 */
public class Context {
	public static final StringValue GLOBAL_GLOBAL = new StringValue("_G");
	
	private final Context parent;
	private TableValue globals;
	private final ArrayList<Value> locals = new ArrayList<>();
	
	public Context(Context parent) {
		this.parent = parent;
		globals = parent.globals;
	}
	
	public Context(TableValue environment) {
		parent = null;
		globals = environment;
	}
	
	public Context() {
		parent = null;
		globals = new TableValue();
		globals.set(GLOBAL_GLOBAL, globals);
	}
	
	public Value getLocal(int var, int level) {
		if (level == 0) {
			if (var >= locals.size())
				throw new MCStackException(new StringValue("tried to get local that does not exist"));
			return locals.get(var);
		}
		if (parent == null)
			throw new MCStackException(new StringValue("tried to get local from a level above the highest level"));
		return parent.getLocal(var, level-1);
	}
	
	public void setLocal(int var, int level, Value value) {
		if (level == 0) {
			if (var >= locals.size())
				throw new MCStackException(new StringValue("tried to set local that does not exist"));
			locals.set(var, value);
		} else {
			if (parent == null)
				throw new MCStackException(new StringValue("tried to set local from a level above the highest level"));
			parent.setLocal(var, level - 1, value);
		}
	}
	
	public void declareLocal() {
		locals.add(NullValue.INSTANCE);
	}
	
	public Value getGlobal(Value key) {
		return globals.get(key);
	}
	
	public void setGlobal(Value key, Value value) {
		globals.set(key, value);
	}
	
	public TableValue getEnvironment() {
		return globals;
	}
	
	public void setEnvironment(TableValue env) {
		globals = env;
	}
}
