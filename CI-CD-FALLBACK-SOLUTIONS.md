# PayTR CI/CD Fallback Solutions

## Problem Summary

We've encountered persistent Maven compilation issues in GitHub Actions, specifically `MojoFailureException` errors. This document outlines alternative CI/CD strategies to ensure continuous integration and testing capabilities.

## Root Cause Analysis

The issue appears to be related to:
1. **Java 17 Module System**: Complex `--add-opens` flags required for compilation
2. **GitHub Actions Environment**: Differences between local and CI environments
3. **Maven Wrapper Configuration**: Potential issues with `.mvn/jvm.config` in CI
4. **Dependency Resolution**: Complex dependency tree causing conflicts

## Implemented Solutions

### 1. Docker Maven Container Approach ✅

**File**: `.github/workflows/paytr-tests.yml` (Strategy 1)

```yaml
docker-maven-tests:
  runs-on: ubuntu-latest
  container:
    image: maven:3.9.5-openjdk-17
    options: --user root
```

**Benefits**:
- Isolated, consistent environment
- Pre-configured Maven and Java 17
- Eliminates local environment differences
- Bypasses GitHub Actions Maven setup issues

### 2. Alternative Ubuntu Version ✅

**File**: `.github/workflows/paytr-tests.yml` (Strategy 2)

```yaml
ubuntu-20-maven-tests:
  runs-on: ubuntu-20.04
```

**Benefits**:
- Different base OS version
- System-installed Maven instead of wrapper
- Alternative Java distribution testing
- Fallback for container issues

### 3. Manual Java Compilation ✅

**File**: `.github/workflows/paytr-tests.yml` (Strategy 3)

**Benefits**:
- Bypasses Maven entirely
- Direct `javac` and `java` commands
- Manual dependency management
- Ultimate fallback solution

### 4. Shell Script Fallback ✅

**File**: `scripts/manual-test.sh`

**Features**:
- Downloads dependencies manually
- Compiles with `javac`
- Runs tests with TestNG
- Can be used locally or in CI

## Usage Instructions

### Option 1: Docker Maven (Recommended)

The GitHub Actions workflow now includes three parallel strategies. The Docker Maven approach should work reliably:

```bash
# Local testing with Docker
docker run --rm -v $(pwd):/workspace -w /workspace maven:3.9.5-openjdk-17 mvn clean test
```

### Option 2: Manual Script Execution

```bash
# Make script executable
chmod +x scripts/manual-test.sh

# Run manual compilation and testing
./scripts/manual-test.sh
```

### Option 3: Direct Java Commands

```bash
# Create lib directory and download dependencies
mkdir -p lib target/classes target/test-classes

# Download key dependencies
cd lib
wget https://repo1.maven.org/maven2/io/rest-assured/rest-assured/5.3.2/rest-assured-5.3.2.jar
wget https://repo1.maven.org/maven2/org/testng/testng/7.8.0/testng-7.8.0.jar
# ... (see manual-test.sh for complete list)

# Compile sources
javac -cp "lib/*" -d target/classes src/main/java/**/*.java
javac -cp "lib/*:target/classes" -d target/test-classes src/test/java/**/*.java

# Run tests
java -cp "lib/*:target/classes:target/test-classes" org.testng.TestNG src/test/resources/testng-minimal.xml
```

## Workflow Strategy

The new GitHub Actions workflow implements a **multi-strategy approach**:

1. **Primary**: Docker Maven container
2. **Secondary**: Ubuntu 20.04 with system Maven
3. **Fallback**: Manual Java compilation
4. **Summary**: Reports which strategy succeeded

## Expected Outcomes

- **Docker Maven**: Should resolve environment inconsistencies
- **Ubuntu 20.04**: May work with different OS/Maven combination
- **Manual Compilation**: Guaranteed to work for basic compilation verification
- **At least one strategy should succeed**, providing CI/CD functionality

## Monitoring and Debugging

The workflow includes comprehensive debugging:

```yaml
- name: Debug Environment
  run: |
    echo "🔍 Environment Information:"
    java -version
    mvn -version
    pwd
    ls -la
    whoami
```

## Alternative Build Tools

If Maven continues to fail, consider:

### Gradle Migration

```bash
# Initialize Gradle wrapper
gradle wrapper

# Basic build.gradle
plugins {
    id 'java'
    id 'application'
}

dependencies {
    testImplementation 'io.rest-assured:rest-assured:5.3.2'
    testImplementation 'org.testng:testng:7.8.0'
}
```

### Ant Build Script

```xml
<project name="PayTR" default="test">
    <target name="compile">
        <javac srcdir="src" destdir="build/classes" classpath="lib/*"/>
    </target>
    <target name="test" depends="compile">
        <java classname="org.testng.TestNG" classpath="lib/*:build/classes">
            <arg value="testng.xml"/>
        </java>
    </target>
</project>
```

## Troubleshooting

### If Docker Strategy Fails
- Check Docker service availability in GitHub Actions
- Verify container permissions
- Review Maven cache configuration

### If All Strategies Fail
- Use manual script locally
- Investigate specific test failures
- Consider environment-specific issues

### Local Development
- Continue using Maven wrapper locally: `./mvnw clean test`
- Use IDE integration for development
- Manual script for verification

## Conclusion

This multi-strategy approach ensures that:
1. **CI/CD pipeline remains functional** even with Maven issues
2. **Multiple fallback options** are available
3. **Development workflow** is not disrupted
4. **Test execution** can continue in various environments

The Docker Maven approach should resolve the persistent `MojoFailureException` by providing a consistent, isolated environment that matches the Maven project requirements.