package io.blocktyper.theotherworlds.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

public class FileUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    final static int[] illegalChars = {34, 60, 62, 124, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 58, 42, 63, 92, 47};

    static {
        Arrays.sort(illegalChars);
    }

    static {
        OBJECT_MAPPER.setDefaultMergeable(false);
        OBJECT_MAPPER.configOverride(Object.class)
                .setMergeable(false);
    }

    public static RootConfig getLocalLwjglApplicationConfig(ClassLoader classLoader, String defaultFromResources, String localOverride) {
        try {
            String configString = FileUtils.getResourceAsString(defaultFromResources, classLoader);

            JsonNode defaults = getJsonNodeFromRawString(configString);

            final JsonNode finalConfig;
            String localConfigString = FileUtils.getLocalFileString(localOverride);
            if (localConfigString != null && !localConfigString.isBlank()) {
                JsonNode overrides = getJsonNodeFromRawString(localConfigString);
                finalConfig = OBJECT_MAPPER.readerForUpdating(defaults).readValue(overrides);
            } else {
                finalConfig = defaults;
            }

            return OBJECT_MAPPER.treeToValue(finalConfig, RootConfig.class);
        } catch (Exception ex) {
            throw new RuntimeException("Config issue: " + ex.getMessage(), ex);
        }
    }

    public static JsonNode getJsonNodeFromRawString(String rawJsonString) throws RuntimeException {
        try{
            return OBJECT_MAPPER.readTree(rawJsonString);
        } catch(Exception ex) {
            System.out.println("Exception reading JsonNode from raw string: " + rawJsonString + " - Message: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        }
    }

    public static byte[] getLocalFileBytes(String path) {
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
            throw new IllegalArgumentException("file not found! " + fileName);
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
        try {
            Path path = Paths.get(fileName);
            Files.createDirectories(path.getParent());
            Files.write(path, contents);
            //Files.createFile(path);
        } catch (Exception ex) {
            System.out.println("Exception writing file " + fileName + " " + ex.getMessage());
        }
    }

    public static String cleanFileName(String badFileName) {
        return cleanFileName(badFileName, true);
    }

    public static String cleanFileName(String badFileName, boolean allowPaths) {

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
