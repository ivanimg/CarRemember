package img.ivanmoreno.carremember;

import android.app.ListActivity;
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
import android.widget.AdapterView;
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
 * Clase que muestra los coches del usuario para modificar sus datos
 */
public class Coches extends ListActivity {

    String[] coches, matricula; // Arrays para cuando se necesiten más de un dato
    String url, login, marca, modelo; // String de datos simples
    SharedPreferences prefs; // Preferencias de la aplicación
    SharedPreferences.Editor editor; // Editor de la preferencias
    ListView lis; // Lista donde se muestran los datos

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Valores que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.coches);

        // Se usan los datos recibidos de la anterior activity
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");

        //Se hace un método para que se recargue la lista al manternerla pulsada
        lis = getListView();
        lis.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                // Se vacía la lista y se vuelve a llamar a la clase
                coches[0]="";
                setListAdapter(null);
                new verCoches().execute();
                return true;
            }
        });
        new verCoches().execute();
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
     * Función que cierra la sesión del cliente
     * @param v Vista que llama a la función
     */
    public void cerrarSesion(View v){
        //Se vacían las preferencias de la aplicación
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editor.putString("login", "");
        editor.putString("password", "");
        editor.putBoolean("auth", false);
        editor.putString("TallerX", "");
        editor.putString("TallerY", "");
        editor.putString("Telefono", "");
        editor.commit();

        // Se vuelve a la activity inicial
        Intent i = new Intent(Coches.this, MainActivity.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
    }

    /**
     * Función que abre la pantalla para crear un coche nuevo
     * @param v Vista que llama a la función
     */
    public void anadirCoche(View v){
        Intent i = new Intent(Coches.this, NuevoCoche.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        startActivity(i);
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
        Intent i = new Intent(Coches.this, EditarCoche.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("matricula",matricula[position]);
        startActivity(i);
    }

    /**
     * Función que llena el listview
     */
    public void cargarCoches() {
        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, coches));
    }

    /**
     * Clase que busca los coches del usuario y si encuentra al menos uno se muestra en la lista
     */
    public class verCoches extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True siempre que no tenga fallos
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            int i=0;
            JSONArray jArray = devuelveArray();
            matricula = new String[jArray.length()];
            coches = new String[jArray.length()+1];
            for (i=0; i < jArray.length(); i++) {
                try {
                    //Se cogen todos los datos y se juntan para mostrarlos
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

        //Si cuando se termina la funcion principal contiene algo se llena la lista
        @Override
        protected void onPostExecute(Boolean result) {
            if (result = true)
                cargarCoches();
        }
    }

    //Función que ejecuta la consulta y formatea los datos de vuelta
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

        // Los datos del POST se formatean a String y se devuelven en un Array de JSON
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
