package com.example.exchangerates;

public class Currency {
    private String id;
    private int numCode;
    private String charCode;
    private int nominal;
    private String name;
    private float value;
    private float previous;

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setNumCode(int numCode) {
        this.numCode = numCode;
    }

    public int getNumCode() {
        return numCode;
    }

    public void setCharCode(String charCode) {
        this.charCode = charCode;
    }

    public String getCharCode() {
        return charCode;
    }

    public void setNominal(int nominal) {
        this.nominal = nominal;
    }

    public int getNominal() {
        return nominal;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setPrevious(float previous) {
        this.previous = previous;
    }

    public float getPrevious() {
        return previous;
    }

    @Override
    public String toString() {
        return charCode;
    }
}
