FROM eclipse-temurin:21
WORKDIR /deepmooc-backend

COPY build/libs/deepmooc-backend-*all.jar app.jar

CMD ["java", "-jar", "app.jar"]