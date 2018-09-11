# ToySpring
Toy project mock Spring Framework

[toc]

最近准备秋招，看到网上看到有人仿写过Spring IOC和AOP，为了更深一步理解Spring IOC和AOP的原理。决定参考一些资料和代码，自己动手实现一个简单的IOC和AOP，并实现如下功能：

1. 根据xml配置文件加载相关bean
2. 对BeanPostProcessor类型的bean提供支持
3. 对BeanFactoryAware类型的bean提供支持
4. 实现基于JDK动态代理的AOP
5. 整合IOC和AOP，使二者更好的协同工作

## 简单IOC

先从简单的 IOC 容器实现开始，最简单的 IOC 容器只需4步即可实现，如下：

1. 加载 xml 配置文件，遍历其中的标签
2. 获取标签中的 id 和 class 属性，加载 class 属性对应的类，并创建 bean
3. 遍历标签中的标签，获取属性值，并将属性值填充到 bean 中
4. 将 bean 注册到 bean 容器中

### 代码结构

```java
SimpleIOC     // IOC 的实现类，实现了上面所说的4个步骤
SimpleIOCTest    // IOC 的测试类
Car           // IOC 测试使用的 bean
Wheel         // 同上 
ioc.xml       // bean 配置文件
```

- SimpleIOC中主要的加载xml文件的代码如下:

```java
/**
* 简单的根据xml文件创建单例的bean，并进行注入，要求xml文件中按依赖顺序被依赖的类在前面，且不存在循环依赖
* @param location
* @throws Exception
*/
private void loadBeans(String location) throws Exception{
    // 1. 加载xml配置文件
    InputStream is = new FileInputStream(location);
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    DocumentBuilder documentBuilder = factory.newDocumentBuilder();
    Document document = documentBuilder.parse(is);
    Element root = document.getDocumentElement();
    NodeList beanNodes = root.getChildNodes();

    // 2. 遍历 <bean> 标签
    for (int i = 0; i < beanNodes.getLength(); ++i){
        Node beanNode = beanNodes.item(i);
        if (beanNode instanceof Element){
            Element beanElement = (Element) beanNode;
            String id = beanElement.getAttribute("id");
            String className = beanElement.getAttribute("class");

            // 2.1 加载beanClass并初始化
            Class beanClass = null;
            try {
                beanClass = Class.forName(className);
            }catch (ClassNotFoundException e){
                e.printStackTrace();
                return;
            }

            // 2.2 创建bean
            Object bean = beanClass.newInstance();

            // 2.3 遍历<property>标签，注入值
            NodeList propertyNodes = beanElement.getElementsByTagName("property");
            for (int j = 0; j < propertyNodes.getLength(); ++j){
                Node propertyNode = propertyNodes.item(j);
                if (propertyNode instanceof Element){
                    Element propertyElement = (Element) propertyNode;
                    String name = propertyElement.getAttribute("name");
                    String value = propertyElement.getAttribute("value");

                    // 利用反射将bean相关字段的访问权限设为可访问
                    Field declaredField = bean.getClass().getDeclaredField(name);
                    declaredField.setAccessible(true);

                    if (value != null && !value.isEmpty()){
                        declaredField.set(bean, value);
                    }else {
                        String ref = propertyElement.getAttribute("ref");

                        if(ref == null || ref.isEmpty()){
                            throw new IllegalArgumentException("Ref object has'nt been creanted");
                        }

                        declaredField.set(bean, getBean(ref));
                    }

                }
            }
			
            // 2.4 将创建好的bean注册到容器中
            registerBean(id, bean);
        }
    }
}
```

