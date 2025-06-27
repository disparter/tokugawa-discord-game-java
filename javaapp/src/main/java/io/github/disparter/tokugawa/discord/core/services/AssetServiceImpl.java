package io.github.disparter.tokugawa.discord.core.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the AssetService interface.
 */
@Slf4j
@Service
public class AssetServiceImpl implements AssetService {

    @Value("${assets.base.path:classpath:assets}")
    private String assetsBasePath;

    @Value("${assets.base.url:}")
    private String assetsBaseUrl;

    @Autowired
    private ResourceLoader resourceLoader;

    // Maps for different asset types
    private final Map<String, String> backgroundImages = new HashMap<>();
    private final Map<String, String> characterImages = new HashMap<>();
    private final Map<String, String> locationImages = new HashMap<>();

    // Fallback images
    private final Map<String, String> fallbackImages = new HashMap<>();

    public AssetServiceImpl() {
        initializeFallbackImages();
        // Initialize asset loading on service creation
        loadAssets();
    }

    /**
     * Load all assets from the file system.
     * This method scans the assets directory and populates the asset maps.
     */
    private void loadAssets() {
        try {
            Path assetsPath = Paths.get(assetsBasePath);
            if (!Files.exists(assetsPath)) {
                log.warn("Assets directory does not exist: {}", assetsPath);
                return;
            }

            // Load background images
            loadAssetsOfType("backgrounds", backgroundImages, ".png");

            // Load character images
            loadAssetsOfType("characters", characterImages, ".png");

            // Load location images
            loadAssetsOfType("locations", locationImages, ".png");

            log.info("Loaded {} background images, {} character images, and {} location images",
                    backgroundImages.size(), characterImages.size(), locationImages.size());
        } catch (Exception e) {
            log.error("Error loading assets", e);
        }
    }

    /**
     * Load assets of a specific type from the file system.
     * 
     * @param directory the directory to scan
     * @param assetMap the map to populate
     * @param extension the file extension to look for
     */
    private void loadAssetsOfType(String directory, Map<String, String> assetMap, String extension) {
        try {
            Path directoryPath = Paths.get(assetsBasePath, directory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
                log.info("Created directory: {}", directoryPath);
                return;
            }

            Files.list(directoryPath)
                .filter(path -> path.toString().endsWith(extension))
                .forEach(path -> {
                    String fileName = path.getFileName().toString();
                    String assetId = fileName.substring(0, fileName.lastIndexOf('.'));
                    String relativePath = directory + "/" + fileName;
                    assetMap.put(assetId, relativePath);
                    log.debug("Loaded asset: {} -> {}", assetId, relativePath);
                });
        } catch (Exception e) {
            log.error("Error loading assets from directory: {}", directory, e);
        }
    }

    /**
     * Initialize the fallback images for different asset types.
     */
    private void initializeFallbackImages() {
        fallbackImages.put("background", "backgrounds/default_background.png");
        fallbackImages.put("character", "characters/default_character.png");
        fallbackImages.put("location", "locations/default_location.png");
        fallbackImages.put("item", "items/default_item.png");
        fallbackImages.put("event", "events/default_event.png");
        fallbackImages.put("club", "clubs/default_club.png");
        fallbackImages.put("default", "default.png");

        // Ensure fallback directories exist
        createFallbackDirectories();

        // Ensure fallback images exist
        createDefaultFallbackImages();
    }

    /**
     * Create the necessary directories for fallback images.
     */
    private void createFallbackDirectories() {
        try {
            Path assetsPath = Paths.get(assetsBasePath);
            if (!Files.exists(assetsPath)) {
                Files.createDirectories(assetsPath);
                log.info("Created assets directory: {}", assetsPath);
            }

            // Create directories for each asset type
            String[] directories = {"backgrounds", "characters", "locations", "items", "events", "clubs"};
            for (String dir : directories) {
                Path dirPath = Paths.get(assetsBasePath, dir);
                if (!Files.exists(dirPath)) {
                    Files.createDirectories(dirPath);
                    log.info("Created directory: {}", dirPath);
                }
            }
        } catch (Exception e) {
            log.error("Error creating fallback directories", e);
        }
    }

    /**
     * Create default fallback images if they don't exist.
     * This ensures that there's always a fallback image available.
     */
    private void createDefaultFallbackImages() {
        try {
            // Check and create the default fallback image
            Path defaultPath = Paths.get(assetsBasePath, fallbackImages.get("default"));
            if (!Files.exists(defaultPath)) {
                createEmptyImageFile(defaultPath);
                log.info("Created default fallback image: {}", defaultPath);
            }

            // Check and create type-specific fallback images
            for (Map.Entry<String, String> entry : fallbackImages.entrySet()) {
                if (!"default".equals(entry.getKey())) {
                    Path path = Paths.get(assetsBasePath, entry.getValue());
                    if (!Files.exists(path)) {
                        createEmptyImageFile(path);
                        log.info("Created fallback image for {}: {}", entry.getKey(), path);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error creating fallback images", e);
        }
    }

    /**
     * Create an empty image file at the specified path.
     * This is a simple 1x1 pixel image that serves as a placeholder.
     * 
     * @param path the path where the image should be created
     * @throws IOException if an I/O error occurs
     */
    private void createEmptyImageFile(Path path) throws IOException {
        // Create parent directories if they don't exist
        Files.createDirectories(path.getParent());

        // Create an empty file
        Files.createFile(path);

        log.debug("Created empty image file: {}", path);
    }

    @Override
    public URL getImageUrl(String context) {
        try {
            // Try to find the image based on context
            String imagePath = findImagePathByContext(context);

            // If image path is found, return the URL
            if (imagePath != null) {
                return createUrl(imagePath);
            }

            // If no image is found, return the default fallback image
            return getFallbackImageUrl("default");
        } catch (Exception e) {
            log.error("Error getting image URL for context: {}", context, e);
            try {
                return getFallbackImageUrl("default");
            } catch (Exception ex) {
                log.error("Error getting fallback image URL", ex);
                return null;
            }
        }
    }

    @Override
    public File getImageFile(String context) {
        try {
            // Try to find the image based on context
            String imagePath = findImagePathByContext(context);

            // If image path is found, return the file
            if (imagePath != null) {
                Path path = Paths.get(assetsBasePath, imagePath);
                if (Files.exists(path)) {
                    return path.toFile();
                }
            }

            // If no image is found, return the default fallback image file
            String fallbackPath = fallbackImages.get("default");
            Path path = Paths.get(assetsBasePath, fallbackPath);
            return path.toFile();
        } catch (Exception e) {
            log.error("Error getting image file for context: {}", context, e);
            try {
                String fallbackPath = fallbackImages.get("default");
                Path path = Paths.get(assetsBasePath, fallbackPath);
                return path.toFile();
            } catch (Exception ex) {
                log.error("Error getting fallback image file", ex);
                return null;
            }
        }
    }

    @Override
    public URL getBackgroundImageUrl(String location) {
        try {
            // Try to find the background image for the location
            String imagePath = backgroundImages.getOrDefault(location, 
                    "backgrounds/" + location + ".png");

            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + imagePath);
                if (resource.exists()) {
                    return createUrl(imagePath);
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", imagePath);
            }

            // If no image is found, return the fallback background image
            return getFallbackImageUrl("background");
        } catch (Exception e) {
            log.error("Error getting background image URL for location: {}", location, e);
            try {
                return getFallbackImageUrl("background");
            } catch (Exception ex) {
                log.error("Error getting fallback background image URL", ex);
                return null;
            }
        }
    }

    @Override
    public URL getCharacterImageUrl(String characterId) {
        try {
            // Try to find the character image
            String imagePath = characterImages.getOrDefault(characterId, 
                    "characters/" + characterId + ".png");

            Path path = Paths.get(assetsBasePath, imagePath);
            if (Files.exists(path)) {
                return createUrl(imagePath);
            }

            // If no image is found, return the fallback character image
            return getFallbackImageUrl("character");
        } catch (Exception e) {
            log.error("Error getting character image URL for character: {}", characterId, e);
            try {
                return getFallbackImageUrl("character");
            } catch (Exception ex) {
                log.error("Error getting fallback character image URL", ex);
                return null;
            }
        }
    }

    @Override
    public URL getLocationImageUrl(String locationId) {
        try {
            // Try to find the location image
            String imagePath = locationImages.getOrDefault(locationId, 
                    "locations/" + locationId + ".png");

            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + imagePath);
                if (resource.exists()) {
                    return createUrl(imagePath);
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", imagePath);
            }

            // If no image is found, return the fallback location image
            return getFallbackImageUrl("location");
        } catch (Exception e) {
            log.error("Error getting location image URL for location: {}", locationId, e);
            try {
                return getFallbackImageUrl("location");
            } catch (Exception ex) {
                log.error("Error getting fallback location image URL", ex);
                return null;
            }
        }
    }

    @Override
    public boolean assetExists(String context) {
        String imagePath = findImagePathByContext(context);
        if (imagePath != null) {
            Path path = Paths.get(assetsBasePath, imagePath);
            return Files.exists(path);
        }
        return false;
    }

    @Override
    public URL getFallbackImageUrl(String assetType) {
        try {
            // Get the fallback path for the specified asset type, or use the default if not found
            String fallbackPath = fallbackImages.getOrDefault(assetType, fallbackImages.get("default"));

            // Check if the fallback image exists
            Path path = Paths.get(assetsBasePath, fallbackPath);
            if (!Files.exists(path)) {
                // If the specific fallback doesn't exist, try to create it
                try {
                    createEmptyImageFile(path);
                    log.info("Created missing fallback image: {}", path);
                } catch (Exception ex) {
                    log.warn("Could not create fallback image: {}", path, ex);

                    // If we can't create the specific fallback, use the default fallback
                    fallbackPath = fallbackImages.get("default");
                    path = Paths.get(assetsBasePath, fallbackPath);

                    // If the default fallback doesn't exist, create it
                    if (!Files.exists(path)) {
                        try {
                            createEmptyImageFile(path);
                            log.info("Created missing default fallback image: {}", path);
                        } catch (Exception e2) {
                            log.error("Could not create default fallback image: {}", path, e2);
                            // At this point, we've tried everything, so we'll return a hardcoded URL
                            return new URL("file:" + assetsBasePath + "/default.png");
                        }
                    }
                }
            }

            return createUrl(fallbackPath);
        } catch (Exception e) {
            log.error("Error getting fallback image URL for asset type: {}", assetType, e);
            try {
                // Last resort: return a hardcoded URL to the default image
                return new URL("file:" + assetsBasePath + "/default.png");
            } catch (Exception ex) {
                log.error("Critical error: Could not create hardcoded fallback URL", ex);
                // If all else fails, return null, but this should never happen
                return null;
            }
        }
    }

    /**
     * Find the image path based on the context.
     * 
     * @param context the context identifier
     * @return the image path, or null if not found
     */
    private String findImagePathByContext(String context) {
        if (context == null || context.isEmpty()) {
            log.warn("Empty context provided to findImagePathByContext");
            return null;
        }

        log.debug("Finding image path for context: {}", context);

        // Check if the context contains multiple parts (e.g., "location:tokyo" or "character:hero:angry")
        if (context.contains(":")) {
            return handleComplexContext(context);
        }

        // Check if the context is directly in one of our maps
        // Check if the context is a background
        if (backgroundImages.containsKey(context)) {
            log.debug("Found background image for context: {}", context);
            return backgroundImages.get(context);
        }

        // Check if the context is a character
        if (characterImages.containsKey(context)) {
            log.debug("Found character image for context: {}", context);
            return characterImages.get(context);
        }

        // Check if the context is a location
        if (locationImages.containsKey(context)) {
            log.debug("Found location image for context: {}", context);
            return locationImages.get(context);
        }

        // Try to infer the type from the context naming pattern
        if (context.startsWith("background_") || context.contains("_background")) {
            String path = "backgrounds/" + context + ".png";
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
                if (resource.exists()) {
                    return path;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", path);
            }
        } else if (context.startsWith("character_") || context.contains("_character")) {
            String path = "characters/" + context + ".png";
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
                if (resource.exists()) {
                    return path;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", path);
            }
        } else if (context.startsWith("location_") || context.contains("_location")) {
            String path = "locations/" + context + ".png";
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
                if (resource.exists()) {
                    return path;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", path);
            }
        }

        // If we can't determine the type, try different paths
        String[] possiblePaths = {
            "backgrounds/" + context + ".png",
            "characters/" + context + ".png",
            "locations/" + context + ".png",
            context + ".png"
        };

        for (String path : possiblePaths) {
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
                if (resource.exists()) {
                    log.debug("Found image at path: {}", path);
                    return path;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", path);
            }

            // Fallback to file system check
            Path fullPath = Paths.get(assetsBasePath, path);
            if (Files.exists(fullPath)) {
                log.debug("Found image at path (file system): {}", path);
                return path;
            }
        }

        log.debug("No image found for context: {}", context);
        return null;
    }

    /**
     * Handle complex context strings that contain multiple parts.
     * Examples: "location:tokyo", "character:hero:angry", "background:school:night"
     * 
     * @param context the complex context string
     * @return the image path, or null if not found
     */
    private String handleComplexContext(String context) {
        String[] parts = context.split(":");
        if (parts.length < 2) {
            return null;
        }

        String type = parts[0].toLowerCase();
        String id = parts[1];
        String variant = parts.length > 2 ? parts[2] : null;

        log.debug("Complex context - Type: {}, ID: {}, Variant: {}", type, id, variant);

        // Handle different types
        switch (type) {
            case "background":
                return findBackgroundImage(id, variant);
            case "character":
                return findCharacterImage(id, variant);
            case "location":
                return findLocationImage(id, variant);
            default:
                log.warn("Unknown asset type in context: {}", type);
                return null;
        }
    }

    /**
     * Find a background image based on ID and variant.
     * 
     * @param id the background ID
     * @param variant the variant (e.g., "night", "day", "rain")
     * @return the image path, or null if not found
     */
    private String findBackgroundImage(String id, String variant) {
        // Try with variant first
        if (variant != null) {
            String variantKey = id + "_" + variant;
            if (backgroundImages.containsKey(variantKey)) {
                return backgroundImages.get(variantKey);
            }

            // Try the path directly
            String variantPath = "backgrounds/" + id + "_" + variant + ".png";
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + variantPath);
                if (resource.exists()) {
                    return variantPath;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", variantPath);
            }
        }

        // Try without variant
        if (backgroundImages.containsKey(id)) {
            return backgroundImages.get(id);
        }

        // Try the path directly
        String path = "backgrounds/" + id + ".png";
        try {
            Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
            if (resource.exists()) {
                return path;
            }
        } catch (Exception e) {
            log.debug("Resource not found in classpath: {}", path);
        }

        return null;
    }

    /**
     * Find a character image based on ID and variant.
     * 
     * @param id the character ID
     * @param variant the variant (e.g., "angry", "happy", "sad")
     * @return the image path, or null if not found
     */
    private String findCharacterImage(String id, String variant) {
        // Try with variant first
        if (variant != null) {
            String variantKey = id + "_" + variant;
            if (characterImages.containsKey(variantKey)) {
                return characterImages.get(variantKey);
            }

            // Try the path directly
            String variantPath = "characters/" + id + "_" + variant + ".png";
            if (Files.exists(Paths.get(assetsBasePath, variantPath))) {
                return variantPath;
            }
        }

        // Try without variant
        if (characterImages.containsKey(id)) {
            return characterImages.get(id);
        }

        // Try the path directly
        String path = "characters/" + id + ".png";
        if (Files.exists(Paths.get(assetsBasePath, path))) {
            return path;
        }

        return null;
    }

    /**
     * Find a location image based on ID and variant.
     * 
     * @param id the location ID
     * @param variant the variant (e.g., "interior", "exterior")
     * @return the image path, or null if not found
     */
    private String findLocationImage(String id, String variant) {
        // Try with variant first
        if (variant != null) {
            String variantKey = id + "_" + variant;
            if (locationImages.containsKey(variantKey)) {
                return locationImages.get(variantKey);
            }

            // Try the path directly
            String variantPath = "locations/" + id + "_" + variant + ".png";
            try {
                Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + variantPath);
                if (resource.exists()) {
                    return variantPath;
                }
            } catch (Exception e) {
                log.debug("Resource not found in classpath: {}", variantPath);
            }
        }

        // Try without variant
        if (locationImages.containsKey(id)) {
            return locationImages.get(id);
        }

        // Try the path directly
        String path = "locations/" + id + ".png";
        try {
            Resource resource = resourceLoader.getResource("classpath:" + assetsBasePath + "/" + path);
            if (resource.exists()) {
                return path;
            }
        } catch (Exception e) {
            log.debug("Resource not found in classpath: {}", path);
        }

        return null;
    }

    /**
     * Create a URL from an image path.
     * 
     * @param imagePath the image path
     * @return the URL
     * @throws MalformedURLException if the URL is malformed
     */
    private URL createUrl(String imagePath) throws MalformedURLException {
        if (assetsBaseUrl != null && !assetsBaseUrl.isEmpty()) {
            // If a base URL is provided, use it to create a web URL
            String urlString = assetsBaseUrl;
            if (!urlString.endsWith("/")) {
                urlString += "/";
            }
            urlString += imagePath;
            return new URL(urlString);
        } else {
            try {
                // Use ResourceLoader to load from classpath
                String resourcePath = assetsBasePath;
                if (resourcePath.startsWith("classpath:")) {
                    resourcePath = resourcePath.substring("classpath:".length());
                }
                if (!resourcePath.endsWith("/")) {
                    resourcePath += "/";
                }
                resourcePath += imagePath;

                Resource resource = resourceLoader.getResource("classpath:" + resourcePath);
                if (resource.exists()) {
                    return resource.getURL();
                } else {
                    log.warn("Resource not found: {}", resourcePath);
                    // Fallback to file URL if resource not found
                    Path path = Paths.get(assetsBasePath, imagePath);
                    return path.toUri().toURL();
                }
            } catch (IOException e) {
                log.error("Error loading resource: {}", imagePath, e);
                // Fallback to file URL
                Path path = Paths.get(assetsBasePath, imagePath);
                return path.toUri().toURL();
            }
        }
    }
}
