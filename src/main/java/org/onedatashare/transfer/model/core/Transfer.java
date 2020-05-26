package org.onedatashare.transfer.model.core;

import lombok.NoArgsConstructor;
import org.apache.commons.vfs2.FileSystemManager;
import org.onedatashare.transfer.model.TransferDetails;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.request.TransferOptions;
import org.onedatashare.transfer.model.tap.Tap;
import org.onedatashare.transfer.model.util.Progress;
import org.onedatashare.transfer.model.util.Throughput;
import org.onedatashare.transfer.model.util.Time;
import org.onedatashare.transfer.model.util.TransferInfo;
import org.onedatashare.transfer.repository.TransferReportRepository;
import org.onedatashare.transfer.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


@NoArgsConstructor
@Service
public class Transfer<S extends Resource, D extends Resource> {
    protected FileSystemManager fileSystemManager;

    private String id;
    private S source;
    private D destination;
    private List<EntityInfo> filesToTransfer;
    private TransferOptions options;
    private EntityInfo sourceInfo;
    private EntityInfo destinationInfo;
    public static String fName = "";

    private AtomicInteger concurrency = new AtomicInteger(5);

    private ArrayList<Disposable> disposableArrayList = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(Transfer.class);

    private TransferReportRepository transferReportRepository;

    /**
     * Periodically updated information about the ongoing transfer.
     */
    private final TransferInfo info = new TransferInfo();

    // Timer counts 0.0 for files with very small size
    private Time timer;
    private long startTime;
    private Progress progress = new Progress();
    private Throughput throughput = new Throughput();

    public Transfer(S source, D destination) {
        this.source = source;
        this.destination = destination;
    }

    long fsize = 0l;

    public Flux start(int sliceSize) {
        fName = "This is from transfer.java";
//      transferDetailsRepository.saveAll(Flux.just(new TransferDetails("abc",12l))).subscribe();
        logger.info("Within transfer start");
        return Flux.fromIterable(this.filesToTransfer)
                .doOnSubscribe(s -> {
                    logger.info("Transfer started....");
                    this.startTime = Time.now();
                })

                .parallel(setParallelism())
                .runOn(Schedulers.elastic())
                .flatMap(file -> {
                    logger.info("Transferring----" + file.getPath() + file.getSize());
                    Tap tap;
                    try {
                        tap = this.source.getTap(this.sourceInfo, file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.error(file + "Unable to read from the tap - " + e.getMessage());
                        return Flux.empty();
                    }
                    Drain drain;
                    try {
                        drain = this.destination.getDrain(this.destinationInfo, file);
                    } catch (Exception e) {
                        logger.error(file + "Unable to create a new file drain - " + e.getMessage());
                        e.printStackTrace();
                        return Flux.empty();
                    }
                    Drain finalDrain = drain;
                    return tap.openTap(sliceSize)
                            .doOnNext(slice -> {
                                logger.info(file.getPath() + " slice received");
                                try {
                                    finalDrain.drain(slice);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                            .doOnComplete(() -> {
                                try {
                                    logger.info("Done transferring " + file.getPath());
                                    finalDrain.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                })
                .sequential()
                .doOnComplete(() -> {
                    this.startTime = Time.now() - this.startTime;
                    logger.info("Done transferring " + this.id + ". Took " + startTime / 1000 + " secs");
                })
                .map(__ -> {
                    transferReportRepository.save(new TransferDetails("abc", 123l));
                    return Flux.empty();
                });
    }

    public int setParallelism() {
        int parallelism = 2;
        double totalSize = 0.0;

        for (EntityInfo ei : filesToTransfer) {
            System.out.println(ei.getPath()+"--"+ei.getSize());
            totalSize += ei.getSize();
        }
        totalSize /= 1024;
        totalSize /= 1024;

        System.out.println("totalsize is :" + totalSize);
        if (totalSize >= 0.25 && totalSize <= 25)
            parallelism = 8;
        else if (totalSize >= 26 && totalSize <= 100)
            parallelism = 4;
        else if (totalSize >= 101 && totalSize <= 600)
            parallelism = 8;
        else if (totalSize > 600)
            parallelism = 5;
        else
            parallelism = 2;
        System.out.println("Concurrency is : " + parallelism);
        return parallelism;
    }


    public TransferInfo addProgress(Slice slice) {
        long size = slice.length();
        progress.add(size);
        throughput.update(size);
        info.update(timer, progress, throughput);
        return info;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<EntityInfo> getFilesToTransfer() {
        return filesToTransfer;
    }

    public void setFilesToTransfer(List<EntityInfo> filesToTransfer) {
        this.filesToTransfer = filesToTransfer;
    }

    public EntityInfo getSourceInfo() {
        return sourceInfo;
    }

    public void setSourceInfo(EntityInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    public EntityInfo getDestinationInfo() {
        return destinationInfo;
    }

    public void setDestinationInfo(EntityInfo destinationInfo) {
        this.destinationInfo = destinationInfo;
    }
}
