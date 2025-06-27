package io.github.disparter.tokugawa.discord.core.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Entity representing a location in the game world.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    // Connected locations that players can move to
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_connections", joinColumns = @JoinColumn(name = "location_id"))
    private List<Long> connectedLocations = new ArrayList<>();

    // NPCs present at this location
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_npcs", joinColumns = @JoinColumn(name = "location_id"))
    private List<Long> npcs = new ArrayList<>();

    // Items that can be found at this location
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_items", joinColumns = @JoinColumn(name = "location_id"))
    private List<Long> items = new ArrayList<>();

    // Hidden areas within this location
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_hidden_areas", joinColumns = @JoinColumn(name = "location_id"))
    private List<String> hiddenAreas = new ArrayList<>();

    // Requirements to discover hidden areas (area name -> requirement description)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_discovery_requirements", joinColumns = @JoinColumn(name = "location_id"))
    private Map<String, String> discoveryRequirements = new HashMap<>();

    // Possible random events at this location
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "location_events", joinColumns = @JoinColumn(name = "location_id"))
    private List<Long> possibleEvents = new ArrayList<>();

    // Whether this location is initially accessible or needs to be unlocked
    private boolean locked = false;

    // Requirements to unlock this location (if locked)
    private String unlockRequirements;

    // Coordinates for map representation (optional)
    private Integer x;
    private Integer y;
}