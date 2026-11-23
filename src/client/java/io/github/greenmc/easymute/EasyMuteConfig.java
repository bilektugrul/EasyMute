package io.github.greenmc.easymute;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.sounds.SoundSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Properties;

public class EasyMuteConfig {

    private static final String SAVED_VOLUME_PREFIX = "savedVolume.";
    private static final String MUTE_IN_BACKGROUND = "muteInBackground";
    private static final String BACKGROUND_USES_CATEGORIES = "backgroundUsesCategories";
    private static final String BACKGROUND_VOLUME_PREFIX = "backgroundVolume.";

    public static Map<SoundSource, Boolean> categoriesToMute = new EnumMap<>(SoundSource.class);
    public static Map<SoundSource, Double> savedVolumes = new EnumMap<>(SoundSource.class);
    public static boolean muteInBackground = false;
    public static boolean backgroundUsesCategories = false;
    public static Map<SoundSource, Double> backgroundVolumes = new EnumMap<>(SoundSource.class);

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

            muteInBackground = Boolean.parseBoolean(props.getProperty(MUTE_IN_BACKGROUND, "false"));
            backgroundUsesCategories = Boolean.parseBoolean(props.getProperty(BACKGROUND_USES_CATEGORIES, "false"));

            for (SoundSource source : SoundSource.values()) {
                String key = source.name();
                if (props.containsKey(key)) {
                    categoriesToMute.put(source, Boolean.parseBoolean(props.getProperty(key)));
                }

                Double savedVolume = parseVolume(props.getProperty(SAVED_VOLUME_PREFIX + key));
                if (savedVolume != null) {
                    savedVolumes.put(source, savedVolume);
                }

                Double backgroundVolume = parseVolume(props.getProperty(BACKGROUND_VOLUME_PREFIX + key));
                if (backgroundVolume != null) {
                    backgroundVolumes.put(source, backgroundVolume);
                }
            }
        } catch (IOException e) {
            EasyMute.LOGGER.error("Failed to load config", e);
        }
    }

    public static void save() {
        Properties props = new Properties();
        props.setProperty(MUTE_IN_BACKGROUND, Boolean.toString(muteInBackground));
        props.setProperty(BACKGROUND_USES_CATEGORIES, Boolean.toString(backgroundUsesCategories));

        for (Map.Entry<SoundSource, Boolean> entry : categoriesToMute.entrySet()) {
            props.setProperty(entry.getKey().name(), entry.getValue().toString());
        }

        for (Map.Entry<SoundSource, Double> entry : savedVolumes.entrySet()) {
            props.setProperty(SAVED_VOLUME_PREFIX + entry.getKey().name(), entry.getValue().toString());
        }

        for (Map.Entry<SoundSource, Double> entry : backgroundVolumes.entrySet()) {
            props.setProperty(BACKGROUND_VOLUME_PREFIX + entry.getKey().name(), entry.getValue().toString());
        }

        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            props.store(out, "EasyMute Configuration");
        } catch (IOException e) {
            EasyMute.LOGGER.error("Failed to save config", e);
        }
    }

    private static Double parseVolume(String value) {
        if (value == null) {
            return null;
        }

        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
