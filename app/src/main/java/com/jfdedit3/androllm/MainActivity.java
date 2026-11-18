package com.jfdedit3.androllm;
import android.app.*;
import android.os.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
 final ExecutorService pool=Executors.newSingleThreadExecutor();
 LinearLayout messages; ScrollView scroll; EditText input; String path=""; boolean loaded=false;
 int bg=0xff10131b,panel=0xff222633;
 TextView label(String value,int size){TextView t=new TextView(this);t.setText(value);t.setTextColor(-1);t.setTextSize(size);t.setPadding(16,16,16,16);return t;}
 LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(1);return l;}
 @Override public void onCreate(Bundle b){super.onCreate(b);path=getPreferences(0).getString("model","");chat();}
 void chat(){
  LinearLayout root=column();root.setBackgroundColor(bg);setContentView(root);
  LinearLayout bar=new LinearLayout(this);TextView title=label("✦ AndroLlm · Local GGUF",21);bar.addView(title,new LinearLayout.LayoutParams(0,-2,1));
  Button models=new Button(this);models.setText("Modèles");models.setOnClickListener(v->models());bar.addView(models);root.addView(bar);
  root.addView(label(loaded?"✓ Modèle chargé":path.isEmpty()?"Aucun modèle GGUF sélectionné":"Modèle sélectionné : "+new File(path).getName(),13));
  scroll=new ScrollView(this);messages=column();scroll.addView(messages);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
  LinearLayout row=new LinearLayout(this);input=new EditText(this);input.setHint("Écris ton message…");input.setHintTextColor(0xff999999);input.setTextColor(-1);
  row.addView(input,new LinearLayout.LayoutParams(0,-2,1));Button send=new Button(this);send.setText("➤");send.setOnClickListener(v->send());row.addView(send);root.addView(row);
  add("IA","Importe un modèle .gguf, charge-le, puis discute hors ligne. Modèles Q4 de 0,5 à 1,5B recommandés.");
 }
 void add(String who,String msg){TextView t=label(who+"\n"+msg,16);t.setBackgroundColor(panel);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(10,6,10,6);messages.addView(t,lp);scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
 void models(){
  LinearLayout root=column();root.setBackgroundColor(bg);root.setPadding(16,16,16,16);setContentView(root);
  root.addView(label("Gestionnaire GGUF",25));root.addView(label("Choisis un fichier GGUF téléchargé. Il sera copié dans le stockage privé de l'application. Prévois plusieurs Go libres.",15));
  root.addView(label(path.isEmpty()?"Aucun fichier":path,13));
  Button pick=new Button(this);pick.setText("Importer un fichier .gguf");pick.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,10);});root.addView(pick);
  Button load=new Button(this);load.setText("Charger le modèle");load.setOnClickListener(v->{if(path.isEmpty()){toast("Importe d'abord un GGUF");return;}load.setEnabled(false);toast("Chargement du modèle en cours…");pool.execute(()->{String error;try{error=LocalEngine.load(path);}catch(Throwable e){error=e.toString();}String result=error;runOnUiThread(()->{loaded=result.isEmpty();toast(loaded?"Modèle chargé !":"Erreur : "+result);load.setEnabled(true);});});});root.addView(load);
  Button back=new Button(this);back.setText("Retour au chat");back.setOnClickListener(v->chat());root.addView(back);
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=10||result!=RESULT_OK||data==null)return;
  Uri uri=data.getData();toast("Importation en cours…");pool.execute(()->{try{
    File folder=new File(getFilesDir(),"models");folder.mkdirs();File target=new File(folder,"selected.gguf");
    try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(target)){byte[] buf=new byte[1024*1024];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}
    path=target.getAbsolutePath();loaded=false;getPreferences(0).edit().putString("model",path).apply();runOnUiThread(()->{toast("GGUF importé");models();});
   }catch(Exception e){runOnUiThread(()->toast("Import impossible : "+e.getMessage()));}});
 }
 void send(){String q=input.getText().toString().trim();if(q.isEmpty())return;if(!loaded){toast("Charge d'abord un modèle dans Modèles");return;}
 input.setText("");add("Vous",q);add("IA","Génération en cours…");pool.execute(()->{String a;try{a=LocalEngine.generate("User: "+q+"\nAssistant:",192);}catch(Throwable e){a="Erreur : "+e;}String response=a;runOnUiThread(()->add("IA",response));});}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 @Override public void onDestroy(){pool.shutdown();super.onDestroy();}
}
