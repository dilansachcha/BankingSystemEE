# Use GlassFish 7 from GitHub Container Registry
FROM ghcr.io/eclipse-ee4j/glassfish:7.0.23

# Deploy WAR built by Maven
COPY target/BankingSystemEE-1.0-SNAPSHOT.war /opt/glassfish7/glassfish/domains/domain1/autodeploy/

# Expose GlassFish HTTP port
EXPOSE 8080
