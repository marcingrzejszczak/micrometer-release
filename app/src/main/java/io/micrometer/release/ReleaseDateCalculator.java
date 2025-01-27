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

import java.time.LocalDate;

/**
 * Our release slot on the release train is the second Monday of the month for patch
 * releases. It's a little more complicated with milestone releases - they are on the same
 * day when they happen, but after M3 is RC1 and after that is of course the GA version.
 * After the GA version there's a month with only patch releases.
 */
class ReleaseDateCalculator {

    static LocalDate calculateDueDate(LocalDate now, String version) {
        // Parse version
        String[] parts = version.split("\\.");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Version must be in format major.minor.patch[-qualifier]");
        }

        // Check if this is a qualified version (M1, RC1, etc)
        boolean isQualified = version.contains("-");
        String qualifier = isQualified ? version.substring(version.indexOf("-") + 1) : null;

        LocalDate dueDate;

        if (isQualified) {
            // Handle milestone and RC releases
            if (qualifier.startsWith("M") || qualifier.startsWith("RC")) {
                // Milestone releases are on second Monday
                // RC follows M3, on second Monday
                dueDate = getSecondMonday(now);
            }
            else {
                throw new IllegalArgumentException("Unknown qualifier: " + qualifier);
            }
        }
        else {
            // For GA or patch releases
            String patch = parts[2];
            if ("0".equals(patch)) {
                // GA release - second Monday
                dueDate = getSecondMonday(now);
            }
            else {
                // Patch release - second Monday of next month if we're past this month's
                // second Monday
                dueDate = getSecondMonday(now);
                if (now.isAfter(dueDate) || now.isEqual(dueDate)) {
                    dueDate = getSecondMonday(now.plusMonths(1));
                }
            }
        }

        return dueDate;
    }

    private static LocalDate getSecondMonday(LocalDate date) {
        // Go to first day of the month
        LocalDate firstDay = date.withDayOfMonth(1);
        if (firstDay.isBefore(date)) {
            firstDay = date.plusMonths(1).withDayOfMonth(1);
        }
        // Find first Monday
        LocalDate firstMonday = firstDay.plusDays((8 - firstDay.getDayOfWeek().getValue()) % 7);

        // Add a week to get to second Monday
        return firstMonday.plusWeeks(1);
    }

}
