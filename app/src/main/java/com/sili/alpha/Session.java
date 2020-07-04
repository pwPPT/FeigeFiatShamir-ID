package com.sili.alpha;

public class Session {

    private String sessionId;
    private boolean isSet;

    public Session() {
        sessionId = null;
        isSet= false;
    }

    public void setSessionId(String value) {
        this.isSet = true;
        this.sessionId = value;
    }

    public String getSessionId() {
        if(isSet) {
            return sessionId;
        } else {
            return null;
        }
    }

    public boolean checkIfSet() {
        return isSet;
    }
}
