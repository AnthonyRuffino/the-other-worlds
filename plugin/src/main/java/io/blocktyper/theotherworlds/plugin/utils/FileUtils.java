package io.blocktyper.theotherworlds.plugin.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class FileUtils {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(illegalChars);
    }

    static {
        OBJECT_MAPPER.setDefaultMergeable(false);
        OBJECT_MAPPER.configOverride(Object.class)
                .setMergeable(false);
    }

    public static <T> Optional<T> deserializeJson(Class<T> clazz, JsonNode jsonNode) {
        try {
            return Optional.ofNullable(OBJECT_MAPPER.treeToValue(jsonNode, clazz));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static JsonNode getJsonNodeWithLocalOverride(
            ClassLoader classLoader,
            String defaultFromResources,
            Optional<JsonNode> optionalLocalOverride
    ) {
        try {
            String configString = FileUtils.getResourceAsString(defaultFromResources, classLoader);

            JsonNode defaults = configString == null ? null : getJsonNodeFromRawString(configString);

            return optionalLocalOverride.flatMap(localOverride -> {
                try {
                    return Optional.ofNullable(defaults == null ? localOverride : merge(defaults, localOverride));
                } catch (Exception ex) {
                    return Optional.empty();
                }
            }).orElse(defaults);
        } catch (Exception ex) {
            throw new RuntimeException("Config issue: " + ex.getMessage(), ex);
        }
    }

    public static JsonNode merge(JsonNode defaults, JsonNode localOverride) {
        try {
            return OBJECT_MAPPER.readerForUpdating(defaults).readValue(localOverride);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static JsonNode merge(Object defaults, Optional<JsonNode> localOverride) {
        JsonNode mappedDefaults = OBJECT_MAPPER.valueToTree(defaults);
        return localOverride.map(l -> merge(mappedDefaults, l)).orElse(mappedDefaults);
    }

    public static String getPrettyString(JsonNode jsonNode) throws JsonProcessingException {
        return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
    }

    public static <T> Optional<T> getJsonNodeWithLocalOverride(
            ClassLoader classLoader,
            String defaultFromResources,
            String localOverridePath,
            Class<T> clazz
    ) {

        Optional<JsonNode> optionalLocalOverride = getLocalOverride(localOverridePath);

        final JsonNode finalConfig = getJsonNodeWithLocalOverride(classLoader, defaultFromResources, optionalLocalOverride);

        return Optional.ofNullable(finalConfig).flatMap(c -> deserializeJson(clazz, c));
    }

    public static Optional<JsonNode> getLocalOverride(String localOverridePath) {
        return Optional.ofNullable(FileUtils.getLocalFileString(localOverridePath))
                .filter(c -> !c.isBlank())
                .map(FileUtils::getJsonNodeFromRawString);
    }

    public static JsonNode getJsonNodeFromRawString(String rawJsonString) throws RuntimeException {
        try {
            return OBJECT_MAPPER.readTree(rawJsonString);
        } catch (Exception ex) {
            System.out.println("Exception reading JsonNode from raw string: " + rawJsonString + " - Message: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public static byte[] getLocalFileBytes(String path) {
        if (!Files.exists(Paths.get(path))) {
            return null;
        }
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Exception getting local file bytes: " + ex.getMessage());
            return null;
        }
    }

    public static String getLocalFileString(String path) {
        final FileInputStream fis;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            return null;
        }
        return inputStringToString(fis);
    }

    public static String getResourceAsString(String fileName, ClassLoader classLoader) {
        InputStream inputStream = classLoader.getResourceAsStream(fileName);

        if (inputStream == null) {
            System.out.println("file not found! " + fileName);
            return null;
        } else {
            return inputStringToString(inputStream);
        }
    }

    public static String inputStringToString(InputStream inputStream) {
        return new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.joining("\n"));
    }


    public static void writeFile(String fileName, byte[] contents) {
        if(contents == null) {
            System.out.println("File contents were null: " + fileName);
            return;
        }

        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, contents);
        } catch (Exception ex) {
            System.out.println("Exception writing file " + fileName + " " + ex.getMessage());
        }
    }

    public static void writeFile(String fileName, InputStream inputStream) {
        if(inputStream == null) {
            System.out.println("Cant write file.  Input stream is null. fileName: " + fileName);
            return;
        }
        try {
            byte[] contents = inputStream.readAllBytes();
            writeFile(fileName, contents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String cleanFileName(String badFileName, boolean allowPaths) {

        if (badFileName == null) {
            badFileName = "";
        }

        if (!allowPaths) {
            badFileName = badFileName.replaceAll("\\.", "");
            badFileName = badFileName.replaceAll("\\\\", "");
            badFileName = badFileName.replaceAll("/", "");
            badFileName = badFileName.replaceAll("\\/", "");
        }

        StringBuilder cleanName = new StringBuilder();
        for (int i = 0; i < badFileName.length(); i++) {
            int c = (int) badFileName.charAt(i);
            if (Arrays.binarySearch(illegalChars, c) < 0) {
                cleanName.append((char) c);
            }
        }

        return cleanName.toString();
    }
}
