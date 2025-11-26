FROM    quay.io/coreos/etcd:v3.5.4
EXPOSE 2379:2379
# ENV ALLOW_NONE_AUTHENTICATION=yes
ENV ETCD_ADVERTISE_CLIENT_URLS=https://0.0.0.0:2379
ENV ETCD_LISTEN_CLIENT_URLS=https://0.0.0.0:2379
ENV ETCD_TRUSTED_CA_FILE=/etc/ssl/certs/concurrent_ca.crt
ENV ETCD_CERT_FILE=/etc/ssl/certs/server.crt
ENV ETCD_KEY_FILE=/etc/ssl/certs/server.key
# ENV ETCD_AUTO_TLS=true
ENV ETCD_ROOT_PASSWORD=kooskoos
# ENV ETCD_CLIENT_CERT_ALLOWED_HOSTNAME=true
ENV ETCD_CLIENT_CERT_AUTH=true
# ENV ETCD_DEBUG=true
# ENV ETCD_LOG_LEVEL=debug

RUN mkdir -p /etc/ssl/certs
COPY tls/ /etc/ssl/certs/

