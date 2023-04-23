package com.cibertec.semana04;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

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
        Locale.setDefault( new Locale("es_ES"));

        txtFechaCreacion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar myCalendar= Calendar.getInstance();
                SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd", new Locale("es"));

                new DatePickerDialog(
                        MainActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int year, int month , int day) {

                                myCalendar.set(Calendar.YEAR, year);
                                myCalendar.set(Calendar.MONTH,month);
                                myCalendar.set(Calendar.DAY_OF_MONTH,day);

                                txtFechaCreacion.setText(dateFormat.format(myCalendar.getTime()));
                            }
                        },
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 //Todos los datos los recibimos como String
                 String razSoc = txtRazonSoc.getText().toString();
                 String dir = txtDireccion.getText().toString();
                 String ruc = txtRuc.getText().toString();
                 String fecCre = txtFechaCreacion.getText().toString();


                 if (!razSoc.matches(ValidacionUtil.TEXTO)){
                     //mensajeToast("La razón social es de 2 a 20 caracteres");
                     txtRazonSoc.setError("La razón social es de 2 a 20 caracteres");
                 }else if (!dir.matches(ValidacionUtil.DIRECCION)){
                     //mensajeToast("La dirección social es de 3 a 30 caracteres");
                     txtDireccion.setError("La dirección social es de 3 a 30 caracteres");
                 }else if (!ruc.matches(ValidacionUtil.RUC)){
                     //mensajeToast("El RUC es 11 dígitos");
                     txtRuc.setError("El RUC es 11 dígitos");
                 }else if (!fecCre.matches(ValidacionUtil.FECHA)){
                     mensajeToast("La fecha de creación es YYYY-MM-dd");
                     txtFechaCreacion.setError("La fecha de creación es YYYY-MM-dd");
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
           Gson gson = new GsonBuilder().setPrettyPrinting().create();
           String json = gson.toJson(objEditorial);
           mensajeAlert(json);


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