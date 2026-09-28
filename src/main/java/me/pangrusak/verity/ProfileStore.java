package me.pangrusak.verity;
import org.bukkit.configuration.file.YamlConfiguration; import java.io.*; import java.util.*;
public final class ProfileStore {
 private final File file; private final Map<UUID,PlayerProfile> profiles=new HashMap<>();
 public ProfileStore(File folder){file=new File(folder,"players.yml");load();}
 public PlayerProfile get(UUID id){return profiles.computeIfAbsent(id,PlayerProfile::new);}
 public void load(){if(!file.exists())return; YamlConfiguration y=YamlConfiguration.loadConfiguration(file); for(String k:y.getKeys(false))try{UUID id=UUID.fromString(k);PlayerProfile p=new PlayerProfile(id);p.accepted=y.getBoolean(k+".accepted");p.encounters=y.getInt(k+".encounters");p.fear=y.getInt(k+".fear");p.lastAiReply=y.getLong(k+".last-ai-reply");p.nextEventAt=y.getLong(k+".next-event-at");p.history.addAll(y.getStringList(k+".history"));profiles.put(id,p);}catch(IllegalArgumentException ignored){}}
 public void save(){YamlConfiguration y=new YamlConfiguration();for(PlayerProfile p:profiles.values()){String k=p.uuid.toString();y.set(k+".accepted",p.accepted);y.set(k+".encounters",p.encounters);y.set(k+".fear",p.fear);y.set(k+".last-ai-reply",p.lastAiReply);y.set(k+".next-event-at",p.nextEventAt);y.set(k+".history",p.history);}try{y.save(file);}catch(IOException ignored){}}
}