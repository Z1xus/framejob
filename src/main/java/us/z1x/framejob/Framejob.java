package us.z1x.framejob;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;
import org.polyfrost.oneconfig.api.config.v1.annotations.DependsOn;
import org.polyfrost.oneconfig.api.config.v1.annotations.MasterToggle;
import org.polyfrost.oneconfig.api.config.v1.annotations.Slider;
import org.polyfrost.oneconfig.api.config.v1.annotations.Switch;

public class Framejob {
    @MasterToggle
    public static boolean enabled = true;

    @Switch(title = "Smooth Swings", description = "Eases the arm swing between ticks.")
    public static boolean smoothSwings = true;

    @DependsOn("smoothSwings")
    @Slider(title = "Swing Smoothing", description = "In ticks. Higher is smoother but trails further behind.", min = 0.25F, max = 3, step = 0.25F)
    public static float swingSmoothing = 1;

    @Switch(title = "Smooth FOV", description = "Eases sprint, speed and bow FOV changes between ticks.")
    public static boolean smoothFov = true;

    @Switch(title = "Smooth Hand Sway", description = "Eases the held item sway when turning between ticks.")
    public static boolean smoothHandSway = true;

    //? if > 1.8.9 {
    @Switch(title = "Precise Frame Clock", description = "Times frames in nanoseconds instead of milliseconds.")
    public static boolean preciseClock = true;
    //?}

    public static float halfwayDelta(float tickDelta) {
        return 2 * (1 - (float) Math.pow(0.5, tickDelta));
    }

    public static void load() throws IOException, IllegalAccessException {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("framejob.properties");
        Properties properties = new Properties();
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                properties.load(reader);
            }
        }
        for (Field field : Framejob.class.getFields()) {
            String value = properties.getProperty(field.getName());
            if (value != null) field.set(null, field.getType() == boolean.class ? Boolean.parseBoolean(value) : Float.parseFloat(value));
            properties.setProperty(field.getName(), String.valueOf(field.get(null)));
        }
        try (Writer writer = Files.newBufferedWriter(path)) {
            properties.store(writer, null);
        }
    }
}
