package org.onedatashare.transfer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.log4j.BasicConfigurator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "OneDataShare Transfer Optimization API",
                version = "1.0",
                description = "OpenAPI REST API documentation",
                license = @License(name = "Apache-2.0", url = "https://github.com/didclab/onedatashare/blob/master/LICENSE"),
                contact = @Contact(url = "http://onedatashare.org", name = "OneDataShare Team", email = "admin@onedatashare.org")
        ),
        servers = {
                @Server(
                        description = "ODS Transfer Optimization",
                        url = "http://onedatashare.org"
                )
        }
)
@SpringBootApplication
public class TransferApplication {
  public static void main(String[] args) {
    BasicConfigurator.configure();
    SpringApplication.run(TransferApplication.class, args);
  }
}
