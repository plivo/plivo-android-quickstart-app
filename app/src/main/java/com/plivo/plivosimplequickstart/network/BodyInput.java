package com.plivo.plivosimplequickstart.network;

public class BodyInput {
    private String iss;
    private Per per;
    private String sub;
    private String nbf;
    private String exp;

    public BodyInput(String iss, Per per, String sub, String nbf, String exp) {
        this.iss = iss;
        this.per = per;
        this.sub = sub;
        this.nbf = nbf;
        this.exp = exp;
    }

    public String getIss() {
        return iss;
    }
    public void setIss(String iss) {
        this.iss = iss;
    }
    public Per getPer() {
        return per;
    }
    public void setPer(Per per) {
        this.per = per;
    }
    public String getSub() {
        return sub;
    }
    public void setSub(String sub) {
        this.sub = sub;
    }
    public String getNbf() {
        return nbf;
    }
    public void setNbf(String nbf) {
        this.nbf = nbf;
    }
    public String getExp() {
        return exp;
    }
    public void setExp(String exp) {
        this.exp = exp;
    }
}



