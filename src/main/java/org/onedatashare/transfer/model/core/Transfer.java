package org.onedatashare.transfer.model.core;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.onedatashare.transfer.model.drain.Drain;
import org.onedatashare.transfer.model.tap.Tap;
import org.onedatashare.transfer.model.util.Progress;
import org.onedatashare.transfer.model.util.Throughput;
import org.onedatashare.transfer.model.util.Time;
import org.onedatashare.transfer.model.util.TransferInfo;
import org.onedatashare.transfer.module.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.List;


@NoArgsConstructor
@Data
public class Transfer {
    public Resource source;
    public Resource destination;

    public List<IdMap> filesToTransfer;

    private static final Logger logger = LoggerFactory.getLogger(Transfer.class);

    /** Periodically updated information about the ongoing transfer. */
    public final TransferInfo info = new TransferInfo();

    // Timer counts 0.0 for files with very small size
    protected Time timer;
    protected Progress progress = new Progress();
    protected Throughput throughput = new Throughput();

    public Transfer(Resource source, Resource destination){
        this.source = source;
        this.destination = destination;
    }

  public Flux<TransferInfo> start(int sliceSize){
        return Flux.fromIterable(filesToTransfer)
                .doOnSubscribe(s -> startTimer())
                .flatMap(id -> {
                    Tap tap = source.getTap(id);
                    Drain drain = destination.getDrain(id);
                    return tap.openTap(sliceSize)
                            .doOnNext(slice -> {
                                try {
                                    drain.drain(slice);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                            .doOnError(err -> logger.error(err.getMessage()))
                            .map(this::addProgress)
                            .doOnComplete(() -> {
                                try {
                                    drain.finish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                })
                .doFinally(s -> done());
  }

//    public Flux<TransferInfo> start(Long sliceSize) {
//        // HTTP is read only
//        if(destination instanceof HttpResourceOld)
//            return Flux.error(new Exception("HTTP is read-only"));
//
//        Stat tapStat = (Stat)source.getTransferStat().block();
//        info.setTotal(tapStat.getSize());


//        return Flux.fromIterable(tapStat.getFilesList())
//                .doOnSubscribe(s -> startTimer())
//                .flatMap(fileStat -> {
//                    final Drain drain;
//                    if( destination instanceof BoxResourceOld){
//                        drain =  ((BoxResourceOld)destination).sink(fileStat, tapStat.isDir());
//                    }
//                    else if(tapStat.isDir())
//                        drain = destination.sink(fileStat);
//                    else {
//                        drain = destination.sink();
//                    }
//                    return source.tap().tap(fileStat, sliceSize)
//                            .subscribeOn(Schedulers.elastic())
//                            .doOnNext(drain::drain)
//                            .subscribeOn(Schedulers.elastic())
//                            .map(this::addProgress)
//                            .doOnComplete(drain::finish);
//                }).doFinally(s -> done());
//        return null;
//    }

//    public void initialize() {
//        Stat stat = (Stat) source.stat().block();
//        info.setTotal(stat.getSize());
//    }

    public void initializeUpload(int fileSize){
        info.setTotal(fileSize);
    }

    public void done() {
        timer.stop();
    }

    public void startTimer() {
        timer = new Time();
    }

    public TransferInfo addProgress(Slice slice) {
        long size = slice.length();
        progress.add(size);
        throughput.update(size);
        info.update(timer, progress, throughput);
        return info;
    }

    public TransferInfo addProgressSize(Long totalSize) {
        long size = totalSize - progress.total();
        progress.add(size);
        throughput.update(size);
        info.update(timer, progress, throughput);
        return info;
    }

}
