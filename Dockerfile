# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the source code and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the jar file from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port (Render will set PORT env var, but good for documentation)
EXPOSE 9090

# Command to run the application
# Low RAM config suggestion: -Xms128m -Xmx256m -XX:+UseSerialGC -Xss512k -XX:ReservedCodeCacheSize=64M
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseSerialGC -Xss512k -XX:ReservedCodeCacheSize=64M"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
