package photos.model;

// https://my-api.plantnet.org/

import config.CommonConstants;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PlantNetQuery {

    private static final String URL = "https://my-api.plantnet.org/v2/identify/all?api-key=" + CommonConstants.getInstance().getApiKeyPlantNet().trim();

    private static Logger logger = LoggerFactory.getLogger(PlantNetQuery.class);
    private File image;
    private String jsonResults;

    public PlantNetQuery(File image) {
        this.image = image;
    }

    public String getJsonResults() {
        return jsonResults;
    }

    /**
     * getting answer from PlantNet API
     * @param organ
     * @return
     */
    public String getAnswerFromPlantNet(String organ) {

        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("images", new FileBody(image)).addTextBody("organs", organ)
                //you can add photos else .addPart("images", new FileBody(file2)).addTextBody("organs", "leaf")
                //also you can add parameter 'language'
                .build();

        HttpPost request = new HttpPost(URL);
        request.setEntity(entity);
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response;
        jsonResults = null;
        try {
            response = client.execute(request);
            jsonResults = EntityUtils.toString(response.getEntity());
        } catch (IOException e) {
            logger.error("Error by querying PlantNet: {}", e.getMessage());
            return null;
        }

        return jsonResults;
    }

    /**
     * parse json results and return List of Plants
     *
     * @return list with recognized plants
     */
    public List<Plant> parseJsonResultsFromPlantNet() {

        JSONParser parser = new JSONParser();
        List<Plant> listPlant = new ArrayList<>();
        String jsonString = getJsonResults();

        try {
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            JSONArray results = (JSONArray) jsonObject.get("results");
            if (jsonString.contains("error")){
                logger.error("Error: {}", jsonString);
            } else if (results == null || results.size() == 0) {
                logger.error("There are no results in PlantNet!");
            } else {
                for (Object result : results) {
                    JSONObject jsonElement = (JSONObject) result;
                    double score = (double) jsonElement.get("score");

                    JSONObject jsonGbif = (JSONObject) jsonElement.get("gbif");
                    String id = (String) jsonGbif.get("id");

                    JSONObject jsonSpecies = (JSONObject) jsonElement.get("species");
                    JSONArray commonNames = (JSONArray) jsonSpecies.get("commonNames");
                    String scientificNameAuthorship = (String) jsonSpecies.get("scientificNameAuthorship");
                    String scientificName = (String) jsonSpecies.get("scientificNameWithoutAuthor");

                    JSONObject jsonFamily = (JSONObject) jsonSpecies.get("family");
                    String scientificNameFamily = (String) jsonFamily.get("scientificNameWithoutAuthor");

                    Plant plant = new Plant(score, scientificNameAuthorship, scientificName, commonNames.toString(), scientificNameFamily, true, id);
                    plant.findAndSetWikiByName();
                    listPlant.add(plant);
                }
            }
        } catch (ParseException e) {
            logger.error("Something wrong with parsing json! ParseException! {}", e.getMessage());
            logger.error("Results: {}", jsonString);
        }
        return listPlant;
    }
}
