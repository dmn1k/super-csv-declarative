/*
 * Copyright 2007 Kasper B. Graversen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.dmn1k.supercsv.internal.cells;

import com.github.dmn1k.supercsv.model.BeanDescriptor;
import com.github.dmn1k.supercsv.internal.util.Form;
import com.github.dmn1k.supercsv.internal.util.ReflectionUtilsExt;
import com.github.dmn1k.supercsv.io.declarative.CellProcessorAnnotationDescriptor;
import com.github.dmn1k.supercsv.model.CellProcessorFactory;
import com.github.dmn1k.supercsv.model.ProcessingMetadata;
import com.github.dmn1k.supercsv.model.DeclarativeCellProcessorProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.cellprocessor.CellProcessorAdaptor;
import org.supercsv.cellprocessor.ift.BoolCellProcessor;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.cellprocessor.ift.DateCellProcessor;
import org.supercsv.cellprocessor.ift.DoubleCellProcessor;
import org.supercsv.cellprocessor.ift.LongCellProcessor;
import org.supercsv.cellprocessor.ift.StringCellProcessor;
import org.supercsv.exception.SuperCsvReflectionException;
import org.supercsv.util.CsvContext;

/**
 * Extracts all cellprocessor from all fields of the provided class
 *
 * @since 2.5
 * @author Dominik Schlosser
 */
final class BeanCellProcessorExtractor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanCellProcessorExtractor.class);
    
    private BeanCellProcessorExtractor() {
        // no instances allowed
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static CellProcessor createCellProcessorFor(BeanDescriptor beanDescriptor, Field field, String context) {
        List<Annotation> annotations = extractAnnotations(field);
        Collections.reverse(annotations);

        List<CellProcessorDefinition> factories = new ArrayList<>();

        for (Annotation annotation : annotations) {
            CellProcessorAnnotationDescriptor cellProcessorMarker = annotation
                    .annotationType().getAnnotation(CellProcessorAnnotationDescriptor.class);
            if (cellProcessorMarker != null && Arrays.asList(cellProcessorMarker.contexts()).contains(context)) {
                DeclarativeCellProcessorProvider provider = ReflectionUtilsExt.instantiateBean(cellProcessorMarker
                        .provider());
                if (!provider.getType().isAssignableFrom(annotation.getClass())) {
                    throw new SuperCsvReflectionException(
                            Form.at(
                                    "Provider declared in annotation of type '{}' cannot be used since accepted annotation-type is not compatible",
                                    annotation.getClass().getName()));
                }

                factories.add(new CellProcessorDefinition(provider.create(new ProcessingMetadata(annotation, field, beanDescriptor)), cellProcessorMarker));
            }
        }

        Collections.sort(factories, new OrderComparator());

        return buildProcessorChain(factories);
    }

    private static List<Annotation> extractAnnotations(Field field){
        List<Annotation> result = new ArrayList<>();
        for(Annotation annotation : field.getAnnotations()){
            Class<? extends Annotation> annotationType = annotation.annotationType();
            CellProcessorAnnotationDescriptor cellProcessorMarker = annotationType.getAnnotation(CellProcessorAnnotationDescriptor.class);
            if(cellProcessorMarker == null){
                Optional<Method> valueMethod = Arrays.asList(annotationType.getMethods()).stream()
                        .filter(m -> m.getName().equals("value"))
                        .filter(m -> Annotation[].class.isAssignableFrom(m.getReturnType()))
                        .findFirst();
                
                if(valueMethod.isPresent()){
                    try {
                        Annotation[] repeatedAnnotations = (Annotation[]) valueMethod.get().invoke(annotation);
                        result.addAll(Arrays.asList(repeatedAnnotations));
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        LOGGER.warn("Exception while trying to read repeatable annotations from {}: {}", annotationType.getClass().getName(), ex.getMessage());
                    }
                }
            } else {
                result.add(annotation);
            }
        }
        
        return result;
    }
    
    private static CellProcessor buildProcessorChain(List<CellProcessorDefinition> definitions) {
        CellProcessor root = new Transient();

        for (CellProcessorDefinition definition : definitions) {
            root = definition.getFactory().create(root);
        }
        return root;
    }

    private static final class OrderComparator implements Comparator<CellProcessorDefinition> {

        @Override
        public int compare(CellProcessorDefinition o1, CellProcessorDefinition o2) {
            return o2.getFactory().getOrder() - o1.getFactory().getOrder();
        }
    }

    private static class Transient extends CellProcessorAdaptor implements LongCellProcessor, DoubleCellProcessor,
            StringCellProcessor, DateCellProcessor, BoolCellProcessor {

        @Override
        public <T> T execute(Object value, CsvContext context) {
            return next.execute(value, context);
        }

    }

    private static class CellProcessorDefinition {

        private final CellProcessorFactory factory;
        private final CellProcessorAnnotationDescriptor descriptor;

        public CellProcessorDefinition(CellProcessorFactory factory, CellProcessorAnnotationDescriptor descriptor) {
            this.factory = factory;
            this.descriptor = descriptor;
        }

        public CellProcessorFactory getFactory() {
            return factory;
        }

        public CellProcessorAnnotationDescriptor getDescriptor() {
            return descriptor;
        }

    }
}
