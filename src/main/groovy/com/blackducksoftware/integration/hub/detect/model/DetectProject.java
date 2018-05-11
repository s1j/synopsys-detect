/**
 * hub-detect
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.detect.model;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.blackducksoftware.integration.hub.detect.DetectConfiguration;
import com.blackducksoftware.integration.hub.detect.codelocation.CodeLocationNameService;
import com.blackducksoftware.integration.hub.detect.util.BdioFileNamer;
import com.blackducksoftware.integration.hub.detect.util.DetectFileFinder;
import com.blackducksoftware.integration.hub.service.model.ProjectRequestBuilder;

public class DetectProject {
    private final Map<String, DetectCodeLocation> codeLocationNameMap = new HashMap<>();
    private final Map<String, String> codeLocationNameToBdioName = new HashMap<>();
    private final List<DetectCodeLocation> detectCodeLocations = new ArrayList<>();
    private final Set<BomToolType> failedBomTools = new HashSet<>();

    private String projectName;
    private String projectVersionName;
    private String codeLocationNamePrefix;
    private String codeLocationNameSuffix;

    /**
     * Only the DetectProjectManager should invoke this method.
     */
    public void setProjectDetails(final String projectName, final String projectVersionName, final String codeLocationNamePrefix, final String codeLocationNameSuffix) {
        this.projectName = projectName;
        this.projectVersionName = projectVersionName;
        this.codeLocationNamePrefix = codeLocationNamePrefix;
        this.codeLocationNameSuffix = codeLocationNameSuffix;
    }

    public void setProjectNameIfNotSet(final String projectName) {
        if (StringUtils.isBlank(this.projectName)) {
            this.projectName = projectName;
        }
    }

    public void setProjectVersionNameIfNotSet(final String projectVersionName) {
        if (StringUtils.isBlank(this.projectVersionName)) {
            this.projectVersionName = projectVersionName;
        }
    }

    public void addAllDetectCodeLocations(final List<DetectCodeLocation> detectCodeLocations) {
        detectCodeLocations
        .stream()
        .forEach(it -> addDetectCodeLocation(it));
    }

    public void addDetectCodeLocation(final DetectCodeLocation detectCodeLocation) {
        setProjectNameIfNotSet(detectCodeLocation.getBomToolProjectName());
        setProjectVersionNameIfNotSet(detectCodeLocation.getBomToolProjectVersionName());

        detectCodeLocations.add(detectCodeLocation);
    }

    public List<DetectCodeLocation> getDetectCodeLocations() {
        return detectCodeLocations;
    }

    public ProjectRequestBuilder createDefaultProjectRequestBuilder(final DetectConfiguration detectConfiguration) {
        final ProjectRequestBuilder builder = new ProjectRequestBuilder();
        builder.setProjectName(getProjectName());
        builder.setVersionName(getProjectVersionName());
        builder.setProjectLevelAdjustments(detectConfiguration.getProjectLevelMatchAdjustments());
        builder.setPhase(detectConfiguration.getProjectVersionPhase());
        builder.setDistribution(detectConfiguration.getProjectVersionDistribution());
        builder.setProjectTier(detectConfiguration.getProjectTier());
        builder.setReleaseComments(detectConfiguration.getProjectVersionNotes());

        return builder;
    }

    public void processDetectCodeLocations(final Logger logger, final DetectFileFinder detectFileFinder, final File sourcePath, final BdioFileNamer bdioFileNamer, final CodeLocationNameService codeLocationNameService) {
        for (final DetectCodeLocation detectCodeLocation : getDetectCodeLocations()) {
            if (detectCodeLocation.getDependencyGraph() == null) {
                logger.warn(String.format("Dependency graph is null for code location %s", detectCodeLocation.getSourcePath()));
                continue;
            }
            if (detectCodeLocation.getDependencyGraph().getRootDependencies().size() <= 0) {
                logger.warn(String.format("Could not find any dependencies for code location %s", detectCodeLocation.getSourcePath()));
            }

            final List<String> pieces = Arrays.asList(detectCodeLocation.getBomToolProjectExternalId().getExternalIdPieces());
            final String name = pieces.stream().collect(Collectors.joining("/"));
            // detectCodeLocation.getCodeLocationNameString(codeLocationNameService, codeLocationName);
            //final CodeLocationName codeLocationName = detectCodeLocation.getBomToolProjectExternalId().getExternalIdPieces()
            // detectCodeLocation.createCodeLocationName(codeLocationNameService, projectName, projectVersionName, getCodeLocationNamePrefix(), getCodeLocationNameSuffix());
            final Path path = new File(detectCodeLocation.getSourcePath()).toPath();
            final Path sourcePathPath = sourcePath.toPath();
            final String relative = sourcePathPath.relativize(path).toString();
            final String codeLocationNameString = createCommonName(relative, name, codeLocationNamePrefix, codeLocationNameSuffix, "bom", detectCodeLocation.getBomToolType().toString());

            if (codeLocationNameMap.containsKey(codeLocationNameString)) {
                failedBomTools.add(detectCodeLocation.getBomToolType());
                logger.error(String.format("Found duplicate Code Locations with the name: %s", codeLocationNameString));
            } else {
                codeLocationNameMap.put(codeLocationNameString, detectCodeLocation);
            }
        }
        final Set<String> bdioFileNames = new HashSet<>();
        for (final Map.Entry<String, DetectCodeLocation> codeLocationEntry : codeLocationNameMap.entrySet()) {
            final String codeLocationNameString = codeLocationEntry.getKey();
            final DetectCodeLocation detectCodeLocation = codeLocationEntry.getValue();

            final String filename = bdioFileNamer.generateShortenedFilename(detectCodeLocation.getBomToolType(), detectCodeLocation.getBomToolProjectExternalId());

            if (!bdioFileNames.add(filename)) {
                failedBomTools.add(detectCodeLocation.getBomToolType());
                logger.error(String.format("Found duplicate Bdio files with the name: %s", filename));
            } else {
                codeLocationNameToBdioName.put(codeLocationNameString, filename);
            }
        }
    }

    private String createCommonName(final String pathPiece, final String externalIdString, final String prefix, final String suffix, final String codeLocationType, final String bomToolType) {
        String name = String.format("%s/%s", pathPiece, externalIdString);
        if (StringUtils.isNotBlank(prefix)) {
            name = String.format("%s/%s", prefix, name);
        }
        if (StringUtils.isNotBlank(suffix)) {
            name = String.format("%s/%s", name, suffix);
        }

        String endPiece = codeLocationType;
        endPiece = String.format("%s/%s", bomToolType, endPiece);

        name = String.format("%s %s", name, endPiece);
        return name;
    }

    public Set<String> getCodeLocationNameStrings() {
        return codeLocationNameMap.keySet();
    }

    public DetectCodeLocation getDetectCodeLocation(final String codeLocationNameString) {
        return codeLocationNameMap.get(codeLocationNameString);
    }

    public String getBdioFilename(final String codeLocationNameString) {
        return codeLocationNameToBdioName.get(codeLocationNameString);
    }

    public Set<BomToolType> getFailedBomTools() {
        return failedBomTools;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectVersionName() {
        return projectVersionName;
    }

    public String getCodeLocationNamePrefix() {
        return codeLocationNamePrefix;
    }

    public String getCodeLocationNameSuffix() {
        return codeLocationNameSuffix;
    }

}
