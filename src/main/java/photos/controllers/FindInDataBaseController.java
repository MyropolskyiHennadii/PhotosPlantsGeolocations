package photos.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.Plant;
import photos.service.DataBaseOperations;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class FindInDataBaseController {
    private static Logger logger = LoggerFactory.getLogger(FindInDataBaseController.class);
    private static List<Plant> listPlants = new ArrayList<>();
    public Button buttonSelectAndClose_tab1;
    public TextField textFieldName_tab1;
    public Button buttonFind_tab1;
    public ListView listViewResults_tab1;
    public Hyperlink hyperlinkResult_tab1;
    public Label labelInfo_tab1;
    private DataBaseCorrectionsDialogController dataBaseCorrectionsDialogController;// common field with MainDialogController
    private DataBaseOperations dataBaseOperations = DataBaseOperations.getInstance();

    /**
     * Close window and give selected Plant
     */
    public void handleButtonSelectAndClose_tab1() {
        int ind = listViewResults_tab1.getSelectionModel().getSelectedIndex();
        Plant selectedPlant = listPlants.get(ind);
        dataBaseCorrectionsDialogController.setPlantFromDB_tab1(selectedPlant);

        Stage stage = (Stage) buttonSelectAndClose_tab1.getScene().getWindow();
        stage.close();
    }

    /**
     * By clicking on reference
     */
    public void handleHyperlinkResult_tab1() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(hyperlinkResult_tab1.getText()));
            } catch (IOException e) {
                logger.error("IOException. Error by openning browser: {}", e.getMessage());
            } catch (URISyntaxException e) {
                logger.error("URISyntaxException. Error by openning browser: {}", e.getMessage());
            }
        }
    }

    /**
     * by moving cursor through the listViewResukts
     */
    public void handleListViewResults_tab1() {
        if (listViewResults_tab1.getItems().size() > 0) {
            int ind = listViewResults_tab1.getSelectionModel().getSelectedIndex();
            Plant selectedPlant = listPlants.get(ind);
            labelInfo_tab1.setText("ID: " + selectedPlant.getId_gbif() + ". " + selectedPlant.getCommon_names());
            String wiki = selectedPlant.getWeb_reference_wiki();
            if (wiki != null) {
                hyperlinkResult_tab1.setText(wiki);
            } else {
                hyperlinkResult_tab1.setText("");
            }
        } else {
            hyperlinkResult_tab1.setText("");
        }
    }

    /**
     * refresh list view
     */
    public void refreshListView_tab1() {
        if (listPlants.size() > 0) {
            ObservableList items = FXCollections.observableArrayList(listPlants);
            listViewResults_tab1.setItems(items);
            listViewResults_tab1.getSelectionModel().selectFirst();
            handleListViewResults_tab1();
        } else {
            listViewResults_tab1.setItems(null);
            labelInfo_tab1.setText("There are no data");
            hyperlinkResult_tab1.setText("");
        }
    }

    /**
     * find records by name
     */
    public void handleButtonFind_tab1() {
        String name = textFieldName_tab1.getText().trim();
        if (!name.isEmpty()) {
            labelInfo_tab1.setText("");
            listPlants = dataBaseOperations.findPlantByName(name);
            refreshListView_tab1();
        } else {
            labelInfo_tab1.setText("You cann't find by empty name! Define the name!");
        }
    }

    /**
     * by openning the form from main dialog
     *
     * @param dataBaseCorrectionsDialogController
     */
    public void setInitialData(DataBaseCorrectionsDialogController dataBaseCorrectionsDialogController) {
        this.dataBaseCorrectionsDialogController = dataBaseCorrectionsDialogController;
    }
}
