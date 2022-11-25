package cool.muyucloud.pullup.util.condition;

import com.google.gson.*;
import cool.muyucloud.pullup.Pullup;
import cool.muyucloud.pullup.util.Registry;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class ConditionLoader {
    private static final Registry<Condition> CONDITIONS = Registry.CONDITIONS;
    private static final Path PATH = FabricLoader.getInstance().getGameDir().resolve("pullup");
    private static final Logger LOGGER = Pullup.getLogger();

    private final String spaceName;
    private final String fileContent;

    /**
     * For client only.
     */
    public ConditionLoader(String fileName, String fileContent) {
        this.spaceName = Objects.equals(fileName, "default") ? "pullup" : getFileNoEx(fileName);
        this.fileContent = fileContent;
    }

    /**
     * Read specific condition set.
     */
    public ConditionLoader(String fileName) throws IOException {
        this.spaceName = getFileNoEx(fileName);
        this.fileContent = readFile(Files.newInputStream(PATH.resolve("fileName")));
    }

    /**
     * Read default condition set.
     */
    public ConditionLoader() {
        this.spaceName = "pullup";
        this.fileContent = readFile(ConditionLoader.class.getClassLoader().getResourceAsStream("default_condition.json"));
    }

    public String getFileContent() {
        return this.fileContent;
    }

    /**
     * Will not change configuration "loadSet".
     */
    public void load() {
        JsonArray array = this.loadConditionArray();
        if (array == null || array.size() == 0) {
            return;
        }

        for (JsonElement element : array) {
            JsonObject object = element.isJsonObject() ? element.getAsJsonObject() : null;
            if (object == null) return;
            Condition condition;
            try {
                condition = new Condition(this.spaceName, object);
            } catch (Exception e) {
                LOGGER.error(
                    String.format("Problems appeared in condition %s:%s in condition file %s.json.",
                        this.spaceName, tryGetName(object), this.spaceName));
                e.printStackTrace();
                continue;
            }
            CONDITIONS.register(condition.getId(), condition);
        }
    }

    private static String tryGetName(JsonObject object) {
        try {
            return object.get("name").getAsString();
        } catch (Exception e) {
            return "@empty";
        }
    }

    private static String readFile(InputStream stream) {
        try (stream) {
            return IOUtils.toString(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    private JsonArray loadConditionArray() {
        try {
            return (new Gson()).fromJson(this.fileContent, JsonArray.class);
        } catch (Exception e) {
            LOGGER.error(String.format("Condition set file %s.json is invalid!", this.spaceName));
            return null;
        }
    }

    private static void prepareDir() {
        if (!Files.exists(PATH)) {
            try {
                Files.createDirectories(PATH);
            } catch (Exception e) {
                LOGGER.warn(String.format("Failed to generate condition set directory at %s!", PATH));
            }
        }
    }

    private static boolean isDirOK() {
        return Files.exists(PATH);
    }

    private static String getFileEx(String name) {
        if ((name != null) && (name.length() > 0)) {
            int dot = name.lastIndexOf('.');
            if (dot > -1 && (dot + 1) < name.length()) {
                return name.substring(dot + 1);
            }
        }
        return "";
    }

    private static String getFileNoEx(String name) {
        if (name.length() > 0) {
            int dot = name.lastIndexOf('.');
            if (dot > 0) {
                return name.substring(0, dot);
            }
        }
        return "";
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
            e.printStackTrace();
        }
    }

    public static HashSet<String> getFileList() {
        prepareDir();
        if (!isDirOK()) {
            return new HashSet<>();
        }

        String[] list = PATH.toFile().list();
        if (list == null) {
            return new HashSet<>();
        }

        HashSet<String> filtered = new HashSet<>();
        for (String name : list) {
            if (Files.isDirectory(PATH.resolve(name)) || !getFileEx(name).equals("json")) {
                continue;
            }
            filtered.add(name);
        }

        return filtered;
    }

    public static boolean containsFile(String name) {
        return getFileList().contains(name);
    }
}
