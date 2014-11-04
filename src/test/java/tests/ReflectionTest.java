package tests;

import httpserver.HTTPHandler;
import httpserver.HTTPRequest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class ReflectionTest {
  
  @Test
  public void getAllMethods() {
    Class<? extends HTTPHandler> c = HandlerTest.class;
    
    List<Method> hellos = getMethodsByName(c, "sayHello");
    List<Class<? extends Object>> parameterTypes = new ArrayList<>();
    parameterTypes.add(String.class);
    
    // find our method
    outer:
    for (Method m : new ArrayList<Method>(hellos)) {
      // determine if this method takes all parameters of this path
      Class<? extends Object>[] reqParams = m.getParameterTypes();
      
      // does it have enough parameters?
      if (reqParams.length < parameterTypes.size() + 1 || 
          reqParams.length > parameterTypes.size() + 3) {
        hellos.remove(m);
        continue;
      }
      
      // going backwards, make sure all parameters match
      for (int i = 1; i <= parameterTypes.size(); i++) {
        // i goes up because inParams and parameterTypes will have different
        // lengths.
        
        Class<?> inClass = parameterTypes.get(parameterTypes.size() - i);
        Class<?> reqClass = reqParams[reqParams.length - i];
        
        if (!inClass.equals(reqClass)) {
          hellos.remove(m);
          continue outer;
        }
      }
      
      if (reqParams.length > parameterTypes.size() + 1) {
        // is second param HTTPRequest?
        if (reqParams[1].equals(HTTPRequest.class)) {
          if (reqParams.length == parameterTypes.size() + 3 &&
              !reqParams[2].equals(Map.class)) {
            hellos.remove(m);
            continue;
          }
        }
        else if (!reqParams[1].equals(Map.class)) {
          hellos.remove(m);
          continue;
        }
      }
    }
    
    for (Method m : hellos) {
      System.out.println(m.toGenericString());
    }
  }

  
  public static List<Method> getMethodsByName(Class<?> c, String name) {
    List<Method> out = new ArrayList<>();
    
    for (Method m: c.getMethods()) {
      if (m.getName().equals(name)) {
        out.add(m);
      }
    }
    
    return out;
  }
}
