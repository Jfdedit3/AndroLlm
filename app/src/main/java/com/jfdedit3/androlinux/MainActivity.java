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
 final String[] names={"Alpine Linux ARM64","Ubuntu ARM64","Debian ARM64"};
 final String[] aliases={"alpine","ubuntu","debian"};
 final String[] images={"docker.io/library/alpine:latest","docker.io/library/ubuntu:latest","docker.io/library/debian:stable"};
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
 root.addView(button("1 · Installer la distribution",()->execute("install",images[selected],"--override-alias",aliases[selected])));
 root.addView(button("Réparer les paquets Ubuntu/Debian",()->repairPackages()));
 root.addView(button("2 · Installer KDE Plasma + VNC",()->installDesktop()));
 root.addView(button("3 · Démarrer le bureau graphique",()->startDesktop()));
 root.addView(button("Terminal · Exécuter une commande",()->console()));
 root.addView(button("Supprimer la distribution",()->new AlertDialog.Builder(this).setTitle("Supprimer "+aliases[selected]+" ?").setNegativeButton("Annuler",null).setPositiveButton("Supprimer",(d,w)->execute("remove",aliases[selected])).show()));
 root.addView(text("Le bureau graphique nécessite KDE Plasma (session X11) et TigerVNC dans la distribution. Le serveur VNC est local au téléphone. Le client VNC intégré n'est pas encore disponible.",13));
 update();
 }
 void update(){if(status!=null)status.setText("Distribution : "+aliases[selected]);}

 void repairPackages(){
 if(selected==0){message("Cette réparation dpkg concerne Ubuntu et Debian.");return;}
 execute("login",aliases[selected],"--","/bin/sh","-c",
 "export DEBIAN_FRONTEND=noninteractive; dpkg --configure -a && apt-get -y -f install && dpkg --configure -a");
 }
 void installDesktop(){
 if(selected==0){message("KDE Plasma est actuellement prévu pour Ubuntu et Debian. Choisis Ubuntu ou Debian.");return;}
 execute("login",aliases[selected],"--","/bin/sh","-c",
 "export DEBIAN_FRONTEND=noninteractive; dpkg --configure -a && apt-get -y -f install && apt-get update && apt-get install -y --no-install-recommends plasma-desktop plasma-workspace konsole tigervnc-standalone-server dbus-x11");
 }
 void startDesktop(){
 if(selected==0){message("Sélectionne Ubuntu ou Debian pour démarrer KDE Plasma.");return;}
 execute("login",aliases[selected],"--","/bin/sh","-c","mkdir -p ~/.vnc; printf '#!/bin/sh\\nexport XDG_SESSION_TYPE=x11\\nexport XDG_CURRENT_DESKTOP=KDE\\nexport DESKTOP_SESSION=plasma\\nexec dbus-launch --exit-with-session startplasma-x11\\n' > ~/.vnc/xstartup; chmod +x ~/.vnc/xstartup; vncserver -localhost yes -SecurityTypes None -geometry 1280x720 -depth 24 :1");
 }
  void console(){
 final EditText input=new EditText(this);input.setSingleLine(false);input.setText("uname -a");
 new AlertDialog.Builder(this).setTitle("Commande Linux : "+aliases[selected]).setView(input)
 .setNegativeButton("Annuler",null).setPositiveButton("Exécuter",(d,w)->execute("login",aliases[selected],"--","/bin/sh","-c",input.getText().toString())).show();
 }
 void execute(String... args){
 worker.execute(()->{
 try{
 File prefix=new File(getFilesDir(),"usr");
 File home=new File(getFilesDir(),"home");
 for(String dir:new String[]{"bin","tmp","etc/pr","var/lib/pr/installed-rootfs","var/lib/pr/dlcache"})new File(prefix,dir).mkdirs();
 home.mkdirs();
 File lib=new File(getApplicationInfo().nativeLibraryDir);
 for(String name:new String[]{"proot","pr-cli"}){
 File target=new File(lib,"lib"+name+".so");
 File link=new File(prefix,"bin/"+name);
 if(!target.isFile())throw new IOException("Binaire absent : "+target);
 if(!link.exists())android.system.Os.symlink(target.getAbsolutePath(),link.getAbsolutePath());
 }
 java.util.ArrayList<String> cmd=new java.util.ArrayList<>();
 cmd.add(new File(prefix,"bin/pr-cli").getAbsolutePath());
 java.util.Collections.addAll(cmd,args);
 ProcessBuilder pb=new ProcessBuilder(cmd).redirectErrorStream(true);
 java.util.Map<String,String> env=pb.environment();
 env.put("APP_PREFIX",prefix.getAbsolutePath());
 env.put("APP_HOME",home.getAbsolutePath());
 env.put("APP_PACKAGE",getPackageName());
 env.put("HOME",home.getAbsolutePath());
 env.put("PATH",new File(prefix,"bin").getAbsolutePath()+":/system/bin");
 env.put("PROOT_NO_SECCOMP","1");
 env.put("PROOT_TMP_DIR",getCacheDir().getAbsolutePath());
 env.put("TMPDIR",getCacheDir().getAbsolutePath());
 env.put("TERM","xterm-256color");
 env.put("LANG","en_US.UTF-8");
 pb.directory(home);
 runOnUiThread(()->status.setText("Exécution : "+java.util.Arrays.toString(args)));
 java.lang.Process proc=pb.start();
 StringBuilder output=new StringBuilder();
 try(BufferedReader reader=new BufferedReader(new InputStreamReader(proc.getInputStream()))){
 String line;
 while((line=reader.readLine())!=null){
 if(output.length()<20000)output.append(line).append((char)10);
 final String progress=line;
 runOnUiThread(()->status.setText(progress));
 }
 }
 int exit=proc.waitFor();
 message("Code retour : "+exit+"\\n"+output.toString());
 }catch(Exception ex){message("Erreur Linux : "+ex.toString());}
 });
 }
 void download(){final int index=selected;final File target=archive();worker.execute(()->{try{
 target.getParentFile().mkdirs();HttpURLConnection conn=(HttpURLConnection)new URL(urls[index]).openConnection();conn.setConnectTimeout(20000);conn.setReadTimeout(30000);if(conn.getResponseCode()!=200)throw new IOException("HTTP "+conn.getResponseCode());
 File part=new File(target.getPath()+".part");try(InputStream in=conn.getInputStream();OutputStream out=new FileOutputStream(part)){byte[] buf=new byte[65536];int n;while((n=in.read(buf))!=-1)out.write(buf,0,n);}
 if(!part.renameTo(target))throw new IOException("Impossible de finaliser l'archive");runOnUiThread(()->{update();message("Archive ARM64 téléchargée.");});
 }catch(Exception e){message("Échec du téléchargement : "+e.getMessage());}});}
 @Override protected void onDestroy(){worker.shutdownNow();super.onDestroy();}
}