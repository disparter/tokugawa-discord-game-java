package io.github.disparter.tokugawa.discord.core.services;

import java.io.File;
import java.net.URL;

/**
 * Service interface for managing game assets.
 */
public interface AssetService {

    /**
     * Get an image URL based on the context.
     *
     * @param context the context identifier (e.g., location, character, event)
     * @return the URL of the image
     */
    URL getImageUrl(String context);

    /**
     * Get an image file based on the context.
     *
     * @param context the context identifier (e.g., location, character, event)
     * @return the File object for the image
     */
    File getImageFile(String context);

    /**
     * Get a background image URL based on the location.
     *
     * @param location the location identifier
     * @return the URL of the background image
     */
    URL getBackgroundImageUrl(String location);

    /**
     * Get a character image URL based on the character ID.
     *
     * @param characterId the character identifier
     * @return the URL of the character image
     */
    URL getCharacterImageUrl(String characterId);

    /**
     * Get a location image URL based on the location ID.
     *
     * @param locationId the location identifier
     * @return the URL of the location image
     */
    URL getLocationImageUrl(String locationId);

    /**
     * Check if an asset exists for the given context.
     *
     * @param context the context identifier
     * @return true if the asset exists, false otherwise
     */
    boolean assetExists(String context);

    /**
     * Get the fallback image URL when a specific image is not found.
     *
     * @param assetType the type of asset (e.g., "background", "character", "location")
     * @return the URL of the fallback image
     */
    URL getFallbackImageUrl(String assetType);
}