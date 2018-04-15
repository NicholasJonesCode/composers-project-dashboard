package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Status {

    private String id;

    private String url;

    private Integer expire;

    private Integer percent;

    private String message;

    private String step;

    private Integer starttime;

    private StatusOutput output;

    private StatusInput input;

    private StatusConverter converter;

    private String group;

    private Integer minutes;

    private Integer endtime;


    public Status() {
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

    public StatusOutput getOutput() {
        return output;
    }

    public void setOutput(StatusOutput output) {
        this.output = output;
    }

    public StatusInput getInput() {
        return input;
    }

    public void setInput(StatusInput input) {
        this.input = input;
    }

    public StatusConverter getConverter() {
        return converter;
    }

    public void setConverter(StatusConverter converter) {
        this.converter = converter;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getMinutes() {
        return minutes;
    }

    public void setMinutes(Integer minutes) {
        this.minutes = minutes;
    }

    public Integer getEndtime() {
        return endtime;
    }

    public void setEndtime(Integer endtime) {
        this.endtime = endtime;
    }
}
