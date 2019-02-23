package io.smallrye.lra.participant;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.LRAParticipant;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AnnotatedMethod;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.WithAnnotations;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class LRACDIExtension implements Extension {

    public void register(@Observes @WithAnnotations(LRAParticipant.class) ProcessAnnotatedType<?> type) throws MalformedURLException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        AnnotatedType<?> annotatedType = type.getAnnotatedType();
        System.out.println("XXXXXXXXXXXXXXXXXXX Found " + annotatedType.getJavaClass().getName());

        Map<String, Method> participantMethods = new HashMap<>();
        
        for (AnnotatedMethod method : annotatedType.getMethods()) {
            Method javaMethod = method.getJavaMember();
            if (method.isAnnotationPresent(Compensate.class)) {
                if (hasInvalidSignature(javaMethod, Compensate.class.getSimpleName())) {
                    return;
                }
                participantMethods.put("compensate", javaMethod);
            } else if (method.isAnnotationPresent(Complete.class)) {
                if (hasInvalidSignature(javaMethod, Complete.class.getSimpleName())) {
                    return;
                }
                participantMethods.put("complete", javaMethod);
            }
        }
        
        
        
    }

    private boolean hasInvalidSignature(Method method, String annotation) {
        Parameter[] parameters = method.getParameters();

        if (parameters.length > 2) {
            throw new IllegalArgumentException(annotation + " method cannot have more than 2 parameters");
        }

        for (Parameter p : parameters) {
            Class<?> type = p.getType();

            if (type.equals(URL.class)) {
                System.out.println("URL PARAM FOUND");
            } else if (type.equals(String.class)) {
                System.out.println("STRING PARAM FOUND");
            } else {
                throw new IllegalArgumentException(String.format("Unknown parameter type for %s method: %s", annotation, type));
            }


        }
        
        return false;
    }

}
