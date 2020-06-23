package org.onedatashare.transfer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

@Value
public class TransferDetails implements Serializable {

    String fileName;
    Long duration;


}
