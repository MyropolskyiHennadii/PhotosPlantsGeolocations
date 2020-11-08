package photos.model;

import javax.persistence.*;

@Entity
@Table(name = "geopositions")
public class Geoposition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column
    private double longitude;
    @Column
    private double latitude;
    @Column
    private String kind;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gbif")
    private Plant plant;//foreign key in database

    public Geoposition(double longitude, double latitude, Plant plant) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.kind = plant.getKindOfPlant();
        this.plant = plant;
    }

    public Geoposition() {
        longitude = - 9999999999999.99;
        latitude = - 9999999999999.99;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if(plant == null) return false;
        Geoposition that = (Geoposition) o;
        return Double.compare(that.getLongitude(), getLongitude()) == 0 &&
                Double.compare(that.getLatitude(), getLatitude()) == 0 &&
                getPlant().equals(that.getPlant());
    }

    @Override
    public int hashCode() {
        return (int)getLongitude();
    }
}
