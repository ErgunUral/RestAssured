# PayTR Test Automation Execution Script for PowerShell
# Bu script PayTR test süitlerini Windows PowerShell'de çalıştırmak için kullanılır

param(
    [string]$TestSuite = "comprehensive",
    [string]$Browser = "chrome", 
    [string]$Environment = "staging",
    [bool]$Headless = $true,
    [bool]$Parallel = $true,
    [switch]$UseDocker,
    [switch]$UseGrid,
    [switch]$ReportOnly,
    [switch]$Clean,
    [switch]$Help
)

# Script configuration
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ProjectDir = Split-Path -Parent $ScriptDir
$Timestamp = Get-Date -Format "yyyyMMdd_HHmmss"

# Valid options
$ValidTestSuites = @("comprehensive", "security", "performance", "api", "smoke")
$ValidBrowsers = @("chrome", "firefox", "edge")
$ValidEnvironments = @("staging", "production", "development")

# Colors for output
$Colors = @{
    Success = "Green"
    Warning = "Yellow"
    Error = "Red"
    Info = "Cyan"
}

# Logging functions
function Write-Log {
    param([string]$Message, [string]$Level = "Info")
    $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    $color = $Colors[$Level]
    Write-Host "[$timestamp] [$Level] $Message" -ForegroundColor $color
}

function Write-Success {
    param([string]$Message)
    Write-Log $Message "Success"
}

function Write-Warning {
    param([string]$Message)
    Write-Log $Message "Warning"
}

function Write-Error {
    param([string]$Message)
    Write-Log $Message "Error"
}

function Show-Help {
    Write-Host @"
PayTR Test Automation Execution Script for PowerShell

USAGE:
    .\run-tests.ps1 [OPTIONS]

OPTIONS:
    -TestSuite <SUITE>      Test suite to run (comprehensive|security|performance|api|smoke)
                           Default: comprehensive

    -Browser <BROWSER>      Browser to use (chrome|firefox|edge)
                           Default: chrome

    -Environment <ENV>      Test environment (staging|production|development)
                           Default: staging

    -Headless <BOOL>        Run in headless mode (true|false)
                           Default: true

    -Parallel <BOOL>        Enable parallel execution (true|false)
                           Default: true

    -UseDocker             Run tests in Docker container

    -UseGrid               Use Selenium Grid

    -ReportOnly            Only generate reports from existing results

    -Clean                 Clean previous test results before running

    -Help                  Show this help message

EXAMPLES:
    # Run comprehensive tests with Chrome in staging
    .\run-tests.ps1 -TestSuite comprehensive -Browser chrome -Environment staging

    # Run security tests in headless mode
    .\run-tests.ps1 -TestSuite security -Headless `$true

    # Run tests in Docker
    .\run-tests.ps1 -UseDocker -TestSuite performance -Browser firefox

    # Clean and run smoke tests
    .\run-tests.ps1 -Clean -TestSuite smoke

"@
}

function Test-Prerequisites {
    Write-Log "Checking prerequisites..."
    
    # Check Java
    try {
        $javaVersion = java -version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Java not found"
        }
        Write-Success "Java is available"
    }
    catch {
        Write-Error "Java is not installed or not in PATH"
        return $false
    }
    
    # Check Maven
    try {
        $mavenVersion = mvn -version 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Maven not found"
        }
        Write-Success "Maven is available"
    }
    catch {
        Write-Error "Maven is not installed or not in PATH"
        return $false
    }
    
    # Check test suite file
    $testSuiteFile = Join-Path $ProjectDir "src\test\resources\testng-$TestSuite.xml"
    if (-not (Test-Path $testSuiteFile)) {
        Write-Error "Test suite file not found: testng-$TestSuite.xml"
        return $false
    }
    Write-Success "Test suite file found"
    
    # Check Docker if needed
    if ($UseDocker) {
        try {
            $dockerVersion = docker --version 2>&1
            if ($LASTEXITCODE -ne 0) {
                throw "Docker not found"
            }
            Write-Success "Docker is available"
        }
        catch {
            Write-Error "Docker is not installed or not in PATH"
            return $false
        }
    }
    
    Write-Success "Prerequisites check passed"
    return $true
}

function Initialize-Directories {
    Write-Log "Setting up directories..."
    
    Set-Location $ProjectDir
    
    # Create necessary directories
    $directories = @(
        "target\surefire-reports",
        "target\allure-results", 
        "target\allure-report",
        "screenshots",
        "logs",
        "reports"
    )
    
    foreach ($dir in $directories) {
        if (-not (Test-Path $dir)) {
            New-Item -ItemType Directory -Path $dir -Force | Out-Null
        }
    }
    
    Write-Success "Directories created successfully"
}

function Clear-PreviousResults {
    if ($Clean) {
        Write-Log "Cleaning previous test results..."
        
        $cleanupPaths = @(
            "target\surefire-reports\*",
            "target\allure-results\*",
            "target\allure-report\*",
            "screenshots\*",
            "logs\*",
            "reports\*"
        )
        
        foreach ($path in $cleanupPaths) {
            if (Test-Path $path) {
                Remove-Item $path -Force -Recurse -ErrorAction SilentlyContinue
            }
        }
        
        Write-Success "Previous results cleaned"
    }
}

function Invoke-MavenTests {
    Write-Log "Running tests with Maven..."
    
    $mavenArgs = @(
        "clean", "test",
        "-Dsurefire.suiteXmlFiles=src/test/resources/testng-$TestSuite.xml",
        "-Dbrowser=$Browser",
        "-Denvironment=$Environment", 
        "-Dheadless=$Headless",
        "-Dparallel=$Parallel",
        "-Dallure.results.directory=target/allure-results",
        "-Dmaven.test.failure.ignore=true"
    )
    
    # Add Selenium Grid configuration if enabled
    if ($UseGrid) {
        $mavenArgs += "-Dselenium.grid.url=http://localhost:4444/wd/hub"
        $mavenArgs += "-Duse.grid=true"
    }
    
    Write-Log "Executing: mvn $($mavenArgs -join ' ')"
    
    # Execute Maven command
    & mvn $mavenArgs
    
    if ($LASTEXITCODE -eq 0) {
        Write-Success "Tests executed successfully"
    } else {
        Write-Warning "Some tests may have failed"
    }
}

function Invoke-DockerTests {
    Write-Log "Running tests with Docker..."
    
    # Build Docker image
    Write-Log "Building Docker image..."
    & docker build -t paytr-tests .
    
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Failed to build Docker image"
        return $false
    }
    Write-Success "Docker image built successfully"
    
    # Run tests in Docker container
    Write-Log "Running tests in Docker container..."
    & docker run --rm `
        -v "${PWD}\test-results:/app/test-results" `
        -v "${PWD}\reports:/app/reports" `
        -v "${PWD}\screenshots:/app/screenshots" `
        -v "${PWD}\logs:/app/logs" `
        -e TEST_SUITE=$TestSuite `
        -e BROWSER=$Browser `
        -e ENVIRONMENT=$Environment `
        -e HEADLESS=$Headless `
        paytr-tests $TestSuite $Browser $Environment $Headless
    
    return $true
}

function New-AllureReport {
    Write-Log "Generating Allure report..."
    
    if (Test-Path "target\allure-results") {
        $allureResults = Get-ChildItem "target\allure-results" -File
        if ($allureResults.Count -gt 0) {
            try {
                $allureVersion = allure --version 2>&1
                if ($LASTEXITCODE -eq 0) {
                    & allure generate target\allure-results --clean --output target\allure-report
                    Write-Success "Allure report generated at: target\allure-report"
                    
                    # Copy to reports directory
                    $reportDir = "reports\allure-report-$Timestamp"
                    if (-not (Test-Path $reportDir)) {
                        New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
                    }
                    Copy-Item "target\allure-report\*" $reportDir -Recurse -Force
                } else {
                    Write-Warning "Allure is not installed. Skipping report generation."
                }
            }
            catch {
                Write-Warning "Allure is not installed. Skipping report generation."
            }
        } else {
            Write-Warning "No Allure results found. Skipping report generation."
        }
    } else {
        Write-Warning "Allure results directory not found. Skipping report generation."
    }
}

function New-HtmlReport {
    Write-Log "Generating HTML report..."
    
    $reportFile = "reports\test-report-$Timestamp.html"
    
    $htmlContent = @"
<!DOCTYPE html>
<html>
<head>
    <title>PayTR Test Report - $Timestamp</title>
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
        <p><strong>Timestamp:</strong> $Timestamp</p>
        <p><strong>Test Suite:</strong> $TestSuite</p>
        <p><strong>Browser:</strong> $Browser</p>
        <p><strong>Environment:</strong> $Environment</p>
        <p><strong>Headless:</strong> $Headless</p>
    </div>
    
    <div class="summary">
        <h2>Test Summary</h2>
        <!-- Test summary will be populated here -->
    </div>
    
    <div>
        <h2>Links</h2>
        <ul>
            <li><a href="allure-report-$Timestamp/index.html">Allure Report</a></li>
            <li><a href="../target/surefire-reports/index.html">Surefire Reports</a></li>
        </ul>
    </div>
</body>
</html>
"@
    
    $htmlContent | Out-File -FilePath $reportFile -Encoding UTF8
    Write-Success "HTML report generated at: $reportFile"
}

function Get-TestResults {
    Write-Log "Parsing test results..."
    
    $passed = 0
    $failed = 0
    $skipped = 0
    
    $testResultsFile = "target\surefire-reports\testng-results.xml"
    if (Test-Path $testResultsFile) {
        try {
            [xml]$testResults = Get-Content $testResultsFile
            $passed = [int]$testResults.testng.suite.passed
            $failed = [int]$testResults.testng.suite.failed
            $skipped = [int]$testResults.testng.suite.skipped
        }
        catch {
            Write-Warning "Could not parse test results file"
        }
    } else {
        Write-Warning "Test results file not found"
    }
    
    Write-Log "Test Results Summary:"
    Write-Success "Passed: $passed"
    Write-Error "Failed: $failed"
    Write-Warning "Skipped: $skipped"
    
    # Save results to file
    $summaryContent = @"
PayTR Test Automation Results
=============================
Timestamp: $Timestamp
Test Suite: $TestSuite
Browser: $Browser
Environment: $Environment

Results:
--------
Passed: $passed
Failed: $failed
Skipped: $skipped
"@
    
    $summaryContent | Out-File -FilePath "reports\test-summary-$Timestamp.txt" -Encoding UTF8
    
    return @{
        Passed = $passed
        Failed = $failed
        Skipped = $skipped
    }
}

function Send-Notifications {
    param([hashtable]$TestResults)
    
    Write-Log "Sending notifications..."
    
    # Email notification (requires additional setup)
    # Slack notification (requires webhook URL)
    
    Write-Log "Notification functionality requires additional configuration"
}

# Main execution
function Main {
    # Show help if requested
    if ($Help) {
        Show-Help
        return 0
    }
    
    # Validate parameters
    if ($TestSuite -notin $ValidTestSuites) {
        Write-Error "Invalid test suite: $TestSuite"
        Write-Log "Valid options: $($ValidTestSuites -join ', ')"
        return 1
    }
    
    if ($Browser -notin $ValidBrowsers) {
        Write-Error "Invalid browser: $Browser"
        Write-Log "Valid options: $($ValidBrowsers -join ', ')"
        return 1
    }
    
    if ($Environment -notin $ValidEnvironments) {
        Write-Error "Invalid environment: $Environment"
        Write-Log "Valid options: $($ValidEnvironments -join ', ')"
        return 1
    }
    
    Write-Log "PayTR Test Automation Script Started (PowerShell)"
    Write-Log "Timestamp: $Timestamp"
    
    # Show configuration
    Write-Log "Configuration:"
    Write-Log "  Test Suite: $TestSuite"
    Write-Log "  Browser: $Browser"
    Write-Log "  Environment: $Environment"
    Write-Log "  Headless: $Headless"
    Write-Log "  Parallel: $Parallel"
    Write-Log "  Docker: $UseDocker"
    Write-Log "  Grid: $UseGrid"
    Write-Log "  Report Only: $ReportOnly"
    Write-Log "  Clean: $Clean"
    
    # Check prerequisites
    if (-not (Test-Prerequisites)) {
        return 1
    }
    
    # Setup directories
    Initialize-Directories
    
    # Clean previous results if requested
    Clear-PreviousResults
    
    # Run tests or generate reports only
    if (-not $ReportOnly) {
        if ($UseDocker) {
            if (-not (Invoke-DockerTests)) {
                return 1
            }
        } else {
            Invoke-MavenTests
        }
    }
    
    # Generate reports
    New-AllureReport
    New-HtmlReport
    
    # Parse test results
    $testResults = Get-TestResults
    
    # Send notifications
    Send-Notifications $testResults
    
    # Final status
    if ($testResults.Failed -eq 0) {
        Write-Success "All tests passed successfully!"
        $exitCode = 0
    } else {
        Write-Warning "Some tests failed. Check reports for details."
        $exitCode = 1
    }
    
    Write-Log "Reports available at: $(Join-Path $PWD 'reports')"
    Write-Log "PayTR Test Automation Script Completed (PowerShell)"
    
    return $exitCode
}

# Execute main function
$exitCode = Main
exit $exitCode