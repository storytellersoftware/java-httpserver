package httpserver;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public abstract class Route {
    private List<String> routePath = new ArrayList<>();
    private boolean usesVarargs = false;


    public Route(String path) {
        String[] pathSegments = cleanPath(path).split("/");

        for (int i = 0; i < pathSegments.length; i++) {
            if (pathSegments[i].isEmpty()) {
                continue;
            }

            routePath.add(pathSegments[i]);

            if (pathSegments[i].equals("{*}")) {
                if (i != pathSegments.length - 1) {
                    while (++i < pathSegments.length && pathSegments[i].isEmpty());
                    if (i != pathSegments.length - 1) {
                        throw new RuntimeException("\"{*}\" must be the final segment in your path.");
                    }

                    usesVarargs = true;
                }
            }
        }
    }


    public void invoke(HttpRequest request, HttpResponse response) {
        try {
            Map<String, String> urlParams = new HashMap<>();
            List<String> varargs = new ArrayList<>();

            List<String> calledPath = request.getSplitPath();

            for (int i = 0; i < routePath.size(); i++) {
                if (isDynamic(routePath.get(i))) {
                    urlParams.put(stripDynamic(routePath.get(i)), calledPath.get(i));
                }

                if (routePath.get(i).equals("{*}")) {
                    while (i < calledPath.size()) {
                        varargs.add(calledPath.get(i));
                        i++;
                    }
                }
            }

            request.mergeParams(urlParams);
            request.mergeVarargs(varargs);

            handle(request, response);
        } catch (Throwable t) {
            response.error(500, t.getMessage(), t);
        }
    }


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


    public abstract void handle(HttpRequest request, HttpResponse response);
}
