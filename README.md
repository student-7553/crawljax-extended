Crawljax 
========

Crawljax is a tool for automatically crawling and testing modern web applications. 
Crawljax can explore any (even single-page dynamic JavaScript-based) web application through an event-driven dynamic crawling engine.
It produces as output a state-flow graph of the dynamic DOM states and the event-based transitions between them.
Crawljax can easily be extended through its easy-to-use [plugin architecture](https://github.com/crawljax/crawljax/wiki/Writing-a-plugin).

Maven
-----
Crawljax releases are available on [Maven central repository](https://central.sonatype.com/search?smo=true&q=crawljax).

	<dependency>
	    <groupId>com.crawljax</groupId>
	    <artifactId>crawljax-core</artifactId>
	    <version>${crawljax-version}</version>
	</dependency>

Documentation
-------------

You can find more technical documentation in our [project wiki](https://github.com/crawljax/crawljax/wiki/). 


Community
---------

Keep track of development and community news.

* We welcome [contributions](https://github.com/crawljax/crawljax/blob/master/CONTRIBUTING.md)!
* Follow [@crawljax](https://twitter.com/crawljax) on Twitter.


Changelog
---------

Detailed change history is available in our [changelog](https://github.com/crawljax/crawljax/blob/master/CHANGELOG.md).

Build
---------
mvn -pl cli -am -DskipTests -Dspotless.check.skip=true package


Model Run
---------
unzip -o cli/target/crawljax-cli-5.2.4-SNAPSHOT.zip -d ./tmp
java -jar ./tmp/crawljax-cli-5.2.4-SNAPSHOT/crawljax-cli-5.2.4-SNAPSHOT.jar \
  http://localhost:1800/ \
  ./out \
  -d 10 \
  -waitAfterEvent 1000 \
  -waitAfterReload 1000 \
  -m ./models/retro-model-1.json -o -v