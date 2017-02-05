package img.ivanmoreno.carremember;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * Clase para editar la fecha de una cita
 */
public class EditarFecha extends Activity {

    String url, login, coche, trabajo, fechaA, horaA, anio, mes, dia, hora, minuto; // Para manejar los valores
    ProgressDialog pDialog;
    DatePicker calendario; // Para guardar la fecha
    TimePicker horario; // Para la hora

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Valores que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fecha);

        // Se cogen los datos del anterior activity
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");
        coche = getIntent().getStringExtra("matricula");
        trabajo = getIntent().getStringExtra("trabajo");
        fechaA = getIntent().getStringExtra("fechaA");
        horaA = getIntent().getStringExtra("horaA");

        //Se muestra el calendario y el reloj
        calendario = (DatePicker) this.findViewById(R.id.dp_fecha);
        horario = (TimePicker) this.findViewById(R.id.tp_hora);
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
    public void ponFecha(View v){
        anio = Integer.toString(calendario.getYear());
        mes = Integer.toString(calendario.getMonth()+1);
        dia = Integer.toString(calendario.getDayOfMonth());

        hora = Integer.toString(horario.getCurrentHour());
        minuto = Integer.toString(horario.getCurrentMinute());

        new editarCita().execute();
    }

    /**
     * Funcion que cancela la edicion de la cita y vuelve a la pantalla de citas
     * @param v Vista que llama a la función
     */
    public void cancelar(View v){
        Intent i = new Intent(EditarFecha.this, Citas.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
        EditarFecha.this.finish();
    }

    /**
     * Clase que editar la fecha y hora de la cita por las seleccionadas
     */
    public class editarCita extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True si ha editado los valores en la base de datos
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            if (booleanArray()){
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
            pDialog = new ProgressDialog(EditarFecha.this);
            pDialog.setMessage("Editando Cita...");
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
            if (result){
                // Si ha modificado los datos en la base de datos se lanza la activity de citas
                Toast.makeText(EditarFecha.this, "Fecha Cambiada!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditarFecha.this, Citas.class);
                i.putExtra("url", url);
                i.putExtra("login", login);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                EditarFecha.this.finish();
            } else {
                Toast.makeText(EditarFecha.this, "Error modificando la cita!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Función que ejecuta la consulta y formatea los datos de vuelta
         * @return True si se ha modificado la base de datos
         */
        private boolean booleanArray() {
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            // Conectamos con HTTP usando el metodo POST
            try {
                String urlLogin = url+"EditarCita.php?fecha="+anio+"-"+mes+"-"+dia+"&hora="+hora+":"+minuto+"&fechaA="+fechaA+"&horaA="+horaA
                        +"&matriculaC="+coche;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Si el InputStream contiene datos, devuelve True
            if (is != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    sb = new StringBuilder(reader.readLine());
                    is.close();
                } catch (Exception e) {
                    Log.e("ERROR => ", "En datos devueltos por el Servicio POST : "+ e.toString());
                    e.printStackTrace();
                }
            }
            if(sb.toString() != String.valueOf(1)){
                return true;
            } else {
                return false;
            }
        }
    }
}