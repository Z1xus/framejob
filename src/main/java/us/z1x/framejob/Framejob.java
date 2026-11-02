package us.z1x.framejob;

import net.fabricmc.loader.api.FabricLoader;
import org.polyfrost.oneconfig.api.config.v1.annotations.MasterToggle;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Framejob {
    private static final Logger LOGGER = LoggerFactory.getLogger("Framejob");

    @MasterToggle
    public static boolean enabled = true;

    @Switch(title = "Smooth Swings", description = "Eases the arm swing between ticks.")
    public static boolean smoothSwings = true;

    @Slider(
            title = "Swing Smoothing",
            description = "In ticks. Higher is smoother but trails further behind.",
            min = 0.25F,
            max = 3,
            step = 0.25F
    )
    public static float swingSmoothing = 1;

    @Switch(title = "Smooth FOV", description = "Eases sprint, speed and bow FOV changes between ticks.")
    public static boolean smoothFov = true;

    @Switch(title = "Smooth Hand Sway", description = "Eases the held item sway when turning between ticks.")
    public static boolean smoothHandSway = true;

    //? if > 1.8.9 {
    @Switch(title = "Smooth Sneak Camera", description = "Eases the camera height when sneaking between ticks.")
    public static boolean smoothSneakCamera = true;

    @Switch(title = "Precise Frame Clock", description = "Times frames in nanoseconds instead of milliseconds.")
    public static boolean preciseClock = true;
    //?}

    public static float exponentialTickDelta(float tickDelta) {
        return 2 * (1 - (float) Math.pow(0.5, tickDelta));
    }

    public static void load() throws IllegalAccessException {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("framejob.properties");
        Properties saved = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                saved.load(reader);
            } catch (IOException | IllegalArgumentException e) {
                LOGGER.warn("Could not read {}", path, e);
            }
        }
        Properties properties = new Properties();
        for (Field field : Framejob.class.getFields()) {
            String value = saved.getProperty(field.getName());
            try {
                if (value != null) {
                    field.set(null, field.getType() == boolean.class ? Boolean.parseBoolean(value) : Float.parseFloat(value));
                }
            } catch (NumberFormatException e) {
                LOGGER.warn("Ignoring {}={} in {}", field.getName(), value, path);
            }
            properties.setProperty(field.getName(), String.valueOf(field.get(null)));
        }
        if (properties.equals(saved)) return;
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, null);
        } catch (IOException e) {
            LOGGER.warn("Could not write {}", path, e);
        }
    }
}
