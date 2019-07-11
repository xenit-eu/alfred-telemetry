<#include "/org/alfresco/repository/admin/admin-template.ftl" />

<@page title=msg("alfred-telemetry.title") readonly=true>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.graphite.title") />
        <ul>
        <#list registryGraphite?keys as key>
        <li><b>${key}</b>: ${registryGraphite[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.jmx.title") />
        <ul>
        <#list registryJmx?keys as key>
        <li><b>${key}</b>: ${registryJmx[key]}</li>
        </#list>
        </ul>
    </div>

    <div class="column-full">
        <@section label=msg("alfred-telemetry.registry.prometheus.title") />
        <p class="info">${msg("alfred-telemetry.registry.prometheus.description")?html}</p>
        <ul>
        <#list registryPrometheus?keys as key>
        <li><b>${key}</b>: ${registryPrometheus[key]}</li>
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