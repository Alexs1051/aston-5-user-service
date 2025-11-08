FROM eclipse-temurin:17-jre

WORKDIR /app

# Копируем все JAR файлы и переименовываем
COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]