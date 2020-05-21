package org.onedatashare.transfer.consumer;

import com.google.gson.Gson;
import org.onedatashare.transfer.Message.MqMessage;
import org.onedatashare.transfer.TransferApplication;
import org.onedatashare.transfer.model.request.TransferJobRequestWithMetaData;
import org.onedatashare.transfer.service.TransferService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MQConsumer {

    @Autowired
    private TransferService transferService;

    @RabbitListener(queues = TransferApplication.DEFAULT_PARSING_QUEUE)
    public Mono<Void> consumeDefaultMessage(final MqMessage message) {
        System.out.println("Received message, tip is: {}" + message.toString());
        Gson g = new Gson();
        TransferJobRequestWithMetaData recData = g.fromJson(message.getText(), TransferJobRequestWithMetaData.class);
        System.out.println(recData.toString());
        return transferService.submit(recData);
    }
}
