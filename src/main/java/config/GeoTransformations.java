package config;

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
}
