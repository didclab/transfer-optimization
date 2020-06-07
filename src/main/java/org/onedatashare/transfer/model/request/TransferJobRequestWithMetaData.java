package org.onedatashare.transfer.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.apache.commons.lang.RandomStringUtils;
import org.onedatashare.transfer.model.core.EndpointType;
import org.onedatashare.transfer.model.core.EntityInfo;

import java.util.ArrayList;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class TransferJobRequestWithMetaData{
    private String ownerId;
    public String id;

    @NonNull protected TransferJobRequest.Source source;
    @NonNull protected TransferJobRequest.Destination destination;
    protected TransferOptions options;

    public static TransferJobRequestWithMetaData getTransferRequestWithMetaData(String owner,
                                                                                TransferJobRequest request){
        TransferJobRequestWithMetaData requestWithMetaData = new TransferJobRequestWithMetaData();
        requestWithMetaData.ownerId = owner;
        requestWithMetaData.source = request.getSource();
        requestWithMetaData.destination = request.getDestination();
        requestWithMetaData.options = request.getOptions();
        requestWithMetaData.id = RandomStringUtils.randomAlphanumeric(6);
        return requestWithMetaData;
    }
}