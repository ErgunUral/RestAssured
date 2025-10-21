# PayTR Enhanced Test Suite Docker Image
# Multi-stage build for optimized test execution environment

# Stage 1: Base image with Java and Maven
FROM maven:3.9.4-eclipse-temurin-17 AS base

# Set working directory
WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    gnupg \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Stage 2: Browser installation
FROM base AS browser-setup

# Install Chrome
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Install Firefox
RUN apt-get update \
    && apt-get install -y firefox-esr \
    && rm -rf /var/lib/apt/lists/*

# Install ChromeDriver
RUN CHROME_DRIVER_VERSION=$(curl -sS chromedriver.storage.googleapis.com/LATEST_RELEASE) \
    && wget -O /tmp/chromedriver.zip http://chromedriver.storage.googleapis.com/$CHROME_DRIVER_VERSION/chromedriver_linux64.zip \
    && unzip /tmp/chromedriver.zip chromedriver -d /usr/local/bin/ \
    && rm /tmp/chromedriver.zip \
    && chmod +x /usr/local/bin/chromedriver

# Install GeckoDriver for Firefox
RUN GECKO_DRIVER_VERSION=$(curl -s "https://api.github.com/repos/mozilla/geckodriver/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/') \
    && wget -O /tmp/geckodriver.tar.gz "https://github.com/mozilla/geckodriver/releases/download/$GECKO_DRIVER_VERSION/geckodriver-$GECKO_DRIVER_VERSION-linux64.tar.gz" \
    && tar -xzf /tmp/geckodriver.tar.gz -C /usr/local/bin/ \
    && rm /tmp/geckodriver.tar.gz \
    && chmod +x /usr/local/bin/geckodriver

# Stage 3: Test environment setup
FROM browser-setup AS test-env

# Set environment variables
ENV MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
ENV DISPLAY=:99
ENV CHROME_BIN=/usr/bin/google-chrome
ENV FIREFOX_BIN=/usr/bin/firefox

# Install Xvfb for headless browser testing
RUN apt-get update \
    && apt-get install -y xvfb \
    && rm -rf /var/lib/apt/lists/*

# Create test user (non-root for security)
RUN groupadd -r testuser && useradd -r -g testuser -s /bin/bash testuser \
    && mkdir -p /home/testuser \
    && chown -R testuser:testuser /home/testuser \
    && chown -R testuser:testuser /app

# Copy project files
COPY --chown=testuser:testuser pom.xml /app/
COPY --chown=testuser:testuser src/ /app/src/
COPY --chown=testuser:testuser scripts/ /app/scripts/
COPY --chown=testuser:testuser *.xml /app/

# Make scripts executable
RUN chmod +x /app/scripts/*.sh

# Switch to test user
USER testuser

# Download dependencies (cache layer)
RUN mvn dependency:go-offline -B

# Stage 4: Final runtime image
FROM test-env AS runtime

# Create directories for reports and logs
RUN mkdir -p /app/target/enhanced-reports \
    && mkdir -p /app/target/logs \
    && mkdir -p /app/target/screenshots \
    && mkdir -p /app/target/allure-results

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Expose port for potential web server
EXPOSE 8080

# Default command
CMD ["./scripts/run-enhanced-tests.sh", "--suite", "smoke-enhanced", "--headless"]

# Labels for metadata
LABEL maintainer="PayTR Test Team"
LABEL version="1.0"
LABEL description="PayTR Enhanced Test Suite Docker Image"
LABEL test.suite.version="2.0"
LABEL test.categories="payment,security,performance,multi-currency,3d-secure,fraud-detection,webhook,accessibility,edge-case,chaos-engineering,business-logic,data-migration"