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

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MiServicio extends Service implements LocationListener{
	
	private Context ctx;
	double latitud;
	double longitud;
	Location location;
	boolean gpsActivo;
	TextView texto;
	LocationManager locationManager;
	boolean enviar=true;
	
	public MiServicio() {
		super();
//		this.ctx=this.getApplicationContext();
	}
	public MiServicio(Context c){
		super();
		this.ctx=c;
		getLocation();
	}
	
	
	
	public double getLatitud() {
		return latitud;
	}
	public double getLongitud() {
		return longitud;
	}
	public void getLocation(){
		try{
			locationManager=(LocationManager)this.ctx.getSystemService(LOCATION_SERVICE);
			gpsActivo=locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		}catch(Exception e){}
		if(gpsActivo){
			locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 10, 1,  this);
			location=locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
			latitud=location.getLatitude();
			longitud=location.getLongitude();
		}
//		Toast.makeText(this,"Coordenadas: "+String.valueOf(latitud)+" "+String.valueOf(longitud),Toast.LENGTH_SHORT).show();
		
		JSONObject jsonObject = new JSONObject();
        try {
        	BDLogin bda=new BDLogin(this);
 			bda.abrir();
 			String[] datos=bda.consulta();
 			bda.cerrar();
			jsonObject.put("longitude", longitud );
			jsonObject.put("latitude", latitud );
			jsonObject.put("sessionid", datos[0] );
			jsonObject.put("username", datos[1]);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(enviar){
			new MiTarea(900).execute();
			new MiTareaPersonal("http://yegsigns.com/api/geoapi.php",jsonObject.toString()).execute();
		}
	}
	
	@Override
    public void onCreate() {
		this.ctx=this;
//          Toast.makeText(this,"Servicio creado",Toast.LENGTH_SHORT).show();
          getLocation();
    }

    @Override
    public int onStartCommand(Intent intenc, int flags, int idArranque) {
//          Toast.makeText(this,"Servicio arrancado "+ idArranque,Toast.LENGTH_SHORT).show();
          enviar=true;
//          reproductor.start();
          return START_STICKY;
    }

    @Override
    public void onDestroy() {
//          Toast.makeText(this,"Servicio detenido",Toast.LENGTH_SHORT).show();
          enviar=false;
//          reproductor.stop();
    }

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onLocationChanged(Location arg0) {
		// TODO Auto-generated method stub
//		Toast.makeText(this,"Cambio realizado",Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	public void mensaje(String a){
//		Toast.makeText(this,a,Toast.LENGTH_SHORT).show();
	}
	
	private class MiTarea extends AsyncTask<String, Float, String>{
		 private int x;
		 
		 public MiTarea(int x){
			 this.x=x;
		 }
	  protected void onPreExecute() {

	   }
	   protected String doInBackground(String... urls) {
	  	 String responce="";
	  	try
      {
	  		int i=0;
	  		while(i<x&&enviar){
	  			Thread.sleep(1000);
	  			i++;
	  		}  
      }catch(InterruptedException e){}
	       return responce;
	   }
	   protected void onProgressUpdate (Float... valores) {

	   }
	   protected void onPostExecute(String tiraJson) {
		   if(enviar){
			   getLocation();
		   }
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
        	if(enviar){
        		mensaje(tiraJson);
        	}
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
