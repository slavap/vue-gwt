package com.axellience.vuegwt.core.client.component.hooks;

import com.axellience.vuegwt.core.annotations.component.HookMethod;
import jsinterop.annotations.JsMethod;

/**
 * @author Adrien Baron
 */
public interface HasActivated
{
    @HookMethod
    @JsMethod
    void activated();
}
