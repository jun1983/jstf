# Java Selenium Test Framework

JAVA SELENIUM TEST FRAMEWORK (JSTF) is aiming to build an automation test platform, consolidating most capabilities from the market in one. 

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
 - JElement: Much different from WebElement, JElement stores element locators instead of a real WebElement instance. It finds web elements when interactting it and automatically retry. (You will never see StaleElementReferenceException again) 
 - JAXE: Automated Accesibility Testing library. Integrated with [AXE core](https://github.com/dequelabs/axe-core).
 - BrokenLinkHelper: Scan broken links in a page or inside an web element.
 - ServiceHelper: Send http request with Apache Http Client, auto adapting proxy setting in JSTF configuration. 
 - JMockProxy: Running a mock proxy behind web browser, programmatically control http traffic. It allows you to manipulate HTTP requests and responses, capture HTTP content, and export performance data as a [HAR file]. Inegrated with [BrowserMob Proxy](https://github.com/lightbody/browsermob-proxy) .
 - ZAP: ZAP security tool is also running behind the web browser. It scans all traffic during selenium tests and generate security test report after test. Inegrated with [OWASP Zed Attack Proxy](https://www.owasp.org/index.php/OWASP_Zed_Attack_Proxy_Project)

