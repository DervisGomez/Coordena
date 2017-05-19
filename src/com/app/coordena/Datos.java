package com.app.coordena;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Datos extends Activity implements OnClickListener{
	private ProgressDialog pd = null;
	private Button salir;

	public Datos() {
		// TODO Auto-generated constructor stub
	}
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.datos);
        salir=(Button)findViewById(R.id.btnSalir);
        salir.setOnClickListener(this);
        Bundle bolsa=getIntent().getExtras();
		String sesion=bolsa.getString("sesion");
		if(sesion.equals("N")){
			showAlertDialog(this, "Login", "successful authentication",false);
//	        ObtenerCoordenadas();
			startService(new Intent(Datos.this, MiServicio.class));
		}
        getActionBar().hide();
    }
	
	public void cerrarSesion(String dato){
		if (this.pd != null) {
            this.pd.dismiss();
        }
		if(dato.length()>3){
			JsonParser parser = new JsonParser();
			Object obje = parser.parse(dato);
	 		JsonObject array=(JsonObject)obje;
	 		String code=array.get("code").getAsString();
	 		String msg=array.get("msg").getAsString();
			if(code.equals("200")){
				BDLogin bda=new BDLogin(this);
				bda.abrir();
				bda.eliminarRespuesta();;
				bda.cerrar();
				showAlertDialog2(this, code, msg,false);
			}else{
				startService(new Intent(Datos.this, MiServicio.class));
				showAlertDialog2(this, "Sign off", "failed action",false);
			}
		}else{
			startService(new Intent(Datos.this, MiServicio.class));
			showAlertDialog2(this, "Sign off","failed action",false);
		}
	}
	
//	@Override
//	public void onBackPressed() {
//	    // Do Here what ever you want do on back press;
//	}
	
	public void showAlertDialog2(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
//		alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);
        alertDialog.setButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            	finish();
            }
        });

        alertDialog.show();
    }
	
	public void showAlertDialog(Context context, String title, String message, Boolean status) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
//		alertDialog.setIcon((status) ? R.drawable.success : R.drawable.fail);
        alertDialog.setButton("Accept", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        alertDialog.show();
    }
	
	private class MiTareaPersonal2 extends AsyncTask<String, Float, String> {
        private String jsonObject;
        private final String HTTP_EVENT;
        private HttpClient httpclient;
        BufferedReader in = null;
	
	 public MiTareaPersonal2(String url,String jsonObject){
         this.HTTP_EVENT=url;
         this.jsonObject=jsonObject;
     }
     protected void onPreExecute() {

     }
     protected String doInBackground(String... urls){
         String resul = "";
         try {
             HttpClient httpClient = new DefaultHttpClient();
             HttpPost post = new HttpPost(HTTP_EVENT);
             StringEntity stringEntity = new StringEntity( jsonObject);
             post.setHeader("Content-type", "application/json");
             post.setEntity(stringEntity);
             HttpResponse response = httpClient.execute(post);
             in = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
             StringBuffer sb = new StringBuffer("");
             String line = "";
             String NL = System.getProperty("line.separator");
             while ((line = in.readLine()) != null) {
                 sb.append(line + NL);
             }
             resul=sb.toString();

         } catch (ClientProtocolException e) {
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return resul;
     }

     protected void onProgressUpdate (Float... valores) {

     }
     protected void onPostExecute(String tiraJson) {
   		cerrarSesion(tiraJson);	
     }
     
     private StringBuilder inputStreamToString(InputStream is) {
         String line = "";
         StringBuilder stringBuilder = new StringBuilder();
         BufferedReader rd = new BufferedReader( new InputStreamReader(is) );
         try{
             while( (line = rd.readLine()) != null ){
                 stringBuilder.append(line);
             }
         }catch( IOException e){
             e.printStackTrace();
         }
         return stringBuilder;
     }
}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		switch (arg0.getId()){
		case R.id.btnSalir:
			AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
	        dialogo1.setTitle("Important!");  
	        dialogo1.setMessage("Do you want to close session?");            
	        dialogo1.setCancelable(false);  
	        dialogo1.setPositiveButton("Accept", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialogo1, int id) {  
	            	detenerServicio();
	            	String[] dato=datos();
	        		JSONObject jsonObject = new JSONObject();
	                try {
	        			jsonObject.put("sessionid",dato[0]);
	        		} catch (JSONException e) {
	        			// TODO Auto-generated catch block
	        			e.printStackTrace();
	        		}
	                procesoSesion(jsonObject.toString());
	                
	            	new MiTareaPersonal2("http://yegsigns.com/api/logoutapi.php",jsonObject.toString()).execute();
	            }  
	        });  
	        dialogo1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {  
	            public void onClick(DialogInterface dialogo1, int id) {  
	            	
	            }  
	        });            
	        dialogo1.show();
		}
	}
	public void procesoSesion(String a){
		this.pd = ProgressDialog.show(this, "Validating data", "Wait a few seconds...", true, false);
	}
	public void detenerServicio(){
		stopService(new Intent(Datos.this,
                MiServicio.class));
	}
	public String[] datos(){
		BDLogin bda=new BDLogin(this);
		bda.abrir();
		String[] datos=bda.consulta();
		bda.cerrar();
		return datos;
	}
}
