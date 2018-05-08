<!DOCTYPE html>
<html lang="en">
<head>
<title>sFlow-RT</title>
<meta charset="utf-8">
<meta name="google" content="notranslate">
<link rel="stylesheet" type="text/css" href="${root}inc/inmsf/main.css" media="all">
<#if styleSheets??>
<#list styleSheets as styleSheet><link rel="stylesheet" type="text/css" href="${root}inc/${styleSheet}">
</#list>
</#if>
<#if scripts??>
<#list scripts as script><script type="text/javascript" src="${root}inc/${script}"></script>
</#list>
</#if>
<link rel="icon" type="image/png" href="${root}inc/img/favicon.png">
</head>
<body>

<div id="main">
<div id="titleBar"><a name="top"></a><div id="product"><span id="logo"></span>sFlow-RT</div></div>
<div id="menuBar">
<ul>
  <li><a href="${root}app/">Apps</a></li>
<#if tab == "agents">
  <li><a class="here" href="${root}agents/html">Agents</a></li> 
<#else>
  <li><a href="${root}agents/html">Agents</a></li>
</#if>
<#if tab == "metrics">
  <li><a class="here" href="${root}metrics/html">Metrics</a></li>
<#else>
  <li><a href="${root}metrics/html">Metrics</a></li>
</#if>
<#if tab == "keys">
  <li><a class="here" href="${root}flowkeys/html">Keys</a></li>
<#else>
  <li><a href="${root}flowkeys/html">Keys</a></li>
</#if>
<#if tab == "flows">
  <li><a class="here" href="${root}flow/html">Flows</a></li>
<#else>
  <li><a href="${root}flow/html">Flows</a></li>
</#if>
<#if tab == "thresholds">
  <li><a class="here" href="${root}threshold/html">Thresholds</a></li>
<#else>
  <li><a href="${root}threshold/html">Thresholds</a></li>
</#if>
<#if tab == "events">
  <li><a class="here" href="${root}events/html">Events</a></li>
<#else>
  <li><a href="${root}events/html">Events</a></li>
</#if>
<#if tab == "about">
  <li><a class="here" href="${root}html/index.html">About</a></li>
<#else>
  <li><a href="${root}html/index.html">About</a></li>
</#if>
</ul>
</div>
