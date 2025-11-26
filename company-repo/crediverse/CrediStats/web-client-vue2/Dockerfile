# Use an official Node.js runtime as the base image
FROM node:18 as build-stage

# Set the working directory in the container
WORKDIR /app

# Copy package.json and package-lock.json to the working directory
COPY package*.json ./

# Install the app dependencies
RUN npm install

# Copy the entire app to the container
COPY . .

# Build the Vue.js app for production
RUN npm run build

# Use a lightweight Node.js runtime as the base image for the production stage
FROM node:18-alpine as production-stage

RUN apk update
RUN apk add runit

# Set the working directory in the container
WORKDIR /app

RUN mkdir -p /var/log/credistats-portal
ADD ./run-portal.sh .
RUN chmod +x ./run-portal.sh

# Copy the built app from the build-stage
COPY --from=build-stage /app/dist .
#COPY ./dist .

# Install serve to run the app
RUN npm install -g serve

# Expose port 80
EXPOSE 8802

# Define the command to start the app
CMD ["/bin/sh", "/app/run-portal.sh"]
