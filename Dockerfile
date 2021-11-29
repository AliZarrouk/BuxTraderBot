FROM openjdk:17.0.1-oraclelinux7
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
COPY input input
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar --product-buy-sell-file=input"]
