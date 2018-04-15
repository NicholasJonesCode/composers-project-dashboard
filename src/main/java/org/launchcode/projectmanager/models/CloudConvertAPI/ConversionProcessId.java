package org.launchcode.projectmanager.models.CloudConvertAPI;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversionProcessId {

    private String url;

    private String id;

    private String host;

    private String expires;

    private Integer maxsize;

    private Integer maxtime;

    private Integer concurrent;


    public ConversionProcessId() {

    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    public Integer getMaxsize() {
        return maxsize;
    }

    public void setMaxsize(Integer maxsize) {
        this.maxsize = maxsize;
    }

    public Integer getMaxtime() {
        return maxtime;
    }

    public void setMaxtime(Integer maxtime) {
        this.maxtime = maxtime;
    }

    public Integer getConcurrent() {
        return concurrent;
    }

    public void setConcurrent(Integer concurrent) {
        this.concurrent = concurrent;
    }
}
