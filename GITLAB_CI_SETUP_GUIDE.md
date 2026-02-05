# PayTR Test Suite - GitLab CI/CD Setup Guide

## 🚀 Overview

This guide provides comprehensive instructions for setting up and running PayTR test automation using GitLab CI/CD pipeline. The pipeline supports multiple test suites, browsers, and environments with automated reporting.

## 📋 Prerequisites

### Required Access
- GitLab account with access to: `https://gitlab-qa.paytr.com/ergun.ural/restassured`
- GitLab private token for API access
- Basic understanding of CI/CD concepts

### Required Tools
- Git (for repository management)
- curl (for API calls)
- bash (for script execution)

## 🔧 GitLab CI Configuration

### Pipeline Features

#### ✅ Multi-Stage Test Execution
1. **Build Stage** - Compile and prepare project
2. **Code Quality** - Static analysis and security checks
3. **Unit Tests** - Basic functionality tests
4. **Smoke Tests** - Critical functionality with Chrome/Firefox
5. **Comprehensive Tests** - Full test suite execution
6. **Performance Tests** - Load and performance validation
7. **Security Tests** - Security vulnerability testing
8. **Report Generation** - Allure test reports
9. **Deploy Reports** - GitLab Pages deployment
10. **Notification** - Test summary and alerts

#### 🌟 Key Features
- **Multiple Browser Support**: Chrome, Firefox, Edge
- **Parallel Execution**: Concurrent test runs
- **TestNG Integration**: Comprehensive test suite management
- **Allure Reporting**: Beautiful test reports with trends
- **GitLab Pages**: Automated report hosting
- **Scheduled Execution**: Nightly test runs
- **Manual Triggers**: On-demand test execution
- **Artifact Management**: Test results and screenshots

## 🚀 Quick Start

### 1. Set Up GitLab Access

#### Generate GitLab Private Token
1. Go to GitLab → User Settings → Access Tokens
2. Create new token with `api` and `read_repository` scopes
3. Save the token securely

#### Set Environment Variable
```bash
export GITLAB_PRIVATE_TOKEN="your-gitlab-private-token"
```

### 2. Trigger Pipeline Manually

#### Run Smoke Tests (Default)
```bash
./trigger-gitlab-pipeline.sh
```

#### Run Comprehensive Tests
```bash
./trigger-gitlab-pipeline.sh --test-suite comprehensive --browser chrome --environment test --wait
```

#### Run Performance Tests
```bash
./trigger-gitlab-pipeline.sh --test-suite performance --browser firefox --wait
```

### 3. Monitor Pipeline Execution

#### Check Pipeline Status
- Visit: `https://gitlab-qa.paytr.com/ergun.ural/restassured/-/pipelines`
- Click on the active pipeline to view real-time progress

#### View Test Reports
- **GitLab Pages**: `https://ergun-ural.gitlab.io/restassured/`
- **Pipeline Artifacts**: Download from pipeline details page
- **Allure Reports**: Interactive test results with trends

## 📊 Test Suite Options

### Available Test Suites

| Test Suite | Description | Execution Time | Use Case |
|------------|-------------|----------------|----------|
| **smoke** | Critical functionality tests | ~5 minutes | Quick validation |
| **comprehensive** | Full regression suite | ~15 minutes | Complete testing |
| **performance** | Load and performance tests | ~10 minutes | Performance validation |
| **security** | Security vulnerability tests | ~8 minutes | Security assessment |
| **regression** | Regression test suite | ~12 minutes | Release validation |

### Browser Options

| Browser | Version | Headless Mode | Status |
|---------|---------|---------------|---------|
| **Chrome** | Latest | ✅ Supported | Recommended |
| **Firefox** | Latest | ✅ Supported | Stable |
| **Edge** | Latest | ✅ Supported | Available |

### Environment Options

| Environment | URL | Description | Access Level |
|-------------|-----|-------------|--------------|
| **test** | https://zeus-uat.paytr.com | Test environment | Full access |
| **staging** | https://staging.paytr.com | Staging environment | Limited access |
| **production** | https://paytr.com | Production environment | Read-only tests |

## 🔧 Advanced Configuration

### Pipeline Variables

#### Default Variables
```yaml
variables:
  MAVEN_OPTS: "-Xmx2048m -XX:MaxPermSize=512m"
  JAVA_VERSION: "17"
  MAVEN_VERSION: "3.9"
  TEST_BROWSER: "chrome"
  TEST_ENVIRONMENT: "test"
  HEADLESS_MODE: "true"
```

#### Custom Variables
You can override default variables when triggering pipeline:
```bash
./trigger-gitlab-pipeline.sh --browser firefox --environment staging --headless false
```

### Scheduled Execution

#### Set Up Nightly Tests
1. Go to GitLab → CI/CD → Schedules
2. Create new schedule:
   - **Description**: Nightly PayTR Tests
   - **Interval Pattern**: `0 2 * * *` (2 AM daily)
   - **Target Branch**: `main`
   - **Variables**: `TEST_SUITE=comprehensive`

#### Custom Schedules
Create schedules for different test suites and environments as needed.

## 📈 Test Reports and Analytics

### Allure Test Reports
- **Interactive dashboards** with test trends
- **Detailed failure analysis** with screenshots
- **Performance metrics** and execution times
- **Test categorization** by features and severity

### Pipeline Artifacts
- **Test Results**: JUnit XML reports
- **Screenshots**: Failed test screenshots
- **Logs**: Detailed execution logs
- **Performance Data**: Response times and metrics

### GitLab Pages
Automated hosting of test reports at:
```
https://ergun-ural.gitlab.io/restassured/
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Pipeline Trigger Failures
**Issue**: `Failed to trigger pipeline`
**Solution**: 
- Verify GitLab private token
- Check network connectivity
- Ensure branch exists

#### 2. Test Execution Failures
**Issue**: High failure rate in tests
**Solution**:
- Check test environment availability
- Verify WebDriver compatibility
- Review test data and configurations

#### 3. Report Generation Issues
**Issue**: Allure reports not generated
**Solution**:
- Check Allure plugin configuration
- Verify test result files exist
- Review Maven configuration

### Debug Commands

#### Check Pipeline Status
```bash
curl -H "PRIVATE-TOKEN: $GITLAB_PRIVATE_TOKEN" \
  "https://gitlab-qa.paytr.com/api/v4/projects/ergun.ural%2Frestassured/pipelines"
```

#### Download Artifacts
```bash
curl -H "PRIVATE-TOKEN: $GITLAB_PRIVATE_TOKEN" \
  "https://gitlab-qa.paytr.com/api/v4/projects/ergun.ural%2Frestassured/jobs/artifacts/main/download?job=allure-report"
```

## 📋 Best Practices

### 1. Test Suite Selection
- **Development**: Use `smoke` suite for quick validation
- **Pre-release**: Use `comprehensive` suite for full testing
- **Production**: Use `regression` suite for critical paths

### 2. Browser Selection
- **CI/CD**: Use `chrome` for consistency
- **Cross-browser**: Test with `firefox` and `edge`
- **Performance**: Use `chrome` with headless mode

### 3. Environment Management
- **Development**: Use `test` environment
- **Staging**: Use `staging` for pre-production
- **Production**: Use `production` with caution

### 4. Monitoring and Alerts
- Monitor pipeline success rates
- Set up notifications for failures
- Review test trends regularly
- Update test suites based on results

## 🔗 Useful Links

### GitLab Repository
- **Project**: `https://gitlab-qa.paytr.com/ergun.ural/restassured`
- **Pipelines**: `https://gitlab-qa.paytr.com/ergun.ural/restassured/-/pipelines`
- **CI/CD Config**: `.gitlab-ci.yml`

### Test Reports
- **GitLab Pages**: `https://ergun-ural.gitlab.io/restassured/`
- **Pipeline Artifacts**: Available in pipeline details

### Documentation
- **Test Scenarios**: `PayTR_Comprehensive_Test_Scenarios.md`
- **Latest Results**: `LATEST_TEST_EXECUTION_SUMMARY.md`
- **Full Results**: `TEST_RESULTS_FEB_5_2026.md`

## 📞 Support

### Getting Help
1. **Check Pipeline Logs**: Review job logs in GitLab
2. **Verify Configuration**: Review `.gitlab-ci.yml`
3. **Test Environment**: Ensure test environment is available
4. **Documentation**: Refer to this guide and linked documents

### Contact Information
- **Repository**: `https://gitlab-qa.paytr.com/ergun.ural/restassured`
- **Issues**: Create issue in GitLab repository

---

**Last Updated**: February 5, 2026  
**Version**: 1.0  
**Status**: Production Ready ✅