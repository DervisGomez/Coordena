package com.app.coordena;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BDLogin extends SQLiteOpenHelper{
	Context ctx;
	BDLogin bdl;
	SQLiteDatabase bd;

	public BDLogin(Context context) {
		super(context, "midb", null, 1);
		// TODO Auto-generated constructor stub
		ctx=context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE login(id fecha TEXT NOT NULL, usuario TEXT NOT NULL)");
		
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		db.execSQL("DROP TABLE IF EXISTS login");
		onCreate(db);
	}
	public void abrir(){
		bdl=new BDLogin(ctx);
		bd=bdl.getWritableDatabase();
	}
	
	public void cerrar(){
		bd.close();
	}
	public long registrar(String id, String usuario) throws Exception{
		ContentValues valores=new ContentValues();
		valores.put("id", id);
		valores.put("usuario",usuario);
		return bd.insert("login", null, valores);
	}
	public String[] consulta(){
		String[] columns=new String[]{"id","usuario"};
		Cursor c=bd.query(true, "login", columns,null, null,null, null, null, null);
		if(c.moveToFirst()){
			do{
				String id=c.getString(0);
				String usuario=c.getString(1);
				String[] a={id,usuario};
				return a;
				}while(c.moveToNext());
		}
		String[] e={"0","0"};
		return e;
	}
	public void eliminarRespuesta(){
		bd.execSQL("DELETE FROM login");
	}
}
