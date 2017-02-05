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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

/**
 * Clase que maneja las citas del usuario
 */
public class Citas extends ListActivity {

    TextView tvCoche, tvReparacion, tvFecha; //Textview donde se muestran datos
    String url, login; // Datos para la conexión HTTP
    SharedPreferences prefs; // Preferencias de la aplicación
    SharedPreferences.Editor editor; // Editor de preferencias
    Vector<String[]> citas; // Lista de citas
    ProgressDialog pDialog; // Para la espera de la carga de datos
    AdaptadorCitas act; //Clase adaptador de la lista
    ListView lis; //Lista donde van los datos

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Parametros que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.citas);

        // Se preparan las opciones
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        // Se crea una funcion para un clic sostenido
        lis = getListView();
        lis.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
                // Se vacía la lista y se vuelve a usar la clase para llenarla
                citas.clear();
                setListAdapter(act);
                new verCitas().execute();
                return true;
            }
        });

        // Se preparan los textview
        tvCoche = (TextView) this.findViewById(R.id.tvCoche);
        tvReparacion = (TextView) this.findViewById(R.id.tvTrabajo);
        tvFecha = (TextView) this.findViewById(R.id.tvFecha);

        // Se cogen los datos de la anterior activity
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");

        // Se inicializa la lista de datos
        citas = new Vector<String[]>();

        new verCitas().execute();
        new verProximaCita().execute();
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
        editor.putString("login", "");
        editor.putString("password", "");
        editor.putBoolean("auth", false);
        editor.putString("TallerX", "");
        editor.putString("TallerY", "");
        editor.putString("Telefono", "");
        editor.apply();

        // Se vuelve a la activity inicial
        Intent i = new Intent(Citas.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        Citas.this.finish();
    }

    /**
     * Función que lanza la pantalla para crear una nueva cita
     * @param v Vista que llama a la función
     */
    public void nuevaCita(View v){
        Intent i = new Intent(Citas.this, NuevaCita.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        startActivity(i);
    }

    /**
     * Funcion que lanza la pantalla para ver los coches
     * @param v Vista que llama a la función
     */
    public void verCoches(View v){
        Intent i = new Intent(Citas.this, Coches.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        startActivity(i);
    }

    /**
     * Funcion que lanza el mapa
     * @param v Vista que llama a la función
     */
    public void verRuta(View v){
        Intent i = new Intent(Citas.this, MapaRuta.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("objetivo", "taller");
        startActivity(i);
    }

    /**
     * Funcion para ver las opciones de emergencia
     * @param v Vista que llama a la función
     */
    public void verSOS(View v){
        Intent i = new Intent(Citas.this, Sos.class);
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
        Intent i = new Intent(Citas.this, EditarCita.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("matricula", citas.get(position)[0]);
        i.putExtra("reparacion", citas.get(position)[1]);
        i.putExtra("fecha", citas.get(position)[2]);
        i.putExtra("hora", citas.get(position)[3]);
        startActivity(i);
    }


    /**
     * Clase que busca las citas de todos los coches del usuario
     */
    public class verCitas extends AsyncTask<Void, Integer, Boolean> {

        /**
         * Función principal de la clase, que se ejecuta en segundo plano
         * @param params Parametros que recibe
         * @return True siempre que no tenga fallos
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            JSONArray jArray = devuelveArray();
            if (jArray != null) {
                try {
                    for (int i = 0; i < jArray.length(); i++) {
                        // Se añaden los datos recibidos
                        citas.add(new String[] {
                                jArray.getJSONObject(i).getString("matriculaC"),
                                jArray.getJSONObject(i).getString("reparacionC"),
                                jArray.getJSONObject(i).getString("fecha"),
                                jArray.getJSONObject(i).getString("hora")});
                    }
                    citas.add(new String[] {"No hay más citas","","",""});
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // Se usa el hilo principal para modificar un objeto de la vista
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        act = new AdaptadorCitas(Citas.this, citas);
                        setListAdapter(act);
                    }
                });
            } else {
                Log.e("ERROR1 => ", "SI JSON ES NULO ");
            }
            return true;
        }

        /**
         * Antes de la función principal se iniciara un Progress Dialog para agilizar la espera
         */
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(Citas.this);
            pDialog.setMessage("Cargando Citas...");
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
        }

        /**
         * Función que ejecuta la consulta y formatea los datos de vuelta
         * @return un Array de JSON
         */
        private JSONArray devuelveArray() {
            InputStream is = null;
            String result = "";
            JSONArray jArray;
            // Se hace la conexión HTTP
            try {
                String urlLogin = url+"ConsultaCitas.php?loginC="+login;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR1 => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Se sacan los datos del InputStream
            if (is != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    if(!sb.toString().equals("")){
                        result = sb.toString();
                    }
                } catch (Exception e) {
                    Log.e("ERROR1 => ", "En datos devueltos por el Servicio POST : "+ e.toString());
                    e.printStackTrace();
                }
            }

            // Los datos del POST se formatean a String y se devuelven en un Array de JSON
            try {
                if(!result.equals("")){//result.toString()
                    jArray = new JSONArray(result);
                } else {
                    jArray = new JSONArray();
                }
                return jArray;
            } catch (JSONException e) {
                Log.e("ERROR1 => ","Error convirtiendo los datos a JSON : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Clase que busca y muestra la proxima cita del coche
     */
    public class verProximaCita extends AsyncTask<Void, Integer, Boolean> {

        /**
         * unción principal que se ejecuta en segundo plano
         * @param params Parametros que recibe la función
         * @return True si no hay ningun fallo
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            JSONArray jArray = devuelveArray();
            for (int i = 0; i < jArray.length(); i++) {
                try {
                    // Se cojen los datos de la primera cita
                    JSONObject jsonObject = jArray.getJSONObject(0);
                    final String sCoche = jsonObject.getString("matriculaC");
                    final String sRepar = jsonObject.getString("reparacionC");
                    final String sFecha = jsonObject.getString("fecha");
                    final String sHora = jsonObject.getString("hora");
                    // Se usa el hilo principal para colocar los datos
                    runOnUiThread(new Runnable() {
                        @SuppressWarnings("deprecation")
                        @Override
                        public void run() {
                            tvCoche.setText(sCoche);
                            tvReparacion.setText(sRepar);
                            SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
                            Date dFecha = null;
                            try {
                                dFecha = formato.parse(sFecha);
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                            tvFecha.setText(dFecha.getDate()+"/"+(dFecha.getMonth()+1)+"/"+(dFecha.getYear()%100)+" "+sHora);
                        }
                    });
                } catch (JSONException e) {
                    Log.e("ERROR2 => ", "Error convirtiendo los datos JSON a variables : "+ e.toString());
                    e.printStackTrace();
                    return false;
                }
            }

            return true;
        }

        /**
         * Función que se ejecuta cuando termina la función principal
         * @param result Valor que devuelve doInBackground
         */
        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                Toast.makeText(Citas.this, "Siguiente Cita Cargada", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Citas.this, "No se pudo cargar la proxima Cita", Toast.LENGTH_SHORT).show();
            }
        }

        //

        /**
         * Devuelve un JSONArray con los datos que recibe de la url
         * @return un Array de JSON
         */
        private JSONArray devuelveArray() {
            InputStream is = null;
            String result = "";
            JSONArray jArray = null;
            //Se hace la conexión y se mandan y reciben los datos
            try {
                String urlLogin = url+"ConsultaProxima.php?login="+login;
                URL web = new URL(urlLogin);
                web.openConnection();
                is = web.openStream();
            } catch (Exception e) {
                Log.e("ERROR2 => ", "Error en conexion http : " + e.toString());
                e.printStackTrace();
            }

            // Se sacan los datos del InputStream
            if (is != null) {
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    if(result==null){
                        result = "";
                    }
                } catch (Exception e) {
                    Log.e("ERROR2 => ", "En datos devueltos por el Servicio POST : "+ e.toString());
                    e.printStackTrace();
                }
            }

            // Los datos del POST se formatean a String y se devuelven en un Array de JSON
            try {
                jArray = new JSONArray(result);
                return jArray;
            } catch (JSONException e) {
                Log.e("ERROR2 => ", "Error convirtiendo los datos a JSON : "+ e.toString());
                e.printStackTrace();
                return new JSONArray();
            }
        }
    }
}