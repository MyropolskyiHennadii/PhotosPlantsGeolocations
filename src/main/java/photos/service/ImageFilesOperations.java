package photos.service;

import config.CommonConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import photos.model.ImageFileWithMetadata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ImageFilesOperations implements ImageFilesSelection {

    private static Logger logger = LoggerFactory.getLogger(ImageFilesOperations.class);
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private String tfNameInput;
    private String tfNameOutput;

    public ImageFilesOperations(LocalDate dateFrom, LocalDate dateTo, String tfNameInput, String tfNameOutput) {
        this.dateFrom = dateFrom;
        this.dateTo = dateTo;
        this.tfNameInput = tfNameInput;
        this.tfNameOutput = tfNameOutput;
    }

    public ImageFilesOperations() {

    }

    /**
     * forming list with all imagges with metadata in dir
     *
     * @param directory
     * @param includeSubDirectory
     * @param listImageFile
     */
    public static void getListImageFilesFromDirectory(String directory, boolean includeSubDirectory, List<ImageFileWithMetadata> listImageFile) {
        File dir = new File(directory);

        File[] allFiles = dir.listFiles();
        if (allFiles != null) {
            for (File file : allFiles) {
                if (file.isDirectory() && includeSubDirectory) {
                    getListImageFilesFromDirectory(file.getAbsolutePath(), includeSubDirectory, listImageFile);
                } else {
                    ImageFileWithMetadata imageFile = ImageFilesSelection.imageWithMetadata(file);
                    if (imageFile != null) {
                        listImageFile.add(imageFile);
                    }
                }
            }
        }
    }

    /**
     * delete file and check, whether it is the last one in directory. If yes, delete directory and so on
     *
     * @param file
     */
    public static void deleteLastFileInDirectory(File file) throws IOException {
        List<File> listDeletable = new ArrayList<>();
        listDeletable.add(file);
        String parent = file.getParent();
        while (parent != null) {
            File dir = new File(parent);
            File[] filesInDir = dir.listFiles();
            if ((filesInDir == null) || (filesInDir.length > 0)) {
                break;
            }
            listDeletable.add(dir);
            parent = file.getParent();
        }
        for (File delFile : listDeletable) {
            boolean isDeleted = delFile.delete();
            if (!isDeleted) {
                logger.info("Cann't delete file " + delFile.getName());
            }
        }
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public String getTfNameInput() {
        return tfNameInput;
    }

    public String getTfNameOutput() {
        return tfNameOutput;
    }

    /**
     * create output directory for month and day
     *
     * @param startPath
     * @param nameDir
     * @return
     * @throws IOException
     */
    public boolean checkAndCreateMonthAndDayDirectories(String startPath, String nameDir) throws IOException {

        if (new File(startPath + File.separator + nameDir).exists()) {
            return new File(startPath + File.separator + nameDir).isDirectory();
        } else {
            logger.info("Creating: {}", startPath + nameDir + File.separator);
            Files.createDirectories(Paths.get(startPath + File.separator + nameDir));
            return true;
        }
    }

    /**
     * reading input files and copying to output directory, if all conditions are good
     *
     * @throws IOException
     */
    public void readInputFilesCreateOutputFiles() throws IOException {
        File rootDir = new File(tfNameInput);
        File[] filesRootDir = null;
        if (rootDir.exists()) {
            filesRootDir = rootDir.listFiles();
        }
        if (filesRootDir == null) {
            filesRootDir = new File[0];
        }
        for (File file : filesRootDir) {

            ImageFileWithMetadata imageFile = ImageFilesSelection.imageWithMetadata(file);
            if (imageFile == null) {
                continue;
            }

            if (!checkTags(imageFile)) {
                continue;
            }

            //filter for longitude and latitude
            if (CommonConstants.getInstance().isCheckLongLat()) {
                double longitude = imageFile.getLongitude();
                double latitude = imageFile.getLatitude();
                if (longitude < -9999 || latitude < -9999) {
                    continue;
                }
                if (longitude > CommonConstants.getInstance().getMaxLongitude() || longitude < CommonConstants.getInstance().getMinLongitude()) {
                    continue;
                }
                if (latitude > CommonConstants.getInstance().getMaxLatitude() || latitude < CommonConstants.getInstance().getMinLatitude()) {
                    continue;
                }
            }

            //filter for dates:
            LocalDate fotosDate = imageFile.getPhotosDate();
            if (dateFrom != null) {
                if (fotosDate.isBefore(dateFrom)) {
                    continue;
                }
            }
            if (dateTo != null) {
                if (fotosDate.isAfter(dateTo)) {
                    continue;
                }
            }

            copyFileToOutputDir(imageFile, tfNameOutput);
        }
    }

    /**
     * @param imageFile
     * @param outputDir
     * @return string with end-path
     * @throws IOException
     */
    public String copyFileToOutputDir(ImageFileWithMetadata imageFile, String outputDir) throws IOException {

        LocalDate fotosDate = imageFile.getPhotosDate();
        String month = "" + fotosDate.getMonthValue();
        String day = "" + fotosDate.getDayOfMonth();
        File file = imageFile.getFile();
        String endPath = null;
        boolean dirWasCreated = checkAndCreateMonthAndDayDirectories(outputDir, month);
        if (dirWasCreated) {
            dirWasCreated = checkAndCreateMonthAndDayDirectories(outputDir + File.separator + month, day);
            if (dirWasCreated) {
                endPath = outputDir + File.separator + month + File.separator + day + File.separator + file.getName();
                Files.copy(file.toPath(), Paths.get(endPath), StandardCopyOption.REPLACE_EXISTING);
                logger.info("Copying file: {}", fotosDate + ": " + file.getName() + "; geo long: " + imageFile.getLongitude() + " geo lat: " + imageFile.getLatitude());
            }
        }
        return endPath;
    }


    @Override
    public boolean checkTags(ImageFileWithMetadata file) {
        boolean ok = true;
        if (file.getMetadata() == null) {
            return false;
        }
        if (file.getLatitude() == null || file.getLongitude() == null) {
            return false;
        }
        if (file.getPhotosDate() == null) {
            return false;
        }
        return ok;
    }

}

