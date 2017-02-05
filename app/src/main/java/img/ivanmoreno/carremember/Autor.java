package img.ivanmoreno.carremember;

import android.app.Activity;
import android.os.Bundle;

/**
 * Clase que lanza la activity
 */
public class Autor  extends Activity {

    /**
     * Función que crea la activity al iniciarse la clase
     * @param savedInstanceState Instancia que inicia la activity
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autor);

        //final String openSourceSoftwareLicenseInfo = GoogleApiAvailability.getOpenSourceSoftwareLicenseInfo(this);
    }
}