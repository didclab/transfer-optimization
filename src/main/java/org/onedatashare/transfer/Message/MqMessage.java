package org.onedatashare.transfer.Message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.api.client.json.Json;
import org.onedatashare.transfer.model.request.TransferJobRequestWithMetaData;


public class MqMessage {
    private final String text;
    private final int priority;
    private final boolean secret;

    public MqMessage(@JsonProperty("text") final String text,
                     @JsonProperty("priority") final int priority,
                     @JsonProperty("secret") final boolean secret) {
        this.text = text;
        this.priority = priority;
        this.secret = secret;
    }

    public String getText() {
        return text;
    }

    public int getPriority() {
        return priority;
    }

    public boolean isSecret() {
        return secret;
    }

    @Override
    public String toString() {
        return "PracticalTipMessage{" +
                "text='" + text + '\'' +
                ", priority=" + priority +
                ", secret=" + secret +
                '}';
    }
}
