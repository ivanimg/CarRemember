package img.ivanmoreno.carremember;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Clase que permite llamar o ver la ruta para una emergencia
 */
public class Sos extends Activity {

    String url, login, numero; // Datos para la activity y la llamada
    SharedPreferences prefs; // Preferencias de la aplicación
    SharedPreferences.Editor editor; // Editor de preferencias

    /**
     * Función que se crea con la clase
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos);

        //Se inicializan los datos Parametros que recibe de la clase
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        editor = prefs.edit();

        // Se recogen los datos
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");

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
     * Función para llamar a emergencias
     * @param v Vista que llama a la función
     */
    public void llamarSos(View v) {
        numero = "112";
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    /**
     * Función para llamar al taller
     * @param v Vista que llama a la función
     */
    public void llamarTaller(View v) {
        numero = prefs.getString("Telefono", "");
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + numero));
        startActivity(intent);
    }

    /**
     * Función que lanza el mapa
     * @param v Vista que llama a la función
     */
    public void verRuta(View v){
        Intent i = new Intent(Sos.this, MapaRuta.class);
        i.putExtra("url", url);
        i.putExtra("login", login);
        i.putExtra("objetivo", "sos");
        startActivity(i);
    }
}