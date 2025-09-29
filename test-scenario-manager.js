// Test Scenario Manager JavaScript
class TestScenarioManager {
    constructor() {
        this.scenarios = [];
        this.selectedScenarios = [];
        this.testResults = [];
        this.isTestRunning = false;
        this.currentTestIndex = 0;
        this.apiBaseUrl = 'http://localhost:8080/api/test-scenarios'; // Backend API URL
        
        this.loadScenariosFromStorage();
        this.initializeEventListeners();
        this.renderScenarioList();
    }

    // Event Listeners
    initializeEventListeners() {
        // Form submission
        document.getElementById('scenarioForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.saveScenario();
        });

        // Search functionality
        document.getElementById('searchScenarios').addEventListener('input', (e) => {
            this.filterScenarios();
        });

        // Category filter
        document.getElementById('filterCategory').addEventListener('change', (e) => {
            this.filterScenarios();
        });
    }

    // Scenario Management
    async saveScenario() {
        const scenario = {
            id: Date.now().toString(),
            name: document.getElementById('scenarioName').value,
            description: document.getElementById('scenarioDescription').value,
            category: document.getElementById('scenarioCategory').value,
            priority: document.getElementById('scenarioPriority').value,
            steps: this.getStepsFromForm(),
            expectedResults: document.getElementById('expectedResults').value,
            testData: this.parseTestData(),
            createdAt: new Date().toISOString(),
            lastModified: new Date().toISOString()
        };

        if (this.validateScenario(scenario)) {
            try {
                const response = await fetch(`${this.apiBaseUrl}`, {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(scenario)
                });
                
                const result = await response.json();
                
                if (result.success) {
                    await this.loadScenariosFromStorage(); // Senaryoları yeniden yükle
                    this.renderScenarioList();
                    this.clearForm();
                    this.showNotification('Senaryo başarıyla kaydedildi!', 'success');
                } else {
                    throw new Error(result.error || 'Senaryo kaydedilemedi');
                }
            } catch (error) {
                console.error('Senaryo kaydetme hatası:', error);
                // Fallback: localStorage'a kaydet
                this.scenarios.push(scenario);
                this.saveScenariosToStorage();
                this.renderScenarioList();
                this.clearForm();
                this.showNotification('Senaryo yerel olarak kaydedildi (sunucu bağlantısı yok)', 'warning');
            }
        }
    }

    validateScenario(scenario) {
        if (!scenario.name.trim()) {
            this.showNotification('Senaryo adı gereklidir!', 'error');
            return false;
        }
        if (!scenario.category) {
            this.showNotification('Kategori seçimi gereklidir!', 'error');
            return false;
        }
        if (scenario.steps.length === 0) {
            this.showNotification('En az bir test adımı gereklidir!', 'error');
            return false;
        }
        if (!scenario.expectedResults.trim()) {
            this.showNotification('Beklenen sonuçlar gereklidir!', 'error');
            return false;
        }
        return true;
    }

    getStepsFromForm() {
        const stepInputs = document.querySelectorAll('#stepsContainer input');
        const steps = [];
        stepInputs.forEach((input, index) => {
            if (input.value.trim()) {
                steps.push({
                    order: index + 1,
                    description: input.value.trim()
                });
            }
        });
        return steps;
    }

    parseTestData() {
        const testDataText = document.getElementById('testData').value.trim();
        if (!testDataText) return {};
        
        try {
            return JSON.parse(testDataText);
        } catch (e) {
            this.showNotification('Test verileri geçerli JSON formatında değil!', 'warning');
            return {};
        }
    }

    // UI Management
    renderScenarioList() {
        const container = document.getElementById('scenarioList');
        container.innerHTML = '';

        if (this.scenarios.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: #6c757d; margin-top: 20px;">Henüz senaryo eklenmedi</p>';
            return;
        }

        this.scenarios.forEach(scenario => {
            const scenarioElement = this.createScenarioElement(scenario);
            container.appendChild(scenarioElement);
        });
    }

    createScenarioElement(scenario) {
        const div = document.createElement('div');
        div.className = `scenario-item priority-${scenario.priority}`;
        div.dataset.scenarioId = scenario.id;
        
        const priorityIcon = {
            'high': '🔴',
            'medium': '🟡',
            'low': '🟢'
        }[scenario.priority];

        const categoryIcon = {
            'login': '🔐',
            'ui': '🖥️',
            'api': '🔌',
            'security': '🛡️',
            'performance': '⚡'
        }[scenario.category] || '📋';

        div.innerHTML = `
            <div class="scenario-title">${categoryIcon} ${scenario.name}</div>
            <div class="scenario-meta">
                ${priorityIcon} ${this.getPriorityText(scenario.priority)} | 
                ${scenario.steps.length} adım | 
                ${new Date(scenario.createdAt).toLocaleDateString('tr-TR')}
            </div>
            <div style="margin-top: 10px;">
                <button class="btn btn-primary" style="padding: 4px 8px; font-size: 12px; margin-right: 5px;" onclick="testManager.selectScenario('${scenario.id}')">Seç</button>
                <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px; margin-right: 5px;" onclick="testManager.editScenario('${scenario.id}')">Düzenle</button>
                <button class="btn btn-danger" style="padding: 4px 8px; font-size: 12px;" onclick="testManager.deleteScenario('${scenario.id}')">Sil</button>
            </div>
        `;

        return div;
    }

    getPriorityText(priority) {
        const texts = {
            'high': 'Yüksek',
            'medium': 'Orta',
            'low': 'Düşük'
        };
        return texts[priority] || priority;
    }

    selectScenario(scenarioId) {
        const scenario = this.scenarios.find(s => s.id === scenarioId);
        if (!scenario) return;

        if (this.selectedScenarios.find(s => s.id === scenarioId)) {
            this.selectedScenarios = this.selectedScenarios.filter(s => s.id !== scenarioId);
            this.showNotification('Senaryo seçimden çıkarıldı', 'info');
        } else {
            this.selectedScenarios.push(scenario);
            this.showNotification('Senaryo seçildi', 'success');
        }

        this.updateSelectedScenariosDisplay();
        this.updateScenarioSelection();
    }

    updateScenarioSelection() {
        document.querySelectorAll('.scenario-item').forEach(item => {
            const scenarioId = item.dataset.scenarioId;
            if (this.selectedScenarios.find(s => s.id === scenarioId)) {
                item.classList.add('selected');
            } else {
                item.classList.remove('selected');
            }
        });
    }

    updateSelectedScenariosDisplay() {
        const container = document.getElementById('selectedScenarios');
        
        if (this.selectedScenarios.length === 0) {
            container.innerHTML = '<p style="color: #6c757d;">Henüz senaryo seçilmedi</p>';
            return;
        }

        container.innerHTML = this.selectedScenarios.map(scenario => `
            <div class="scenario-item" style="margin-bottom: 10px;">
                <div class="scenario-title">${scenario.name}</div>
                <div class="scenario-meta">${scenario.steps.length} adım</div>
            </div>
        `).join('');
    }

    editScenario(scenarioId) {
        const scenario = this.scenarios.find(s => s.id === scenarioId);
        if (!scenario) return;

        // Switch to edit tab
        this.showTab('edit');
        
        // Populate edit form
        this.populateEditForm(scenario);
    }

    populateEditForm(scenario) {
        const editContent = document.getElementById('editContent');
        editContent.innerHTML = `
            <form id="editScenarioForm">
                <input type="hidden" id="editScenarioId" value="${scenario.id}">
                
                <div class="form-group">
                    <label for="editScenarioName">Senaryo Adı *</label>
                    <input type="text" id="editScenarioName" class="form-control" value="${scenario.name}" required>
                </div>
                
                <div class="form-group">
                    <label for="editScenarioDescription">Senaryo Açıklaması</label>
                    <textarea id="editScenarioDescription" class="form-control" rows="3">${scenario.description}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="editScenarioCategory">Kategori *</label>
                    <select id="editScenarioCategory" class="form-control" required>
                        <option value="login" ${scenario.category === 'login' ? 'selected' : ''}>Giriş Testleri</option>
                        <option value="ui" ${scenario.category === 'ui' ? 'selected' : ''}>UI Testleri</option>
                        <option value="api" ${scenario.category === 'api' ? 'selected' : ''}>API Testleri</option>
                        <option value="security" ${scenario.category === 'security' ? 'selected' : ''}>Güvenlik Testleri</option>
                        <option value="performance" ${scenario.category === 'performance' ? 'selected' : ''}>Performans Testleri</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label for="editScenarioPriority">Öncelik *</label>
                    <select id="editScenarioPriority" class="form-control" required>
                        <option value="high" ${scenario.priority === 'high' ? 'selected' : ''}>🔴 Yüksek</option>
                        <option value="medium" ${scenario.priority === 'medium' ? 'selected' : ''}>🟡 Orta</option>
                        <option value="low" ${scenario.priority === 'low' ? 'selected' : ''}>🟢 Düşük</option>
                    </select>
                </div>
                
                <div class="form-group">
                    <label>Test Adımları *</label>
                    <div id="editStepsContainer">
                        ${scenario.steps.map((step, index) => `
                            <div class="step-item">
                                <span class="step-number">${index + 1}</span>
                                <input type="text" class="form-control" value="${step.description}" style="flex: 1;">
                                <button type="button" class="btn btn-danger" style="margin-left: 10px; padding: 8px 12px;" onclick="this.parentElement.remove(); testManager.updateStepNumbers('editStepsContainer')">🗑️</button>
                            </div>
                        `).join('')}
                    </div>
                    <button type="button" class="btn btn-secondary" onclick="testManager.addStepToContainer('editStepsContainer')" style="margin-top: 10px;">➕ Adım Ekle</button>
                </div>
                
                <div class="form-group">
                    <label for="editExpectedResults">Beklenen Sonuçlar *</label>
                    <textarea id="editExpectedResults" class="form-control" rows="3" required>${scenario.expectedResults}</textarea>
                </div>
                
                <div class="form-group">
                    <label for="editTestData">Test Verileri (JSON formatında)</label>
                    <textarea id="editTestData" class="form-control" rows="4">${JSON.stringify(scenario.testData, null, 2)}</textarea>
                </div>
                
                <button type="submit" class="btn btn-primary">💾 Değişiklikleri Kaydet</button>
                <button type="button" class="btn btn-secondary" onclick="testManager.cancelEdit()" style="margin-left: 10px;">❌ İptal</button>
            </form>
        `;

        // Add event listener for edit form
        document.getElementById('editScenarioForm').addEventListener('submit', (e) => {
            e.preventDefault();
            this.updateScenario();
        });
    }

    async updateScenario() {
        const scenarioId = document.getElementById('editScenarioId').value;
        const scenarioIndex = this.scenarios.findIndex(s => s.id === scenarioId);
        
        if (scenarioIndex === -1) return;

        const updatedScenario = {
            ...this.scenarios[scenarioIndex],
            name: document.getElementById('editScenarioName').value,
            description: document.getElementById('editScenarioDescription').value,
            category: document.getElementById('editScenarioCategory').value,
            priority: document.getElementById('editScenarioPriority').value,
            steps: this.getStepsFromContainer('editStepsContainer'),
            expectedResults: document.getElementById('editExpectedResults').value,
            testData: this.parseTestDataFromField('editTestData'),
            lastModified: new Date().toISOString()
        };

        if (this.validateScenario(updatedScenario)) {
            try {
                const response = await fetch(`${this.apiBaseUrl}/${scenarioId}`, {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify(updatedScenario)
                });
                
                const result = await response.json();
                
                if (result.success) {
                    await this.loadScenariosFromStorage(); // Senaryoları yeniden yükle
                    this.renderScenarioList();
                    this.showNotification('Senaryo başarıyla güncellendi!', 'success');
                    this.showTab('create');
                } else {
                    throw new Error(result.error || 'Senaryo güncellenemedi');
                }
            } catch (error) {
                console.error('Senaryo güncelleme hatası:', error);
                // Fallback: localStorage'da güncelle
                this.scenarios[scenarioIndex] = updatedScenario;
                this.saveScenariosToStorage();
                this.renderScenarioList();
                this.showNotification('Senaryo yerel olarak güncellendi (sunucu bağlantısı yok)', 'warning');
                this.showTab('create');
            }
        }
    }

    getStepsFromContainer(containerId) {
        const stepInputs = document.querySelectorAll(`#${containerId} input`);
        const steps = [];
        stepInputs.forEach((input, index) => {
            if (input.value.trim()) {
                steps.push({
                    order: index + 1,
                    description: input.value.trim()
                });
            }
        });
        return steps;
    }

    parseTestDataFromField(fieldId) {
        const testDataText = document.getElementById(fieldId).value.trim();
        if (!testDataText) return {};
        
        try {
            return JSON.parse(testDataText);
        } catch (e) {
            this.showNotification('Test verileri geçerli JSON formatında değil!', 'warning');
            return {};
        }
    }

    cancelEdit() {
        this.showTab('create');
    }

    async deleteScenario(scenarioId) {
        if (confirm('Bu senaryoyu silmek istediğinizden emin misiniz?')) {
            try {
                const response = await fetch(`${this.apiBaseUrl}/${scenarioId}`, {
                    method: 'DELETE'
                });
                
                const result = await response.json();
                
                if (result.success) {
                    await this.loadScenariosFromStorage(); // Senaryoları yeniden yükle
                    this.selectedScenarios = this.selectedScenarios.filter(s => s.id !== scenarioId);
                    this.renderScenarioList();
                    this.updateSelectedScenariosDisplay();
                    this.showNotification('Senaryo başarıyla silindi!', 'success');
                } else {
                    throw new Error(result.error || 'Senaryo silinemedi');
                }
            } catch (error) {
                console.error('Senaryo silme hatası:', error);
                // Fallback: localStorage'dan sil
                this.scenarios = this.scenarios.filter(s => s.id !== scenarioId);
                this.selectedScenarios = this.selectedScenarios.filter(s => s.id !== scenarioId);
                this.saveScenariosToStorage();
                this.renderScenarioList();
                this.updateSelectedScenariosDisplay();
                this.showNotification('Senaryo yerel olarak silindi (sunucu bağlantısı yok)', 'warning');
            }
        }
    }

    // Step Management
    addStep() {
        this.addStepToContainer('stepsContainer');
    }

    addStepToContainer(containerId) {
        const container = document.getElementById(containerId);
        const stepCount = container.children.length + 1;
        
        const stepDiv = document.createElement('div');
        stepDiv.className = 'step-item';
        stepDiv.innerHTML = `
            <span class="step-number">${stepCount}</span>
            <input type="text" class="form-control" placeholder="Test adımını açıklayın..." style="flex: 1;">
            <button type="button" class="btn btn-danger" style="margin-left: 10px; padding: 8px 12px;" onclick="removeStep(this)">🗑️</button>
        `;
        
        container.appendChild(stepDiv);
    }

    updateStepNumbers(containerId) {
        const container = document.getElementById(containerId);
        const stepNumbers = container.querySelectorAll('.step-number');
        stepNumbers.forEach((numberSpan, index) => {
            numberSpan.textContent = index + 1;
        });
    }

    // Test Execution
    async startTestExecution() {
        if (this.selectedScenarios.length === 0) {
            this.showNotification('Lütfen çalıştırılacak senaryoları seçin!', 'warning');
            return;
        }

        try {
            const scenarioIds = this.selectedScenarios.map(s => s.id);
            const response = await fetch(`${this.apiBaseUrl}/execute`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ scenarioIds: scenarioIds })
            });
            
            const result = await response.json();
            
            if (result.success) {
                this.isTestRunning = true;
                this.currentTestIndex = 0;
                this.testResults = [];
                this.executionId = result.executionId;

                // UI Updates
                document.getElementById('startTestBtn').style.display = 'none';
                document.getElementById('stopTestBtn').style.display = 'inline-block';
                document.getElementById('testProgress').style.display = 'block';

                const testLog = document.getElementById('testLog');
                testLog.innerHTML = '';

                this.logMessage('🚀 Test çalıştırması başlatıldı...');
                this.logMessage(`📋 Toplam ${this.selectedScenarios.length} senaryo çalıştırılacak`);
                
                // Durum takibi başlat
                this.startStatusPolling();
            } else {
                throw new Error(result.error || 'Test başlatılamadı');
            }
        } catch (error) {
            console.error('Test başlatma hatası:', error);
            this.showNotification('Test başlatılırken hata oluştu, yerel simülasyon başlatılıyor...', 'warning');
            
            // Fallback: Yerel simülasyon
            this.isTestRunning = true;
            this.currentTestIndex = 0;
            this.testResults = [];

            // UI Updates
            document.getElementById('startTestBtn').style.display = 'none';
            document.getElementById('stopTestBtn').style.display = 'inline-block';
            document.getElementById('testProgress').style.display = 'block';

            const testLog = document.getElementById('testLog');
            testLog.innerHTML = '';

            this.logMessage('🚀 Test çalıştırması başlatıldı (yerel simülasyon)...');
            this.logMessage(`📋 Toplam ${this.selectedScenarios.length} senaryo çalıştırılacak`);

            // Execute scenarios locally
            for (let i = 0; i < this.selectedScenarios.length && this.isTestRunning; i++) {
                this.currentTestIndex = i;
                const scenario = this.selectedScenarios[i];
                
                this.updateProgress();
                this.logMessage(`\n🧪 Test başlatılıyor: ${scenario.name}`);
                
                const result = await this.executeScenario(scenario);
                this.testResults.push(result);
                
                if (result.status === 'success') {
                    this.logMessage(`✅ Test başarılı: ${scenario.name}`);
                } else {
                    this.logMessage(`❌ Test başarısız: ${scenario.name} - ${result.error}`);
                }
            }

            if (this.isTestRunning) {
                this.completeTestExecution();
            }
        }
    }

    async executeScenario(scenario) {
        const startTime = Date.now();
        
        try {
            // Simulate test execution
            this.logMessage(`  📝 Senaryo adımları çalıştırılıyor...`);
            
            for (let step of scenario.steps) {
                if (!this.isTestRunning) break;
                
                this.logMessage(`    ${step.order}. ${step.description}`);
                
                // Simulate step execution time
                await this.delay(Math.random() * 2000 + 1000);
                
                // Simulate random success/failure (90% success rate)
                if (Math.random() < 0.1) {
                    throw new Error(`Adım ${step.order} başarısız oldu`);
                }
            }

            // Take screenshot if enabled
            if (document.getElementById('takeScreenshots').checked) {
                this.logMessage(`  📸 Ekran görüntüsü alınıyor...`);
                await this.delay(500);
            }

            const endTime = Date.now();
            const duration = endTime - startTime;

            return {
                scenarioId: scenario.id,
                scenarioName: scenario.name,
                status: 'success',
                duration: duration,
                timestamp: new Date().toISOString(),
                steps: scenario.steps.length,
                screenshot: document.getElementById('takeScreenshots').checked ? `screenshot_${scenario.id}_${Date.now()}.png` : null
            };

        } catch (error) {
            const endTime = Date.now();
            const duration = endTime - startTime;

            return {
                scenarioId: scenario.id,
                scenarioName: scenario.name,
                status: 'failed',
                duration: duration,
                timestamp: new Date().toISOString(),
                error: error.message,
                steps: scenario.steps.length
            };
        }
    }

    updateProgress() {
        const progress = ((this.currentTestIndex + 1) / this.selectedScenarios.length) * 100;
        document.getElementById('progressFill').style.width = `${progress}%`;
        document.getElementById('currentTest').textContent = 
            `Test ${this.currentTestIndex + 1}/${this.selectedScenarios.length}: ${this.selectedScenarios[this.currentTestIndex].name}`;
    }

    completeTestExecution() {
        this.isTestRunning = false;
        
        // UI Updates
        document.getElementById('startTestBtn').style.display = 'inline-block';
        document.getElementById('stopTestBtn').style.display = 'none';
        document.getElementById('progressFill').style.width = '100%';
        document.getElementById('currentTest').textContent = 'Test çalıştırması tamamlandı!';

        this.logMessage('\n🎉 Tüm testler tamamlandı!');
        
        // Generate summary
        const successCount = this.testResults.filter(r => r.status === 'success').length;
        const failedCount = this.testResults.filter(r => r.status === 'failed').length;
        const totalDuration = this.testResults.reduce((sum, r) => sum + r.duration, 0);

        this.logMessage(`📊 Özet: ${successCount} başarılı, ${failedCount} başarısız`);
        this.logMessage(`⏱️ Toplam süre: ${(totalDuration / 1000).toFixed(2)} saniye`);

        // Update results tab
        this.updateResultsDisplay();
        
        // Test sonuçlarını kaydet
        this.saveTestResults();
        
        this.showNotification('Test çalıştırması tamamlandı!', 'success');
    }

    async stopTestExecution() {
        if (this.isTestRunning && this.executionId) {
            try {
                const response = await fetch(`${this.apiBaseUrl}/execution/${this.executionId}/stop`, {
                    method: 'POST'
                });
                
                const result = await response.json();
                
                if (result.success) {
                    this.isTestRunning = false;
                    this.logMessage('\n⏹️ Test çalıştırması durduruldu');
                    
                    document.getElementById('startTestBtn').style.display = 'inline-block';
                    document.getElementById('stopTestBtn').style.display = 'none';
                    
                    // Durum takibini durdur
                    if (this.statusPollingInterval) {
                        clearInterval(this.statusPollingInterval);
                    }
                    
                    this.showNotification('Test çalıştırması durduruldu!', 'warning');
                } else {
                    throw new Error(result.error || 'Test durdurulamadı');
                }
            } catch (error) {
                console.error('Test durdurma hatası:', error);
                // Fallback: Yerel durdurma
                this.isTestRunning = false;
                this.logMessage('\n⏹️ Test çalıştırması durduruldu (yerel)');
                
                document.getElementById('startTestBtn').style.display = 'inline-block';
                document.getElementById('stopTestBtn').style.display = 'none';
                
                this.showNotification('Test çalıştırması yerel olarak durduruldu', 'warning');
            }
        } else {
            this.isTestRunning = false;
            this.logMessage('\n⏹️ Test çalıştırması durduruldu');
            
            document.getElementById('startTestBtn').style.display = 'inline-block';
            document.getElementById('stopTestBtn').style.display = 'none';
            
            this.showNotification('Test çalıştırması durduruldu', 'info');
        }
    }

    updateResultsDisplay() {
        const summaryContainer = document.getElementById('testSummary');
        const detailsContainer = document.getElementById('detailedResults');

        if (this.testResults.length === 0) {
            summaryContainer.innerHTML = '<p style="color: #6c757d;">Henüz test çalıştırılmadı</p>';
            detailsContainer.innerHTML = '';
            return;
        }

        const successCount = this.testResults.filter(r => r.status === 'success').length;
        const failedCount = this.testResults.filter(r => r.status === 'failed').length;
        const totalDuration = this.testResults.reduce((sum, r) => sum + r.duration, 0);
        const successRate = ((successCount / this.testResults.length) * 100).toFixed(1);

        summaryContainer.innerHTML = `
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; margin-bottom: 20px;">
                <div style="background: #d4edda; padding: 15px; border-radius: 8px; text-align: center;">
                    <div style="font-size: 24px; font-weight: bold; color: #155724;">${successCount}</div>
                    <div style="color: #155724;">Başarılı Test</div>
                </div>
                <div style="background: #f8d7da; padding: 15px; border-radius: 8px; text-align: center;">
                    <div style="font-size: 24px; font-weight: bold; color: #721c24;">${failedCount}</div>
                    <div style="color: #721c24;">Başarısız Test</div>
                </div>
                <div style="background: #fff3cd; padding: 15px; border-radius: 8px; text-align: center;">
                    <div style="font-size: 24px; font-weight: bold; color: #856404;">${successRate}%</div>
                    <div style="color: #856404;">Başarı Oranı</div>
                </div>
                <div style="background: #d1ecf1; padding: 15px; border-radius: 8px; text-align: center;">
                    <div style="font-size: 24px; font-weight: bold; color: #0c5460;">${(totalDuration / 1000).toFixed(1)}s</div>
                    <div style="color: #0c5460;">Toplam Süre</div>
                </div>
            </div>
        `;

        detailsContainer.innerHTML = `
            <h4>Detaylı Sonuçlar</h4>
            ${this.testResults.map(result => `
                <div class="result-item">
                    <div>
                        <strong>${result.scenarioName}</strong><br>
                        <small>${result.steps} adım | ${(result.duration / 1000).toFixed(2)}s</small>
                    </div>
                    <span class="status-badge status-${result.status}">
                        ${result.status === 'success' ? '✅ Başarılı' : '❌ Başarısız'}
                    </span>
                </div>
            `).join('')}
        `;
    }

    // Utility Functions
    // Durum takibi başlatma
    startStatusPolling() {
        this.statusPollingInterval = setInterval(async () => {
            if (!this.isTestRunning || !this.executionId) {
                clearInterval(this.statusPollingInterval);
                return;
            }
            
            try {
                const response = await fetch(`${this.apiBaseUrl}/execution/${this.executionId}/status`);
                const result = await response.json();
                
                if (result.success) {
                    const status = result.data;
                    
                    // Progress güncelle
                    if (status.progress !== undefined) {
                        this.currentTestIndex = Math.floor((status.progress / 100) * this.selectedScenarios.length);
                        this.updateProgress();
                    }
                    
                    // Log mesajları güncelle
                    if (status.logs && status.logs.length > 0) {
                        status.logs.forEach(log => {
                            this.logMessage(log.message);
                        });
                    }
                    
                    // Test tamamlandı mı kontrol et
                    if (status.status === 'completed' || status.status === 'failed') {
                        this.isTestRunning = false;
                        clearInterval(this.statusPollingInterval);
                        
                        // Sonuçları yükle
                        await this.loadTestResults(this.executionId);
                        this.completeTestExecution();
                    }
                }
            } catch (error) {
                console.error('Durum takip hatası:', error);
            }
        }, 2000); // Her 2 saniyede bir kontrol et
    }
    
    // Test sonuçlarını yükleme
    async loadTestResults(executionId) {
        try {
            const response = await fetch(`${this.apiBaseUrl}/execution/${executionId}/results`);
            const result = await response.json();
            
            if (result.success && result.data) {
                this.testResults = result.data.results || [];
                
                // Sonuçları localStorage'a da kaydet
                const allResults = JSON.parse(localStorage.getItem('testResults') || '[]');
                allResults.push({
                    id: executionId,
                    timestamp: new Date().toISOString(),
                    results: this.testResults,
                    summary: result.data.summary
                });
                localStorage.setItem('testResults', JSON.stringify(allResults));
            }
        } catch (error) {
            console.error('Test sonuçları yükleme hatası:', error);
        }
    }

    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    logMessage(message) {
        const testLog = document.getElementById('testLog');
        testLog.innerHTML += message + '\n';
        testLog.scrollTop = testLog.scrollHeight;
    }

    // Filter and Search
    filterScenarios() {
        const searchTerm = document.getElementById('searchScenarios').value.toLowerCase();
        const categoryFilter = document.getElementById('filterCategory').value;

        const filteredScenarios = this.scenarios.filter(scenario => {
            const matchesSearch = scenario.name.toLowerCase().includes(searchTerm) ||
                                scenario.description.toLowerCase().includes(searchTerm);
            const matchesCategory = !categoryFilter || scenario.category === categoryFilter;
            
            return matchesSearch && matchesCategory;
        });

        this.renderFilteredScenarios(filteredScenarios);
    }

    renderFilteredScenarios(scenarios) {
        const container = document.getElementById('scenarioList');
        container.innerHTML = '';

        if (scenarios.length === 0) {
            container.innerHTML = '<p style="text-align: center; color: #6c757d; margin-top: 20px;">Arama kriterlerine uygun senaryo bulunamadı</p>';
            return;
        }

        scenarios.forEach(scenario => {
            const scenarioElement = this.createScenarioElement(scenario);
            container.appendChild(scenarioElement);
        });

        this.updateScenarioSelection();
    }

    // Results Management
    async saveTestResults() {
        try {
            const newResult = {
                id: this.executionId || this.generateId(),
                timestamp: new Date().toISOString(),
                scenarios: this.selectedScenarios.map(s => s.name),
                results: this.testResults,
                summary: this.generateTestSummary()
            };
            
            // Backend'e kaydet
            const response = await fetch(`${this.apiBaseUrl}/results`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(newResult)
            });
            
            const result = await response.json();
            
            if (result.success) {
                this.showNotification('Test sonuçları kaydedildi!', 'success');
            } else {
                throw new Error(result.error || 'Sonuçlar kaydedilemedi');
            }
        } catch (error) {
            console.error('Sonuç kaydetme hatası:', error);
            
            // Fallback: localStorage'a kaydet
            const results = JSON.parse(localStorage.getItem('testResults') || '[]');
            const newResult = {
                id: this.executionId || this.generateId(),
                timestamp: new Date().toISOString(),
                scenarios: this.selectedScenarios.map(s => s.name),
                results: this.testResults,
                summary: this.generateTestSummary()
            };
            
            results.unshift(newResult);
            localStorage.setItem('testResults', JSON.stringify(results));
            
            this.showNotification('Test sonuçları yerel olarak kaydedildi', 'warning');
        }
        
        this.renderTestResults();
    }

    generateTestSummary() {
        const successCount = this.testResults.filter(r => r.status === 'success').length;
        const failedCount = this.testResults.filter(r => r.status === 'failed').length;
        const totalDuration = this.testResults.reduce((sum, r) => sum + r.duration, 0);
        
        return {
            total: this.testResults.length,
            success: successCount,
            failed: failedCount,
            successRate: ((successCount / this.testResults.length) * 100).toFixed(1),
            duration: totalDuration
        };
    }

    async renderTestResults() {
        const resultsContainer = document.getElementById('testResultsList');
        if (!resultsContainer) return;
        
        let results = [];
        
        try {
            // Backend'den sonuçları yükle
            const response = await fetch(`${this.apiBaseUrl}/results`);
            const result = await response.json();
            
            if (result.success) {
                results = result.data || [];
            } else {
                throw new Error('Backend sonuçları yüklenemedi');
            }
        } catch (error) {
            console.error('Sonuç yükleme hatası:', error);
            // Fallback: localStorage'dan yükle
            results = JSON.parse(localStorage.getItem('testResults') || '[]');
        }
        
        if (results.length === 0) {
            resultsContainer.innerHTML = '<p style="text-align: center; color: #6c757d; margin-top: 20px;">Henüz test sonucu bulunmuyor</p>';
            return;
        }

        resultsContainer.innerHTML = results.map(result => `
            <div style="border: 1px solid #e9ecef; border-radius: 8px; padding: 15px; margin-bottom: 15px;">
                <div style="display: flex; justify-content: between; align-items: center; margin-bottom: 10px;">
                    <h4 style="margin: 0;">${new Date(result.timestamp).toLocaleString('tr-TR')}</h4>
                    <span style="background: ${result.summary.failed > 0 ? '#f8d7da' : '#d4edda'}; color: ${result.summary.failed > 0 ? '#721c24' : '#155724'}; padding: 4px 8px; border-radius: 4px; font-size: 12px;">
                        ${result.summary.success}/${result.summary.total} Başarılı
                    </span>
                </div>
                <p style="color: #6c757d; margin: 5px 0;">Senaryolar: ${result.scenarios.join(', ')}</p>
                <p style="color: #6c757d; margin: 5px 0;">Süre: ${(result.summary.duration / 1000).toFixed(2)} saniye</p>
                <button class="btn btn-secondary" style="padding: 4px 8px; font-size: 12px;" onclick="testManager.viewResultDetails('${result.id}')">Detayları Görüntüle</button>
            </div>
        `).join('');
    }

    generateId() {
        return Date.now().toString() + Math.random().toString(36).substr(2, 9);
    }

    // Storage Management
    saveScenariosToStorage() {
        localStorage.setItem('testScenarios', JSON.stringify(this.scenarios));
    }

    async loadScenariosFromStorage() {
        try {
            const response = await fetch(`${this.apiBaseUrl}`);
            const result = await response.json();
            
            if (result.success) {
                this.scenarios = result.data || [];
            } else {
                console.error('Senaryolar yüklenemedi:', result.error);
                this.scenarios = [];
            }
        } catch (error) {
            console.error('Senaryo yükleme hatası:', error);
            // Fallback: localStorage'dan yükle
            const stored = localStorage.getItem('testScenarios');
            if (stored) {
                try {
                    this.scenarios = JSON.parse(stored);
                } catch (e) {
                    console.error('Error loading scenarios from storage:', e);
                    this.scenarios = [];
                }
            } else {
                this.scenarios = [];
            }
        }
    }

    // Import/Export
    exportScenarios() {
        const dataStr = JSON.stringify(this.scenarios, null, 2);
        const dataBlob = new Blob([dataStr], {type: 'application/json'});
        
        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = `test-scenarios-${new Date().toISOString().split('T')[0]}.json`;
        link.click();
        
        this.showNotification('Senaryolar dışa aktarıldı', 'success');
    }

    importScenarios(event) {
        const file = event.target.files[0];
        if (!file) return;

        const reader = new FileReader();
        reader.onload = (e) => {
            try {
                const importedScenarios = JSON.parse(e.target.result);
                
                if (Array.isArray(importedScenarios)) {
                    this.scenarios = [...this.scenarios, ...importedScenarios];
                    this.saveScenariosToStorage();
                    this.renderScenarioList();
                    this.showNotification(`${importedScenarios.length} senaryo içe aktarıldı`, 'success');
                } else {
                    this.showNotification('Geçersiz dosya formatı', 'error');
                }
            } catch (error) {
                this.showNotification('Dosya okuma hatası', 'error');
            }
        };
        reader.readAsText(file);
    }

    exportResults() {
        if (this.testResults.length === 0) {
            this.showNotification('Dışa aktarılacak test sonucu bulunamadı', 'warning');
            return;
        }

        const reportData = {
            timestamp: new Date().toISOString(),
            summary: {
                total: this.testResults.length,
                success: this.testResults.filter(r => r.status === 'success').length,
                failed: this.testResults.filter(r => r.status === 'failed').length,
                duration: this.testResults.reduce((sum, r) => sum + r.duration, 0)
            },
            results: this.testResults
        };

        const dataStr = JSON.stringify(reportData, null, 2);
        const dataBlob = new Blob([dataStr], {type: 'application/json'});
        
        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = `test-results-${new Date().toISOString().split('T')[0]}.json`;
        link.click();
        
        this.showNotification('Test sonuçları dışa aktarıldı', 'success');
    }

    // UI Helpers
    clearForm() {
        document.getElementById('scenarioForm').reset();
        document.getElementById('stepsContainer').innerHTML = `
            <div class="step-item">
                <span class="step-number">1</span>
                <input type="text" class="form-control" placeholder="Test adımını açıklayın..." style="flex: 1;">
                <button type="button" class="btn btn-danger" style="margin-left: 10px; padding: 8px 12px;" onclick="removeStep(this)">🗑️</button>
            </div>
        `;
    }

    showNotification(message, type = 'info') {
        // Create notification element
        const notification = document.createElement('div');
        notification.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px 20px;
            border-radius: 8px;
            color: white;
            font-weight: 600;
            z-index: 10000;
            max-width: 300px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            transition: all 0.3s ease;
        `;

        const colors = {
            'success': '#28a745',
            'error': '#dc3545',
            'warning': '#ffc107',
            'info': '#17a2b8'
        };

        notification.style.backgroundColor = colors[type] || colors.info;
        notification.textContent = message;

        document.body.appendChild(notification);

        // Auto remove after 3 seconds
        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateX(100%)';
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }, 3000);
    }
}

// Global Functions
function showTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Remove active class from all nav tabs
    document.querySelectorAll('.nav-tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Show selected tab
    document.getElementById(tabName + 'Tab').classList.add('active');
    
    // Add active class to clicked nav tab
    event.target.classList.add('active');
}

function removeStep(button) {
    const container = button.closest('#stepsContainer, #editStepsContainer');
    button.parentElement.remove();
    testManager.updateStepNumbers(container.id);
}

function runSelectedScenarios() {
    if (testManager.selectedScenarios.length === 0) {
        testManager.showNotification('Lütfen çalıştırılacak senaryoları seçin!', 'warning');
        return;
    }
    
    showTab('run');
    testManager.updateSelectedScenariosDisplay();
}

function viewScreenshots() {
    const modal = document.getElementById('screenshotModal');
    const gallery = document.getElementById('screenshotGallery');
    
    // Get screenshots from test results
    const screenshots = testManager.testResults
        .filter(result => result.screenshot)
        .map(result => ({
            name: result.scenarioName,
            file: result.screenshot,
            timestamp: result.timestamp
        }));
    
    if (screenshots.length === 0) {
        gallery.innerHTML = '<p style="text-align: center; color: #6c757d;">Henüz ekran görüntüsü bulunmuyor</p>';
    } else {
        gallery.innerHTML = screenshots.map(screenshot => `
            <div style="margin-bottom: 20px; border: 1px solid #e9ecef; border-radius: 8px; padding: 15px;">
                <h4>${screenshot.name}</h4>
                <p style="color: #6c757d; font-size: 14px;">${new Date(screenshot.timestamp).toLocaleString('tr-TR')}</p>
                <div style="background: #f8f9fa; padding: 20px; text-align: center; border-radius: 6px;">
                    📸 ${screenshot.file}
                    <br><small style="color: #6c757d;">Ekran görüntüsü dosyası</small>
                </div>
            </div>
        `).join('');
    }
    
    modal.style.display = 'block';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Initialize the application
let testManager;
document.addEventListener('DOMContentLoaded', () => {
    testManager = new TestScenarioManager();
});

// Close modal when clicking outside
window.onclick = function(event) {
    const modals = document.querySelectorAll('.modal');
    modals.forEach(modal => {
        if (event.target === modal) {
            modal.style.display = 'none';
        }
    });
}