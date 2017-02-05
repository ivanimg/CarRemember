package img.ivanmoreno.carremember;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Clase que administra las opciones de la aplicación
 */
public class Preferencias extends PreferenceActivity {
    /**
     * Función que se crea con la clase
     * @param savedInstanceState
     */
    @SuppressWarnings("deprecation")
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferencias);
    }
}