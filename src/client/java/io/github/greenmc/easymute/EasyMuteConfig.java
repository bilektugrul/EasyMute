package io.github.greenmc.easymute;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.sounds.SoundSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EasyMuteConfig {

    public static Map<SoundSource, Boolean> categoriesToMute = new HashMap<>();

    private static final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("easymute.properties").toFile();

    static {
        for (SoundSource source : SoundSource.values()) {
            categoriesToMute.put(source, source == SoundSource.MASTER);
        }
    }

    public static void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream(CONFIG_FILE)) {
            props.load(in);

            for (SoundSource source : SoundSource.values()) {
                String key = source.name();
                if (props.containsKey(key)) {
                    categoriesToMute.put(source, Boolean.parseBoolean(props.getProperty(key)));
                }
            }
        } catch (IOException e) {
            System.err.println("[EasyMute] Failed to load config: " + e.getMessage());
        }
    }

    public static void save() {
        Properties props = new Properties();

        for (Map.Entry<SoundSource, Boolean> entry : categoriesToMute.entrySet()) {
            props.setProperty(entry.getKey().name(), entry.getValue().toString());
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "EasyMute Configuration");
        } catch (IOException e) {
            System.err.println("[EasyMute] Failed to save config: " + e.getMessage());
        }
    }

}