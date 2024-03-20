FROM eclipse-temurin:11
WORKDIR /deepmooc-backend

COPY build/libs/deepmooc-backend-*all.jar app.jar

CMD ["java", "-jar", "app.jar"]