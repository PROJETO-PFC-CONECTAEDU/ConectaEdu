# Estágio de Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o pom.xml e as dependências primeiro para aproveitar o cache
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests && \
    cp target/api-0.0.1-SNAPSHOT.jar app.jar

# Estágio de Execução
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS="-Xms128m -Xmx200m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
