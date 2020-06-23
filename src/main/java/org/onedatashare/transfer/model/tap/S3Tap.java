package org.onedatashare.transfer.model.tap;
import com.amazonaws.services.s3.model.S3Object;
import com.box.sdk.BoxAPIResponse;
import org.apache.commons.io.IOUtils;
import org.onedatashare.transfer.model.core.Slice;
import reactor.core.publisher.Flux;

import java.io.InputStream;

public class S3Tap implements Tap {
    private long size;
    private S3Object headerOverrideObject;

    public static S3Tap initialize(S3Object headerOverrideObject, long size){
        S3Tap s3Tap = new S3Tap();
        s3Tap.size = size;
        s3Tap.headerOverrideObject = headerOverrideObject;
        return s3Tap;
    }
    @Override
    public Flux<Slice> openTap(int sliceSize) {
        InputStream inputStream = headerOverrideObject.getObjectContent();
        return Flux.generate(() -> 0, (state, sink) -> {
            try {
                if (state + sliceSize < this.size) {
                    sink.next(new Slice(IOUtils.toByteArray(inputStream, sliceSize)));
                }
                else{
                    int remaining = Math.toIntExact(size - state);
                    sink.next(new Slice(IOUtils.toByteArray(inputStream, remaining)));
                    inputStream.close();
                    sink.complete();
                }
            } catch (Exception e){
                e.printStackTrace();
                sink.error(e);
            }
            return state + sliceSize;
        });
    }
}
