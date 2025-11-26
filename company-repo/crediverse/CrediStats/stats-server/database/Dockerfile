# Use the official CouchDB image
FROM couchdb:latest

# Set environment variables if needed. This is just an example, and might not be required.
ENV COUCHDB_USER=admin
ENV COUCHDB_PASSWORD=password

# Copy docker.ini into the container
COPY docker.ini /opt/couchdb/etc/local.d/

# Expose the default CouchDB port
EXPOSE 5984

# Command to run CouchDB (this is typically defined in the base image, but just adding for clarity)
CMD ["couchdb"]


