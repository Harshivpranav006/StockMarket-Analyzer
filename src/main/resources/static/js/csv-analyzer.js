document.addEventListener('DOMContentLoaded', function() {
    const uploadForm = document.getElementById('uploadForm');
    const modelsList = document.getElementById('modelsList');

    // Load existing models
    loadModels();

    // Handle file upload
    uploadForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const fileInput = document.getElementById('csvFile');
        const file = fileInput.files[0];
        
        if (!file) {
            alert('Please select a CSV file');
            return;
        }

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await fetch('/api/csv/upload', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const model = await response.json();
                updateModelDetails(model);
                loadModels();
                fileInput.value = '';
            } else {
                alert('Error uploading file');
            }
        } catch (error) {
            console.error('Error:', error);
            alert('Error uploading file');
        }
    });

    async function loadModels() {
        try {
            const response = await fetch('/api/csv/models');
            if (response.ok) {
                const models = await response.json();
                displayModels(models);
            }
        } catch (error) {
            console.error('Error loading models:', error);
        }
    }

    function displayModels(models) {
        modelsList.innerHTML = '';
        models.forEach(model => {
            const modelItem = document.createElement('div');
            modelItem.className = 'list-group-item d-flex justify-content-between align-items-center';
            modelItem.innerHTML = `
                <div>
                    <h5 class="mb-1">${model.fileName}</h5>
                    <small>Type: ${model.modelType} | Accuracy: ${(model.accuracy * 100).toFixed(2)}%</small>
                </div>
                <div>
                    <button class="btn btn-sm btn-info me-2" onclick="viewModel('${model.fileName}')">View</button>
                    <button class="btn btn-sm btn-danger" onclick="deleteModel('${model.fileName}')">Delete</button>
                </div>
            `;
            modelsList.appendChild(modelItem);
        });
    }

    function updateModelDetails(model) {
        document.getElementById('fileName').textContent = model.fileName;
        document.getElementById('columns').textContent = model.metadata.columns;
        document.getElementById('rows').textContent = model.metadata.rows;
        document.getElementById('modelType').textContent = model.modelType;
        document.getElementById('accuracy').textContent = (model.accuracy * 100).toFixed(2) + '%';

        // Update predictions
        const predictionsDiv = document.getElementById('predictions');
        predictionsDiv.innerHTML = '';
        for (const [key, value] of Object.entries(model.predictions)) {
            const predictionItem = document.createElement('div');
            predictionItem.className = 'mb-2';
            predictionItem.innerHTML = `
                <strong>${key}:</strong> ${(value * 100).toFixed(2)}%
                <div class="progress">
                    <div class="progress-bar" role="progressbar" style="width: ${value * 100}%"></div>
                </div>
            `;
            predictionsDiv.appendChild(predictionItem);
        }
    }
});

async function viewModel(fileName) {
    try {
        const response = await fetch(`/api/csv/models/${fileName}`);
        if (response.ok) {
            const model = await response.json();
            updateModelDetails(model);
        }
    } catch (error) {
        console.error('Error loading model:', error);
    }
}

async function deleteModel(fileName) {
    if (confirm('Are you sure you want to delete this model?')) {
        try {
            const response = await fetch(`/api/csv/models/${fileName}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                loadModels();
                document.getElementById('modelDetails').innerHTML = '<p>No model selected</p>';
                document.getElementById('predictions').innerHTML = '';
            }
        } catch (error) {
            console.error('Error deleting model:', error);
        }
    }
} 