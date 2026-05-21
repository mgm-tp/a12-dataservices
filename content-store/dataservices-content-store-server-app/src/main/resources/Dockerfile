ARG REGISTRY
ARG BASE_IMAGE

FROM $REGISTRY/$BASE_IMAGE AS jar_extraction
ARG JAR_FILE
COPY $JAR_FILE app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

FROM $REGISTRY/$BASE_IMAGE
ARG JAVA_OPTS
ARG APP_VERSION
RUN set -eux; \
        groupadd -r a12 --gid=1001; \
        useradd -r -g a12 --uid=1001 --home-dir=/var/lib/a12 a12; \
        mkdir -p /var/lib/a12/application; \
        chown -R a12:a12 /var/lib/a12;
RUN mkdir -p /licensing
COPY --from=jar_extraction /extracted/application/META-INF/LICENSE /licensing/LICENSE
COPY --from=jar_extraction /extracted/application/META-INF/NOTICE /licensing/NOTICE
COPY --from=jar_extraction /extracted/application/META-INF/THIRD_PARTY_NOTICES /licensing/THIRD_PARTY_NOTICES
COPY --from=jar_extraction /extracted/application/META-INF/licenses/ /licensing/licenses/
LABEL org.opencontainers.image.title="a12-content-store"
LABEL org.opencontainers.image.version="$APP_VERSION"
LABEL org.opencontainers.image.licenses="EUPL-1.2 OR LicenseRef-commercial"
LABEL org.opencontainers.image.authors="mgm technology partners GmbH"
LABEL org.opencontainers.image.source="https://github.com/mgm-tp"
USER a12
WORKDIR /var/lib/a12/application
COPY --from=jar_extraction --chown=a12:a12 extracted/dependencies/ ./
COPY --from=jar_extraction --chown=a12:a12 extracted/spring-boot-loader/ ./
COPY --from=jar_extraction --chown=a12:a12 extracted/snapshot-dependencies/ ./
COPY --from=jar_extraction --chown=a12:a12 extracted/application/ ./
CMD java org.springframework.boot.loader.launch.JarLauncher $JAVA_OPTS
