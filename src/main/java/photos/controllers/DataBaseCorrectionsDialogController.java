package photos.controllers;

import config.CommonConstants;
import config.GeoTransformations;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.Geoposition;
import photos.model.ImageFileWithMetadata;
import photos.model.Plant;
import photos.service.DataBaseOperations;
import photos.service.ImageFilesOperations;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class DataBaseCorrectionsDialogController {

    private static Logger logger = LoggerFactory.getLogger(ManageIncomingPhotosController.class);

    private static List<ImageFileWithMetadata> listPhotosSample = new ArrayList<>();//list with image's files
    private static int currentIndPhotoInListSample = 0;//current index photo in list

    //tab1
    public Button buttonFindPlantID_tab1;
    public TextField textFieldPlantID_tab1;
    public Label labelCurrentPlant_tab1;
    public TextField textFieldPhotosDirectory_tab1;
    public Button buttonInputDirectory_tab1;
    public ImageView imageView_tab1;
    public Button buttonPrevious_tab1;
    public Button buttonNext_tab1;
    public Button buttonRecordToDatabase_tab1;
    public Label labelImageInfo_tab1;
    public Label labelMistake_tab1;
    public Button buttonDeleteFile_tab1;
    public ListView listView_tab1;
    public Button buttonFindInDataBase_tab1;

    //tab2
    public WebView webView_tab2;
    public TextField textFieldSchiftLatitude_tab2;
    public TextField textFieldSchiftLongitude_tab2;
    public Button buttonRefreshMap_tab2;
    public Button buttonPutSchiftToImage_tab2;
    public TextField textFieldGeopositionID_tab2;
    public Label labelInfoPositionByID_tab2;
    public Button buttonShowPositionByID_tab2;
    public Button buttonUpdateGeopositionInDB_tab2;
    public Label labelInfo_tab2;

    //tab3
    public Button buttonFindPlantID_tab3;
    public TextField textFieldPlantID_tab3;
    public Label labelCurrentPlant_tab3;
    public Button buttonFindInDataBase_tab3;
    public TextField textFieldFromDate_tab3;
    public TextField textFieldFromMonth_tab3;
    public TextField textFieldToDate_tab3;
    public TextField textFieldToMonth_tab3;
    public Button buttonWriteEventToDB_tab3;
    public Button buttonRefreshAllLanguages_tab3;
    public Label labelInfoRefreshSynonyms_tab3;

    private Plant currentPlantSample_tab1;
    private Plant currentPlantSample_tab3;
    private DataBaseOperations dataBaseOperations = DataBaseOperations.getInstance();
    //for nearest plants (we do need, if there is mistake in recognition
    //and we want to replace record in database with correct one
    private List<Geoposition> nearestGeopositions = new ArrayList<>();


    /**
     * check nearest trees: may be the mistake with recognition and we want to replace record with current plant
     */
    public void refreshListView_tab1(ImageFileWithMetadata imageFile) {
        if (imageFile != null) {
            if (imageFile.getLatitude() != null && imageFile.getLongitude() != null) {
                if (imageFile.getLongitude() > 0 && imageFile.getLatitude() > 0) {
                    nearestGeopositions = dataBaseOperations.findNearestPlants(imageFile.getLongitude(), imageFile.getLatitude());
                    List<String> tempGeo = new ArrayList<>();
                    for (Geoposition position : nearestGeopositions) {
                        tempGeo.add("ID=" + position.getId() +
                                ". Distance=" + GeoTransformations.getDistance(position, imageFile.getLongitude(), imageFile.getLatitude())
                                + " m. " + position.getPlant().getScientific_name());
                    }
                    ObservableList items = FXCollections.observableArrayList(tempGeo);
                    listView_tab1.setItems(items);
                } else {
                    listView_tab1.setItems(null);
                    nearestGeopositions = null;
                }
            } else {
                listView_tab1.setItems(null);
                nearestGeopositions = null;
            }
        } else {
            listView_tab1.setItems(null);
            nearestGeopositions = null;
        }
    }

    /**
     * setting image to tab1 by string path to image
     */
    public void setImageView_tab1() {
        if (listPhotosSample.size() > 0) {
            ImageFileWithMetadata imageFile = listPhotosSample.get(currentIndPhotoInListSample);
            try (FileInputStream fileInputStream = new FileInputStream(imageFile.getFile().getAbsoluteFile())) {
                Image image = new Image(fileInputStream);
                imageView_tab1.setImage(image);
                labelImageInfo_tab1.setText(imageFile.toString());
                //check nearest trees: may be the mistake with recognition and we have to replace record with current plant
                refreshListView_tab1(imageFile);
            } catch (IOException e) {
                imageView_tab1.setImage(null);
                labelImageInfo_tab1.setText("");
                refreshListView_tab1(null);
            }
        } else {
            imageView_tab1.setImage(null);
            labelImageInfo_tab1.setText("");
            currentPlantSample_tab1 = null;
            currentIndPhotoInListSample = 0;
            refreshListView_tab1(null);
        }
    }

    /**
     * refreshing list with files and view at the tab1
     */
    public void refreshListPhotoAndViewImage_tab1(String directory) {
        listPhotosSample.clear();
        ImageFilesOperations.getListImageFilesFromDirectory(directory, true, listPhotosSample);
        currentIndPhotoInListSample = 0;
        setImageView_tab1();
    }

    /**
     * by selecting tab1
     */
    public void handleOnSelectionChanged_tab1() {

    }

    /**
     * find in database plant by ID
     */
    public void handleButtonFindPlantID_tab1() {

        if (textFieldPlantID_tab1.getText().isEmpty()) {
            labelCurrentPlant_tab1.setText("You have to define not empty ID!");
        } else {
            String PlantID = textFieldPlantID_tab1.getText().trim();
            List<Plant> listPlant = dataBaseOperations.findPlantById_gbif(PlantID);
            if (listPlant == null) {
                labelCurrentPlant_tab1.setText("NULL-answer from the database!");
            } else {
                if (listPlant.size() == 0) {
                    labelCurrentPlant_tab1.setText("There no such records in the database!");
                } else {
                    if (listPlant.size() > 1) {
                        logger.error("For plantID {} there are more then one record in the database!", PlantID);
                    }
                    currentPlantSample_tab1 = listPlant.get(0);
                    labelCurrentPlant_tab1.setText(currentPlantSample_tab1.toString());
                }
            }
        }
    }

    /**
     * select picture's directory on tab1
     */
    public void handleButtonInputDirectory_tab1() {
        byChoosingDirectory("group");
    }

    /**
     * change picture to previous image tab4
     */
    public void handleSchiftPicturePrev_tab1() {
        if (currentIndPhotoInListSample > 0) {
            currentIndPhotoInListSample--;
        } else {
            currentIndPhotoInListSample = listPhotosSample.size() > 0 ? listPhotosSample.size() - 1 : 0;
        }
        setImageView_tab1();
    }

    /**
     * change picture to next image tab4
     */
    public void handleSchiftPictureNext_tab1() {
        if (currentIndPhotoInListSample < listPhotosSample.size() - 1) {
            currentIndPhotoInListSample++;
        } else {
            currentIndPhotoInListSample = 0;
        }
        setImageView_tab1();
    }

    /**
     * delete file
     */
    public void handleButtonDeleteFile_tab1() {
        if (currentIndPhotoInListSample < listPhotosSample.size()) {
            ImageFileWithMetadata imFile = listPhotosSample.get(currentIndPhotoInListSample);
            if (imFile != null) {
                listPhotosSample.remove(currentIndPhotoInListSample);
                try {
                    ImageFilesOperations.deleteLastFileInDirectory(imFile.getFile());
                } catch (IOException e) {
                    logger.error("Can't delete file {}", imFile.getFile().getAbsolutePath());
                }
                currentIndPhotoInListSample = currentIndPhotoInListSample > 0 ? currentIndPhotoInListSample - 1 : currentIndPhotoInListSample;
            }
        }
        setImageView_tab1();
    }

    /**
     * record to the database tab1
     */
    public void handleButtonRecordToDatabase_tab1() {
        if (currentPlantSample_tab1 != null && listPhotosSample.size() > 0) {
            ImageFileWithMetadata imFile = listPhotosSample.get(currentIndPhotoInListSample);
            if (imFile != null) {
                if (imFile.getLatitude() > 0 || imFile.getLongitude() > 0) {
                    //writing to database and refresh controller's fields
                    dataBaseOperations.createGeoPosition(currentPlantSample_tab1, imFile);
                    //deleting file
                    handleButtonDeleteFile_tab1();
                }
            } else {
                labelMistake_tab1.setText("You have to select image-file!");
            }
        } else {
            labelMistake_tab1.setText("You have to select sample for plant (id) and be sure, that list of photos isn't empty!");
        }
    }

    /**
     * all records to database
     */
    public void handleButtonRecordAll_tab1() {
        if (currentPlantSample_tab1 != null && listPhotosSample.size() > 0) {
            for (int i = 0; i < listPhotosSample.size(); i++) {
                currentIndPhotoInListSample = i;
                handleButtonRecordToDatabase_tab1();
                logger.info("Adding group of photos, number {}", i);
            }
        }
        listPhotosSample.clear();
        currentIndPhotoInListSample = 0;
        nearestGeopositions = null;
        listView_tab1.setItems(null);
    }

    /**
     * by click on list view - replace record in DB with current plant
     */
    public void handleListView_tab1() {
        if (currentPlantSample_tab1 != null && listPhotosSample.size() > 0) {
            //ask and update
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Update record in the database");
            alert.setHeaderText("Record would be replace");
            alert.setContentText("Are you ok with this?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                // ... user chose OK
                dataBaseOperations.updateGeopositionWithCorrectPlant(nearestGeopositions.get(listView_tab1.getSelectionModel().getSelectedIndex()), currentPlantSample_tab1);
                handleButtonDeleteFile_tab1();
            }
        }
    }

    /**
     * select directory
     *
     * @param operation ("group" - dir for group's maintenance)
     */
    public void byChoosingDirectory(String operation) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory;
        switch (operation) {
            case "group":
                if (!textFieldPhotosDirectory_tab1.getText().isEmpty()) {
                    File defaultDirectory = new File(textFieldPhotosDirectory_tab1.getText());
                    directoryChooser.setInitialDirectory(defaultDirectory);
                }
                directoryChooser.setTitle("Choose Directory for Group Maintenance");
                selectedDirectory = directoryChooser.showDialog(CommonConstants.getInstance().getStage());
                if (selectedDirectory != null) {
                    textFieldPhotosDirectory_tab1.setText(selectedDirectory.getAbsolutePath());
                    refreshListPhotoAndViewImage_tab1(textFieldPhotosDirectory_tab1.getText());
                }
                break;
            default:
                break;
        }
    }

    /**
     * handle selected Plant from FindInDataBase
     *
     * @param plant
     */
    public void setPlantFromDB_tab1(Plant plant) {
        currentPlantSample_tab1 = plant;
        currentPlantSample_tab3 = plant;
        if (plant != null) {
            textFieldPlantID_tab1.setText(plant.getId_gbif());
            labelCurrentPlant_tab1.setText(plant.toString());
            textFieldPlantID_tab3.setText(plant.getId_gbif());
            labelCurrentPlant_tab3.setText(plant.toString());
        }
    }

    /**
     * find in database by name and so on
     */
    public void handleFindInDataBase_tab1() {
        Stage stageRecognition = new Stage();
        stageRecognition.setTitle("Find in Database");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FindInDataBase.fxml"));

        Region findInDB = null;
        try {
            findInDB = loader.load();
            FindInDataBaseController controller = loader.getController();
            controller.setInitialData(this);
            Scene scene = new Scene(findInDB);
            stageRecognition.setScene(scene);
            stageRecognition.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * by selecting tab2
     */
    public void handleOnSelectionChanged_tab2() {
        textFieldSchiftLatitude_tab2.setText("0.0");
        textFieldSchiftLongitude_tab2.setText("0.0");

        //text-fields with coordinates have to be double
        UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

            @Override
            public TextFormatter.Change apply(TextFormatter.Change t) {

                if (t.isReplaced())
                    if (t.getText().matches("[^0-9]"))
                        t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));


                if (t.isAdded()) {
                    String wholeText = t.getControlText();
                    if (wholeText.startsWith("-")) {//allow minus at start position
                        if (wholeText.contains(".")) {
                            if (t.getText().matches("[^0-9]")) {
                                t.setText("");
                            }
                        } else if (t.getText().matches("[^0-9.]")) {
                            t.setText("");
                        }
                    } else {
                        if (wholeText.contains(".")) {
                            if (t.getText().matches("[^0-9-]")) {
                                t.setText("");
                            }
                        } else if (t.getText().matches("[^0-9.-]")) {
                            t.setText("");
                        }
                    }
                }
                return t;
            }
        };

        textFieldSchiftLatitude_tab2.setTextFormatter(new TextFormatter<>(filter));
        textFieldSchiftLongitude_tab2.setTextFormatter(new TextFormatter<>(filter));

        refreshMap_tab2();
    }

    public void refreshMap_tab2() {

        webView_tab2.setVisible(true);
        double shiftLongitude = 0;
        double shiftLatitude = 0;
        if (!textFieldSchiftLongitude_tab2.getText().isEmpty()) {
            shiftLongitude = 0.000025 * Double.parseDouble(textFieldSchiftLongitude_tab2.getText()) / 1.77;
        }
        if (!textFieldSchiftLatitude_tab2.getText().isEmpty()) {
            shiftLatitude = 0.000025 * Double.parseDouble(textFieldSchiftLatitude_tab2.getText()) / 1.77;
        }
        String url = "https://www.openstreetmap.org";
        ImageFileWithMetadata imageFile = null;
        if (listPhotosSample.size() > 0) {
            imageFile = listPhotosSample.get(currentIndPhotoInListSample);
            if ((imageFile.getLongitude() != null) && (imageFile.getLatitude() != null)) {
                if (imageFile.getLongitude() > 0 && imageFile.getLatitude() > 0) {
                    url = "https://www.openstreetmap.org/?mlat=" +
                            (imageFile.getLatitude() + shiftLatitude) + "&amp;mlon=" +
                            (imageFile.getLongitude() + shiftLongitude) +
                            "#map=17/" + (imageFile.getLatitude() + shiftLatitude) + "/" + (imageFile.getLongitude() + shiftLongitude);
                }
            }
        }
        if (imageFile != null) {
            if (imageFile.getLatitude() != null && imageFile.getLongitude() != null) {
                WebEngine webEngine = webView_tab2.getEngine();
                webEngine.load(url);
            } else {
                webView_tab2.setVisible(false);
            }
        } else {
            webView_tab2.setVisible(false);
        }
    }

    /**
     * try to open picture with windows Paint tab2
     */
    public void handleMouthClickPicture_tab2() {
        if (currentIndPhotoInListSample <= listPhotosSample.size() - 1) {
            ImageFileWithMetadata image = listPhotosSample.get(currentIndPhotoInListSample);
            try {
                Runtime.getRuntime().exec(new String[]{"C:\\WINDOWS\\system32\\mspaint.exe", image.getFile().getAbsolutePath()});
            } catch (IOException e) {
                logger.error("Can't open picture with windows Paint {}", image.getFile().getAbsolutePath());
            }
        }
    }

    /**
     * refresh map after shift
     */
    public void handleButtonRefreshMap_tab2() {
        refreshMap_tab2();
    }

    /**
     * put shift to coordinates in object ImageFileWithMetadata (sometimes metadata isn't true)
     */
    public void handleButtonPutSchiftToImage_tab2() {
        if (currentIndPhotoInListSample < listPhotosSample.size()) {
            double shiftLongitude = 0;
            double shiftLatitude = 0;
            if (!textFieldSchiftLongitude_tab2.getText().isEmpty()) {
                shiftLongitude = GeoTransformations.getGradFromMeter(textFieldSchiftLongitude_tab2.getText());
            }
            if (!textFieldSchiftLatitude_tab2.getText().isEmpty()) {
                shiftLatitude = GeoTransformations.getGradFromMeter(textFieldSchiftLatitude_tab2.getText());
            }
            if (shiftLongitude != 0.0 || shiftLatitude != 0.0) {
                ImageFileWithMetadata imageFile = listPhotosSample.get(currentIndPhotoInListSample);
                imageFile.setLongitude(imageFile.getLongitude() + shiftLongitude);
                imageFile.setLatitude(imageFile.getLatitude() + shiftLatitude);
                handleOnSelectionChanged_tab2();
            }
        }
    }

    /**
     * schow info and position by ID of Geoposition
     */
    public void handleButtonShowInfoAndPositionByID_tab2() {
        String id = textFieldGeopositionID_tab2.getText().trim();
        if (!id.isEmpty()) {
            Geoposition geoposition = dataBaseOperations.findGeopositionByID(Integer.parseInt(id));
            if (geoposition != null) {
                webView_tab2.setVisible(true);
                double shiftLongitude = 0;
                double shiftLatitude = 0;
                if (!textFieldSchiftLongitude_tab2.getText().isEmpty()) {
                    shiftLongitude = GeoTransformations.getGradFromMeter(textFieldSchiftLongitude_tab2.getText());
                }
                if (!textFieldSchiftLatitude_tab2.getText().isEmpty()) {
                    shiftLatitude = GeoTransformations.getGradFromMeter(textFieldSchiftLatitude_tab2.getText());
                }
                String url = "https://www.openstreetmap.org/?mlat=" +
                        (geoposition.getLatitude() + shiftLatitude) + "&amp;mlon=" +
                        (geoposition.getLongitude() + shiftLongitude) +
                        "#map=17/" + (geoposition.getLatitude() + shiftLatitude) + "/" + (geoposition.getLongitude() + shiftLongitude);

                WebEngine webEngine = webView_tab2.getEngine();
                webEngine.load(url);
            }
        }
    }

    public void handleButtonUpdateGeopositionInDB_tab2() {
        Long id = Long.parseLong(textFieldGeopositionID_tab2.getText().trim());
        dataBaseOperations.updateGeopositionWithCorrectLocation(id, Double.parseDouble(textFieldSchiftLongitude_tab2.getText()), Double.parseDouble(textFieldSchiftLatitude_tab2.getText()));
        handleOnSelectionChanged_tab2();
        labelInfo_tab2.setText("Done");
    }

    /**
     * by selecting tab3
     */
    public void handleOnSelectionChanged_tab3() {

        textFieldFromDate_tab3.setText("1");
        textFieldFromMonth_tab3.setText("1");
        textFieldToDate_tab3.setText("1");
        textFieldToMonth_tab3.setText("1");

        //text-fields with coordinates have to be double
        UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

            @Override
            public TextFormatter.Change apply(TextFormatter.Change t) {

                if (t.isReplaced())
                    if (t.getText().matches("[^1-9]"))
                        t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));


                if (t.isAdded()) {
                    String wholeText = t.getControlText();
                    if (wholeText.startsWith("-")) {//allow minus at start position
                        if (wholeText.contains(".")) {
                            if (t.getText().matches("[^0-9]")) {
                                t.setText("");
                            }
                        } else if (t.getText().matches("[^0-9.]")) {
                            t.setText("");
                        }
                    } else {
                        if (wholeText.contains(".")) {
                            if (t.getText().matches("[^0-9-]")) {
                                t.setText("");
                            }
                        } else if (t.getText().matches("[^0-9.-]")) {
                            t.setText("");
                        }
                    }
                }
                return t;
            }
        };

        textFieldFromDate_tab3.setTextFormatter(new TextFormatter<>(filter));
        textFieldFromMonth_tab3.setTextFormatter(new TextFormatter<>(filter));
        textFieldToDate_tab3.setTextFormatter(new TextFormatter<>(filter));
        textFieldToMonth_tab3.setTextFormatter(new TextFormatter<>(filter));

        labelInfoRefreshSynonyms_tab3.setText("");
    }

    /**
     * find in database by name and so on
     */
    public void handleFindInDataBase_tab3() {
        handleFindInDataBase_tab1();
    }

    /**
     * find in database plant by ID tab3
     */
    public void handleButtonFindPlantID_tab3() {

        if (textFieldPlantID_tab3.getText().isEmpty()) {
            labelCurrentPlant_tab3.setText("You have to define not empty ID!");
        } else {
            String PlantID = textFieldPlantID_tab3.getText().trim();
            List<Plant> listPlant = dataBaseOperations.findPlantById_gbif(PlantID);
            if (listPlant == null) {
                labelCurrentPlant_tab3.setText("NULL-answer from the database!");
            } else {
                if (listPlant.size() == 0) {
                    labelCurrentPlant_tab3.setText("There no such records in the database!");
                } else {
                    if (listPlant.size() > 1) {
                        logger.error("For plantID {} there are more then one record in the database!", PlantID);
                    }
                    currentPlantSample_tab3 = listPlant.get(0);
                    labelCurrentPlant_tab3.setText(currentPlantSample_tab3.toString());
                }
            }
        }
    }

    /**
     * register event to DB (now - only flowering)
     */
    public void handleButtonWriteEventToDB_tab3() {
        if (currentPlantSample_tab3 != null) {
            dataBaseOperations.wtitePlantsEvent(currentPlantSample_tab3, "flowering", Integer.parseInt(textFieldFromDate_tab3.getText()), Integer.parseInt(textFieldFromMonth_tab3.getText()), Integer.parseInt(textFieldToDate_tab3.getText()), Integer.parseInt(textFieldToMonth_tab3.getText()));
            labelInfoRefreshSynonyms_tab3.setText("Done");
        } else {
            labelInfoRefreshSynonyms_tab3.setText("Undefined currentPlantSample_tab3");
        }
    }

    /**
     * refresh database with synonyms and wiki in different languages
     */
    public void handleButtonRefreshAllLanguages_tab3() {
        List<Plant> plants = dataBaseOperations.findAllPlants();
        for (Plant p : plants) {
            dataBaseOperations.refreshPlantSynonyms(p, CommonConstants.getInstance().getLanguages());
        }
        labelInfoRefreshSynonyms_tab3.setText("Done");
    }

}
