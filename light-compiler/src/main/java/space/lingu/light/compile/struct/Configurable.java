/*
 * Copyright (C) 2022 Lingu Light Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.lingu.light.compile.struct;

import space.lingu.light.Configurations;
import space.lingu.light.LightConfiguration;
import space.lingu.light.LightConfigurations;
import space.lingu.light.compile.JavaPoetClass;
import space.lingu.light.compile.coder.GenerateCodeBlock;

import javax.lang.model.element.Element;
import java.util.StringJoiner;

/**
 * @author RollW
 */
public interface Configurable {
    default Configurations getConfigurations() {
        return Configurations.empty();
    }

    static Configurations createFrom(LightConfiguration[] initial, Element element) {
        Configurations configurations = Configurable.createFrom(initial);
        LightConfiguration lightConfigurationAnno = element.getAnnotation(LightConfiguration.class);
        if (lightConfigurationAnno != null) {
            configurations = configurations.plus(
                    Configurations.createFrom(lightConfigurationAnno)
            );
        }
        LightConfigurations lightConfigurationsAnno = element.getAnnotation(LightConfigurations.class);
        if (lightConfigurationsAnno != null) {
            Configurations fromRepeatableAnno = Configurable.createFrom(lightConfigurationsAnno.value());
            return configurations.plus(fromRepeatableAnno);
        }
        return configurations;
    }

    static Configurations createFrom(LightConfiguration[] lightConfigurations) {
        return Configurations.createFrom(lightConfigurations);
    }

    static void newConfiguration(Configurations.Configuration configuration,
                                 String varName,
                                 GenerateCodeBlock block) {
        block.builder().addStatement("$T $L = new $T($S, $S)",
                JavaPoetClass.CONFIGURATION, varName, JavaPoetClass.CONFIGURATION,
                configuration.key, configuration.value);
    }

    static String writeConfiguration(Configurable configurable,
                                     String prefix,
                                     GenerateCodeBlock block) {
        final String varName = block.getTempVar("_configs" + prefix);
        StringJoiner varNameJoiner = new StringJoiner(", ");

        for (Configurations.Configuration configuration : configurable.getConfigurations().configurations()) {
            String confVarName = block.getTempVar("_configuration_tmp_" + prefix);
            Configurable.newConfiguration(configuration, confVarName, block);
            varNameJoiner.add(confVarName);
        }
        block.builder().addStatement("$T $L = $T.createFrom($L)",
                JavaPoetClass.CONFIGURATIONS, varName,
                JavaPoetClass.CONFIGURATIONS,
                varNameJoiner.toString()
        );
        return varName;
    }


}
