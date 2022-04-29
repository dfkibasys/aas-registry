# see https://www.baeldung.com/spring-boot-docker-images#layered-jars 
FROM adoptopenjdk:11-jre-hotspot as builder
ARG JAR_FILE_WITHOUT=aas-registry-service
COPY target/${JAR_FILE_WITHOUT}-*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk:11-jre-hotspot
RUN mkdir /workspace
COPY --from=builder dependencies/ /workspace
COPY --from=builder snapshot-dependencies/ /workspace
RUN true
COPY --from=builder spring-boot-loader/ /workspace
COPY --from=builder application/ /workspace
EXPOSE 8080
WORKDIR /workspace
ENV SPRING_PROFILES_ACTIVE=kafkaEvents,elasticsearchStorage
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]


