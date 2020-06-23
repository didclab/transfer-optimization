package org.onedatashare.transfer.model.request;

import lombok.Data;
import lombok.NonNull;
import org.onedatashare.transfer.model.core.EndpointType;
import org.onedatashare.transfer.model.core.EntityInfo;

import java.util.ArrayList;

@Data
public class TransferJobRequest {
    @NonNull private String ownerId;
    private int priority;
    @NonNull private String id;
    @NonNull private Source source;
    @NonNull private Destination destination;
    private TransferOptions options;


    @Data
    public static class Destination {
        @NonNull private EndpointType type;
        @NonNull private String credId;
        private EntityInfo info;
    }

    @Data
    public static class Source {
        @NonNull private EndpointType type;
        @NonNull private String credId;
        @NonNull private EntityInfo info;
        @NonNull private ArrayList<EntityInfo> infoList;
    }
}