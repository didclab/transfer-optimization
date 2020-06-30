export EUREKA_USER=admin
export EUREKA_PASS=admin
export BOOT_ADMIN_USER=default
export BOOT_ADMIN_PASS=default
export EUREKA_URI=localhost:8090
java -jar onedatashare-transfers-1.0-SNAPSHOT.jar -Xms 500m -Xmx 800m -XX:+UseG1GC
