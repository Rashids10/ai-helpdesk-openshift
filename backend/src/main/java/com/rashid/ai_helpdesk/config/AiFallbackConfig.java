package com.rashid.ai_helpdesk.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiFallbackConfig {

    @Bean
    ChatClient chatClient() {
        return (ChatClient) Proxy.newProxyInstance(
                ChatClient.class.getClassLoader(),
                new Class<?>[] { ChatClient.class },
                new DefaultProxyHandler("ChatClient"));
    }

    @Bean
    VectorStore vectorStore() {
        return new VectorStore() {
            @Override
            public void add(List<Document> documents) {
                // no-op fallback for local startup
            }

            @Override
            public void delete(List<String> idList) {
                // no-op fallback for local startup
            }

            @Override
            public void delete(Filter.Expression expression) {
                // no-op fallback for local startup
            }

            @Override
            public List<Document> similaritySearch(SearchRequest request) {
                return List.of();
            }

            @Override
            public String getName() {
                return "fallbackVectorStore";
            }

            @Override
            public <T> Optional<T> getNativeClient() {
                return Optional.empty();
            }
        };
    }

    private static final class DefaultProxyHandler implements InvocationHandler {

        private final String name;

        private DefaultProxyHandler(String name) {
            this.name = name;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            if ("toString".equals(methodName)) {
                return name + "FallbackProxy";
            }
            if ("hashCode".equals(methodName)) {
                return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
                return proxy == (args != null && args.length > 0 ? args[0] : null);
            }

            if (returnType == Void.TYPE) {
                return null;
            }
            if (returnType == Boolean.TYPE) {
                return false;
            }
            if (returnType == Byte.TYPE) {
                return (byte) 0;
            }
            if (returnType == Short.TYPE) {
                return (short) 0;
            }
            if (returnType == Integer.TYPE) {
                return 0;
            }
            if (returnType == Long.TYPE) {
                return 0L;
            }
            if (returnType == Float.TYPE) {
                return 0.0f;
            }
            if (returnType == Double.TYPE) {
                return 0.0d;
            }
            if (returnType == Character.TYPE) {
                return '\0';
            }
            if (returnType == String.class) {
                return "";
            }
            if (Optional.class.isAssignableFrom(returnType)) {
                return Optional.empty();
            }
            if (List.class.isAssignableFrom(returnType)) {
                return List.of();
            }
            if (Map.class.isAssignableFrom(returnType)) {
                return Collections.emptyMap();
            }
            if (returnType.isInterface()) {
                return Proxy.newProxyInstance(
                        returnType.getClassLoader(),
                        new Class<?>[] { returnType },
                        new DefaultProxyHandler(returnType.getSimpleName()));
            }

            return null;
        }
    }
}
