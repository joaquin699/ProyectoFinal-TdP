package com.example.joaquinrau.diariodeglucosa;

public class VisitorSumaHiperGlucemia extends Visitor{
    private int valorMaximo;

    public VisitorSumaHiperGlucemia(int valorMaximo){
        this.valorMaximo=valorMaximo;
        total=0;
    }

    @Override
    public void visitarValor(ValorGlucosa valor) {
        int v=valor.getValor();
        if(v>=valorMaximo){
            total++;
        }
    }
}