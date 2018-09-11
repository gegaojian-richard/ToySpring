package com.gegaojian.main;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 加载 xml 配置文件，遍历其中的标签
 * 获取标签中的 id 和 class 属性，加载 class 属性对应的类，并创建 bean
 * 遍历标签中的标签，获取属性值，并将属性值填充到 bean 中
 * 将 bean 注册到 bean 容器中
 */
public class SimpleIOC {
    // 保存加载成功的bean
    private Map<String, Object> singletonBeans = new HashMap<>();

    public SimpleIOC(String location) throws Exception{
        loadBeans(location);
    }

    /**
     * 根据name获取Bean
     * @param name
     * @return
     */
    public Object getBean(String name){
        Object bean = singletonBeans.get(name);

        if (bean == null){
            throw new IllegalArgumentException("There is no bean with name " + name);
        }

        return bean;
    }

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

                registerBean(id, bean);
            }
        }
    }

    private void registerBean(String id, Object bean){
        singletonBeans.put(id, bean);
    }
}
