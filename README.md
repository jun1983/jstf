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

 - QDriver: A wrapped selenium web driver, more advanced and robust.
 - QElement: Much different from WebElement, QElement stores element locators instead of a real WebElement instance. It finds web elements when interactting it and automatically retry. `You will never see StaleElementReferenceException again` 
 - QAXE: Automated Accesibility Testing library. Integrated with [AXE core](https://github.com/dequelabs/axe-core).
 - BrokenLinkHelper: Scan broken links in a page or inside an web element.
 - ServiceHelper: Send http request with Apache Http Client, auto adapting proxy setting in JSTF configuration. 
 - QMockProxy: Running a mock proxy behind web browser, programmatically control http traffic. It allows you to manipulate HTTP requests and responses, capture HTTP content, and export performance data as a [HAR file]. Inegrated with [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) .
 - ZAP: ZAP security tool is also running behind the web browser. It scans all traffic during selenium tests and generate security test report after test. Inegrated with [OWASP Zed Attack Proxy](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)

### Framework Configuration
JSFT uses yml file for configuration. Be default it is named jconfig.yml sitting under the project. To use a customised location, set envrionment vairible `jstf_config_file` on your machine before running test. 

```
#JSTF Configuration
browser: "chrome" #chrome, firefox.   remote driver & other browsers are coming soon.
is_headless_browser: "false"
reuse_browser: "false"
#browserHub=http://browserstack:4444/wd/hub

#mockproxy settings
is_mock_proxy_enabled: "true"
is_cors_enabled: "true" #only works when mock proxy enabled

#zap settings
is_zap_enabled: "false"
zap_server: 127.0.0.1:6666
zap_app_path: "" #ZAP zipped binary, or start script file.
zap_report_path: "target/ZAPReport.html"
zap_log_file: "target/jstflogs/zap.log"

#proxy settings
isProxyEnabled: "false"
proxyAddress: 10.10.10.10:8080
#proxyBypass: 10.*, 127.0.0.1

#Selenium settings
element_timeout: "30"
assertion_timeout: "3"
element_retrytimes: "5"
default_window_size: "" #1024*768, default is max window


#Accessibility(AXE) settings
axe_report_level: critical #critical, serious, moderate, all
axe_rules: all  #wcag2a, wcag2aa, wcag412,section508,section508.22.a, all
```

###HTTP Request Manipulation

HTTP request manipulation is supported in JMockProxy. it is very easy to use and reliable. For most use cases, inspecting and modifying requests/responses, `addRequestFilter` and `addResponseFilter` will be sufficient. You can programmatically setup the injection rule before the request happens.
```java
    qDriver.getMockProxy().addRequestFilter(new RequestFilter() {
        @Override
        public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo) {
            if(messageInfo.getOriginalUrl().equals(homepageUrl)){
                request.headers().add("testHeader", "value");
            }
            return null;
		}
	});
    
    qDriver.getMockProxy().addResponseFilter(new ResponseFilter() {
		@Override
		public void filterResponse(HttpResponse response, HttpMessageContents contents, HttpMessageInfo messageInfo) {
			if(messageInfo.getOriginalUrl().equals(homepageUrl)) {
				response.headers().add("testHeader", "value");
			}
		}
	});
```

For more practical use of JMockProxy, please refer to [QMockProxyTestCases](src/test/java/com/jstf/tests/MockProxyTest.java)
