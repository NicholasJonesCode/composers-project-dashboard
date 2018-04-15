package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusConverter {

    private String mode;

    private String format;

    private String type;

    private StatusConverterOptions options;


    public StatusConverter() {
    }


    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public StatusConverterOptions getOptions() {
        return options;
    }

    public void setOptions(StatusConverterOptions options) {
        this.options = options;
    }
}
