package com.example.joaquinrau.diariodeglucosa;

public abstract class Visitor {
    protected int total;

    public abstract void visitarValor(ValorGlucosa valor);

    /**
     * Retorna el valor total almacenado
     * @return total almacenado
     */
    public int getTotal(){
        return total;
    }
}