package org.example;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class HttpBinResponse {
    @JsonProperty("headers")
    private Map<String, String> headers;

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
