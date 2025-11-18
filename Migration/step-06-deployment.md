# Step 6: Azure Deployment

**Duration**: 45 minutes

## 🎯 Objectives

- Deploy the migrated application to Azure
- Configure Azure Container Apps using modern Infrastructure as Code
- Verify functionality in the cloud
- Set up monitoring and logging
- Understand Azure deployment patterns (azd, Bicep, and manual approaches)

## 📋 Prerequisites

- [ ] Completed Step 5: Local Testing
- [ ] Azure subscription (free tier works)
- [ ] Application tested locally
- [ ] Docker image builds successfully
- [ ] Azure Developer CLI (azd) installed (instructions below if needed)

## ⚡ Quick Start (Copy & Paste Commands)

**For demo participants** who want to get started immediately with a modern Infrastructure as Code approach:

> 💡 **Tip**: These commands use Azure Developer CLI (azd) and let GitHub Copilot Coding Agent generate all the Bicep infrastructure files for you!

### 1. Install azd (if not already installed)
```powershell
# Windows
winget install microsoft.azd

# macOS
brew tap azure/azd && brew install azd

# Linux
curl -fsSL https://aka.ms/install-azd.sh | bash
```

### 2. Let GitHub Copilot Generate Infrastructure

**🤖 Let the GitHub Copilot Coding Agent create all Bicep files and deployment scripts for you!**

**Copy this exact prompt into GitHub Copilot Chat** (Ctrl+Shift+I or Cmd+Shift+I):
```
@workspace Create Azure infrastructure using Bicep for deploying this Spring Boot application to Azure Container Apps. Include:

1. Create an `infra` folder with Bicep templates
2. main.bicep - Main infrastructure template with:
   - Azure Container Registry for storing Docker images
   - Azure Container Apps Environment
   - Azure Container App with:
     - External ingress enabled on port 8080
     - 1 replica (for H2 in-memory database consistency)
     - 1.0 vCPU and 2.0 GiB memory (Java requirement)
     - Environment variables for Spring Boot
   - Log Analytics workspace for monitoring
3. main.parameters.json - Parameter file with default values
4. azure.yaml - Azure Developer CLI configuration file specifying:
   - Project name: message-service
   - Service configuration with Docker build context
   - Target port: 8080
5. All resources should use consistent naming with proper prefixes
6. Include comments explaining each resource

After generating, test with: azd up
```

**What GitHub Copilot will create for you:**
- ✅ `infra/` folder with complete Bicep templates
- ✅ `azure.yaml` configuration file  
- ✅ All necessary parameters and configurations
- ✅ Production-ready infrastructure code

**No manual Bicep coding required!** Just copy the prompt above and let Copilot do the work.

### 3. Deploy to Azure
```powershell
# Login to Azure
azd auth login

# Deploy everything (infrastructure + app)
azd up

# Follow the prompts:
# - Environment name: dev
# - Azure subscription: <select yours>
# - Azure location: eastus

# Wait 5-10 minutes for deployment to complete
```

### 4. Test Your Deployment
```powershell
# Get your app URL
azd show

# Test API
curl https://your-app-url/api/messages
```

### 5. Cleanup When Done
```powershell
# Delete all Azure resources
azd down
```

## 🏗️ Deployment Architecture

**Target**: Azure Container Apps

```
┌─────────────────────────────────────┐
│   Azure Container Registry (ACR)   │
│   (Stores Docker Image)             │
└────────────┬────────────────────────┘
             │ docker push
             ↓
┌─────────────────────────────────────┐
│  Azure Container Apps Environment   │
│  ┌───────────────────────────────┐  │
│  │  Message Service Container    │  │
│  │  ┌─────────────────────────┐  │  │
│  │  │  REST API (Port 8080)   │  │  │
│  │  │  Scheduled Task (1 min) │  │  │
│  │  │  H2 Database (Memory)   │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
│         ↑                            │
│         │ HTTPS                      │
└─────────┴─────────────────────── ────┘
          │
          ↓
     Internet Users
```

## 🚀 Recommended Approach: Azure Developer CLI (azd) with Bicep

Azure Developer CLI (azd) is the modern, streamlined approach to deploying applications to Azure. It uses Infrastructure as Code (Bicep) and automates the entire deployment process.

### Why azd + Bicep?

✅ **Infrastructure as Code**: All resources defined in Bicep templates  
✅ **Reproducible**: Deploy identical environments every time  
✅ **Version Control**: Track infrastructure changes in Git  
✅ **GitHub Copilot Integration**: Let Copilot generate Bicep templates for you  
✅ **One Command Deploy**: `azd up` handles everything  
✅ **Best Practice**: Modern Azure deployment standard  

### Step 6.1: Install Azure Developer CLI (azd)

**Check if already installed**:
```powershell
# Check azd version
azd version
```

**If not installed**:

**Windows (PowerShell)**:
```powershell
# Using winget (recommended)
winget install microsoft.azd

# Or using PowerShell script
powershell -ex AllSigned -c "Invoke-RestMethod 'https://aka.ms/install-azd.ps1' | Invoke-Expression"
```

**macOS**:
```bash
# Using Homebrew
brew tap azure/azd && brew install azd
```

**Linux**:
```bash
# Using script
curl -fsSL https://aka.ms/install-azd.sh | bash
```

**Verify installation**:
```powershell
azd version
# Should show version 1.0.0 or higher
```

### Step 6.2: Let GitHub Copilot Generate Your Infrastructure

Rather than manually creating Bicep files, let GitHub Copilot Coding Agent generate them for you!

**Option A: Use GitHub Copilot in VS Code**

Open GitHub Copilot Chat (Ctrl+Shift+I or Cmd+Shift+I) and paste:

```
@workspace Create Azure infrastructure using Bicep for deploying this Spring Boot application to Azure Container Apps. Include:

1. Create an `infra` folder with Bicep templates
2. main.bicep - Main infrastructure template with:
   - Azure Container Registry for storing Docker images
   - Azure Container Apps Environment
   - Azure Container App with:
     - External ingress enabled on port 8080
     - 1 replica (for H2 in-memory database consistency)
     - 1.0 vCPU and 2.0 GiB memory (Java requirement)
     - Environment variables for Spring Boot
   - Log Analytics workspace for monitoring
3. main.parameters.json - Parameter file with default values
4. azure.yaml - Azure Developer CLI configuration file specifying:
   - Project name: message-service
   - Service configuration with Docker build context
   - Target port: 8080
5. All resources should use consistent naming with proper prefixes
6. Include comments explaining each resource

After generating, test with: azd up
```

**Option B: Use GitHub Copilot CLI Agent**

If you have GitHub Copilot installed in your terminal:

```bash
# Let Copilot generate the infrastructure
gh copilot suggest "Create Bicep infrastructure for Azure Container Apps deployment with ACR, Container Apps Environment, and Container App for a Spring Boot application on port 8080"
```

**Option C: Manual Bicep Creation (If Copilot is unavailable)**

Create the infrastructure manually (see "Alternative: Manual Deployment" section below).

### Step 6.3: Login to Azure with azd

```powershell
# Login to Azure
azd auth login

# Browser will open for authentication
# Select your account and subscription
```

**Verify login**:
```powershell
# Check authentication status
azd auth login --check-status
```

### Step 6.4: Initialize Your Project (If Starting Fresh)

If Copilot hasn't created the azure.yaml file yet:

```powershell
# Navigate to project root
cd /home/runner/work/JavaMigrationGitHubCopilot/JavaMigrationGitHubCopilot

# Initialize azd project
azd init

# Follow prompts:
# - Environment name: dev (or prod, test, etc.)
# - Select: Use code in the current directory
# - Detect services: Yes
# - Select service: Java
```

This creates:
- `azure.yaml` - Project configuration
- `.azure/` folder - Environment settings

### Step 6.5: Deploy to Azure with One Command! 🎉

```powershell
# Deploy everything with one command
azd up

# This will:
# ✅ Create Azure resources from Bicep templates
# ✅ Build Docker image
# ✅ Push image to Azure Container Registry
# ✅ Deploy to Azure Container Apps
# ✅ Configure networking and ingress
# ✅ Display application URL

# Deployment takes 5-10 minutes
```

**What `azd up` does**:
1. **Provision** (`azd provision`) - Creates Azure resources from Bicep
2. **Package** - Builds Docker image
3. **Deploy** (`azd deploy`) - Pushes image and deploys to Container Apps

**Prompts you'll see**:
```
? Please enter a new environment name: dev
? Please select an Azure Subscription to use: <your subscription>
? Please select an Azure location to use: eastus

Provisioning Azure resources...
Creating resource group...
Creating Container Registry...
Creating Container Apps Environment...
Creating Container App...

Packaging services...
Building Docker image...

Deploying services...
Pushing image to ACR...
Updating Container App...

SUCCESS: Your application is running at: https://app-message-service-xxx.azurecontainerapps.io
```

### Step 6.6: Get Your Application URL

```powershell
# Show all deployed endpoints
azd show

# Get specific endpoint
azd show --output json | jq -r '.services.app.endpoint'
```

**Test your deployment**:
```powershell
# Set the URL (replace with your actual URL from azd output)
$APP_URL = "https://your-app-url.azurecontainerapps.io"

# Test API
Invoke-WebRequest -Uri "$APP_URL/api/messages" -UseBasicParsing
```

### Step 6.7: View Deployment Details

```powershell
# Show all resources created
azd show

# View environment variables
azd env get-values

# List all environments
azd env list

# View logs
azd logs
```

## 📝 Sample Bicep Templates Reference

If GitHub Copilot is unavailable or you want to create Bicep templates manually, here's what the infrastructure should look like:

### Folder Structure
```
.
├── azure.yaml                      # azd configuration
├── infra/
│   ├── main.bicep                  # Main infrastructure template
│   ├── main.parameters.json        # Parameters file
│   └── modules/
│       ├── containerRegistry.bicep # ACR module (optional)
│       └── containerApp.bicep      # Container App module (optional)
```

### Sample azure.yaml
```yaml
name: message-service
services:
  app:
    project: .
    language: java
    host: containerapp
    docker:
      path: Dockerfile
      context: .
    port: 8080
```

### Sample main.bicep (Simplified)
```bicep
// Parameters
param location string = resourceGroup().location
param environmentName string = 'dev'
param appName string = 'message-service'

// Variables
var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))
var acrName = 'acr${resourceToken}'
var containerAppName = 'app-${appName}'
var containerAppEnvName = 'env-${appName}'

// Container Registry
resource containerRegistry 'Microsoft.ContainerRegistry/registries@2023-01-01-preview' = {
  name: acrName
  location: location
  sku: {
    name: 'Basic'
  }
  properties: {
    adminUserEnabled: true
  }
}

// Container Apps Environment
resource containerAppEnvironment 'Microsoft.App/managedEnvironments@2023-05-01' = {
  name: containerAppEnvName
  location: location
  properties: {}
}

// Container App
resource containerApp 'Microsoft.App/containerApps@2023-05-01' = {
  name: containerAppName
  location: location
  properties: {
    managedEnvironmentId: containerAppEnvironment.id
    configuration: {
      ingress: {
        external: true
        targetPort: 8080
      }
      registries: [
        {
          server: containerRegistry.properties.loginServer
          username: containerRegistry.listCredentials().username
          passwordSecretRef: 'registry-password'
        }
      ]
      secrets: [
        {
          name: 'registry-password'
          value: containerRegistry.listCredentials().passwords[0].value
        }
      ]
    }
    template: {
      containers: [
        {
          name: appName
          image: '${containerRegistry.properties.loginServer}/${appName}:latest'
          resources: {
            cpu: json('1.0')
            memory: '2.0Gi'
          }
        }
      ]
      scale: {
        minReplicas: 1
        maxReplicas: 1
      }
    }
  }
}

// Outputs
output containerAppFQDN string = containerApp.properties.configuration.ingress.fqdn
output acrLoginServer string = containerRegistry.properties.loginServer
```

**Note**: This is a simplified example. For production, use separate modules, add Log Analytics, use managed identities instead of admin credentials, and parameterize more values.

### Creating These Files Manually

If you need to create these files yourself:

```powershell
# Create directory structure
mkdir infra
cd infra

# Create files (use your editor to add content above)
# main.bicep
# main.parameters.json

# Return to project root
cd ..

# Create azure.yaml
# (add content above)

# Deploy
azd up
```

## 🔧 Alternative: Manual Azure CLI Installation & Setup

If you prefer the traditional approach or need more control:

### Install Azure CLI (if needed)

**Check if already installed**:
```powershell
az --version
```

**If not installed**:
```powershell
# Download and install Azure CLI
# Visit: https://aka.ms/installazurecliwindows
# Or use winget:
winget install Microsoft.AzureCLI
```

### Login to Azure

```powershell
# Login to Azure
az login

# Browser will open for authentication
# Select your account and subscription
```

**Verify login**:
```powershell
# List subscriptions
az account list --output table

# Set default subscription (if multiple)
az account set --subscription "<Subscription Name or ID>"
```

### Install Container Apps Extension

```powershell
# Add the containerapp extension
az extension add --name containerapp --upgrade

# Register providers
az provider register --namespace Microsoft.App
az provider register --namespace Microsoft.OperationalInsights
```

## 🧪 Testing Your azd Deployment

### Test 1: API Endpoints

```powershell
# Get your app URL from azd
$APP_URL = (azd show --output json | ConvertFrom-Json).services.app.endpoint

# Get all messages
$response = Invoke-RestMethod -Uri "$APP_URL/api/messages" -Method Get
$response | ConvertTo-Json

# Create message
$body = @{
    content = "Deployed with Azure Developer CLI!"
    author = "azd-user"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "$APP_URL/api/messages" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"

$response | ConvertTo-Json
```

### Test 2: View Logs

```powershell
# Stream logs in real-time
azd logs --follow

# View recent logs
azd logs

# Press Ctrl+C to stop following
```

### Test 3: Verify Scheduled Task

Look for output every 60 seconds in logs:
```
Message Statistics Task - Executing
```

## 🔄 Updating Your Application with azd

After making code changes:

```powershell
# Option 1: Full redeployment (safest)
azd up

# Option 2: Deploy only (if infrastructure unchanged)
azd deploy

# Option 3: Deploy specific service
azd deploy app-message-service
```

**Quick update workflow**:
```powershell
# 1. Make code changes
# Edit your Java files...

# 2. Build locally (optional, to verify)
mvn clean package -DskipTests

# 3. Deploy new version
azd deploy

# 4. Test
$APP_URL = (azd show --output json | ConvertFrom-Json).services.app.endpoint
Invoke-WebRequest -Uri "$APP_URL/api/messages" -UseBasicParsing
```

## 🧹 Cleanup Resources with azd

```powershell
# Delete all Azure resources
azd down

# This removes:
# ✅ Container App
# ✅ Container Apps Environment
# ✅ Container Registry
# ✅ Log Analytics Workspace
# ✅ Resource Group
# ✅ All associated resources

# Keeps local configuration in .azure/ folder
# To also remove local config: azd down --purge
```

## 💡 azd Tips and Tricks

### Multiple Environments

```powershell
# Create different environments (dev, test, prod)
azd env new dev
azd env new test
azd env new prod

# Switch between environments
azd env select dev
azd up

azd env select prod
azd up

# Each environment has separate Azure resources
```

### View Resource Costs

```powershell
# Show deployed resources
azd show

# View in Azure Portal to see costs
# Or use Azure Cost Management
```

### CI/CD with GitHub Actions

```powershell
# Generate GitHub Actions workflow
azd pipeline config

# This creates .github/workflows/azure-dev.yml
# Commit and push to enable automated deployments
```

## 📦 Alternative: Manual Deployment (Step-by-Step)

If you prefer manual control or need to understand the underlying Azure resources, follow these steps:

### Step 6.8: Create Resource Group

```powershell
# Set variables
$RESOURCE_GROUP="rg-message-service-workshop"
$LOCATION="eastus"

# Create resource group
az group create `
  --name $RESOURCE_GROUP `
  --location $LOCATION

# Expected output: JSON with provisioningState: "Succeeded"
```

### Step 6.9: Create Container Registry

```powershell
# Set ACR name (must be globally unique, lowercase, no hyphens)
$ACR_NAME="acrmessageservice$((Get-Random -Maximum 9999))"

# Create Azure Container Registry
az acr create `
  --resource-group $RESOURCE_GROUP `
  --name $ACR_NAME `
  --sku Basic `
  --admin-enabled true

# Get ACR login server
$ACR_SERVER=$(az acr show --name $ACR_NAME --query loginServer --output tsv)
Write-Host "ACR Server: $ACR_SERVER"

# Expected: <acrname>.azurecr.io
```

### Step 6.10: Build and Push Docker Image

**Option A: Build Locally and Push**

```powershell
# Build JAR file
mvn clean package -DskipTests

# Login to ACR
az acr login --name $ACR_NAME

# Build image with ACR tag
$IMAGE_NAME="$ACR_SERVER/message-service:v1"
docker build -t $IMAGE_NAME .

# Push to ACR
docker push $IMAGE_NAME

# Verify image in ACR
az acr repository list --name $ACR_NAME --output table
```

**Option B: Build in Azure (Recommended for workshops)**

```powershell
# ACR can build the image for you
az acr build `
  --registry $ACR_NAME `
  --image message-service:v1 `
  --file Dockerfile `
  .

# Faster, no local Docker push needed
```

### Step 6.11: Get ACR Credentials

```powershell
# Get username
$ACR_USERNAME=$(az acr credential show --name $ACR_NAME --query username --output tsv)

# Get password
$ACR_PASSWORD=$(az acr credential show --name $ACR_NAME --query "passwords[0].value" --output tsv)

# Store for next steps
Write-Host "ACR Username: $ACR_USERNAME"
Write-Host "ACR Password: [hidden]"
```

### Step 6.12: Create Container Apps Environment

```powershell
# Set environment name
$ENVIRONMENT="env-message-service"

# Create environment (this takes 3-5 minutes)
az containerapp env create `
  --name $ENVIRONMENT `
  --resource-group $RESOURCE_GROUP `
  --location $LOCATION

# Wait for completion
# Expected: provisioningState: "Succeeded"
```

### Step 6.13: Deploy Container App

```powershell
# Set app name
$APP_NAME="app-message-service"

# Deploy the application
az containerapp create `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --environment $ENVIRONMENT `
  --image "$ACR_SERVER/message-service:v1" `
  --registry-server $ACR_SERVER `
  --registry-username $ACR_USERNAME `
  --registry-password $ACR_PASSWORD `
  --target-port 8080 `
  --ingress external `
  --min-replicas 1 `
  --max-replicas 1 `
  --cpu 1.0 `
  --memory 2.0Gi

# Deployment takes 2-4 minutes
```

**Parameters Explained**:
- `--target-port 8080`: Application listens on 8080
- `--ingress external`: Accessible from internet
- `--min-replicas 1`: Always run 1 instance (for scheduled task)
- `--max-replicas 1`: Don't scale (H2 in-memory limitation)
- `--cpu 1.0`: 1 vCPU
- `--memory 2.0Gi`: 2 GB RAM (Java needs more memory)

### Step 6.14: Get Application URL

```powershell
# Get the FQDN
$APP_URL=$(az containerapp show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --query properties.configuration.ingress.fqdn `
  --output tsv)

Write-Host "Application URL: https://$APP_URL"

# Test endpoint
Invoke-WebRequest -Uri "https://$APP_URL/api/messages" -UseBasicParsing
```

## 🧪 Testing Manual Deployment

### Test 1: API Endpoints

```powershell
# Get all messages
$response = Invoke-RestMethod -Uri "https://$APP_URL/api/messages" -Method Get
$response | ConvertTo-Json

# Create message
$body = @{
    content = "Deployed to Azure Container Apps!"
    author = "azure-user"
} | ConvertTo-Json

$response = Invoke-RestMethod `
  -Uri "https://$APP_URL/api/messages" `
  -Method Post `
  -Body $body `
  -ContentType "application/json"

$response | ConvertTo-Json
```

### Test 2: Verify Scheduled Task

```powershell
# View logs to see scheduled task
az containerapp logs show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --follow

# Look for output every 60 seconds:
# "Message Statistics Task - Executing"
# Press Ctrl+C to stop following
```

**⚠️ Verify**: Task should execute every 60 seconds consistently

### Test 3: H2 Console Access

**Note**: H2 console may not be accessible externally for security reasons.

To enable (development/workshop only):
```powershell
# Update environment variables
az containerapp update `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --set-env-vars "SPRING_H2_CONSOLE_SETTINGS_WEB-ALLOW-OTHERS=true"

# Access H2 console
# https://$APP_URL/h2-console
```

**Production**: Use Azure SQL Database or external PostgreSQL instead of H2.

## 📊 Monitoring and Logging

### View Application Logs

```powershell
# Stream logs in real-time
az containerapp logs show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --follow

# Get recent logs (last 100 lines)
az containerapp logs show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --tail 100
```

### View Container App Metrics

```powershell
# Get app details including replica status
az containerapp show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --output table

# Check revision status
az containerapp revision list `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --output table
```

### Set Up Log Analytics (Optional)

Container Apps automatically sends logs to Log Analytics.

**Access in Azure Portal**:
1. Go to Azure Portal
2. Navigate to your Container App
3. Click **Logs** in left menu
4. Run queries:

```kusto
// View all logs from last hour
ContainerAppConsoleLogs_CL
| where TimeGenerated > ago(1h)
| project TimeGenerated, Log_s
| order by TimeGenerated desc

// Find scheduled task executions
ContainerAppConsoleLogs_CL
| where Log_s contains "Message Statistics Task"
| project TimeGenerated, Log_s
| order by TimeGenerated desc
```

## 🔄 Updating the Application

### Update Application Code

After making changes:

```powershell
# Rebuild JAR
mvn clean package -DskipTests

# Build and push new image version
az acr build `
  --registry $ACR_NAME `
  --image message-service:v2 `
  --file Dockerfile `
  .

# Update container app
az containerapp update `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --image "$ACR_SERVER/message-service:v2"

# Container will restart with new version
```

### Check Deployment Status

```powershell
# Watch revision provisioning
az containerapp revision list `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --output table

# Old revision will be deactivated
# New revision should show "Running"
```

## 💰 Cost Management

### View Current Costs

```powershell
# Estimated monthly cost for this setup:
# - Container Apps: $0-50/month (mostly idle)
# - Container Registry: $5/month (Basic tier)
# - Total: ~$5-55/month
```

**Cost Optimization**:
- Use Azure Free Trial credits
- Stop container when not in use:
  ```powershell
  az containerapp update --name $APP_NAME --resource-group $RESOURCE_GROUP --min-replicas 0
  ```
- Delete resources after workshop

### Delete Resources

```powershell
# Delete entire resource group (removes everything)
az group delete --name $RESOURCE_GROUP --yes --no-wait

# This will delete:
# - Container App
# - Container Apps Environment
# - Container Registry
# - All associated resources
```

## 🔐 Security Best Practices

### Use Managed Identity (Production)

Instead of admin credentials:

```powershell
# Enable managed identity
az containerapp identity assign `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --system-assigned

# Grant ACR pull permissions to managed identity
$PRINCIPAL_ID=$(az containerapp show --name $APP_NAME --resource-group $RESOURCE_GROUP --query identity.principalId --output tsv)

az role assignment create `
  --assignee $PRINCIPAL_ID `
  --scope $(az acr show --name $ACR_NAME --query id --output tsv) `
  --role AcrPull
```

### Enable HTTPS Only

```powershell
# Container Apps uses HTTPS by default
# Verify:
az containerapp ingress show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP
```

### Add Custom Domain (Optional)

```powershell
# Add custom domain
az containerapp hostname add `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --hostname "api.yourdomain.com"

# Requires DNS configuration
```

## 🎯 Production Considerations

### Replace H2 with Persistent Database

For production, use Azure Database for PostgreSQL:

```powershell
# Create PostgreSQL server
az postgres flexible-server create `
  --resource-group $RESOURCE_GROUP `
  --name "psql-message-service" `
  --admin-user myadmin `
  --admin-password "MyPassword123!" `
  --sku-name Standard_B1ms

# Update application.properties:
# spring.datasource.url=jdbc:postgresql://psql-message-service.postgres.database.azure.com:5432/messagedb
# spring.datasource.username=myadmin
# spring.datasource.password=MyPassword123!
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
```

### Enable Scaling (With External DB)

```powershell
# Allow scaling when using external database
az containerapp update `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --min-replicas 1 `
  --max-replicas 10 `
  --scale-rule-name "http-scale" `
  --scale-rule-type "http" `
  --scale-rule-http-concurrency 50
```

**Note**: Scheduled tasks need special handling with multiple replicas (use Azure Functions or leader election)

### Set Up CI/CD with GitHub Actions

Create `.github/workflows/deploy.yml`:

```yaml
name: Deploy to Azure Container Apps

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Build with Maven
        run: mvn clean package -DskipTests
      
      - name: Login to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}
      
      - name: Build and push image
        run: |
          az acr build \
            --registry ${{ secrets.ACR_NAME }} \
            --image message-service:${{ github.sha }} \
            --file Dockerfile \
            .
      
      - name: Deploy to Container Apps
        run: |
          az containerapp update \
            --name app-message-service \
            --resource-group rg-message-service-workshop \
            --image ${{ secrets.ACR_NAME }}.azurecr.io/message-service:${{ github.sha }}
```

## ✅ Deployment Checklist

Verify successful deployment:

### Using azd (Recommended)
- [ ] Azure Developer CLI (azd) installed
- [ ] Logged in with `azd auth login`
- [ ] Infrastructure defined in Bicep templates (or generated by Copilot)
- [ ] Successfully ran `azd up`
- [ ] Application URL obtained from `azd show`
- [ ] All API endpoints accessible via HTTPS
- [ ] Logs accessible via `azd logs`

### Infrastructure (All Deployment Methods)
- [ ] Resource group created
- [ ] Container Registry created and accessible
- [ ] Docker image pushed to ACR
- [ ] Container Apps Environment created
- [ ] Container App deployed and running

### Application
- [ ] Application starts without errors
- [ ] ⚠️ Scheduled task runs every 60 seconds
- [ ] All API endpoints accessible via HTTPS
- [ ] Database (H2) initializes correctly
- [ ] Sample data loads successfully

### Monitoring
- [ ] Logs accessible (via azd logs or Azure CLI)
- [ ] Log Analytics showing data
- [ ] No error logs or exceptions
- [ ] Application metrics available

### Testing
- [ ] Can create messages via API
- [ ] Can read messages via API
- [ ] Can update messages via API
- [ ] Can delete messages via API
- [ ] Search functionality works
- [ ] Scheduled task visible in logs

## 📊 Deployment Approach Comparison

### Which Deployment Method Should You Use?

| Approach | Best For | Pros | Cons |
|----------|----------|------|------|
| **azd + Bicep** (Recommended) | Production, Teams, IaC | ✅ Infrastructure as Code<br>✅ One-command deploy<br>✅ Version control<br>✅ Copilot integration<br>✅ Repeatable | ⚠️ Learning curve for Bicep |
| **Manual Azure CLI** | Learning, Understanding | ✅ Step-by-step control<br>✅ Learn Azure services<br>✅ Quick prototyping | ❌ Not reproducible<br>❌ Error-prone<br>❌ Hard to maintain |
| **Azure Portal** | First time users | ✅ Visual interface<br>✅ No CLI needed | ❌ Slowest<br>❌ Not automatable<br>❌ Hard to repeat |

**Our Recommendation**: Start with **azd + Bicep** and let GitHub Copilot generate the infrastructure for you. This gives you the benefits of Infrastructure as Code without the complexity of learning Bicep syntax.

## 🎓 Alternative Deployment Options

### Option 1: Azure App Service (Traditional)

```powershell
# Create App Service Plan
az appservice plan create `
  --name "plan-message-service" `
  --resource-group $RESOURCE_GROUP `
  --sku B1 `
  --is-linux

# Create Web App
az webapp create `
  --name "app-message-service-webapp" `
  --resource-group $RESOURCE_GROUP `
  --plan "plan-message-service" `
  --runtime "JAVA:17-java17"

# Deploy JAR
az webapp deploy `
  --resource-group $RESOURCE_GROUP `
  --name "app-message-service-webapp" `
  --src-path target/message-service-1.0.0.jar `
  --type jar
```

**Pros**: Traditional, familiar, easy deployment  
**Cons**: More expensive, less cloud-native

### Option 2: Azure Functions (Scheduled Task Only)

For the scheduled task separately:

```powershell
# Create Function App
az functionapp create `
  --name "func-message-stats" `
  --resource-group $RESOURCE_GROUP `
  --consumption-plan-location $LOCATION `
  --runtime java `
  --runtime-version 17 `
  --functions-version 4
```

**Pros**: Perfect for scheduled tasks, cost-effective  
**Cons**: Need to migrate API separately, serverless constraints

## 🛠️ Troubleshooting

### azd-Specific Issues

#### Issue: "azd: command not found"

**Solution**: Install Azure Developer CLI
```powershell
# Windows
winget install microsoft.azd

# macOS
brew tap azure/azd && brew install azd

# Linux
curl -fsSL https://aka.ms/install-azd.sh | bash

# Verify
azd version
```

#### Issue: "No azd infrastructure found"

**Solution**: Initialize azd or generate Bicep templates
```powershell
# Option 1: Initialize azd
azd init

# Option 2: Use GitHub Copilot to generate infrastructure
# See "Step 6.2: Let GitHub Copilot Generate Your Infrastructure"
```

#### Issue: `azd up` fails with provisioning errors

**Solution**: Check Bicep templates and parameters
```powershell
# View detailed logs
azd up --debug

# Common issues:
# - Invalid resource names (must be globally unique)
# - Invalid location
# - Subscription permissions

# Fix and retry
azd up
```

#### Issue: Cannot push Docker image to ACR

**Solution**: Ensure ACR exists and you have permissions
```powershell
# Check if ACR was created
az acr list --output table

# Login to ACR
az acr login --name <your-acr-name>

# Retry deployment
azd deploy
```

### General Issues

#### Issue: Container App Won't Start

```powershell
# Check logs for errors
az containerapp logs show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP

# Common issues:
# - Image pull errors (check ACR credentials)
# - Application startup failures (check JAR is in image)
# - Port misconfiguration (should be 8080)
```

### Issue: Can't Access Application URL

```powershell
# Verify ingress is external
az containerapp ingress show `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP

# Should show: "external": true
```

### Issue: Scheduled Task Not Running

Check logs for:
- @EnableScheduling in Application class
- No exceptions during task execution
- Task not being killed mid-execution

### Issue: High Memory Usage

```powershell
# Increase memory allocation
az containerapp update `
  --name $APP_NAME `
  --resource-group $RESOURCE_GROUP `
  --memory 3.0Gi
```

## ✅ Checklist - Step 6 Complete

Workshop completion checklist:

### Recommended Path (azd)
- [ ] Azure Developer CLI (azd) installed
- [ ] Logged in with `azd auth login`
- [ ] Infrastructure created (Bicep templates or Copilot-generated)
- [ ] Deployed with `azd up`
- [ ] Application accessible via HTTPS
- [ ] Know how to update with `azd deploy`
- [ ] Understand cleanup with `azd down`

### Traditional Path (Manual Azure CLI)
- [ ] Azure CLI installed and configured
- [ ] Logged into Azure subscription
- [ ] Resource group created
- [ ] Container Registry created
- [ ] Docker image built and pushed to ACR
- [ ] Container Apps Environment created
- [ ] Application deployed to Container Apps

### Verification (All Paths)
- [ ] Application accessible via HTTPS
- [ ] All API endpoints tested and working
- [ ] Scheduled task verified running every 60 seconds
- [ ] Logs accessible and readable
- [ ] Understand deployment process
- [ ] Know how to update and redeploy
- [ ] Aware of costs and cleanup process

## 🎓 Key Takeaways

1. **Modern Deployment** - Azure Developer CLI (azd) + Bicep is the recommended approach
2. **Infrastructure as Code** - Bicep templates make deployments reproducible and version-controlled
3. **GitHub Copilot Integration** - Let Copilot generate Bicep infrastructure for you
4. **One Command Deploy** - `azd up` simplifies the entire deployment process
5. **Cloud-Native** - Containers are the modern standard for cloud deployment
6. **Azure Container Apps** - Great balance of simplicity and power
7. **Monitoring Matters** - Always verify apps work in cloud
8. **Cost Awareness** - Clean up resources with `azd down` when done
9. **Security First** - Use managed identities, HTTPS, private registries
10. **Multiple Approaches** - Choose between azd (recommended), manual CLI, or other options

## 🏆 Congratulations!

You've successfully:
✅ Migrated a legacy Java 1.8 / Spring 4.x application  
✅ Modernized to JDK 17 / Spring Boot 3.x  
✅ Containerized with Docker  
✅ Deployed to Azure Container Apps  
✅ Learned to use GitHub Copilot as an autonomous team member  

## 📚 Next Steps

### Continue Learning:
1. **Add Azure SQL Database** - Replace H2 with persistent storage
2. **Implement CI/CD** - Automate deployment with GitHub Actions
3. **Add API Authentication** - Secure with Azure AD or API keys
4. **Enable Monitoring** - Set up Application Insights
5. **Try Azure Spring Apps** - Purpose-built for Spring Boot
6. **Explore AKS** - Kubernetes for complex microservices

### Apply to Your Projects:
- Use this migration pattern for your legacy applications
- Leverage GitHub Copilot for code modernization
- Adopt Azure Container Apps for new cloud deployments
- Implement CI/CD pipelines for automation

## 🤝 Sharing Your Success

Share your experience:
- Blog about your migration journey
- Present to your team
- Contribute improvements to this workshop
- Help others migrate their legacy applications

---

## 🎉 Workshop Complete!

Thank you for completing the Java Application Migration Workshop!

**Questions or Issues?** 
- Review workshop documentation
- Check Azure documentation
- Ask GitHub Copilot for help!

**Happy Coding! 🚀**
