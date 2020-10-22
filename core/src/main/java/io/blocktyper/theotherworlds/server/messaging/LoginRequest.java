package io.blocktyper.theotherworlds.server.messaging;

public class LoginRequest {
    public String username = "";
    public String signedChallenge;
    public String captcha;
    public byte[] publicKey;
}
