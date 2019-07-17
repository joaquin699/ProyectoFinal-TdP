package com.example.joaquinrau.diariodeglucosa;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class ListaValoresGlucosa extends ArrayList<ValorGlucosa> implements Parcelable {

    public ListaValoresGlucosa(){

    }

    public void writeToParcel(Parcel dest, int flags){
        int size= this.size();
        dest.writeInt(size);
        for(int i=0;i<size;i++){
            ValorGlucosa valor= this.get(i);
            dest.writeInt(valor.getValor());
            dest.writeString(valor.getMomentoMedicion());
            dest.writeInt(valor.getYear());
            dest.writeInt(valor.getMonthNumber());
            dest.writeInt(valor.getDay());
            dest.writeInt(valor.getHora());
            dest.writeInt(valor.getMinuto());
        }
    }

    public ListaValoresGlucosa(Parcel in){
        readfromParcel(in);
    }

    private void readfromParcel(Parcel in){
        int size;
        this.clear();
        size= in.readInt();
        for(int i=0;i<size;i++){
            ValorGlucosa val= new ValorGlucosa(in.readInt(),in.readString(),in.readInt(),in.readInt(),in.readInt(),in.readInt(),in.readInt());
            this.add(val);
        }
    }

    public static final Parcelable.Creator CREATOR= new Parcelable.Creator(){
        public ListaValoresGlucosa createFromParcel(Parcel in){
            return new ListaValoresGlucosa(in);
        }

        public Object[] newArray(int arg0){
            return null;
        }
    };

    public int describeContents(){
        return 0;
    }
}
