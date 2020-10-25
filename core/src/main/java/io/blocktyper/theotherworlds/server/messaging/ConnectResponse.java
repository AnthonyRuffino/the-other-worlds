package io.blocktyper.theotherworlds.server.messaging;

import java.util.List;

public class ConnectResponse {
    public boolean success;
    public String username = "";
    public String signatureChallenge;
    public boolean newUser;
    public String message;
    public List<? extends Drawable> captcha;
}
