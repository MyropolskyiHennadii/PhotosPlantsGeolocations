package photos.controllers;

import com.drew.imaging.ImageProcessingException;
import config.CommonConstants;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import photos.model.ImageFileWithMetadata;
import photos.model.Plant;
import photos.model.PlantNetQuery;
import photos.service.DataBaseOperations;
import photos.service.ImageFilesOperations;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ManageIncomingPhotosController {

    private static Logger logger = LoggerFactory.getLogger(ManageIncomingPhotosController.class);
    private static List<ImageFileWithMetadata> listPhotos = new ArrayList<>();//list with image's files
    private static int currentIndPhotoInList = 0;//current index photo in list
    //tab 1
    public DatePicker dateFrom_tab1;
    public DatePicker dateTo_tab1;
    public TextField inputDirectory_tab1;
    public TextField outputDirectory_tab1;
    public Button buttonInputDirectory_tab1;
    public Button buttonOutputDirectory_tab1;
    public Label labelMistake_tab1;
    public Button buttonMaintainFiles_tab1;
    public TextField textFieldMinLongitude_tab1;
    public TextField textFieldMaxLongitude_tab1;
    public TextField textFieldMinLatitude_tab1;
    public TextField textFieldMaxLatitude_tab1;
    //tab 2
    public TextField photosDirectory_tab2;
    public Button buttonPhotosDirectory_tab2;
    public CheckBox checkBoxIncludeSubdirectories_tab2;
    public Label labelCurrentImagePath_tab2;
    public ImageView imageView_tab2;
    public Button buttonPrevious_tab2;
    public Button buttonNext_tab2;
    public Button buttonDeletePicture_tab2;
    public Label labelOrgan_tab2;
    public ChoiceBox choiseBoxOrgan_tab2;
    public Button buttonSendQueryToPlantNet_tab2;
    public Label labelAnswerFromPlantNet_tab2;
    //tab3
    public ImageView imageView_tab3;
    public Label labeImagePath_tab3;
    public Label labePlant_tab3;
    public Label labelImageGeo_tab3;
    public Hyperlink hyperlinkWiki_tab3;
    public CheckBox checkBoxShowMap_tab3;
    public WebView webView_tab3;
    public Button buttonRecordToDB_tab3;
    public ComboBox comboBoxKindOfPlant_tab3;
    public CheckBox checkBoxShowOnlyFlowering_tab3;


    private Plant currentPlant;
    private DataBaseOperations dataBaseOperations = DataBaseOperations.getInstance();

    /**
     * by selecting tab1
     */
    public void handleOnSelectionChanged_tab1() {

        //text-fields with coordinates have to be double
        UnaryOperator<TextFormatter.Change> filter = new UnaryOperator<TextFormatter.Change>() {

            @Override
            public TextFormatter.Change apply(TextFormatter.Change t) {

                if (t.isReplaced())
                    if (t.getText().matches("[^0-9]"))
                        t.setText(t.getControlText().substring(t.getRangeStart(), t.getRangeEnd()));


                if (t.isAdded()) {
                    if (t.getControlText().contains(".")) {
                        if (t.getText().matches("[^0-9]")) {
                            t.setText("");
                        }
                    } else if (t.getText().matches("[^0-9.]")) {
                        t.setText("");
                    }
                }

                return t;
            }
        };

        textFieldMinLatitude_tab1.setTextFormatter(new TextFormatter<>(filter));
        textFieldMaxLatitude_tab1.setTextFormatter(new TextFormatter<>(filter));
        textFieldMinLongitude_tab1.setTextFormatter(new TextFormatter<>(filter));
        textFieldMaxLongitude_tab1.setTextFormatter(new TextFormatter<>(filter));

        //default values for coordinates
        if (textFieldMaxLatitude_tab1.getText().isEmpty() || textFieldMinLatitude_tab1.getText().isEmpty()) {
            textFieldMinLatitude_tab1.setText("" + CommonConstants.getInstance().getMinLatitude());
            textFieldMaxLatitude_tab1.setText("" + CommonConstants.getInstance().getMaxLatitude());
            textFieldMinLongitude_tab1.setText("" + CommonConstants.getInstance().getMinLongitude());
            textFieldMaxLongitude_tab1.setText("" + CommonConstants.getInstance().getMaxLongitude());
        }
    }

    /**
     * by changing fromDate tab1
     */
    public void handleFromDateChange_tab1() {
        clearMessages();
    }

    /**
     * by changig toDate tab1
     */
    public void handleToDateChange_tab1() {
        clearMessages();
    }

    /**
     * button choose input dir tab1
     */
    public void handleChooseInputDirectory_tab1() {
        clearMessages();
        byChoosingDirectory("input");
    }

    /**
     * button choose output dir tab1
     */
    public void handleChooseOutputDirectory_tab1() {
        clearMessages();
        byChoosingDirectory("output");
    }

    /**
     * button Maintain Files tab1
     *
     * @throws IOException
     * @throws ImageProcessingException
     */
    public void handleMaintainFiles_tab1() throws IOException, ImageProcessingException {

        if (checkData_tab1()) {
            labelMistake_tab1.setText("");
            searchAndCopyFiles();
            refreshListPhotoAndViewImage_tab2(photosDirectory_tab2.getText(), checkBoxIncludeSubdirectories_tab2.isSelected());
        }
    }

    /**
     * select directory
     *
     * @param operation ("input" -> input dir, "output" -> output dir, "photos" -> photo's dir)
     */
    public void byChoosingDirectory(String operation) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory;
        switch (operation) {
            case "input":
                directoryChooser.setTitle("Choose Input Directory");
                if (!inputDirectory_tab1.getText().isEmpty()) {
                    File defaultDirectory = new File(inputDirectory_tab1.getText());
                    directoryChooser.setInitialDirectory(defaultDirectory);
                }
                selectedDirectory = directoryChooser.showDialog(CommonConstants.getInstance().getStage());
                if (selectedDirectory != null) {
                    inputDirectory_tab1.setText(selectedDirectory.getAbsolutePath());
                    CommonConstants.getInstance().setInputDirectory(selectedDirectory.getAbsolutePath());
                }
                break;
            case "output":
                directoryChooser.setTitle("Choose Output Directory");
                if (!outputDirectory_tab1.getText().isEmpty()) {
                    File defaultDirectory = new File(outputDirectory_tab1.getText());
                    directoryChooser.setInitialDirectory(defaultDirectory);
                }
                selectedDirectory = directoryChooser.showDialog(CommonConstants.getInstance().getStage());
                if (selectedDirectory != null) {
                    outputDirectory_tab1.setText(selectedDirectory.getAbsolutePath());
                    photosDirectory_tab2.setText(selectedDirectory.getAbsolutePath());//because mostly the same with outputDirectory_tab1
                    CommonConstants.getInstance().setOutputDirectory(selectedDirectory.getAbsolutePath());
                }
                break;
            case "photos":
                directoryChooser.setTitle("Choose Photo's Directory");
                if (!photosDirectory_tab2.getText().isEmpty()) {
                    File defaultDirectory = new File(photosDirectory_tab2.getText());
                    directoryChooser.setInitialDirectory(defaultDirectory);
                }
                selectedDirectory = directoryChooser.showDialog(CommonConstants.getInstance().getStage());
                if (selectedDirectory != null) {
                    photosDirectory_tab2.setText(selectedDirectory.getAbsolutePath());
                    refreshListPhotoAndViewImage_tab2(photosDirectory_tab2.getText(), checkBoxIncludeSubdirectories_tab2.isSelected());
                }
                break;
            default:
                break;
        }
    }


    /**
     * copying files from input dir to output dir tab1
     *
     * @throws IOException
     */
    private void searchAndCopyFiles() throws IOException {

        CommonConstants.getInstance().setMinLongitude(Double.parseDouble(textFieldMinLongitude_tab1.getText()));
        CommonConstants.getInstance().setMaxLongitude(Double.parseDouble(textFieldMaxLongitude_tab1.getText()));
        CommonConstants.getInstance().setMinLatitude(Double.parseDouble(textFieldMinLatitude_tab1.getText()));
        CommonConstants.getInstance().setMaxLatitude(Double.parseDouble(textFieldMaxLatitude_tab1.getText()));

        ImageFilesOperations imageFilesOperations = new ImageFilesOperations(dateFrom_tab1.getValue(), dateTo_tab1.getValue(),
                CommonConstants.getInstance().getInputDirectory().trim(),
                CommonConstants.getInstance().getOutputDirectory().trim());
        imageFilesOperations.readInputFilesCreateOutputFiles();

        checkBoxIncludeSubdirectories_tab2.setSelected(true);

        labelMistake_tab1.setText("Done");
    }

    /**
     * check data entry tab1
     *
     * @return true, if it's ok
     */
    private boolean checkData_tab1() {
        boolean ok = true;

        if (!new File(inputDirectory_tab1.getText().trim()).isDirectory()) {
            labelMistake_tab1.setText("There is no such input directory!");
            return false;
        }
        if (!new File(outputDirectory_tab1.getText().trim()).isDirectory()) {
            labelMistake_tab1.setText("There is no such output directory!");
            return false;
        }
        if (dateFrom_tab1.getValue() != null && dateTo_tab1.getValue() != null) {
            if (dateTo_tab1.getValue().isBefore(dateFrom_tab1.getValue())) {
                labelMistake_tab1.setText("The date TO can't be before the date FROM!");
                return false;
            }
        }
        if (textFieldMaxLongitude_tab1.getText().isEmpty() || textFieldMinLongitude_tab1.getText().isEmpty()
                || textFieldMinLatitude_tab1.getText().isEmpty() || textFieldMaxLatitude_tab1.getText().isEmpty()) {
            labelMistake_tab1.setText("Coordinates can't be empty!");
            return false;
        }
        return ok;
    }

    /**
     * clear form from messages and so on - tab1
     */
    public void clearMessages() {
        labelMistake_tab1.setText("");
        labeImagePath_tab3.setText("");
        labePlant_tab3.setText("");
        labelImageGeo_tab3.setText("");
        hyperlinkWiki_tab3.setText("");
        checkBoxShowMap_tab3.setSelected(false);
    }

    /**
     * refreshing list with files and view at the tab2
     */
    public void refreshListPhotoAndViewImage_tab2(String directory, boolean includeSubDirectory) {
        listPhotos.clear();
        ImageFilesOperations.getListImageFilesFromDirectory(directory, includeSubDirectory, listPhotos);
        currentIndPhotoInList = 0;
        setImageView_tab2();
    }

    /**
     * setting image to tab2 by string path to image
     */
    public void setImageView_tab2() {
        if (listPhotos.size() > 0) {
            ImageFileWithMetadata imageFile = listPhotos.get(currentIndPhotoInList);
            try (FileInputStream fileInputStream = new FileInputStream(imageFile.getFile().getAbsoluteFile())) {
                Image image = new Image(fileInputStream);
                imageView_tab2.setImage(image);
                labelCurrentImagePath_tab2.setText(imageFile.toString());
                labelAnswerFromPlantNet_tab2.setText("");
            } catch (IOException e) {
                imageView_tab2.setImage(null);
                labelCurrentImagePath_tab2.setText("");
                labelAnswerFromPlantNet_tab2.setText("");
            }
        } else {
            imageView_tab2.setImage(null);
            labelCurrentImagePath_tab2.setText("");
            labelAnswerFromPlantNet_tab2.setText("");
            currentPlant = null;
            currentIndPhotoInList = 0;
        }
    }

    /**
     * button choose photo's dir tab2
     */
    public void handleChoosePhotosDirectory_tab2() {
        clearMessages();
        byChoosingDirectory("photos");
    }

    /**
     * checkbox Include Subdirectories tab2
     */
    public void handleCheckBoxIncludeDir_tab2() {
        refreshListPhotoAndViewImage_tab2(photosDirectory_tab2.getText().trim(), checkBoxIncludeSubdirectories_tab2.isSelected());
    }

    /**
     * try to open picture with windows Paint tab2
     */
    public void handleMouthClickPicture_tab2() {
        if (currentIndPhotoInList <= listPhotos.size() - 1) {
            ImageFileWithMetadata image = listPhotos.get(currentIndPhotoInList);
            try {
                Runtime.getRuntime().exec(new String[]{"C:\\WINDOWS\\system32\\mspaint.exe", image.getFile().getAbsolutePath()});
            } catch (IOException e) {
                logger.error("Can't open picture with windows Paint {}", image.getFile().getAbsolutePath());
            }
        }
    }

    /**
     * by click on tab2
     */
    public void handleOnSelectionChanged_tab2() {
        if (choiseBoxOrgan_tab2.getValue() == null) {
            choiseBoxOrgan_tab2.setItems(FXCollections.observableArrayList(
                    "flower", "leaf", "fruit", "bark", "habit", "other"));
        }
        if (photosDirectory_tab2.getText().trim().isEmpty()) {
            listPhotos.clear();
        } else {
            if (currentIndPhotoInList > listPhotos.size() - 1) {
                refreshListPhotoAndViewImage_tab2(photosDirectory_tab2.getText().trim(), checkBoxIncludeSubdirectories_tab2.isSelected());
            }
        }
        setImageView_tab2();
    }

    /**
     * change picture to previous /tab2
     */
    public void handleSchiftPicturePrev_tab2() {
        if (currentIndPhotoInList > 0) {
            currentIndPhotoInList--;
        } else {
            currentIndPhotoInList = listPhotos.size() > 0 ? listPhotos.size() - 1 : 0;
        }
        setImageView_tab2();
    }

    /**
     * change picture to next /tab2
     */
    public void handleSchiftPictureNext_tab2() {
        if (currentIndPhotoInList < listPhotos.size() - 1) {
            currentIndPhotoInList++;
        } else {
            currentIndPhotoInList = 0;
        }
        setImageView_tab2();
    }

    /**
     * deleting picture from list and from directory tab2
     */
    public void handleDeletePicture_tab2() {
        if (listPhotos.size() > 0) {
            File file = listPhotos.get(currentIndPhotoInList).getFile();
            listPhotos.remove(currentIndPhotoInList);
            if (currentIndPhotoInList > listPhotos.size() - 1) {
                currentIndPhotoInList = 0;
            }
            try {
                ImageFilesOperations.deleteLastFileInDirectory(file);
            } catch (IOException e) {
                logger.error("Cann't delete file: {}", file.getAbsolutePath() + "; " + e.getMessage());
            }
        }
        setImageView_tab2();
    }

    /**
     * set label text by selected plant tab2
     *
     * @param plant
     */
    public void setLabelAnswerFromPlantNet_tab2(Plant plant) {
        currentPlant = plant;
        if (currentPlant != null) {
            String wiki = currentPlant.getWeb_reference_wiki();
            labelAnswerFromPlantNet_tab2.setText(currentPlant.toString() + "\n" + wiki);
        } else {
            labelAnswerFromPlantNet_tab2.setText("");
        }
    }

    /**
     * send query to PlantNet: to recognize the plant tab2
     */
    public void handleQueryToPlantNet_tab2() {
        labelAnswerFromPlantNet_tab2.setText("Wait for result, please");
        if (CommonConstants.getInstance().getApiKeyPlantNet() == null || CommonConstants.getInstance().getApiKeyPlantNet().trim().isEmpty()) {
            labelAnswerFromPlantNet_tab2.setText("You have not api-key in Common Constants!");
        } else if (listPhotos.size() == 0) {
            labelAnswerFromPlantNet_tab2.setText("You have not any pictures!");
        } else if (imageView_tab2.getImage() == null) {
            labelAnswerFromPlantNet_tab2.setText("You haven't select the picture!");
        } else if (choiseBoxOrgan_tab2.getValue() == null || choiseBoxOrgan_tab2.getValue().toString().isEmpty()) {
            labelAnswerFromPlantNet_tab2.setText("You have to define the organ!");
        } else {

            Stage stageRecognition = new Stage();
            stageRecognition.setTitle("Recognition of Plant");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PlantRecognition.fxml"));

            try {
                PlantNetQuery plantNetQuery = new PlantNetQuery(listPhotos.get(currentIndPhotoInList).getFile());
                String jsonString = plantNetQuery.getAnswerFromPlantNet(choiseBoxOrgan_tab2.getValue().toString().trim());
                if (jsonString == null) {
                    labelAnswerFromPlantNet_tab2.setText("Can't receive answer from PlantNet!");
                } else {
                    Region rootRecognition = loader.load();
                    PlantRecognitionController controller = loader.getController();
                    controller.setInitialData(listPhotos.get(currentIndPhotoInList).getFile(), plantNetQuery, this);

                    Scene scene = new Scene(rootRecognition);
                    stageRecognition.setScene(scene);
                    stageRecognition.show();
                }
            } catch (IOException e) {
                logger.error("Can't open new window: {}", e.getMessage());
            }
        }
    }

    /**
     * by click on tab3
     */
    public void handleOnSelectionChanged_tab3() {

        imageView_tab3.setImage(imageView_tab2.getImage());
        if (listPhotos.size() > 0 && currentPlant != null) {
            ImageFileWithMetadata imageFile = listPhotos.get(currentIndPhotoInList);
            labeImagePath_tab3.setText(imageFile.getFile().getAbsolutePath());
            labelImageGeo_tab3.setText("lat.: " + imageFile.getLatitude() + ", long.: " + imageFile.getLongitude());
            labePlant_tab3.setText(choiseBoxOrgan_tab2.getValue().toString() + "; " + currentPlant.getScientific_name() + ". " + currentPlant.getCommon_names());
            hyperlinkWiki_tab3.setText(currentPlant.getWeb_reference_wiki());
        }
        if (comboBoxKindOfPlant_tab3.getValue() == null) {
            comboBoxKindOfPlant_tab3.setPromptText("Select kind of plant");
            comboBoxKindOfPlant_tab3.getItems().addAll("Tree", "Bush", "Flower");
            comboBoxKindOfPlant_tab3.getSelectionModel().selectFirst();
        }
        handleCheckBoxShowMap_tab3();
    }

    /**
     * show or hide the map tab3
     */
    public void handleCheckBoxShowMap_tab3() {
        if (checkBoxShowMap_tab3.isSelected()) {
            webView_tab3.setVisible(true);
            WebEngine webEngine = webView_tab3.getEngine();

            String url = "https://www.openstreetmap.org";
            if (listPhotos.size() > 0) {
                ImageFileWithMetadata imageFile = listPhotos.get(currentIndPhotoInList);
                url = "https://www.openstreetmap.org/?mlat=" +
                        imageFile.getLatitude() + "&amp;mlon=" +
                        imageFile.getLongitude() +
                        "#map=17/" + imageFile.getLatitude() + "/" + imageFile.getLongitude();
            }
            webEngine.load(url);
        } else {
            webView_tab3.setVisible(false);
        }
    }

    /**
     * record to database tab3
     */
    public void handleButtonRecordToDB_tab3() {

        Path pathToPictures = Paths.get(CommonConstants.getInstance().getPreparedPhotosDirectory());

        ImageFileWithMetadata imFile = null;
        if (currentIndPhotoInList >= 0 && currentIndPhotoInList < listPhotos.size()) {
            imFile = listPhotos.get(currentIndPhotoInList);
        }
        if (currentPlant != null && imFile != null) {
            if (currentPlant.getId_gbif().trim().isEmpty()) {
                logger.error("Impossible to save in DB. Null plant or empty id_gbif in plant {}", currentPlant.toString());
            } else {
                imFile.setOrgan(choiseBoxOrgan_tab2.getValue().toString());

                //if photo has no date or coordinates - simple save only Plant
                boolean saveOnlyPlant = true;
                if (imFile.getPhotos_date() != null && imFile.getLongitude() > 0 && imFile.getLatitude() > 0) {
                    saveOnlyPlant = false;
                }

                //copy file to pictures final directory and set relative path
                if (!saveOnlyPlant) {
                    String endPath = null;
                    try { //set field to imFile
                        endPath = new ImageFilesOperations().copyFileToOutputDir(imFile, CommonConstants.getInstance().getPreparedPhotosDirectory());
                    } catch (IOException e) {
                        logger.error("Can't copy file to prepared photo's directory! {}", e.getMessage());
                    }

                    imFile.setPath_to_picture(pathToPictures.relativize(Paths.get(endPath)).toString().replaceAll("\\\\", "/"));
                }
                imFile.setPlant(currentPlant);

                //kind of plant
                currentPlant.setKind(comboBoxKindOfPlant_tab3.getValue().toString());
                //show only flowering:
                currentPlant.setShow_only_flowering(checkBoxShowOnlyFlowering_tab3.isSelected()? 1: 0);
                currentPlant.setUpdated(1);
                //writing to database and refresh controller's fields
                if (!saveOnlyPlant) {//all records
                    dataBaseOperations.createAllRecordsToDataBase(currentPlant, imFile);
                } else {//only plant
                    dataBaseOperations.createPlant(currentPlant);
                }

                currentPlant = null;

                listPhotos.remove(currentIndPhotoInList);
                try {
                    ImageFilesOperations.deleteLastFileInDirectory(imFile.getFile());
                } catch (IOException e) {
                    logger.error("Can't delete file {}", imFile.getFile().getAbsolutePath());
                }
                currentIndPhotoInList = currentIndPhotoInList > 0 ? currentIndPhotoInList - 1 : currentIndPhotoInList;
            }
        } else {
            labePlant_tab3.setText("There isn't selected plant or image. May be you already have wrote this record to DB!");
        }

        clearMessages();
        labePlant_tab3.setText("Done");
    }

    /**
     * open browser with link tab3
     */
    public void handleHyperLinkWiki_tab3() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(hyperlinkWiki_tab3.getText()));
            } catch (IOException e) {
                logger.error("IOException. Error by openning browser: {}", e.getMessage());
            } catch (URISyntaxException e) {
                logger.error("URISyntaxException. Error by openning browser: {}", e.getMessage());
            }
        }
    }
}

