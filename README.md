# Java Selenium Test Framework

JAVA SELENIUM TEST FRAMEWORK (JSTF) is aiming to build an automation test platform, consolidating major capabilities in one. 

### Getting started
To use JSTF in your java + selenium tests or application, add the `JSTF` dependency to your pom:
```xml
    <dependency>
        <groupId>com.jstf</groupId>
        <artifactId>jselenium</artifactId>
        <version>0.0.1</version>
        <scope>test</scope>
    </dependency>
```

### Setup before running test:
```java
  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
    JSTF.setup();
  }
  
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
    JSTF.teardown();
  }
```

### Features and Usage

JSTF provides a series of enhanced selenium functions and libraries that enable users to run functional, non-functional testing with very little effort.

 - JDriver: A wrapped selenium web driver, more advanced and robust.
 - JElement: Much different from WebElement, JElement stores element locators instead of a real WebElement instance. It finds web elements when interactting it and automatically retry. `You will never see StaleElementReferenceException again` 
 - JAXE: Automated Accesibility Testing library. Integrated with [AXE core](https://github.com/dequelabs/axe-core).
 - BrokenLinkHelper: Scan broken links in a page or inside an web element.
 - ServiceHelper: Send http request with Apache Http Client, auto adapting proxy setting in JSTF configuration. 
 - JMockProxy: Running a mock proxy behind web browser, programmatically control http traffic. It allows you to manipulate HTTP requests and responses, capture HTTP content, and export performance data as a [HAR file]. Inegrated with [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) .
 - ZAP: ZAP security tool is also running behind the web browser. It scans all traffic during selenium tests and generate security test report after test. Inegrated with [OWASP Zed Attack Proxy](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)

### Framework Configuration
JSFT uses yml file for configuration. Be default it is named jconfig.yml sitting under the project. To use a customised location, set envrionment vairible `jstf_config_file` on your machine before running test. 

```
#JSTF Configuration
browser: "chrome"
is_headless_browser: "false"
reuse_browser: "false"
#browserHub=http://selenium-grid.master.dev.a1028-01.ams02.nonp.qcpaws.qantas.com.au:4444/wd/hub

#mockproxy & zap settings
is_mock_proxy_enabled: "true"

is_zap_enabled: "false"
zap_server: 127.0.0.1:6666
zap_app_path: "" #ZAP zipped binary, or start script file.
zap_report_path: "target/ZAPReport.html"
zap_log_file: "target/jstflogs/zap.log"

isProxyEnabled: "false"
proxyAddress: proxy.qcpaws.qantas.com.au:3128
proxyBypass: 10.*, *.qcpaws.qantas.com.au, www-staging.qantas.com, 127.0.0.1

#Corporrate Proxy Setup
is_proxy_enabled: "false" #true, false
proxy_addr: "10.10.10.10:8080"
proxy_bypass: ""

#Selenium settings
element_timeout: "30"
assertion_timeout: "3"
element_retrytimes: "5"
default_window_size: "" #1024*768, default is max window


#Accessibility(AXE) settings
axe_report_level: critical #critical, serious, moderate, all
axe_rules: all  #wcag2a, wcag2aa, wcag412,section508,section508.22.a, all
```
