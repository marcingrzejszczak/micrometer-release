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
 * Always second Monday of the next month
 */
class ReleaseDateCalculator {

    static LocalDate calculateDueDate(LocalDate now) {
        // Go to first day of the next month
        LocalDate nextMonth = now.withDayOfMonth(1).plusMonths(1);

        // Find first Monday
        LocalDate firstMonday = nextMonth.plusDays((8 - nextMonth.getDayOfWeek().getValue()) % 7);

        // Add a week to get to second Monday
        return firstMonday.plusWeeks(1);
    }

}
