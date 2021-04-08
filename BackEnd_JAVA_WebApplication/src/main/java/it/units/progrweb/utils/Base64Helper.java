package it.units.progrweb.utils;

import it.units.progrweb.EnvironmentVariables;

import java.util.Base64;

/**
 * @author Matteo Ferfoglia
 */
public class Base64Helper {

    private static final Base64.Encoder BASE64_URLENCODER = Base64.getUrlEncoder();
    private static final Base64.Decoder BASE64_URLDECODER = Base64.getUrlDecoder();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    public static String encodeToBase64UrlEncoded( String stringDaCodificare ) {
        if( stringDaCodificare == null )
            return "";
        return encodeToBase64UrlEncoded( stringDaCodificare.getBytes( EnvironmentVariables.STANDARD_CHARSET ) );
    }

    public static String encodeToBase64UrlEncoded( byte[] byteDaCodificare ) {
        if( byteDaCodificare == null )
            return "";
        return BASE64_URLENCODER.withoutPadding().encodeToString( byteDaCodificare );
    }

    @SuppressWarnings("unused") // metodo aggiunto per completezza
    public static String encodeToBase64_rfc2045_encoder( byte[] byteDaCodificare ) {
        if( byteDaCodificare == null )
            return "";
        return BASE64_ENCODER.withoutPadding().encodeToString( byteDaCodificare );
    }

    public static String decodeFromBase64UrlEncodedToString( String stringDaDecodificare ) {
        if( stringDaDecodificare == null )
            return "";
        return new String( BASE64_URLDECODER.decode( stringDaDecodificare ) );
    }

    @SuppressWarnings("unused") // metodo aggiunto per completezza
    public static String decodeFromBase64_rfc2045_decoder( String stringDaDecodificare ) {
        if( stringDaDecodificare == null )
            return "";
        return new String( BASE64_DECODER.decode( stringDaDecodificare ) );
    }
}
