package org.onedatashare.transfer.model.core;


import lombok.Data;
import lombok.experimental.Accessors;
import org.onedatashare.transfer.model.request.TransferJobRequest;
import org.onedatashare.transfer.model.request.TransferJobRequestWithMetaData;
import org.onedatashare.transfer.model.util.Time;
import org.onedatashare.transfer.model.util.Times;
import org.onedatashare.transfer.model.util.TransferInfo;
import org.springframework.data.annotation.Id;

@Data
@Accessors(chain = true)
public class TransferJob {
    @Id
    private String id;

    /** An ID meaningful to the user who owns the job. */
    private int job_id;

    private JobStatus status = JobStatus.scheduled;

    private TransferJobRequest.Source src;
    private TransferJobRequest.Destination dest;

    private String message;

    /** Byte progress of the transfer. */
    private TransferInfo bytes;
    /** File progress of the transfer. Currently unused. */
    private TransferInfo files;

    private int attempts = 1, max_attempts = 10;


    /** To mark job as deleted **/
    private boolean deleted = false;

    /** The owner of the job. */
    private String owner;

    /** Identifiers for jobs restarted using restart job functionality */
    public Boolean restartedJob = false;
    public Integer sourceJob = null;


    /** Times of various important events. */
    private Times times = new Times();

    public TransferJob(TransferJobRequest.Source src, TransferJobRequest.Destination dest) {
        this.src = src;
        this.dest = dest;
        this.bytes = new TransferInfo();
        status = JobStatus.scheduled;
    }

    public TransferJob updateJobWithTransferInfo(TransferInfo info) {
        setBytes(info);
        return this;
    }

    public TransferJob setStatus(JobStatus status) {
        if (status == null || status.isFilter)
            throw new Error("Cannot set job state to status: "+status);


        // Handle entering the new state.
        switch (this.status = status) {
            case scheduled:
                times.scheduled = Time.now();
                times.started = Time.now(); break;
            case transferring:
                times.started = Time.now(); break;
            case removed:
            case cancelled:
            case failed:
            case complete:
                times.completed = Time.now(); break;
        }
        return this;
    }
}
