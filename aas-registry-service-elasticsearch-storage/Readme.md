# Asset Administration Registry Server Elasticsearch Storage

This registry storage implementation uses [ElasticSearch](https://www.elastic.co/de/elastic-stack/) as document store and generates a specific data model mit ElasticSearch annotations. Include this dependency if you want to use this storage implementation:

```xml

	<dependency>
		<groupId>org.eclipse.basyx.aas.registry</groupId>
		<artifactId>aas-registry-service-elasticsearch-storage</artifactId>
	</dependency>
```

Then included, you can active the it by either setting the active profile or the registry-storage attribute:
```
 <stringAttribute key="org.eclipse.jdt.launching.VM_ARGUMENTS" value="-Delasticsearch.url=127.0.0.1:9200 -Dspring.profiles.active=logEvents,elasticsearchStorage"/>

```

Dont't forget to also set the elastic search url

```
-Delasticsearch.url=127.0.0.1:9200 
```
It also comes whith testcases

