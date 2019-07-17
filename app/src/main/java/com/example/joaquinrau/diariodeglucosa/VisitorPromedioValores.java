package com.example.joaquinrau.diariodeglucosa;

public class VisitorPromedioValores extends Visitor {

    private int cantValores;

    public VisitorPromedioValores(){
        total=0;
        cantValores= 0;
    }

    @Override
    public void visitarValor(ValorGlucosa valor) {
        total+= valor.getValor();
        cantValores++;
    }

    @Override
    public int getTotal() {
        int promedio=0;

        if(cantValores>0){
            promedio= Math.round(total/cantValores);
        }
        return promedio;
    }
}
