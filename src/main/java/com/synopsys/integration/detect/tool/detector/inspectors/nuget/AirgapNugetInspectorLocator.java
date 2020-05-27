/**
 * synopsys-detect
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.detect.tool.detector.inspectors.nuget;

import java.io.File;
import java.util.Optional;

import com.synopsys.integration.detect.workflow.airgap.AirGapInspectorPaths;
import com.synopsys.integration.detectable.detectable.exception.DetectableException;

public class AirgapNugetInspectorLocator implements NugetInspectorLocator {
    private final AirGapInspectorPaths airGapInspectorPaths;

    public AirgapNugetInspectorLocator(AirGapInspectorPaths airGapInspectorPaths) {
        this.airGapInspectorPaths = airGapInspectorPaths;
    }

    @Override
    public File locateDotnet3Inspector() throws DetectableException {
        return locateInspector(INSPECTOR_DIR_DOTNET3);
    }

    @Override
    public File locateDotnetInspector() throws DetectableException {
        return locateInspector(INSPECTOR_DIR_DOTNET);
    }

    @Override
    public File locateExeInspector() throws DetectableException {
        return locateInspector(INSPECTOR_DIR_CLASSIC);
    }

    private File locateInspector(String childName) throws DetectableException {
        Optional<File> nugetAirGapPath = airGapInspectorPaths.getNugetInspectorAirGapFile();
        if (nugetAirGapPath.isPresent()) {
            return new File(nugetAirGapPath.get(), childName);
        }
        throw new DetectableException("Could not get the nuget air gap path");
    }
}
