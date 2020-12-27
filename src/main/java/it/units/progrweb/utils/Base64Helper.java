package it.units.progrweb.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Base64Helper {

    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getUrlDecoder();

    public static String encodeToBase64UrlEncoded(String stringDaCodificare) {
        return encodeToBase64UrlEncoded(stringDaCodificare.getBytes(StandardCharsets.UTF_8));
    }

    public static String encodeToBase64UrlEncoded(byte[] byteDaCodificare) {
        return base64Encoder.withoutPadding()
                .encodeToString(byteDaCodificare);
    }

    public static String decodeFromBase64UrlEncodedToString(String stringDaDecodificare) {
        return new String(base64Decoder.decode(stringDaDecodificare));
    }
}
