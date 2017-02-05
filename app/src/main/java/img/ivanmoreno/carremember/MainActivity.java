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
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Clase Principal que permite acceder a la aplicación introduciendo correctamente login y password
 */
public class MainActivity extends /**ActionBar**/Activity {

    EditText login, password; // Las casillas de texto donde se recogen los datos
    ProgressDialog pDialog; // Para Amenizar la espera mientras se comprueban los datos
    String url;//="carremember.x10.mx"; // Pagina web del servidor
    SharedPreferences prefs; // Opciones de la aplicación
    SharedPreferences.Editor editor; //Para modificar las preferencias

    /**
     * Función que se usa al crear la actividad
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (EditText) findViewById(R.id.etLogin);
        password = (EditText) findViewById(R.id.etPass);

        // Se cogen los datos de las opciones para el "login automatico"
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editor.apply();
        url="http://"+prefs.getString("IP", "carremember.x10.mx")+"/carremember/";
        String loginGuardado = prefs.getString("login", "");
        String passGuardado = prefs.getString("password", "");
        Boolean autentificado = prefs.getBoolean("auth", false);
        // Si hay datos y esta puesto como logueado se hace el login sin preguntar al usuario
        if(autentificado && loginGuardado!="" && passGuardado!=""){
            login.setText(loginGuardado);
            password.setText(passGuardado);
            new accesoUsuarios().execute();
        }
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
     * Funcion que lanza la clase para comprobar los datos
     * @param v Vista que ha creado el botón
     */
    public void comprobarDatos(View v){
        editor.putString("Login", login.getText().toString());
        editor.putString("Password", password.getText().toString());
        editor.putBoolean("auth", true);
        editor.apply();
        new accesoUsuarios().execute();
    }

    /**
     * Clase basada en un hilo asincrono que verifica la identidad de los usuarios cuando intentan acceder
     */
    public class accesoUsuarios extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Funcion principal de la clase, que se ejecuta en segundo plano
         * @param arg0 Grupo de argumentos que recibe la función
         * @return true si la consulta en la base de datos devuelve al menos una fila, false si no devuelve ninguna
         */
        @Override
        protected Boolean doInBackground(Void... arg0) {
            JSONArray jArray = getArray();
            if (jArray.length() > 0) {
                try {
                    // Se cogen los datos del usuario
                    JSONObject jsonObject = jArray.getJSONObject(0);
                    String taller = jsonObject.getString("taller");
                    String coorX = jsonObject.getString("tallerx");
                    String coorY = jsonObject.getString("tallery");
                    String telef = jsonObject.getString("telefono_taller");
                    // Se ponen como opciones de la aplicación
                    editor.putString("TallerX", coorX);
                    editor.putString("TallerY", coorY);
                    editor.putString("Telefono", telef);
                    editor.putString("Taller", taller);
                    editor.apply();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
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
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Verificando Usuario...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        /**
         * Despues de la funcion principal se cerrara el Progress Dialog y se iniciara el siguiente activity
         * @param result Lo que devuelve la funcion doInBackGround
         */
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result) {
                // Si los datos introducidos son correctos se lanza la activity para ver las citas
                Toast.makeText(MainActivity.this, "Login Correcto!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(MainActivity.this, Citas.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                i.putExtra("url", url);
                i.putExtra("login",login.getText().toString());
                startActivity(i);
                MainActivity.this.finish();
            } else {
                // Si los datos no son correctos se avisa y se vacían las opciones
                Toast.makeText(MainActivity.this, "Login Incorrecto!",	Toast.LENGTH_SHORT).show();
                editor.putString("Login", "");
                editor.putString("Password", "");
                editor.putBoolean("auth", false);
                editor.putString("TallerX", "");
                editor.putString("TallerY", "");
                editor.putString("Telefono", "");
                editor.apply();
            }
        }
    }

    /**
     * Función que envía los datos al servidor y formatea los datos que recibe
     * @return Devuelve un Arrays de JSONs
     */
    private JSONArray getArray() {
        InputStream is = null;
        String result = "";
        JSONArray jArray;

        // Hacemos una conexioncon el Servidor mediante HTTP usando POST
        try {
            String urlCompleta = url+"ConsultaLogin.php?login="+login.getText().toString()+"&pass="+password.getText().toString();
            URL web = new URL(urlCompleta);
            web.openConnection();
            is = web.openStream();
        } catch (Exception e) {
            Log.e("ERROR => ", "Error en la conexion http : " + e.toString());
            e.printStackTrace();
        }

        // Si recibimos datos del POST les damos formato
        if (is != null) {
            try {
                BufferedReader reader = new BufferedReader(	new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                is.close();
                result = sb.toString();
            } catch (Exception e) {
                Log.e("ERROR => ", "En datos devueltos por el Servicio POST : " + e.toString());
                e.printStackTrace();
            }
        }


        // Estos datos formateados en String se recogen y se añaden a un JSON
        try {
            jArray = new JSONArray(result);
            return jArray;
        } catch (JSONException e) {
            Log.e("ERROR => ", "Error convirtiendo los datos a JSON : "+ e.toString());
            e.printStackTrace();
            return new JSONArray();
        }
    }
}