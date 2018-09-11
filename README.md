# ToySpring

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
Main          // 在主类中使用简单IOC
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

## 简单AOP

使用JDK动态代理为目标对象创建代理对象，同时将切面逻辑织入到代理对象中。

1. 定义一个包含切面逻辑的对象，这里假设叫 logMethodInvocation
2. 定义一个 Advice 对象（实现了 InvocationHandler 接口），并将上面的 logMethodInvocation 和 目标对象传入
3. 将上面的 Adivce 对象和目标对象传给 JDK 动态代理方法，为目标对象生成代理

### 代码结构

```
MethodInvocation 接口  // 一个函数式接口，用来传入切面逻辑
Advice 接口        // 继承了 InvocationHandler 接口，是Advices的根接口
BeforeAdvice 类    // 前置增强，实现了 Advice 接口，传入目标对象和切面逻辑
SimpleAOP 类       // 生成代理类，并将Advice织入到代理类中，可以理解为一个创建代理类的工厂
Main      		   // 在主类中使用简单AOP
HelloService 接口   // 目标对象接口
HelloServiceImpl   // 目标对象
```

- BeforeAdvice的实现代码：

```java
public class BeforeAdvice implements Advice{

    // 目标对象
    private Object bean;

    // 切面逻辑 ()->void
    private MethodInvocation methodInvocation;

    public BeforeAdvice(Object bean, MethodInvocation methodInvocation) {
        this.bean = bean;
        this.methodInvocation = methodInvocation;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        methodInvocation.invoke();
        return method.invoke(bean, args);
    }
}
```

- SimpleAOP的实现代码

```java
/**
 * 为指定的目标类生成代理类，并将指定的增强织入到代理类中
 */
public class SimpleAOP {
    public static Object getProxy(Object bean, Advice advice){
        return Proxy.newProxyInstance(SimpleAOP.class.getClassLoader(), bean.getClass().getInterfaces(), advice);
    }
}
```

## 使用简单IOC和AOP

- 主类：

```java
public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws Exception{

	    // 测试IOC
        String location = "/Users/gegaojian/Documents/JavaProjects/ToySpring/src/com/gegaojian/main/ioc.xml";
        SimpleIOC beanFactory = new SimpleIOC(location);
        Wheel wheel = (Wheel) beanFactory.getBean("wheel");
        logger.info(wheel.toString());
        Car car = (Car) beanFactory.getBean("car");
        logger.info(car.toString());

        // 测试AOP
        HelloService helloService = new HelloWorldImpl();
        // 1. 创建一个增强
        Advice beforeAdvice = new BeforeAdvice(helloService, ()->System.out.println("Log"));
        // 2. 生成代理对象并织入增强
        HelloService helloServiceImplProxy = (HelloService) SimpleAOP.getProxy(helloService, beforeAdvice);
        // 3. 通过代理类来访问接口
        helloServiceImplProxy.sayHelloWorld();
    }
}
```

