# Azure Deployment Guide - Message Service

This guide covers deploying the modernized Message Service application (Spring Boot 3.x / JDK 17) to Azure App Service using Azure Developer CLI (azd).

## 📋 Prerequisites

### Required Tools

1. **Azure CLI** (version 2.50.0 or higher)
   ```bash
   # Install Azure CLI
   # Windows (PowerShell)
   winget install Microsoft.AzureCLI
   
   # macOS
   brew install azure-cli
   
   # Linux
   curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash
   
   # Verify installation
   az --version
   ```

2. **Azure Developer CLI (azd)** (version 1.5.0 or higher)
   ```bash
   # Install azd
   # Windows (PowerShell)
   winget install Microsoft.Azd
   
   # macOS
   brew tap azure/azd && brew install azd
   
   # Linux
   curl -fsSL https://aka.ms/install-azd.sh | bash
   
   # Verify installation
   azd version
   ```

3. **JDK 17** (for local builds)
   ```bash
   # Verify Java version
   java -version
   # Should show Java 17 or higher
   ```

4. **Maven 3.6+**
   ```bash
   # Verify Maven version
   mvn -version
   ```

### Azure Account Requirements

- Active Azure subscription
- Permissions to create:
  - Resource Groups
  - App Service Plans
  - App Services
  - Log Analytics Workspaces (optional, for Application Insights)
  - Application Insights (optional, for monitoring)

## 🚀 Deployment Steps

### Step 1: Login to Azure

```bash
# Login to Azure
az login

# Set your subscription (if you have multiple)
az account list --output table
az account set --subscription "YOUR_SUBSCRIPTION_ID"

# Verify
az account show
```

### Step 2: Initialize Azure Developer CLI

```bash
# Navigate to project directory
cd /path/to/message-service

# Initialize azd (first time only)
azd init

# You'll be prompted for:
# - Environment name (e.g., "dev", "staging", "prod")
# This will create .azure/<environment-name> directory
```

**Note:** If you've already initialized, you can skip this step.

### Step 3: Configure Environment (Optional)

You can customize deployment settings in `.azure/<environment-name>/.env`:

```bash
# Edit environment variables
# Optional customizations:

# Change App Service Plan SKU (default: B1)
# Options: B1, B2, B3, S1, S2, S3, P1v2, P2v2, P3v2
APP_SERVICE_PLAN_SKU=B1

# Enable/Disable Application Insights (default: true)
ENABLE_APP_INSIGHTS=true

# Set Azure region (default: eastus)
AZURE_LOCATION=eastus
```

### Step 4: Provision and Deploy

```bash
# Provision Azure resources AND deploy application
azd up

# This will:
# 1. Build your application (mvn clean package)
# 2. Provision Azure resources (Resource Group, App Service Plan, App Service, etc.)
# 3. Deploy the application JAR to App Service
# 4. Configure application settings
# 5. Display the application URL
```

**Alternative: Separate Commands**

```bash
# Provision resources only (without deploying)
azd provision

# Deploy application only (after provisioning)
azd deploy

# Monitor deployment
azd monitor
```

### Step 5: Verify Deployment

After deployment completes, you'll see output like:

```
SUCCESS: Your application was provisioned and deployed to Azure in X minutes Y seconds.

You can view the resources created under the resource group rg-<environment-name> in Azure Portal:
https://portal.azure.com/#@/resource/subscriptions/.../resourceGroups/rg-<environment-name>

API Base URL: https://app-<unique-id>.azurewebsites.net
```

**Test the deployed application:**

```bash
# Get the application URL
azd env get-values | grep API_BASE_URL

# Test health endpoint
curl https://app-<unique-id>.azurewebsites.net/api/messages

# Or open in browser
open https://app-<unique-id>.azurewebsites.net/api/messages
```

## 🧪 Testing the Deployed Application

### 1. Test REST API Endpoints

```bash
# Set your API base URL
API_URL="https://app-<unique-id>.azurewebsites.net"

# Get all messages
curl $API_URL/api/messages

# Create a new message
curl -X POST $API_URL/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"Deployed to Azure!","author":"azd"}'

# Get message by ID
curl $API_URL/api/messages/1

# Update a message
curl -X PUT $API_URL/api/messages/1 \
  -H "Content-Type: application/json" \
  -d '{"content":"Updated message content"}'

# Search messages
curl "$API_URL/api/messages/search?keyword=Azure"

# Get messages by author
curl $API_URL/api/messages/author/admin

# Delete a message
curl -X DELETE $API_URL/api/messages/1
```

### 2. Verify Scheduled Task

```bash
# View App Service logs to confirm scheduled task runs every 60 seconds
az webapp log tail --name <app-service-name> --resource-group <resource-group-name>

# You should see output every minute like:
# ========================================
# Message Statistics Task - Executing
# ========================================
# Execution Time: 2025-11-19 18:30:00
# Total Messages: 5
# Active Messages: 5
# ...
```

### 3. Access Application Insights (if enabled)

```bash
# Get Application Insights connection string
azd env get-values | grep APPLICATIONINSIGHTS_CONNECTION_STRING

# View in Azure Portal
# Navigate to: Resource Group → Application Insights → Logs/Metrics
```

## 📊 Monitoring and Logs

### View Application Logs

```bash
# Stream logs in real-time
az webapp log tail \
  --name <app-service-name> \
  --resource-group <resource-group-name>

# Download logs
az webapp log download \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --log-file app-logs.zip
```

### Application Insights Queries

If Application Insights is enabled, you can query logs:

1. Go to Azure Portal → Application Insights
2. Click on "Logs" in the left menu
3. Run queries:

```kusto
// View all traces
traces
| where timestamp > ago(1h)
| order by timestamp desc

// View scheduled task executions
traces
| where message contains "Message Statistics Task"
| order by timestamp desc

// View API requests
requests
| where timestamp > ago(1h)
| project timestamp, name, resultCode, duration
| order by timestamp desc

// Check for errors
exceptions
| where timestamp > ago(24h)
| order by timestamp desc
```

### Health Check Endpoint

The App Service is configured with a health check at `/api/messages`:

```bash
# Test health check
curl https://app-<unique-id>.azurewebsites.net/api/messages
```

## 🔧 Configuration Management

### Application Settings

View and update application settings:

```bash
# List all app settings
az webapp config appsettings list \
  --name <app-service-name> \
  --resource-group <resource-group-name>

# Set a new app setting
az webapp config appsettings set \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --settings KEY=VALUE

# Example: Change Spring profile
az webapp config appsettings set \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --settings SPRING_PROFILES_ACTIVE=prod
```

### Environment Variables

Current default settings:

| Setting | Value | Description |
|---------|-------|-------------|
| `JAVA_OPTS` | `-Dserver.port=8080 -Xmx512m` | Java runtime options |
| `PORT` | `8080` | Application port |
| `SPRING_PROFILES_ACTIVE` | `prod` | Spring profile |
| `APPLICATIONINSIGHTS_CONNECTION_STRING` | (auto-generated) | Application Insights connection |

## 🔄 Update Deployment

To deploy updates after code changes:

```bash
# Build and deploy
azd deploy

# Or full provision + deploy (if infrastructure changed)
azd up
```

## 🗑️ Cleanup Resources

To delete all Azure resources:

```bash
# Delete all resources for the environment
azd down

# This will:
# 1. Prompt for confirmation
# 2. Delete the Resource Group and all contained resources
# 3. Clean up local environment files (optional)
```

**Warning:** This will permanently delete all resources and data.

## 💰 Cost Estimation

### App Service Plan Pricing (Pay-as-you-go)

| Tier | Specs | Monthly Cost (Est.) | Use Case |
|------|-------|---------------------|----------|
| B1 (Basic) | 1 Core, 1.75 GB RAM | ~$13-15/month | Development, Testing |
| B2 (Basic) | 2 Cores, 3.5 GB RAM | ~$26-30/month | Small Production |
| B3 (Basic) | 4 Cores, 7 GB RAM | ~$52-60/month | Medium Production |
| S1 (Standard) | 1 Core, 1.75 GB RAM | ~$70-75/month | Production with staging slots |
| P1v2 (Premium) | 1 Core, 3.5 GB RAM | ~$100-110/month | Enterprise Production |

### Additional Costs

- **Application Insights**: ~$2-5/month (5GB free tier)
- **Log Analytics**: ~$2-5/month (5GB free tier)
- **Data Transfer**: Minimal for typical usage

**Total Estimated Monthly Cost:**
- **Development:** $15-20/month (B1 + monitoring)
- **Production:** $75-115/month (S1 or P1v2 + monitoring)

**Notes:**
- Prices vary by region
- Free tier available for Application Insights and Log Analytics
- Check [Azure Pricing Calculator](https://azure.microsoft.com/pricing/calculator/) for exact pricing

## 🛡️ Security Best Practices

### 1. Enable HTTPS Only

```bash
az webapp update \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --https-only true
```

### 2. Set Minimum TLS Version

```bash
az webapp config set \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --min-tls-version 1.2
```

### 3. Enable Managed Identity (Optional)

```bash
az webapp identity assign \
  --name <app-service-name> \
  --resource-group <resource-group-name>
```

### 4. Configure Firewall Rules (Optional)

Restrict access to specific IP addresses:

```bash
az webapp config access-restriction add \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --rule-name "AllowMyIP" \
  --action Allow \
  --ip-address YOUR_IP_ADDRESS/32 \
  --priority 100
```

## 🔍 Troubleshooting

### Issue: Application won't start

**Check logs:**
```bash
az webapp log tail --name <app-service-name> --resource-group <resource-group-name>
```

**Common causes:**
- JDK version mismatch (ensure Java 17)
- Missing application settings
- Port configuration issues

### Issue: Scheduled task not running

**Verify:**
1. Check that `@EnableScheduling` is present in Application.java
2. View logs to confirm task execution
3. Ensure `alwaysOn` is enabled (requires Basic tier or higher)

```bash
az webapp config set \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --always-on true
```

### Issue: High memory usage

**Solution:** Adjust `JAVA_OPTS`:

```bash
az webapp config appsettings set \
  --name <app-service-name> \
  --resource-group <resource-group-name> \
  --settings JAVA_OPTS="-Dserver.port=8080 -Xmx1024m"
```

### Issue: Slow performance

**Solutions:**
1. Scale up to a higher tier (B2, B3, or S1)
2. Enable Application Insights for performance diagnostics
3. Check database connection pooling settings

## 📚 Additional Resources

- [Azure App Service Documentation](https://docs.microsoft.com/azure/app-service/)
- [Azure Developer CLI Documentation](https://learn.microsoft.com/azure/developer/azure-developer-cli/)
- [Spring Boot on Azure](https://learn.microsoft.com/azure/developer/java/spring-framework/)
- [Application Insights for Java](https://learn.microsoft.com/azure/azure-monitor/app/java-in-process-agent)

## 🎯 Next Steps

1. ✅ Deploy to Azure using `azd up`
2. ✅ Test all API endpoints
3. ✅ Verify scheduled task execution
4. ✅ Set up monitoring and alerts
5. ✅ Configure custom domain (optional)
6. ✅ Set up CI/CD with GitHub Actions (optional)

---

**Need Help?** Check the [Azure App Service troubleshooting guide](https://docs.microsoft.com/azure/app-service/troubleshoot) or open an issue in the repository.
