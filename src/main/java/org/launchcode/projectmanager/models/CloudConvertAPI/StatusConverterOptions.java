package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatusConverterOptions {

    private Boolean input_markdown_syntax;


    public StatusConverterOptions() {
    }

    public Boolean getInput_markdown_syntax() {
        return input_markdown_syntax;
    }

    public void setInput_markdown_syntax(Boolean input_markdown_syntax) {
        this.input_markdown_syntax = input_markdown_syntax;
    }
}
