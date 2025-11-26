FROM node:18

RUN apt-get -y update
RUN apt-get -y install runit

# Create app directory
WORKDIR /app

# Bundle app source
COPY . .

# Install app dependencies
#COPY package*.json ./

#RUN npm install
# If you are building your code for production
RUN npm ci --omit=dev

RUN mkdir -p /var/log/credistats-server
ADD run-server.sh /app
RUN chmod +x /app/run-server.sh

EXPOSE 8801
CMD [ "/bin/bash", "/app/run-server.sh"  ]

