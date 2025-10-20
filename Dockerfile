# PayTR Test Automation Docker Image
FROM openjdk:11-jdk-slim

# Metadata
LABEL maintainer="PayTR Test Team"
LABEL description="PayTR Test Automation Environment"
LABEL version="1.0"

# Environment variables
ENV JAVA_HOME=/usr/local/openjdk-11
ENV MAVEN_HOME=/opt/maven
ENV ALLURE_HOME=/opt/allure
ENV PATH=$JAVA_HOME/bin:$MAVEN_HOME/bin:$ALLURE_HOME/bin:$PATH

# Install system dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    xvfb \
    x11vnc \
    fluxbox \
    wmctrl \
    gnupg2 \
    software-properties-common \
    apt-transport-https \
    ca-certificates \
    fonts-liberation \
    libasound2 \
    libatk-bridge2.0-0 \
    libatk1.0-0 \
    libatspi2.0-0 \
    libdrm2 \
    libgtk-3-0 \
    libnspr4 \
    libnss3 \
    libx11-xcb1 \
    libxcomposite1 \
    libxdamage1 \
    libxrandr2 \
    xdg-utils \
    && rm -rf /var/lib/apt/lists/*

# Install Chrome
RUN wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" > /etc/apt/sources.list.d/google-chrome.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Install Firefox
RUN apt-get update && apt-get install -y firefox-esr \
    && rm -rf /var/lib/apt/lists/*

# Install Maven
RUN wget -q https://archive.apache.org/dist/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz \
    && tar -xzf apache-maven-3.8.6-bin.tar.gz -C /opt \
    && mv /opt/apache-maven-3.8.6 $MAVEN_HOME \
    && rm apache-maven-3.8.6-bin.tar.gz

# Install Allure
RUN wget -q https://github.com/allure-framework/allure2/releases/download/2.20.1/allure-2.20.1.tgz \
    && tar -xzf allure-2.20.1.tgz -C /opt \
    && mv /opt/allure-2.20.1 $ALLURE_HOME \
    && rm allure-2.20.1.tgz

# Install ChromeDriver
RUN CHROME_VERSION=$(google-chrome --version | awk '{print $3}' | cut -d. -f1) \
    && CHROMEDRIVER_VERSION=$(curl -s "https://chromedriver.storage.googleapis.com/LATEST_RELEASE_${CHROME_VERSION}") \
    && wget -q "https://chromedriver.storage.googleapis.com/${CHROMEDRIVER_VERSION}/chromedriver_linux64.zip" \
    && unzip chromedriver_linux64.zip \
    && mv chromedriver /usr/local/bin/ \
    && chmod +x /usr/local/bin/chromedriver \
    && rm chromedriver_linux64.zip

# Install GeckoDriver (Firefox)
RUN GECKODRIVER_VERSION=$(curl -s "https://api.github.com/repos/mozilla/geckodriver/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/') \
    && wget -q "https://github.com/mozilla/geckodriver/releases/download/${GECKODRIVER_VERSION}/geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz" \
    && tar -xzf geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz \
    && mv geckodriver /usr/local/bin/ \
    && chmod +x /usr/local/bin/geckodriver \
    && rm geckodriver-${GECKODRIVER_VERSION}-linux64.tar.gz

# Create test user
RUN useradd -m -s /bin/bash testuser \
    && usermod -aG sudo testuser

# Create directories
RUN mkdir -p /app/test-results \
    && mkdir -p /app/screenshots \
    && mkdir -p /app/logs \
    && mkdir -p /app/reports \
    && chown -R testuser:testuser /app

# Set working directory
WORKDIR /app

# Copy project files
COPY pom.xml ./
COPY src ./src/
COPY testng*.xml ./

# Change ownership
RUN chown -R testuser:testuser /app

# Switch to test user
USER testuser

# Download Maven dependencies
RUN mvn dependency:go-offline -B

# Create entrypoint script
USER root
RUN cat > /entrypoint.sh << 'EOF'
#!/bin/bash

# Start Xvfb for headless browser testing
export DISPLAY=:99
Xvfb :99 -screen 0 1920x1080x24 &

# Wait for Xvfb to start
sleep 2

# Start window manager
fluxbox &

# Function to run tests
run_tests() {
    local TEST_SUITE=${1:-comprehensive}
    local BROWSER=${2:-chrome}
    local ENVIRONMENT=${3:-staging}
    local HEADLESS=${4:-true}
    
    echo "🚀 Starting PayTR Test Automation..."
    echo "📋 Test Suite: $TEST_SUITE"
    echo "🌐 Browser: $BROWSER"
    echo "🏗️ Environment: $ENVIRONMENT"
    echo "👁️ Headless: $HEADLESS"
    
    # Run tests
    mvn clean test \
        -Dsurefire.suiteXmlFiles=testng-${TEST_SUITE}.xml \
        -Dbrowser=${BROWSER} \
        -Denvironment=${ENVIRONMENT} \
        -Dheadless=${HEADLESS} \
        -Dallure.results.directory=test-results/allure-results \
        -Dmaven.test.failure.ignore=true
    
    # Generate Allure report
    if [ -d "test-results/allure-results" ] && [ "$(ls -A test-results/allure-results)" ]; then
        echo "📊 Generating Allure report..."
        allure generate test-results/allure-results --clean --output reports/allure-report
        echo "✅ Allure report generated at: reports/allure-report"
    fi
    
    # Copy screenshots and logs
    if [ -d "screenshots" ]; then
        cp -r screenshots/* test-results/ 2>/dev/null || true
    fi
    
    if [ -d "logs" ]; then
        cp -r logs/* test-results/ 2>/dev/null || true
    fi
    
    echo "🏁 Test execution completed!"
}

# Check command line arguments
if [ $# -eq 0 ]; then
    echo "Usage: docker run paytr-tests [TEST_SUITE] [BROWSER] [ENVIRONMENT] [HEADLESS]"
    echo "Example: docker run paytr-tests comprehensive chrome staging true"
    echo "Running with default parameters..."
    run_tests
else
    run_tests "$@"
fi
EOF

RUN chmod +x /entrypoint.sh

# Switch back to test user
USER testuser

# Expose ports for VNC (optional)
EXPOSE 5900

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Set entrypoint
ENTRYPOINT ["/entrypoint.sh"]

# Default command
CMD ["comprehensive", "chrome", "staging", "true"]