FROM fedora:36

LABEL description Robot Framework in Docker.

# Set the reports directory environment variable
ENV ROBOT_REPORTS_DIR /opt/robotframework/reports

# Set the tests directory environment variable
ENV ROBOT_TESTS_DIR /opt/robotframework/tests

# Set the working directory environment variable
ENV ROBOT_WORK_DIR /opt/robotframework/temp

# Define the default user who'll run the tests
ENV ROBOT_UID 1000
ENV ROBOT_GID 1000

# Dependency versions
ENV AXE_SELENIUM_LIBRARY_VERSION 2.1.6
ENV BROWSER_LIBRARY_VERSION 14.0.0
ENV CHROMIUM_VERSION 109.0.5414.74
ENV DATABASE_LIBRARY_VERSION 1.2.4
ENV REQUESTS_VERSION 0.9.2
ENV ROBOT_FRAMEWORK_VERSION 5.0
ENV SELENIUM_LIBRARY_VERSION 6.0.0
ENV SSH_LIBRARY_VERSION 3.8.0
ENV XVFB_VERSION 1.20
ENV SOAP_LIBRARY_VERSION 0.8
ENV JSON_LIBRARY_VERSION 0.3.1


# Prepare binaries to be executed
COPY scripts/chromedriver.sh /opt/robotframework/bin/chromedriver
COPY scripts/chromium-browser.sh /opt/robotframework/bin/chromium-browser
#COPY scripts/run-tests-in-virtual-screen.sh /opt/robotframework/bin/

# Install system dependencies
RUN dnf upgrade -y --refresh \
  && dnf install -y \
    chromedriver-${CHROMIUM_VERSION} \
    chromium-${CHROMIUM_VERSION} \
    npm \
    nodejs \
    python3-pip \
    tzdata \
    xorg-x11-server-Xvfb-${XVFB_VERSION}* \
  && dnf clean all

# FIXME: below is a workaround, as the path is ignored
RUN mv /usr/lib64/chromium-browser/chromium-browser /usr/lib64/chromium-browser/chromium-browser-original \
  && ln -sfv /opt/robotframework/bin/chromium-browser /usr/lib64/chromium-browser/chromium-browser

# Install Robot Framework and associated libraries
RUN pip3 install \
  --no-cache-dir \
  robotframework==$ROBOT_FRAMEWORK_VERSION \
  robotframework-browser==$BROWSER_LIBRARY_VERSION \
  robotframework-databaselibrary==$DATABASE_LIBRARY_VERSION \
  robotframework-requests==$REQUESTS_VERSION \
  robotframework-seleniumlibrary==$SELENIUM_LIBRARY_VERSION \
  robotframework-soaplibrary==$SOAP_LIBRARY_VERSION \
  robotframework-jsonlibrary==$JSON_LIBRARY_VERSION \
  pymysql \
  axe-selenium-python==$AXE_SELENIUM_LIBRARY_VERSION \
  PyYAML \
  && dnf remove -y \
    wget \
  && dnf clean all

# Install the Node dependencies for the Browser library
# FIXME: Playright currently doesn't support relying on system browsers, which is why the `--skip-browsers` parameter cannot be used here.
RUN rfbrowser init

# Create the default report and work folders with the default user to avoid runtime issues
# These folders are writeable by anyone, to ensure the user can be changed on the command line.
RUN mkdir -p ${ROBOT_REPORTS_DIR} \
  && mkdir -p ${ROBOT_WORK_DIR} \
  && chown ${ROBOT_UID}:${ROBOT_GID} ${ROBOT_REPORTS_DIR} \
  && chown ${ROBOT_UID}:${ROBOT_GID} ${ROBOT_WORK_DIR} \
  && chmod ugo+w ${ROBOT_REPORTS_DIR} ${ROBOT_WORK_DIR}

# Allow any user to write logs
RUN chmod ugo+w /var/log \
  && chown ${ROBOT_UID}:${ROBOT_GID} /var/log

# Update system path
ENV PATH=/opt/robotframework/bin:/opt/robotframework/drivers:$PATH

# Set up a volume for the generated reports
VOLUME ${ROBOT_REPORTS_DIR}

USER ${ROBOT_UID}:${ROBOT_GID}

# A dedicated work folder to allow for the creation of temporary files
WORKDIR ${ROBOT_WORK_DIR}

# Execute all robot tests
CMD ["/scripts/run_suite.sh"]
