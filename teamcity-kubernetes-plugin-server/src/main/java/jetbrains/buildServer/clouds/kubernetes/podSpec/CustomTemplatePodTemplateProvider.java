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

package jetbrains.buildServer.clouds.kubernetes.podSpec;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.client.utils.Serialization;
import jetbrains.buildServer.Used;
import jetbrains.buildServer.clouds.CloudInstanceUserData;
import jetbrains.buildServer.clouds.kubernetes.*;
import jetbrains.buildServer.serverSide.ServerSettings;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;

/**
 * Created by ekoshkin (koshkinev@gmail.com) on 15.06.17.
 */
public class CustomTemplatePodTemplateProvider extends AbstractPodTemplateProvider {
    private final ServerSettings myServerSettings;

    public CustomTemplatePodTemplateProvider(ServerSettings serverSettings) {
        myServerSettings = serverSettings;
    }

    @NotNull
    @Override
    public String getId() {
        return "custom-pod-template";
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Use custom pod template";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @NotNull
    @Override
    public Pod getPodTemplate(@NotNull String kubeInstanceName,
                              @NotNull CloudInstanceUserData cloudInstanceUserData,
                              @NotNull KubeCloudImage kubeCloudImage,
                              @NotNull KubeCloudClientParameters kubeClientParams) {

        String podTemplate = kubeCloudImage.getPodTemplate();
        return getPodTemplateInternal(cloudInstanceUserData, kubeCloudImage.getId(), kubeClientParams.getNamespace(), kubeInstanceName, podTemplate);
    }

    @Used("tests")
    /* package local for tests */ Pod getPodTemplateInternal(@NotNull final CloudInstanceUserData cloudInstanceUserData,
                                      @NotNull final String imageId,
                                      @NotNull final String namespace,
                                      final String instanceName,
                                      String spec) {
        spec = spec.replaceAll("%instance\\.id%", instanceName);

        if (StringUtil.isEmpty(spec)) {
            throw new KubeCloudException("Custom pod template spec is not specified for image " + imageId);
        }

        final PodTemplateSpec podTemplateSpec = Serialization.unmarshal(
          new ByteArrayInputStream(spec.getBytes()),
          PodTemplateSpec.class
        );

        return patchedPodTemplateSpec(podTemplateSpec,
                                      instanceName,
                                      namespace,
                                      myServerSettings.getServerUUID(),
                                      imageId,
                                      cloudInstanceUserData);
    }

    @Nullable
    @Override
    public PersistentVolumeClaim getPVC(@NotNull final String instanceName,
                                        @NotNull final KubeCloudImage kubeCloudImage) {
        String pvcTemplate = kubeCloudImage.getPVCTemplate();
        if (StringUtil.isEmpty(pvcTemplate)){
            return null;
        }
        pvcTemplate = pvcTemplate.replaceAll("%instance\\.id%", instanceName);
        final PersistentVolumeClaim pvc = Serialization.unmarshal(
          new ByteArrayInputStream(pvcTemplate.getBytes()),
          PersistentVolumeClaim.class
        );

        return pvc;
    }
}
