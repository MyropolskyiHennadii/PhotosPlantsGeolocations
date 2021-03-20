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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class PlantNetQuery {

    private static final String URL_plantAPI = "https://my-api.plantnet.org/v2/identify/all?api-key=" + CommonConstants.getInstance().getApiKeyPlantNet().trim();

    //if PlantNet doen't return gbif
    //Species API: https://www.gbif.org/developer/species
    private static final String URL_SpeciesAPI = "https://api.gbif.org/v1/species/match?verbose=true&name=";

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
     *
     * @param organ
     * @return
     */
    public String getAnswerFromPlantNet(String organ) {

        HttpEntity entity = MultipartEntityBuilder.create()
                .addPart("images", new FileBody(image)).addTextBody("organs", organ)
                //you can add photos else .addPart("images", new FileBody(file2)).addTextBody("organs", "leaf")
                //also you can add parameter 'language'
                .build();

        HttpPost request = new HttpPost(URL_plantAPI);
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

    //Species API: https://www.gbif.org/developer/species
    public static String getId_GbifFromSpeciesAPI(String name) {

        URL obj = null;
        HttpURLConnection con = null;
        String jsonResultsSpeciesAPI = "";
        JSONParser parser = new JSONParser();
        try {
            obj = new URL(URL_SpeciesAPI + name.replaceAll(" ", "%20"));
            con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");
            //con.setRequestProperty("verbose", "true");
            //con.setRequestProperty("name", name);

            logger.info("Starting GET Species API: {}", name);

            con.setUseCaches(false);
            con.setDoOutput(true);
            int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                logger.info("Ok, finished GET, response code: {}", responseCode);
                JSONObject jsonObject = null;
                try {
                    jsonObject = (JSONObject) parser.parse(response.toString());
                    jsonResultsSpeciesAPI = ""+jsonObject.get("usageKey");
                    if (jsonResultsSpeciesAPI == null) {
                        return "";
                    }

                } catch (ParseException e) {
                    logger.error("Can't parse Json: {}", e.getMessage());
                } catch (NullPointerException e) {
                    logger.error("GET request hasn't usageKey: {}", e.getMessage());
                }
            } else {
                logger.error("GET request doesn't work: {}", name);
                logger.info("Response code: {}", responseCode);
            }
        } catch (IOException e) {
            logger.error("GET request doesn't work: {}", e.getMessage());
        }
        return jsonResultsSpeciesAPI;
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
            if (jsonString.contains("error")) {
                logger.error("Error: {}", jsonString);
            } else if (results == null || results.size() == 0) {
                logger.error("There are no results in PlantNet!");
            } else {
                for (Object result : results) {
                    JSONObject jsonElement = (JSONObject) result;
                    double score = (double) jsonElement.get("score");

                    JSONObject jsonSpecies = (JSONObject) jsonElement.get("species");
                    JSONArray commonNames = (JSONArray) jsonSpecies.get("commonNames");
                    String scientificNameAuthorship = (String) jsonSpecies.get("scientificNameAuthorship");
                    String scientificName = (String) jsonSpecies.get("scientificNameWithoutAuthor");
                    JSONObject jsonFamily = (JSONObject) jsonSpecies.get("family");
                    String scientificNameFamily = (String) jsonFamily.get("scientificNameWithoutAuthor");

                    //gbif id
                    String id = getId_GbifFromSpeciesAPI(scientificName);
                    if (id.isEmpty()) {
                        //try this: sometimes PlantNet returns gbif
                        JSONObject jsonGbif = null;
                        try {
                            jsonGbif = (JSONObject) jsonElement.get("gbif");
                            id = (String) jsonGbif.get("id");

                        } catch (NullPointerException e) {
                            logger.error("Undefined plant-id gbif for jsonResult {}", jsonElement);
                            continue;
                        }
                    }
                    Plant plant = new Plant(score, scientificNameAuthorship, scientificName, commonNames.toString(), scientificNameFamily, "Tree", id);
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
