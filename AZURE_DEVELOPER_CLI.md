# Azure Developer CLI (azd) Deployment Guide

This guide explains how to deploy the Message Service Spring Boot application to Azure App Service using the Azure Developer CLI (`azd`).

## Prerequisites

1. **Azure Developer CLI**: Install from https://aka.ms/azd-install
2. **Azure CLI**: Install from https://docs.microsoft.com/cli/azure/install-azure-cli
3. **Java 17**: JDK 17 installed locally
4. **Maven**: Maven 3.6+ installed
5. **Azure Subscription**: Active Azure subscription with appropriate permissions

## Quick Start

### 1. Initialize Azure Developer CLI

```bash
# Login to Azure
azd auth login

# Initialize the environment (first time only)
azd init
# Or if already initialized, you can skip this step
```

When prompted:
- **Environment name**: Choose a name (e.g., `message-service-dev`)
- **Location**: Choose an Azure region (e.g., `eastus`, `westus2`)

### 2. Deploy to Azure

```bash
# Build, provision infrastructure, and deploy in one command
azd up
```

This single command will:
1. Build the Spring Boot application using Maven
2. Create Azure resources (App Service Plan, App Service, Application Insights)
3. Deploy the JAR file to Azure App Service
4. Configure the application settings

### 3. Access Your Application

After deployment completes, `azd` will display the application URL:

```
Deploying services (azd deploy)
  (✓) Done: Deploying service api
  
  App Service: https://app-xxxxxxxxx.azurewebsites.net
```

Test the endpoints:
```bash
# Get the app URL (stored in azd environment)
APP_URL=$(azd env get-values | grep WEBSITE_URL | cut -d'=' -f2 | tr -d '"')

# Test the API
curl $APP_URL/api/messages

# Create a message
curl -X POST $APP_URL/api/messages \
  -H "Content-Type: application/json" \
  -d '{"content":"Hello from Azure!","author":"azd user"}'
```

## Available Commands

### Deploy Application Only
```bash
# Deploy code changes without re-provisioning infrastructure
azd deploy
```

### Provision Infrastructure Only
```bash
# Create/update Azure resources without deploying code
azd provision
```

### View Environment Variables
```bash
# Show all environment variables and outputs
azd env get-values
```

### View Logs
```bash
# Stream application logs
azd monitor

# Or use Azure CLI for more control
az webapp log tail --name $(azd env get-values | grep APP_SERVICE_NAME | cut -d'=' -f2 | tr -d '"') \
  --resource-group rg-$(azd env get-values | grep AZURE_ENV_NAME | cut -d'=' -f2 | tr -d '"')
```

### Clean Up Resources
```bash
# Delete all Azure resources
azd down
```

## Application Configuration

### Scheduled Task Verification

The scheduled task runs every 60 seconds. To verify it's working:

1. Check the logs in Azure Portal:
   - Navigate to your App Service
   - Go to "Log stream" under Monitoring
   - Look for "Message Statistics Task - Executing" every minute

2. Or use Azure CLI:
```bash
az webapp log tail --name <app-service-name> --resource-group <resource-group-name>
```

You should see output like:
```
========================================
Message Statistics Task - Executing
========================================
Execution Time: 2025-11-17 22:30:00
Total Messages: 5
...
```

### Important App Settings

The deployment automatically configures:

- **WEBSITES_PORT**: `8080` (tells Azure the app listens on port 8080)
- **SERVER_PORT**: `8080` (Spring Boot server port)
- **Always On**: Enabled (ensures scheduled tasks continue to run)
- **Health Check**: `/api/messages` (monitors application health)
- **Application Insights**: Automatically integrated for monitoring

### Scaling Configuration

The default deployment uses a **Basic B1** App Service Plan, which is suitable for development/testing.

To scale for production:

```bash
# Scale up to a higher tier
az appservice plan update \
  --name <plan-name> \
  --resource-group <resource-group-name> \
  --sku P1V2

# Scale out with more instances
az appservice plan update \
  --name <plan-name> \
  --resource-group <resource-group-name> \
  --number-of-workers 2
```

## Project Structure

```
.
├── azure.yaml                      # Main azd configuration
├── infra/
│   ├── main.bicep                  # Main infrastructure template
│   ├── main.parameters.json        # Deployment parameters
│   └── resources.bicep             # Resource definitions
├── .azure/                         # azd environment data (git-ignored)
├── pom.xml                         # Maven build configuration
└── src/                            # Application source code
```

## Infrastructure Details

The deployment creates these Azure resources:

1. **Resource Group**: `rg-{environmentName}`
   - Container for all resources

2. **App Service Plan**: Linux-based, Basic B1 tier
   - Java 17 runtime support
   - Always-on enabled for scheduled tasks

3. **App Service**: Linux web app
   - Java 17 (JAVA|17-java17)
   - Health monitoring enabled
   - HTTPS only

4. **Application Insights**: Application monitoring
   - Integrated with App Service
   - Monitors performance and errors
   - Tracks scheduled task execution

5. **Log Analytics Workspace**: Centralized logging
   - 30-day retention
   - Supports Application Insights

## Customization

### Change Azure Region

Edit `infra/main.parameters.json` or use environment variables:

```bash
azd env set AZURE_LOCATION westus2
azd provision
```

### Modify App Service Plan Tier

Edit `infra/resources.bicep`, line ~44:

```bicep
sku: {
  name: 'P1V2'  // Change to Premium tier
  tier: 'PremiumV2'
  capacity: 1
}
```

### Add Environment Variables

Edit `infra/resources.bicep`, add to `appSettings` array:

```bicep
{
  name: 'MY_CUSTOM_VARIABLE'
  value: 'my-value'
}
```

## Troubleshooting

### Deployment Fails

1. Check your Azure permissions
2. Verify the region supports App Service Plan
3. Review error messages from `azd up`

### Scheduled Task Not Running

1. Verify "Always On" is enabled in App Service configuration
2. Check application logs for errors
3. Confirm the task executes locally: `mvn clean spring-boot:run`

### Application Not Accessible

1. Check the App Service is running:
```bash
az webapp show --name <app-service-name> --resource-group <resource-group-name> --query state
```

2. Verify health check endpoint responds:
```bash
curl https://<app-url>/api/messages
```

3. Check Application Insights for errors

### Build Fails

1. Ensure Java 17 is installed: `java -version`
2. Ensure Maven is installed: `mvn -version`
3. Try building locally first: `mvn clean package`

## Cost Estimation

Basic B1 App Service Plan costs approximately:
- **~$13-15 USD/month** (730 hours)
- Includes:
  - 1 vCPU
  - 1.75 GB RAM
  - 10 GB storage
  - Unlimited bandwidth (within tier limits)

Application Insights and Log Analytics have minimal costs for this workload (typically <$5/month).

## Additional Resources

- [Azure Developer CLI Documentation](https://learn.microsoft.com/azure/developer/azure-developer-cli/)
- [Azure App Service Documentation](https://docs.microsoft.com/azure/app-service/)
- [Spring Boot on Azure](https://docs.microsoft.com/azure/developer/java/spring-framework/)
- [Bicep Documentation](https://docs.microsoft.com/azure/azure-resource-manager/bicep/)

## Support

For issues or questions:
- Review [azd documentation](https://learn.microsoft.com/azure/developer/azure-developer-cli/)
- Check [Azure App Service documentation](https://docs.microsoft.com/azure/app-service/)
- View application logs: `azd monitor`
