package com.example.joaquinrau.diariodeglucosa;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private ListaValoresGlucosa valores;
    private AdaptadorValores adaptador;

    private int valorMaximo,valorMinimo;

    private static final String TABLE_NAME="valores";

    private static final String DATABASE_NAME="tabla_valores";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setTitle(R.string.valores_name);

        valorMaximo=170;
        valorMinimo=70;

        recuperarValoresBaseDatos();

        adaptador = new AdaptadorValores(this);
        ListView lv1 = (ListView) findViewById(R.id.listaValores);
        lv1.setAdapter(adaptador);


        FloatingActionButton fab = (FloatingActionButton) findViewById (R.id.botonCargaValores);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(MainActivity.this, CargarValorActivity.class );
                startActivityForResult(i,1);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuopciones, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id== R.id.opcionAcercaDe) {
            Intent acercade= new Intent(this,AcercaDeActivity.class);
            startActivity(acercade);
        }
        if (id==R.id.opcionEstadisticas) {
            Intent estadisticas= new Intent(this,EstadisticasActivity.class);
            estadisticas.putExtra("lista_valores",(Parcelable) valores);
            estadisticas.putExtra("valor_maximo",valorMaximo);
            estadisticas.putExtra("valor_minimo",valorMinimo);
            startActivity(estadisticas);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        int posicion= -1;
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode== RESULT_OK || resultCode == RESULT_FIRST_USER){
            ValorGlucosa valorNuevo= (ValorGlucosa) data.getParcelableExtra("nuevo_valor");

            if(resultCode == RESULT_OK){ //Se estuvo editando un valor
                posicion= data.getExtras().getInt("posicion_valor_borrar");
                eliminarValorBaseDatos(valores.get(posicion));
                valores.remove(posicion);
            }
           obtenerIdValorNuevo(valorNuevo);


            guardarValorBaseDatos(valorNuevo);

            insertarValorOrdenado(valorNuevo);
            adaptador.notifyDataSetChanged();

        }
    }


    /**
     * Inserta, de forma ordenada, en la lista valores almacenada como atributo, el valor recibido como parametro
     * @param valor valor a insertar en la lista de valores
     */
    private void insertarValorOrdenado(ValorGlucosa valor){
        int posicion= buscarPosicion(valor);
        valores.add(posicion,valor);
    }


    /**
     * Busca la posicion donde se debe insertar el valor en la lista valores y la retorna como parametro.
     * @param valor valor que se debe insertar
     * @return posicion de insercion del valor
     */
    private int buscarPosicion(ValorGlucosa valor){
        int pos= valores.size();
        for(int i=0;i<valores.size();i++){
            ValorGlucosa valorAux= valores.get(i);
            if(valor.esMasNuevo(valorAux)){
                pos=i;
                break;
            }
        }
        return pos;
    }

    /**
     * Obtiene un id para el valor pasado como parametro y se lo asigna
     * @param valor valor al que se le asignara un id
     */
    private void obtenerIdValorNuevo(ValorGlucosa valor){
        int id;
        int maxId=-1;
        ListaValoresGlucosa listaFiltrada= obtenerValoresDiaHora(valor.getDay(),valor.getMonthNumber(),valor.getYear(),valor.getHora(),valor.getMinuto());
        if(listaFiltrada.size()>0){
            for(ValorGlucosa v : listaFiltrada){
                int idvalor= v.getID();
                if(idvalor>maxId){
                    maxId= idvalor;
                }
            }
        }
        id=++maxId;

        valor.setID(id);
    }

    /**
     * Obtiene una lista con los valores en el mismo dia y hora que indican parametros y la retorna
     * @return lista con los valores en el dia y hora que indican los parametros
     */
    private ListaValoresGlucosa obtenerValoresDiaHora(int day,int month,int year,int hora,int minutos){
        ListaValoresGlucosa lista= new ListaValoresGlucosa();
        boolean mismoDia=false;
        boolean mismoMes=false;
        boolean mismoAnio=false;
        boolean mismaHora= false;
        boolean mismoMinuto=false;

        for(ValorGlucosa valor : valores){
            mismoDia= valor.getDay()== day;
            mismoMes= valor.getMonthNumber()== month;
            mismoAnio= valor.getYear()== year;
            mismaHora= valor.getHora() == hora;
            mismoMinuto= valor.getMinuto() == minutos;
            if(mismoAnio && mismoMes && mismoDia && mismaHora && mismoMinuto){
                lista.add(valor);
            }
        }
        return lista;
    }

    /**
     * Recupera de la base de datos todos valores almacenados e inicializa la lista con estos valores
     */
    private void recuperarValoresBaseDatos(){
        valores= new ListaValoresGlucosa();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,DATABASE_NAME, null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        Cursor total_filas = bd.rawQuery("select codigo,valor,momento,observacion from "+TABLE_NAME, null);


        if(total_filas.moveToFirst()){
            do{
                //Recupero las columnas almacenadas
                String codigo= total_filas.getString(total_filas.getColumnIndex("codigo"));
                String momento= total_filas.getString(total_filas.getColumnIndex("momento"));
                String obs= total_filas.getString(total_filas.getColumnIndex("observacion"));
                int valor= total_filas.getInt(total_filas.getColumnIndex("valor"));

                //Recupero la fecha y hora del valor almacenado codigo= dia-mes-año-hora-minuto-id
                String codigoDividido []= codigo.split("-");

                int day= Integer.parseInt(codigoDividido[0]);
                int month= Integer.parseInt(codigoDividido[1]);
                int year= Integer.parseInt(codigoDividido[2]);
                int hora= Integer.parseInt(codigoDividido[3]);
                int minuto= Integer.parseInt(codigoDividido[4]);
                int id= Integer.parseInt(codigoDividido[5]);

                //Creo el valor
                ValorGlucosa v= new ValorGlucosa(valor,momento,year,month,day,hora,minuto);
                v.setObservacion(obs);
                v.setID(id);

                //Lo inserto en la lista
                insertarValorOrdenado(v);

            }while(total_filas.moveToNext());
        }
        bd.close();

    }

    /**
     * Guarda el valor pasado como parametro en la base de datos
     * @param valor valor que se debe almacenar
     */
    private void guardarValorBaseDatos(ValorGlucosa valor){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,DATABASE_NAME, null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        String fecha= valor.getDay()+"-"+valor.getMonthNumber()+"-"+valor.getYear();
        String hora= valor.getHora()+"-"+valor.getMinuto();
        int id= valor.getID();
        String codigo= fecha+"-"+hora+"-"+id;
        int val= valor.getValor();
        String momento= valor.getMomentoMedicion();
        String observacion= valor.getObservacion();

        ContentValues registro = new ContentValues();
        registro.put("codigo",codigo);
        registro.put("valor",val);
        registro.put("momento",momento);
        registro.put("observacion",observacion);

        bd.insert(TABLE_NAME, null, registro);
        bd.close();
    }

    /**
     * Elimina de la base de datos el valor pasado como parametro
     * @param valor valor que se debe eliminar
     */
    private void eliminarValorBaseDatos(ValorGlucosa valor){
        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,DATABASE_NAME, null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();

        String fecha= valor.getDay()+"-"+valor.getMonthNumber()+"-"+valor.getYear();
        String hora= valor.getHora()+"-"+valor.getMinuto();
        int id= valor.getID();
        String cod= fecha+"-"+hora+"-"+id;

        String[] args = new String[]{cod};
        int cant= bd.delete("valores","codigo=?",args);

        bd.close();
    }


    /**
     * Clase Adaptador para mostrar los valores almacenados en la lista "valores"
     */
    class AdaptadorValores extends ArrayAdapter<ValorGlucosa> {

        AppCompatActivity appCompatActivity;

        AdaptadorValores(AppCompatActivity context) {
            super(context, R.layout.valor_glucosa, valores);
            appCompatActivity = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View item= convertView;
            if(item == null) {
                LayoutInflater inflater = appCompatActivity.getLayoutInflater();
                item = inflater.inflate(R.layout.valor_glucosa, null);
            }

            //Obtengo todas las componentes que se muestran en un elemento de la lista.
            TextView tFecha = (TextView)item.findViewById(R.id.tvFecha);
            TextView tValor = (TextView) item.findViewById(R.id.tvValor);
            TextView tMomento= (TextView) item.findViewById(R.id.tvMomento);
            TextView tHora= (TextView) item.findViewById(R.id.tvHora);
            final TextView mObservacion= (TextView) item.findViewById(R.id.tvMensajeObservacion);
            mObservacion.setVisibility(View.GONE);
            final TextView tObservacion= (TextView) item.findViewById(R.id.tvObservacion);
            tObservacion.setVisibility(View.GONE);

            Button bEliminar= (Button) item.findViewById(R.id.botonEliminar);
            Button bEditar= (Button) item.findViewById(R.id.botonEditar);
            Button bVerMas= (Button) item.findViewById(R.id.botonVerMas);

            //Modifico los que van a ser mostrados, y agrego los Listener a los botones
            tFecha.setText(""+valores.get(position).getDay()+" de "+valores.get(position).getMonthName()+" de "+valores.get(position).getYear());
            if(valores.get(position).getValor()>=valorMaximo || valores.get(position).getValor()<=valorMinimo){
                tValor.setBackgroundResource(R.mipmap.imagen_fondo_valor_fuera_rango);
            }
            else{
                tValor.setBackgroundResource(R.mipmap.imagen_fondo_valor);
            }
            tValor.setText(""+valores.get(position).getValor()+"\nmg/dl");
            tMomento.setText(""+valores.get(position).getMomentoMedicion());

            int minutos= valores.get(position).getMinuto();
            int hora= valores.get(position).getHora();

            String m= ""+minutos;
            String h= ""+hora;
            if(minutos<10){
                m= "0"+minutos;
            }
            if(hora<10){
                h= "0"+hora;
            }
            tHora.setText(h+":"+m);


            //Listener para el boton eliminar
            bEliminar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder mensajeBorrar = new AlertDialog.Builder(MainActivity.this);
                    mensajeBorrar.setMessage("¿Esta seguro que quiere eliminar esta entrada?");
                    mensajeBorrar.setCancelable(false);
                    mensajeBorrar.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface mensajeBorrar, int which) {
                            eliminar(position);
                        }
                    });
                    mensajeBorrar.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface mensajeBorrar, int which) {
                            //Se sigue en la creacion de un valor
                        }
                    });
                    mensajeBorrar.show();
                }
            });

            //Listener para el boton Editar
            bEditar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentEditar= new Intent(MainActivity.this, CargarValorActivity.class );
                    intentEditar.putExtra("valor_para_editar",valores.get(position));
                    intentEditar.putExtra("posicion_valor", position);
                    startActivityForResult(intentEditar,1);
                }
            });


            bVerMas.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(tObservacion.getVisibility() == View.VISIBLE){
                        tObservacion.setVisibility(View.GONE);
                        mObservacion.setVisibility(View.GONE);
                    }else{
                        tObservacion.setText(""+valores.get(position).getObservacion());
                        tObservacion.setVisibility(View.VISIBLE);
                        mObservacion.setVisibility(View.VISIBLE);
                    }

                }
            });

            return(item);
        }

        private void eliminar(int posicion){
            eliminarValorBaseDatos(valores.get(posicion));

            valores.remove(posicion);
            notifyDataSetChanged();
        }

    }


}
