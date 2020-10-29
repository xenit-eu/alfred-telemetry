<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/admin/admin-common.lib.js">

function main()
{
   model.tools = Admin.getConsoleTools("alfred-telemetry");
   model.metadata = Admin.getServerMetaData();
}

main();