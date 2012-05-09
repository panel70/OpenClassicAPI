package ch.spacebase.openclassic.api.command;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ch.spacebase.openclassic.api.command.annotation.Command;


public abstract class CommandExecutor {

	public final Method getCommand(String command) {
		for(Method method : this.getClass().getMethods()) {
			Class<?> params[] = method.getParameterTypes();
			if(params.length == 3 && method.getAnnotation(Command.class) != null) {
				for(String alias : method.getAnnotation(Command.class).aliases()) {
					if(alias.equalsIgnoreCase(command)) return method;
				}
			}
		}
			
		return null;
	}
	
	public final List<Method> getCommands() {
		List<Method> result = new ArrayList<Method>();
		
		for(Method method : this.getClass().getMethods()) {
			Class<?> params[] = method.getParameterTypes();
			if(params.length == 3 && method.getAnnotation(Command.class) != null) {
				result.add(method);
			}
		}
		
		return result;
	}
	
}
