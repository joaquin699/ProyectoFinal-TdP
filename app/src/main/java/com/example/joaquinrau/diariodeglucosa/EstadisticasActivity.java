package com.example.joaquinrau.diariodeglucosa;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Hashtable;

public class EstadisticasActivity extends AppCompatActivity {

    private int valorMaximo,valorMinimo;

    private Spinner spinnerDias;
    private Spinner spinnerMomentos;

    private TextView tDias;
    private TextView tPromedio,tEtiquetaPromedio;
    private TextView tEntradas,tEtiquetaEntradas;
    private TextView tEntradasDia,tEtiquetaEntradasDia;

    private TextView tEtiquetaHiper,tHiper;
    private TextView tEtiquetaHipo,tHipo;

    private ListaValoresGlucosa valores;

    private Hashtable<String,Integer> tablaDias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas);

        this.setTitle(R.string.estadisticas_name);

        valorMaximo= getIntent().getExtras().getInt("valor_maximo");
        valorMinimo= getIntent().getExtras().getInt("valor_minimo");

        valores= (ListaValoresGlucosa) getIntent().getExtras().getParcelable("lista_valores");

        spinnerDias= (Spinner) findViewById(R.id.spinnerDias);
        spinnerMomentos= (Spinner) findViewById(R.id.spinnerMoment);

        String [] dias= {"7 dias","14 dias","30 dias","90 dias"};
        ArrayAdapter<String> adapterDias= new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,dias);
        spinnerDias.setAdapter(adapterDias);

        String [] momentos= {"Todos","Ayunas","2 horas despues del desayuno","Almuerzo","2 horas despues del almuerzo",
                "Merienda","2 horas despues de la merienda","Cena","2 horas despues de la cena"};
        ArrayAdapter<String> adapterMomentos = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, momentos);
        spinnerMomentos.setAdapter(adapterMomentos);

        tablaDias =new Hashtable<String,Integer>();
        tablaDias.put("7 dias",7);
        tablaDias.put("14 dias",14);
        tablaDias.put("30 dias",30);
        tablaDias.put("90 dias",90);

        tDias= (TextView) findViewById(R.id.tvDias);
        ocultarEtiqueta(tDias);

        tPromedio= (TextView) findViewById(R.id.tvPromedio);
        ocultarEtiqueta(tPromedio);
        tEtiquetaPromedio= (TextView) findViewById(R.id.tvEtiquetaPromedio);
        ocultarEtiqueta(tEtiquetaPromedio);

        tEntradas= (TextView) findViewById(R.id.tvEntradas);
        ocultarEtiqueta(tEntradas);
        tEtiquetaEntradas= (TextView) findViewById(R.id.tvEtiquetaEntradas);
        ocultarEtiqueta(tEtiquetaEntradas);

        tEntradasDia= (TextView) findViewById(R.id.tvEntradasDia);
        ocultarEtiqueta(tEntradasDia);
        tEtiquetaEntradasDia = (TextView) findViewById(R.id.tvEtiquetaEntradasDia);
        ocultarEtiqueta(tEtiquetaEntradasDia);

        tHiper= (TextView) findViewById(R.id.tvHiper);
        ocultarEtiqueta(tHiper);
        tEtiquetaHiper= (TextView) findViewById(R.id.tvEtiquetaHiper);
        ocultarEtiqueta(tEtiquetaHiper);

        tHipo = (TextView) findViewById(R.id.tvHipo);
        ocultarEtiqueta(tHipo);
        tEtiquetaHipo= (TextView) findViewById(R.id.tvEtiquetaHipo);
        ocultarEtiqueta(tEtiquetaHipo);
    }


    /**
     * Calcula las estadisticas a partir de los valores seleccionados y las muestra
     */
    public void calcularEstadisticas(View view){
        String dias= spinnerDias.getSelectedItem().toString();
        String momento= spinnerMomentos.getSelectedItem().toString();

        ListaValoresGlucosa listaFiltrada;

        int cantDias= tablaDias.get(dias);
        int promedio;
        int cantHipos,cantHiper;

        mostrarEtiqueta(tDias);
        tDias.setText("Mis ultimos "+dias);

        listaFiltrada= obtenerValoresUltimosDias(cantDias);
        if(momento!="Todos") {
            listaFiltrada = obtenerValoresMomentos(listaFiltrada, momento);
        }

        mostrarEtiqueta(tEtiquetaPromedio);
        mostrarEtiqueta(tPromedio);
        VisitorPromedioValores visitorValores= new VisitorPromedioValores();
        operar(listaFiltrada,visitorValores);
        promedio= visitorValores.getTotal();

        tPromedio.setText(promedio+"\nmg/dl");
        if(promedio >=valorMaximo || promedio<=valorMinimo){
            tPromedio.setBackgroundResource(R.mipmap.imagen_fondo_valor_fuera_rango);
        }
        else{
            tPromedio.setBackgroundResource(R.mipmap.imagen_fondo_valor);
        }

        mostrarEtiqueta(tEtiquetaEntradas);
        mostrarEtiqueta(tEntradas);
        tEntradas.setText(""+listaFiltrada.size());

        mostrarEtiqueta(tEtiquetaEntradasDia);
        mostrarEtiqueta(tEntradasDia);
        tEntradasDia.setText(""+Math.round((listaFiltrada.size()/cantDias)));


        Visitor visitorHiper= new VisitorSumaHiperGlucemia(valorMaximo);
        Visitor visitorHipos= new VisitorSumaHipoGlucemia(valorMinimo);
        operar(listaFiltrada,visitorHiper);
        operar(listaFiltrada,visitorHipos);

        cantHipos= visitorHipos.getTotal();
        cantHiper= visitorHiper.getTotal();

        mostrarEtiqueta(tEtiquetaHipo);
        mostrarEtiqueta(tHipo);
        tHipo.setText(""+cantHipos);

        mostrarEtiqueta(tEtiquetaHiper);
        mostrarEtiqueta(tHiper);
        tHiper.setText(""+cantHiper);
    }


    /**
     *  Obtiene todos los valores en los ultimos "dias" de la lista "valores" y los retorna en una lista
     * @return lista con los valores de los ultimos dias
     */
    private ListaValoresGlucosa obtenerValoresUltimosDias(int dias){
        ListaValoresGlucosa lista= new ListaValoresGlucosa();

        Calendar fecha = Calendar.getInstance();

        int day,month,year,hora,minutos;

        fecha.add(Calendar.DATE,(0-dias));
        day= fecha.get(Calendar.DAY_OF_MONTH);
        month= fecha.get(Calendar.MONTH)+1;
        year= fecha.get(Calendar.YEAR);
        hora= fecha.get(Calendar.HOUR);
        minutos= fecha.get(Calendar.MINUTE);

        for(ValorGlucosa valor : valores){
            if(estaEnFecha(valor,day,month,year,hora,minutos)){
                lista.add(valor);
            }
            else{
                break;
            }
        }

        return lista;
    }


    /**
     * Obtiene todos los valores de la lista "lista" en el momento indicado y los retorna en una nueva lista
     * @param lista lista con los valores a para seleccionar
     * @param momento indica el momento de los valores que se quieren seleccionar
     * @return lista con los valores en el momento indicado
     */
    private ListaValoresGlucosa obtenerValoresMomentos(ListaValoresGlucosa lista,String momento){
        ListaValoresGlucosa listaMomentos= new ListaValoresGlucosa();

        for(ValorGlucosa valor : lista){
            if(valor.getMomentoMedicion().equals(momento)){
                listaMomentos.add(valor);
            }
        }
        return listaMomentos;
    }


    /**
     * Retorna verdadero si la fecha del valor pasado es posterior a la fecha pasada como parametro
     * @return
     */
    private boolean estaEnFecha(ValorGlucosa valor,int day,int month,int year,int hora,int minuto){
        boolean esta=false;

        if(valor.getYear()> year) {
            esta= true;
        }
        else{
            if(valor.getYear() == year){
                if(valor.getMonthNumber() > month){
                    esta = true;
                }
                else{
                    if(month == valor.getMonthNumber()){
                        if(valor.getDay() > day){
                            esta = true;
                        }
                        else{
                            if(day == valor.getDay()){
                                if(valor.getHora() > hora){
                                    esta = true;
                                }
                                else{
                                    if(hora== valor.getHora()){
                                        if(valor.getMinuto() > minuto){
                                            esta = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return esta;
    }

    /**
     * Metodo para realizar distintas operaciones sobre la lista pasada como parametro
     * @param lista lista sobre la que hay que realizar operaciones
     * @param visitor operacion que se quiere realizar sobre la lista
     */
    private void operar(ListaValoresGlucosa lista,Visitor visitor){
        for(ValorGlucosa valor : lista){
            valor.aceptar(visitor);
        }
    }

    /**
     * Oculta la visivilidad de la etiqueta pasada como parametro
     * @param etiqueta
     */
    private void ocultarEtiqueta(TextView etiqueta){
        etiqueta.setVisibility(View.INVISIBLE);
    }


    /**
     * Cambia la visivilidad de la etiqueta a visible
     * @param etiqueta
     */
    private void mostrarEtiqueta(TextView etiqueta){
        etiqueta.setVisibility(View.VISIBLE);
    }

}
