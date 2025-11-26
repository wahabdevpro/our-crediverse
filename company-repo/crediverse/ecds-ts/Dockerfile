FROM adoptopenjdk/openjdk11:jdk-11.0.11_9-alpine
ENV BASEDIR /opt/cs/c4u
RUN apk add --no-cache tzdata
RUN apk add --no-cache mysql-client
RUN apk add --no-cache nano
RUN apk add --no-cache bash
RUN apk add --no-cache mlocate
RUN updatedb
COPY ./docker_image/opt /opt
COPY ./docker_image/var /var
COPY ./docker_image/build/resources/main/bin/c4u-hostprocess.sh ${BASEDIR}/bin/
COPY ./docker_image/tls ${BASEDIR}/tls
RUN /bin/chmod 0755  ${BASEDIR}/bin/c4u-hostprocess.sh
#license file symlink
RUN mkdir -p /opt/cs/c4u/1.0/hostprocess/license
RUN ln -s /opt/cs/c4u/1.0/hostprocess/plugins/registration.lic /opt/cs/c4u/1.0/hostprocess/license/registration.lic
RUN mkdir -p /var/opt/cs/c4u/log
WORKDIR /var/opt/cs/c4u
ENTRYPOINT ["/bin/sh", "/opt/cs/c4u/bin/c4u-hostprocess.sh"]
# Default to run the service in server mode.
CMD ["server"]
#Other supported option is "gui"
#Example: CMD ["gui -h <host>"]
VOLUME /var/opt/cs/c4u/
VOLUME /opt/cs/c4u/1.0
#license file must be copied to the following location: /opt/cs/c4u/1.0/hostprocess/license/registration.lic (required for server mode only)
#i.e. cp registration.lic /var/lib/docker/volumes/{volumeID}/_data/hostprocess/license
EXPOSE 3301/tcp
EXPOSE 8082/tcp
EXPOSE 10101/tcp
EXPOSE 10012/tcp
EXPOSE 14000/tcp
EXPOSE 14100/tcp
EXPOSE 14200/tcp
EXPOSE 14400/tcp
EXPOSE 16100/tcp
#server: -p 3301 -p 10101 -p 10012 -p 14000 -p 14100 -p 14200 -p 14400 -p 16100
#gui: -p 8082
