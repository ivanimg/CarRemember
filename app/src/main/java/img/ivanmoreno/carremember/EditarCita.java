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
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Clase que muestra los datos de una cita
 */
public class EditarCita extends Activity {

    String url, login, coche, fecha, hora, reparacion; // Para manejar los datos
    EditText etLogin, etMatri, etTrabajo, etFecha, etHora; // Donde se muestran los datos
    ProgressDialog pDialog; // Para amenizar la carga de datos

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Parametros que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_cita);

        //Se cogen todos los datos de la activity anterior
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");
        coche = getIntent().getStringExtra("matricula");
        reparacion = getIntent().getStringExtra("reparacion");
        fecha = getIntent().getStringExtra("fecha");
        hora = getIntent().getStringExtra("hora");

        //Se inicializa donde van a ir los datos
        etLogin = (EditText) findViewById(R.id.etPropietarioCI);
        etMatri = (EditText) findViewById(R.id.etMatriCI);
        etTrabajo = (EditText) findViewById(R.id.etTrabajosCI);
        etFecha = (EditText) findViewById(R.id.etFechaCI);
        etHora = (EditText) findViewById(R.id.etHoraCI);

        //Se asignan los datos
        etLogin.setText(login);
        etMatri.setText(coche);
        etTrabajo.setText(reparacion);
        etFecha.setText(fecha);
        etHora.setText(hora);
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
     * Funcion que lanza la pantalla para cambiar la fecha de la cita
     * @param V Vista que llama la función
     */
    public void editarFecha(View V){
        Intent i = new Intent(EditarCita.this, EditarFecha.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("matricula", coche);
        i.putExtra("reparacion", reparacion);
        i.putExtra("fechaA", fecha);
        i.putExtra("horaA", hora);
        startActivity(i);
    }

    /**
     * Funcion que lanza la clase para borrar la cita
     * @param v Vista que llama la función
     */
    public void borraCita (View v){
        new borrarCita().execute();
    }

    /**
     * Clase que borra la cita seleccionada
     */
    public class borrarCita extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True si se ha borrado la cita, False si no
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
         * Antes de la funcion principal se iniciara Progress Dialog para agilizar la espera
         */
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(EditarCita.this);
            pDialog.setMessage("Borrando Cita...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Funcion que se ejecuta cuando acaba la función principal
         * @param result Valor que devuelve el doInBackground
         */
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result){
                Toast.makeText(EditarCita.this, "Cita Borrada!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditarCita.this, Citas.class);
                i.putExtra("url", url);
                i.putExtra("login", login);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                EditarCita.this.finish();
            } else {
                Toast.makeText(EditarCita.this, "Error añadiendo el coche!", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * Hace una consulta a la BBDD y devuelve true si el array no esta vacio
         * @return True si se ha realizado el delete en la base de datos
         */
        private boolean booleanArray() {
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            // Conectamos con HTTP usando el metodo POST
            try {
                String urlLogin = url+"BorrarCita.php?fecha="+fecha+"&hora="+hora;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Si el InputStream contiene datos, estos se formatean para ver que valor tienen
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