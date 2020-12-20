#JAXP Note 静下心来读读官方文档

### First,What is JAXP
JAXP，听起来比较陌生，不像Spring，听起来就知道什么意思，春天嘛。JAXP，JAVA API FOR XML PROCESSING，JAVA的用来处理XML的API，就是一套类库。

官网上这样一句话：JAXP leverages the parser standards Simple API for XML Parsing (SAX) and Document Object Model (DOM) so that you can choose to parse your data as a stream of events or to build an object representation of it.
就像读书那会，每个知识点都可能包含不止一个考点。JAXP有两种解析方式，`Simple API for xml parsing`  和  `Document Object Model`,也就是常听到的SAX模型和DOM模型，区别也在这句话里说明了：
`事件流解析` 和 `DOM模型`。  JAXP也支持 XSLT，提供了命名空间的支持。1.4版本，这里应该是指的JDK1.4吧，JAXP支持了StAX，就是Streaming API for xml processing。


### SAX  DOM
SAX是XML-DEV group , DOM是W3C定义。类库包名主要有几个：
javax.xml.parser        提供了通用的XML处理接口给SAX和DOM
org.w3c.dom             定义了Document和DOM的其他组件
org.xml.sax             定义了SAX基本API
javax.xml.transform     定义了XSLT的API
javax.xml.stream        定义了StAX的API

特点： SAX  事件驱动(event-driven)、串行访问机制(serial-access)、逐元素处理(element-by-element)。
      DOM  简单的API，类似树结构的对象，直观且庞大，整个对象模型都存在于内存中。 处理时候将整个XML读入，转换成DOM树存在于内存中。
      StAX 基于流，事件驱动、推拉模型，使用起来更简便，内存使用效率更高效。  看来有必要学习下StAX了，哪怕不能深入，也要学习下如何使用。
      
      
### SAX
一口气看完了SAX官网的介绍，明了简洁。SAXParserFactory提供了SAXParser，SAXParser包装了SAXReader，当调用parse方法解析xml时候，一些回调方法就会被触发。
ContentHandler、ErrorHandler、DTDHandler、EntityResolver。 虽然还没开始使用，但是已经有了一个猜测，SAXParseFactory构造时候需要传入回调的几个钩子
实例，然后工厂模式获取一个parser， 解析一个InputStream，各个钩子的回调方法就会触发。


#### SAX各个类
SAXParserFactory        生成parser实例的工厂，由系统变量决定，这里存疑。包全限定名：javax.xml.parser.SAXParserFactory。
SAXParser               传入XML数据和DefaultHandler,  DefaultHandler没在上面出现，它用来处理XML并且调用上面回调的各个组件。   
SAXReader               SAXParser包含一个SAXReader。通常不关心，但是一旦要用到，SAXParser.getXMLReader()就能拿到。
DefaultHandler          上图中没有出现，因为DefaultHandler继承了ContentHandler、ErrorHandler、DTDHandler、EntityResolver。人傻了，我们可以继承他来只实现我们需要覆盖的部分。
ContentHandler          当识别XML元素，会调用 startDocument  endDocument  startElement  endElement，当遇到文本或内联指令的时候还会调用characters和 processingInstruction，
                        （内联指令不知道翻译的对不对，我没遇到过，抖机灵）
ErrorHandler            解析时候发生错误，调用error fatalError warning， 默认的errorHandler只有fatal error才会抛出异常，并且忽略其他错误。 即使使用DOM，也需要了解SAX parser,
                        针对某些情况其他错误，你可能需要手动抛出异常。
DTDHandler              定义了方法你通常不会用到，处理DTD的时候会用到识别、转换不认识的实体。                                               
EntityResolver          解析器必须识别URI标记的数据时候调用其resolveEntity方法。  某些场景文档可以用URN标识，区别于URL。EntityResolver使用URN而不是URL来查找文档。
                        这里有点点懵逼，后面用到再说。
                        
综上可以发现，ContentHandler是应用程序所必须要覆写的，因各种需求来解析XML。

SAX parser在这些包中：
org.xml.sax             SAX的常用接口定义。
org.xml.sax.ext         
org.xml.sax.helpers     使得我们更容易使用SAX API，继承default handler，只需要覆写我们用到的方法。
javax.xml.parser        定义了SAXParserFactory，以及一些报告错误的异常类

SAX官方示例程序真的难找，网上苦搜还是没有找到.于是按照官方文档自己写了类、写了资源文件.

#### SAX Hello World
见sax.capater1中SAXLocalNameCount。 几个知识点记录下,

##### XML的小知识
如下是一个Spring的配置文件，`xmlns="http://www.springframework.org/schema/beans"`这个是默认的命名空间的写法，后面这个http://www.springframework.org/schema/beans
命名空间的URI，URI不一定要是真实可访问的URI。 `xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"`这个是一个约定的写法，代表XML的结构是XSD(XML Schema Definition)文件，
`xmlns:context="http://www.springframework.org/schema/context" `这个就是Spring自定义的命名空间，而xsi:schemaLocation就是XSD文件所在的位置，
xsi:schemaLocation是key  value形式书写的。

有了上面的基础，context:component-scan为例，context就是命名空间namespace，component-scan就是localName，节点名称，而context:component-scan叫QName，
是命名空间:节点名称。而这里的URI就是context的URI，即http://www.springframework.org/schema/context。 命名空间的作用，我觉得一个是方便区分各个XML节点的分类，比如spring就有很多命名空间，
context、tx、bean等等， 第二个作用是唯一标识，假如context和tx中都有标签叫bean呢，属性可能完全不一样，没有命名空间会很麻烦。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context" 
       xmlns:tx="http://www.springframework.org/schema/tx"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
        http://www.springframework.org/schema/beans/spring-beans.xsd 
        http://www.springframework.org/schema/context 
        http://www.springframework.org/schema/context/spring-context.xsd 
        http://www.springframework.org/schema/tx 
        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <!--扫描service包-->
    <context:component-scan base-package="ssm.shiro.service.impl"></context:component-scan>
</beans>
```


##### setNamespaceAware方法：
java代码中有这样一行 saxParserFactory.setNamespaceAware(true);看了上面的XML基础，就是 设置是否对namespace命名空间感知。

```java
public static void main(String[] args)throws Exception {
        String filename = null;
        for (int i = 0; i < args.length ; i++) {
            filename = args[i];
            if (i!=args.length-1){
                usage();
            }
        }
        if (filename == null){
            usage();
        }

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        SAXParser parser = factory.newSAXParser();
        XMLReader xmlReader = parser.getXMLReader();
        xmlReader.setContentHandler(new SAXLocalNameCount());
        xmlReader.parse(convertToFileURL(filename));
    }
```

先来说说， 如何设置，以及设置这个值了有什么用。
一。设置namespaceAware有两种方式： 
1.       factory.setNamespaceAware(true);
2.       factory.setFeature("http://xml.org/sax/features/namespaces",true);               
      
第二种方式的URI在哪儿呢？ com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl这个类中定义了一些列字符串常量，可以从中找到。

二。设置了这个值为true，有什么用呢？
对命名空间可感知为true， 当我们实现ContentHandler重写 startElement方法时候，就能够获取到uri、localname两个值，否则就需要从qName中去根据：来拆分，这样会有多一步骤麻烦点。
public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {}
startElement 方法的uri  localName  就是设置了命名空间感知true才能够获取到，否则都是""空字符串。

三。为什么设置了true，才能够拿到uri localname？
AbstractSAXParser类中如下：`String localpart = fNamespaces ? element.localpart : "";` 下面一行就是调用的startElement方法了。
fNamespace就是我们设置的namespaceaware的值，可以看出来为false的时候就拿不到localname，此时uri也是空字符串的。
![](https://img2020.cnblogs.com/blog/1550387/202012/1550387-20201220201404729-128091587.png)

    
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
      
















