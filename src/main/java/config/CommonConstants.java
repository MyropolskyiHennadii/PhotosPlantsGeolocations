package config;

import javafx.stage.Stage;

public class CommonConstants {

    private static CommonConstants instance;

    //main stage
    Stage stage;
    //map's rectangle
    private double minLongitude = 11.5061;
    private double maxLongitude = 11.537;
    private double minLatitude = 48.1198;
    private double maxLatitude = 48.1273;
    private boolean checkLongLat = true;
    //photo's directories
    private String inputDirectory = "";
    private String outputDirectory = "";
    private String preparedPhotosDirectory = "D:\\PreparedPhotoesForPlantsDB";
    //api PlantNet
    private String apiKeyPlantNet = "";

    private CommonConstants() {
    }

    public static synchronized CommonConstants getInstance() {
        if (instance == null) {
            instance = new CommonConstants();
        }
        return instance;
    }

    public boolean isCheckLongLat() {
        return checkLongLat;
    }

    public double getMinLongitude() {
        return minLongitude;
    }

    public void setMinLongitude(double minLongitude) {
        this.minLongitude = minLongitude;
    }

    public double getMaxLongitude() {
        return maxLongitude;
    }

    public void setMaxLongitude(double maxLongitude) {
        this.maxLongitude = maxLongitude;
    }

    public double getMinLatitude() {
        return minLatitude;
    }

    public void setMinLatitude(double minLatitude) {
        this.minLatitude = minLatitude;
    }

    public double getMaxLatitude() {
        return maxLatitude;
    }

    public void setMaxLatitude(double maxLatitude) {
        this.maxLatitude = maxLatitude;
    }

    public String getInputDirectory() {
        return inputDirectory;
    }

    public void setInputDirectory(String inputDirectory) {
        this.inputDirectory = inputDirectory;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(String outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getPreparedPhotosDirectory() {
        return preparedPhotosDirectory;
    }

    public void setPreparedPhotosDirectory(String preparedPhotosDirectory) {
        this.preparedPhotosDirectory = preparedPhotosDirectory;
    }

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public String getApiKeyPlantNet() {
        return apiKeyPlantNet;
    }
}
