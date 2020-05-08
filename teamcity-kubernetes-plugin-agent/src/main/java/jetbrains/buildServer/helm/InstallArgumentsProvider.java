/*
 * Copyright 2000-2020 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.helm;

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static jetbrains.buildServer.helm.HelmConstants.*;

/**
 * Created by Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com) on 18.10.17.
 */
public class InstallArgumentsProvider implements HelmCommandArgumentsProvider {
    @NotNull
    @Override
    public String getCommandId() {
        return HELM_INSTALL_COMMAND;
    }

    @NotNull
    @Override
    public List<String> getArguments(@NotNull Map<String, String> runnerParameters) {
        String chart = runnerParameters.get(CHART);
        String additionalFlags = runnerParameters.get(ADDITIONAL_FLAGS);
        return StringUtil.splitCommandArgumentsAndUnquote(String.format("%s %s %s", HELM_INSTALL_COMMAND, StringUtil.emptyIfNull(chart), StringUtil.emptyIfNull(additionalFlags)));
    }
}
