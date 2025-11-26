FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
ENV BASEDIR /etc/opt/cs/ecdsapi/default
RUN apk add --no-cache nano
COPY ./docker_image/opt /opt
COPY ./docker_image/etc /etc
COPY ./docker_image/opt/cs/ecdsapi/bin/docker-entrypoint.sh ${BASEDIR}/bin/
RUN /bin/chmod 0755 ${BASEDIR}/bin/docker-entrypoint.sh
WORKDIR ${BASEDIR}
ENTRYPOINT ["/bin/sh", "/opt/cs/ecdsapi/bin/docker-entrypoint.sh"]
# Default to run the service in server mode.
CMD ["admin"]
VOLUME /var/opt/cs/ecdsapi
VOLUME /opt/cs/ecdsapi/
EXPOSE 9084/tcp
