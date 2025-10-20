#!/bin/bash

# PayTR Test Automation Execution Script
# Bu script PayTR test süitlerini çalıştırmak için kullanılır

set -e  # Exit on any error

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Default values
DEFAULT_TEST_SUITE="comprehensive"
DEFAULT_BROWSER="chrome"
DEFAULT_ENVIRONMENT="staging"
DEFAULT_HEADLESS="true"
DEFAULT_PARALLEL="true"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging function
log() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] ✅ $1${NC}"
}

log_warning() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] ⚠️  $1${NC}"
}

log_error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ❌ $1${NC}"
}

# Help function
show_help() {
    cat << EOF
PayTR Test Automation Execution Script

Usage: $0 [OPTIONS]

OPTIONS:
    -s, --suite SUITE       Test suite to run (comprehensive|security|performance|api|smoke)
                           Default: $DEFAULT_TEST_SUITE
    
    -b, --browser BROWSER   Browser to use (chrome|firefox|edge)
                           Default: $DEFAULT_BROWSER
    
    -e, --env ENVIRONMENT   Test environment (staging|production|development)
                           Default: $DEFAULT_ENVIRONMENT
    
    -h, --headless         Run in headless mode (true|false)
                           Default: $DEFAULT_HEADLESS
    
    -p, --parallel         Enable parallel execution (true|false)
                           Default: $DEFAULT_PARALLEL
    
    -d, --docker           Run tests in Docker container
    
    -g, --grid             Use Selenium Grid
    
    -r, --report-only      Only generate reports from existing results
    
    -c, --clean            Clean previous test results before running
    
    --help                 Show this help message

EXAMPLES:
    # Run comprehensive tests with Chrome in staging
    $0 -s comprehensive -b chrome -e staging
    
    # Run security tests in headless mode
    $0 -s security -h true
    
    # Run tests in Docker
    $0 -d -s performance -b firefox
    
    # Run with Selenium Grid
    $0 -g -s api -p true
    
    # Clean and run smoke tests
    $0 -c -s smoke

EOF
}

# Parse command line arguments
parse_arguments() {
    TEST_SUITE="$DEFAULT_TEST_SUITE"
    BROWSER="$DEFAULT_BROWSER"
    ENVIRONMENT="$DEFAULT_ENVIRONMENT"
    HEADLESS="$DEFAULT_HEADLESS"
    PARALLEL="$DEFAULT_PARALLEL"
    USE_DOCKER=false
    USE_GRID=false
    REPORT_ONLY=false
    CLEAN_RESULTS=false

    while [[ $# -gt 0 ]]; do
        case $1 in
            -s|--suite)
                TEST_SUITE="$2"
                shift 2
                ;;
            -b|--browser)
                BROWSER="$2"
                shift 2
                ;;
            -e|--env)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -h|--headless)
                HEADLESS="$2"
                shift 2
                ;;
            -p|--parallel)
                PARALLEL="$2"
                shift 2
                ;;
            -d|--docker)
                USE_DOCKER=true
                shift
                ;;
            -g|--grid)
                USE_GRID=true
                shift
                ;;
            -r|--report-only)
                REPORT_ONLY=true
                shift
                ;;
            -c|--clean)
                CLEAN_RESULTS=true
                shift
                ;;
            --help)
                show_help
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
}

# Validate arguments
validate_arguments() {
    # Validate test suite
    case $TEST_SUITE in
        comprehensive|security|performance|api|smoke)
            ;;
        *)
            log_error "Invalid test suite: $TEST_SUITE"
            log "Valid options: comprehensive, security, performance, api, smoke"
            exit 1
            ;;
    esac

    # Validate browser
    case $BROWSER in
        chrome|firefox|edge)
            ;;
        *)
            log_error "Invalid browser: $BROWSER"
            log "Valid options: chrome, firefox, edge"
            exit 1
            ;;
    esac

    # Validate environment
    case $ENVIRONMENT in
        staging|production|development)
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT"
            log "Valid options: staging, production, development"
            exit 1
            ;;
    esac
}

# Setup directories
setup_directories() {
    log "Setting up directories..."
    
    cd "$PROJECT_DIR"
    
    # Create necessary directories
    mkdir -p target/surefire-reports
    mkdir -p target/allure-results
    mkdir -p target/allure-report
    mkdir -p screenshots
    mkdir -p logs
    mkdir -p reports
    
    log_success "Directories created successfully"
}

# Clean previous results
clean_previous_results() {
    if [ "$CLEAN_RESULTS" = true ]; then
        log "Cleaning previous test results..."
        
        rm -rf target/surefire-reports/*
        rm -rf target/allure-results/*
        rm -rf target/allure-report/*
        rm -rf screenshots/*
        rm -rf logs/*
        rm -rf reports/*
        
        log_success "Previous results cleaned"
    fi
}

# Check prerequisites
check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check Java
    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi
    
    # Check if test suite file exists
    if [ ! -f "src/test/resources/testng-${TEST_SUITE}.xml" ]; then
        log_error "Test suite file not found: testng-${TEST_SUITE}.xml"
        exit 1
    fi
    
    # Check Docker if needed
    if [ "$USE_DOCKER" = true ]; then
        if ! command -v docker &> /dev/null; then
            log_error "Docker is not installed or not in PATH"
            exit 1
        fi
    fi
    
    log_success "Prerequisites check passed"
}

# Run tests with Maven
run_maven_tests() {
    log "Running tests with Maven..."
    
    local maven_cmd="mvn clean test"
    maven_cmd="$maven_cmd -Dsurefire.suiteXmlFiles=src/test/resources/testng-${TEST_SUITE}.xml"
    maven_cmd="$maven_cmd -Dbrowser=${BROWSER}"
    maven_cmd="$maven_cmd -Denvironment=${ENVIRONMENT}"
    maven_cmd="$maven_cmd -Dheadless=${HEADLESS}"
    maven_cmd="$maven_cmd -Dparallel=${PARALLEL}"
    maven_cmd="$maven_cmd -Dallure.results.directory=target/allure-results"
    maven_cmd="$maven_cmd -Dmaven.test.failure.ignore=true"
    
    # Add Selenium Grid configuration if enabled
    if [ "$USE_GRID" = true ]; then
        maven_cmd="$maven_cmd -Dselenium.grid.url=http://localhost:4444/wd/hub"
        maven_cmd="$maven_cmd -Duse.grid=true"
    fi
    
    log "Executing: $maven_cmd"
    
    # Execute Maven command
    if eval $maven_cmd; then
        log_success "Tests executed successfully"
        return 0
    else
        log_warning "Some tests may have failed"
        return 1
    fi
}

# Run tests with Docker
run_docker_tests() {
    log "Running tests with Docker..."
    
    # Build Docker image
    log "Building Docker image..."
    if docker build -t paytr-tests .; then
        log_success "Docker image built successfully"
    else
        log_error "Failed to build Docker image"
        exit 1
    fi
    
    # Run tests in Docker container
    log "Running tests in Docker container..."
    docker run --rm \
        -v "$(pwd)/test-results:/app/test-results" \
        -v "$(pwd)/reports:/app/reports" \
        -v "$(pwd)/screenshots:/app/screenshots" \
        -v "$(pwd)/logs:/app/logs" \
        -e TEST_SUITE="$TEST_SUITE" \
        -e BROWSER="$BROWSER" \
        -e ENVIRONMENT="$ENVIRONMENT" \
        -e HEADLESS="$HEADLESS" \
        paytr-tests "$TEST_SUITE" "$BROWSER" "$ENVIRONMENT" "$HEADLESS"
}

# Generate Allure report
generate_allure_report() {
    log "Generating Allure report..."
    
    if [ -d "target/allure-results" ] && [ "$(ls -A target/allure-results)" ]; then
        if command -v allure &> /dev/null; then
            allure generate target/allure-results --clean --output target/allure-report
            log_success "Allure report generated at: target/allure-report"
            
            # Copy to reports directory
            cp -r target/allure-report reports/allure-report-${TIMESTAMP}
            
            # Create latest symlink
            ln -sfn allure-report-${TIMESTAMP} reports/latest
            
        else
            log_warning "Allure is not installed. Skipping report generation."
        fi
    else
        log_warning "No Allure results found. Skipping report generation."
    fi
}

# Generate HTML report
generate_html_report() {
    log "Generating HTML report..."
    
    local report_file="reports/test-report-${TIMESTAMP}.html"
    
    cat > "$report_file" << EOF
<!DOCTYPE html>
<html>
<head>
    <title>PayTR Test Report - ${TIMESTAMP}</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
        .summary { margin: 20px 0; }
        .success { color: green; }
        .failure { color: red; }
        .warning { color: orange; }
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>PayTR Test Automation Report</h1>
        <p><strong>Timestamp:</strong> ${TIMESTAMP}</p>
        <p><strong>Test Suite:</strong> ${TEST_SUITE}</p>
        <p><strong>Browser:</strong> ${BROWSER}</p>
        <p><strong>Environment:</strong> ${ENVIRONMENT}</p>
        <p><strong>Headless:</strong> ${HEADLESS}</p>
    </div>
    
    <div class="summary">
        <h2>Test Summary</h2>
        <!-- Test summary will be populated here -->
    </div>
    
    <div>
        <h2>Links</h2>
        <ul>
            <li><a href="allure-report-${TIMESTAMP}/index.html">Allure Report</a></li>
            <li><a href="../target/surefire-reports/index.html">Surefire Reports</a></li>
        </ul>
    </div>
</body>
</html>
EOF
    
    log_success "HTML report generated at: $report_file"
}

# Parse test results
parse_test_results() {
    log "Parsing test results..."
    
    local passed=0
    local failed=0
    local skipped=0
    local total=0
    
    # Try to parse from TEST-TestSuite.xml (Surefire format)
    if [ -f "target/surefire-reports/TEST-TestSuite.xml" ]; then
        total=$(grep -o 'tests="[0-9]*"' target/surefire-reports/TEST-TestSuite.xml | cut -d'"' -f2 || echo "0")
        failed=$(grep -o 'failures="[0-9]*"' target/surefire-reports/TEST-TestSuite.xml | cut -d'"' -f2 || echo "0")
        skipped=$(grep -o 'skipped="[0-9]*"' target/surefire-reports/TEST-TestSuite.xml | cut -d'"' -f2 || echo "0")
        passed=$((total - failed - skipped))
    # Fallback to testng-results.xml if available
    elif [ -f "target/surefire-reports/testng-results.xml" ]; then
        passed=$(grep -o 'passed="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0")
        failed=$(grep -o 'failed="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0")
        skipped=$(grep -o 'skipped="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0")
        total=$((passed + failed + skipped))
    fi
    
    local success_rate=0
    
    if [ $total -gt 0 ]; then
        success_rate=$((passed * 100 / total))
    fi
    
    log "Test Results Summary:"
    log_success "Passed: $passed"
    log_error "Failed: $failed"
    log_warning "Skipped: $skipped"
    log "Total: $total"
    log "Success Rate: ${success_rate}%"
    
    # Save results to file
    cat > "reports/test-summary-${TIMESTAMP}.txt" << EOF
PayTR Test Automation Results
=============================
Timestamp: ${TIMESTAMP}
Test Suite: ${TEST_SUITE}
Browser: ${BROWSER}
Environment: ${ENVIRONMENT}

Results:
--------
Passed: $passed
Failed: $failed
Skipped: $skipped
Total: $total
Success Rate: ${success_rate}%
EOF
    
    return $failed
}

# Send notifications
send_notifications() {
    local exit_code=$1
    
    log "Sending notifications..."
    
    # Email notification (if configured)
    if [ -n "${EMAIL_RECIPIENTS:-}" ]; then
        local subject="PayTR Test Results - ${TEST_SUITE} - ${TIMESTAMP}"
        local body="Test execution completed. Check reports for details."
        
        # Send email (requires mail command to be configured)
        echo "$body" | mail -s "$subject" "$EMAIL_RECIPIENTS" 2>/dev/null || true
    fi
    
    # Slack notification (if webhook configured)
    if [ -n "${SLACK_WEBHOOK_URL:-}" ]; then
        local color="good"
        if [ $exit_code -ne 0 ]; then
            color="danger"
        fi
        
        curl -X POST -H 'Content-type: application/json' \
            --data "{\"text\":\"PayTR Test Results\", \"color\":\"$color\", \"fields\":[{\"title\":\"Suite\",\"value\":\"$TEST_SUITE\",\"short\":true},{\"title\":\"Browser\",\"value\":\"$BROWSER\",\"short\":true}]}" \
            "$SLACK_WEBHOOK_URL" 2>/dev/null || true
    fi
}

# Main execution function
main() {
    log "🚀 PayTR Test Automation Script Started"
    log "Timestamp: $TIMESTAMP"
    
    # Parse and validate arguments
    parse_arguments "$@"
    validate_arguments
    
    # Show configuration
    log "Configuration:"
    log "  Test Suite: $TEST_SUITE"
    log "  Browser: $BROWSER"
    log "  Environment: $ENVIRONMENT"
    log "  Headless: $HEADLESS"
    log "  Parallel: $PARALLEL"
    log "  Docker: $USE_DOCKER"
    log "  Grid: $USE_GRID"
    log "  Report Only: $REPORT_ONLY"
    log "  Clean: $CLEAN_RESULTS"
    
    # Setup
    setup_directories
    clean_previous_results
    
    local test_exit_code=0
    
    # Run tests or generate reports only
    if [ "$REPORT_ONLY" = false ]; then
        check_prerequisites
        
        if [ "$USE_DOCKER" = true ]; then
            run_docker_tests
            test_exit_code=$?
        else
            run_maven_tests
            test_exit_code=$?
        fi
    fi
    
    # Generate reports
    generate_allure_report
    generate_html_report
    
    # Parse results
    parse_test_results
    local parse_exit_code=$?
    
    # Send notifications
    send_notifications $parse_exit_code
    
    # Final status
    if [ $parse_exit_code -eq 0 ]; then
        log_success "🎉 All tests passed successfully!"
    else
        log_warning "⚠️ Some tests failed. Check reports for details."
    fi
    
    log "📊 Reports available at: $(pwd)/reports"
    log "🏁 PayTR Test Automation Script Completed"
    
    exit $parse_exit_code
}

# Execute main function with all arguments
main "$@"