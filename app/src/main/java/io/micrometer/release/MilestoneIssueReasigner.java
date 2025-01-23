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

import io.micrometer.release.MilestoneMigrator.Milestone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;

class MilestoneIssueReasigner {

    private static final Logger log = LoggerFactory.getLogger(MilestoneIssueReasigner.class);

    private final ProcessRunner processRunner;

    private final String ghRepo;

    MilestoneIssueReasigner(ProcessRunner processRunner, String ghRepo) {
        this.processRunner = processRunner;
        this.ghRepo = ghRepo;
    }

    void reassignIssues(Milestone concreteMilestone, String refName, List<Integer> closedIssues,
            List<Integer> openIssues) {
        log.info("Moving closed issues to concrete milestone {}", concreteMilestone.number());
        reassignIssues(closedIssues, concreteMilestone.number());

        // Create next milestone if GA version
        if (!refName.contains("-")) {
            String[] parts = refName.split("\\.");
            String nextVersion = parts[0] + "." + parts[1] + "." + (Integer.parseInt(parts[2]) + 1);
            log.info("Version is GA will create a new milestone {}", nextVersion);

            int newMilestoneNumber = createMilestone(nextVersion);

            // Move open issues to next milestone
            reassignIssues(openIssues, newMilestoneNumber);
        }
        else {
            log.info("Version {} is not GA, won't create a new milestone", refName);
        }
    }

    private void reassignIssues(List<Integer> issueNumbers, int milestoneNumber) {
        for (Integer issueNumber : issueNumbers) {
            processRunner.run("gh", "api", String.format("/repos/%s/issues/%d", ghRepo, issueNumber), "-X", "PATCH",
                    "-f", String.format("milestone=%d", milestoneNumber));
        }
    }

    private int createMilestone(String version) {
        LocalDate dueDate = ReleaseDateCalculator.calculateDueDate(version);

        List<String> lines = processRunner.run("gh", "api", "/repos/" + ghRepo + "/milestones", "-X", "POST", "-f",
                "title=" + version, "-f", "due_on=" + dueDate.toString() + "T17:00:00Z" // Using
        // 17:00
        // UTC
        // for
        // consistency
        );

        if (lines.isEmpty()) {
            throw new IllegalStateException("Could not create milestone " + version);
        }

        String line = lines.get(0);
        return Integer.parseInt(line.split("\"number\":")[1].split(",")[0].trim());
    }

}
