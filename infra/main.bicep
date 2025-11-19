targetScope = 'subscription'

@minLength(1)
@maxLength(64)
@description('Name of the environment that will be used to name resources in a pattern like <resourceAbbreviation>-<environmentName>-<location>')
param environmentName string

@minLength(1)
@description('Primary location for all resources')
param location string

@description('Id of the user or app to assign application roles')
param principalId string = ''

@description('Type of the principal (User, ServicePrincipal, Group)')
param principalType string = ''

// Optional parameters with defaults
@description('Whether to enable Application Insights')
param enableAppInsights bool = true

@description('The tier of the App Service Plan')
@allowed(['B1', 'B2', 'B3', 'S1', 'S2', 'S3', 'P1v2', 'P2v2', 'P3v2'])
param appServicePlanSku string = 'B1'

var tags = {
  'azd-env-name': environmentName
  'application': 'message-service'
  'environment': environmentName
}

var abbrs = loadJsonContent('./abbreviations.json')
var resourceToken = toLower(uniqueString(subscription().id, environmentName, location))

// Resource group
resource rg 'Microsoft.Resources/resourceGroups@2021-04-01' = {
  name: '${abbrs.resourcesResourceGroups}${environmentName}'
  location: location
  tags: tags
}

// Call the resources module to create the App Service and related resources
module resources './resources.bicep' = {
  name: 'resources'
  scope: rg
  params: {
    location: location
    tags: tags
    environmentName: environmentName
    resourceToken: resourceToken
    principalId: principalId
    principalType: principalType
    enableAppInsights: enableAppInsights
    appServicePlanSku: appServicePlanSku
  }
}

// Outputs
output AZURE_LOCATION string = location
output AZURE_TENANT_ID string = tenant().tenantId
output AZURE_RESOURCE_GROUP string = rg.name

output API_BASE_URL string = resources.outputs.API_BASE_URL
output APP_SERVICE_NAME string = resources.outputs.APP_SERVICE_NAME
output APPLICATIONINSIGHTS_CONNECTION_STRING string = enableAppInsights ? resources.outputs.APPLICATIONINSIGHTS_CONNECTION_STRING : ''
