package com.axellience.vuegwt.template.parser.context;

import com.axellience.vuegwt.client.component.VueComponent;
import com.axellience.vuegwt.client.jsnative.jstypes.JsArray;
import com.axellience.vuegwt.client.component.template.TemplateExpressionKind;
import com.axellience.vuegwt.jsr69.GenerationUtil;
import com.axellience.vuegwt.jsr69.component.annotations.Computed;
import com.axellience.vuegwt.template.parser.InvalidExpressionException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JField;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.dom.client.NativeEvent;
import org.jsoup.nodes.Node;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Adrien Baron
 */
public class TemplateParserContext
{
    public static final String CONTEXT_PREFIX = "VUE_GWT_CTX_";
    private final ContextLayer rootContext;
    private final Deque<ContextLayer> contextLayers = new ArrayDeque<>();
    private final JClassType vueComponentClass;
    private int contextId = 0;

    private Map<String, String> classNameToFullyQualifiedName = new HashMap<>();

    private Node currentNode;
    private String currentExpressionReturnType;
    private TemplateExpressionKind currentExpressionKind;

    public TemplateParserContext(JClassType vueComponentClass)
    {
        this.vueComponentClass = vueComponentClass;

        // Add some useful imports
        this.addImport(NativeEvent.class.getCanonicalName());
        this.addImport(JsArray.class.getCanonicalName());

        // Init root context
        this.rootContext = new ContextLayer("");

        this.rootContext.addVariable(String.class, "_uid");
        processVueComponentClass(vueComponentClass);
        this.contextLayers.add(this.rootContext);
    }

    private void processVueComponentClass(JClassType vueComponentClass)
    {
        if (vueComponentClass == null || vueComponentClass
            .getQualifiedSourceName()
            .equals(VueComponent.class.getCanonicalName()))
            return;

        for (JField jField : vueComponentClass.getFields())
        {
            this.rootContext.addVariable(jField);
        }
        for (JMethod jMethod : vueComponentClass.getMethods())
        {
            Computed computed = jMethod.getAnnotation(Computed.class);
            if (computed == null)
                continue;

            String name = GenerationUtil.getComputedPropertyName(computed, jMethod.getName());
            this.rootContext.addVariable(jMethod.getReturnType().getQualifiedSourceName(), name);
        }

        processVueComponentClass(vueComponentClass.getSuperclass());
    }

    public void addContextLayer()
    {
        contextLayers.push(new ContextLayer(CONTEXT_PREFIX + contextId + "_"));
        contextId++;
    }

    public ContextLayer popContextLayer()
    {
        return contextLayers.pop();
    }

    public boolean isInContextLayer()
    {
        return contextLayers.size() > 1;
    }

    public LocalVariableInfo addLocalVariable(String typeQualifiedName, String name)
    {
        return contextLayers.getFirst().addLocalVariable(typeQualifiedName, name);
    }

    public VariableInfo findVariableInContext(String name)
    {
        for (ContextLayer contextLayer : contextLayers)
        {
            if (contextLayer.hasVariable(name))
                return contextLayer.getVariableInfo(name);
        }

        throw new InvalidExpressionException("Couldn't find the variable: " + name);
    }

    public JClassType getVueComponentClass()
    {
        return vueComponentClass;
    }

    public Node getCurrentNode()
    {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode)
    {
        this.currentNode = currentNode;
    }

    public String getCurrentExpressionReturnType()
    {
        return currentExpressionReturnType;
    }

    public void setCurrentExpressionReturnType(String currentExpressionReturnType)
    {
        this.currentExpressionReturnType = currentExpressionReturnType;
    }

    public TemplateExpressionKind getCurrentExpressionKind()
    {
        return currentExpressionKind;
    }

    public void setCurrentExpressionKind(TemplateExpressionKind currentExpressionKind)
    {
        this.currentExpressionKind = currentExpressionKind;
    }

    public void addImport(String fullyQualifiedName)
    {
        String[] importSplit = fullyQualifiedName.split("\\.");
        String className = importSplit[importSplit.length - 1];

        classNameToFullyQualifiedName.put(className, fullyQualifiedName);
    }

    public String getFullyQualifiedNameForClassName(String className)
    {
        if (!classNameToFullyQualifiedName.containsKey(className))
            return className;

        return classNameToFullyQualifiedName.get(className);
    }

    public boolean hasImport(String className)
    {
        return classNameToFullyQualifiedName.containsKey(className);
    }

    public void addRootVariable(String type, String name)
    {
        this.rootContext.addVariable(type, name);
    }
}
