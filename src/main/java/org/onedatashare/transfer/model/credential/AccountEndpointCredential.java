package org.onedatashare.transfer.model.credential;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;


/**
 * POJO for storing account credential i.e., userName and Password
 */
@Data
@Document
public class AccountEndpointCredential extends EndpointCredential {
    private String username;
    private String secret;
}
