# Azure Deployment Guide

This guide provides instructions for deploying the Message Service to Azure Container Apps.

## Prerequisites

- Azure CLI installed (`az --version`)
- Docker installed
- Azure subscription with appropriate permissions
- Azure Container Registry (ACR) or Docker Hub account

## Deployment Steps

### 1. Build and Test Locally

```bash
# Build the application
mvn clean package

# Build Docker image
docker build -t message-service:latest .

# Test locally
docker run -p 8080:8080 message-service:latest

# Test endpoints
curl http://localhost:8080/api/messages
```

### 2. Push Image to Azure Container Registry

```bash
# Login to Azure
az login

# Create resource group
az group create --name message-service-rg --location eastus

# Create Azure Container Registry
az acr create \
  --resource-group message-service-rg \
  --name messageserviceacr \
  --sku Basic \
  --admin-enabled true

# Login to ACR
az acr login --name messageserviceacr

# Tag and push image
docker tag message-service:latest messageserviceacr.azurecr.io/message-service:latest
docker push messageserviceacr.azurecr.io/message-service:latest
```

### 3. Deploy to Azure Container Apps

```bash
# Create Container Apps environment
az containerapp env create \
  --name message-service-env \
  --resource-group message-service-rg \
  --location eastus

# Get ACR credentials
ACR_USERNAME=$(az acr credential show --name messageserviceacr --query username -o tsv)
ACR_PASSWORD=$(az acr credential show --name messageserviceacr --query passwords[0].value -o tsv)

# Deploy the container app
az containerapp create \
  --name message-service-app \
  --resource-group message-service-rg \
  --environment message-service-env \
  --image messageserviceacr.azurecr.io/message-service:latest \
  --target-port 8080 \
  --ingress external \
  --registry-server messageserviceacr.azurecr.io \
  --registry-username $ACR_USERNAME \
  --registry-password $ACR_PASSWORD \
  --cpu 0.5 \
  --memory 1.0Gi \
  --min-replicas 1 \
  --max-replicas 3

# Get the application URL
az containerapp show \
  --name message-service-app \
  --resource-group message-service-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv
```

### 4. Verify Deployment

```bash
# Get the app URL
APP_URL=$(az containerapp show \
  --name message-service-app \
  --resource-group message-service-rg \
  --query properties.configuration.ingress.fqdn \
  --output tsv)

# Test endpoints
curl https://$APP_URL/api/messages
curl https://$APP_URL/api/messages/stats

# Check logs
az containerapp logs show \
  --name message-service-app \
  --resource-group message-service-rg \
  --follow
```

### 5. Monitoring and Management

```bash
# View application insights
az containerapp show \
  --name message-service-app \
  --resource-group message-service-rg

# Scale application
az containerapp update \
  --name message-service-app \
  --resource-group message-service-rg \
  --min-replicas 2 \
  --max-replicas 5

# Update image
az containerapp update \
  --name message-service-app \
  --resource-group message-service-rg \
  --image messageserviceacr.azurecr.io/message-service:v2
```

## Azure Deployment Options Comparison

### Option 1: Azure Container Apps (Recommended ⭐)
**Services Used:**
- Azure Container Apps (for REST API and scheduled task)

**Pros:**
- ✅ Fully managed serverless containers
- ✅ Built-in auto-scaling (0-N instances)
- ✅ Native support for scheduled jobs (KEDA)
- ✅ Integrated with Azure services
- ✅ Simplified deployment and management
- ✅ Built-in ingress and service discovery
- ✅ Pay only for what you use

**Cons:**
- ⚠️ Newer service (less mature than AKS)
- ⚠️ Limited customization vs. Kubernetes

**Cost:** $ (Low - pay per use)
**Complexity:** Simple
**Best for:** Modern containerized apps with variable load

### Option 2: Azure App Service
**Services Used:**
- Azure App Service (for REST API)
- Azure Functions with Timer Trigger (for scheduled task)

**Pros:**
- ✅ Very easy deployment (direct JAR deployment)
- ✅ Integrated monitoring and diagnostics
- ✅ Built-in CI/CD support
- ✅ Automatic SSL certificates
- ✅ Mature platform with extensive documentation

**Cons:**
- ⚠️ Requires separate service for scheduled tasks
- ⚠️ Less control over environment
- ⚠️ Higher baseline cost (always-on instances)

**Cost:** $$ (Medium - always-on plans)
**Complexity:** Simple
**Best for:** Traditional web applications, quick deployments

### Option 3: Azure Kubernetes Service (AKS)
**Services Used:**
- AKS cluster
- Kubernetes CronJob for scheduled task

**Pros:**
- ✅ Full Kubernetes capabilities
- ✅ Maximum flexibility and control
- ✅ Industry-standard orchestration
- ✅ Easy to move to other Kubernetes platforms
- ✅ Advanced networking and security options

**Cons:**
- ⚠️ Complex setup and management
- ⚠️ Requires Kubernetes expertise
- ⚠️ Higher operational overhead
- ⚠️ More expensive (cluster always running)

**Cost:** $$$ (High - cluster costs)
**Complexity:** Complex
**Best for:** Large-scale microservices, existing Kubernetes users

### Option 4: Azure Spring Apps
**Services Used:**
- Azure Spring Apps (managed Spring Boot service)

**Pros:**
- ✅ Purpose-built for Spring Boot applications
- ✅ Integrated Spring Cloud features
- ✅ Built-in monitoring with App Insights
- ✅ Blue-green deployments
- ✅ Scheduled tasks supported

**Cons:**
- ⚠️ Vendor lock-in to Azure
- ⚠️ Higher cost than Container Apps
- ⚠️ Limited to Spring Boot applications

**Cost:** $$$ (High - specialized service)
**Complexity:** Moderate
**Best for:** Spring Boot microservices ecosystems

### Option 5: All-in Azure Functions
**Services Used:**
- Azure Functions (HTTP triggers for API)
- Azure Functions (Timer trigger for scheduled task)

**Pros:**
- ✅ True serverless (pay per execution)
- ✅ Auto-scaling
- ✅ Very low cost for low traffic
- ✅ Integrated with Azure services

**Cons:**
- ⚠️ Requires refactoring application architecture
- ⚠️ Cold start latency
- ⚠️ Execution time limits
- ⚠️ Not suitable for Spring Boot without modifications

**Cost:** $ (Low for low traffic, can scale)
**Complexity:** Complex (requires refactoring)
**Best for:** Event-driven, low-traffic applications

## Recommended Approach: Azure Container Apps

**Why Azure Container Apps?**
1. **Native Container Support:** Application is already containerized
2. **Built-in Scheduling:** Supports scheduled tasks natively (no separate service needed)
3. **Cost-Effective:** Pay only for actual usage, can scale to zero
4. **Simple Management:** Less complexity than AKS, more flexible than App Service
5. **Modern Platform:** Designed for cloud-native applications
6. **Auto-Scaling:** Scales based on HTTP traffic and custom metrics

**Migration Effort:** 4-8 hours
- 2 hours: Container image creation and testing
- 2 hours: Azure infrastructure setup
- 2 hours: Deployment and configuration
- 2 hours: Testing and validation

**Risk Assessment:**
- **Low Risk:** Application already containerized and tested
- **Medium Risk:** Scheduled task timing verification in Azure
- **Low Risk:** H2 database remains in-memory (stateless)

## Environment Variables

The application supports the following environment variables:

```bash
# Server configuration
SERVER_PORT=8080

# Database (H2 in-memory - no changes needed)
SPRING_DATASOURCE_URL=jdbc:h2:mem:messagedb

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_COM_NYTOUR_DEMO=DEBUG
```

## Cleanup

```bash
# Delete all resources
az group delete --name message-service-rg --yes --no-wait
```

## Support

For issues or questions:
- Check Azure Container Apps documentation: https://docs.microsoft.com/azure/container-apps/
- Review application logs in Azure Portal
- Use `az containerapp logs` command for debugging
