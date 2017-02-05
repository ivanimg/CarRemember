package img.ivanmoreno.carremember;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Clase que con da a elegir una fecha y una hora para crear una cita
 */
public class NuevaFecha extends Activity {

    String url, login, coche, reparacion, taller, anio, mes, dia, hora, minuto; // Datos de la cita
    ProgressDialog pDialog; // Progress Dialog para amenizar la carga
    DatePicker calendarioN; // Calendario de donde se saca la fecha
    TimePicker horarioN; // El reloj para escoger la hora
    SharedPreferences prefs; // Preferencias de la aplicación

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Parametros que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fecha);

        // Se usan datos de la activity anterior o de las opciones
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        taller = prefs.getString("Taller","0");
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");
        coche = getIntent().getStringExtra("matricula");
        reparacion = getIntent().getStringExtra("reparacion");

        // Se preparán para coger los datos
        calendarioN = (DatePicker) this.findViewById(R.id.dp_fecha);
        horarioN = (TimePicker) this.findViewById(R.id.tp_hora);
    }

    /**
     * Funcion que lanza el menú
     * @param menu Boton de menú
     * @return True siempre que lanza el menú
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Funcion que controla el menú
     * @param item Objeto que va a la opción escogida
     * @return True siempre que se elige una opcion
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ajustes:
                Intent i = new Intent(this, Preferencias.class);
                startActivity(i);
                break;
            case R.id.atr:
                i = new Intent(this, Autor.class);
                startActivity(i);
                break;
        }
        return true;
    }

    /**
     * Función que lanza el asynctask para cambiar la fecha y la hora
     * @param v Vista que llama a la función
     */
    public void ponFecha(View v) {
        anio = Integer.toString(calendarioN.getYear());
        mes = Integer.toString(calendarioN.getMonth() + 1);
        dia = Integer.toString(calendarioN.getDayOfMonth());
        Log.e("Fecha => ", "la fecha elegida es " + anio + " " + mes + " " + dia);

        hora = Integer.toString(horarioN.getCurrentHour());
        minuto = Integer.toString(horarioN.getCurrentMinute());
        Log.e("Hora => ", "la hora elegida es " + hora + " " + minuto);

        new hacerCita().execute();
    }

    /**
     * Funcion que cancela la edicion de la cita y vuelve a la pantalla de citas
     * @param v Vista que llama a la función
     */
    public void cancelar(View v){
        Intent i = new Intent(NuevaFecha.this, Citas.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        NuevaFecha.this.finish();
    }

    /**
     * Clase que coge todos los datos de la cita y la envia a la BBDD
     */
    public class hacerCita extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True si ha editado los valores en la base de datos
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            if (booleanArray()) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Antes de la función principal se iniciara un Progress Dialog para agilizar la espera
         */
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(NuevaFecha.this);
            pDialog.setMessage("Creando Cita...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Funcion que se ejecuta cuando acaba la función principal
         * @param result lo que devuelve el doInBackground
         */
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result) {
                // Si ha modificado los datos en la base de datos se lanza la activity de citas
                Toast.makeText(NuevaFecha.this, "Cita Creada!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(NuevaFecha.this, Citas.class);
                i.putExtra("url", url);
                i.putExtra("login", login);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                NuevaFecha.this.finish();
            } else {
                Toast.makeText(NuevaFecha.this, "Error creando la cita!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Hace una consulta a la BBDD y devuelve true si el array no esta vacio
         * @return True si se ha modificado la base de datos
         */
        private boolean booleanArray() {
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            // Conectamos con HTTP usando el metodo POST
            try {
                String urlLogin = url + "InsercionCita.php?loginC="+login+"&matriculaC="+coche+"&reparacionC="+reparacion+"&tallerC="+taller
                        +"&fecha="+anio+"-"+mes+"-"+dia+"&hora="+hora+":"+minuto;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Si el InputStream contiene datos, estos se formatean
            if (is != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    sb = new StringBuilder(reader.readLine());
                    is.close();
                } catch (Exception e) {
                    Log.e("ERROR => ", "En datos devueltos por el Servicio POST : " + e.toString());
                    e.printStackTrace();
                }
            }

            // Si no esta vacío es True
            if(sb.toString().equals(String.valueOf(1))){
                return true;
            } else {
                return false;
            }
        }
    }
}