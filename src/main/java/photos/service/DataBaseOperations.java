package photos.service;

import config.CommonConstants;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.Geoposition;
import photos.model.ImageFileWithMetadata;
import photos.model.Plant;
import photos.model.PlantsSynonym;

import javax.persistence.*;
import java.io.IOException;
import java.util.List;

public class DataBaseOperations {

    private static Logger logger = LoggerFactory.getLogger(DataBaseOperations.class);
    private static DataBaseOperations instance;

    private EntityManagerFactory emfGeoposition = Persistence.createEntityManagerFactory("geopositions");
    private EntityManagerFactory emfPlant = Persistence.createEntityManagerFactory("plants");
    private EntityManagerFactory emfImage = Persistence.createEntityManagerFactory("pictures");
    private EntityManagerFactory emfSynonym = Persistence.createEntityManagerFactory("plants_synonyms");

    private DataBaseOperations() {
    }

    public static synchronized DataBaseOperations getInstance() {
        if (instance == null) {
            instance = new DataBaseOperations();
        }
        return instance;
    }

    /**
     * get synonym from main (english) page
     *
     * @param doc
     */
    public static String getSynonymInLanguagesFromPage(Document doc, String lang) {
        String href = null;
        Element p_lang = doc.getElementById("p-lang");
        if (p_lang != null) {
            Elements links = p_lang.select("a[href]");
            for (Element link : links) {
                String langPage = link.attr("lang").trim();
                if (langPage.equals(lang)) {
                    //our case
                    href = link.attr("abs:href");
                }
            }
        } else {
            return null;
        }
        return href;
    }

    /**
     * get name of the plant
     *
     * @param doc html doc
     * @return
     */
    public static String getPlantsNameFromNativeWiki(Document doc) {
        Element content = null;
        Element mw_body = doc.getElementById("content");
        //.getElementsByClass("mw-body");
        if (mw_body != null) {//there are tags mw-body
            content = mw_body.getElementById("firstHeading");
        }
        if (content == null) {
            return "????";
        }
        return content.text();
    }

    /**
     * write all records to db
     *
     * @param plant
     * @param imageFileWithMetadata
     */
    public void createAllRecordsToDataBase(Plant plant, ImageFileWithMetadata imageFileWithMetadata) {
        if (plant == null) {
            logger.info("Can't save plant = null! Image {}", imageFileWithMetadata);
        } else {
            createPlant(plant);
            if (imageFileWithMetadata != null && imageFileWithMetadata.getPath_to_picture() != null) {
                createImage(imageFileWithMetadata);
            }
            createGeoPosition(plant, imageFileWithMetadata);
        }
    }

    /**
     * save geopositions
     *
     * @param plant
     * @param imageFileWithMetadata
     */
    public void createGeoPosition(Plant plant, ImageFileWithMetadata imageFileWithMetadata) {

        double longitude = imageFileWithMetadata.getLongitude();
        double latitude = imageFileWithMetadata.getLatitude();
        //check position in rectangle:
        if (longitude < CommonConstants.getInstance().getMinLongitude()
                || longitude > CommonConstants.getInstance().getMaxLongitude()
                || latitude < CommonConstants.getInstance().getMinLatitude()
                || latitude > CommonConstants.getInstance().getMaxLatitude()) {
            logger.error("Coordinates out of bounds! {}", imageFileWithMetadata.toString());
            return;
        }

        Geoposition geoposition = new Geoposition(longitude, latitude, plant);
        EntityManager em = emfGeoposition.createEntityManager();

        //if there are geoposition for this plant and with same coordinates = no save
        TypedQuery<Geoposition> query = em.createQuery(
                "SELECT e FROM Geoposition e WHERE e.plant.id_gbif = '" + plant.getId_gbif() + "' AND e.longitude = '" +
                        imageFileWithMetadata.getLongitude() + "' AND e.latitude = '" + imageFileWithMetadata.getLatitude() + "'", Geoposition.class);
        List<Geoposition> geopositions = query.getResultList();

        if (geopositions.size() == 0) {
            logger.info("Saving geoposition {}, {}", plant, imageFileWithMetadata);
            em.getTransaction().begin();
            em.persist(geoposition);
            em.getTransaction().commit();
        } else {
            logger.info("Can't save geoposition. There is geoposition {} for plant {}", geoposition, plant);
        }
    }

    /**
     * save plant
     *
     * @param plant
     */
    public void createPlant(Plant plant) {
        EntityManager em = emfPlant.createEntityManager();

        if (em.find(Plant.class, plant.getId_gbif()) == null) {
            logger.info("Saving plant {}", plant);
            em.getTransaction().begin();
            em.persist(plant);
            em.getTransaction().commit();
            //synonyms:
            refreshPlantSynonyms(plant, CommonConstants.getInstance().getLanguages());
        } else {
            logger.info("Can't save plant to DB. It is already exists. {}", plant.toString());
        }
    }

    /**
     * save reference to image
     *
     * @param imageFileWithMetadata
     */
    public void createImage(ImageFileWithMetadata imageFileWithMetadata) {

        EntityManager em = emfImage.createEntityManager();

        Plant empPlant = imageFileWithMetadata.getPlant();

        //if there are images for this plant and with same organ = no save
        TypedQuery<ImageFileWithMetadata> query = em.createQuery(
                "SELECT e FROM ImageFileWithMetadata e WHERE e.plant.id_gbif = '" + empPlant.getId_gbif()
                        + "' AND e.organ = '" + imageFileWithMetadata.getOrgan() + "'", ImageFileWithMetadata.class);
        List<ImageFileWithMetadata> images = query.getResultList();

        if (images.size() == 0) {
            logger.info("Saving image {}", imageFileWithMetadata);
            em.getTransaction().begin();
            em.persist(imageFileWithMetadata);
            em.getTransaction().commit();
        } else {
            logger.info("Can't save image. There are already images with organ {} for plant {}", imageFileWithMetadata.getOrgan(), empPlant);
        }
    }

    /**
     * find plant by id_gbif
     *
     * @param id_gbif
     * @return
     */
    public List<Plant> findPlantById_gbif(String id_gbif) {

        EntityManager em = emfPlant.createEntityManager();

        TypedQuery<Plant> query = em.createQuery(
                "SELECT e FROM Plant e WHERE e.id_gbif = '" + id_gbif + "'", Plant.class);
        return query.getResultList();
    }

    /**
     * find records in Plants be name/part of name
     *
     * @param name
     * @return
     */
    public List<Plant> findPlantByName(String name) {
        EntityManager em = emfPlant.createEntityManager();
        Query query = em.createQuery("SELECT e FROM Plant e "
                + "WHERE e.scientific_name LIKE CONCAT('%',?1,'%')", Plant.class);
        query.setParameter(1, name);
        return query.getResultList();
    }

    /**
     * find nearest locations with plants to defined position
     *
     * @param longitude
     * @param latitude
     * @return
     */
    public List<Geoposition> findNearestPlants(double longitude, double latitude) {
        double tolerableLimit = CommonConstants.getInstance().getTolerableLimit();
        EntityManager em = emfGeoposition.createEntityManager();

        TypedQuery<Geoposition> query = em.createQuery(
                "SELECT e FROM Geoposition e WHERE " +
                        "ABS(100000*(e.longitude - '" + longitude + "'))<='" + tolerableLimit
                        + "' AND " +
                        "ABS(100000*(e.latitude - '" + latitude + "'))<='" + tolerableLimit + "'",
                Geoposition.class);

        return query.getResultList();
    }

    /**
     * return list with all the plants
     *
     * @return
     */
    public List<Plant> findAllPlants() {
        EntityManager em = emfPlant.createEntityManager();
        Query query = em.createQuery("SELECT e FROM Plant e ", Plant.class);
        return query.getResultList();
    }

    /**
     * find synonym of plant in language lang
     *
     * @param p
     * @param lang
     * @return
     */
    public PlantsSynonym findPlantsSynonymByLang(Plant p, String lang) {
        EntityManager em = emfSynonym.createEntityManager();
        TypedQuery<PlantsSynonym> query = em.createQuery(
                "SELECT e FROM PlantsSynonym e WHERE e.plant.id_gbif = '"
                        + p.getId_gbif() + "' AND e.lang = '"
                        + lang + "'", PlantsSynonym.class);
        List<PlantsSynonym> find = query.getResultList();
        if (find.size() == 0) {
            return null;
        }
        return find.get(0);
    }

    /**
     * refresh synonyms for plants in languages arrLanguages
     *
     * @param p
     * @param arrLanguages
     */
    public void refreshPlantSynonyms(Plant p, String[] arrLanguages) {
        EntityManager em = emfSynonym.createEntityManager();
        for (String lang : arrLanguages) {
            try {
                //english wiki:
                Document doc = Jsoup.connect(p.getWeb_reference_wiki()).get();
                //ref to wikipage in lang:
                String href_lang = getSynonymInLanguagesFromPage(doc, lang);
                if (href_lang != null) {
                    //string with synonym-name in lang
                    String strSynonym = getPlantsNameFromNativeWiki(Jsoup.connect(href_lang).get());
                    if (strSynonym != null) {
                        PlantsSynonym plantsSynonym = new PlantsSynonym(p, lang, strSynonym, href_lang);
                        PlantsSynonym existingSynonym = findPlantsSynonymByLang(p, lang);
                        if (existingSynonym == null) {
                            //add record
                            logger.info("Creating synonyms {}", plantsSynonym);
                            em.getTransaction().begin();
                            em.persist(plantsSynonym);
                            em.getTransaction().commit();
                        } else {
                            //update record
                            logger.info("Updating synonyms {}", existingSynonym);
                            em.getTransaction().begin();
                            existingSynonym.setWeb_reference_wiki(href_lang);
                            existingSynonym.setLang_name(strSynonym);
                            em.persist(existingSynonym);
                            em.getTransaction().commit();
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Can't create www doc! {}", e.getMessage());
            }
        }
    }

    /**
     * find geoposition by id
     * @param id
     * @return
     */
    public Geoposition findGeopositionByID(int id){
        EntityManager em = emfGeoposition.createEntityManager();
        TypedQuery<Geoposition> query = em.createQuery(
                "SELECT e FROM Geoposition e WHERE e.id = '"
                        + id + "'", Geoposition.class);
        Geoposition find = query.getResultList().get(0);
        return find;
    }

    /**
     * update existing record with new plant
     *
     * @param geoposition
     */
    public void updateGeopositionWithCorrectPlant(Geoposition geoposition, Plant plant) {
        EntityManager em = emfGeoposition.createEntityManager();
        Geoposition oldPosition = em.find(Geoposition.class, geoposition.getId());
        if (oldPosition != null && geoposition.getPlant() != null) {
            logger.info("Updating geoposition {}", geoposition);
            em.getTransaction().begin();
            oldPosition.setPlant(plant);
            em.persist(oldPosition);
            em.getTransaction().commit();
        } else {
            logger.info("Can't update geoposition {}", geoposition);
        }
    }

    /**
     * update existing record with new location
     *
     * @param id geoposition, shift longitude and latitude in meter
     */
    public void updateGeopositionWithCorrectLocation(long id, double shiftLongitude, double shiftLatitude) {
        EntityManager em = emfGeoposition.createEntityManager();
        Geoposition oldPosition = em.find(Geoposition.class, id);
        if (oldPosition != null) {
            logger.info("Updating geoposition {}", oldPosition);
            em.getTransaction().begin();
            oldPosition.setLatitude(oldPosition.getLatitude() + 0.000025 * shiftLatitude/1.77);
            oldPosition.setLongitude(oldPosition.getLongitude() + 0.000025 * shiftLongitude/1.77);
            em.persist(oldPosition);
            em.getTransaction().commit();
        } else {
            logger.info("Can't update geoposition {}", oldPosition);
        }
    }

    /**
     * all pictures
     * @return
     */
    public List<ImageFileWithMetadata> findAllImages(){
        EntityManager em = emfImage.createEntityManager();
        TypedQuery<ImageFileWithMetadata> query = em.createQuery(
                "SELECT e FROM ImageFileWithMetadata e", ImageFileWithMetadata.class);
        return query.getResultList();
    }

    /**
     * corricting record in pictures by id
     * @param id
     * @param newPath
     */
    public void correctImageFileWithPath(int id, String newPath){
        EntityManager em = emfImage.createEntityManager();
        ImageFileWithMetadata imFile = em.find(ImageFileWithMetadata.class, id);
        em.getTransaction().begin();
        imFile.setPath_to_picture(newPath);
        em.persist(imFile);
        em.getTransaction().commit();
    }
}
