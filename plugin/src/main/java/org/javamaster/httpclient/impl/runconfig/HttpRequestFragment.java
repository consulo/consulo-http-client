/*
 * Copyright 2013-2026 consulo.io
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
package org.javamaster.httpclient.impl.runconfig;

import consulo.disposer.Disposable;
import consulo.execution.configuration.fragment.RunConfigurationFragment;
import consulo.execution.configuration.fragment.RunConfigurationFragmentEditor;
import consulo.execution.configuration.fragment.RunConfigurationFragmentLayout;
import consulo.fileChooser.FileChooserDescriptorFactory;
import consulo.fileChooser.FileChooserTextBoxBuilder;
import consulo.httpClient.localize.HttpClientLocalize;
import consulo.localize.LocalizeValue;
import consulo.project.Project;
import consulo.ui.ComboBox;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.model.FlatDataModel;
import consulo.ui.model.MutableFlatDataModel;
import consulo.util.lang.StringUtil;
import org.javamaster.httpclient.HttpFileType;
import org.javamaster.httpclient.env.EnvFileService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * The {@code .http} file and the environment an {@link HttpRunConfiguration} runs with; the environments are the ones defined
 * next to the file.
 *
 * @author VISTALL
 * @since 2026-10-24
 */
class HttpRequestFragment implements RunConfigurationFragment<HttpRunConfiguration> {
    private static final String NO_ENV = "";

    @Override
    public String getId() {
        return "httpRequest";
    }

    @Override
    public LocalizeValue getName() {
        return HttpClientLocalize.runConfigurationFragmentRequest();
    }

    @Override
    public boolean isRemovable() {
        return false;
    }

    @RequiredUIAccess
    @Override
    public RunConfigurationFragmentEditor<HttpRunConfiguration> createEditor(Project project, Disposable parent) {
        MutableFlatDataModel<String> envModel = FlatDataModel.of(List.of(NO_ENV));
        ComboBox<String> envBox = ComboBox.create(envModel);
        envBox.setRenderer((presentation, item) -> {
            String env = item.getValue();
            if (env != null) {
                presentation.append(env.isEmpty() ? HttpClientLocalize.noEnv() : LocalizeValue.of(env));
            }
        });

        FileChooserTextBoxBuilder httpFileBuilder = FileChooserTextBoxBuilder.create(project);
        httpFileBuilder.fileChooserDescriptor(FileChooserDescriptorFactory.createSingleFileDescriptor(HttpFileType.INSTANCE));
        httpFileBuilder.uiDisposable(parent);
        FileChooserTextBoxBuilder.Controller httpFileBox = httpFileBuilder.build();

        return new RunConfigurationFragmentEditor<>() {
            @RequiredUIAccess
            @Override
            public void addRows(RunConfigurationFragmentLayout layout) {
                layout.addLabeled(HttpClientLocalize.httpFile(), httpFileBox.getComponent());
                layout.addLabeled(HttpClientLocalize.env(), envBox);
            }

            @RequiredUIAccess
            @Override
            public void reset(HttpRunConfiguration target) {
                String httpFilePath = target.getHttpFilePath();
                httpFileBox.setValue(httpFilePath, false);

                File parentFile = new File(httpFilePath).getParentFile();
                List<String> envs = new ArrayList<>();
                envs.add(NO_ENV);
                envs.addAll(EnvFileService.getService(project).getPresetEnvSet(parentFile == null ? null : parentFile.getAbsolutePath()));
                String env = target.getEnv();
                if (!env.isEmpty() && !envs.contains(env)) {
                    envs.add(env);
                }
                envModel.replaceAll(envs);
                envBox.setValue(env);
            }

            @RequiredUIAccess
            @Override
            public void apply(HttpRunConfiguration target) {
                target.setHttpFilePath(StringUtil.notNullize(httpFileBox.getValue()).replace('\\', '/'));
                target.setEnv(StringUtil.notNullize(envBox.getValue()));
            }
        };
    }
}
