package hxc.services.ecds.rest;

import hxc.ecds.protocol.rest.AuthenticationResponse;

public class CannotPopulateSessionException extends Exception {
    AuthenticationResponse response;

    public CannotPopulateSessionException(String message, AuthenticationResponse response) {
        super(message);
        this.response = response;
    }
}
