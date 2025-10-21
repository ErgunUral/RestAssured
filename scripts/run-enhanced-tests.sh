#!/bin/bash

# PayTR Enhanced Test Execution Script
# Provides comprehensive test execution with enhanced reporting and monitoring

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
REPORTS_DIR="$PROJECT_DIR/target/enhanced-reports"
LOGS_DIR="$PROJECT_DIR/target/logs"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

# Default values
TEST_SUITE="smoke-enhanced"
BROWSER="chrome"
ENVIRONMENT="test"
HEADLESS="true"
PARALLEL="false"
THREAD_COUNT="1"
GENERATE_REPORTS="true"
CLEANUP_OLD_REPORTS="true"
SEND_NOTIFICATIONS="false"

# Function to print colored output
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Function to print banner
print_banner() {
    echo ""
    print_message $CYAN "================================================================================================"
    print_message $CYAN "🚀 PayTR Enhanced Test Suite Execution"
    print_message $CYAN "================================================================================================"
    echo ""
}

# Function to print usage
print_usage() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -s, --suite SUITE          Test suite to run (smoke-enhanced, comprehensive-enhanced, regression-enhanced, parallel-enhanced)"
    echo "  -b, --browser BROWSER      Browser to use (chrome, firefox, edge)"
    echo "  -e, --environment ENV      Environment to test (test, staging, production)"
    echo "  -h, --headless             Run in headless mode (default: true)"
    echo "  -p, --parallel             Enable parallel execution"
    echo "  -t, --threads COUNT        Number of parallel threads (default: 1)"
    echo "  -r, --reports              Generate enhanced reports (default: true)"
    echo "  -c, --cleanup              Cleanup old reports (default: true)"
    echo "  -n, --notifications        Send notifications on completion"
    echo "  --help                     Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -s smoke-enhanced -b chrome -h"
    echo "  $0 -s comprehensive-enhanced -b firefox -p -t 4"
    echo "  $0 -s regression-enhanced -e staging -r -n"
    echo ""
}

# Function to parse command line arguments
parse_arguments() {
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
            -e|--environment)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -h|--headless)
                HEADLESS="true"
                shift
                ;;
            -p|--parallel)
                PARALLEL="true"
                shift
                ;;
            -t|--threads)
                THREAD_COUNT="$2"
                shift 2
                ;;
            -r|--reports)
                GENERATE_REPORTS="true"
                shift
                ;;
            -c|--cleanup)
                CLEANUP_OLD_REPORTS="true"
                shift
                ;;
            -n|--notifications)
                SEND_NOTIFICATIONS="true"
                shift
                ;;
            --help)
                print_usage
                exit 0
                ;;
            *)
                print_message $RED "Unknown option: $1"
                print_usage
                exit 1
                ;;
        esac
    done
}

# Function to validate arguments
validate_arguments() {
    local valid_suites=("smoke-enhanced" "comprehensive-enhanced" "regression-enhanced" "parallel-enhanced")
    local valid_browsers=("chrome" "firefox" "edge")
    local valid_environments=("test" "staging" "production")
    
    # Validate test suite
    if [[ ! " ${valid_suites[@]} " =~ " ${TEST_SUITE} " ]]; then
        print_message $RED "❌ Invalid test suite: $TEST_SUITE"
        print_message $YELLOW "Valid suites: ${valid_suites[*]}"
        exit 1
    fi
    
    # Validate browser
    if [[ ! " ${valid_browsers[@]} " =~ " ${BROWSER} " ]]; then
        print_message $RED "❌ Invalid browser: $BROWSER"
        print_message $YELLOW "Valid browsers: ${valid_browsers[*]}"
        exit 1
    fi
    
    # Validate environment
    if [[ ! " ${valid_environments[@]} " =~ " ${ENVIRONMENT} " ]]; then
        print_message $RED "❌ Invalid environment: $ENVIRONMENT"
        print_message $YELLOW "Valid environments: ${valid_environments[*]}"
        exit 1
    fi
    
    # Validate thread count
    if ! [[ "$THREAD_COUNT" =~ ^[0-9]+$ ]] || [ "$THREAD_COUNT" -lt 1 ] || [ "$THREAD_COUNT" -gt 10 ]; then
        print_message $RED "❌ Invalid thread count: $THREAD_COUNT (must be 1-10)"
        exit 1
    fi
}

# Function to setup directories
setup_directories() {
    print_message $BLUE "📁 Setting up directories..."
    
    mkdir -p "$REPORTS_DIR"
    mkdir -p "$LOGS_DIR"
    mkdir -p "$PROJECT_DIR/target/screenshots"
    mkdir -p "$PROJECT_DIR/target/allure-results"
    
    print_message $GREEN "✅ Directories created successfully"
}

# Function to cleanup old reports
cleanup_old_reports() {
    if [ "$CLEANUP_OLD_REPORTS" = "true" ]; then
        print_message $BLUE "🧹 Cleaning up old reports..."
        
        # Remove reports older than 7 days
        find "$REPORTS_DIR" -name "*.html" -mtime +7 -delete 2>/dev/null || true
        find "$REPORTS_DIR" -name "*.json" -mtime +7 -delete 2>/dev/null || true
        find "$PROJECT_DIR/target/screenshots" -name "*.png" -mtime +7 -delete 2>/dev/null || true
        
        print_message $GREEN "✅ Old reports cleaned up"
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_message $BLUE "🔍 Checking prerequisites..."
    
    # Check if Maven is installed
    if ! command -v mvn &> /dev/null; then
        print_message $RED "❌ Maven is not installed or not in PATH"
        exit 1
    fi
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        print_message $RED "❌ Java is not installed or not in PATH"
        exit 1
    fi
    
    # Check if project directory exists
    if [ ! -f "$PROJECT_DIR/pom.xml" ]; then
        print_message $RED "❌ pom.xml not found in project directory"
        exit 1
    fi
    
    print_message $GREEN "✅ Prerequisites check passed"
}

# Function to print test configuration
print_test_configuration() {
    print_message $PURPLE "📋 Test Configuration:"
    echo "  Test Suite: $TEST_SUITE"
    echo "  Browser: $BROWSER"
    echo "  Environment: $ENVIRONMENT"
    echo "  Headless Mode: $HEADLESS"
    echo "  Parallel Execution: $PARALLEL"
    echo "  Thread Count: $THREAD_COUNT"
    echo "  Generate Reports: $GENERATE_REPORTS"
    echo "  Cleanup Old Reports: $CLEANUP_OLD_REPORTS"
    echo "  Send Notifications: $SEND_NOTIFICATIONS"
    echo "  Project Directory: $PROJECT_DIR"
    echo "  Reports Directory: $REPORTS_DIR"
    echo ""
}

# Function to run tests
run_tests() {
    print_message $BLUE "🚀 Starting test execution..."
    
    local log_file="$LOGS_DIR/test_execution_${TIMESTAMP}.log"
    local maven_args=()
    
    # Build Maven command
    maven_args+=("clean" "test")
    maven_args+=("-P$TEST_SUITE")
    maven_args+=("-Dbrowser=$BROWSER")
    maven_args+=("-Denvironment=$ENVIRONMENT")
    maven_args+=("-Dheadless=$HEADLESS")
    
    if [ "$PARALLEL" = "true" ]; then
        maven_args+=("-Dparallel=methods")
        maven_args+=("-DthreadCount=$THREAD_COUNT")
    fi
    
    # Add additional properties
    maven_args+=("-Dmaven.test.failure.ignore=true")
    maven_args+=("-Dallure.results.directory=target/allure-results")
    
    print_message $YELLOW "📝 Maven command: mvn ${maven_args[*]}"
    print_message $YELLOW "📄 Log file: $log_file"
    
    # Execute tests
    cd "$PROJECT_DIR"
    
    if mvn "${maven_args[@]}" 2>&1 | tee "$log_file"; then
        print_message $GREEN "✅ Test execution completed successfully"
        return 0
    else
        print_message $RED "❌ Test execution completed with errors"
        return 1
    fi
}

# Function to generate reports
generate_reports() {
    if [ "$GENERATE_REPORTS" = "true" ]; then
        print_message $BLUE "📊 Generating enhanced reports..."
        
        cd "$PROJECT_DIR"
        
        # Generate Allure report
        if command -v allure &> /dev/null; then
            print_message $YELLOW "📈 Generating Allure report..."
            mvn allure:report || print_message $YELLOW "⚠️ Allure report generation failed"
        else
            print_message $YELLOW "⚠️ Allure command not found, using Maven plugin..."
            mvn allure:report || print_message $YELLOW "⚠️ Allure Maven plugin failed"
        fi
        
        # Generate Surefire report
        print_message $YELLOW "📋 Generating Surefire report..."
        mvn surefire-report:report || print_message $YELLOW "⚠️ Surefire report generation failed"
        
        print_message $GREEN "✅ Reports generated successfully"
    fi
}

# Function to analyze test results
analyze_test_results() {
    print_message $BLUE "🔍 Analyzing test results..."
    
    local surefire_reports="$PROJECT_DIR/target/surefire-reports"
    local total_tests=0
    local passed_tests=0
    local failed_tests=0
    local skipped_tests=0
    
    if [ -d "$surefire_reports" ]; then
        # Count test results from XML files
        for xml_file in "$surefire_reports"/TEST-*.xml; do
            if [ -f "$xml_file" ]; then
                local tests=$(grep -o 'tests="[0-9]*"' "$xml_file" | grep -o '[0-9]*' || echo "0")
                local failures=$(grep -o 'failures="[0-9]*"' "$xml_file" | grep -o '[0-9]*' || echo "0")
                local errors=$(grep -o 'errors="[0-9]*"' "$xml_file" | grep -o '[0-9]*' || echo "0")
                local skipped=$(grep -o 'skipped="[0-9]*"' "$xml_file" | grep -o '[0-9]*' || echo "0")
                
                total_tests=$((total_tests + tests))
                failed_tests=$((failed_tests + failures + errors))
                skipped_tests=$((skipped_tests + skipped))
            fi
        done
        
        passed_tests=$((total_tests - failed_tests - skipped_tests))
        
        # Calculate success rate
        local success_rate=0
        if [ $total_tests -gt 0 ]; then
            success_rate=$((passed_tests * 100 / total_tests))
        fi
        
        print_message $PURPLE "📊 Test Results Summary:"
        echo "  Total Tests: $total_tests"
        echo "  Passed: $passed_tests"
        echo "  Failed: $failed_tests"
        echo "  Skipped: $skipped_tests"
        echo "  Success Rate: $success_rate%"
        echo ""
        
        # Determine overall result
        if [ $failed_tests -eq 0 ]; then
            print_message $GREEN "🎉 All tests passed successfully!"
            return 0
        else
            print_message $RED "❌ Some tests failed"
            return 1
        fi
    else
        print_message $YELLOW "⚠️ No test results found"
        return 1
    fi
}

# Function to send notifications
send_notifications() {
    if [ "$SEND_NOTIFICATIONS" = "true" ]; then
        print_message $BLUE "📱 Sending notifications..."
        
        local status=$1
        local message="PayTR Test Suite ($TEST_SUITE) execution "
        
        if [ $status -eq 0 ]; then
            message+="completed successfully ✅"
        else
            message+="completed with failures ❌"
        fi
        
        message+=" | Environment: $ENVIRONMENT | Browser: $BROWSER"
        
        # Here you would integrate with your notification system
        # Examples: Slack, email, Teams, etc.
        print_message $YELLOW "📧 Notification: $message"
        
        # Example Slack webhook (uncomment and configure)
        # curl -X POST -H 'Content-type: application/json' \
        #   --data "{\"text\":\"$message\"}" \
        #   YOUR_SLACK_WEBHOOK_URL
        
        print_message $GREEN "✅ Notifications sent"
    fi
}

# Function to print final summary
print_final_summary() {
    local status=$1
    local end_time=$(date)
    
    print_message $CYAN "================================================================================================"
    print_message $CYAN "📋 Execution Summary"
    print_message $CYAN "================================================================================================"
    echo "  Test Suite: $TEST_SUITE"
    echo "  Browser: $BROWSER"
    echo "  Environment: $ENVIRONMENT"
    echo "  Start Time: $start_time"
    echo "  End Time: $end_time"
    echo "  Reports Directory: $REPORTS_DIR"
    echo "  Logs Directory: $LOGS_DIR"
    
    if [ $status -eq 0 ]; then
        print_message $GREEN "  Status: SUCCESS ✅"
    else
        print_message $RED "  Status: FAILURE ❌"
    fi
    
    print_message $CYAN "================================================================================================"
    echo ""
}

# Main execution function
main() {
    local start_time=$(date)
    local exit_code=0
    
    print_banner
    
    # Parse and validate arguments
    parse_arguments "$@"
    validate_arguments
    
    # Print configuration
    print_test_configuration
    
    # Setup and checks
    check_prerequisites
    setup_directories
    cleanup_old_reports
    
    # Run tests
    if ! run_tests; then
        exit_code=1
    fi
    
    # Generate reports
    generate_reports
    
    # Analyze results
    if ! analyze_test_results; then
        exit_code=1
    fi
    
    # Send notifications
    send_notifications $exit_code
    
    # Print final summary
    print_final_summary $exit_code
    
    exit $exit_code
}

# Execute main function with all arguments
main "$@"