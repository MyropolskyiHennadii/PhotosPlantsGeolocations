package photos.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.Plant;
import photos.model.PlantNetQuery;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class PlantRecognitionController {

    private static Logger logger = LoggerFactory.getLogger(PlantRecognitionController.class);
    public ImageView imageViewPlant;
    public Button buttonSelectAndClose;
    public Label labelInfo;
    public ListView<String> listViewResults;
    public Hyperlink hyperlinkWiki;
    private List<Plant> listPlants;//list of results from PlantNet API
    private ManageIncomingPhotosController mainDialogController;// common field with MainDialogController

    /**
     * initiation of controller
     *
     * @param file           image
     * @param plantNetQuery  exemplar of PlantNetQuery with results
     * @param mainController to return to main window selected result
     */
    public void setInitialData(File file, PlantNetQuery plantNetQuery, ManageIncomingPhotosController mainController) {

        mainDialogController = mainController;

        try (FileInputStream fileInputStream = new FileInputStream(file.getAbsoluteFile())) {
            Image image = new Image(fileInputStream);
            imageViewPlant.setImage(image);
        } catch (FileNotFoundException e) {
            labelInfo.setText("Something wrong with picture! FileNotFoundException");
            imageViewPlant.setImage(null);
        } catch (IOException e) {
            imageViewPlant.setImage(null);
            labelInfo.setText("Something wrong with picture! IOException");
        }

        listPlants = plantNetQuery.parseJsonResultsFromPlantNet();
        if (listPlants == null || listPlants.size() == 0) {
            labelInfo.setText("Something wrong with parsing results of NetPlant's query!");
        } else {
            ObservableList items = FXCollections.observableArrayList(listPlants);
            listViewResults.setItems(items);
            listViewResults.getSelectionModel().selectFirst();
            if(items.size() > 0){
                handleListViewResults();
            }
        }

    }

    /**
     * clicking mouse or moving cursor in listView
     */
    public void handleListViewResults() {
        int ind = listViewResults.getSelectionModel().getSelectedIndex();
        Plant selectedPlant = listPlants.get(ind);
        labelInfo.setText("Score: " + selectedPlant.getScore() + ". " + selectedPlant.getCommon_names());
        String wiki = selectedPlant.getWeb_reference_wiki();
        if (wiki != null) {
            hyperlinkWiki.setText(wiki);
        } else {
            hyperlinkWiki.setText("");
        }
    }

    /**
     * open browser by wiki-link
     */
    public void handleWikiHyperlink(){
        if(Desktop.isDesktopSupported())
        {
            try {
                Desktop.getDesktop().browse(new URI(hyperlinkWiki.getText()));
            } catch (IOException e) {
                logger.error("IOException. Error by openning browser: {}", e.getMessage());
            } catch (URISyntaxException e) {
                logger.error("URISyntaxException. Error by openning browser: {}", e.getMessage());
            }
        }
    }

    /**
     * Close window and give selected Plant
     */
    public void handleButtonSelectAndClose() {
        int ind = listViewResults.getSelectionModel().getSelectedIndex();
        Plant selectedPlant = listPlants.get(ind);

        mainDialogController.setLabelAnswerFromPlantNet_tab2(selectedPlant);

        Stage stage = (Stage) buttonSelectAndClose.getScene().getWindow();
        stage.close();
    }

}
