package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertedObjectInput {

    private String type;

    public ConvertedObjectInput() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
