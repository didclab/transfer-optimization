package org.onedatashare.transfer.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.onedatashare.transfer.model.core.EndpointType;

import java.util.HashSet;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class TransferJobRequest {
    @NonNull protected Source source;
    @NonNull protected Destination destination;
    protected TransferOptions options;


    @Data
    @Accessors(chain = true)
    public static class Destination {
        @NonNull protected EndpointType type;
        @NonNull protected String credId;
        @NonNull protected EntityInfo info;
    }

    @Data
    @Accessors(chain = true)
    public static class Source {
        @NonNull protected EndpointType type;
        @NonNull protected String credId;
        @NonNull protected EntityInfo info;
        @NonNull protected HashSet<EntityInfo> infoList;
    }

    @Data
    @Accessors(chain = true)
    public static class EntityInfo {
        protected String id;
        protected String path;
        protected long size;
    }

}
