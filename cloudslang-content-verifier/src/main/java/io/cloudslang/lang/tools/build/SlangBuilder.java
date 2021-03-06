/*
 * (c) Copyright 2014 Hewlett-Packard Development Company, L.P.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License v2.0 which accompany this distribution.
 *
 * The Apache License is available at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 */
package io.cloudslang.lang.tools.build;

import io.cloudslang.lang.compiler.modeller.model.Executable;
import io.cloudslang.lang.entities.CompilationArtifact;
import io.cloudslang.lang.tools.build.tester.RunTestsResults;
import io.cloudslang.lang.tools.build.tester.SlangTestRunner;
import io.cloudslang.lang.tools.build.tester.parse.SlangTestCase;
import io.cloudslang.lang.tools.build.verifier.SlangContentVerifier;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/*
 * Created by stoneo on 2/9/2015.
 */
/**
 * Verifies all files with extensions: .sl, .sl.yaml or .sl.yml in a given directory are valid
 */
@Component
public class SlangBuilder {

    @Autowired
    private SlangContentVerifier slangContentVerifier;

    @Autowired
    private SlangTestRunner slangTestRunner;

    private final static Logger log = Logger.getLogger(SlangBuilder.class);

    public SlangBuildResults buildSlangContent(String projectPath, String contentPath, String testsPath, List<String> testSuits){

        String projectName = FilenameUtils.getName(projectPath);
        log.info("");
        log.info("------------------------------------------------------------");
        log.info("Building project: " + projectName);
        log.info("------------------------------------------------------------");

        log.info("");
        log.info("--- compiling sources ---");
        Map<String, Executable> slangModels =
                slangContentVerifier.createModelsAndValidate(contentPath);

        Map<String, CompilationArtifact> compiledSources = compileModels(slangModels);

        RunTestsResults runTestsResults = new RunTestsResults();
        if (StringUtils.isNotBlank(testsPath) && new File(testsPath).isDirectory()) {
            runTestsResults = runTests(slangModels, projectPath, testsPath, testSuits);
        }

        return new SlangBuildResults(compiledSources.size(), runTestsResults);
    }

    /**
     * Compiles all CloudSlang models
     * @return the number of valid CloudSlang files in the given directory
     */
    private Map<String, CompilationArtifact> compileModels(Map<String, Executable> slangModels){
        Map<String, CompilationArtifact> compiledSlangFiles =
                slangContentVerifier.compileSlangModels(slangModels);

        if(compiledSlangFiles.size() != slangModels.size()){
            throw new RuntimeException("Some Slang files were not compiled.\n" +
                    "Found: " + slangModels.size() + " slang models, but managed to compile only: "
                    + compiledSlangFiles.size());
        }

        log.info("Successfully finished Compilation of: " + compiledSlangFiles.size() + " Slang files");
        return compiledSlangFiles;
    }

    private RunTestsResults runTests(Map<String, Executable> contentSlangModels,
                          String projectPath, String testsPath, List<String> testSuites){
        log.info("");
        log.info("--- compiling tests sources ---");
        // Compile all slang test flows under the test directory
        Map<String, Executable> testFlowModels = slangContentVerifier.createModelsAndValidate(testsPath);
        // Add also all of the slang models of the content in order to allow for compilation of the test flows
        testFlowModels.putAll(contentSlangModels);
        // Compiling all the test flows
        Map<String, CompilationArtifact> compiledFlows = slangContentVerifier.compileSlangModels(testFlowModels);

        Map<String, SlangTestCase> testCases = slangTestRunner.createTestCases(testsPath);

        log.info("");
        log.info("--- running tests ---");
        log.info("Going to run " + testCases.size() + " tests");
        return slangTestRunner.runAllTests(projectPath, testCases, compiledFlows, testSuites);
    }


}
