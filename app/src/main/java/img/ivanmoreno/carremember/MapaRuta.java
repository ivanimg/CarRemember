package img.ivanmoreno.carremember;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Clase que muestra un mapa y una ruta
 */
public class MapaRuta extends FragmentActivity implements OnMapReadyCallback{

    String url, login, objetivo, clave = "AIzaSyCW0kpVv3A46ealgc6hSf59nr4yKIbV4S8"; //Valores para el mapa
    SupportMapFragment mapFragment; // Para manejar el mapa
    GoogleMap mapa; // Mapa que se esta viendo
    PolylineOptions mOptions; // Opciones de las lineas que se dibujaran
    LatLng origen, destino, almeria = new LatLng(36.8503776, -2.4656472); // Coordenadas para el mapa
    SharedPreferences prefs; // Preferencias de la aplicación
    SharedPreferences.Editor editor; // Editor de preferencias

    /**
     * Función que se crea con la clase
     * @param savedInstanceState Parametros que recibe de la clase
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mapa_ruta);

        //Se prepara el mapa
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ruta);
        mapFragment.getMapAsync(MapaRuta.this);
        mapa = mapFragment.getMap();

        //Se cogen los valores de las opciones
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Se Coegn los valores del anterior activity
        url = getIntent().getStringExtra("url");
        login = getIntent().getStringExtra("login");
        objetivo = getIntent().getStringExtra("objetivo");

        editor = prefs.edit();
        editor.apply();
        if(objetivo.equals("taller")){
            destino = new LatLng(Double.valueOf(prefs.getString("tallerx", "36.7648333")),Double.valueOf(prefs.getString("tallery", "-2.6145363")));
        } else if(objetivo.equals("sos")){
            destino = new LatLng(36.8616366,-2.4433947);
        }

        // Se centra el mapa en el punto por defecto
        CameraUpdate inicioAlmeria = CameraUpdateFactory.newLatLng(new LatLng(18.013610,-77.498803));
        mapa.moveCamera(inicioAlmeria);


        mOptions = new PolylineOptions();

        new verRuta().execute();
    }

    /**
     * Función que se ejecuta cuando esta listo el mapa moviendo el mapa a un punto
     * @param map Mapa que se esta ejecutando
     */
    @Override
    public void onMapReady(GoogleMap map) {
        // Al cargar el mapa se pone un marcador por la posicion por defecto
        LatLng almeria = new LatLng(36.8503776, -2.4656472);
        map.addMarker(new MarkerOptions().position(almeria).title("Posición inicial si no hay GPS disponible"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(almeria, 15));
    }

    /**
     * Función que crea una ruta hasta el taller de preferencia
     * @param v Vista que llama la función
     */
    public void rutaTaller (View v){
        destino = new LatLng(Double.valueOf(prefs.getString("tallerX", "36.7648333")),Double.valueOf(prefs.getString("tallerY", "-2.6145363")));
        new verRuta().execute();
    }

    /**
     * Función que crea una ruta hasta un hospital
     * @param v Vista que llama la función
     */
    public void rutaHospital (View v){
        destino = new LatLng(36.8616366,-2.4433947);
        new verRuta().execute();
    }

    /**
     * Función que crea una ruta hasta un cuartel
     * @param v Vista que llama la función
     */
    public void rutaCuartel (View v){
        destino = new LatLng(36.8353118, -2.4568093);
        new verRuta().execute();
    }

    /**
     * Clase que crea la ruta
     */
    public class verRuta extends AsyncTask<Void, Integer, Boolean> {

        String result;
        InputStream is;

        protected Boolean doInBackground(Void... params) {
            String urlLogin="";
            //Se hace la petición con JSON a la URL
            try {
                urlLogin = "https://maps.googleapis.com/maps/api/directions/json?origin="+almeria.latitude+","+almeria.longitude+"&destination="+
                        destino.latitude+","+destino.longitude+"&region=es&key=AIzaSyCW0kpVv3A46ealgc6hSf59nr4yKIbV4S8";
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
            try {
                if(result!=null) {
                    mOptions = new PolylineOptions();
                    //Convertimos en JSON el string obtenido
                    final JSONObject json = new JSONObject(result);
                    //Buscamos la ruta que nos da
                    final JSONObject jsonRuta = json.getJSONArray("routes").getJSONObject(0);
                    //Usamos la unica ruta ya que no tienes puntos intermedios
                    final JSONObject leg = jsonRuta.getJSONArray("legs").getJSONObject(0);
                    //Cojemos los distintos pasos que tiene esa ruta
                    final JSONArray pasos = leg.getJSONArray("steps");
                    for (int i = 0; i < pasos.length(); i++) {//Get the individual step
                        final JSONObject paso = pasos.getJSONObject(i);
                        final JSONObject inicio = paso.getJSONObject("start_location");
                        final LatLng posicion = new LatLng(inicio.getDouble("lat"), inicio.getDouble("lng"));
                        mOptions.add(posicion);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mapa.clear();
                            mapa.addPolyline(mOptions);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e("Error haciendo la ruta", e.getMessage());
                return false;
            }
            return true;
        }
    }
}
