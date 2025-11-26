FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
ENV BASEDIR /etc/opt/cs/ecdsui/default
RUN apk add --no-cache nano
COPY ./docker_image/opt /opt
COPY ./docker_image/etc /etc
COPY ./docker_image/opt/cs/ecdsui/bin/docker-entrypoint.sh ${BASEDIR}/bin/
COPY ./docker_image/opt/cs/ecdsui/tls ${BASEDIR}/tls
# COPY ./docker_image/tls ${BASEDIR}/tls

RUN /bin/chmod 0755 ${BASEDIR}/bin/docker-entrypoint.sh
WORKDIR ${BASEDIR}
ENTRYPOINT ["/bin/sh", "/opt/cs/ecdsui/bin/docker-entrypoint.sh"]
# Default to run the service in server mode from /etc/opt/cs/ecdsui/default.
# Override the command and the working directory to run in portal or mobile mode.
CMD ["admin"]
VOLUME /var/opt/cs/ecdsui
VOLUME /opt/cs/ecdsui/
EXPOSE 8080/tcp
EXPOSE 8086/tcp
EXPOSE 8088/tcp
#admin: -p 8080
#test: not supported
#portal: -p 8086
#mobile: -p 8088
