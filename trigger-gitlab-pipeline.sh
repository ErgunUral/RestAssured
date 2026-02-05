#!/bin/bash

# PayTR GitLab Pipeline Trigger Script
# This script triggers GitLab CI/CD pipeline for test execution

set -e

# GitLab Configuration
GITLAB_URL="https://gitlab-qa.paytr.com"
PROJECT_ID="ergun.ural/restassured"
BRANCH_NAME="test-results-feb-5-2026"
PRIVATE_TOKEN="${GITLAB_PRIVATE_TOKEN:-}"  # Set this environment variable

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
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
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -t, --test-suite SUITE     Test suite to run (smoke, comprehensive, performance, security, regression)"
    echo "  -b, --browser BROWSER      Browser to use (chrome, firefox, edge)"
    echo "  -e, --environment ENV      Test environment (test, staging, production)"
    echo "  -h, --headless MODE       Headless mode (true, false)"
    echo "  -s, --schedule            Trigger scheduled pipeline"
    echo "  -m, --manual              Trigger manual pipeline"
    echo "  -w, --wait                Wait for pipeline completion"
    echo "  -p, --poll-interval SEC   Poll interval in seconds (default: 30)"
    echo "  --token TOKEN            GitLab private token (or set GITLAB_PRIVATE_TOKEN env var)"
    echo "  --help                   Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 --test-suite smoke --browser chrome --environment test"
    echo "  $0 -t comprehensive -b firefox -e staging --wait"
    echo "  $0 --manual --test-suite performance"
    echo ""
}

# Default values
TEST_SUITE="smoke"
TEST_BROWSER="chrome"
TEST_ENVIRONMENT="test"
HEADLESS_MODE="true"
TRIGGER_TYPE="manual"
WAIT_FOR_COMPLETION=false
POLL_INTERVAL=30

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -t|--test-suite)
            TEST_SUITE="$2"
            shift 2
            ;;
        -b|--browser)
            TEST_BROWSER="$2"
            shift 2
            ;;
        -e|--environment)
            TEST_ENVIRONMENT="$2"
            shift 2
            ;;
        -h|--headless)
            HEADLESS_MODE="$2"
            shift 2
            ;;
        -s|--schedule)
            TRIGGER_TYPE="schedule"
            shift
            ;;
        -m|--manual)
            TRIGGER_TYPE="manual"
            shift
            ;;
        -w|--wait)
            WAIT_FOR_COMPLETION=true
            shift
            ;;
        -p|--poll-interval)
            POLL_INTERVAL="$2"
            shift 2
            ;;
        --token)
            PRIVATE_TOKEN="$2"
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

# Validate required parameters
if [ -z "$PRIVATE_TOKEN" ]; then
    print_error "GitLab private token is required. Set GITLAB_PRIVATE_TOKEN environment variable or use --token option."
    exit 1
fi

# Validate test suite
VALID_SUITES=("smoke" "comprehensive" "performance" "security" "regression")
if [[ ! " ${VALID_SUITES[@]} " =~ " ${TEST_SUITE} " ]]; then
    print_error "Invalid test suite: $TEST_SUITE. Valid options: ${VALID_SUITES[*]}"
    exit 1
fi

# Validate browser
VALID_BROWSERS=("chrome" "firefox" "edge")
if [[ ! " ${VALID_BROWSERS[@]} " =~ " ${TEST_BROWSER} " ]]; then
    print_error "Invalid browser: $TEST_BROWSER. Valid options: ${VALID_BROWSERS[*]}"
    exit 1
fi

# Validate environment
VALID_ENVIRONMENTS=("test" "staging" "production")
if [[ ! " ${VALID_ENVIRONMENTS[@]} " =~ " ${TEST_ENVIRONMENT} " ]]; then
    print_error "Invalid environment: $TEST_ENVIRONMENT. Valid options: ${VALID_ENVIRONMENTS[*]}"
    exit 1
fi

print_status "PayTR GitLab Pipeline Trigger"
print_status "================================"
print_status "GitLab URL: $GITLAB_URL"
print_status "Project: $PROJECT_ID"
print_status "Branch: $BRANCH_NAME"
print_status "Test Suite: $TEST_SUITE"
print_status "Browser: $TEST_BROWSER"
print_status "Environment: $TEST_ENVIRONMENT"
print_status "Headless: $HEADLESS_MODE"
print_status "Trigger Type: $TRIGGER_TYPE"
print_status "Wait for completion: $WAIT_FOR_COMPLETION"

# Function to trigger pipeline
trigger_pipeline() {
    local api_url="$GITLAB_URL/api/v4/projects/$(echo $PROJECT_ID | sed 's/\//%2F/g)/pipeline"
    
    print_status "Triggering pipeline..."
    
    # Prepare variables for pipeline
    local variables="{\"TEST_SUITE\": \"$TEST_SUITE\", \"TEST_BROWSER\": \"$TEST_BROWSER\", \"TEST_ENVIRONMENT\": \"$TEST_ENVIRONMENT\", \"HEADLESS_MODE\": \"$HEADLESS_MODE\"}"
    
    local response=$(curl -s -X POST \
        -H "PRIVATE-TOKEN: $PRIVATE_TOKEN" \
        -H "Content-Type: application/json" \
        -d "{\"ref\": \"$BRANCH_NAME\", \"variables\": $variables}" \
        "$api_url")
    
    # Check if pipeline was triggered successfully
    if echo "$response" | grep -q '"id"'; then
        local pipeline_id=$(echo "$response" | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)
        local web_url=$(echo "$response" | grep -o '"web_url":"[^"]*"' | cut -d'"' -f4)
        
        print_success "Pipeline triggered successfully!"
        print_success "Pipeline ID: $pipeline_id"
        print_success "Pipeline URL: $web_url"
        
        echo "$pipeline_id"
        return 0
    else
        print_error "Failed to trigger pipeline"
        print_error "Response: $response"
        return 1
    fi
}

# Function to check pipeline status
check_pipeline_status() {
    local pipeline_id=$1
    local api_url="$GITLAB_URL/api/v4/projects/$(echo $PROJECT_ID | sed 's/\//%2F/g)/pipelines/$pipeline_id"
    
    local response=$(curl -s -X GET \
        -H "PRIVATE-TOKEN: $PRIVATE_TOKEN" \
        "$api_url")
    
    if echo "$response" | grep -q '"status"'; then
        local status=$(echo "$response" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)
        local duration=$(echo "$response" | grep -o '"duration":[0-9]*' | cut -d':' -f2 || echo "0")
        local created_at=$(echo "$response" | grep -o '"created_at":"[^"]*"' | cut -d'"' -f4)
        
        echo "$status|$duration|$created_at"
        return 0
    else
        print_error "Failed to get pipeline status"
        return 1
    fi
}

# Function to wait for pipeline completion
wait_for_pipeline() {
    local pipeline_id=$1
    local start_time=$(date +%s)
    
    print_status "Waiting for pipeline $pipeline_id to complete..."
    
    while true; do
        local status_info=$(check_pipeline_status $pipeline_id)
        if [ $? -eq 0 ]; then
            local status=$(echo "$status_info" | cut -d'|' -f1)
            local duration=$(echo "$status_info" | cut -d'|' -f2)
            
            case $status in
                "success")
                    print_success "Pipeline completed successfully!"
                    print_success "Duration: ${duration}s"
                    return 0
                    ;;
                "failed")
                    print_error "Pipeline failed!"
                    print_error "Duration: ${duration}s"
                    return 1
                    ;;
                "canceled")
                    print_warning "Pipeline was canceled!"
                    return 1
                    ;;
                "skipped")
                    print_warning "Pipeline was skipped!"
                    return 1
                    ;;
                "running"|"pending"|"created")
                    local current_time=$(date +%s)
                    local elapsed=$((current_time - start_time))
                    print_status "Pipeline status: $status (elapsed: ${elapsed}s)"
                    sleep $POLL_INTERVAL
                    ;;
                *)
                    print_warning "Unknown pipeline status: $status"
                    sleep $POLL_INTERVAL
                    ;;
            esac
        else
            print_error "Failed to check pipeline status"
            return 1
        fi
    done
}

# Main execution
print_status "Triggering GitLab pipeline..."

# Trigger the pipeline
pipeline_id=$(trigger_pipeline)
if [ $? -ne 0 ]; then
    print_error "Failed to trigger pipeline"
    exit 1
fi

# Get pipeline web URL
pipeline_web_url="$GITLAB_URL/$PROJECT_ID/-/pipelines/$pipeline_id"
print_status "Pipeline Web URL: $pipeline_web_url"

# Wait for completion if requested
if [ "$WAIT_FOR_COMPLETION" = true ]; then
    wait_for_pipeline $pipeline_id
    exit_code=$?
    
    if [ $exit_code -eq 0 ]; then
        print_success "Pipeline execution completed successfully!"
        print_success "View detailed results at: $pipeline_web_url"
    else
        print_error "Pipeline execution failed!"
        print_error "View failure details at: $pipeline_web_url"
        exit 1
    fi
else
    print_status "Pipeline triggered. View progress at: $pipeline_web_url"
    print_status "Use --wait option to wait for completion"
fi

print_status "Script completed!"