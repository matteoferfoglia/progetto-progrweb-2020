package it.units.progrweb.utils.mail;

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


    /** Proprietà di configurazione del server SMTP.*/
    private Properties properties;
    
    /** Mail session di questa istanza.*/
    private Session session;

    /** Default constructor. Utilizza {@link Session#getDefaultInstance(Properties, Authenticator)},
     * (Properties inizializzate con il default constructor ed Authenticator posto a null). */
    public MailSender() {
        this.properties = new Properties();
        this.session = Session.getDefaultInstance(properties, null);
    }

    /** Costruttore con parametri: permette di configurare il servizio SMTP
     * (<a href="https://stackoverflow.com/a/2957332">Fonte</a>).
     * Nota: in Appengine <strong>non</strong> è possibile usare un server
     * SMTP esterno.
     * @param smtpHost Es.: "smtp.example.com"
     * @param smtpPort Es.: "25".*/
    public MailSender( String smtpHost, String smtpPort ) {

        this();
        properties.put("mail.transport.protocol", "smtp");
        properties.put("mail.smtp.host", smtpHost);
        properties.put("mail.smtp.port", smtpPort);
        this.session = Session.getDefaultInstance(properties, null);

    }

    /** Come {@link #MailSender(String, String)}, permette di specificare
     * le proprietà di autenticazione.
     * Nota: in Appengine <strong>non</strong> è possibile usare un server
     * SMTP esterno.*/
    public MailSender( String smtpHost, String smtpPort,
                       String usernameAuth, String passwordAuth ) {

        this(smtpHost, smtpPort);

        properties.put("mail.smtp.auth", "true");
        Authenticator authenticator = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(usernameAuth, passwordAuth);
            }
        };

        this.session = Session.getDefaultInstance(properties, authenticator);

    }


    /** Metodo per l'invio di email con solo contenuto testuale.
     * @throws MessagingException Problemi con l'invio dell'email,
     *  in particolare {@link AddressException} se l'indirizzo
     *  email sembra invalido.*/
    public void inviaEmail( String emailDestinatario, String nomeDestinatario,
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
    public void inviaEmailMultiPart(@NotNull String emailDestinatario, @NotNull String nomeDestinatario,
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
    private Message setupMessaggioEmail( String emailDestinatario,
                                                String nomeDestinatario,
                                                String oggettoMail,
                                                String corpoMessaggioMail)
            throws MessagingException, UnsupportedEncodingException {

        Message messaggio = new MimeMessage(session);

        messaggio.setFrom(new InternetAddress(INDIRIZZO_MAIL_MITTENTE, NOME_PERSONALE_MAIL_MITTENTE));
        messaggio.addRecipient( Message.RecipientType.TO,  // Message.RecipientType.TO, Message.RecipientType.CC oppure Message.RecipientType.BCC
                                new InternetAddress(emailDestinatario, nomeDestinatario) );
        messaggio.setSubject(oggettoMail);
        messaggio.setText(corpoMessaggioMail);

        return messaggio;

    }

}
