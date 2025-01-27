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

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class MilestoneMigratorTests {

    private static final String GH_REPO = "micrometer-metrics/build-test";

    ProcessRunner runner = mock();

    MilestoneIssueReassigner reasigner = mock();

    MilestoneMigrator migrator = new MilestoneMigrator(runner, GH_REPO, reasigner);

    @Test
    void should_throw_exception_when_no_milestone_found() {

    }

    @Test
    void should_throw_exception_when_no_milestone_found_in_github_response() {

    }

    @Test
    void should_throw_exception_when_no_generic_milestone_found() {

    }

    @Test
    void should_throw_exception_when_no_generic_milestone_found_in_github_response() {

    }

    @Test
    void should_throw_exception_when_no_milestone_found_when_retrieving_associated_issues() {
    }

    @Test
    void should_reassign_issues_from_generic_milestone_to_concrete_one() {
    }

}
