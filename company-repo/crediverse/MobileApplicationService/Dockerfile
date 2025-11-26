FROM rust:latest

RUN apt-get -y update

RUN apt-get -y install apt-utils runit wget

RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v23.2/protoc-23.2-linux-x86_64.zip

RUN unzip protoc-23.2-linux-x86_64.zip -d /usr/local/

ARG mas_api_port=5000 

EXPOSE $mas_api_port

ENV MAS_API_PORT=$mas_api_port 

RUN mkdir -p /var/log/mas

COPY mobile_application_service.sh /usr/local/bin/
RUN chmod +x /usr/local/bin/mobile_application_service.sh

RUN echo $PATH

RUN mkdir src

WORKDIR /usr/src/mobile_application_service

ARG CI_DOCKER_TAG
ENV CI_DOCKER_TAG $CI_DOCKER_TAG

ARG CI_GITHUB_TAG
ENV CI_GITHUB_TAG $CI_GITHUB_TAG

ARG CI_BRANCH_NAME
ENV CI_BRANCH_NAME $CI_BRANCH_NAME

ARG CI_BUILD_NUMBER
ENV CI_BUILD_NUMBER $CI_BUILD_NUMBER

ARG CI_BUILD_DATETIME
ENV CI_BUILD_DATETIME $CI_BUILD_DATETIME

ARG CI_COMMIT_REF
ENV CI_COMMIT_REF $CI_COMMIT_REF

COPY . .

RUN cargo install --path . --bin dummy

COPY . .

RUN cargo install --profile release --path . --locked 

RUN mkdir -p output/feedback

CMD ["mobile_application_service.sh"]

