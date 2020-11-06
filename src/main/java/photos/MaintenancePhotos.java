package photos;

import config.CommonConstants;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class MaintenancePhotos extends Application {

    private static Logger logger = LoggerFactory.getLogger(MaintenancePhotos.class);

    public static void main(String[] args) {
        logger.info("Start: ------------ {}", LocalDateTime.now());
        Application.launch(MaintenancePhotos.class, args);
        logger.info("Finish: ------------ {}", LocalDateTime.now());
    }

    @Override
    public void start(Stage stage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainDialog.fxml"));
        CommonConstants.getInstance().setStage(stage);

        stage.setTitle("Welcome To Photo's Maintenance");
        Scene scene = new Scene(root, 600, 450);
        scene.getStylesheets().add("css/application.css");
        stage.setScene(scene);
        stage.show();
    }
}
