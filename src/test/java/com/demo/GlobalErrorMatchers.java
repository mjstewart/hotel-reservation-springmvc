package com.demo;

import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;

import java.util.Arrays;

import static org.springframework.test.util.AssertionErrors.assertTrue;
import static org.springframework.test.util.AssertionErrors.fail;

public class GlobalErrorMatchers {

    public static GlobalErrorMatchers globalErrorMatchers() {
        return new GlobalErrorMatchers();
    }

    /**
     * Usage
     * <p>
     * <p>Assumes a domain object {@code Guest} is bound to the {@code BindingResult}. This results in
     * {@code bindingResult.reject} registering a global error on the {@code Guest} with the error code 'exists'.
     *
     * <pre>
     *     bindingResult.reject("exists", "A guest with this name already exists");
     *
     *     globalErrorMatchers().hasGlobalErrorCode("guest", "exists")
     * </pre>
     *
     * @param object The name of the target object the global error was created for.
     * @param code The error code.
     */
    public ResultMatcher hasGlobalErrorCode(final String object, final String code) {
        return mvcResult -> {
            ModelAndView mav = getModelAndView(mvcResult);
            BindingResult result = getBindingResult(mav, object);

            boolean foundCode = result.getGlobalErrors().stream()
                    .flatMap(objectError -> Arrays.stream(objectError.getCodes()))
                    .anyMatch(errorCode -> errorCode.equals(code));

            assertTrue("Global error code '" + code + "' does not exist on object '" + object + "'", foundCode);
        };
    }

    private ModelAndView getModelAndView(MvcResult mvcResult) {
        ModelAndView mav = mvcResult.getModelAndView();
        if (mav == null) {
            fail("No ModelAndView found");
        }
        return mav;
    }

    private BindingResult getBindingResult(ModelAndView mav, String name) {
        BindingResult result = (BindingResult) mav.getModel().get(BindingResult.MODEL_KEY_PREFIX + name);
        if (result == null) {
            fail("No BindingResult for attribute: " + name);
        }
        return result;
    }
}
