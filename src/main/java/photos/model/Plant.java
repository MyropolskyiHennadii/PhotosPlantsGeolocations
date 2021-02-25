package photos.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "plants")
public class Plant {

    private static Logger logger = LoggerFactory.getLogger(Plant.class);

    @Id
    private String id_gbif;//id gbif
    @Column
    private String common_names;// array.toString()
    @Column
    private String scientific_name_family;//
    @Column
    private String scientific_name_authorship;
    @Column
    private String scientific_name;//Latin
    @Column
    private String web_reference_wiki;//
    @Column
    private String kind = "Tree";
    @Column
    private int show_only_flowering = 0;//if Yes == 1
    @Column
    private int updated;//1 = was updated, 0 = wasn't
    @Column
    private int deleted;//1 = was marked as deleted, 0 = wasn't

    @OneToMany(targetEntity=ImageFileWithMetadata.class, mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference//!!! important to prevent infinite loop with json references
    private Set<ImageFileWithMetadata> images = new HashSet<>();// foreign key in database. One Plant = many Images

    @OneToMany(targetEntity=PlantsSynonym.class, mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference//!!! important to prevent infinite loop with json references
    private Set<PlantsSynonym> synonyms = new HashSet<>();// foreign key in database. One Plant = many synonyms

    @OneToMany(targetEntity=PlantsEvent.class, mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference//!!! important to prevent infinite loop with json references
    private Set<PlantsEvent> events = new HashSet<>();// foreign key in database. One Plant = many events

    @Transient
    private double score;//by search

    public Plant(double score, String scientific_name_authorship, String scientific_name, String common_names, String scientific_name_family, String kind, String id_gbif) {
        this.score = score;
        this.scientific_name_authorship = scientific_name_authorship;
        this.scientific_name = scientific_name;
        this.common_names = common_names;
        this.scientific_name_family = scientific_name_family;
        this.kind = kind;
        this.id_gbif = id_gbif;
    }

    public Plant(String common_names, String scientific_name_family, String scientific_name_authorship, String scientific_name, String kind, String id_gbif, String web_reference_wiki) {
        this.common_names = common_names;
        this.scientific_name_family = scientific_name_family;
        this.scientific_name_authorship = scientific_name_authorship;
        this.scientific_name = scientific_name;
        this.kind = kind;
        this.id_gbif = id_gbif;
        this.web_reference_wiki = web_reference_wiki;
    }

    public Plant() {
        this.kind = "Tree";
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getScientific_name_authorship() {
        return scientific_name_authorship;
    }

    public void setScientific_name_authorship(String scientific_name_authorship) {
        this.scientific_name_authorship = scientific_name_authorship;
    }

    public String getScientific_name() {
        return scientific_name;
    }

    public void setScientific_name(String scientific_name) {
        this.scientific_name = scientific_name;
    }

    public String getCommon_names() {
        return common_names;
    }

    public void setCommon_names(String common_names) {
        this.common_names = common_names;
    }

    public String getScientific_name_family() {
        return scientific_name_family;
    }

    public void setScientific_name_family(String scientific_name_family) {
        this.scientific_name_family = scientific_name_family;
    }

    public String getWeb_reference_wiki() {
        return web_reference_wiki;
    }

    public void setWeb_reference_wiki(String web_reference_wiki) {
        this.web_reference_wiki = web_reference_wiki;
    }

    public String getId_gbif() {
        return id_gbif;
    }

    public void setId_gbif(String id_gbif) {
        this.id_gbif = id_gbif;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getShow_only_flowering() {
        return show_only_flowering;
    }

    public void setShow_only_flowering(int show_only_flowering) {
        this.show_only_flowering = show_only_flowering;
    }

    public Set<ImageFileWithMetadata> getImages() {
        return images;
    }

    public void setImages(Set<ImageFileWithMetadata> images) {
        this.images = images;
    }

    public Set<PlantsSynonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(Set<PlantsSynonym> synonyms) {
        this.synonyms = synonyms;
    }

    public Set<PlantsEvent> getEvents() {
        return events;
    }

    public void setEvents(Set<PlantsEvent> events) {
        this.events = events;
    }

    public int getUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    public int getDeleted() {
        return deleted;
    }

    public void setDeleted(int deleted) {
        this.deleted = deleted;
    }

    /**
     * find wiki-page by getScientificName and set it to field
     */
    public void findAndSetWikiByName() {
        String name = getScientific_name();
        String refName = "https://en.wikipedia.org/wiki/" + name.replaceAll(" ", "_");
        try {
            URLConnection connection = new URL(refName).openConnection();
            connection.connect();
            setWeb_reference_wiki(refName);
        } catch (final MalformedURLException e) {
            logger.error("Can't find wiki-page for " + name + ". MalformedURLException!" + e.getMessage());
        } catch (final IOException e) {
            logger.error("Can't find wiki-page for " + name + ". IOException!" + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return scientific_name + ", nameAuthorship = " + scientific_name_authorship + ". id_gbif = " + id_gbif;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Plant plant = (Plant) o;
        return getId_gbif().equals(plant.getId_gbif());
    }

    @Override
    public int hashCode() {
        return getId_gbif().length();
    }
}
