/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks.compile.incremental.recomp;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import java.io.File;
import java.util.Collection;

public class GroovySourceToNameConverter extends LocationBasedSourceToNameConverter {
    private final Multimap<File, String> sourceClassesMapping;

    public GroovySourceToNameConverter(CompilationSourceDirs sourceDirs, Multimap<File, String> sourceClassesMapping) {
        super(sourceDirs, "groovy");
        this.sourceClassesMapping = sourceClassesMapping;
    }

    @Override
    public Collection<String> getClassNames(File groovySourceFile) {
        Collection<String> classes = sourceClassesMapping.get(groovySourceFile);
        if(classes.isEmpty()) {
            // new files
            return super.getClassNames(groovySourceFile);
        }else {
            return ImmutableSet.copyOf(classes);
        }
    }
}
