FROM openjdk:17.0.1-oraclelinux7
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","${JAVA_OPTS}", "-jar","/app.jar"]
