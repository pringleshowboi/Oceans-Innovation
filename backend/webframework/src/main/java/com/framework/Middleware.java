package com.framework;

import java.util.function.Function;

public interface Middleware extends Function<HttpRequest, HttpRequest> {
}
