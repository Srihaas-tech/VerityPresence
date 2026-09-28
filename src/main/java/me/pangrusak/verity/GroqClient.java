package me.pangrusak.verity;
import org.bukkit.*; import org.bukkit.entity.Player; import java.net.URI; import java.net.http.*; import java.time.Duration; import java.util.*; import java.util.concurrent.*;
public final class GroqClient {
 private final VerityPresencePlugin plugin; private final HttpClient http=HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build(); private final Deque<Long> requests=new ArrayDeque<>();
 public GroqClient(VerityPresencePlugin plugin){this.plugin=plugin;}
 public void ask(Player player,PlayerProfile profile,String message){
  if(!plugin.getConfig().getBoolean("ai.enabled",true)){plugin.reply(player,plugin.fallback(profile));return;}
  String key=plugin.getConfig().getString("ai.api-key","").trim(); if(key.isEmpty()||key.equals("PASTE_YOUR_GROQ_KEY_HERE")){plugin.reply(player,"The sphere is silent. Its voice has not been connected.");return;}
  long now=System.currentTimeMillis(), cd=plugin.getConfig().getLong("ai.cooldown-seconds",30)*1000L; if(now-profile.lastAiReply<cd){plugin.reply(player,"The smile waits. Speak again soon.");return;} if(!allowGlobal(now)){plugin.reply(player,"The sphere is listening to too many voices. Wait a moment.");return;} profile.lastAiReply=now; plugin.remember(profile,player.getName()+": "+message); send(player,profile,message,key,false);
 }
 private boolean allowGlobal(long now){while(!requests.isEmpty()&&now-requests.peekFirst()>60000L)requests.removeFirst();if(requests.size()>=plugin.getConfig().getInt("ai.global-requests-per-minute",12))return false;requests.addLast(now);return true;}
 private void send(Player player,PlayerProfile profile,String message,String key,boolean retry){
  String context="Player: "+player.getName()+" | World: "+player.getWorld().getName()+" | Position: "+player.getLocation().getBlockX()+","+player.getLocation().getBlockY()+","+player.getLocation().getBlockZ()+" | Encounters: "+profile.encounters+" | Fear: "+profile.fear+"\nRecent in-game history:\n"+String.join("\n",profile.history)+"\nPlayer says: "+message;
  String payload="{\"model\":"+Json.quote(plugin.getConfig().getString("ai.model","openai/gpt-oss-20b"))+",\"temperature\":0.9,\"max_tokens\":110,\"messages\":[{\"role\":\"system\",\"content\":"+Json.quote(plugin.getConfig().getString("persona","You are a fictional Minecraft entity."))+"},{\"role\":\"user\",\"content\":"+Json.quote(context)+"}]}";
  HttpRequest r=HttpRequest.newBuilder(URI.create(plugin.getConfig().getString("ai.endpoint"))).timeout(Duration.ofSeconds(plugin.getConfig().getInt("ai.timeout-seconds",12))).header("Authorization","Bearer "+key).header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(payload)).build();
  http.sendAsync(r,HttpResponse.BodyHandlers.ofString()).whenComplete((res,err)->Bukkit.getScheduler().runTask(plugin,()->handle(player,profile,message,key,retry,res,err)));
 }
 private void handle(Player p,PlayerProfile profile,String message,String key,boolean retry,HttpResponse<String> res,Throwable err){
  if(!p.isOnline())return; int code=res==null?0:res.statusCode(); String reply=code>=200&&code<300?Json.content(res.body()):null;
  if(reply!=null&&!reply.isBlank()){int max=plugin.getConfig().getInt("ai.max-reply-characters",260);if(reply.length()>max)reply=reply.substring(0,max).trim();plugin.remember(profile,"Verity: "+reply);plugin.reply(p,reply);return;}
  if(!retry&&plugin.getConfig().getBoolean("ai.retry-on-temporary-error",true)&&(code==0||code==429||code>=500)){Bukkit.getScheduler().runTaskLater(plugin,()->send(p,profile,message,key,true),40L);return;}
  if(code==401||code==403)plugin.getLogger().warning("Groq rejected the API key (HTTP "+code+"). Check config.yml; the key was not logged."); else if(code==429)plugin.getLogger().warning("Groq rate limit reached; using fallback dialogue."); else if(err!=null)plugin.getLogger().warning("Groq request failed: "+err.getClass().getSimpleName()+"; using fallback dialogue."); else plugin.getLogger().warning("Groq returned HTTP "+code+"; using fallback dialogue."); plugin.reply(p,plugin.fallback(profile));
 }
}