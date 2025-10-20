pipeline {
    agent any
    
    parameters {
        choice(
            name: 'TEST_SUITE',
            choices: ['comprehensive', 'security', 'performance', 'api', 'smoke'],
            description: 'Test Suite to run'
        )
        choice(
            name: 'BROWSER',
            choices: ['chrome', 'firefox', 'edge'],
            description: 'Browser to use for testing'
        )
        choice(
            name: 'ENVIRONMENT',
            choices: ['staging', 'production', 'development'],
            description: 'Test Environment'
        )
        booleanParam(
            name: 'HEADLESS',
            defaultValue: true,
            description: 'Run tests in headless mode'
        )
        booleanParam(
            name: 'PARALLEL_EXECUTION',
            defaultValue: true,
            description: 'Enable parallel test execution'
        )
    }
    
    environment {
        JAVA_HOME = '/usr/lib/jvm/java-11-openjdk'
        MAVEN_HOME = '/usr/share/maven'
        ALLURE_HOME = '/opt/allure'
        PATH = "${JAVA_HOME}/bin:${MAVEN_HOME}/bin:${ALLURE_HOME}/bin:${PATH}"
        
        // Test configuration
        TEST_SUITE = "${params.TEST_SUITE}"
        BROWSER = "${params.BROWSER}"
        ENVIRONMENT = "${params.ENVIRONMENT}"
        HEADLESS = "${params.HEADLESS}"
        PARALLEL_EXECUTION = "${params.PARALLEL_EXECUTION}"
        
        // Reporting
        ALLURE_RESULTS_DIR = 'target/allure-results'
        ALLURE_REPORT_DIR = 'target/allure-report'
        SCREENSHOTS_DIR = 'screenshots'
        LOGS_DIR = 'logs'
    }
    
    triggers {
        // Zamanlanmış çalıştırma - her gün saat 02:00, 08:00, 14:00, 20:00
        cron('0 2,8,14,20 * * *')
        
        // SCM değişikliklerinde çalıştır
        pollSCM('H/5 * * * *')
    }
    
    options {
        // Build geçmişini sınırla
        buildDiscarder(logRotator(numToKeepStr: '30', daysToKeepStr: '30'))
        
        // Timeout
        timeout(time: 2, unit: 'HOURS')
        
        // Timestamps
        timestamps()
        
        // Parallel builds'i engelle
        disableConcurrentBuilds()
        
        // Retry on failure
        retry(1)
    }
    
    stages {
        stage('Preparation') {
            steps {
                script {
                    echo "🚀 PayTR Test Automation Pipeline Başlatılıyor..."
                    echo "📋 Test Suite: ${TEST_SUITE}"
                    echo "🌐 Browser: ${BROWSER}"
                    echo "🏗️ Environment: ${ENVIRONMENT}"
                    echo "👁️ Headless: ${HEADLESS}"
                    echo "⚡ Parallel: ${PARALLEL_EXECUTION}"
                    
                    // Build bilgilerini kaydet
                    currentBuild.description = "Suite: ${TEST_SUITE} | Browser: ${BROWSER} | Env: ${ENVIRONMENT}"
                }
                
                // Workspace'i temizle
                cleanWs()
                
                // Kodu çek
                checkout scm
                
                // Directories oluştur
                sh '''
                    mkdir -p ${SCREENSHOTS_DIR}
                    mkdir -p ${LOGS_DIR}
                    mkdir -p target/surefire-reports
                    mkdir -p ${ALLURE_RESULTS_DIR}
                '''
            }
        }
        
        stage('Environment Setup') {
            parallel {
                stage('Java & Maven Setup') {
                    steps {
                        sh '''
                            echo "☕ Java Version:"
                            java -version
                            echo "🔨 Maven Version:"
                            mvn -version
                        '''
                    }
                }
                
                stage('Browser Setup') {
                    steps {
                        script {
                            if (BROWSER == 'chrome') {
                                sh '''
                                    echo "🌐 Chrome Browser Setup"
                                    google-chrome --version || echo "Chrome not found, installing..."
                                    # Chrome kurulum komutları buraya eklenebilir
                                '''
                            } else if (BROWSER == 'firefox') {
                                sh '''
                                    echo "🦊 Firefox Browser Setup"
                                    firefox --version || echo "Firefox not found, installing..."
                                    # Firefox kurulum komutları buraya eklenebilir
                                '''
                            }
                        }
                    }
                }
                
                stage('Allure Setup') {
                    steps {
                        sh '''
                            echo "📊 Allure Setup"
                            allure --version || echo "Allure not found, installing..."
                            # Allure kurulum komutları buraya eklenebilir
                        '''
                    }
                }
            }
        }
        
        stage('Dependency Management') {
            steps {
                echo "📦 Maven Dependencies İndiriliyor..."
                sh 'mvn clean compile test-compile'
            }
        }
        
        stage('Pre-Test Validation') {
            steps {
                script {
                    echo "✅ Pre-Test Validations"
                    
                    // Test dosyalarının varlığını kontrol et
                    sh '''
                        if [ ! -f "src/test/resources/testng-${TEST_SUITE}.xml" ]; then
                            echo "❌ Test suite file not found: testng-${TEST_SUITE}.xml"
                            exit 1
                        fi
                        echo "✅ Test suite file found: testng-${TEST_SUITE}.xml"
                    '''
                    
                    // Test environment'ın erişilebilir olduğunu kontrol et
                    sh '''
                        echo "🌐 Testing environment connectivity..."
                        # Environment connectivity check komutları buraya eklenebilir
                    '''
                }
            }
        }
        
        stage('Test Execution') {
            parallel {
                stage('Smoke Tests') {
                    when {
                        anyOf {
                            expression { params.TEST_SUITE == 'smoke' }
                            expression { params.TEST_SUITE == 'comprehensive' }
                        }
                    }
                    steps {
                        script {
                            echo "💨 Smoke Tests Çalıştırılıyor..."
                            sh '''
                                mvn test \
                                    -Dsurefire.suiteXmlFiles=src/test/resources/testng-smoke.xml \
                                    -Dbrowser=${BROWSER} \
                                    -Denvironment=${ENVIRONMENT} \
                                    -Dheadless=${HEADLESS} \
                                    -Dallure.results.directory=${ALLURE_RESULTS_DIR} \
                                    -Dmaven.test.failure.ignore=true
                            '''
                        }
                    }
                }
                
                stage('Security Tests') {
                    when {
                        anyOf {
                            expression { params.TEST_SUITE == 'security' }
                            expression { params.TEST_SUITE == 'comprehensive' }
                        }
                    }
                    steps {
                        script {
                            echo "🔒 Security Tests Çalıştırılıyor..."
                            sh '''
                                mvn test \
                                    -Dsurefire.suiteXmlFiles=src/test/resources/testng-security.xml \
                                    -Dbrowser=${BROWSER} \
                                    -Denvironment=${ENVIRONMENT} \
                                    -Dheadless=${HEADLESS} \
                                    -Dsecurity.level=high \
                                    -Dallure.results.directory=${ALLURE_RESULTS_DIR} \
                                    -Dmaven.test.failure.ignore=true
                            '''
                        }
                    }
                }
                
                stage('Performance Tests') {
                    when {
                        anyOf {
                            expression { params.TEST_SUITE == 'performance' }
                            expression { params.TEST_SUITE == 'comprehensive' }
                        }
                    }
                    steps {
                        script {
                            echo "⚡ Performance Tests Çalıştırılıyor..."
                            sh '''
                                mvn test \
                                    -Dsurefire.suiteXmlFiles=src/test/resources/testng-performance.xml \
                                    -Dbrowser=${BROWSER} \
                                    -Denvironment=${ENVIRONMENT} \
                                    -Dheadless=${HEADLESS} \
                                    -Dperformance.threshold.page.load=3000 \
                                    -Dperformance.threshold.api.response=1000 \
                                    -Dallure.results.directory=${ALLURE_RESULTS_DIR} \
                                    -Dmaven.test.failure.ignore=true
                            '''
                        }
                    }
                }
                
                stage('API Tests') {
                    when {
                        anyOf {
                            expression { params.TEST_SUITE == 'api' }
                            expression { params.TEST_SUITE == 'comprehensive' }
                        }
                    }
                    steps {
                        script {
                            echo "🔌 API Tests Çalıştırılıyor..."
                            sh '''
                                mvn test \
                                    -Dsurefire.suiteXmlFiles=src/test/resources/testng-api.xml \
                                    -Denvironment=${ENVIRONMENT} \
                                    -Dapi.timeout=30000 \
                                    -Dapi.retries=3 \
                                    -Dallure.results.directory=${ALLURE_RESULTS_DIR} \
                                    -Dmaven.test.failure.ignore=true
                            '''
                        }
                    }
                }
            }
        }
        
        stage('Test Results Analysis') {
            steps {
                script {
                    echo "📊 Test Sonuçları Analiz Ediliyor..."
                    
                    // Test sonuçlarını parse et
                    def testResults = sh(
                        script: '''
                            if [ -f "target/surefire-reports/testng-results.xml" ]; then
                                grep -o 'passed="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0"
                            else
                                echo "0"
                            fi
                        ''',
                        returnStdout: true
                    ).trim()
                    
                    def failedTests = sh(
                        script: '''
                            if [ -f "target/surefire-reports/testng-results.xml" ]; then
                                grep -o 'failed="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0"
                            else
                                echo "0"
                            fi
                        ''',
                        returnStdout: true
                    ).trim()
                    
                    def skippedTests = sh(
                        script: '''
                            if [ -f "target/surefire-reports/testng-results.xml" ]; then
                                grep -o 'skipped="[0-9]*"' target/surefire-reports/testng-results.xml | cut -d'"' -f2 || echo "0"
                            else
                                echo "0"
                            fi
                        ''',
                        returnStdout: true
                    ).trim()
                    
                    echo "✅ Başarılı Testler: ${testResults}"
                    echo "❌ Başarısız Testler: ${failedTests}"
                    echo "⏭️ Atlanan Testler: ${skippedTests}"
                    
                    // Build description'ı güncelle
                    currentBuild.description += " | ✅${testResults} ❌${failedTests} ⏭️${skippedTests}"
                }
            }
        }
        
        stage('Report Generation') {
            steps {
                script {
                    echo "📋 Raporlar Oluşturuluyor..."
                    
                    // Allure raporu oluştur
                    sh '''
                        if [ -d "${ALLURE_RESULTS_DIR}" ] && [ "$(ls -A ${ALLURE_RESULTS_DIR})" ]; then
                            allure generate ${ALLURE_RESULTS_DIR} --clean --output ${ALLURE_REPORT_DIR}
                            echo "✅ Allure raporu oluşturuldu"
                        else
                            echo "⚠️ Allure results bulunamadı"
                        fi
                    '''
                }
            }
        }
    }
    
    post {
        always {
            script {
                echo "🧹 Post-Build İşlemleri..."
                
                // Test sonuçlarını arşivle
                archiveArtifacts artifacts: '''
                    target/surefire-reports/**/*,
                    target/allure-results/**/*,
                    target/allure-report/**/*,
                    screenshots/**/*,
                    logs/**/*
                ''', allowEmptyArchive: true
                
                // TestNG sonuçlarını yayınla
                publishTestResults testResultsPattern: 'target/surefire-reports/testng-results.xml'
                
                // Allure raporunu yayınla
                allure([
                    includeProperties: false,
                    jdk: '',
                    properties: [],
                    reportBuildPolicy: 'ALWAYS',
                    results: [[path: 'target/allure-results']]
                ])
                
                // JUnit test sonuçlarını yayınla
                publishTestResults testResultsPattern: 'target/surefire-reports/TEST-*.xml'
            }
        }
        
        success {
            script {
                echo "✅ Pipeline Başarıyla Tamamlandı!"
                
                // Başarı bildirimi gönder
                emailext (
                    subject: "✅ PayTR Test Automation - SUCCESS - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>PayTR Test Automation - Başarılı</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Test Suite:</strong> ${TEST_SUITE}</p>
                        <p><strong>Browser:</strong> ${BROWSER}</p>
                        <p><strong>Environment:</strong> ${ENVIRONMENT}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        
                        <p><a href="${env.BUILD_URL}">Build Details</a></p>
                        <p><a href="${env.BUILD_URL}allure/">Test Report</a></p>
                        
                        <p><em>Bu otomatik bir mesajdır.</em></p>
                    """,
                    to: "${env.EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )
                
                // Slack bildirimi
                slackSend(
                    channel: '#paytr-tests',
                    color: 'good',
                    message: """
                        ✅ *PayTR Test Automation - SUCCESS*
                        *Job:* ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Suite:* ${TEST_SUITE} | *Browser:* ${BROWSER} | *Env:* ${ENVIRONMENT}
                        *Duration:* ${currentBuild.durationString}
                        *Report:* ${env.BUILD_URL}allure/
                    """
                )
            }
        }
        
        failure {
            script {
                echo "❌ Pipeline Başarısız!"
                
                // Hata bildirimi gönder
                emailext (
                    subject: "❌ PayTR Test Automation - FAILURE - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>PayTR Test Automation - Başarısız</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Test Suite:</strong> ${TEST_SUITE}</p>
                        <p><strong>Browser:</strong> ${BROWSER}</p>
                        <p><strong>Environment:</strong> ${ENVIRONMENT}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        
                        <p><strong>Failure Reason:</strong> ${currentBuild.result}</p>
                        
                        <p><a href="${env.BUILD_URL}">Build Details</a></p>
                        <p><a href="${env.BUILD_URL}console">Console Output</a></p>
                        <p><a href="${env.BUILD_URL}allure/">Test Report</a></p>
                        
                        <p><em>Bu otomatik bir mesajdır.</em></p>
                    """,
                    to: "${env.EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )
                
                // Slack bildirimi
                slackSend(
                    channel: '#paytr-tests',
                    color: 'danger',
                    message: """
                        ❌ *PayTR Test Automation - FAILURE*
                        *Job:* ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Suite:* ${TEST_SUITE} | *Browser:* ${BROWSER} | *Env:* ${ENVIRONMENT}
                        *Duration:* ${currentBuild.durationString}
                        *Console:* ${env.BUILD_URL}console
                        *Report:* ${env.BUILD_URL}allure/
                    """
                )
            }
        }
        
        unstable {
            script {
                echo "⚠️ Pipeline Kararsız (Bazı testler başarısız)!"
                
                // Kararsız durum bildirimi
                emailext (
                    subject: "⚠️ PayTR Test Automation - UNSTABLE - ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """
                        <h2>PayTR Test Automation - Kararsız</h2>
                        <p><strong>Job:</strong> ${env.JOB_NAME}</p>
                        <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                        <p><strong>Test Suite:</strong> ${TEST_SUITE}</p>
                        <p><strong>Browser:</strong> ${BROWSER}</p>
                        <p><strong>Environment:</strong> ${ENVIRONMENT}</p>
                        <p><strong>Duration:</strong> ${currentBuild.durationString}</p>
                        
                        <p><strong>Status:</strong> Bazı testler başarısız oldu</p>
                        
                        <p><a href="${env.BUILD_URL}">Build Details</a></p>
                        <p><a href="${env.BUILD_URL}testReport/">Test Results</a></p>
                        <p><a href="${env.BUILD_URL}allure/">Test Report</a></p>
                        
                        <p><em>Bu otomatik bir mesajdır.</em></p>
                    """,
                    to: "${env.EMAIL_RECIPIENTS}",
                    mimeType: 'text/html'
                )
                
                // Slack bildirimi
                slackSend(
                    channel: '#paytr-tests',
                    color: 'warning',
                    message: """
                        ⚠️ *PayTR Test Automation - UNSTABLE*
                        *Job:* ${env.JOB_NAME} #${env.BUILD_NUMBER}
                        *Suite:* ${TEST_SUITE} | *Browser:* ${BROWSER} | *Env:* ${ENVIRONMENT}
                        *Duration:* ${currentBuild.durationString}
                        *Tests:* ${env.BUILD_URL}testReport/
                        *Report:* ${env.BUILD_URL}allure/
                    """
                )
            }
        }
        
        cleanup {
            script {
                echo "🧹 Cleanup İşlemleri..."
                
                // Eski build artifact'larını temizle
                sh '''
                    find . -name "*.log" -mtime +7 -delete || true
                    find . -name "*.tmp" -delete || true
                '''
            }
        }
    }
}