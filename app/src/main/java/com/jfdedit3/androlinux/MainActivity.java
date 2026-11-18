package com.jfdedit3.androlinux;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;

public class MainActivity extends Activity {
  private final int background=0xff10131b;
  private LinearLayout root;
  private String selected="debian";
  private final String[][] distros={{"Debian","debian"},{"Ubuntu","ubuntu"},{"Alpine Linux","alpine"},{"Arch Linux","archlinux"}};
  private TextView text(String s,int size) {
    TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(size);t.setPadding(16,12,16,12);return t;
  }
  private Button button(String title,Runnable action){
    Button b=new Button(this);b.setText(title);b.setAllCaps(false);b.setOnClickListener(v->action.run());return b;
  }
  @Override public void onCreate(Bundle state){super.onCreate(state);render();}
  private void render(){
    ScrollView scroll=new ScrollView(this);
    root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,18,14,18);root.setBackgroundColor(background);
    scroll.addView(root);setContentView(scroll);
    root.addView(text("🐧 AndroLinux",28));
    root.addView(text("Distributions Linux ARM64 via Termux et PRoot-Distro · sans root",15));
    root.addView(text("1. Installe Termux depuis F-Droid ou GitHub.\n2. Dans Termux, exécute : pkg update && pkg install proot-distro\n3. Active allow-external-apps=true dans ~/.termux/termux.properties puis redémarre Termux.\n4. Accorde à AndroLinux l'autorisation « Run commands in Termux ».",14));
    root.addView(button("Ouvrir Termux",()->{
      try{Intent i=getPackageManager().getLaunchIntentForPackage("com.termux");if(i==null)throw new Exception();startActivity(i);}
      catch(Exception e){new AlertDialog.Builder(this).setMessage("Termux n'est pas installé. Installe-le depuis F-Droid : https://f-droid.org/packages/com.termux/").setPositiveButton("OK",null).show();}
    }));
    root.addView(text("Choisir une distribution",21));
    RadioGroup group=new RadioGroup(this);
    for(String[] distro:distros){
      RadioButton radio=new RadioButton(this);radio.setText(distro[0]);radio.setTextColor(Color.WHITE);radio.setPadding(12,10,12,10);
      group.addView(radio);if(distro[1].equals(selected))radio.setChecked(true);
      radio.setOnClickListener(v->selected=distro[1]);
    }
    root.addView(group);
    root.addView(button("📦 Installer "+selected,()->confirm("Installer "+selected+" ?", "proot-distro install "+selected)));
    root.addView(button("▶ Lancer Linux",()->run("proot-distro login "+selected)));
    root.addView(button("⌨ Terminal Linux",()->run("proot-distro login "+selected)));
    root.addView(button("📋 Voir les distributions installées",()->run("proot-distro list")));
    root.addView(button("🗑 Supprimer la distribution",()->confirm("Supprimer "+selected+" et toutes ses données ?", "proot-distro remove "+selected)));
    root.addView(text("Les commandes s'exécutent dans Termux. Une distribution Linux PRoot partage le noyau Android ; ce n'est pas une machine virtuelle complète.",13));
  }
  private void confirm(String message,String command){
    new AlertDialog.Builder(this).setTitle("Confirmation").setMessage(message).setNegativeButton("Annuler",null).setPositiveButton("Continuer",(d,w)->run(command)).show();
  }
  private void run(String command){
    if(android.os.Build.VERSION.SDK_INT>=23 && checkSelfPermission("com.termux.permission.RUN_COMMAND")!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{"com.termux.permission.RUN_COMMAND"},123);
      new AlertDialog.Builder(this).setMessage("Autorise AndroLinux à exécuter des commandes dans Termux, puis réessaie.").setPositiveButton("OK",null).show();
      return;
    }
    try {
      Intent i=new Intent("com.termux.RUN_COMMAND");
      i.setClassName("com.termux","com.termux.app.RunCommandService");
      i.putExtra("com.termux.RUN_COMMAND_PATH","/data/data/com.termux/files/usr/bin/bash");
      i.putExtra("com.termux.RUN_COMMAND_ARGUMENTS",new String[]{"-lc",command});
      i.putExtra("com.termux.RUN_COMMAND_WORKDIR","/data/data/com.termux/files/home");
      i.putExtra("com.termux.RUN_COMMAND_BACKGROUND",false);
      i.putExtra("com.termux.RUN_COMMAND_SESSION_ACTION","0");
      startService(i);
      Toast.makeText(this,"Commande envoyée à Termux : "+command,Toast.LENGTH_LONG).show();
    } catch(Exception e) {
      new AlertDialog.Builder(this).setTitle("Impossible de lancer Termux")
        .setMessage("Vérifie que Termux est installé, que l'autorisation RUN_COMMAND est accordée et que allow-external-apps=true est configuré.\n\n"+e.getMessage())
        .setPositiveButton("OK",null).show();
    }
  }
}