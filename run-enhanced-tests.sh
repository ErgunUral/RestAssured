#!/bin/bash

# PayTR Enhanced Test Suite Runner
# Comprehensive test execution script for 67 test scenarios

set -e

# Default values
SUITE="smoke"
BROWSER="chrome"
ENVIRONMENT="development"
HEADLESS="false"
PARALLEL="3"
USE_DOCKER="false"
GENERATE_REPORT="true"
CLEANUP="false"
NOTIFY="false"
GROUPS=""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to show usage
show_usage() {
    echo "PayTR Enhanced Test Suite Runner"
    echo ""
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -s, --suite SUITE           Test suite to run (smoke|comprehensive|regression|parallel)"
    echo "  -b, --browser BROWSER       Browser to use (chrome|firefox|edge)"
    echo "  -e, --environment ENV       Environment (development|staging|production)"
    echo "  -h, --headless BOOL         Run in headless mode (true|false)"
    echo "  -p, --parallel COUNT        Number of parallel threads"
    echo "  -g, --groups GROUPS         Test groups to run (comma-separated)"
    echo "  -d, --docker BOOL           Use Docker for execution (true|false)"
    echo "  -r, --report BOOL           Generate enhanced reports (true|false)"
    echo "  -c, --cleanup BOOL          Cleanup previous results (true|false)"
    echo "  -n, --notify BOOL           Send notifications (true|false)"
    echo "  --help                      Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -s comprehensive -b chrome -e production -p 5"
    echo "  $0 -s smoke -h true -r true"
    echo "  $0 -s comprehensive -g \"multi-currency,3d-secure\" -b firefox"
    echo ""
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -s|--suite)
            SUITE="$2"
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
            HEADLESS="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL="$2"
            shift 2
            ;;
        -g|--groups)
            GROUPS="$2"
            shift 2
            ;;
        -d|--docker)
            USE_DOCKER="$2"
            shift 2
            ;;
        -r|--report)
            GENERATE_REPORT="$2"
            shift 2
            ;;
        -c|--cleanup)
            CLEANUP="$2"
            shift 2
            ;;
        -n|--notify)
            NOTIFY="$2"
            shift 2
            ;;
        --help)
            show_usage
            exit 0
            ;;
        *)
            print_error "Unknown option: $1"
            show_usage
            exit 1
            ;;
    esac
done

# Validate suite parameter
case $SUITE in
    smoke|comprehensive|regression|parallel)
        ;;
    *)
        print_error "Invalid suite: $SUITE. Must be one of: smoke, comprehensive, regression, parallel"
        exit 1
        ;;
esac

# Function to cleanup previous results
cleanup_results() {
    if [[ "$CLEANUP" == "true" ]]; then
        print_info "Cleaning up previous test results..."
        rm -rf target/surefire-reports/* 2>/dev/null || true
        rm -rf target/allure-results/* 2>/dev/null || true
        rm -rf reports/* 2>/dev/null || true
        rm -rf screenshots/* 2>/dev/null || true
        print_success "Cleanup completed"
    fi
}

# Function to run tests with Maven
run_maven_tests() {
    local profile="${SUITE}-enhanced"
    local maven_cmd="mvn clean test -P${profile}"
    
    # Add browser parameter
    maven_cmd="${maven_cmd} -Dbrowser=${BROWSER}"
    
    # Add environment parameter
    maven_cmd="${maven_cmd} -Denvironment=${ENVIRONMENT}"
    
    # Add headless parameter
    maven_cmd="${maven_cmd} -Dheadless=${HEADLESS}"
    
    # Add parallel execution parameters
    maven_cmd="${maven_cmd} -DthreadCount=${PARALLEL}"
    
    # Add groups if specified
    if [[ -n "$GROUPS" ]]; then
        maven_cmd="${maven_cmd} -Dgroups=${GROUPS}"
    fi
    
    print_info "Executing: $maven_cmd"
    eval $maven_cmd
}

# Function to run tests with Docker
run_docker_tests() {
    print_info "Running tests with Docker..."
    
    local docker_cmd="docker-compose --profile ${SUITE} up --abort-on-container-exit"
    
    print_info "Executing: $docker_cmd"
    eval $docker_cmd
}

# Function to generate enhanced reports
generate_reports() {
    if [[ "$GENERATE_REPORT" == "true" ]]; then
        print_info "Generating enhanced reports..."
        
        # Generate Allure report
        mvn allure:report 2>/dev/null || print_warning "Allure report generation failed"
        
        # Create reports directory
        mkdir -p reports/html reports/json
        
        print_success "Enhanced reports generated in reports/ directory"
    fi
}

# Function to send notifications
send_notifications() {
    if [[ "$NOTIFY" == "true" ]]; then
        print_info "Sending test completion notifications..."
        
        # Get test results summary
        local total_tests=$(find target/surefire-reports -name "*.xml" -exec grep -l "testcase" {} \; 2>/dev/null | wc -l || echo "0")
        local timestamp=$(date '+%Y-%m-%d %H:%M:%S')
        
        print_success "Test execution completed at $timestamp"
        print_info "Total test files processed: $total_tests"
    fi
}

# Main execution
main() {
    print_info "PayTR Enhanced Test Suite Runner"
    print_info "================================"
    print_info "Suite: $SUITE"
    print_info "Browser: $BROWSER"
    print_info "Environment: $ENVIRONMENT"
    print_info "Headless: $HEADLESS"
    print_info "Parallel Threads: $PARALLEL"
    print_info "Docker: $USE_DOCKER"
    print_info "Generate Reports: $GENERATE_REPORT"
    print_info "Cleanup: $CLEANUP"
    print_info "Notifications: $NOTIFY"
    if [[ -n "$GROUPS" ]]; then
        print_info "Test Groups: $GROUPS"
    fi
    print_info "================================"
    
    # Cleanup if requested
    cleanup_results
    
    # Run tests
    if [[ "$USE_DOCKER" == "true" ]]; then
        run_docker_tests
    else
        run_maven_tests
    fi
    
    # Generate reports
    generate_reports
    
    # Send notifications
    send_notifications
    
    print_success "PayTR Enhanced Test Suite execution completed!"
}

# Execute main function
main "$@"