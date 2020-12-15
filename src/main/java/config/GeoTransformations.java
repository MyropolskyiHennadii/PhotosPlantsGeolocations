package config;

import photos.model.Geoposition;

import static java.lang.Math.sqrt;

public class GeoTransformations {

    /**
     * transform String coordinate (with grad) to double
     *
     * @param coordinate
     * @return
     */
    public static Double fromStringWithGradToDouble(String coordinate) {

        int grad = coordinate.indexOf("°");
        if (grad < 0) {
            return null;
        }
        Double transformedCoordinate = Double.parseDouble(coordinate.substring(0, grad));
        coordinate = coordinate.substring(grad + 1, coordinate.length()).trim();

        //minutes
        int minutes = coordinate.indexOf("'");
        if (minutes < 0) {
            return null;
        }
        double decimal = Double.parseDouble(coordinate.substring(0, minutes)) / 60;

        coordinate = coordinate.substring(minutes + 1, coordinate.length()).trim().replace(',', '.');
        coordinate = coordinate.substring(0, coordinate.length() - 1).trim();

        //seconds and minutes
        decimal += Double.parseDouble(coordinate) / 3600;

        //without infinite fraction
        String strDecimal = "" + decimal;
        if (strDecimal.length() > 10) {
            strDecimal = strDecimal.substring(0, 9);
        }

        return transformedCoordinate + Double.parseDouble(strDecimal);
    }

    /**
     * calculate distance between Geoposition and point with long and lat
     *
     * @param position
     * @param longitude
     * @param latitude
     * @return
     */
    public static double getDistance(Geoposition position, double longitude, double latitude) {
        return sqrt((100000 * position.getLongitude() - 100000 * longitude) * (100000 * position.getLongitude() - 100000 * longitude)
                + (100000 * position.getLatitude() - 100000 * latitude) * (100000 * position.getLatitude() - 100000 * latitude))*0.708;
    }

    /**
     * return grad from meter (meter in string form)
     * @param meter
     * @return
     */
    public static double getGradFromMeter(String meter){
        return 0.000025 * Double.parseDouble(meter) / 1.77;
    }
}
