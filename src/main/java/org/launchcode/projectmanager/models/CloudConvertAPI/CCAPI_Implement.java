package org.launchcode.projectmanager.models.CloudConvertAPI;

import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class CCAPI_Implement {

    //Some keys lol
    //Nw_KX8DDBah89cWmFDL00xl3sAMp-idcCGGkcoe9iluM2eywWpLSNRrXVx1F0DJVfmv8Lpu8KWm1KvgV02xEiQ
    //6Z5LV1mfoLKGS6LeYQgRro5k_mj5qzBM9F7EQ6pECtVe3B-9nwuu0Dy6Fvq5eQmyCm9RcJknaZXd0BG8NTmGig

    private static String outputPath = System.getProperty("user.dir") + File.separator + "src\\main\\resources\\files\\indexReadme.html";

    //get one time process id for the conversion process
    public static String getHttpsProcessId(String apiKey) {
        RestTemplate restTemplate = new RestTemplate();

        final String URLgetProcessId = "https://api.cloudconvert.com/process";

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("inputformat", "md");
        body.add("outputformat", "html");

        HttpEntity<MultiValueMap> httpEntity = new HttpEntity<>(body, headers);

        ConversionProcessId conversionProcessId = restTemplate.postForObject(URLgetProcessId, httpEntity, ConversionProcessId.class);

        return "https:" + conversionProcessId.getUrl();
    }

    //use that process id url to convert the damn thing
    public static String getHttpsConversionDownloadLink(String apiKey) {

        RestTemplate restTemplate = new RestTemplate();
        String url = getHttpsProcessId(apiKey);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("input", "download");
        body.add("file", "https://raw.githubusercontent.com/NicholasJonesCode/composers-project-dashboard/master/README.md");
        body.add("outputformat", "html");

        ConvertedObject convertedObject = restTemplate.postForObject(url, body, ConvertedObject.class);

        return "https:" + convertedObject.getOutput().getUrl();
    }

    //This is what you wanna call, boi, to get ur magic html string
    public static String getHTMLString(String apiKey) throws IOException {

        URL url = new URL(getHttpsConversionDownloadLink(apiKey));
        File file = new File(outputPath);

        try {
            //convert download link to local file
            FileUtils.copyURLToFile(url,file, 10000,10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Convert local file to String
        return FileUtils.readFileToString(file, "UTF-8");
    }

    public static String getLastSuccessfullyConvertedHTMLString() throws IOException {

        File file = new File(outputPath);

        //Convert local file to String
        return FileUtils.readFileToString(file, "UTF-8");

    }

}
