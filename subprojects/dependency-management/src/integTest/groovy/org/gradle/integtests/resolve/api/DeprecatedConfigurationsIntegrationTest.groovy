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

package org.gradle.integtests.resolve.api

import org.gradle.integtests.fixtures.AbstractIntegrationSpec
import org.gradle.util.GradleVersion

class DeprecatedConfigurationsIntegrationTest extends AbstractIntegrationSpec {
    def nextMajor = GradleVersion.current().nextMajor.version

    def setup() {
        executer.expectDeprecationWarning()

        mavenRepo.module("module", "foo", '1.0').publish()

        buildFile << """
            repositories {
                maven { url "${mavenRepo.uri}" }
            }
            allprojects {
                configurations {
                    implementation
                    compile.deprecateForDeclaration("implementation")
                    compileOnly.deprecateForConsumption("compileElements")
                    compileOnly.deprecateForResolution("compileClasspath")
                    apiElements {
                        canBeConsumed = true
                        canBeResolved = false
                        extendsFrom compile
                        extendsFrom compileOnly
                        extendsFrom implementation
                    }
                    compileClasspath {
                        canBeConsumed = false
                        canBeResolved = true
                        extendsFrom compile
                        extendsFrom compileOnly
                        extendsFrom implementation
                    }
                }
            }
        """
    }

    def "warn if a dependency is declared on a deprecated configuration"() {
        given:
        buildFile << """
            dependencies {
                compile 'module:foo:1.0'
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The compile configuration has been deprecated for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use the implementation configuration instead."
    }

    def "warn if a dependency is declared on a configuration meant for consumption only"() {
        given:
        buildFile << """
            dependencies {
                apiElements 'module:foo:1.0'
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The apiElements configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if a dependency is declared on a configuration meant for resolution only"() {
        given:
        buildFile << """
            dependencies {
                compileClasspath 'module:foo:1.0'
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The compileClasspath configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if a dependency constraint is declared on a deprecated configuration"() {
        given:
        buildFile << """
            dependencies {
                constraints {
                    compile 'module:foo:1.0'
                }
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The compile configuration has been deprecated for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use the implementation configuration instead."
    }
    def "warn if a dependency constraint is declared on a configuration meant for consumption only"() {
        given:
        buildFile << """
            dependencies {
                constraints {
                    apiElements 'module:foo:1.0'
                }
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The apiElements configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if a dependency constraint is declared on a configuration meant for resolution only"() {
        given:
        buildFile << """
            dependencies {
                constraints {
                    compileClasspath 'module:foo:1.0'
                }
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The compileClasspath configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if an artifact is declared on a configuration meant for consumption only"() {
        given:
        buildFile << """
            artifacts {
                apiElements file('some.jar')
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The apiElements configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if an artifact is declared on a configuration meant for resolution only"() {
        given:
        buildFile << """
            artifacts {
                compileClasspath file('some.jar')
            }
        """

        when:
        succeeds 'help'

        then:
        outputContains "The compileClasspath configuration is not suitable for dependency declaration. This will fail with an error in Gradle $nextMajor. Please use/define another configuration with both canBeResolved and canBeConsumed set to false."
    }

    def "warn if a deprecated configuration is resolved"() {
        given:
        buildFile << """
            task resolve {
                doLast {
                    configurations.compileOnly.files
                }
            }
        """

        when:
        succeeds 'resolve'

        then:
        outputContains "The compileOnly configuration has been deprecated for resolution. This will fail with an error in Gradle $nextMajor. Please resolve the compileClasspath configuration instead."
    }

    def "warn if a deprecated project configuration is consumed"() {
        given:
        settingsFile << "include 'a', 'b'"
        buildFile << """
            project(':b') {
                dependencies {
                    implementation project(path: ':a', configuration: 'compileOnly')
                }
                
                task resolve {
                    doLast {
                        configurations.compileClasspath.files
                    }
                }
            }
        """

        when:
        succeeds ':b:resolve'

        then:
        outputContains "The compileOnly configuration has been deprecated for consumption. This will fail with an error in Gradle $nextMajor. Please use attributes to consume the compileElements configuration instead."
    }

    def "warn if a deprecated project configuration is consumed directly"() {
        // this is testing legacy code that we can/should probably get rid of
        given:
        settingsFile << "include 'a', 'b'"
        buildFile << """
            project(':b') {
                dependencies {
                    implementation project(path: ':a', configuration: 'compileOnly')
                }
                
                task resolve {
                    doLast {
                        configurations.implementation.dependencies[0].resolve()
                    }
                }
            }
        """

        when:
        succeeds ':b:resolve', ':b:dependencies'

        then:
        outputContains "The compileOnly configuration has been deprecated for consumption. This will fail with an error in Gradle $nextMajor. Please use attributes to consume the compileElements configuration instead."
    }
}
