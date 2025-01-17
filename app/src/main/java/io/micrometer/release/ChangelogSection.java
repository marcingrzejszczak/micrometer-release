package io.micrometer.release;

import java.util.*;
import java.util.regex.Pattern;

class ChangelogSection {

    private static final String CONTRIBUTORS_TEXT = "Thank you to all the contributors who worked on this release:";
    private static final Pattern GITHUB_HANDLE = Pattern.compile("@([a-zA-Z0-9](?:-?[a-zA-Z0-9])*[a-zA-Z0-9])");

    enum Section {

        FEATURES(":star: New Features"), BUGS(":lady_beetle: Bug Fixes"), DOCUMENTATION(
            ":notebook_with_decorative_cover: Documentation"), UPGRADES(
            ":hammer: Dependency Upgrades"), CONTRIBUTORS(":heart: Contributors");

        private final String title;

        Section(String title) {
            this.title = title;
        }

        static Section fromTitle(String title) {
            return Arrays.stream(Section.values())
                .filter(section -> section.title.equalsIgnoreCase(title)).findFirst().orElseThrow(
                    () -> new IllegalArgumentException("Title is not supported [" + title + "]"));
        }
    }

    private final Section section;
    private final Set<String> entries = new TreeSet<>();

    ChangelogSection(Section section) {
        this.section = section;
    }

    void addEntry(String entry) {
        entries.add(entry);
    }

    Section getSection() {
        return section;
    }

    String getTitle() {
        return section.title;
    }

    Set<String> getEntries() {
        return new HashSet<>(entries);
    }

    void merge(ChangelogSection other) {
        switch (other.getSection()) {
            case FEATURES:
            case BUGS:
            case DOCUMENTATION:
            case UPGRADES:
                entries.addAll(other.getEntries());
                break;
            case CONTRIBUTORS:
                // remove ChangelogSection.CONTRIBUTORS_TEXT from both
                // iterate over lines and extract all `@text-hyphens-numbers-to-first-space-or-end-line-or-comma`
                //  put them in a set
                // print CONTRIBUTORS_TEXT + new line
                //  print all entries from the set in alphabetical order
                Set<String> finalEntries = new HashSet<>()
            }
        }
    }
}
