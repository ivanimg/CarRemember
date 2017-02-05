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
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class EditarCoche extends Activity {

    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    String url, login, coche;
    TextView prop;
    EditText matriET, bastiET, marcaET, modeloET, anioET;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editar_coche);

        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");
        coche = getIntent().getStringExtra("matricula");

        matriET = (EditText) findViewById(R.id.etMatriculaED);
        bastiET = (EditText) findViewById(R.id.etBastidorED);
        marcaET = (EditText) findViewById(R.id.etMarcaED);
        modeloET = (EditText) findViewById(R.id.etModeloED);
        anioET = (EditText) findViewById(R.id.etAnioED);
        prop = (EditText) findViewById(R.id.etPropietarioED);

        new verCoche().execute();
    }


    //Funcion que lanza el menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //Funcion que controla el menu
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

    //Función que cierra la sesión del cliente
    public void cerrarSesion(View v){
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();
        editor.putString("login", "");
        editor.putString("password", "");
        editor.putBoolean("auth", false);
        editor.commit();

        Intent i = new Intent(EditarCoche.this, MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        EditarCoche.this.finish();
    }

    public void editarCoche(View v){
        new editaCoche().execute();
    }


    //Funcion que borra el coche seleccionado
    public void borraCoche(View v){
        Log.e("Boton => ", "boton pulsado "+coche);
        new borrarCoche().execute();
    }

    /**
     * Clase que busca los datos del coche del usuario
     */
    public class verCoche extends AsyncTask<Void, Integer, Boolean> {

        //Clase principal que hace la consulta
        @Override
        protected Boolean doInBackground(Void... params) {
            JSONArray jArray = devuelveArray();
            try {
                JSONObject jsonObject = jArray.getJSONObject(0);
                final String bastiS = jsonObject.getString("numbastidor");
                final String marcaS = jsonObject.getString("marca");
                final String modeloS = jsonObject.getString("modelo");
                final String anioS = jsonObject.getString("anio");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        matriET.setText(coche);
                        bastiET.setText(bastiS);
                        marcaET.setText(marcaS);
                        modeloET.setText(modeloS);
                        anioET.setText(anioS);
                        prop.setText(login);
                    }
                });
            } catch (JSONException e) {
                Log.e("ERROR => ", "Error convirtiendo los datos JSON a variables : "+ e.toString());
                e.printStackTrace();
                return false;
            }
            return true;
        }

        // Antes de la funcion principal se iniciara Progress Dialog para agilizar la espera
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(EditarCoche.this);
            pDialog.setMessage("Cargando Datos del Coche...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Funcion que se ejecuta cuando acaba la funci�n principal
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
        }

        //Función que ejecuta la consulta y formatea los datos de vuelta
        private JSONArray devuelveArray() {
            InputStream is = null;
            String result = "";
            JSONArray jArray = null;

            // Se hace la conexión HTTP
            try {
                String urlLogin = url+"VerCoche.php?coche="+coche;
                URL web = new URL(urlLogin);
                web.openConnection();
                Log.e("URLWEB => ", web.toString());
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
                jArray = new JSONArray(result);
                return jArray;
            } catch (JSONException e) {
                Log.e("ERROR => ","Error convirtiendo los datos a JSON : " + e.toString());
                e.printStackTrace();
                return null;
            }
        }
    }

    /**
     * Clase que edita los datos del coche que se ha seleccionado antes
     */
    public class editaCoche extends AsyncTask<Void, Integer, Boolean> {

        //Clase principal que hace la consulta
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.e("doinBack => ", "en el doinback "+coche);
            if (booleanArray()){
                return true;
            } else {
                return false;
            }
        }

        // Antes de la función principal se iniciara Progress Dialog para agilizar la espera
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(EditarCoche.this);
            pDialog.setMessage("Editando Coche...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Funcion que se ejecuta cuando acaba la función principal
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result){
                Toast.makeText(EditarCoche.this, "Coche Editado!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditarCoche.this, Citas.class);
                i.putExtra("url", url);
                i.putExtra("login", login);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                EditarCoche.this.finish();
            } else {
                Toast.makeText(EditarCoche.this, "Error añadiendo el coche!", Toast.LENGTH_SHORT).show();
            }
        }

        // Hace una consulta a la BBDD y devuelve true si el array no esta vacio
        private boolean booleanArray() {
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            // Conectamos con HTTP usando el metodo POST
            try {
                String urlLogin = url+"EditarCoche.php?coche="+matriET.getText().toString()+"&numbastidor="+bastiET.getText().toString()
                        +"&marca="+marcaET.getText().toString()+"&modelo="+modeloET.getText().toString()+"&anio="+anioET.getText().toString()
                        +"&propietario="+login+"&cocheA="+coche;
                URL web = new URL(urlLogin);
                web.openConnection();
                Log.e("URLWEB => ", web.toString());
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

    /**
     * Clase que borra el coche que se ha seleccionado antes y todas sus citas
     */
    public class borrarCoche extends AsyncTask<Void, Integer, Boolean> {

        //Clase principal que hace la consulta
        @Override
        protected Boolean doInBackground(Void... params) {
            Log.e("doinBack => ", "en el doinback "+coche);
            if (booleanArray("BorrarCoche.php")){
                return true;
            } else {
                return false;
            }
        }

        // Antes de la funci�n principal se iniciara Progress Dialog para agilizar la espera
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(EditarCoche.this);
            pDialog.setMessage("Borrando Coche y Citas...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Funcion que se ejecuta cuando acaba la funci�n principal
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result){
                Toast.makeText(EditarCoche.this, "Coche Borrado!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(EditarCoche.this, Coches.class);
                i.putExtra("url", url);
                i.putExtra("login", login);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                EditarCoche.this.finish();
            } else {
                Toast.makeText(EditarCoche.this, "Error añadiendo el coche!", Toast.LENGTH_SHORT).show();
            }
        }

        // Hace una consulta a la BBDD y devuelve true si el array no esta vacio
        private boolean booleanArray(String archivo) {
            InputStream is = null;
            StringBuilder sb = new StringBuilder();

            // Conectamos con HTTP usando el metodo POST
            try {/*
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url+archivo);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("coche", coche));
                httppost.setEntity(new UrlEncodedFormEntity(params));
                Log.e("Alog => ", "el log es "+url+archivo+ " a"+coche);
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                is = entity.getContent();*/
                String urlLogin = url+"BorrarCoche.php?coche="+coche;
                URL web = new URL(urlLogin);
                web.openConnection();
                Log.e("URLWEB => ", web.toString());
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