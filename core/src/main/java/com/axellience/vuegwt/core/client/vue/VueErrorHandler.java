package com.axellience.vuegwt.core.client.vue;

import com.axellience.vuegwt.core.client.component.IsVueComponent;
import elemental2.core.JsObject;
import jsinterop.annotations.JsFunction;

/**
 * @author Adrien Baron
 */
@JsFunction
@FunctionalInterface
public interface VueErrorHandler
{
    void action(JsObject err, IsVueComponent vue, String info);
}
