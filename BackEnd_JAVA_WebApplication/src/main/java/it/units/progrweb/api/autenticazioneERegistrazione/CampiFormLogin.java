package it.units.progrweb.api.autenticazioneERegistrazione;

import it.units.progrweb.utils.EncoderPrevenzioneXSS;

/**
 * @author Matteo Ferfoglia
 */
public class CampiFormLogin {

    private String username;
    private String password;

    CampiFormLogin() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = EncoderPrevenzioneXSS.encodeForJava(username == null ? "" : username);
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = EncoderPrevenzioneXSS.encodeForJava(password == null ? "" : password);
    }

}
