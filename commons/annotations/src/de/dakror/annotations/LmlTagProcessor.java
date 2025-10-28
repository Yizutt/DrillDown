/*******************************************************************************
 * Copyright 2018 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.dakror.annotations;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject;

import de.dakror.common.libgdx.lml.CustomTag;
import de.dakror.common.libgdx.lml.LmlTag;

/**
 * @author Maximilian Stark | Dakror
 */
@SupportedAnnotationTypes(value = { "de.dakror.common.libgdx.lml.LmlTag" })
@SupportedSourceVersion(value = SourceVersion.RELEASE_7)
public class LmlTagProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final TypeMirror tagI = processingEnv.getElementUtils().getTypeElement(CustomTag.class.getName()).asType();

        for (TypeElement annotation : annotations) {
            try {
                JavaFileObject file = processingEnv.getFiler().createSourceFile("de.dakror.gen.CustomTagRegistrator");
                try (PrintWriter pw = new PrintWriter(file.openWriter())) {
                    pw.println("package de.dakror.gen;");
                    pw.println("import com.github.czyzby.lml.util.LmlParserBuilder;");
                    pw.println("import de.dakror.common.libgdx.lml.CustomTagProvider;");
                    pw.println("public class CustomTagRegistrator {");
                    pw.println("    public static void addCustomTags(LmlParserBuilder lml) {");

                    ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(annotation))
                            .stream()
                            .filter(new Predicate<TypeElement>() {
                                @Override
                                public boolean test(TypeElement x) {
                                    return x.getKind() == ElementKind.CLASS
                                            && x.getInterfaces().contains(tagI);
                                }
                            }).forEach(new Consumer<TypeElement>() {
                                @Override
                                public void accept(TypeElement x) {
                                    pw.println("         lml.tag(new CustomTagProvider(" + x.getQualifiedName().toString() + ".class), \"" + x.getAnnotation(LmlTag.class).tagName() + "\");");
                                }
                            });

                    pw.println("    }");
                    pw.println("}");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

}
