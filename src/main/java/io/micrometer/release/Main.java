/*
 * Copyright 2025 Broadcom.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micrometer.release;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // Env Vars

        // Single Project Post Release
        // GH_TOKEN
        // CHANGELOG_GENERATOR_VERSION
        // GITHUB_REPOSITORY
        // GITHUB_REF_NAME
        // PREVIOUS_REF_NAME
        // BSKY_IDENTIFIER
        // BSKY_HANDLE
        // SPRING_RELEASE_GCHAT_WEBHOOK_URL

        // Train Project Post Release
        // TRAIN_VERSIONS

        ProcessRunner processRunner = new ProcessRunner();
        PostReleaseWorkflow postReleaseWorkflow = newPostReleaseWorkflow(processRunner);
        String githubOrgRepo = System.getenv("GITHUB_REPOSITORY");
        String githubRefName = System.getenv("GITHUB_REF_NAME");
        String previousRefName = System.getenv("PREVIOUS_REF_NAME");
        String trainVersions = System.getenv("TRAIN_VERSIONS");
        String artifactToCheck = System.getenv("ARTIFACT_TO_CHECK");

        log.info("""
                !!!

                @@@ MICROMETER RELEASE @@@

                Processing following env variables:

                GITHUB_REPOSITORY [%s]
                GITHUB_REF_NAME [%s]
                PREVIOUS_REF_NAME [%s]
                TRAIN_VERSIONS [%s]
                ARTIFACT_TO_CHECK [%s]

                !!!
                """.formatted(githubOrgRepo, githubRefName, previousRefName, trainVersions, artifactToCheck));

        if (trainVersions != null && !trainVersions.isBlank()) {
            log.info("Will proceed with train release...");
            trainReleaseWorkflow(githubOrgRepo, artifactToCheck, postReleaseWorkflow, processRunner).run(trainVersions);
        }
        else {
            log.info("Will proceed with single project post release workflow...");
            postReleaseWorkflow.run(githubOrgRepo, githubRefName, previousRefName);
        }
    }

    private static ProjectTrainReleaseWorkflow trainReleaseWorkflow(String githubOrgRepo, String artifactToCheck,
            PostReleaseWorkflow postReleaseWorkflow, ProcessRunner processRunner) {
        return new ProjectTrainReleaseWorkflow(githubOrgRepo, artifactToCheck, processRunner, postReleaseWorkflow);
    }

    private static PostReleaseWorkflow newPostReleaseWorkflow(ProcessRunner processRunner) {
        return new PostReleaseWorkflow(new ChangelogGeneratorDownloader(), new ChangelogGenerator(processRunner),
                new ChangelogFetcher(processRunner), new ChangelogProcessor(processRunner),
                new ReleaseNotesUpdater(processRunner), new MilestoneUpdater(processRunner), new NotificationSender());
    }

}
