package org.rutebanken.irkalla.util;

public class Http4URL {

    public static String toHttp4Url(String url) {
        if (url.startsWith("http4") || url.startsWith("https4")) {
            return url;
        }
        return url.replaceFirst("https:", "https4:").replaceFirst("http:", "http4:");
    }
}
