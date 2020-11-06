package photos.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Set;

@Entity
@Table(name = "plants")
public class Plant {

    private static Logger logger = LoggerFactory.getLogger(Plant.class);

    @Id
    private String id_gbif;//id gbif
    @Column
    private String commonNames;// array.toString()
    @Column
    private String scientificNameFamily;//
    @Column
    private String scientificNameAuthorship;
    @Column
    private String scientificName;//Latin
    @Column
    private String webReferenceWiki;//

    @OneToMany(targetEntity=ImageFileWithMetadata.class, mappedBy = "plant", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private Set<ImageFileWithMetadata> images;// foreign key in database. One Plant = many Images

    @Transient
    private double score;//by search
    @Transient
    private boolean isTree = true;

    public Plant(double score, String scientificNameAuthorship, String scientificName, String commonNames, String scientificNameFamily, boolean isTree, String id_gbif) {
        this.score = score;
        this.scientificNameAuthorship = scientificNameAuthorship;
        this.scientificName = scientificName;
        this.commonNames = commonNames;
        this.scientificNameFamily = scientificNameFamily;
        this.isTree = isTree;
        this.id_gbif = id_gbif;
    }

    public Plant(String commonNames, String scientificNameFamily, String scientificNameAuthorship, String scientificName, boolean isTree, String id_gbif, String webReferenceWiki) {
        this.commonNames = commonNames;
        this.scientificNameFamily = scientificNameFamily;
        this.scientificNameAuthorship = scientificNameAuthorship;
        this.scientificName = scientificName;
        this.isTree = isTree;
        this.id_gbif = id_gbif;
        this.webReferenceWiki = webReferenceWiki;
    }

    public Plant() {
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getScientificNameAuthorship() {
        return scientificNameAuthorship;
    }

    public void setScientificNameAuthorship(String scientificNameAuthorship) {
        this.scientificNameAuthorship = scientificNameAuthorship;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getCommonNames() {
        return commonNames;
    }

    public void setCommonNames(String commonNames) {
        this.commonNames = commonNames;
    }

    public String getScientificNameFamily() {
        return scientificNameFamily;
    }

    public void setScientificNameFamily(String scientificNameFamily) {
        this.scientificNameFamily = scientificNameFamily;
    }

    public String getWebReferenceWiki() {
        return webReferenceWiki;
    }

    public void setWebReferenceWiki(String webReferenceWiki) {
        this.webReferenceWiki = webReferenceWiki;
    }

    public String getId_gbif() {
        return id_gbif;
    }

    public void setId_gbif(String id_gbif) {
        this.id_gbif = id_gbif;
    }

    public boolean isTree() {
        return isTree;
    }

    public void setTree(boolean tree) {
        isTree = tree;
    }

    public Set<ImageFileWithMetadata> getImages() {
        return images;
    }

    /**
     * find wiki-page by getScientificName and set it to field
     */
    public void findAndSetWikiByName() {
        String name = getScientificName();
        String refName = "https://en.wikipedia.org/wiki/" + name.replaceAll(" ", "_");
        try {
            URLConnection connection = new URL(refName).openConnection();
            connection.connect();
            setWebReferenceWiki(refName);
        } catch (final MalformedURLException e) {
            logger.error("Can't find wiki-page for " + name + ". MalformedURLException!" + e.getMessage());
        } catch (final IOException e) {
            logger.error("Can't find wiki-page for " + name + ". IOException!" + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return scientificName + ", nameAuthorship = " + scientificNameAuthorship + ". id_gbif = " + id_gbif;
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
