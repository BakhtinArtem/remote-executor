package com.project.runner.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;

public final class CustomLoader extends URLClassLoader {

    public CustomLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public CustomLoader(URL[] urls) {
        super(urls);
    }

    public CustomLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public CustomLoader(String name, URL[] urls, ClassLoader parent) {
        super(name, urls, parent);
    }

    public CustomLoader(String name, URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(name, urls, parent, factory);
    }
}
