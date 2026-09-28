package me.pangrusak.verity;
import java.util.*;
public final class PlayerProfile {
 public final UUID uuid; public boolean accepted; public int encounters; public int fear; public long lastAiReply; public long nextEventAt; public final List<String> history=new ArrayList<>();
 public PlayerProfile(UUID uuid){this.uuid=uuid;}
}