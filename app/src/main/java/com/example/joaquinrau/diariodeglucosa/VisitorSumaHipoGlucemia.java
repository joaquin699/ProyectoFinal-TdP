package com.example.joaquinrau.diariodeglucosa;

public class VisitorSumaHipoGlucemia extends Visitor {
    private int valorMinimo;

    public VisitorSumaHipoGlucemia(int valorMinimo){
        this.valorMinimo = valorMinimo;
        total=0;
    }

    @Override
    public void visitarValor(ValorGlucosa valor) {
        int v= valor.getValor();
        if(v<= valorMinimo){
            total++;
        }
    }
}