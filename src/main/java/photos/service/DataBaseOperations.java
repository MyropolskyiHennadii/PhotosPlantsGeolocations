package photos.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.Geoposition;
import photos.model.ImageFileWithMetadata;
import photos.model.Plant;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import java.util.List;

public class DataBaseOperations {

    private static Logger logger = LoggerFactory.getLogger(DataBaseOperations.class);

    /**
     * write all records to db
     *
     * @param plant
     * @param imageFileWithMetadata
     */
    public void saveRecordsToDataBase(Plant plant, ImageFileWithMetadata imageFileWithMetadata) {
        if (plant == null) {
            logger.info("Can't save plant = null! Image {}", imageFileWithMetadata);
        } else {
            savePlant(plant);
            if (imageFileWithMetadata != null && imageFileWithMetadata.getPathToPicture() != null) {
                saveImage(imageFileWithMetadata);
            }
            saveGeoPosition(plant, imageFileWithMetadata);
        }
    }

    /**
     * save geopositions
     *
     * @param plant
     * @param imageFileWithMetadata
     */
    public void saveGeoPosition(Plant plant, ImageFileWithMetadata imageFileWithMetadata) {

        Geoposition geoposition = new Geoposition(imageFileWithMetadata.getLongitude(), imageFileWithMetadata.getLatitude(), plant);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("geopositions");
        EntityManager em = emf.createEntityManager();

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
    public void savePlant(Plant plant) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("plants");
        EntityManager em = emf.createEntityManager();

        if (em.find(Plant.class, plant.getId_gbif()) == null) {
            logger.info("Saving plant {}", plant);
            em.getTransaction().begin();
            em.persist(plant);
            em.getTransaction().commit();
        } else {
            logger.info("Can't save plant to DB. It is already exists. {}", plant.toString());
        }
    }

    /**
     * save reference to image
     *
     * @param imageFileWithMetadata
     */
    public void saveImage(ImageFileWithMetadata imageFileWithMetadata) {

        EntityManagerFactory emf = Persistence.createEntityManagerFactory("pictures");
        EntityManager em = emf.createEntityManager();

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

}
