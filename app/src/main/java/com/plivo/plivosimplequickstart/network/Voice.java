package com.plivo.plivosimplequickstart.network;

public class Voice {
    private Boolean incoming_allow;
    private Boolean outgoing_allow;

    public Voice(Boolean incoming_allow, Boolean outgoing_allow) {
        this.incoming_allow = incoming_allow;
        this.outgoing_allow = outgoing_allow;
    }

    public Boolean getIncomingAllow() {
        return incoming_allow;
    }
    public void setIncomingAllow(Boolean incomingAllow) {
        this.incoming_allow = incomingAllow;
    }
    public Boolean getOutgoingAllow() {
        return outgoing_allow;
    }
    public void setOutgoingAllow(Boolean outgoingAllow) {
        this.outgoing_allow = outgoingAllow;
    }
}
