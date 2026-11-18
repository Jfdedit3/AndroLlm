package com.jfdedit3.androlinux;
import android.app.*;
import android.content.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;
public class MainActivity extends Activity {
 final ExecutorService worker=Executors.newSingleThreadExecutor();
 LinearLayout root;TextView status;int selected=0;
 final String[] names={"Alpine Linux ARM64","Ubuntu Base ARM64"};
 final String[] urls={"https://dl-cdn.alpinelinux.org/alpine/v3.20/releases/aarch64/alpine-minirootfs-3.20.8-aarch64.tar.gz","https://cdimage.ubuntu.com/ubuntu-base/releases/24.04/release/ubuntu-base-24.04.3-base-arm64.tar.gz"};
 TextView text(String s,int n){TextView t=new TextView(this);t.setText(s);t.setTextColor(-1);t.setTextSize(n);t.setPadding(12,12,12,12);return t;}
 Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(v->r.run());return b;}
 File archive(){return new File(getFilesDir(),"downloads/distro-"+selected+".tar.gz");}
 void message(String s){runOnUiThread(()->new AlertDialog.Builder(this).setMessage(s).setPositiveButton("OK",null).show());}
 @Override public void onCreate(Bundle b){super.onCreate(b);render();}
 void render(){
 ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(16,16,16,16);root.setBackgroundColor(0xff11151e);scroll.addView(root);setContentView(scroll);
 root.addView(text("🐧 AndroLinux",28));root.addView(text("Gestionnaire de rootfs ARM64 autonome · sans Termux",15));
 Spinner spinner=new Spinner(this);spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,names));spinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){selected=pos;update();}});root.addView(spinner);
 status=text("",15);root.addView(status);
 root.addView(button("Télécharger le rootfs",()->download()));
 root.addView(button("Lancer Linux",()->message("Le moteur PRoot Android natif n'est pas encore intégré. Cette version prépare le téléchargement des rootfs, mais ne lance pas Linux.")));
 root.addView(button("Supprimer l'archive",()->{archive().delete();update();}));
 root.addView(text("Version de préparation : les archives téléchargées ne sont pas encore extraites. Aucun environnement Linux n'est lancé. L'intégration PRoot natif et d'un terminal reste nécessaire.",13));update();
 }
 void update(){if(status!=null)status.setText("Archive : "+(archive().exists()?archive().length()/1048576+" Mio":"non téléchargée"));}
 void download(){final int index=selected;final File target=archive();worker.execute(()->{try{
 target.getParentFile().mkdirs();HttpURLConnection conn=(HttpURLConnection)new URL(urls[index]).openConnection();conn.setConnectTimeout(20000);conn.setReadTimeout(30000);if(conn.getResponseCode()!=200)throw new IOException("HTTP "+conn.getResponseCode());
 File part=new File(target.getPath()+".part");try(InputStream in=conn.getInputStream();OutputStream out=new FileOutputStream(part)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}
 if(!part.renameTo(target))throw new IOException("Impossible de finaliser l'archive");runOnUiThread(()->{update();message("Archive ARM64 téléchargée.");});
 }catch(Exception e){message("Échec du téléchargement : "+e.getMessage());}});}
 @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}