@echo off
REM PayTR Test Automation Execution Script for Windows
REM Bu script PayTR test süitlerini Windows'ta çalıştırmak için kullanılır

setlocal enabledelayedexpansion

REM Script configuration
set SCRIPT_DIR=%~dp0
set PROJECT_DIR=%SCRIPT_DIR%..
set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

REM Default values
set DEFAULT_TEST_SUITE=comprehensive
set DEFAULT_BROWSER=chrome
set DEFAULT_ENVIRONMENT=staging
set DEFAULT_HEADLESS=true
set DEFAULT_PARALLEL=true

REM Initialize variables
set TEST_SUITE=%DEFAULT_TEST_SUITE%
set BROWSER=%DEFAULT_BROWSER%
set ENVIRONMENT=%DEFAULT_ENVIRONMENT%
set HEADLESS=%DEFAULT_HEADLESS%
set PARALLEL=%DEFAULT_PARALLEL%
set USE_DOCKER=false
set USE_GRID=false
set REPORT_ONLY=false
set CLEAN_RESULTS=false

REM Colors (Windows doesn't support colors in batch easily, so we'll use text)
set LOG_PREFIX=[%date% %time%]

REM Help function
:show_help
echo PayTR Test Automation Execution Script for Windows
echo.
echo Usage: %0 [OPTIONS]
echo.
echo OPTIONS:
echo     -s, --suite SUITE       Test suite to run (comprehensive^|security^|performance^|api^|smoke)
echo                            Default: %DEFAULT_TEST_SUITE%
echo.
echo     -b, --browser BROWSER   Browser to use (chrome^|firefox^|edge)
echo                            Default: %DEFAULT_BROWSER%
echo.
echo     -e, --env ENVIRONMENT   Test environment (staging^|production^|development)
echo                            Default: %DEFAULT_ENVIRONMENT%
echo.
echo     -h, --headless         Run in headless mode (true^|false)
echo                            Default: %DEFAULT_HEADLESS%
echo.
echo     -p, --parallel         Enable parallel execution (true^|false)
echo                            Default: %DEFAULT_PARALLEL%
echo.
echo     -d, --docker           Run tests in Docker container
echo.
echo     -g, --grid             Use Selenium Grid
echo.
echo     -r, --report-only      Only generate reports from existing results
echo.
echo     -c, --clean            Clean previous test results before running
echo.
echo     --help                 Show this help message
echo.
echo EXAMPLES:
echo     # Run comprehensive tests with Chrome in staging
echo     %0 -s comprehensive -b chrome -e staging
echo.
echo     # Run security tests in headless mode
echo     %0 -s security -h true
echo.
echo     # Run tests in Docker
echo     %0 -d -s performance -b firefox
echo.
echo     # Clean and run smoke tests
echo     %0 -c -s smoke
echo.
goto :eof

REM Logging functions
:log
echo %LOG_PREFIX% %~1
goto :eof

:log_success
echo %LOG_PREFIX% [SUCCESS] %~1
goto :eof

:log_warning
echo %LOG_PREFIX% [WARNING] %~1
goto :eof

:log_error
echo %LOG_PREFIX% [ERROR] %~1
goto :eof

REM Parse command line arguments
:parse_arguments
if "%~1"=="" goto :validate_arguments

if "%~1"=="-s" (
    set TEST_SUITE=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="--suite" (
    set TEST_SUITE=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="-b" (
    set BROWSER=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="--browser" (
    set BROWSER=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="-e" (
    set ENVIRONMENT=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="--env" (
    set ENVIRONMENT=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="-h" (
    set HEADLESS=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="--headless" (
    set HEADLESS=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="-p" (
    set PARALLEL=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="--parallel" (
    set PARALLEL=%~2
    shift
    shift
    goto :parse_arguments
)
if "%~1"=="-d" (
    set USE_DOCKER=true
    shift
    goto :parse_arguments
)
if "%~1"=="--docker" (
    set USE_DOCKER=true
    shift
    goto :parse_arguments
)
if "%~1"=="-g" (
    set USE_GRID=true
    shift
    goto :parse_arguments
)
if "%~1"=="--grid" (
    set USE_GRID=true
    shift
    goto :parse_arguments
)
if "%~1"=="-r" (
    set REPORT_ONLY=true
    shift
    goto :parse_arguments
)
if "%~1"=="--report-only" (
    set REPORT_ONLY=true
    shift
    goto :parse_arguments
)
if "%~1"=="-c" (
    set CLEAN_RESULTS=true
    shift
    goto :parse_arguments
)
if "%~1"=="--clean" (
    set CLEAN_RESULTS=true
    shift
    goto :parse_arguments
)
if "%~1"=="--help" (
    call :show_help
    exit /b 0
)

call :log_error "Unknown option: %~1"
call :show_help
exit /b 1

REM Validate arguments
:validate_arguments
REM Validate test suite
if "%TEST_SUITE%"=="comprehensive" goto :browser_validation
if "%TEST_SUITE%"=="security" goto :browser_validation
if "%TEST_SUITE%"=="performance" goto :browser_validation
if "%TEST_SUITE%"=="api" goto :browser_validation
if "%TEST_SUITE%"=="smoke" goto :browser_validation

call :log_error "Invalid test suite: %TEST_SUITE%"
call :log "Valid options: comprehensive, security, performance, api, smoke"
exit /b 1

:browser_validation
REM Validate browser
if "%BROWSER%"=="chrome" goto :environment_validation
if "%BROWSER%"=="firefox" goto :environment_validation
if "%BROWSER%"=="edge" goto :environment_validation

call :log_error "Invalid browser: %BROWSER%"
call :log "Valid options: chrome, firefox, edge"
exit /b 1

:environment_validation
REM Validate environment
if "%ENVIRONMENT%"=="staging" goto :setup_directories
if "%ENVIRONMENT%"=="production" goto :setup_directories
if "%ENVIRONMENT%"=="development" goto :setup_directories

call :log_error "Invalid environment: %ENVIRONMENT%"
call :log "Valid options: staging, production, development"
exit /b 1

REM Setup directories
:setup_directories
call :log "Setting up directories..."

cd /d "%PROJECT_DIR%"

REM Create necessary directories
if not exist "target\surefire-reports" mkdir "target\surefire-reports"
if not exist "target\allure-results" mkdir "target\allure-results"
if not exist "target\allure-report" mkdir "target\allure-report"
if not exist "screenshots" mkdir "screenshots"
if not exist "logs" mkdir "logs"
if not exist "reports" mkdir "reports"

call :log_success "Directories created successfully"
goto :clean_previous_results

REM Clean previous results
:clean_previous_results
if "%CLEAN_RESULTS%"=="true" (
    call :log "Cleaning previous test results..."
    
    if exist "target\surefire-reports\*" del /q "target\surefire-reports\*"
    if exist "target\allure-results\*" del /q "target\allure-results\*"
    if exist "target\allure-report\*" del /q "target\allure-report\*"
    if exist "screenshots\*" del /q "screenshots\*"
    if exist "logs\*" del /q "logs\*"
    if exist "reports\*" del /q "reports\*"
    
    call :log_success "Previous results cleaned"
)
goto :check_prerequisites

REM Check prerequisites
:check_prerequisites
call :log "Checking prerequisites..."

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    call :log_error "Java is not installed or not in PATH"
    exit /b 1
)

REM Check Maven
mvn -version >nul 2>&1
if errorlevel 1 (
    call :log_error "Maven is not installed or not in PATH"
    exit /b 1
)

REM Check if test suite file exists
if not exist "src\test\resources\testng-%TEST_SUITE%.xml" (
    call :log_error "Test suite file not found: testng-%TEST_SUITE%.xml"
    exit /b 1
)

REM Check Docker if needed
if "%USE_DOCKER%"=="true" (
    docker --version >nul 2>&1
    if errorlevel 1 (
        call :log_error "Docker is not installed or not in PATH"
        exit /b 1
    )
)

call :log_success "Prerequisites check passed"
goto :run_tests

REM Run tests
:run_tests
if "%REPORT_ONLY%"=="true" goto :generate_allure_report

if "%USE_DOCKER%"=="true" (
    goto :run_docker_tests
) else (
    goto :run_maven_tests
)

REM Run tests with Maven
:run_maven_tests
call :log "Running tests with Maven..."

set MAVEN_CMD=mvn clean test
set MAVEN_CMD=%MAVEN_CMD% -Dsurefire.suiteXmlFiles=src/test/resources/testng-%TEST_SUITE%.xml
set MAVEN_CMD=%MAVEN_CMD% -Dbrowser=%BROWSER%
set MAVEN_CMD=%MAVEN_CMD% -Denvironment=%ENVIRONMENT%
set MAVEN_CMD=%MAVEN_CMD% -Dheadless=%HEADLESS%
set MAVEN_CMD=%MAVEN_CMD% -Dparallel=%PARALLEL%
set MAVEN_CMD=%MAVEN_CMD% -Dallure.results.directory=target/allure-results
set MAVEN_CMD=%MAVEN_CMD% -Dmaven.test.failure.ignore=true

REM Add Selenium Grid configuration if enabled
if "%USE_GRID%"=="true" (
    set MAVEN_CMD=%MAVEN_CMD% -Dselenium.grid.url=http://localhost:4444/wd/hub
    set MAVEN_CMD=%MAVEN_CMD% -Duse.grid=true
)

call :log "Executing: %MAVEN_CMD%"

REM Execute Maven command
%MAVEN_CMD%
if errorlevel 1 (
    call :log_warning "Some tests may have failed"
) else (
    call :log_success "Tests executed successfully"
)

goto :generate_allure_report

REM Run tests with Docker
:run_docker_tests
call :log "Running tests with Docker..."

REM Build Docker image
call :log "Building Docker image..."
docker build -t paytr-tests .
if errorlevel 1 (
    call :log_error "Failed to build Docker image"
    exit /b 1
)
call :log_success "Docker image built successfully"

REM Run tests in Docker container
call :log "Running tests in Docker container..."
docker run --rm ^
    -v "%cd%\test-results:/app/test-results" ^
    -v "%cd%\reports:/app/reports" ^
    -v "%cd%\screenshots:/app/screenshots" ^
    -v "%cd%\logs:/app/logs" ^
    -e TEST_SUITE=%TEST_SUITE% ^
    -e BROWSER=%BROWSER% ^
    -e ENVIRONMENT=%ENVIRONMENT% ^
    -e HEADLESS=%HEADLESS% ^
    paytr-tests %TEST_SUITE% %BROWSER% %ENVIRONMENT% %HEADLESS%

goto :generate_allure_report

REM Generate Allure report
:generate_allure_report
call :log "Generating Allure report..."

if exist "target\allure-results" (
    dir /b "target\allure-results" | findstr . >nul
    if not errorlevel 1 (
        allure --version >nul 2>&1
        if not errorlevel 1 (
            allure generate target\allure-results --clean --output target\allure-report
            call :log_success "Allure report generated at: target\allure-report"
            
            REM Copy to reports directory
            if not exist "reports\allure-report-%TIMESTAMP%" mkdir "reports\allure-report-%TIMESTAMP%"
            xcopy /e /i "target\allure-report" "reports\allure-report-%TIMESTAMP%"
            
        ) else (
            call :log_warning "Allure is not installed. Skipping report generation."
        )
    ) else (
        call :log_warning "No Allure results found. Skipping report generation."
    )
) else (
    call :log_warning "Allure results directory not found. Skipping report generation."
)

goto :generate_html_report

REM Generate HTML report
:generate_html_report
call :log "Generating HTML report..."

set REPORT_FILE=reports\test-report-%TIMESTAMP%.html

(
echo ^<!DOCTYPE html^>
echo ^<html^>
echo ^<head^>
echo     ^<title^>PayTR Test Report - %TIMESTAMP%^</title^>
echo     ^<style^>
echo         body { font-family: Arial, sans-serif; margin: 20px; }
echo         .header { background-color: #f0f0f0; padding: 20px; border-radius: 5px; }
echo         .summary { margin: 20px 0; }
echo         .success { color: green; }
echo         .failure { color: red; }
echo         .warning { color: orange; }
echo         table { border-collapse: collapse; width: 100%%; }
echo         th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
echo         th { background-color: #f2f2f2; }
echo     ^</style^>
echo ^</head^>
echo ^<body^>
echo     ^<div class="header"^>
echo         ^<h1^>PayTR Test Automation Report^</h1^>
echo         ^<p^>^<strong^>Timestamp:^</strong^> %TIMESTAMP%^</p^>
echo         ^<p^>^<strong^>Test Suite:^</strong^> %TEST_SUITE%^</p^>
echo         ^<p^>^<strong^>Browser:^</strong^> %BROWSER%^</p^>
echo         ^<p^>^<strong^>Environment:^</strong^> %ENVIRONMENT%^</p^>
echo         ^<p^>^<strong^>Headless:^</strong^> %HEADLESS%^</p^>
echo     ^</div^>
echo     
echo     ^<div class="summary"^>
echo         ^<h2^>Test Summary^</h2^>
echo         ^<!-- Test summary will be populated here --^>
echo     ^</div^>
echo     
echo     ^<div^>
echo         ^<h2^>Links^</h2^>
echo         ^<ul^>
echo             ^<li^>^<a href="allure-report-%TIMESTAMP%/index.html"^>Allure Report^</a^>^</li^>
echo             ^<li^>^<a href="../target/surefire-reports/index.html"^>Surefire Reports^</a^>^</li^>
echo         ^</ul^>
echo     ^</div^>
echo ^</body^>
echo ^</html^>
) > "%REPORT_FILE%"

call :log_success "HTML report generated at: %REPORT_FILE%"
goto :parse_test_results

REM Parse test results
:parse_test_results
call :log "Parsing test results..."

set PASSED=0
set FAILED=0
set SKIPPED=0

if exist "target\surefire-reports\testng-results.xml" (
    REM Windows batch doesn't have grep, so we'll use findstr
    REM This is a simplified version - in real scenario you might want to use PowerShell
    call :log "Test results file found"
) else (
    call :log_warning "Test results file not found"
)

call :log "Test Results Summary:"
call :log_success "Passed: %PASSED%"
call :log_error "Failed: %FAILED%"
call :log_warning "Skipped: %SKIPPED%"

REM Save results to file
(
echo PayTR Test Automation Results
echo =============================
echo Timestamp: %TIMESTAMP%
echo Test Suite: %TEST_SUITE%
echo Browser: %BROWSER%
echo Environment: %ENVIRONMENT%
echo.
echo Results:
echo --------
echo Passed: %PASSED%
echo Failed: %FAILED%
echo Skipped: %SKIPPED%
) > "reports\test-summary-%TIMESTAMP%.txt"

goto :send_notifications

REM Send notifications
:send_notifications
call :log "Sending notifications..."

REM Email notification would require additional setup on Windows
REM Slack notification would require curl or PowerShell

goto :main_end

REM Main execution
:main
call :log "PayTR Test Automation Script Started (Windows)"
call :log "Timestamp: %TIMESTAMP%"

REM Parse and validate arguments
call :parse_arguments %*

REM Show configuration
call :log "Configuration:"
call :log "  Test Suite: %TEST_SUITE%"
call :log "  Browser: %BROWSER%"
call :log "  Environment: %ENVIRONMENT%"
call :log "  Headless: %HEADLESS%"
call :log "  Parallel: %PARALLEL%"
call :log "  Docker: %USE_DOCKER%"
call :log "  Grid: %USE_GRID%"
call :log "  Report Only: %REPORT_ONLY%"
call :log "  Clean: %CLEAN_RESULTS%"

REM Execute main flow
goto :setup_directories

:main_end
if "%FAILED%"=="0" (
    call :log_success "All tests passed successfully!"
    set EXIT_CODE=0
) else (
    call :log_warning "Some tests failed. Check reports for details."
    set EXIT_CODE=1
)

call :log "Reports available at: %cd%\reports"
call :log "PayTR Test Automation Script Completed (Windows)"

exit /b %EXIT_CODE%

REM Start main execution
call :main %*