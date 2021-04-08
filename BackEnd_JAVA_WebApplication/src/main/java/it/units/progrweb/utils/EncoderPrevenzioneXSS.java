package it.units.progrweb.utils;

import org.owasp.encoder.Encode;

/**
 * Classe di utilità per l'encoding col fine di difesa dagli attacchi XSS.
 * @see Encode .
 *
 * @author Matteo Ferfoglia
 */
@SuppressWarnings("unused") // alcuni metodi non usati, ma mantenuti per completezza della classe
public class EncoderPrevenzioneXSS {

    /** @see org.owasp.encoder.Encode#forJava(String)   */
    public static String encodeForJava (String input) {
        return Encode.forJava(input.replaceAll("'","^^^^^"))
                .replaceAll("\\^\\^\\^\\^\\^","'");
        // replaceAll serve a "salvare" l'apostrofo dall'encoding sfruttando una particolare
        // sequenza di caratteri di comodo.
    }

    /** @see org.owasp.encoder.Encode#forCssString(String)   */
    public static String encodeForCSS (String input) {
        return Encode.forCssString(input);
    }

    /** @see org.owasp.encoder.Encode#forHtml(String)  */
    public static String encodeForHTML (String input) {
        return Encode.forHtml(input);
    }

    /** @see org.owasp.encoder.Encode#forHtmlAttribute(String)   */
    public static String encodeForHTMLAttribute (String input) {
        return Encode.forHtmlAttribute(input);
    }

    /** @see org.owasp.encoder.Encode#forHtmlContent(String)   */
    public static String encodeForHTMLContent (String input) {
        return Encode.forHtmlContent(input);
    }

    /** @see org.owasp.encoder.Encode#forJavaScript(String)   */
    public static String encodeForJavaScript (String input) {
        return Encode.forJavaScript(input);
    }

    /** @see org.owasp.encoder.Encode#forJavaScriptAttribute(String)   */
    public static String encodeForJavaScriptContainedInHTMLAttributes (String input) {
        return Encode.forJavaScriptAttribute(input);
    }

}
