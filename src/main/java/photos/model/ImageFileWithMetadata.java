package photos.model;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDescriptor;
import com.drew.metadata.exif.GpsDirectory;
import config.GeoTransformations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Table(name = "pictures")
public class ImageFileWithMetadata {

    private static Logger logger = LoggerFactory.getLogger(ImageFileWithMetadata.class);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Transient
    private File file;
    @Column
    private String organ;
    @Column
    private LocalDate photosDate;
    @Column
    private String pathToPicture;//path to final directory (final pictures directory)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gbif")
    private Plant plant;//foreign key in database

    @Transient
    private Metadata metadata;
    @Transient
    private Double longitude;
    @Transient
    private Double latitude;

    public ImageFileWithMetadata(File file) throws ImageProcessingException {
        this.file = file;
        setAllTagesWeNeed();
    }

    public ImageFileWithMetadata() {
        this.latitude = -999999.99999;
        this.longitude = -999999.99999;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public LocalDate getPhotosDate() {
        return photosDate;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    public File getFile() {
        return file;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setPhotosDate(LocalDate photosDate) {
        this.photosDate = photosDate;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public String getPathToPicture() {
        return pathToPicture;
    }

    public void setPathToPicture(String pathToPicture) {
        this.pathToPicture = pathToPicture;
    }

    /**
     * setting fields by file-metadata
     */
    private void setAllTagesWeNeed() {
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        } catch (ImageProcessingException | IOException e) {
            logger.error("Error reading metadata: {}", e.getMessage());
            return;
        }
        if (metadata != null) {
            GpsDirectory gpsDir = metadata.getFirstDirectoryOfType(GpsDirectory.class);
            if (gpsDir != null) {
                GpsDescriptor gpsDesc = new GpsDescriptor(gpsDir);
                String strLongitude = gpsDesc.getGpsLongitudeDescription();
                String strLatitude = gpsDesc.getGpsLatitudeDescription();
                if (strLatitude.isEmpty()) {
                    latitude = -99999999.0;
                } else {
                    latitude = GeoTransformations.fromStringWithGradToDouble(strLatitude);
                    if (latitude == null) {// if representation without "°"
                        latitude = Double.parseDouble(strLatitude);
                    }
                }
                if (strLongitude.isEmpty()) {
                    longitude = -99999999.0;
                } else {
                    longitude = GeoTransformations.fromStringWithGradToDouble(strLongitude);

                    if (longitude == null) {// if representation without "°"
                        longitude = Double.parseDouble(strLongitude);
                    }
                }
            }
            Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
                if (date != null) {
                    photosDate = date.toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                }
            }
        }
    }

    @Override
    public String toString() {
        return "Image {" + (file==null? "No File!!!": file.getName()) +
                ", date = " + photosDate +
                ", longitude = " + longitude +
                ", latitude = " + latitude +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(getFile() == null) return false;
        ImageFileWithMetadata that = (ImageFileWithMetadata) o;
        return getFile().getAbsolutePath().equals(that.getFile().getAbsolutePath());
    }

    @Override
    public int hashCode() {
        return getLongitude().intValue();
    }
}
