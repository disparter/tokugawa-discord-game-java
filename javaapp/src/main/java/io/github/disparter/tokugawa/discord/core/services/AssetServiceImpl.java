package io.github.disparter.tokugawa.discord.core.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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
@Service
public class AssetServiceImpl implements AssetService {

    private static final Logger logger = LoggerFactory.getLogger(AssetServiceImpl.class);

    @Value("${assets.base.path:assets}")
    private String assetsBasePath;

    @Value("${assets.base.url:}")
    private String assetsBaseUrl;

    // Maps for different asset types
    private final Map<String, String> backgroundImages = new HashMap<>();
    private final Map<String, String> characterImages = new HashMap<>();
    private final Map<String, String> locationImages = new HashMap<>();

    // Fallback images
    private final Map<String, String> fallbackImages = new HashMap<>();

    public AssetServiceImpl() {
        initializeFallbackImages();
    }

    /**
     * Initialize the fallback images for different asset types.
     */
    private void initializeFallbackImages() {
        fallbackImages.put("background", "backgrounds/default_background.jpg");
        fallbackImages.put("character", "characters/default_character.png");
        fallbackImages.put("location", "locations/default_location.jpg");
        fallbackImages.put("default", "default.png");
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
            logger.error("Error getting image URL for context: {}", context, e);
            try {
                return getFallbackImageUrl("default");
            } catch (Exception ex) {
                logger.error("Error getting fallback image URL", ex);
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
            logger.error("Error getting image file for context: {}", context, e);
            try {
                String fallbackPath = fallbackImages.get("default");
                Path path = Paths.get(assetsBasePath, fallbackPath);
                return path.toFile();
            } catch (Exception ex) {
                logger.error("Error getting fallback image file", ex);
                return null;
            }
        }
    }

    @Override
    public URL getBackgroundImageUrl(String location) {
        try {
            // Try to find the background image for the location
            String imagePath = backgroundImages.getOrDefault(location, 
                    "backgrounds/" + location + ".jpg");
            
            Path path = Paths.get(assetsBasePath, imagePath);
            if (Files.exists(path)) {
                return createUrl(imagePath);
            }
            
            // If no image is found, return the fallback background image
            return getFallbackImageUrl("background");
        } catch (Exception e) {
            logger.error("Error getting background image URL for location: {}", location, e);
            try {
                return getFallbackImageUrl("background");
            } catch (Exception ex) {
                logger.error("Error getting fallback background image URL", ex);
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
            logger.error("Error getting character image URL for character: {}", characterId, e);
            try {
                return getFallbackImageUrl("character");
            } catch (Exception ex) {
                logger.error("Error getting fallback character image URL", ex);
                return null;
            }
        }
    }

    @Override
    public URL getLocationImageUrl(String locationId) {
        try {
            // Try to find the location image
            String imagePath = locationImages.getOrDefault(locationId, 
                    "locations/" + locationId + ".jpg");
            
            Path path = Paths.get(assetsBasePath, imagePath);
            if (Files.exists(path)) {
                return createUrl(imagePath);
            }
            
            // If no image is found, return the fallback location image
            return getFallbackImageUrl("location");
        } catch (Exception e) {
            logger.error("Error getting location image URL for location: {}", locationId, e);
            try {
                return getFallbackImageUrl("location");
            } catch (Exception ex) {
                logger.error("Error getting fallback location image URL", ex);
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
            String fallbackPath = fallbackImages.getOrDefault(assetType, fallbackImages.get("default"));
            return createUrl(fallbackPath);
        } catch (Exception e) {
            logger.error("Error getting fallback image URL for asset type: {}", assetType, e);
            return null;
        }
    }

    /**
     * Find the image path based on the context.
     * 
     * @param context the context identifier
     * @return the image path, or null if not found
     */
    private String findImagePathByContext(String context) {
        // Check if the context is a location
        if (backgroundImages.containsKey(context)) {
            return backgroundImages.get(context);
        }
        
        // Check if the context is a character
        if (characterImages.containsKey(context)) {
            return characterImages.get(context);
        }
        
        // Check if the context is a location ID
        if (locationImages.containsKey(context)) {
            return locationImages.get(context);
        }
        
        // Try to infer the type from the context
        if (context.startsWith("background_") || context.contains("_background")) {
            return "backgrounds/" + context + ".jpg";
        } else if (context.startsWith("character_") || context.contains("_character")) {
            return "characters/" + context + ".png";
        } else if (context.startsWith("location_") || context.contains("_location")) {
            return "locations/" + context + ".jpg";
        }
        
        // If we can't determine the type, try different paths
        String[] possiblePaths = {
            "backgrounds/" + context + ".jpg",
            "characters/" + context + ".png",
            "locations/" + context + ".jpg",
            context + ".jpg",
            context + ".png"
        };
        
        for (String path : possiblePaths) {
            Path fullPath = Paths.get(assetsBasePath, path);
            if (Files.exists(fullPath)) {
                return path;
            }
        }
        
        // If no image is found, return null
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
            // Otherwise, create a file URL
            Path path = Paths.get(assetsBasePath, imagePath);
            return path.toUri().toURL();
        }
    }
}