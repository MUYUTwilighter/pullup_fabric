package cool.muyucloud.pullup.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import cool.muyucloud.pullup.Pullup;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Logger LOGGER = Pullup.getLogger();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("pullup.json");

    private final JsonObject properties = new JsonObject();

    public Config() {
        this.properties.addProperty("enable", true);
        this.properties.addProperty("loadServer", true);
        this.properties.addProperty("sendServer", true);
        this.properties.addProperty("maxDistance", 500);
        this.properties.addProperty("sendDelay", 50);
        this.properties.addProperty("loadSet", "default");
        this.properties.addProperty("hudTextDisplayX", 0.65f);
        this.properties.addProperty("hudTextDisplayY", 0.6f);
    }

    public String getAsString(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsString();
    }

    public boolean getAsBool(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsBoolean();
    }

    public int getAsInt(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsInt();
    }

    public float getAsFloat(String key) {
        if (!this.properties.has(key)) {
            throw new NullPointerException("Tried to access property %s but it does not exists!".formatted(key));
        }

        return this.properties.getAsJsonPrimitive(key).getAsFloat();
    }

    public void set(String key, String value) {
        this.properties.addProperty(key, value);
    }

    public void set(String key, boolean value) {
        this.properties.addProperty(key, value);
    }

    public void loadAndCorrect() {
        if (!Files.exists(PATH)) {
            // try to create new config file
            LOGGER.info("saplanting.json does not exist, generating.");
            this.save();
            return;
        }
        this.readFile();
    }

    private void readFile() {
        try (InputStream inputStream = Files.newInputStream(PATH)) {
            JsonObject read = (new Gson()).fromJson(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), JsonObject.class);

            // Analyzing properties
            for (String key : read.keySet()) {
                if (this.properties.has(key)) {
                    JsonPrimitive dst = this.properties.getAsJsonPrimitive(key);
                    JsonPrimitive src = read.get(key).getAsJsonPrimitive();

                    try {
                        if (dst.isBoolean()) {
                            this.properties.addProperty(key, src.getAsBoolean());
                            continue;
                        }
                    } catch (Exception e) {
                        LOGGER.warn("Problems occurred during analyzing property %s.".formatted(key));
                    }
                    this.properties.addProperty(key, src.getAsString());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during reading config file.");
        }
    }

    private void verifyOrGenFile() {
        if (!Files.exists(PATH)) {
            // try to create new config file
            LOGGER.info("saplanting.json does not exist, generating.");
            this.genFile();
        }
    }

    private void genFile() {
        try {
            Files.createFile(PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to generate config file at %s.".formatted(PATH), e);
        }
    }

    public void save() {
        String json = (new GsonBuilder().setPrettyPrinting().create()).toJson(this.properties);
        this.verifyOrGenFile();
        this.writeFile(json);
    }

    private void writeFile(String json) {
        try (OutputStream outputStream = Files.newOutputStream(PATH)) {
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOGGER.warn("Problems occurred during writing config file.");
        }
    }
}
