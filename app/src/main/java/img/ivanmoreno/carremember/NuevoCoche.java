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

public class NuevoCoche extends Activity {

    ProgressDialog pDialog;
    String url, propietario;
    EditText tMatricula, tNumBastidor, tMarca, tModelo, tAnio, tPropietario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nuevo_coche);

        url = getIntent().getStringExtra("url");
        propietario = getIntent().getStringExtra("login");

        tMatricula = (EditText) findViewById(R.id.etMatriculaED);
        tNumBastidor = (EditText) findViewById(R.id.etBastidorED);
        tMarca = (EditText) findViewById(R.id.etMarcaED);
        tModelo = (EditText) findViewById(R.id.etModeloED);
        tAnio = (EditText) findViewById(R.id.etAnioED);
        tPropietario = (EditText) findViewById(R.id.etPropietarioED);

        tPropietario.setText(propietario);
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

    public void cancelar(View v){
        Intent i = new Intent(NuevoCoche.this, Citas.class);
        i.putExtra("url", url);
        i.putExtra("login", propietario);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        NuevoCoche.this.finish();
    }

    // Hace la inserción del coche nuevo y muestra los coches
    public void AnadirCoche(View v){
        new HacerInsert().execute();
    }

    /*
     * Clase que hace el insert del coche en la tabla de la BBDD
     */
    public class HacerInsert extends AsyncTask<Void, Integer, Boolean> {

        // Función Principal de la clase
        @Override
        protected Boolean doInBackground(Void... params) {
            if (devuelveArray()){
                return true;
            } else {
                return false;
            }
        }

        // Antes de la función principal se iniciara Progress Dialog para agilizar la espera
        @Override
        protected void onPreExecute() {
            pDialog = new ProgressDialog(NuevoCoche.this);
            pDialog.setMessage("Añadiendo Coche...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        // Funcion que se ejecuta cuando acaba la función principal
        @Override
        protected void onPostExecute(Boolean result) {
            pDialog.dismiss();
            if (result){
                Toast.makeText(NuevoCoche.this, "Coche Añadido!", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(NuevoCoche.this, Citas.class);
                i.putExtra("url", url);
                i.putExtra("login", propietario);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                NuevoCoche.this.finish();
            } else {
                Toast.makeText(NuevoCoche.this, "Error añadiendo el coche!", Toast.LENGTH_SHORT).show();
            }
        }

    }

    // Hace una consulta a la BBDD y devuelve true si el array no esta vacio
    private boolean devuelveArray() {
        InputStream is = null;
        StringBuilder sb = new StringBuilder();

        // Conectamos con HTTP usando el metodo POST
        try {
            String urlLogin = url+"InsercionCoche.php?matricula="+tMatricula.getText().toString()+
                    "&numbastidor="+tNumBastidor.getText().toString()+"&marca="+tMarca.getText().toString()+
                    "&modelo="+tModelo.getText().toString()+"&anio="+tAnio.getText().toString()+"&propietario="+propietario;
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
                Log.e("SBNuevo => ", "asdf "+sb.toString());
                is.close();
            } catch (Exception e) {
                Log.e("ERROR => ", "En datos devueltos por el Servicio POST : "
                        + e.toString());
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
