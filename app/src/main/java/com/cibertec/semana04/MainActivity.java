package com.cibertec.semana04;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.cibertec.semana04.entity.Editorial;
import com.cibertec.semana04.entity.Pais;
import com.cibertec.semana04.service.ServiceEditorial;
import com.cibertec.semana04.service.ServicePais;
import com.cibertec.semana04.util.ConnectionRest;
import com.cibertec.semana04.util.FunctionUtil;
import com.cibertec.semana04.util.ValidacionUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    Spinner spnPais;
    ArrayAdapter<String> adaptador;
    ArrayList<String> paises = new ArrayList<String>();

    //Servicio
    ServiceEditorial serviceEditorial;
    ServicePais servicePais;

    //Componentes del formulario
    Button btnRegistrar;
    EditText txtRazonSoc, txtDireccion, txtRuc, txtFechaCreacion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        servicePais = ConnectionRest.getConnecion().create(ServicePais.class);
        serviceEditorial = ConnectionRest.getConnecion().create(ServiceEditorial.class);

        //Para el adapatador
        adaptador = new ArrayAdapter<String>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, paises);
        spnPais = findViewById(R.id.spnRegEdiPais);
        spnPais.setAdapter(adaptador);

        cargaPais();

        txtRazonSoc = findViewById(R.id.txtRegEdiRazonSocial);
        txtDireccion = findViewById(R.id.txtRegEdiDirecccion);
        txtRuc = findViewById(R.id.txtRegEdiRuc);
        txtFechaCreacion = findViewById(R.id.txtRegEdiFechaCreacion);
        btnRegistrar = findViewById(R.id.btnRegEdiEnviar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 //Todos los datos los recibimos como String
                 String razSoc = txtRazonSoc.getText().toString();
                 String dir = txtDireccion.getText().toString();
                 String ruc = txtRuc.getText().toString();
                 String fecCre = txtFechaCreacion.getText().toString();


                 if (!razSoc.matches(ValidacionUtil.TEXTO)){
                     mensajeToast("La razón social es de 2 a 20 caracteres");
                 }else if (!dir.matches(ValidacionUtil.DIRECCION)){
                     mensajeToast("La dirección social es de 3 a 30 caracteres");
                 }else if (!ruc.matches(ValidacionUtil.RUC)){
                     mensajeToast("El RUC es 11 dígitos");
                 }else if (!fecCre.matches(ValidacionUtil.FECHA)){
                     mensajeToast("La fecha de creación es YYYY-MM-dd");
                 }else{
                     String pais = spnPais.getSelectedItem().toString();
                     String idPais = pais.split(":")[0];
                     Pais objPais = new Pais();
                     objPais.setIdPais(Integer.parseInt(idPais));

                     Editorial objEditorial = new Editorial();
                     objEditorial.setRazonSocial(razSoc);
                     objEditorial.setDireccion(dir);
                     objEditorial.setRuc(ruc);
                     objEditorial.setFechaCreacion(fecCre);
                     objEditorial.setFechaRegistro(FunctionUtil.getFechaActualStringDateTime());
                     objEditorial.setEstado(1);
                     objEditorial.setPais(objPais);

                     insertaEditorial(objEditorial);
                 }


            }
        });

    }

    public  void insertaEditorial(Editorial objEditorial){

           Call<Editorial> call = serviceEditorial.insertaEditorial(objEditorial);
           call.enqueue(new Callback<Editorial>() {
               @Override
               public void onResponse(Call<Editorial> call, Response<Editorial> response) {
                      if (response.isSuccessful()){
                            Editorial objSalida = response.body();
                            mensajeAlert(" Registro exitoso  >>> ID >> " + objSalida.getIdEditorial()
                                  + " >>> Razón Social >>> " +  objSalida.getRazonSocial());
                      }else{
                          mensajeAlert(response.toString());
                      }
               }
               @Override
               public void onFailure(Call<Editorial> call, Throwable t) {
                   mensajeToast("Error al acceder al Servicio Rest >>> " + t.getMessage());
               }
           });
    }

    public void cargaPais(){
        Call<List<Pais>> call = servicePais.listaPais();
        call.enqueue(new Callback<List<Pais>>() {
            @Override
            public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                if (response.isSuccessful()){
                    List<Pais> lstPaises =  response.body();
                    for(Pais obj: lstPaises){
                        paises.add(obj.getIdPais() +":"+ obj.getNombre());
                    }
                    adaptador.notifyDataSetChanged();
                }else{
                    mensajeToast("Error al acceder al Servicio Rest >>> ");
                }
            }

            @Override
            public void onFailure(Call<List<Pais>> call, Throwable t) {
                mensajeToast("Error al acceder al Servicio Rest >>> " + t.getMessage());
            }
        });
    }

    public void mensajeToast(String mensaje){
        Toast toast1 =  Toast.makeText(getApplicationContext(),mensaje, Toast.LENGTH_LONG);
        toast1.show();
    }

    public void mensajeAlert(String msg){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }

}