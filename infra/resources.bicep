param location string = resourceGroup().location
param tags object = {}
param principalId string = ''

// Generate unique names for resources
var resourceToken = uniqueString(subscription().id, resourceGroup().id, location)
var appServicePlanName = 'plan-${resourceToken}'
var appServiceName = 'app-${resourceToken}'
var logAnalyticsName = 'log-${resourceToken}'
var appInsightsName = 'appi-${resourceToken}'

// Log Analytics workspace for monitoring
resource logAnalytics 'Microsoft.OperationalInsights/workspaces@2022-10-01' = {
  name: logAnalyticsName
  location: location
  tags: tags
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

// Application Insights for application monitoring
resource appInsights 'Microsoft.Insights/components@2020-02-02' = {
  name: appInsightsName
  location: location
  tags: tags
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalytics.id
  }
}

// App Service Plan - Linux with Java 17 support
resource appServicePlan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: appServicePlanName
  location: location
  tags: tags
  sku: {
    name: 'B1' // Basic tier - suitable for dev/test workloads
    tier: 'Basic'
    capacity: 1
  }
  kind: 'linux'
  properties: {
    reserved: true // Required for Linux plans
  }
}

// App Service for hosting the Spring Boot application
resource appService 'Microsoft.Web/sites@2022-09-01' = {
  name: appServiceName
  location: location
  tags: union(tags, { 'azd-service-name': 'api' })
  kind: 'app,linux'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17' // Java 17 runtime
      alwaysOn: true // Keep the app always running to ensure scheduled tasks work
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
      healthCheckPath: '/api/messages' // Health check endpoint
      appSettings: [
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: appInsights.properties.ConnectionString
        }
        {
          name: 'APPINSIGHTS_INSTRUMENTATIONKEY'
          value: appInsights.properties.InstrumentationKey
        }
        {
          name: 'ApplicationInsightsAgent_EXTENSION_VERSION'
          value: '~3'
        }
        {
          name: 'XDT_MicrosoftApplicationInsights_Mode'
          value: 'recommended'
        }
        {
          name: 'WEBSITE_RUN_FROM_PACKAGE'
          value: '1'
        }
        {
          name: 'SERVER_PORT'
          value: '8080'
        }
        {
          name: 'WEBSITES_PORT'
          value: '8080' // Tell Azure the app listens on port 8080
        }
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'prod'
        }
      ]
    }
  }
}

// Outputs for use by azd
output WEBSITE_URL string = 'https://${appService.properties.defaultHostName}'
output APP_SERVICE_NAME string = appService.name
output APPLICATIONINSIGHTS_CONNECTION_STRING string = appInsights.properties.ConnectionString
