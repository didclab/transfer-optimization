//package org.onedatashare.transfer.model;
//
//import com.fasterxml.jackson.annotation.JsonProperty;
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.data.mongodb.core.mapping.Document;
//
//import java.io.Serializable;
//
//@Getter
//@Setter
//@Document
//public class TransferDetails implements Serializable {
//
//    String fileName;
//    Long duration;
//
//    public TransferDetails(@JsonProperty("fileName") String fileName,
//                           @JsonProperty("duration") Long duration) {
//        this.fileName = fileName;
//        this.duration = duration;
//    }
//
//    public TransferDetails()
//    {
//        this.fileName = "testFile";
//        this.duration = 123l;
//    }
//
//    @Override
//    public String toString() {
//        return "TransferDetails{" +
//                "fileName='" + fileName + '\'' +
//                ", duration=" + duration +
//                '}';
//    }
//}