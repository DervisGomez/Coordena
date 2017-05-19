package com.app.coordena;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View.OnClickListener;
import android.view.View;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends Activity implements OnClickListener {
	private EditText etUsuario;
    private EditText etClave;
    private Button btnEntrar;
    private ProgressDialog pd = null;
    private String usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        etUsuario = (EditText) findViewById(R.id.etUsuario);
        etClave = (EditText) findViewById(R.id.etClave);
        btnEntrar = (Button) findViewById(R.id.btnEntrar);
        btnEntrar.setOnClickListener(this);
        getActionBar().hide();
        BDLogin bda=new BDLogin(this);
		bda.abrir();
		String[] datos=bda.consulta();
		bda.cerrar();
		if(!datos[0].equals("0")&&!datos[1].equals("0")){
			Bundle bolsa=new Bundle();
 			bolsa.putString("sesion","A");
			Intent intent=new Intent("com.app.coordena.Datos");
			intent.putExtras(bolsa);
 	        startActivity(intent);
 	        finish();
		}
    }

    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEntrar:
                usuario = etUsuario.getText().toString();
                String clave = etClave.getText().toString();
                if (usuario.length() > 0 && clave.length() > 0) {
                	this.pd = ProgressDialog.show(this, "Validating data", "Wait a few seconds...", true, false);
//                	usuario="test1@test.com";
//                	clave="test12345";
                    etClave.setText("");
                    etUsuario.setText("");
                    etUsuario.requestFocus();
                    JSONObject jsonObject = new JSONObject();
                    try {
						jsonObject.put("username", usuario );
						jsonObject.put("password", clave );
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                    
                    String json="{'username':'"+usuario+"','password':'"+clave+"'}";
                    new MiTareaPersonal("http://yegsigns.com/api/authapi.php",jsonObject.toString()).execute();

                }else{
                    showAlertDialog(this, "Login", "empty fields",false);
                }
                break;
        }
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

    public void iniciarSesion(String dato){
    	JsonParser parser = new JsonParser();
		Object obje = parser.parse(dato);
 		JsonObject array=(JsonObject)obje;
 		String code=array.get("code").getAsString();
 		String msg=array.get("msg").getAsString();
        
 		if(code.equals("200")){
 			BDLogin bda=new BDLogin(this);
 			bda.abrir();
 			try {
				bda.registrar(msg, usuario);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
 			bda.cerrar();
 			Bundle bolsa=new Bundle();
 			bolsa.putString("sesion","N");
 			Intent intent=new Intent("com.app.coordena.Datos");
 			intent.putExtras(bolsa);
 	        startActivity(intent);
 	        finish();
 		}else{
 			usuario="";
 			showAlertDialog(this, code, msg,false);
 		}
        
        if (this.pd != null) {
            this.pd.dismiss();
        }
    }



    private class MiTareaPersonal extends AsyncTask<String, Float, String> {
        private String jsonObject;
        private final String HTTP_EVENT;
        private HttpClient httpclient;
        BufferedReader in = null;

        public MiTareaPersonal(String url,String jsonObject){
            this.HTTP_EVENT=url;
            this.jsonObject=jsonObject;
        }
        protected void onPreExecute() {

        }

        protected String doInBackground(String... urls){
            String resul = "";
            try {

                //Creamos un objeto Cliente HTTP para manejar la peticion al servidor
                HttpClient httpClient = new DefaultHttpClient();
                //Creamos objeto para armar peticion de tipo HTTP POST
                HttpPost post = new HttpPost(HTTP_EVENT);

                //Configuramos los parametos que vaos a enviar con la peticion HTTP POST
                StringEntity stringEntity = new StringEntity( jsonObject);
                post.setHeader("Content-type", "application/json");
                post.setEntity(stringEntity);

                //Se ejecuta el envio de la peticion y se espera la respuesta de la misma.
                HttpResponse response = httpClient.execute(post);
//   			Log.w(APP_TAG, response.getStatusLine().toString());

                //Obtengo el contenido de la respuesta en formato InputStream Buffer y la paso a formato String
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
           iniciarSesion(tiraJson);
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
}
