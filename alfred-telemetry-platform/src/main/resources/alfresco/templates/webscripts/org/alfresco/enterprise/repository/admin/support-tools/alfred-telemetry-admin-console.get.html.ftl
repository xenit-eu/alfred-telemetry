<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<@page title=msg("alfred-telemetry.title") readonly=true>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.settings") />

        <@field label="Alfred Telemetry Version" description="The version of the Alfred Telemetry module" value="${telemetry.module.version}" />
        <@field label="Micrometer Version" description="The version of micrometer detected on the classpath" value="${telemetry.dependencies.micrometer.version}" />
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.graphite.title") />
        <ul>
        <#list telemetry.registries.graphite.properties?keys as key>
            <li><b>${key}</b>: ${telemetry.registries.graphite.properties[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.jmx.title") />
        <ul>
        <#list telemetry.registries.jmx.properties?keys as key>
            <li><b>${key}</b>: ${telemetry.registries.jmx.properties[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.prometheus.title") />
        <p class="info">${msg("alfred-telemetry.registry.prometheus.description")?html}</p>
        <ul>
        <#list telemetry.registries.prometheus.properties?keys as key>
            <li><b>${key}</b>: ${telemetry.registries.prometheus.properties[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.alfresco-integration.title") />
        <p class="info">${msg("alfred-telemetry.alfresco-integration.description")?html}</p>
        <ul>
        <#list alfrescoIntegration?keys as key>
        <li><b>${key}</b>: ${alfrescoIntegration[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.metrics.title") />
        <table class="data scheduledjobs" width=80%>
          <thead>
            <tr>
                <th>${msg("meters.table-header.meter-name")?html}</th>
                <th>${msg("meters.table-header.meter-description")?html}</th>
                <th>${msg("meters.table-header.meter-tags")?html}</th>
                <th>${msg("meters.table-header.meter-type")?html}</th>
            </tr>
          </thead>
          <tbody>
             <#list meters as meter>
            <tr>
                <td>${meter.name!""}</td>
                <td>${meter.description!""}</td>
                <td>${meter.tags?join(", ")!""}</td>
                <td>${meter.type!""}</td>
            </tr>
            </#list>
          </tbody>
        </table>
    </div>

</@page>