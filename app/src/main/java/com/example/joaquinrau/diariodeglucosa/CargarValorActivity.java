package com.example.joaquinrau.diariodeglucosa;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.Calendar;

public class CargarValorActivity extends AppCompatActivity {

    private Spinner spinner_momentos_mediciones;
    private Button botonFecha,botonHora;
    private TextView tValor, tObservacion;

    private int day,month,year;
    private int hora,minutos;
    private int posicion_valor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cargar_valor);

        this.setTitle(R.string.cargar_valor_name);

        posicion_valor=-1;

        //Inicializo Spinner
        spinner_momentos_mediciones= (Spinner) findViewById(R.id.spinnerMomentos);
        String [] momentos= {"Otro","Ayunas","2 horas despues del desayuno","Almuerzo","2 horas despues del almuerzo",
                "Merienda","2 horas despues de la merienda","Cena","2 horas despues de la cena"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item, momentos);
        spinner_momentos_mediciones.setAdapter(adapter);


        tValor= (TextView) findViewById(R.id.tvValor);
        tValor.addTextChangedListener(new TextWatcher() {
            private String beforeChange="";
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeChange= s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int number;
                if(beforeChange.length()==0){
                    number= Integer.parseInt(String.valueOf(s));
                    if(number==0){
                        tValor.setText("");
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //No hago nada
            }
        });

        tObservacion= (TextView) findViewById(R.id.tvObservacion);


        if(getIntent().getExtras() != null){
            ValorGlucosa valorRecibido= (ValorGlucosa) getIntent().getParcelableExtra("valor_para_editar");
            posicion_valor= getIntent().getExtras().getInt("posicion_valor");
            day= valorRecibido.getDay();
            month= valorRecibido.getMonthNumber();
            year= valorRecibido.getYear();
            hora= valorRecibido.getHora();
            minutos= valorRecibido.getMinuto();

            tValor.setText(""+valorRecibido.getValor());
            tObservacion.setText(""+valorRecibido.getObservacion());
        }
        else{
            Calendar fecha = Calendar.getInstance();
            day= fecha.get(Calendar.DAY_OF_MONTH);
            month = fecha.get(Calendar.MONTH) + 1;
            year= fecha.get(Calendar.YEAR);

            hora= fecha.get(Calendar.HOUR_OF_DAY);
            minutos= fecha.get(Calendar.MINUTE);
        }

        botonFecha= (Button) findViewById(R.id.bFecha);
        botonHora= (Button) findViewById(R.id.bHora);

        botonFecha.setText(" "+ day +"/"+ month +"/"+ year+" ");
        String m= ""+minutos;
        String h= ""+hora;
        if(minutos<10){
            m= "0"+minutos;
        }
        if(hora<10){
            h= "0"+hora;
        }

        botonHora.setText(h+":"+m);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuguardar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemGuardar) {
            ValorGlucosa valorNuevo= crearValor();
            if(valorNuevo!=null){
                Intent i= getIntent();
                i.putExtra("nuevo_valor",valorNuevo);
                if(posicion_valor != -1){
                    i.putExtra("posicion_valor_borrar",posicion_valor);
                    setResult(RESULT_OK,i);
                }
                else{
                    setResult(RESULT_FIRST_USER,i);
                }
                finish();
            }
        }
        return true;
    }


    /**
     * Crea un nuevo valor de glucosa a partir de los datos que se ingresaron en los distintos cuadros
     * Si no se ingreso ningun valor, muestra un mensaje con el error, y retorna null
     * @return nuevo valor de glucosa
     */
    private ValorGlucosa crearValor(){
        ValorGlucosa valorGlucosa= null;
        String val;
        String momento= spinner_momentos_mediciones.getSelectedItem().toString();
        String observacion;

        if(tValor.getText().toString().isEmpty()){
            Toast notificacion= Toast.makeText(this,"No se ha ingresado un valor de glucosa",Toast.LENGTH_LONG);
            notificacion.show();
        }
        else{
            val= tValor.getText().toString();
            int valor= Integer.parseInt(val);
            valorGlucosa = new ValorGlucosa(valor,momento,year,month,day,hora,minutos);
            if(!tObservacion.getText().toString().isEmpty()) {
                observacion = tObservacion.getText().toString();
                valorGlucosa.setObservacion(observacion);
            }
        }
        return valorGlucosa;
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder mensajeSalir = new AlertDialog.Builder(this);
        mensajeSalir.setTitle("¿Desea Salir?");
        mensajeSalir.setMessage("Perdera todos los cambios");
        mensajeSalir.setCancelable(false);
        mensajeSalir.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface mensajeSalir, int which) {
                aceptar();
            }
        });
        mensajeSalir.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface mensajeSalir, int which) {
                //Se sigue en la creacion de un valor
            }
        });
        mensajeSalir.show();
    }

    /**
     * Metodo que se utiliza cuando se preciona Ok en el mensaje de alerta
     */
    private void aceptar(){
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * Crea el cuadro para elegir una fecha
     */
    public void seleccionarFecha(View view){
        DatePickerDialog datePickerDialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int yearSelected, int monthOfYear, int dayOfMonth) {
                botonFecha.setText(" "+dayOfMonth +"/"+ (monthOfYear + 1) +"/"+ yearSelected+" ");
                day= dayOfMonth;
                month= monthOfYear + 1;
                year= yearSelected;
            }
        },year,(month-1),day);
        datePickerDialog.show();
    }

    /**
     * Crea el cuadro para seleccionar una hora
     */
    public void seleccionarHora(View view){
        TimePickerDialog timePickerDialog= new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                String m= ""+minute;
                String h= ""+hourOfDay;
                if(minute<10){
                    m= "0"+minute;
                }
                if(hourOfDay<10){
                    h= "0"+hourOfDay;
                }
                botonHora.setText(h+":"+m);
                hora= hourOfDay;
                minutos=minute;
            }
        },hora,minutos,true);
        timePickerDialog.show();
    }
}
