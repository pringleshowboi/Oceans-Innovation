package com.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Router {
    private final Map<String, Function<HttpRequest, HttpResponse>> routes = new HashMap<>();

    public void addRoute(String path, Function<HttpRequest, HttpResponse> handler) {
        routes.put(path, handler);
    }

    public HttpResponse handleRequest(HttpRequest request) {
        Function<HttpRequest, HttpResponse> handler = routes.get(request.getPath());
        if (handler != null) {
            return handler.apply(request);
        }
        HttpResponse response = new HttpResponse();
        response.setStatusCode(404);
        response.setBody("Not Found");
        return response;
    }
}

