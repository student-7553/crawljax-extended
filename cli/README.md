# Crawljax Command-line
This is the Command-line distribution of Crawljax. The project is assembled in a ZIP file containing the jar that you can run to execute the crawler.

Build from the repo root:

```
mvn -pl cli -am -DskipTests -Dspotless.check.skip=true package
```

Do **not** run `cli/target/crawljax-cli-*.jar` directly. That jar is thin and expects a `lib/` folder next to it (commons-cli and other deps). Unzip the distribution and run from that folder:

```
unzip -o cli/target/crawljax-cli-5.2.4-SNAPSHOT.zip -d /tmp
cd /tmp/crawljax-cli-5.2.4-SNAPSHOT
java -jar crawljax-cli-5.2.4-SNAPSHOT.jar theUrl theOutputDir [OPTIONS]
```

Example with an inferred model (paths relative to the unzipped dir, or use absolute paths):

```
java -jar crawljax-cli-5.2.4-SNAPSHOT.jar \
  http://localhost:3001/ /path/to/out \
  -m /path/to/samples/model.json -o -v
```

Alternatively, from the repo without unzipping:

```
mvn -pl cli -Dspotless.check.skip=true exec:java \
  -Dexec.mainClass=com.crawljax.cli.JarRunner \
  -Dexec.args="http://localhost:3001/ ./out -m samples/model.json -o -v"
```

```
usage: java -jar crawljax-cli-version.jar theUrl theOutputDir [OPTIONS]
   -a,--crawlHiddenAnchors     Crawl anchors even if they are not visible in the
                               browser.
   -b,--browser <arg>          browser type: chrome (default), chrome_headless, firefox, firefox_headless,
                               phantomjs
   -click <arg>                a comma separated list of HTML tags that should
                               be clicked. Default is A and BUTTON
   -d,--depth <arg>            crawl depth level. Default is 2
   -h,--help                   print this message
   -log <arg>                  Log to this file instead of the console
   -m,--model <arg>            path to an inferred model JSON file (GK-Tail)
                               used to guide the crawl
   -o,--override               Override the output directory if non-empty
   -p,--parallel <arg>         Number of browsers to use for crawling. Default
                               is 1
   -s,--maxstates <arg>        max number of states to crawl. Default is 0
                               (unlimited)
   -t,--timeout <arg>          Specify the maximum crawl time in minutes
   -v,--verbose                Be extra verbose
   -version                    print the version information and exit
   -waitAfterEvent <arg>       the time to wait after an event has been fired in
                               milliseconds. Default is 500
   -waitAfterReload <arg>      the time to wait after an URL has been loaded in
                               milliseconds. Default is 500
```

The output folder will contain the output of the Crawl overview plugin.

When `-m` / `--model` is given, Crawljax loads the inferred automaton and fires its pointer/keyboard actions until every reachable model state has been visited at least once, then stops.