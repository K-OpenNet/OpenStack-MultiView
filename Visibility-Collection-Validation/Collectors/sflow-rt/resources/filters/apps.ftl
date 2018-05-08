<!DOCTYPE html>
<html lang="en">
<head>
<title>sFlow-RT Applications</title>
<link rel="stylesheet" href="../inc/inmsf/main.css" type="text/css">
</head>
<body>
<div id="titleBar"><div id="product"><span id="logo"></span>sFlow-RT</div></div>
<div id="content">
<h1>Installed Applications</h1>
<#assign names = apps?keys?sort>
<#if names[0]??>
<p>Click on an application in the list to access the application's home page:</p>
<#assign names = apps?keys?sort>
<ul>
<#list names as name>
<li><a href="${name}/html/">${name}</a></li></#list>
</ul>
<#else>
<p><i>No applications installed</i></p>
<p>Visit <a href="http://www.sflow-rt.com/">sFlow-RT.com</a> to find applications,  learn how to author applications, and connect with the sFlow-RT developer and user community.</p>
</#if>
</div>
<#include "resources/filters/footer.ftl">
