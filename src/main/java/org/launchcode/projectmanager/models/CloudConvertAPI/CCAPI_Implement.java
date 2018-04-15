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

    public static String getHttpsProcessId() {
        RestTemplate restTemplate = new RestTemplate();

        final String URLgetProcessId = "https://api.cloudconvert.com/process";
        final String apiKey = "Nw_KX8DDBah89cWmFDL00xl3sAMp-idcCGGkcoe9iluM2eywWpLSNRrXVx1F0DJVfmv8Lpu8KWm1KvgV02xEiQ";
        //Optional key:
        //final String apiKey = "6Z5LV1mfoLKGS6LeYQgRro5k_mj5qzBM9F7EQ6pECtVe3B-9nwuu0Dy6Fvq5eQmyCm9RcJknaZXd0BG8NTmGig";

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
    public static String getHttpsConversionDownloadLink() {

        RestTemplate restTemplate = new RestTemplate();
        String url = getHttpsProcessId();

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("input", "download");
        body.add("file", "https://raw.githubusercontent.com/NicholasJonesCode/composers-project-dashboard/master/README.md");
        body.add("outputformat", "html");

        ConvertedObject convertedObject = restTemplate.postForObject(url, body, ConvertedObject.class);

        return "https:" + convertedObject.getOutput().getUrl();
    }

    public static String getHTMLString() throws IOException {

        URL url = new URL(getHttpsConversionDownloadLink());
        File file = new File("C:\\Testing\\test.html");

        try {
            FileUtils.copyURLToFile(url,file, 10000,10000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return FileUtils.readFileToString(file, "UTF-8");
    }


}
