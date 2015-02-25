package httpserver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// TODO Rewrite almost all of the javadocs for this class.
//      They're based on old, reflective, less conventional ways of doing
//      things.
//      Also, just clean this file up in general.
//      And maybe rename it to "RouteWrapper?"
/**
 * A MethodWrapper is a wrapper for the {@link java.lang.reflect.Method} class.
 * It allows us to easily invoke a method based on a path without worrying
 * about parsing out the variables and whatnot.
 *
 * MethodWrapper isn't visible to the outside world, because it shouldn't be
 * used outside of this httpserver. This documentation exists to help people
 * better understand and modify the underlying server.
 */
class MethodWrapper {
    private static final String LANG_PATH = "java.lang.";

    private List<String> routePath = new ArrayList<>();
    private Route route;
    private boolean usesVarargs = false;

    /**
     * Create a MethodWrapper.
     *
     * Paths should come in <code>/relative/path/to/match</code>. To use a
     * variable in a path, it should come in
     * <code>/path/with/{VariableType}</code> form, where VariableType is a
     * class inside of the `java.lang` package, and has a constructor that takes
     * in a String. A better explaination is in {@link HttpHandler#addGET}
     *
     * @param path          Path the be matched for this method to be used.
     * @param route         Route, with the `handle` method to be called.
     *
     * @throws HttpException   If the passed in path is invalid.
     * @see HttpHandler#addGET
     */
    public MethodWrapper(String path, Route route) {
        if (route == null) {
            throw new NullPointerException("Route may not be null!");
        }

        this.route = route;

        String[] paths = cleanPath(path).split("/");

        for (int i = 0; i < paths.length; i++) {
            if (paths[i].isEmpty()) {
                continue;
            }

            routePath.add(paths[i]);
            if (paths[i].equals("{*}")) {
                // confirm this is the last segment
                if (i != paths.length - 1) {
                    while (++i < paths.length && paths[i].isEmpty());
                    if (i != paths.length - 1) {
                        throw new RuntimeException("{*} must be the final section of your path!");
                    }
                    usesVarargs = true;
                }
            }
        }
    }

    /**
     * Call the route.
     *
     * @param path          The path that caused the method to be called. This is
     *                      where variables come from.
     */
    public void invoke(HttpRequest request, HttpResponse response, List<String> calledPath) {
        Map<String, String> params = new HashMap<>();
        List<String> varargs = new ArrayList<>();

        for (int i = 0; i < routePath.size(); i++) {
            if (isDynamic(routePath.get(i))) {
                params.put(stripDynamic(routePath.get(i)), calledPath.get(i));
            }

            if (routePath.get(i).equals("{*}")) {
                while (i < calledPath.size()) {
                    varargs.add(calledPath.get(i));
                    i++;
                }
            }
        }

        request.mergeParams(params);
        request.mergeVarargs(varargs);

        route.handle(request, response);
    }


    /**
     * Determines how correct a route is from a path. The higher the number,
     * the more likely this route is the correct route.
     *
     * Correctness is based on the similarity of the passed in path, and the
     * route's path. If a path segment (the part between two slashes) matches
     * this route's corresponding segment exactly, the correctness number is
     * incremented by two. If the segment is dynamic, the correctness number is
     * incremented by one.
     *
     * If a zero is returned, the passed in path doesn't match this method's
     * path at all.
     *
     * @param calledPath  The path, in split apart and sanitized form.
     *
     * @return  A "correctness number", based on how well the passed in
     *          path matches this method's path.
     */
    public int howCorrect(List<String> calledPath) {
        // If the paths aren't the same length and it is not an array,
        // this is the wrong method.
        if (calledPath.size() != routePath.size()) {
            if (!usesVarargs) {
                return 0;
            }
        }

        // Start count at 1 because of the length matching.
        int count = 1;
        for (int i = 0; i < routePath.size(); i++) {
            // If the paths are equal, give it priority over other methods.
            if (routePath.get(i).equals(calledPath.get(i))) {
                count += 2;
            }
            else if (isDynamic(routePath.get(i))) {
                count += 1;
            }
        }
        return count;
    }

    /**
     * Checks if there is dynamic text in part of a path.
     *
     * @param path  Part of the path you want to check for dynamic data.
     * @return  If the path matches the regex pattern `\{([A-Za-z0-9]{1,}|\*)\}`
     */
    private boolean isDynamic(String path) {
        return path.matches("\\{([A-Za-z0-9]{1,}|\\*)\\}");
    }

    public boolean matchesPerfectly(List<String> path) {
        return routePath.equals(path);
    }

    public static String cleanPath(String path) {
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        return path;
    }

    public static String stripDynamic(String dynamicPath) {
        return dynamicPath.substring(1, dynamicPath.length() - 1);
    }
}
