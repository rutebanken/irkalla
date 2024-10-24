package org.rutebanken.irkalla.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BatchNumber {
    public static String getTiamatUrlPageNumber(String tiamatUrl)  {
        final URI uri;
        try {
            uri = new URI(tiamatUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        final String queryPrams = URLDecoder.decode(uri.getQuery(), StandardCharsets.UTF_8);

        return Arrays.stream(queryPrams.split("&"))
                .filter(param -> param.startsWith("page"))
                .map(pram -> pram.split("=")[1])
                .findFirst().orElse("0");
    }

}
