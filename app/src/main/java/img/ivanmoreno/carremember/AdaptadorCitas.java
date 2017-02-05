package img.ivanmoreno.carremember;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Vector;

/**
 * Clase que maneja el adaptador especifico de la lista de citas
 */
public class AdaptadorCitas extends BaseAdapter {

    private final Activity actividad; // La actividad donde esta la cita
    private final Vector<String[]> lista; // La lista de datos que se añaden a la lista


    /**
     * Constructor por defecto de la clase
     * @param actividad Activiry donde que llama a la clase
     * @param lista Datos que se usan para la lista
     */
    public AdaptadorCitas(Activity actividad, Vector<String[]> lista) {
        super();
        this.actividad = actividad;
        this.lista = lista;
    }

    /**
     * Función que pone cada dato en su sitio
     * @param position Posición de la lista
     * @param convertView Objeto donde va el dato
     * @param parent Padre del objeto para poder moverse entre objetos
     * @return Devuelve la vista de cada objeto con todos los datos
     */
    @Override
    @SuppressLint("ViewHolder")
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = View.inflate(actividad, R.layout.elemento_citas, null);
        TextView matricula = (TextView) convertView.findViewById(R.id.tVMat);
        matricula.setText(lista.elementAt(position)[0]);
        TextView reparacion = (TextView) convertView.findViewById(R.id.tvRep);
        reparacion.setText(lista.elementAt(position)[1]);
        TextView fecha = (TextView) convertView.findViewById(R.id.tvFec);
        fecha.setText(lista.elementAt(position)[2]);
        TextView hora = (TextView) convertView.findViewById(R.id.tvHor);
        hora.setText(lista.elementAt(position)[3]);
        return convertView;
    }

    /**
     * Cuenta el numero de elementos de la lista
     * @return Un entero con en numero de elementos
     */
    @Override
    public int getCount() {
        return lista.size();
    }

    /**
     * Devuelve un objeto de la lista
     * @param arg0 Argumento de entrada de datos
     * @return Objeto de la lista
     */
    @Override
    public Object getItem(int arg0) {
        return lista.elementAt(arg0);
    }

    /**
     * Devuelve la id de un objeto
     * @param position Posicion de la lista
     * @return Id del objeto
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
}