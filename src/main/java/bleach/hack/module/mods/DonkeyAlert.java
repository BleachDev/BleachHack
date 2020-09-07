package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonParser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.*;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class DonkeyAlert extends Module {

    public DonkeyAlert(){
        super("Donkey Alert", KEY_UNBOUND, Category.WORLD, "Stole this shit from aurora");
        this.cachedUUIDs = new HashMap<String, String>() {};
        this.apiRequests = 0;
    }

    private int apiRequests;

    private final Map<String, String> cachedUUIDs;
    private final String invalidText = "Offline or invalid UUID!";
    private static long startTime;
    private static long startTime1;


    public String getNameFromUUID(String uuid)
    {
        uuid = uuid.replace("-", "");
        if (cachedUUIDs.containsKey(uuid))
        {
            return cachedUUIDs.get(uuid);
        }

        final String url = "https://api.mojang.com/user/profiles/" + uuid + "/names";
        try
        {
            final String nameJson = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (nameJson != null && nameJson.length() > 0)
            {
                JsonParser parser = new JsonParser();

                return parser.parse(nameJson).getAsJsonArray().get(parser.parse(nameJson).getAsJsonArray().size() - 1)
                        .getAsJsonObject().get("name").toString();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private String getUsername(final String uuid) {
        for (final Map.Entry<String, String> entries : this.cachedUUIDs.entrySet()) {
            if (entries.getKey().equalsIgnoreCase(uuid)) {
                return entries.getValue();
            }
        }
        try {
            try {
                if (this.apiRequests > 10) {
                    return "Too many API requests";
                }
                this.cachedUUIDs.put(uuid, Objects.requireNonNull(getNameFromUUID(uuid)).replace("\"", ""));
                ++this.apiRequests;
            }
            catch (IllegalStateException illegal) {
                this.cachedUUIDs.put(uuid, "Offline or invalid UUID!");
            }
        }
        catch (NullPointerException e) {
            this.cachedUUIDs.put(uuid, "Offline or invalid UUID!");
        }
        for (final Map.Entry<String, String> entries : this.cachedUUIDs.entrySet()) {
            if (entries.getKey().equalsIgnoreCase(uuid)) {
                return entries.getValue();
            }
        }
        return "Offline or invalid UUID!";
    }

    private void resetCache() {
        if (startTime == 0L) {
            startTime = System.currentTimeMillis();
        }
        if (startTime + 20 * 1000 <= System.currentTimeMillis()) {
            startTime = System.currentTimeMillis();
            for (final Map.Entry<String, String> entries : this.cachedUUIDs.entrySet()) {
                if (entries.getKey().equalsIgnoreCase("Offline or invalid UUID!")) {
                    this.cachedUUIDs.clear();
                }
            }
        }
    }

    private void resetRequests() {
        if (startTime1 == 0L) {
            startTime1 = System.currentTimeMillis();
        }
        if (startTime1 + 10000L <= System.currentTimeMillis()) {
            startTime1 = System.currentTimeMillis();
            if (this.apiRequests >= 2) {
                this.apiRequests = 0;
            }
        }
    }

    //+ Formatting.BLUE + "Username: " + Formatting.WHITE + getUsername()

    @Subscribe
    public void onLivingRender(EventEntityRender.Render event) {
        this.resetRequests();
        this.resetCache();
        if(mc.world == null){
            return;
        }
        for (final Entity e : mc.world.getEntities()) {
            if (e instanceof AbstractDonkeyEntity) {
                final AbstractDonkeyEntity abstractDonkeyEntity = (AbstractDonkeyEntity) e;
                if(!abstractDonkeyEntity.isTame()){
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Donkey! X: " + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z: " + Formatting.WHITE + (int)e.getZ()));
                    continue;
                }else if (abstractDonkeyEntity.getOwnerUuid() == null) {
                    continue;
                }
                mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found a Donkey-Like Entity! X: " + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z: " + Formatting.WHITE + (int)e.getZ() + Formatting.BLUE + "Owner: " + getUsername(abstractDonkeyEntity.getUuidAsString())));
            }
                /*if(abstractDonkeyEntity.getEntityId() == 24){
                    if(abstractDonkeyEntity.isTame()){
                        continue;
                    }else{
                        mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Donkey! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                    }
                    if (abstractDonkeyEntity.getOwnerUuid() == null) {
                        continue;
                    }
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Donkey! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ() + Formatting.BLUE + "Owner: " + this.getUsername(abstractDonkeyEntity.getUuidAsString())));
                }
                if(abstractDonkeyEntity.getEntityId() == 25){
                    if(abstractDonkeyEntity.isTame()){
                        continue;
                    }else{
                        mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Mule! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                    }
                    if (abstractDonkeyEntity.getOwnerUuid() == null) {
                        continue;
                    }
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Mule! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ() + Formatting.BLUE + "Owner: " + this.getUsername(abstractDonkeyEntity.getUuidAsString())));
                }
                if(abstractDonkeyEntity.getEntityId() == 29){
                    if(abstractDonkeyEntity.isTame()){
                        continue;
                    }else{
                        mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Llama! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                    }
                    if (abstractDonkeyEntity.getOwnerUuid() == null) {
                        continue;
                    }
                    mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Llama! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ() + Formatting.BLUE + "Owner: " + this.getUsername(abstractDonkeyEntity.getUuidAsString())));
                }*/


            }
        }
    }
