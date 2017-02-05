package img.ivanmoreno.carremember;

import android.app.ListActivity;
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
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Clase que muestra los coches para crear una cita
 */
public class NuevaCita extends ListActivity {

    String[] matricula, coches; // Arrays para datos
    String url, login, marca, modelo; //Para datos individuales
    SharedPreferences prefs; // Preferencias de la aplicación
    SharedPreferences.Editor editor; //Para editar las preferencias
    ProgressDialog pDialog; // Para agilizar la carga de datos

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Parametros que recibe de la clase
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nueva_cita);

        // Se preparan las opciones
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        //Se cogen los datos
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");

        new cargarMatriculas().execute();
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
     * Funcion que cancela la creación de la cita y vuelve a la pantalla de citas
     * @param v Vista que llama a la función
     */
    public void cancelarNCita(View v){
        Intent i = new Intent(NuevaCita.this, Citas.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        NuevaCita.this.finish();
    }

    /**
     * Función que lanza otra activity cuando se hace clic
     * @param l Lista donde se ha hecho clic
     * @param v Vista que llama a la función
     * @param position Posición donde se ha pulsado
     * @param id Id del objeto
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(NuevaCita.this, NuevaReparacion.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("matricula", matricula[position]);
        startActivity(i);
    }

    /**
     * Función que carga la lista
     */
    public void cargarCoches(){
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coches));
    }

    /**
     * Función que lanza la clase cargarMatriculas desde un botón
     * @param v Vista que llama la función
     */
    public void recargarCoches(View v){
        new cargarMatriculas().execute();
    }

    /**
     * Clase que busca los coches del usuario y y los expone para elegir uno para la cita
     */
    public class cargarMatriculas extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True siempre que no haya error
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            int i=0;
            JSONArray jArray = devuelveArray();
            matricula = new String[jArray.length()];
            coches = new String[jArray.length()+1];
            for (i=0; i < jArray.length(); i++) {
                try {
                    // Se sacan los datos y se añaden al Array
                    JSONObject jsonObject = jArray.getJSONObject(i);
                    marca = jsonObject.getString("marca");
                    modelo = jsonObject.getString("modelo");
                    matricula[i] = jsonObject.getString("matricula");
                    coches[i] = marca+" "+modelo+" "+matricula[i];
                } catch (JSONException e) {
                    Log.e("ERROR => ", "Error convirtiendo los datos JSON a variables : "+ e.toString());
                    e.printStackTrace();
                    return false;
                }
            }
            coches[i] = "No hay más coches";
            return true;
        }

        /**
         * Antes de la función principal se iniciara un Progress Dialog para agilizar la espera
         */
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(NuevaCita.this);
            pDialog.setMessage("Cargando Coches...");
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
            cargarCoches();
        }

        /**
         * Devuelve un JSONArray con los datos que recibe de la url
         * @return un Array de JSON
         */
        private JSONArray devuelveArray() {
            InputStream is = null;
            String result = "";
            JSONArray jArray = null;

            // Se hace la conexión HTTP
            try {
                String urlLogin = url+"ConsultaCoches.php?login="+login;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Se sacan los datos del InputStream
            if (is != null) {
                try {
                    BufferedReader reader = new BufferedReader(	new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                } catch (Exception e) {
                    Log.e("ERROR => ", "En datos devueltos por el Servicio POST : "+ e.toString());
                    e.printStackTrace();
                }
            }

            // Los datos del POST se formatean a String y se devuelven en un JSON
            try {
                if(!result.toString().equals("")){
                    jArray = new JSONArray(result);
                } else {
                    jArray = new JSONArray();
                }
                return jArray;
            } catch (JSONException e) {
                Log.e("ERROR => ","Error convirtiendo los datos a JSON : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }
}