# Stage 1: Build the Angular app
FROM marcaureln/volta:2.0.1 AS angular-build
ENV VOLTA_FEATURE_PNPM=1

WORKDIR /angular-app

COPY ./booklore-ui/package.json  ./
RUN pnpm install

COPY ./booklore-ui /angular-app/
RUN pnpm build --configuration=production

# Stage 2: Build the Spring Boot app with Gradle
FROM amazoncorretto:25-alpine-jdk AS springboot-build

WORKDIR /springboot-app

COPY ./booklore-api /springboot-app/
RUN /springboot-app/gradlew clean build -x test

# Stage 3: Final image
FROM amazoncorretto:25-alpine

RUN apk update && apk add nginx  \
    && apk add envsubst \
    && apk add su-exec \
    && rm -rf /var/cache/apk/*

COPY ./start.sh /start.sh
COPY ./nginx.conf /etc/nginx/nginx.conf

COPY --from=angular-build /angular-app/dist/booklore/browser /usr/share/nginx/html
COPY --from=springboot-build /springboot-app/build/libs/booklore-api-0.0.1-SNAPSHOT.jar /app/app.jar

RUN chmod +x /start.sh

EXPOSE 8080 80

CMD ["/start.sh"]