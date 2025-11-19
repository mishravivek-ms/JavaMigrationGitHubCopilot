@description('Location for all resources')
param location string

@description('Tags to apply to all resources')
param tags object

@description('Environment name')
param environmentName string

@description('Resource token for unique naming')
param resourceToken string

@description('Principal ID for RBAC')
param principalId string

@description('Principal type (User, ServicePrincipal, Group)')
param principalType string

@description('Enable Application Insights')
param enableAppInsights bool

@description('App Service Plan SKU')
param appServicePlanSku string

var abbrs = loadJsonContent('./abbreviations.json')

// Application Insights
resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2022-10-01' = if (enableAppInsights) {
  name: '${abbrs.operationalInsightsWorkspaces}${resourceToken}'
  location: location
  tags: tags
  properties: {
    sku: {
      name: 'PerGB2018'
    }
    retentionInDays: 30
  }
}

resource appInsights 'Microsoft.Insights/components@2020-02-02' = if (enableAppInsights) {
  name: '${abbrs.insightsComponents}${resourceToken}'
  location: location
  tags: tags
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: enableAppInsights ? logAnalyticsWorkspace.id : null
  }
}

// App Service Plan
resource appServicePlan 'Microsoft.Web/serverfarms@2022-09-01' = {
  name: '${abbrs.webServerFarms}${resourceToken}'
  location: location
  tags: tags
  sku: {
    name: appServicePlanSku
  }
  kind: 'linux'
  properties: {
    reserved: true
  }
}

// App Service
resource appService 'Microsoft.Web/sites@2022-09-01' = {
  name: '${abbrs.webSitesAppService}${resourceToken}'
  location: location
  tags: union(tags, {
    'azd-service-name': 'api'
  })
  kind: 'app,linux'
  properties: {
    serverFarmId: appServicePlan.id
    siteConfig: {
      linuxFxVersion: 'JAVA|17-java17'
      alwaysOn: true
      healthCheckPath: '/api/messages'
      appSettings: [
        {
          name: 'APPLICATIONINSIGHTS_CONNECTION_STRING'
          value: enableAppInsights ? appInsights.properties.ConnectionString : ''
        }
        {
          name: 'ApplicationInsightsAgent_EXTENSION_VERSION'
          value: '~3'
        }
        {
          name: 'PORT'
          value: '8080'
        }
        {
          name: 'SPRING_PROFILES_ACTIVE'
          value: 'prod'
        }
        {
          name: 'JAVA_OPTS'
          value: '-Dserver.port=8080 -Xmx512m'
        }
      ]
      javaVersion: '17'
      use32BitWorkerProcess: false
      ftpsState: 'Disabled'
      minTlsVersion: '1.2'
    }
    httpsOnly: true
  }
}

// Outputs
output API_BASE_URL string = 'https://${appService.properties.defaultHostName}'
output APP_SERVICE_NAME string = appService.name
output APPLICATIONINSIGHTS_CONNECTION_STRING string = enableAppInsights ? appInsights.properties.ConnectionString : ''
output APP_SERVICE_PLAN_NAME string = appServicePlan.name
