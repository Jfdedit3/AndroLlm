package com.jfdedit3.androllm;

import android.app.*;
import android.os.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;

public class MainActivity extends Activity {
  final ExecutorService executor = Executors.newSingleThreadExecutor();
  LinearLayout messages; EditText prompt; ScrollView scroll;
  String endpoint, model, key, system;
  JSONArray history = new JSONArray();
  int bg=Color.rgb(17,19,26), panel=Color.rgb(30,33,44), accent=Color.rgb(118,108,255);
  TextView text(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.WHITE); t.setPadding(16,14,16,14); return t; }
  LinearLayout vertical(){ LinearLayout l=new LinearLayout(this); l.setOrientation(1); return l; }
  @Override public void onCreate(Bundle b){super.onCreate(b); load(); showChat();}
  void load(){ android.content.SharedPreferences p=getSharedPreferences("settings",0); endpoint=p.getString("url","http://127.0.0.1:8080/v1/chat/completions"); model=p.getString("model","local-model"); key=p.getString("key",""); system=p.getString("system","You are a helpful assistant."); }
  void showChat(){
    LinearLayout root=vertical(); root.setBackgroundColor(bg); setContentView(root);
    LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
    TextView title=text("✦ AndroLlm",23); top.addView(title,new LinearLayout.LayoutParams(0,-2,1));
    Button settings=new Button(this);settings.setText("⚙"); settings.setOnClickListener(v->showSettings());top.addView(settings);root.addView(top);
    TextView info=text("Modèle : "+model+"  •  API compatible OpenAI",12);root.addView(info);
    scroll=new ScrollView(this);messages=vertical();scroll.addView(messages);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
    LinearLayout bottom=new LinearLayout(this);prompt=new EditText(this);prompt.setTextColor(-1);prompt.setHintTextColor(0xffaaaaaa);prompt.setHint("Écris ton message…");prompt.setMinLines(1);prompt.setMaxLines(5);
    bottom.addView(prompt,new LinearLayout.LayoutParams(0,-2,1));
    Button send=new Button(this);send.setText("➤");send.setOnClickListener(v->send());bottom.addView(send);root.addView(bottom);
    Button clear=new Button(this);clear.setText("Nouvelle conversation");clear.setOnClickListener(v->{history=new JSONArray();messages.removeAllViews();});root.addView(clear);
    addMessage("assistant","Bienvenue ! Configure un serveur LLM compatible OpenAI dans ⚙ pour discuter. L'inférence GGUF sur le téléphone n'est pas encore intégrée.");
  }
  void addMessage(String role,String value){TextView t=text((role.equals("user")?"Vous":"IA")+"\n"+value,16);t.setBackgroundColor(role.equals("user")?0xff303850:panel);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(12,7,12,7);messages.addView(t,lp);scroll.post(()->scroll.fullScroll(View.FOCUS_DOWN));}
  void send(){
    String q=prompt.getText().toString().trim();if(q.isEmpty())return;prompt.setText("");addMessage("user",q);
    try{history.put(new JSONObject().put("role","user").put("content",q));}catch(Exception ignored){}
    executor.execute(()->{
      try{
        JSONArray msgs=new JSONArray();msgs.put(new JSONObject().put("role","system").put("content",system));
        for(int i=0;i<history.length();i++)msgs.put(history.getJSONObject(i));
        JSONObject payload=new JSONObject().put("model",model).put("messages",msgs).put("stream",false);
        HttpURLConnection c=(HttpURLConnection)new URL(endpoint).openConnection();c.setRequestMethod("POST");c.setConnectTimeout(15000);c.setReadTimeout(120000);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");
        if(!key.isEmpty())c.setRequestProperty("Authorization","Bearer "+key);
        try(OutputStream out=c.getOutputStream()){out.write(payload.toString().getBytes(StandardCharsets.UTF_8));}
        InputStream in=(c.getResponseCode()<400)?c.getInputStream():c.getErrorStream();
        ByteArrayOutputStream buf=new ByteArrayOutputStream();byte[] chunk=new byte[8192];int n;while((n=in.read(chunk))!=-1)buf.write(chunk,0,n);
        String raw=buf.toString("UTF-8");if(c.getResponseCode()>=400)throw new IOException("HTTP "+c.getResponseCode()+": "+raw);
        String answer=new JSONObject(raw).getJSONArray("choices").getJSONObject(0).getJSONObject("message").optString("content");
        history.put(new JSONObject().put("role","assistant").put("content",answer));
        runOnUiThread(()->addMessage("assistant",answer));c.disconnect();
      }catch(Exception e){runOnUiThread(()->addMessage("assistant","Erreur : "+e.getMessage()));}
    });
  }
  EditText field(LinearLayout root,String label,String value){root.addView(text(label,14));EditText e=new EditText(this);e.setSingleLine(false);e.setText(value);e.setTextColor(-1);root.addView(e);return e;}
  void showSettings(){
    LinearLayout root=vertical();root.setPadding(20,20,20,20);root.setBackgroundColor(bg);ScrollView sc=new ScrollView(this);sc.addView(root);setContentView(sc);
    root.addView(text("⚙ Configuration",24));
    root.addView(text("Connecte un serveur llama.cpp, LM Studio, Ollama (API compatible OpenAI) ou un fournisseur distant. Pour un serveur sur PC, utilise son IP LAN, pas 127.0.0.1.",14));
    EditText u=field(root,"URL /v1/chat/completions",endpoint),m=field(root,"Nom du modèle",model),k=field(root,"Clé API (optionnelle)",key),s=field(root,"System prompt",system);
    Button save=new Button(this);save.setText("Enregistrer et revenir");root.addView(save);
    save.setOnClickListener(v->{endpoint=u.getText().toString().trim();model=m.getText().toString().trim();key=k.getText().toString().trim();system=s.getText().toString();
      getSharedPreferences("settings",0).edit().putString("url",endpoint).putString("model",model).putString("key",key).putString("system",system).apply();showChat();});
  }
  @Override public void onDestroy(){executor.shutdownNow();super.onDestroy();}
}