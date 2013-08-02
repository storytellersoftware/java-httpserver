package httpserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class MethodWrapper {

	private String path;
	private Method method;
	private List<Class> parameterTypes;

	public MethodWrapper(String path, String methodName, Class callingClass) throws HTTPException {
		try {
			parameterTypes = new ArrayList<Class>();
			String[] paths = path.split("/");
			StringBuilder pathBuilder = new StringBuilder();

			for(String part : paths) {
				if(!part.matches("\\{[A-Za-z0-9]{1,}\\}"))
					part.toLowerCase();
				else {
					String paramClass = "java.lang."  + part.substring(1, part.length() - 1);
					parameterTypes.add(Class.forName(paramClass));
				}

				pathBuilder.append('/');
				pathBuilder.append(part);
			}

			path = pathBuilder.toString();

			Class[] paramTypes = new Class[parameterTypes.size()];
			for(int i=0; i<parameterTypes.size(); i++)
				paramTypes[i] = parameterTypes.get(i);

			method = callingClass.getMethod(methodName, paramTypes);

		} catch(ClassNotFoundException | NoSuchMethodException | SecurityException e) {
			throw new HTTPException("Could not add path.", e);
		}
	}

	public void invoke(Object callingClass, List<Object> parameters) throws HTTPException {
		Object[] params = parameters.toArray();
		try {
			method.invoke(callingClass, params);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			//throw new HTTPException("Could not invoke method.", e);
			e.printStackTrace();
		}
	}
}
