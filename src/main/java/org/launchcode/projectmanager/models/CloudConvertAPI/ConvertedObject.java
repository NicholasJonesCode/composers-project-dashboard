package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConvertedObject {

    private String id;

    private String url;

    private Integer expire;

    private Integer percent;

    private String message;

    private String step;

    private Integer starttime;

    private ConvertedObjectOutput output;

    private ConvertedObjectInput input;

    public ConvertedObject() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getExpire() {
        return expire;
    }

    public void setExpire(Integer expire) {
        this.expire = expire;
    }

    public Integer getPercent() {
        return percent;
    }

    public void setPercent(Integer percent) {
        this.percent = percent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public Integer getStarttime() {
        return starttime;
    }

    public void setStarttime(Integer starttime) {
        this.starttime = starttime;
    }

    public ConvertedObjectOutput getOutput() {
        return output;
    }

    public void setOutput(ConvertedObjectOutput output) {
        this.output = output;
    }

    public ConvertedObjectInput getInput() {
        return input;
    }

    public void setInput(ConvertedObjectInput input) {
        this.input = input;
    }
}
