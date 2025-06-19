# Этап сборки
#FROM gradle:8.10-jdk21 AS builder
#
#WORKDIR /app
#
## Копируем только файлы конфигурации Gradle для кэширования зависимостей
#COPY build.gradle settings.gradle /app/
#COPY gradle /app/gradle
#
## Загружаем зависимости (кэшируем слой)
#RUN gradle build --no-daemon || true
#
## Копируем исходный код
#COPY src /app/src
#
## Выполняем сборку приложения
#RUN gradle installDist --no-daemon
#
## Этап выполнения
#FROM eclipse-temurin:21-jre-alpine
#
#WORKDIR /app
#
## Копируем только необходимые файлы из этапа сборки
#COPY --from=builder /app/build/install/app /app
#
## Указываем команду для запуска приложения
#CMD ["./bin/app"]

FROM gradle:8.10-jdk21

WORKDIR /app

COPY /app .

RUN gradle installDist

CMD ./build/install/app/bin/app