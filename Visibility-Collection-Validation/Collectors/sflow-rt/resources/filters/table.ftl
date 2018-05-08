<#assign tab = "metrics">
<#include "resources/filters/header.ftl"/>
<div id="content">
<#if metrics[0]??>
<table id="metrics">
<thead>
<tr class="firstrow"><th>agent</th>
<#list metrics[0] as heading>
<th colspan="2">${heading.metricName}</th>
</#list>
</tr>
<tr class="secondrow">
<th></th>
<#list metrics[0] as heading>
<th>dataSource</th>
<th>metricValue</th>
</#list>
</tr>
</thead>
<tbody>
<#list metrics as row>
<#assign trCss = (row_index % 2 == 0)?string("even","odd")>
<tr class="${trCss}">
<td>${row[0].agent}</td>
<#list row as el>
<td>${el.dataSource}</td>
<td>${el.metricValue}</td>
</#list>
</tr></#list>
</tbody>
</table>
</#if>
</div>
<#include "resources/filters/footer.ftl">
