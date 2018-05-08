<#assign tab = "about">
<#include "resources/filters/header.ftl"/>
<#setting datetime_format="yyyy-MM-dd'T'HH:mm:ss'Z'">
<#assign licenseType = licenseInfo.license.type>
<#assign row_num = 0>
<#function zebra>
  <#assign row_num = row_num + 1>
  <#if row_num% 2 == 0>
    <#return "even" />
  <#else>
    <#return "odd" />
  </#if>
</#function>
<#function status message flag>
  <#if flag>
     <#return message/>
  <#else>
      <#return '<span class="warn">${message}</span>'/>
  </#if>
</#function>
<div id="content">
<table class="overview">
<tbody>
<tr class="${zebra()}"><th>Software Version</th><td>${version}</td></tr>
<#if licenseInfo.analyzer.host??>
<tr class="${zebra()}"><th>Host Name</th><td>${licenseInfo.analyzer.host}</td></tr>
</#if>
<#if licenseInfo.analyzer.bps??>
<tr class="${zebra()}"><th>sFlow Rate</th><td><a href="../analyzer/html">${licenseInfo.analyzer.bps?round} bits/s</a></td></tr>
</#if>
<tr><td class="sep" colspan="2">&nbsp;</td></tr>
<#if licenseType == "production" || licenseType == "enterprise">
<#if licenseType == "production">
<tr class="${zebra()}"><th>License Agreement</th><td><a href="license.html">Production License</a></td></tr>
<#elseif licenseType == "enterprise">
<tr class="${zebra()}"><th>License Agreement</th><td><a href="license.html">Enterprise License</a></td></tr>
</#if>
<#if licenseInfo.license.license??>
<tr class="${zebra()}"><th>License Number</th><td>${licenseInfo.license.license}</td></tr>
</#if>
<#if licenseInfo.license.account??>
<tr class="${zebra()}"><th>Licensed User</th><td>${licenseInfo.license.account}</td></tr>
</#if>
<#if licenseInfo.license.host??>
<tr class="${zebra()}"><th>Licensed Host</th><td>${status(licenseInfo.license.host,licenseInfo.license.host_ok)}</td></tr>
</#if>
<#if licenseInfo.license.max_bps??>
<tr class="${zebra()}"><th>Licensed Max. sFlow Rate</th><td>${status(licenseInfo.license.max_bps?number + " bits/s",licenseInfo.license.max_bps_ok)}</td></tr>
</#if>
<#if licenseInfo.license.expires??>
<tr class="${zebra()}"><th>License Expires</th><td>${status(licenseInfo.license.expires?datetime?date,licenseInfo.license.expires_ok)}</td></tr>
</#if>
<#else>
<tr class="${zebra()}"><th>License</th><td><a href="license.html">Research and Evaluation License</a></td></tr>
</#if>
</tbody>
</table>
<p>The sFlow-RT analytics module incorporates InMon's 
asynchronous sFlow analysis technology (patent pending) to deliver real-time performance metrics
through the <a href="http://sflow-rt.com/reference.php">REST and JavaScript APIs</a>. Visit <a href="http://www.sflow-rt.com">sFlow-RT.com</a> for documentation, software, and community support.</p>
<#if licenseType == "no-production">
<p>For startup settings, see <a href="http://sflow-rt.com/reference.php#properties">System Properties</a>. The default memory setting in the startup script is optimized for small deployments and must be increased for large scale installations.</p>
</#if>
<p><a href="acknowledgements.html">Acknowledgements</a> lists
third party software included in this package.</p>
</div>
<#include "resources/filters/footer.ftl">
