FROM adoptopenjdk:11-jre-hotspot as builder
ARG JAR_FILE_WITHOUT=aas-registry-service
COPY target/${JAR_FILE_WITHOUT}-*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM adoptopenjdk:11-jre-hotspot
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
RUN true
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=logEvents,elasticsearchStorage
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher"]

