FROM rust:latest

RUN apt-get -y update

RUN apt-get -y install apt-utils libprotoc-dev libprotoc23 wget runit

RUN wget https://github.com/protocolbuffers/protobuf/releases/download/v23.2/protoc-23.2-linux-x86_64.zip
RUN unzip protoc-23.2-linux-x86_64.zip -d /usr/local/

ARG team_service_api_port=5100 

EXPOSE $team_service_api_port

ENV TEAM_SERVICE_API_PORT=$team_service_api_port

WORKDIR /usr/src/team_service_api

RUN mkdir src
ADD Cargo.toml .
ADD src/dummy.rs src/

RUN cargo install --path . --bin dummy

COPY . .

RUN cargo install --profile release --path . --locked 

CMD ["team-service"]

