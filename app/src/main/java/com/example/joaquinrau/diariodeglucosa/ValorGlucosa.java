package com.example.joaquinrau.diariodeglucosa;

import android.os.Parcel;
import android.os.Parcelable;

public class ValorGlucosa implements Parcelable {
    private int ID;
    private int valor;
    int year;
    int month;
    int day;
    private int hora;
    private int minuto;
    private String momento_medicion;
    private String observacion;


    public ValorGlucosa(int valor,String momento,int year,int month,int day,int hora,int minuto){
        this.valor=valor;
        this.momento_medicion=momento;
        this.year= year;
        this.month= month;
        this.day= day;
        observacion= "";
        this.hora=hora;
        this.minuto=minuto;
        this.ID= 0;
    }


    //Consultas triviales

    public int getValor(){
        return valor;
    }

    public int getDay(){
        return day;
    }

    public int getMonthNumber(){
        return month;
    }

    public String getMonthName(){
        String nombre_mes="";

        switch (month){
            case 1:{
                nombre_mes="Enero";
                break;
            }
            case 2:{
                nombre_mes="Febrero";
                break;
            }
            case 3:{
                nombre_mes="Marzo";
                break;
            }
            case 4:{
                nombre_mes="Abril";
                break;
            }
            case 5:{
                nombre_mes="Mayo";
                break;
            }
            case 6:{
                nombre_mes="Junio";
                break;
            }
            case 7:{
                nombre_mes="Julio";
                break;
            }
            case 8:{
                nombre_mes="Agosto";
                break;
            }
            case 9:{
                nombre_mes="Septiembre";
                break;
            }
            case 10:{
                nombre_mes="Octubre";
                break;
            }
            case 11:{
                nombre_mes="Noviembre";
                break;
            }
            case 12:{
                nombre_mes="Diciembre";
                break;
            }
        }
        return nombre_mes;
    }

    public int getYear(){
        return year;
    }

    public int getHora(){
        return hora;
    }

    public int getMinuto(){
        return minuto;
    }

    public String getMomentoMedicion(){
        return momento_medicion;
    }

    public String getObservacion(){
        return observacion;
    }

    public int getID() {
        return ID;
    }

    //Comandos triviales
    public void setObservacion(String obs){
        observacion= obs;
    }

    public void setID(int id){
        this.ID= id;
    }

    //Otras consultas
    public boolean esMasNuevo(ValorGlucosa val){
        boolean nuevo=false;

        if(year > val.getYear()) {
            nuevo = true;
        }
        else{
            if(year == val.getYear()){
                if(month > val.getMonthNumber()){
                    nuevo=true;
                }
                else{
                    if(month == val.getMonthNumber()){
                        if(day > val.getDay()){
                            nuevo= true;
                        }
                        else{
                            if(day == val.getDay()){
                                if(hora > val.getHora()){
                                    nuevo= true;
                                }
                                else{
                                    if(hora== val.getHora()){
                                        if(minuto > val.getMinuto()){
                                            nuevo=true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return nuevo;
    }

    public void aceptar(Visitor visitor){
        visitor.visitarValor(this);
    }


    //Implementacion Interfaz Parcelable
    protected ValorGlucosa(Parcel in) {
        valor = in.readInt();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        hora = in.readInt();
        minuto = in.readInt();
        momento_medicion = in.readString();
        observacion = in.readString();
        ID= in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(valor);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(hora);
        dest.writeInt(minuto);
        dest.writeString(momento_medicion);
        dest.writeString(observacion);
        dest.writeInt(ID);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<ValorGlucosa> CREATOR = new Parcelable.Creator<ValorGlucosa>() {
        @Override
        public ValorGlucosa createFromParcel(Parcel in) {
            return new ValorGlucosa(in);
        }

        @Override
        public ValorGlucosa[] newArray(int size) {
            return new ValorGlucosa[size];
        }
    };
}