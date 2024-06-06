package org.yuri.model;

public class Line {
    private String cnpj;
    private final String description;
    private final int cfop;
    private final double total;
    private final double base;
    private final double icms;


    public Line(String description, int cfop, double total, double base, double icms) {
        this.description = description;
        this.cfop = cfop;
        this.total = total;
        this.base = base;
        this.icms = icms;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String description() {
        return description;
    }

    public int cfop() {
        return cfop;
    }

    public double total() {
        return total;
    }

    public double base() {
        return base;
    }

    public double icms() {
        return icms;
    }
}
