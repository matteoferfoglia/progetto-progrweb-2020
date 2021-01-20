package it.units.progrweb.mail;

import javax.mail.*;
import javax.mail.internet.*;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * Classe tratta da:
 * <a href="https://cloud.google.com/appengine/docs/standard/java/mail/sending-mail-with-mail-api#sending_email_messages">Fonte</a>.
 * @author Matteo Ferfoglia
 */
public class MailSender {

    /** Indirizzo email in formato RFC822.*/
    private static final String INDIRIZZO_MAIL_MITTENTE      = "admin@example.com";       // TODO: variabile d'ambiente

    /** Nome personale utilizzato nelle mail.*/
    private static final String NOME_PERSONALE_MAIL_MITTENTE = "Servizio di Filesharing"; // TODO: variabile d'ambiente


    /** Metodo per l'invio di email con solo contenuto testuale.
     * @throws MessagingException Problemi con l'invio dell'email,
     *  in particolare {@link AddressException} se l'indirizzo
     *  email sembra invalido.*/
    public static void inviaEmail( String emailDestinatario, String nomeDestinatario,
                            String oggettoMail, String corpoMessaggioMail )
            throws MessagingException, UnsupportedEncodingException {

        Message msg = setupMessaggioEmail( emailDestinatario,
                                           nomeDestinatario,
                                           oggettoMail,
                                           corpoMessaggioMail );

        // Invia e-mail
        Transport.send(msg);
    }

    /** Metodo per l'invio di email con messaggio Multipart.
     * Permette di aggiungere il corpo del messaggio HTML.
     * Permette l'invio di allegati. I campi non usati possono
     * essere impostati a null.
     * @param nomeDocumentoAllegato Es.: "DocumentoProva.pdf"
     * @param contentTypeDocumentoAllegato Es.: "application/pdf"
     * @throws MessagingException Problemi con l'invio dell'email,
     *  in particolare {@link AddressException} se l'indirizzo
     *  email sembra invalido.*/
    public static void inviaMailMultiPart(@NotNull String emailDestinatario, @NotNull String nomeDestinatario,
                                   @NotNull String oggettoMail, @NotNull String corpoMessaggioMail,
                                   @NotNull String corpoMessaggioMailHtml,
                                   byte[] documentoAllegato, String nomeDocumentoAllegato, String contentTypeDocumentoAllegato )
            throws UnsupportedEncodingException, MessagingException {

        Message msg = setupMessaggioEmail( emailDestinatario,
                                           nomeDestinatario,
                                           oggettoMail,
                                           corpoMessaggioMail );

        Multipart mp = new MimeMultipart();

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(corpoMessaggioMailHtml, "text/html");
        mp.addBodyPart(htmlPart);

        if( documentoAllegato != null ) {
            MimeBodyPart allegato = new MimeBodyPart();
            InputStream attachmentDataStream = new ByteArrayInputStream(documentoAllegato);
            allegato.setFileName(nomeDocumentoAllegato);
            allegato.setContent(attachmentDataStream, contentTypeDocumentoAllegato);
            mp.addBodyPart(allegato);
        }

        msg.setContent(mp);


        // Invio del messaggio
        Transport.send(msg);

    }



    /** Metodo di setup per l'invio di un messaggio email.*/
    private static Message setupMessaggioEmail( String emailDestinatario,
                                                String nomeDestinatario,
                                                String oggettoMail,
                                                String corpoMessaggioMail)
            throws MessagingException, UnsupportedEncodingException {

        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        Message messaggio = new MimeMessage(session);

        messaggio.setFrom(new InternetAddress(INDIRIZZO_MAIL_MITTENTE, NOME_PERSONALE_MAIL_MITTENTE));
        messaggio.addRecipient( Message.RecipientType.TO,  // Message.RecipientType.TO, Message.RecipientType.CC oppure Message.RecipientType.BCC
                                new InternetAddress(emailDestinatario, nomeDestinatario) );
        messaggio.setSubject(oggettoMail);
        messaggio.setText(corpoMessaggioMail);

        return messaggio;

    }

}
