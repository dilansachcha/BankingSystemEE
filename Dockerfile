FROM ghcr.io/eclipse-ee4j/glassfish:7.0.23

USER root

RUN apt-get update && apt-get install -y ca-certificates && update-ca-certificates

RUN wget https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar -P /opt/glassfish7/glassfish/domains/domain1/lib/ext/

COPY target/BankingSystemEE-1.0-SNAPSHOT.war /opt/glassfish7/glassfish/domains/domain1/autodeploy/

EXPOSE 8080