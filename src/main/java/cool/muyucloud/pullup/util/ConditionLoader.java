package cool.muyucloud.pullup.util;

import com.google.gson.*;
import cool.muyucloud.pullup.Pullup;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class ConditionLoader {
    private static final Registry<Condition> CONDITIONS = Registry.CONDITIONS;
    private static final Config CONFIG = Pullup.getConfig();
    private static final Path PATH = FabricLoader.getInstance().getGameDir().resolve("pullup");
    private static final Logger LOGGER = Pullup.getLogger();

    public static void load(String name) {
        prepareDir();
        HashSet<String> files = new HashSet<>(List.of(getFiles()));

        if (files.contains(name)) {
            CONDITIONS.clear();
            CONFIG.set("loadSet", name);
            JsonArray array = readArray(name);
            if (array == null || array.size() == 0) {
                return;
            }

            String spaceName = getFileNameNoEx(name);
            for (int i = 0; i < array.size(); ++i) {
                Condition condition = parseCondition(array.get(i));
                if (condition == null) {
                    LOGGER.warn(String.format("Problems occurred during analyzing conditions in file %s.", name));
                    continue;
                }
                CONDITIONS.register(new Identifier(String.format("%s:%s", spaceName, condition.getName())), condition);
            }
        }
    }

    @NotNull
    public static String[] getFiles() {
        prepareDir();
        String[] output = PATH.toFile().list();
        return output == null ? new String[0] : output;
    }

    private static Condition parseCondition(JsonElement element) {
        try {
            JsonObject object = element.getAsJsonObject();

            String name = object.getAsJsonPrimitive("name").getAsString();
            String sound = object.getAsJsonPrimitive("sound").getAsString();
            int checkDelay = object.getAsJsonPrimitive("check_delay").getAsInt();
            JsonArray argsJson = object.getAsJsonArray("arguments");
            JsonArray expsJson = object.getAsJsonArray("expressions");

            if (argsJson.size() == 0 || expsJson.size() == 0) {
                return new Condition(name, sound, checkDelay, new String[]{"-1"});
            }

            String[] argArray = parseJsonArrayAsString(argsJson);
            String[] expArray = parseJsonArrayAsString(expsJson);

            return new Condition(name, sound, checkDelay, expArray, argArray);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static JsonArray readArray(String name) {
        JsonArray array = null;
        try (InputStream inputStream = Files.newInputStream(PATH.resolve(name))) {
            array = (new Gson()).fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonArray.class);
        } catch (Exception e) {
            LOGGER.warn(String.format("Problems occurred during reading condition file %s.", name));
            e.printStackTrace();
        }
        return array;
    }

    private static String getFileNameNoEx(String filename) {
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if (dot > -1) {
                return filename.substring(0, dot);
            }
        }
        return filename;
    }

    public static void loadDefault() {
        prepareDir();
        CONDITIONS.clear();
        CONFIG.set("loadSet", "default");
        String json;
        JsonArray array;

        try {
            json = IOUtils.toString(
                Objects.requireNonNull(
                    ConditionLoader.class.getClassLoader()
                        .getResourceAsStream("default_condition.json")),
                StandardCharsets.UTF_8);
            array = (new Gson()).fromJson(json, JsonArray.class);
        } catch (Exception e) {
            throw new NullPointerException("Can not read default condition file!");
        }

        String spaceName = "pullup";
        for (int i = 0; i < array.size(); ++i) {
            Condition condition = parseCondition(array.get(i));
            if (condition == null) {
                LOGGER.warn("Problems occurred during analyzing conditions in default condition file.");
                continue;
            }
            CONDITIONS.register(new Identifier(String.format("%s:%s", spaceName, condition.getName())), condition);
        }
    }

    public static String[] parseJsonArrayAsString(JsonArray array) {
        String[] output = new String[array.size()];
        for (int i = 0; i < array.size(); ++i) {
            output[i] = array.get(i).getAsString();
        }
        return output;
    }

    public static void writeDefaultConditions() {
        prepareDir();

        try {
            Files.copy(
                Objects.requireNonNull(ConditionLoader.class.getClassLoader().getResourceAsStream("default_condition.json")),
                PATH.resolve("example.json"),
                StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            LOGGER.warn("Can not generate example condition set.");
            throw new RuntimeException(e);
        }
    }

    public static void prepareDir() {
        if (!Files.exists(PATH)) {
            try {
                Files.createDirectories(PATH);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
