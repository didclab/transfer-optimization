package org.onedatashare.transfer.model.drain;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.transfer.TransferManager;
import com.amazonaws.services.s3.transfer.TransferManagerBuilder;
import com.amazonaws.services.s3.transfer.Upload;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.onedatashare.transfer.model.core.Slice;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class S3Drain implements Drain {
    private String bucketName;
    private String keyName;
    private TransferManager tm;


    private S3Drain(){}

    public static S3Drain getInstance(AmazonS3Client s3Client, String bucketName, String keyName){
        S3Drain s3Drain = new S3Drain();
        s3Drain.bucketName = bucketName;
        s3Drain.keyName = keyName;
        s3Drain.tm = TransferManagerBuilder.standard().withS3Client(s3Client).build();

        return s3Drain;
    }

    @Override
    public void drain(Slice slice) throws Exception {
        ObjectMetadata omd = new ObjectMetadata();
        Upload upload = tm.upload(bucketName, keyName, new ByteArrayInputStream((slice.asBytes())), omd);
        upload.waitForCompletion();
    }


    @Override
    public void finish() throws Exception {
        //
    }
}
